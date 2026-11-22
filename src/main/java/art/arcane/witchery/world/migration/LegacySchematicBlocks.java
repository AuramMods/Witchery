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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

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
                var flattened = BlockStateData.getTag((encoded & ~15) | metadata).convert(NbtOps.INSTANCE);
                var updated = DataFixers.getDataFixer().update(References.BLOCK_STATE, flattened,
                        1451, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
                if (updated.getValue() instanceof CompoundTag tag) {
                    BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag);
                    return state.getBlock() instanceof DoorBlock ? LegacyChunkUpgrade.restoreDoor(state, metadata, mate) : state;
                }
            }
        }
        var block = ForgeRegistries.BLOCKS.containsKey(name) ? ForgeRegistries.BLOCKS.getValue(name) : null;
        return block == null ? null : block.defaultBlockState();
    }
}
