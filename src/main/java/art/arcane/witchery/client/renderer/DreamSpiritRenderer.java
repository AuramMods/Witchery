package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.DreamSpiritModel;
import art.arcane.witchery.entity.DreamSpirit;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class DreamSpiritRenderer extends MobRenderer<DreamSpirit, DreamSpiritModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/spirit.png");

    public DreamSpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new DreamSpiritModel(context.bakeLayer(DreamSpiritModel.LAYER)), 0);
    }

    @Override
    public ResourceLocation getTextureLocation(DreamSpirit spirit) {
        return TEXTURE;
    }

    @Override
    protected void scale(DreamSpirit spirit, PoseStack pose, float partialTick) {
        pose.scale(0.5F, 0.5F, 0.5F);
    }

    @Nullable
    @Override
    protected RenderType getRenderType(DreamSpirit spirit, boolean visible, boolean translucent, boolean glowing) {
        return visible || translucent ? RenderType.entityTranslucent(TEXTURE) : super.getRenderType(spirit, false, false, glowing);
    }
}
