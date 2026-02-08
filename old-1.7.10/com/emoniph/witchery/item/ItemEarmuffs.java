package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.client.model.ModelEarmuffs;
import com.emoniph.witchery.util.ItemUtil;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;

public class ItemEarmuffs extends ItemArmor {
   @SideOnly(Side.CLIENT)
   private ModelEarmuffs modelClothesChest;
   @SideOnly(Side.CLIENT)
   private ModelEarmuffs modelClothesLegs;

   public ItemEarmuffs(int armorSlot) {
      super(ArmorMaterial.CLOTH, 1, armorSlot);
      this.func_77656_e(ArmorMaterial.CLOTH.func_78046_a(armorSlot));
      this.func_77637_a(WitcheryCreativeTab.INSTANCE);
   }

   public Item func_77655_b(String itemName) {
      ItemUtil.registerItem(this, itemName);
      return super.func_77655_b(itemName);
   }

   public boolean func_82816_b_(ItemStack stack) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public int func_82790_a(ItemStack stack, int par2) {
      return super.func_82790_a(stack, par2);
   }

   @SideOnly(Side.CLIENT)
   public boolean func_77623_v() {
      return false;
   }

   public int func_82814_b(ItemStack stack) {
      return !this.func_82816_b_(stack) ? 16777215 : super.func_82814_b(stack);
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack itemstack) {
      return EnumRarity.common;
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean advancedTooltips) {
      if (stack != null) {
         String localText = Witchery.resource(this.func_77658_a() + ".tip");
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

   public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
      if (stack != null) {
         return type == null ? "witchery:textures/entities/earmuffs.png" : "witchery:textures/entities/empty64x64_overlay.png";
      } else {
         return null;
      }
   }

   @SideOnly(Side.CLIENT)
   public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack stack, int armorSlot) {
      if (this.modelClothesChest == null) {
         this.modelClothesChest = new ModelEarmuffs();
      }

      ModelBiped armorModel = null;
      if (stack != null && stack.func_77973_b() instanceof ItemArmor) {
         int type = ((ItemArmor)stack.func_77973_b()).field_77881_a;
         if (type != 2) {
            armorModel = this.modelClothesChest;
         } else {
            armorModel = this.modelClothesChest;
         }

         if (armorModel != null) {
            boolean isVisible = true;
            armorModel.field_78116_c.field_78806_j = isVisible && armorSlot == 0;
            armorModel.field_78114_d.field_78806_j = isVisible && armorSlot == 0;
            armorModel.field_78115_e.field_78806_j = isVisible && (armorSlot == 1 || armorSlot == 2);
            armorModel.field_78112_f.field_78806_j = isVisible && armorSlot == 1;
            armorModel.field_78113_g.field_78806_j = isVisible && armorSlot == 1;
            armorModel.field_78123_h.field_78806_j = isVisible && (armorSlot == 3 || armorSlot == 2);
            armorModel.field_78124_i.field_78806_j = isVisible && (armorSlot == 3 || armorSlot == 2);
            armorModel.field_78117_n = entityLiving.func_70093_af();
            armorModel.field_78093_q = entityLiving.func_70115_ae();
            armorModel.field_78091_s = entityLiving.func_70631_g_();
            ItemStack heldStack = entityLiving.func_71124_b(0);
            armorModel.field_78120_m = heldStack != null ? 1 : 0;
            armorModel.field_78118_o = false;
            if (entityLiving instanceof EntityPlayer && heldStack != null && ((EntityPlayer)entityLiving).func_71057_bx() > 0) {
               EnumAction enumaction = heldStack.func_77975_n();
               if (enumaction == EnumAction.block) {
                  armorModel.field_78120_m = 3;
               }

               armorModel.field_78118_o = enumaction == EnumAction.bow;
            }

            return armorModel;
         }
      }

      return null;
   }

   public static boolean isHelmWorn(EntityPlayer entity) {
      ItemStack currentArmor = entity.func_82169_q(3);
      return currentArmor != null && currentArmor.func_77973_b() == Witchery.Items.EARMUFFS;
   }

   public static class ClientEventHooks {
      @SubscribeEvent
      public void onSound(PlaySoundEvent17 event) {
         Minecraft MC = Minecraft.func_71410_x();
         EntityPlayer player = MC.field_71439_g;
         if (player != null && ItemEarmuffs.isHelmWorn(player)) {
            event.result = null;
         }

      }
   }
}
