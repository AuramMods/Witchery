package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryClientConfig;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;

import java.io.IOException;

public final class BossGlintLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation SHADER_ID = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "boss_glint");
    private static final float SCALED_COSINE = (float) (Math.sqrt(3) / 6);
    private static ShaderInstance shader;
    private final Tint tint;

    enum Tint {
        HUNTSMAN(0, 0.5F, 0), PATRON(0.2F, 0.7F, 0.7F);

        private final float red;
        private final float green;
        private final float blue;

        Tint(float red, float green, float blue) {
            this.red = 0.76F * red;
            this.green = 0.76F * green;
            this.blue = 0.76F * blue;
        }
    }

    BossGlintLayer(RenderLayerParent<T, M> parent, Tint tint) {
        super(parent);
        this.tint = tint;
    }

    public static void registerShader(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), SHADER_ID, DefaultVertexFormat.NEW_ENTITY),
                loaded -> shader = loaded);
    }

    static boolean isVisible(LivingEntity boss) {
        return !boss.isInvisible() && Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST
                && WitcheryClientConfig.RENDER_HUNTSMAN_GLINT.get();
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T boss, float stride, float amount,
                       float partialTick, float age, float yaw, float pitch) {
        if (isVisible(boss)) draw(getParentModel(), boss.tickCount, pose, buffers, light, tint, false);
    }

    static void draw(Model model, int tick, PoseStack pose, MultiBufferSource buffers, int light, Tint tint, boolean armor) {
        VertexConsumer output = buffers.getBuffer(armor ? GlintState.ARMOR : GlintState.BODY);
        for (int pass = 0; pass < 2; pass++) {
            // The legacy texture matrix is a one-third scale, a +/-30 degree rotation and a scroll.
            float sine = (1 - 2 * pass) / 6F;
            float scroll = tick * (0.001F + pass * 0.003F) * 20;
            VertexConsumer moving = new VertexConsumerWrapper(output) {
                @Override
                public VertexConsumer uv(float u, float v) {
                    float translated = v + scroll;
                    return super.uv(u * SCALED_COSINE - translated * sine, u * sine + translated * SCALED_COSINE);
                }
            };
            model.renderToBuffer(pose, moving, light, OverlayTexture.NO_OVERLAY, tint.red, tint.green, tint.blue, 1);
        }
    }

    private static final class GlintState extends RenderStateShard {
        private static final RenderType BODY = type("witchery_boss_glint", NO_LAYERING);
        private static final RenderType ARMOR = type("witchery_boss_armor_glint", VIEW_OFFSET_Z_LAYERING);

        private GlintState(String name, Runnable setup, Runnable clear) {
            super(name, setup, clear);
        }

        private static RenderType type(String name, LayeringStateShard layering) {
            return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false,
                    RenderType.CompositeState.builder().setShaderState(new ShaderStateShard(() -> shader))
                            .setTextureState(new TextureStateShard(ResourceLocation.withDefaultNamespace(
                                    "textures/misc/enchanted_glint_entity.png"), true, false))
                            .setTransparencyState(GLINT_TRANSPARENCY).setDepthTestState(EQUAL_DEPTH_TEST)
                            .setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).setLayeringState(layering)
                            .createCompositeState(false));
        }
    }
}
