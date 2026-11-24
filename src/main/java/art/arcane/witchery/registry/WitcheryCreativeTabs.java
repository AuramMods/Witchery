package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.CursedControls;
import art.arcane.witchery.block.GlowGlobeBlock;
import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.block.ShadedGlassBlock;
import art.arcane.witchery.block.StockadeBlock;
import art.arcane.witchery.block.WickerBundleBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.block.entity.FetishBlockEntity;
import art.arcane.witchery.block.entity.FetishEffect;
import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.CircleTalismanItem;
import art.arcane.witchery.item.FetishItem;
import art.arcane.witchery.item.LeonardsUrnItem;
import art.arcane.witchery.item.SpectralStoneItem;
import art.arcane.witchery.item.VampireBookItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.item.WitchWoodBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Set;

public final class WitcheryCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Witchery.MODID);

    private static final Set<String> HIDDEN_ITEMS = Set.of(
            "ingredient", "bound_waystone", "creature_bound_waystone",
            "coffinblock", "demonheart", "dreamcatcher", "mirrorblock", "mirrorblock2",
            "distilleryburning", "witchesovenburning",
            "artichoke", "belladonna", "garlicplant", "mandrake", "mindrake", "snowbell", "wolfsbane", "wormwood",
            "barrier", "bucketbrew", "cactus", "circle", "circleglyphinfernal", "circleglyphotherwhere",
            "circleglyphritual", "force", "light", "lilypad", "placeditem", "slurp", "spiritportal",
            "tormentstone", "vine", "wallgen", "web");
    private static final int[] TALISMAN_PATTERNS = {0, 1, 2, 3, 8, 16, 24, 64, 128, 192};

    public static final RegistryObject<CreativeModeTab> WITCHERY_TAB = CREATIVE_MODE_TABS.register("witchery_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Witchery"))
                    .icon(() -> new ItemStack(WitcheryIngredient.ENCHANTED_BROOM.item()))
                    .displayItems((parameters, output) -> WitcheryItems.allForCreativeTab()
                            .forEach(entry -> addItem(entry.get(), entry.getId().getPath(), output)))
                    .build());

    private WitcheryCreativeTabs() {
    }

    private static void addItem(Item item, String path, CreativeModeTab.Output output) {
        if (HIDDEN_ITEMS.contains(path) || item instanceof BrewBottleItem) return;
        if (item instanceof BlockItem blockItem && (blockItem.getBlock() instanceof CursedControls.Control
                || blockItem.getBlock() instanceof LiquidBlock || blockItem.getBlock() instanceof GlowGlobeBlock)) return;

        if (item instanceof WitchWoodBlockItem wood) {
            for (WitchWoodType type : WitchWoodType.values()) output.accept(type.stack(wood));
        } else if (item instanceof StockadeBlock.Item stockade) {
            for (var wood : StockadeBlock.Wood.values()) output.accept(wood.stack(stockade.getBlock()));
        } else if (item instanceof PlantMineBlock.Item mine) {
            for (PlantMineBlock.Variant variant : PlantMineBlock.Variant.values()) output.accept(variant.stack(mine));
        } else if (item instanceof ShadedGlassBlock.Item glass) {
            if (!glass.isDarkened()) {
                for (ShadedGlassBlock.Color color : ShadedGlassBlock.Color.values()) output.accept(color.stack(glass));
            }
        } else if (item instanceof WickerBundleBlock.Item wicker) {
            output.accept(wicker);
            var bloodied = new ItemStack(wicker);
            WickerBundleBlock.markBloodied(bloodied);
            output.accept(bloodied);
        } else if (item instanceof BrambleBlock.Item bramble) {
            output.accept(bramble);
            var ender = new ItemStack(bramble);
            BrambleBlock.setWild(ender, false);
            output.accept(ender);
        } else if (item instanceof CircleTalismanItem) {
            for (int pattern : TALISMAN_PATTERNS) {
                var stack = new ItemStack(item);
                CircleTalismanItem.setStoredPattern(stack, pattern);
                output.accept(stack);
            }
        } else if (item instanceof SpectralStoneItem) {
            output.accept(item);
            for (var kind : SpectralUndead.Kind.values()) {
                var stack = new ItemStack(item);
                SpectralStoneItem.applyLegacyContents(stack, 16 | kind.legacyId());
                output.accept(stack);
            }
        } else if (item instanceof LeonardsUrnItem) {
            output.accept(item);
            for (int tier = 1; tier <= 3; tier++) {
                var stack = new ItemStack(item);
                LeonardsUrnItem.setTier(stack, tier);
                output.accept(stack);
            }
        } else if (item instanceof VampireBookItem) {
            output.accept(item);
            var complete = new ItemStack(item);
            VampireBookItem.setTornPages(complete, VampireBookItem.MAX_TORN_PAGES);
            output.accept(complete);
        } else if (item instanceof FetishItem) {
            addFetishes(item, path, output);
        } else {
            output.accept(item);
        }
    }

    private static void addFetishes(Item item, String path, CreativeModeTab.Output output) {
        output.accept(item);
        var effects = switch (path) {
            case "scarecrow" -> List.of(FetishEffect.ENHANCED_POPPETS, FetishEffect.SCREAMER,
                    FetishEffect.SENTINEL, FetishEffect.TWISTER, FetishEffect.GHOST_WALKER);
            case "witchsladder" -> List.of(FetishEffect.SCREAMER);
            default -> List.<FetishEffect>of();
        };
        for (var effect : effects) {
            var stack = new ItemStack(item);
            FetishBlockEntity.setItemEffect(stack, effect);
            output.accept(stack);
        }
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
