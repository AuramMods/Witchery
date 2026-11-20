package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.BabaYagaModel;
import art.arcane.witchery.entity.BabaYaga;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

public final class BabaYagaRenderer extends MobRenderer<BabaYaga, BabaYagaModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID,
            "textures/entities/babayaga.png");

    public BabaYagaRenderer(EntityRendererProvider.Context context) {
        super(context, new BabaYagaModel(context.bakeLayer(BabaYagaModel.LAYER)), 0.5F);
        addLayer(new BottleLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void scale(BabaYaga entity, PoseStack pose, float partialTick) {
        pose.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public ResourceLocation getTextureLocation(BabaYaga entity) {
        return TEXTURE;
    }

    private static final class BottleLayer extends RenderLayer<BabaYaga, BabaYagaModel> {
        private final ItemInHandRenderer items;

        private BottleLayer(BabaYagaRenderer parent, ItemInHandRenderer items) {
            super(parent);
            this.items = items;
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, BabaYaga entity, float limbSwing,
                           float limbAmount, float partialTick, float age, float yaw, float pitch) {
            var bottle = entity.getMainHandItem();
            if (bottle.isEmpty()) return;
            pose.pushPose();
            getParentModel().translateToBottle(pose);
            pose.mulPose(Axis.XP.rotationDegrees(-90));
            pose.mulPose(Axis.ZP.rotationDegrees(40));
            pose.scale(0.75F, 0.75F, 0.75F);
            items.renderItem(entity, bottle, ItemDisplayContext.GROUND, false, pose, buffers, light);
            pose.popPose();
        }
    }
}
