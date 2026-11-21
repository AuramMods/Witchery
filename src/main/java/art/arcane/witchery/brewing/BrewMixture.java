package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.CreatureAlignment;
import art.arcane.witchery.entity.Leonard;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
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

    public enum Delivery {
        DRINK, SPLASH, GAS, LIQUID, TRIGGERED
    }

    private enum Modifier {
        CAPACITY, STRENGTH, DURATION, INVERT, HIDE_PARTICLES, DRINK_SPEED, DELIVERY, EXTENT, LIFETIME, WAYSTONE, RITUAL
    }

    private enum Action {
        EXTINGUISH, COLD, ICE_SHELL, HARM_DEMONS, REVEAL, MOVE_ANIMALS;

        String nameKey(boolean inverted) {
            return switch (this) {
                case EXTINGUISH -> "brew.witchery.extinguish";
                case COLD -> "brew.witchery.cold";
                case ICE_SHELL -> "brew.witchery.ice_shell";
                case HARM_DEMONS -> "brew.witchery.harm_demons";
                case REVEAL -> "brew.witchery.revealing";
                case MOVE_ANIMALS -> inverted ? "brew.witchery.animal_repulsion" : "brew.witchery.animal_attraction";
            };
        }
    }

    private sealed interface BrewEffect permits Application, ActionApplication {}

    private record ActionApplication(Action action, int strength, boolean inverted, int durationMultiplier,
                                     boolean showParticles) implements BrewEffect {
        String nameKey() { return action.nameKey(inverted); }
    }

    private record Ingredient(int power, @Nullable Modifier modifier, int amount, int ceiling,
                              int yieldModifier, @Nullable Potion normal, @Nullable Potion inverted, @Nullable Action action) {
        boolean isEffect() { return normal != null || action != null; }
    }

    private record Potion(Supplier<MobEffect> effect, int duration) {}

    public record Application(MobEffect effect, int ticks, int amplifier, boolean showParticles) implements BrewEffect {
        public MobEffectInstance instance(int duration) {
            return new MobEffectInstance(effect, duration, amplifier, false, showParticles, true);
        }
    }

    private final List<BrewEffect> effects;
    private final int capacity;
    private final int used;
    private final int power;
    private final int drinkTicks;
    private final int yieldModifier;
    private final int color;
    private final Delivery delivery;
    private final int extent;
    private final int lifetime;
    @Nullable
    private final WaystoneItem.Location destination;

    private BrewMixture(Mixing mixing) {
        effects = List.copyOf(mixing.effects);
        capacity = mixing.capacity;
        used = mixing.used;
        power = mixing.power;
        drinkTicks = Math.max(2, mixing.drinkTicks);
        yieldModifier = mixing.yieldModifier;
        color = mixing.color & 0xFFFFFF;
        delivery = mixing.delivery;
        extent = mixing.extent;
        lifetime = mixing.lifetime;
        destination = mixing.destination;
    }

    public static boolean canAdd(List<ItemStack> current, ItemStack offered) {
        Mixing mixing = mix(current);
        ResourceLocation id = id(offered);
        Ingredient ingredient = INGREDIENTS.get(id);
        return mixing != null && ingredient != null && ingredient.modifier != Modifier.RITUAL
                && (ingredient.modifier != Modifier.WAYSTONE || WaystoneItem.location(offered) != null) && !mixing.segment.contains(id)
                && (!ingredient.isEffect() || mixing.used + ingredient.amount <= mixing.capacity);
    }

    public static void add(List<ItemStack> current, ItemStack offered) {
        Ingredient ingredient = INGREDIENTS.get(id(offered));
        if (ingredient != null && ingredient.modifier == Modifier.DELIVERY) {
            current.removeIf(stack -> {
                Ingredient previous = INGREDIENTS.get(id(stack));
                return previous != null && previous.modifier == Modifier.DELIVERY;
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
            stacks.add(loadIngredient((CompoundTag) entry));
        }
        BrewMixture mixture = from(stacks);
        return mixture != null && (!tag.getBoolean("Splash") || mixture.splash()) ? mixture : null;
    }

    public static ItemStack loadIngredient(CompoundTag tag) {
        if ("minecraft:skull".equals(tag.getString("id"))) {
            String id = switch (tag.getInt("Damage")) {
                case 1 -> "minecraft:wither_skeleton_skull";
                case 2 -> "minecraft:zombie_head";
                default -> null;
            };
            if (id != null) {
                CompoundTag migrated = tag.copy();
                migrated.putString("id", id);
                migrated.remove("Damage");
                return WitcheryIngredient.loadInventoryStack(migrated);
            }
        }
        return WitcheryIngredient.loadInventoryStack(tag);
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
        tag.putBoolean("Splash", splash());
        return tag;
    }

    public List<Application> effects() {
        return effects.stream().filter(Application.class::isInstance).map(Application.class::cast).toList();
    }

    public List<String> actionNames() {
        return effects.stream().filter(ActionApplication.class::isInstance).map(ActionApplication.class::cast)
                .map(ActionApplication::nameKey).distinct().toList();
    }

    public record GuideIngredient(ItemStack stack, int power, int capacityCost, Component effect, @Nullable Component reversed) {}

    public static List<GuideIngredient> guideIngredients() {
        return INGREDIENTS.entrySet().stream().sorted(Map.Entry.comparingByKey()).flatMap(entry ->
                BuiltInRegistries.ITEM.getOptional(entry.getKey()).stream().map(item -> {
                    Ingredient ingredient = entry.getValue();
                    Component effect;
                    Component reversed = null;
                    if (ingredient.normal != null) {
                        effect = ingredient.normal.effect.get().getDisplayName();
                        if (!ingredient.normal.equals(ingredient.inverted)) {
                            reversed = ingredient.inverted.effect.get().getDisplayName();
                        }
                    } else if (ingredient.action != null) {
                        effect = Component.translatable(ingredient.action.nameKey(false));
                        if (!ingredient.action.nameKey(false).equals(ingredient.action.nameKey(true))) {
                            reversed = Component.translatable(ingredient.action.nameKey(true));
                        }
                    } else {
                        effect = Component.translatable("guide.witchery.cauldron.modifier."
                                + ingredient.modifier.name().toLowerCase(java.util.Locale.ROOT), ingredient.amount, ingredient.ceiling);
                    }
                    return new GuideIngredient(new ItemStack(item), ingredient.power, ingredient.isEffect() ? ingredient.amount : 0, effect, reversed);
                })).toList();
    }
    public int power() { return power; }
    public int used() { return used; }
    public int capacity() { return capacity; }
    public int drinkTicks() { return drinkTicks; }
    public int yieldModifier() { return yieldModifier; }
    public int color() { return color; }
    public boolean splash() { return delivery != Delivery.DRINK; }
    public Delivery delivery() { return delivery; }
    public int extent() { return extent; }
    public int lifetime() { return lifetime; }
    public int splashRadius() { return 3 + extent; }
    public boolean hasEffects() { return !effects.isEmpty(); }
    @Nullable
    public WaystoneItem.Location destination() { return destination; }

    public void applyToBlock(ServerLevel level, BlockPos pos, Direction face, int radius, boolean ritual, @Nullable Player caster) {
        for (BrewEffect effect : effects) {
            if (!(effect instanceof ActionApplication application)) continue;
            switch (application.action) {
                case EXTINGUISH -> BrewTerrainEffects.extinguish(level, pos, radius, ritual, application.strength, caster);
                case COLD -> BrewColdEffects.freezeWater(level, pos, application.strength, caster);
                case ICE_SHELL -> BrewColdEffects.shellOnImpact(level, pos, face, application.strength, caster);
                default -> { }
            }
        }
    }

    public void apply(LivingEntity target, @Nullable Entity source, @Nullable LivingEntity caster,
                      double strengthScale, double durationScale) {
        apply(target, source, caster, strengthScale, durationScale, false);
    }

    public void apply(LivingEntity target, @Nullable Entity source, @Nullable LivingEntity caster,
                      double strengthScale, double durationScale, boolean blocksHarmfulEffects) {
        apply(target, source, caster, strengthScale, durationScale, blocksHarmfulEffects, false);
    }

    public void applySplash(LivingEntity target, Entity source, @Nullable LivingEntity caster, double scale, boolean glancing) {
        apply(target, source, caster, scale, scale * 0.5, false, glancing);
    }

    private void apply(LivingEntity target, @Nullable Entity source, @Nullable LivingEntity caster,
                       double strengthScale, double durationScale, boolean blocksHarmfulEffects, boolean glancing) {
        if (target.isDeadOrDying() || durationScale <= 0) return;
        for (BrewEffect effect : effects) {
            if (effect instanceof Application application) {
                if (blocksHarmfulEffects && application.effect.getCategory() == MobEffectCategory.HARMFUL) continue;
                if (application.effect.isInstantenous()) {
                    application.effect.applyInstantenousEffect(source, caster, target, application.amplifier, strengthScale);
                } else {
                    target.addEffect(application.instance(Mth.ceil(application.ticks * durationScale)), source);
                }
            } else if (effect instanceof ActionApplication application) {
                applyAction(application, target, source, caster, strengthScale, durationScale, blocksHarmfulEffects, glancing);
            }
        }
    }

    private static void applyAction(ActionApplication application, LivingEntity target, @Nullable Entity source,
                                    @Nullable LivingEntity caster, double strengthScale, double durationScale,
                                    boolean blocksHarmfulEffects, boolean glancing) {
        int strength = application.strength;
        switch (application.action) {
            case EXTINGUISH -> {
                if (strength <= 1 && target.level().dimensionType().ultraWarm()) return;
                if (strength > 0 && target instanceof Blaze) {
                    target.hurt(caster instanceof Player player ? target.damageSources().playerAttack(player)
                            : target.damageSources().magic(), (strength + 1) * 4.0F);
                }
                target.clearFire();
            }
            case COLD, ICE_SHELL -> {
                if (!(target.level() instanceof ServerLevel level)) return;
                Player player = caster instanceof Player actor ? actor : null;
                boolean shell = application.action == Action.ICE_SHELL;
                if (shell && target.getType().is(BrewColdEffects.SHELL_RESISTANT)) {
                    BrewColdEffects.wetResistantTarget(level, target.blockPosition(), player);
                    return;
                }
                int ticks = Mth.ceil((shell ? 200 : 3600) * application.durationMultiplier * durationScale);
                target.addEffect(new MobEffectInstance(WitcheryEffects.CHILLED.get(), ticks, Math.min(strength, 10),
                        false, application.showParticles, true), source);
                if (shell && !glancing) BrewColdEffects.shell(level, target.blockPosition(), strength, player);
            }
            case HARM_DEMONS -> {
                if (blocksHarmfulEffects) return;
                if (target instanceof Leonard leonard) {
                    leonard.harmedByIce(caster, Mth.ceil((10 << strength) * strengthScale));
                } else {
                    int damage = (CreatureAlignment.demonic(target) ? 10 : 3) << strength;
                    target.hurt(target.damageSources().indirectMagic(source, caster), Mth.ceil(damage * strengthScale));
                }
            }
            case REVEAL -> BrewCreatureEffects.reveal(target);
            case MOVE_ANIMALS -> BrewCreatureEffects.moveAnimals(target, strength, application.inverted);
        }
    }

    private static ResourceLocation id(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    static int ingredientPower(ItemStack stack) {
        ResourceLocation id = id(stack);
        Ingredient ingredient = INGREDIENTS.get(id);
        if (ingredient != null) return ingredient.power;
        return switch (id.toString()) {
            case "minecraft:ender_pearl" -> 1000;
            case "minecraft:dandelion" -> 200;
            case "minecraft:vine" -> 150;
            case "witchery:ent_branch" -> 350;
            case "witchery:refined_evil" -> 3000;
            default -> 0;
        };
    }

    @Nullable
    private static Mixing mix(List<ItemStack> stacks) {
        Mixing mixing = new Mixing();
        for (ItemStack stack : stacks) {
            ResourceLocation id = id(stack);
            Ingredient ingredient = INGREDIENTS.get(id);
            if (ingredient == null || stack.isEmpty()) return null;
            mixing.add(stack, id, ingredient);
        }
        return mixing;
    }

    private static final class Mixing {
        private final List<BrewEffect> effects = new ArrayList<>();
        private final Set<ResourceLocation> segment = new HashSet<>();
        private int capacity;
        private int used;
        private int power;
        private int drinkTicks = 32;
        private int yieldModifier;
        private int color = 17;
        private Delivery delivery = Delivery.DRINK;
        private int extent;
        private int lifetime;
        private int strength;
        private int duration;
        private int strengthBoosts;
        private int durationBoosts;
        private boolean inverted;
        private boolean hideParticles;
        private boolean unlimitedBoosts;
        @Nullable
        private WaystoneItem.Location destination;

        void add(ItemStack stack, ResourceLocation id, Ingredient ingredient) {
            power += ingredient.power;
            yieldModifier += ingredient.yieldModifier;
            color = color * 37 + id.hashCode();
            if (ingredient.isEffect()) {
                applyEffect(ingredient);
                segment.clear();
            } else {
                applyModifier(ingredient);
                if (ingredient.modifier == Modifier.WAYSTONE && destination == null) destination = WaystoneItem.location(stack);
            }
            segment.add(id);
        }

        private void applyEffect(Ingredient ingredient) {
            if (used + ingredient.amount > capacity) return;
            used += ingredient.amount;
            if (ingredient.action != null) {
                int actionStrength = ingredient.action == Action.HARM_DEMONS
                        ? Math.min(strength, unlimitedBoosts ? 3 : 1) : strength;
                effects.add(new ActionApplication(ingredient.action, actionStrength, inverted, duration + 1, !hideParticles));
            } else {
                Potion effect = inverted ? ingredient.inverted : ingredient.normal;
                effects.add(new Application(effect.effect.get(), effect.duration * (duration + 1),
                        Math.min(strength, ingredient.ceiling), !hideParticles));
            }
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
                case DELIVERY -> delivery = Delivery.values()[ingredient.amount];
                case EXTENT -> {
                    if (extent < ingredient.ceiling) extent++;
                }
                case LIFETIME -> {
                    if (lifetime < ingredient.ceiling) lifetime++;
                }
                case WAYSTONE, RITUAL -> { }
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
        modifier(entries, "witchery:koboldite_pentacle", 1000, Modifier.CAPACITY, 6, 16, 0);
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
        modifier(entries, "gunpowder", 0, Modifier.DELIVERY, Delivery.SPLASH.ordinal(), 0, 0);
        modifier(entries, "witchery:globe_artichoke", 0, Modifier.DELIVERY, Delivery.SPLASH.ordinal(), 0, 0);
        modifier(entries, "witchery:bat_wool", 0, Modifier.DELIVERY, Delivery.GAS.ordinal(), 0, 0);
        modifier(entries, "witchery:wormwood_leaf", 0, Modifier.DELIVERY, Delivery.LIQUID.ordinal(), 0, 0);
        modifier(entries, "zombie_head", 0, Modifier.DELIVERY, Delivery.TRIGGERED.ordinal(), 0, 0);
        modifier(entries, "witchery:wood_ash", 50, Modifier.EXTENT, 1, 1, 0);
        modifier(entries, "cocoa_beans", 100, Modifier.EXTENT, 1, 2, 0);
        modifier(entries, "witchery:somniancotton", 150, Modifier.EXTENT, 1, 3, 0);
        modifier(entries, "witchery:belladonna_flower", 50, Modifier.LIFETIME, 1, 1, 0);
        modifier(entries, "lapis_lazuli", 100, Modifier.LIFETIME, 1, 2, 0);
        modifier(entries, "end_stone", 150, Modifier.LIFETIME, 1, 3, 0);
        modifier(entries, "witchery:bound_waystone", 100, Modifier.WAYSTONE, 0, 0, 0);
        modifier(entries, "witchery:dog_tongue", 250, Modifier.RITUAL, 0, 0, 0);
        modifier(entries, "witchery:taglockkit", 1000, Modifier.RITUAL, 0, 0, 0);
        entries.put(ResourceLocation.withDefaultNamespace("coal"), new Ingredient(0, null, 1, 10, 0, null, null, Action.EXTINGUISH));

        entries.put(ResourceLocation.parse("witchery:icy_needle"), new Ingredient(200, null, 2, 10, 0, null, null, Action.COLD));
        entries.put(ResourceLocation.parse("witchery:frozen_heart"), new Ingredient(500, null, 5, 10, 0, null, null, Action.ICE_SHELL));
        entries.put(ResourceLocation.withDefaultNamespace("ice"), new Ingredient(500, null, 5, 3, 0, null, null, Action.HARM_DEMONS));
        entries.put(ResourceLocation.fromNamespaceAndPath("witchery", "odour_of_purity"),
                new Ingredient(100, null, 4, 0, 0, null, null, Action.REVEAL));
        entries.put(ResourceLocation.fromNamespaceAndPath("witchery", "heart_of_gold"),
                new Ingredient(500, null, 4, 0, 0, null, null, Action.MOVE_ANIMALS));

        potion(entries, "witchery:wolfsbane_flower", 0, 1, WitcheryEffects.WOLFSBANE, 1200);
        potion(entries, "witchery:ender_dew", 200, 1, WitcheryEffects.ENDER_INHIBITION, 1800);
        potion(entries, "gravel", 100, 2, WitcheryEffects.GAS_MASK, 1800);
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
        entries.put(ResourceLocation.parse(id), new Ingredient(power, modifier, amount, ceiling, yieldModifier, null, null, null));
    }

    private static void potion(Map<ResourceLocation, Ingredient> entries, String id, int power, int levels,
                               Supplier<MobEffect> effect, int duration) {
        Potion potion = new Potion(effect, duration);
        potion(entries, id, power, levels, 10, potion, potion);
    }

    private static void potion(Map<ResourceLocation, Ingredient> entries, String id, int power, int levels,
                               int strengthCeiling, Potion normal, Potion inverted) {
        entries.put(ResourceLocation.parse(id), new Ingredient(power, null, levels, strengthCeiling, 0, normal, inverted, null));
    }
}
