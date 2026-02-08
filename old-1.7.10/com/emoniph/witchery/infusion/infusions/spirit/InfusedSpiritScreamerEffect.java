package com.emoniph.witchery.infusion.infusions.spirit;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;

public class InfusedSpiritScreamerEffect extends InfusedSpiritEffect {
   public InfusedSpiritScreamerEffect(int id, int spirits, int spectres, int banshees, int poltergeists) {
      super(id, "screamer", spirits, spectres, banshees, poltergeists);
   }

   public boolean doUpdateEffect(TileEntity tile, boolean triggered, ArrayList<EntityLivingBase> foundEntities) {
      if (triggered) {
         ParticleEffect.REDDUST.send(tile.func_145838_q() != Witchery.Blocks.FETISH_WITCHS_LADDER ? SoundEffect.WITCHERY_MOB_SPECTRE_SPECTRE_HIT : SoundEffect.NONE, tile.func_145831_w(), 0.5D + (double)tile.field_145851_c, 0.3D + (double)tile.field_145848_d, 0.5D + (double)tile.field_145849_e, 0.2D, 0.5D, 16);
      }

      return triggered;
   }

   public boolean isRedstoneSignaller() {
      return true;
   }

   public double getRadius() {
      return 16.0D;
   }
}
