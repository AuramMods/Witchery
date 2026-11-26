package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.HobgoblinGodModel;
import art.arcane.witchery.config.WitcheryClientConfig;
import art.arcane.witchery.entity.HobgoblinGod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public final class HobgoblinGodRenderer extends MobRenderer<HobgoblinGod, HobgoblinGodModel> {
    private final ResourceLocation texture;

    public HobgoblinGodRenderer(EntityRendererProvider.Context context, boolean mog) {
        super(context, new HobgoblinGodModel(context.bakeLayer(mog ? HobgoblinGodModel.MOG : HobgoblinGodModel.GULG)), 0.5F);
        texture = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/" + (mog ? "mog" : "gulg") + ".png");
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        addLayer(new RenderLayer<>(this) {
            @Override
            public void render(PoseStack pose, MultiBufferSource buffers, int light, HobgoblinGod god, float stride,
                               float speed, float partial, float age, float yaw, float pitch) {
                if (!god.isInvisible() && Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST
                        && WitcheryClientConfig.RENDER_HUNTSMAN_GLINT.get()) {
                    getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.entityGlint()), light,
                            OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
                }
            }
        });
    }

    @Override
    protected int getBlockLightLevel(HobgoblinGod god, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(HobgoblinGod god) {
        return texture;
    }

    @Override
    public Vec3 getRenderOffset(HobgoblinGod god, float partialTick) {
        Vec3 offset = super.getRenderOffset(god, partialTick);
        return god.isShiftKeyDown() ? offset.add(0, -0.125, 0) : offset;
    }
}
