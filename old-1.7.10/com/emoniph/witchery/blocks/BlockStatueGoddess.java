package com.emoniph.witchery.blocks;

import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStatueGoddess extends BlockBaseContainer {
   public BlockStatueGoddess() {
      super(Material.field_151576_e, BlockStatueGoddess.TileEntityStatueGoddess.class);
      this.func_149722_s();
      this.func_149752_b(1000.0F);
      this.func_149711_c(2.5F);
      this.func_149672_a(field_149769_e);
      this.func_149676_a(0.0F, 0.0F, 0.1F, 1.0F, 2.0F, 0.9F);
   }

   public void func_149689_a(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
      int l = MathHelper.func_76128_c((double)(par5EntityLivingBase.field_70177_z * 4.0F / 360.0F) + 0.5D) & 3;
      if (l == 0) {
         par1World.func_72921_c(par2, par3, par4, 2, 2);
      }

      if (l == 1) {
         par1World.func_72921_c(par2, par3, par4, 5, 2);
      }

      if (l == 2) {
         par1World.func_72921_c(par2, par3, par4, 3, 2);
      }

      if (l == 3) {
         par1World.func_72921_c(par2, par3, par4, 4, 2);
      }

      if (!par1World.field_72995_K && par5EntityLivingBase instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)par5EntityLivingBase;
         BlockStatueGoddess.TileEntityStatueGoddess tile = (BlockStatueGoddess.TileEntityStatueGoddess)BlockUtil.getTileEntity(par1World, par2, par3, par4, BlockStatueGoddess.TileEntityStatueGoddess.class);
         if (tile != null) {
            tile.setOwner(player.func_70005_c_());
         }
      }

   }

   public void func_149699_a(World world, int x, int y, int z, EntityPlayer player) {
      if (!world.field_72995_K) {
         BlockStatueGoddess.TileEntityStatueGoddess tile = (BlockStatueGoddess.TileEntityStatueGoddess)BlockUtil.getTileEntity(world, x, y, z, BlockStatueGoddess.TileEntityStatueGoddess.class);
         if (tile != null && (player.field_71075_bZ.field_75098_d || player.func_70005_c_().equals(tile.getOwner()) && player.func_70093_af())) {
            for(int dy = y; world.func_147439_a(x, dy, z) == this; ++dy) {
               world.func_147468_f(x, dy, z);
               world.func_72838_d(new EntityItem(world, 0.5D + (double)x, 0.5D + (double)dy, 0.5D + (double)z, new ItemStack(this)));
            }
         }
      }

   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
      if (world.field_72995_K) {
         return super.func_149727_a(world, x, y, z, player, par6, par7, par8, par9);
      } else {
         NBTTagCompound nbtTag = Infusion.getNBT(player);
         if (nbtTag == null || !nbtTag.func_74764_b("witcheryCursed") && !nbtTag.func_74764_b("witcheryInsanity") && !nbtTag.func_74764_b("witcherySinking") && !nbtTag.func_74764_b("witcheryOverheating") && !nbtTag.func_74764_b("witcheryWakingNightmare")) {
            SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
         } else {
            if (nbtTag.func_74764_b("witcheryCursed")) {
               nbtTag.func_82580_o("witcheryCursed");
               ChatUtil.sendTranslated(EnumChatFormatting.BLUE, player, "tile.witcheryStatusGoddess.curemisfortune");
            }

            if (nbtTag.func_74764_b("witcheryInsanity")) {
               nbtTag.func_82580_o("witcheryInsanity");
               ChatUtil.sendTranslated(EnumChatFormatting.BLUE, player, "tile.witcheryStatusGoddess.cureinsanity");
            }

            if (nbtTag.func_74764_b("witcherySinking")) {
               nbtTag.func_82580_o("witcherySinking");
               ChatUtil.sendTranslated(EnumChatFormatting.BLUE, player, "tile.witcheryStatusGoddess.curesinking");
               Infusion.syncPlayer(world, player);
            }

            if (nbtTag.func_74764_b("witcheryOverheating")) {
               nbtTag.func_82580_o("witcheryOverheating");
               ChatUtil.sendTranslated(EnumChatFormatting.BLUE, player, "tile.witcheryStatusGoddess.cureoverheating");
            }

            if (nbtTag.func_74764_b("witcheryWakingNightmare")) {
               nbtTag.func_82580_o("witcheryWakingNightmare");
               ChatUtil.sendTranslated(EnumChatFormatting.BLUE, player, "tile.witcheryStatusGoddess.curenightmare");
            }

            if (player.func_70644_a(Potion.field_76436_u)) {
               player.func_82170_o(Potion.field_76436_u.field_76415_H);
            }

            if (player.func_70644_a(Potion.field_76437_t)) {
               player.func_82170_o(Potion.field_76437_t.field_76415_H);
            }

            if (player.func_70644_a(Potion.field_76440_q)) {
               player.func_82170_o(Potion.field_76440_q.field_76415_H);
            }

            if (player.func_70644_a(Potion.field_76419_f)) {
               player.func_82170_o(Potion.field_76419_f.field_76415_H);
            }

            if (player.func_70644_a(Potion.field_76421_d)) {
               player.func_82170_o(Potion.field_76421_d.field_76415_H);
            }

            ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_FIZZ, player, 1.0D, 2.0D, 8);
         }

         return true;
      }
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public int func_149745_a(Random rand) {
      return 1;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister par1IconRegister) {
      this.field_149761_L = par1IconRegister.func_94245_a("stone");
   }

   public static class TileEntityStatueGoddess extends TileEntity {
      private static final String OWNER_KEY = "WITCPlacer";
      private String owner;

      public boolean canUpdate() {
         return false;
      }

      public void setOwner(String username) {
         this.owner = username;
      }

      public String getOwner() {
         return this.owner != null ? this.owner : "";
      }

      public void func_145841_b(NBTTagCompound nbtTag) {
         super.func_145841_b(nbtTag);
         nbtTag.func_74778_a("WITCPlacer", this.getOwner());
      }

      public void func_145839_a(NBTTagCompound nbtTag) {
         super.func_145839_a(nbtTag);
         if (nbtTag.func_74764_b("WITCPlacer")) {
            this.owner = nbtTag.func_74779_i("WITCPlacer");
         } else {
            this.owner = "";
         }

      }
   }
}
