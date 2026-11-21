package art.arcane.witchery.ritual;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

public final class ClimateChange {
    static final int CAST_STAGE = 5;
    private static final int MINIMUM_COVEN = 4;
    private static final int QUART_WIDTH = QuartPos.fromBlock(16);
    private static final int QUART_CENTER = QuartPos.toBlock(1) / 2;

    private ClimateChange() {}

    private enum Weather { NONE, CLEAR, RAIN }

    private enum Focus {
        FOREST(Items.OAK_SAPLING, BiomeTags.IS_FOREST, Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST),
        PLAINS(Items.GRASS, Tags.Biomes.IS_PLAINS, Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS),
        MOUNTAINS(Items.OBSIDIAN, BiomeTags.IS_MOUNTAIN, Biomes.WINDSWEPT_HILLS, Biomes.JAGGED_PEAKS, Biomes.STONY_PEAKS),
        HILLS(Items.STONE, BiomeTags.IS_HILL, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WOODED_BADLANDS),
        SWAMP(Items.SLIME_BALL, Tags.Biomes.IS_SWAMP, Biomes.SWAMP, Biomes.MANGROVE_SWAMP),
        WATER(Items.WATER_BUCKET, Tags.Biomes.IS_WATER, Biomes.OCEAN, Biomes.RIVER, Biomes.DEEP_OCEAN),
        DESERT(Items.CACTUS, Tags.Biomes.IS_DESERT, Biomes.DESERT),
        FROZEN(null, Tags.Biomes.IS_SNOWY, Biomes.SNOWY_PLAINS, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER),
        JUNGLE(Items.JUNGLE_SAPLING, BiomeTags.IS_JUNGLE, Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE),
        WASTELAND(Items.NETHERRACK, Tags.Biomes.IS_WASTELAND, Biomes.BADLANDS, Biomes.ERODED_BADLANDS),
        BEACH(Items.SAND, BiomeTags.IS_BEACH, Biomes.BEACH, Biomes.SNOWY_BEACH, Biomes.STONY_SHORE),
        MUSHROOM(Items.RED_MUSHROOM, Tags.Biomes.IS_MUSHROOM, Biomes.MUSHROOM_FIELDS),
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

