package com.emoniph.witchery.brewing;

import com.emoniph.witchery.item.ItemBase;
import com.emoniph.witchery.util.EntityPosition;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemBrew extends ItemBase {
   @SideOnly(Side.CLIENT)
   protected IIcon itemIconOverlay;
   @SideOnly(Side.CLIENT)
   protected IIcon itemIconSplash;

   public ItemBrew() {
      this.func_77625_d(8);
      this.func_77627_a(true);
      this.func_77656_e(0);
      this.registerWithCreativeTab = false;
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
      if (pass == 0) {
         return this.itemIconOverlay;
      } else {
         return stack != null && !WitcheryBrewRegistry.INSTANCE.isSplash(stack.func_77978_p()) ? this.field_77791_bV : this.itemIconSplash;
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_94581_a(IIconRegister iconRegister) {
      super.func_94581_a(iconRegister);
      this.itemIconOverlay = iconRegister.func_94245_a("witchery:brew_overlay");
      this.itemIconSplash = iconRegister.func_94245_a("witchery:brew_splash");
   }

   @SideOnly(Side.CLIENT)
   public String func_77653_i(ItemStack stack) {
      NBTTagCompound nbtRoot = stack.func_77978_p();
      return nbtRoot != null ? nbtRoot.func_74779_i("BrewName") : super.func_77653_i(stack);
   }

   @SideOnly(Side.CLIENT)
   public int func_82790_a(ItemStack stack, int pass) {
      if (pass == 0) {
         NBTTagCompound nbtRoot = stack.func_77978_p();
         return WitcheryBrewRegistry.INSTANCE.getBrewColor(nbtRoot);
      } else {
         return super.func_82790_a(stack, pass);
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean expanded) {
      NBTTagCompound nbtRoot = stack.func_77978_p();
      if (nbtRoot != null) {
         String localText = nbtRoot.func_74779_i("BrewInfo");
         if (localText != null) {
            String[] arr$ = localText.split("\n");
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               String s = arr$[i$];
               if (!s.isEmpty()) {
                  list.add(s);
               }
            }
         }
      }

   }

   public EnumRarity func_77613_e(ItemStack stack) {
      return EnumRarity.common;
   }

   public int func_77626_a(ItemStack stack) {
      int DEFAULT_SPEED = true;
      NBTTagCompound nbtRoot = stack.func_77978_p();
      int drinkSpeed = nbtRoot != null ? nbtRoot.func_74762_e("BrewDrinkSpeed") : 32;
      return drinkSpeed > 0 ? drinkSpeed : 32;
   }

   public EnumAction func_77661_b(ItemStack stack) {
      return WitcheryBrewRegistry.INSTANCE.isSplash(stack.func_77978_p()) ? EnumAction.bow : EnumAction.drink;
   }

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      if (WitcheryBrewRegistry.INSTANCE.isSplash(stack.func_77978_p())) {
         if (!player.field_71075_bZ.field_75098_d) {
            --stack.field_77994_a;
         }

         world.func_72956_a(player, "random.bow", 0.5F, 0.4F / (field_77697_d.nextFloat() * 0.4F + 0.8F));
         if (!world.field_72995_K) {
            world.func_72838_d(new EntityBrew(world, player, stack, false));
         }
      } else {
         player.func_71008_a(stack, this.func_77626_a(stack));
      }

      return stack;
   }

   public ItemStack func_77654_b(ItemStack stack, World world, EntityPlayer player) {
      if (!player.field_71075_bZ.field_75098_d) {
         --stack.field_77994_a;
      }

      if (!world.field_72995_K) {
         ModifiersEffect modifiers = new ModifiersEffect(1.0D, 1.0D, false, (EntityPosition)null, false, 0, player);
         WitcheryBrewRegistry.INSTANCE.applyToEntity(world, player, stack.func_77978_p(), modifiers);
      }

      if (!player.field_71075_bZ.field_75098_d) {
         if (stack.field_77994_a <= 0) {
            return new ItemStack(Items.field_151069_bo);
         }

         player.field_71071_by.func_70441_a(new ItemStack(Items.field_151069_bo));
      }

      return stack;
   }
}
