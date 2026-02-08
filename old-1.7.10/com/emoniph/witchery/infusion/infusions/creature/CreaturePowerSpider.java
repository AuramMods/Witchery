package com.emoniph.witchery.infusion.infusions.creature;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.EntityWitchProjectile;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class CreaturePowerSpider extends CreaturePower {
   public CreaturePowerSpider(int powerID, Class<? extends EntitySpider> creatureType) {
      super(powerID, creatureType);
   }

   public void onActivate(World world, EntityPlayer player, int elapsedTicks, MovingObjectPosition mop) {
      if (!world.field_72995_K) {
         world.func_72956_a(player, "random.bow", 0.5F, 0.4F / (world.field_73012_v.nextFloat() * 0.4F + 0.8F));
         world.func_72838_d(new EntityWitchProjectile(world, player, Witchery.Items.GENERIC.itemWeb));
      }

   }

   public void onUpdate(World world, EntityPlayer player) {
      int blockX = MathHelper.func_76128_c(player.field_70165_t);
      int blockY = MathHelper.func_76128_c(player.field_70163_u + 1.0D);
      int blockZ = MathHelper.func_76128_c(player.field_70161_v);
      if (world.func_147439_a(blockX, blockY, blockZ).func_149688_o().func_76220_a()) {
         player.field_70181_x *= 0.6D;
      }

      if (player.field_70123_F) {
         player.field_70181_x = 0.3D;
      }

      if (player.func_70093_af() && player instanceof EntityPlayer && player.field_70123_F) {
         player.field_70181_x = 0.0D;
      }

   }
}
