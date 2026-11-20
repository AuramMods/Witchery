package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitcheryBolt;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class WitcheryBoltRenderer extends ArrowRenderer<WitcheryBolt> {
    private static final ResourceLocation STAKE = texture("bolt");
    private static final ResourceLocation ANTI_MAGIC = texture("bolt2");
    private static final ResourceLocation HOLY = texture("bolt3");
    private static final ResourceLocation SILVER = texture("bolt4");

    public WitcheryBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(WitcheryBolt bolt) {
        return switch (bolt.kind()) {
            case STAKE -> STAKE;
            case ANTI_MAGIC, POWERED_ANTI_MAGIC -> ANTI_MAGIC;
            case HOLY -> HOLY;
            case SILVER -> SILVER;
        };
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/" + name + ".png");
    }
}
