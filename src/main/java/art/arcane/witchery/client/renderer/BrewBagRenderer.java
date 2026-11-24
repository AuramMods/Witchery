package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.BrewBagItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class BrewBagRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "brewbag"), "held");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/brewbottle.png");
    private final ModelPart bottle;
    private final ModelPart stopper;

    public BrewBagRenderer(Minecraft minecraft) {
        super(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
        ModelPart model = minecraft.getEntityModels().bakeLayer(LAYER);
        bottle = model.getChild("bottle");
        stopper = model.getChild("stopper");
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("bottle", CubeListBuilder.create().mirror()
                .texOffs(0, 14).addBox(-1.5F, -2, -1.5F, 3, 2, 3)
                .texOffs(2, 8).addBox(-1, -2.5F, -1, 2, 3, 2)
                .texOffs(4, 4).addBox(-0.5F, -4, -0.5F, 1, 2, 1), PartPose.ZERO);
        mesh.getRoot().addOrReplaceChild("stopper", CubeListBuilder.create()
                .texOffs(2, 0).addBox(-1, -4.5F, -1, 2, 1, 2), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        int color = BrewBagItem.color(stack);
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.scale(1, -1, -1);
        bottle.render(pose, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light, overlay, red, green, blue, 1);
        stopper.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, overlay);
        pose.popPose();
    }
}
