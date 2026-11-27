package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.CovenWitch;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.PotionItem;
import org.joml.Quaternionf;

import java.util.List;
import java.util.Map;

public final class CovenWitchRenderer extends MobRenderer<CovenWitch, WitchModel<CovenWitch>> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[5];

    static {
        for (int skin = 0; skin < TEXTURES.length; skin++) {
            TEXTURES[skin] = ResourceLocation.fromNamespaceAndPath(Witchery.MODID,
                    "textures/entities/covenwitch" + (skin + 1) + ".png");
        }
    }

    public CovenWitchRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchModel<>(modelRoot(context)), 0.5F);
        addLayer(new HeldPotionLayer(this, context.getItemInHandRenderer()));
    }

    private static ModelPart modelRoot(EntityRendererProvider.Context context) {
        var root = context.bakeLayer(ModelLayers.WITCH);
        var head = root.getChild("head");
        // The witch has its own hat. The inherited modern villager brim adds an extra mesh.
        head.getChild("hat").getChild("hat_rim").visible = false;
        var parts = new MeshDefinition().getRoot();
        // The legacy witch baked both sleeves with the same texture orientation.
        var arms = parts.addOrReplaceChild("arms", CubeListBuilder.create()
                .texOffs(44, 22).addBox(-8, -2, -2, 4, 8, 4).addBox(4, -2, -2, 4, 8, 4)
                .texOffs(40, 38).addBox(-4, 2, -2, 8, 4, 4), root.getChild("arms").getInitialPose()).bake(64, 128);
        var body = parts.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 20)
                .addBox(-4, 0, -3, 8, 12, 6), PartPose.ZERO);
        body.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(0, 38)
                .addBox(-4, 0, -3, 8, 18, 6, new CubeDeformation(0.5F)), PartPose.ZERO);
        return new ModelPart(List.of(), Map.of("arms", arms, "head", head, "body", body.bake(64, 128),
                "right_leg", root.getChild("right_leg"), "left_leg", root.getChild("left_leg")));
    }

    @Override
    public void render(CovenWitch witch, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int light) {
        getModel().setHoldingItem(!witch.getMainHandItem().isEmpty());
        super.render(witch, yaw, partialTick, pose, buffer, light);
    }

    @Override
    protected void scale(CovenWitch witch, PoseStack pose, float partialTick) {
        pose.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public ResourceLocation getTextureLocation(CovenWitch witch) {
        return TEXTURES[witch.skin()];
    }

    private static final class HeldPotionLayer extends WitchItemLayer<CovenWitch> {
        private final ItemInHandRenderer itemRenderer;

        private HeldPotionLayer(CovenWitchRenderer parent, ItemInHandRenderer itemRenderer) {
            super(parent, itemRenderer);
            this.itemRenderer = itemRenderer;
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, CovenWitch witch,
                           float swing, float amount, float partialTick, float age, float yaw, float pitch) {
            var stack = witch.getMainHandItem();
            if (!(stack.getItem() instanceof PotionItem)) {
                super.render(pose, buffers, light, witch, swing, amount, partialTick, age, yaw, pitch);
                return;
            }

            pose.pushPose();
            // The old bottle follows the nose rotation at its unshifted pivot, independently of head aim.
            var nose = getParentModel().getNose();
            pose.translate(0, -0.125, 0);
            pose.mulPose(new Quaternionf().rotationZYX(nose.zRot, nose.yRot, nose.xRot));
            pose.translate(0.1875, 0.71875, 0.03125);
            pose.scale(0.375F, 0.375F, 0.375F);
            pose.mulPose(Axis.ZP.rotationDegrees(60));
            pose.mulPose(Axis.XP.rotationDegrees(-90));
            pose.mulPose(Axis.ZP.rotationDegrees(20));
            pose.mulPose(Axis.XP.rotationDegrees(-15));
            pose.mulPose(Axis.ZP.rotationDegrees(40));

            // Convert the old extruded icon transform to Minecraft's centered baked item coordinates.
            pose.translate(0, -0.3F, 0);
            pose.scale(1.5F, 1.5F, 1.5F);
            pose.mulPose(Axis.YP.rotationDegrees(50));
            pose.mulPose(Axis.ZP.rotationDegrees(-25));
            pose.translate(-0.4375, 0.4375, -0.03125);
            itemRenderer.renderItem(witch, stack, ItemDisplayContext.NONE, false, pose, buffers, light);
            pose.popPose();
        }
    }
}
