package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.Config;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemEntityLocator extends ItemBase {
   @SideOnly(Side.CLIENT)
   private IIcon[] icons;

   public ItemEntityLocator() {
      this.func_77656_e(0);
      this.func_77625_d(1);
   }

   @SideOnly(Side.CLIENT)
   public void func_94581_a(IIconRegister iconRegister) {
      this.icons = new IIcon[33];

      for(int i = 0; i < this.icons.length; ++i) {
         this.icons[i] = iconRegister.func_94245_a(this.func_111208_A() + i);
      }

      this.field_77791_bV = this.icons[0];
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean advTooltips) {
      super.func_77624_a(stack, player, list, advTooltips);
      String entityID = Witchery.Items.TAGLOCK_KIT.getBoundEntityDisplayName(stack, 1);
      if (entityID != null && !entityID.isEmpty()) {
         list.add(String.format(Witchery.resource("item.witcheryTaglockKit.boundto"), entityID));
      } else {
         list.add(Witchery.resource("item.witcheryTaglockKit.unbound"));
      }

   }

   public IIcon func_77617_a(int damageValue) {
      return damageValue > 0 && damageValue < this.icons.length ? this.icons[damageValue] : this.icons[0];
   }

   public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
      item.func_77964_b(0);
      return super.onDroppedByPlayer(item, player);
   }

   public void func_77663_a(ItemStack stack, World world, Entity player, int inventorySlot, boolean isHeldItem) {
      if (world != null && world.field_72995_K && world.func_72820_D() % 10L == 2L) {
         if (Witchery.Items.TAGLOCK_KIT.isTaglockPresent(stack, 1)) {
            double d3 = 0.0D;
            EntityLivingBase target = Witchery.Items.TAGLOCK_KIT.getBoundEntity(world, (Entity)null, stack, 1);
            if (target == null || target.field_71093_bK != player.field_71093_bK && (target.field_71093_bK != 0 || player.field_71093_bK != Config.instance().dimensionDreamID)) {
               d3 = Math.random() * 3.141592653589793D * 2.0D;
            } else {
               double playerX = player.field_70165_t;
               double playerZ = player.field_70161_v;
               double d4 = target.field_70165_t - playerX;
               double d5 = target.field_70161_v - playerZ;
               double playerYaw = (double)player.field_70177_z;
               playerYaw %= 360.0D;
               d3 = -((playerYaw - 90.0D) * 3.141592653589793D / 180.0D - Math.atan2(d5, d4));
            }

            int SIZE = this.icons.length - 1;

            int i;
            for(i = (int)((d3 / 6.283185307179586D + 1.0D) * (double)SIZE) % SIZE; i < 0; i = (i + SIZE) % SIZE) {
            }

            stack.func_77964_b(i + 1);
         } else {
            stack.func_77964_b(0);
         }
      }

   }
}
