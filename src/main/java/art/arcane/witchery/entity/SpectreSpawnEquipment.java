package art.arcane.witchery.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class SpectreSpawnEquipment {
    private static final UUID SPAWN_RANGE_ID = UUID.fromString("7788dce6-012f-43c9-864b-335f67708f82");
    private static final String SPAWN_RANGE_NAME = "Random spawn bonus";
    private static final List<EquipmentSlot> ARMOR_SLOTS =
            List.of(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    private static final Set<Enchantment> EXCLUSIVE_PROTECTION = Set.of(Enchantments.ALL_DAMAGE_PROTECTION,
            Enchantments.FIRE_PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.PROJECTILE_PROTECTION);

    // The original weighted selection iterates numeric enchantment IDs in the buckets of a 16-entry map.
    private static final List<ArmorEnchantment> ARMOR_ENCHANTMENTS = List.of(
            new ArmorEnchantment(Enchantments.ALL_DAMAGE_PROTECTION, null, 10, 4, 1, 11, 21, 11),
            new ArmorEnchantment(Enchantments.FIRE_PROTECTION, null, 5, 4, 10, 8, 22, 8),
            new ArmorEnchantment(Enchantments.FALL_PROTECTION, EquipmentSlot.FEET, 5, 4, 5, 6, 15, 6),
            new ArmorEnchantment(Enchantments.UNBREAKING, null, 5, 3, 5, 8, 61, 10),
            new ArmorEnchantment(Enchantments.BLAST_PROTECTION, null, 2, 4, 5, 8, 17, 8),
            new ArmorEnchantment(Enchantments.PROJECTILE_PROTECTION, null, 5, 4, 3, 6, 18, 6),
            new ArmorEnchantment(Enchantments.RESPIRATION, EquipmentSlot.HEAD, 2, 3, 10, 10, 40, 10),
            new ArmorEnchantment(Enchantments.AQUA_AFFINITY, EquipmentSlot.HEAD, 2, 1, 1, 0, 41, 0),
            new ArmorEnchantment(Enchantments.THORNS, null, 1, 3, 10, 20, 61, 10));

    private SpectreSpawnEquipment() {}

    static void initialize(SpectralUndead spectre) {
        var followRange = spectre.getAttribute(Attributes.FOLLOW_RANGE);
        var multiplyBase = AttributeModifier.Operation.MULTIPLY_BASE;
        // Ordinary Forge spawns already received vanilla's modifier before reaching this implementation.
        for (var modifier : followRange.getModifiers(multiplyBase).stream()
                .filter(modifier -> SPAWN_RANGE_NAME.equals(modifier.getName())).toList()) {
            followRange.removeModifier(modifier);
        }
        RandomSource random = spectre.getRandom();
        followRange.addPermanentModifier(new AttributeModifier(SPAWN_RANGE_ID, SPAWN_RANGE_NAME,
                random.nextGaussian() * 0.05, multiplyBase));

        var level = spectre.level();
        Difficulty difficulty = level.getDifficulty();
        long inhabitedTicks = level.getChunkAt(spectre.blockPosition()).getInhabitedTime();
        float localDifficulty = localDifficulty(difficulty, inhabitedTicks, level.getMoonBrightness());
        if (random.nextFloat() < 0.15F * localDifficulty) {
            int tier = random.nextInt(2);
            for (int roll = 0; roll < 3; roll++) {
                if (random.nextFloat() < 0.095F) tier++;
            }
            equip(spectre, EquipmentSlot.HEAD, tier);
            if (random.nextFloat() >= (difficulty == Difficulty.HARD ? 0.1F : 0.25F)) {
                equip(spectre, EquipmentSlot.CHEST, tier);
            }
        }

        ItemStack weapon = spectre.getMainHandItem();
        if (!weapon.isEmpty() && random.nextFloat() < 0.25F * localDifficulty) {
            var enchanted = EnchantmentHelper.enchantItem(random, weapon, enchantmentPower(random, localDifficulty), false);
            spectre.setItemSlot(EquipmentSlot.MAINHAND, enchanted);
        }
        for (var slot : ARMOR_SLOTS) {
            ItemStack stack = spectre.getItemBySlot(slot);
            if (stack.isEmpty() || random.nextFloat() >= 0.5F * localDifficulty) continue;
            int power = enchantmentPower(random, localDifficulty);
            if (stack.getItem() instanceof ArmorItem armor) {
                enchantArmor(random, stack, armor.getEquipmentSlot(), power);
            } else {
                spectre.setItemSlot(slot, EnchantmentHelper.enchantItem(random, stack, power, false));
            }
        }
    }

    private static float localDifficulty(Difficulty difficulty, long inhabitedTicks, float moonBrightness) {
        if (difficulty == Difficulty.PEACEFUL) return 0;
        float inhabited = Mth.clamp(inhabitedTicks / 3_600_000F, 0, 1);
        float factor = inhabited * (difficulty == Difficulty.HARD ? 1 : 0.75F) + moonBrightness * 0.25F;
        return difficulty == Difficulty.EASY ? factor * 0.5F : factor;
    }

    private static void equip(SpectralUndead spectre, EquipmentSlot slot, int tier) {
        if (!spectre.getItemBySlot(slot).isEmpty()) return;
        var item = Mob.getEquipmentForSlot(slot, tier);
        if (item != null) spectre.setItemSlot(slot, new ItemStack(item));
    }

    private static int enchantmentPower(RandomSource random, float localDifficulty) {
        return (int) (5F + localDifficulty * random.nextInt(18));
    }

    private static void enchantArmor(RandomSource random, ItemStack stack, EquipmentSlot slot, int power) {
        int enchantability = stack.getEnchantmentValue();
        if (enchantability <= 0) return;
        power += 1 + random.nextInt(enchantability / 4 + 1) + random.nextInt(enchantability / 4 + 1);
        float variance = (random.nextFloat() + random.nextFloat() - 1) * 0.15F;
        int adjustedPower = Math.max(1, (int) (power * (1 + variance) + 0.5F));

        var candidates = new ArrayList<WeightedEntry.Wrapper<EnchantmentInstance>>();
        for (var option : ARMOR_ENCHANTMENTS) {
            var candidate = option.candidate(slot, adjustedPower);
            if (candidate != null) candidates.add(candidate);
        }
        var selected = WeightedRandom.getRandomItem(random, candidates).map(WeightedEntry.Wrapper::getData).orElse(null);
        if (selected == null) return;
        applyArmorEnchantment(stack, candidates, selected);
        // The original keeps drawing against the halved chance even if there are no candidates left.
        for (int remaining = adjustedPower; random.nextInt(50) <= remaining; remaining /= 2) {
            selected = WeightedRandom.getRandomItem(random, candidates).map(WeightedEntry.Wrapper::getData).orElse(null);
            if (selected != null) {
                applyArmorEnchantment(stack, candidates, selected);
            }
        }
    }

    private static void applyArmorEnchantment(ItemStack stack, List<WeightedEntry.Wrapper<EnchantmentInstance>> candidates,
                                              EnchantmentInstance selected) {
        stack.enchant(selected.enchantment, selected.level);
        Enchantment chosen = selected.enchantment;
        candidates.removeIf(candidate -> {
            Enchantment enchantment = candidate.getData().enchantment;
            return enchantment == chosen || EXCLUSIVE_PROTECTION.contains(chosen) && EXCLUSIVE_PROTECTION.contains(enchantment);
        });
    }

    private record ArmorEnchantment(Enchantment enchantment, @Nullable EquipmentSlot slot,
                                    int weight, int maxRank, int min, int minStep, int max, int maxStep) {
        @Nullable
        WeightedEntry.Wrapper<EnchantmentInstance> candidate(EquipmentSlot armor, int power) {
            if (slot != null && slot != armor) return null;
            for (int level = maxRank; level >= 1; level--) {
                if (min + (level - 1) * minStep <= power && power <= max + (level - 1) * maxStep) {
                    return WeightedEntry.wrap(new EnchantmentInstance(enchantment, level), weight);
                }
            }
            return null;
        }
    }
}
