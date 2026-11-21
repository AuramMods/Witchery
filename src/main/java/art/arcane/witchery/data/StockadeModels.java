package art.arcane.witchery.data;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.StockadeBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

final class StockadeModels extends BlockStateProvider {
    private enum Shape { POST, ROW, CROSS }

    private record Models(Map<Shape, BlockModelBuilder> pointed, Map<Shape, BlockModelBuilder> covered) {
        BlockModelBuilder get(Shape shape, boolean hasTop) {
            return (hasTop ? covered : pointed).get(shape);
        }
    }

    StockadeModels(PackOutput output, ExistingFileHelper existing) {
        super(output, Witchery.MODID, existing);
    }

    @Override
    protected void registerStatesAndModels() {
        Models templates = new Models(new EnumMap<>(Shape.class), new EnumMap<>(Shape.class));
        for (Shape shape : Shape.values()) {
            templates.pointed.put(shape, template(shape, false));
            templates.covered.put(shape, template(shape, true));
        }

        Block stockade = WitcheryBlocks.getRequired("stockade").get();
        var woodenModels = new EnumMap<StockadeBlock.Wood, Models>(StockadeBlock.Wood.class);
        for (var wood : StockadeBlock.Wood.values()) {
            String name = wood.getSerializedName();
            boolean vanilla = wood.witchWood() == null;
            String texture = vanilla ? "block/" + name + "_log" : "block/log_" + name;
            ResourceLocation bark = vanilla ? mcLoc(texture) : modLoc(texture);
            ResourceLocation end = vanilla ? mcLoc(texture + "_top") : modLoc(texture + "_top");
            woodenModels.put(wood, material(templates, "stockade/" + name, bark, end, false));
        }
        getVariantBuilder(stockade).forAllStatesExcept(
                state -> configure(woodenModels.get(state.getValue(StockadeBlock.WOOD)), state), StockadeBlock.WATERLOGGED);
        var item = itemModels().getBuilder("stockade")
                .parent(woodenModels.get(StockadeBlock.Wood.OAK).get(Shape.POST, false));
        for (var wood : StockadeBlock.Wood.values()) {
            if (wood == StockadeBlock.Wood.OAK) continue;
            item.override().predicate(modLoc("wood_type"), wood.ordinal())
                    .model(woodenModels.get(wood).get(Shape.POST, false)).end();
        }

        Block frozen = WitcheryBlocks.getRequired("icestockade").get();
        ResourceLocation ice = mcLoc("block/ice");
        Models iceModels = material(templates, "icestockade", ice, ice, true);
        getVariantBuilder(frozen).forAllStatesExcept(state -> configure(iceModels, state), StockadeBlock.WATERLOGGED);
        simpleBlockItem(frozen, iceModels.get(Shape.POST, false));
    }

    private Models material(Models templates, String path, ResourceLocation bark, ResourceLocation end, boolean translucent) {
        Models forms = new Models(new EnumMap<>(Shape.class), new EnumMap<>(Shape.class));
        for (Shape shape : Shape.values()) {
            forms.pointed.put(shape, textured(path + suffix(shape, false), templates.get(shape, false), bark, end, translucent));
            forms.covered.put(shape, textured(path + suffix(shape, true), templates.get(shape, true), bark, end, translucent));
        }
        return forms;
    }

    private BlockModelBuilder textured(String id, ModelFile parent, ResourceLocation bark, ResourceLocation end, boolean ice) {
        var model = models().getBuilder(id).parent(parent).texture("bark", bark).texture("end", end).texture("particle", bark);
        return ice ? model.renderType("translucent") : model;
    }

    private static ConfiguredModel[] configure(Models forms, BlockState state) {
        boolean alongZ = StockadeBlock.alongNorthSouth(state);
        boolean alongX = StockadeBlock.alongEastWest(state);
        Shape shape = alongZ && alongX ? Shape.CROSS : alongZ || alongX ? Shape.ROW : Shape.POST;
        ModelFile model = forms.get(shape, state.getValue(StockadeBlock.COVERED));
        return new ConfiguredModel[]{new ConfiguredModel(model, 0, alongZ && !alongX ? 90 : 0, false)};
    }

    private BlockModelBuilder template(Shape shape, boolean covered) {
        var model = models().withExistingParent("stockade/template" + suffix(shape, covered), mcLoc("block/block"));
        if (shape == Shape.POST) {
            post(model, 4.8F, 4.8F, covered, false);
        } else {
            post(model, 0.8F, 4.8F, covered, shape == Shape.CROSS);
            post(model, 8.8F, 4.8F, covered, false);
            if (shape == Shape.CROSS) {
                post(model, 4.8F, 0.8F, covered, true);
                post(model, 4.8F, 8.8F, covered, false);
            }
        }
        return model;
    }

    private static String suffix(Shape shape, boolean covered) {
        return "_" + shape.name().toLowerCase(Locale.ROOT) + (covered ? "_covered" : "");
    }

    private static void post(BlockModelBuilder model, float x, float z, boolean covered, boolean tall) {
        float shoulder = covered ? 16 : tall ? 9.6F : 8;
        model.element().from(x, 0, z).to(x + 6.4F, shoulder, z + 6.4F)
                .allFaces((face, builder) -> builder.texture(face.getAxis() == Direction.Axis.Y ? "#end" : "#bark")).end();
        if (covered) return;
        for (int step = 0; step < 4; step++) {
            float inset = (step + 1) * 0.64F;
            model.element()
                    .from(x + inset, shoulder + step * 1.344F, z + inset)
                    .to(x + 6.4F - inset, shoulder + (step + 1) * 1.344F, z + 6.4F - inset)
                    .textureAll("#end").end();
        }
    }
}
