package art.arcane.witchery.mixin;

import art.arcane.witchery.config.WitcheryClientConfig;
import art.arcane.witchery.item.CaneSwordItem;
import art.arcane.witchery.item.HuntsmanSpearItem;
import art.arcane.witchery.item.MysticBranchItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemRenderer.class)
abstract class HeldItemGlintMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;hasFoil()Z"))
    private boolean witchery$heldGlint(ItemStack stack, ItemStack rendered, ItemDisplayContext context,
                                       boolean leftHand, PoseStack pose, MultiBufferSource buffers,
                                       int light, int overlay, BakedModel model) {
        var item = stack.getItem();
        boolean held = context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (held && (item instanceof MysticBranchItem || item instanceof HuntsmanSpearItem || item instanceof CaneSwordItem)
                && !WitcheryClientConfig.RENDER_3D_GLINT.get()) return false;
        return stack.hasFoil();
    }
}
