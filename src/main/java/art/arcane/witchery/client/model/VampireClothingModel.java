package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Vampire;
import art.arcane.witchery.item.VampireClothingItem;
import art.arcane.witchery.item.VampireClothingItem.Style;
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
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class VampireClothingModel extends HumanoidModel<LivingEntity> {
    private final Style style;
    private boolean hideCape;

    private VampireClothingModel(Style style, ModelPart root) {
        super(root);
        this.style = style;
    }

    public static ModelLayerLocation layerId(Style style) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, style.path()), "armor");
    }

    public static IClientItemExtensions extension(VampireClothingItem clothing) {
        return new IClientItemExtensions() {
            private VampireClothingModel baked;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                                          HumanoidModel<?> original) {
                if (baked == null) {
                    var style = clothing.style();
                    baked = new VampireClothingModel(style, Minecraft.getInstance().getEntityModels().bakeLayer(layerId(style)));
                }
                baked.hideCape = entity instanceof Vampire;
                return baked;
            }
        };
    }

    public static LayerDefinition layer(Style style) {
        var mesh = HumanoidModel.createMesh(new CubeDeformation(style.armorType() == ArmorItem.Type.LEGGINGS ? 0.02F : 0.3F), 0);
        var root = mesh.getRoot();
        if (style == Style.HAT) {
            root.getChild("head").addOrReplaceChild("top_hat", CubeListBuilder.create()
                    .texOffs(0, 67).addBox(-4, -15.5F, -4, 8, 8, 8, new CubeDeformation(0.6F))
                    .texOffs(0, 85).addBox(-5, -7, -5, 10, 1, 10, new CubeDeformation(0.7F)), PartPose.ZERO);
        }
        if (style.armorType() == ArmorItem.Type.CHESTPLATE) addCoat(root.getChild("body"), style.fitted());
        if (style == Style.KILT) {
            var body = root.getChild("body");
            int[] textures = {32, 68, 68, 68, 50};
            for (int i = 0; i < textures.length; i++) {
                body.addOrReplaceChild("skirt_" + i, CubeListBuilder.create().texOffs(26, textures[i])
                        .addBox(-4.5F, 0, -2.5F, 9, 12, 5), PartPose.offset(0, 11, 0));
            }
        }
        return LayerDefinition.create(mesh, 64, 96);
    }

    private static void addCoat(PartDefinition body, boolean fitted) {
        body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 33)
                .addBox(-6, 0, 2.5F, 12, 22, 1), PartPose.offsetAndRotation(0, 1, 0, 0.04555F, 0, 0));
        body.addOrReplaceChild("left_collar", CubeListBuilder.create().texOffs(0, 56)
                .addBox(-3.5F, -8, 4, 7, 7, 1), PartPose.rotation(-0.349F, 0.511F, 0.411F));
        body.addOrReplaceChild("right_collar", CubeListBuilder.create().texOffs(0, 56)
                .addBox(-3.5F, -8, 4, 7, 7, 1), PartPose.rotation(-0.349F, -0.511F, -0.411F));
        if (fitted) {
            body.addOrReplaceChild("fitted_front", CubeListBuilder.create().texOffs(16, 67)
                    .addBox(-4, -2, -5, 8, 4, 4), PartPose.offsetAndRotation(0, 2, 0, 0.7854F, 0, 0));
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        if (style == Style.HAT) hat.visible = false;
        if (style.armorType() == ArmorItem.Type.CHESTPLATE) {
            ModelPart cape = body.getChild("cape");
            ModelPart left = body.getChild("left_collar");
            ModelPart right = body.getChild("right_collar");
            cape.visible = left.visible = right.visible = !hideCape;
            cape.xRot = crouching ? 0.6F : 0.04555F + Math.max(0, Math.abs(leftLeg.xRot) - 0.1F) * 0.35F;
            left.xRot = right.xRot = head.xRot < -0.15F ? head.xRot - 0.15F : -0.3F;
        } else if (style == Style.KILT) {
            float back = Math.max(leftLeg.xRot, rightLeg.xRot);
            float front = Math.min(leftLeg.xRot, rightLeg.xRot);
            for (int i = 0; i < 5; i++) {
                var panel = body.getChild("skirt_" + i);
                panel.visible = !riding;
                panel.xRot = back + (front - back) * i / 4;
                panel.y = crouching ? 8 : 11;
                panel.z = crouching ? 4 : 0;
            }
        }
        super.renderToBuffer(pose, vertices, light, overlay, red, green, blue, alpha);
    }
}
