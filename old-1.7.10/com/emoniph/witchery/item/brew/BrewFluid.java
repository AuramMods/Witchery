package com.emoniph.witchery.item.brew;

import com.emoniph.witchery.item.ItemGeneral;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BrewFluid extends ItemGeneral.Brew {
   protected final Fluid fluid;

   public BrewFluid(int damageValue, String unlocalisedName, Fluid fluid) {
      super(damageValue, unlocalisedName);
      this.fluid = fluid;
   }

   public ItemGeneral.Brew.BrewResult onImpact(World world, EntityLivingBase thrower, MovingObjectPosition mop, boolean enhanced, double brewX, double brewY, double brewZ, AxisAlignedBB brewBounds) {
      switch(mop.field_72313_a) {
      case BLOCK:
         this.depositLiquid(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, mop.field_72310_e, enhanced);
         break;
      case ENTITY:
         int x = MathHelper.func_76128_c(mop.field_72308_g.field_70165_t);
         int y = MathHelper.func_76128_c(mop.field_72308_g.field_70163_u);
         int z = MathHelper.func_76128_c(mop.field_72308_g.field_70161_v);
         this.depositLiquid(world, x, y, z, -1, enhanced);
      case MISS:
      }

      return ItemGeneral.Brew.BrewResult.SHOW_EFFECT;
   }

   public void depositLiquid(World world, int posX, int posY, int posZ, int side, boolean enhanced) {
      int x = posX + (side == 4 ? -1 : (side == 5 ? 1 : 0));
      int z = posZ + (side == 2 ? -1 : (side == 3 ? 1 : 0));
      int y = posY + (side == 0 ? -1 : (side == 1 ? 1 : 0));
      if (side == 1 && !world.func_147439_a(x, posY, z).func_149688_o().func_76220_a()) {
         --y;
      }

      setBlockIfNotSolid(world, x, y, z, this.fluid.getBlock());
   }
}
