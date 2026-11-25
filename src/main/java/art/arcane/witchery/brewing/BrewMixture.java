package art.arcane.witchery.brewing;

import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.WitchSaplingBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.entity.CreatureAlignment;
import art.arcane.witchery.entity.InfernoSplatter;
import art.arcane.witchery.entity.Leonard;
import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.BiomeNoteItem;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
    private static final int TAGLOCK_DURATION_MULTIPLIER = 10_001;
    private static final ResourceLocation BRAMBLE = ResourceLocation.parse("witchery:bramble");
    private static final ResourceLocation ENDER_BRAMBLE = ResourceLocation.parse("witchery:bramble_ender");
    private static final Map<ResourceLocation, Ingredient> INGREDIENTS = ingredients();

    public enum Delivery {
        DRINK, SPLASH, GAS, LIQUID, TRIGGERED
    }

    private enum Modifier {
        CAPACITY, STRENGTH, DURATION, INVERT, AMBIENT, DRINK_SPEED, DELIVERY, EXTENT, LIFETIME, WAYSTONE, RITUAL,
        NO_BLOCK_TARGET, NO_ENTITY_TARGET, RAISE_EFFECT_CEILING, SET_COLOR
    }

    private enum Action {
        EXTINGUISH, COLD, ICE_SHELL, HARM_DEMONS, HARM_UNDEAD, HARM_INSECTS, HARM_WEREWOLVES,
        WEAKEN_VAMPIRES, REVEAL, MOVE_ANIMALS, CHANGING_SEASONS, ICE_WORLD, SNOW,
        REMOVE_DEBUFFS, REMOVE_BUFFS, WASTING, DRAIN_MAGIC, DURATION_BOOST, STEAL_BUFFS, SPREAD_DEBUFFS,
        AIR_HIKE, FROG_TONGUE, KNOCKBACK, BAT_BURST, BODEGA, POISON_TOAD, RAISING,
        FLOWERS, FERTILIZATION, HARVESTING, TILLING, PLANTING, PRUNING, FELLING, PULVERISATION,
        DISSIPATE, TIDE_HOLD, LAVA_HOLD, LILIFY, EROSION, LEVELLING, WEBS, VINES, THORNS, SPROUTING, BLIGHT, RAISE_LAND,
        TRANSPOSE, TRANSPOSE_ORE, INFERNO, HELL_GATE, BLAST,
        TREE_OAK(BrewTrees.Species.OAK), TREE_SPRUCE(BrewTrees.Species.SPRUCE), TREE_BIRCH(BrewTrees.Species.BIRCH),
        TREE_JUNGLE(BrewTrees.Species.JUNGLE), TREE_ACACIA(BrewTrees.Species.ACACIA), TREE_DARK_OAK(BrewTrees.Species.DARK_OAK),
        TREE_ROWAN(BrewTrees.Species.ROWAN), TREE_ALDER(BrewTrees.Species.ALDER), TREE_HAWTHORN(BrewTrees.Species.HAWTHORN);

        @Nullable private final BrewTrees.Species tree;

        Action() { this(null); }

        Action(@Nullable BrewTrees.Species tree) { this.tree = tree; }

        String nameKey(boolean inverted) {
            return switch (this) {
                case EXTINGUISH -> "brew.witchery.extinguish";
                case COLD -> "brew.witchery.cold";
                case ICE_SHELL -> "brew.witchery.ice_shell";
                case ICE_WORLD -> "brew.witchery.ice_world";
                case HARM_DEMONS -> "brew.witchery.harm_demons";
                case HARM_UNDEAD -> "brew.witchery.harm_undead";
                case HARM_INSECTS -> "brew.witchery.harm_insects";
                case HARM_WEREWOLVES -> "brew.witchery.harm_werewolves";
                case WEAKEN_VAMPIRES -> "brew.witchery.weaken_vampires";
                case REVEAL -> "brew.witchery.revealing";
                case MOVE_ANIMALS -> inverted ? "brew.witchery.animal_repulsion" : "brew.witchery.animal_attraction";
                case CHANGING_SEASONS -> "brew.witchery.changing_seasons";
                case SNOW -> "brew.witchery.snow";
                case REMOVE_DEBUFFS -> "brew.witchery.remove_debuffs";
                case REMOVE_BUFFS -> "brew.witchery.remove_buffs";
                case WASTING -> inverted ? "brew.witchery.fullness" : "brew.witchery.wasting";
                case DRAIN_MAGIC -> "brew.witchery.drain_magic";
                case DURATION_BOOST -> "brew.witchery.duration_boost";
                case STEAL_BUFFS -> "brew.witchery.steal_buffs";
                case SPREAD_DEBUFFS -> "brew.witchery.spread_debuffs";
                case AIR_HIKE -> "brew.witchery.air_hike";
                case FROG_TONGUE -> "brew.witchery.frog_tongue";
                case KNOCKBACK -> "brew.witchery.knockback";
                case BAT_BURST -> "brew.witchery.bat_burst";
                case BODEGA -> "brew.witchery.bodega";
                case POISON_TOAD -> "brew.witchery.poison_toad";
                case RAISING -> "brew.witchery.raising";
                case FLOWERS -> "brew.witchery.flowers";
                case FERTILIZATION -> "brew.witchery.fertilization";
                case HARVESTING -> "brew.witchery.harvesting";
                case TILLING -> "brew.witchery.tilling";
                case PLANTING -> "brew.witchery.planting";
                case PRUNING -> "brew.witchery.pruning";
                case FELLING -> "brew.witchery.felling";
                case PULVERISATION -> "brew.witchery.pulverisation";
                case DISSIPATE -> "brew.witchery.dissipate";
                case TIDE_HOLD -> "brew.witchery.tide_hold";
                case LAVA_HOLD -> "brew.witchery.lava_hold";
                case LILIFY -> "brew.witchery.lilify";
                case EROSION -> "brew.witchery.erosion";
                case LEVELLING -> "brew.witchery.levelling";
                case WEBS -> "brew.witchery.webs";
                case VINES -> "brew.witchery.vines";
                case THORNS -> "brew.witchery.thorns";
                case SPROUTING -> "brew.witchery.sprouting";
                case BLIGHT -> "brew.witchery.blight";
                case RAISE_LAND -> "brew.witchery.raiseland";
                case TRANSPOSE -> "brew.witchery.transpose";
                case TRANSPOSE_ORE -> "brew.witchery.transposeore";
                case INFERNO -> "brew.witchery.inferno";
                case HELL_GATE -> "brew.witchery.hellgate";
                case BLAST -> "brew.witchery.blast";
                case TREE_OAK, TREE_SPRUCE, TREE_BIRCH, TREE_JUNGLE, TREE_ACACIA, TREE_DARK_OAK,
                     TREE_ROWAN, TREE_ALDER, TREE_HAWTHORN -> tree.nameKey;
            };
        }

        boolean affectsTerrain() {
            return switch (this) {
                case EXTINGUISH, COLD, ICE_SHELL, ICE_WORLD, CHANGING_SEASONS, SNOW,
                     BAT_BURST, FROG_TONGUE, POISON_TOAD, RAISING,
                     FLOWERS, FERTILIZATION, HARVESTING, TILLING, PLANTING, PRUNING, FELLING, PULVERISATION,
                     DISSIPATE, TIDE_HOLD, LAVA_HOLD, LILIFY, EROSION, LEVELLING, WEBS, VINES, THORNS, SPROUTING,
                     BLIGHT, RAISE_LAND, TRANSPOSE_ORE, INFERNO, BLAST -> true;
                default -> tree != null;
            };
        }

        int displayedDuration() {
            return switch (this) {
                case SNOW, COLD -> 3600;
                case VINES, THORNS -> 1800;
                case SPROUTING -> 300;
                default -> 0;
            };
        }
    }

    private sealed interface BrewEffect permits Application, ActionApplication {
        boolean affectsEntities();
    }

    private record ActionApplication(Action action, int strength, int blockStrength, boolean inverted, int durationMultiplier,
                                     int blockDurationMultiplier, boolean ambient, boolean affectsEntities, boolean affectsBlocks,
                                     @Nullable ResourceLocation biome) implements BrewEffect {}

    private record Ingredient(int power, @Nullable Modifier modifier, int amount, int ceiling,
                              int yieldModifier, @Nullable Potion normal, @Nullable Potion inverted, @Nullable Action action) {
        boolean isEffect() { return normal != null || action != null; }

        boolean retainsTaglockModifiers() {
            return normal != null && (normal.curse || inverted.curse || normal.dye != null);
        }
    }

    private record Potion(Supplier<MobEffect> effect, int duration, boolean curse, @Nullable BrewDye dye) {
        Potion(Supplier<MobEffect> effect, int duration) {
            this(effect, duration, false);
        }

        Potion(Supplier<MobEffect> effect, int duration, boolean curse) {
            this(effect, duration, curse, null);
        }

        Component brewName() {
            return dye == null ? BrewMixture.brewName(effect.get()) : dye.label();
        }
    }

    public record Application(MobEffect effect, int ticks, int amplifier, boolean ambient,
                              boolean inverted, int taglockTicks, boolean affectsEntities) implements BrewEffect {
        public MobEffectInstance instance(int duration) {
            return instance(duration, amplifier, ambient);
        }

        private MobEffectInstance instance(int duration, int strength, boolean ambient) {
            return new MobEffectInstance(effect, duration, strength, ambient, true);
        }
    }

    private static Component brewName(MobEffect effect) {
        String name = switch (effect.getDescriptionId()) {
            case "effect.minecraft.water_breathing" -> "gills";
            case "effect.minecraft.fire_resistance" -> "fire_resist";
            case "effect.minecraft.night_vision" -> "owl_eye";
            case "effect.minecraft.instant_health" -> "healing";
            case "effect.minecraft.instant_damage" -> "harming";
            case "effect.minecraft.jump_boost" -> "jumping";
            case "effect.minecraft.blindness" -> "ink";
            case "effect.witchery.potion.grotesque" -> "fear";
            case "effect.witchery.potion.keepinventory" -> "sticky_items";
            case "effect.witchery.potion.keepeffects" -> "sticky_effects";
            case "effect.witchery.potion.illfitting" -> "ill_fitting";
            case "effect.witchery.potion.repellattacker" -> "repel";
            case "effect.witchery.potion.feelnopain" -> "moonshine";
            case "effect.witchery.potion.swimming" -> "swim_boost";
            case "effect.witchery.potion.doublejump" -> "frogs_legs";
            case "effect.witchery.potion.brewingexpertise" -> "decanting";
            case "effect.witchery.potion.allergydark" -> "grues_prey";
            case "effect.witchery.potion.allergysun" -> "undeads_curse";
            case "effect.witchery.potion.diseased" -> "disease";
            case "effect.witchery.potion.fortune" -> "luck";
            default -> null;
        };
        return name == null ? effect.getDisplayName() : Component.translatable("brew.witchery." + name);
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
    private final List<WaystoneItem.Location> destinations;

    private BrewMixture(Mixing mixing, int storedColor) {
        effects = List.copyOf(mixing.effects);
        capacity = mixing.capacity;
        used = mixing.used;
        power = mixing.power;
        drinkTicks = Math.max(2, mixing.drinkTicks);
        yieldModifier = mixing.yieldModifier;
        color = storedColor;
        delivery = mixing.delivery;
        extent = mixing.extent;
        lifetime = mixing.lifetime;
        destinations = List.copyOf(mixing.destinations);
    }

    public static boolean canAdd(List<ItemStack> current, ItemStack offered) {
        Mixing mixing = mix(current, false, false);
        ResourceLocation id = id(offered);
        Ingredient ingredient = INGREDIENTS.get(id);
        return mixing != null && ingredient != null && ingredient.modifier != Modifier.RITUAL
                && (ingredient.modifier != Modifier.WAYSTONE || WaystoneItem.location(offered) != null)
                && (ingredient.action != Action.CHANGING_SEASONS || BiomeNoteItem.biomeId(offered) != null)
                && !mixing.segment.contains(id)
                && (!ingredient.isEffect() || mixing.used + ingredient.amount <= mixing.capacity);
    }

    public static void add(List<ItemStack> current, ItemStack offered) {
        Ingredient ingredient = INGREDIENTS.get(id(offered));
        if (ingredient != null && ingredient.modifier == Modifier.DELIVERY) {
            current.removeIf(stack -> {
                // The original dispersals replace any earlier Wheat (Moonshine) action as well.
                if (stack.is(Items.WHEAT)) return true;
                Ingredient previous = INGREDIENTS.get(id(stack));
                return previous != null && previous.modifier == Modifier.DELIVERY;
            });
        }
        current.add(offered.copy());
    }

    @Nullable
    public static BrewMixture from(List<ItemStack> stacks) {
        return from(stacks, null);
    }

    @Nullable
    public static BrewMixture from(List<ItemStack> stacks, @Nullable Integer savedColor) {
        return from(stacks, savedColor, false);
    }

    @Nullable
    private static BrewMixture from(List<ItemStack> stacks, @Nullable Integer savedColor, boolean taglock) {
        if (stacks.isEmpty() && savedColor == null) return null;
        Mixing mixing = mix(stacks, false, taglock);
        return mixing != null ? new BrewMixture(mixing, savedColor != null ? savedColor : BrewColor.fromIngredients(stacks)) : null;
    }

    @Nullable
    public static BrewMixture from(@Nullable CompoundTag tag) {
        return from(tag, false);
    }

    @Nullable
    static BrewMixture forTaglock(CompoundTag tag) {
        return from(tag, true);
    }

    @Nullable
    private static BrewMixture from(@Nullable CompoundTag tag, boolean taglock) {
        if (tag == null || !tag.contains(ITEMS, Tag.TAG_LIST)) return null;
        List<ItemStack> stacks = loadIngredients(tag);
        Integer color = BrewColor.saved(tag);
        BrewMixture mixture = isFixedRitual(tag) ? fixedMixture(stacks, color) : from(stacks, color, taglock);
        return mixture != null && (!tag.getBoolean("Splash") || mixture.splash()) ? mixture : null;
    }

    private static List<ItemStack> loadIngredients(CompoundTag tag) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Tag entry : tag.getList(ITEMS, Tag.TAG_COMPOUND)) stacks.add(loadIngredient((CompoundTag) entry));
        return stacks;
    }

    private static boolean isFixedRitual(CompoundTag tag) {
        return tag.contains("FixedRitual", Tag.TAG_STRING)
                || tag.getBoolean("RitualTriggered") && tag.contains("BrewName", Tag.TAG_STRING);
    }

    public static ItemStack loadIngredient(CompoundTag tag) {
        int metadata = tag.getInt("Damage");
        String id = switch (tag.getString("id")) {
            case "minecraft:coal" -> metadata == 1 ? "minecraft:charcoal" : null;
            case "minecraft:snow" -> tag.contains("Damage", Tag.TAG_ANY_NUMERIC) ? "minecraft:snow_block" : null;
            case "minecraft:skull" -> switch (metadata) {
                case 0 -> "minecraft:skeleton_skull";
                case 1 -> "minecraft:wither_skeleton_skull";
                case 2 -> "minecraft:zombie_head";
                case 4 -> "minecraft:creeper_head";
                default -> null;
            };
            case "minecraft:fish" -> switch (metadata) {
                case 0 -> "minecraft:cod";
                case 1 -> "minecraft:salmon";
                case 2 -> "minecraft:tropical_fish";
                case 3 -> "minecraft:pufferfish";
                default -> null;
            };
            case "minecraft:web" -> "minecraft:cobweb";
            case "minecraft:reeds" -> "minecraft:sugar_cane";
            case "minecraft:wool" -> "minecraft:" + DyeColor.byId(metadata).getName() + "_wool";
            case "minecraft:dye" -> "minecraft:" + BrewDye.byLegacyId(metadata).item();
            case "minecraft:double_plant" -> metadata == 0 ? "minecraft:sunflower" : null;
            case "minecraft:netherbrick" -> "minecraft:nether_brick";
            case "minecraft:speckled_melon" -> "minecraft:glistering_melon_slice";
            case "minecraft:yellow_flower" -> metadata == 0 ? "minecraft:dandelion" : null;
            case "minecraft:waterlily" -> "minecraft:lily_pad";
            case "minecraft:sapling" -> switch (metadata & 7) {
                case 0 -> "minecraft:oak_sapling";
                case 1 -> "minecraft:spruce_sapling";
                case 2 -> "minecraft:birch_sapling";
                case 3 -> "minecraft:jungle_sapling";
                case 4 -> "minecraft:acacia_sapling";
                case 5 -> "minecraft:dark_oak_sapling";
                default -> null;
            };
            case "minecraft:sand" -> metadata == 1 ? "minecraft:red_sand" : null;
            case "minecraft:dirt" -> switch (metadata) {
                case 1 -> "minecraft:coarse_dirt";
                case 2 -> "minecraft:podzol";
                default -> null;
            };
            case "minecraft:red_flower" -> switch (metadata) {
                case 0 -> "minecraft:poppy";
                case 1 -> "minecraft:blue_orchid";
                default -> null;
            };
            case "minecraft:tallgrass" -> metadata == 0 ? "minecraft:dead_bush" : null;
            case "minecraft:deadbush" -> "minecraft:dead_bush";
            case "minecraft:golden_apple" -> metadata == 1 ? "minecraft:enchanted_golden_apple" : null;
            default -> null;
        };
        if (id == null) return WitcheryIngredient.loadInventoryStack(tag);
        CompoundTag migrated = tag.copy();
        migrated.putString("id", id);
        migrated.remove("Damage");
        return WitcheryIngredient.loadInventoryStack(migrated);
    }

    public static CompoundTag ingredientTag(List<ItemStack> stacks, @Nullable Integer color) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (ItemStack stack : stacks) items.add(stack.save(new CompoundTag()));
        tag.put(ITEMS, items);
        if (color != null) tag.putInt("Color", color);
        return tag;
    }

    public CompoundTag bottleTag(List<ItemStack> stacks) {
        CompoundTag tag = ingredientTag(stacks, color);
        tag.putInt("Power", power);
        tag.putInt("EffectCount", effects.size());
        tag.putInt("UsedCapacity", used);
        tag.putInt("RemainingCapacity", capacity - used);
        tag.putInt("BrewDrinkSpeed", drinkTicks);
        tag.putBoolean("Splash", splash());
        return tag;
    }

    public static CompoundTag fixedRitualBottleTag(List<ItemStack> offerings, @Nullable Integer color, ResourceLocation recipe, int power) {
        CompoundTag tag = fixedMixture(offerings, color).bottleTag(offerings);
        tag.putString("FixedRitual", recipe.toString());
        tag.putBoolean("RitualTriggered", true);
        tag.putInt("Power", power);
        return tag;
    }

    public static BrewMixture fixedMixture(List<ItemStack> offerings, @Nullable Integer color) {
        return new BrewMixture(mix(offerings, true, false), color != null ? color : BrewColor.fromIngredients(offerings));
    }

    public List<Application> effects() {
        return effects.stream().filter(Application.class::isInstance).map(Application.class::cast).toList();
    }

    public record NamedEffect(Component name, int strength, int duration) {}

    public record Description(List<NamedEffect> effects, Delivery delivery, int extent, int lifetime, int drinkTicks) {}

    @Nullable
    public static Description describe(@Nullable CompoundTag tag) {
        if (tag == null || !tag.contains(ITEMS, Tag.TAG_LIST)) return null;
        List<ItemStack> stacks = loadIngredients(tag);
        boolean fixedRitual = isFixedRitual(tag);
        if (!fixedRitual && stacks.isEmpty() && BrewColor.saved(tag) == null) return null;

        var description = new DescriptionBuilder();
        for (ItemStack stack : stacks) {
            var ingredient = validIngredient(stack, id(stack));
            if (ingredient != null) description.add(ingredient);
            else if (!fixedRitual) return null;
        }
        if (tag.getBoolean("Splash") && description.delivery == Delivery.DRINK) return null;
        return new Description(List.copyOf(description.effects), description.delivery, description.dispersalExtent,
                description.dispersalLifetime, Math.max(2, description.drinkTicks));
    }

    public record GuideIngredient(ItemStack stack, int power, int capacityCost, Component effect, @Nullable Component reversed) {}

    public static List<GuideIngredient> guideIngredients() {
        return INGREDIENTS.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {
            Ingredient ingredient = entry.getValue();
            BrewTrees.Species tree = ingredient.action != null ? ingredient.action.tree : null;
            boolean enderBramble = entry.getKey().equals(ENDER_BRAMBLE);
            ItemStack stack = tree != null ? tree.ingredient()
                    : new ItemStack(BuiltInRegistries.ITEM.get(enderBramble ? BRAMBLE : entry.getKey()));
            if (enderBramble) BrambleBlock.setWild(stack, false);
            Component effect;
            Component reversed = null;
            if (ingredient.normal != null) {
                effect = ingredient.normal.brewName();
                if (!ingredient.normal.equals(ingredient.inverted)) {
                    reversed = ingredient.inverted.brewName();
                }
            } else if (ingredient.action != null) {
                effect = Component.translatable(ingredient.action.nameKey(false));
                if (!ingredient.action.nameKey(false).equals(ingredient.action.nameKey(true))) {
                    reversed = Component.translatable(ingredient.action.nameKey(true));
                }
            } else if (ingredient.modifier == Modifier.DELIVERY) {
                var delivery = Delivery.values()[ingredient.amount];
                effect = Component.translatable("guide.witchery.cauldron.delivery."
                        + delivery.name().toLowerCase(java.util.Locale.ROOT));
            } else {
                effect = Component.translatable("guide.witchery.cauldron.modifier."
                        + ingredient.modifier.name().toLowerCase(java.util.Locale.ROOT), ingredient.amount, ingredient.ceiling);
            }
            return new GuideIngredient(stack, ingredient.power, ingredient.isEffect() ? ingredient.amount : 0, effect, reversed);
        }).filter(ingredient -> !ingredient.stack().isEmpty()).toList();
    }
    public int power() { return power; }
    public int used() { return used; }
    public int capacity() { return capacity; }
    public int drinkTicks() { return drinkTicks; }
    public int yieldModifier() { return yieldModifier; }
    public int color() { return color & 0xFFFFFF; }
    public boolean splash() { return delivery != Delivery.DRINK; }
    public Delivery delivery() { return delivery; }
    public Delivery impactDelivery() {
        if (!splash()) return delivery;
        for (BrewEffect effect : effects) {
            if (effect instanceof ActionApplication application && application.action == Action.CHANGING_SEASONS) {
                return Delivery.SPLASH;
            }
        }
        return delivery;
    }
    public int extent() { return extent; }
    public int lifetime() { return lifetime; }
    public int splashRadius() { return 3 + extent; }
    public boolean hasEffects() { return !effects.isEmpty(); }
    public boolean hasTerrainEffects() {
        return effects.stream().anyMatch(effect -> effect instanceof ActionApplication action
                && action.affectsBlocks && action.action.affectsTerrain());
    }
    @Nullable
    public WaystoneItem.Location destination() { return destinations.isEmpty() ? null : destinations.get(0); }

    @Nullable
    WaystoneItem.Location secondDestination() { return destinations.size() < 2 ? null : destinations.get(1); }

    boolean hasTransposition() {
        return hasAction(Action.TRANSPOSE);
    }

    boolean requiresInfernalCircle() {
        return hasAction(Action.HELL_GATE);
    }

    private boolean hasAction(Action wanted) {
        return effects.stream().anyMatch(effect -> effect instanceof ActionApplication action && action.action == wanted);
    }

    public void applyToBlock(ServerLevel level, BlockPos pos, Direction face, int radius, boolean ritual, @Nullable Player caster) {
        applyToBlock(level, pos, face, radius, ritual, caster, Vec3.atLowerCornerOf(pos));
    }

    public void applyToBlock(ServerLevel level, BlockPos pos, Direction face, int radius, boolean ritual,
                             @Nullable Player caster, Vec3 impact) {
        applyToBlock(level, pos, face, radius, ritual, caster, 0, impact, false, true);
    }

    public void applySplashToBlock(ServerLevel level, BlockPos pos, Direction face, Vec3 impact, @Nullable Player caster) {
        applyToBlock(level, pos, face, splashRadius(), false, caster, 0, impact, false, true);
    }

    public void applyLiquidToBlock(ServerLevel level, BlockPos pos, Direction face, Vec3 impact, @Nullable Player caster) {
        applyToBlock(level, pos, face, 1, false, caster, 0, impact, true, false);
    }

    void applyRitualToBlock(ServerLevel level, BlockPos pos, int coven) {
        applyRitualToBlock(level, pos, coven, 1, null);
    }

    boolean applyRitualToBlock(ServerLevel level, BlockPos pos, int coven, int pulse, @Nullable BrewTransposition.Site destination) {
        return applyToBlock(level, pos, Direction.UP, splashRadius(), true, null, coven, Vec3.atLowerCornerOf(pos),
                false, true, pulse, destination);
    }

    void applyGasRitualToBlock(ServerLevel level, BlockPos pos, int coven) {
        applyToBlock(level, pos, Direction.UP, 1, true, null, coven, Vec3.atLowerCornerOf(pos), false, false);
    }

    private void applyToBlock(ServerLevel level, BlockPos pos, Direction face, int radius, boolean ritual,
                              @Nullable Player caster, int coven, Vec3 impact, boolean liquid, boolean fullExposure) {
        applyToBlock(level, pos, face, radius, ritual, caster, coven, impact, liquid, fullExposure, 0, null);
    }

    private boolean applyToBlock(ServerLevel level, BlockPos pos, Direction face, int radius, boolean ritual,
                                 @Nullable Player caster, int coven, Vec3 impact, boolean liquid, boolean fullExposure,
                                 int pulse, @Nullable BrewTransposition.Site destination) {
        boolean ongoing = false;
        for (BrewEffect effect : effects) {
            if (!(effect instanceof ActionApplication application) || !application.affectsBlocks) continue;
            int strength = Math.max(0, application.blockStrength - (liquid ? 1 : 0));
            if (ritual) strength = Math.min(strength, ritualStrengthLimit(coven));
            switch (application.action) {
                case EXTINGUISH -> BrewTerrainEffects.extinguish(level, pos, radius, ritual, strength, caster);
                case COLD -> BrewColdEffects.freezeWater(level, pos, strength, caster);
                case ICE_SHELL -> BrewColdEffects.shellOnImpact(level, pos, face, strength, caster);
                case ICE_WORLD -> IceWorldBrew.freeze(level, pos, radius, caster);
                case CHANGING_SEASONS -> BrewTerrainEffects.changeSeasons(level, pos, application.biome, strength, ritual, caster);
                case SNOW -> SnowTrailEffect.cover(level, pos, 2 + 2 * strength, caster);
                case HARM_WEREWOLVES -> {
                    if (ritual && fullExposure) BrewTerrainEffects.summonRain(level, strength);
                }
                case BAT_BURST -> BrewCreatureEffects.releaseBats(level, pos.relative(face), (fullExposure ? 10 : 1) + strength);
                case FROG_TONGUE -> BrewCreatureEffects.frogTongue(level, impact, radius, caster, strength);
                case POISON_TOAD -> BrewCreatureEffects.poisonToad(level, Vec3.atCenterOf(pos).add(0, 2, 0), strength);
                case RAISING -> BrewImpacts.raiseFromCauldron(level, pos, face, strength, ritual,
                        caster instanceof ServerPlayer player ? player : null);
                case FLOWERS -> BrewFarming.flowers(level, pos, radius, strength, caster);
                case FERTILIZATION -> BrewFarming.fertilize(level, pos, radius, strength, caster);
                case HARVESTING -> BrewFarming.harvest(level, pos, radius, strength, caster);
                case TILLING -> BrewFarming.till(level, pos, radius, strength, caster);
                case PLANTING -> BrewFarming.plant(level, pos, radius, strength, caster);
                case PRUNING -> BrewFarming.prune(level, pos, radius, strength, caster);
                case FELLING -> BrewFarming.fell(level, pos, radius, strength, caster);
                case PULVERISATION -> BrewFarming.pulverise(level, pos, radius, caster);
                case DISSIPATE -> BrewTerrainEffects.dissipate(level, pos, radius, strength, caster);
                case TIDE_HOLD, LAVA_HOLD -> BrewTerrainEffects.holdFluid(level, pos, radius,
                        200 * application.blockDurationMultiplier, application.action == Action.TIDE_HOLD ? Blocks.WATER : Blocks.LAVA, caster);
                case LILIFY -> BrewTerrainEffects.lilify(level, pos, face, strength, caster);
                case EROSION -> BrewErosion.erodeFromCauldron(level, pos, radius, caster);
                case LEVELLING -> BrewLevelling.levelTerrain(level, pos, radius, strength, ritual, caster);
                case WEBS -> BrewPlantEffects.webs(level, pos, face, strength, caster);
                case VINES -> BrewPlantEffects.vines(level, pos, face, strength, caster);
                case THORNS -> BrewPlantEffects.thorns(level, pos, face, strength, caster);
                case SPROUTING -> BrewTerrain.sprout(level, pos, face, 8 + 2 * strength, new AABB(pos), caster);
                case BLIGHT -> BrewLandscape.blight(level, pos, radius, caster);
                case RAISE_LAND -> BrewLandscape.raise(level, pos, strength, ritual, caster);
                case TRANSPOSE -> {
                    if (pulse == 1 && destination != null) {
                        BrewTransposition.moveTerrain(new BrewTransposition.Site(level, pos), destination);
                    }
                }
                case TRANSPOSE_ORE -> {
                    if (pulse > 0) ongoing |= BrewTransposition.orePulse(level, pos, radius, strength, coven, pulse);
                    else BrewTransposition.extractOre(level, pos, radius, radius + application.blockStrength, strength, caster);
                }
                case INFERNO -> InfernoSplatter.burst(level, pos.relative(face), strength, caster);
                case HELL_GATE -> {
                    if (pulse > 0) BrewFireEffects.openHellGate(level, pos);
                }
                case BLAST -> BrewFireEffects.blast(level, Vec3.atCenterOf(pos.relative(face)), strength, caster);
                case TREE_OAK, TREE_SPRUCE, TREE_BIRCH, TREE_JUNGLE, TREE_ACACIA, TREE_DARK_OAK,
                     TREE_ROWAN, TREE_ALDER, TREE_HAWTHORN -> BrewTrees.grow(level, pos, face, application.action.tree,
                        application.action == Action.TREE_JUNGLE ? application.blockStrength : strength, caster);
                default -> { }
            }
        }
        return ongoing;
    }

    public void apply(LivingEntity target, @Nullable Entity source, @Nullable LivingEntity caster,
                      double strengthScale, double durationScale) {
        apply(target, source, caster, strengthScale, durationScale, false);
    }

    public void apply(LivingEntity target, @Nullable Entity source, @Nullable LivingEntity caster,
                      double strengthScale, double durationScale, boolean blocksHarmfulEffects) {
        apply(target, source, caster, strengthScale, durationScale, blocksHarmfulEffects, false, -1, false, null);
    }

    public void applyAt(LivingEntity target, @Nullable Entity source, @Nullable LivingEntity caster,
                        double strengthScale, double durationScale, boolean blocksHarmfulEffects, Vec3 impact) {
        apply(target, source, caster, strengthScale, durationScale, blocksHarmfulEffects, false, -1, false, impact);
    }

    public void applySplash(LivingEntity target, Entity source, @Nullable LivingEntity caster, double scale, boolean glancing) {
        applySplash(target, source, caster, scale, glancing, source.position());
    }

    public void applySplash(LivingEntity target, Entity source, @Nullable LivingEntity caster, double scale,
                            boolean glancing, Vec3 impact) {
        apply(target, source, caster, scale, scale * 0.5, false, glancing, -1, false, impact);
    }

    void applyTaglock(LivingEntity target, ServerPlayer caster, int coven, @Nullable BrewTransposition.Site destination) {
        apply(target, caster, caster, 1, 1, false, false, coven, true, target.position(), destination);
    }

    public void applyRitualExposure(LivingEntity target, @Nullable Entity source, int coven, Vec3 impact) {
        apply(target, source, null, 1, 1, false, false, coven, false, impact);
    }

    private void apply(LivingEntity target, @Nullable Entity source, @Nullable LivingEntity caster,
                       double strengthScale, double durationScale, boolean blocksHarmfulEffects, boolean glancing,
                       int ritualCoven, boolean taglock, @Nullable Vec3 impact) {
        apply(target, source, caster, strengthScale, durationScale, blocksHarmfulEffects, glancing, ritualCoven, taglock, impact, null);
    }

    private void apply(LivingEntity target, @Nullable Entity source, @Nullable LivingEntity caster,
                       double strengthScale, double durationScale, boolean blocksHarmfulEffects, boolean glancing,
                       int ritualCoven, boolean taglock, @Nullable Vec3 impact, @Nullable BrewTransposition.Site destination) {
        if (target.isDeadOrDying() || durationScale <= 0) return;
        int strengthLimit = ritualCoven < 0 ? Integer.MAX_VALUE : ritualStrengthLimit(ritualCoven);
        for (BrewEffect effect : effects) {
            if (!effect.affectsEntities()) continue;
            if (effect instanceof Application application) {
                if (blocksHarmfulEffects && application.effect.getCategory() == MobEffectCategory.HARMFUL) continue;
                if (application.effect == WitcheryEffects.COLORFUL.get()) {
                    target.addEffect(application.instance(application.ticks), source);
                    continue;
                }
                if (application.effect == WitcheryEffects.DOUBLE_JUMP.get() && !application.inverted
                        && !(caster instanceof ServerPlayer player && FamiliarBinding.hasBrewMastery(player))) continue;
                if (application.effect == WitcheryEffects.DISEASED.get() && !application.inverted
                        && CreatureAlignment.immuneToDisease(target)) continue;
                int amplifier = Math.min(application.amplifier, strengthLimit);
                if (application.effect.isInstantenous()) {
                    application.effect.applyInstantenousEffect(source, caster, target, amplifier, strengthScale);
                } else {
                    boolean extended = taglock && application.taglockTicks > 0;
                    int duration = extended ? application.taglockTicks : Mth.ceil(application.ticks * durationScale);
                    target.addEffect(application.instance(duration, amplifier, extended || application.ambient), source);
                }
            } else if (effect instanceof ActionApplication application) {
                int strength = Math.min(application.strength, strengthLimit);
                applyAction(application, target, source, caster, strengthScale, durationScale, glancing, strength,
                        ritualCoven >= 0, taglock, impact, destination);
            }
        }
    }

    private static int ritualStrengthLimit(int coven) {
        return 1 + Math.min(coven, 6) / 2;
    }

    private static void applyAction(ActionApplication application, LivingEntity target, @Nullable Entity source,
                                    @Nullable LivingEntity caster, double strengthScale, double durationScale,
                                    boolean glancing, int strength, boolean ritual, boolean taglock, @Nullable Vec3 impact,
                                    @Nullable BrewTransposition.Site destination) {
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
                        application.ambient, true), source);
                if (shell && !glancing) BrewColdEffects.shell(level, target.blockPosition(), strength, player);
            }
            case HARM_DEMONS, HARM_UNDEAD, HARM_INSECTS, HARM_WEREWOLVES -> {
                if (application.action == Action.HARM_DEMONS && target instanceof Leonard leonard) {
                    leonard.harmedByIce(caster, Mth.ceil((10 << strength) * strengthScale));
                } else {
                    boolean silver = application.action == Action.HARM_WEREWOLVES && CreatureAlignment.werewolf(target);
                    int damage = switch (application.action) {
                        case HARM_DEMONS -> CreatureAlignment.demonic(target) ? 10 : 3;
                        case HARM_UNDEAD -> {
                            boolean undead = target instanceof Player ? CreatureAlignment.undead(target)
                                    : target.isInvertedHealAndHarm();
                            yield undead ? 10 : 3;
                        }
                        case HARM_INSECTS -> target.getMobType() == MobType.ARTHROPOD ? 10 : 3;
                        default -> silver ? 3 : 1;
                    };
                    Entity direct = application.action != Action.HARM_DEMONS && caster != null ? caster : source;
                    var damageSource = silver ? WitcheryDamageTypes.silverBrew(target.level(), direct, caster)
                            : target.damageSources().indirectMagic(direct, caster);
                    target.hurt(damageSource, Mth.ceil((damage << strength) * strengthScale));
                }
            }
            case WEAKEN_VAMPIRES -> {
                if (!Vampirism.isVampire(target)) return;
                if (target instanceof ServerPlayer player) Vampirism.drainBloodPower(player, 50 + 20 * strength);
                int ticks = Mth.ceil(1800 * application.durationMultiplier * durationScale);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, strength), source);
            }
            case REVEAL -> BrewCreatureEffects.reveal(target);
            case MOVE_ANIMALS -> BrewCreatureEffects.moveAnimals(target, strength, application.inverted);
            case SNOW -> target.addEffect(new MobEffectInstance(WitcheryEffects.SNOW_TRAIL.get(),
                    Mth.ceil(3600 * application.durationMultiplier * durationScale), strength,
                    application.ambient, true), source);
            case REMOVE_DEBUFFS -> {
                if (taglock) BrewStatusActions.ritualCleanse(target, strength);
                else BrewStatusActions.cleanse(target, caster, strength);
            }
            case REMOVE_BUFFS -> BrewStatusActions.removeBuffs(target, strength);
            case WASTING -> BrewStatusActions.wasting(target, source, strength,
                    application.durationMultiplier * durationScale, application.inverted);
            case DRAIN_MAGIC -> InfusionEnergy.drain(target, (float) (0.25 * (1 + strength) * strengthScale));
            case DURATION_BOOST -> BrewStatusActions.boostDuration(target, strength, strengthScale);
            case STEAL_BUFFS -> BrewStatusActions.stealBuffs(target, strength, strengthScale);
            case SPREAD_DEBUFFS -> BrewStatusActions.spreadDebuffs(target, strength, strengthScale);
            case AIR_HIKE -> BrewCreatureEffects.airHike(target, strength);
            case FROG_TONGUE -> BrewCreatureEffects.frogTongue(target, caster, impact, strength);
            case KNOCKBACK -> BrewCreatureEffects.knockback(target, impact, strength, strengthScale);
            case BAT_BURST -> BrewCreatureEffects.batBurst(target, strength, strengthScale, glancing);
            case BODEGA -> BrewCreatureEffects.bodega(target, caster, strength, strengthScale, glancing);
            case POISON_TOAD -> BrewCreatureEffects.poisonToad(target, strength, strengthScale);
            case DISSIPATE -> {
                if (target instanceof SpectralUndead) {
                    target.hurt(target.damageSources().indirectMagic(caster != null ? caster : source, caster), (strength + 1) * 5.0F);
                }
            }
            case LILIFY -> {
                if (target.level() instanceof ServerLevel level) {
                    BrewTerrainEffects.lilify(level, target.blockPosition(), Direction.UP, strength, caster instanceof Player player ? player : null);
                }
            }
            case EROSION -> BrewErosion.corrodeFromCauldron(target, caster, strengthScale);
            case WEBS -> {
                if (target.level() instanceof ServerLevel level) {
                    BrewPlantEffects.webs(level, target.blockPosition(), null, strength, caster instanceof Player player ? player : null);
                }
            }
            case VINES, THORNS -> {
                var effect = application.action == Action.VINES ? WitcheryEffects.WRAPPED_IN_VINE : WitcheryEffects.SPIKED;
                int ticks = Mth.ceil(1800 * application.durationMultiplier * durationScale);
                target.addEffect(new MobEffectInstance(effect.get(), ticks, Math.min(strength, 10),
                        application.ambient, true), source);
            }
            case SPROUTING -> target.addEffect(new MobEffectInstance(WitcheryEffects.SPROUTING.get(),
                    Mth.ceil(300 * application.durationMultiplier * durationScale), strength), source);
            case BLIGHT -> BrewCreatureEffects.blight(target, strength);
            case RAISE_LAND -> {
                if (target.level() instanceof ServerLevel level) {
                    BrewLandscape.raiseColumn(level, target.blockPosition().below(), (strength + 1) * (ritual ? 2 : 3),
                            caster instanceof Player player ? player : null);
                }
            }
            case TRANSPOSE -> BrewTransposition.transpose(target, strength, impact, destination);
            case INFERNO -> {
                if (!glancing && target.level() instanceof ServerLevel level) {
                    target.setSecondsOnFire(2 + 2 * strength);
                    if (strengthScale == 1) InfernoSplatter.burst(level, target.blockPosition(), strength, caster);
                }
            }
            case HELL_GATE -> target.addEffect(new MobEffectInstance(WitcheryEffects.HELLISH_AURA.get(),
                    Mth.ceil(3600 * application.durationMultiplier * durationScale), Math.min(strength, 10),
                    application.ambient, true), source);
            case BLAST -> {
                if (target.level() instanceof ServerLevel level && (strengthScale == 1 || level.random.nextDouble() < strengthScale * 0.2)) {
                    BrewFireEffects.blast(level, target.position(), strength, caster);
                }
            }
            case CHANGING_SEASONS, ICE_WORLD, RAISING, TIDE_HOLD, LAVA_HOLD, LEVELLING, TRANSPOSE_ORE,
                 FLOWERS, FERTILIZATION, HARVESTING, TILLING, PLANTING, PRUNING, FELLING, PULVERISATION,
                 TREE_OAK, TREE_SPRUCE, TREE_BIRCH, TREE_JUNGLE, TREE_ACACIA, TREE_DARK_OAK, TREE_ROWAN, TREE_ALDER, TREE_HAWTHORN -> { }
        }
    }

    private static ResourceLocation id(ItemStack stack) {
        if (stack.getItem() instanceof BrambleBlock.Item && !BrambleBlock.isWild(stack)) return ENDER_BRAMBLE;
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof WitchSaplingBlock) {
            return BrewTrees.witchSaplingKey(WitchWoodType.fromStack(stack));
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static boolean hasAction(ItemStack stack) {
        return INGREDIENTS.containsKey(id(stack));
    }

    public static int woolColor(ItemStack stack) {
        Ingredient ingredient = INGREDIENTS.get(id(stack));
        return ingredient != null && ingredient.modifier == Modifier.SET_COLOR ? ingredient.amount : -1;
    }

    public static int blendColor(int color, ItemStack stack) {
        return BrewColor.append(color, stack) & 0xFFFFFF;
    }

    static int ingredientPower(ItemStack stack) {
        ResourceLocation id = id(stack);
        Ingredient ingredient = INGREDIENTS.get(id);
        if (ingredient != null) return ingredient.power;
        return switch (id.toString()) {
            case "minecraft:vine" -> 150;
            default -> 0;
        };
    }

    @Nullable
    private static Ingredient validIngredient(ItemStack stack, ResourceLocation id) {
        Ingredient ingredient = INGREDIENTS.get(id);
        if (ingredient == null || stack.isEmpty()
                || ingredient.action == Action.CHANGING_SEASONS && BiomeNoteItem.biomeId(stack) == null) return null;
        return ingredient;
    }

    @Nullable
    private static Mixing mix(List<ItemStack> stacks, boolean ignoreRecipeOfferings, boolean taglock) {
        Mixing mixing = new Mixing(taglock);
        for (ItemStack stack : stacks) {
            ResourceLocation id = id(stack);
            Ingredient ingredient = validIngredient(stack, id);
            if (ingredient == null) {
                if (ignoreRecipeOfferings) continue;
                return null;
            }
            mixing.add(stack, id, ingredient);
        }
        return mixing;
    }

    private static final class EffectBoost {
        private int current;
        private int total;

        void increase(int ceiling, boolean unlimitedBoosts) {
            if (current < ceiling && (total < 7 || unlimitedBoosts)) {
                current++;
                total++;
            }
        }
    }

    private static final class DescriptionBuilder {
        private final List<NamedEffect> effects = new ArrayList<>();
        private final EffectBoost strength = new EffectBoost();
        private final EffectBoost duration = new EffectBoost();
        private boolean inverted;
        private boolean unlimitedBoosts;
        private int extent;
        private int lifetime;
        private int dispersalExtent;
        private int dispersalLifetime;
        private int drinkTicks = 32;
        private Delivery delivery = Delivery.DRINK;

        void add(Ingredient ingredient) {
            if (ingredient.isEffect()) {
                Component name;
                int baseDuration;
                if (ingredient.action != null) {
                    name = Component.translatable(ingredient.action.nameKey(inverted));
                    baseDuration = ingredient.action.displayedDuration();
                } else {
                    name = (inverted ? ingredient.inverted : ingredient.normal).brewName();
                    // The original label uses the ingredient's base duration for both effect names.
                    baseDuration = ingredient.normal.duration;
                }
                effects.add(new NamedEffect(name, strength.current, baseDuration * (duration.current + 1)));
                strength.current = 0;
                duration.current = 0;
                inverted = false;
                return;
            }
            // The legacy name records each modifier in order, independently of the runtime effect caps.
            switch (ingredient.modifier) {
                case CAPACITY -> unlimitedBoosts |= ingredient.ceiling == 10;
                case STRENGTH -> strength.increase(Integer.MAX_VALUE, unlimitedBoosts);
                case DURATION -> duration.increase(Integer.MAX_VALUE, unlimitedBoosts);
                case EXTENT -> extent++;
                case LIFETIME -> lifetime++;
                case DELIVERY -> {
                    delivery = Delivery.values()[ingredient.amount];
                    dispersalExtent = extent;
                    dispersalLifetime = lifetime;
                    extent = 0;
                    lifetime = 0;
                }
                case INVERT -> inverted = true;
                case DRINK_SPEED -> drinkTicks += ingredient.amount;
                default -> { }
            }
        }
    }

    private static final class Mixing {
        private final List<BrewEffect> effects = new ArrayList<>();
        private final Set<ResourceLocation> segment = new HashSet<>();
        private final boolean taglock;
        private int capacity;
        private int used;
        private int power;
        private int drinkTicks = 32;
        private int yieldModifier;
        private Delivery delivery = Delivery.DRINK;
        private int extent;
        private int lifetime;
        private final EffectBoost entityStrength = new EffectBoost();
        private final EffectBoost blockStrength = new EffectBoost();
        private final EffectBoost entityDuration = new EffectBoost();
        private final EffectBoost blockDuration = new EffectBoost();
        private boolean inverted;
        private boolean ambient;
        private boolean unlimitedBoosts;
        private boolean raisedEffectCeiling;
        private boolean noBlockTarget;
        private boolean noEntityTarget;
        private final List<WaystoneItem.Location> destinations = new ArrayList<>(2);

        Mixing(boolean taglock) {
            this.taglock = taglock;
        }

        void add(ItemStack stack, ResourceLocation id, Ingredient ingredient) {
            power += ingredient.power;
            yieldModifier += ingredient.yieldModifier;
            if (ingredient.isEffect()) {
                applyEffect(stack, ingredient);
                segment.clear();
            } else {
                applyModifier(ingredient);
                if (ingredient.modifier == Modifier.WAYSTONE && destinations.size() < 2) {
                    var location = WaystoneItem.location(stack);
                    if (location != null) destinations.add(location);
                }
            }
            segment.add(id);
        }

        private void applyEffect(ItemStack stack, Ingredient ingredient) {
            if (used + ingredient.amount > capacity) return;
            used += ingredient.amount;
            if (ingredient.action != null) {
                int actionStrength = switch (ingredient.action) {
                    case HARM_DEMONS, HARM_UNDEAD, HARM_INSECTS, HARM_WEREWOLVES ->
                            Math.min(entityStrength.current, raisedEffectCeiling ? 3 : 1);
                    default -> entityStrength.current;
                };
                var biome = ingredient.action == Action.CHANGING_SEASONS ? BiomeNoteItem.biomeId(stack) : null;
                effects.add(new ActionApplication(ingredient.action, actionStrength, blockStrength.current, inverted,
                        entityDuration.current + 1, blockDuration.current + 1, ambient, !noEntityTarget, !noBlockTarget, biome));
                // Creature-only potion effects do not spend the modifiers for the terrain impact.
                blockStrength.current = 0;
                blockDuration.current = 0;
            } else {
                Potion effect = inverted ? ingredient.inverted : ingredient.normal;
                if (effect.dye != null) {
                    effects.add(new Application(effect.effect.get(), effect.duration, effect.dye.ordinal(),
                            true, false, 0, !noEntityTarget));
                } else {
                    int ceiling = raisedEffectCeiling ? 10 : ingredient.ceiling;
                    effects.add(new Application(effect.effect.get(), effect.duration * (entityDuration.current + 1),
                            Math.min(entityStrength.current, ceiling), ambient, inverted,
                            effect.curse ? effect.duration * TAGLOCK_DURATION_MULTIPLIER : 0, !noEntityTarget));
                }
            }
            entityStrength.current = 0;
            inverted = false;
            // The original ritual curse restores these two modifiers before the next ingredient.
            if (!taglock || !ingredient.retainsTaglockModifiers()) {
                entityDuration.current = 0;
                ambient = false;
            }
        }

        private void applyModifier(Ingredient ingredient) {
            switch (ingredient.modifier) {
                case CAPACITY -> {
                    if (capacity < ingredient.ceiling) capacity += ingredient.amount;
                    if (ingredient.ceiling == 10) unlimitedBoosts = true;
                }
                case STRENGTH -> {
                    entityStrength.increase(ingredient.ceiling, unlimitedBoosts);
                    blockStrength.increase(ingredient.ceiling, unlimitedBoosts);
                }
                case DURATION -> {
                    entityDuration.increase(ingredient.ceiling, unlimitedBoosts);
                    blockDuration.increase(ingredient.ceiling, unlimitedBoosts);
                }
                case INVERT -> inverted = true;
                case AMBIENT -> ambient = true;
                case DRINK_SPEED -> drinkTicks += ingredient.amount;
                case DELIVERY -> delivery = Delivery.values()[ingredient.amount];
                case EXTENT -> {
                    if (extent < ingredient.ceiling) extent++;
                }
                case LIFETIME -> {
                    if (lifetime < ingredient.ceiling) lifetime++;
                }
                case NO_BLOCK_TARGET -> noBlockTarget = true;
                case NO_ENTITY_TARGET -> noEntityTarget = true;
                case RAISE_EFFECT_CEILING -> raisedEffectCeiling = true;
                case SET_COLOR, WAYSTONE, RITUAL -> { }
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
        modifier(entries, "gold_nugget", 50, Modifier.AMBIENT, 0, 0, 0);
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
        modifier(entries, "nether_brick", 50, Modifier.NO_BLOCK_TARGET, 0, 0, 0);
        modifier(entries, "brick", 50, Modifier.NO_ENTITY_TARGET, 0, 0, 0);
        modifier(entries, "tropical_fish", 200, Modifier.RAISE_EFFECT_CEILING, 0, 0, 0);
        for (BrewDye dye : BrewDye.values()) {
            modifier(entries, dye.wool(), 0, Modifier.SET_COLOR, dye.rgb(), 0, 0);
            if (dye.colorsCreatures()) {
                Potion color = new Potion(WitcheryEffects.COLORFUL, ColorfulEffect.DURATION, false, dye);
                potion(entries, dye.item(), 0, 1, 0, color, color);
            }
        }
        entries.put(ResourceLocation.withDefaultNamespace("coal"), new Ingredient(0, null, 1, 10, 0, null, null, Action.EXTINGUISH));
        entries.put(ResourceLocation.withDefaultNamespace("snowball"), new Ingredient(0, null, 1, 10, 0, null, null, Action.SNOW));

        entries.put(ResourceLocation.parse("witchery:icy_needle"), new Ingredient(200, null, 2, 10, 0, null, null, Action.COLD));
        entries.put(ResourceLocation.parse("witchery:frozen_heart"), new Ingredient(500, null, 5, 10, 0, null, null, Action.ICE_SHELL));
        entries.put(ResourceLocation.withDefaultNamespace("ender_eye"), new Ingredient(2000, null, 5, 0, 0, null, null, Action.ICE_WORLD));
        entries.put(ResourceLocation.withDefaultNamespace("ice"), new Ingredient(500, null, 5, 3, 0, null, null, Action.HARM_DEMONS));
        entries.put(ResourceLocation.withDefaultNamespace("pumpkin"), new Ingredient(200, null, 2, 3, 0, null, null, Action.HARM_UNDEAD));
        entries.put(ResourceLocation.withDefaultNamespace("blue_orchid"), new Ingredient(200, null, 2, 3, 0, null, null, Action.HARM_INSECTS));
        entries.put(ResourceLocation.parse("witchery:silver_dust"), new Ingredient(1000, null, 4, 3, 0, null, null, Action.HARM_WEREWOLVES));
        entries.put(ResourceLocation.parse("witchery:garlic"), new Ingredient(500, null, 4, 10, 0, null, null, Action.WEAKEN_VAMPIRES));
        entries.put(ResourceLocation.parse("witchery:purified_milk"), new Ingredient(200, null, 2, 10, 0, null, null, Action.REMOVE_DEBUFFS));
        entries.put(ResourceLocation.parse("witchery:reek_of_misfortune"), new Ingredient(250, null, 2, 10, 0, null, null, Action.REMOVE_BUFFS));
        entries.put(ResourceLocation.parse("witchery:subdued_spirit"), new Ingredient(500, null, 4, 10, 0, null, null, Action.WASTING));
        entries.put(ResourceLocation.parse("witchery:condensed_fear"), new Ingredient(1000, null, 6, 10, 0, null, null, Action.DRAIN_MAGIC));
        entries.put(ResourceLocation.parse("witchery:creeper_heart"), new Ingredient(5000, null, 6, 10, 0, null, null, Action.DURATION_BOOST));
        entries.put(ResourceLocation.withDefaultNamespace("skeleton_skull"), new Ingredient(100, null, 6, 10, 0, null, null, Action.STEAL_BUFFS));
        entries.put(ResourceLocation.withDefaultNamespace("creeper_head"), new Ingredient(2000, null, 8, 10, 0, null, null, Action.SPREAD_DEBUFFS));
        entries.put(ResourceLocation.parse("witchery:bat_ball"), new Ingredient(1000, null, 2, 10, 0, null, null, Action.BAT_BURST));
        entries.put(ResourceLocation.parse("witchery:owlets_wing"), new Ingredient(1000, null, 2, 10, 0, null, null, Action.BODEGA));
        entries.put(ResourceLocation.parse("witchery:breath_of_the_goddess"), new Ingredient(750, null, 2, 10, 0, null, null, Action.AIR_HIKE));
        entries.put(ResourceLocation.withDefaultNamespace("slime_ball"), new Ingredient(150, null, 2, 10, 0, null, null, Action.FROG_TONGUE));
        entries.put(ResourceLocation.withDefaultNamespace("stick"), new Ingredient(200, null, 2, 10, 0, null, null, Action.KNOCKBACK));
        entries.put(ResourceLocation.withDefaultNamespace("sunflower"), new Ingredient(500, null, 5, 10, 0, null, null, Action.POISON_TOAD));
        entries.put(ResourceLocation.withDefaultNamespace("bone"), new Ingredient(2000, null, 4, 10, 0, null, null, Action.RAISING));
        entries.put(ResourceLocation.withDefaultNamespace("dandelion"), new Ingredient(200, null, 1, 10, 0, null, null, Action.FLOWERS));
        entries.put(ResourceLocation.withDefaultNamespace("bone_meal"), new Ingredient(250, null, 1, 10, 0, null, null, Action.FERTILIZATION));
        entries.put(ResourceLocation.withDefaultNamespace("apple"), new Ingredient(0, null, 1, 10, 0, null, null, Action.HARVESTING));
        entries.put(ResourceLocation.withDefaultNamespace("dirt"), new Ingredient(0, null, 1, 10, 0, null, null, Action.TILLING));
        entries.put(ResourceLocation.withDefaultNamespace("wheat_seeds"), new Ingredient(0, null, 1, 10, 0, null, null, Action.PLANTING));
        entries.put(ResourceLocation.withDefaultNamespace("brown_mushroom"), new Ingredient(0, null, 1, 10, 0, null, null, Action.PRUNING));
        entries.put(ResourceLocation.withDefaultNamespace("string"), new Ingredient(0, null, 1, 10, 0, null, null, Action.FELLING));
        entries.put(ResourceLocation.withDefaultNamespace("flint"), new Ingredient(250, null, 1, 10, 0, null, null, Action.PULVERISATION));
        entries.put(ResourceLocation.withDefaultNamespace("stone"), new Ingredient(0, null, 1, 10, 0, null, null, Action.DISSIPATE));
        entries.put(ResourceLocation.withDefaultNamespace("sand"), new Ingredient(0, null, 1, 10, 0, null, null, Action.TIDE_HOLD));
        entries.put(ResourceLocation.withDefaultNamespace("cobblestone"), new Ingredient(100, null, 2, 10, 0, null, null, Action.LAVA_HOLD));
        entries.put(ResourceLocation.withDefaultNamespace("lily_pad"), new Ingredient(200, null, 1, 10, 0, null, null, Action.LILIFY));
        entries.put(ResourceLocation.parse("witchery:oil_of_vitriol"), new Ingredient(200, null, 2, 10, 0, null, null, Action.EROSION));
        entries.put(ResourceLocation.withDefaultNamespace("netherrack"), new Ingredient(200, null, 2, 10, 0, null, null, Action.LEVELLING));
        entries.put(ResourceLocation.parse("witchery:thrown_web"), new Ingredient(200, null, 2, 10, 0, null, null, Action.WEBS));
        entries.put(ResourceLocation.withDefaultNamespace("vine"), new Ingredient(150, null, 2, 10, 0, null, null, Action.VINES));
        entries.put(ResourceLocation.withDefaultNamespace("cactus"), new Ingredient(150, null, 2, 10, 0, null, null, Action.THORNS));
        entries.put(ResourceLocation.parse("witchery:ent_branch"), new Ingredient(350, null, 2, 10, 0, null, null, Action.SPROUTING));
        for (Action action : Action.values()) {
            if (action.tree != null) entries.put(action.tree.key, new Ingredient(200, null, 2, 10, 0, null, null, action));
        }
        entries.put(ResourceLocation.withDefaultNamespace("poisonous_potato"),
                new Ingredient(2000, null, 4, 10, 0, null, null, Action.BLIGHT));
        entries.put(ResourceLocation.withDefaultNamespace("quartz"),
                new Ingredient(2000, null, 4, 10, 0, null, null, Action.RAISE_LAND));
        entries.put(ResourceLocation.withDefaultNamespace("ender_pearl"),
                new Ingredient(1000, null, 4, 10, 0, null, null, Action.TRANSPOSE));
        entries.put(ResourceLocation.withDefaultNamespace("iron_ingot"),
                new Ingredient(2000, null, 4, 10, 0, null, null, Action.TRANSPOSE_ORE));
        entries.put(ResourceLocation.parse("witchery:glintweed"),
                new Ingredient(750, null, 3, 10, 0, null, null, Action.INFERNO));
        entries.put(ResourceLocation.parse("witchery:refined_evil"),
                new Ingredient(3000, null, 5, 10, 0, null, null, Action.HELL_GATE));
        entries.put(ResourceLocation.withDefaultNamespace("gold_ingot"),
                new Ingredient(500, null, 5, 10, 0, null, null, Action.BLAST));
        entries.put(ResourceLocation.fromNamespaceAndPath("witchery", "odour_of_purity"),
                new Ingredient(100, null, 4, 0, 0, null, null, Action.REVEAL));
        entries.put(ResourceLocation.fromNamespaceAndPath("witchery", "heart_of_gold"),
                new Ingredient(500, null, 4, 0, 0, null, null, Action.MOVE_ANIMALS));
        entries.put(ResourceLocation.fromNamespaceAndPath("witchery", "biomenote"),
                new Ingredient(5000, null, 8, 0, 0, null, null, Action.CHANGING_SEASONS));

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
        potion(entries, "witchery:focused_will", 10000, 8, WitcheryEffects.KEEP_INVENTORY, 7200);
        potion(entries, "witchery:redstone_soup", 10000, 8, WitcheryEffects.KEEP_EFFECTS, 7200);
        potion(entries, "witchery:hint_of_rebirth", 2500, 6, WitcheryEffects.REINCARNATION, 3600);
        potion(entries, BRAMBLE.toString(), 8000, 6, WitcheryEffects.ILL_FITTING, 120);
        potion(entries, ENDER_BRAMBLE.toString(), 250, 2, WitcheryEffects.REPEL_ATTACKER, 1800);
        potion(entries, "witchery:whiff_of_magic", 2000, 4, WitcheryEffects.ABSORB_MAGIC, 1200);
        potion(entries, "witchery:spectral_dust", 2000, 5, WitcheryEffects.REFLECT_DAMAGE, 1800);
        potion(entries, "witchery:foul_fume", 1000, 4, WitcheryEffects.STOUT_BELLY, 1800);
        potion(entries, "red_mushroom", 200, 2, WitcheryEffects.POISON_WEAPONS, 1800);
        potion(entries, "wheat", 0, 1, WitcheryEffects.FEEL_NO_PAIN, 1800);
        potion(entries, "sugar_cane", 250, 2, WitcheryEffects.FLOATING, 1800);
        potion(entries, "feather", 100, 2, WitcheryEffects.FEATHER_FALL, 1200);
        potion(entries, "cod", 0, 1, WitcheryEffects.SWIMMING, 3600);
        potion(entries, "witchery:toe_of_frog", 500, 4, WitcheryEffects.DOUBLE_JUMP, 7200);
        potion(entries, "cobweb", 250, 2, 10,
                new Potion(WitcheryEffects.REFLECT_PROJECTILES, 1800), new Potion(WitcheryEffects.ATTRACT_PROJECTILES, 900));
        potion(entries, "witchery:drop_of_luck", 5000, 4, 10,
                new Potion(WitcheryEffects.BREWING_EXPERTISE, 7200), new Potion(WitcheryEffects.INSANITY, 3600, true));
        curse(entries, "witchery:disturbed_cotton", 3000, WitcheryEffects.SINKING);
        curse(entries, "rotten_flesh", 2000, WitcheryEffects.DISEASED);
        curse(entries, "witchery:embermoss", 3000, WitcheryEffects.OVERHEATING);
        curse(entries, "witchery:mellifluous_hunger", 10000, WitcheryEffects.WAKING_NIGHTMARE);
        potion(entries, "soul_sand", 4000, 4, WitcheryEffects.DARKNESS_ALLERGY, 2400);
        potion(entries, "salmon", 1000, 6, WitcheryEffects.SUN_ALLERGY, 1200);
        potion(entries, "golden_apple", 1000, 4, () -> MobEffects.ABSORPTION, 600);
        potion(entries, "enchanted_golden_apple", 1000, 4, () -> MobEffects.HEALTH_BOOST, 2400);
        potion(entries, "poppy", 500, 4, WitcheryEffects.LOVE, 200);
        potion(entries, "dead_bush", 1000, 4, WitcheryEffects.VOLATILITY, 3600);
        potion(entries, "clay_ball", 1000, 6, WitcheryEffects.FORTUNE, 3600);
        potion(entries, "emerald", 2500, 6, WitcheryEffects.RESIZING, 400);
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

    private static void curse(Map<ResourceLocation, Ingredient> entries, String id, int power, Supplier<MobEffect> effect) {
        Potion potion = new Potion(effect, 3600, true);
        potion(entries, id, power, 4, 10, potion, potion);
    }

    private static void potion(Map<ResourceLocation, Ingredient> entries, String id, int power, int levels,
                               int strengthCeiling, Potion normal, Potion inverted) {
        entries.put(ResourceLocation.parse(id), new Ingredient(power, null, levels, strengthCeiling, 0, normal, inverted, null));
    }
}
