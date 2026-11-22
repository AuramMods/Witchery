package art.arcane.witchery.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PlacedOwnerBlockEntity extends BlockEntity {
    private static final String OWNER_TAG = "WITCPlacer";
    private static final String RECOVERABLE_PLACEHOLDER_TAG = "RecoverablePlaceholder";
    private String owner = "";
    private boolean recoverablePlaceholder;

    public PlacedOwnerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void placedBy(Player player) {
        owner = player.getGameProfile().getName();
        recoverablePlaceholder = false;
        setChanged();
    }

    public boolean canRecover(Player player) {
        return recoverablePlaceholder || player.getGameProfile().getName().equals(owner);
    }

    public void recoverableFromEarlierPort() {
        recoverablePlaceholder = true;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.getString(OWNER_TAG);
        recoverablePlaceholder = tag.getBoolean(RECOVERABLE_PLACEHOLDER_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(OWNER_TAG, owner);
        if (recoverablePlaceholder) tag.putBoolean(RECOVERABLE_PLACEHOLDER_TAG, true);
    }
}
