package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.TimeUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PotionFeelNoPain extends PotionBase implements IHandleLivingHurt, IHandleLivingUpdate {
   public PotionFeelNoPain(int id, int color) {
      super(id, color);
      this.setIncurable();
   }

   public void onLivingUpdate(World world, EntityLivingBase entity, LivingUpdateEvent event, int amplifier, int duration) {
      if (!world.field_72995_K && world.func_72820_D() % 20L == 2L && amplifier > 0 && !entity.func_70644_a(Potion.field_76431_k) && !entity.func_70644_a(Witchery.Potions.STOUT_BELLY) && world.field_73012_v.nextInt(5 - Math.min(amplifier, 3)) == 0) {
         entity.func_70690_d(new PotionEffect(Potion.field_76431_k.field_76415_H, TimeUtil.secsToTicks(6 + amplifier * 2)));
      }

   }

   public boolean handleAllHurtEvents() {
      return false;
   }

   public void onLivingHurt(World world, EntityLivingBase entity, LivingHurtEvent event, int amplifier) {
      if (!world.field_72995_K && entity instanceof EntityPlayer && (event.source.func_76355_l() == "mob" || event.source.func_76355_l() == "player" || event.source.func_76346_g() != null && event.source.func_76346_g() instanceof EntityLivingBase)) {
         EntityPlayer player = (EntityPlayer)entity;
         float currentHealth = entity.func_110143_aJ();
         float newHealth = EntityUtil.getHealthAfterDamage(event, currentHealth, entity);
         float damage = currentHealth - newHealth;
         int food = player.func_71024_bL().func_75116_a();
         if (food > 0) {
            int modifiedDamage = (int)Math.ceil(amplifier > 0 ? (double)Math.max(damage / (float)amplifier, amplifier > 1 ? 1.0F : 2.0F) : (double)Math.max(damage * 2.0F, 3.0F));
            int foodPenalty = Math.min(modifiedDamage, food);
            player.func_71024_bL().func_75122_a(-foodPenalty, 2.0F);
            event.setCanceled(true);
         }
      }

   }
}
