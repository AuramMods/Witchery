package art.arcane.witchery.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class DeathClothingModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("witchery", "death_clothing"), "armor");

    private DeathClothingModel() {}

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().mirror()
                .texOffs(56, 0).addBox(-4, -8, -4, 8, 8, 8, new CubeDeformation(0.5F))
                .texOffs(0, 45).addBox(-4.5F, -8.5F, -4.5F, 9, 10, 9, new CubeDeformation(0.4F)), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().mirror()
                .texOffs(72, 16).addBox(-4, 0, -2, 8, 12, 4)
                .texOffs(36, 37).addBox(-4.5F, 0, -2.5F, 9, 22, 5, new CubeDeformation(0.1F)), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", sleeve(-3), PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("left_arm", sleeve(-1), PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("right_leg", boot(), PartPose.offset(-1.9F, 12, 0));
        root.addOrReplaceChild("left_leg", boot(), PartPose.offset(1.9F, 12, 0));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static CubeListBuilder sleeve(float x) {
        return CubeListBuilder.create().mirror()
                .texOffs(96, 16).addBox(x, -2, -2, 4, 12, 4, new CubeDeformation(0.05F))
                .texOffs(64, 50).addBox(x, -2, -2, 4, 10, 4, new CubeDeformation(0.1F));
    }

    private static CubeListBuilder boot() {
        return CubeListBuilder.create().mirror().texOffs(56, 16)
                .addBox(-2, 0, -2, 4, 12, 4, new CubeDeformation(0.05F));
    }

    public static IClientItemExtensions extension() {
        return new IClientItemExtensions() {
            private HumanoidModel<LivingEntity> baked;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                                          HumanoidModel<?> original) {
                if (baked == null) baked = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
                return baked;
            }
        };
    }
}
