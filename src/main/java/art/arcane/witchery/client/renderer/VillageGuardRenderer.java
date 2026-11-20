package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.VillageGuard;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.item.BowItem;

public final class VillageGuardRenderer extends HumanoidMobRenderer<VillageGuard, HumanoidModel<VillageGuard>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "villageguard"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/entities/villageguard.png");

    public VillageGuardRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER)) {
            @Override
            public void prepareMobModel(VillageGuard guard, float swing, float amount, float partialTick) {
                boolean aiming = guard.isAggressive() && guard.getMainHandItem().getItem() instanceof BowItem;
                rightArmPose = aiming && guard.getMainArm() == HumanoidArm.RIGHT ? ArmPose.BOW_AND_ARROW : ArmPose.EMPTY;
                leftArmPose = aiming && guard.getMainArm() == HumanoidArm.LEFT ? ArmPose.BOW_AND_ARROW : ArmPose.EMPTY;
                super.prepareMobModel(guard, swing, amount, partialTick);
            }
        }, 0.5F);
        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    public static LayerDefinition layer() {
        var mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        var head = mesh.getRoot().addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(28, 46).addBox(-4, -10, -4, 8, 10, 8), PartPose.ZERO);
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(52, 46).addBox(-1, -1, -6, 2, 4, 2),
                PartPose.offset(0, -2, 0));
        mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4)
                .texOffs(0, 38).addBox(-4, 0, -3, 8, 18, 6, new CubeDeformation(0.5F)), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    protected void scale(VillageGuard guard, PoseStack pose, float partialTick) {
        if (guard.getGuardType() == 1) pose.scale(1.2F, 1.3F, 1.2F);
    }

    @Override
    public ResourceLocation getTextureLocation(VillageGuard guard) {
        return TEXTURE;
    }
}
