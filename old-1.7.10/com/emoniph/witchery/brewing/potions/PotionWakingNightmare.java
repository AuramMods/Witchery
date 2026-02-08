package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.entity.EntityNightmare;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PotionWakingNightmare extends PotionBase implements IHandleLivingUpdate {
   public PotionWakingNightmare(int id, int color) {
      super(id, true, color);
   }

   public void postContructInitialize() {
      this.setPermenant();
      this.setIncurable();
   }

   public void onLivingUpdate(World world, EntityLivingBase entity, LivingUpdateEvent event, int amplifier, int duration) {
      if (!world.field_72995_K && world.func_82737_E() % 20L == 3L && entity.field_71093_bK != Config.instance().dimensionDreamID && world.field_73012_v.nextInt(amplifier > 3 ? 30 : (amplifier > 1 ? 60 : 180)) == 0) {
         double R = 16.0D;
         double H = 8.0D;
         AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(entity.field_70165_t - 16.0D, entity.field_70163_u - 8.0D, entity.field_70161_v - 16.0D, entity.field_70165_t + 16.0D, entity.field_70163_u + 8.0D, entity.field_70161_v + 16.0D);
         List<EntityNightmare> entities = world.func_72872_a(EntityNightmare.class, bounds);
         boolean doNothing = false;
         Iterator i$ = entities.iterator();

         while(i$.hasNext()) {
            EntityNightmare nightmare = (EntityNightmare)i$.next();
            if (nightmare.getVictimName().equalsIgnoreCase(entity.func_70005_c_())) {
               doNothing = true;
               break;
            }
         }

         if (!doNothing) {
            Infusion.spawnCreature(world, EntityNightmare.class, MathHelper.func_76128_c(entity.field_70165_t), MathHelper.func_76128_c(entity.field_70163_u), MathHelper.func_76128_c(entity.field_70161_v), entity, 2, 6, (ParticleEffect)null, SoundEffect.NONE);
         }
      }

   }
}
