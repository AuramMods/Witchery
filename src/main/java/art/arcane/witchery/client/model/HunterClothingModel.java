package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.item.HunterClothingItem.Piece;
import art.arcane.witchery.item.HunterClothingItem.Upgrade;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class HunterClothingModel extends HumanoidModel<LivingEntity> {
    private final Piece piece;

    private HunterClothingModel(Piece piece, ModelPart root) {
        super(root);
        this.piece = piece;
    }

    public static ModelLayerLocation layerId(Piece piece) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, piece.path(Upgrade.PLAIN)), "armor");
    }

    public static IClientItemExtensions extension(HunterClothingItem clothing) {
        return new IClientItemExtensions() {
            private HunterClothingModel baked;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                                          HumanoidModel<?> original) {
                if (baked == null) {
                    Piece piece = clothing.piece();
                    baked = new HunterClothingModel(piece, Minecraft.getInstance().getEntityModels().bakeLayer(layerId(piece)));
                }
                return baked;
            }
        };
    }

    public static LayerDefinition layer(Piece piece) {
        CubeDeformation cloth = new CubeDeformation(piece == Piece.HAT || piece == Piece.LEGS ? 0.01F : 0.4F);
        var mesh = HumanoidModel.createMesh(cloth, 0);
        var root = mesh.getRoot();
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-1, -2, -2, 4, 12, 4, cloth), PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-2, 0, -2, 4, 12, 4, cloth), PartPose.offset(1.9F, 12, 0));
        if (piece == Piece.HAT) {
            var brim = root.getChild("head").addOrReplaceChild("brim", CubeListBuilder.create().texOffs(0, 50)
                    .addBox(-6.5F, 0, -6.5F, 13, 1, 13, new CubeDeformation(0.32F)), PartPose.offset(0, -6, 0));
            var mid = brim.addOrReplaceChild("mid", CubeListBuilder.create().texOffs(40, 52)
                    .addBox(-4, 0, -4, 8, 2, 8, new CubeDeformation(0.52F)), PartPose.offset(0, -2, 0));
            mid.addOrReplaceChild("top", CubeListBuilder.create().texOffs(12, 41)
                    .addBox(-3.5F, 0, -3.5F, 7, 2, 7, new CubeDeformation(0.52F)), PartPose.offset(0, -2, 0));
        }
        if (piece == Piece.COAT) {
            root.getChild("body").addOrReplaceChild("coat_tails", CubeListBuilder.create().texOffs(41, 33)
                    .addBox(-5.5F, 0, -3, 11, 10, 6, new CubeDeformation(-0.3F)), PartPose.offset(0, 12, 0));
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        if (piece == Piece.LEGS) body.visible = false;
        super.renderToBuffer(pose, vertices, light, overlay, red, green, blue, alpha);
    }
}
