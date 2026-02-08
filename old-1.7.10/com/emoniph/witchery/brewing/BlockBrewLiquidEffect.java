package com.emoniph.witchery.brewing;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockBaseContainer;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.EntityPosition;
import com.emoniph.witchery.util.EntityUtil;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

public class BlockBrewLiquidEffect extends BlockBaseContainer implements IFluidBlock {
   protected static final Map<Block, Boolean> defaultDisplacements = Maps.newHashMap();
   protected Map<Block, Boolean> displacements = Maps.newHashMap();
   protected int quantaPerBlock = 6;
   protected float quantaPerBlockFloat = 8.0F;
   protected int density = 1;
   protected int densityDir = -1;
   protected int temperature = 295;
   protected int tickRate = 20;
   protected int renderPass = 1;
   protected int maxScaledLight = 0;
   protected boolean[] isOptimalFlowDirection = new boolean[4];
   protected int[] flowCost = new int[4];
   protected FluidStack stack;
   protected final String fluidName;
   @SideOnly(Side.CLIENT)
   protected IIcon[] icons;

   public BlockBrewLiquidEffect() {
      super(Material.field_151586_h, TileEntityBrewFluid.class);
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      this.func_149675_a(true);
      this.registerWithCreateTab = false;
      this.func_149649_H();
      Fluid fluid = Witchery.Fluids.BREW_LIQUID;
      this.fluidName = fluid.getName();
      this.density = fluid.getDensity();
      this.temperature = fluid.getTemperature();
      this.maxScaledLight = fluid.getLuminosity();
      this.tickRate = fluid.getViscosity() / 200;
      this.densityDir = fluid.getDensity() > 0 ? -1 : 1;
      fluid.setBlock(this);
      this.stack = new FluidStack(fluid, 1000);
      this.displacements.putAll(defaultDisplacements);
   }

