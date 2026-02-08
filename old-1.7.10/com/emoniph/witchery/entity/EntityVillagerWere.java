package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityVillagerWere extends EntityVillager {
   private boolean infectious;

   public EntityVillagerWere(World world) {
      this(world, 0, false);
   }

   public EntityVillagerWere(World world, int profession, boolean infectious) {
      super(world, profession);
      this.infectious = infectious;
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      nbtRoot.func_74757_a("Infectious", this.infectious);
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      this.infectious = nbtRoot.func_74767_n("Infectious");
   }

   protected void func_70619_bc() {
      super.func_70619_bc();
      if (!this.field_70170_p.field_72995_K && this.field_70173_aa % 100 == 3 && !this.func_70631_g_() && CreatureUtil.isFullMoon(this.field_70170_p) && !this.func_70644_a(Witchery.Potions.WOLFSBANE)) {
         this.convertToWolfman();
      }

   }

   protected void convertToWolfman() {
      EntityWolfman entity = new EntityWolfman(this.field_70170_p);
      if (this.infectious) {
         entity.setInfectious();
      }

      entity.setFormerProfession(this.func_70946_n(), this.field_70956_bz, this.field_70963_i);
      entity.func_110163_bv();
      entity.func_82149_j(this);
      entity.func_110161_a((IEntityLivingData)null);
      this.field_70170_p.func_72900_e(this);
      this.field_70170_p.func_72838_d(entity);
      this.field_70170_p.func_72889_a((EntityPlayer)null, 1017, (int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v, 0);
      SoundEffect.WITCHERY_MOB_WOLFMAN_HOWL.playAt(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v);
   }
}
