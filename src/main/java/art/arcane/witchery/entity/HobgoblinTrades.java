package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;

final class HobgoblinTrades {
    private static final int STARTING_STOCK = 7;
    private static final List<Listing> FARMER = List.of(
            buy(Items.WHEAT, 0.9F, 18, 22), buy(Items.WHITE_WOOL, 0.5F, 14, 22),
            buy(Items.CHICKEN, 0.5F, 14, 18), buy(Items.COOKED_COD, 0.4F, 9, 13),
            sell(Items.BREAD, 0.9F, -4, -2), sell(Items.MELON_SLICE, 0.3F, -8, -4),
            sell(Items.APPLE, 0.3F, -8, -4), sell(Items.COOKIE, 0.3F, -10, -7),
            sell(Items.SHEARS, 0.3F, 3, 4), sell(Items.FLINT_AND_STEEL, 0.3F, 3, 4),
            sell(Items.COOKED_CHICKEN, 0.3F, -8, -6), sell(Items.ARROW, 0.5F, -12, -8));
    private static final List<Listing> SMITH = List.of(
            buy(Items.COAL, 0.7F, 16, 24), buy(Items.IRON_INGOT, 0.5F, 8, 10),
            buy(Items.GOLD_INGOT, 0.5F, 8, 10), buy(Items.DIAMOND, 0.5F, 4, 6),
            sell(Items.IRON_SWORD, 0.5F, 7, 11), sell(Items.DIAMOND_SWORD, 0.5F, 12, 14),
            sell(Items.IRON_AXE, 0.3F, 6, 8), sell(Items.DIAMOND_AXE, 0.3F, 9, 12),
            sell(Items.IRON_PICKAXE, 0.5F, 7, 9), sell(Items.DIAMOND_PICKAXE, 0.5F, 10, 12),
            sell(Items.IRON_SHOVEL, 0.2F, 4, 6), sell(Items.DIAMOND_SHOVEL, 0.2F, 7, 8),
            sell(Items.IRON_HOE, 0.2F, 4, 6), sell(Items.DIAMOND_HOE, 0.2F, 7, 8),
            sell(Items.IRON_BOOTS, 0.2F, 4, 6), sell(Items.DIAMOND_BOOTS, 0.2F, 7, 8),
            sell(Items.IRON_HELMET, 0.2F, 4, 6), sell(Items.DIAMOND_HELMET, 0.2F, 7, 8),
            sell(Items.IRON_CHESTPLATE, 0.2F, 10, 14), sell(Items.DIAMOND_CHESTPLATE, 0.2F, 16, 19),
            sell(Items.IRON_LEGGINGS, 0.2F, 8, 10), sell(Items.DIAMOND_LEGGINGS, 0.2F, 11, 14),
            sell(Items.CHAINMAIL_BOOTS, 0.1F, 5, 7), sell(Items.CHAINMAIL_HELMET, 0.1F, 5, 7),
            sell(Items.CHAINMAIL_CHESTPLATE, 0.1F, 11, 15), sell(Items.CHAINMAIL_LEGGINGS, 0.1F, 9, 11));
    private static final List<Listing> BUTCHER = List.of(
            buy(Items.COAL, 0.7F, 16, 24), buy(Items.PORKCHOP, 0.5F, 14, 18),
            buy(Items.BEEF, 0.5F, 14, 18), sell(Items.SADDLE, 0.1F, 6, 8),
            sell(Items.LEATHER_CHESTPLATE, 0.3F, 4, 5), sell(Items.LEATHER_BOOTS, 0.3F, 2, 4),
            sell(Items.LEATHER_HELMET, 0.3F, 2, 4), sell(Items.LEATHER_LEGGINGS, 0.3F, 2, 4),
            sell(Items.COOKED_PORKCHOP, 0.3F, -7, -5), sell(Items.COOKED_BEEF, 0.3F, -7, -5));

    private HobgoblinTrades() {}

