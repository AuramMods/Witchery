package com.emoniph.witchery.brewing;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.TileEntityBase;
import com.emoniph.witchery.brewing.action.BrewAction;
import com.emoniph.witchery.brewing.potions.PotionBase;
import com.emoniph.witchery.common.IPowerSource;
import com.emoniph.witchery.common.PowerSources;
import com.emoniph.witchery.entity.EntityCovenWitch;
import com.emoniph.witchery.entity.EntityLeonard;
import com.emoniph.witchery.util.CircleUtil;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityCauldron extends TileEntityBase implements IFluidHandler {
   private int ticksHeated;
   private boolean powered;
   private int ritualTicks;
   private static final int TICKS_TO_BOIL = TimeUtil.secsToTicks(5);
   private FluidTank tank = new FluidTank(3000);

   public boolean isBoiling() {
      return this.ticksHeated == TICKS_TO_BOIL;
   }

   public boolean isPowered() {
      return this.powered;
   }

   public int getRedstoneSignalStrength() {
      if (!this.isFilled()) {
         return 0;
      } else if (!this.isBoiling()) {
         return 3;
      } else {
         NBTTagCompound nbtRoot = this.tank.getFluid().tag;
         if (nbtRoot != null && nbtRoot.func_74762_e("EffectCount") != 0) {
            if (!this.isPowered()) {
               return 9;
            } else {
               return nbtRoot.func_74762_e("RemainingCapacity") > 0 ? 12 : 15;
            }
         } else {
            return 6;
         }
      }
   }

   public void func_145845_h() {
      super.func_145845_h();
      if (!this.field_145850_b.field_72995_K) {
         boolean sync = false;
         Block blockBelow = this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d - 1, this.field_145849_e);
         if (blockBelow == Blocks.field_150480_ab && this.isFilled()) {
            if (this.ticksHeated < TICKS_TO_BOIL && ++this.ticksHeated == TICKS_TO_BOIL) {
               sync = true;
            }
         } else if (this.ticksHeated > 0) {
            this.ticksHeated = 0;
            sync = true;
         }

         boolean playerCoven;
         if (this.isBoiling() && this.ticks % 20L == 7L) {
            boolean wasPowered = this.powered;
            int power = this.getPower();
            if (power == 0) {
               this.powered = true;
            } else if (power > 0) {
               IPowerSource source = PowerSources.findClosestPowerSource(this);
               double powerNeeded;
               if (this.tank.getFluid() != null && this.tank.getFluid().tag != null && this.tank.getFluidAmount() == 3000 && this.tank.getFluid().tag.func_74767_n("RitualTriggered")) {
                  boolean small = CircleUtil.isSmallCircle(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, Witchery.Blocks.GLYPH_RITUAL);
                  playerCoven = CircleUtil.isSmallCircle(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, Witchery.Blocks.GLYPH_INFERNAL);
                  boolean medium = CircleUtil.isMediumCircle(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, Witchery.Blocks.GLYPH_RITUAL);
                  boolean mediumPower = CircleUtil.isMediumCircle(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, Witchery.Blocks.GLYPH_INFERNAL);
                  double powerScale = 1.4D;
                  if (small) {
                     powerScale -= 0.2D;
                  }

                  if (medium) {
                     powerScale -= 0.2D;
                  }

                  if (playerCoven) {
                     powerScale -= 0.37D;
                  }

                  if (mediumPower) {
                     powerScale -= 0.37D;
                  }

                  powerNeeded = (double)power * powerScale;
               } else {
                  powerNeeded = (double)power;
               }

               this.powered = power == 0 || source != null && powerNeeded <= (double)source.getCurrentPower();
            } else {
               this.powered = false;
            }

            if (wasPowered != this.powered) {
               sync = true;
            }
         }

         if (this.ticks % 10L == 8L) {
            int oldRitualTicks = this.ritualTicks;
            int UPDATES_TO_ACTIVATE = true;
            if (this.isBoiling() && this.isPowered() && this.tank.getFluid() != null && this.tank.getFluid().tag != null && this.tank.getFluidAmount() == 3000) {
               NBTTagCompound nbtRoot = this.tank.getFluid().tag;
               if (nbtRoot.func_74767_n("RitualTriggered")) {
                  ++this.ritualTicks;
                  int witchCount = 0;
                  List<EntityCovenWitch> covenWitches = EntityUtil.getEntitiesInRadius(EntityCovenWitch.class, this, 6.0D);
                  Iterator i$ = covenWitches.iterator();

                  while(i$.hasNext()) {
                     EntityCovenWitch witch = (EntityCovenWitch)i$.next();
                     if (witch.func_70902_q() != null) {
                        ++witchCount;
                     }
                  }

                  List<EntityPlayer> playerWitches = EntityUtil.getEntitiesInRadius(EntityPlayer.class, this, 6.0D);
                  playerCoven = false;
                  Iterator i$ = playerWitches.iterator();

                  while(i$.hasNext()) {
                     EntityPlayer player = (EntityPlayer)i$.next();
                     if (EntityCovenWitch.getCovenSize(player) > 0) {
                        if (playerCoven) {
                           ++witchCount;
                        } else {
                           playerCoven = true;
                        }
                     }
                  }

                  if (this.ritualTicks > 20) {
                     IPowerSource powerSource = PowerSources.findClosestPowerSource(this);
                     int neededPower = this.getPower();
                     boolean small = CircleUtil.isSmallCircle(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, Witchery.Blocks.GLYPH_RITUAL);
                     boolean smallPower = CircleUtil.isSmallCircle(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, Witchery.Blocks.GLYPH_INFERNAL);
                     boolean medium = CircleUtil.isMediumCircle(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, Witchery.Blocks.GLYPH_RITUAL);
                     boolean mediumPower = CircleUtil.isMediumCircle(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, Witchery.Blocks.GLYPH_INFERNAL);
                     double powerScale = 1.4D;
                     if (small) {
                        powerScale -= 0.2D;
                     }

                     if (medium) {
                        powerScale -= 0.2D;
                     }

                     int risk = 0;
                     if (!small && !medium) {
                        ++risk;
                     }

                     if (smallPower) {
                        ++risk;
                        powerScale -= 0.37D;
                     }

                     if (mediumPower) {
                        ++risk;
                        powerScale -= 0.37D;
                     }

                     if (neededPower != 0 && (powerSource == null || !powerSource.consumePower((float)((int)Math.floor((double)neededPower * powerScale))))) {
                        if (this.ritualTicks > 21) {
                           this.drain(ForgeDirection.UNKNOWN, this.getLiquidQuantity(), true);
                           this.ritualTicks = 0;
                           this.powered = false;
                           this.ticksHeated = 0;
                           ParticleEffect.SPELL_COLORED.send(SoundEffect.RANDOM_POP, this.field_145850_b, (double)this.field_145851_c + 0.5D, (double)this.field_145848_d + 0.6D, (double)this.field_145849_e + 0.5D, 0.5D, 1.0D, 8, -7829504);
                           sync = true;
                        }
                     } else {
                        double R = 16.0D;
                        AxisAlignedBB bb = AxisAlignedBB.func_72330_a((double)this.field_145851_c - R, (double)this.field_145848_d - R, (double)this.field_145849_e - R, (double)this.field_145851_c + R, (double)this.field_145848_d + R, (double)this.field_145849_e + R);
                        List<EntityLeonard> leonards = this.field_145850_b.func_72872_a(EntityLeonard.class, bb);
                        boolean lenny = false;
                        Iterator i$ = leonards.iterator();

                        while(i$.hasNext()) {
                           EntityLeonard leonard = (EntityLeonard)i$.next();
                           if (!leonard.field_70128_L && leonard.func_110143_aJ() > 1.0F) {
                              lenny = true;
                              break;
                           }
                        }

                        RitualStatus status = WitcheryBrewRegistry.INSTANCE.updateRitual(MinecraftServer.func_71276_C(), this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, nbtRoot, witchCount, this.ritualTicks - 20, lenny);
                        boolean failed = false;
                        switch(status) {
                        case ONGOING:
                           this.checkForMisfortune(risk + (lenny ? 1 : 0), neededPower);
                           break;
                        case COMPLETE:
                           ParticleEffect.SPELL.send(SoundEffect.RANDOM_FIZZ, this.field_145850_b, (double)this.field_145851_c + 0.5D, (double)this.field_145848_d + 0.6D, (double)this.field_145849_e + 0.5D, 0.5D, 1.0D, 8);
                           this.drain(ForgeDirection.UNKNOWN, this.getLiquidQuantity(), true);
                           this.ritualTicks = 0;
                           this.powered = false;
                           this.ticksHeated = 0;
                           sync = true;
                           this.checkForMisfortune(risk + (lenny ? 1 : 0), neededPower);
                           break;
                        case FAILED_DISTANCE:
                           ParticleEffect.SPELL_COLORED.send(SoundEffect.RANDOM_POP, this.field_145850_b, (double)this.field_145851_c + 0.5D, (double)this.field_145848_d + 0.6D, (double)this.field_145849_e + 0.5D, 0.5D, 1.0D, 8, -16742400);
                           failed = true;
                           break;
                        case FAILED_NO_COVEN:
                           ParticleEffect.SPELL_COLORED.send(SoundEffect.RANDOM_POP, this.field_145850_b, (double)this.field_145851_c + 0.5D, (double)this.field_145848_d + 0.6D, (double)this.field_145849_e + 0.5D, 0.5D, 1.0D, 8, -16777080);
                           failed = true;
                           break;
                        case FAILED_INVALID_CIRCLES:
                           ParticleEffect.SPELL_COLORED.send(SoundEffect.RANDOM_POP, this.field_145850_b, (double)this.field_145851_c + 0.5D, (double)this.field_145848_d + 0.6D, (double)this.field_145849_e + 0.5D, 0.5D, 1.0D, 8, -7864320);
                           failed = true;
                           break;
                        case FAILED:
                           ParticleEffect.SPELL_COLORED.send(SoundEffect.RANDOM_POP, this.field_145850_b, (double)this.field_145851_c + 0.5D, (double)this.field_145848_d + 0.6D, (double)this.field_145849_e + 0.5D, 0.5D, 1.0D, 8, -7864184);
                           failed = true;
                        }

                        if (failed) {
                           NBTTagList nbtItems = nbtRoot.func_150295_c("Items", 10);
                           ItemStack stack = ItemStack.func_77949_a(nbtItems.func_150305_b(nbtItems.func_74745_c() - 1));
                           nbtItems.func_74744_a(nbtItems.func_74745_c() - 1);
                           EntityUtil.spawnEntityInWorld(this.field_145850_b, new EntityItem(this.field_145850_b, (double)this.field_145851_c, (double)this.field_145848_d, (double)this.field_145849_e, stack));
                           nbtRoot.func_74757_a("RitualTriggered", false);
                           this.ritualTicks = 0;
                           sync = true;
                        }
                     }
                  }
               }
            } else {
               if (this.ritualTicks > 20) {
                  this.drain(ForgeDirection.UNKNOWN, this.getLiquidQuantity(), true);
                  this.ritualTicks = 0;
                  this.powered = false;
                  this.ticksHeated = 0;
                  ParticleEffect.SPELL_COLORED.send(SoundEffect.RANDOM_POP, this.field_145850_b, (double)this.field_145851_c + 0.5D, (double)this.field_145848_d + 0.6D, (double)this.field_145849_e + 0.5D, 0.5D, 1.0D, 8, -7829504);
                  sync = true;
               }

               this.ritualTicks = 0;
            }

            if (this.ritualTicks != oldRitualTicks) {
               sync = true;
            }
         }

         if (sync) {
            this.markBlockForUpdate(true);
         }
      }

   }

   private void checkForMisfortune(int risk, int power) {
      if (risk > 0 && power > 0) {
         double roll = this.field_145850_b.field_73012_v.nextDouble() * (1.0D + (double)(risk - 1) * 0.2D);
         if (roll < 0.5D) {
            return;
         }

         if (roll < 0.75D) {
            this.applyToAllNear(new PotionEffect(Potion.field_76421_d.field_76415_H, TimeUtil.secsToTicks(60), 1));
         } else if (roll < 0.9D) {
            this.applyToAllNear(new PotionEffect(Witchery.Potions.PARALYSED.field_76415_H, TimeUtil.secsToTicks(20), 2));
         } else if (roll < 0.98D) {
            this.applyToAllNear(new PotionEffect(Witchery.Potions.INSANITY.field_76415_H, TimeUtil.minsToTicks(3), 2));
         } else {
            this.applyToAllNear(new PotionEffect(Witchery.Potions.PARALYSED.field_76415_H, TimeUtil.secsToTicks(10), 2));

            for(int i = 0; i < this.field_145850_b.field_73012_v.nextInt(3) + 2; ++i) {
               this.spawnBolt(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, 1, 4);
            }
         }
      }

   }

   private void applyToAllNear(PotionEffect effect) {
      double R = 16.0D;
      double RSq = 256.0D;
      AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(0.5D + (double)this.field_145851_c - 16.0D, (double)(this.field_145848_d - 2), 0.5D + (double)this.field_145849_e - 16.0D, 0.5D + (double)this.field_145851_c + 16.0D, (double)(this.field_145848_d + 4), 0.5D + (double)this.field_145849_e + 16.0D);
      List<EntityLivingBase> entities = this.field_145850_b.func_72872_a(EntityLivingBase.class, bounds);
      Iterator i$ = entities.iterator();

      while(true) {
         EntityLivingBase entity;
         do {
            do {
               do {
                  if (!i$.hasNext()) {
                     return;
                  }

                  entity = (EntityLivingBase)i$.next();
               } while(!(entity.func_70092_e(0.5D + (double)this.field_145851_c, entity.field_70163_u, 0.5D + (double)this.field_145849_e) < 256.0D));
            } while(entity instanceof IMob);
         } while(entity instanceof IBossDisplayData);

         List<Potion> effectsToRemove = new ArrayList();
         Collection<PotionEffect> effects = entity.func_70651_bq();
         Iterator i$ = effects.iterator();

         while(i$.hasNext()) {
            PotionEffect buffs = (PotionEffect)i$.next();
            Potion potion = Potion.field_76425_a[effect.func_76456_a()];
            if (!PotionBase.isDebuff(potion)) {
               effectsToRemove.add(potion);
            }
         }

         i$ = effectsToRemove.iterator();

         while(i$.hasNext()) {
            Potion potion = (Potion)i$.next();
            entity.func_82170_o(potion.field_76415_H);
         }

         entity.func_70690_d(new PotionEffect(effect));
      }
   }

   private void spawnBolt(World world, int posX, int posY, int posZ, int min, int max) {
      int activeRadius = max - min;
      int ax = world.field_73012_v.nextInt(activeRadius * 2 + 1);
      if (ax > activeRadius) {
         ax += min * 2;
      }

      int x = posX - max + ax;
      int az = world.field_73012_v.nextInt(activeRadius * 2 + 1);
      if (az > activeRadius) {
         az += min * 2;
      }

      int z = posZ - max + az;
      EntityLightningBolt bolt = new EntityLightningBolt(world, (double)x, (double)posY, (double)z);
      world.func_72942_c(bolt);
   }

   public boolean isRitualInProgress() {
      return this.ritualTicks > 0;
   }

   public boolean addItem(BrewAction brewAction, ItemStack entityItem) {
      if (this.field_145850_b.field_72995_K) {
         return false;
      } else {
         NBTTagCompound nbtRoot;
         if (this.tank.getFluid().getFluid().getName().equals(FluidRegistry.WATER.getName())) {
            nbtRoot = new NBTTagCompound();
            nbtRoot.func_74782_a("Items", new NBTTagList());
            if (!WitcheryBrewRegistry.INSTANCE.canAdd(nbtRoot, brewAction, this.tank.getFluidAmount() == this.tank.getCapacity())) {
               return false;
            }

            this.tank.setFluid(new FluidStack(Witchery.Fluids.BREW, this.tank.getFluid().amount));
            this.tank.getFluid().tag = nbtRoot;
         }

         if (this.tank.getFluid().tag == null) {
            this.tank.getFluid().tag = new NBTTagCompound();
         }

         nbtRoot = this.tank.getFluid().tag;
         if (!nbtRoot.func_74764_b("Items")) {
            nbtRoot.func_74782_a("Items", new NBTTagList());
         }

         if (!WitcheryBrewRegistry.INSTANCE.canAdd(nbtRoot, brewAction, this.tank.getFluidAmount() == this.tank.getCapacity())) {
            return false;
         } else {
            if (!brewAction.removeWhenAddedToCauldron(this.field_145850_b)) {
               NBTTagList nbtItems = nbtRoot.func_150295_c("Items", 10);
               NBTTagCompound nbtItem = new NBTTagCompound();
               WitcheryBrewRegistry.INSTANCE.nullifyItems(nbtRoot, nbtItems, brewAction);
               entityItem.func_77955_b(nbtItem);
               nbtItems.func_74742_a(nbtItem);
            }

            int color = brewAction.augmentColor(nbtRoot.func_74762_e("Color"));
            nbtRoot.func_74768_a("Color", color);
            AltarPower powerNeeded = WitcheryBrewRegistry.INSTANCE.getPowerRequired(nbtRoot);
            nbtRoot.func_74768_a("Power", powerNeeded.getPower());
            nbtRoot.func_74778_a("BrewName", WitcheryBrewRegistry.INSTANCE.getBrewName(nbtRoot));
            WitcheryBrewRegistry.INSTANCE.updateBrewInformation(nbtRoot);
            nbtRoot.func_74768_a("BrewDrinkSpeed", WitcheryBrewRegistry.INSTANCE.getBrewDrinkSpeed(nbtRoot));
            if (brewAction.createsSplash()) {
               nbtRoot.func_74757_a("Splash", true);
            }

            if (brewAction.triggersRitual()) {
               nbtRoot.func_74757_a("RitualTriggered", true);
               this.ritualTicks = 0;
            }

            this.markBlockForUpdate(true);
            return true;
         }
      }
   }

   public boolean explodeBrew(EntityPlayer nearestPlayer) {
      if (!this.field_145850_b.field_72995_K && nearestPlayer != null) {
         if (this.tank.getFluid() == null) {
            return false;
         } else if (this.tank.getFluid().getFluid().getName().equals(FluidRegistry.WATER.getName())) {
            return false;
         } else if (this.tank.getFluid().tag == null) {
            return false;
         } else {
            NBTTagCompound nbtRoot = this.tank.getFluid().tag;
            if (!nbtRoot.func_74764_b("Items")) {
               return false;
            } else {
               WitcheryBrewRegistry.INSTANCE.explodeBrew(this.field_145850_b, nbtRoot, nearestPlayer, 0.5D + (double)this.field_145851_c, 1.5D + (double)this.field_145848_d, 0.5D + (double)this.field_145849_e);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public int getColor() {
      NBTTagCompound nbtRoot = this.tank.getFluid() != null ? this.tank.getFluid().tag : null;
      if (nbtRoot != null) {
         int color = nbtRoot.func_74762_e("Color");
         return color;
      } else {
         return -1;
      }
   }

   public int getPower() {
      NBTTagCompound nbtRoot = this.tank.getFluid() != null ? this.tank.getFluid().tag : null;
      if (nbtRoot != null) {
         int power = nbtRoot.func_74762_e("Power");
         return power;
      } else {
         return -1;
      }
   }

   public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
      int filled;
      FluidStack newStack;
      if (this.tank.getFluid() == null) {
         filled = this.tank.fill(resource, doFill);
         newStack = this.tank.getFluid();
         if (newStack != null) {
            newStack.tag = resource.tag != null ? (NBTTagCompound)resource.tag.func_74737_b() : null;
            this.markBlockForUpdate(false);
         }

         return filled;
      } else if (resource.isFluidEqual(this.tank.getFluid()) && (this.tank.getFluid().tag == null || resource.tag != null && this.tank.getFluid().tag.func_150295_c("Items", 10).equals(resource.tag.func_150295_c("Items", 10)))) {
         filled = this.tank.fill(resource, doFill);
         newStack = this.tank.getFluid();
         if (newStack != null) {
            newStack.tag = resource.tag != null ? (NBTTagCompound)resource.tag.func_74737_b() : null;
         }

         this.markBlockForUpdate(false);
         return filled;
      } else {
         return 0;
      }
   }

   public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
      if (resource != null && resource.isFluidEqual(this.tank.getFluid())) {
         NBTTagCompound oldTag = this.tank.getFluid() != null && this.tank.getFluid().tag != null ? this.tank.getFluid().tag : null;
         FluidStack drained = this.tank.drain(resource.amount, doDrain);
         drained.tag = oldTag != null ? (NBTTagCompound)oldTag.func_74737_b() : null;
         if (this.tank.getFluidAmount() == 0) {
            this.powered = false;
         }

         this.markBlockForUpdate(false);
         return drained;
      } else {
         return null;
      }
   }

   public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
      NBTTagCompound oldTag = this.tank.getFluid() != null && this.tank.getFluid().tag != null ? this.tank.getFluid().tag : null;
      FluidStack fluid = this.tank.drain(maxDrain, doDrain);
      if (fluid != null) {
         fluid.tag = oldTag != null ? (NBTTagCompound)oldTag.func_74737_b() : null;
      }

      if (this.tank.getFluidAmount() == 0) {
         this.powered = false;
      }

      this.markBlockForUpdate(false);
      return fluid;
   }

   public boolean canFill(ForgeDirection from, Fluid fluid) {
      if (fluid == null) {
         return false;
      } else {
         return fluid.getName().equals(FluidRegistry.WATER.getName()) || fluid == Witchery.Fluids.BREW;
      }
   }

   public boolean canDrain(ForgeDirection from, Fluid fluid) {
      if (fluid == null) {
         return false;
      } else {
         return fluid.getName().equals(FluidRegistry.WATER.getName()) || fluid == Witchery.Fluids.BREW;
      }
   }

   public FluidTankInfo[] getTankInfo(ForgeDirection from) {
      return new FluidTankInfo[]{this.tank.getInfo()};
   }

   public boolean isFilled() {
      return this.tank.getFluid() != null;
   }

   public int getMaxLiquidQuantity() {
      return this.tank.getCapacity();
   }

   public int getLiquidQuantity() {
      return this.tank.getFluidAmount();
   }

   public double getPercentFilled() {
      return (double)this.tank.getFluidAmount() / (double)this.tank.getCapacity();
   }

   public Packet func_145844_m() {
      NBTTagCompound nbtTag = new NBTTagCompound();
      this.func_145841_b(nbtTag);
      return new S35PacketUpdateTileEntity(this.field_145851_c, this.field_145848_d, this.field_145849_e, 1, nbtTag);
   }

   public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
      super.onDataPacket(net, packet);
      this.func_145839_a(packet.func_148857_g());
      this.field_145850_b.func_147479_m(this.field_145851_c, this.field_145848_d, this.field_145849_e);
   }

   public void func_145839_a(NBTTagCompound nbtRoot) {
      super.func_145839_a(nbtRoot);
      if (this.tank.getFluidAmount() > 0) {
         this.tank.drain(this.tank.getFluidAmount(), true);
      }

      this.tank.readFromNBT(nbtRoot);
      this.ticksHeated = nbtRoot.func_74762_e("TicksHeated");
      this.powered = nbtRoot.func_74767_n("Powered");
      this.ritualTicks = nbtRoot.func_74762_e("RitualTicks");
   }

   public void func_145841_b(NBTTagCompound nbtRoot) {
      super.func_145841_b(nbtRoot);
      this.tank.writeToNBT(nbtRoot);
      nbtRoot.func_74768_a("TicksHeated", this.ticksHeated);
      nbtRoot.func_74757_a("Powered", this.powered);
      nbtRoot.func_74768_a("RitualTicks", this.ritualTicks);
   }

   public int getRitualSeconds() {
      return this.ritualTicks;
   }
}
