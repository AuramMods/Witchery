package com.emoniph.witchery.util;

import com.emoniph.witchery.common.INullSource;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class Coord {
   public final int x;
   public final int y;
   public final int z;

   public Coord(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Coord(int x, int y, int z, ForgeDirection side) {
      this.x = x + side.offsetX;
      this.y = y + side.offsetY;
      this.z = z + side.offsetZ;
   }

   public Coord(TileEntity tileEntity) {
      this(tileEntity.field_145851_c, tileEntity.field_145848_d, tileEntity.field_145849_e);
   }

   public Coord(Entity entity) {
      this(MathHelper.func_76128_c(entity.field_70165_t), MathHelper.func_76128_c(entity.field_70163_u), MathHelper.func_76128_c(entity.field_70161_v));
   }

   public Coord(INullSource entity) {
      this(entity.getPosX(), entity.getPosY(), entity.getPosZ());
   }

   public Coord(MovingObjectPosition mop, EntityPosition alternativePos, boolean before) {
      if (mop != null) {
         switch(mop.field_72313_a) {
         case BLOCK:
            if (before) {
               this.x = mop.field_72311_b + (mop.field_72310_e == 4 ? -1 : (mop.field_72310_e == 5 ? 1 : 0));
               this.y = mop.field_72312_c + (mop.field_72310_e == 0 ? -1 : (mop.field_72310_e == 1 ? 1 : 0));
               this.z = mop.field_72309_d + (mop.field_72310_e == 2 ? -1 : (mop.field_72310_e == 3 ? 1 : 0));
            } else {
               this.x = mop.field_72311_b;
               this.y = mop.field_72312_c;
               this.z = mop.field_72309_d;
            }
            break;
         case ENTITY:
            this.x = MathHelper.func_76128_c(mop.field_72308_g.field_70165_t);
            this.y = MathHelper.func_76128_c(mop.field_72308_g.field_70163_u);
            this.z = MathHelper.func_76128_c(mop.field_72308_g.field_70161_v);
            break;
         case MISS:
         default:
            if (alternativePos != null) {
               this.x = MathHelper.func_76128_c(alternativePos.x);
               this.y = MathHelper.func_76128_c(alternativePos.y);
               this.z = MathHelper.func_76128_c(alternativePos.z);
            } else {
               this.x = 0;
               this.y = 0;
               this.z = 0;
            }
         }
      } else if (alternativePos != null) {
         this.x = MathHelper.func_76128_c(alternativePos.x);
         this.y = MathHelper.func_76128_c(alternativePos.y);
         this.z = MathHelper.func_76128_c(alternativePos.z);
      } else {
         this.x = 0;
         this.y = 0;
         this.z = 0;
      }

   }

   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         Coord other = (Coord)obj;
         return this.x == other.x && this.y == other.y && this.z == other.z;
      } else {
         return false;
      }
   }

   public boolean isAtPosition(TileEntity tileEntity) {
      return tileEntity != null && this.x == tileEntity.field_145851_c && this.y == tileEntity.field_145848_d && this.z == tileEntity.field_145849_e;
   }

   public Coord north() {
      return this.north(1);
   }

   public Coord north(int n) {
      return new Coord(this.x, this.y, this.z - n);
   }

   public Coord south() {
      return this.south(1);
   }

   public Coord south(int n) {
      return new Coord(this.x, this.y, this.z + n);
   }

   public Coord east() {
      return this.east(1);
   }

   public Coord east(int n) {
      return new Coord(this.x + n, this.y, this.z);
   }

   public Coord west() {
      return this.west(1);
   }

   public Coord west(int n) {
      return new Coord(this.x - n, this.y, this.z);
   }

   public Coord northEast() {
      return new Coord(this.x + 1, this.y, this.z - 1);
   }

   public Coord northWest() {
      return new Coord(this.x - 1, this.y, this.z - 1);
   }

   public Coord southEast() {
      return new Coord(this.x + 1, this.y, this.z + 1);
   }

   public Coord southWest() {
      return new Coord(this.x - 1, this.y, this.z + 1);
   }

   public Block getBlock(World world) {
      return this.getBlock(world, 0, 0, 0);
   }

   public Block getBlock(World world, int offsetX, int offsetY, int offsetZ) {
      return world.func_147439_a(this.x + offsetX, this.y + offsetY, this.z + offsetZ);
   }

   public TileEntity getBlockTileEntity(World world) {
      return this.getBlockTileEntity(world, 0, 0, 0);
   }

   public TileEntity getBlockTileEntity(World world, int offsetX, int offsetY, int offsetZ) {
      return world.func_147438_o(this.x + offsetX, this.y + offsetY, this.z + offsetZ);
   }

   public <T> T getTileEntity(IBlockAccess world, Class<T> clazz) {
      return BlockUtil.getTileEntity(world, this.x, this.y, this.z, clazz);
   }

   public int getBlockMetadata(World world) {
      return this.getBlockMetadata(world, 0, 0, 0);
   }

   public int getBlockMetadata(World world, int offsetX, int offsetY, int offsetZ) {
      return world.func_72805_g(this.x + offsetX, this.y + offsetY, this.z + offsetZ);
   }

   public void setNBT(NBTTagCompound nbtTag, String key) {
      nbtTag.func_74768_a(key + "X", this.x);
      nbtTag.func_74768_a(key + "Y", this.y);
      nbtTag.func_74768_a(key + "Z", this.z);
   }

   public static double distance(Coord first, Coord second) {
      double dX = (double)(first.x - second.x);
      double dY = (double)(first.y - second.y);
      double dZ = (double)(first.z - second.z);
      return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
   }

   public static double distance(double firstX, double firstY, double firstZ, double secondX, double secondY, double secondZ) {
      double dX = firstX - secondX;
      double dY = firstY - secondY;
      double dZ = firstZ - secondZ;
      return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
   }

   public static double distanceSq(double firstX, double firstY, double firstZ, double secondX, double secondY, double secondZ) {
      double dX = firstX - secondX;
      double dY = firstY - secondY;
      double dZ = firstZ - secondZ;
      return dX * dX + dY * dY + dZ * dZ;
   }

   public double distanceTo(Coord other) {
      double dX = (double)(other.x - this.x);
      double dY = (double)(other.y - this.y);
      double dZ = (double)(other.z - this.z);
      return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
   }

   public double distanceSqTo(Coord other) {
      double dX = (double)(other.x - this.x);
      double dY = (double)(other.y - this.y);
      double dZ = (double)(other.z - this.z);
      return dX * dX + dY * dY + dZ * dZ;
   }

   public double distanceSqTo(int x, int y, int z) {
      double dX = (double)(x - this.x);
      double dY = (double)(y - this.y);
      double dZ = (double)(z - this.z);
      return dX * dX + dY * dY + dZ * dZ;
   }

   public static Coord createFrom(NBTTagCompound nbtTag, String key) {
      return nbtTag.func_74764_b(key + "X") && nbtTag.func_74764_b(key + "Y") && nbtTag.func_74764_b(key + "Z") ? new Coord(nbtTag.func_74762_e(key + "X"), nbtTag.func_74762_e(key + "Y"), nbtTag.func_74762_e(key + "Z")) : null;
   }

   public boolean isWestOf(Coord coord) {
      return this.x < coord.x;
   }

   public boolean isNorthOf(Coord coord) {
      return this.z < coord.z;
   }

   public boolean setBlock(World world, Block block) {
      return world.func_147449_b(this.x, this.y, this.z, block);
   }

   public boolean setBlock(World world, Block block, int metadata, int flags) {
      return world.func_147465_d(this.x, this.y, this.z, block, metadata, flags);
   }

   public void setAir(World world) {
      world.func_147468_f(this.x, this.y, this.z);
   }

   public void markBlockForUpdate(World world) {
      world.func_147471_g(this.x, this.y, this.z);
   }

   public int getHeading(Coord destination) {
      double dX = (double)(this.x - destination.x);
      double dZ = (double)(this.z - destination.z);
      double yaw = Math.atan2(dZ, dX);
      double PI8 = 0.39269908169872414D;
      double PI2 = 1.5707963267948966D;
      if (yaw > -0.39269908169872414D && yaw <= 0.39269908169872414D) {
         return 6;
      } else if (yaw > 0.39269908169872414D && yaw <= 1.1780972450961724D) {
         return 7;
      } else if (yaw > 1.1780972450961724D && yaw <= 1.9634954084936207D) {
         return 0;
      } else if (yaw > 1.9634954084936207D && yaw <= 2.748893571891069D) {
         return 1;
      } else if (!(yaw > 2.748893571891069D) && !(yaw <= -2.748893571891069D)) {
         if (yaw > -2.748893571891069D && yaw <= -1.9634954084936207D) {
            return 3;
         } else {
            return yaw > -1.9634954084936207D && yaw <= -1.1780972450961724D ? 4 : 5;
         }
      } else {
         return 2;
      }
   }

   public String toString() {
      return String.format("%d, %d, %d", this.x, this.y, this.z);
   }

   public NBTTagCompound toTagNBT() {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.func_74768_a("posX", this.x);
      nbt.func_74768_a("posY", this.y);
      nbt.func_74768_a("posZ", this.z);
      return nbt;
   }

   public static Coord fromTagNBT(NBTTagCompound nbt) {
      return nbt.func_74764_b("posX") && nbt.func_74764_b("posY") && nbt.func_74764_b("posZ") ? new Coord(nbt.func_74762_e("posX"), nbt.func_74762_e("posY"), nbt.func_74762_e("posZ")) : null;
   }

   public boolean isMatch(int x, int y, int z) {
      return this.x == x && this.y == y && this.z == z;
   }
}
