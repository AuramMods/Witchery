package art.arcane.witchery.data;

import art.arcane.witchery.Witchery;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
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
        if (!event.includeServer()) {
            return;
        }

        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new WitcheryBlockTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(true, new WitcheryRecipeProvider(output));
        generator.addProvider(true, new WitcheryLootTableProvider(output));
    }
}
