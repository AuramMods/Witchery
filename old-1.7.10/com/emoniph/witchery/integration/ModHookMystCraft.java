package com.emoniph.witchery.integration;

import com.emoniph.witchery.Witchery;
import cpw.mods.fml.common.event.FMLInterModComms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

public class ModHookMystCraft extends ModHook {
   public String getModID() {
      return "Mystcraft";
   }

   protected void doInit() {
      this.removeMystCraftFluid(Witchery.Fluids.FLOWING_SPIRIT.getName());
      this.removeMystCraftFluid(Witchery.Fluids.HOLLOW_TEARS.getName());
      this.removeMystCraftFluid(Witchery.Fluids.BREW.getName());
      this.removeMystCraftFluid(Witchery.Fluids.BREW_LIQUID.getName());
      this.removeMystCraftFluid(Witchery.Fluids.BREW_GAS.getName());
   }

   private void removeMystCraftFluid(String fluid) {
      NBTTagCompound nbtRoot = new NBTTagCompound();
      nbtRoot.func_74782_a("fluidsymbol", new NBTTagCompound());
      NBTTagCompound nbtSymbol = nbtRoot.func_74775_l("fluidsymbol");
      nbtSymbol.func_74778_a("fluidname", fluid);
      nbtSymbol.func_74776_a("rarity", 0.0F);
      nbtSymbol.func_74776_a("grammarweight", 0.0F);
      nbtSymbol.func_74776_a("instabilityPerBlock", 10000.0F);
      FMLInterModComms.sendMessage(this.getModID(), "fluidsymbol", nbtRoot);
   }

   protected void doPostInit() {
   }

   protected void doReduceMagicPower(EntityLivingBase entity, float factor) {
   }
}
