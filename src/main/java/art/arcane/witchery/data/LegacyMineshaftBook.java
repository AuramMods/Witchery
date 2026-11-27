package art.arcane.witchery.data;

import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

final class LegacyMineshaftBook {
    // 1.7.10 enchantment IDs, weights, inclusive costs by rank and conflicts.
    private static final List<Rule> RULES = List.of(
            new Rule(0, Enchantments.ALL_DAMAGE_PROTECTION, 10,
                    new int[]{1, 12, 23, 34}, new int[]{21, 32, 43, 54}, 1, 3, 4),
            new Rule(1, Enchantments.FIRE_PROTECTION, 5,
                    new int[]{10, 18, 26, 34}, new int[]{22, 30, 38, 46}, 0, 3, 4),
            new Rule(2, Enchantments.FALL_PROTECTION, 5,
                    new int[]{5, 11, 17, 23}, new int[]{15, 21, 27, 33}),
            new Rule(3, Enchantments.BLAST_PROTECTION, 2,
                    new int[]{5, 13, 21, 29}, new int[]{17, 25, 33, 41}, 0, 1, 4),
            new Rule(4, Enchantments.PROJECTILE_PROTECTION, 5,
                    new int[]{3, 9, 15, 21}, new int[]{18, 24, 30, 36}, 0, 1, 3),
            new Rule(5, Enchantments.RESPIRATION, 2,
                    new int[]{10, 20, 30}, new int[]{40, 50, 60}),
            new Rule(6, Enchantments.AQUA_AFFINITY, 2,
                    new int[]{1}, new int[]{41}),
            new Rule(7, Enchantments.THORNS, 1,
                    new int[]{10, 30, 50}, new int[]{61, 71, 81}),
            new Rule(16, Enchantments.SHARPNESS, 10,
                    new int[]{1, 12, 23, 34, 45}, new int[]{21, 32, 43, 54, 65}, 17, 18),
            new Rule(17, Enchantments.SMITE, 5,
                    new int[]{5, 13, 21, 29, 37}, new int[]{25, 33, 41, 49, 57}, 16, 18),
            new Rule(18, Enchantments.BANE_OF_ARTHROPODS, 5,
                    new int[]{5, 13, 21, 29, 37}, new int[]{25, 33, 41, 49, 57}, 16, 17),
            new Rule(19, Enchantments.KNOCKBACK, 5,
                    new int[]{5, 25}, new int[]{61, 71}),
            new Rule(20, Enchantments.FIRE_ASPECT, 2,
                    new int[]{10, 30}, new int[]{61, 71}),
            new Rule(21, Enchantments.MOB_LOOTING, 2,
                    new int[]{15, 24, 33}, new int[]{61, 71, 81}, 33),
            new Rule(32, Enchantments.BLOCK_EFFICIENCY, 10,
                    new int[]{1, 11, 21, 31, 41}, new int[]{61, 71, 81, 91, 101}),
            new Rule(33, Enchantments.SILK_TOUCH, 1,
                    new int[]{15}, new int[]{61}, 35),
            new Rule(34, Enchantments.UNBREAKING, 5,
                    new int[]{5, 13, 21}, new int[]{61, 71, 81}),
            new Rule(35, Enchantments.BLOCK_FORTUNE, 2,
                    new int[]{15, 24, 33}, new int[]{61, 71, 81}, 33),
            new Rule(48, Enchantments.POWER_ARROWS, 10,
                    new int[]{1, 11, 21, 31, 41}, new int[]{16, 26, 36, 46, 56}),
            new Rule(49, Enchantments.PUNCH_ARROWS, 2,
                    new int[]{12, 32}, new int[]{37, 57}),
            new Rule(50, Enchantments.FLAMING_ARROWS, 2,
                    new int[]{20}, new int[]{50}),
            new Rule(51, Enchantments.INFINITY_ARROWS, 1,
                    new int[]{20}, new int[]{50}),
            new Rule(61, Enchantments.FISHING_LUCK, 2,
                    new int[]{15, 24, 33}, new int[]{61, 71, 81}, 33),
            new Rule(62, Enchantments.FISHING_SPEED, 2,
                    new int[]{15, 24, 33}, new int[]{61, 71, 81}));

    private LegacyMineshaftBook() {}

    static ItemStack roll(RandomSource random) {
        // Book enchantability is one. Both integer draws still advance the original random source.
        int level = 31 + random.nextInt(1) + random.nextInt(1);
        float variance = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
        int power = Math.round(level * (1.0F + variance));
        // The original weighted iteration traversed a HashMap keyed by numeric enchantment ID.
        var candidates = new HashMap<Integer, WeightedEntry.Wrapper<Choice>>();
        for (Rule rule : RULES) {
            int rank = rule.rankAt(power);
            if (rank > 0) candidates.put(rule.id(), WeightedEntry.wrap(new Choice(rule, rank), rule.weight()));
        }

        var book = new ItemStack(Items.ENCHANTED_BOOK);
        Rule selected = addEnchantment(book, random, candidates.values());
        for (int remaining = power; random.nextInt(50) <= remaining; remaining /= 2) {
            Rule previous = selected;
            candidates.values().removeIf(candidate -> previous.conflictsWith(candidate.getData().rule()));
            if (!candidates.isEmpty()) selected = addEnchantment(book, random, candidates.values());
        }
        return book;
    }

    private static Rule addEnchantment(ItemStack book, RandomSource random,
                                       Collection<WeightedEntry.Wrapper<Choice>> candidates) {
        var choice = WeightedRandom.getRandomItem(random, List.copyOf(candidates)).orElseThrow().getData();
        EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(choice.rule().enchantment(), choice.rank()));
        return choice.rule();
    }

    private record Choice(Rule rule, int rank) {}

    private record Rule(int id, Enchantment enchantment, int weight, int[] minCost, int[] maxCost, int... conflicts) {
        int rankAt(int power) {
            for (int rank = minCost.length; rank > 0; rank--) {
                if (power >= minCost[rank - 1] && power <= maxCost[rank - 1]) return rank;
            }
            return 0;
        }

        boolean conflictsWith(Rule other) {
            // Forge checks the original compatibility rules in both directions.
            return excludes(other.id()) || other.excludes(id);
        }

        boolean excludes(int other) {
            if (id == other) return true;
            for (int conflict : conflicts) {
                if (conflict == other) return true;
            }
            return false;
        }
    }
}
