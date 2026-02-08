package com.emoniph.witchery.brewing;

import com.emoniph.witchery.util.BlockPosition;
import com.emoniph.witchery.util.EntityPosition;
import com.emoniph.witchery.util.EntityUtil;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class DispersalInstant extends Dispersal {
   public void onImpactSplashPotion(World world, NBTTagCompound nbtBrew, MovingObjectPosition mop, ModifiersImpact modifiers) {
      double R = (double)(3 + modifiers.extent);
      double R_SQ = R * R;
      EntityPosition position = modifiers.impactPosition;
      AxisAlignedBB bb = AxisAlignedBB.func_72330_a(position.x - R, position.y - R, position.z - R, position.x + R, position.y + R, position.z + R);
      List<EntityLivingBase> list1 = world.func_72872_a(EntityLivingBase.class, bb);
      Iterator i$ = list1.iterator();

      while(i$.hasNext()) {
         EntityLivingBase targetEntity = (EntityLivingBase)i$.next();
         double distanceSq = position.getDistanceSqToEntity(targetEntity);
         if (distanceSq <= R_SQ) {
            boolean directHit = targetEntity == mop.field_72308_g;
            double effectScalingFactor = directHit ? 1.0D : 1.0D - Math.sqrt(distanceSq) / R;
            WitcheryBrewRegistry.INSTANCE.applyToEntity(world, targetEntity, nbtBrew, new ModifiersEffect(effectScalingFactor, 0.5D * effectScalingFactor, !directHit, position, modifiers));
         }
      }

      if (mop.field_72313_a == MovingObjectType.BLOCK) {
         WitcheryBrewRegistry.INSTANCE.applyToBlock(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, ForgeDirection.getOrientation(mop.field_72310_e), MathHelper.func_76143_f(R), nbtBrew, new ModifiersEffect(1.0D, 1.0D, false, position, modifiers));
      }

   }

   public String getUnlocalizedName() {
      return "witchery:brew.dispersal.splash";
   }

   public RitualStatus onUpdateRitual(World world, int x, int y, int z, NBTTagCompound nbtBrew, ModifiersRitual modifiers, ModifiersImpact impactModifiers) {
      BlockPosition target = modifiers.getTarget();
      ModifiersEffect effectModifiers = new ModifiersEffect(1.0D, 1.0D, false, new EntityPosition(target), true, modifiers.covenSize, EntityUtil.playerOrFake(world, (EntityLivingBase)((EntityPlayer)null)));
      WitcheryBrewRegistry.INSTANCE.applyRitualToBlock(world, target.x, target.y, target.z, ForgeDirection.UP, 3 + impactModifiers.extent, nbtBrew, modifiers, effectModifiers);
      return modifiers.getRitualStatus();
   }
}
