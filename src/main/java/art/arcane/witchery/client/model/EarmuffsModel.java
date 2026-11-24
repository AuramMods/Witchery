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

public final class EarmuffsModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "earmuffs"), "armor");

    private EarmuffsModel() {}

    public static LayerDefinition layer() {
        var mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(46, 38).addBox(-4, -10, -0.5F, 8, 1, 1)
                .texOffs(33, 32).addBox(-6, -6, -2, 2, 4, 4)
                .texOffs(33, 32).addBox(4, -6, -2, 2, 4, 4)
                .texOffs(46, 32).addBox(4, -10, -0.5F, 1, 4, 1)
                .texOffs(46, 32).addBox(-5, -10, -0.5F, 1, 4, 1), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static IClientItemExtensions extension() {
        return new IClientItemExtensions() {
            private HumanoidModel<LivingEntity> baked;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
                                                          HumanoidModel<?> original) {
                if (baked == null) {
                    baked = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER));
                }
                return baked;
            }
        };
    }
}
