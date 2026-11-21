package art.arcane.witchery.client;

import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.block.WickerBundleBlock;
import art.arcane.witchery.entity.BrewDroplet;
import art.arcane.witchery.item.BrewFuelItem;
import art.arcane.witchery.item.SpectralStoneItem;
import art.arcane.witchery.item.PlayerCompassItem;
import art.arcane.witchery.client.screen.AltarScreen;
import art.arcane.witchery.client.screen.DistilleryScreen;
import art.arcane.witchery.client.screen.LegacyPlaceholderScreen;
import art.arcane.witchery.client.screen.SpinningWheelScreen;
import art.arcane.witchery.client.screen.WitchesOvenScreen;
import art.arcane.witchery.client.screen.LeonardsUrnScreen;
import art.arcane.witchery.item.LeonardsUrnItem;
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
import art.arcane.witchery.client.model.HobgoblinModel;
import art.arcane.witchery.client.model.HobgoblinGodModel;
import art.arcane.witchery.client.model.GoblinEquipmentModel;
import art.arcane.witchery.client.model.LilithModel;
import art.arcane.witchery.client.model.LordOfTormentModel;
import art.arcane.witchery.client.model.NightmareModel;
import art.arcane.witchery.client.model.DeathClothingModel;
import art.arcane.witchery.client.model.DeathModel;
import art.arcane.witchery.client.model.LeonardModel;
import art.arcane.witchery.entity.DreamSpirit;
import art.arcane.witchery.entity.LostSoul;
import art.arcane.witchery.client.renderer.LeonardRenderer;
import art.arcane.witchery.client.renderer.IllusionRenderer;
import art.arcane.witchery.client.renderer.BrewProjectileRenderer;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import art.arcane.witchery.client.model.DreamSpiritModel;
import art.arcane.witchery.client.model.SpectralUndeadModel;
import art.arcane.witchery.client.model.WitchOwlModel;
import art.arcane.witchery.client.model.WitchToadModel;
import art.arcane.witchery.client.model.WitchClothingModel;
import art.arcane.witchery.client.model.HunterClothingModel;
import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.client.model.VampireClothingModel;
import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.item.VampireClothingItem;
import art.arcane.witchery.client.renderer.DreamCatcherRenderer;
import art.arcane.witchery.client.renderer.FetishRenderer;
import art.arcane.witchery.client.renderer.BabaYagaRenderer;
import art.arcane.witchery.client.renderer.BroomRenderer;
import art.arcane.witchery.client.renderer.DarkMarkRenderer;
import art.arcane.witchery.client.renderer.CovenWitchRenderer;
import art.arcane.witchery.client.renderer.HobgoblinRenderer;
import art.arcane.witchery.client.renderer.HobgoblinGodRenderer;
import art.arcane.witchery.client.renderer.StatueOfWorshipRenderer;
import art.arcane.witchery.client.renderer.CoffinRenderer;
import art.arcane.witchery.client.renderer.CrystalBallRenderer;
import art.arcane.witchery.client.renderer.DemonRenderer;
import art.arcane.witchery.client.renderer.EntRenderer;
import art.arcane.witchery.client.renderer.DemonHeartRenderer;
import art.arcane.witchery.client.renderer.GrotesqueLayer;
import art.arcane.witchery.client.renderer.FollowerRenderer;
import art.arcane.witchery.client.renderer.BloodCrucibleRenderer;
import art.arcane.witchery.client.renderer.BrewGasRenderer;
import art.arcane.witchery.client.renderer.SilverVatRenderer;
import art.arcane.witchery.client.renderer.VampireRenderer;
import art.arcane.witchery.client.renderer.VillageGuardRenderer;
import art.arcane.witchery.client.renderer.WitcheryBoltRenderer;
import art.arcane.witchery.client.renderer.WitchHunterRenderer;
import art.arcane.witchery.client.renderer.HellhoundRenderer;
import art.arcane.witchery.client.renderer.HornedHuntsmanRenderer;
import art.arcane.witchery.client.renderer.WolfHeadRenderer;
import art.arcane.witchery.client.renderer.WolfmanRenderer;
import art.arcane.witchery.client.renderer.WolfAltarRenderer;
import art.arcane.witchery.client.renderer.HuntingTrapRenderer;
import art.arcane.witchery.client.renderer.GrassperRenderer;
import art.arcane.witchery.client.renderer.ImpRenderer;
import art.arcane.witchery.client.renderer.KettleRenderer;
import art.arcane.witchery.client.renderer.LeechChestRenderer;
import art.arcane.witchery.client.renderer.LilithRenderer;
import art.arcane.witchery.client.renderer.LordOfTormentRenderer;
import art.arcane.witchery.client.renderer.MandrakeRenderer;
import art.arcane.witchery.client.renderer.MirrorBlockRenderer;
import art.arcane.witchery.client.renderer.NightmareRenderer;
import art.arcane.witchery.client.renderer.DeathHandRenderer;
import art.arcane.witchery.client.renderer.DeathHorseRenderer;
import art.arcane.witchery.client.renderer.DeathRenderer;
import art.arcane.witchery.client.renderer.DreamSpiritRenderer;
import art.arcane.witchery.client.renderer.SpectralUndeadRenderer;
import art.arcane.witchery.client.renderer.PlacedItemRenderer;
import art.arcane.witchery.client.renderer.PoppetShelfRenderer;
import art.arcane.witchery.client.renderer.ReflectionRenderer;
import art.arcane.witchery.client.renderer.MirrorFaceRenderer;
import art.arcane.witchery.client.renderer.SleepingBodyRenderer;
import art.arcane.witchery.client.renderer.SpinningWheelRenderer;
import art.arcane.witchery.client.renderer.SymbolProjectileRenderer;
import art.arcane.witchery.client.renderer.WitchOwlRenderer;
import art.arcane.witchery.client.renderer.WitchToadRenderer;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.GlassGobletItem;
import art.arcane.witchery.item.BroomItem;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.item.CaneSwordItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.item.GuideBookItem;
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
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
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
        event.registerLayerDefinition(DreamSpiritModel.LAYER, DreamSpiritModel::layer);
        event.registerLayerDefinition(DeathModel.LAYER, DeathModel::layer);
        event.registerLayerDefinition(LeonardModel.LAYER, LeonardModel::layer);
        event.registerLayerDefinition(DeathClothingModel.LAYER, DeathClothingModel::layer);
        event.registerLayerDefinition(DeathHandRenderer.LAYER, DeathHandRenderer::layer);
        event.registerLayerDefinition(SpectralUndeadModel.ROBED_LAYER, SpectralUndeadModel::robedLayer);
        event.registerLayerDefinition(SpectralUndeadModel.POLTERGEIST_LAYER, SpectralUndeadModel::poltergeistLayer);
        event.registerLayerDefinition(DemonModel.LAYER, DemonModel::layer);
        event.registerLayerDefinition(EntModel.LAYER, EntModel::layer);
        event.registerLayerDefinition(WitchOwlModel.LAYER, WitchOwlModel::layer);
        event.registerLayerDefinition(WitchToadModel.LAYER, WitchToadModel::layer);
        event.registerLayerDefinition(BroomRenderer.LAYER, BroomRenderer::layer);
        event.registerLayerDefinition(CrystalBallRenderer.LAYER, CrystalBallRenderer::layer);
        event.registerLayerDefinition(ImpModel.LAYER, ImpModel::layer);
        event.registerLayerDefinition(HobgoblinModel.LAYER, HobgoblinModel::layer);
        event.registerLayerDefinition(HobgoblinGodModel.MOG, () -> HobgoblinGodModel.layer(true));
        event.registerLayerDefinition(HobgoblinGodModel.GULG, () -> HobgoblinGodModel.layer(false));
        event.registerLayerDefinition(GoblinEquipmentModel.OUTER, GoblinEquipmentModel::outerLayer);
        event.registerLayerDefinition(GoblinEquipmentModel.INNER, GoblinEquipmentModel::innerLayer);
        event.registerLayerDefinition(StatueOfWorshipRenderer.STONE_LAYER, StatueOfWorshipRenderer::layer);
        event.registerLayerDefinition(DarkMarkModel.LAYER, DarkMarkModel::layer);
        event.registerLayerDefinition(LordOfTormentModel.LAYER, LordOfTormentModel::layer);
        event.registerLayerDefinition(GrotesqueLayer.LAYER, GrotesqueLayer::layer);
        event.registerLayerDefinition(GrassperRenderer.LAYER, GrassperRenderer::layer);
        event.registerLayerDefinition(MirrorBlockRenderer.LAYER, MirrorBlockRenderer::layer);
        event.registerLayerDefinition(MirrorFaceRenderer.LAYER, MirrorFaceRenderer::layer);
        event.registerLayerDefinition(DemonHeartRenderer.LAYER, DemonHeartRenderer::layer);
        event.registerLayerDefinition(DreamCatcherRenderer.LAYER, DreamCatcherRenderer::layer);
        event.registerLayerDefinition(FetishRenderer.SCARECROW, FetishRenderer::scarecrowLayer);
        event.registerLayerDefinition(FetishRenderer.TRENT, FetishRenderer::trentLayer);
        event.registerLayerDefinition(FetishRenderer.LADDER, FetishRenderer::ladderLayer);
        event.registerLayerDefinition(KettleRenderer.LAYER, KettleRenderer::layer);
        event.registerLayerDefinition(LeechChestRenderer.LAYER, LeechChestRenderer::layer);
        event.registerLayerDefinition(CoffinRenderer.LAYER, CoffinRenderer::layer);
        event.registerLayerDefinition(SilverVatRenderer.LAYER, SilverVatRenderer::layer);
        event.registerLayerDefinition(LilithModel.LAYER, LilithModel::layer);
        event.registerLayerDefinition(FollowerRenderer.LAYER, FollowerRenderer::layer);
        event.registerLayerDefinition(VampireRenderer.LAYER, VampireRenderer::layer);
        event.registerLayerDefinition(VillageGuardRenderer.LAYER, VillageGuardRenderer::layer);
        event.registerLayerDefinition(WitchHunterRenderer.LAYER, WitchHunterRenderer::layer);
        event.registerLayerDefinition(HellhoundRenderer.LAYER, HellhoundRenderer::layer);
        event.registerLayerDefinition(HornedHuntsmanRenderer.LAYER, HornedHuntsmanRenderer::layer);
        event.registerLayerDefinition(WolfHeadRenderer.LAYER, WolfHeadRenderer::layer);
        event.registerLayerDefinition(WolfmanRenderer.LAYER, WolfmanRenderer::layer);
        event.registerLayerDefinition(WolfAltarRenderer.LAYER, WolfAltarRenderer::layer);
        event.registerLayerDefinition(HuntingTrapRenderer.LAYER, HuntingTrapRenderer::layer);
        for (var style : VampireClothingItem.Style.values()) {
            event.registerLayerDefinition(VampireClothingModel.layerId(style), () -> VampireClothingModel.layer(style));
        }
        for (var piece : HunterClothingItem.Piece.values()) {
            event.registerLayerDefinition(HunterClothingModel.layerId(piece), () -> HunterClothingModel.layer(piece));
        }
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
        event.registerEntityRenderer(WitcheryEntities.VAMPIRE.get(), VampireRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.VILLAGE_GUARD.get(), VillageGuardRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.WITCH_HUNTER.get(), WitchHunterRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.HELLHOUND.get(), HellhoundRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.HORNED_HUNTSMAN.get(), HornedHuntsmanRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.WOLFMAN.get(), WolfmanRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.WERE_VILLAGER.get(), VillagerRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.ATTACK_BAT.get(), BatRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.SCRY_EYE.get(), NoopRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.MANDRAKE.get(), MandrakeRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.NIGHTMARE.get(), NightmareRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.SPIRIT.get(), context -> new DreamSpiritRenderer<>(context, DreamSpirit::color));
        event.registerEntityRenderer(WitcheryEntities.LOST_SOUL.get(), context -> new DreamSpiritRenderer<>(context, LostSoul::color));
        event.registerEntityRenderer(WitcheryEntities.LEONARD.get(), LeonardRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.ILLUSION_CREEPER.get(), context ->
                new IllusionRenderer<>(context, new CreeperModel<>(context.bakeLayer(ModelLayers.CREEPER)), "creeper"));
        event.registerEntityRenderer(WitcheryEntities.ILLUSION_SPIDER.get(), context ->
                new IllusionRenderer<>(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), "spider"));
        event.registerEntityRenderer(WitcheryEntities.ILLUSION_ZOMBIE.get(), context ->
                new IllusionRenderer<>(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), "zombie"));
        event.registerEntityRenderer(WitcheryEntities.SPECTRE.get(), context -> new SpectralUndeadRenderer(context, "spectre", false));
        event.registerEntityRenderer(WitcheryEntities.BANSHEE.get(), context -> new SpectralUndeadRenderer(context, "banshee", false));
        event.registerEntityRenderer(WitcheryEntities.POLTERGEIST.get(), context -> new SpectralUndeadRenderer(context, "poltergeist", true));
        event.registerEntityRenderer(WitcheryEntities.DEMON.get(), DemonRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.DEATH.get(), DeathRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.DEATH_HORSE.get(), DeathHorseRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.ENT.get(), EntRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.IMP.get(), ImpRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.LORD_OF_TORMENT.get(), LordOfTormentRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.SOULFIRE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.BREW_DROPLET.get(), context -> new ThrownItemRenderer<>(context, 0.5F, false));
        event.registerEntityRenderer(WitcheryEntities.BOLT.get(), WitcheryBoltRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.SYMBOL_PROJECTILE.get(), SymbolProjectileRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.DARK_MARK.get(), DarkMarkRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.WITCH_CAT.get(), CatRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.OWL.get(), WitchOwlRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.TOAD.get(), WitchToadRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.BROOM.get(), BroomRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.ITEM_WAYSTONE.get(), ItemEntityRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.CRYSTAL_BALL.get(), CrystalBallRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.COVEN_WITCH.get(), CovenWitchRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.GOBLIN.get(), HobgoblinRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.MOG.get(), context -> new HobgoblinGodRenderer(context, true));
        event.registerEntityRenderer(WitcheryEntities.GULG.get(), context -> new HobgoblinGodRenderer(context, false));
        event.registerEntityRenderer(WitcheryEntities.REFLECTION.get(), ReflectionRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.MIRROR_FACE.get(), MirrorFaceRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.SLEEPING_BODY.get(), SleepingBodyRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.THROWN_BREW.get(), BrewProjectileRenderer::new);
        event.registerEntityRenderer(WitcheryEntities.GRENADE.get(), ThrownItemRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.KETTLE.get(), KettleRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.BREW_GAS.get(), BrewGasRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.BREW_LIQUID.get(), BrewGasRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.SPINNING_WHEEL.get(), SpinningWheelRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.WOLF_HEAD.get(), WolfHeadRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.WOLF_ALTAR.get(), WolfAltarRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.WORSHIP_STATUE.get(), StatueOfWorshipRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.HUNTING_TRAP.get(), HuntingTrapRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.PLACED_ITEM.get(), PlacedItemRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.GRASSPER.get(), GrassperRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.POPPET_SHELF.get(), PoppetShelfRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.DREAM_CATCHER.get(), DreamCatcherRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.fetish("scarecrow").get(),
                context -> new FetishRenderer(context, FetishRenderer.SCARECROW));
        event.registerBlockEntityRenderer(WitcheryBlockEntities.fetish("trent").get(),
                context -> new FetishRenderer(context, FetishRenderer.TRENT));
        event.registerBlockEntityRenderer(WitcheryBlockEntities.fetish("witchsladder").get(),
                context -> new FetishRenderer(context, FetishRenderer.LADDER));
        event.registerBlockEntityRenderer(WitcheryBlockEntities.DEMON_HEART.get(), DemonHeartRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.MIRROR.get(), MirrorBlockRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.ROOM_MIRROR.get(), MirrorBlockRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.REFILLING_CHEST.get(), ChestRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.LEECH_CHEST.get(), LeechChestRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.COFFIN.get(), CoffinRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.BLOOD_CRUCIBLE.get(), BloodCrucibleRenderer::new);
        event.registerBlockEntityRenderer(WitcheryBlockEntities.SILVER_VAT.get(), SilverVatRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(WitcheryMenus.WITCHES_OVEN.get(), WitchesOvenScreen::new);
            MenuScreens.register(WitcheryMenus.ALTAR.get(), AltarScreen::new);
            MenuScreens.register(WitcheryMenus.DISTILLERY.get(), DistilleryScreen::new);
            MenuScreens.register(WitcheryMenus.SPINNING_WHEEL.get(), SpinningWheelScreen::new);
            MenuScreens.register(WitcheryMenus.LEONARDS_URN.get(), LeonardsUrnScreen::new);
            ItemProperties.register(WitcheryItems.getRequired("ingredient").get(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "guide_book"),
                    (stack, level, entity, seed) -> {
                        var volume = GuideBookItem.volume(stack);
                        return volume == null ? 0 : volume.ordinal() + 1;
                    });
            ItemProperties.register(WitcheryItems.getRequired("leonardsurn").get(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "urn_tier"),
                    (stack, level, entity, seed) -> LeonardsUrnItem.tier(stack));
            for (RegistryObject<MenuType<LegacyPlaceholderMenu>> menuType : WitcheryMenus.placeholders().values()) {
                MenuScreens.register(menuType.get(), LegacyPlaceholderScreen::new);
            }
            for (String name : new String[]{"witchsapling", "witchleaves", "witchlog", "witchwood"}) {
                ItemProperties.register(WitcheryBlocks.getRequired(name).get().asItem(), WOOD_TYPE_MODEL,
                        (stack, level, entity, seed) -> WitchWoodType.fromStack(stack).ordinal());
            }
            ItemProperties.register(WitcheryItems.getRequired("spectralstone").get(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "spectral_type"),
                    (stack, level, entity, seed) -> SpectralStoneItem.typeId(stack));
            ItemProperties.register(WitcheryItems.getRequired("taglockkit").get(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "bound"),
                    (stack, level, entity, seed) -> TaglockItem.isBound(stack) ? 1 : 0);
            ItemProperties.register(WitcheryItems.getRequired("glassgoblet").get(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "filled"),
                    (stack, level, entity, seed) -> GlassGobletItem.filled(stack) ? 1 : 0);
            ItemProperties.register(WitcheryItems.getRequired("canesword").get(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "drawn"),
                    (stack, level, entity, seed) -> CaneSwordItem.isDrawn(stack) ? 1 : 0);
            ItemProperties.register(WitcheryItems.getRequired("brewbottle").get(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "splash"),
                    (stack, level, entity, seed) -> BrewBottleItem.isSplash(stack) ? 1 : 0);
            ItemProperties.register(WitcheryItems.getRequired("playercompass").get(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "needle"),
                    (stack, level, entity, seed) -> PlayerCompassItem.needle(stack));
            ItemProperties.register(WitcheryBlocks.getRequired("crittersnare").get().asItem(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "critter"),
                    (stack, level, entity, seed) -> CritterSnareBlock.critterFromStack(stack).ordinal());
            ItemProperties.register(WitcheryBlocks.getRequired("bramble").get().asItem(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "ender"),
                    (stack, level, entity, seed) -> BrambleBlock.isWild(stack) ? 0 : 1);
            ItemProperties.register(WitcheryBlocks.getRequired("plantmine").get().asItem(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "plant"),
                    (stack, level, entity, seed) -> PlantMineBlock.Item.plantModel(stack));
            ItemProperties.register(WitcheryBlocks.getRequired("wickerbundle").get().asItem(),
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "bloodied"),
                    (stack, level, entity, seed) -> WickerBundleBlock.isBloodied(stack) ? 1 : 0);

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
        event.register((state, level, pos, tint) -> {
            if (level == null || pos == null) return 0x3F76E4;
            if (level.getBlockEntity(pos) instanceof WitchesCauldronBlockEntity cauldron && cauldron.clientBrewColor() >= 0) {
                return cauldron.clientBrewColor();
            }
            return BiomeColors.getAverageWaterColor(level, pos);
        }, WitcheryBlocks.getRequired("cauldron").get());
        event.register((state, level, pos, tint) -> {
            if (tint != 0) {
                return -1;
            }
            WitchWoodType wood = state.getValue(WitchWoodType.PROPERTY);
            return wood == WitchWoodType.ROWAN && level != null && pos != null
                    ? BiomeColors.getAverageFoliageColor(level, pos)
                    : foliageColor(wood);
        }, WitcheryBlocks.getRequired("witchleaves").get());
        event.register((state, level, pos, tint) -> tint != 0 ? -1 : level != null && pos != null
                        ? BiomeColors.getAverageGrassColor(level, pos) : net.minecraft.world.level.GrassColor.get(0.5, 1),
                WitcheryBlocks.getRequired("pitgrass").get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> tint == 0 ? BrewBottleItem.color(stack) : -1, WitcheryItems.getRequired("brewbottle").get());
        event.register((stack, tint) -> tint == 0 ? BrewFuelItem.color(stack) : -1, WitcheryItems.getRequired("brew.fuel").get());
        event.register((stack, tint) -> tint == 0 ? 0x0000FF : -1, WitcheryItems.getRequired("brew.water").get());
        event.register((stack, tint) -> tint == 0 ? net.minecraft.world.level.GrassColor.get(0.5, 1) : -1,
                WitcheryBlocks.getRequired("pitgrass").get());
        event.register((stack, tint) -> tint == 0 && stack.getTag() != null && stack.getTag().contains(BrewDroplet.TINT_KEY)
                ? stack.getTag().getInt(BrewDroplet.TINT_KEY) : -1, WitcheryIngredient.QUARTZ_SPHERE.item());
        var hunterClothing = java.util.Arrays.stream(HunterClothingItem.Piece.values())
                .flatMap(piece -> java.util.Arrays.stream(HunterClothingItem.Upgrade.values())
                        .map(upgrade -> WitcheryItems.getRequired(piece.path(upgrade)).get()))
                .toArray(net.minecraft.world.item.Item[]::new);
        event.register((stack, tint) -> tint == 0 && stack.getItem() instanceof HunterClothingItem clothing
                ? clothing.getColor(stack) : -1, hunterClothing);
        var vampireClothing = java.util.Arrays.stream(VampireClothingItem.Style.values())
                .map(style -> WitcheryItems.getRequired(style.path()).get()).toArray(net.minecraft.world.item.Item[]::new);
        event.register((stack, tint) -> tint == 0 && stack.getItem() instanceof VampireClothingItem clothing
                ? clothing.getColor(stack) : -1, vampireClothing);
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
