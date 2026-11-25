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
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class AltarBlockEntity extends BlockEntity {
    private static final int BASE_RANGE = 16;
    public static final int MAX_RANGE = BASE_RANGE * 2;
    private static final int RECHARGE_INTERVAL = 20;
    private static final int SCAN_INTERVAL = 100;
    private static final int POWER_PER_SECOND = 10;
    @Nullable
    private BlockPos corePos;
    private int capacity;
    private float power;
    private int powerScale = 1;
    private int recharge = 1;
    private int range = BASE_RANGE;
    private int enhancement;
    private long ticks;
    private long lastScan;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.ALTAR.get(), pos, state);
    }

    public static void form(ServerLevel server, BlockPos pos) {
        AltarBlock.Structure structure = AltarBlock.changedStructure(server, pos);
        BlockPos newCore = structure.core();
        for (BlockPos member : structure.members()) {
            if (server.getBlockEntity(member) instanceof AltarBlockEntity altar) {
                altar.reset();
                altar.rememberCore(newCore);
            }
        }
        for (BlockPos member : structure.members()) {
            BlockState state = server.getBlockState(member);
            if (state.getValue(AltarBlock.FORMED) != (newCore != null)) {
                server.setBlock(member, state.setValue(AltarBlock.FORMED, newCore != null), Block.UPDATE_ALL);
            }
        }
        if (newCore != null && server.getBlockEntity(newCore) instanceof AltarBlockEntity core) {
            core.scanNature(server);
            // Artifacts are evaluated in traversal order from the newly selected core.
            core.updateArtifacts(server, AltarBlock.members(server, newCore));
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AltarBlockEntity altar) {
        if (!(level instanceof ServerLevel server) || ++altar.ticks % RECHARGE_INTERVAL != 0) {
            return;
        }
        List<BlockPos> members = AltarBlock.members(server, pos);
        AltarBlockEntity core = resolveCore(server, members);
        boolean formed = core != null;
        // A missing chunk cannot tell us whether one of its altar blocks was removed.
        if (!formed && !server.hasChunksAt(pos.offset(-2, 0, -2), pos.offset(2, 0, 2))) return;
        boolean becameCore = altar == core && !pos.equals(altar.corePos);
        altar.rememberCore(formed ? core.getBlockPos() : null);
        if (state.getValue(AltarBlock.FORMED) != formed) {
            level.setBlock(pos, state.setValue(AltarBlock.FORMED, formed), Block.UPDATE_ALL);
        }
        if (altar != core) {
            altar.reset();
            return;
        }
        if (becameCore) {
            altar.scanNature(server);
            altar.updateArtifacts(server, members);
        }
        altar.rechargePower();
    }

    private void rememberCore(@Nullable BlockPos pos) {
        if (!Objects.equals(corePos, pos)) {
            corePos = pos == null ? null : pos.immutable();
            setChanged();
        }
    }

    public void inspect(ServerLevel server) {
        updateArtifacts(server, AltarBlock.members(server, worldPosition));
        long elapsed = ticks - lastScan;
        if (elapsed <= 0 || elapsed > SCAN_INTERVAL) {
            scanNature(server);
        }
    }

    private void scanNature(ServerLevel server) {
        lastScan = ticks;
        int updated = AltarNature.capacity(server, worldPosition);
        if (updated != capacity) {
            capacity = updated;
            setChanged();
        }
    }

    private void rechargePower() {
        float updated = (int) Math.min(power + POWER_PER_SECOND * recharge, capacity());
        if (updated != power) {
            power = updated;
            setChanged();
        }
    }

    public void refreshArtifacts(ServerLevel level) {
        List<BlockPos> members = AltarBlock.members(level, worldPosition);
        AltarBlockEntity core = resolveCore(level, members);
        if (core != null) {
            List<BlockPos> orderedMembers = this == core ? members : AltarBlock.members(level, core.getBlockPos());
            core.updateArtifacts(level, orderedMembers);
        }
    }

    private void reset() {
        if (lastScan != 0 || capacity != 0 || power != 0 || range != BASE_RANGE
                || powerScale != 1 || recharge != 1 || enhancement != 0) {
            lastScan = 0;
            capacity = 0;
            power = 0;
            powerScale = 1;
            recharge = 1;
            range = BASE_RANGE;
            enhancement = 0;
            setChanged();
        }
    }

    private void updateArtifacts(ServerLevel level, List<BlockPos> structure) {
        int skullBonus = 0;
        int candleBonus = 0;
        int chaliceBonus = 0;
        boolean infinityEgg = false;
        boolean arthana = false;
        boolean mysticBranch = false;
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
                mysticBranch |= placed.stack().is(WitcheryItems.getRequired("mysticbranch").get());
                pentacle |= WitcheryIngredient.KOBOLDITE_PENTACLE.matches(placed.stack());
            }
        }
        int newPowerScale = (1 + skullBonus + chaliceBonus) * (infinityEgg ? 10 : 1);
        int newRecharge = (1 + skullBonus + candleBonus) * (pentacle ? 2 : 1) * (infinityEgg ? 10 : 1);
        int newRange = arthana ? MAX_RANGE : BASE_RANGE;
        int newEnhancement = mysticBranch ? 1 : 0;
        if (powerScale != newPowerScale || recharge != newRecharge || range != newRange || enhancement != newEnhancement) {
            powerScale = newPowerScale;
            recharge = newRecharge;
            range = newRange;
            enhancement = newEnhancement;
            setChanged();
        }
    }

    @Nullable
    public AltarBlockEntity resolveCore(ServerLevel server) {
        return resolveCore(server, AltarBlock.members(server, worldPosition));
    }

    @Nullable
    private static AltarBlockEntity resolveCore(ServerLevel server, List<BlockPos> members) {
        AltarBlockEntity saved = null;
        AltarBlockEntity first = null;
        for (BlockPos member : members) {
            if (server.getBlockEntity(member) instanceof AltarBlockEntity altar) {
                if (member.equals(altar.corePos) && (saved == null || member.compareTo(saved.getBlockPos()) < 0)) {
                    saved = altar;
                }
                if (first == null || member.compareTo(first.getBlockPos()) < 0) first = altar;
            }
        }
        return saved != null ? saved : first;
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
        corePos = tag.contains("CoreX", Tag.TAG_ANY_NUMERIC) && tag.contains("CoreY", Tag.TAG_ANY_NUMERIC)
                && tag.contains("CoreZ", Tag.TAG_ANY_NUMERIC)
                ? new BlockPos(tag.getInt("CoreX"), tag.getInt("CoreY"), tag.getInt("CoreZ")) : null;
        power = Math.max(0, tag.getFloat("Power"));
        capacity = Math.max(0, tag.getInt("MaxPower"));
        powerScale = Math.max(1, tag.getInt("PowerScale"));
        recharge = Math.max(1, tag.getInt("RechargeScale"));
        range = tag.getInt("RangeScale") >= 2 ? MAX_RANGE : BASE_RANGE;
        enhancement = tag.getInt("EnhancementLevel") > 0 ? 1 : 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (corePos != null) {
            tag.putInt("CoreX", corePos.getX());
            tag.putInt("CoreY", corePos.getY());
            tag.putInt("CoreZ", corePos.getZ());
        }
        tag.putFloat("Power", power);
        tag.putInt("MaxPower", capacity);
        tag.putInt("PowerScale", powerScale);
        tag.putInt("RechargeScale", recharge);
        tag.putInt("RangeScale", range / BASE_RANGE);
        tag.putInt("EnhancementLevel", enhancement);
    }
}
