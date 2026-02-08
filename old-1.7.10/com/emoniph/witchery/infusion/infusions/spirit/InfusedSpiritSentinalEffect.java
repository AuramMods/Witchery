package com.emoniph.witchery.infusion.infusions.spirit;

import com.emoniph.witchery.entity.EntitySpectre;
import com.emoniph.witchery.infusion.infusions.InfusionInfernal;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;

public class InfusedSpiritSentinalEffect extends InfusedSpiritEffect {
   public InfusedSpiritSentinalEffect(int id, int spirits, int spectres, int banshees, int poltergeists) {
      super(id, "sentinal", spirits, spectres, banshees, poltergeists);
   }

   public int getCooldownTicks() {
      return TimeUtil.secsToTicks(30);
   }

   public double getRadius() {
      return 8.0D;
   }

   public boolean doUpdateEffect(TileEntity tile, boolean triggered, ArrayList<EntityLivingBase> foundEntities) {
      if (triggered) {
         int number = foundEntities.size() > 1 ? 1 : 2;
         Iterator i$ = foundEntities.iterator();

         while(i$.hasNext()) {
            EntityLivingBase entity = (EntityLivingBase)i$.next();

            for(int i = 0; i < number; ++i) {
               int blockX = MathHelper.func_76128_c(entity.field_70165_t);
               int blockY = MathHelper.func_76128_c(entity.field_70163_u);
               int blockZ = MathHelper.func_76128_c(entity.field_70161_v);
               EntitySpectre creature = (EntitySpectre)InfusionInfernal.spawnCreature(tile.func_145831_w(), EntitySpectre.class, blockX, blockY, blockZ, entity, 1, 1, ParticleEffect.INSTANT_SPELL, SoundEffect.WITCHERY_MOB_SPECTRE_SPECTRE_SAY);
               if (creature != null) {
                  EntityUtil.setTarget(creature, entity);
                  creature.field_70715_bh.func_75776_a(1, new EntityAINearestAttackableTarget(creature, entity.getClass(), 0, true));
                  creature.setTimeToLive(TimeUtil.secsToTicks(30));
                  creature.func_110161_a((IEntityLivingData)null);
               }
            }
         }
      }

      return triggered;
   }
}
