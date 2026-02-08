package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.client.model.ModelGoblinClothes;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.common.ServerTickEvents;
import com.emoniph.witchery.network.PacketPushTarget;
import com.emoniph.witchery.util.ItemUtil;
import com.emoniph.witchery.util.TransformCreature;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;

public class ItemGoblinClothes extends ItemArmor {
   @SideOnly(Side.CLIENT)
   private ModelGoblinClothes modelClothesChest;
   @SideOnly(Side.CLIENT)
   private ModelGoblinClothes modelClothesLegs;

   public ItemGoblinClothes(int armorSlot) {
      super(armorSlot == 0 ? ArmorMaterial.IRON : ArmorMaterial.CLOTH, 1, armorSlot);
      this.func_77656_e(armorSlot == 0 ? ArmorMaterial.DIAMOND.func_78046_a(armorSlot) : ArmorMaterial.IRON.func_78046_a(armorSlot));
      this.func_77637_a(WitcheryCreativeTab.INSTANCE);
   }

   public Item func_77655_b(String itemName) {
      ItemUtil.registerItem(this, itemName);
      return super.func_77655_b(itemName);
   }

   public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
      double R2;
      AxisAlignedBB bb2;
      List entities;
      Iterator i$;
      Object obj;
      if (!world.field_72995_K && world.func_82737_E() % 100L == 0L) {
         R2 = 8.0D;
         bb2 = AxisAlignedBB.func_72330_a(player.field_70165_t - 8.0D, player.field_70163_u - 8.0D, player.field_70161_v - 8.0D, player.field_70165_t + 8.0D, player.field_70163_u + 8.0D, player.field_70161_v + 8.0D);
         entities = world.func_72872_a(EntityPlayer.class, bb2);
         i$ = entities.iterator();

         while(i$.hasNext()) {
            obj = i$.next();
            EntityPlayer otherPlayer = (EntityPlayer)obj;
            if (player != otherPlayer && (isQuiverWorn(player) && isBeltWorn(otherPlayer) || isQuiverWorn(otherPlayer) && isBeltWorn(player))) {
               if (player.func_70644_a(Potion.field_76429_m)) {
                  player.func_82170_o(Potion.field_76429_m.field_76415_H);
               }

               player.func_70690_d(new PotionEffect(Potion.field_76429_m.field_76415_H, 200, 1));
               return;
            }
         }
      }

