package art.arcane.witchery.item;

import art.arcane.witchery.entity.DreamSpirit;
import art.arcane.witchery.entity.LegacyWaystoneEntity;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.ritual.RitualCircle;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class SpiritOfferingItem extends Item {
    private static final String TOSSED = "WITCSpiritOfferingTossed";
    private final DreamSpirit.Kind releaseKind;

    public SpiritOfferingItem(DreamSpirit.Kind releaseKind) {
        super(new Properties());
        this.releaseKind = releaseKind;
    }

    public static void markToss(ItemEntity item) {
        if (item.getItem().getItem() instanceof SpiritOfferingItem offering
                && offering.releaseKind != DreamSpirit.Kind.VILLAGE) item.getPersistentData().putBoolean(TOSSED, true);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (releaseKind == DreamSpirit.Kind.GUIDE) return InteractionResult.PASS;
        var level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!DreamSpirit.release((ServerLevel) level, context.getClickedPos(), releaseKind, context.getPlayer())) {
            return InteractionResult.FAIL;
        }
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return releaseKind != DreamSpirit.Kind.GUIDE || super.isFoil(stack);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity item) {
        if (!(item.level() instanceof ServerLevel level) || releaseKind == DreamSpirit.Kind.VILLAGE
                || !(item instanceof LegacyWaystoneEntity || item.getPersistentData().getBoolean(TOSSED))
                || item.getAge() <= 40 || item.getAge() % 40 != 0) return false;
        BlockPos center = item.blockPosition();
        var glyph = WitcheryBlocks.getRequired("circleglyphritual").get();
        if (!RitualCircle.TINY.matches(level, center, glyph)
                || !DreamSpirit.release(level, center, DreamSpirit.Kind.GUIDE, null)) return false;
        if (stack.getCount() == 1) item.discard();
        else item.setItem(stack.copyWithCount(stack.getCount() - 1));
        RitualCircle.TINY.erase(level, center, glyph);
        WitcheryNetwork.sendSpiritCircle(level, item.position());
        return item.isRemoved();
    }
}
