package art.arcane.witchery.entity;

import art.arcane.witchery.block.entity.StatueOfWorshipBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

final class HobgoblinWorshipGoal extends Goal {
    private static final int BASE_DURATION = 600;
    private final Hobgoblin goblin;
    @Nullable
    private BlockPos statue;
    private int remaining;

    HobgoblinWorshipGoal(Hobgoblin goblin) {
        this.goblin = goblin;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
    }

    void offer(BlockPos pos) {
        if (remaining > 0 || goblin.getRandom().nextInt(3) == 0) return;
        statue = pos.immutable();
        remaining = BASE_DURATION + goblin.getRandom().nextInt(10);
    }

    @Override
    public boolean canUse() {
        if (remaining > 0 && !canContinueToUse()) stop();
        return !goblin.isTrading() && remaining > 0;
    }

    @Override
    public boolean canContinueToUse() {
        return remaining > 0 && (statue == null || goblin.level().getBlockEntity(statue) instanceof StatueOfWorshipBlockEntity);
    }

    @Override
    public void start() {
        goblin.setWorshipping(true);
        if (statue != null) goblin.getNavigation().moveTo(goblin.getNavigation().createPath(statue, 1), 0.4);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (statue != null) goblin.getLookControl().setLookAt(Vec3.atCenterOf(statue));
        if (remaining > 1 || goblin.getRandom().nextInt(3) != 0) remaining--;
    }

    @Override
    public void stop() {
        remaining = 0;
        statue = null;
        goblin.setWorshipping(false);
        goblin.getNavigation().stop();
    }

    void save(CompoundTag tag) {
        if (remaining <= 0) return;
        tag.putBoolean("Worshipping", goblin.isWorshipping());
        tag.putInt("WorshipTicks", remaining);
        if (statue != null) tag.putLong("WorshipPos", statue.asLong());
    }

    void load(CompoundTag tag) {
        remaining = tag.contains("WorshipTicks", Tag.TAG_ANY_NUMERIC)
                ? Mth.clamp(tag.getInt("WorshipTicks"), 0, BASE_DURATION + 9)
                : tag.getBoolean("Worshipping") ? BASE_DURATION : 0;
        statue = tag.contains("WorshipPos", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("WorshipPos")) : null;
        goblin.setWorshipping(remaining > 0 && tag.getBoolean("Worshipping"));
    }
}
