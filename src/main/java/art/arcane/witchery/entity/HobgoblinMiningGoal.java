package art.arcane.witchery.entity;

import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.EnumSet;
import java.util.UUID;

final class HobgoblinMiningGoal extends Goal {
    static final UUID MINER = UUID.fromString("ab06acb0-0cdb-11e4-9191-0800200c9a66");
    static final UUID KOBOLDITE_MINER = UUID.fromString("24818ae0-0cde-11e4-9191-0800200c9a66");
    private static final GameProfile NORMAL_PROFILE = new GameProfile(MINER, "[Minecraft]");
    private static final GameProfile KOBOLDITE_PROFILE = new GameProfile(KOBOLDITE_MINER, "[Minecraft]");
    private static final double WORK_REACH_SQUARED = 6.25;

    private final Hobgoblin goblin;
    private BlockPos target;
    private BlockState targetState;
    private int failedSearches;
    private int workTicks;

    HobgoblinMiningGoal(Hobgoblin goblin) {
        this.goblin = goblin;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!canWork() || !goblin.getRandom().nextBoolean()) return false;
        boolean down = failedSearches == 15;
        failedSearches = down ? 0 : failedSearches + 1;
        Vec3 eye = goblin.getEyePosition();
        Vec3 direction = Vec3.directionFromRotation(down ? 90 : 0, goblin.getRandom().nextInt(360));
        var hit = goblin.level().clip(new ClipContext(eye, eye.add(direction.scale(down ? 1 : 4)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, goblin));
        if (hit.getType() != HitResult.Type.BLOCK) return false;
        BlockPos pos = hit.getBlockPos();
        var state = goblin.level().getBlockState(pos);
        boolean mineable = state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK);
        if (!mineable || state.getDestroySpeed(goblin.level(), pos) < 0) return false;
        target = pos;
        targetState = state;
        failedSearches = 0;
        return true;
    }

    private boolean canWork() {
        return goblin.isLeashed() && !goblin.isBaby() && !goblin.isTrading()
                && goblin.getMainHandItem().getItem() instanceof PickaxeItem
                && ForgeEventFactory.getMobGriefingEvent(goblin.level(), goblin);
    }

    @Override
    public void start() {
        workTicks = usesKobolditePick(goblin.getMainHandItem()) ? 4 : 60;
        goblin.setWorking(true);
        if (goblin.distanceToSqr(Vec3.atCenterOf(target)) > WORK_REACH_SQUARED) {
            goblin.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 0.6);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && canWork() && goblin.level().getBlockState(target) == targetState
                && (goblin.distanceToSqr(Vec3.atCenterOf(target)) <= WORK_REACH_SQUARED || !goblin.getNavigation().isDone());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (target == null) return;
        Vec3 center = Vec3.atCenterOf(target);
        goblin.getLookControl().setLookAt(center);
        if (goblin.distanceToSqr(center) > WORK_REACH_SQUARED || --workTicks > 0) return;
        if (goblin.level() instanceof ServerLevel level && level.getWorldBorder().isWithinBounds(target)) {
            harvest(level, target, targetState);
        }
        target = null;
    }

    private void harvest(ServerLevel level, BlockPos pos, BlockState state) {
        ItemStack tool = goblin.getMainHandItem();
        var miner = FakePlayerFactory.get(level, usesKobolditePick(tool) ? KOBOLDITE_PROFILE : NORMAL_PROFILE);
        miner.setPos(goblin.getX(), goblin.getY(), goblin.getZ());
        miner.setItemInHand(InteractionHand.MAIN_HAND, tool.copy());
        try {
            if (state.canHarvestBlock(level, pos, miner) && miner.gameMode.destroyBlock(pos)
                    && level.getBlockState(pos) != state) {
                level.levelEvent(2001, pos, Block.getId(state));
            }
        } finally {
            miner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    @Override
    public void stop() {
        target = null;
        targetState = null;
        goblin.setWorking(false);
        goblin.getNavigation().stop();
    }

    static boolean usesKobolditePick(ItemStack stack) {
        return stack.is(WitcheryItems.getRequired("kobolditepickaxe").get());
    }
}
