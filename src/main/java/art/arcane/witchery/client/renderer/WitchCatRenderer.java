package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;

public final class WitchCatRenderer extends CatRenderer {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/blackcat.png");

    public WitchCatRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.5F;
        layers.removeIf(CatCollarLayer.class::isInstance);
    }

    @Override
    protected void scale(Cat cat, PoseStack pose, float partialTick) {
        if (cat.isTame()) super.scale(cat, pose, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(Cat cat) {
        return TEXTURE;
    }
}
