package art.arcane.witchery;

import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Witchery.MODID)
public class Witchery {
    public static final String MODID = "witchery";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Witchery(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        WitcheryRegistries.register(modEventBus);
        WitcheryNetwork.register();
    }
}
