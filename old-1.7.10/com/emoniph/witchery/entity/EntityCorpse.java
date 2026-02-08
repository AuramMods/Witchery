package com.emoniph.witchery.entity;

import com.emoniph.witchery.dimension.WorldProviderDreamWorld;
import com.emoniph.witchery.util.Config;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityCorpse extends EntityLiving {
   private ThreadDownloadImageData downloadImageSkin;
   private ResourceLocation locationSkin;

   public EntityCorpse(World world) {
      super(world);
      this.func_70105_a(1.2F, 0.5F);
   }

   public boolean func_70104_M() {
      return false;
   }

   public boolean func_70067_L() {
      return super.func_70067_L();
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(20.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111266_c).func_111128_a(1.0D);
   }

   public void func_70091_d(double par1, double par3, double par5) {
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(17, "");
   }

   protected boolean func_70085_c(EntityPlayer par1EntityPlayer) {
      return true;
   }

   public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
      if (!this.field_70170_p.field_72995_K) {
         if (par1DamageSource.func_76364_f() != null && par1DamageSource.func_76364_f() instanceof EntityPlayer && ((EntityPlayer)par1DamageSource.func_76364_f()).field_71075_bZ.field_75098_d) {
            return super.func_70097_a(par1DamageSource, par2);
         } else {
            String username = this.getOwnerName();
            WorldServer[] arr$ = MinecraftServer.func_71276_C().field_71305_c;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               WorldServer world = arr$[i$];
               EntityPlayer player = world.func_72924_a(username);
               if (player != null) {
                  return super.func_70097_a(par1DamageSource, par2);
               }
            }

            return false;
         }
      } else {
         return super.func_70097_a(par1DamageSource, par2);
      }
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.body.name");
   }

   public boolean func_70650_aV() {
      return true;
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      if (this.getOwnerName() == null) {
         nbtRoot.func_74778_a("Owner", "");
      } else {
         nbtRoot.func_74778_a("Owner", this.getOwnerName());
      }

   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      String s = nbtRoot.func_74779_i("Owner");
      if (s.length() > 0) {
         this.setOwner(s);
      }

   }

   public String getOwnerName() {
      return this.field_70180_af.func_75681_e(17);
   }

   public void setOwner(String username) {
      this.func_110163_bv();
      this.field_70180_af.func_75692_b(17, username);
   }

   protected void setupCustomSkin() {
      String username = this.getOwnerName();
      this.locationSkin = AbstractClientPlayer.func_110311_f(username);
      this.downloadImageSkin = AbstractClientPlayer.func_110304_a(this.locationSkin, username);
   }

   public EntityPlayer getOwnerEntity() {
      return this.field_70170_p.func_72924_a(this.getOwnerName());
   }

   public void func_70645_a(DamageSource par1DamageSource) {
      super.func_70645_a(par1DamageSource);
      if (!this.field_70170_p.field_72995_K) {
         String username = this.getOwnerName();
         WorldServer[] arr$ = MinecraftServer.func_71276_C().field_71305_c;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            WorldServer world = arr$[i$];
            EntityPlayer player = world.func_72924_a(username);
            if (player != null) {
               if (player.field_71093_bK == Config.instance().dimensionDreamID) {
                  WorldProviderDreamWorld.returnPlayerToOverworld(player);
               } else if (WorldProviderDreamWorld.getPlayerIsGhost(player)) {
                  WorldProviderDreamWorld.returnGhostPlayerToSpiritWorld(player);
                  WorldProviderDreamWorld.returnPlayerToOverworld(player);
               }
               break;
            }
         }
      }

   }

   public void func_70636_d() {
      super.func_70636_d();
   }

   public ResourceLocation getLocationSkin() {
      if (this.locationSkin == null) {
         this.setupCustomSkin();
      }

      return this.locationSkin != null ? this.locationSkin : AbstractClientPlayer.field_110314_b;
   }
}
