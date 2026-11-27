package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.RiteHeartBlock;
import art.arcane.witchery.block.RitualGlyphBlock;
import art.arcane.witchery.config.ConfiguredBlockState;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.migration.LegacySchematicBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

final class GrowthRites {
    private static final TagKey<Block> SOIL = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "natures_power_replaceable"));
    private static final LandscapeRites.Species[] TREES = LandscapeRites.Species.values();
    private static final int PATCH_RADIUS = 3;

    private GrowthRites() {}

    static int fertilityRadius(int coven) {
        return 50 + 2 * coven;
    }

    static void fertility(ServerLevel level, BlockPos heart, int radius, @Nullable ServerPlayer caster) {
        ServerPlayer grower = caster != null && caster.level() == level ? caster : null;
        boolean enhanced = grower != null && FamiliarBinding.hasCurseMastery(grower);
        Vec3 center = Vec3.atCenterOf(heart);
        for (ServerPlayer player : level.players()) {
            if (!ExpandingWave.contains(player, center, radius)) continue;
            player.removeEffect(MobEffects.CONFUSION);
            player.removeEffect(MobEffects.BLINDNESS);
            player.removeEffect(MobEffects.POISON);
            if (enhanced) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 2400));
            }
        }
        AABB bounds = new AABB(heart).inflate(radius);
        for (ZombieVillager zombie : level.getEntitiesOfClass(ZombieVillager.class, bounds,
                mob -> mob.isAlive() && ExpandingWave.contains(mob, center, radius))) {
            if (ForgeEventFactory.canLivingConvert(zombie, EntityType.VILLAGER, delay -> {}) && zombie.isAlive()) {
                zombie.finishConversion(level);
            }
        }

        ExpandingWave.visitSurface(level, heart, radius, false, pos -> {
            if (grower != null && level.mayInteract(grower, pos)) {
                BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, grower);
            }
        });
    }

    static void naturesPower(ServerLevel level, BlockPos heart, int coven) {
        int reach = 14 + 2 * coven;
        RandomSource random = level.getRandom();
        BlockPos column = heart.offset(random.nextInt(2 * reach) - reach, -1, random.nextInt(2 * reach) - reach);
        level.levelEvent(LevelEvent.PARTICLES_PLANT_GROWTH, heart.above(), 0);
        if (!level.getWorldBorder().isWithinBounds(column)) return;
        BlockPos surface = exposedSurface(level, column);
        if (surface == null) return;
        level.levelEvent(LevelEvent.PARTICLES_PLANT_GROWTH, surface.above(), 0);

        ServerPlayer grower = FakePlayerFactory.getMinecraft(level);
        var replaceableSoil = replaceableSoil();
        Set<BlockPos> unsettledSources = new HashSet<>();
        int error = 1 - PATCH_RADIUS;
        for (int x = PATCH_RADIUS, z = 0; x >= z; z++) {
            // Each pass can change the water neighbors and plants seen by the next overlapping span.
            naturalizeSpan(level, surface, x, z, grower, replaceableSoil, unsettledSources);
            naturalizeSpan(level, surface, z, x, grower, replaceableSoil, unsettledSources);
            naturalizeSpan(level, surface, x, -z, grower, replaceableSoil, unsettledSources);
            naturalizeSpan(level, surface, z, -x, grower, replaceableSoil, unsettledSources);
            if (error < 0) error += 2 * (z + 1) + 1;
            else error += 2 * (z - --x + 2);
        }
    }

    private static void naturalizeSpan(ServerLevel level, BlockPos center, int halfWidth, int z, ServerPlayer grower,
                                       Predicate<BlockState> replaceableSoil, Set<BlockPos> unsettledSources) {
        RandomSource random = level.getRandom();
        int left = -halfWidth + (random.nextInt(5) == 0 ? 1 : 0);
        int right = halfWidth - (random.nextInt(5) == 0 ? 1 : 0);
        for (int x = left; x <= right; x++) {
            naturalize(level, center.offset(x, 0, z), grower, replaceableSoil, unsettledSources);
        }
    }

    static Predicate<BlockState> replaceableSoil() {
        Set<Block> configuredBlocks = new HashSet<>();
        Set<BlockState> configuredStates = new HashSet<>();
        for (String value : WitcheryServerConfig.NATURES_POWER_REPLACEABLE_BLOCKS.get()) {
            String entry = value.strip();
            int separator = entry.lastIndexOf(',');
            if (separator >= 0 && !entry.contains("[") && entry.substring(separator + 1).strip().equals("32767")) {
                addLegacySoils(configuredBlocks, entry.substring(0, separator).strip());
            } else if (entry.contains("[") || separator >= 0) {
                BlockState state = ConfiguredBlockState.parse(entry);
                if (state != null) configuredStates.add(state);
            } else {
                ResourceLocation blockId = ResourceLocation.tryParse(entry);
                if (blockId != null) {
                    if (BuiltInRegistries.BLOCK.containsKey(blockId)) configuredBlocks.add(BuiltInRegistries.BLOCK.get(blockId));
                    else addLegacySoils(configuredBlocks, entry);
                }
            }
        }
        return soil -> soil.is(SOIL) || configuredBlocks.contains(soil.getBlock()) || configuredStates.contains(soil);
    }

    private static void addLegacySoils(Set<Block> soils, String blockId) {
        for (int metadata = 0; metadata < 16; metadata++) {
            BlockState state = LegacySchematicBlocks.read(blockId, metadata, 0);
            if (state != null && !state.isAir()) soils.add(state.getBlock());
        }
    }

    @Nullable
    private static BlockPos exposedSurface(ServerLevel level, BlockPos column) {
        for (int height = 0; height < 8; height++) {
            BlockPos above = column.above(height);
            if (level.getBlockState(above).blocksMotion() && level.isEmptyBlock(above.above())) return above;
            if (height > 0) {
                BlockPos below = column.below(height);
                var cover = level.getBlockState(below.above());
                if (level.getBlockState(below).blocksMotion() && (cover.isAir() || cover.is(Blocks.SNOW_BLOCK))) return below;
            }
        }
        return null;
    }

    private static void naturalize(ServerLevel level, BlockPos ground, ServerPlayer grower,
                                   Predicate<BlockState> replaceableSoil, Set<BlockPos> unsettledSources) {
        if (!level.getWorldBorder().isWithinBounds(ground)) return;
        BlockPos top = ground.above();
        BlockState cover = level.getBlockState(top);
        if (cover.blocksMotion()) return;
        BlockState soil = level.getBlockState(ground);
        RandomSource random = level.getRandom();
        boolean vineMaterial = cover.is(Blocks.VINE) || cover.is(Blocks.GRASS) || cover.is(Blocks.FERN)
                || cover.getBlock() instanceof RitualGlyphBlock || cover.getBlock() instanceof RiteHeartBlock
                || cover.getBlock() instanceof DreamCatcherBlock;
        if (replaceableSoil.test(soil) && random.nextInt(8) != 0) {
            BlockState replacement = !vineMaterial && random.nextDouble() <= (adjacentWater(level, ground, unsettledSources) ? 0.7 : 0.02)
                    ? Blocks.WATER.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
            if (placeNaturalBlock(level, ground, replacement, grower, unsettledSources)) soil = replacement;
        }
        boolean excludedLeaves = soil.is(BlockTags.LEAVES) && !soil.is(Blocks.ACACIA_LEAVES) && !soil.is(Blocks.DARK_OAK_LEAVES);
        if (!vineMaterial && !soil.isAir() && !excludedLeaves && random.nextInt(4) == 0) {
            BlockState plant = randomPlant(random);
            if (level.getBlockState(top).canBeReplaced() && plant.canSurvive(level, top)
                    && (!plant.is(Blocks.PUMPKIN) || soil.isFaceSturdy(level, ground, Direction.UP))) {
                placeNaturalBlock(level, top, plant, grower, unsettledSources);
            }
        }
        if (random.nextInt(3) == 0 && level.mayInteract(grower, top)) {
            BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, top, grower);
            for (int attempt = 0; attempt < 8 && LandscapeRites.isLegacySapling(level.getBlockState(top)); attempt++) {
                BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, top, grower);
            }
        }
    }

    private static boolean placeNaturalBlock(ServerLevel level, BlockPos pos, BlockState replacement, ServerPlayer grower,
                                             Set<BlockPos> unsettledSources) {
        BlockState previous = level.getBlockState(pos);
        if (!LandscapeRites.replace(level, pos, replacement, grower)) return false;
        if (!previous.equals(replacement)) {
            // A neighboring write switched stationary water to flowing water in 1.7.10, even at level zero.
            unsettledSources.remove(pos);
            for (Direction direction : Direction.values()) unsettledSources.add(pos.relative(direction));
        }
        return true;
    }

    private static boolean adjacentWater(ServerLevel level, BlockPos pos, Set<BlockPos> unsettledSources) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(direction);
            var state = level.getBlockState(neighbor);
            if (state.is(Blocks.WATER) && state.getFluidState().isSource() && !unsettledSources.contains(neighbor)) return true;
        }
        return false;
    }

    private static BlockState randomPlant(RandomSource random) {
        // Seven slots each produce short grass and ferns.
        int choice = random.nextInt(29);
        if (choice < TREES.length) return TREES[choice].saplingState();
        return switch (choice) {
            case 7 -> WitcheryBlocks.getRequired("embermoss").get().defaultBlockState();
            case 10 -> Blocks.BROWN_MUSHROOM.defaultBlockState();
            case 11 -> Blocks.RED_MUSHROOM.defaultBlockState();
            case 12 -> Blocks.POPPY.defaultBlockState();
            case 13 -> Blocks.DANDELION.defaultBlockState();
            case 26 -> Blocks.PUMPKIN.defaultBlockState();
            case 27 -> Blocks.MELON.defaultBlockState();
            case 28 -> WitcheryBlocks.getRequired("glintweed").get().defaultBlockState();
            default -> (choice % 2 == 0 ? Blocks.GRASS : Blocks.FERN).defaultBlockState();
        };
    }
}
