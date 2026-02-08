package com.emoniph.witchery.worldgen;

import com.emoniph.witchery.entity.EntityVillageGuard;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;

public class ComponentVillageKeep extends Village {
   public static final WeightedRandomChestContent[] villageTowerChestContents;
   private boolean hasMadeChest;
   private int guardsSpawned;

   public static ComponentVillageKeep construct(Start start, List pieces, Random rand, int p1, int p2, int p3, int p4, int p5) {
      StructureBoundingBox bounds = StructureBoundingBox.func_78889_a(p1, p2, p3, 0, 0, 0, 16, 26, 16, p4);
      return func_74895_a(bounds) && StructureComponent.func_74883_a(pieces, bounds) == null ? new ComponentVillageKeep(start, p5, rand, bounds, p4) : null;
   }

   public ComponentVillageKeep() {
   }

   public ComponentVillageKeep(Start start, int componentType, Random rand, StructureBoundingBox bounds, int coordMode) {
      super(start, componentType);
      this.field_74885_f = coordMode;
      this.field_74887_e = bounds;
   }

   private void fill(World world, StructureBoundingBox bounds, int x, int y, int z, int w, int h, int d, Block block) {
      this.func_151549_a(world, bounds, x, y, z, x + w - 1, y + h - 1, z + d - 1, block, block, false);
   }

