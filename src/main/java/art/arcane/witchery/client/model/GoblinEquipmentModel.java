package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class GoblinEquipmentModel {
    public static final ModelLayerLocation OUTER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "goblin_equipment"), "outer");
    public static final ModelLayerLocation INNER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "goblin_equipment"), "inner");

    private GoblinEquipmentModel() {}

    public static LayerDefinition outerLayer() {
        var mesh = HumanoidModel.createMesh(new CubeDeformation(0.61F), 0);
        mesh.getRoot().getChild("body").addOrReplaceChild("quiver", CubeListBuilder.create().mirror()
                .texOffs(33, 0).addBox(-2, -3, 0, 4, 7, 1)
                .texOffs(44, 4).addBox(0.5F, -5, 0, 1, 2, 1)
                .texOffs(44, 4).addBox(-1.5F, -5, 0, 1, 2, 1)
                .texOffs(44, 0).addBox(-2, -7, 0, 4, 2, 1),
                PartPose.offsetAndRotation(0, 7, 2, 0, 0, -0.3490659F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition innerLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0), 64, 32);
    }

    public static IClientItemExtensions extension(boolean inner) {
        return new IClientItemExtensions() {
            private HumanoidModel<LivingEntity> baked;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                                           HumanoidModel<?> original) {
                if (baked == null) baked = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(inner ? INNER : OUTER));
                return baked;
            }
        };
    }
}