    static void addNext(MerchantOffers offers, int profession, RandomSource random) {
        if (profession == 1 || profession == 2) {
            add(offers, refineryOffer(offers.size(), random));
            return;
        }
        List<Listing> listings = switch (profession) {
            case 0 -> FARMER;
            case 3 -> SMITH;
            default -> BUTCHER;
        };
        float bonus = (float) Math.sqrt(offers.size()) * 0.2F;
        var eligible = new ArrayList<MerchantOffer>();
        for (Listing listing : listings) {
            if (random.nextFloat() < chance(listing.chance, bonus)) eligible.add(listing.create(random));
        }
        if (profession == 0 && random.nextFloat() < chance(0.5F, bonus)) {
            eligible.add(offer(new ItemStack(Items.GRAVEL, 10), new ItemStack(Items.EMERALD),
                    new ItemStack(Items.FLINT, 4 + random.nextInt(2))));
        }
        add(offers, eligible.isEmpty() ? fallback(random) : eligible.get(random.nextInt(eligible.size())));
    }

    private static float chance(float base, float bonus) {
        return Math.min(base + bonus, 1.8F - base - bonus);
    }

    private static MerchantOffer refineryOffer(int step, RandomSource random) {
        Item dust = WitcheryIngredient.KOBOLDITE_DUST.item();
        Item nugget = WitcheryIngredient.KOBOLDITE_NUGGET.item();
        return switch (step) {
            case 0 -> offer(new ItemStack(dust, 9), new ItemStack(Items.GOLD_NUGGET, 5), new ItemStack(nugget));
            case 1 -> offer(new ItemStack(dust, 16), new ItemStack(Items.GOLD_INGOT), new ItemStack(nugget, 2));
            case 2 -> offer(new ItemStack(nugget, 9), new ItemStack(Items.EMERALD), new ItemStack(WitcheryIngredient.KOBOLDITE_INGOT.item()));
            default -> fallback(random);
        };
    }

    private static MerchantOffer fallback(RandomSource random) {
        return offer(new ItemStack(Items.GOLD_INGOT, 8 + random.nextInt(2)), ItemStack.EMPTY, new ItemStack(Items.EMERALD));
    }

    private static void add(MerchantOffers offers, MerchantOffer added) {
        for (int i = 0; i < offers.size(); i++) {
            MerchantOffer old = offers.get(i);
            if (old.getBaseCostA().getItem() == added.getBaseCostA().getItem() && old.getCostB().getItem() == added.getCostB().getItem()
                    && old.getResult().getItem() == added.getResult().getItem()) {
                if (added.getBaseCostA().getCount() < old.getBaseCostA().getCount()
                        || added.getCostB().getCount() < old.getCostB().getCount()) offers.set(i, added);
                return;
            }
        }
        offers.add(added);
    }

    private static MerchantOffer offer(ItemStack first, ItemStack second, ItemStack result) {
        return new MerchantOffer(first, second, result, STARTING_STOCK, 0, 0);
    }

    private static Listing buy(Item item, float chance, int min, int max) {
        return new Listing(item, chance, min, max, true);
    }

    private static Listing sell(Item item, float chance, int min, int max) {
        return new Listing(item, chance, min, max, false);
    }

    private record Listing(Item item, float chance, int min, int max, boolean buys) {
        MerchantOffer create(RandomSource random) {
            if (buys) {
                Item currency = random.nextInt(3) == 0 ? WitcheryIngredient.KOBOLDITE_DUST.item() : Items.EMERALD;
                return offer(new ItemStack(item, count(random)), ItemStack.EMPTY, new ItemStack(currency));
            }
            int amount = count(random);
            return offer(new ItemStack(WitcheryIngredient.KOBOLDITE_NUGGET.item(), Math.max(1, amount)),
                    ItemStack.EMPTY, new ItemStack(item, Math.max(1, -amount)));
        }

        private int count(RandomSource random) {
            return min == max ? min : min + random.nextInt(max - min);
        }
    }
}