   public boolean func_74875_a(World world, Random rand, StructureBoundingBox bounds) {
      int height = true;
      if (this.field_143015_k < 0) {
         this.field_143015_k = this.func_74889_b(world, bounds);
         if (this.field_143015_k < 0) {
            return true;
         }

         this.field_74887_e.func_78886_a(0, this.field_143015_k - this.field_74887_e.field_78894_e + 26 - 1, 0);
      }

      this.func_151549_a(world, bounds, 1, 1, 1, 14, 26, 14, Blocks.field_150350_a, Blocks.field_150350_a, false);
      this.drawTower(world, bounds, 0, 0);
      this.drawTower(world, bounds, 8, 4);
      this.fill(world, bounds, 7, 0, 2, 3, 1, 3, Blocks.field_150347_e);
      this.fill(world, bounds, 7, 4, 3, 3, 1, 2, Blocks.field_150347_e);
      this.fill(world, bounds, 7, 5, 2, 3, 1, 1, Blocks.field_150347_e);
      this.func_151550_a(world, Blocks.field_150347_e, 0, 8, 6, 2, bounds);
      int meta = this.func_151555_a(Blocks.field_150364_r, 8);

      int x;
      for(x = 7; x <= 9; ++x) {
         this.func_151550_a(world, Blocks.field_150364_r, meta, x, 4, 2, bounds);
      }

      this.fill(world, bounds, 7, 3, 3, 3, 1, 1, Blocks.field_150422_aJ);
      this.func_151550_a(world, Blocks.field_150333_U, 11, 7, 3, 2, bounds);
      this.func_151550_a(world, Blocks.field_150333_U, 11, 7, 3, 4, bounds);
      this.func_151550_a(world, Blocks.field_150333_U, 11, 9, 3, 2, bounds);
      this.func_151550_a(world, Blocks.field_150333_U, 11, 9, 3, 4, bounds);
      meta = this.func_151555_a(Blocks.field_150446_ar, 3);
      int meta2 = this.func_151555_a(Blocks.field_150446_ar, 2);

      for(x = 7; x <= 9; ++x) {
         this.func_151550_a(world, Blocks.field_150446_ar, meta, x, 0, 1, bounds);
         this.func_151550_a(world, Blocks.field_150446_ar, meta2, x, 0, 4, bounds);
      }

      this.fill(world, bounds, 2, 0, 9, 4, 16, 1, Blocks.field_150347_e);
      this.fill(world, bounds, 2, 0, 14, 4, 16, 1, Blocks.field_150347_e);
      this.fill(world, bounds, 1, 0, 10, 1, 16, 4, Blocks.field_150347_e);
      this.fill(world, bounds, 6, 0, 10, 1, 16, 4, Blocks.field_150347_e);
      this.fill(world, bounds, 2, 0, 10, 4, 1, 4, Blocks.field_150347_e);
      this.fill(world, bounds, 1, 4, 9, 6, 1, 6, Blocks.field_150347_e);
      this.fill(world, bounds, 1, 9, 9, 6, 1, 6, Blocks.field_150347_e);
      this.fill(world, bounds, 1, 14, 9, 6, 1, 6, Blocks.field_150347_e);
      this.fill(world, bounds, 3, 16, 9, 2, 1, 1, Blocks.field_150347_e);
      this.fill(world, bounds, 3, 16, 14, 2, 1, 1, Blocks.field_150347_e);
      this.fill(world, bounds, 1, 16, 11, 1, 1, 2, Blocks.field_150347_e);
      this.fill(world, bounds, 6, 16, 11, 1, 1, 2, Blocks.field_150347_e);
      this.fill(world, bounds, 3, 1, 14, 2, 3, 1, Blocks.field_150364_r);
      this.fill(world, bounds, 1, 1, 11, 1, 3, 2, Blocks.field_150364_r);
      this.fill(world, bounds, 3, 11, 9, 2, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 3, 6, 14, 2, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 3, 11, 14, 2, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 1, 6, 11, 1, 2, 2, Blocks.field_150411_aY);
      this.fill(world, bounds, 1, 11, 11, 1, 2, 2, Blocks.field_150411_aY);
      this.fill(world, bounds, 6, 11, 11, 1, 2, 2, Blocks.field_150411_aY);
      this.fill(world, bounds, 4, 1, 9, 1, 2, 1, Blocks.field_150350_a);
      this.fill(world, bounds, 4, 5, 9, 1, 2, 1, Blocks.field_150350_a);
      this.fill(world, bounds, 6, 1, 11, 1, 2, 1, Blocks.field_150350_a);
      this.fill(world, bounds, 6, 5, 11, 1, 2, 1, Blocks.field_150350_a);
      this.func_151550_a(world, Blocks.field_150364_r, this.func_151555_a(Blocks.field_150364_r, 8), 4, 7, 9, bounds);
      this.func_151550_a(world, Blocks.field_150364_r, this.func_151555_a(Blocks.field_150364_r, 4), 6, 7, 11, bounds);
      meta = this.func_151555_a(Blocks.field_150468_ap, 2);

      int n;
      for(n = 1; n <= 14; ++n) {
         this.func_151550_a(world, Blocks.field_150468_ap, meta, 2, n, 10, bounds);
      }

      this.func_151550_a(world, Blocks.field_150478_aa, 0, 2, 2, 13, bounds);
      this.func_151550_a(world, Blocks.field_150478_aa, 0, 2, 6, 13, bounds);
      this.func_151550_a(world, Blocks.field_150478_aa, 0, 2, 11, 13, bounds);
      this.fill(world, bounds, 11, 0, 9, 3, 19, 1, Blocks.field_150347_e);
      this.fill(world, bounds, 11, 0, 13, 3, 19, 1, Blocks.field_150347_e);
      this.fill(world, bounds, 10, 0, 10, 1, 19, 3, Blocks.field_150347_e);
      this.fill(world, bounds, 14, 0, 10, 1, 19, 3, Blocks.field_150347_e);
      this.fill(world, bounds, 11, 0, 10, 3, 1, 3, Blocks.field_150347_e);
      this.fill(world, bounds, 10, 4, 9, 5, 1, 5, Blocks.field_150347_e);
      this.fill(world, bounds, 10, 9, 9, 5, 1, 5, Blocks.field_150347_e);
      this.fill(world, bounds, 10, 14, 9, 5, 1, 5, Blocks.field_150347_e);
      this.fill(world, bounds, 10, 19, 9, 5, 1, 5, Blocks.field_150347_e);
      this.fill(world, bounds, 12, 1, 13, 1, 3, 1, Blocks.field_150364_r);
      this.fill(world, bounds, 14, 1, 11, 1, 3, 1, Blocks.field_150364_r);
      this.fill(world, bounds, 12, 6, 13, 1, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 12, 11, 9, 1, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 12, 16, 9, 1, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 12, 11, 13, 1, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 12, 16, 13, 1, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 14, 6, 11, 1, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 14, 11, 11, 1, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 14, 16, 11, 1, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 10, 11, 11, 1, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 10, 16, 11, 1, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 12, 5, 9, 1, 2, 1, Blocks.field_150350_a);
      this.fill(world, bounds, 12, 1, 9, 1, 2, 1, Blocks.field_150350_a);
      this.fill(world, bounds, 10, 5, 11, 1, 2, 1, Blocks.field_150350_a);
      this.fill(world, bounds, 10, 1, 11, 1, 2, 1, Blocks.field_150350_a);
      this.func_151550_a(world, Blocks.field_150364_r, this.func_151555_a(Blocks.field_150364_r, 8), 12, 7, 9, bounds);
      this.func_151550_a(world, Blocks.field_150364_r, this.func_151555_a(Blocks.field_150364_r, 4), 10, 7, 11, bounds);
      meta = this.func_151555_a(Blocks.field_150468_ap, 2);

      for(n = 1; n <= 14; ++n) {
         this.func_151550_a(world, Blocks.field_150468_ap, meta, 11, n, 10, bounds);
      }

      this.func_151550_a(world, Blocks.field_150478_aa, 0, 11, 2, 12, bounds);
      this.func_151550_a(world, Blocks.field_150478_aa, 0, 11, 6, 12, bounds);
      this.func_151550_a(world, Blocks.field_150478_aa, 0, 11, 11, 12, bounds);
      this.func_151550_a(world, Blocks.field_150478_aa, 0, 11, 16, 12, bounds);
      this.func_151550_a(world, Blocks.field_150364_r, 0, 11, 19, 10, bounds);
      this.fill(world, bounds, 10, 20, 9, 5, 2, 5, Blocks.field_150344_f);
      this.fill(world, bounds, 11, 22, 10, 3, 2, 3, Blocks.field_150344_f);
      this.fill(world, bounds, 12, 24, 11, 1, 2, 1, Blocks.field_150344_f);
      this.fill(world, bounds, 11, 20, 10, 3, 2, 3, Blocks.field_150350_a);
      n = this.func_151555_a(Blocks.field_150476_ad, 3);
      int s = this.func_151555_a(Blocks.field_150476_ad, 2);
      int w = this.func_151555_a(Blocks.field_150476_ad, 0);
      int e = this.func_151555_a(Blocks.field_150476_ad, 1);

      for(x = 9; x <= 15; ++x) {
         this.func_151550_a(world, Blocks.field_150476_ad, n, x, 20, 8, bounds);
         this.func_151550_a(world, Blocks.field_150476_ad, s, x, 20, 14, bounds);
      }

      for(x = 10; x <= 14; ++x) {
         this.func_151550_a(world, Blocks.field_150476_ad, n, x, 22, 9, bounds);
         this.func_151550_a(world, Blocks.field_150476_ad, s, x, 22, 13, bounds);
      }

      for(x = 11; x <= 13; ++x) {
         this.func_151550_a(world, Blocks.field_150476_ad, n, x, 24, 10, bounds);
         this.func_151550_a(world, Blocks.field_150476_ad, s, x, 24, 12, bounds);
      }

      int z;
      for(z = 9; z <= 13; ++z) {
         this.func_151550_a(world, Blocks.field_150476_ad, w, 9, 20, z, bounds);
         this.func_151550_a(world, Blocks.field_150476_ad, e, 15, 20, z, bounds);
      }

      for(z = 10; z <= 12; ++z) {
         this.func_151550_a(world, Blocks.field_150476_ad, w, 10, 22, z, bounds);
         this.func_151550_a(world, Blocks.field_150476_ad, e, 14, 22, z, bounds);
      }

      this.func_151550_a(world, Blocks.field_150476_ad, w, 11, 24, 11, bounds);
      this.func_151550_a(world, Blocks.field_150476_ad, e, 13, 24, 11, bounds);
      this.fill(world, bounds, 7, 0, 11, 3, 1, 2, Blocks.field_150347_e);
      this.fill(world, bounds, 7, 4, 11, 3, 1, 1, Blocks.field_150347_e);
      this.fill(world, bounds, 7, 1, 12, 3, 5, 1, Blocks.field_150347_e);
      this.func_151550_a(world, Blocks.field_150347_e, 0, 8, 6, 12, bounds);
      this.fill(world, bounds, 7, 1, 12, 1, 4, 1, Blocks.field_150364_r);
      this.func_151550_a(world, Blocks.field_150478_aa, 0, 8, 2, 11, bounds);
      this.fill(world, bounds, 9, 1, 12, 1, 4, 1, Blocks.field_150364_r);
      meta = this.func_151555_a(Blocks.field_150446_ar, 3);

      for(x = 7; x <= 9; ++x) {
         this.func_151550_a(world, Blocks.field_150446_ar, meta, x, 0, 10, bounds);
      }

      this.func_151550_a(world, Blocks.field_150333_U, 11, 7, 3, 11, bounds);
      this.func_151550_a(world, Blocks.field_150333_U, 11, 9, 3, 11, bounds);
      this.fill(world, bounds, 3, 0, 6, 2, 1, 3, Blocks.field_150347_e);
      this.fill(world, bounds, 4, 4, 6, 1, 1, 3, Blocks.field_150347_e);
      this.fill(world, bounds, 3, 1, 6, 1, 5, 3, Blocks.field_150347_e);
      this.func_151550_a(world, Blocks.field_150347_e, 0, 3, 6, 7, bounds);
      this.fill(world, bounds, 3, 1, 6, 1, 4, 1, Blocks.field_150364_r);
      this.func_151550_a(world, Blocks.field_150478_aa, 0, 4, 2, 7, bounds);
      this.fill(world, bounds, 3, 1, 8, 1, 4, 1, Blocks.field_150364_r);
      meta = this.func_151555_a(Blocks.field_150446_ar, 1);

      for(z = 6; z <= 8; ++z) {
         this.func_151550_a(world, Blocks.field_150446_ar, meta, 5, 0, z, bounds);
      }

      this.func_151550_a(world, Blocks.field_150333_U, 11, 4, 3, 6, bounds);
      this.func_151550_a(world, Blocks.field_150333_U, 11, 4, 3, 8, bounds);
      this.fill(world, bounds, 12, 0, 6, 2, 1, 3, Blocks.field_150347_e);
      this.fill(world, bounds, 12, 4, 6, 1, 1, 3, Blocks.field_150347_e);
      this.fill(world, bounds, 13, 1, 6, 1, 5, 3, Blocks.field_150347_e);
      this.func_151550_a(world, Blocks.field_150347_e, 0, 13, 6, 7, bounds);
      this.fill(world, bounds, 13, 1, 6, 1, 4, 1, Blocks.field_150364_r);
      this.func_151550_a(world, Blocks.field_150478_aa, 0, 12, 2, 7, bounds);
      this.fill(world, bounds, 13, 1, 8, 1, 4, 1, Blocks.field_150364_r);
      meta = this.func_151555_a(Blocks.field_150446_ar, 0);

      for(z = 6; z <= 8; ++z) {
         this.func_151550_a(world, Blocks.field_150446_ar, meta, 11, 0, z, bounds);
      }

      this.func_151550_a(world, Blocks.field_150333_U, 11, 12, 3, 6, bounds);
      this.func_151550_a(world, Blocks.field_150333_U, 11, 12, 3, 8, bounds);
      int k;
      if (!this.hasMadeChest) {
         int x = 13;
         int z = 12;
         int y = 20;
         k = this.func_74862_a(y);
         int j = this.func_74865_a(x, z);
         int k = this.func_74873_b(x, z);
         if (bounds.func_78890_b(j, k, k)) {
            this.hasMadeChest = true;
            this.func_74879_a(world, bounds, rand, x, y, z, villageTowerChestContents, 3 + rand.nextInt(6));
         }
      }

      for(int j = 1; j < 15; ++j) {
         for(k = 1; k < 15; ++k) {
            this.func_74871_b(world, k, 26, j, bounds);
            this.func_151554_b(world, Blocks.field_150347_e, 0, k, -1, j, bounds);
         }
      }

      this.spawnGuards(world, bounds, 7, 1, 7, 3);
      this.spawnGuards(world, bounds, 5, 10, 4, 4);
      this.spawnGuards(world, bounds, 13, 10, 4, 5);
      return true;
   }

