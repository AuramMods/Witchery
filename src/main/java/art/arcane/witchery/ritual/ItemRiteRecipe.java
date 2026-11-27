package art.arcane.witchery.ritual;

import art.arcane.witchery.block.RitualGlyphBlock;
import art.arcane.witchery.block.entity.GrassperBlockEntity;
import art.arcane.witchery.item.ArthanaItem;
import art.arcane.witchery.item.BolineItem;
import art.arcane.witchery.item.CircleTalismanItem;
import art.arcane.witchery.item.SpectralStoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryRecipes;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.RecipeMatcher;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public record ItemRiteRecipe(ResourceLocation id, List<Ingredient> sacrifices, List<Ring> rings, int power,
                             ItemStack result, Action action, boolean nightOnly, @Nullable EntityType<?> livingOffering,
                             boolean consumeNecromanticStone)
        implements Recipe<Container> {
    private static final int GRASSPER_RANGE = 5;

    public enum Action {
        ITEM,
        MANIFESTATION,
        SUMMON_DEMON,
        CURSE_WAKING_NIGHTMARE(DirectedCurse.WAKING_NIGHTMARE, true),
        CURE_WAKING_NIGHTMARE(DirectedCurse.WAKING_NIGHTMARE, false),
        BIND_FAMILIAR,
        CALL_FAMILIAR,
        SUMMON_IMP,
        INFUSE_OVERWORLD,
        RECHARGE_INFUSION,
        INFUSE_INFERNAL,
        INFUSE_LIGHT,
        INFUSE_OTHERWHERE,
        CORRUPT_VOODOO_PROTECTION,
        BIND_WAYSTONE,
        BIND_CREATURE_WAYSTONE,
        TRAVEL_WAYSTONE,
        CURE_VAMPIRISM,
        CURSE_OF_THE_WOLF,
        CURE_LYCANTHROPY,
        TRANSPOSE_CREATURE,
        TRANSPOSE_ORES,
        BIND_SPECTRAL_STONE,
        BIND_FETISH,
        SUMMON_REFLECTION,
        GLYPHIC_TRANSFORMATION,
        FORESTATION,
        RAISE_VOLCANO,
        ICE_SHELL,
        CLIMATE_CHANGE,
        BIND_WORSHIP_STATUE,
        CALL_STORM,
        CALL_GREATER_STORM,
        CALL_PORTABLE_STORM,
        ECLIPSE,
        PROTECTION,
        IMPRISONMENT,
        BARRIER,
        GREATER_BARRIER,
        PORTABLE_BARRIER,
        COPY_WAYSTONE,
        BIND_CIRCLE_TALISMAN,
        PART_EARTH,
        RAISE_EARTH,
        RAISE_EARTH_MEDIUM,
        RAISE_EARTH_LARGE,
        BANISH_DEMON,
        SUMMON_WITHER,
        SUMMON_WITCH,
        SUMMON_SPECTRAL_FAMILIAR,
        FERTILITY,
        NATURES_POWER,
        CALL_BEASTS,
        CURSE_MISFORTUNE(DirectedCurse.MISFORTUNE, true),
        CURE_MISFORTUNE(DirectedCurse.MISFORTUNE, false),
        CURSE_INSANITY(DirectedCurse.INSANITY, true),
        CURE_INSANITY(DirectedCurse.INSANITY, false),
        CURSE_SINKING(DirectedCurse.SINKING, true),
        CURE_SINKING(DirectedCurse.SINKING, false),
        CURSE_OVERHEATING(DirectedCurse.OVERHEATING, true),
        CURE_OVERHEATING(DirectedCurse.OVERHEATING, false),
        RAISE_PORTABLE_VOLCANO,
        COOK_FOOD,
        COOK_FOOD_PORTABLE,
        BLIGHT,
        AREA_BLINDNESS,
        HELL_ON_EARTH,
        RAIN_TOADS,
        PRIOR_INCARNATION;

        @Nullable private final DirectedCurse directedCurse;
        private final boolean inflictsDirectedCurse;

        Action() {
            this(null, false);
        }

        Action(@Nullable DirectedCurse directedCurse, boolean inflictsDirectedCurse) {
            this.directedCurse = directedCurse;
            this.inflictsDirectedCurse = inflictsDirectedCurse;
        }

        @Nullable
        public DirectedCurse directedCurse() {
            return directedCurse;
        }

        public boolean inflictsDirectedCurse() {
            return inflictsDirectedCurse;
        }

        public boolean summonsCreature() {
            return switch (this) {
                case SUMMON_DEMON, SUMMON_IMP, SUMMON_REFLECTION, SUMMON_WITHER, SUMMON_WITCH, SUMMON_SPECTRAL_FAMILIAR -> true;
                default -> false;
            };
        }

        public boolean usesLandscapeUpkeep() {
            return usesWaystoneTarget() || this == RAISE_PORTABLE_VOLCANO || this == ICE_SHELL || this == CLIMATE_CHANGE || this == PART_EARTH
                    || raisedEarthRadius() > 0 || this == FERTILITY || this == NATURES_POWER || this == CALL_BEASTS
                    || expandsCurse() || this == RAIN_TOADS;
        }

        public boolean usesWaystoneTarget() {
            return this == FORESTATION || this == RAISE_VOLCANO || this == RAISE_EARTH || this == HELL_ON_EARTH;
        }

        public boolean expandsCurse() {
            return this == BLIGHT || this == AREA_BLINDNESS || this == HELL_ON_EARTH;
        }

        public boolean usesExpansionClock() {
            return expandsCurse() || this == FERTILITY || this == ICE_SHELL;
        }

        public boolean isVolcano() {
            return this == RAISE_VOLCANO || this == RAISE_PORTABLE_VOLCANO;
        }

        public boolean cooksFood() {
            return this == COOK_FOOD || this == COOK_FOOD_PORTABLE;
        }

        public int raisedEarthRadius() {
            return switch (this) {
                case RAISE_EARTH -> 4;
                case RAISE_EARTH_MEDIUM -> 6;
                case RAISE_EARTH_LARGE -> 9;
                default -> 0;
            };
        }

        public boolean targetsStormWithWaystone() {
            return this == CALL_STORM || this == CALL_GREATER_STORM;
        }

        public boolean callsStorm() {
            return targetsStormWithWaystone() || this == CALL_PORTABLE_STORM;
        }

        public boolean acceptsOptionalWaystone() {
            var ward = protection();
            return usesWaystoneTarget() || targetsStormWithWaystone() || ward != null && ward.acceptsWaystone();
        }

        @Nullable
        public ProtectionRites.Kind protection() {
            return switch (this) {
                case PROTECTION -> ProtectionRites.Kind.PROTECTION;
                case IMPRISONMENT -> ProtectionRites.Kind.IMPRISONMENT;
                case BARRIER -> ProtectionRites.Kind.BARRIER;
                case GREATER_BARRIER -> ProtectionRites.Kind.GREATER_BARRIER;
                case PORTABLE_BARRIER -> ProtectionRites.Kind.PORTABLE_BARRIER;
                default -> null;
            };
        }
    }

    public record Ring(RitualCircle pattern, Block glyph) {
    }

    public ItemStack returnedOffering(ItemStack offering) {
        if (offering.is(WitcheryIngredient.CHARGED_ATTUNED_STONE.item())) {
            return new ItemStack(WitcheryIngredient.ATTUNED_STONE.item());
        }
        if (offering.getItem() instanceof ArthanaItem || offering.getItem() instanceof BolineItem
                || offering.is(WitcheryIngredient.NECROMANTIC_STONE.item()) && !consumeNecromanticStone) {
            return offering;
        }
        return ItemStack.EMPTY;
    }

    public record OfferingSource(@Nullable UUID drop, @Nullable BlockPos grassper) {
    }

    public record Offering(ItemStack stack, Vec3 position, OfferingSource source, Runnable consume) {
        @Nullable
        public BlockPos grassper() {
            return source.grassper();
        }
    }

    public boolean hasCircles(ServerLevel level, BlockPos pos) {
        return rings.stream().allMatch(ring -> ring.pattern.matches(level, pos, ring.glyph));
    }

    public boolean dayOnly() {
        return action == Action.ECLIPSE;
    }

    public boolean stagesLivingOffering() {
        return livingOffering != null && isLegacyRite();
    }

    public boolean stagesAltarPower() {
        return power > 0 && isLegacyRite();
    }

    public boolean chargesBeforeLivingOffering() {
        return RiteActivation.legacyId(id) == 95; // Rite of the Mirror
    }

    private boolean isLegacyRite() {
        return RiteActivation.legacyId(id) != Integer.MAX_VALUE;
    }

    @Nullable
    public String startFailure(ServerLevel level) {
        if (action == Action.HELL_ON_EARTH && !level.dimension().equals(Level.OVERWORLD)) {
            return "message.witchery.rite.hell_on_earth_overworld";
        }
        if (level.dimension().equals(WitcheryDimensions.DREAM)) {
            if (action.cooksFood()) return "message.witchery.rite.cooking_dream";
            if (action.directedCurse() != null) return "message.witchery.rite.directed_curse_dream";
            if (action == Action.PRIOR_INCARNATION) return "message.witchery.rite.prior_incarnation_dream";
            if (action == Action.CALL_BEASTS) return "message.witchery.rite.call_beasts_dream";
            if (action.usesLandscapeUpkeep()) return "message.witchery.rite.landscape_dream";
            if (action == Action.TRANSPOSE_CREATURE || action == Action.TRANSPOSE_ORES) {
                return "message.witchery.rite.transpose_dream";
            }
            if (action.callsStorm() || dayOnly()) return "message.witchery.rite.weather_dream";
            if (action.protection() != null) return "message.witchery.rite.protection_dream";
        }
        boolean legacy = isLegacyRite();
        if (dayOnly() && !(legacy ? legacyDaytime(level) : level.isDay())) return "message.witchery.rite.day_only";
        if (nightOnly && (legacy ? legacyDaytime(level) : !level.isNight())) return "message.witchery.rite.night_only";
        if (action.inflictsDirectedCurse() && !(legacy ? WeatherRites.hasLegacyThunder(level) : level.isThundering())) {
            return "message.witchery.rite.storm_only";
        }
        return null;
    }

    public static boolean legacyFullMoon(ServerLevel level) {
        return !legacyDaytime(level) && level.getMoonPhase() == 0;
    }

    private static boolean legacyDaytime(ServerLevel level) {
        var dimension = level.dimension();
        // The original providers supplied a constant day or night in these realms.
        if (dimension.equals(Level.END)) return true;
        if (dimension.equals(WitcheryDimensions.DREAM)) return !DreamTravel.isNightmare(level);
        if (dimension.equals(Level.NETHER) || dimension.equals(WitcheryDimensions.MIRROR)
                || dimension.equals(WitcheryDimensions.TORMENT)) return false;
        return level.getSkyDarken() < 4;
    }

    @Nullable
    public Mob findLivingOffering(ServerLevel level, BlockPos pos) {
        if (livingOffering == null) return null;
        boolean legacy = isLegacyRite();
        int radius = offeringRadius() + 1;
        AABB area = legacy ? legacyOfferingBounds(pos) : new AABB(pos).inflate(radius, 1, radius);
        Vec3 origin = legacy ? Vec3.atLowerCornerOf(pos) : Vec3.atBottomCenterOf(pos);
        return level.getEntitiesOfClass(Mob.class, area, mob -> mob.isAlive()
                        && (mob.getType() == livingOffering || legacy && livingOffering == EntityType.ZOMBIE && mob instanceof Zombie)
                        && mob.distanceToSqr(origin) <= radius * radius)
                .stream().findFirst().orElse(null);
    }

    public boolean hasRequiredOfferings(ServerLevel level, BlockPos pos) {
        return matchSacrifices(validOfferings(level, pos), 0) != null;
    }

    @Nullable
    public Offering findNextOffering(ServerLevel level, BlockPos pos, int step) {
        List<Offering> items = validOfferings(level, pos);
        if (!isLegacyRite()) {
            List<Offering> matched = matchSacrifices(items, step);
            return matched == null ? null : matched.get(0);
        }
        Ingredient required = sacrifices.get(step);
        for (Offering offering : items) {
            if (required.test(offering.stack())) return offering;
        }
        return null;
    }

    @Nullable
    List<Offering> activationOfferings(ServerLevel level, BlockPos pos, Predicate<Offering> available) {
        List<Offering> items = validOfferings(level, pos);
        items.removeIf(available.negate());
        List<Offering> matched = matchSacrifices(items, 0);
        if (matched == null) return null;

        // The legacy matcher also checked the Grasspers after finding a dropped stack.
        var reserved = new ArrayList<>(matched);
        for (int index = 0; index < matched.size(); index++) {
            if (matched.get(index).grassper() != null) continue;
            Ingredient ingredient = sacrifices.get(index);
            for (Offering offering : items) {
                if (offering.grassper() != null && !reserved.contains(offering) && ingredient.test(offering.stack())) {
                    reserved.add(offering);
                    break;
                }
            }
        }
        return reserved;
    }

    private List<Offering> validOfferings(ServerLevel level, BlockPos pos) {
        List<Offering> items = nearbyOfferings(level, pos);
        boolean legacy = isLegacyRite();
        if (legacy) {
            // The original item comparison required damage zero, with exceptions for these two tools.
            items.removeIf(offering -> {
                ItemStack stack = offering.stack();
                return stack.getMaxDamage() > 0 && stack.getDamageValue() != 0
                        && !(stack.getItem() instanceof ArthanaItem || stack.getItem() instanceof BolineItem);
            });
        }
        if (action == Action.BIND_CIRCLE_TALISMAN) {
            items.removeIf(offering -> offering.stack().getItem() instanceof CircleTalismanItem
                    && CircleTalismanItem.storedPattern(offering.stack()) != 0);
        } else if (action == Action.BIND_SPECTRAL_STONE) {
            items.removeIf(offering -> offering.stack().getItem() instanceof SpectralStoneItem
                    && SpectralStoneItem.typeId(offering.stack()) != 0);
        } else if (!legacy) {
            if (action.isVolcano()) {
                items.removeIf(offering -> offering.stack().is(Items.GOLDEN_SWORD) && offering.stack().isDamaged());
            } else if (action == Action.ICE_SHELL) {
                items.removeIf(offering -> offering.stack().is(Items.DIAMOND_SWORD) && offering.stack().isDamaged());
            } else if (action.callsStorm() || action == Action.ECLIPSE) {
                items.removeIf(offering -> offering.stack().isDamaged());
            }
        }
        return items;
    }

    @Nullable
    private List<Offering> matchSacrifices(List<Offering> items, int first) {
        List<Ingredient> required = sacrifices.subList(first, sacrifices.size());
        int[] owners = new int[items.size()];
        int[] assigned = new int[required.size()];
        Arrays.fill(owners, -1);
        for (int ingredient = 0; ingredient < required.size(); ingredient++) {
            if (!assign(ingredient, required, items, owners, assigned, new boolean[items.size()])) {
                return null;
            }
        }
        return Arrays.stream(assigned).mapToObj(items::get).toList();
    }

    List<Offering> activationWaystones(ServerLevel level, BlockPos pos, Predicate<Offering> available) {
        var selected = new ArrayList<Offering>(2);
        boolean foundDrop = false;
        for (Offering offering : nearbyOfferings(level, pos)) {
            if (!offering.stack().is(WitcheryIngredient.BOUND_WAYSTONE.item()) || !available.test(offering)) continue;
            if (offering.grassper() != null) {
                selected.add(offering);
                break;
            }
            if (!foundDrop) {
                selected.add(offering);
                foundDrop = true;
            }
        }
        return selected;
    }

    @Nullable
    public Offering findOptionalWaystone(ServerLevel level, BlockPos pos) {
        for (Offering offering : nearbyOfferings(level, pos)) {
            if (offering.stack().is(WitcheryIngredient.BOUND_WAYSTONE.item())) return offering;
        }
        return null;
    }

    private List<Offering> nearbyOfferings(ServerLevel level, BlockPos pos) {
        boolean legacy = isLegacyRite();
        int radius = offeringRadius();
        AABB area = legacy ? legacyOfferingBounds(pos) : new AABB(pos).inflate(radius, 0, radius);
        Vec3 origin = legacy ? Vec3.atLowerCornerOf(pos) : Vec3.atBottomCenterOf(pos);
        List<Offering> items = new ArrayList<>();
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, area, item -> item.isAlive()
                && item.distanceToSqr(origin.x, legacy ? origin.y : item.getY(), origin.z) <= radius * radius)) {
            ItemStack stack = entity.getItem();
            items.add(new Offering(stack, entity.position(), new OfferingSource(entity.getUUID(), null), () -> {
                if (stack.getCount() == 1) entity.discard();
                else entity.setItem(stack.copyWithCount(stack.getCount() - 1));
            }));
        }
        // Original rites scan all Z positions at each X before moving to the next column.
        BlockPos.MutableBlockPos point = new BlockPos.MutableBlockPos();
        for (int outer = -GRASSPER_RANGE; outer <= GRASSPER_RANGE; outer++) {
            for (int inner = -GRASSPER_RANGE; inner <= GRASSPER_RANGE; inner++) {
                point.setWithOffset(pos, legacy ? outer : inner, 0, legacy ? inner : outer);
                if (level.getBlockEntity(point) instanceof GrassperBlockEntity grassper && !grassper.isEmpty()) {
                    items.add(new Offering(grassper.getItem(0), Vec3.atBottomCenterOf(point).add(0, 0.8, 0),
                            new OfferingSource(null, grassper.getBlockPos()), () -> grassper.removeItem(0, 1)));
                }
            }
        }
        return items;
    }

    private int offeringRadius() {
        return rings.stream().mapToInt(ring -> ring.pattern.sacrificeRadius()).max().orElse(4);
    }

    private static AABB legacyOfferingBounds(BlockPos pos) {
        return new AABB(pos.offset(-8, 0, -8), pos.offset(8, 1, 8));
    }

    private static boolean assign(int ingredient, List<Ingredient> required, List<Offering> items,
                                  int[] owners, int[] assigned, boolean[] visited) {
        for (int item = 0; item < items.size(); item++) {
            if (visited[item] || !required.get(ingredient).test(items.get(item).stack())) {
                continue;
            }
            visited[item] = true;
            if (owners[item] < 0 || assign(owners[item], required, items, owners, assigned, visited)) {
                owners[item] = ingredient;
                assigned[ingredient] = item;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(Container container, Level level) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.getItem(slot).isEmpty()) {
                stacks.add(container.getItem(slot));
            }
        }
        return RecipeMatcher.findMatches(stacks, sacrifices) != null;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= sacrifices.size();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.ITEM_RITE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return WitcheryRecipes.ITEM_RITE.get();
    }

    public static final class Serializer implements RecipeSerializer<ItemRiteRecipe> {
        private static final int MAX_SACRIFICES = 24;
        private static final Map<String, RitualCircle> PATTERNS = Map.of(
                "small", RitualCircle.SMALL_RITE, "medium", RitualCircle.MEDIUM_RITE, "large", RitualCircle.LARGE_RITE);

        @Override
        public ItemRiteRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray sacrifices = GsonHelper.getAsJsonArray(json, "sacrifices");
            JsonArray ringData = GsonHelper.getAsJsonArray(json, "circles");
            int power = GsonHelper.getAsInt(json, "power", 0);
            if (sacrifices.size() > MAX_SACRIFICES || ringData.size() > PATTERNS.size() || power < 0) {
                throw new JsonSyntaxException("Invalid sacrifice, circle or power requirement for " + id);
            }

            List<Ingredient> ingredients = new ArrayList<>();
            sacrifices.forEach(entry -> ingredients.add(Ingredient.fromJson(entry, false)));
            List<Ring> rings = new ArrayList<>();
            ringData.forEach(entry -> {
                JsonObject ring = GsonHelper.convertToJsonObject(entry, "circle");
                RitualCircle pattern = PATTERNS.get(GsonHelper.getAsString(ring, "size"));
                ResourceLocation glyphId = ResourceLocation.tryParse(GsonHelper.getAsString(ring, "glyph"));
                Block glyph = glyphId == null ? null : BuiltInRegistries.BLOCK.getOptional(glyphId).orElse(null);
                if (pattern == null || !(glyph instanceof RitualGlyphBlock)
                        || rings.stream().anyMatch(existing -> existing.pattern == pattern)) {
                    throw new JsonSyntaxException("Invalid circle in " + id);
                }
                rings.add(new Ring(pattern, glyph));
            });
            Action action = switch (GsonHelper.getAsString(json, "action", "item")) {
                case "item" -> Action.ITEM;
                case "manifestation" -> Action.MANIFESTATION;
                case "summon_demon" -> Action.SUMMON_DEMON;
                case "banish_demon" -> Action.BANISH_DEMON;
                case "summon_wither" -> Action.SUMMON_WITHER;
                case "summon_witch" -> Action.SUMMON_WITCH;
                case "summon_spectral_familiar" -> Action.SUMMON_SPECTRAL_FAMILIAR;
                case "summon_imp" -> Action.SUMMON_IMP;
                case "summon_reflection" -> Action.SUMMON_REFLECTION;
                case "glyphic_transformation" -> Action.GLYPHIC_TRANSFORMATION;
                case "forestation" -> Action.FORESTATION;
                case "raise_volcano" -> Action.RAISE_VOLCANO;
                case "raise_portable_volcano" -> Action.RAISE_PORTABLE_VOLCANO;
                case "cook_food" -> Action.COOK_FOOD;
                case "cook_food_portable" -> Action.COOK_FOOD_PORTABLE;
                case "blight" -> Action.BLIGHT;
                case "area_blindness" -> Action.AREA_BLINDNESS;
                case "hell_on_earth" -> Action.HELL_ON_EARTH;
                case "rain_toads" -> Action.RAIN_TOADS;
                case "prior_incarnation" -> Action.PRIOR_INCARNATION;
                case "part_earth" -> Action.PART_EARTH;
                case "raise_earth" -> Action.RAISE_EARTH;
                case "raise_earth_medium" -> Action.RAISE_EARTH_MEDIUM;
                case "raise_earth_large" -> Action.RAISE_EARTH_LARGE;
                case "ice_shell" -> Action.ICE_SHELL;
                case "climate_change" -> Action.CLIMATE_CHANGE;
                case "fertility" -> Action.FERTILITY;
                case "natures_power" -> Action.NATURES_POWER;
                case "call_beasts" -> Action.CALL_BEASTS;
                case "infuse_overworld" -> Action.INFUSE_OVERWORLD;
                case "recharge_infusion" -> Action.RECHARGE_INFUSION;
                case "infuse_infernal" -> Action.INFUSE_INFERNAL;
                case "infuse_light" -> Action.INFUSE_LIGHT;
                case "infuse_otherwhere" -> Action.INFUSE_OTHERWHERE;
                case "corrupt_voodoo_protection" -> Action.CORRUPT_VOODOO_PROTECTION;
                case "curse_waking_nightmare" -> Action.CURSE_WAKING_NIGHTMARE;
                case "cure_waking_nightmare" -> Action.CURE_WAKING_NIGHTMARE;
                case "curse_misfortune" -> Action.CURSE_MISFORTUNE;
                case "cure_misfortune" -> Action.CURE_MISFORTUNE;
                case "curse_insanity" -> Action.CURSE_INSANITY;
                case "cure_insanity" -> Action.CURE_INSANITY;
                case "curse_sinking" -> Action.CURSE_SINKING;
                case "cure_sinking" -> Action.CURE_SINKING;
                case "curse_overheating" -> Action.CURSE_OVERHEATING;
                case "cure_overheating" -> Action.CURE_OVERHEATING;
                case "bind_familiar" -> Action.BIND_FAMILIAR;
                case "call_familiar" -> Action.CALL_FAMILIAR;
                case "bind_waystone" -> Action.BIND_WAYSTONE;
                case "copy_waystone" -> Action.COPY_WAYSTONE;
                case "bind_circle_talisman" -> Action.BIND_CIRCLE_TALISMAN;
                case "bind_creature_waystone" -> Action.BIND_CREATURE_WAYSTONE;
                case "travel_waystone" -> Action.TRAVEL_WAYSTONE;
                case "cure_vampirism" -> Action.CURE_VAMPIRISM;
                case "curse_of_the_wolf" -> Action.CURSE_OF_THE_WOLF;
                case "cure_lycanthropy" -> Action.CURE_LYCANTHROPY;
                case "transpose_creature" -> Action.TRANSPOSE_CREATURE;
                case "transpose_ores" -> Action.TRANSPOSE_ORES;
                case "bind_spectral_stone" -> Action.BIND_SPECTRAL_STONE;
                case "bind_fetish" -> Action.BIND_FETISH;
                case "bind_worship_statue" -> Action.BIND_WORSHIP_STATUE;
                case "call_storm" -> Action.CALL_STORM;
                case "call_greater_storm" -> Action.CALL_GREATER_STORM;
                case "call_portable_storm" -> Action.CALL_PORTABLE_STORM;
                case "eclipse" -> Action.ECLIPSE;
                case "protection" -> Action.PROTECTION;
                case "imprisonment" -> Action.IMPRISONMENT;
                case "barrier" -> Action.BARRIER;
                case "greater_barrier" -> Action.GREATER_BARRIER;
                case "portable_barrier" -> Action.PORTABLE_BARRIER;
                default -> throw new JsonSyntaxException("Unknown rite action for " + id);
            };
            ItemStack result = action == Action.ITEM
                    ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")) : ItemStack.EMPTY;
            EntityType<?> living = json.has("living_offering")
                    ? EntityType.byString(GsonHelper.getAsString(json, "living_offering")).orElseThrow(
                            () -> new JsonSyntaxException("Unknown living offering for " + id)) : null;
            return new ItemRiteRecipe(id, List.copyOf(ingredients), List.copyOf(rings), power, result,
                    action, GsonHelper.getAsBoolean(json, "night_only", false), living,
                    GsonHelper.getAsBoolean(json, "consume_necromantic_stone", false));
        }

        @Override
        public ItemRiteRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int ingredientCount = buffer.readVarInt();
            if (ingredientCount < 0 || ingredientCount > MAX_SACRIFICES) {
                throw new IllegalArgumentException("Invalid rite sacrifice count");
            }
            List<Ingredient> sacrifices = new ArrayList<>();
            for (int i = 0; i < ingredientCount; i++) {
                sacrifices.add(Ingredient.fromNetwork(buffer));
            }
            int ringCount = buffer.readVarInt();
            if (ringCount < 0 || ringCount > PATTERNS.size()) {
                throw new IllegalArgumentException("Invalid rite circle count");
            }
            List<Ring> rings = new ArrayList<>();
            for (int i = 0; i < ringCount; i++) {
                rings.add(new Ring(buffer.readEnum(RitualCircle.class), BuiltInRegistries.BLOCK.get(buffer.readResourceLocation())));
            }
            return new ItemRiteRecipe(id, List.copyOf(sacrifices), List.copyOf(rings), buffer.readVarInt(), buffer.readItem(),
                    buffer.readEnum(Action.class), buffer.readBoolean(),
                    buffer.readNullable(input -> BuiltInRegistries.ENTITY_TYPE.get(input.readResourceLocation())),
                    buffer.readBoolean());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ItemRiteRecipe recipe) {
            buffer.writeVarInt(recipe.sacrifices.size());
            recipe.sacrifices.forEach(ingredient -> ingredient.toNetwork(buffer));
            buffer.writeVarInt(recipe.rings.size());
            for (Ring ring : recipe.rings) {
                buffer.writeEnum(ring.pattern);
                buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(ring.glyph));
            }
            buffer.writeVarInt(recipe.power);
            buffer.writeItem(recipe.result);
            buffer.writeEnum(recipe.action);
            buffer.writeBoolean(recipe.nightOnly);
            buffer.writeNullable(recipe.livingOffering, (output, type) -> output.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(type)));
            buffer.writeBoolean(recipe.consumeNecromanticStone);
        }
    }
}
