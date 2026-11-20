package art.arcane.witchery.brewing;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class BrewMixture {
    private static final String ITEMS = "Items";
    private static final Map<ResourceLocation, Ingredient> INGREDIENTS = ingredients();

    private enum Modifier {
        CAPACITY, STRENGTH, DURATION, INVERT, HIDE_PARTICLES, DRINK_SPEED, SPLASH, EXTENT
    }

    private record Ingredient(int power, @Nullable Modifier modifier, int amount, int ceiling,
                              int yieldModifier, @Nullable Potion normal, @Nullable Potion inverted) {
        boolean isEffect() { return normal != null; }
    }

    private record Potion(Supplier<MobEffect> effect, int duration) {}

    public record Application(MobEffect effect, int ticks, int amplifier, boolean showParticles) {
        public MobEffectInstance instance(int duration) {
            return new MobEffectInstance(effect, duration, amplifier, false, showParticles, true);
        }
    }

    private final List<Application> effects;
    private final int capacity;
    private final int used;
    private final int power;
    private final int drinkTicks;
    private final int yieldModifier;
    private final int color;
    private final boolean splash;
    private final int splashRadius;

    private BrewMixture(Mixing mixing) {
        effects = List.copyOf(mixing.effects);
        capacity = mixing.capacity;
        used = mixing.used;
        power = mixing.power;
        drinkTicks = Math.max(2, mixing.drinkTicks);
        yieldModifier = mixing.yieldModifier;
        color = mixing.color & 0xFFFFFF;
        splash = mixing.splash;
        splashRadius = 3 + mixing.extent;
    }

    public static boolean canAdd(List<ItemStack> current, ItemStack offered) {
        Mixing mixing = mix(current);
        ResourceLocation id = id(offered);
        Ingredient ingredient = INGREDIENTS.get(id);
        return mixing != null && ingredient != null && !mixing.segment.contains(id)
                && (!ingredient.isEffect() || mixing.used + ingredient.amount <= mixing.capacity);
    }

    public static void add(List<ItemStack> current, ItemStack offered) {
        Ingredient ingredient = INGREDIENTS.get(id(offered));
        if (ingredient != null && ingredient.modifier == Modifier.SPLASH) {
            current.removeIf(stack -> {
                Ingredient previous = INGREDIENTS.get(id(stack));
                return previous != null && previous.modifier == Modifier.SPLASH;
            });
        }
        current.add(offered.copyWithCount(1));
    }

    @Nullable
    public static BrewMixture from(List<ItemStack> stacks) {
        if (stacks.isEmpty()) return null;
        Mixing mixing = mix(stacks);
        return mixing != null ? new BrewMixture(mixing) : null;
    }

    @Nullable
    public static BrewMixture from(@Nullable CompoundTag tag) {
        if (tag == null) return null;
        List<ItemStack> stacks = new ArrayList<>();
        for (Tag entry : tag.getList(ITEMS, Tag.TAG_COMPOUND)) {
            stacks.add(WitcheryIngredient.loadInventoryStack((CompoundTag) entry));
        }
        BrewMixture mixture = from(stacks);
        return mixture != null && (!tag.getBoolean("Splash") || mixture.splash) ? mixture : null;
    }

    public CompoundTag bottleTag(List<ItemStack> stacks) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (ItemStack stack : stacks) items.add(stack.save(new CompoundTag()));
        tag.put(ITEMS, items);
        tag.putInt("Power", power);
        tag.putInt("Color", color);
        tag.putInt("EffectCount", effects.size());
        tag.putInt("UsedCapacity", used);
        tag.putInt("RemainingCapacity", capacity - used);
        tag.putInt("BrewDrinkSpeed", drinkTicks);
        tag.putBoolean("Splash", splash);
        return tag;
    }

    public List<Application> effects() { return effects; }
    public int power() { return power; }
    public int used() { return used; }
    public int capacity() { return capacity; }
    public int drinkTicks() { return drinkTicks; }
    public int yieldModifier() { return yieldModifier; }
    public int color() { return color; }
    public boolean splash() { return splash; }
    public int splashRadius() { return splashRadius; }

    public void apply(LivingEntity target, @Nullable Entity source, @Nullable LivingEntity caster,
                      double strengthScale, double durationScale) {
        if (target.isDeadOrDying() || durationScale <= 0) return;
        for (Application application : effects) {
            if (application.effect.isInstantenous()) {
                application.effect.applyInstantenousEffect(source, caster, target, application.amplifier, strengthScale);
            } else {
                target.addEffect(application.instance(Mth.ceil(application.ticks * durationScale)), source);
            }
        }
    }

    private static ResourceLocation id(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    @Nullable
    private static Mixing mix(List<ItemStack> stacks) {
        Mixing mixing = new Mixing();
        for (ItemStack stack : stacks) {
            ResourceLocation id = id(stack);
            Ingredient ingredient = INGREDIENTS.get(id);
            if (ingredient == null || stack.isEmpty()) return null;
            mixing.add(id, ingredient);
        }
        return mixing;
    }

    private static final class Mixing {
        private final List<Application> effects = new ArrayList<>();
        private final Set<ResourceLocation> segment = new HashSet<>();
        private int capacity;
        private int used;
        private int power;
        private int drinkTicks = 32;
        private int yieldModifier;
        private int color = 17;
        private boolean splash;
        private int extent;
        private int strength;
        private int duration;
        private int strengthBoosts;
        private int durationBoosts;
        private boolean inverted;
        private boolean hideParticles;
        private boolean unlimitedBoosts;

        void add(ResourceLocation id, Ingredient ingredient) {
            power += ingredient.power;
            yieldModifier += ingredient.yieldModifier;
            color = color * 37 + id.hashCode();
            if (ingredient.isEffect()) {
                applyEffect(ingredient);
                segment.clear();
            } else {
                applyModifier(ingredient);
            }
            segment.add(id);
        }

        private void applyEffect(Ingredient ingredient) {
            if (used + ingredient.amount > capacity) return;
            used += ingredient.amount;
            Potion effect = inverted ? ingredient.inverted : ingredient.normal;
            effects.add(new Application(effect.effect.get(), effect.duration * (duration + 1),
                    Math.min(strength, ingredient.ceiling), !hideParticles));
            strength = 0;
            duration = 0;
            inverted = false;
            hideParticles = false;
        }

        private void applyModifier(Ingredient ingredient) {
            switch (ingredient.modifier) {
                case CAPACITY -> {
                    if (capacity < ingredient.ceiling) capacity += ingredient.amount;
                    if (ingredient.ceiling == 10) unlimitedBoosts = true;
                }
                case STRENGTH -> {
                    if (strength < ingredient.ceiling && (strengthBoosts < 7 || unlimitedBoosts)) {
                        strength++;
                        strengthBoosts++;
                    }
                }
                case DURATION -> {
                    if (duration < ingredient.ceiling && (durationBoosts < 7 || unlimitedBoosts)) {
                        duration++;
                        durationBoosts++;
                    }
                }
                case INVERT -> inverted = true;
                case HIDE_PARTICLES -> hideParticles = true;
                case DRINK_SPEED -> drinkTicks += ingredient.amount;
                case SPLASH -> splash = true;
                case EXTENT -> {
                    if (extent < ingredient.ceiling) extent++;
                }
            }
        }
    }

    private static Map<ResourceLocation, Ingredient> ingredients() {
        Map<ResourceLocation, Ingredient> entries = new HashMap<>();
        modifier(entries, "witchery:mandrake_root", 0, Modifier.CAPACITY, 1, 1, 0);
        modifier(entries, "nether_wart", 50, Modifier.CAPACITY, 2, 2, 0);
        modifier(entries, "witchery:tear_of_the_goddess", 100, Modifier.CAPACITY, 2, 4, 0);
        modifier(entries, "witchery:diamond_vapour", 150, Modifier.CAPACITY, 2, 6, 0);
        modifier(entries, "diamond", 150, Modifier.CAPACITY, 2, 8, -2);
        modifier(entries, "nether_star", 150, Modifier.CAPACITY, 4, 10, 0);
        modifier(entries, "glowstone_dust", 50, Modifier.STRENGTH, 1, 1, 0);
        modifier(entries, "blaze_rod", 100, Modifier.STRENGTH, 1, 2, 0);
        modifier(entries, "witchery:charged_attuned_stone", 150, Modifier.STRENGTH, 1, 3, 0);
        modifier(entries, "redstone", 50, Modifier.DURATION, 1, 1, 0);
        modifier(entries, "obsidian", 100, Modifier.DURATION, 1, 2, 0);
        modifier(entries, "witchery:seedsmindrake", 150, Modifier.DURATION, 1, 3, 0);
        modifier(entries, "fermented_spider_eye", 25, Modifier.INVERT, 0, 0, 0);
        modifier(entries, "gold_nugget", 50, Modifier.HIDE_PARTICLES, 0, 0, 0);
        modifier(entries, "witchery:rowan_berries", 50, Modifier.DRINK_SPEED, -8, 0, 0);
        modifier(entries, "witchery:exhale_of_the_horned_one", 0, Modifier.DRINK_SPEED, -4, 0, 0);
        modifier(entries, "witchery:spanishmoss", 50, Modifier.DRINK_SPEED, -4, 0, 0);
        modifier(entries, "gunpowder", 0, Modifier.SPLASH, 0, 0, 0);
        modifier(entries, "witchery:globe_artichoke", 0, Modifier.SPLASH, 0, 0, 0);
        modifier(entries, "witchery:wood_ash", 50, Modifier.EXTENT, 1, 1, 0);
        modifier(entries, "cocoa_beans", 100, Modifier.EXTENT, 1, 2, 0);
        modifier(entries, "witchery:somniancotton", 150, Modifier.EXTENT, 1, 3, 0);

        potion(entries, "witchery:wolfsbane_flower", 0, 1, WitcheryEffects.WOLFSBANE, 1200);
        potion(entries, "witchery:ender_dew", 200, 1, WitcheryEffects.ENDER_INHIBITION, 1800);
        potion(entries, "spider_eye", 0, 2, () -> MobEffects.POISON, 900);
        potion(entries, "sugar", 100, 2, 10,
                new Potion(() -> MobEffects.MOVEMENT_SPEED, 3600), new Potion(() -> MobEffects.MOVEMENT_SLOWDOWN, 1800));
        potion(entries, "pufferfish", 100, 2, () -> MobEffects.WATER_BREATHING, 3600);
        potion(entries, "magma_cream", 100, 2, () -> MobEffects.FIRE_RESISTANCE, 3600);
        potion(entries, "golden_carrot", 200, 2, 10,
                new Potion(() -> MobEffects.NIGHT_VISION, 3600), new Potion(() -> MobEffects.INVISIBILITY, 3600));
        potion(entries, "ghast_tear", 200, 2, 10,
                new Potion(() -> MobEffects.REGENERATION, 900), new Potion(() -> MobEffects.POISON, 900));
        potion(entries, "blaze_powder", 200, 2, 10,
                new Potion(() -> MobEffects.DAMAGE_BOOST, 3600), new Potion(() -> MobEffects.WEAKNESS, 1800));
        potion(entries, "glistering_melon_slice", 200, 2, 1,
                new Potion(() -> MobEffects.HEAL, 0), new Potion(() -> MobEffects.HARM, 0));
        potion(entries, "leather", 200, 2, () -> MobEffects.JUMP, 3600);
        potion(entries, "wither_skeleton_skull", 200, 4, () -> MobEffects.WITHER, 300);
        potion(entries, "ink_sac", 1000, 4, () -> MobEffects.BLINDNESS, 300);
        potion(entries, "witchery:infernal_blood", 500, 2, WitcheryEffects.GROTESQUE, 3600);
        potion(entries, "witchery:demon_heart", 750, 4, WitcheryEffects.PARALYSIS, 200);
        return Map.copyOf(entries);
    }

    private static void modifier(Map<ResourceLocation, Ingredient> entries, String id, int power, Modifier modifier,
                                 int amount, int ceiling, int yieldModifier) {
        entries.put(ResourceLocation.parse(id), new Ingredient(power, modifier, amount, ceiling, yieldModifier, null, null));
    }

    private static void potion(Map<ResourceLocation, Ingredient> entries, String id, int power, int levels,
                               Supplier<MobEffect> effect, int duration) {
        Potion potion = new Potion(effect, duration);
        potion(entries, id, power, levels, 10, potion, potion);
    }

    private static void potion(Map<ResourceLocation, Ingredient> entries, String id, int power, int levels,
                               int strengthCeiling, Potion normal, Potion inverted) {
        entries.put(ResourceLocation.parse(id), new Ingredient(power, null, levels, strengthCeiling, 0, normal, inverted));
    }
}
