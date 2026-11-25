package art.arcane.witchery.ritual;

import art.arcane.witchery.block.entity.VoidBrambleBlockEntity;
import art.arcane.witchery.world.WitcheryDimensions;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Map.entry;

public final class RiteActivation {
    public static final ResourceLocation TRAVEL_TO_LOCATION = id("rite_of_traveling_to_a_waystone");
    public static final ResourceLocation TRAVEL_TO_CREATURE = id("rite_of_traveling_to_a_creature_waystone");
    public static final String NULL_FIELD_FAILURE = "message.witchery.rite.null_field";

    private static final Map<String, Integer> LEGACY_ORDER = Map.ofEntries(
            entry("rite_of_binding_a_circle", 1), entry("rite_of_binding_a_waystone", 2), entry("rite_of_charging", 3),
            entry("rite_of_infusion_recharging", 4), entry(TRAVEL_TO_LOCATION.getPath(), 5),
            entry("rite_of_transposition_creature", 6), entry("rite_of_transposition_ores", 7),
            entry("rite_of_protection", 8), entry("rite_of_imprisonment", 9),
            entry("rite_of_the_barrier", 10), entry("rite_of_the_barrier_greater", 11),
            entry("rite_of_the_barrier_portable", 12), entry("rite_of_raising_a_volcano_portable", 13),
            entry("rite_of_the_skys_wrath", 14),
            entry("rite_of_the_skys_wrath_greater", 15), entry("rite_of_the_skys_wrath_portable", 16),
            entry("rite_of_total_eclipse", 17), entry("rite_of_total_eclipse_portable", 18),
            entry("rite_of_broken_earth", 19), entry("rite_of_moving_earth", 20),
            entry("rite_of_banishing_a_demon_portable", 21), entry("rite_of_banishing_a_demon", 22),
            entry("rite_of_demon_summoning", 23), entry("rite_of_demon_summoning_without_villager", 24),
            entry("rite_of_wither_summoning", 25), entry("rite_of_wither_summoning_without_villager", 26),
            entry("infusion_of_the_light", 27), entry("infusion_of_the_overworld", 28),
            entry("infusion_of_otherwhere", 29), entry("infusion_of_the_infernal", 30),
            entry("rite_of_infusing_the_broom", 31), entry("rite_of_necromantic_stone", 32),
            entry("rite_of_summoning_a_spectral_familiar", 33), entry("rite_of_copying_a_waystone", 34),
            entry("rite_of_fertility", 35), entry("rite_of_fertility_portable", 36),
            entry("curse_of_blight", 37), entry("curse_of_blindness", 38), entry("rite_of_hell_on_earth", 39),
            entry("rite_of_witch_summoning", 40), entry("rite_of_binding_a_waystone_portable", 41),
            entry("rite_of_copying_a_waystone_portable", 42), entry("rite_of_natures_power", 43),
            entry("rite_of_prior_incarnation", 44), entry("rite_of_binding_a_circle_portable", 45),
            entry("rite_of_moving_earth_medium", 46), entry("rite_of_moving_earth_large", 47),
            entry("curse_of_misfortune", 48), entry("cure_of_misfortune", 49),
            entry("rite_of_the_mystic_branch", 50), entry("rite_of_cooking", 51),
            entry("curse_of_insanity", 52), entry("cure_of_insanity", 53),
            entry("rite_of_binding_a_familiar", 54),
            entry("rite_of_calling_a_familiar", 55), entry("rite_of_corrupting_voodoo_protection", 56),
            entry("rite_of_the_crystal_ball", 57), entry("rite_of_cooking_portable", 58),
            entry("curse_of_sinking", 59), entry("cure_of_sinking", 60),
            entry("rite_of_the_seer_stone", 61), entry("curse_of_overheating", 62), entry("cure_of_overheating", 63),
            entry("rite_of_climate_change", 64), entry("rite_of_ice_shell", 65),
            entry(RainOfToads.RECIPE.getPath(), 66),
            entry("rite_of_glyphic_transformation", 67), entry("rite_of_calling_the_beasts", 68),
            entry("rite_of_manifestation", 69),
            entry("forestation/oak", 70), entry("forestation/spruce", 71), entry("forestation/birch", 72),
            entry("forestation/jungle", 73), entry("forestation/rowan", 74), entry("forestation/alder", 75),
            entry("forestation/hawthorn", 76), entry("rite_of_raising_a_volcano", 77),
            entry("curse_of_waking_nightmare", 78), entry("cure_of_waking_nightmare", 79),
            entry("rite_of_infusing_soaring", 80), entry("rite_of_infusing_grave", 81),
            entry("rite_of_necromancy_spectral_stone", 82), entry("rite_of_necromancy_bind_spectral_stone", 83),
            entry("rite_of_binding_scarecrow", 84), entry("rite_of_binding_trent", 85),
            entry("rite_of_binding_witchsladder", 86), entry("rite_of_imp_summoning", 87),
            entry("rite_of_binding_a_creature_waystone", 88), entry("rite_of_binding_a_creature_waystone_portable", 89),
            entry("rite_of_binding_a_worship_statue", 90), entry(TRAVEL_TO_CREATURE.getPath(), 91),
            entry("curse_of_the_wolf", 92), entry("cure_lycanthropy", 93), entry("remove_vampirism", 94),
            entry("rite_of_the_mirror", 95), entry("rite_of_summoning_a_reflection", 96));

    private static final Comparator<ItemRiteRecipe> ORDER = Comparator.comparingInt((ItemRiteRecipe rite) -> legacyId(rite.id()))
            .thenComparing(ItemRiteRecipe::id);

    private RiteActivation() {}

    public static int legacyId(ResourceLocation recipe) {
        return recipe.getNamespace().equals(Witchery.MODID)
                ? LEGACY_ORDER.getOrDefault(recipe.getPath(), Integer.MAX_VALUE) : Integer.MAX_VALUE;
    }

    @Nullable
    public static ResourceLocation legacyRecipeId(int legacyId) {
        for (var entry : LEGACY_ORDER.entrySet()) {
            if (entry.getValue() == legacyId) return id(entry.getKey());
        }
        return null;
    }

    public record Selection(List<ResourceLocation> rites, String failure) {}

    public static Selection select(ServerLevel level, BlockPos heart) {
        if (level.dimension().equals(WitcheryDimensions.DREAM) || VoidBrambleBlockEntity.preventsRite(level, heart)) {
            return new Selection(List.of(), NULL_FIELD_FAILURE);
        }
        var recipes = level.getRecipeManager().getAllRecipesFor(WitcheryRecipes.ITEM_RITE.get()).stream().sorted(ORDER).toList();
        var queue = new ArrayList<ResourceLocation>();
        Set<ItemRiteRecipe.OfferingSource> reserved = new HashSet<>();
        Predicate<ItemRiteRecipe.Offering> available = offering -> !reserved.contains(offering.source());
        String reason = "message.witchery.rite.unknown";
        for (ItemRiteRecipe rite : recipes) {
            if (!rite.hasCircles(level, heart)) continue;
            String failure = rite.startFailure(level);
            if (failure != null && !queue.isEmpty()) continue;
            var offerings = rite.activationOfferings(level, heart, available);
            if (offerings == null) continue;
            if (failure != null) {
                reason = failure;
                continue;
            }
            offerings.forEach(offering -> reserved.add(offering.source()));
            if (rite.action().acceptsOptionalWaystone()) {
                rite.activationWaystones(level, heart, available).forEach(stone -> reserved.add(stone.source()));
            }
            queue.add(rite.id());
        }
        return new Selection(List.copyOf(queue), reason);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Witchery.MODID, path);
    }
}
