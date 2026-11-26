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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class WitchOwlRenderer extends MobRenderer<WitchOwl, WitchOwlModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/owl.png");

    public WitchOwlRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchOwlModel(context.bakeLayer(WitchOwlModel.LAYER)), 0.5F);
        var items = context.getItemRenderer();
        addLayer(new RenderLayer<>(this) {
            @Override
            public void render(PoseStack pose, MultiBufferSource buffers, int light, WitchOwl owl,
                               float swing, float walk, float partialTick, float age, float yaw, float pitch) {
                ItemStack carried = owl.getMainHandItem();
                if (carried.isEmpty()) return;
                pose.pushPose();
                boolean block = carried.getItem() instanceof BlockItem
                        && items.getModel(carried, owl.level(), owl, owl.getId()).isGui3d();
                positionParcel(pose, getParentModel().young, WitchOwlModel.hasLandingSurface(owl), block);
                items.renderStatic(carried, ItemDisplayContext.NONE, light, 0, pose, buffers, owl.level(), owl.getId());
                pose.popPose();
            }
        });
    }

    private static void positionParcel(PoseStack pose, boolean young, boolean landed, boolean block) {
        if (young) {
            pose.translate(0, 0.625F, 0);
            pose.mulPose(Axis.XP.rotationDegrees(20));
            pose.scale(0.5F, 0.5F, 0.5F);
        }

        pose.translate(-0.0625F, landed ? 1.1375F : 1.375F, landed ? 0.0625F : 0.3F);
        if (block) {
            pose.translate(0, 0.1875F, -0.3125F);
            pose.mulPose(Axis.XP.rotationDegrees(20));
            pose.mulPose(Axis.YP.rotationDegrees(45));
            pose.scale(-0.375F, -0.375F, 0.375F);
        } else {
            pose.translate(0.25F, 0.1875F, -0.3F);
            pose.scale(0.375F, 0.375F, 0.375F);
            pose.mulPose(Axis.ZP.rotationDegrees(60));
            pose.mulPose(Axis.XP.rotationDegrees(-120));
            pose.mulPose(Axis.ZP.rotationDegrees(90));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(WitchOwl owl) {
        return TEXTURE;
    }
}
