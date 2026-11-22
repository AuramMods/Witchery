package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.WingedMonkeyModel;
import art.arcane.witchery.entity.WingedMonkey;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class WingedMonkeyRenderer extends MobRenderer<WingedMonkey, WingedMonkeyModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/wingedmonkey.png");

    public WingedMonkeyRenderer(EntityRendererProvider.Context context) {
        super(context, new WingedMonkeyModel(context.bakeLayer(WingedMonkeyModel.LAYER)), .5F);
    }

    @Override
    public ResourceLocation getTextureLocation(WingedMonkey monkey) {
        return TEXTURE;
    }
}
