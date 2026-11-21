package art.arcane.witchery.block.entity;

import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SlurpBlockEntity extends BlockEntity {
    private BlockState savedFluid = Blocks.AIR.defaultBlockState();
    private int remainingTicks;

    public SlurpBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.SLURP.get(), pos, state);
    }

    public void hold(BlockState fluid, int duration) {
        savedFluid = fluid;
        remainingTicks = duration;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SlurpBlockEntity hold) {
        if (hold.remainingTicks <= 1) {
            level.setBlockAndUpdate(pos, hold.savedFluid);
        } else {
            hold.remainingTicks--;
            level.blockEntityChanged(pos);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("RemainingTicks", remainingTicks);
        tag.put("HeldFluid", NbtUtils.writeBlockState(savedFluid));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        remainingTicks = Math.max(0, tag.getInt(tag.contains("RemainingTicks", Tag.TAG_ANY_NUMERIC) ? "RemainingTicks" : "Timeout"));
        BlockState fluid = tag.contains("HeldFluid", Tag.TAG_COMPOUND)
                ? NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("HeldFluid")) : legacyFluid(tag);
        savedFluid = fluid.is(Blocks.WATER) || fluid.is(Blocks.LAVA) ? fluid : Blocks.AIR.defaultBlockState();
    }

    private static BlockState legacyFluid(CompoundTag tag) {
        var block = switch (tag.getString("blockName")) {
            case "minecraft:water", "minecraft:flowing_water" -> Blocks.WATER;
            case "minecraft:lava", "minecraft:flowing_lava" -> Blocks.LAVA;
            default -> Blocks.AIR;
        };
        return block == Blocks.AIR ? block.defaultBlockState()
                : block.defaultBlockState().setValue(LiquidBlock.LEVEL, Mth.clamp(tag.getInt("blockMeta"), 0, 15));
    }
}
