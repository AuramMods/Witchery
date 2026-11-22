package art.arcane.witchery.crafting;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryCommonConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public enum ProtectionRecipeCondition implements ICondition {
    GODDESS_STATUE("statuegoddess", WitcheryCommonConfig.ALLOW_GODDESS_STATUE_RECIPE),
    VOID_BRAMBLE("voidbramble", WitcheryCommonConfig.ALLOW_VOID_BRAMBLE_RECIPE);

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "protection_recipe_enabled");
    private final String recipe;
    private final ForgeConfigSpec.BooleanValue enabled;

    ProtectionRecipeCondition(String recipe, ForgeConfigSpec.BooleanValue enabled) {
        this.recipe = recipe;
        this.enabled = enabled;
    }

    public ResourceLocation recipeId() {
        return ResourceLocation.fromNamespaceAndPath(Witchery.MODID, recipe);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return enabled.get();
    }

    public static final class Serializer implements IConditionSerializer<ProtectionRecipeCondition> {
        @Override
        public void write(JsonObject json, ProtectionRecipeCondition condition) {
            json.addProperty("recipe", condition.recipe);
        }

        @Override
        public ProtectionRecipeCondition read(JsonObject json) {
            String recipe = GsonHelper.getAsString(json, "recipe");
            for (ProtectionRecipeCondition condition : values()) {
                if (condition.recipe.equals(recipe)) return condition;
            }
            throw new JsonSyntaxException("Unknown Witchery protection recipe " + recipe);
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
