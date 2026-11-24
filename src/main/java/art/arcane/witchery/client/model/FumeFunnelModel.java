package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.FumeFunnelBlock;
import art.arcane.witchery.block.WitchesOvenBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class FumeFunnelModel extends BakedModelWrapper<BakedModel> {
    public static final IGeometryLoader<Geometry> LOADER = (json, context) ->
            new Geometry(ResourceLocation.parse(GsonHelper.getAsString(json, "body")));

    private static final ResourceLocation CHIMNEY = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "block/fume_funnel_chimney");
    private static final ResourceLocation CLOCKWISE_PIPE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "block/fume_funnel_clockwise_pipe");
    private static final ResourceLocation COUNTERCLOCKWISE_PIPE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "block/fume_funnel_counterclockwise_pipe");
    private static final ModelProperty<Connections> CONNECTIONS = new ModelProperty<>();

    private final BakedModel chimney;
    private final BakedModel clockwisePipe;
    private final BakedModel counterclockwisePipe;

    private FumeFunnelModel(BakedModel body, BakedModel chimney, BakedModel clockwisePipe, BakedModel counterclockwisePipe) {
        super(body);
        this.chimney = chimney;
        this.clockwisePipe = clockwisePipe;
        this.counterclockwisePipe = counterclockwisePipe;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        Direction facing = state.getValue(FumeFunnelBlock.FACING);
        Connections connections = new Connections(
                level.getBlockState(pos.below()).getBlock() instanceof WitchesOvenBlock,
                level.getBlockState(pos.relative(facing.getClockWise())).getBlock() instanceof WitchesOvenBlock,
                level.getBlockState(pos.relative(facing.getCounterClockWise())).getBlock() instanceof WitchesOvenBlock);
        return modelData.derive().with(CONNECTIONS, connections).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
                                    ModelData data, @Nullable RenderType renderType) {
        Connections connections = data.get(CONNECTIONS);
        if (connections == null) {
            return super.getQuads(state, side, random, data, renderType);
        }

        BakedModel center = connections.below() ? chimney : originalModel;
        List<BakedQuad> centerQuads = center.getQuads(state, side, random, data, renderType);
        if (side != null || (!connections.clockwise() && !connections.counterclockwise())) {
            return centerQuads;
        }

        List<BakedQuad> quads = new ArrayList<>(centerQuads);
        if (connections.clockwise()) {
            quads.addAll(clockwisePipe.getQuads(state, side, random, data, renderType));
        }
        if (connections.counterclockwise()) {
            quads.addAll(counterclockwisePipe.getQuads(state, side, random, data, renderType));
        }
        return quads;
    }

    private record Connections(boolean below, boolean clockwise, boolean counterclockwise) {
    }

    public record Geometry(ResourceLocation body) implements IUnbakedGeometry<Geometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver, IGeometryBakingContext context) {
            for (ResourceLocation part : List.of(body, CHIMNEY, CLOCKWISE_PIPE, COUNTERCLOCKWISE_PIPE)) {
                resolver.apply(part).resolveParents(resolver);
            }
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                               Function<Material, TextureAtlasSprite> sprites, ModelState state,
                               ItemOverrides overrides, ResourceLocation location) {
            return new FumeFunnelModel(baker.bake(body, state, sprites), baker.bake(CHIMNEY, state, sprites),
                    baker.bake(CLOCKWISE_PIPE, state, sprites), baker.bake(COUNTERCLOCKWISE_PIPE, state, sprites));
        }
    }
}
