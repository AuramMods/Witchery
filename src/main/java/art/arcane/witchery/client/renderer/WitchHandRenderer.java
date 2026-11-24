package art.arcane.witchery.client.renderer;

import art.arcane.witchery.client.model.DeathModel;
import art.arcane.witchery.item.DeathHandItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class WitchHandRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ModelLayerLocation WITCH_HAND = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("witchery", "witchhand"), "main");
    public static final ModelLayerLocation DEATH_HAND = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("witchery", "deathshand"), "main");
    private final ResourceLocation texture;
    private final ModelPart model;
    private final boolean skeletal;

    public WitchHandRenderer(Minecraft minecraft, boolean skeletal) {
        super(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        this.skeletal = skeletal;
        model = minecraft.getEntityModels().bakeLayer(skeletal ? DEATH_HAND : WITCH_HAND);
        texture = ResourceLocation.fromNamespaceAndPath("witchery",
                skeletal ? "textures/entities/deathshand.png" : "textures/entities/witchhand.png");
    }

    public static LayerDefinition layer(boolean skeletal) {
        var shape = skeletal ? new CubeDeformation(-0.3F) : CubeDeformation.NONE;
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        var hand = root.addOrReplaceChild("hand", CubeListBuilder.create(), PartPose.ZERO);
        var firstPerson = hand.addOrReplaceChild("first_person", CubeListBuilder.create(), PartPose.ZERO);
        var thirdPerson = hand.addOrReplaceChild("third_person", CubeListBuilder.create(), PartPose.ZERO);
        hand.addOrReplaceChild("wrist", CubeListBuilder.create().texOffs(0, 0).mirror(skeletal)
                .addBox(-3, 0, 0, 5, 5, 2), PartPose.ZERO);
        addPart(hand, shape, skeletal, "palm_upper", 0, 7, 0, 0, 0, 5, 1, 5,
                PartPose.offsetAndRotation(-3, 0, -5, 0, 0, 0));
        addPart(hand, shape, skeletal, "palm_lower", 0, 13, 0, 0, 0, 5, 2, 3,
                PartPose.offsetAndRotation(-3, 1, -3, 0, 0, 0));
        addPart(hand, shape, skeletal, "finger1_upper", 0, 18, 0, 0, -2, 1, 1, 4,
                PartPose.offsetAndRotation(-3, 1, -7, 0.4833219F, 0, 0));
        addPart(hand, shape, skeletal, "finger2_upper", 6, 19, 0, 0, -2, 1, 1, 4,
                PartPose.offsetAndRotation(-1, 1, -7, 0.4833219F, 0, 0));
        addPart(hand, shape, skeletal, "finger3_upper", 12, 18, 0, 0, -2, 1, 1, 4,
                PartPose.offsetAndRotation(1, 1, -7, 0.4833219F, 0, 0));
        addPart(hand, shape, skeletal, "finger1_lower", 0, 23, 0, 0, -4, 1, 1, 4,
                PartPose.offsetAndRotation(-3, 2, -9, 2.044824F, 0, 0));
        addPart(hand, shape, skeletal, "finger2_lower", 6, 24, 0, 0, -4, 1, 1, 4,
                PartPose.offsetAndRotation(-1, 2, -9, 2.044824F, 0, 0));
        addPart(hand, shape, skeletal, "finger3_lower", 12, 23, 0, 0, -4, 1, 1, 4,
                PartPose.offsetAndRotation(1, 2, -9, 2.044824F, 0, 0));
        addPart(firstPerson, shape, skeletal, "right_palm", 16, 0, 0, 0, 0, 2, 1, 6,
                PartPose.offsetAndRotation(2, 0, -5, 0, 0, 0));
        addPart(firstPerson, shape, skeletal, "right_finger_upper", 20, 7, 0, 0, -4, 1, 1, 4,
                PartPose.offsetAndRotation(3, 0, -5, -0.5205006F, 0, 0));
        addPart(firstPerson, shape, skeletal, "right_finger_lower", 20, 12, 0, 0, 0, 1, 1, 4,
                PartPose.offsetAndRotation(3, -1, -8, -2.732628F, 0, 0));
        addPart(firstPerson, shape, skeletal, "right_thumb_upper", 22, 17, 0, 0, 0, 1, 1, 3,
                PartPose.offsetAndRotation(3.5F, 1, 0, -2.7F, -0.7F, -0.669215F));
        addPart(firstPerson, shape, skeletal, "right_thumb_lower", 22, 21, 0, -1, -1, 1, 1, 3,
                PartPose.offsetAndRotation(5, 3, -2, -1.896109F, 0.2602503F, 0.3717861F));
        addPart(thirdPerson, shape, skeletal, "left_palm", 16, 0, 0, 0, 0, 2, 1, 6,
                PartPose.offsetAndRotation(-5, 0, -5, 0, 0, 0));
        addPart(thirdPerson, shape, skeletal, "left_finger_upper", 20, 7, 0, 0, -4, 1, 1, 4,
                PartPose.offsetAndRotation(-5, 0, -5, -0.5205006F, 0, 0));
        addPart(thirdPerson, shape, skeletal, "left_finger_lower", 20, 12, 0, 0, 0, 1, 1, 4,
                PartPose.offsetAndRotation(-5, -1, -8, -2.732628F, 0, 0));
        addPart(thirdPerson, shape, skeletal, "left_thumb_upper", 22, 17, 0, 0, 0, 1, 1, 3,
                PartPose.offsetAndRotation(-5, 1, 0, -1.7F, 0.8F, 0.148711F));
        addPart(thirdPerson, shape, skeletal, "left_thumb_lower", 22, 21, 0, 0, 0, 1, 1, 3,
                PartPose.offsetAndRotation(-6, 4, -1, -2.082002F, 0.0371828F, -0.6320403F));
        if (skeletal) {
            root.addOrReplaceChild("scythe", DeathModel.scythe(), PartPose.offsetAndRotation(0, -3, -5, 0, Mth.HALF_PI, 0));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addPart(PartDefinition parent, CubeDeformation shape, boolean mirror, String name,
                                   int u, int v, float x, float y, float z,
                                   float width, float height, float depth, PartPose pose) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).mirror(mirror)
                .addBox(x, y, z, width, height, depth, shape), pose);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        boolean firstPerson = context.firstPerson();
        ModelPart hand = model.getChild("hand");
        hand.getChild("first_person").visible = firstPerson;
        hand.getChild("third_person").visible = !firstPerson;
        if (skeletal) {
            boolean deployed = DeathHandItem.deployed(stack);
            curlOuterFinger(hand.getChild("first_person"), "right", 3, deployed);
            curlOuterFinger(hand.getChild("third_person"), "left", -5, deployed);
            model.getChild("scythe").visible = deployed;
        }
        var player = Minecraft.getInstance().player;
        boolean translucent = !skeletal && firstPerson && player != null && player.isInvisible();

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.scale(1, -1, -1);
        var renderType = translucent ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture);
        model.render(pose, buffers.getBuffer(renderType), light, overlay, 1, 1, 1, translucent ? 0.2F : 1);
        pose.popPose();
    }

    private static void curlOuterFinger(ModelPart hand, String side, int x, boolean deployed) {
        hand.getChild(side + "_finger_upper").setPos(x, deployed ? 4 : 0, deployed ? -4 : -5);
        hand.getChild(side + "_finger_lower").setPos(x, deployed ? 1 : -1, deployed ? -4 : -8);
    }
}
