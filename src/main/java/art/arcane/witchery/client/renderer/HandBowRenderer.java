package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.HandBowItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class HandBowRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/handbow.png");
    private final ModelPart bow = geometry().bakeRoot();

    public HandBowRenderer(Minecraft minecraft) {
        super(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        HandBowItem.Ammo ammo = HandBowItem.loaded(stack);
        int drawnTicks = ammo == null ? windUpTicks(stack) : 20;
        boolean drawn = drawnTicks > 10;
        boolean drawing = drawnTicks > 5;
        setLimbs("_left", drawn, drawing);
        setLimbs("_right", drawn, drawing);
        bow.getChild("string_center").z = drawn ? 1 : drawing ? -1 : -3.25F;
        bow.getChild("stake").visible = ammo == HandBowItem.Ammo.STAKE;
        bow.getChild("antimagic").visible = ammo == HandBowItem.Ammo.ANTI_MAGIC;
        bow.getChild("holy").visible = ammo == HandBowItem.Ammo.HOLY;
        bow.getChild("silver").visible = ammo == HandBowItem.Ammo.SILVER;
        bow.getChild("splitting").visible = ammo == HandBowItem.Ammo.SPLITTING;
        bow.getChild("splitting_head").visible = ammo == HandBowItem.Ammo.SPLITTING;

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        var vertices = ItemRenderer.getFoilBufferDirect(buffers, RenderType.entityCutout(TEXTURE), false, stack.hasFoil());
        bow.render(pose, vertices, light, overlay);
        pose.popPose();
    }

    private void setLimbs(String side, boolean drawn, boolean drawing) {
        bow.getChild("inner_arm" + side).z = drawing ? -6 : -7;
        bow.getChild("outer_arm" + side).z = drawn ? -4 : drawing ? -5 : -6;
        bow.getChild("inner_string" + side).z = drawn ? -1 : drawing ? -2 : -4;
        bow.getChild("middle_string" + side).z = drawn ? 0 : drawing ? -2 : -4;
        bow.getChild("outer_string" + side).z = drawn ? -2 : drawing ? -3 : -4;
    }

    private static int windUpTicks(ItemStack stack) {
        var player = Minecraft.getInstance().player;
        return player != null && player.isShiftKeyDown() && player.getUseItem() == stack
                ? player.getTicksUsingItem() : 0;
    }

    private static LayerDefinition geometry() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        part(root, "stock_top", 2, 2, -1, 0, -5, 2, 1, 7, 0, 0, 0);
        part(root, "stock_bottom", 0, 10, -0.5F, 0, -6, 1, 1, 8, 0, 1, 0);
        part(root, "catch", 1, 11, -0.5F, 0, 0, 1, 1, 1, 0, -1, 1);
        part(root, "grip", 0, 3, -0.5F, 0, -1, 1, 3, 2, 0, 2, 0);
        part(root, "cross", 1, 19, -3, 0, 0, 6, 1, 2, 0, -0.3F, -7);
        part(root, "outer_arm_right", 0, 14, -1, 0, 0, 1, 1, 2, -4, 0, -4);
        part(root, "inner_arm_right", 0, 14, -1, 0, 0, 1, 1, 2, -3, -0.15F, -6);
        part(root, "outer_arm_left", 0, 14, 0, 0, 0, 1, 1, 2, 4, 0, -4);
        part(root, "inner_arm_left", 0, 14, 0, 0, 0, 1, 1, 2, 3, -0.15F, -6);
        part(root, "inner_string_right", 0, 0, -1, 0, 0, 1, 1, 1, -2, -0.05F, -1);
        part(root, "middle_string_right", 0, 0, -1, 0, 0, 1, 1, 1, -1, -0.1F, 0);
        part(root, "outer_string_right", 0, 0, -1, 0, 0, 1, 1, 1, -3, 0, -2);
        part(root, "inner_string_left", 0, 0, 0, 0, 0, 1, 1, 1, 2, -0.05F, -1);
        part(root, "middle_string_left", 0, 0, 0, 0, 0, 1, 1, 1, 1, -0.1F, 0);
        part(root, "outer_string_left", 0, 0, 0, 0, 0, 1, 1, 1, 3, 0, -2);
        root.addOrReplaceChild("string_center", CubeListBuilder.create().texOffs(4, 0).addBox(-1.5F, 0, -0.5F, 3, 1, 1),
                PartPose.offsetAndRotation(0, -0.1F, 1, 0, (float) Math.toRadians(1), 0));
        part(root, "stake", 0, 22, -0.5F, 0.5F, -6, 1, 1, 9, 0, -1, -2);
        part(root, "antimagic", 20, 22, -0.5F, 0.5F, -6, 1, 1, 9, 0, -1, -2);
        part(root, "holy", 40, 22, -0.5F, 0.5F, -6, 1, 1, 9, 0, -1, -2);
        part(root, "splitting", 20, 12, -0.5F, 0.5F, -6, 1, 1, 9, 0, -1, -2);
        part(root, "splitting_head", 17, 11, -0.5F, 0.5F, -6, 2, 1, 4, -0.5F, -1.5F, -1);
        part(root, "silver", 40, 12, -0.5F, 0.5F, -6, 1, 1, 9, 0, -1, -2);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void part(PartDefinition root, String name, int u, int v, float x, float y, float z, int width, int height,
                             int length, float pivotX, float pivotY, float pivotZ) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, width, height, length),
                PartPose.offset(pivotX, pivotY, pivotZ));
    }
}
