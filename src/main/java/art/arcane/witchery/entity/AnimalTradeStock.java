package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;

final class AnimalTradeStock {
    private AnimalTradeStock() {}

    static MerchantOffers generate(Mob animal, RandomSource random) {
        var goods = new ArrayList<ItemStack>();
        var common = commonGoods();
        goods.add(common.get(random.nextInt(common.size())));
        if (random.nextDouble() < 0.03) goods.add(new ItemStack(WitcheryIngredient.TREEFYD_SEEDS.item()));

        List<Item> currencies = addLocalGoods(animal, random, goods);
        var candidates = new ObjectArrayList<MerchantOffer>(goods.size());
        for (ItemStack goodsStack : goods) candidates.add(price(goodsStack, currencies, animal, random));
        Util.shuffle(candidates, random);

        var offers = new MerchantOffers();
        int count = Math.min(candidates.size(), 1 + random.nextInt(2));
        for (int index = 0; index < count; index++) offers.add(candidates.get(index));
        return offers;
    }

    private static List<ItemStack> commonGoods() {
        return List.of(new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item(), 3), new ItemStack(WitcheryIngredient.BELLADONNA_FLOWER.item(), 3),
                new ItemStack(WitcheryIngredient.GLOBE_ARTICHOKE.item(), 3),
                new ItemStack(Items.OAK_SAPLING, 4), new ItemStack(Items.SPRUCE_SAPLING, 4),
                new ItemStack(Items.BIRCH_SAPLING, 4), new ItemStack(Items.JUNGLE_SAPLING, 4),
                new ItemStack(Items.SUGAR_CANE, 2), new ItemStack(Items.CACTUS, 2),
                new ItemStack(Items.GOLD_NUGGET, 5), new ItemStack(Items.IRON_INGOT, 2),
                new ItemStack(Items.BONE, 4), new ItemStack(Items.FLINT, 5), new ItemStack(WitcheryIngredient.DOG_TONGUE.item(), 2),
                new ItemStack(Items.POTATO, 5), new ItemStack(Items.POISONOUS_POTATO, 2), new ItemStack(Items.CARROT, 5),
                new ItemStack(Items.CLAY_BALL, 10));
    }

    private static List<Item> addLocalGoods(Mob animal, RandomSource random, List<ItemStack> goods) {
        if (animal instanceof Pig) {
            goods.add(new ItemStack(Items.RED_MUSHROOM, 5));
            goods.add(new ItemStack(Items.BROWN_MUSHROOM, 5));
            addGems(random, goods);
            return List.of(Items.CARROT, Items.APPLE, Items.POTATO);
        }
        if (animal instanceof AbstractHorse) {
            if (random.nextDouble() < 0.01) goods.add(new ItemStack(Items.SADDLE));
            return List.of(Items.CARROT, Items.APPLE, Items.WHEAT);
        }
        if (animal instanceof Wolf) {
            goods.add(new ItemStack(Items.BONE, 5));
            addGems(random, goods);
            return List.of(Items.BEEF, Items.PORKCHOP, Items.CHICKEN);
        }
        if (animal instanceof Cat || animal instanceof Ocelot) return List.of(Items.MILK_BUCKET, Items.COD);
        // Mooshrooms also matched the Cow branch in the original shop.
        if (animal instanceof Cow || animal instanceof Sheep) return List.of(Items.WHEAT);
        if (animal instanceof Chicken) {
            goods.add(new ItemStack(Items.FEATHER, 10));
            goods.add(new ItemStack(Items.EGG, 5));
            return List.of(Items.WHEAT_SEEDS);
        }
        if (animal instanceof Squid) {
            goods.add(new ItemStack(Items.INK_SAC, 10));
            return List.of(Items.COD);
        }
        if (animal instanceof Bat) {
            goods.add(new ItemStack(WitcheryIngredient.BAT_WOOL.item(), 5));
            return List.of(Items.WHEAT_SEEDS, Items.WHEAT, Items.BEEF, Items.PORKCHOP);
        }
        if (animal instanceof Spider) {
            goods.add(new ItemStack(Items.STRING, 8));
            goods.add(new ItemStack(WitcheryIngredient.WEB.item(), 4));
            return List.of(Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.COD);
        }
        if (animal instanceof Creeper) {
            if (random.nextDouble() < 0.05) goods.add(new ItemStack(WitcheryIngredient.SPECTRAL_DUST.item(), 2));
            if (random.nextDouble() < 0.10) goods.add(new ItemStack(WitcheryIngredient.TREEFYD_SEEDS.item()));
            if (random.nextDouble() < 0.02) goods.add(new ItemStack(WitcheryIngredient.CREEPER_HEART.item()));
            return List.of(Items.GUNPOWDER, Items.COD);
        }
        if (animal.getMobType() == MobType.UNDEAD) {
            goods.add(new ItemStack(WitcheryIngredient.SPECTRAL_DUST.item()));
            return List.of(Items.BONE);
        }
        return List.of(Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.COD, Items.WHEAT, Items.WHEAT_SEEDS,
                Items.CARROT, Items.APPLE, Items.POTATO);
    }

    private static void addGems(RandomSource random, List<ItemStack> goods) {
        if (random.nextDouble() < 0.02) goods.add(new ItemStack(Items.EMERALD));
        if (random.nextDouble() < 0.01) goods.add(new ItemStack(Items.DIAMOND));
    }

    private static MerchantOffer price(ItemStack offered, List<Item> currencies, Mob animal, RandomSource random) {
        int quantity = Math.min(offered.getMaxStackSize(), random.nextInt(offered.getCount()) + (offered.getCount() > 4 ? 3 : 1));
        Item currency = currencies.get(random.nextInt(currencies.size()));
        boolean premium = offered.is(Items.DIAMOND) || offered.is(Items.EMERALD) || offered.is(Items.SADDLE)
                || offered.is(WitcheryIngredient.TREEFYD_SEEDS.item()) || animal.getMobType() == MobType.UNDEAD;
        int multiplier = premium ? 2 : 1;
        int factor = quantity > 4 ? 1 : 2;
        int cost = random.nextInt(2) + quantity * multiplier * (random.nextInt(2) + factor);
        ItemStack payment = new ItemStack(currency);
        payment.setCount(Math.min(cost, payment.getMaxStackSize()));
        return new MerchantOffer(payment, offered.copyWithCount(quantity), 1 + random.nextInt(2), 0, 0);
    }
}
