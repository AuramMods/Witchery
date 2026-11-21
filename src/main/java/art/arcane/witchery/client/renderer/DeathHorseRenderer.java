package art.arcane.witchery.client.renderer;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class DeathHorseRenderer extends UndeadHorseRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/horse/horse_skeleton.png");

    public DeathHorseRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.SKELETON_HORSE);
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractHorse horse) {
        return TEXTURE;
    }
}
