package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.entity.EntityFamiliar;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAIFamiliarFindDiamonds extends EntityAIBase {
   private final EntityFamiliar theFamiliar;
   private final double field_75404_b;
   private int currentTick;
   private int field_75402_d;
   private int maxSittingTicks;
   private int sitableBlockX;
   private int sitableBlockY;
   private int sitableBlockZ;

   public EntityAIFamiliarFindDiamonds(EntityFamiliar familiarEntity, double par2) {
      this.theFamiliar = familiarEntity;
      this.field_75404_b = par2;
      this.func_75248_a(5);
   }

   public boolean func_75250_a() {
      EntityLivingBase entitylivingbase = this.theFamiliar.func_70902_q();
      return this.theFamiliar.func_70909_n() && !this.theFamiliar.func_70906_o() && this.theFamiliar.getBlockIDToFind() != null && entitylivingbase != null && this.theFamiliar.func_70068_e(entitylivingbase) < 100.0D && this.theFamiliar.func_70681_au().nextDouble() <= 0.1D && this.getNearbySitableBlockDistance();
   }

   public boolean func_75253_b() {
      EntityLivingBase entitylivingbase = this.theFamiliar.func_70902_q();
      return this.currentTick <= this.maxSittingTicks && this.field_75402_d <= 60 && entitylivingbase != null && this.theFamiliar.func_70068_e(entitylivingbase) < 100.0D && this.isSittableBlock(this.theFamiliar.field_70170_p, this.sitableBlockX, this.sitableBlockY, this.sitableBlockZ);
   }

   public void func_75249_e() {
      if (!this.theFamiliar.func_70661_as().func_75492_a((double)this.sitableBlockX + 0.5D, (double)(this.sitableBlockY + 1), (double)this.sitableBlockZ + 0.5D, this.field_75404_b)) {
         this.theFamiliar.func_70661_as().func_75492_a((double)this.sitableBlockX + 0.5D, this.theFamiliar.field_70163_u + 1.0D, (double)this.sitableBlockZ + 0.5D, this.field_75404_b);
      }

      this.currentTick = 0;
      this.field_75402_d = 0;
      this.maxSittingTicks = this.theFamiliar.func_70681_au().nextInt(this.theFamiliar.func_70681_au().nextInt(1200) + 1200) + 1200;
      this.theFamiliar.func_70907_r().func_75270_a(false);
   }

   public void func_75251_c() {
      this.theFamiliar.func_70904_g(false);
   }

   public void func_75246_d() {
      ++this.currentTick;
      this.theFamiliar.func_70907_r().func_75270_a(false);
      if (this.theFamiliar.func_70092_e((double)this.sitableBlockX, this.theFamiliar.field_70163_u, (double)this.sitableBlockZ) > 2.0D) {
         this.theFamiliar.func_70904_g(false);
         if (!this.theFamiliar.func_70661_as().func_75492_a((double)this.sitableBlockX + 0.5D, (double)(this.sitableBlockY + 1), (double)this.sitableBlockZ + 0.5D, this.field_75404_b)) {
            this.theFamiliar.func_70661_as().func_75492_a((double)this.sitableBlockX + 0.5D, this.theFamiliar.field_70163_u, (double)this.sitableBlockZ + 0.5D, this.field_75404_b);
         }

         ++this.field_75402_d;
      } else if (!this.theFamiliar.func_70906_o()) {
         EntityLivingBase living = this.theFamiliar.func_70902_q();
         if (living != null && living instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)living;
            SoundEffect.RANDOM_ORB.playAtPlayer(this.theFamiliar.field_70170_p, player);
            ChatUtil.sendTranslated(EnumChatFormatting.LIGHT_PURPLE, player, "witchery.familiar.foundsomething", Integer.valueOf(MathHelper.func_76128_c(this.theFamiliar.field_70165_t)).toString(), Integer.valueOf(MathHelper.func_76128_c(this.theFamiliar.field_70163_u)).toString(), Integer.valueOf(MathHelper.func_76128_c(this.theFamiliar.field_70161_v)).toString());
         }

         this.theFamiliar.clearItemToFind();
         this.theFamiliar.setAISitting(true);
      } else {
         --this.field_75402_d;
      }

   }

   protected boolean getNearbySitableBlockDistance() {
      int MAX_WIDTH = true;
      int DEPTH = this.theFamiliar.getDepthToFind();

      for(int i = 1; (double)i < (double)DEPTH; ++i) {
         for(int j = (int)this.theFamiliar.field_70165_t - 4; (double)j < this.theFamiliar.field_70165_t + 4.0D; ++j) {
            for(int k = (int)this.theFamiliar.field_70161_v - 4; (double)k < this.theFamiliar.field_70161_v + 4.0D; ++k) {
               if (this.isSittableBlock(this.theFamiliar.field_70170_p, j, i, k)) {
                  this.sitableBlockX = j;
                  this.sitableBlockY = i;
                  this.sitableBlockZ = k;
                  return true;
               }
            }
         }
      }

      return false;
   }

   protected boolean isSittableBlock(World par1World, int par2, int par3, int par4) {
      Block blockID = this.theFamiliar.getBlockIDToFind();
      Block foundBlockID = par1World.func_147439_a(par2, par3, par4);
      if (blockID == Blocks.field_150482_ag) {
         return foundBlockID == blockID || foundBlockID == Blocks.field_150412_bA;
      } else if (blockID != Blocks.field_150412_bA) {
         return foundBlockID == blockID;
      } else {
         return foundBlockID == blockID || foundBlockID == Blocks.field_150482_ag;
      }
   }
}
