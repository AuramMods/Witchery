package art.arcane.witchery.data;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class WitcheryBlockTagsProvider extends BlockTagsProvider {
    public static final TagKey<Block> LEGACY_BLOCKS =
            TagKey.create(Registries.BLOCK, new ResourceLocation(Witchery.MODID, "legacy_blocks"));

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
    }
}
