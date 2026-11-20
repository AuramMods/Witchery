package art.arcane.witchery.item;

import art.arcane.witchery.entity.DreamSpirit;
import art.arcane.witchery.entity.LegacyWaystoneEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.ritual.RitualCircle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

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
        BlockPos landing = context.getClickedPos().relative(context.getClickedFace());
        if (context.getPlayer() != null && !level.mayInteract(context.getPlayer(), landing)) return InteractionResult.FAIL;
        if (!DreamSpirit.release((ServerLevel) level, Vec3.atBottomCenterOf(landing), releaseKind)) return InteractionResult.FAIL;
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
                || !DreamSpirit.release(level, item.position(), DreamSpirit.Kind.GUIDE)) return false;
        if (stack.getCount() == 1) item.discard();
        else item.setItem(stack.copyWithCount(stack.getCount() - 1));
        RitualCircle.TINY.erase(level, center, glyph);
        level.sendParticles(ParticleTypes.POOF, item.getX(), item.getY(), item.getZ(), 16, 0.35, 0.2, 0.35, 0.035);
        level.playSound(null, center, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7F, 1);
        return item.isRemoved();
    }
}
