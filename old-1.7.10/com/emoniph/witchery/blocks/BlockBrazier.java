package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.IPowerSource;
import com.emoniph.witchery.common.PowerSources;
import com.emoniph.witchery.crafting.BrazierRecipes;
import com.emoniph.witchery.util.BlockSide;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBrazier extends BlockBaseContainer {
   public BlockBrazier() {
      super(Material.field_151573_f, BlockBrazier.TileEntityBrazier.class);
      this.func_149711_c(3.5F);
      this.func_149672_a(field_149777_j);
      this.func_149676_a(0.2F, 0.0F, 0.2F, 0.8F, 0.95F, 0.8F);
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149646_a(IBlockAccess iblockaccess, int i, int j, int k, int l) {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public void func_149726_b(World world, int x, int y, int z) {
      super.func_149726_b(world, x, y, z);
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      TileEntity tile = world.func_147438_o(x, y, z);
      if (tile != null && tile instanceof BlockBrazier.TileEntityBrazier) {
         BlockBrazier.TileEntityBrazier brazier = (BlockBrazier.TileEntityBrazier)tile;
         if (brazier.isBurning()) {
            double d0 = (double)((float)x + 0.4F + (float)rand.nextInt(3) * 0.1F);
            double d1 = (double)((float)y + 1.1F + (float)rand.nextInt(2) * 0.1F);
            double d2 = (double)((float)z + 0.4F + (float)rand.nextInt(3) * 0.1F);
            world.func_72869_a(ParticleEffect.FLAME.toString(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   public void func_149749_a(World world, int x, int y, int z, Block oldBlockID, int oldBlockMetadata) {
      TileEntity tile = world.func_147438_o(x, y, z);
      if (tile != null && tile instanceof BlockBrazier.TileEntityBrazier) {
         BlockBrazier.TileEntityBrazier brazier = (BlockBrazier.TileEntityBrazier)tile;
         if (!brazier.isBurning()) {
            for(int j1 = 0; j1 < brazier.func_70302_i_(); ++j1) {
               ItemStack itemstack = brazier.func_70301_a(j1);
               this.dropItemFromBrokenBlock(world, x, y, z, itemstack);
               world.func_147453_f(x, y, z, oldBlockID);
            }
         } else {
            this.dropItemFromBrokenBlock(world, x, y, z, Witchery.Items.GENERIC.itemAshWood.createStack());
            world.func_147453_f(x, y, z, oldBlockID);
         }
      }

      super.func_149749_a(world, x, y, z, oldBlockID, oldBlockMetadata);
   }

   private void dropItemFromBrokenBlock(World world, int x, int y, int z, ItemStack itemstack) {
      if (itemstack != null) {
         float f = world.field_73012_v.nextFloat() * 0.8F + 0.1F;
         float f1 = world.field_73012_v.nextFloat() * 0.8F + 0.1F;
         float f2 = world.field_73012_v.nextFloat() * 0.8F + 0.1F;

         while(itemstack.field_77994_a > 0) {
            int k1 = world.field_73012_v.nextInt(21) + 10;
            if (k1 > itemstack.field_77994_a) {
               k1 = itemstack.field_77994_a;
            }

            itemstack.field_77994_a -= k1;
            EntityItem entityitem = new EntityItem(world, (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), new ItemStack(itemstack.func_77973_b(), k1, itemstack.func_77960_j()));
            if (itemstack.func_77942_o()) {
               entityitem.func_92059_d().func_77982_d((NBTTagCompound)itemstack.func_77978_p().func_74737_b());
            }

            float f3 = 0.05F;
            entityitem.field_70159_w = (double)((float)world.field_73012_v.nextGaussian() * 0.05F);
            entityitem.field_70181_x = (double)((float)world.field_73012_v.nextGaussian() * 0.05F + 0.2F);
            entityitem.field_70179_y = (double)((float)world.field_73012_v.nextGaussian() * 0.05F);
            world.func_72838_d(entityitem);
         }
      }

   }

   public boolean func_149740_M() {
      return true;
   }

   public int func_149736_g(World world, int x, int y, int z, int side) {
      return Container.func_94526_b((IInventory)world.func_147438_o(x, y, z));
   }

   public boolean func_149727_a(World world, int posX, int posY, int posZ, EntityPlayer player, int par6, float par7, float par8, float par9) {
      if (world.field_72995_K) {
         return true;
      } else {
         TileEntity tile = world.func_147438_o(posX, posY, posZ);
         if (tile != null && tile instanceof BlockBrazier.TileEntityBrazier) {
            BlockBrazier.TileEntityBrazier brazier = (BlockBrazier.TileEntityBrazier)tile;
            ItemStack stack = player.func_70694_bm();
            if (stack == null) {
               return false;
            }

            if (stack.func_77973_b() == Items.field_151068_bn && stack.func_77960_j() == 0) {
               if (!brazier.isEmpty()) {
                  brazier.reset();
                  if (!player.field_71075_bZ.field_75098_d && player.field_71071_by != null) {
                     player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, new ItemStack(Items.field_151069_bo));
                     if (player instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                     }
                  }

                  SoundEffect.WATER_SPLASH.playAtPlayer(world, player);
               }
            } else if (stack.func_77973_b() == Items.field_151131_as) {
               if (!brazier.isEmpty()) {
                  brazier.reset();
                  if (!player.field_71075_bZ.field_75098_d && player.field_71071_by != null) {
                     player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, new ItemStack(Items.field_151133_ar));
                     if (player instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                     }
                  }

                  SoundEffect.WATER_SPLASH.playAtPlayer(world, player);
               }
            } else {
               if (stack.func_77973_b() == Items.field_151033_d) {
                  if (!brazier.isEmpty()) {
                     brazier.begin();
                  }

                  return false;
               }

               boolean added = false;

               for(int i = 0; i < brazier.func_70302_i_() - 1; ++i) {
                  if (brazier.func_70301_a(i) == null) {
                     if (!player.field_71075_bZ.field_75098_d && player.field_71071_by != null) {
                        ItemStack newStack = stack.func_77979_a(1);
                        brazier.func_70299_a(i, newStack);
                        if (stack.field_77994_a == 0) {
                           player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                        }
                     } else {
                        brazier.func_70299_a(i, new ItemStack(stack.func_77973_b(), 1, stack.func_77960_j()));
                     }

                     added = true;
                     break;
                  }
               }
            }
         }

         return true;
      }
   }

   public static void tryIgnite(World world, int x, int y, int z) {
      TileEntity tile = world.func_147438_o(x, y, z);
      if (tile != null && tile instanceof BlockBrazier.TileEntityBrazier) {
         BlockBrazier.TileEntityBrazier brazier = (BlockBrazier.TileEntityBrazier)tile;
         if (!brazier.isEmpty()) {
            brazier.begin();
         }
      }

   }

   public void func_149695_a(World par1World, int par2, int par3, int par4, Block par5) {
      boolean flag = par1World.func_72864_z(par2, par3, par4);
      TileEntity tile = par1World.func_147438_o(par2, par3, par4);
      if (tile != null && tile instanceof BlockBrazier.TileEntityBrazier) {
         BlockBrazier.TileEntityBrazier brazier = (BlockBrazier.TileEntityBrazier)tile;
         if (brazier.previousRedstoneState != flag && flag && !brazier.isEmpty()) {
            brazier.begin();
         }

         brazier.previousRedstoneState = flag;
      }

   }

   public static class TileEntityBrazier extends TileEntityBase implements ISidedInventory {
      private ItemStack[] slots = new ItemStack[4];
      private int furnaceCookTime = 0;
      public boolean previousRedstoneState;
      private Coord powerSourceCoord;
      private static final int POWER_SOURCE_RADIUS = 16;
      private static final float POWER_PER_TICK = 1.0F;
      public int powerLevel;
      private long storage;
      private static final int SLOT_1 = 0;
      private static final int SLOT_2 = 1;
      private static final int SLOT_3 = 2;
      private static final int SLOT_RESULT = 3;
      private static final int[] slots_top = new int[]{0, 1, 2};
      private static final int[] slots_bottom = new int[]{0, 1, 2};
      private static final int[] slots_sides = new int[]{0, 1, 2};

      public int func_70302_i_() {
         return this.slots.length;
      }

      public void begin() {
         this.func_70299_a(3, Witchery.Items.GENERIC.itemAshWood.createStack());
      }

      public ItemStack func_70301_a(int slot) {
         return this.slots[slot];
      }

      public boolean isBurning() {
         for(int i = 0; i < this.func_70302_i_(); ++i) {
            if (this.func_70301_a(i) == null) {
               return false;
            }
         }

         return true;
      }

      public ItemStack func_70298_a(int slot, int quantity) {
         if (this.slots[slot] != null) {
            ItemStack itemstack;
            if (this.slots[slot].field_77994_a <= quantity) {
               itemstack = this.slots[slot];
               this.slots[slot] = null;
               return itemstack;
            } else {
               itemstack = this.slots[slot].func_77979_a(quantity);
               if (this.slots[slot].field_77994_a == 0) {
                  this.slots[slot] = null;
               }

               return itemstack;
            }
         } else {
            return null;
         }
      }

      public boolean isFull() {
         if (this.func_70301_a(3) != null) {
            return true;
         } else {
            for(int slot = 0; slot < 3; ++slot) {
               if (this.func_70301_a(slot) == null) {
                  return false;
               }
            }

            return true;
         }
      }

      public boolean isEmpty() {
         for(int slot = 0; slot < 3; ++slot) {
            if (this.func_70301_a(slot) != null) {
               return false;
            }
         }

         return true;
      }

      public int getIngredientCount() {
         int count = 0;

         for(int slot = 0; slot < 3; ++slot) {
            if (this.func_70301_a(slot) != null) {
               ++count;
            }
         }

         return count;
      }

      public void reset() {
         for(int slot = 0; slot < this.func_70302_i_(); ++slot) {
            this.func_70299_a(slot, (ItemStack)null);
         }

      }

      public ItemStack func_70304_b(int slot) {
         if (this.slots[slot] != null) {
            ItemStack itemstack = this.slots[slot];
            this.slots[slot] = null;
            return itemstack;
         } else {
            return null;
         }
      }

      public void func_70299_a(int slot, ItemStack stack) {
         this.slots[slot] = stack;
         if (stack != null && stack.field_77994_a > this.func_70297_j_()) {
            stack.field_77994_a = this.func_70297_j_();
         }

         this.func_70296_d();
      }

      public String func_145825_b() {
         return this.func_145838_q().func_149732_F();
      }

      public boolean func_145818_k_() {
         return true;
      }

      public void func_145839_a(NBTTagCompound nbtRoot) {
         super.func_145839_a(nbtRoot);
         NBTTagList nbtSlotList = nbtRoot.func_150295_c("Items", 10);
         this.slots = new ItemStack[this.func_70302_i_()];

         for(int i = 0; i < nbtSlotList.func_74745_c(); ++i) {
            NBTTagCompound nbtSlot = nbtSlotList.func_150305_b(i);
            byte b0 = nbtSlot.func_74771_c("Slot");
            if (b0 >= 0 && b0 < this.slots.length) {
               this.slots[b0] = ItemStack.func_77949_a(nbtSlot);
            }
         }

         this.furnaceCookTime = nbtRoot.func_74765_d("CookTime");
         this.powerLevel = nbtRoot.func_74765_d("PowerLevel");
         this.storage = nbtRoot.func_74763_f("PowerStorage");
      }

      public void func_145841_b(NBTTagCompound nbtRoot) {
         super.func_145841_b(nbtRoot);
         nbtRoot.func_74777_a("CookTime", (short)this.furnaceCookTime);
         nbtRoot.func_74777_a("PowerLevel", (short)this.powerLevel);
         nbtRoot.func_74772_a("PowerStorage", this.storage);
         NBTTagList nbtSlotList = new NBTTagList();

         for(int i = 0; i < this.slots.length; ++i) {
            if (this.slots[i] != null) {
               NBTTagCompound nbtSlot = new NBTTagCompound();
               nbtSlot.func_74774_a("Slot", (byte)i);
               this.slots[i].func_77955_b(nbtSlot);
               nbtSlotList.func_74742_a(nbtSlot);
            }
         }

         nbtRoot.func_74782_a("Items", nbtSlotList);
      }

      public int func_70297_j_() {
         return 64;
      }

      private IPowerSource getPowerSource() {
         if (this.powerSourceCoord != null && this.ticks % 100L != 0L) {
            TileEntity tileEntity = this.powerSourceCoord.getBlockTileEntity(this.field_145850_b);
            if (!(tileEntity instanceof BlockAltar.TileEntityAltar)) {
               return this.findNewPowerSource();
            } else {
               BlockAltar.TileEntityAltar altarTileEntity = (BlockAltar.TileEntityAltar)tileEntity;
               return (IPowerSource)(!altarTileEntity.isValid() ? this.findNewPowerSource() : altarTileEntity);
            }
         } else {
            return this.findNewPowerSource();
         }
      }

      private IPowerSource findNewPowerSource() {
         List<PowerSources.RelativePowerSource> sources = PowerSources.instance() != null ? PowerSources.instance().get(this.field_145850_b, new Coord(this), 16) : null;
         return sources != null && sources.size() > 0 ? ((PowerSources.RelativePowerSource)sources.get(0)).source() : null;
      }

      public void func_145845_h() {
         super.func_145845_h();
         boolean update = false;
         boolean cooking = this.furnaceCookTime > 0;
         if (!this.field_145850_b.field_72995_K) {
            boolean powered = this.powerLevel > 0;
            BrazierRecipes.BrazierRecipe recipe = BrazierRecipes.instance().getRecipe(new ItemStack[]{this.slots[0], this.slots[1], this.slots[2]});
            IPowerSource powerSource;
            if (recipe != null && this.func_70301_a(3) != null) {
               powerSource = this.getPowerSource();
               if (powerSource != null && !powerSource.isLocationEqual(this.powerSourceCoord)) {
                  this.powerSourceCoord = powerSource.getLocation();
               } else {
                  this.powerSourceCoord = null;
               }

               boolean needsPower = recipe.getNeedsPower();
               this.powerLevel = needsPower && powerSource == null ? 0 : 1;
               if (recipe.getNeedsPower() && (powerSource == null || !powerSource.consumePower(1.0F))) {
                  this.powerLevel = 0;
                  if (powered != this.powerLevel > 0) {
                     update = true;
                  }
               } else {
                  update = this.furnaceCookTime == 0;
                  ++this.furnaceCookTime;
                  if ((long)this.furnaceCookTime == (long)recipe.burnTicks + this.storage * 400L) {
                     this.furnaceCookTime = 0;
                     recipe.onBurnt(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, this.ticks, this);
                     this.func_70299_a(0, (ItemStack)null);
                     this.func_70299_a(1, (ItemStack)null);
                     this.func_70299_a(2, (ItemStack)null);
                     update = true;
                  } else {
                     this.storage += (long)recipe.onBurning(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, this.ticks, this);
                     if (this.storage == Long.MAX_VALUE) {
                        this.storage = 0L;
                     }
                  }

                  if (powered != this.powerLevel > 0) {
                     update = true;
                  }
               }
            } else {
               if (this.func_70301_a(3) != null) {
                  this.reset();
                  ParticleEffect.SMOKE.send(SoundEffect.RANDOM_FIZZ, this.field_145850_b, 0.5D + (double)this.field_145851_c, 1.0D + (double)this.field_145848_d, 0.5D + (double)this.field_145849_e, 0.5D, 0.5D, 8);
               }

               if (this.ticks % 40L == 0L) {
                  powerSource = this.getPowerSource();
                  if (powerSource != null && !powerSource.isLocationEqual(this.powerSourceCoord)) {
                     this.powerSourceCoord = powerSource.getLocation();
                  }

                  this.powerLevel = powerSource == null ? 0 : 1;
               }

               update = this.furnaceCookTime > 0 || powered != this.powerLevel > 0;
               this.furnaceCookTime = 0;
            }
         }

         if (update) {
            this.func_70296_d();
         }

      }

      public void func_70296_d() {
         super.func_70296_d();
         if (!this.field_145850_b.field_72995_K) {
            this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
         }

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

      public boolean func_70300_a(EntityPlayer par1EntityPlayer) {
         return this.field_145850_b.func_147438_o(this.field_145851_c, this.field_145848_d, this.field_145849_e) != this ? false : par1EntityPlayer.func_70092_e((double)this.field_145851_c + 0.5D, (double)this.field_145848_d + 0.5D, (double)this.field_145849_e + 0.5D) <= 64.0D;
      }

      public void func_70295_k_() {
      }

      public void func_70305_f() {
      }

      public boolean func_94041_b(int slot, ItemStack itemstack) {
         if (slot != 3 && itemstack != null) {
            if (itemstack.field_77994_a != 1) {
               return false;
            } else if (slot >= 0 && slot < this.slots.length) {
               return this.slots[slot] == null;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      public int[] func_94128_d(int side) {
         return BlockSide.BOTTOM.isEqual(side) ? slots_bottom : (BlockSide.TOP.isEqual(side) ? slots_top : slots_sides);
      }

      public boolean func_102007_a(int slot, ItemStack itemstack, int par3) {
         return this.func_94041_b(slot, itemstack);
      }

      public boolean func_102008_b(int slot, ItemStack stack, int side) {
         return false;
      }
   }
}