   public int func_149720_d(IBlockAccess world, int x, int y, int z) {
      TileEntityBrewFluid fluid = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, x, y, z, TileEntityBrewFluid.class);
      return fluid != null ? fluid.color : 68;
   }

   public BlockBrewLiquidEffect setFluidStack(FluidStack stack) {
      this.stack = stack;
      return this;
   }

   public BlockBrewLiquidEffect setFluidStackAmount(int amount) {
      this.stack.amount = amount;
      return this;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int side, int meta) {
      return side != 0 && side != 1 ? this.icons[1] : this.icons[0];
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister iconRegister) {
      this.icons = new IIcon[]{iconRegister.func_94245_a(this.func_149641_N() + "_still"), iconRegister.func_94245_a(this.func_149641_N() + "_flow")};
   }

   public BlockBrewLiquidEffect setQuantaPerBlock(int quantaPerBlock) {
      if (quantaPerBlock > 16 || quantaPerBlock < 1) {
         quantaPerBlock = 8;
      }

      this.quantaPerBlock = quantaPerBlock;
      this.quantaPerBlockFloat = (float)quantaPerBlock;
      return this;
   }

   public BlockBrewLiquidEffect setDensity(int density) {
      if (density == 0) {
         density = 1;
      }

      this.density = density;
      this.densityDir = density > 0 ? -1 : 1;
      return this;
   }

   public BlockBrewLiquidEffect setTemperature(int temperature) {
      this.temperature = temperature;
      return this;
   }

   public BlockBrewLiquidEffect setTickRate(int tickRate) {
      if (tickRate <= 0) {
         tickRate = 20;
      }

      this.tickRate = tickRate;
      return this;
   }

   public BlockBrewLiquidEffect setRenderPass(int renderPass) {
      this.renderPass = renderPass;
      return this;
   }

   public BlockBrewLiquidEffect setMaxScaledLight(int maxScaledLight) {
      this.maxScaledLight = maxScaledLight;
      return this;
   }

   public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
      if (world.func_147439_a(x, y, z).isAir(world, x, y, z)) {
         return true;
      } else if (world.func_147439_a(x, y, z).func_149688_o().func_76224_d()) {
         return false;
      } else {
         Block block = world.func_147439_a(x, y, z);
         if (block == this) {
            return false;
         } else if (this.displacements.containsKey(block)) {
            return (Boolean)this.displacements.get(block);
         } else {
            Material material = block.func_149688_o();
            if (!material.func_76230_c() && material != Material.field_151567_E) {
               int density = getDensity(world, x, y, z);
               if (density == Integer.MAX_VALUE) {
                  return true;
               } else {
                  return this.density > density;
               }
            } else {
               return false;
            }
         }
      }
   }

   public boolean displaceIfPossible(World world, int x, int y, int z) {
      if (world.func_147439_a(x, y, z).isAir(world, x, y, z)) {
         return true;
      } else if (world.func_147439_a(x, y, z).func_149688_o().func_76224_d()) {
         return false;
      } else {
         Block block = world.func_147439_a(x, y, z);
         if (block == this) {
            return false;
         } else if (this.displacements.containsKey(block)) {
            if ((Boolean)this.displacements.get(block)) {
               block.func_149697_b(world, x, y, z, world.func_72805_g(x, y, z), 0);
               return true;
            } else {
               return false;
            }
         } else {
            Material material = block.func_149688_o();
            if (!material.func_76230_c() && material != Material.field_151567_E) {
               int density = getDensity(world, x, y, z);
               if (density == Integer.MAX_VALUE) {
                  block.func_149697_b(world, x, y, z, world.func_72805_g(x, y, z), 0);
                  return true;
               } else {
                  return this.density > density;
               }
            } else {
               return false;
            }
         }
      }
   }

   public void func_149726_b(World world, int x, int y, int z) {
      world.func_147464_a(x, y, z, this, this.tickRate);
   }

   public void func_149695_a(World world, int x, int y, int z, Block block) {
      world.func_147464_a(x, y, z, this, this.tickRate);
   }

   public boolean func_149698_L() {
      return false;
   }

   public boolean func_149655_b(IBlockAccess world, int x, int y, int z) {
      return true;
   }

   public AxisAlignedBB func_149668_a(World world, int x, int y, int z) {
      return null;
   }

   public Item func_149650_a(int par1, Random par2Random, int par3) {
      return null;
   }

   public int func_149745_a(Random par1Random) {
      return 0;
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return null;
   }

   public int func_149738_a(World world) {
      return this.tickRate;
   }

   public void func_149640_a(World world, int x, int y, int z, Entity entity, Vec3 vec) {
      if (this.densityDir <= 0) {
         Vec3 vec_flow = this.getFlowVector(world, x, y, z);
         vec.field_72450_a += vec_flow.field_72450_a * (double)(this.quantaPerBlock * 4);
         vec.field_72448_b += vec_flow.field_72448_b * (double)(this.quantaPerBlock * 4);
         vec.field_72449_c += vec_flow.field_72449_c * (double)(this.quantaPerBlock * 4);
      }
   }

   public int func_149645_b() {
      return Witchery.proxy.getBrewLiquidRenderId();
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public int func_149677_c(IBlockAccess world, int x, int y, int z) {
      int lightThis = world.func_72802_i(x, y, z, 0);
      int lightUp = world.func_72802_i(x, y + 1, z, 0);
      int lightThisBase = lightThis & 255;
      int lightUpBase = lightUp & 255;
      int lightThisExt = lightThis >> 16 & 255;
      int lightUpExt = lightUp >> 16 & 255;
      return (lightThisBase > lightUpBase ? lightThisBase : lightUpBase) | (lightThisExt > lightUpExt ? lightThisExt : lightUpExt) << 16;
   }

   public int func_149701_w() {
      return this.renderPass;
   }

   public boolean func_149646_a(IBlockAccess world, int x, int y, int z, int side) {
      Block block = world.func_147439_a(x, y, z);
      if (block != this) {
         return !block.func_149662_c();
      } else {
         return block.func_149688_o() == this.func_149688_o() ? false : super.func_149646_a(world, x, y, z, side);
      }
   }

   public static final int getDensity(IBlockAccess world, int x, int y, int z) {
      Block block = world.func_147439_a(x, y, z);
      return !(block instanceof BlockBrewLiquidEffect) ? Integer.MAX_VALUE : ((BlockBrewLiquidEffect)block).density;
   }

   public static final int getTemperature(IBlockAccess world, int x, int y, int z) {
      Block block = world.func_147439_a(x, y, z);
      return !(block instanceof BlockBrewLiquidEffect) ? Integer.MAX_VALUE : ((BlockBrewLiquidEffect)block).temperature;
   }

   public static double getFlowDirection(IBlockAccess world, int x, int y, int z) {
      Block block = world.func_147439_a(x, y, z);
      if (!block.func_149688_o().func_76224_d()) {
         return -1000.0D;
      } else {
         Vec3 vec = ((BlockBrewLiquidEffect)block).getFlowVector(world, x, y, z);
         return vec.field_72450_a == 0.0D && vec.field_72449_c == 0.0D ? -1000.0D : Math.atan2(vec.field_72449_c, vec.field_72450_a) - 1.5707963267948966D;
      }
   }

   public final int getQuantaValueBelow(IBlockAccess world, int x, int y, int z, int belowThis) {
      int quantaRemaining = this.getQuantaValue(world, x, y, z);
      return quantaRemaining >= belowThis ? -1 : quantaRemaining;
   }

   public final int getQuantaValueAbove(IBlockAccess world, int x, int y, int z, int aboveThis) {
      int quantaRemaining = this.getQuantaValue(world, x, y, z);
      return quantaRemaining <= aboveThis ? -1 : quantaRemaining;
   }

   public final float getQuantaPercentage(IBlockAccess world, int x, int y, int z) {
      int quantaRemaining = this.getQuantaValue(world, x, y, z);
      return (float)quantaRemaining / this.quantaPerBlockFloat;
   }

   public Vec3 getFlowVector(IBlockAccess world, int x, int y, int z) {
      Vec3 vec = Vec3.func_72443_a(0.0D, 0.0D, 0.0D);
      int decay = this.quantaPerBlock - this.getQuantaValue(world, x, y, z);

      for(int side = 0; side < 4; ++side) {
         int x2 = x;
         int z2 = z;
         switch(side) {
         case 0:
            x2 = x - 1;
            break;
         case 1:
            z2 = z - 1;
            break;
         case 2:
            x2 = x + 1;
            break;
         case 3:
            z2 = z + 1;
         }

         int otherDecay = this.quantaPerBlock - this.getQuantaValue(world, x2, y, z2);
         int power;
         if (otherDecay >= this.quantaPerBlock) {
            if (!world.func_147439_a(x2, y, z2).func_149688_o().func_76230_c()) {
               otherDecay = this.quantaPerBlock - this.getQuantaValue(world, x2, y - 1, z2);
               if (otherDecay >= 0) {
                  power = otherDecay - (decay - this.quantaPerBlock);
                  vec = vec.func_72441_c((double)((x2 - x) * power), (double)((y - y) * power), (double)((z2 - z) * power));
               }
            }
         } else if (otherDecay >= 0) {
            power = otherDecay - decay;
            vec = vec.func_72441_c((double)((x2 - x) * power), (double)((y - y) * power), (double)((z2 - z) * power));
         }
      }

      if (world.func_147439_a(x, y + 1, z) == this) {
         boolean flag = this.func_149747_d(world, x, y, z - 1, 2) || this.func_149747_d(world, x, y, z + 1, 3) || this.func_149747_d(world, x - 1, y, z, 4) || this.func_149747_d(world, x + 1, y, z, 5) || this.func_149747_d(world, x, y + 1, z - 1, 2) || this.func_149747_d(world, x, y + 1, z + 1, 3) || this.func_149747_d(world, x - 1, y + 1, z, 4) || this.func_149747_d(world, x + 1, y + 1, z, 5);
         if (flag) {
            vec = vec.func_72432_b().func_72441_c(0.0D, -6.0D, 0.0D);
         }
      }

      vec = vec.func_72432_b();
      return vec;
   }

   public Fluid getFluid() {
      return FluidRegistry.getFluid(this.fluidName);
   }

   public float getFilledPercentage(World world, int x, int y, int z) {
      int quantaRemaining = this.getQuantaValue(world, x, y, z) + 1;
      float remaining = (float)quantaRemaining / this.quantaPerBlockFloat;
      if (remaining > 1.0F) {
         remaining = 1.0F;
      }

      return remaining * (float)(this.density > 0 ? 1 : -1);
   }

   public int getQuantaValue(IBlockAccess world, int x, int y, int z) {
      if (world.func_147439_a(x, y, z) == Blocks.field_150350_a) {
         return 0;
      } else if (world.func_147439_a(x, y, z) != this) {
         return -1;
      } else {
         int quantaRemaining = this.quantaPerBlock - world.func_72805_g(x, y, z);
         return quantaRemaining;
      }
   }

   public boolean func_149678_a(int meta, boolean fullHit) {
      return fullHit && meta == 0;
   }

   public int getMaxRenderHeightMeta() {
      return 0;
   }

   public int getLightValue(IBlockAccess world, int x, int y, int z) {
      if (this.maxScaledLight == 0) {
         return super.getLightValue(world, x, y, z);
      } else {
         int data = this.quantaPerBlock - world.func_72805_g(x, y, z) - 1;
         return (int)((float)data / this.quantaPerBlockFloat * (float)this.maxScaledLight);
      }
   }

   private boolean isTargetBlock(World world, Block block, int x, int y, int z) {
      return block != null && (block != Blocks.field_150350_a || world.func_147439_a(x, y - 1, z).func_149688_o().func_76220_a()) && block != this;
   }

   public boolean isFlowingVertically(IBlockAccess world, int x, int y, int z) {
      return world.func_147439_a(x, y + this.densityDir, z) == this || world.func_147439_a(x, y, z) == this && this.canFlowInto(world, x, y + this.densityDir, z);
   }

   public boolean isSourceBlock(IBlockAccess world, int x, int y, int z) {
      return world.func_147439_a(x, y, z) == this && world.func_72805_g(x, y, z) == 0;
   }

   protected boolean[] getOptimalFlowDirections(World world, int x, int y, int z) {
      int side;
      int x2;
      for(side = 0; side < 4; ++side) {
         this.flowCost[side] = 1000;
         x2 = x;
         int z2 = z;
         switch(side) {
         case 0:
            x2 = x - 1;
            break;
         case 1:
            x2 = x + 1;
            break;
         case 2:
            z2 = z - 1;
            break;
         case 3:
            z2 = z + 1;
         }

         if (this.canFlowInto(world, x2, y, z2) && !this.isSourceBlock(world, x2, y, z2)) {
            if (this.canFlowInto(world, x2, y + this.densityDir, z2)) {
               this.flowCost[side] = 0;
            } else {
               this.flowCost[side] = this.calculateFlowCost(world, x2, y, z2, 1, side);
            }
         }
      }

      side = this.flowCost[0];

      for(x2 = 1; x2 < 4; ++x2) {
         if (this.flowCost[x2] < side) {
            side = this.flowCost[x2];
         }
      }

      for(x2 = 0; x2 < 4; ++x2) {
         this.isOptimalFlowDirection[x2] = this.flowCost[x2] == side;
      }

      return this.isOptimalFlowDirection;
   }

   protected int calculateFlowCost(World world, int x, int y, int z, int recurseDepth, int side) {
      int cost = 1000;

      for(int adjSide = 0; adjSide < 4; ++adjSide) {
         if ((adjSide != 0 || side != 1) && (adjSide != 1 || side != 0) && (adjSide != 2 || side != 3) && (adjSide != 3 || side != 2)) {
            int x2 = x;
            int z2 = z;
            switch(adjSide) {
            case 0:
               x2 = x - 1;
               break;
            case 1:
               x2 = x + 1;
               break;
            case 2:
               z2 = z - 1;
               break;
            case 3:
               z2 = z + 1;
            }

            if (this.canFlowInto(world, x2, y, z2) && !this.isSourceBlock(world, x2, y, z2)) {
               if (this.canFlowInto(world, x2, y + this.densityDir, z2)) {
                  return recurseDepth;
               }

               if (recurseDepth < 4) {
                  int min = this.calculateFlowCost(world, x2, y, z2, recurseDepth + 1, adjSide);
                  if (min < cost) {
                     cost = min;
                  }
               }
            }
         }
      }

      return cost;
   }

   protected void flowIntoBlock(World world, int x, int y, int z, int meta, TileEntityBrewFluid sourceFluid) {
      if (meta >= 0) {
         if (this.displaceIfPossible(world, x, y, z)) {
            world.func_147465_d(x, y, z, this, meta, 3);
            TileEntityBrewFluid targetFluid = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, x, y, z, TileEntityBrewFluid.class);
            if (targetFluid != null && sourceFluid != null && sourceFluid.nbtEffect != null) {
               targetFluid.nbtEffect = (NBTTagCompound)sourceFluid.nbtEffect.func_74737_b();
               targetFluid.expansion = sourceFluid.expansion;
               targetFluid.color = sourceFluid.color;
               targetFluid.duration = sourceFluid.duration;
               targetFluid.thrower = sourceFluid.thrower;
            }
         }

      }
   }

   protected boolean canFlowInto(IBlockAccess world, int x, int y, int z) {
      if (world.func_147439_a(x, y, z).isAir(world, x, y, z)) {
         return true;
      } else {
         Block block = world.func_147439_a(x, y, z);
         if (block == this) {
            return true;
         } else if (this.displacements.containsKey(block)) {
            return (Boolean)this.displacements.get(block);
         } else {
            Material material = block.func_149688_o();
            if (!material.func_76230_c() && material != Material.field_151586_h && material != Material.field_151587_i && material != Material.field_151567_E) {
               int density = getDensity(world, x, y, z);
               if (density == Integer.MAX_VALUE) {
                  return true;
               } else {
                  return this.density > density;
               }
            } else {
               return false;
            }
         }
      }
   }

   protected int getLargerQuanta(IBlockAccess world, int x, int y, int z, int compare) {
      int quantaRemaining = this.getQuantaValue(world, x, y, z);
      if (quantaRemaining <= 0) {
         return compare;
      } else {
         return quantaRemaining >= compare ? quantaRemaining : compare;
      }
   }

   public FluidStack drain(World world, int x, int y, int z, boolean doDrain) {
      return null;
   }

   public boolean canDrain(World world, int x, int y, int z) {
      return false;
   }

   public void func_149674_a(World world, int x, int y, int z, Random rand) {
      if (!world.field_72995_K) {
         boolean evaporated = false;
         TileEntityBrewFluid fluid = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, x, y, z, TileEntityBrewFluid.class);
         if (!world.field_72995_K && fluid != null && this.isSourceBlock(world, x, y, z)) {
            if (++fluid.updateCount <= 3 || fluid.duration != 0 && rand.nextInt(fluid.duration) != 0) {
               world.func_147464_a(x, y, z, this, this.tickRate);
            } else {
               world.func_147468_f(x, y, z);
               evaporated = true;
            }
         }

         if (!evaporated) {
            int quantaRemaining = this.quantaPerBlock - world.func_72805_g(x, y, z);
            int expQuanta = true;
            int flowMeta;
            if (quantaRemaining >= this.quantaPerBlock) {
               if (quantaRemaining >= this.quantaPerBlock) {
                  world.func_72921_c(x, y, z, 0, 2);
               }
            } else {
               flowMeta = y - this.densityDir;
               int expQuanta;
               if (world.func_147439_a(x, flowMeta, z) != this && world.func_147439_a(x - 1, flowMeta, z) != this && world.func_147439_a(x + 1, flowMeta, z) != this && world.func_147439_a(x, flowMeta, z - 1) != this && world.func_147439_a(x, flowMeta, z + 1) != this) {
                  int maxQuanta = -100;
                  int maxQuanta = this.getLargerQuanta(world, x - 1, y, z, maxQuanta);
                  maxQuanta = this.getLargerQuanta(world, x + 1, y, z, maxQuanta);
                  maxQuanta = this.getLargerQuanta(world, x, y, z - 1, maxQuanta);
                  maxQuanta = this.getLargerQuanta(world, x, y, z + 1, maxQuanta);
                  expQuanta = maxQuanta - 1;
               } else {
                  expQuanta = this.quantaPerBlock - 1;
               }

               if (expQuanta != quantaRemaining) {
                  quantaRemaining = expQuanta;
                  if (expQuanta <= 0) {
                     world.func_147449_b(x, y, z, Blocks.field_150350_a);
                  } else {
                     world.func_72921_c(x, y, z, this.quantaPerBlock - expQuanta, 3);
                     world.func_147464_a(x, y, z, this, this.tickRate);
                     world.func_147459_d(x, y, z, this);
                  }
               }
            }

            if (this.canDisplace(world, x, y + this.densityDir, z)) {
               this.flowIntoBlock(world, x, y + this.densityDir, z, 1, fluid);
               return;
            }

            flowMeta = this.quantaPerBlock - quantaRemaining + 1;
            if (flowMeta >= this.quantaPerBlock) {
               return;
            }

            if (this.isSourceBlock(world, x, y, z) || !this.isFlowingVertically(world, x, y, z)) {
               if (world.func_147439_a(x, y - this.densityDir, z) == this) {
                  flowMeta = 1;
               }

               boolean[] flowTo = this.getOptimalFlowDirections(world, x, y, z);
               if (flowTo[0]) {
                  this.flowIntoBlock(world, x - 1, y, z, flowMeta, fluid);
               }

               if (flowTo[1]) {
                  this.flowIntoBlock(world, x + 1, y, z, flowMeta, fluid);
               }

               if (flowTo[2]) {
                  this.flowIntoBlock(world, x, y, z - 1, flowMeta, fluid);
               }

               if (flowTo[3]) {
                  this.flowIntoBlock(world, x, y, z + 1, flowMeta, fluid);
               }
            }

            if (fluid != null && fluid.nbtEffect != null) {
               ForgeDirection[] arr$ = ForgeDirection.VALID_DIRECTIONS;
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  ForgeDirection direction = arr$[i$];
                  int x2 = x + direction.offsetX;
                  int y2 = y + direction.offsetY;
                  int z2 = z + direction.offsetZ;
                  if (world.field_73012_v.nextDouble() < 0.01D && this.isTargetBlock(world, world.func_147439_a(x2, y2, z2), x2, y2, z2)) {
                     ModifiersEffect modifiers = new ModifiersEffect(1.0D, 1.0D, false, new EntityPosition((double)x + 0.5D, (double)y, (double)z + 0.5D), false, 0, EntityUtil.playerOrFake(world, fluid.thrower));
                     ++modifiers.strengthPenalty;
                     WitcheryBrewRegistry.INSTANCE.applyToBlock(world, x2, y2, z2, direction.getOpposite(), 1, fluid.nbtEffect, modifiers);
                  }
               }

               world.func_147464_a(x, y, z, this, this.tickRate);
            }
         }

      }
   }

   public void func_149670_a(World world, int x, int y, int z, Entity entity) {
      if (entity != null && entity instanceof EntityLivingBase && !world.field_72995_K && world.field_73012_v.nextInt(10) == 4) {
         TileEntityBrewFluid liquid = (TileEntityBrewFluid)BlockUtil.getTileEntity(world, x, y, z, TileEntityBrewFluid.class);
         if (liquid != null && liquid.nbtEffect != null) {
            EntityLivingBase living = (EntityLivingBase)entity;
            WitcheryBrewRegistry.INSTANCE.applyToEntity(world, living, liquid.nbtEffect, new ModifiersEffect(0.25D, 0.5D, false, new EntityPosition(x, y, z), false, 0, EntityUtil.playerOrFake(world, liquid.thrower)));
         }
      }

   }

   static {
      defaultDisplacements.put(Blocks.field_150466_ao, false);
      defaultDisplacements.put(Blocks.field_150454_av, false);
      defaultDisplacements.put(Blocks.field_150472_an, false);
      defaultDisplacements.put(Blocks.field_150444_as, false);
      defaultDisplacements.put(Blocks.field_150436_aH, false);
   }
}
