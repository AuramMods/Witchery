package com.emoniph.witchery.util;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.network.PacketSound;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public enum SoundEffect {
   NONE(""),
   RANDOM_ORB("random.orb"),
   RANDOM_FIZZ("random.fizz"),
   NOTE_SNARE("note.snare"),
   WATER_SPLASH("game.player.swim.splash"),
   DAMAGE_HIT("damage.hit"),
   FIREWORKS_BLAST1("fireworks.blast"),
   WATER_SWIM("game.player.swim"),
   NOTE_HARP("note.harp"),
   NOTE_PLING("note.pling"),
   RANDOM_EXPLODE("random.explode"),
   RANDOM_POP("random.pop"),
   DIG_CLOTH("dig.cloth"),
   MOB_SLIME_BIG("mob.slime.big"),
   MOB_SLIME_SMALL("mob.slime.small"),
   MOB_ZOMBIE_DEATH("mob.zombie.death"),
   MOB_ENDERMEN_PORTAL("mob.endermen.portal"),
   FIRE_FIRE("fire.fire"),
   FIRE_IGNITE("fire.ignite"),
   MOB_GHAST_FIREBALL("mob.ghast.fireball"),
   MOB_WITHER_SPAWN("mob.wither.spawn"),
   MOB_HORSE_SKELETON_DEATH("mob.horse.skeleton.death"),
   RANDOM_SPLASH("witchery:random.splash"),
   MOB_SILVERFISH_KILL("mob.silverfish.kill"),
   MOB_ZOMBIE_INFECT("mob.zombie.infect"),
   MOB_WOLF_DEATH("mob.wolf.death"),
   MOB_OCELOT_DEATH("mob.ocelot.death"),
   MOB_ENDERDRAGON_GROWL("mob.enderdragon.growl"),
   MOB_HORSE_SKELETON_HIT("mob.horse.skeleton.hit"),
   RANDOM_LEVELUP("random.levelup"),
   MOB_SPIDER_SAY("mob.spider.say"),
   MOB_ZOMBIE_SAY("mob.zombie.say"),
   WITCHERY_RANDOM_THEYCOME("witchery:random.theycome"),
   MOB_ENDERDRAGON_HIT("mob.enderdragon.hit"),
   WITCHERY_MOB_BABA_DEATH("witchery:mob.baba.baba_death"),
   WITCHERY_MOB_BABA_LIVING("witchery:mob.baba.baba_living"),
   WITCHERY_RANDOM_CLICK("witchery:random.click"),
   WITCHERY_RANDOM_WINDUP("witchery:random.wind_up"),
   WITCHERY_RANDOM_LOVED("witchery:random.loved"),
   MOB_ENDERMAN_IDLE("mob.enderman.idle"),
   MOB_WITHER_DEATH("mob.wither.death"),
   RANDOM_BREATH("random.breath"),
   WITCHERY_MOB_SPECTRE_SPECTRE_HIT("witchery:mob.spectre.spectre_hit"),
   WITCHERY_MOB_SPECTRE_SPECTRE_SAY("witchery:mob.spectre.spectre_say"),
   MOB_BLAZE_DEATH("mob.blaze.death"),
   WITCHERY_MOB_IMP_LAUGH("witchery:mob.imp.laugh"),
   MOB_GHAST_DEATH("mob.ghast.death"),
   MOB_CREEPER_DEATH("mob.creeper.death"),
   WITCHERY_RANDOM_CHALK("witchery:random.chalk"),
   WITCHERY_MOB_WOLFMAN_HOWL("witchery:mob.wolfman.howl"),
   WITCHERY_MOB_WOLFMAN_EAT("witchery:mob.wolfman.eat"),
   WITCHERY_MOB_WOLFMAN_LORD("witchery:mob.wolfman.lord"),
   WITCHERY_RANDOM_HORN("witchery:random.horn"),
   WITCHERY_RANDOM_MANTRAP("witchery:random.mantrap"),
   WITCHERY_MOB_WOLFMAN_TALK("witchery:mob.wolfman.say"),
   WITCHERY_RANDOM_HYPNOSIS("witchery:random.hypnosis"),
   WITCHERY_RANDOM_DRINK("witchery:random.drink"),
   WITCHERY_RANDOM_POOF("witchery:random.poof"),
   WITCHERY_MOB_LILITH_TALK("witchery:mob.lilith.say"),
   WITCHERY_RANDOM_SWORD_DRAW("witchery:random.sworddraw"),
   WITCHERY_RANDOM_SWORD_SHEATHE("witchery:random.swordsheathe"),
   WITCHERY_MOB_REFLECTION_SPEECH("witchery:mob.reflection.speech");

   final String sound;

   private SoundEffect(String sound) {
      this.sound = sound;
   }

   public String toString() {
      return this.sound;
   }

   public void playAtPlayer(World world, EntityPlayer player) {
      this.playAtPlayer(world, player, 0.5F);
   }

   public void playAtPlayer(World world, EntityPlayer player, float volume) {
      if (!world.field_72995_K) {
         world.func_72956_a(player, this.sound, volume, 0.4F / ((float)world.field_73012_v.nextDouble() * 0.4F + 0.8F));
      }

   }

   public void playAtPlayer(World world, EntityPlayer player, float volume, float pitch) {
      if (!world.field_72995_K) {
         world.func_72956_a(player, this.sound, volume, pitch);
      }

   }

   public void playAt(EntityLiving entity) {
      this.playAt(entity, 0.5F);
   }

   public void playAt(EntityLiving entity, float volume) {
      this.playAt(entity, volume, 0.4F / ((float)entity.field_70170_p.field_73012_v.nextDouble() * 0.4F + 0.8F));
   }

   public void playAt(EntityLiving entity, float volume, float pitch) {
      if (!entity.field_70170_p.field_72995_K) {
         entity.field_70170_p.func_72956_a(entity, this.sound, volume, pitch);
      }

   }

   public void playAt(TileEntity tile) {
      this.playAt(tile, 0.5F);
   }

   public void playAt(TileEntity tile, float volume) {
      this.playAt(tile.func_145831_w(), (double)tile.field_145851_c, (double)tile.field_145848_d, (double)tile.field_145849_e, volume);
   }

   public void playAt(World world, double x, double y, double z) {
      this.playAt(world, x, y, z, 0.5F);
   }

   public void playAt(World world, double x, double y, double z, float volume) {
      this.playAt(world, x, y, z, volume, 0.4F / ((float)world.field_73012_v.nextDouble() * 0.4F + 0.8F));
   }

   public void playAt(World world, double x, double y, double z, float volume, float pitch) {
      if (!world.field_72995_K) {
         world.func_72908_a(x, y, z, this.sound, volume, pitch);
      }

   }

   public void playOnlyTo(EntityPlayer player) {
      this.playOnlyTo(player, -1.0F, -1.0F);
   }

   public void playOnlyTo(EntityPlayer player, float volume, float pitch) {
      if (this != NONE) {
         Witchery.packetPipeline.sendTo((IMessage)(new PacketSound(this, player, volume, pitch)), (EntityPlayer)player);
      }

   }
}
