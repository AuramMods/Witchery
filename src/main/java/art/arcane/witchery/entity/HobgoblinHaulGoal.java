package art.arcane.witchery.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;

final class HobgoblinHaulGoal extends Goal {
    private static final int SEARCH_RANGE = 24;
    private static final double SPEED = 0.6;

    private final Hobgoblin goblin;
    private ItemEntity droppedItem;
    private BlockEntity destination;
    private Path path;
    private long nextSearch;

    HobgoblinHaulGoal(Hobgoblin goblin) {
        this.goblin = goblin;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!canHaul() || goblin.level().getGameTime() < nextSearch) return false;
        ItemStack held = goblin.getMainHandItem();
        PathNavigation navigation = goblin.getNavigation();
        if (held.isEmpty()) {
            nextSearch = goblin.level().getGameTime() + 10;
            var items = goblin.level().getEntitiesOfClass(ItemEntity.class, goblin.getBoundingBox().inflate(SEARCH_RANGE),
                    item -> item.isAlive() && !item.hasPickUpDelay());
            items.sort(Comparator.comparingDouble(goblin::distanceToSqr));
            for (ItemEntity item : items) {
                if (goblin.distanceToSqr(item) <= 2.25 || choosePath(navigation.createPath(item, 0))) {
                    droppedItem = item;
                    return true;
                }
            }
        } else if (!(held.getItem() instanceof DiggerItem) && goblin.level() instanceof ServerLevel level) {
            nextSearch = level.getGameTime() + 60;
            for (BlockEntity storage : nearbyStorage(level)) {
                if (!hasSpace((Container) storage, held)) continue;
                if (goblin.distanceToSqr(Vec3.atCenterOf(storage.getBlockPos())) <= 6.25
                        || choosePath(navigation.createPath(storage.getBlockPos(), 1))) {
                    destination = storage;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canHaul() {
        return goblin.isLeashed() && !goblin.isBaby() && !goblin.isTrading();
    }

    private boolean choosePath(Path candidate) {
        if (candidate == null || !candidate.canReach()) return false;
        path = candidate;
        return true;
    }

    @Override
    public void start() {
        goblin.setHauling(true);
        if (path != null) goblin.getNavigation().moveTo(path, SPEED);
    }

    private ArrayList<BlockEntity> nearbyStorage(ServerLevel level) {
        var result = new ArrayList<BlockEntity>();
        var origin = goblin.blockPosition();
        for (int x = (origin.getX() - SEARCH_RANGE) >> 4; x <= (origin.getX() + SEARCH_RANGE) >> 4; x++) {
            for (int z = (origin.getZ() - SEARCH_RANGE) >> 4; z <= (origin.getZ() + SEARCH_RANGE) >> 4; z++) {
                var chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) continue;
                for (BlockEntity candidate : chunk.getBlockEntities().values()) {
                    if (!candidate.isRemoved() && candidate instanceof Container storage && storage.getContainerSize() >= 27
                            && origin.distSqr(candidate.getBlockPos()) <= SEARCH_RANGE * SEARCH_RANGE) {
                        result.add(candidate);
                    }
                }
            }
        }
        result.sort(Comparator.comparingDouble(storage -> goblin.distanceToSqr(Vec3.atCenterOf(storage.getBlockPos()))));
        return result;
    }

    private static boolean hasSpace(Container storage, ItemStack held) {
        for (int slot = 0; slot < storage.getContainerSize(); slot++) {
            if (!storage.canPlaceItem(slot, held)) continue;
            ItemStack stack = storage.getItem(slot);
            if (stack.isEmpty() || ItemStack.isSameItemSameTags(stack, held)
                    && stack.getCount() < Math.min(stack.getMaxStackSize(), storage.getMaxStackSize())) return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!canHaul()) return false;
        ItemStack held = goblin.getMainHandItem();
        if (droppedItem != null) return held.isEmpty() && droppedItem.isAlive()
                && (goblin.distanceToSqr(droppedItem) <= 2.25 || !goblin.getNavigation().isDone());
        return destination != null && !destination.isRemoved() && !held.isEmpty() && !(held.getItem() instanceof DiggerItem)
                && (goblin.distanceToSqr(Vec3.atCenterOf(destination.getBlockPos())) <= 6.25 || !goblin.getNavigation().isDone());
    }

    @Override
    public void tick() {
        if (droppedItem != null && goblin.distanceToSqr(droppedItem) <= 2.25) {
            goblin.setItemInHand(InteractionHand.MAIN_HAND, droppedItem.getItem());
            goblin.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            droppedItem.discard();
            droppedItem = null;
        } else if (destination instanceof Container storage
                && goblin.distanceToSqr(Vec3.atCenterOf(destination.getBlockPos())) <= 6.25) {
            ItemStack remaining = HopperBlockEntity.addItem(null, storage, goblin.getMainHandItem().copy(), null);
            goblin.setItemInHand(InteractionHand.MAIN_HAND, remaining);
            destination = null;
        }
    }

    @Override
    public void stop() {
        droppedItem = null;
        destination = null;
        path = null;
        goblin.setHauling(false);
        goblin.getNavigation().stop();
    }
}
