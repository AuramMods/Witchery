package com.emoniph.witchery.brewing.potions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PotionOverheating extends PotionBase implements IHandleLivingUpdate {
   public PotionOverheating(int id, int color) {
      super(id, true, color);
   }

   public void postContructInitialize() {
      this.setPermenant();
      this.setIncurable();
   }

   public void onLivingUpdate(World world, EntityLivingBase entity, LivingUpdateEvent event, int amplifier, int duration) {
      if (!world.field_72995_K && world.func_82737_E() % 5L == 3L && !entity.func_70027_ad() && world.field_73012_v.nextInt(amplifier > 1 ? 20 : (amplifier > 0 ? 25 : 30)) == 0) {
         int x = MathHelper.func_76128_c(entity.field_70165_t);
         int z = MathHelper.func_76128_c(entity.field_70161_v);
         BiomeGenBase biome = world.func_72807_a(x, z);
         if ((double)biome.field_76750_F >= 1.5D && (!biome.func_76738_d() || !world.func_72896_J()) && !entity.func_70090_H()) {
            entity.func_70015_d(Math.min(world.field_73012_v.nextInt(amplifier < 3 ? 2 : amplifier) + 1, 4));
         }
      }

   }
}
