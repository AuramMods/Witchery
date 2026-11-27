package art.arcane.witchery.client.renderer;

import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.ForgeHooksClient;

public final class BrewLiquidRenderer {
    private static final int QUANTA = 6;
    // The original renderer divides the six quanta by eight when sampling flowing cells.
    private static final float HEIGHT_DIVISOR = 8.0F;
    private static final float SOURCE_HEIGHT = 0.875F;
    private static final float INSET = 0.001F;
    private static final Direction[] SIDES = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    // Corners are ordered northwest, southwest, southeast, northeast, viewed from above.
    private static final int[] CORNER_X = {0, 0, 1, 1};
    private static final int[] CORNER_Z = {0, 1, 1, 0};
    private static final int[] SIDE_START = {0, 2, 1, 3};
    private static final int[] SIDE_END = {3, 1, 0, 2};

    private BrewLiquidRenderer() {}

    public static void render(BlockAndTintGetter level, BlockPos pos, VertexConsumer vertices,
                              BlockState state, FluidState fluid, BrewDispersalBlock block) {
        boolean top = !level.getBlockState(pos.above()).is(block);
        boolean bottom = visibleFrom(level, pos.below(), block);
        int visibleSides = 0;
        for (int side = 0; side < SIDES.length; side++) {
            if (visibleFrom(level, pos.relative(SIDES[side]), block)) visibleSides |= 1 << side;
        }
        if (!top && !bottom && visibleSides == 0) return;

        int color = level.getBlockEntity(pos) instanceof BrewFluidBlockEntity brew ? brew.color() : 68;
        TextureAtlasSprite[] sprites = ForgeHooksClient.getFluidSprites(level, pos, fluid);
        float[] heights = cornerHeights(level, pos, state, block);
        int x = pos.getX() & 15;
        int y = pos.getY() & 15;
        int z = pos.getZ() & 15;
        if (top) {
            for (int i = 0; i < heights.length; i++) heights[i] -= INSET;
            renderTop(vertices, sprites, x, y, z, heights, flowAngle(level, pos, state, block), shade(color, 1.0F), light(level, pos));
        }
        if (bottom) {
            int shaded = shade(color, 0.5F);
            var still = sprites[0];
            int light = light(level, pos.below());
            for (int i = 0; i < heights.length; i++) {
                int corner = (1 - i) & 3;
                vertex(vertices, x + CORNER_X[corner], y + (double) INSET, z + CORNER_Z[corner], shaded,
                        still.getU(CORNER_X[corner] * 16), still.getV(CORNER_Z[corner] * 16), light);
            }
        }
        for (int side = 0; side < SIDES.length; side++) {
            if ((visibleSides & 1 << side) != 0) {
                Direction direction = SIDES[side];
                renderSide(vertices, sprites[1], x, y, z, heights, side, direction,
                        shade(color, side < 2 ? 0.8F : 0.6F), light(level, pos.relative(direction)));
            }
        }
    }

    private static boolean visibleFrom(BlockAndTintGetter level, BlockPos neighbor, BrewDispersalBlock block) {
        BlockState state = level.getBlockState(neighbor);
        return !state.is(block) && !state.isSolidRender(level, neighbor);
    }

    private static float[] cornerHeights(BlockAndTintGetter level, BlockPos pos, BlockState state, BrewDispersalBlock block) {
        float center = height(level, pos, state, block);
        if (center == 1.0F) return new float[]{center, center, center, center};
        float north = height(level, pos.north(), block);
        float south = height(level, pos.south(), block);
        float west = height(level, pos.west(), block);
        float east = height(level, pos.east(), block);
        return new float[]{
                average(height(level, pos.offset(-1, 0, -1), block), west, north, center),
                average(west, height(level, pos.offset(-1, 0, 1), block), south, center),
                average(south, east, height(level, pos.offset(1, 0, 1), block), center),
                average(north, height(level, pos.offset(1, 0, -1), block), east, center)
        };
    }

    private static float height(BlockAndTintGetter level, BlockPos pos, BrewDispersalBlock block) {
        return height(level, pos, level.getBlockState(pos), block);
    }

    private static float height(BlockAndTintGetter level, BlockPos pos, BlockState state, BrewDispersalBlock block) {
        BlockState above = level.getBlockState(pos.above());
        if (state.is(block)) {
            if (above.liquid()) return 1.0F;
            if (state.getValue(BrewDispersalBlock.LEVEL) == 0) return SOURCE_HEIGHT;
        }
        if (!state.blocksMotion() && above.is(block)) return 1.0F;
        return quanta(state, block) / HEIGHT_DIVISOR * SOURCE_HEIGHT;
    }