   public void drawTower(World world, StructureBoundingBox bounds, int offsetX, int flipX) {
      this.fill(world, bounds, 3 + offsetX, 0, 1, 3, 11, 1, Blocks.field_150347_e);
      this.fill(world, bounds, 3 + offsetX, 0, 5, 3, 11, 1, Blocks.field_150347_e);
      this.fill(world, bounds, 2 + offsetX, 0, 2, 1, 11, 3, Blocks.field_150347_e);
      this.fill(world, bounds, 6 + offsetX, 0, 2, 1, 11, 3, Blocks.field_150347_e);
      this.fill(world, bounds, 3 + offsetX, 0, 2, 3, 1, 3, Blocks.field_150347_e);
      this.fill(world, bounds, 2 + offsetX, 4, 1, 5, 1, 5, Blocks.field_150347_e);
      this.fill(world, bounds, 2 + offsetX, 9, 1, 5, 1, 5, Blocks.field_150347_e);
      this.func_151550_a(world, Blocks.field_150347_e, 0, 4 + offsetX, 11, 1, bounds);
      this.func_151550_a(world, Blocks.field_150347_e, 0, 4 + offsetX, 11, 5, bounds);
      this.func_151550_a(world, Blocks.field_150347_e, 0, 2 + offsetX, 11, 3, bounds);
      this.func_151550_a(world, Blocks.field_150347_e, 0, 6 + offsetX, 11, 3, bounds);
      this.fill(world, bounds, 4 + offsetX, 1, 1, 1, 3, 1, Blocks.field_150364_r);
      this.fill(world, bounds, 2 + offsetX + flipX, 1, 3, 1, 3, 1, Blocks.field_150364_r);
      this.fill(world, bounds, 4 + offsetX, 6, 1, 1, 2, 1, Blocks.field_150411_aY);
      this.fill(world, bounds, 2 + offsetX + flipX, 6, 3, 1, 2, 1, Blocks.field_150411_aY);
      this.func_151550_a(world, Blocks.field_150364_r, this.func_151555_a(Blocks.field_150364_r, 8), 4 + offsetX, 7, 5, bounds);
      this.func_151550_a(world, Blocks.field_150364_r, this.func_151555_a(Blocks.field_150364_r, 4), 6 + offsetX - flipX, 7, 3, bounds);
      this.fill(world, bounds, 4 + offsetX, 5, 5, 1, 2, 1, Blocks.field_150350_a);
      this.fill(world, bounds, 4 + offsetX, 1, 5, 1, 2, 1, Blocks.field_150350_a);
      this.fill(world, bounds, 6 + offsetX - flipX, 5, 3, 1, 2, 1, Blocks.field_150350_a);
      int meta = this.func_151555_a(Blocks.field_150468_ap, 2);

      for(int h = 1; h <= 9; ++h) {
         this.func_151550_a(world, Blocks.field_150468_ap, meta, 3 + offsetX, h, 2, bounds);
      }

      this.func_151550_a(world, Blocks.field_150478_aa, 0, 3 + offsetX, 2, 4, bounds);
      this.func_151550_a(world, Blocks.field_150478_aa, 0, 3 + offsetX, 6, 4, bounds);
   }

