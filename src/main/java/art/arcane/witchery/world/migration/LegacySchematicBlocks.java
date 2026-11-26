package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.mixin.LegacyBlockStateNamesAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.Map.entry;

public final class LegacySchematicBlocks {
    private LegacySchematicBlocks() {}

    @Nullable
    public static BlockState read(String id, int metadata, int mate) {
        ResourceLocation name = ResourceLocation.tryParse(id);
        if (name == null) return null;
        if (name.getNamespace().equals(Witchery.MODID)) {
            if (!ForgeRegistries.BLOCKS.containsKey(name)) return null;
            int saved = ForgeRegistries.BLOCKS.getValue(name) instanceof DoorBlock ? mate : 0;
            CompoundTag state = LegacyChunkUpgrade.restoreState(name, metadata, saved);
            if (state != null) return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), state);
        } else if (name.getNamespace().equals("minecraft")) {
            int encoded = LegacyBlockStateNamesAccessor.witchery$getIdsByOldName().getInt(name.toString());
            if (encoded >= 0) {
                BlockState state = readVanilla((encoded & ~15) | metadata);
                if (state != null) {
                    return state.getBlock() instanceof DoorBlock ? LegacyChunkUpgrade.restoreDoor(state, metadata, mate) : state;
                }
            }
        }
        var block = ForgeRegistries.BLOCKS.containsKey(name) ? ForgeRegistries.BLOCKS.getValue(name) : null;
        return block == null ? null : block.defaultBlockState();
    }

    public static boolean shareLegacyBlockId(Block reference, Block candidate) {
        return reference == candidate || VanillaVariants.FAMILIES.getOrDefault(reference, Set.of()).contains(candidate);
    }

    @Nullable
    private static BlockState readVanilla(int encoded) {
        var flattened = BlockStateData.getTag(encoded).convert(NbtOps.INSTANCE);
        var updated = DataFixers.getDataFixer().update(References.BLOCK_STATE, flattened,
                1451, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        return updated.getValue() instanceof CompoundTag tag
                ? NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag) : null;
    }

    private static final class VanillaVariants {
        private static final Map<Block, Set<Block>> FAMILIES = buildFamilies();

        private static Map<Block, Set<Block>> buildFamilies() {
            // Vanilla 1.7.10 families whose metadata flattened into different blocks.
            var consecutiveVariants = Map.ofEntries(
                    entry(5, 6),   // Planks
                    entry(6, 6),   // Saplings
                    entry(12, 2),  // Sand
                    entry(18, 4),  // Leaves
                    entry(24, 3),  // Sandstone
                    entry(31, 3),  // Tall grass
                    entry(35, 16), // Wool
                    entry(38, 9),  // Red flowers
                    entry(43, 16), // Double stone slabs, including the seamless bit
                    entry(44, 8),  // Stone slabs
                    entry(95, 16), // Stained glass
                    entry(97, 6),  // Infested stone
                    entry(98, 4),  // Stone bricks
                    entry(125, 6), // Double wood slabs
                    entry(126, 6), // Wood slabs
                    entry(139, 2), // Walls
                    entry(155, 3), // Quartz
                    entry(159, 16), // Stained clay
                    entry(160, 16), // Stained glass panes
                    entry(161, 2), // Acacia and dark oak leaves
                    entry(171, 16), // Carpet
                    entry(175, 6)); // Double plants
            Map<Block, Set<Block>> families = new HashMap<>();
            consecutiveVariants.forEach((id, count) -> addFamily(families, id, IntStream.range(0, count)));
            addFamily(families, 3, IntStream.of(0, 2)); // Dirt and podzol
            addFamily(families, 17, IntStream.concat(IntStream.range(0, 4), IntStream.range(12, 16))); // Logs and bark
            addFamily(families, 145, IntStream.of(0, 4, 8)); // Anvil damage
            addFamily(families, 162, IntStream.of(0, 1, 12, 13)); // Acacia and dark oak logs and bark
            return families;
        }

        private static void addFamily(Map<Block, Set<Block>> families, int id, IntStream metadata) {
            Set<Block> variants = new HashSet<>();
            metadata.forEach(value -> {
                BlockState state = readVanilla(id << 4 | value);
                if (state != null && !state.isAir()) variants.add(state.getBlock());
            });
            if (variants.size() < 2) return;
            for (Block variant : variants) {
                families.computeIfAbsent(variant, ignored -> new HashSet<>()).addAll(variants);
            }
        }
    }
}
