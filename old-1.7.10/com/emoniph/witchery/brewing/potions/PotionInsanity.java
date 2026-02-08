package com.emoniph.witchery.brewing.potions;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.EntityIllusionCreeper;
import com.emoniph.witchery.entity.EntityIllusionSpider;
import com.emoniph.witchery.entity.EntityIllusionZombie;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class PotionInsanity extends PotionBase {
   public PotionInsanity(int id, int color) {
      super(id, true, color);
   }

   public void postContructInitialize() {
      this.setIncurable();
      this.setPermenant();
      this.hideInventoryText();
   }

   public boolean func_76397_a(int duration, int amplifier) {
      return duration % 20 == 13;
   }

   public void func_76394_a(EntityLivingBase entity, int amplifier) {
      if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         World world = entity.field_70170_p;
         int level = Math.max(0, amplifier) + 1;
         int x = MathHelper.func_76128_c(entity.field_70165_t);
         int y = MathHelper.func_76128_c(entity.field_70163_u);
         int z = MathHelper.func_76128_c(entity.field_70161_v);
         if (world.field_73012_v.nextInt(level > 2 ? 25 : (level > 1 ? 30 : 35)) == 0) {
            Class<? extends EntityCreature> creatureType = null;
            switch(world.field_73012_v.nextInt(3)) {
            case 0:
            default:
               creatureType = EntityIllusionCreeper.class;
               break;
            case 1:
               creatureType = EntityIllusionSpider.class;
               break;
            case 2:
               creatureType = EntityIllusionZombie.class;
            }

            int MAX_DISTANCE = true;
            int MIN_DISTANCE = true;
            Infusion.spawnCreature(world, creatureType, x, y, z, player, 4, 9);
         } else if (level >= 4 && world.field_73012_v.nextInt(20) == 0) {
            SoundEffect sound = SoundEffect.NONE;
            switch(world.field_73012_v.nextInt(3)) {
            case 0:
            case 2:
            case 3:
            default:
               sound = SoundEffect.RANDOM_EXPLODE;
               break;
            case 1:
               sound = SoundEffect.MOB_ENDERMAN_IDLE;
            }

            sound.playOnlyTo((EntityPlayer)entity, 1.0F, 1.0F);
         }
      }

   }

   public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
      int factor = effect.func_76459_b() / 60 % 7;
      String s1 = I18n.func_135052_a(Witchery.resource("witchery:potion.insanity." + factor), new Object[0]);
      mc.field_71466_p.func_78261_a(s1, x + 10 + 18, y + 6, 16777215);
      String s = Potion.func_76389_a(effect);
      mc.field_71466_p.func_78261_a(s, x + 10 + 18, y + 6 + 10, 8355711);
   }
}
