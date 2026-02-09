package art.arcane.witchery.data;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.function.Consumer;

public class WitcheryRecipeProvider extends RecipeProvider {
    public WitcheryRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> recipeOutput) {
        // Breadth-first scaffold: recipe migration is a later depth pass.
    }
}
