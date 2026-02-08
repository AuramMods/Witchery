package com.emoniph.witchery.blocks;

import net.minecraft.tileentity.TileEntity;

public class TileEntityBase extends TileEntity {
   protected long ticks = 0L;

   public void func_145845_h() {
      super.func_145845_h();
      if (this.ticks == 0L) {
         this.initiate();
      } else if (this.ticks >= Long.MAX_VALUE) {
         this.ticks = 1L;
      }

      ++this.ticks;
   }

   protected void initiate() {
   }

   public void markBlockForUpdate(boolean notifyNeighbours) {
      this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
      if (notifyNeighbours && this.field_145850_b != null) {
         this.field_145850_b.func_147444_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, this.func_145838_q());
      }

   }
}
