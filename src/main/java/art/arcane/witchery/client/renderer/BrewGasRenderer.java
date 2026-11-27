package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.phys.Vec3;

public final class BrewGasRenderer implements BlockEntityRenderer<BrewFluidBlockEntity> {
    private static final ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "block/brewgas");
    private static final float INSET = 0.001F;
    private static final int INNER_ALPHA = 51;
    private static final int OUTER_ALPHA = 102;
    private static final Direction[] WALLS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final RenderType SURFACE = GasRenderState.create();

    public BrewGasRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public boolean shouldRender(BrewFluidBlockEntity fluid, Vec3 camera) {
        // Section visibility already limits the cloud, as it did for the original chunk mesh.
        return true;
    }

    @Override
    public void render(BrewFluidBlockEntity fluid, float partialTick, PoseStack matrices,
                       MultiBufferSource buffers, int light, int overlay) {
        if (!(fluid.getBlockState().getBlock() instanceof BrewDispersalBlock block) || !block.isGas()) return;
        var level = fluid.getLevel();
        if (level == null) return;
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(SPRITE);
        renderMesh(level, fluid.getBlockPos(), block, fluid.color(), sprite, matrices.last(), buffers.getBuffer(SURFACE));
    }

    static void renderMesh(BlockAndTintGetter level, BlockPos pos, BrewDispersalBlock block, int color,
                           TextureAtlasSprite sprite, PoseStack.Pose pose, VertexConsumer vertices) {
        int topColor = faceColor(level, pos.above(), block, color);
        int topLight = light(level, pos);
        float top = 1.0F - INSET;
        float u4 = sprite.getU(4.0D);
        float u12 = sprite.getU(12.0D);
        float v4 = sprite.getV(4.0D);
        float v12 = sprite.getV(12.0D);
        vertex(pose, vertices, 0, top, 0, u4, v4, topColor, topLight);
        vertex(pose, vertices, 0, top, 1, u4, v12, topColor, topLight);
        vertex(pose, vertices, 1, top, 1, u12, v12, topColor, topLight);
        vertex(pose, vertices, 1, top, 0, u12, v4, topColor, topLight);
        vertex(pose, vertices, 0, top, 0, u4, v4, topColor, topLight);
        vertex(pose, vertices, 1, top, 0, u12, v4, topColor, topLight);
        vertex(pose, vertices, 1, top, 1, u12, v12, topColor, topLight);
        vertex(pose, vertices, 0, top, 1, u4, v12, topColor, topLight);

        BlockPos below = pos.below();
        int bottomColor = faceColor(level, below, block, color);
        int bottomLight = light(level, below);
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        vertex(pose, vertices, 0, INSET, 1, u0, v1, bottomColor, bottomLight);
        vertex(pose, vertices, 0, INSET, 0, u0, v0, bottomColor, bottomLight);
        vertex(pose, vertices, 1, INSET, 0, u1, v0, bottomColor, bottomLight);
        vertex(pose, vertices, 1, INSET, 1, u1, v1, bottomColor, bottomLight);

        for (Direction wall : WALLS) {
            BlockPos neighbor = pos.relative(wall);
            renderWall(pose, vertices, sprite, wall, faceColor(level, neighbor, block, color), light(level, neighbor));
        }
    }

    private static void renderWall(PoseStack.Pose pose, VertexConsumer vertices, TextureAtlasSprite sprite,
                                   Direction wall, int color, int light) {
        int alongX = -wall.getStepZ();
        int alongZ = wall.getStepX();
        float x1 = 0.5F + wall.getStepX() * (0.5F - INSET) - alongX * 0.5F;
        float z1 = 0.5F + wall.getStepZ() * (0.5F - INSET) - alongZ * 0.5F;
        float x2 = x1 + alongX;
        float z2 = z1 + alongZ;
        float top = 1.0F - INSET;
        float u0 = sprite.getU0();
        float u8 = sprite.getU(8.0D);
        float upper = sprite.getV(INSET * 8.0D);
        float lower = sprite.getV(8.0D);
        vertex(pose, vertices, x1, top, z1, u0, upper, color, light);
        vertex(pose, vertices, x2, top, z2, u8, upper, color, light);
        vertex(pose, vertices, x2, 0, z2, u8, lower, color, light);
        vertex(pose, vertices, x1, 0, z1, u0, lower, color, light);
        vertex(pose, vertices, x1, 0, z1, u0, lower, color, light);
        vertex(pose, vertices, x2, 0, z2, u8, lower, color, light);
        vertex(pose, vertices, x2, top, z2, u8, upper, color, light);
        vertex(pose, vertices, x1, top, z1, u0, upper, color, light);
    }

    private static int faceColor(BlockAndTintGetter level, BlockPos neighbor, BrewDispersalBlock block, int color) {
        var state = level.getBlockState(neighbor);
        int alpha = state.is(block) || state.isSolidRender(level, neighbor) ? INNER_ALPHA : OUTER_ALPHA;
        return color & 0xFFFFFF | alpha << 24;
    }

    private static int light(BlockAndTintGetter level, BlockPos pos) {
        int packed = LevelRenderer.getLightColor(level, pos);
        return packed == 0 && level.getBlockState(pos).getBlock() instanceof SlabBlock
                ? LevelRenderer.getLightColor(level, pos.below()) : packed;
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, float x, float y, float z,
                               float u, float v, int color, int light) {
        vertices.vertex(pose.pose(), x, y, z).color(color >> 16 & 255, color >> 8 & 255, color & 255, color >>> 24)
                .uv(u, v).uv2(light).normal(pose.normal(), 0, 1, 0).endVertex();
    }

    private static final class GasRenderState extends RenderStateShard {
        private GasRenderState() {
            super("witchery_brew_gas", () -> {}, () -> {});
        }

        private static RenderType create() {
            // Fabulous clears the terrain target after block entities have submitted their buffers.
            // The block shader applies the original packed light and fog without adding entity shading.
            return RenderType.create("witchery_brew_gas", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS,
                    262144, false, true, RenderType.CompositeState.builder()
                            .setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
                            .setTextureState(BLOCK_SHEET_MIPPED)
                            .setLightmapState(LIGHTMAP)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setOutputState(ITEM_ENTITY_TARGET)
                            .createCompositeState(false));
        }
    }
}
