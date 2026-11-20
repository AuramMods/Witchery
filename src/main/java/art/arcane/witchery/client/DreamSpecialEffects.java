package art.arcane.witchery.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;

public final class DreamSpecialEffects extends DimensionSpecialEffects {
    private static final Vec3 DREAM_FOG = new Vec3(0.8, 0.2, 0.6);
    private static final Vec3 NIGHTMARE_FOG = new Vec3(0, 0.06, 0);
    private static final Vec3 DEMONIC_FOG = new Vec3(0.06, 0, 0);
    private static final ResourceLocation SUN = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
    private static final ResourceLocation MOON = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private WeakReference<ClientLevel> observedLevel = new WeakReference<>(null);
    private int nightmareLevel;

    public DreamSpecialEffects() {
        super(0, true, SkyType.NONE, false, false);
    }

    public static void updateClientMode(int nightmareLevel) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.effects() instanceof DreamSpecialEffects effects) {
            if (effects.observedLevel.get() != level) effects.observedLevel = new WeakReference<>(level);
            effects.nightmareLevel = Mth.clamp(nightmareLevel, 0, 2);
        }
    }

    private int mode(ClientLevel level) {
        return observedLevel.get() == level ? nightmareLevel : 0;
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 naturalFog, float brightness) {
        return switch (mode(Minecraft.getInstance().level)) {
            case 2 -> DEMONIC_FOG;
            case 1 -> NIGHTMARE_FOG;
            default -> DREAM_FOG;
        };
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    @Nullable
    public float[] getSunriseColor(float time, float partialTick) {
        return null;
    }

    @Override
    public void adjustLightmapColors(ClientLevel level, float partialTick, float skyDarken, float blockLightRedFlicker,
                                     float skyLight, int blockLightLevel, int skyLightLevel, Vector3f colors) {
        if (mode(level) == 0) {
            float daylight = LightTexture.getBrightness(level.dimensionType(), skyLightLevel)
                    * (1 - level.getRainLevel(partialTick) * 0.3F);
            float illumination = Math.max(0, daylight - skyLight);
            colors.add(illumination, illumination, illumination);
        }
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack pose, Camera camera,
                             Matrix4f projection, boolean foggy, Runnable setupFog) {
        setupFog.run();
        FogType fog = camera.getFluidInCamera();
        if (foggy || fog == FogType.POWDER_SNOW || fog == FogType.LAVA
                || camera.getEntity() instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS))) {
            return true;
        }

        boolean moon = mode(level) > 0;
        int phase = level.getMoonPhase();
        float minU = moon ? phase % 4 / 4F : 0;
        float minV = moon ? phase / 4 / 2F : 0;
        float maxU = minU + (moon ? 0.25F : 1);
        float maxV = minV + (moon ? 0.5F : 1);
        float radius = moon ? 20 : 30;
        float alpha = 1 - level.getRainLevel(partialTick);

        FogRenderer.levelFogColor();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, moon ? MOON : SUN);
        Matrix4f view = pose.last().pose();
        BufferBuilder quad = Tesselator.getInstance().getBuilder();
        quad.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        quad.vertex(view, -radius, 100, -radius).uv(minU, minV).endVertex();
        quad.vertex(view, radius, 100, -radius).uv(maxU, minV).endVertex();
        quad.vertex(view, radius, 100, radius).uv(maxU, maxV).endVertex();
        quad.vertex(view, -radius, 100, radius).uv(minU, maxV).endVertex();
        BufferUploader.drawWithShader(quad.end());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        return true;
    }
}
