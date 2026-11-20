package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.CovenWitch;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class CovenWitchRenderer extends MobRenderer<CovenWitch, WitchModel<CovenWitch>> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[5];

    static {
        for (int skin = 0; skin < TEXTURES.length; skin++) {
            TEXTURES[skin] = ResourceLocation.fromNamespaceAndPath(Witchery.MODID,
                    "textures/entities/covenwitch" + (skin + 1) + ".png");
        }
    }

    public CovenWitchRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchModel<>(context.bakeLayer(ModelLayers.WITCH)), 0.5F);
    }

    @Override
    protected void scale(CovenWitch witch, PoseStack pose, float partialTick) {
        pose.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public ResourceLocation getTextureLocation(CovenWitch witch) {
        return TEXTURES[witch.skin()];
    }
}
