package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.WitchOwlModel;
import art.arcane.witchery.entity.WitchOwl;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class WitchOwlRenderer extends MobRenderer<WitchOwl, WitchOwlModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/owl.png");

    public WitchOwlRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchOwlModel(context.bakeLayer(WitchOwlModel.LAYER)), 0.3F);
        var items = context.getItemRenderer();
        addLayer(new RenderLayer<>(this) {
            @Override
            public void render(PoseStack pose, MultiBufferSource buffers, int light, WitchOwl owl,
                               float swing, float walk, float partialTick, float age, float yaw, float pitch) {
                ItemStack carried = owl.getMainHandItem();
                if (carried.isEmpty()) return;
                pose.pushPose();
                getParentModel().translateToBeak(pose);
                pose.mulPose(Axis.XP.rotationDegrees(90));
                pose.scale(0.55F, 0.55F, 0.55F);
                items.renderStatic(carried, ItemDisplayContext.GROUND, light, 0, pose, buffers, owl.level(), owl.getId());
                pose.popPose();
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(WitchOwl owl) {
        return TEXTURE;
    }
}
