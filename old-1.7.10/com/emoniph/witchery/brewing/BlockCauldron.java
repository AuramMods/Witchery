package com.emoniph.witchery.brewing;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockBaseContainer;
import com.emoniph.witchery.brewing.action.BrewAction;
import com.emoniph.witchery.client.particle.BubblesFX;
import com.emoniph.witchery.client.particle.NaturePowerFX;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.common.IPowerSource;
import com.emoniph.witchery.common.PowerSources;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class BlockCauldron extends BlockBaseContainer {
   public BlockCauldron() {
      super(Material.field_151573_f, TileEntityCauldron.class);
      this.func_149711_c(2.0F);
      this.func_149672_a(field_149777_j);
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 0.85F, 1.0F);
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      float f = 0.0625F;
      return AxisAlignedBB.func_72330_a((double)((float)x + 0.0625F), (double)y, (double)((float)z + 0.0625F), (double)((float)(x + 1) - 0.0625F), (double)((float)(y + 1) - 0.0625F), (double)((float)(z + 1) - 0.0625F));
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess world, int x, int y, int z, int side) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      TileEntityCauldron cauldron = (TileEntityCauldron)BlockUtil.getTileEntity(world, x, y, z, TileEntityCauldron.class);
      if (cauldron != null && cauldron.isBoiling()) {
         double yPos = 0.2D + cauldron.getPercentFilled() * 0.5D;
         int color = cauldron.getColor();
         if (color == -1) {
            color = 3432410;
         } else if (rand.nextInt(5) == 0) {
            world.func_72980_b((double)x, (double)y, (double)z, "witchery:random.blop", 0.8F + rand.nextFloat() * 0.2F, 0.8F + rand.nextFloat() * 0.2F, false);
         }

         float red = (float)(color >>> 16 & 255) / 256.0F;
         float green = (float)(color >>> 8 & 255) / 256.0F;
         float blue = (float)(color & 255) / 256.0F;

         int i;
         double width;
         double xPos;
         double zPos;
         for(i = 0; i < 2; ++i) {
            width = 0.6D;
            xPos = 0.2D + rand.nextDouble() * 0.6D;
            zPos = 0.2D + rand.nextDouble() * 0.6D;
            BubblesFX sparkle = new BubblesFX(world, (double)x + xPos, (double)y + yPos, (double)z + zPos);
            sparkle.setScale(0.4F);
            if (rand.nextInt(4) == 0) {
               sparkle.setGravity(-0.02F);
               sparkle.setCanMove(true);
               sparkle.setMaxAge(15 + rand.nextInt(10));
            } else {
               sparkle.setGravity(0.0F);
               sparkle.setCanMove(false);
               sparkle.setMaxAge(10 + rand.nextInt(10));
            }

            sparkle.func_70538_b(red, green, blue);
            sparkle.func_82338_g(0.2F);
            Minecraft.func_71410_x().field_71452_i.func_78873_a(sparkle);
         }

         if (cauldron.isPowered()) {
            for(i = 0; i < 1 + Math.min(cauldron.getRitualSeconds(), 5); ++i) {
               width = 0.4D;
               xPos = 0.3D + rand.nextDouble() * 0.4D;
               zPos = 0.3D + rand.nextDouble() * 0.4D;
               double d0 = (double)x + xPos;
               double d1 = (double)y + yPos;
               double d2 = (double)z + zPos;
               NaturePowerFX sparkle = new NaturePowerFX(world, d0, d1, d2);
               sparkle.setCircling(cauldron.isRitualInProgress());
               sparkle.setScale(0.6F);
               sparkle.setGravity(0.25F);
               sparkle.setCanMove(true);
               double maxSpeed = 0.04D;
               double doubleSpeed = 0.08D;
               sparkle.func_70016_h(rand.nextDouble() * 0.08D - 0.04D, rand.nextDouble() * 0.05D + 0.08D, rand.nextDouble() * 0.08D - 0.04D);
               sparkle.setMaxAge(25 + rand.nextInt(cauldron.isRitualInProgress() ? 10 : 10));
               float maxColorShift = 0.2F;
               float doubleColorShift = maxColorShift * 2.0F;
               float colorshiftR = rand.nextFloat() * doubleColorShift - maxColorShift;
               float colorshiftG = rand.nextFloat() * doubleColorShift - maxColorShift;
               float colorshiftB = rand.nextFloat() * doubleColorShift - maxColorShift;
               sparkle.func_70538_b(red + colorshiftR, green + colorshiftG, blue + colorshiftB);
               sparkle.func_82338_g(0.1F);
               Minecraft.func_71410_x().field_71452_i.func_78873_a(sparkle);
            }
         }
      }

   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      if (!world.field_72995_K && entity instanceof EntityItem) {
         TileEntityCauldron cauldron = (TileEntityCauldron)BlockUtil.getTileEntity(world, x, y, z, TileEntityCauldron.class);
         if (cauldron != null) {
            EntityItem itemEntity = (EntityItem)entity;
            if (cauldron.isFilled()) {
               if (Witchery.Items.GENERIC.itemGypsum.isMatch(itemEntity.func_92059_d())) {
                  entity.func_70106_y();
                  SoundEffect.RANDOM_FIZZ.playAt(world, (double)x + 0.5D, (double)y + 0.6D, (double)z + 0.5D, 1.0F, 2.0F);
                  ParticleEffect.SMOKE.send(SoundEffect.NONE, world, (double)x + 0.5D, (double)y + 0.6D, (double)z + 0.5D, 0.5D, 1.0D, 8);
                  cauldron.drain(ForgeDirection.UNKNOWN, cauldron.getLiquidQuantity(), true);
                  cauldron.markBlockForUpdate(true);
               } else if (Witchery.Items.GENERIC.itemQuicklime.isMatch(itemEntity.func_92059_d())) {
                  EntityPlayer nearestPlayer = (EntityPlayer)EntityUtil.findNearestEntityWithinAABB(world, EntityPlayer.class, entity.field_70121_D.func_72314_b(5.0D, 5.0D, 5.0D), entity);
                  if (nearestPlayer != null && cauldron.explodeBrew(nearestPlayer)) {
                     ParticleEffect.SMOKE.send(SoundEffect.NONE, world, (double)x + 0.5D, (double)y + 0.6D, (double)z + 0.5D, 0.5D, 1.0D, 8);
                     cauldron.drain(ForgeDirection.UNKNOWN, cauldron.getLiquidQuantity(), true);
                     entity.func_70106_y();
                     cauldron.markBlockForUpdate(true);
                  }
               } else if (cauldron.isBoiling()) {
                  BrewAction brewAction = WitcheryBrewRegistry.INSTANCE.getActionForItemStack(itemEntity.func_92059_d());
                  if (brewAction != null && cauldron.addItem(brewAction, itemEntity.func_92059_d())) {
                     Item containerItem = itemEntity.func_92059_d().func_77973_b().func_77668_q();
                     if (containerItem != null) {
                        EntityUtil.spawnEntityInWorld(world, new EntityItem(world, 0.5D + (double)x, 1.0D + (double)y, 0.5D + (double)z, new ItemStack(containerItem)));
                     }

                     entity.func_70106_y();
                     ParticleEffect.SPLASH.send(SoundEffect.WATER_SPLASH, world, (double)x + 0.5D, (double)y + 0.6D, (double)z + 0.5D, 0.5D, 0.5D, 8);
                  }
               }
            }
         }
      }

   }

   public boolean func_149740_M() {
      return true;
   }

   public int func_149736_g(World world, int x, int y, int z, int side) {
      TileEntityCauldron cauldron = (TileEntityCauldron)BlockUtil.getTileEntity(world, x, y, z, TileEntityCauldron.class);
      int signal = 0;
      return cauldron != null ? cauldron.getRedstoneSignalStrength() : signal;
   }

   public boolean tryFillWith(World world, int x, int y, int z, FluidStack fluidStack) {
      if (world.field_72995_K) {
         return true;
      } else {
         TileEntityCauldron cauldron = (TileEntityCauldron)BlockUtil.getTileEntity(world, x, y, z, TileEntityCauldron.class);
         if (cauldron != null) {
            new FluidStack(FluidRegistry.WATER.getID(), 1000);
            if (cauldron.canFill(ForgeDirection.UNKNOWN, fluidStack.getFluid())) {
               int quantity = cauldron.fill(ForgeDirection.UNKNOWN, fluidStack, true);
               fluidStack.amount -= quantity;
               if (fluidStack.amount < 0) {
                  fluidStack.amount = 0;
               }

               if (quantity > 0) {
                  SoundEffect.WATER_SWIM.playAt(world, (double)x, (double)y, (double)z);
                  cauldron.markBlockForUpdate(true);
               }

               return quantity > 0;
            }
         }

         return false;
      }
   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
      if (world.field_72995_K) {
         return true;
      } else {
         TileEntityCauldron cauldron = (TileEntityCauldron)BlockUtil.getTileEntity(world, x, y, z, TileEntityCauldron.class);
         ItemStack heldStack = player.func_70694_bm();
         if (cauldron != null && heldStack != null) {
            FluidStack fluidStackToFill = FluidContainerRegistry.getFluidForFilledItem(heldStack);
            if (fluidStackToFill != null) {
               fluidStackToFill.tag = heldStack.func_77942_o() ? (NBTTagCompound)heldStack.func_77978_p().func_74737_b() : null;
               if (cauldron.canFill(ForgeDirection.UNKNOWN, fluidStackToFill.getFluid())) {
                  int quantityFilled = cauldron.fill(ForgeDirection.UNKNOWN, fluidStackToFill, true);
                  if (quantityFilled != 0) {
                     if (!player.field_71075_bZ.field_75098_d) {
                        player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, consumeItem(heldStack));
                     }

                     SoundEffect.WATER_SWIM.playAtPlayer(world, player);
                     cauldron.markBlockForUpdate(true);
                  }
               }

               return true;
            }

            if (heldStack.func_77973_b() == Witchery.Items.BREW_ENDLESS_WATER) {
               if (this.tryFillWith(world, x, y, z, new FluidStack(FluidRegistry.WATER, 3000))) {
                  heldStack.func_77972_a(1, player);
               }

               return true;
            }

            FluidStack fluidStackInCauldron = cauldron.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
            if (fluidStackInCauldron != null) {
               ItemStack filledBucketStack = FluidContainerRegistry.fillFluidContainer(fluidStackInCauldron, heldStack);
               FluidStack fluidStackToEmpty = FluidContainerRegistry.getFluidForFilledItem(filledBucketStack);
               if (fluidStackToEmpty != null) {
                  if (fluidStackInCauldron.tag != null) {
                     filledBucketStack.func_77982_d((NBTTagCompound)fluidStackInCauldron.tag.func_74737_b());
                  }

                  if (!player.field_71075_bZ.field_75098_d) {
                     if (heldStack.field_77994_a > 1) {
                        if (!player.field_71071_by.func_70441_a(filledBucketStack)) {
                           return false;
                        }

                        player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, consumeItem(heldStack));
                     } else {
                        player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, consumeItem(heldStack));
                        player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, filledBucketStack);
                     }
                  }

                  cauldron.drain(ForgeDirection.UNKNOWN, fluidStackToEmpty.amount, true);
                  SoundEffect.WATER_SWIM.playAtPlayer(world, player);
                  world.func_147471_g(x, y, z);
                  cauldron.markBlockForUpdate(true);
               } else if (heldStack.func_77973_b() == Items.field_151069_bo) {
                  int drainAmount = this.getDrainAmount(player, fluidStackInCauldron.tag);
                  if (drainAmount > 0 && cauldron.isPowered() && cauldron.isBoiling()) {
                     NBTTagCompound nbtFluid = (NBTTagCompound)fluidStackInCauldron.tag.func_74737_b();
                     boolean enoughLiquid = drainAmount <= cauldron.getLiquidQuantity();
                     if (enoughLiquid) {
                        IPowerSource source = PowerSources.findClosestPowerSource(cauldron);
                        int power = cauldron.getPower();
                        if (power == 0 || source != null && source.consumePower((float)cauldron.getPower())) {
                           cauldron.drain(ForgeDirection.UNKNOWN, Math.min(drainAmount, cauldron.getLiquidQuantity()), true);
                           SoundEffect.WATER_SWIM.playAtPlayer(world, player);
                           cauldron.markBlockForUpdate(true);
                           this.processSkillChanges(player, fluidStackInCauldron.tag);
                           ItemStack brewStack = new ItemStack(Witchery.Items.BREW);
                           brewStack.func_77982_d(nbtFluid);
                           if (heldStack.field_77994_a > 1) {
                              if (player.field_71071_by.func_70441_a(brewStack)) {
                                 player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, consumeItem(heldStack));
                                 EntityUtil.syncInventory(player);
                                 return false;
                              }

                              return true;
                           }

                           player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, consumeItem(heldStack));
                           player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, brewStack);
                           EntityUtil.syncInventory(player);
                           return true;
                        }
                     }
                  }
               }
            }
         }

         return super.func_149727_a(world, x, y, z, player, side, hitX, hitY, hitZ);
      }
   }

   public ItemStack fillBottleFromCauldron(World world, int x, int y, int z, int drainAmount) {
      TileEntityCauldron cauldron = (TileEntityCauldron)BlockUtil.getTileEntity(world, x, y, z, TileEntityCauldron.class);
      if (cauldron != null) {
         FluidStack fluidStackInCauldron = cauldron.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
         if (fluidStackInCauldron != null && drainAmount > 0 && cauldron.isPowered() && cauldron.isBoiling()) {
            NBTTagCompound nbtFluid = (NBTTagCompound)fluidStackInCauldron.tag.func_74737_b();
            boolean enoughLiquid = drainAmount <= cauldron.getLiquidQuantity();
            cauldron.drain(ForgeDirection.UNKNOWN, Math.min(drainAmount, cauldron.getLiquidQuantity()), true);
            SoundEffect.WATER_SWIM.playAt(world, (double)x, (double)y, (double)z);
            cauldron.markBlockForUpdate(true);
            if (enoughLiquid) {
               ItemStack brewStack = new ItemStack(Witchery.Items.BREW);
               brewStack.func_77982_d(nbtFluid);
               return brewStack;
            }
         }
      }

      return null;
   }

   private void processSkillChanges(EntityPlayer player, NBTTagCompound nbtBrew) {
      ExtendedPlayer props = ExtendedPlayer.get(player);
      if (props != null) {
         EffectLevelCounter levels = WitcheryBrewRegistry.INSTANCE.getBrewLevel(nbtBrew);
         int currentLevel = props.getSkillPotionBottling();
         if (levels.canIncreasePlayerSkill(currentLevel)) {
            props.increaseSkillPotionBottling();
         }
      }

   }

   private int getDrainAmount(EntityPlayer player, NBTTagCompound nbtFluid) {
      ModifierYield yieldModifier = WitcheryBrewRegistry.INSTANCE.getYieldModifier(nbtFluid);
      int[][] yieldLevels = new int[][]{{1, 3000}, {2, 1500}, {3, 1000}, {4, 750}, {5, 600}, {6, 500}, {8, 375}, {10, 300}, {15, 200}, {30, 100}};
      int yield = 0;
      ExtendedPlayer props = ExtendedPlayer.get(player);
      if (props != null) {
         int levelSkill = props.getSkillPotionBottling() / 30;
         PotionEffect potionEffect = player.func_70660_b(Witchery.Potions.BREWING_EXPERT);
         int levelPotion = potionEffect != null ? potionEffect.func_76458_c() + 1 : 0;
         int gearLevel = 0;
         ItemStack headItem = player.field_71071_by.func_70440_f(3);
         int gearLevel = gearLevel + (Witchery.Items.WITCH_HAT.isHatWorn(player) ? 1 : 0);
         gearLevel += Witchery.Items.BABAS_HAT.isHatWorn(player) ? 2 : 0;
         gearLevel += Witchery.Items.WITCH_ROBES.isRobeWorn(player) ? 1 : 0;
         gearLevel += Witchery.Items.NECROMANCERS_ROBES.isRobeWorn(player) ? 1 : 0;
         int familiarLevel = Familiar.hasActiveBrewMasteryFamiliar(player) ? 1 : 0;
         if (levelSkill == 0) {
            yield = 0;
         } else if (levelSkill == 1) {
            if (gearLevel < 1 && levelPotion < 1) {
               yield = 1;
            } else {
               yield = 2;
            }
         } else if (levelSkill >= 2) {
            if (gearLevel >= 3 && levelPotion >= 3 && familiarLevel >= 1) {
               yield = 9;
            } else if (gearLevel >= 2 && levelPotion >= 3 && familiarLevel >= 1) {
               yield = 8;
            } else if (gearLevel >= 2 && levelPotion >= 3) {
               yield = 7;
            } else if (gearLevel >= 2 && levelPotion >= 2) {
               yield = 6;
            } else if (gearLevel >= 2 && levelPotion >= 1) {
               yield = 5;
            } else if (gearLevel < 2 && levelPotion < 1) {
               if (gearLevel < 1 && levelPotion < 1) {
                  yield = 2;
               } else {
                  yield = 3;
               }
            } else {
               yield = 4;
            }
         }
      }

      return yieldLevels[Math.max(yield - yieldModifier.getYieldModification(), 0)][1];
   }

   private static ItemStack consumeItem(ItemStack stack) {
      if (stack.field_77994_a == 1) {
         return stack.func_77973_b().hasContainerItem(stack) ? stack.func_77973_b().getContainerItem(stack) : null;
      } else {
         stack.func_77979_a(1);
         return stack;
      }
   }
}
