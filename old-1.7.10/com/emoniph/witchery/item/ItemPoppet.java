package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockPoppetShelf;
import com.emoniph.witchery.entity.EntityWitchHunter;
import com.emoniph.witchery.infusion.infusions.spirit.InfusedSpiritEffect;
import com.emoniph.witchery.network.PacketPushTarget;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ItemPoppet extends ItemBase {
   private final ArrayList<ItemPoppet.PoppetType> poppetTypes = new ArrayList();
   public final ItemPoppet.PoppetType unboundPoppet;
   public final ItemPoppet.PoppetType earthPoppet;
   public final ItemPoppet.PoppetType waterPoppet;
   public final ItemPoppet.PoppetType firePoppet;
   public final ItemPoppet.PoppetType foodPoppet;
   public final ItemPoppet.PoppetType toolPoppet;
   public final ItemPoppet.PoppetType deathPoppet;
   public final ItemPoppet.PoppetType antiVoodooPoppet;
   public final ItemPoppet.PoppetType voodooPoppet;
   public final ItemPoppet.PoppetType vampiricPoppet;
   public final ItemPoppet.PoppetType poppetProtectionPoppet;
   public final ItemPoppet.PoppetType armorPoppet;
   private static final String KEY_DAMAGE = "WITCDamage";
   private static final int MAX_DAMAGE = 1000;
   private static final float AUTO_REPAIR_THRESHOLD = 0.1F;

   public ItemPoppet() {
      this.unboundPoppet = ItemPoppet.PoppetType.register(new ItemPoppet.PoppetType(0, "poppet", "Poppet", 0), this.poppetTypes);
      this.earthPoppet = ItemPoppet.PoppetType.register((new ItemPoppet.PoppetType(1, "protectEarth", "Earth Protection Poppet")).setDestroyOnUse(true), this.poppetTypes);
      this.waterPoppet = ItemPoppet.PoppetType.register((new ItemPoppet.PoppetType(2, "protectWater", "Water Protection Poppet")).setDestroyOnUse(true), this.poppetTypes);
      this.firePoppet = ItemPoppet.PoppetType.register((new ItemPoppet.PoppetType(3, "protectFire", "Fire Protection Poppet")).setDestroyOnUse(true), this.poppetTypes);
      this.foodPoppet = ItemPoppet.PoppetType.register((new ItemPoppet.PoppetType(4, "protectStarvation", "Hunger Protection Poppet")).setDestroyOnUse(true), this.poppetTypes);
      this.toolPoppet = ItemPoppet.PoppetType.register((new ItemPoppet.PoppetType(5, "protectTool", "Tool Protection Poppet")).setDestroyOnUse(true), this.poppetTypes);
      this.deathPoppet = ItemPoppet.PoppetType.register((new ItemPoppet.PoppetType(6, "protectDeath", "Death Protection Poppet", 2)).setDestroyOnUse(true), this.poppetTypes);
      this.antiVoodooPoppet = ItemPoppet.PoppetType.register(new ItemPoppet.PoppetType(7, "protectVoodoo", "Voodoo Protection Poppet"), this.poppetTypes);
      this.voodooPoppet = ItemPoppet.PoppetType.register(new ItemPoppet.PoppetType(8, "voodoo", "Voodoo Poppet"), this.poppetTypes);
      this.vampiricPoppet = ItemPoppet.PoppetType.register(new ItemPoppet.PoppetType(9, "vampiric", "Vampiric Poppet", 2), this.poppetTypes);
      this.poppetProtectionPoppet = ItemPoppet.PoppetType.register(new ItemPoppet.PoppetType(10, "protectPoppet", "Poppet Protection", 2), this.poppetTypes);
      this.armorPoppet = ItemPoppet.PoppetType.register((new ItemPoppet.PoppetType(11, "protectArmor", "Armor Protection Poppet")).setDestroyOnUse(true), this.poppetTypes);
      this.setNoRepair();
      this.func_77625_d(1);
      this.func_77656_e(0);
      this.func_77627_a(true);
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack itemstack) {
      return EnumRarity.values()[((ItemPoppet.PoppetType)this.poppetTypes.get(itemstack.func_77960_j())).rarity];
   }

   public String func_77667_c(ItemStack itemStack) {
      int damage = itemStack.func_77960_j();

      assert damage >= 0 && damage < this.poppetTypes.size() : "damage value is too large";

      return damage >= 0 && damage < this.poppetTypes.size() ? ((ItemPoppet.PoppetType)this.poppetTypes.get(damage)).getUnlocalizedName() : "";
   }

   public String func_77653_i(ItemStack itemstack) {
      String sourceID;
      String targetID;
      if (this.vampiricPoppet.isMatch(itemstack)) {
         sourceID = Witchery.Items.TAGLOCK_KIT.getBoundEntityDisplayName(itemstack, 1);
         targetID = Witchery.Items.TAGLOCK_KIT.getBoundEntityDisplayName(itemstack, 2);
         String localizedName = super.func_77653_i(itemstack);
         if (!sourceID.isEmpty() && !targetID.isEmpty()) {
            return String.format("%s (%s -> %s)", localizedName, sourceID, targetID);
         } else if (!sourceID.isEmpty()) {
            return String.format("%s (%s -> ??)", localizedName, sourceID);
         } else {
            return !targetID.isEmpty() ? String.format("%s (?? -> %s)", localizedName, targetID) : localizedName;
         }
      } else {
         sourceID = Witchery.Items.TAGLOCK_KIT.getBoundEntityDisplayName(itemstack, 1);
         targetID = super.func_77653_i(itemstack);
         return !sourceID.isEmpty() ? String.format("%s (%s)", targetID, sourceID) : targetID;
      }
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean advTooltips) {
      String sourceID;
      if (this.vampiricPoppet.isMatch(stack)) {
         sourceID = Witchery.Items.TAGLOCK_KIT.getBoundEntityDisplayName(stack, 1);
         String targetID = Witchery.Items.TAGLOCK_KIT.getBoundEntityDisplayName(stack, 2);
         super.func_77653_i(stack);
         if (!sourceID.isEmpty() && !targetID.isEmpty()) {
            list.add(String.format(Witchery.resource("item.witcheryTaglockKit.boundto"), String.format("%s -> %s", sourceID, targetID)));
         } else if (!sourceID.isEmpty()) {
            list.add(String.format(Witchery.resource("item.witcheryTaglockKit.boundto"), String.format("%s -> ??", sourceID)));
         } else if (!targetID.isEmpty()) {
            list.add(String.format(Witchery.resource("item.witcheryTaglockKit.boundto"), String.format("?? -> %s", targetID)));
         } else {
            list.add(Witchery.resource("item.witcheryTaglockKit.unbound"));
         }
      } else {
         sourceID = Witchery.Items.TAGLOCK_KIT.getBoundEntityDisplayName(stack, 1);
         super.func_77653_i(stack);
         if (sourceID != null && !sourceID.isEmpty()) {
            list.add(String.format(Witchery.resource("item.witcheryTaglockKit.boundto"), sourceID));
         } else {
            list.add(Witchery.resource("item.witcheryTaglockKit.unbound"));
         }
      }

   }

   public void func_94581_a(IIconRegister iconRegister) {
      Iterator i$ = this.poppetTypes.iterator();

      while(i$.hasNext()) {
         ItemPoppet.PoppetType poppetType = (ItemPoppet.PoppetType)i$.next();
         poppetType.registerIcon(iconRegister, this);
      }

   }

   @SideOnly(Side.CLIENT)
   public IIcon func_77617_a(int damage) {
      if (damage < 0 || damage >= this.poppetTypes.size()) {
         damage = 0;
      }

      return ((ItemPoppet.PoppetType)this.poppetTypes.get(damage)).icon;
   }

   @SideOnly(Side.CLIENT)
   public void func_150895_a(Item item, CreativeTabs tab, List itemList) {
      Iterator i$ = this.poppetTypes.iterator();

      while(i$.hasNext()) {
         ItemPoppet.PoppetType poppetType = (ItemPoppet.PoppetType)i$.next();
         itemList.add(new ItemStack(item, 1, poppetType.damageValue));
      }

   }

   public void func_77622_d(ItemStack stack, World world, EntityPlayer player) {
      this.ensureNBT(stack);
      super.func_77622_d(stack, world, player);
   }

   public boolean func_77645_m() {
      return true;
   }

   public boolean isDamaged(ItemStack stack) {
      this.ensureNBT(stack);
      return this.getDamageNBT(stack) > 0;
   }

   public void setDamage(ItemStack stack, int damage) {
   }

   public int getDisplayDamage(ItemStack stack) {
      this.ensureNBT(stack);
      return this.getDamageNBT(stack);
   }

   public int func_77612_l() {
      return 1000;
   }

   private int getDamageNBT(ItemStack stack) {
      this.ensureNBT(stack);
      return stack.func_77978_p().func_74762_e("WITCDamage");
   }

   private void setDamageNBT(IInventory inventory, ItemStack stack, int damage) {
      this.ensureNBT(stack);
      damage = Math.min(damage, 1000);
      stack.func_77978_p().func_74768_a("WITCDamage", damage);
      if (damage == 1000) {
         stack.field_77994_a = 0;
         if (inventory != null) {
            for(int i = 0; i < inventory.func_70302_i_(); ++i) {
               if (inventory.func_70301_a(i) == stack) {
                  inventory.func_70299_a(i, (ItemStack)null);
                  break;
               }
            }
         }
      }

   }

   private void ensureNBT(ItemStack stack) {
      if (!stack.func_77942_o()) {
         stack.func_77982_d(new NBTTagCompound());
      }

      if (!stack.func_77978_p().func_74764_b("WITCDamage")) {
         stack.func_77978_p().func_74768_a("WITCDamage", 0);
      }

   }

   public void func_77663_a(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
      if (!world.field_72995_K && this.voodooPoppet.isMatch(stack) && entity.func_70055_a(Material.field_151586_h) && entity.func_70086_ai() <= 0) {
         EntityLivingBase boundEntity = Witchery.Items.TAGLOCK_KIT.getBoundEntity(world, entity, stack, 1);
         if (boundEntity != null && boundEntity.func_70089_S() && !this.voodooProtectionActivated(entity instanceof EntityPlayer ? (EntityPlayer)entity : null, stack, boundEntity, true, false)) {
            if (entity instanceof EntityPlayer) {
               EntityWitchHunter.blackMagicPerformed((EntityPlayer)entity);
            }

            boolean damageDisabled = boundEntity instanceof EntityPlayer && ((EntityPlayer)boundEntity).field_71075_bZ.field_75102_a;
            if (!ItemHunterClothes.isMagicalProtectionActive(boundEntity) && !boundEntity.func_70648_aU() && !boundEntity.func_82165_m(Potion.field_76427_o.field_76415_H) && !damageDisabled) {
               for(int i = 0; i < 8; ++i) {
                  float f = world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat();
                  float f1 = world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat();
                  float f2 = world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat();
                  world.func_72869_a("bubble", boundEntity.field_70165_t + (double)f, boundEntity.field_70163_u + (double)f1, boundEntity.field_70161_v + (double)f2, boundEntity.field_70159_w, boundEntity.field_70181_x, boundEntity.field_70179_y);
               }

               boundEntity.func_70097_a(DamageSource.field_76369_e, 1.0F);
            }

            boundEntity.func_70066_B();
         }
      }

      super.func_77663_a(stack, world, entity, par4, par5);
   }

   public ItemStack func_77659_a(ItemStack itemstack, World world, EntityPlayer player) {
      if (this.voodooPoppet.isMatch(itemstack)) {
         player.func_71008_a(itemstack, this.func_77626_a(itemstack));
      }

      return super.func_77659_a(itemstack, world, player);
   }

   public int func_77626_a(ItemStack par1ItemStack) {
      return 80;
   }

   public EnumAction func_77661_b(ItemStack par1ItemStack) {
      return EnumAction.bow;
   }

   public void func_77615_a(ItemStack itemstack, World world, EntityPlayer player, int ticks) {
      if (!world.field_72995_K) {
         if (this.voodooPoppet.isMatch(itemstack)) {
            EntityLivingBase entity = Witchery.Items.TAGLOCK_KIT.getBoundEntity(world, player, itemstack, 1);
            if (entity != null) {
               EntityWitchHunter.blackMagicPerformed(player);
               MovingObjectPosition hitObject = this.func_77621_a(world, player, true);
               if (hitObject != null && hitObject.field_72313_a == MovingObjectType.BLOCK) {
                  Material hitMaterial = world.func_147439_a(hitObject.field_72311_b, hitObject.field_72312_c, hitObject.field_72309_d).func_149688_o();
                  world.func_147439_a(hitObject.field_72311_b, hitObject.field_72312_c, hitObject.field_72309_d);
                  if (hitMaterial == Material.field_151587_i) {
                     if (!this.voodooProtectionActivated(player, itemstack, entity, true, false) && !ItemHunterClothes.isMagicalProtectionActive(player)) {
                        entity.func_70015_d(10);
                     }

                     this.setDamageNBT(player.field_71071_by, itemstack, 1000);
                     world.func_72956_a(player, "random.fizz", 0.4F, 2.0F + world.field_73012_v.nextFloat() * 0.4F);
                     return;
                  }
               }

               if (!player.func_70093_af()) {
                  if (!this.voodooProtectionActivated(player, itemstack, entity, true, false) && !ItemHunterClothes.isMagicalProtectionActive(player)) {
                     Vec3 look = player.func_70040_Z();
                     float scaling = (float)((this.func_77626_a(itemstack) - ticks) / 20);
                     double motionX = look.field_72450_a * 0.9D * (double)scaling;
                     double motionY = look.field_72448_b * 0.3D * (double)scaling;
                     double motionZ = look.field_72449_c * 0.9D * (double)scaling;
                     if (entity instanceof EntityPlayer) {
                        EntityPlayer targetPlayer = (EntityPlayer)entity;
                        Witchery.packetPipeline.sendTo((IMessage)(new PacketPushTarget(motionX, motionY, motionZ)), (EntityPlayer)targetPlayer);
                     } else {
                        entity.field_70159_w = motionX;
                        entity.field_70181_x = motionY;
                        entity.field_70179_y = motionZ;
                     }

                     this.setDamageNBT(player.field_71071_by, itemstack, this.getDamageNBT(itemstack) + 10);
                  }

                  return;
               }

               if (player.field_71075_bZ.field_75098_d || Witchery.Items.GENERIC.itemBoneNeedle.isItemInInventory(player.field_71071_by)) {
                  DamageSource damageSource = new ItemPoppet.PoppetDamageSource(DamageSource.field_76376_m, player);
                  if (!this.voodooProtectionActivated(player, itemstack, entity, true, false)) {
                     entity.func_70097_a(damageSource, 0.5F);
                     if (!player.field_71075_bZ.field_75098_d) {
                        Witchery.Items.GENERIC.itemBoneNeedle.consumeItemFromInventory(player.field_71071_by);
                        this.setDamageNBT(player.field_71071_by, itemstack, this.getDamageNBT(itemstack) + 10);
                     }
                  }
               }

               return;
            }
         }

         super.func_77615_a(itemstack, world, player, ticks);
      }

   }

   public boolean voodooProtectionActivated(EntityPlayer attackingEntity, ItemStack voodooStack, EntityLivingBase targetEntity, int strength) {
      if (strength <= 1 || targetEntity instanceof EntityPlayer && InfusedSpiritEffect.POPPET_ENHANCEMENT.isNearTo((EntityPlayer)targetEntity)) {
         return this.voodooProtectionActivated(attackingEntity, voodooStack, targetEntity, true, false);
      } else {
         for(int i = 1; i <= strength; ++i) {
            boolean allowLightning = i == strength;
            if (!this.voodooProtectionActivated(attackingEntity, voodooStack, targetEntity, allowLightning, false)) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean voodooProtectionActivated(EntityPlayer attackingEntity, ItemStack voodooStack, EntityLivingBase targetEntity, boolean allowLightning, boolean onlyBoosted) {
      int ITEM_DAMAGE = true;
      if (targetEntity instanceof EntityPlayer) {
         EntityPlayer targetPlayer = (EntityPlayer)targetEntity;
         ItemStack defenseStack = findBoundPoppetInWorld(this.antiVoodooPoppet, targetPlayer, 350, false, onlyBoosted);
         if (defenseStack != null && !targetPlayer.field_70170_p.field_72995_K) {
            if (attackingEntity != null && voodooStack != null) {
               this.setDamageNBT(attackingEntity.field_71071_by, voodooStack, this.getDamageNBT(voodooStack) + 350);
            }

            if (attackingEntity != null && allowLightning) {
               EntityLightningBolt lightning = new EntityLightningBolt(attackingEntity.field_70170_p, attackingEntity.field_70165_t, attackingEntity.field_70163_u, attackingEntity.field_70161_v);
               attackingEntity.field_70170_p.func_72942_c(lightning);
            }

            return true;
         }
      }

      return false;
   }

   public boolean poppetProtectionActivated(EntityPlayer attackingEntity, ItemStack voodooStack, EntityLivingBase targetEntity, boolean allowLightning) {
      int ITEM_DAMAGE = true;
      if (targetEntity instanceof EntityPlayer) {
         EntityPlayer targetPlayer = (EntityPlayer)targetEntity;
         ItemStack defenseStack = findBoundPoppetInWorld(this.poppetProtectionPoppet, targetPlayer, 350);
         if (defenseStack != null && !attackingEntity.field_70170_p.field_72995_K) {
            if (voodooStack != null) {
               this.setDamageNBT(attackingEntity.field_71071_by, voodooStack, this.getDamageNBT(voodooStack) + 350);
            }

            if (attackingEntity != null && allowLightning) {
               EntityLightningBolt lightning = new EntityLightningBolt(attackingEntity.field_70170_p, attackingEntity.field_70165_t, attackingEntity.field_70163_u, attackingEntity.field_70161_v);
               attackingEntity.field_70170_p.func_72942_c(lightning);
            }

            return true;
         }
      }

      return false;
   }

   public void destroyAntiVoodooPoppets(EntityPlayer attackingEntity, EntityLivingBase targetEntity, int poppetsToDestroy) {
      int ITEM_DAMAGE = true;
      int MAX = true;
      if (targetEntity instanceof EntityPlayer) {
         EntityPlayer targetPlayer = (EntityPlayer)targetEntity;

         for(int i = 0; i < poppetsToDestroy; ++i) {
            ItemStack defenseStack = findBoundPoppetInWorld(this.antiVoodooPoppet, targetPlayer, 1000);
            if (defenseStack == null) {
               return;
            }
         }
      }

   }

   public static ItemStack findBoundPoppetInWorld(ItemPoppet.PoppetType poppetType, EntityPlayer player, int foundItemDamage) {
      return findBoundPoppetInWorld(poppetType, player, foundItemDamage, false, false);
   }

   public static ItemStack findBoundPoppetInWorld(ItemPoppet.PoppetType poppetType, EntityPlayer player, int foundItemDamage, boolean allIndices, boolean onlyBoosted) {
      if (ItemHunterClothes.isFullSetWorn(player, false)) {
         return null;
      } else {
         int damageValue = poppetType.damageValue;
         ItemStack poppetStack = findBoundPoppetInInventory(Witchery.Items.POPPET, damageValue, player, player.field_71071_by, foundItemDamage, allIndices, onlyBoosted);
         if (poppetStack != null) {
            return poppetStack;
         } else {
            if (!player.field_70170_p.field_72995_K && !onlyBoosted) {
               MinecraftServer server = MinecraftServer.func_71276_C();
               WorldServer[] arr$ = server.field_71305_c;
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  WorldServer world = arr$[i$];
                  if (!Config.instance().restrictPoppetShelvesToVanillaAndSpiritDimensions || world.field_73011_w.field_76574_g == 0 || world.field_73011_w.field_76574_g == -1 || world.field_73011_w.field_76574_g == 1 || world.field_73011_w.field_76574_g == Config.instance().dimensionDreamID) {
                     Iterator i$ = world.field_147482_g.iterator();

                     while(i$.hasNext()) {
                        Object obj = i$.next();
                        if (obj instanceof BlockPoppetShelf.TileEntityPoppetShelf) {
                           poppetStack = findBoundPoppetInInventory(Witchery.Items.POPPET, damageValue, player, (IInventory)obj, foundItemDamage, allIndices, false);
                           if (poppetStack != null) {
                              return poppetStack;
                           }
                        }
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   private static ItemStack findBoundPoppetInInventory(Item item, int damage, EntityPlayer player, IInventory inventory, int foundItemDamage, boolean allIndices, boolean onlyBoosted) {
      for(int i = 0; i < inventory.func_70302_i_(); ++i) {
         ItemStack itemstack = inventory.func_70301_a(i);
         if (itemstack != null && itemstack.func_77973_b() == item && itemstack.func_77960_j() == damage && Witchery.Items.TAGLOCK_KIT.containsTaglockForEntity(itemstack, player, 1) && (!allIndices || Witchery.Items.TAGLOCK_KIT.isTaglockPresent(itemstack, 2))) {
            if (onlyBoosted) {
               if (InfusedSpiritEffect.POPPET_ENHANCEMENT.isNearTo(player)) {
                  ((ItemPoppet.PoppetType)Witchery.Items.POPPET.poppetTypes.get(damage)).applyDamageOnUse(inventory, i, itemstack, foundItemDamage);
                  return itemstack;
               } else {
                  return null;
               }
            } else {
               ((ItemPoppet.PoppetType)Witchery.Items.POPPET.poppetTypes.get(damage)).applyDamageOnUse(inventory, i, itemstack, foundItemDamage);
               return itemstack;
            }
         }
      }

      return null;
   }

   public void addDamageToPoppet(ItemStack sourcePoppet, ItemStack destPoppet) {
      this.setDamageNBT((IInventory)null, destPoppet, this.getDamageNBT(sourcePoppet));
   }

   public void cancelEventIfPoppetFound(EntityPlayer player, ItemPoppet.PoppetType poppetType, LivingHurtEvent event, boolean heal) {
      this.cancelEventIfPoppetFound(player, poppetType, event, heal, false);
   }

   public void cancelEventIfPoppetFound(EntityPlayer player, ItemPoppet.PoppetType poppetType, LivingHurtEvent event, boolean heal, boolean onlyHandheld) {
      ItemStack stack = findBoundPoppetInWorld(poppetType, player, 1000, false, onlyHandheld);
      if (stack != null) {
         event.setCanceled(true);
         if (heal && player.func_110143_aJ() < 10.0F) {
            player.func_70606_j(10.0F);
         }

         SoundEffect.RANDOM_ORB.playAtPlayer(player.field_70170_p, player);
      }

   }

   public void checkForArmorProtection(EntityPlayer player) {
      for(int i = 0; i < player.field_71071_by.field_70460_b.length; ++i) {
         ItemStack armorPiece = player.field_71071_by.field_70460_b[i];
         if (armorPiece != null && armorPiece.func_77984_f()) {
            int itemDamage = armorPiece.func_77960_j();
            int maxDamage = armorPiece.func_77958_k();
            int repairThreshold = (int)((float)maxDamage * 0.9F);
            if (itemDamage >= repairThreshold) {
               ItemPoppet var10000 = Witchery.Items.POPPET;
               ItemStack protectStack = findBoundPoppetInWorld(Witchery.Items.POPPET.armorPoppet, player, 1000);
               if (protectStack != null) {
                  armorPiece.func_77964_b(0);
                  player.field_70170_p.func_72956_a(player, "random.orb", 0.5F, 0.4F / ((float)player.field_70170_p.field_73012_v.nextDouble() * 0.4F + 0.8F));
               }
            }
         }
      }

   }

   public static class PoppetType {
      public final int damageValue;
      private final String unlocalizedName;
      private final String localizedName;
      private final int rarity;
      @SideOnly(Side.CLIENT)
      private IIcon icon;
      private boolean destroyOnUse;

      private static ItemPoppet.PoppetType register(ItemPoppet.PoppetType poppetType, ArrayList<ItemPoppet.PoppetType> poppetTypes) {
         poppetTypes.add(poppetType);
         return poppetType;
      }

      private PoppetType(int damageValue, String unlocalizedName, String localizedName) {
         this(damageValue, unlocalizedName, localizedName, 1);
      }

      private PoppetType(int damageValue, String unlocalizedName, String localizedName, int rarity) {
         this.damageValue = damageValue;
         this.unlocalizedName = unlocalizedName;
         this.localizedName = localizedName;
         this.rarity = rarity;
      }

      public ItemStack createStack() {
         return this.createStack(1);
      }

      public ItemStack createStack(int quantity) {
         return new ItemStack(Witchery.Items.POPPET, quantity, this.damageValue);
      }

      public boolean isMatch(ItemStack itemstack) {
         return itemstack != null && itemstack.func_77960_j() == this.damageValue;
      }

      private String getUnlocalizedName() {
         return this.damageValue > 0 ? String.format("%s.%s", Witchery.Items.POPPET.func_77658_a(), this.unlocalizedName) : Witchery.Items.POPPET.func_77658_a();
      }

      @SideOnly(Side.CLIENT)
      private ItemPoppet.PoppetType registerIcon(IIconRegister iconRegister, ItemPoppet item) {
         if (this.unlocalizedName.equals("poppet")) {
            this.icon = iconRegister.func_94245_a(item.func_111208_A());
         } else {
            this.icon = iconRegister.func_94245_a(item.func_111208_A() + "." + this.unlocalizedName);
         }

         return this;
      }

      public ItemPoppet.PoppetType setDestroyOnUse(boolean destroyOnUse) {
         this.destroyOnUse = destroyOnUse;
         return this;
      }

      private boolean applyDamageOnUse(IInventory inventory, int i, ItemStack itemstack, int itemDamage) {
         if (this.destroyOnUse) {
            inventory.func_70299_a(i, (ItemStack)null);
            itemstack.field_77994_a = 0;
         } else {
            Witchery.Items.POPPET.setDamageNBT(inventory, itemstack, Witchery.Items.POPPET.getDamageNBT(itemstack) + itemDamage);
         }

         return false;
      }

      // $FF: synthetic method
      PoppetType(int x0, String x1, String x2, int x3, Object x4) {
         this(x0, x1, x2, x3);
      }

      // $FF: synthetic method
      PoppetType(int x0, String x1, String x2, Object x3) {
         this(x0, x1, x2);
      }
   }

   public static class PoppetEventHooks {
      @SubscribeEvent
      public void onPlayerInteract(PlayerInteractEvent event) {
         if (!event.entityPlayer.field_70170_p.field_72995_K) {
            EntityPlayer player = event.entityPlayer;
            ItemStack heldItem = player.func_70694_bm();
            if (heldItem != null && heldItem.func_77984_f()) {
               int itemDamage = heldItem.func_77960_j();
               int maxDamage = heldItem.func_77958_k();
               int repairThreshold = (int)((float)maxDamage * 0.9F);
               if (itemDamage >= repairThreshold) {
                  ItemStack protectStack = ItemPoppet.findBoundPoppetInWorld(Witchery.Items.POPPET.toolPoppet, player, 1000);
                  if (protectStack != null) {
                     heldItem.func_77964_b(0);
                     player.field_70170_p.func_72956_a(player, "random.orb", 0.5F, 0.4F / ((float)player.field_70170_p.field_73012_v.nextDouble() * 0.4F + 0.8F));
                  }
               }
            }
         }

      }
   }

   private static class PoppetDamageSource extends EntityDamageSource {
      private PoppetDamageSource(DamageSource damageType, Entity source) {
         super(damageType.func_76355_l(), source);
         this.func_76348_h();
         this.func_82726_p();
      }

      // $FF: synthetic method
      PoppetDamageSource(DamageSource x0, Entity x1, Object x2) {
         this(x0, x1);
      }
   }
}
