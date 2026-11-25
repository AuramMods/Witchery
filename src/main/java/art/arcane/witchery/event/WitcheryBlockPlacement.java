package art.arcane.witchery.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class WitcheryBlockPlacement {
    private WitcheryBlockPlacement() {}

    public static boolean place(ServerLevel level, BlockPos pos, BlockState state, @Nullable Entity actor, Direction face) {
        // An enclosing placement transaction owns approval and notifications for its captured changes.
        if (actor == null || level.captureBlockSnapshots) return level.setBlockAndUpdate(pos, state);
        return placeNow(level, pos, state, actor, face);
    }

    public static boolean placeNow(ServerLevel level, BlockPos pos, BlockState state, Entity actor, Direction face) {
        boolean capturing = level.captureBlockSnapshots;
        int first = level.capturedBlockSnapshots.size();
        List<BlockSnapshot> changes = List.of();
        boolean approved = false;
        try {
            boolean changed;
            level.captureBlockSnapshots = true;
            try {
                changed = level.setBlockAndUpdate(pos, state);
            } finally {
                var captured = level.capturedBlockSnapshots.subList(first, level.capturedBlockSnapshots.size());
                changes = List.copyOf(captured);
                captured.clear();
                level.captureBlockSnapshots = false;
            }
            if (!changed || changes.isEmpty()) return false;
            approved = changes.size() == 1
                    ? !ForgeEventFactory.onBlockPlace(actor, changes.get(0), face)
                    : !ForgeEventFactory.onMultiBlockPlace(actor, changes, face);
            if (approved) notifyPlacement(level, changes);
            return approved;
        } finally {
            try {
                if (!approved && !changes.isEmpty()) restore(level, changes);
            } finally {
                level.captureBlockSnapshots = capturing;
            }
        }
    }

    public static void notifyPlacement(ServerLevel level, List<BlockSnapshot> changes) {
        for (BlockSnapshot change : changes) {
            BlockPos pos = change.getPos();
            BlockState placed = level.getBlockState(pos);
            BlockState previous = change.getReplacedBlock();
            placed.onPlace(level, pos, previous, false);
            level.markAndNotifyBlock(pos, level.getChunkAt(pos), previous, placed, change.getFlag(), 512);
        }
    }

    public static void restore(ServerLevel level, List<BlockSnapshot> changes) {
        boolean capturing = level.captureBlockSnapshots;
        boolean restoring = level.restoringBlockSnapshots;
        level.captureBlockSnapshots = false;
        level.restoringBlockSnapshots = true;
        try {
            for (int i = changes.size() - 1; i >= 0; i--) changes.get(i).restore(true, false);
        } finally {
            level.captureBlockSnapshots = capturing;
            level.restoringBlockSnapshots = restoring;
        }
    }
}
