package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.item.ItemAlluringSkull;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockAlluringSkull extends BlockBaseContainer {
   private static final int UPDATE_FREQUENCY = 100;

   public BlockAlluringSkull() {
      super(Material.field_151594_q, BlockAlluringSkull.TileEntityAlluringSkull.class, ItemAlluringSkull.class);
      this.func_149715_a(0.5F);
      this.func_149672_a(field_149769_e);
      this.func_149722_s();
      this.func_149752_b(1000.0F);
      this.func_149676_a(0.25F, 0.0F, 0.25F, 0.75F, 0.5F, 0.75F);
   }

   public int func_149645_b() {
      return -1;
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public boolean func_149727_a(World world, int posX, int posY, int posZ, EntityPlayer player, int par6, float par7, float par8, float par9) {
      if (!world.field_72995_K) {
         BlockAlluringSkull.TileEntityAlluringSkull tileEntity = (BlockAlluringSkull.TileEntityAlluringSkull)world.func_147438_o(posX, posY, posZ);
         int type = tileEntity.getSkullType();
         ItemStack itemstack = player.func_70694_bm();
         if (itemstack != null && Witchery.Items.GENERIC.itemNecroStone.isMatch(itemstack)) {
            if (type == 0) {
               ParticleEffect.FLAME.send(SoundEffect.MOB_HORSE_SKELETON_DEATH, world, 0.5D + (double)posX, 0.3D + (double)posY, 0.5D + (double)posZ, 0.5D, 0.5D, 16);
               tileEntity.setSkullType(type == 0 ? 1 : 0);
            } else {
               ParticleEffect.EXPLODE.send(SoundEffect.MOB_HORSE_SKELETON_HIT, world, 0.5D + (double)posX, 0.3D + (double)posY, 0.5D + (double)posZ, 0.5D, 0.5D, 16);
               world.func_147468_f(posX, posY, posZ);
               world.func_72838_d(new EntityItem(world, 0.5D + (double)posX, 0.8D + (double)posY, 0.5D + (double)posZ, new ItemStack(this)));
            }

            return true;
         }
      }

      return super.func_149727_a(world, posX, posY, posZ, player, par6, par7, par8, par9);
   }

   public void func_149719_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
      int l = par1IBlockAccess.func_72805_g(par2, par3, par4) & 7;
      switch(l) {
      case 1:
      default:
         this.func_149676_a(0.25F, 0.0F, 0.25F, 0.75F, 0.5F, 0.75F);
         break;
      case 2:
         this.func_149676_a(0.25F, 0.25F, 0.5F, 0.75F, 0.75F, 1.0F);
         break;
      case 3:
         this.func_149676_a(0.25F, 0.25F, 0.0F, 0.75F, 0.75F, 0.5F);
         break;
      case 4:
         this.func_149676_a(0.5F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
         break;
      case 5:
         this.func_149676_a(0.0F, 0.25F, 0.25F, 0.5F, 0.75F, 0.75F);
      }

   }

   public AxisAlignedBB func_149668_a(World par1World, int par2, int par3, int par4) {
      this.func_149719_a(par1World, par2, par3, par4);
      return super.func_149668_a(par1World, par2, par3, par4);
   }

   public void func_149689_a(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
      int l = MathHelper.func_76128_c((double)(par5EntityLivingBase.field_70177_z * 4.0F / 360.0F) + 2.5D) & 3;
      par1World.func_72921_c(par2, par3, par4, l, 2);
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return new ItemStack(this);
   }

   public int func_149643_k(World par1World, int par2, int par3, int par4) {
      TileEntity tileentity = par1World.func_147438_o(par2, par3, par4);
      return tileentity != null && tileentity instanceof BlockAlluringSkull.TileEntityAlluringSkull ? ((BlockAlluringSkull.TileEntityAlluringSkull)tileentity).getSkullType() : super.func_149643_k(par1World, par2, par3, par4);
   }

   public int func_149692_a(int par1) {
      return par1;
   }

   public void func_149681_a(World par1World, int par2, int par3, int par4, int par5, EntityPlayer par6EntityPlayer) {
      if (par6EntityPlayer.field_71075_bZ.field_75098_d) {
         par5 |= 8;
         par1World.func_72921_c(par2, par3, par4, par5, 4);
      }

      this.func_149697_b(par1World, par2, par3, par4, par5, 0);
      super.func_149681_a(par1World, par2, par3, par4, par5, par6EntityPlayer);
   }

   public void func_149749_a(World par1World, int par2, int par3, int par4, Block par5, int par6) {
      super.func_149749_a(par1World, par2, par3, par4, par5, par6);
   }

   public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
      ArrayList<ItemStack> drops = new ArrayList();
      if ((metadata & 8) == 0) {
         ItemStack itemstack = new ItemStack(this);
         BlockAlluringSkull.TileEntityAlluringSkull tileentityskull = (BlockAlluringSkull.TileEntityAlluringSkull)world.func_147438_o(x, y, z);
         if (tileentityskull == null) {
            return drops;
         }

         drops.add(itemstack);
      }

      return drops;
   }

   public Item func_149650_a(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
      return Item.func_150898_a(this);
   }

   private boolean func_82528_d(World par1World, int par2, int par3, int par4, int par5) {
      if (par1World.func_147439_a(par2, par3, par4) != this) {
         return false;
      } else {
         TileEntity tileentity = par1World.func_147438_o(par2, par3, par4);
         return tileentity != null && tileentity instanceof BlockAlluringSkull.TileEntityAlluringSkull ? ((BlockAlluringSkull.TileEntityAlluringSkull)tileentity).getSkullType() == par5 : false;
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister par1IconRegister) {
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int par1, int par2) {
      return Blocks.field_150425_aM.func_149733_h(par1);
   }

   @SideOnly(Side.CLIENT)
   public String func_149702_O() {
      return this.func_149641_N();
   }

   public static void allure(World world, double posX, double posY, double posZ, int quad) {
      try {
         float r = 64.0F;
         float dy = 10.0F;
         AxisAlignedBB bounds = null;
         switch(quad) {
         case 0:
            bounds = AxisAlignedBB.func_72330_a(posX, posY - 10.0D, posZ - 64.0D, posX + 64.0D, posY, posZ);
            break;
         case 1:
            bounds = AxisAlignedBB.func_72330_a(posX - 64.0D, posY - 10.0D, posZ - 64.0D, posX, posY, posZ);
            break;
         case 2:
            bounds = AxisAlignedBB.func_72330_a(posX, posY - 10.0D, posZ, posX + 64.0D, posY, posZ + 64.0D);
            break;
         case 3:
            bounds = AxisAlignedBB.func_72330_a(posX - 64.0D, posY - 10.0D, posZ, posX, posY, posZ + 64.0D);
            break;
         case 4:
            bounds = AxisAlignedBB.func_72330_a(posX - 64.0D, posY + 1.0D, posZ - 64.0D, posX, posY + 10.0D, posZ);
            break;
         case 5:
            bounds = AxisAlignedBB.func_72330_a(posX, posY + 1.0D, posZ, posX + 64.0D, posY + 10.0D, posZ + 64.0D);
            break;
         case 6:
            bounds = AxisAlignedBB.func_72330_a(posX - 64.0D, posY + 1.0D, posZ, posX, posY + 10.0D, posZ + 64.0D);
            break;
         case 7:
         default:
            bounds = AxisAlignedBB.func_72330_a(posX, posY + 1.0D, posZ - 64.0D, posX + 64.0D, posY + 10.0D, posZ);
         }

         Iterator i$ = world.func_72872_a(EntityCreature.class, bounds).iterator();

         while(i$.hasNext()) {
            Object obj = i$.next();
            EntityCreature creature = (EntityCreature)obj;
            if (creature.func_70668_bt() == EnumCreatureAttribute.UNDEAD && !creature.func_70661_as().func_75492_a(posX, posY, posZ, 1.0D)) {
               int x = MathHelper.func_76128_c(posX);
               int y = MathHelper.func_76128_c(posY);
               int z = MathHelper.func_76128_c(posZ);
               PathEntity path = world.func_72844_a(creature, x, y, z, 10.0F, true, false, false, true);
               if (path != null) {
                  creature.func_70778_a(path);
               }
            }
         }
      } catch (Exception var18) {
         Log.instance().debug(String.format("Exception occurred alluring with a skull! %s", var18.toString()));
      }

   }

   public static class TileEntityAlluringSkull extends TileEntityBase {
      private int skullType;
      private int skullRotation;
      private int quad = 0;

      public void func_145845_h() {
         super.func_145845_h();
         if (!this.field_145850_b.field_72995_K && this.skullType == 1 && this.ticks % 100L == 0L) {
            if (++this.quad >= 8) {
               this.quad = 0;
            }

            BlockAlluringSkull.allure(this.field_145850_b, (double)this.field_145851_c, (double)this.field_145848_d, (double)this.field_145849_e, this.quad);
         }

      }

      public void func_145841_b(NBTTagCompound par1NBTTagCompound) {
         super.func_145841_b(par1NBTTagCompound);
         par1NBTTagCompound.func_74774_a("SkullType", (byte)(this.skullType & 255));
         par1NBTTagCompound.func_74774_a("Rot", (byte)(this.skullRotation & 255));
      }

      public void func_145839_a(NBTTagCompound par1NBTTagCompound) {
         super.func_145839_a(par1NBTTagCompound);
         this.skullType = par1NBTTagCompound.func_74771_c("SkullType");
         this.skullRotation = par1NBTTagCompound.func_74771_c("Rot");
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

      public void setSkullType(int par1) {
         this.skullType = par1;
         this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
      }

      public int getSkullType() {
         return this.skullType;
      }

      public void setSkullRotation(int par1) {
         this.skullRotation = par1;
      }

      @SideOnly(Side.CLIENT)
      public int func_82119_b() {
         return this.skullRotation;
      }
   }
}
