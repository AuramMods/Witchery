package com.emoniph.witchery.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.item.ItemStack;

public class SpinningRecipes {
   private static final SpinningRecipes INSTANCE = new SpinningRecipes();
   public final ArrayList<SpinningRecipes.SpinningRecipe> recipes = new ArrayList();

   public static SpinningRecipes instance() {
      return INSTANCE;
   }

   private SpinningRecipes() {
   }

   public SpinningRecipes.SpinningRecipe addRecipe(ItemStack result, ItemStack fibre, ItemStack... modifiers) {
      SpinningRecipes.SpinningRecipe recipe = new SpinningRecipes.SpinningRecipe(result, fibre, modifiers);
      this.recipes.add(recipe);
      return recipe;
   }

   public SpinningRecipes.SpinningRecipe getRecipe(ItemStack fibre, ItemStack[] modifiers) {
      Iterator i$ = this.recipes.iterator();

      SpinningRecipes.SpinningRecipe recipe;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         recipe = (SpinningRecipes.SpinningRecipe)i$.next();
      } while(!recipe.isMatch(fibre, modifiers));

      return recipe;
   }

   public SpinningRecipes.SpinningRecipe findRecipeFor(ItemStack result) {
      Iterator i$ = this.recipes.iterator();

      SpinningRecipes.SpinningRecipe recipe;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         recipe = (SpinningRecipes.SpinningRecipe)i$.next();
      } while(!recipe.getResult().func_77969_a(result));

      return recipe;
   }

   public SpinningRecipes.SpinningRecipe findRecipeUsing(ItemStack ingredient) {
      Iterator i$ = this.recipes.iterator();

      SpinningRecipes.SpinningRecipe recipe;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         recipe = (SpinningRecipes.SpinningRecipe)i$.next();
      } while(!recipe.uses(ingredient));

      return recipe;
   }

   public SpinningRecipes.SpinningRecipe findRecipeUsingFibre(ItemStack ingredient) {
      Iterator i$ = this.recipes.iterator();

      SpinningRecipes.SpinningRecipe recipe;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         recipe = (SpinningRecipes.SpinningRecipe)i$.next();
      } while(!recipe.fibre.func_77969_a(ingredient));

      return recipe;
   }

   public static class SpinningRecipe {
      public final ItemStack fibre;
      public final ItemStack[] modifiers;
      public final ItemStack result;

      private SpinningRecipe(ItemStack result, ItemStack fibre, ItemStack[] modifiers) {
         this.fibre = fibre;
         this.result = result;
         this.modifiers = modifiers;
      }

      public ItemStack getResult() {
         return this.result;
      }

      public ArrayList<ItemStack> getMutableModifiersList() {
         ArrayList<ItemStack> available = new ArrayList();
         ItemStack[] arr$ = this.modifiers;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ItemStack item = arr$[i$];
            if (item != null) {
               available.add(item);
            }
         }

         return available;
      }

      private boolean isMatch(ItemStack fibre, ItemStack[] modifiers) {
         if (fibre != null && fibre.func_77969_a(this.fibre) && fibre.field_77994_a >= this.fibre.field_77994_a) {
            ArrayList<ItemStack> available = new ArrayList();
            ItemStack[] arr$ = modifiers;
            int len$ = modifiers.length;

            int i$;
            ItemStack modifier;
            for(i$ = 0; i$ < len$; ++i$) {
               modifier = arr$[i$];
               if (modifier != null) {
                  available.add(modifier);
               }
            }

            arr$ = this.modifiers;
            len$ = arr$.length;

            for(i$ = 0; i$ < len$; ++i$) {
               modifier = arr$[i$];
               int index = this.indexOf(available, modifier);
               if (index == -1) {
                  return false;
               }

               available.remove(index);
            }

            return true;
         } else {
            return false;
         }
      }

      private int indexOf(ArrayList<ItemStack> list, ItemStack item) {
         for(int i = 0; i < list.size(); ++i) {
            if (((ItemStack)list.get(i)).func_77969_a(item)) {
               return i;
            }
         }

         return -1;
      }

      public boolean uses(ItemStack ingredient) {
         if (ingredient == null) {
            return false;
         } else if (this.fibre.func_77969_a(ingredient)) {
            return true;
         } else {
            ItemStack[] arr$ = this.modifiers;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               ItemStack item = arr$[i$];
               if (item != null && item.func_77969_a(ingredient)) {
                  return true;
               }
            }

            return false;
         }
      }

      // $FF: synthetic method
      SpinningRecipe(ItemStack x0, ItemStack x1, ItemStack[] x2, Object x3) {
         this(x0, x1, x2);
      }
   }
}
