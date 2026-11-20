package art.arcane.witchery.client;

import art.arcane.witchery.client.screen.AltarScreen;
import art.arcane.witchery.client.screen.DistilleryScreen;
import art.arcane.witchery.client.screen.LegacyPlaceholderScreen;
import art.arcane.witchery.client.screen.SpinningWheelScreen;
import art.arcane.witchery.client.screen.WitchesOvenScreen;
import art.arcane.witchery.menu.LegacyPlaceholderMenu;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.CritterSnareBlock;
import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.client.model.BabaYagaModel;
import art.arcane.witchery.client.model.MandrakeModel;
import art.arcane.witchery.client.model.DarkMarkModel;
import art.arcane.witchery.client.model.DemonModel;
import art.arcane.witchery.client.model.EntModel;
import art.arcane.witchery.client.model.ImpModel;
import art.arcane.witchery.client.model.LilithModel;
import art.arcane.witchery.client.model.LordOfTormentModel;
import art.arcane.witchery.client.model.NightmareModel;
import art.arcane.witchery.client.model.WitchOwlModel;
import art.arcane.witchery.client.model.WitchToadModel;
import art.arcane.witchery.client.model.WitchClothingModel;
import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.client.renderer.DreamCatcherRenderer;
import art.arcane.witchery.client.renderer.BabaYagaRenderer;
import art.arcane.witchery.client.renderer.BroomRenderer;
import art.arcane.witchery.client.renderer.DarkMarkRenderer;
import art.arcane.witchery.client.renderer.CovenWitchRenderer;
import art.arcane.witchery.client.renderer.CoffinRenderer;
import art.arcane.witchery.client.renderer.CrystalBallRenderer;
import art.arcane.witchery.client.renderer.DemonRenderer;
import art.arcane.witchery.client.renderer.EntRenderer;
import art.arcane.witchery.client.renderer.DemonHeartRenderer;
import art.arcane.witchery.client.renderer.GrotesqueLayer;
import art.arcane.witchery.client.renderer.FollowerRenderer;
import art.arcane.witchery.client.renderer.GrassperRenderer;
import art.arcane.witchery.client.renderer.ImpRenderer;
import art.arcane.witchery.client.renderer.KettleRenderer;
import art.arcane.witchery.client.renderer.LeechChestRenderer;
import art.arcane.witchery.client.renderer.LilithRenderer;
import art.arcane.witchery.client.renderer.LordOfTormentRenderer;
import art.arcane.witchery.client.renderer.MandrakeRenderer;
import art.arcane.witchery.client.renderer.MirrorBlockRenderer;
import art.arcane.witchery.client.renderer.NightmareRenderer;
import art.arcane.witchery.client.renderer.PlacedItemRenderer;
import art.arcane.witchery.client.renderer.PoppetShelfRenderer;
import art.arcane.witchery.client.renderer.ReflectionRenderer;
import art.arcane.witchery.client.renderer.SleepingBodyRenderer;
import art.arcane.witchery.client.renderer.SpinningWheelRenderer;
import art.arcane.witchery.client.renderer.SymbolProjectileRenderer;
import art.arcane.witchery.client.renderer.WitchOwlRenderer;
import art.arcane.witchery.client.renderer.WitchToadRenderer;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.GlassGobletItem;
import art.arcane.witchery.item.BroomItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryMenus;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
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
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("infusion_charge", ClientInfusion::render);
        event.registerAboveAll("vampire_blood", ClientVampirism::render);
    }

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(WitcheryDimensions.DREAM.location(), new DreamSpecialEffects());
        event.register(WitcheryDimensions.MIRROR.location(),
                new DimensionSpecialEffects(Float.NaN, false, DimensionSpecialEffects.SkyType.NONE, false, true) {
                    @Override
                    public Vec3 getBrightnessDependentFogColor(Vec3 base, float brightness) {
                        return new Vec3(0, 0.03, 0.1);
                    }

                    @Override
                    public boolean isFoggyAt(int x, int z) {
                        return true;
                    }
                });
    }

    @SubscribeEvent
    public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (WitchClothingItem.Style style : WitchClothingItem.Style.values()) {
            event.registerLayerDefinition(WitchClothingModel.layerId(style), () -> WitchClothingModel.layer(style));
        }
        event.registerLayerDefinition(BabaYagaModel.LAYER, BabaYagaModel::layer);
        event.registerLayerDefinition(MandrakeModel.LAYER, MandrakeModel::layer);
        event.registerLayerDefinition(NightmareModel.LAYER, NightmareModel::layer);
        event.registerLayerDefinition(DemonModel.LAYER, DemonModel::layer);
        event.registerLayerDefinition(EntModel.LAYER, EntModel::layer);
        event.registerLayerDefinition(WitchOwlModel.LAYER, WitchOwlModel::layer);
        event.registerLayerDefinition(WitchToadModel.LAYER, WitchToadModel::layer);
        event.registerLayerDefinition(BroomRenderer.LAYER, BroomRenderer::layer);
        event.registerLayerDefinition(CrystalBallRenderer.LAYER, CrystalBallRenderer::layer);
        event.registerLayerDefinition(ImpModel.LAYER, ImpModel::layer);
        event.registerLayerDefinition(DarkMarkModel.LAYER, DarkMarkModel::layer);
        event.registerLayerDefinition(LordOfTormentModel.LAYER, LordOfTormentModel::layer);
        event.registerLayerDefinition(GrotesqueLayer.LAYER, GrotesqueLayer::layer);
        event.registerLayerDefinition(GrassperRenderer.LAYER, GrassperRenderer::layer);
        event.registerLayerDefinition(MirrorBlockRenderer.LAYER, MirrorBlockRenderer::layer);
        event.registerLayerDefinition(DemonHeartRenderer.LAYER, DemonHeartRenderer::layer);
        event.registerLayerDefinition(DreamCatcherRenderer.LAYER, DreamCatcherRenderer::layer);
        event.registerLayerDefinition(KettleRenderer.LAYER, KettleRenderer::layer);
        event.registerLayerDefinition(LeechChestRenderer.LAYER, LeechChestRenderer::layer);
        event.registerLayerDefinition(CoffinRenderer.LAYER, CoffinRenderer::layer);
        event.registerLayerDefinition(LilithModel.LAYER, LilithModel::layer);
        event.registerLayerDefinition(FollowerRenderer.LAYER, FollowerRenderer::layer);
        event.registerLayerDefinition(SpinningWheelRenderer.LAYER, SpinningWheelRenderer::layer);
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            if (event.getPlayerSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new GrotesqueLayer(renderer, event.getContext().bakeLayer(GrotesqueLayer.LAYER)));
            }
        }
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WitcheryEntities.BABA_YAGA.get(), BabaYagaRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.LILITH.get(), LilithRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.FOLLOWER.get(), FollowerRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.SCRY_EYE.get(), NoopRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.MANDRAKE.get(), MandrakeRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.NIGHTMARE.get(), NightmareRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.DEMON.get(), DemonRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.ENT.get(), EntRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.IMP.get(), ImpRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.LORD_OF_TORMENT.get(), LordOfTormentRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.SOULFIRE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.SYMBOL_PROJECTILE.get(), SymbolProjectileRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.DARK_MARK.get(), DarkMarkRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.WITCH_CAT.get(), CatRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.OWL.get(), WitchOwlRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.TOAD.get(), WitchToadRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.BROOM.get(), BroomRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.ITEM_WAYSTONE.get(), ItemEntityRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.CRYSTAL_BALL.get(), CrystalBallRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.COVEN_WITCH.get(), CovenWitchRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.REFLECTION.get(), ReflectionRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.SLEEPING_BODY.get(), SleepingBodyRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.THROWN_BREW.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.GRENADE.get(), ThrownItemRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.KETTLE.get(), KettleRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.SPINNING_WHEEL.get(), SpinningWheelRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.PLACED_ITEM.get(), PlacedItemRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.GRASSPER.get(), GrassperRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.POPPET_SHELF.get(), PoppetShelfRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.DREAM_CATCHER.get(), DreamCatcherRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.DEMON_HEART.get(), DemonHeartRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.MIRROR.get(), MirrorBlockRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.ROOM_MIRROR.get(), MirrorBlockRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.REFILLING_CHEST.get(), ChestRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.LEECH_CHEST.get(), LeechChestRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.COFFIN.get(), CoffinRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(WitcheryMenus.WITCHES_OVEN.get(), WitchesOvenScreen::new);
            MenuScreens.register(WitcheryMenus.ALTAR.get(), AltarScreen::new);
            MenuScreens.register(WitcheryMenus.DISTILLERY.get(), DistilleryScreen::new);
            MenuScreens.register(WitcheryMenus.SPINNING_WHEEL.get(), SpinningWheelScreen::new);
            for (RegistryObject<MenuType<LegacyPlaceholderMenu>> menuType : WitcheryMenus.placeholders().values()) {
                MenuScreens.register(menuType.get(), LegacyPlaceholderScreen::new);
            }
            for (String name : new String[]{"witchsapling", "witchleaves", "witchlog", "witchwood"}) {
                ItemProperties.register(WitcheryBlocks.getRequired(name).get().asItem(), WOOD_TYPE_MODEL,
                        (stack, level, entity, seed) -> WitchWoodType.fromStack(stack).ordinal());
            }
            ItemProperties.register(WitcheryItems.getRequired("taglockkit").get(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "bound"),
                    (stack, level, entity, seed) -> TaglockItem.isBound(stack) ? 1 : 0);
            ItemProperties.register(WitcheryItems.getRequired("glassgoblet").get(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "filled"),
                    (stack, level, entity, seed) -> GlassGobletItem.filled(stack) ? 1 : 0);
            ItemProperties.register(WitcheryBlocks.getRequired("crittersnare").get().asItem(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "critter"),
                    (stack, level, entity, seed) -> CritterSnareBlock.critterFromStack(stack).ordinal());
            ItemProperties.register(WitcheryBlocks.getRequired("bramble").get().asItem(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "ender"),
                    (stack, level, entity, seed) -> BrambleBlock.isWild(stack) ? 0 : 1);

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
        event.register((state, level, pos, tint) -> tint == 0 ? 0xCCDDFF : -1,
                WitcheryBlocks.getRequired("mirrorwall").get());
        event.register((state, level, pos, tint) -> tint == 0 ? 0x00FF66 : -1,
                WitcheryBlocks.getRequired("spiritportal").get());
        event.register((state, level, pos, tint) -> tint == 0 ? 0xFF0022 : -1,
                WitcheryBlocks.getRequired("tormentportal").get());
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
        event.register((stack, tint) -> tint == 0 && stack.getItem() instanceof WitchClothingItem.Dyeable clothing
                        ? clothing.getColor(stack) : -1,
                WitcheryItems.getRequired("witchhat").get(), WitcheryItems.getRequired("witchrobe").get(),
                WitcheryItems.getRequired("necromancerrobe").get());
        event.register((stack, tint) -> tint == 0 ? foliageColor(WitchWoodType.fromStack(stack)) : -1,
                WitcheryBlocks.getRequired("witchleaves").get());
        event.register((stack, tint) -> {
            int color = BroomItem.brushColor(stack);
            return tint == 1 ? DyeColor.byId(color < 0 ? 12 : color).getFireworkColor() : -1;
        }, WitcheryIngredient.ENCHANTED_BROOM.item());
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
