package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.AltarBlock;
import art.arcane.witchery.power.AltarNature;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
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
    private static final int RECHARGE_INTERVAL = 20;
    private static final int SCAN_INTERVAL = 100;
    private static final int POWER_PER_SECOND = 10;
    private int capacity;
    private float power;
    private int powerScale = 1;
    private int recharge = 1;
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
        float oldPower = power;
        long time = server.getGameTime();
        if (lastScan == Long.MIN_VALUE || time - lastScan >= SCAN_INTERVAL) {
            capacity = AltarNature.capacity(server, worldPosition);
            lastScan = time;
        }
        updateArtifacts(server, structure);
        power = (int) Math.min(power + POWER_PER_SECOND * recharge, capacity());
        if (oldPower != power || oldCapacity != capacity || oldScale != powerScale || oldRecharge != recharge) {
            setChanged();
        }
    }

    private void reset() {
        if (lastScan != Long.MIN_VALUE || capacity != 0 || power != 0) {
            lastScan = Long.MIN_VALUE;
            capacity = 0;
            power = 0;
            powerScale = 1;
            recharge = 1;
            setChanged();
        }
    }

    private void updateArtifacts(ServerLevel level, List<BlockPos> structure) {
        int skullBonus = 0;
        int candleBonus = 0;
        int chaliceBonus = 0;
        boolean infinityEgg = false;
        for (BlockPos part : structure) {
            BlockState artifact = level.getBlockState(part.above());
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
            if (artifact.is(WitcheryBlocks.getRequired("chalice").get())) {
                chaliceBonus = 1;
            } else if (artifact.is(WitcheryBlocks.getRequired("infinityegg").get())) {
                infinityEgg = true;
            }
        }
        powerScale = (1 + skullBonus + chaliceBonus) * (infinityEgg ? 10 : 1);
        recharge = (1 + skullBonus + candleBonus) * (infinityEgg ? 10 : 1);
    }

    @Nullable
    public AltarBlockEntity resolveCore(ServerLevel server) {
        List<BlockPos> members = AltarBlock.members(server, worldPosition);
        return !members.isEmpty() && server.getBlockEntity(members.get(0)) instanceof AltarBlockEntity core ? core : null;
    }

    public boolean isCore(ServerLevel server) {
        return resolveCore(server) == this;
    }

    public boolean consumePower(float amount) {
        if (!(level instanceof ServerLevel server) || !Float.isFinite(amount) || amount < 0 || power < amount || !isCore(server)) {
            return false;
        }
        power -= amount;
        setChanged();
        return true;
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
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("Power", power);
        tag.putInt("MaxPower", capacity);
        tag.putInt("PowerScale", powerScale);
        tag.putInt("RechargeScale", recharge);
    }
}
