package art.arcane.witchery.ritual;

import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.event.WitcheryBlockBreak;
import art.arcane.witchery.event.WitcheryBlockPlacement;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class LandscapeRites {
    private static final int FOREST_RADIUS = 20;
    private static final int FOREST_HEIGHT = 8;
    private static final Set<Block> LEGACY_VANILLA_SAPLINGS = Set.of(Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING,
            Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING);
    private static final int VOLCANO_RADIUS = 8;
    private static final List<Direction> SHAFT_SIDES = List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
    private static final List<Direction> LEGACY_VENT_SIDES = List.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH);
    private static final List<BlockPos> RESERVOIR_NEIGHBORS = List.of(
            new BlockPos(0, -1, 0), new BlockPos(-1, 0, 0), new BlockPos(1, -1, 0),
            new BlockPos(0, 0, -1), new BlockPos(0, 0, 1), new BlockPos(0, 1, 1));
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private LandscapeRites() {}

    public enum Species {
        OAK(Blocks.OAK_SAPLING),
        SPRUCE(Blocks.SPRUCE_SAPLING),
        BIRCH(Blocks.BIRCH_SAPLING),
        JUNGLE(Blocks.JUNGLE_SAPLING),
        ROWAN(WitchWoodType.ROWAN),
        ALDER(WitchWoodType.ALDER),
        HAWTHORN(WitchWoodType.HAWTHORN);

        private static final Species[] VALUES = values();
        @Nullable private final Block vanillaSapling;
        @Nullable private final WitchWoodType witchWood;

        Species(Block sapling) {
            vanillaSapling = sapling;
            witchWood = null;
        }

        Species(WitchWoodType type) {
            vanillaSapling = null;
            witchWood = type;
        }

        BlockState saplingState() {
            if (vanillaSapling != null) return vanillaSapling.defaultBlockState();
            return WitcheryBlocks.getRequired("witchsapling").get().defaultBlockState().setValue(WitchWoodType.PROPERTY, witchWood);
        }

        @Nullable
        public static Species forStack(ItemStack stack) {
            if (stack.is(WitcheryBlocks.getRequired("witchsapling").get().asItem())) {
                return VALUES[4 + WitchWoodType.fromStack(stack).ordinal()];
            }
            return Arrays.stream(VALUES).filter(species -> species.vanillaSapling != null
                    && stack.is(species.vanillaSapling.asItem())).findFirst().orElse(null);
        }

        @Nullable
        public static Species forLegacyRite(int id) {
            return id >= 70 && id <= 76 ? VALUES[id - 70] : null;
        }
    }

    private static final int EXPANSION_TICKS = 5;
    private static final int HELL_SPAWN_STAGES = 40 / EXPANSION_TICKS;

    public static int hellSpawnOffset(int completedStage, int glyphPhase) {
        // Each stage advances the glyph clock by five ticks. Capture which stage shares its 40-tick boundary.
        return Math.floorMod(completedStage - glyphPhase / EXPANSION_TICKS, HELL_SPAWN_STAGES);
    }

    public record Progress(ItemRiteRecipe.Action action, @Nullable Species species, BlockPos target, int stage,
                           @Nullable EarthRites.Fissure fissure, boolean followsHeart, int hellSpawnOffset) {
        public int interval() {
            return switch (action) {
                case RAISE_VOLCANO, RAISE_PORTABLE_VOLCANO -> 15;
                case ICE_SHELL, RAISE_EARTH, RAISE_EARTH_MEDIUM, RAISE_EARTH_LARGE, FERTILITY -> 5;
                case BLIGHT, AREA_BLINDNESS, HELL_ON_EARTH -> EXPANSION_TICKS;
                case RAIN_TOADS -> 30;
                case CALL_BEASTS -> 60;
                default -> 20;
            };
        }

        public int lastStage(int coven) {
            return switch (action) {
                case RAISE_VOLCANO, RAISE_PORTABLE_VOLCANO -> 2 * volcanoHeight(coven);
                case ICE_SHELL -> IceShell.radius(coven) - 4;
                case CLIMATE_CHANGE -> ClimateChange.CAST_STAGE;
                case PART_EARTH -> EarthRites.FISSURE_PULSES;
                case RAISE_EARTH, RAISE_EARTH_MEDIUM, RAISE_EARTH_LARGE -> EarthRites.RISE_PULSES;
                case FERTILITY -> GrowthRites.fertilityRadius(coven) - 3;
                case NATURES_POWER -> 150 + 5 * coven;
                case CALL_BEASTS -> 250;
                case BLIGHT, AREA_BLINDNESS -> AreaCurses.curseRadius(coven) - 3;
                case HELL_ON_EARTH -> 251;
                case RAIN_TOADS -> RainOfToads.LAST_STAGE;
                default -> 60 + 5 * coven;
            };
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.putString("Action", action.name());
            if (species != null) tag.putString("Species", species.name());
            tag.putLong("Target", target.asLong());
            tag.putInt("Stage", stage);
            if (fissure != null) {
                tag.putLong("FissureSeed", fissure.seed());
                tag.putInt("FissureHeading", fissure.heading());
                if (fissure.usesLegacyRandom()) tag.putBoolean("FissureUsesLegacyRandom", true);
            }
            if (followsHeart) tag.putBoolean("FollowsHeart", true);
            if (action == ItemRiteRecipe.Action.HELL_ON_EARTH) tag.putInt("HellSpawnOffset", hellSpawnOffset);
            return tag;
        }

        @Nullable
        public static Progress load(CompoundTag tag, int coven) {
            if (!tag.contains("Target", Tag.TAG_LONG)) return null;
            Species species = null;
            if (tag.contains("Species")) {
                try {
                    species = Species.valueOf(tag.getString("Species"));
                } catch (IllegalArgumentException invalid) {
                    return null;
                }
            }
            ItemRiteRecipe.Action action = species == null ? ItemRiteRecipe.Action.RAISE_VOLCANO : ItemRiteRecipe.Action.FORESTATION;
            if (tag.contains("Action", Tag.TAG_STRING)) {
                try {
                    action = ItemRiteRecipe.Action.valueOf(tag.getString("Action"));
                } catch (IllegalArgumentException invalid) {
                    return null;
                }
            }
            if (!action.usesLandscapeUpkeep() || (action == ItemRiteRecipe.Action.FORESTATION) != (species != null)) return null;
            EarthRites.Fissure fissure = action == ItemRiteRecipe.Action.PART_EARTH
                    ? new EarthRites.Fissure(tag.getLong("FissureSeed"), tag.getInt("FissureHeading"),
                            tag.getBoolean("FissureUsesLegacyRandom")) : null;
            var progress = new Progress(action, species, BlockPos.of(tag.getLong("Target")), tag.getInt("Stage"), fissure,
                    tag.getBoolean("FollowsHeart"), Math.floorMod(tag.getInt("HellSpawnOffset"), HELL_SPAWN_STAGES));
            return progress.stage() > 0 && progress.stage() < progress.lastStage(coven) ? progress : null;
        }
    }

    public static boolean canReach(ServerLevel level, BlockPos heart, BlockPos target, int coven) {
        int distance = 50 + 50 * coven;
        return level.isInWorldBounds(target) && level.getWorldBorder().isWithinBounds(target)
                && heart.distSqr(target) <= distance * distance;
    }

    public static boolean hasLavaBelow(ServerLevel level, BlockPos target) {
        for (BlockPos.MutableBlockPos point = target.mutable(); point.getY() >= level.getMinBuildHeight(); point.move(Direction.DOWN)) {
            if (level.getBlockState(point).is(Blocks.BEDROCK)) return false;
            if (!lavaSource(level, point)) continue;
            int neighbors = 0;
            for (BlockPos offset : RESERVOIR_NEIGHBORS) {
                if (lavaSource(level, point.offset(offset)) && ++neighbors == 2) return true;
            }
        }
        return false;
    }

    private static boolean lavaSource(ServerLevel level, BlockPos pos) {
        var fluid = level.getFluidState(pos);
        return fluid.is(FluidTags.LAVA) && fluid.isSource();
    }

    @Nullable
    public static Progress pulse(ServerLevel level, BlockPos heart, Progress progress, int coven,
                                 @Nullable ServerPlayer caster, Consumer<Runnable> terrainChanges) {
        return pulse(level, heart, progress, coven, caster, terrainChanges, false);
    }

    @Nullable
    public static Progress pulse(ServerLevel level, BlockPos heart, Progress progress, int coven,
                                 @Nullable ServerPlayer caster, Consumer<Runnable> terrainChanges, boolean legacy) {
        int stage = progress.stage() + 1;
        switch (progress.action()) {
            case FORESTATION -> {
                if (stage < progress.lastStage(coven)) {
                    forestPulse(level, progress.target(), progress.species(), coven, caster, legacy);
                }
            }
            case RAISE_VOLCANO, RAISE_PORTABLE_VOLCANO ->
                    volcanoPulse(level, heart, progress.target(), stage, coven, caster, legacy);
            case ICE_SHELL -> IceShell.pulse(level, progress.target(), stage, coven, caster);
            case CLIMATE_CHANGE -> {
                if (!ClimateChange.pulse(level, progress.target(), stage, coven, caster, legacy)) return null;
            }
            case PART_EARTH -> progress.fissure().cut(level, progress.target(), stage, coven, caster);
            case RAISE_EARTH, RAISE_EARTH_MEDIUM, RAISE_EARTH_LARGE -> {
                BlockPos center = progress.followsHeart() ? heart : progress.target();
                terrainChanges.accept(() -> EarthRites.raise(level, center, progress.action().raisedEarthRadius(), stage, coven, caster));
            }
            case FERTILITY -> GrowthRites.fertility(level, progress.target(), stage + 3, caster);
            case NATURES_POWER -> {
                if (stage < progress.lastStage(coven)) GrowthRites.naturesPower(level, progress.target(), coven);
            }
            case CALL_BEASTS -> CallOfTheBeasts.pulse(level, progress.target(), stage);
            case BLIGHT, AREA_BLINDNESS, HELL_ON_EARTH -> {
                boolean summon = (stage - 1) % HELL_SPAWN_STAGES == progress.hellSpawnOffset();
                if (!AreaCurses.pulse(level, heart, progress.target(), stage, coven, progress.action(), caster, summon, legacy)) {
                    return null;
                }
            }
            case RAIN_TOADS -> RainOfToads.shower(level, progress.target());
            default -> throw new IllegalStateException("Unexpected landscape rite " + progress.action());
        }
        return stage >= progress.lastStage(coven) ? null
                : new Progress(progress.action(), progress.species(), progress.target(), stage, progress.fissure(),
                        progress.followsHeart(), progress.hellSpawnOffset());
    }

    private static void forestPulse(ServerLevel level, BlockPos origin, Species species, int coven,
                                    @Nullable ServerPlayer caster, boolean legacy) {
        RandomSource random = level.getRandom();
        int radius = FOREST_RADIUS + 2 * coven;
        BlockPos column = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            int x = random.nextInt(2 * radius) - radius;
            int z = random.nextInt(2 * radius) - radius;
            if (x * x + z * z <= (radius + 1) * (radius + 1)) {
                column = origin.offset(x, 0, z);
                break;
            }
        }
        if (column == null) return;
        level.levelEvent(LevelEvent.PARTICLES_PLANT_GROWTH, origin.above(), 0);
        BlockPos site = plantingSite(level, column);
        if (site == null) return;
        if (legacy) level.levelEvent(LevelEvent.PARTICLES_PLANT_GROWTH, site, 0);
        BlockState sapling = species.saplingState();
        if (sapling.canSurvive(level, site)) {
            if (!replace(level, site, sapling, caster)) return;
        } else if (!legacy) {
            return;
        }
        if (!legacy) level.levelEvent(LevelEvent.PARTICLES_PLANT_GROWTH, site, 0);
        ServerPlayer grower = legacy || caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
        for (int attempt = 0; attempt < 11; attempt++) {
            // The original invokes Forge's bonemeal hook once even when the soil cannot grow a sapling.
            BlockState current = level.getBlockState(site);
            boolean stillSapling = legacy ? isLegacySapling(current) : current.is(sapling.getBlock());
            if (!stillSapling && (!legacy || attempt != 0)) break;
            BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, site, grower);
        }
    }

    static boolean isLegacySapling(BlockState state) {
        return LEGACY_VANILLA_SAPLINGS.contains(state.getBlock()) || state.is(WitcheryBlocks.getRequired("witchsapling").get());
    }

    @Nullable
    private static BlockPos plantingSite(ServerLevel level, BlockPos column) {
        if (level.isEmptyBlock(column) && level.getBlockState(column.below()).blocksMotion()) return column;
        for (int offset = 1; offset < FOREST_HEIGHT; offset++) {
            BlockPos above = column.above(offset);
            if (supportsPlanting(level, above)) return above;
            BlockPos below = column.below(offset);
            if (supportsPlanting(level, below)) return below;
        }
        return null;
    }

    private static boolean supportsPlanting(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.canBeReplaced() && state.getFluidState().isEmpty() && level.getBlockState(pos.below()).blocksMotion();
    }

    private static int volcanoHeight(int coven) {
        return 8 + 4 * coven;
    }

    private static void volcanoPulse(ServerLevel level, BlockPos heart, BlockPos target, int stage, int coven,
                                     @Nullable ServerPlayer caster, boolean legacy) {
        int height = volcanoHeight(coven);
        int radius = VOLCANO_RADIUS + 2 * coven;
        if (stage <= height) {
            for (int layer = 1; layer <= stage; layer++) {
                float reach = radius - (float) (height - stage - 1 + layer) * radius / height;
                int width = Math.max(1, (int) Math.ceil(reach));
                BlockPos center = target.above(layer - 1);
                fillLayer(level, center, width, layer < 3, true, caster, legacy);
                if (legacy) {
                    if (stage == height) fillVolcanoFoundation(level, target, radius, layer < 3, caster, true);
                    liftVolcanoLayer(level, center, reach);
                } else {
                    liftBuriedEntities(level, center, width);
                }
            }
            if (!legacy && stage == height) fillVolcanoFoundation(level, target, radius, true, caster, false);
        } else if (stage < 2 * height - 1) {
            replace(level, target.above(), STONE, caster);
            replace(level, target.above(stage - height), Blocks.LAVA.defaultBlockState(), caster);
        } else if (stage == 2 * height - 1) {
            BlockPos summit = target.above(height - 1);
            replace(level, summit, Blocks.LAVA.defaultBlockState(), caster);
            replace(level, target.above(), Blocks.LAVA.defaultBlockState(), caster);
            int vent = level.getRandom().nextInt(8);
            var sides = legacy ? LEGACY_VENT_SIDES : SHAFT_SIDES;
            if (vent < sides.size()) replace(level, summit.relative(sides.get(vent)), AIR, caster);
        } else {
            openShaft(level, heart, target, caster);
        }
    }

    private static void fillVolcanoFoundation(ServerLevel level, BlockPos target, int radius, boolean grassEdge,
                                              @Nullable ServerPlayer caster, boolean legacy) {
        for (int depth = 1; depth <= 4; depth++) {
            fillLayer(level, target.below(depth), Math.max(radius - depth + 1, 2), grassEdge, false, caster, legacy);
        }
    }

    private static void fillLayer(ServerLevel level, BlockPos center, int radius, boolean grassEdge,
                                  boolean replaceExisting, @Nullable ServerPlayer caster, boolean legacy) {
        int error = 1 - radius;
        for (int x = radius, z = 0; x >= z; z++) {
            drawSpan(level, center, x, z, radius, grassEdge, replaceExisting, caster, legacy);
            // The original redraws overlapping spans and takes a new set of edge rolls each time.
            if (legacy) drawSpan(level, center, z, x, radius, grassEdge, replaceExisting, caster, true);
            if (legacy || z != 0) drawSpan(level, center, x, -z, radius, grassEdge, replaceExisting, caster, legacy);
            if (!legacy && x != z) drawSpan(level, center, z, x, radius, grassEdge, replaceExisting, caster, false);
            if (legacy || x != z) {
                drawSpan(level, center, z, -x, radius, grassEdge, replaceExisting, caster, legacy);
            }
            if (error < 0) error += 2 * (z + 1) + 1;
            else error += 2 * (z - --x + 2);
        }
    }

    private static void drawSpan(ServerLevel level, BlockPos center, int halfWidth, int z, int radius, boolean grassEdge,
                                 boolean replaceExisting, @Nullable ServerPlayer caster, boolean legacy) {
        RandomSource random = level.getRandom();
        int min = -halfWidth + (radius > 1 && random.nextInt(5) == 0 ? 1 : 0);
        int max = halfWidth - (radius > 1 && random.nextInt(5) == 0 ? 1 : 0);
        for (int x = min; x <= max; x++) {
            if (x == 0 && z == 0) continue;
            BlockPos pos = center.offset(x, 0, z);
            BlockState state = level.getBlockState(pos);
            if ((!replaceExisting && !state.isAir()) || (legacy && protectedTerrain(level, pos, state))) continue;
            boolean edge = grassEdge && (x == min || x == max || Math.abs(z) == radius);
            replace(level, pos, edge && random.nextInt(5) != 0 ? Blocks.GRASS_BLOCK.defaultBlockState() : STONE, caster);
        }
    }

    private static void liftVolcanoLayer(ServerLevel level, BlockPos center, float radius) {
        float x = center.getX();
        int y = center.getY() + 1;
        float z = center.getZ();
        var bounds = new AABB(x - radius, y, z - radius, x + radius, y, z + radius);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, bounds)) {
            if (entity.distanceToSqr(x, y, z) > (double) radius * radius) continue;
            // Keep the original horizontal lookup and find the block at the feet below modern Y zero.
            var feet = new BlockPos((int) entity.getX(), entity.getBlockY(), (int) entity.getZ());
            if (level.getBlockState(feet).blocksMotion()) {
                entity.teleportTo(entity.getX(), entity.getY() + 1, entity.getZ());
            }
        }
    }

    private static void liftBuriedEntities(ServerLevel level, BlockPos center, int radius) {
        var bounds = new AABB(center).inflate(radius, 0.5, radius);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, bounds)) {
            var raised = entity.position().add(0, 1, 0);
            if (level.getBlockState(entity.blockPosition()).blocksMotion() && WitcheryTeleport.canArrive(entity, level, raised)) {
                WitcheryTeleport.move(entity, level, raised);
            }
        }
    }

    private static void openShaft(ServerLevel level, BlockPos heart, BlockPos target, @Nullable ServerPlayer caster) {
        var pos = target.mutable();
        for (; pos.getY() >= level.getMinBuildHeight() && !isLava(level, pos); pos.move(Direction.DOWN)) {
            if (pos.equals(heart)) level.removeBlock(heart, false);
            else if (!replace(level, pos, AIR, caster)) return;
        }
        while (pos.getY() >= level.getMinBuildHeight() && isLava(level, pos)) {
            if (!replace(level, pos, AIR, caster)) return;
            for (Direction side : SHAFT_SIDES) {
                if (isLava(level, pos.relative(side))) replace(level, pos.relative(side), AIR, caster);
            }
            pos.move(Direction.DOWN);
        }
    }

    private static boolean isLava(ServerLevel level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.LAVA);
    }

    static boolean replace(ServerLevel level, BlockPos pos, BlockState replacement, @Nullable ServerPlayer caster) {
        if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)) return false;
        BlockState existing = level.getBlockState(pos);
        if (existing.equals(replacement)) return true;
        if (protectedTerrain(level, pos, existing)) return false;
        if (caster != null && (!level.mayInteract(caster, pos)
                || !existing.isAir() && WitcheryBlockBreak.isCanceled(level, pos, existing, caster))) {
            return false;
        }
        return replacement.isAir() ? level.setBlockAndUpdate(pos, replacement)
                : WitcheryBlockPlacement.place(level, pos, replacement, caster, Direction.UP);
    }

    private static boolean protectedTerrain(ServerLevel level, BlockPos pos, BlockState state) {
        return state.hasBlockEntity() || state.getDestroySpeed(level, pos) < 0;
    }

    public static void playRiteStart(ServerLevel level, BlockPos target) {
        level.sendParticles(ParticleTypes.PORTAL, target.getX() + 0.5, target.getY() + 0.35, target.getZ() + 0.5,
                16, 0.35, 0.4, 0.35, 0.08);
        level.playSound(null, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1);
    }
}
