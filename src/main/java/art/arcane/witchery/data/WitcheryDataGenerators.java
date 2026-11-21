package art.arcane.witchery.data;

import art.arcane.witchery.Witchery;
import net.minecraft.core.HolderLookup;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WitcheryDataGenerators {
    private WitcheryDataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();
        if (event.includeClient()) generator.addProvider(true, new StockadeModels(output, existingFileHelper));
        if (!event.includeServer()) {
            return;
        }

        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new WitcheryBlockTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(true, new WitcheryRecipeProvider(output));
        generator.addProvider(true, new WitcheryLootTableProvider(output));
    }
}
