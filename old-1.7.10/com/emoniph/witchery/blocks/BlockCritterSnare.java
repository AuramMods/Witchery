package com.emoniph.witchery.blocks;

import com.emoniph.witchery.item.ItemPolynesiaCharm;
import com.emoniph.witchery.util.MultiItemBlock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockCritterSnare extends BlockBaseBush {
   private static final String[] CAUGHT_TYPES = new String[]{"empty", "bat", "silverfish", "slime", "magmacube"};
   private static final String[] CAUGHT_TYPES_SOUNDS = new String[]{"", "mob.bat.idle", "mob.silverfish.say", "mob.slime.small", "mob.magmacube.small"};
   @SideOnly(Side.CLIENT)
   private IIcon[] critterIcons;

   public BlockCritterSnare() {
      super(Material.field_151585_k, BlockCritterSnare.ClassItemBlock.class);
      this.func_149672_a(field_149779_h);
      float f = 0.45F;
      this.func_149676_a(0.050000012F, 0.0F, 0.050000012F, 0.95F, 1.0F, 0.95F);
   }

   public void func_149670_a(World world, int posX, int posY, int posZ, Entity entity) {
      int meta = world.func_72805_g(posX, posY, posZ);
      if (meta == 0 && !world.field_72995_K && entity != null && entity.func_70089_S()) {
         if (entity instanceof EntityBat) {
            boolean hasStock = ItemPolynesiaCharm.hasStockInventory((EntityBat)entity);
            world.func_72921_c(posX, posY, posZ, hasStock ? 9 : 1, 3);
            entity.func_70106_y();
         } else if (entity instanceof EntitySilverfish) {
            world.func_72921_c(posX, posY, posZ, 2, 3);
            entity.func_70106_y();
         } else if (entity instanceof EntityMagmaCube && ((EntityMagmaCube)entity).func_70809_q() == 1) {
            world.func_72921_c(posX, posY, posZ, 4, 3);
            entity.func_70106_y();
         } else if (entity instanceof EntitySlime && ((EntitySlime)entity).func_70809_q() == 1) {
            world.func_72921_c(posX, posY, posZ, 3, 3);
            entity.func_70106_y();
         }
      }

   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9) {
      int meta = world.func_72805_g(x, y, z);
      if (!world.field_72995_K && meta > 0 && player.func_70093_af()) {
         world.func_72921_c(x, y, z, 0, 3);
         int tries = false;
         int tries;
         switch(this.getCritterFromMeta(meta)) {
         case 1:
            EntityBat bat = new EntityBat(world);
            bat.func_70012_b(0.5D + (double)x, 1.5D + (double)y, 0.5D + (double)z, 0.0F, 0.0F);
            if ((meta & 8) == 8) {
               ItemPolynesiaCharm.setEmptyStockInventory(world, bat);
            }

            world.func_72838_d(bat);
            break;
         case 2:
            EntitySilverfish silverfish = new EntitySilverfish(world);
            silverfish.func_70012_b(player.field_70165_t < (double)x ? (double)x - 0.5D : (double)x + 1.5D, player.field_70163_u + 0.5D, player.field_70161_v < (double)z ? (double)z - 0.5D : (double)z + 1.5D, 0.0F, 0.0F);
            world.func_72838_d(silverfish);
            break;
         case 3:
            EntitySlime slime = null;
            tries = 20;

            do {
               slime = new EntitySlime(world);
               if (slime.func_70809_q() == 1) {
                  break;
               }

               --tries;
            } while(tries > 0);

            if (tries > 0) {
               slime.func_70012_b(player.field_70165_t < (double)x ? (double)x - 0.5D : (double)x + 1.5D, player.field_70163_u + 0.5D, player.field_70161_v < (double)z ? (double)z - 0.5D : (double)z + 1.5D, 0.0F, 0.0F);
               world.func_72838_d(slime);
            } else {
               world.func_72838_d(new EntityItem(world, 0.5D + (double)x, 1.5D + (double)y, 0.5D + (double)z, new ItemStack(Items.field_151123_aH)));
            }
            break;
         case 4:
            EntityMagmaCube cube = null;
            tries = 20;

            do {
               cube = new EntityMagmaCube(world);
               if (cube.func_70809_q() == 1) {
                  break;
               }

               --tries;
            } while(tries > 0);

            if (tries > 0) {
               cube.func_70012_b(player.field_70165_t < (double)x ? (double)x - 0.5D : (double)x + 1.5D, player.field_70163_u + 0.5D, player.field_70161_v < (double)z ? (double)z - 0.5D : (double)z + 1.5D, 0.0F, 0.0F);
               world.func_72838_d(cube);
            } else {
               world.func_72838_d(new EntityItem(world, 0.5D + (double)x, 1.5D + (double)y, 0.5D + (double)z, new ItemStack(Items.field_151064_bs)));
            }
         }
      }

      return true;
   }

   private int getCritterFromMeta(int meta) {
      int critter = meta & 7;
      if (critter < 0 || critter >= CAUGHT_TYPES.length) {
         critter = 0;
      }

      return critter;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int side, int meta) {
      int critterType = this.getCritterFromMeta(meta);
      return this.critterIcons[critterType];
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      this.critterIcons = new IIcon[CAUGHT_TYPES.length];

      for(int i = 0; i < CAUGHT_TYPES.length; ++i) {
         this.critterIcons[i] = iconRegister.func_94245_a(this.func_149641_N() + "_" + CAUGHT_TYPES[i]);
      }

   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      if (rand.nextInt(24) == 0) {
         int meta = world.func_72805_g(x, y, z);
         int critterType = this.getCritterFromMeta(meta);
         if (critterType > 0 && critterType < CAUGHT_TYPES_SOUNDS.length) {
            String sound = CAUGHT_TYPES_SOUNDS[critterType];
            world.func_72980_b((double)x, (double)y, (double)z, sound, 0.5F, 0.4F / ((float)world.field_73012_v.nextDouble() * 0.4F + 0.8F), false);
         }
      }

   }

   public Item func_149650_a(int par1, Random rand, int fortune) {
      return Item.func_150898_a(this);
   }

   public int func_149692_a(int metadata) {
      return metadata;
   }

   @SideOnly(Side.CLIENT)
   public void func_149666_a(Item item, CreativeTabs creativeTabs, List list) {
      list.add(new ItemStack(item, 1, 0));
   }

   protected ItemStack func_149644_j(int par1) {
      return new ItemStack(this, 1, par1);
   }

   public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
      return super.func_149742_c(par1World, par2, par3, par4) && this.func_149718_j(par1World, par2, par3, par4);
   }

   protected boolean func_149854_a(Block block) {
      return block != null && block.func_149662_c();
   }

   public boolean func_149718_j(World world, int posX, int posY, int posZ) {
      Material material = world.func_147439_a(posX, posY - 1, posZ).func_149688_o();
      return material != null && material.func_76220_a();
   }

   public void func_149636_a(World par3World, EntityPlayer player, int par4, int par5, int par6, int damageValue) {
      super.func_149636_a(par3World, player, par4, par5, par6, damageValue);
   }

   public static class ClassItemBlock extends MultiItemBlock {
      public ClassItemBlock(Block block) {
         super(block);
      }

      protected String[] getNames() {
         return BlockCritterSnare.CAUGHT_TYPES;
      }

      @SideOnly(Side.CLIENT)
      public IIcon func_77617_a(int par1) {
         return this.field_150939_a.func_149691_a(0, par1);
      }
   }
}
