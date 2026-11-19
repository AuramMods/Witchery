package art.arcane.witchery.client;

import art.arcane.witchery.client.screen.AltarScreen;
import art.arcane.witchery.client.screen.DistilleryScreen;
import art.arcane.witchery.client.screen.LegacyPlaceholderScreen;
import art.arcane.witchery.client.screen.WitchesOvenScreen;
import art.arcane.witchery.menu.LegacyPlaceholderMenu;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.client.model.MandrakeModel;
import art.arcane.witchery.client.renderer.KettleRenderer;
import art.arcane.witchery.client.renderer.MandrakeRenderer;
import art.arcane.witchery.client.renderer.PlacedItemRenderer;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WitcheryClient {
    private static final ResourceLocation WOOD_TYPE_MODEL =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "wood_type");

    private WitcheryClient() {
    }

    @SubscribeEvent
    public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MandrakeModel.LAYER, MandrakeModel::layer);
        event.registerLayerDefinition(KettleRenderer.LAYER, KettleRenderer::layer);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WitcheryEntities.MANDRAKE.get(), MandrakeRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.THROWN_BREW.get(), ThrownItemRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.KETTLE.get(), KettleRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.PLACED_ITEM.get(), PlacedItemRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(WitcheryMenus.WITCHES_OVEN.get(), WitchesOvenScreen::new);
            MenuScreens.register(WitcheryMenus.ALTAR.get(), AltarScreen::new);
            MenuScreens.register(WitcheryMenus.DISTILLERY.get(), DistilleryScreen::new);
            for (RegistryObject<MenuType<LegacyPlaceholderMenu>> menuType : WitcheryMenus.placeholders().values()) {
                MenuScreens.register(menuType.get(), LegacyPlaceholderScreen::new);
            }
            for (String name : new String[]{"witchsapling", "witchleaves", "witchlog", "witchwood"}) {
                ItemProperties.register(WitcheryBlocks.getRequired(name).get().asItem(), WOOD_TYPE_MODEL,
                        (stack, level, entity, seed) -> WitchWoodType.fromStack(stack).ordinal());
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
                    "kettle", "spinningwheel", "distilleryidle", "distilleryburning",
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

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> level != null && pos != null
                ? BiomeColors.getAverageWaterColor(level, pos) : 0x3F76E4, WitcheryBlocks.getRequired("cauldron").get());
        event.register((state, level, pos, tint) -> {
            if (tint != 0) {
                return -1;
            }
            WitchWoodType wood = state.getValue(WitchWoodType.PROPERTY);
            return wood == WitchWoodType.ROWAN && level != null && pos != null
                    ? BiomeColors.getAverageFoliageColor(level, pos)
                    : foliageColor(wood);
        }, WitcheryBlocks.getRequired("witchleaves").get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> tint == 0 ? foliageColor(WitchWoodType.fromStack(stack)) : -1,
                WitcheryBlocks.getRequired("witchleaves").get());
    }

    private static int foliageColor(WitchWoodType wood) {
        return switch (wood) {
            case ROWAN -> 4764952;
            case ALDER -> 3774771;
            case HAWTHORN -> 6728294;
        };
    }

    private static void setRenderType(String legacyName, RenderType renderType) {
        Block block = WitcheryBlocks.getRequired(legacyName).get();
        ItemBlockRenderTypes.setRenderLayer(block, renderType);
    }
}
