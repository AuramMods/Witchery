package art.arcane.witchery.ritual;

import art.arcane.witchery.entity.LegacyWaystoneEntity;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.WaystoneTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class DroppedWaystones {
    private static final String TOSSED = "WITCDroppedWaystone";

    private DroppedWaystones() {}

    public static void markToss(ItemEntity item) {
        if (item.getItem().getItem() instanceof WaystoneItem) item.getPersistentData().putBoolean(TOSSED, true);
    }

    public static boolean isTossedWaystone(ItemEntity item) {
        return item.getItem().getItem() instanceof WaystoneItem
                && (item instanceof LegacyWaystoneEntity || item.getPersistentData().getBoolean(TOSSED));
    }

    public static boolean update(ItemEntity item) {
        if (!(item.level() instanceof ServerLevel level) || !isTossedWaystone(item)
                || item.getAge() <= 40 || item.getAge() % 40 != 0) return false;
        Block glyph = WitcheryBlocks.getRequired("circleglyphotherwhere").get();
        BlockPos point = item.blockPosition();
        if (item.getItem().is(WitcheryIngredient.WAYSTONE.item())) {
            if (RitualCircle.TINY.matches(level, point, glyph)) bind(level, item, point, glyph);
        } else if (WaystoneItem.isBoundWaystone(item.getItem())) {
            BlockPos circle = smallCircleNear(level, point, glyph);
            if (circle != null) {
                ItemStack usedStone = item.getItem().copyWithCount(1);
                consume(level, item, 1);
                if (!WaystoneTravel.teleportDropped(level, circle, usedStone)) fail(level, circle);
            }
        }
        return item.isRemoved();
    }

    private static void bind(ServerLevel level, ItemEntity source, BlockPos pos, Block glyph) {
        LivingEntity target = WaystoneTravel.findBindingTarget(level, pos, true);
        var altar = target == null ? null : AltarPower.nearest(level, pos);
        if (target != null && (altar == null || altar.power() < 4000)) {
            fail(level, pos);
            return;
        }
        int count = target == null ? Math.min(source.getItem().getCount(), 8) : 1;
        ItemStack bound = new ItemStack((target == null ? WitcheryIngredient.BOUND_WAYSTONE
                : WitcheryIngredient.CREATURE_BOUND_WAYSTONE).item(), count);
        if (target == null) WaystoneItem.bindLocation(bound, level, pos);
        else TaglockItem.bindSignature(bound, target);
        ItemEntity result = drop(level, source, bound);
        if (!level.addFreshEntity(result)) return;
        if (altar != null && !altar.consumePower(4000)) {
            result.discard();
            fail(level, pos);
            return;
        }
        consume(level, source, count);
        RitualCircle.TINY.erase(level, pos, glyph);
        level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5,
                16, 0.4, 0.2, 0.4, 0.035);
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7F, 1);
    }

    private static void consume(ServerLevel level, ItemEntity source, int count) {
        ItemStack stack = source.getItem();
        if (stack.getCount() == count) {
            source.discard();
            return;
        }
        ItemStack remaining = stack.copyWithCount(stack.getCount() - count);
        if (level.addFreshEntity(drop(level, source, remaining))) source.discard();
        else source.setItem(remaining);
    }

    private static ItemEntity drop(ServerLevel level, ItemEntity source, ItemStack result) {
        var drop = new ItemEntity(level, source.getX(), source.getY(), source.getZ(), result, 0, 0.05, 0);
        drop.setDefaultPickUpDelay();
        return drop;
    }

    @Nullable
    private static BlockPos smallCircleNear(ServerLevel level, BlockPos point, Block glyph) {
        for (BlockPos pos : BlockPos.betweenClosed(point.offset(-1, 0, -1), point.offset(1, 0, 1))) {
            if (RitualCircle.SMALL.matches(level, pos, glyph)) return pos.immutable();
        }
        return null;
    }

    private static void fail(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5,
                16, 0.35, 0.2, 0.35, 0.035);
        level.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 0.6F, 1);
    }
}
