package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.EntityGoblin;
import com.emoniph.witchery.util.BlockUtil;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.oredict.OreDictionary;

public class EntityAIDigBlocks extends EntityAIBase {
   protected final EntityGoblin entity;
   protected final double range;
   protected final double kobolditeChance;
   public static final GameProfile NORMAL_MINER_PROFILE = new GameProfile(UUID.fromString("AB06ACB0-0CDB-11E4-9191-0800200C9A66"), "[Minecraft]");
   public static final GameProfile KOBOLDITE_MINER_PROFILE = new GameProfile(UUID.fromString("24818AE0-0CDE-11E4-9191-0800200C9A66"), "[Minecraft]");
   MovingObjectPosition mop = null;
   int failedChecks = 0;
   private int waitTimer = 60;

   public EntityAIDigBlocks(EntityGoblin entity, double range, double kobolditeChance) {
      this.entity = entity;
      this.range = range;
      this.kobolditeChance = kobolditeChance;
      this.func_75248_a(7);
   }

   public boolean func_75250_a() {
      if (this.entity != null && !this.entity.isWorshipping() && this.entity.func_70694_bm() != null && this.entity.func_70694_bm().func_77973_b() instanceof ItemPickaxe && this.entity.func_110167_bD() && this.entity.field_70170_p.field_73012_v.nextInt(2) == 0) {
         MovingObjectPosition mop = raytraceBlocks(this.entity.field_70170_p, this.entity, true, this.failedChecks == 15 ? 1.0D : 4.0D, this.failedChecks == 15);
         if (mop != null && mop.field_72313_a == MovingObjectType.BLOCK) {
            Block block = BlockUtil.getBlock(this.entity.field_70170_p, mop);
            if (this.isMineable(block, this.entity.field_70170_p, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d)) {
               this.failedChecks = 0;
               this.mop = mop;
               return true;
            } else {
               this.mop = null;
               ++this.failedChecks;
               return false;
            }
         } else {
            ++this.failedChecks;
            mop = null;
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean isMineable(Block block, World world, int x, int y, int z) {
      if (block.func_149688_o() != Material.field_151576_e && block.func_149688_o() != Material.field_151595_p && block.func_149688_o() != Material.field_151577_b && block.func_149688_o() != Material.field_151597_y && block.func_149688_o() != Material.field_151578_c) {
         return false;
      } else {
         return !(block.func_149712_f(world, x, y, z) < 0.0F);
      }
   }

   private static MovingObjectPosition raytraceBlocks(World world, EntityLiving player, boolean collisionFlag, double reachDistance, boolean down) {
      Vec3 playerPosition = Vec3.func_72443_a(player.field_70165_t, player.field_70163_u + (double)player.func_70047_e(), player.field_70161_v);
      float rotationYaw = (float)world.field_73012_v.nextInt(360);
      player.field_70177_z = rotationYaw;
      float rotationPitch = down ? 90.0F : 0.0F;
      float f1 = MathHelper.func_76134_b(-rotationYaw * 0.017453292F - 3.1415927F);
      float f2 = MathHelper.func_76126_a(-rotationYaw * 0.017453292F - 3.1415927F);
      float f3 = -MathHelper.func_76134_b(-rotationPitch * 0.017453292F);
      float f4 = MathHelper.func_76126_a(-rotationPitch * 0.017453292F);
      Vec3 playerLook = Vec3.func_72443_a((double)(f2 * f3), (double)f4, (double)(f1 * f3));
      Vec3 playerViewOffset = Vec3.func_72443_a(playerPosition.field_72450_a + playerLook.field_72450_a * reachDistance, playerPosition.field_72448_b + playerLook.field_72448_b * reachDistance, playerPosition.field_72449_c + playerLook.field_72449_c * reachDistance);
      return world.func_147447_a(playerPosition, playerViewOffset, collisionFlag, !collisionFlag, false);
   }

   public void func_75249_e() {
      double SPEED = 0.6D;
      this.entity.func_70661_as().func_75492_a((double)this.mop.field_72311_b, (double)this.mop.field_72312_c, (double)this.mop.field_72309_d, 0.6D);
   }

   public boolean func_75253_b() {
      return this.entity != null && !this.entity.isWorshipping() && this.entity.func_70694_bm() != null && this.entity.func_70694_bm().func_77973_b() instanceof ItemPickaxe && this.entity.func_110167_bD() && this.mop != null;
   }

   public void func_75251_c() {
      if (this.entity.isWorking()) {
         this.entity.setWorking(false);
      }

   }

   public void func_75246_d() {
      double DROP_RANGE = 2.5D;
      double DROP_RANGE_SQ = 6.25D;
      double dist = this.entity.func_70092_e((double)this.mop.field_72311_b + 0.5D, (double)this.mop.field_72312_c + 0.5D, (double)this.mop.field_72309_d + 0.5D);
      boolean retry = true;
      if (dist <= 6.25D) {
         if (!this.entity.isWorking()) {
            this.entity.setWorking(true);
         }

         if (--this.waitTimer == 0) {
            if (!tryHarvestBlock(this.entity.field_70170_p, this.mop.field_72311_b, this.mop.field_72312_c, this.mop.field_72309_d, this.entity)) {
               retry = false;
            }

            this.mop = null;
            this.waitTimer = this.getNextHarvestDelay();
         }
      } else if (this.entity.func_70661_as().func_75500_f()) {
         this.mop = null;
         this.waitTimer = this.getNextHarvestDelay();
         if (this.entity.isWorking()) {
            this.entity.setWorking(false);
         }
      } else if (!this.entity.isWorking()) {
         this.entity.setWorking(true);
      }

      if (this.mop == null && retry && this.entity.field_70170_p.field_73012_v.nextInt(20) != 0) {
         MovingObjectPosition mop = raytraceBlocks(this.entity.field_70170_p, this.entity, true, 4.0D, false);
         if (mop != null && mop.field_72313_a == MovingObjectType.BLOCK) {
            Block block = BlockUtil.getBlock(this.entity.field_70170_p, mop);
            if (this.isMineable(block, this.entity.field_70170_p, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d)) {
               this.mop = mop;
               this.waitTimer = this.getNextHarvestDelay();
            }
         }
      }

   }

   private int getNextHarvestDelay() {
      return isHoldingKobolditePick(this.entity) ? 4 : 60;
   }

   private static boolean isHoldingKobolditePick(EntityLivingBase entity) {
      return entity.func_70694_bm() != null && entity.func_70694_bm().func_77973_b() == Witchery.Items.KOBOLDITE_PICKAXE;
   }

   public static boolean tryHarvestBlock(World world, int par1, int par2, int par3, EntityLivingBase harvester) {
      boolean kobolditePick = isHoldingKobolditePick(harvester);
      EntityPlayer minerPlayer = FakePlayerFactory.get((WorldServer)world, kobolditePick ? KOBOLDITE_MINER_PROFILE : NORMAL_MINER_PROFILE);
      return tryHarvestBlock(world, par1, par2, par3, harvester, minerPlayer);
   }

   public static boolean tryHarvestBlock(World world, int par1, int par2, int par3, EntityLivingBase harvester, EntityPlayer minerPlayer) {
      Block block = world.func_147439_a(par1, par2, par3);
      int blockMeta = world.func_72805_g(par1, par2, par3);
      BreakEvent event = new BreakEvent(par1, par2, par3, world, block, blockMeta, minerPlayer);
      event.setCanceled(false);
      MinecraftForge.EVENT_BUS.post(event);
      if (event.isCanceled()) {
         return false;
      } else {
         ItemStack stack = harvester.func_70694_bm();
         if (stack != null && stack.func_77973_b().onBlockStartBreak(stack, par1, par2, par3, minerPlayer)) {
            return false;
         } else {
            world.func_72926_e(2001, par1, par2, par3, Block.func_149682_b(block) + (blockMeta << 12));
            boolean canHarvest = false;
            if (block.func_149712_f(world, par1, par2, par3) >= 0.0F) {
               if (block.func_149688_o().func_76229_l()) {
                  canHarvest = true;
               }

               String tool = block.getHarvestTool(blockMeta);
               int toolLevel = stack != null ? stack.func_77973_b().getHarvestLevel(stack, tool) : 0;
               if (toolLevel < 0) {
                  canHarvest = true;
               }

               if (toolLevel >= block.getHarvestLevel(blockMeta)) {
                  canHarvest = true;
               }
            }

            if (canHarvest) {
               canHarvest = removeBlock(world, par1, par2, par3, minerPlayer);
               if (canHarvest) {
                  block.func_149636_a(world, minerPlayer, par1, par2, par3, blockMeta);
               }
            }

            return canHarvest;
         }
      }
   }

   private static boolean removeBlock(World world, int x, int y, int z, EntityPlayer player) {
      Block block = world.func_147439_a(x, y, z);
      int metadata = world.func_72805_g(x, y, z);
      block.func_149681_a(world, x, y, z, metadata, player);
      boolean flag = block.removedByPlayer(world, player, x, y, z, true);
      if (flag) {
         block.func_149664_b(world, x, y, z, metadata);
      }

      return flag;
   }

   public static void onHarvestDrops(EntityPlayer harvester, HarvestDropsEvent event) {
      if (harvester != null && !harvester.field_70170_p.field_72995_K && !event.isCanceled() && (isEqual(harvester.func_146103_bH(), KOBOLDITE_MINER_PROFILE) || isEqual(harvester.func_146103_bH(), NORMAL_MINER_PROFILE))) {
         boolean hasKobolditePick = isEqual(harvester.func_146103_bH(), KOBOLDITE_MINER_PROFILE);
         ArrayList<ItemStack> newDrops = new ArrayList();
         double kobolditeChance = hasKobolditePick ? 0.02D : 0.01D;
         Iterator i$ = event.drops.iterator();

         ItemStack drop;
         while(i$.hasNext()) {
            drop = (ItemStack)i$.next();
            int[] oreIDs = OreDictionary.getOreIDs(drop);
            boolean addOriginal = true;
            if (oreIDs.length > 0) {
               String oreName = OreDictionary.getOreName(oreIDs[0]);
               if (oreName != null && oreName.startsWith("ore")) {
                  ItemStack smeltedDrop = FurnaceRecipes.func_77602_a().func_151395_a(drop);
                  if (smeltedDrop != null && hasKobolditePick && harvester.field_70170_p.field_73012_v.nextDouble() < 0.5D) {
                     addOriginal = false;
                     newDrops.add(smeltedDrop.func_77946_l());
                     newDrops.add(smeltedDrop.func_77946_l());
                     if (harvester.field_70170_p.field_73012_v.nextDouble() < 0.25D) {
                        newDrops.add(smeltedDrop.func_77946_l());
                     }
                  }

                  kobolditeChance = hasKobolditePick ? 0.08D : 0.05D;
               }
            }

            if (addOriginal) {
               newDrops.add(drop);
            }
         }

         event.drops.clear();
         i$ = newDrops.iterator();

         while(i$.hasNext()) {
            drop = (ItemStack)i$.next();
            event.drops.add(drop);
         }

         if (kobolditeChance > 0.0D && harvester.field_70170_p.field_73012_v.nextDouble() < kobolditeChance) {
            event.drops.add(Witchery.Items.GENERIC.itemKobolditeDust.createStack());
         }
      }

   }

   private static boolean isEqual(GameProfile a, GameProfile b) {
      return a != null && b != null && a.getId() != null && b.getId() != null ? a.getId().equals(b.getId()) : false;
   }
}
