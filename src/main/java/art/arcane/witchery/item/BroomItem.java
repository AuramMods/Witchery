package art.arcane.witchery.item;

import art.arcane.witchery.entity.WitchBroom;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class BroomItem extends Item {
    public static final String BRUSH_COLOR = "BrushColor";
    private final boolean enchanted;

    public BroomItem(boolean enchanted) {
        super(new Properties());
        this.enchanted = enchanted;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return enchanted || super.isFoil(stack);
    }

    public static int brushColor(ItemStack stack) {
        var tag = stack.getTag();
        return tag != null && tag.contains(BRUSH_COLOR, Tag.TAG_ANY_NUMERIC) ? tag.getByte(BRUSH_COLOR) & 15 : -1;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (!enchanted || player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockPos above = level.getBlockState(clicked).is(Blocks.SNOW) ? clicked : clicked.above();
        if (!level.mayInteract(player, clicked) || !player.mayUseItemAt(above, context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        WitchBroom broom = WitcheryEntities.BROOM.get().create(level);
        if (broom == null) return InteractionResult.FAIL;
        Vec3 origin = Vec3.atBottomCenterOf(above);
        broom.moveTo(origin.x, origin.y, origin.z, Math.round(player.getYRot() / 90) * 90, 0);
        if (!level.noCollision(broom, broom.getBoundingBox())) return InteractionResult.FAIL;
        if (!level.isClientSide) {
            ItemStack stack = context.getItemInHand();
            if (stack.hasCustomHoverName()) broom.setCustomName(stack.getHoverName());
            broom.setBrushColor(brushColor(stack));
            if (!level.addFreshEntity(broom)) return InteractionResult.FAIL;
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
