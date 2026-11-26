package art.arcane.witchery.world.migration;

import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class StoredMerchantOffers extends MerchantOffers {
    private final List<PendingOffer> pending = new ArrayList<>();

    private record PendingOffer(int index, CompoundTag recipe) {}

    private StoredMerchantOffers() {}

    public static StoredMerchantOffers read(CompoundTag saved) {
        return read(saved, WitcheryIngredient::loadInventoryStack);
    }

    public static StoredMerchantOffers read(CompoundTag saved, Function<CompoundTag, ItemStack> loadStack) {
        var offers = new StoredMerchantOffers();
        var recipes = saved.getList("Recipes", Tag.TAG_COMPOUND);
        for (int index = 0; index < recipes.size(); index++) {
            var recipe = recipes.getCompound(index);
            var buyTag = recipe.getCompound("buy");
            var secondTag = recipe.getCompound("buyB");
            var sellTag = recipe.getCompound("sell");
            if (buyTag.getInt("Count") <= 0 || sellTag.getInt("Count") <= 0) continue;

            ItemStack buy = loadStack.apply(buyTag.copy());
            String secondId = secondTag.getString("id");
            // An empty payment can acquire a positive count and tags in a modded save.
            boolean hasSecondPayment = secondTag.getInt("Count") > 0
                    && !secondId.equals("minecraft:air") && !secondId.equals("air");
            ItemStack second = hasSecondPayment ? loadStack.apply(secondTag.copy()) : ItemStack.EMPTY;
            ItemStack sell = loadStack.apply(sellTag.copy());
            if (buy.isEmpty() || sell.isEmpty() || hasSecondPayment && second.isEmpty()) {
                // Keep unavailable goods and payments out of the menu until a later load can resolve them.
                offers.pending.add(new PendingOffer(index, recipe.copy()));
                continue;
            }

            var loaded = recipe.copy();
            loaded.put("buy", buy.save(new CompoundTag()));
            loaded.put("buyB", second.save(new CompoundTag()));
            loaded.put("sell", sell.save(new CompoundTag()));
            offers.add(new MerchantOffer(loaded));
        }
        return offers;
    }

    public boolean hasPending() {
        return !pending.isEmpty();
    }

    @Override
    public CompoundTag createTag() {
        CompoundTag saved = super.createTag();
        var recipes = saved.getList("Recipes", Tag.TAG_COMPOUND);
        for (PendingOffer offer : pending) {
            recipes.add(Math.min(offer.index(), recipes.size()), offer.recipe().copy());
        }
        return saved;
    }
}
