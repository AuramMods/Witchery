package com.emoniph.witchery.brewing;

import com.emoniph.witchery.item.ItemBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

public class ItemBrewBucket extends ItemBase {
   @SideOnly(Side.CLIENT)
   private IIcon overlayIcon;

   public ItemBrewBucket() {
      this.func_77625_d(1);
      this.func_77656_e(0);
      this.registerWithCreativeTab = false;
   }

   @SideOnly(Side.CLIENT)
   public int func_82790_a(ItemStack stack, int pass) {
      if (pass == 0) {
         NBTTagCompound nbtRoot = stack.func_77978_p();
         int color = nbtRoot != null ? nbtRoot.func_74762_e("Color") : -16744448;
         return color;
      } else {
         return super.func_82790_a(stack, pass);
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_94581_a(IIconRegister iconRegister) {
      super.func_94581_a(iconRegister);
      this.overlayIcon = iconRegister.func_94245_a(this.func_111208_A() + "_overlay");
   }

   @SideOnly(Side.CLIENT)
   public boolean func_77623_v() {
      return true;
   }

   public IIcon func_77618_c(int damage, int pass) {
      return pass == 0 ? this.overlayIcon : this.field_77791_bV;
   }

   public boolean hasEffect(ItemStack par1ItemStack, int pass) {
      return pass == 0;
   }
}
