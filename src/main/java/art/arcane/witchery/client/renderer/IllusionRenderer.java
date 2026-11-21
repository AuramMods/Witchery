package art.arcane.witchery.client.renderer;

import art.arcane.witchery.entity.Illusion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class IllusionRenderer<M extends EntityModel<Illusion>> extends MobRenderer<Illusion, M> {
    private final ResourceLocation texture;

    public IllusionRenderer(EntityRendererProvider.Context context, M model, String variant) {
        super(context, model, 0);
        texture = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/" + variant + "/" + variant + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(Illusion entity) {
        return texture;
    }

    @Override
    public boolean shouldRender(Illusion entity, Frustum frustum, double x, double y, double z) {
        var player = Minecraft.getInstance().player;
        return player != null && player.getGameProfile().getName().equals(entity.victimName())
                && super.shouldRender(entity, frustum, x, y, z);
    }
}
