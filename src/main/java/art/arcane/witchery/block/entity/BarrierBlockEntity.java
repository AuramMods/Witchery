package art.arcane.witchery.block.entity;

import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class BarrierBlockEntity extends BlockEntity {
    private int remaining = 60;
    private boolean blocksPlayers;
    private String ownerName = "";
    @Nullable private UUID owner;

    public BarrierBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.BARRIER.get(), pos, state);
    }

    public void configure(@Nullable Player owner, boolean blocksPlayers, int duration) {
        this.owner = owner == null ? null : owner.getUUID();
        ownerName = owner == null ? "" : owner.getGameProfile().getName();
        this.blocksPlayers = blocksPlayers;
        remaining = duration;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public boolean allows(Player player) {
        return !blocksPlayers || (player.isCreative() && player.isShiftKeyDown())
                || (owner != null ? owner.equals(player.getUUID()) : ownerName.equals(player.getGameProfile().getName()));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BarrierBlockEntity barrier) {
        if (--barrier.remaining <= 0) level.removeBlock(pos, false);
        else barrier.setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        remaining = tag.contains("remainingTicks", Tag.TAG_ANY_NUMERIC) ? tag.getInt("remainingTicks") : 60;
        blocksPlayers = tag.getBoolean("blocksPlayers");
        ownerName = tag.getString("owner");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("remainingTicks", remaining);
        tag.putBoolean("blocksPlayers", blocksPlayers);
        tag.putString("owner", ownerName);
        if (owner != null) tag.putUUID("Owner", owner);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
