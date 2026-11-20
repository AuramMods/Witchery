package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.LordOfTormentModel;
import art.arcane.witchery.entity.LordOfTorment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public final class LordOfTormentRenderer extends MobRenderer<LordOfTorment, LordOfTormentModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/lordoftorment.png");

    public LordOfTormentRenderer(EntityRendererProvider.Context context) {
        super(context, new LordOfTormentModel(context.bakeLayer(LordOfTormentModel.LAYER)), 0.5F);
    }

    @Override
    protected int getBlockLightLevel(LordOfTorment entity, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(LordOfTorment entity) {
        return TEXTURE;
    }
}