      if (!world.field_72995_K && isHelmWorn(player) && world.func_82737_E() % 5L == 1L) {
         R2 = 16.0D;
         bb2 = AxisAlignedBB.func_72330_a(player.field_70165_t - 16.0D, player.field_70163_u - 16.0D, player.field_70161_v - 16.0D, player.field_70165_t + 16.0D, player.field_70163_u + 16.0D, player.field_70161_v + 16.0D);
         entities = world.func_72872_a(EntityLivingBase.class, bb2);
         i$ = entities.iterator();

         while(i$.hasNext()) {
            obj = i$.next();
            EntityLivingBase otherPlayer = (EntityLivingBase)obj;
            if (player != otherPlayer && this.shouldAffectTarget(player, otherPlayer)) {
               if (otherPlayer instanceof EntityPlayer) {
                  double yawRadians = Math.atan2(otherPlayer.field_70161_v - player.field_70161_v, otherPlayer.field_70165_t - player.field_70161_v);
                  double yaw = Math.toDegrees(yawRadians) + 180.0D;
                  float rev = ((float)yaw + 90.0F) % 360.0F;
                  if (otherPlayer instanceof EntityPlayerMP) {
                     S08PacketPlayerPosLook packet = new S08PacketPlayerPosLook(otherPlayer.field_70165_t, otherPlayer.field_70163_u, otherPlayer.field_70161_v, rev, otherPlayer.field_70125_A, false);
                     Witchery.packetPipeline.sendTo((Packet)packet, (EntityPlayer)((EntityPlayerMP)otherPlayer));
                  }

                  if (!otherPlayer.func_70644_a(Potion.field_76438_s)) {
                     otherPlayer.func_70690_d(new PotionEffect(Potion.field_76438_s.field_76415_H, 100, 0));
                  }
               } else if (!otherPlayer.func_70644_a(Potion.field_76437_t)) {
                  otherPlayer.func_70690_d(new PotionEffect(Potion.field_76437_t.field_76415_H, 100, 0));
               }
            }
         }
      }

   }

   private boolean shouldAffectTarget(EntityLivingBase player, EntityLivingBase target) {
      ItemStack itemstack = target.func_71124_b(1);
      if (itemstack != null && itemstack.func_77973_b() == Item.func_150898_a(Blocks.field_150423_aK)) {
         return false;
      } else {
         Vec3 vec3 = target.func_70676_i(1.0F).func_72432_b();
         Vec3 vec31 = Vec3.func_72443_a(player.field_70165_t - target.field_70165_t, player.field_70121_D.field_72338_b + (double)(player.field_70131_O / 2.0F) - (target.field_70163_u + (double)target.func_70047_e()), player.field_70161_v - target.field_70161_v);
         double d0 = vec31.func_72433_c();
         vec31 = vec31.func_72432_b();
         double d1 = vec3.func_72430_b(vec31);
         return d1 > 1.0D - 0.025D / d0 && target.func_70685_l(player);
      }
   }

   public boolean hasEffect(ItemStack stack, int pass) {
      return super.hasEffect(stack, pass) || stack.func_77973_b() != Witchery.Items.KOBOLDITE_HELM;
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
      return super.field_77881_a != 0 ? EnumRarity.epic : EnumRarity.rare;
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
         if (slot == 0) {
            return "witchery:textures/entities/goblinclothes_head" + (type == null ? "" : "_overlay") + ".png";
         } else {
            return slot == 2 ? "witchery:textures/entities/goblinclothes_legs" + (type == null ? "" : "_overlay") + ".png" : "witchery:textures/entities/goblinclothes" + (type == null ? "" : "_overlay") + ".png";
         }
      } else {
         return null;
      }
   }

   @SideOnly(Side.CLIENT)
   public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack stack, int armorSlot) {
      if (this.modelClothesChest == null) {
         this.modelClothesChest = new ModelGoblinClothes(0.61F);
      }

      if (this.modelClothesLegs == null) {
         this.modelClothesLegs = new ModelGoblinClothes(0.0F);
      }

      ModelBiped armorModel = null;
      if (stack != null && stack.func_77973_b() instanceof ItemArmor) {
         int type = ((ItemArmor)stack.func_77973_b()).field_77881_a;
         if (type != 2) {
            armorModel = this.modelClothesChest;
         } else {
            armorModel = this.modelClothesLegs;
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

   private static boolean isQuiverWorn(EntityPlayer player) {
      ItemStack currentArmor = player.func_82169_q(2);
      return currentArmor != null && currentArmor.func_77973_b() == Witchery.Items.MOGS_QUIVER;
   }

   private static boolean isHelmWorn(EntityPlayer player) {
      ItemStack currentArmor = player.func_82169_q(3);
      return currentArmor != null && currentArmor.func_77973_b() == Witchery.Items.KOBOLDITE_HELM;
   }

   private static boolean isBeltWorn(EntityPlayer player) {
      ItemStack currentArmor = player.func_82169_q(1);
      return currentArmor != null && currentArmor.func_77973_b() == Witchery.Items.GULGS_GURDLE;
   }

   public static class EventHooks {
      @SubscribeEvent
      public void onLivingHurt(LivingHurtEvent event) {
         if (!event.entityLiving.field_70170_p.field_72995_K && !event.isCanceled()) {
            if (event.source.func_76352_a()) {
               if (event.source.func_76364_f() != null) {
                  boolean mogged = event.source.func_76364_f().getEntityData().func_74767_n("WITCMogged");
                  if (mogged) {
                     if (event.entityLiving.field_70160_al) {
                        event.ammount *= 3.0F;
                     }

                     if (event.entityLiving.func_70644_a(Potion.field_76437_t)) {
                        event.entityLiving.func_82170_o(Potion.field_76437_t.field_76415_H);
                     }

                     event.entityLiving.func_70690_d(new PotionEffect(Potion.field_76437_t.field_76415_H, 200, 0));
                  }
               }
            } else if (event.source.func_76355_l().equals("player")) {
               Entity source = event.source.func_76346_g();
               if (source != null && source instanceof EntityPlayer) {
                  EntityPlayer player = (EntityPlayer)source;
                  if (ItemGoblinClothes.isBeltWorn(player) && player.func_70694_bm() == null) {
                     event.ammount = 5.0F;
                     final EntityLivingBase entity = event.entityLiving;
                     if (entity instanceof EntityPlayer) {
                        Witchery.packetPipeline.sendTo((IMessage)(new PacketPushTarget(entity.field_70159_w, 1.0D, entity.field_70179_y)), (EntityPlayer)((EntityPlayer)entity));
                     } else {
                        ServerTickEvents.TASKS.add(new ServerTickEvents.ServerTickTask(player.field_70170_p) {
                           public boolean process() {
                              if (entity != null && !entity.field_70128_L) {
                                 entity.field_70181_x = 1.0D;
                              }

                              return true;
                           }
                        });
                     }
                  }
               }
            }
         }

      }

      @SubscribeEvent
      public void onArrowLoose(ArrowLooseEvent event) {
         if (!event.isCanceled() && ItemGoblinClothes.isQuiverWorn(event.entityPlayer)) {
            float f = (float)event.charge / 20.0F;
            f = (f * f + f * 2.0F) / 3.0F;
            if ((double)f < 0.1D) {
               return;
            }

            if (f > 1.0F) {
               f = 1.0F;
            }

            EntityArrow entityarrow = new EntityArrow(event.entityPlayer.field_70170_p, event.entityPlayer, f * 3.0F);
            entityarrow.getEntityData().func_74757_a("WITCMogged", true);
            if (f == 1.0F) {
               entityarrow.func_70243_d(true);
            }

            int k = EnchantmentHelper.func_77506_a(Enchantment.field_77345_t.field_77352_x, event.bow);
            if (k > 0) {
               entityarrow.func_70239_b(entityarrow.func_70242_d() + (double)k * 0.5D + 0.5D);
            }

            int l = EnchantmentHelper.func_77506_a(Enchantment.field_77344_u.field_77352_x, event.bow);
            if (l > 0) {
               entityarrow.func_70240_a(l);
            }

            if (EnchantmentHelper.func_77506_a(Enchantment.field_77343_v.field_77352_x, event.bow) > 0) {
               entityarrow.func_70015_d(100);
            }

            event.bow.func_77972_a(1, event.entityPlayer);
            event.entityPlayer.field_70170_p.func_72956_a(event.entityPlayer, "random.bow", 1.0F, 1.0F / (ItemGoblinClothes.field_77697_d.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
            entityarrow.field_70251_a = 2;
            if (!event.entityPlayer.field_70170_p.field_72995_K) {
               event.entityPlayer.field_70170_p.func_72838_d(entityarrow);
            }

            event.setCanceled(true);
         }

      }

      @SubscribeEvent
      public void onArrowNock(ArrowNockEvent event) {
         ExtendedPlayer playerEx = ExtendedPlayer.get(event.entityPlayer);
         if (playerEx.getCreatureType() != TransformCreature.NONE) {
            event.setCanceled(true);
         } else {
            if (!event.isCanceled() && ItemGoblinClothes.isQuiverWorn(event.entityPlayer)) {
               event.entityPlayer.func_71008_a(event.result, event.result.func_77973_b().func_77626_a(event.result));
            }

         }
      }
   }
}
