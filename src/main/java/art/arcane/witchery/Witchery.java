package art.arcane.witchery;

import art.arcane.witchery.block.entity.PoppetShelfBlockEntity;
import art.arcane.witchery.config.WitcheryClientConfig;
import art.arcane.witchery.config.WitcheryCommonConfig;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.EndlessWaterBrewItem;
import art.arcane.witchery.item.GrenadeItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(Witchery.MODID)
public class Witchery {
    public static final String MODID = "witchery";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Witchery(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, WitcheryCommonConfig.SPEC);
        context.registerConfig(ModConfig.Type.SERVER, WitcheryServerConfig.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, WitcheryClientConfig.SPEC);
        IEventBus modEventBus = context.getModEventBus();
        WitcheryRegistries.register(modEventBus);
        modEventBus.addListener(Witchery::setupCommon);
        WitcheryNetwork.register();
    }

    private static void setupCommon(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ForgeChunkManager.setForcedChunkLoadingCallback(MODID, PoppetShelfBlockEntity::restoreTickets);
            GrenadeItem.registerDispenser();
            EndlessWaterBrewItem.registerDispenser();
        });
    }
}
