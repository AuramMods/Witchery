package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
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
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.VineBlock;
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
        level.levelEvent(2005, heart.above(), 0);
        if (!level.hasChunkAt(column)) return;
        BlockPos surface = exposedSurface(level, column);
        if (surface == null) return;
        level.levelEvent(2005, surface.above(), 0);

        // The midpoint circle of radius three has rows with half-widths 1, 2, 3, 3, 3, 2, 1.
        ServerPlayer grower = FakePlayerFactory.getMinecraft(level);
        var replaceableSoil = replaceableSoil();
        for (int z = -3; z <= 3; z++) {
            int halfWidth = Math.min(3, 4 - Math.abs(z));
            int left = -halfWidth + (random.nextInt(5) == 0 ? 1 : 0);
            int right = halfWidth - (random.nextInt(5) == 0 ? 1 : 0);
            for (int x = left; x <= right; x++) naturalize(level, surface.offset(x, 0, z), grower, replaceableSoil);
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
                if (level.getBlockState(below).blocksMotion() && (cover.isAir() || cover.is(Blocks.SNOW))) return below;
            }
        }
        return null;
    }

    private static void naturalize(ServerLevel level, BlockPos ground, ServerPlayer grower,
                                   Predicate<BlockState> replaceableSoil) {
        if (!level.hasChunkAt(ground) || !level.getWorldBorder().isWithinBounds(ground)) return;
        BlockPos top = ground.above();
        BlockState cover = level.getBlockState(top);
        if (cover.blocksMotion()) return;
        BlockState soil = level.getBlockState(ground);
        RandomSource random = level.getRandom();
        boolean vinesOrChalk = cover.getBlock() instanceof VineBlock || cover.getBlock() instanceof RitualGlyphBlock
                || cover.getBlock() instanceof RiteHeartBlock
                || cover.is(Blocks.GRASS) || cover.is(Blocks.FERN) || cover.is(Blocks.TALL_GRASS) || cover.is(Blocks.LARGE_FERN);
        if (replaceableSoil.test(soil) && random.nextInt(8) != 0) {
            BlockState replacement = !vinesOrChalk && random.nextDouble() <= (adjacentWater(level, ground) ? 0.7 : 0.02)
                    ? Blocks.WATER.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
            LandscapeRites.replace(level, ground, replacement, grower);
        }
        if (!soil.isAir() && !soil.is(BlockTags.LEAVES) && (cover.isAir() || cover.is(Blocks.SNOW)) && random.nextInt(4) == 0) {
            BlockState plant = randomPlant(random);
            if (plant.canSurvive(level, top)) LandscapeRites.replace(level, top, plant, grower);
        }
        if (random.nextInt(3) == 0 && level.mayInteract(grower, top)) {
            BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, top, grower);
            for (int attempt = 0; attempt < 8 && level.getBlockState(top).getBlock() instanceof SaplingBlock; attempt++) {
                BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, top, grower);
            }
        }
    }

    private static boolean adjacentWater(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(direction);
            if (level.hasChunkAt(neighbor) && level.getBlockState(neighbor).is(Blocks.WATER)) return true;
        }
        return false;
    }

    private static BlockState randomPlant(RandomSource random) {
        // The 1.7.10 pool weights short grass and ferns six times each.
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
