package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.HobgoblinModel;
import art.arcane.witchery.entity.Hobgoblin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public final class HobgoblinRenderer extends MobRenderer<Hobgoblin, HobgoblinModel> {
    private static final ResourceLocation[] SKINS = {skin("goblin2"), skin("goblin"), skin("goblin4"), skin("goblin3"), skin("goblin")};

    public HobgoblinRenderer(EntityRendererProvider.Context context) {
        super(context, new HobgoblinModel(context.bakeLayer(HobgoblinModel.LAYER)), 0.35F);
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Hobgoblin entity) {
        return SKINS[entity.profession()];
    }

    @Override
    public Vec3 getRenderOffset(Hobgoblin goblin, float partialTick) {
        Vec3 offset = super.getRenderOffset(goblin, partialTick);
        return goblin.isShiftKeyDown() ? offset.add(0, -0.125, 0) : offset;
    }

    private static ResourceLocation skin(String name) {
        return ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/" + name + ".png");
    }
}
