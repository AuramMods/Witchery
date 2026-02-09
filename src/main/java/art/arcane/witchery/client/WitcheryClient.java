package art.arcane.witchery.client;

import art.arcane.witchery.client.screen.LegacyPlaceholderScreen;
import art.arcane.witchery.menu.LegacyPlaceholderMenu;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WitcheryClient {
    private WitcheryClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (RegistryObject<MenuType<LegacyPlaceholderMenu>> menuType : WitcheryMenus.all().values()) {
                MenuScreens.register(menuType.get(), LegacyPlaceholderScreen::new);
            }

            for (String name : new String[]{
                    "belladonna", "mandrake", "artichoke", "snowbell", "wormwood", "mindrake", "wolfsbane", "garlicplant",
                    "bloodrose", "glintweed", "embermoss", "crittersnare", "plantmine", "bramble", "somniancotton",
                    "spanishmoss", "vine", "web", "witchsapling", "lilypad", "leapinglily",
                    "rowanwooddoor", "alderwooddoor", "cwoodendoor", "icedoor",
                    "beartrap", "wolftrap", "demonheart", "wolfhead", "alluringskull", "chalice", "candelabra",
                    "brazier", "crystalball", "decurseteleport", "decursedirected", "scarecrow", "trent", "witchsladder",
                    "placeditem", "glowglobe", "infinityegg", "garlicgarland", "poppetshelf", "dreamcatcher",
                    "daylightcollector", "bloodcrucible", "silvervat", "cauldron", "cactus",
                    "circle", "circleglyphritual", "circleglyphotherwhere", "circleglyphinfernal",
                    "altar", "kettle", "spinningwheel", "distilleryidle", "distilleryburning",
                    "witchesovenidle", "witchesovenburning", "fumefunnel", "filteredfumefunnel",
                    "coffinblock", "statueofworship", "refillingchest", "leechchest", "statuegoddess", "wolfaltar",
                    "stockade", "icestockade", "voidbramble"
            }) {
                setRenderType(name, RenderType.cutout());
            }

            setRenderType("witchleaves", RenderType.cutoutMipped());

            for (String name : new String[]{
                    "shadedglass", "shadedglass_active", "perpetualice",
                    "spiritportal", "tormentportal", "brew", "brewliquid", "disease",
                    "spiritflowing", "hollowtears", "brewgas", "force", "barrier", "slurp",
                    "mirrorwall", "mirrorblock", "mirrorblock2"
            }) {
                setRenderType(name, RenderType.translucent());
            }
        });
    }

    private static void setRenderType(String legacyName, RenderType renderType) {
        Block block = WitcheryBlocks.getRequired(legacyName).get();
        ItemBlockRenderTypes.setRenderLayer(block, renderType);
    }
}
