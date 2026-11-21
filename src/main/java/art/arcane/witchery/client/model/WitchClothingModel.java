package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.item.WitchClothingItem.Style;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.Locale;

public final class WitchClothingModel {
    private static final CubeDeformation CLOTH = new CubeDeformation(0.61F);

    private WitchClothingModel() {}

    public static ModelLayerLocation layerId(Style style) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Witchery.MODID,
                style.name().toLowerCase(Locale.ROOT)), "armor");
    }

    public static IClientItemExtensions extension(WitchClothingItem clothing) {
        return new IClientItemExtensions() {
            private HumanoidModel<?> baked;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                                          HumanoidModel<?> original) {
                if (baked == null) {
                    // Forge installs the extension while Item's constructor is still running.
                    var parts = Minecraft.getInstance().getEntityModels().bakeLayer(layerId(clothing.style()));
                    baked = new HumanoidModel<>(parts);
                }
                return baked;
            }
        };
    }

    public static LayerDefinition layer(Style style) {
        if (style == Style.BITING_BELT || style == Style.BARK_BELT) {
            var beltCloth = new CubeDeformation(0.45F);
            var beltMesh = HumanoidModel.createMesh(beltCloth, 0);
            var beltRoot = beltMesh.getRoot();
            beltRoot.getChild("body").addOrReplaceChild("hem", CubeListBuilder.create().mirror().texOffs(43, 46)
                    .addBox(-4, 0, -2, 8, 6, 4, beltCloth), PartPose.offset(0, 12, 0));
            beltRoot.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
            beltRoot.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
            return LayerDefinition.create(beltMesh, 128, 64);
        }
        MeshDefinition mesh = HumanoidModel.createMesh(CLOTH, 0);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        switch (style) {
            case WITCH_HAT -> head.addOrReplaceChild("pointed_hat", CubeListBuilder.create().mirror()
                    .texOffs(0, 49).addBox(-7, -7, -7, 14, 1, 14)
                    .texOffs(0, 36).addBox(-5, -9, -5, 10, 2, 10)
                    .texOffs(31, 34).addBox(-3, -14, -3, 6, 5, 6)
                    .texOffs(50, 34).addBox(-1, -17, -1, 2, 3, 2), PartPose.ZERO);
            case BABA_HAT -> addBabaHat(head);
            case WITCH_ROBE, NECROMANCER_ROBE -> {
                root.getChild("body").addOrReplaceChild("robe_hem", CubeListBuilder.create().mirror().texOffs(43, 46)
                        .addBox(-4, 0, -2, 8, 6, 4, CLOTH), PartPose.offset(0, 12, 0));
                if (style == Style.NECROMANCER_ROBE) {
                    CubeDeformation padding = CLOTH.extend(0.1F);
                    root.getChild("right_arm").addOrReplaceChild("shoulder_pad", CubeListBuilder.create().mirror()
                            .texOffs(61, 32).addBox(0, 0, 0, 5, 1, 6, padding), PartPose.offset(-4, -2, -3));
                    root.getChild("left_arm").addOrReplaceChild("shoulder_pad", CubeListBuilder.create().mirror()
                            .texOffs(61, 39).addBox(0, 0, 0, 5, 1, 6, padding), PartPose.offset(0, -2, -3));
                }
            }
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static void addBabaHat(PartDefinition head) {
        PartDefinition brim = head.addOrReplaceChild("baba_hat", CubeListBuilder.create().mirror()
                .texOffs(72, 48).addBox(0, 0, 0, 14, 2, 14, new CubeDeformation(0.52F)), PartPose.offset(-7, -8, -7));
        PartDefinition crown = brim.addOrReplaceChild("crown", CubeListBuilder.create()
                        .texOffs(83, 29).addBox(0, 0, 0, 7, 4, 7, new CubeDeformation(0.4F)),
                PartPose.offsetAndRotation(3.75F, -4, 4, -0.05235988F, 0, 0.02617994F));
        PartDefinition point = crown.addOrReplaceChild("point", CubeListBuilder.create().texOffs(83, 40)
                        .addBox(0, 0, 0, 4, 4, 4),
                PartPose.offsetAndRotation(1.75F, -4, 2, -0.10471976F, 0, 0.05235988F));
        point.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(81, 48)
                        .addBox(0, 0, 0, 1, 2, 1, new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(1.75F, -2, 2, -0.20943952F, 0, 0.10471976F));
    }
}
