package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.FetishEffect;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.brewing.CauldronRitualRecipe;
import art.arcane.witchery.brewing.KettleRecipe;
import art.arcane.witchery.crafting.BrazierRecipe;
import art.arcane.witchery.crafting.DistillingRecipe;
import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.item.GuideBookItem.Volume;
import art.arcane.witchery.registry.WitcheryRecipes;
import art.arcane.witchery.ritual.ClimateChange;
import art.arcane.witchery.ritual.ItemRiteRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class GuideBookPages {
    private static final String KEY = "guide.witchery.";
    private static final Set<ResourceLocation> RESTORED_KETTLE_PAGES = Set.of(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "kettle/infernal_animus"),
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "kettle/spirit_of_otherwhere"));
    private static final List<String> PLANTS = List.of("belladonna:belladonna_stage_4", "embermoss", "glintweed",
            "mandrake:mandrake_stage_4", "snowbell:snowbell_stage_4", "spanishmoss", "wild_bramble:bramble_wild",
            "ender_bramble:bramble_ender", "voidbramble", "artichoke:artichoke_stage_4", "grassper:grasspericon",
            "crittersnare:crittersnare_empty", "bloodrose", "somniancotton", "wolfsbane:wolfsbane_stage_7",
            "garlic:garlic_stage_5", "alder:sapling_alder", "hawthorn:sapling_hawthorn", "rowan:sapling_rowan");

    public record Page(Component title, List<Component> text, List<ItemStack> icons, List<ItemRiteRecipe.Ring> circles,
                       @Nullable ResourceLocation picture, @Nullable ResourceLocation entryId) {
        public Page(Component title, List<Component> text, List<ItemStack> icons, List<ItemRiteRecipe.Ring> circles,
                    @Nullable ResourceLocation picture) {
            this(title, text, icons, circles, picture, null);
        }

        public Page(Component title, List<Component> text) {
            this(title, text, List.of(), List.of(), null);
        }
    }

    private GuideBookPages() {}

    public static List<Page> entries(Volume book, Level level, int knownSpells) {
        var pages = new ArrayList<Page>();
        if (book != Volume.ATLAS) pages.add(new Page(book.title(), List.of(Component.translatable(book.textKey("intro")))));
        switch (book) {
            case FUMES -> {
                pages.add(topic(book, "jars"));
                pages.add(topic(book, "saplings"));
            }
            case DISTILLING -> level.getRecipeManager().getAllRecipesFor(WitcheryRecipes.DISTILLING.get()).stream()
                    .sorted(Comparator.comparing(DistillingRecipe::id)).map(GuideBookPages::distilling).forEachOrdered(pages::add);
            case CIRCLES -> CircleBookIndex.chapters(level).stream().map(GuideBookPages::rite).forEachOrdered(pages::add);
            case INFUSIONS -> level.getRecipeManager().getAllRecipesFor(WitcheryRecipes.KETTLE.get()).stream()
                    .filter(recipe -> !hiddenKettle(recipe.id()))
                    // Existing books store page numbers, so the two restored pages follow the existing recipes.
                    .sorted(Comparator.comparing((KettleRecipe recipe) -> RESTORED_KETTLE_PAGES.contains(recipe.id()))
                            .thenComparing(KettleRecipe::id))
                    .map(GuideBookPages::kettle).forEachOrdered(pages::add);
            case HERBOLOGY -> {
                PLANTS.forEach(plant -> pages.add(plant(plant)));
                pages.add(new Page(text("plant.treefyd.title"), List.of(text("plant.treefyd")),
                        List.of(new ItemStack(WitcheryIngredient.TREEFYD_SEEDS.item())), List.of(), null));
            }
            case BIOMES -> climate(level, pages);
            case ATLAS -> BiomeBookIndex.pages(level.registryAccess()).stream().map(GuideBookPages::atlas).forEachOrdered(pages::add);
            case SYMBOLOGY -> Arrays.stream(SymbolSpell.values()).filter(spell -> spell.visibleInGuide(knownSpells))
                    .sorted(Comparator.comparingInt(SymbolSpell::id)).map(GuideBookPages::symbol).forEachOrdered(pages::add);
            case CONJURATION -> {
                for (BrazierRecipe recipe : BrazierRecipe.values()) {
                    if (recipe != BrazierRecipe.POLTERGEIST) pages.add(brazier(recipe));
                }
                pages.add(topic(book, "binding"));
                for (FetishEffect effect : FetishEffect.values()) {
                    if (effect != FetishEffect.DEATH) pages.add(fetish(effect));
                }
            }
            case CAULDRON -> {
                pages.add(topic(book, "delivery"));
                pages.add(topic(book, "rituals"));
                level.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get()).stream()
                        .sorted(Comparator.comparing(CauldronRitualRecipe::id)).map(GuideBookPages::cauldron).forEachOrdered(pages::add);
                BrewMixture.guideIngredients().stream().map(GuideBookPages::ingredient).forEachOrdered(pages::add);
            }
        }
        if (pages.isEmpty()) pages.add(new Page(book.title(), List.of(text("no_biomes"))));
        return List.copyOf(pages);
    }

    private static Page topic(Volume book, String key) {
        return new Page(Component.translatable(book.textKey(key + ".title")), List.of(Component.translatable(book.textKey(key))));
    }

    private static Page distilling(DistillingRecipe recipe) {
        var outputs = recipe.outputs().stream().filter(stack -> !stack.isEmpty()).toList();
        var steps = new ArrayList<>(inputs(recipe.getIngredients()));
        if (recipe.jars() > 0) steps.add(text("jars", recipe.jars()));
        for (var stack : outputs) steps.add(text("output", stack.getCount(), stack.getHoverName()));
        return new Page(outputs.get(0).getHoverName(), steps, outputs, List.of(), null);
    }

    private static Page rite(CircleBookIndex.Chapter chapter) {
        var recipe = chapter.recipe();
        var steps = new ArrayList<>(inputs(recipe.sacrifices()));
        if (!steps.isEmpty() && chapter.firstOfferings().size() > 1) {
            var choices = chapter.firstOfferings().stream().map(GuideBookPages::ingredientName).toList();
            steps.set(0, text("ingredient", ComponentUtils.formatList(choices, Component.literal(" / "))));
        }
        if (recipe.power() > 0) steps.add(text("power", recipe.power()));
        if (recipe.livingOffering() != null) steps.add(text("living_offering", recipe.livingOffering().getDescription()));
        if (recipe.nightOnly()) steps.add(text("night_only"));
        if (recipe.dayOnly()) steps.add(text("day_only"));
        if (recipe.action().inflictsDirectedCurse()) steps.add(text("storm_only"));
        if (recipe.action().directedCurse() != null) {
            if (recipe.action().inflictsDirectedCurse()) steps.add(text("directed_curse." + recipe.action().directedCurse().key()));
            else steps.add(text("directed_cure"));
        }
        if (recipe.action().targetsStormWithWaystone()) steps.add(text("storm_waystone"));
        if (recipe.action() == ItemRiteRecipe.Action.PART_EARTH) steps.add(text("broken_earth_heading"));
        if (recipe.action() == ItemRiteRecipe.Action.RAISE_EARTH) steps.add(text("moving_earth_waystone"));
        if (recipe.action().summonsCreature()) steps.add(text("summoning_clearance"));
        if (recipe.action() == ItemRiteRecipe.Action.BANISH_DEMON) steps.add(text("demon_banishment"));
        if (recipe.action() == ItemRiteRecipe.Action.SUMMON_SPECTRAL_FAMILIAR) steps.add(text("spectral_familiar_search"));
        if (recipe.action() == ItemRiteRecipe.Action.FERTILITY) steps.add(text("fertility_effect"));
        if (recipe.action() == ItemRiteRecipe.Action.NATURES_POWER) steps.add(text("natures_power_effect"));
        if (recipe.action() == ItemRiteRecipe.Action.CALL_BEASTS) steps.add(text("call_beasts_effect"));
        if (recipe.action() == ItemRiteRecipe.Action.BLIGHT) steps.add(text("blight_effect"));
        if (recipe.action() == ItemRiteRecipe.Action.AREA_BLINDNESS) steps.add(text("area_blindness_effect"));
        if (recipe.action() == ItemRiteRecipe.Action.HELL_ON_EARTH) {
            steps.add(text("hell_on_earth_effect"));
            steps.add(text("hell_on_earth_upkeep"));
        }
        if (recipe.action() == ItemRiteRecipe.Action.RAIN_TOADS) steps.add(text("toad_rain_effect"));
        if (recipe.action() == ItemRiteRecipe.Action.PRIOR_INCARNATION) steps.add(text("prior_incarnation_effect"));
        if (recipe.action().isVolcano()) {
            steps.add(text("volcano_lava"));
            if (recipe.action().usesWaystoneTarget()) steps.add(text("volcano_waystone"));
        }
        if (recipe.action().cooksFood()) {
            steps.add(text("cooking_effect"));
            steps.add(text("cooking_burn_chance", recipe.action() == ItemRiteRecipe.Action.COOK_FOOD_PORTABLE ? 16 : 8));
        }
        if (recipe.action() == ItemRiteRecipe.Action.BIND_WAYSTONE) steps.add(text("bind_waystone"));
        if (recipe.action() == ItemRiteRecipe.Action.COPY_WAYSTONE) steps.add(text("copy_waystone"));
        if (recipe.action() == ItemRiteRecipe.Action.BIND_CIRCLE_TALISMAN) steps.add(text("bind_circle_talisman"));
        var protection = recipe.action().protection();
        if (protection != null) {
            steps.add(text("protection_effect." + protection.name().toLowerCase(Locale.ROOT)));
            if (protection.powerPerSecond() > 0) steps.add(text("protection_upkeep", protection.powerPerSecond()));
            if (protection.acceptsWaystone()) steps.add(text("barrier_waystone"));
        }
        if (recipe.consumeNecromanticStone()) steps.add(text("necromantic_stone"));
        if (!recipe.result().isEmpty()) steps.add(text("output", recipe.result().getCount(), recipe.result().getHoverName()));
        return new Page(recipeTitle(chapter.id()), steps, List.of(), recipe.rings(), null, chapter.id());
    }

    private static boolean hiddenKettle(ResourceLocation id) {
        return id.getNamespace().equals(Witchery.MODID)
                && List.of("kettle/spirit_of_otherwhere_from_splash", "kettle/infernal_animus_from_splash",
                "kettle/brew_of_solid_rock", "kettle/brew_of_solid_sand", "kettle/brew_of_solid_sandstone",
                "kettle/brew_of_solid_erosion").contains(id.getPath());
    }

    private static Page kettle(KettleRecipe recipe) {
        var steps = new ArrayList<>(inputs(recipe.ingredients()));
        if (recipe.power() > 0) steps.add(text("power", recipe.power()));
        if (recipe.dimension() != null) steps.add(text("dimension", readable(recipe.dimension().getPath())));
        if (recipe.bottlingFamiliar() != null) steps.add(text("familiar", readable(recipe.bottlingFamiliar().name())));
        steps.add(text("output", recipe.result().getCount(), recipe.result().getHoverName()));
        return new Page(recipe.result().getHoverName(), steps, List.of(recipe.result()), List.of(), null);
    }

    private static Page plant(String spec) {
        String[] fields = spec.split(":");
        String key = "plant." + fields[0];
        var picture = ResourceLocation.fromNamespaceAndPath(Witchery.MODID,
                "textures/block/" + fields[fields.length - 1] + ".png");
        return new Page(text(key + ".title"), List.of(text(key)), List.of(), List.of(), picture);
    }

    private static void climate(Level level, List<Page> pages) {
        var registry = level.registryAccess().registryOrThrow(Registries.BIOME);
        for (var focus : ClimateChange.Focus.values()) {
            var choices = focus.candidates(registry);
            var item = new ItemStack(focus.focusItem());
            for (int start = 0; start < choices.size(); start += 8) {
                var lines = new ArrayList<Component>();
                lines.add(text("climate_focus", item.getHoverName()));
                for (int i = start; i < Math.min(choices.size(), start + 8); i++) {
                    lines.add(text("climate_variant", i + 1, biomeName(choices.get(i).unwrapKey().orElseThrow().location())));
                }
                pages.add(new Page(text("focus." + focus.name().toLowerCase(Locale.ROOT)), lines,
                        List.of(item), List.of(), null));
            }
        }
    }

    public static Page atlas(BiomeBookIndex.Entry entry) {
        var climate = entry.biome().getModifiedClimateSettings();
        var precipitation = entry.biome().getPrecipitationAt(BlockPos.ZERO);
        return new Page(biomeName(entry.id()), List.of(
                text("temperature", String.format(Locale.ROOT, "%.2f", climate.temperature())),
                text("downfall", String.format(Locale.ROOT, "%.2f", climate.downfall())),
                text("precipitation", text("weather." + precipitation.name().toLowerCase(Locale.ROOT))),
                text("weather.lightning", text(precipitation == net.minecraft.world.level.biome.Biome.Precipitation.RAIN ? "yes" : "no")),
                text("atlas_copy")));
    }

    private static Page symbol(SymbolSpell spell) {
        var lines = new ArrayList<Component>();
        lines.add(text("symbol_charge", spell.baseCost()));
        for (String pattern : spell.gestures()) {
            lines.add(text("symbol_gesture", spell.gestureLevel(pattern), String.join(" ", pattern.split(""))));
        }
        lines.add(text("symbol." + spell.name().toLowerCase(Locale.ROOT)));
        return new Page(spell.displayName(), lines);
    }

    private static Page brazier(BrazierRecipe recipe) {
        String key = "brazier." + recipe.name().toLowerCase(Locale.ROOT);
        return new Page(text(key + ".title"), List.of(text(key), text("burn_time", recipe.burnTicks() / 20),
                text(recipe.needsPower() ? "needs_altar" : "no_altar")),
                recipe.ingredients().stream().map(ItemStack::new).toList(), List.of(), null);
    }

    private static Page fetish(FetishEffect effect) {
        String key = "fetish." + effect.key;
        var lines = new ArrayList<Component>();
        lines.add(text(key));
        lines.add(text("spirit_counts", effect.spirits, effect.spectres, effect.banshees, effect.poltergeists));
        if (effect.radius > 0) lines.add(text("radius", effect.radius));
        return new Page(text(key + ".title"), lines);
    }

    private static Page cauldron(CauldronRitualRecipe recipe) {
        var lines = new ArrayList<Component>(inputs(recipe.ingredients()));
        if (!recipe.trigger().isEmpty()) lines.add(text("trigger", ingredientName(recipe.trigger())));
        lines.add(text("power", recipe.power()));
        if (recipe.summon() != null) lines.add(text("summon", recipe.summon().getDescription()));
        var title = recipe.result().isEmpty() ? recipeTitle(recipe.id()) : recipe.result().getHoverName();
        return new Page(title, lines, recipe.result().isEmpty() ? List.of() : List.of(recipe.result()), List.of(), null);
    }

    private static Page ingredient(BrewMixture.GuideIngredient entry) {
        var lines = new ArrayList<Component>(List.of(entry.effect(), text("power", entry.power())));
        if (entry.capacityCost() > 0) lines.add(text("capacity_cost", entry.capacityCost()));
        if (entry.reversed() != null) lines.add(text("reversed", entry.reversed()));
        return new Page(entry.stack().getHoverName(), lines, List.of(entry.stack()), List.of(), null);
    }

    private static List<Component> inputs(List<Ingredient> ingredients) {
        return ingredients.stream().map(GuideBookPages::ingredientName)
                .map(name -> (Component) text("ingredient", name)).toList();
    }

    private static Component ingredientName(Ingredient ingredient) {
        var choices = ingredient.getItems();
        if (choices.length == 0) return text("missing_ingredient");
        var name = ingredientStackName(choices[0]);
        for (int i = 1; i < Math.min(3, choices.length); i++) name.append(" / ").append(ingredientStackName(choices[i]));
        if (choices.length > 3) name.append(" …");
        return name;
    }

    private static MutableComponent ingredientStackName(ItemStack stack) {
        var name = stack.getHoverName().copy();
        if (!(stack.getItem() instanceof PotionItem)) return name;
        var effects = PotionUtils.getMobEffects(stack);
        if (effects.isEmpty()) return name;

        var effect = effects.get(0);
        if (effect.getAmplifier() > 0) {
            name.append(" ").append(Component.translatable("potion.potency." + effect.getAmplifier()));
        }
        if (effect.getDuration() > 20) {
            name.append(" (").append(MobEffectUtil.formatDuration(effect, 1)).append(")");
        }
        return name;
    }

    private static Component recipeTitle(ResourceLocation id) {
        String path = id.getPath();
        return Component.translatableWithFallback(KEY + "recipe." + path.replace('/', '.'), readable(path.substring(path.lastIndexOf('/') + 1)));
    }

    private static String readable(String id) {
        return Arrays.stream(id.toLowerCase(Locale.ROOT).split("_"))
                .map(part -> part.isEmpty() ? part : Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private static Component biomeName(ResourceLocation id) {
        return Component.translatableWithFallback(id.toLanguageKey("biome"), readable(id.getPath()));
    }

    private static Component text(String key, Object... args) {
        return Component.translatable(KEY + key, args);
    }
}