        List<Holder<Biome>> candidates(Registry<Biome> biomes) {
            var ordered = new LinkedHashSet<Holder<Biome>>();
            for (var key : preferred) biomes.getHolder(key).ifPresent(ordered::add);
            biomes.getTag(tag).ifPresent(entries -> entries.stream()
                    .filter(entry -> !entry.is(BiomeTags.IS_NETHER) && !entry.is(BiomeTags.IS_END))
                    .sorted(Comparator.comparing(entry -> entry.unwrapKey().orElseThrow().location()))
                    .forEachOrdered(ordered::add));
            if (ordered.isEmpty() && this == MAGICAL) biomes.getHolder(Biomes.DARK_FOREST).ifPresent(ordered::add);
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

    static boolean pulse(ServerLevel level, BlockPos center, int stage, int coven, @Nullable ServerPlayer caster) {
        String failure = failure(level, center, coven);
        if (failure != null) return fail(level, center, caster, failure);
        if (stage < CAST_STAGE) {
            level.sendParticles(ParticleTypes.INSTANT_EFFECT, center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5,
                    16, stage * 0.5, stage * 0.38, stage * 0.5, 0.12);
            return true;
        }
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5,
                1, 0, 0, 0, 0);
        Focus focus = null;
        Weather weather = Weather.NONE;
        int glowstone = 0;
        AABB fociRange = new AABB(center.getX() - 8, center.getY() - 2, center.getZ() - 8,
                center.getX() + 8, center.getY() + 2, center.getZ() + 8);
        for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, fociRange, ItemEntity::isAlive)) {
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
            level.sendParticles(ParticleTypes.INSTANT_EFFECT, drop.getX(), drop.getY(), drop.getZ(),
                    16, 0.25, 0.18, 0.25, 0.07);
            level.playSound(null, drop.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.45F, 1);
            drop.discard();
        }
        List<Holder<Biome>> choices = focus == null ? List.of() : focus.candidates(level.registryAccess().registryOrThrow(Registries.BIOME));
        if (choices.isEmpty()) return fail(level, center, caster, "message.witchery.rite.climate_focus");
        int radius = 16 * (coven - 3);
        Holder<Biome> biome = select(choices, glowstone, level.getRandom());
        repaint(level, center, radius, biome);
        if (weather == Weather.CLEAR) {
            removeSnow(level, center, radius, caster);
            if (level.isRaining() || level.isThundering()) level.setWeatherParameters(0, 0, false, false);
        } else if (weather == Weather.RAIN && !level.isRaining() && !level.isThundering()) {
            level.setWeatherParameters(0, (300 + level.getRandom().nextInt(600)) * 20, true, false);
        }
        return true;
    }

    private static Holder<Biome> select(List<Holder<Biome>> choices, int glowstone, RandomSource random) {
        int index = glowstone > 0 ? Math.min(glowstone, choices.size()) - 1 : choices.size() >= 3 ? random.nextInt(3) : 0;
        return choices.get(index);
    }

    private static void repaint(ServerLevel level, BlockPos center, int radius, Holder<Biome> biome) {
        List<ChunkAccess> changed = new ArrayList<>();
        var chunkSource = level.getChunkSource();
        var sampler = chunkSource.randomState().sampler();
        int minChunkX = SectionPos.blockToSectionCoord(center.getX() - radius);
        int maxChunkX = SectionPos.blockToSectionCoord(center.getX() + radius);
        int minChunkZ = SectionPos.blockToSectionCoord(center.getZ() - radius);
        int maxChunkZ = SectionPos.blockToSectionCoord(center.getZ() + radius);
        for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                var quartColumns = quartColumns(level, center, radius, chunkX, chunkZ);
                if (quartColumns.isEmpty()) continue;
                ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
                chunk.fillBiomesFromNoise((x, y, z, ignored) -> quartColumns.get(columnIndex(x, z))
                        ? biome : chunk.getNoiseBiome(x, y, z), sampler);
                chunk.setUnsaved(true);
                changed.add(chunk);
            }
        }
        chunkSource.chunkMap.resendBiomesForChunks(changed);
    }

    private static BitSet quartColumns(ServerLevel level, BlockPos center, int radius, int chunkX, int chunkZ) {
        BitSet result = new BitSet(QUART_WIDTH * QUART_WIDTH);
        for (int qz = 0; qz < QUART_WIDTH; qz++) {
            for (int qx = 0; qx < QUART_WIDTH; qx++) {
                int x = SectionPos.sectionToBlockCoord(chunkX) + QuartPos.toBlock(qx) + QUART_CENTER;
                int z = SectionPos.sectionToBlockCoord(chunkZ) + QuartPos.toBlock(qz) + QUART_CENTER;
                if (inCircle(center, radius, x, z) && level.getWorldBorder().isWithinBounds(x, z)) {
                    result.set(columnIndex(qx, qz));
                }
            }
        }
        return result;
    }

    private static int columnIndex(int quartX, int quartZ) {
        return Math.floorMod(quartZ, QUART_WIDTH) * QUART_WIDTH + Math.floorMod(quartX, QUART_WIDTH);
    }

    private static boolean inCircle(BlockPos center, int radius, int x, int z) {
        int dx = x - center.getX();
        int dz = z - center.getZ();
        return dx * dx + dz * dz <= radius * radius;
    }

    private static void removeSnow(ServerLevel level, BlockPos center, int radius, @Nullable ServerPlayer caster) {
        var top = new BlockPos.MutableBlockPos();
        for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
            for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
                if (!inCircle(center, radius, x, z) || !level.getWorldBorder().isWithinBounds(x, z)) continue;
                top.set(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1, z);
                var state = level.getBlockState(top);
                if (state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW)) {
                    LandscapeRites.replace(level, top, Blocks.AIR.defaultBlockState(), caster);
                }
            }
        }
    }

    private static boolean fail(ServerLevel level, BlockPos center, @Nullable ServerPlayer caster, String message) {
        level.playSound(null, center, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 0.9F, 1);
        if (caster != null) caster.displayClientMessage(Component.translatable(message), true);
        return false;
    }
}
