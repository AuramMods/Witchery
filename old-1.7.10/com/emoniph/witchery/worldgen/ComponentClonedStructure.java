package com.emoniph.witchery.worldgen;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.item.ItemDuplicationStaff;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public abstract class ComponentClonedStructure extends WitcheryComponent {
   private ItemDuplicationStaff.Rotation rotation;
   private int witchesSpawned = 0;
   public static final WeightedRandomChestContent[] shackChestContents;
   private boolean hasMadeChest;
   private static final String CHEST_KEY = "WITCShackChest";

   public ComponentClonedStructure() {
   }

   public ComponentClonedStructure(int direction, Random random, int x, int z, int w, int h, int d) {
      super(direction, random, x, z, w, h, d);
      this.rotation = ItemDuplicationStaff.Rotation.values()[direction];
   }

   public boolean addComponentParts(World world, Random random) {
      BiomeGenBase biom = world.func_72807_a(this.func_74865_a(0, 0), this.func_74873_b(0, 0));
      int groundAvg = this.calcGroundHeight(world, this.field_74887_e);
      if (groundAvg < 0) {
         return true;
      } else {
         this.field_74887_e.func_78886_a(0, groundAvg - this.field_74887_e.field_78894_e + this.field_74887_e.func_78882_c() - 1, 0);
         if (!this.isWaterBelow(world, 0, -1, 0, this.field_74887_e) && !this.isWaterBelow(world, 0, -1, this.field_74887_e.func_78880_d() - 1, this.field_74887_e) && !this.isWaterBelow(world, this.field_74887_e.func_78883_b() - 1, -1, 0, this.field_74887_e) && !this.isWaterBelow(world, this.field_74887_e.func_78883_b() - 1, -1, this.field_74887_e.func_78880_d() - 1, this.field_74887_e)) {
            Block groundID = Blocks.field_150349_c;
            Block undergroundID = Blocks.field_150346_d;
            if (biom.field_76756_M == BiomeGenBase.field_76769_d.field_76756_M || biom.field_76756_M == BiomeGenBase.field_76786_s.field_76756_M || biom.field_76756_M == BiomeGenBase.field_76787_r.field_76756_M) {
               Block groundID = Blocks.field_150354_m;
               undergroundID = Blocks.field_150354_m;
            }

            NBTTagCompound nbtSchematic = this.getSchematic(world, random);
            ItemDuplicationStaff.drawSchematicInWorld(world, this.field_74887_e.field_78897_a, this.field_74887_e.field_78895_b, this.field_74887_e.field_78896_c, this.rotation, true, nbtSchematic);

            for(int i = 0; i < this.field_74887_e.func_78883_b(); ++i) {
               for(int j = 0; j < this.field_74887_e.func_78880_d(); ++j) {
                  this.func_151554_b(world, (Block)undergroundID, 0, j, 0, i, this.field_74887_e);
               }
            }

            this.spawnWitches(world, this.field_74887_e, this.field_74887_e.func_78883_b() - 3, 1, 3, 1);
            return true;
         } else {
            return false;
         }
      }
   }

   protected abstract NBTTagCompound getSchematic(World var1, Random var2);

   private void spawnWitches(World par1World, StructureBoundingBox par2StructureBoundingBox, int par3, int par4, int par5, int par6) {
      if (this.witchesSpawned < par6) {
         for(int i1 = this.witchesSpawned; i1 < par6; ++i1) {
            int j1 = this.func_74865_a(par3 + i1, par5);
            int k1 = this.func_74862_a(par4);
            int l1 = this.func_74873_b(par3 + i1, par5);
            if (!par2StructureBoundingBox.func_78890_b(j1, k1, l1)) {
               break;
            }

            ++this.witchesSpawned;
            this.spawnInhabitant(par1World, par2StructureBoundingBox);
         }
      }

   }

   protected abstract void spawnInhabitant(World var1, StructureBoundingBox var2);

   protected void func_143012_a(NBTTagCompound par1NBTTagCompound) {
      super.func_143012_a(par1NBTTagCompound);
      par1NBTTagCompound.func_74757_a("WITCShackChest", this.hasMadeChest);
      par1NBTTagCompound.func_74768_a("WITCWCount", this.witchesSpawned);
   }

   protected void func_143011_b(NBTTagCompound par1NBTTagCompound) {
      super.func_143011_b(par1NBTTagCompound);
      this.hasMadeChest = par1NBTTagCompound.func_74767_n("WITCShackChest");
      if (par1NBTTagCompound.func_74764_b("WITCWCount")) {
         this.witchesSpawned = par1NBTTagCompound.func_74762_e("WITCWCount");
      } else {
         this.witchesSpawned = 0;
      }

   }

   static {
      shackChestContents = new WeightedRandomChestContent[]{new WeightedRandomChestContent(Items.field_151069_bo, 0, 1, 1, 10), new WeightedRandomChestContent(Items.field_151025_P, 0, 1, 3, 15), new WeightedRandomChestContent(Items.field_151034_e, 0, 1, 3, 15), new WeightedRandomChestContent(Items.field_151101_aQ, 0, 1, 3, 10), new WeightedRandomChestContent(Item.func_150898_a(Blocks.field_150345_g), 1, 1, 1, 15), new WeightedRandomChestContent(Witchery.Items.GENERIC, Witchery.Items.GENERIC.itemRowanBerries.damageValue, 1, 2, 10), new WeightedRandomChestContent(Items.field_151037_a, 0, 1, 1, 5), new WeightedRandomChestContent(Items.field_151035_b, 0, 1, 1, 5)};
   }
}