    private static float average(float one, float two, float three, float four) {
        float total = 0.0F;
        int count = 0;
        float source = 0.0F;
        for (float height : new float[]{one, two, three, four}) {
            if (height >= SOURCE_HEIGHT && source != 1.0F) source = height;
            if (height >= 0.0F) {
                total += height;
                count++;
            }
        }
        return source != 0.0F ? source : total / count;
    }

    private static int quanta(BlockState state, BrewDispersalBlock block) {
        if (state.is(block)) return QUANTA - state.getValue(BrewDispersalBlock.LEVEL);
        return state.isAir() ? 0 : -1;
    }

    private static float flowAngle(BlockAndTintGetter level, BlockPos pos, BlockState state, BrewDispersalBlock block) {
        int depth = state.getValue(BrewDispersalBlock.LEVEL);
        int x = 0;
        int z = 0;
        for (Direction direction : SIDES) {
            BlockPos neighbor = pos.relative(direction);
            BlockState side = level.getBlockState(neighbor);
            int sideDepth = QUANTA - quanta(side, block);
            int weight;
            if (sideDepth >= QUANTA && !side.blocksMotion()) {
                int belowDepth = QUANTA - quanta(level.getBlockState(neighbor.below()), block);
                weight = belowDepth >= 0 ? belowDepth - depth + QUANTA : 0;
            } else {
                weight = sideDepth >= 0 && sideDepth < QUANTA ? sideDepth - depth : 0;
            }
            x += direction.getStepX() * weight;
            z += direction.getStepZ() * weight;
        }
        // The downward component of a falling cell does not affect the UV angle.
        return x == 0 && z == 0 ? Float.NaN : (float) (Math.atan2(z, x) - Math.PI / 2.0D);
    }

    private static void renderTop(VertexConsumer vertices, TextureAtlasSprite[] sprites, int x, int y, int z,
                                  float[] heights, float angle, int color, int light) {
        boolean still = Float.isNaN(angle);
        TextureAtlasSprite sprite = sprites[still ? 0 : 1];
        float sin = still ? 0.0F : Mth.sin(angle) * 0.25F;
        float cos = still ? 0.0F : Mth.cos(angle) * 0.25F;
        for (int corner = 0; corner < heights.length; corner++) {
            int dx = CORNER_X[corner];
            int dz = CORNER_Z[corner];
            int uSign = 2 * dx - 1;
            int vSign = 2 * dz - 1;
            float u = still ? 16 * dx : 8.0F + (uSign * cos + vSign * sin) * 16.0F;
            float v = still ? 16 * dz : 8.0F + (vSign * cos - uSign * sin) * 16.0F;
            vertex(vertices, x + dx, y + (double) heights[corner], z + dz, color, sprite.getU(u), sprite.getV(v), light);
        }
    }

    private static void renderSide(VertexConsumer vertices, TextureAtlasSprite sprite, int x, int y, int z,
                                   float[] heights, int side, Direction direction, int color, int light) {
        int start = SIDE_START[side];
        int end = SIDE_END[side];
        double x1 = x + CORNER_X[start] - (double) INSET * direction.getStepX();
        double z1 = z + CORNER_Z[start] - (double) INSET * direction.getStepZ();
        double x2 = x + CORNER_X[end] - (double) INSET * direction.getStepX();
        double z2 = z + CORNER_Z[end] - (double) INSET * direction.getStepZ();
        float u1 = sprite.getU0();
        float u2 = sprite.getU(8.0D);
        float bottom = sprite.getV(8.0D);
        vertex(vertices, x1, y + (double) heights[start], z1, color, u1, sprite.getV((1.0D - heights[start]) * 8), light);
        vertex(vertices, x2, y + (double) heights[end], z2, color, u2, sprite.getV((1.0D - heights[end]) * 8), light);
        vertex(vertices, x2, y, z2, color, u2, bottom, light);
        vertex(vertices, x1, y, z1, color, u1, bottom, light);
    }

    private static int shade(int color, float shade) {
        int red = shadeChannel(color >> 16 & 255, shade);
        int green = shadeChannel(color >> 8 & 255, shade);
        int blue = shadeChannel(color & 255, shade);
        return red << 16 | green << 8 | blue;
    }

    private static int shadeChannel(int color, float shade) {
        return (int) (shade * (color / 255.0F) * 255.0F);
    }

    private static int light(BlockAndTintGetter level, BlockPos pos) {
        int here = LevelRenderer.getLightColor(level, pos);
        int above = LevelRenderer.getLightColor(level, pos.above());
        return Math.max(here & 255, above & 255) | Math.max(here >> 16 & 255, above >> 16 & 255) << 16;
    }

    private static void vertex(VertexConsumer vertices, double x, double y, double z, int color, float u, float v, int light) {
        vertices.vertex(x, y, z).color(color >> 16 & 255, color >> 8 & 255, color & 255, 255)
                .uv(u, v).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
    }
}