   protected int func_151555_a(Block block, int meta) {
      if (block == Blocks.field_150364_r) {
         int rawMeta = meta / 4;
         if (rawMeta == 0) {
            return meta;
         } else {
            switch(this.field_74885_f) {
            case 0:
            case 2:
               return rawMeta == 2 ? 4 : 8;
            case 1:
            case 3:
            default:
               return rawMeta == 1 ? 8 : 4;
            }
         }
      } else {
         return super.func_151555_a(block, meta);
      }
   }

   protected void func_143012_a(NBTTagCompound nbtRoot) {
      super.func_143012_a(nbtRoot);
      nbtRoot.func_74757_a("Chest", this.hasMadeChest);
      nbtRoot.func_74768_a("Guards", this.guardsSpawned);
   }

   protected void func_143011_b(NBTTagCompound nbtRoot) {
      super.func_143011_b(nbtRoot);
      this.hasMadeChest = nbtRoot.func_74767_n("Chest");
      this.guardsSpawned = nbtRoot.func_74762_e("Guards");
   }

   private void spawnGuards(World world, StructureBoundingBox bounds, int x, int y, int z, int count) {
      if (this.guardsSpawned < count) {
         for(int guardNumber = this.guardsSpawned; guardNumber <= count; ++guardNumber) {
            int j1 = this.func_74865_a(x, z);
            int k1 = this.func_74862_a(y);
            int l1 = this.func_74873_b(x, z);
            if (!bounds.func_78890_b(j1, k1, l1)) {
               break;
            }

            ++this.guardsSpawned;
            EntityVillageGuard guard = new EntityVillageGuard(world);
            guard.func_70012_b((double)j1 + 0.5D, (double)k1, (double)l1 + 0.5D, 0.0F, 0.0F);
            guard.func_110163_bv();
            guard.func_110161_a((IEntityLivingData)null);
            world.func_72838_d(guard);
         }
      }

   }

   static {
      villageTowerChestContents = new WeightedRandomChestContent[]{new WeightedRandomChestContent(Items.field_151043_k, 0, 1, 6, 10), new WeightedRandomChestContent(Items.field_151074_bl, 0, 1, 15, 20), new WeightedRandomChestContent(Items.field_151006_E, 0, 1, 1, 5), new WeightedRandomChestContent(Items.field_151010_B, 0, 1, 1, 5), new WeightedRandomChestContent(Items.field_151171_ah, 0, 1, 1, 5), new WeightedRandomChestContent(Items.field_151169_ag, 0, 1, 1, 5), new WeightedRandomChestContent(Items.field_151149_ai, 0, 1, 1, 5), new WeightedRandomChestContent(Items.field_151151_aj, 0, 1, 1, 5), new WeightedRandomChestContent(Items.field_151125_bZ, 0, 1, 1, 1), new WeightedRandomChestContent(Items.field_151136_bY, 0, 1, 1, 1)};
   }
}
