package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.AltarBlock;
import art.arcane.witchery.block.ChaliceBlock;
import art.arcane.witchery.block.PlacedItemBlock;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.power.AltarNature;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class AltarBlockEntity extends BlockEntity {
    private static final int BASE_RANGE = 16;
    public static final int MAX_RANGE = BASE_RANGE * 2;
    private static final int RECHARGE_INTERVAL = 20;
    private static final int SCAN_INTERVAL = 100;
    private static final int POWER_PER_SECOND = 10;
    private int capacity;
    private float power;
    private int powerScale = 1;
    private int recharge = 1;
    private int range = BASE_RANGE;
    private long lastScan = Long.MIN_VALUE;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.ALTAR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        if (!(level instanceof ServerLevel server) || Math.floorMod(level.getGameTime() + pos.asLong(), RECHARGE_INTERVAL) != 0) {
            return;
        }
        List<BlockPos> members = AltarBlock.members(server, pos);
        boolean formed = !members.isEmpty();
        if (state.getValue(AltarBlock.FORMED) != formed) {
            level.setBlock(pos, state.setValue(AltarBlock.FORMED, formed), Block.UPDATE_ALL);
        }
        if (!formed || !pos.equals(members.get(0))) {
            altar.reset();
            return;
        }
        altar.updatePower(server, members);
    }

    private void updatePower(ServerLevel server, List<BlockPos> structure) {
        int oldCapacity = capacity;
        int oldScale = powerScale;
        int oldRecharge = recharge;
        int oldRange = range;
        float oldPower = power;
        long time = server.getGameTime();
        if (lastScan == Long.MIN_VALUE || time - lastScan >= SCAN_INTERVAL) {
            capacity = AltarNature.capacity(server, worldPosition);
            lastScan = time;
        }
        updateArtifacts(server, structure);
        power = (int) Math.min(power + POWER_PER_SECOND * recharge, capacity());
        if (oldPower != power || oldCapacity != capacity || oldScale != powerScale || oldRecharge != recharge || oldRange != range) {
            setChanged();
        }
    }

    private void reset() {
        if (lastScan != Long.MIN_VALUE || capacity != 0 || power != 0 || range != BASE_RANGE) {
            lastScan = Long.MIN_VALUE;
            capacity = 0;
            power = 0;
            powerScale = 1;
            recharge = 1;
            range = BASE_RANGE;
            setChanged();
        }
    }

    private void updateArtifacts(ServerLevel level, List<BlockPos> structure) {
        int skullBonus = 0;
        int candleBonus = 0;
        int chaliceBonus = 0;
        boolean infinityEgg = false;
        boolean arthana = false;
        boolean pentacle = false;
        for (BlockPos part : structure) {
            BlockPos artifactPos = part.above();
            BlockState artifact = level.getBlockState(artifactPos);
            if (skullBonus == 0) {
                if (artifact.is(Blocks.SKELETON_SKULL)) {
                    skullBonus = 1;
                } else if (artifact.is(Blocks.WITHER_SKELETON_SKULL)) {
                    skullBonus = 2;
                } else if (artifact.is(Blocks.PLAYER_HEAD)) {
                    skullBonus = 3;
                }
            }
            if (candleBonus == 0) {
                if (artifact.is(WitcheryBlocks.getRequired("candelabra").get())) {
                    candleBonus = 2;
                } else if (artifact.is(Blocks.TORCH)) {
                    candleBonus = 1;
                }
            }
            if (artifact.getBlock() instanceof ChaliceBlock) {
                if (chaliceBonus == 0) {
                    chaliceBonus = artifact.getValue(ChaliceBlock.FILLED) ? 2 : 1;
                }
            } else if (artifact.is(WitcheryBlocks.getRequired("infinityegg").get())) {
                infinityEgg = true;
            } else if (artifact.getBlock() instanceof PlacedItemBlock
                    && level.getBlockEntity(artifactPos) instanceof PlacedItemBlockEntity placed) {
                arthana |= placed.stack().is(WitcheryItems.getRequired("arthana").get());
                pentacle |= placed.stack().is(WitcheryIngredient.KOBOLDITE_PENTACLE.item());
            }
        }
        powerScale = (1 + skullBonus + chaliceBonus) * (infinityEgg ? 10 : 1);
        recharge = (1 + skullBonus + candleBonus) * (pentacle ? 2 : 1) * (infinityEgg ? 10 : 1);
        range = arthana ? MAX_RANGE : BASE_RANGE;
    }

    @Nullable
    public AltarBlockEntity resolveCore(ServerLevel server) {
        List<BlockPos> members = AltarBlock.members(server, worldPosition);
        return !members.isEmpty() && server.getBlockEntity(members.get(0)) instanceof AltarBlockEntity core ? core : null;
    }

    public boolean isCore(ServerLevel server) {
        return resolveCore(server) == this;
    }

    public boolean isInRange(BlockPos target) {
        return worldPosition.distSqr(target) <= range * range;
    }

    public boolean consumePower(float amount) {
        if (!(level instanceof ServerLevel server) || !Float.isFinite(amount) || amount < 0 || power < amount || !isCore(server)) {
            return false;
        }
        power -= amount;
        setChanged();
        return true;
    }

    void refundPower(float amount) {
        power = Math.min(capacity(), power + amount);
        setChanged();
    }

    public float power() {
        return power;
    }

    public int capacity() {
        return capacity * powerScale;
    }

    public int rechargeMultiplier() {
        return recharge;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        power = Math.max(0, tag.getFloat("Power"));
        capacity = Math.max(0, tag.getInt("MaxPower"));
        powerScale = Math.max(1, tag.getInt("PowerScale"));
        recharge = Math.max(1, tag.getInt("RechargeScale"));
        range = tag.getInt("RangeScale") >= 2 ? MAX_RANGE : BASE_RANGE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("Power", power);
        tag.putInt("MaxPower", capacity);
        tag.putInt("PowerScale", powerScale);
        tag.putInt("RechargeScale", recharge);
        tag.putInt("RangeScale", range / BASE_RANGE);
    }
}
