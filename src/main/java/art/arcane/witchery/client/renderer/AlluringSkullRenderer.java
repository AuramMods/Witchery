package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.AlluringSkullBlock;
import art.arcane.witchery.block.entity.AlluringSkullBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.RotationSegment;

public final class AlluringSkullRenderer implements BlockEntityRenderer<AlluringSkullBlockEntity> {
    private static final ResourceLocation INACTIVE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/alluringskull.png");
    private static final ResourceLocation ACTIVE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/alluringskull2.png");
    private final SkullModel model;

    public AlluringSkullRenderer(BlockEntityRendererProvider.Context context) {
        model = new SkullModel(context.bakeLayer(ModelLayers.SKELETON_SKULL));
    }

    @Override
    public void render(AlluringSkullBlockEntity skull, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        var state = skull.getBlockState();
        Direction facing = state.getValue(AlluringSkullBlock.FACING);
        boolean onFloor = facing == Direction.UP;
        int angle = onFloor ? state.getValue(AlluringSkullBlock.ROTATION) : RotationSegment.convertToSegment(facing.getOpposite());
        ResourceLocation texture = state.getValue(AlluringSkullBlock.ACTIVE) ? ACTIVE : INACTIVE;
        SkullBlockRenderer.renderSkull(onFloor ? null : facing, RotationSegment.convertToDegrees(angle), 0,
                pose, buffers, light, model, RenderType.entityCutoutNoCullZOffset(texture));
    }
}
