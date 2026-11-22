package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class AnimalMerchant implements Merchant {
    public static final String STOCK_TAG = "WitcheryShopStock";
    private static final Map<Mob, WeakReference<AnimalMerchant>> OPEN_SHOPS = new WeakHashMap<>();
    private final Mob animal;
    @Nullable private Player customer;
    @Nullable private MerchantOffers offers;

    private AnimalMerchant(Mob animal, Player customer) {
        this.animal = animal;
        this.customer = customer;
    }

    public static boolean eligible(Player player, Mob animal) {
        if (!animal.isAlive() || animal.isBaby() || animal.getTarget() != null
                || animal instanceof SpectralFamiliar || animal instanceof CovenWitch || animal instanceof Imp
                || animal instanceof Bat && animal.getPersistentData().getBoolean("WITCNoDrops")) return false;
        return animal instanceof Animal || animal instanceof AmbientCreature || animal instanceof Spider
                || animal instanceof WaterAnimal || WitchClothingItem.deters(animal, player);
    }

    public static boolean open(ServerPlayer player, Mob animal) {
        var previous = OPEN_SHOPS.get(animal);
        var current = previous == null ? null : previous.get();
        if (current != null) {
            if (current.getTradingPlayer() != null) return false;
            current.setTradingPlayer(null);
        }
        var shop = new AnimalMerchant(animal, player);
        var menu = player.openMenu(new SimpleMenuProvider((id, inventory, customer) -> new MerchantMenu(id, inventory, shop),
                animal.getDisplayName()));
        if (menu.isEmpty()) return false;
        OPEN_SHOPS.put(animal, new WeakReference<>(shop));
        player.sendMerchantOffers(menu.getAsInt(), shop.getOffers(), 0, 0, false, false);
        for (int i = 0; i < 3; i++) animal.playAmbientSound();
        return true;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        customer = player;
        if (player == null) {
            var registered = OPEN_SHOPS.get(animal);
            if (registered != null && registered.get() == this) OPEN_SHOPS.remove(animal);
        }
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        if (!(customer instanceof ServerPlayer player) || player.hasDisconnected() || !player.isAlive()
                || !animal.isAlive() || animal.level() != player.level() || animal.distanceToSqr(player) > 64) return null;
        return player;
    }

    @Override
    public MerchantOffers getOffers() {
        if (offers == null) {
            var data = animal.getPersistentData();
            if (data.contains(STOCK_TAG, Tag.TAG_COMPOUND)) {
                offers = loadOffers(data.getCompound(STOCK_TAG));
            } else {
                offers = AnimalTradeStock.generate(animal, animal.level().getRandom());
                data.put(STOCK_TAG, offers.createTag());
            }
        }
        return offers;
    }

    private static MerchantOffers loadOffers(CompoundTag saved) {
        var loaded = new MerchantOffers();
        for (Tag entry : saved.getList("Recipes", Tag.TAG_COMPOUND)) {
            CompoundTag recipe = ((CompoundTag) entry).copy();
            ItemStack buy = WitcheryIngredient.loadInventoryStack(recipe.getCompound("buy"));
            ItemStack sell = WitcheryIngredient.loadInventoryStack(recipe.getCompound("sell"));
            if (buy.isEmpty() || sell.isEmpty()) continue;
            recipe.put("buy", buy.save(new CompoundTag()));
            recipe.put("sell", sell.save(new CompoundTag()));
            if (recipe.contains("buyB", Tag.TAG_COMPOUND)) {
                CompoundTag oldSecond = recipe.getCompound("buyB");
                ItemStack second = WitcheryIngredient.loadInventoryStack(oldSecond);
                if (oldSecond.getByte("Count") > 0 && second.isEmpty()) continue;
                recipe.put("buyB", second.save(new CompoundTag()));
            }
            loaded.add(new MerchantOffer(recipe));
        }
        return loaded;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        animal.getPersistentData().put(STOCK_TAG, getOffers().createTag());
        animal.playAmbientSound();
    }

    @Override
    public void notifyTradeUpdated(ItemStack result) {
        animal.playAmbientSound();
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {}

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }
}
