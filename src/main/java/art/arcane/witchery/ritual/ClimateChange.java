package art.arcane.witchery.ritual;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.world.BiomePainting;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ClimateChange {
    static final int CAST_STAGE = 5;
    private static final int MINIMUM_COVEN = 4;

    private ClimateChange() {}

    private enum Weather { NONE, CLEAR, RAIN }

    public enum Focus {
        // Keep each position in the original Forge dictionary even when vanilla merged two biomes.
        FOREST(Items.OAK_SAPLING, BiomeTags.IS_FOREST, Biomes.FOREST, Biomes.TAIGA, Biomes.TAIGA, Biomes.FOREST,
                Biomes.SPARSE_JUNGLE, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST,
                Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA,
                Biomes.WINDSWEPT_FOREST, Biomes.FLOWER_FOREST, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.WOODED_BADLANDS),
        PLAINS(Items.GRASS, Tags.Biomes.IS_PLAINS, Biomes.PLAINS, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU,
                Biomes.SUNFLOWER_PLAINS, Biomes.DESERT, Biomes.ERODED_BADLANDS, Biomes.BADLANDS),
        MOUNTAINS(Items.OBSIDIAN, BiomeTags.IS_MOUNTAIN, Biomes.WINDSWEPT_HILLS, Biomes.SNOWY_PLAINS,
                Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST),
        HILLS(Items.STONE, BiomeTags.IS_HILL, Biomes.WINDSWEPT_HILLS, Biomes.TAIGA, Biomes.DESERT, Biomes.JUNGLE,
                Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA,
                Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.FLOWER_FOREST, Biomes.TAIGA, Biomes.ICE_SPIKES,
                Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST,
                Biomes.DARK_FOREST, Biomes.SNOWY_TAIGA, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_SAVANNA,
                Biomes.WINDSWEPT_SAVANNA),
        SWAMP(Items.SLIME_BALL, Tags.Biomes.IS_SWAMP, Biomes.SWAMP, Biomes.SWAMP),
        WATER(Items.WATER_BUCKET, Tags.Biomes.IS_WATER, Biomes.OCEAN, Biomes.RIVER, Biomes.DEEP_OCEAN),
        DESERT(Items.CACTUS, Tags.Biomes.IS_DESERT, Biomes.DESERT),
        FROZEN(null, Tags.Biomes.IS_SNOWY, Biomes.SNOWY_PLAINS, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER),
        JUNGLE(Items.JUNGLE_SAPLING, BiomeTags.IS_JUNGLE, Biomes.JUNGLE, Biomes.JUNGLE, Biomes.SPARSE_JUNGLE),
        WASTELAND(Items.NETHERRACK, Tags.Biomes.IS_WASTELAND, Biomes.SNOWY_PLAINS),
        BEACH(Items.SAND, BiomeTags.IS_BEACH, Biomes.BEACH, Biomes.MUSHROOM_FIELDS, Biomes.STONY_SHORE, Biomes.SNOWY_BEACH),
        MUSHROOM(Items.RED_MUSHROOM, Tags.Biomes.IS_MUSHROOM, Biomes.MUSHROOM_FIELDS, Biomes.MUSHROOM_FIELDS),
        MAGICAL(Items.SKELETON_SKULL, Tags.Biomes.IS_MAGICAL);

        @Nullable private final Item item;
        private final TagKey<Biome> tag;
        private final List<ResourceKey<Biome>> preferred;

        @SafeVarargs
        Focus(@Nullable Item item, TagKey<Biome> tag, ResourceKey<Biome>... preferred) {
            this.item = item;
            this.tag = tag;
            this.preferred = List.of(preferred);
        }

        @Nullable
        static Focus of(ItemStack stack) {
            for (Focus focus : values()) {
                if (stack.getItem() == focus.item) return focus;
            }
            return stack.is(WitcheryIngredient.ICY_NEEDLE.item()) ? FROZEN : null;
        }

        public Item focusItem() {
            return item == null ? WitcheryIngredient.ICY_NEEDLE.item() : item;
        }

        public List<Holder<Biome>> candidates(Registry<Biome> biomes) {
            var ordered = new ArrayList<Holder<Biome>>();
            for (var key : preferred) biomes.getHolder(key).ifPresent(ordered::add);
            var seen = new HashSet<>(ordered);
            // These dictionary categories were empty on stock 1.7.10. Use their modern tag membership.
            boolean useModernFamily = this == WATER || this == DESERT || this == FROZEN || this == MAGICAL;
            biomes.getTag(tag).ifPresent(entries -> entries.stream()
                    .filter(entry -> !entry.is(BiomeTags.IS_NETHER) && !entry.is(BiomeTags.IS_END))
                    .filter(entry -> useModernFamily || !entry.unwrapKey().orElseThrow().location().getNamespace().equals("minecraft"))
                    .sorted(Comparator.comparing(entry -> entry.unwrapKey().orElseThrow().location()))
                    .filter(seen::add)
                    .forEachOrdered(ordered::add));
            return List.copyOf(ordered);
        }
    }

    @Nullable
    public static String failure(ServerLevel level, BlockPos center, int coven) {
        if (!WitcheryServerConfig.ALLOW_BIOME_MODIFICATION_RITUALS.get()) return "message.witchery.rite.climate_disabled";
        if (level.dimension().equals(Level.END) || level.dimension().equals(Level.NETHER)
                || level.getBiome(center).is(BiomeTags.IS_END) || level.getBiome(center).is(BiomeTags.IS_NETHER)) {
            return "message.witchery.rite.climate_dimension";
        }
        return coven < MINIMUM_COVEN ? "message.witchery.rite.climate_coven" : null;
    }

    public static void playStartCue(ServerLevel level, BlockPos center) {
        playOriginalSound(level, Vec3.atLowerCornerOf(center), SoundEvents.FIRE_EXTINGUISH);
    }

    public static void playInitialFailureCue(ServerLevel level, BlockPos center) {
        playOriginalSound(level, Vec3.atLowerCornerOf(center), SoundEvents.NOTE_BLOCK_SNARE.value());
    }

    static boolean pulse(ServerLevel level, BlockPos center, int stage, int coven,
                         @Nullable ServerPlayer caster, boolean legacy) {
        String failure = failure(level, center, coven);
        if (failure != null) return fail(level, center, caster, failure, ChatFormatting.RED, legacy);
        if (legacy) {
            WitcheryNetwork.sendClimatePulse(level, center, stage, stage >= CAST_STAGE);
        } else if (stage < CAST_STAGE) {
            level.sendParticles(ParticleTypes.INSTANT_EFFECT, center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5,
                    16, stage * 0.5, stage * 0.38, stage * 0.5, 0.12);
        } else {
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5,
                    1, 0, 0, 0, 0);
        }
        if (stage < CAST_STAGE) return true;

        Focus focus = null;
        Weather weather = Weather.NONE;
        int glowstone = 0;
        AABB fociRange = new AABB(center.getX() - 8, center.getY() - 2, center.getZ() - 8,
                center.getX() + 8, center.getY() + 2, center.getZ() + 8);
        for (ItemEntity drop : ItemRiteRecipe.droppedItems(level, fociRange, legacy, ItemEntity::isAlive)) {
            ItemStack stack = drop.getItem();
            if (stack.is(Items.GLOWSTONE_DUST)) {
                glowstone += stack.getCount();
            } else {
                Focus found = Focus.of(stack);
                if (found == null) continue;
                focus = found;
                if (found == Focus.DESERT) weather = Weather.CLEAR;
                else if (found == Focus.FROZEN) weather = Weather.RAIN;
            }
            drop.discard();
            if (legacy) {
                WitcheryNetwork.sendClimateFocus(level, drop.position());
            } else {
                level.sendParticles(ParticleTypes.INSTANT_EFFECT, drop.getX(), drop.getY(), drop.getZ(),
                        16, 0.25, 0.18, 0.25, 0.07);
                level.playSound(null, drop.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.45F, 1);
            }
        }
        if (focus == null) return fail(level, center, caster, "message.witchery.rite.climate_focus", ChatFormatting.RED, legacy);
        var choices = focus.candidates(level.registryAccess().registryOrThrow(Registries.BIOME));
        if (choices.isEmpty()) return fail(level, center, caster, "message.witchery.rite.climate_focus", ChatFormatting.DARK_RED, legacy);

        int[] widths = BiomePainting.circleWidths(16 * (coven - 3));
        Holder<Biome> biome = select(choices, glowstone, level.getRandom());
        BiomePainting.paint(level, center, widths, biome);
        if (weather == Weather.CLEAR) removeSnow(level, center, widths, caster, legacy);
        int weatherTicks = (300 + level.getRandom().nextInt(600)) * 20;
        if (weather == Weather.CLEAR && (level.isRaining() || level.isThundering())) {
            level.setWeatherParameters(0, 0, false, false);
        } else if (weather == Weather.RAIN && !level.isRaining() && !level.isThundering()) {
            level.setWeatherParameters(0, weatherTicks, true, false);
        }
        return true;
    }

    private static Holder<Biome> select(List<Holder<Biome>> choices, int glowstone, RandomSource random) {
        int index = glowstone > 0 ? Math.min(glowstone, choices.size()) - 1 : choices.size() >= 3 ? random.nextInt(3) : 0;
        return choices.get(index);
    }

    private static void removeSnow(ServerLevel level, BlockPos center, int[] widths,
                                   @Nullable ServerPlayer caster, boolean legacy) {
        int radius = widths.length - 1;
        Heightmap.Types heightmap = legacy ? Heightmap.Types.MOTION_BLOCKING : Heightmap.Types.WORLD_SURFACE;
        var top = new BlockPos.MutableBlockPos();
        for (int z = -radius; z <= radius; z++) {
            int width = widths[Math.abs(z)];
            for (int x = -width; x <= width; x++) {
                int worldX = center.getX() + x;
                int worldZ = center.getZ() + z;
                if (!level.getWorldBorder().isWithinBounds(worldX, worldZ)) continue;
                // The original checks for a full snow block at the precipitation height.
                int height = level.getHeight(heightmap, worldX, worldZ);
                top.set(worldX, legacy ? height : height - 1, worldZ);
                var state = level.getBlockState(top);
                if (state.is(Blocks.SNOW_BLOCK) || !legacy && state.is(Blocks.SNOW)) {
                    LandscapeRites.replace(level, top, Blocks.AIR.defaultBlockState(), caster);
                }
            }
        }
    }

    private static boolean fail(ServerLevel level, BlockPos center, @Nullable ServerPlayer caster,
                                String message, ChatFormatting color, boolean legacy) {
        if (legacy) {
            playInitialFailureCue(level, center);
            if (caster != null && caster.serverLevel() == level) {
                caster.sendSystemMessage(Component.translatable(message).withStyle(color));
            }
            playInitialFailureCue(level, center);
        } else {
            level.playSound(null, center, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 0.9F, 1);
            if (caster != null) caster.displayClientMessage(Component.translatable(message), true);
        }
        return false;
    }

    private static void playOriginalSound(ServerLevel level, Vec3 pos, SoundEvent sound) {
        float pitch = 0.4F / ((float) level.getRandom().nextDouble() * 0.4F + 0.8F);
        level.playSeededSound(null, pos.x, pos.y, pos.z, sound, SoundSource.BLOCKS,
                0.5F, pitch, ThreadLocalRandom.current().nextLong());
    }
}
