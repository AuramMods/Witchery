package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.WitcheryCreativeTab;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.ItemUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

public class ItemBoline extends ItemSword {
   public static final Block[] blocksEffectiveAgainst;
   private float effectiveWeaponDamage;

   public ItemBoline() {
      super(ToolMaterial.IRON);
      this.effectiveWeaponDamage = 4.0F + ToolMaterial.WOOD.func_78000_c();
      this.func_77637_a(WitcheryCreativeTab.INSTANCE);
   }

   public Item func_77655_b(String itemName) {
      ItemUtil.registerItem(this, itemName);
      return super.func_77655_b(itemName);
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack stack) {
      return EnumRarity.uncommon;
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean moreTips) {
      String localText = Witchery.resource("item.witchery:boline.tip");
      if (localText != null) {
         String[] arr$ = localText.split("\n");
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String s = arr$[i$];
            if (!s.isEmpty()) {
               list.add(s);
            }
         }
      }

   }

   public Multimap func_111205_h() {
      Multimap multimap = HashMultimap.create();
      multimap.put(SharedMonsterAttributes.field_111264_e.func_111108_a(), new AttributeModifier(field_111210_e, "Weapon modifier", (double)this.effectiveWeaponDamage, 0));
      return multimap;
   }

   public float func_150931_i() {
      return ToolMaterial.WOOD.func_78000_c();
   }

   public boolean func_150894_a(ItemStack stack, World world, Block block, int posX, int posY, int posZ, EntityLivingBase entity) {
      if (block != null && block != Blocks.field_150362_t && block != Blocks.field_150321_G && block != Blocks.field_150329_H && block != Blocks.field_150395_bd && block != Blocks.field_150473_bD && !(block instanceof IShearable) && block.func_149712_f(world, posX, posY, posZ) != 0.0F) {
         stack.func_77972_a(2, entity);
      }

      return true;
   }

   public boolean canHarvestBlock(Block par1Block, ItemStack stack) {
      return par1Block == Witchery.Blocks.WEB || par1Block == Blocks.field_150321_G || par1Block == Blocks.field_150488_af || par1Block == Blocks.field_150473_bD;
   }

   public float func_150893_a(ItemStack stack, Block block) {
      if (block != Witchery.Blocks.WEB && block != Blocks.field_150321_G && block != Blocks.field_150362_t) {
         return block != Blocks.field_150325_L && block != Witchery.Blocks.TRAPPED_PLANT ? super.func_150893_a(stack, block) : 5.0F;
      } else {
         return 15.0F;
      }
   }

   public boolean func_111207_a(ItemStack itemstack, EntityPlayer player, EntityLivingBase entity) {
      if (entity.field_70170_p.field_72995_K) {
         return false;
      } else if (!(entity instanceof IShearable)) {
         return false;
      } else {
         IShearable target = (IShearable)entity;
         if (target.isShearable(itemstack, entity.field_70170_p, (int)entity.field_70165_t, (int)entity.field_70163_u, (int)entity.field_70161_v)) {
            ArrayList<ItemStack> drops = target.onSheared(itemstack, entity.field_70170_p, (int)entity.field_70165_t, (int)entity.field_70163_u, (int)entity.field_70161_v, EnchantmentHelper.func_77506_a(Enchantment.field_77346_s.field_77352_x, itemstack));
            Random rand = new Random();

            EntityItem ent;
            for(Iterator i$ = drops.iterator(); i$.hasNext(); ent.field_70179_y += (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F)) {
               ItemStack stack = (ItemStack)i$.next();
               ent = entity.func_70099_a(stack, 1.0F);
               ent.field_70181_x += (double)(rand.nextFloat() * 0.05F);
               ent.field_70159_w += (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F);
            }

            itemstack.func_77972_a(1, entity);
         }

         return true;
      }
   }

   public boolean onBlockStartBreak(ItemStack itemstack, int x, int y, int z, EntityPlayer player) {
      if (player.field_70170_p.field_72995_K) {
         return false;
      } else {
         World world = player.field_70170_p;
         Block block = BlockUtil.getBlock(world, x, y, z);
         if (block == null) {
            return false;
         } else if (block == Blocks.field_150321_G) {
            world.func_147468_f(x, y, z);
            world.func_72838_d(new EntityItem(world, 0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z, new ItemStack(block)));
            this.func_150894_a(itemstack, world, block, x, y, z, player);
            if (itemstack.field_77994_a == 0) {
               player.func_71028_bD();
            }

            return true;
         } else {
            int meta;
            if (block == Witchery.Blocks.TRAPPED_PLANT) {
               meta = world.func_72805_g(x, y, z);
               world.func_147468_f(x, y, z);
               world.func_72838_d(new EntityItem(world, 0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z, new ItemStack(block, 1, meta)));
               this.func_150894_a(itemstack, world, block, x, y, z, player);
               if (itemstack.field_77994_a == 0) {
                  player.func_71028_bD();
               }

               return true;
            } else if (block == Witchery.Blocks.BLOOD_ROSE) {
               meta = world.func_72805_g(x, y, z);
               world.func_147468_f(x, y, z);
               world.func_72838_d(new EntityItem(world, 0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z, new ItemStack(block, 1, meta)));
               this.func_150894_a(itemstack, world, block, x, y, z, player);
               if (itemstack.field_77994_a == 0) {
                  player.func_71028_bD();
               }

               return true;
            } else {
               if (block instanceof IShearable) {
                  IShearable target = (IShearable)block;
                  if (target.isShearable(itemstack, player.field_70170_p, x, y, z)) {
                     ArrayList<ItemStack> drops = target.onSheared(itemstack, player.field_70170_p, x, y, z, EnchantmentHelper.func_77506_a(Enchantment.field_77346_s.field_77352_x, itemstack));
                     Random rand = new Random();
                     Iterator i$ = drops.iterator();

                     while(i$.hasNext()) {
                        ItemStack stack = (ItemStack)i$.next();
                        float f = 0.7F;
                        double d = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                        double d1 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                        double d2 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                        EntityItem entityitem = new EntityItem(player.field_70170_p, (double)x + d, (double)y + d1, (double)z + d2, stack);
                        entityitem.field_145804_b = 10;
                        player.field_70170_p.func_72838_d(entityitem);
                     }

                     itemstack.func_77972_a(1, player);
                  }
               }

               return false;
            }
         }
      }
   }

   static {
      blocksEffectiveAgainst = new Block[]{Blocks.field_150344_f, Blocks.field_150342_X, Blocks.field_150344_f, Blocks.field_150486_ae, Blocks.field_150333_U, Blocks.field_150423_aK, Blocks.field_150428_aP};
   }
}
