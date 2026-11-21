package art.arcane.witchery.data;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
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
        for (WitchCrop crop : WitchCrop.values()) {
            tag(BlockTags.CROPS).add(crop.block());
        }
        Block coffin = WitcheryBlocks.getRequired("coffinblock").get();
        tag(BlockTags.BEDS).add(coffin);
        tag(BlockTags.MINEABLE_WITH_AXE).add(log, planks, coffin,
                WitcheryBlocks.getRequired("scarecrow").get(), WitcheryBlocks.getRequired("trent").get(),
                WitcheryBlocks.getRequired("witchsladder").get());
        Block silverVat = WitcheryBlocks.getRequired("silvervat").get();
        tag(BlockTags.NEEDS_STONE_TOOL).add(silverVat);
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                silverVat,
                WitcheryBlocks.getRequired("altar").get(),
                WitcheryBlocks.getRequired("distilleryidle").get(),
                WitcheryBlocks.getRequired("distilleryburning").get(),
                WitcheryBlocks.getRequired("leechchest").get(),
                WitcheryBlocks.getRequired("witchesovenidle").get(),
                WitcheryBlocks.getRequired("witchesovenburning").get(),
                WitcheryBlocks.getRequired("cauldron").get());
    }
}
