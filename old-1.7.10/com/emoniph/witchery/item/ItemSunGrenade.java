package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.EntityGrenade;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemSunGrenade extends ItemBase {
   private final int mode;
   @SideOnly(Side.CLIENT)
   protected IIcon itemIconOverlay;

   public ItemSunGrenade(int mode) {
      this.mode = mode;
      this.func_77625_d(16);
      this.func_77656_e(0);
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean moreTips) {
      super.func_77624_a(stack, player, list, moreTips);
      if (this.mode == 1) {
         list.add(String.format(Witchery.resource("item.witchery:dupgrenade.tip"), getOwnerName(stack)));
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean hasEffect(ItemStack stack, int pass) {
      return pass == 0;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_77623_v() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public IIcon getIcon(ItemStack stack, int pass) {
      return pass == 0 ? this.itemIconOverlay : this.field_77791_bV;
   }

   @SideOnly(Side.CLIENT)
   public void func_94581_a(IIconRegister iconRegister) {
      super.func_94581_a(iconRegister);
      this.itemIconOverlay = iconRegister.func_94245_a("witchery:ingredient.quartzSphere");
   }

   public EnumRarity func_77613_e(ItemStack stack) {
      return EnumRarity.uncommon;
   }

   public EnumAction func_77661_b(ItemStack stack) {
      return EnumAction.bow;
   }

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      if (!player.field_71075_bZ.field_75098_d) {
         --stack.field_77994_a;
      }

      world.func_72956_a(player, "random.bow", 0.5F, 0.4F / (field_77697_d.nextFloat() * 0.4F + 0.8F));
      if (!world.field_72995_K) {
         EntityGrenade grenade = new EntityGrenade(world, player, stack);
         grenade.setMode(this.mode);
         if (this.mode == 1) {
            grenade.setOwner(getOwnerName(stack));
         }

         world.func_72838_d(grenade);
      }

      return stack;
   }

   public static String getOwnerName(ItemStack stack) {
      if (stack.func_77942_o()) {
         NBTTagCompound nbtRoot = stack.func_77978_p();
         return nbtRoot.func_74779_i("Owner");
      } else {
         return null;
      }
   }

   public static void setOwnerName(ItemStack stack, String name) {
      if (!stack.func_77942_o()) {
         stack.func_77982_d(new NBTTagCompound());
      }

      NBTTagCompound nbtRoot = stack.func_77978_p();
      nbtRoot.func_74778_a("Owner", name);
   }
}
