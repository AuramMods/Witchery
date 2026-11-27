package art.arcane.witchery.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RiteOfferingDrops extends SavedData {
    private static final String ID = "witchery_rite_refunds";
    private final List<Refund> refunds = new ArrayList<>();

    private record Refund(BlockPos origin, ItemStack stack, boolean legacy) {}

    private RiteOfferingDrops() {}

    private static RiteOfferingDrops forLevel(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(RiteOfferingDrops::load, RiteOfferingDrops::new, ID);
    }

    public static boolean trySpawn(ServerLevel level, BlockPos origin, ItemStack stack, boolean legacy) {
        Vec3 pos = Vec3.atCenterOf(origin);
        ItemEntity drop;
        if (legacy) {
            var motion = ThreadLocalRandom.current();
            drop = new ItemEntity(level, pos.x, pos.y, pos.z, stack.copy(),
                    motion.nextDouble(-0.1, 0.1), 0.2F, motion.nextDouble(-0.1, 0.1));
            drop.setNoPickUpDelay();
        } else {
            drop = new ItemEntity(level, pos.x, pos.y, pos.z, stack.copy());
            drop.setDefaultPickUpDelay();
        }
        return level.addFreshEntity(drop);
    }

    public static void returnHeld(ServerLevel level, BlockPos origin, ItemStack stack, boolean legacy) {
        if (trySpawn(level, origin, stack, legacy)) return;
        // A veto may outlive the cast or the Heart Glyph. Retain the already consumed item in the world save.
        var data = forLevel(level);
        data.refunds.add(new Refund(origin.immutable(), stack.copy(), legacy));
        data.setDirty();
    }

    public static void tick(ServerLevel level) {
        if (level.getGameTime() % 20 != 0) return;
        var data = forLevel(level);
        for (Refund refund : List.copyOf(data.refunds)) {
            if (level.isPositionEntityTicking(refund.origin())
                    && trySpawn(level, refund.origin(), refund.stack(), refund.legacy())) {
                data.refunds.remove(refund);
                data.setDirty();
            }
        }
    }

    private static RiteOfferingDrops load(CompoundTag tag) {
        var data = new RiteOfferingDrops();
        for (Tag entry : tag.getList("Refunds", Tag.TAG_COMPOUND)) {
            CompoundTag saved = (CompoundTag) entry;
            ItemStack stack = ItemStack.of(saved.getCompound("Stack"));
            if (!stack.isEmpty() && saved.contains("Origin", Tag.TAG_LONG)) {
                data.refunds.add(new Refund(BlockPos.of(saved.getLong("Origin")), stack, saved.getBoolean("Legacy")));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        for (Refund refund : refunds) {
            var entry = new CompoundTag();
            entry.putLong("Origin", refund.origin().asLong());
            entry.put("Stack", refund.stack().save(new CompoundTag()));
            entry.putBoolean("Legacy", refund.legacy());
            entries.add(entry);
        }
        tag.put("Refunds", entries);
        return tag;
    }
}
