package art.arcane.witchery.registry;

import art.arcane.witchery.block.StockadeBlock;
import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.CursedControls;
import art.arcane.witchery.block.GlowGlobeBlock;
import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.block.WickerBundleBlock;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.WitchWoodBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class WitcheryCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Witchery.MODID);

    public static final RegistryObject<CreativeModeTab> WITCHERY_TAB = CREATIVE_MODE_TABS.register("witchery_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.literal("Witchery"))
                    .icon(() -> {
                        RegistryObject<net.minecraft.world.item.Item> iconItem = WitcheryItems.LEGACY_ITEMS.values().iterator().next();
                        return new ItemStack(iconItem.get());
                    })
                    .displayItems((parameters, output) -> WitcheryItems.allForCreativeTab().forEach(entry -> {
                        if (entry.get() instanceof WitchWoodBlockItem wood) {
                            for (WitchWoodType type : WitchWoodType.values()) {
                                output.accept(type.stack(wood));
                            }
                        } else if (entry.get() instanceof StockadeBlock.Item stockade) {
                            for (var wood : StockadeBlock.Wood.values()) {
                                output.accept(wood.stack(stockade.getBlock()));
                            }
                        } else if (entry.get() instanceof PlantMineBlock.Item mine) {
                            for (PlantMineBlock.Variant variant : PlantMineBlock.Variant.values()) {
                                output.accept(variant.stack(mine));
                            }
                        } else if (entry.get() instanceof WickerBundleBlock.Item wicker) {
                            output.accept(wicker);
                            var bloodied = new ItemStack(wicker);
                            WickerBundleBlock.markBloodied(bloodied);
                            output.accept(bloodied);
                        } else if (entry.get() instanceof BrambleBlock.Item bramble) {
                            output.accept(bramble);
                            var ender = new ItemStack(bramble);
                            BrambleBlock.setWild(ender, false);
                            output.accept(ender);
                        } else if (!(entry.get() instanceof BrewBottleItem)) {
                            if (!(entry.get() instanceof BlockItem blockItem
                                    && (blockItem.getBlock() instanceof CursedControls.Control
                                    || blockItem.getBlock() instanceof BrewDispersalBlock
                                    || blockItem.getBlock() instanceof GlowGlobeBlock))) {
                                output.accept(entry.get());
                            }
                        }
                    }))
                    .build());

    private WitcheryCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
