package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.TreefydModel;
import art.arcane.witchery.entity.Treefyd;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class TreefydRenderer extends MobRenderer<Treefyd, TreefydModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/treefyd.png");

    public TreefydRenderer(EntityRendererProvider.Context context) {
        super(context, new TreefydModel(context.bakeLayer(TreefydModel.LAYER)), 0.5F);
    }

    @Override
    protected void setupRotations(Treefyd treefyd, PoseStack pose, float age, float yaw, float partialTick) {
        super.setupRotations(treefyd, pose, age, yaw, partialTick);
        if (treefyd.walkAnimation.speed() >= 0.01) {
            float cycle = treefyd.walkAnimation.position(partialTick) + 6;
            pose.mulPose(Axis.ZP.rotationDegrees(5 * Mth.triangleWave(cycle, 13)));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Treefyd treefyd) {
        return TEXTURE;
    }
}
