package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.WitcheryIngredient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class BrewProjectileRenderer extends ThrownItemRenderer<ThrownBrew> {
    private static final RenderType SPELL = RenderType.entityCutoutNoCull(ProjectileBillboard.QUARTZ_SPHERE);
    private static final RenderType LIQUID = RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/brew_overlay.png"));
    private static final RenderType SPLASH_GLASS = RenderType.entityCutoutNoCullZOffset(ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/brew_splash.png"));
    private static final RenderType DRINKABLE_GLASS = RenderType.entityCutoutNoCullZOffset(ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/brew_drinkable.png"));
    private static final RenderType FIXED_GLASS = RenderType.entityCutoutNoCull(
            ResourceLocation.withDefaultNamespace("textures/item/splash_potion.png"));
    private final ItemRenderer items;

    public BrewProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
    }

    @Override
    public void render(ThrownBrew brew, float yaw, float tick, PoseStack pose, MultiBufferSource buffers, int light) {
        var stack = brew.getItem();
        boolean custom = stack.getItem() instanceof BrewBottleItem;
        if (!custom && !ThrownBrew.isFixedPayload(stack)) {
            super.render(brew, yaw, tick, pose, buffers, light);
            return;
        }

        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(0.5F, 0.5F, 0.5F);
        int packedLight = brew.displayFireAnimation() ? LightTexture.FULL_BRIGHT : light;
        if (custom) {
            renderCustom(stack, brew.isNoGravity(), pose.last(), buffers, packedLight);
        } else {
            renderFixed(brew, stack, pose.last(), buffers, packedLight);
        }
        pose.popPose();
    }

    private static void renderCustom(ItemStack bottle, boolean spell, PoseStack.Pose pose, MultiBufferSource buffers,
                                     int light) {
        int color = BrewBottleItem.color(bottle);
        if (spell) {
            ProjectileBillboard.draw(buffers.getBuffer(SPELL), pose, color, light);
        } else {
            ProjectileBillboard.draw(buffers.getBuffer(LIQUID), pose, color, light);
            var glass = BrewBottleItem.isSplash(bottle) ? SPLASH_GLASS : DRINKABLE_GLASS;
            ProjectileBillboard.draw(buffers.getBuffer(glass), pose, 0xFFFFFF, light);
        }
    }

    private void renderFixed(ThrownBrew brew, ItemStack stack, PoseStack.Pose pose, MultiBufferSource buffers, int light) {
        if (!stack.is(WitcheryIngredient.QUICKLIME.item())) {
            ProjectileBillboard.draw(buffers.getBuffer(FIXED_GLASS), pose, 0xFFFFFF, light);
        }
        // Generated item models use their first texture as the particle sprite, including solidification brews.
        var sprite = items.getModel(stack, brew.level(), null, brew.getId()).getParticleIcon();
        var vertices = buffers.getBuffer(RenderType.entityCutoutNoCullZOffset(sprite.atlasLocation()));
        ProjectileBillboard.draw(sprite.wrap(vertices), pose, 0xFFFFFF, light);
    }
}
