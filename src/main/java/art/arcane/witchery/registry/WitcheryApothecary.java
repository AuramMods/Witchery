package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.item.WitcheryIngredient;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class WitcheryApothecary {
    private static final String NAME = "apothecary";
    private static final String SHELF = "apothecary_shelf";
    private static final ResourceKey<PoiType> SHELF_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, SHELF));
    private static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, Witchery.MODID);
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, Witchery.MODID);
    public static final RegistryObject<PoiType> SHELF_POI = POI_TYPES.register(SHELF, () -> new PoiType(
            Set.copyOf(WitcheryBlocks.APOTHECARY_SHELF.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final RegistryObject<VillagerProfession> PROFESSION = PROFESSIONS.register(NAME,
            () -> new VillagerProfession(NAME, poi -> poi.is(SHELF_KEY), poi -> poi.is(SHELF_KEY),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CLERIC));

    private static final List<Trade> TRADES = List.of(
            new Trade(2, WitcheryIngredient.DOG_TONGUE::item, 2),
            new Trade(1, WitcheryIngredient.BAT_WOOL::item, 3),
            new Trade(4, WitcheryIngredient.SPECTRAL_DUST::item, 1),
            new Trade(5, WitchCrop.GARLIC::seed, 1),
            new Trade(6, WitcheryIngredient.GLOBE_ARTICHOKE::item, 1),
            new Trade(7, () -> Items.POPPY, 5),
            new Trade(8, () -> Items.SLIME_BALL, 1),
            new Trade(3, () -> Items.CLAY_BALL, 5));

    private WitcheryApothecary() {}

    static void register(IEventBus bus) {
        POI_TYPES.register(bus);
        PROFESSIONS.register(bus);
    }

    public static MerchantOffers residentOffers() {
        MerchantOffers offers = new MerchantOffers();
        TRADES.forEach(trade -> offers.add(trade.offer()));
        return offers;
    }

    @SubscribeEvent
    public static void addTrades(VillagerTradesEvent event) {
        if (event.getType() != PROFESSION.get()) return;
        for (int i = 0; i < TRADES.size(); i++) {
            Trade trade = TRADES.get(i);
            event.getTrades().get(i / 2 + 1).add((villager, random) -> trade.offer());
        }
    }

    private record Trade(int emeralds, Supplier<? extends ItemLike> item, int count) {
        private MerchantOffer offer() {
            return new MerchantOffer(new ItemStack(Items.EMERALD, emeralds), new ItemStack(item.get(), count), 7, 5, 0.05F);
        }
    }
}
