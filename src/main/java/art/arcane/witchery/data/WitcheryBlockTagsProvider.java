package art.arcane.witchery.data;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class WitcheryBlockTagsProvider extends BlockTagsProvider {
    public static final TagKey<Block> LEGACY_BLOCKS =
            TagKey.create(Registries.BLOCK, ResourceLocation.parse(Witchery.MODID + ":legacy_blocks"));

    public WitcheryBlockTagsProvider(PackOutput output,
                                     CompletableFuture<HolderLookup.Provider> lookupProvider,
                                     @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Witchery.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (RegistryObject<Block> block : WitcheryBlocks.LEGACY_BLOCKS.values()) {
            tag(LEGACY_BLOCKS).add(block.get());
        }
        Block log = WitcheryBlocks.getRequired("witchlog").get();
        Block planks = WitcheryBlocks.getRequired("witchwood").get();
        tag(BlockTags.LOGS).add(log);
        tag(BlockTags.PLANKS).add(planks);
        tag(BlockTags.LEAVES).add(WitcheryBlocks.getRequired("witchleaves").get());
        tag(BlockTags.SAPLINGS).add(WitcheryBlocks.getRequired("witchsapling").get());
        Block[] witchWoodStairs = {WitcheryBlocks.getRequired("stairswoodrowan").get(),
                WitcheryBlocks.getRequired("stairswoodalder").get(), WitcheryBlocks.getRequired("stairswoodhawthorn").get()};
        tag(BlockTags.WOODEN_STAIRS).add(witchWoodStairs);
        tag(BlockTags.MINEABLE_WITH_AXE).add(witchWoodStairs).add(WitcheryBlocks.APOTHECARY_SHELF.get());
        var slab = WitcheryBlocks.getRequired("witchwoodslab").get();
        var doubleSlab = WitcheryBlocks.getRequired("witchwooddoubleslab").get();
        tag(BlockTags.WOODEN_SLABS).add(slab, doubleSlab);
        tag(BlockTags.MINEABLE_WITH_AXE).add(slab, doubleSlab);
        for (WitchCrop crop : WitchCrop.values()) {
            tag(BlockTags.CROPS).add(crop.block());
        }
        addBrewSourceReplacements();
        addBrewLiquidDisplacements();
        Block coffin = WitcheryBlocks.getRequired("coffinblock").get();
        tag(BlockTags.BEDS).add(coffin);
        tag(BlockTags.MINEABLE_WITH_AXE).add(log, planks, coffin,
                WitcheryBlocks.getRequired("scarecrow").get(), WitcheryBlocks.getRequired("trent").get(),
                WitcheryBlocks.getRequired("witchsladder").get(), WitcheryBlocks.getRequired("rowanwooddoor").get(),
                WitcheryBlocks.getRequired("alderwooddoor").get(), WitcheryBlocks.getRequired("stockade").get(),
                WitcheryBlocks.getRequired("bramble").get(), WitcheryBlocks.getRequired("circle").get(),
                WitcheryBlocks.getRequired("circleglyphritual").get(), WitcheryBlocks.getRequired("circleglyphotherwhere").get(),
                WitcheryBlocks.getRequired("circleglyphinfernal").get(),
                WitcheryBlocks.getRequired("cwoodendoor").get(), WitcheryBlocks.getRequired("cwoodpressureplate").get(),
                WitcheryBlocks.getRequired("dreamcatcher").get(), WitcheryBlocks.getRequired("icefencegate").get(),
                WitcheryBlocks.getRequired("plantmine").get(), WitcheryBlocks.getRequired("spanishmoss").get(),
                WitcheryBlocks.getRequired("spinningwheel").get(), WitcheryBlocks.getRequired("wickerbundle").get());
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(
                WitcheryBlocks.getRequired("snowstairs").get(), WitcheryBlocks.getRequired("snowslab").get(),
                WitcheryBlocks.getRequired("snowdoubleslab").get(), WitcheryBlocks.getRequired("snowpressureplate").get(),
                WitcheryBlocks.getRequired("csnowpressureplate").get());
        Block silverVat = WitcheryBlocks.getRequired("silvervat").get();
        tag(BlockTags.NEEDS_STONE_TOOL).add(silverVat);
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                silverVat,
                WitcheryBlocks.getRequired("altar").get(),
                WitcheryBlocks.getRequired("statuegoddess").get(),
                WitcheryBlocks.getRequired("decursedirected").get(),
                WitcheryBlocks.getRequired("decurseteleport").get(),
                WitcheryBlocks.getRequired("statueofworship").get(),
                WitcheryBlocks.getRequired("wolfaltar").get(),
                WitcheryBlocks.getRequired("distilleryidle").get(),
                WitcheryBlocks.getRequired("distilleryburning").get(),
                WitcheryBlocks.getRequired("leechchest").get(),
                WitcheryBlocks.getRequired("witchesovenidle").get(),
                WitcheryBlocks.getRequired("witchesovenburning").get(),
                WitcheryBlocks.getRequired("cauldron").get(),
                WitcheryBlocks.getRequired("beartrap").get(), WitcheryBlocks.getRequired("wolftrap").get(),
                WitcheryBlocks.getRequired("bloodcrucible").get(), WitcheryBlocks.getRequired("poppetshelf").get(),
                WitcheryBlocks.getRequired("brazier").get(), WitcheryBlocks.getRequired("kettle").get(),
                WitcheryBlocks.getRequired("fumefunnel").get(), WitcheryBlocks.getRequired("filteredfumefunnel").get(),
                WitcheryBlocks.getRequired("candelabra").get(), WitcheryBlocks.getRequired("chalice").get(),
                WitcheryBlocks.getRequired("crystalball").get(), WitcheryBlocks.getRequired("daylightcollector").get(),
                WitcheryBlocks.getRequired("cstonepressureplate").get(), WitcheryBlocks.getRequired("shadedglass_active").get());
    }

    private void addBrewSourceReplacements() {
        var replaceable = tag(BrewDispersalBlock.SOURCE_REPLACEABLE);
        replaceable.add(Blocks.WATER, Blocks.LAVA, Blocks.GRASS, Blocks.FERN, Blocks.DEAD_BUSH,
                Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.SNOW, Blocks.VINE);
        for (String id : new String[]{"spanishmoss", "snowpressureplate", "snowslab", "snowdoubleslab",
                "dreamcatcher", "light", "circle", "circleglyphritual", "circleglyphotherwhere", "circleglyphinfernal",
                "spiritflowing", "hollowtears", "disease", "brew", "brewliquid", "csnowpressureplate"}) {
            replaceable.add(WitcheryBlocks.getRequired(id).get());
        }
    }

    private void addBrewLiquidDisplacements() {
        var displaceable = tag(BrewDispersalBlock.LIQUID_DISPLACEABLE);
        displaceable.addTags(BlockTags.SAPLINGS, BlockTags.SMALL_FLOWERS, BlockTags.TALL_FLOWERS, BlockTags.CROPS, BlockTags.RAILS,
                BlockTags.BUTTONS, BlockTags.WOOL_CARPETS, BlockTags.FLOWER_POTS);
        displaceable.add(Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.COBWEB, Blocks.TORCH, Blocks.WALL_TORCH,
                Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH, Blocks.REDSTONE_WIRE, Blocks.REPEATER, Blocks.COMPARATOR,
                Blocks.LEVER, Blocks.LADDER, Blocks.LILY_PAD, Blocks.NETHER_WART, Blocks.COCOA, Blocks.ATTACHED_MELON_STEM,
                Blocks.ATTACHED_PUMPKIN_STEM, Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK, Blocks.SKELETON_SKULL,
                Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL,
                Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD,
                Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD);
        for (String id : new String[]{"alluringskull", "bloodrose", "bramble", "brewgas", "cbuttonstone",
                "cbuttonwood", "circle", "circleglyphinfernal", "circleglyphotherwhere", "circleglyphritual", "clever",
                "crittersnare", "csnowpressureplate", "dreamcatcher", "embermoss", "garlicgarland", "glintweed",
                "grassper", "leapinglily", "light", "lilypad", "plantmine", "snowdoubleslab", "snowpressureplate",
                "snowslab", "somniancotton", "spanishmoss", "voidbramble", "wolfhead"}) {
            displaceable.add(WitcheryBlocks.getRequired(id).get());
        }
    }
}
