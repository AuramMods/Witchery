package com.emoniph.witchery.brewing;

import com.emoniph.witchery.util.Log;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BrewItemKey {
   public final Item ITEM;
   public final int DAMAGE;
   private final int ITEM_HASH;

   public BrewItemKey(Item item) {
      this((Item)item, 0);
   }

   public BrewItemKey(Block block) {
      this((Block)block, 0);
   }

   public BrewItemKey(Block block, int damage) {
      this(Item.func_150898_a(block), damage);
   }

   public BrewItemKey(Item item, int damage) {
      this.ITEM = item;
      this.DAMAGE = damage;
      if (this.ITEM != null) {
         if (this.ITEM.func_77658_a() != null) {
            this.ITEM_HASH = this.ITEM.func_77658_a().hashCode();
         } else {
            Log.instance().warning(String.format("unlocalizedname=null for item passed to BrewItemKey constructor (another mod cleared it?)"));
            this.ITEM_HASH = "".hashCode();
         }
      } else {
         Log.instance().warning("item=null passed to BrewItemKey constructor, block to item map must be busted somehow (tweaking blocks?).");
         this.ITEM_HASH = "".hashCode();
      }

   }

   public ItemStack toStack() {
      return new ItemStack(this.ITEM, 1, this.DAMAGE);
   }

   public int hashCode() {
      int result = 17;
      int result = 37 * result + this.ITEM_HASH;
      return result;
   }

   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         if (obj == this) {
            return true;
         } else {
            BrewItemKey other = (BrewItemKey)obj;
            return this.ITEM == other.ITEM && (this.DAMAGE == 32767 || other.DAMAGE == 32767 || this.DAMAGE == other.DAMAGE);
         }
      } else {
         return false;
      }
   }

   public static BrewItemKey fromStack(ItemStack stack) {
      return new BrewItemKey(stack.func_77973_b(), stack.func_77960_j());
   }
}
