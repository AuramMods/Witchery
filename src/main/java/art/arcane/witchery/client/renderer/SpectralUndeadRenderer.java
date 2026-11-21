package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.SpectralUndeadModel;
import art.arcane.witchery.entity.SpectralUndead;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class SpectralUndeadRenderer extends MobRenderer<SpectralUndead, SpectralUndeadModel> {
    private final ResourceLocation texture;

    public SpectralUndeadRenderer(EntityRendererProvider.Context context, String name, boolean poltergeist) {
        super(context, new SpectralUndeadModel(context.bakeLayer(poltergeist
                ? SpectralUndeadModel.POLTERGEIST_LAYER : SpectralUndeadModel.ROBED_LAYER), poltergeist), 0);
        texture = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/" + name + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(SpectralUndead entity) {
        return texture;
    }

    @Override
    @Nullable
    protected RenderType getRenderType(SpectralUndead entity, boolean visible, boolean translucent, boolean glowing) {
        return visible || translucent ? RenderType.entityTranslucent(texture)
                : super.getRenderType(entity, false, false, glowing);
    }
}
