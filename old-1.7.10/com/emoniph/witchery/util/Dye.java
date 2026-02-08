package com.emoniph.witchery.util;

import com.emoniph.witchery.brewing.BrewItemKey;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public enum Dye {
   INK_SAC(0, 1118481, "black"),
   ROSE_RED(1, 12464176, "red"),
   CACTUS_GREEN(2, 5732898, "green"),
   COCOA_BEANS(3, 5057301, "brown"),
   LAPIS_LAZULI(4, 2247599, "blue"),
   PURPLE_DYE(5, 8532146, "purple"),
   CYAN_DYE(6, 3968688, "cyan"),
   LIGHT_GRAY_DYE(7, 11513789, "lightgray"),
   GRAY_DYE(8, 7763574, "gray"),
   PINK_DYE(9, 15574987, "pink"),
   LIME_DYE(10, 8639516, "lime"),
   DANDELION_YELLOW(11, 15197994, "yellow"),
   LIGHT_BLUE_DYE(12, 12179199, "lightblue"),
   MAGENTA_DYE(13, 14383829, "magenta"),
   ORANGE_DYE(14, 15113780, "orange"),
   BONE_MEAL(15, 16777215, "white");

   public final int damageValue;
   public final int rgb;
   public final String unlocalizedName;
   public static final Dye[] DYES = new Dye[]{INK_SAC, ROSE_RED, CACTUS_GREEN, COCOA_BEANS, LAPIS_LAZULI, PURPLE_DYE, CYAN_DYE, LIGHT_GRAY_DYE, GRAY_DYE, PINK_DYE, LIME_DYE, DANDELION_YELLOW, LIGHT_BLUE_DYE, MAGENTA_DYE, ORANGE_DYE, BONE_MEAL};

   private Dye(int damageValue, int rgb, String unlocalizedName) {
      this.damageValue = damageValue;
      this.rgb = rgb;
      this.unlocalizedName = unlocalizedName;
   }

   public ItemStack createStack() {
      return this.createStack(1);
   }

   public ItemStack createStack(int quantity) {
      return new ItemStack(Items.field_151100_aR, quantity, this.damageValue);
   }

   public static Dye fromStack(ItemStack potion) {
      return potion.func_77973_b() == Items.field_151100_aR && potion.func_77960_j() >= 0 && potion.func_77960_j() < DYES.length ? DYES[potion.func_77960_j()] : BONE_MEAL;
   }

   public BrewItemKey getBrewItemKey() {
      return new BrewItemKey(Items.field_151100_aR, this.damageValue);
   }
}
