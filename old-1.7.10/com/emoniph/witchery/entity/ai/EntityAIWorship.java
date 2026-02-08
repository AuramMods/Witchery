package com.emoniph.witchery.entity.ai;

import com.emoniph.witchery.entity.EntityGoblin;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;

public class EntityAIWorship extends EntityAIBase {
   private final EntityGoblin goblin;
   private final double maxDuration;
   private int currentTick;
   private boolean shouldBegin;
   private int posX;
   private int posY;
   private int posZ;

   public EntityAIWorship(EntityGoblin goblin, double maxDuration) {
      this.goblin = goblin;
      this.maxDuration = maxDuration;
      this.func_75248_a(15);
   }

   public boolean func_75250_a() {
      return this.shouldBegin || this.goblin.isWorshipping();
   }

   public void func_75249_e() {
      this.currentTick = 0;
      this.shouldBegin = false;
      this.goblin.setWorshipping(true);
      this.goblin.func_70661_as().func_75492_a((double)this.posX, (double)this.posY, (double)this.posZ, 0.4D);
   }

   public boolean func_75253_b() {
      return (double)this.currentTick <= this.maxDuration || this.goblin.field_70170_p.field_73012_v.nextInt(3) == 0;
   }

   public void func_75251_c() {
      this.goblin.setWorshipping(false);
   }

   public void func_75246_d() {
      ++this.currentTick;
   }

   public void begin(TileEntity tile) {
      if (this.goblin.field_70170_p.field_73012_v.nextInt(3) != 0) {
         this.shouldBegin = true;
         this.posX = tile.field_145851_c;
         this.posY = tile.field_145848_d;
         this.posZ = tile.field_145849_e;
      }

   }
}
