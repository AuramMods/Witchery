package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitchHunter;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;

public final class WitchHunterRenderer extends HumanoidMobRenderer<WitchHunter, HumanoidModel<WitchHunter>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "witchhunter"), "main");
    private static final ResourceLocation[] SKINS = {skin(1), skin(2), skin(3)};

    public WitchHunterRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER)) {
            @Override
            public void prepareMobModel(WitchHunter hunter, float swing, float amount, float partialTick) {
                boolean aiming = hunter.isAggressive() && hunter.hasCrossbow();
                rightArmPose = aiming && hunter.getMainArm() == HumanoidArm.RIGHT ? ArmPose.CROSSBOW_HOLD : ArmPose.EMPTY;
                leftArmPose = aiming && hunter.getMainArm() == HumanoidArm.LEFT ? ArmPose.CROSSBOW_HOLD : ArmPose.EMPTY;
                super.prepareMobModel(hunter, swing, amount, partialTick);
            }
        }, 0.5F);
        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    public static LayerDefinition layer() {
        var mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        var root = mesh.getRoot();
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-1, -2, -2, 4, 12, 4), PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(1.9F, 12, 0));
        var head = root.getChild("head");
        head.addOrReplaceChild("hat_brim", CubeListBuilder.create().texOffs(0, 32)
                .addBox(-7, 0, -7, 14, 1, 14), PartPose.offset(0, -6, 0));
        head.addOrReplaceChild("hat_top", CubeListBuilder.create().texOffs(33, 48)
                .addBox(-3, 0, -3, 6, 2, 6), PartPose.offset(0, -10, 0));
        root.getChild("body").addOrReplaceChild("coat_bottom", CubeListBuilder.create().texOffs(1, 47)
                .addBox(-5, 0, -2.5F, 10, 11, 5), PartPose.offset(0, 11, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ResourceLocation getTextureLocation(WitchHunter hunter) {
        return SKINS[hunter.hunterType()];
    }

    private static ResourceLocation skin(int number) {
        return ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/witchhunter" + number + ".png");
    }
}
