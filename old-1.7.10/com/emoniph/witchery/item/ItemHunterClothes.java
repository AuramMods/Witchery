package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.client.model.ModelHunterClothes;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.ItemUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;

public class ItemHunterClothes extends ItemArmor implements ISpecialArmor {
   private boolean silvered;
   private boolean garlicked;
   @SideOnly(Side.CLIENT)
   private IIcon iconOverlaySilver;
   @SideOnly(Side.CLIENT)
   private IIcon iconOverlaySilverGarlic;
   @SideOnly(Side.CLIENT)
   private ModelHunterClothes modelClothesChest;
   @SideOnly(Side.CLIENT)
   private ModelHunterClothes modelClothesLegs;
   private static final String BIBLIOCRAFT_ARMOR_STAND_ENTITY_NAME = "AbstractSteve";

   public ItemHunterClothes(int armorSlot, boolean silvered, boolean garlicked) {
      super(ArmorMaterial.CLOTH, 1, armorSlot);
      this.silvered = silvered;
      this.garlicked = garlicked;
      this.func_77656_e(ArmorMaterial.IRON.func_78046_a(armorSlot));
      this.func_77637_a(WitcheryCreativeTab.INSTANCE);
   }

   public Item func_77655_b(String itemName) {
      ItemUtil.registerItem(this, itemName);
      return super.func_77655_b(itemName);
   }

   public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
      if (stack != null && this.field_77881_a == 2) {
         return "witchery:textures/entities/hunterclothes2" + (type == null ? "" : "_overlay") + ".png";
      } else {
         return stack != null ? "witchery:textures/entities/hunterclothes" + (type == null ? "" : "_overlay") + ".png" : null;
      }
   }

   public boolean func_82816_b_(ItemStack stack) {
      return true;
   }

   public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
      if (!world.field_72995_K && player.field_70173_aa % 20 == 2) {
         ExtendedPlayer playerEx = ExtendedPlayer.get(player);
         if (this.silvered && playerEx.getWerewolfLevel() > 0 || this.garlicked && playerEx.isVampire()) {
            player.func_70097_a(DamageSource.field_76372_a, 1.0F);
         }
      }

   }

   public int func_82814_b(ItemStack stack) {
      if (!this.func_82816_b_(stack)) {
         return super.func_82814_b(stack);
      } else {
         int color = super.func_82814_b(stack);
         if (color == 10511680) {
            if (stack.func_77973_b() == Witchery.Items.HUNTER_BOOTS) {
               color = 1642763;
            } else if (stack.func_77973_b() == Witchery.Items.HUNTER_LEGS) {
               color = 4798251;
            } else {
               color = 4139550;
            }
         }

         return color;
      }
   }

   @SideOnly(Side.CLIENT)
   public int func_82790_a(ItemStack stack, int par2) {
      return super.func_82790_a(stack, par2);
   }

   @SideOnly(Side.CLIENT)
   public boolean func_77623_v() {
      return this.silvered || this.garlicked;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_77618_c(int damage, int renderPass) {
      if (renderPass == 1) {
         return this.garlicked ? this.iconOverlaySilverGarlic : this.iconOverlaySilver;
      } else {
         return this.func_77617_a(damage);
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_94581_a(IIconRegister iconRegister) {
      super.func_94581_a(iconRegister);
      this.iconOverlaySilver = iconRegister.func_94245_a(this.func_111208_A() + "_silvered");
      this.iconOverlaySilverGarlic = iconRegister.func_94245_a(this.func_111208_A() + "_garlicked");
   }

   @SideOnly(Side.CLIENT)
   public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack stack, int armorSlot) {
      if (this.modelClothesChest == null) {
         this.modelClothesChest = new ModelHunterClothes(0.4F, false);
      }

      if (this.modelClothesLegs == null) {
         this.modelClothesLegs = new ModelHunterClothes(0.01F, false);
      }

      ModelBiped armorModel = null;
      if (stack != null && stack.func_77973_b() instanceof ItemHunterClothes) {
         int type = ((ItemArmor)stack.func_77973_b()).field_77881_a;
         if (type != 1 && type != 3) {
            armorModel = this.modelClothesLegs;
         } else {
            armorModel = this.modelClothesChest;
         }

         if (armorModel != null) {
            boolean isVisible = true;
            if (entityLiving != null && entityLiving.func_82150_aj()) {
               String entityTypeName = entityLiving.getClass().getSimpleName();
               isVisible = entityTypeName == null || entityTypeName.isEmpty() || entityTypeName.equals("AbstractSteve");
            }

            armorModel.field_78116_c.field_78806_j = isVisible && armorSlot == 0;
            armorModel.field_78114_d.field_78806_j = isVisible && armorSlot == 0;
            armorModel.field_78115_e.field_78806_j = isVisible && armorSlot == 1;
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

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack stack) {
      return EnumRarity.uncommon;
   }

   public String func_77653_i(ItemStack stack) {
      String baseName = super.func_77653_i(stack);
      return baseName;
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean advancedTooltips) {
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

   public static boolean isFullSetWorn(EntityLivingBase entity, boolean silvered) {
      for(int i = 1; i <= 4; ++i) {
         ItemStack item = entity.func_71124_b(i);
         if (item == null) {
            return false;
         }

         if (!(item.func_77973_b() instanceof ItemHunterClothes)) {
            return false;
         }

         ItemHunterClothes clothes = (ItemHunterClothes)item.func_77973_b();
         if (silvered && !clothes.silvered) {
            return false;
         }
      }

      return true;
   }

   public static boolean isMagicalProtectionActive(EntityLivingBase entity) {
      return entity != null && isFullSetWorn(entity, false) && entity.field_70170_p != null && entity.field_70170_p.field_73012_v.nextDouble() < 0.25D;
   }

   public static boolean isCurseProtectionActive(EntityLivingBase entity) {
      return entity != null && isFullSetWorn(entity, false) && entity.field_70170_p != null && entity.field_70170_p.field_73012_v.nextDouble() < 0.9D;
   }

   public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
      if (this.silvered && source != null && CreatureUtil.isWerewolf(source.func_76346_g())) {
         source.func_76346_g().func_70097_a(DamageSource.field_76372_a, 1.0F);
         return new ArmorProperties(0, (double)this.field_77879_b * 2.5D / 25.0D, armor.func_77958_k() + 1 - armor.func_77960_j());
      } else {
         return this.garlicked && source != null && CreatureUtil.isVampire(source.func_76346_g()) ? new ArmorProperties(0, (double)this.field_77879_b * 2.5D / 25.0D, armor.func_77958_k() + 1 - armor.func_77960_j()) : new ArmorProperties(0, (double)this.field_77879_b / 25.0D, armor.func_77958_k() + 1 - armor.func_77960_j());
      }
   }

   public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
      return this.field_77879_b;
   }

   public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
      if (!this.silvered || source == null || !CreatureUtil.isWerewolf(source.func_76346_g())) {
         if (!this.garlicked || source == null || !CreatureUtil.isVampire(source.func_76346_g())) {
            stack.func_77972_a(damage, entity);
         }
      }
   }

   public static boolean isWolfProtectionActive(EntityLivingBase entity) {
      return entity != null && isFullSetWorn(entity, true);
   }
}
