package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.ai.EntityAITreefydWander;
import com.emoniph.witchery.familiar.IFamiliar;
import com.emoniph.witchery.item.ItemTaglockKit;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityTreefyd extends EntityMob implements IEntitySelector {
   public EntityTreefyd.CreatureID testID = new EntityTreefyd.CreatureID(new UUID(0L, 0L), "");
   private static ArrayList<String> groupables = null;
   private ArrayList<String> knownPlayers = new ArrayList();
   private ArrayList<String> knownCreatureTypes = new ArrayList();
   private ArrayList<EntityTreefyd.CreatureID> knownCreatures = new ArrayList();

   public EntityTreefyd(World par1World) {
      super(par1World);
      this.func_70105_a(0.4F, 1.8F);
      this.func_70661_as().func_75491_a(true);
      this.func_70661_as().func_75495_e(true);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(4, new EntityAIAttackOnCollide(this, 1.0D, false));
      this.field_70714_bg.func_75776_a(5, new EntityAITreefydWander(this, 0.8D));
      this.field_70714_bg.func_75776_a(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70714_bg.func_75776_a(6, new EntityAILookIdle(this));
      this.field_70715_bh.func_75776_a(3, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 0, false, true, this));
      this.field_70715_bh.func_75776_a(2, new EntityAIHurtByTarget(this, false));
   }

   public boolean func_82704_a(Entity entity) {
      if (entity.getClass() != this.getClass() && !(entity instanceof EntityHornedHuntsman) && !(entity instanceof EntityEnt) && !(entity instanceof EntityFlying) && !(entity instanceof EntityFlyingTameable) && !(entity instanceof EntityAmbientCreature) && !(entity instanceof EntityWaterMob) && !this.isFamiliar(entity) && !(entity instanceof EntityCovenWitch) && !(entity instanceof EntityCorpse)) {
         if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entity;
            String ownerName = this.getOwnerName();
            if (ownerName != null && !ownerName.isEmpty() && player.func_70005_c_().equals(ownerName)) {
               return false;
            }

            if (this.knownPlayers != null && this.knownPlayers.contains(player.func_70005_c_())) {
               return false;
            }
         }

         if (entity instanceof EntityLiving) {
            EntityLiving creature = (EntityLiving)entity;
            if (this.knownCreatureTypes != null && this.knownCreatureTypes.contains(creature.func_70005_c_())) {
               return false;
            }

            this.testID.id = entity.func_110124_au();
            if (this.knownCreatures != null && this.knownCreatures.contains(this.testID)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public int func_70627_aG() {
      return super.func_70627_aG() * 2;
   }

   protected boolean func_70085_c(EntityPlayer player) {
      if (!this.field_70170_p.field_72995_K && player != null && player.func_70005_c_().equals(this.getOwnerName())) {
         ItemStack stack = player.func_70694_bm();
         if (stack != null && stack.func_77973_b() == Witchery.Items.TAGLOCK_KIT) {
            this.func_70624_b((EntityLivingBase)null);
            ItemTaglockKit.BoundType boundEntityType = Witchery.Items.TAGLOCK_KIT.getBoundEntityType(stack, 1);
            switch(boundEntityType) {
            case PLAYER:
               String otherUsername = Witchery.Items.TAGLOCK_KIT.getBoundUsername(stack, 1);
               if (!player.func_70005_c_().equals(otherUsername)) {
                  if (!player.func_70093_af() && !this.knownPlayers.contains(otherUsername)) {
                     this.knownPlayers.add(otherUsername);
                  } else {
                     if (!player.func_70093_af() || !this.knownPlayers.contains(otherUsername)) {
                        this.showCurrentKnownEntities(player);
                        return super.func_70085_c(player);
                     }

                     this.knownPlayers.remove(otherUsername);
                  }

                  --stack.field_77994_a;
                  if (stack.field_77994_a <= 0) {
                     player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                  }

                  if (player instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                  }

                  this.showCurrentKnownEntities(player);
                  return true;
               }
               break;
            case CREATURE:
               UUID otherCreature = Witchery.Items.TAGLOCK_KIT.getBoundCreatureID(stack, 1);
               String creatureName = Witchery.Items.TAGLOCK_KIT.getBoundEntityDisplayName(stack, 1);
               if (!otherCreature.equals(this.func_110124_au())) {
                  if (this.isGroupableCreature(otherCreature, creatureName)) {
                     if (!player.func_70093_af() && !this.knownCreatureTypes.contains(creatureName)) {
                        this.knownCreatureTypes.add(creatureName);
                     } else {
                        if (!player.func_70093_af() || !this.knownCreatureTypes.contains(creatureName)) {
                           this.showCurrentKnownEntities(player);
                           return super.func_70085_c(player);
                        }

                        this.knownCreatureTypes.remove(creatureName);
                     }
                  } else {
                     EntityTreefyd.CreatureID creatureID = new EntityTreefyd.CreatureID(otherCreature, creatureName);
                     if (!player.func_70093_af() && !this.knownCreatures.contains(creatureID)) {
                        this.knownCreatures.add(creatureID);
                     } else {
                        if (!player.func_70093_af() || !this.knownCreatures.contains(creatureID)) {
                           this.showCurrentKnownEntities(player);
                           return super.func_70085_c(player);
                        }

                        this.knownCreatures.remove(creatureID);
                     }
                  }

                  --stack.field_77994_a;
                  if (stack.field_77994_a <= 0) {
                     player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                  }

                  if (player instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
                  }
               }

               this.showCurrentKnownEntities(player);
               return true;
            case NONE:
            }
         } else if (stack != null && Witchery.Items.GENERIC.itemSeedsTreefyd.isMatch(stack)) {
            if (!this.field_70170_p.field_72995_K) {
               EntityTreefyd entity = new EntityTreefyd(this.field_70170_p);
               entity.func_70012_b(0.5D + this.field_70165_t, this.field_70163_u, 0.5D + this.field_70161_v, 0.0F, 0.0F);
               entity.func_110161_a((IEntityLivingData)null);
               entity.setOwner(player.func_70005_c_());
               entity.func_110163_bv();
               entity.knownPlayers = (ArrayList)this.knownPlayers.clone();
               entity.knownCreatureTypes = (ArrayList)this.knownCreatureTypes.clone();
               entity.knownCreatures = (ArrayList)this.knownCreatures.clone();
               this.field_70170_p.func_72838_d(entity);
               ParticleEffect.SLIME.send(SoundEffect.MOB_SILVERFISH_KILL, this, 1.0D, 2.0D, 16);
               ParticleEffect.EXPLODE.send(SoundEffect.NONE, this, 1.0D, 2.0D, 16);
            }

            if (!player.field_71075_bZ.field_75098_d) {
               --stack.field_77994_a;
            }
         } else if (stack != null && Witchery.Items.GENERIC.itemCreeperHeart.isMatch(stack)) {
            if (!this.field_70170_p.field_72995_K) {
               this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(100.0D);
               this.func_70606_j(this.func_110138_aP());
               this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(4.0D);
               ParticleEffect.SLIME.send(SoundEffect.MOB_SILVERFISH_KILL, this, 0.5D, 2.0D, 16);
            }

            if (!player.field_71075_bZ.field_75098_d) {
               --stack.field_77994_a;
            }
         } else if (stack != null && Witchery.Items.GENERIC.itemDemonHeart.isMatch(stack)) {
            if (!this.field_70170_p.field_72995_K) {
               this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(150.0D);
               this.func_70606_j(this.func_110138_aP());
               this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(5.0D);
               ParticleEffect.FLAME.send(SoundEffect.MOB_ENDERDRAGON_GROWL, this, 0.5D, 2.0D, 16);
            }

            if (!player.field_71075_bZ.field_75098_d) {
               --stack.field_77994_a;
            }
         } else if (stack != null && stack.func_77973_b() == Witchery.Items.BOLINE && !this.field_70170_p.field_72995_K) {
            this.setSentinal(!this.isSentinal());
         }

         this.showCurrentKnownEntities(player);
      }

      return super.func_70085_c(player);
   }

   private boolean isGroupableCreature(UUID otherCreature, String creatureName) {
      if (groupables == null) {
         groupables = new ArrayList();
         this.addGroupableType(EntityVillager.class);
         this.addGroupableType(EntityGoblin.class);
         this.addGroupableType(EntitySheep.class);
         this.addGroupableType(EntityCow.class);
         this.addGroupableType(EntityMooshroom.class);
         this.addGroupableType(EntityChicken.class);
         this.addGroupableType(EntityPig.class);
         this.addGroupableType(EntityHorse.class);
      }

      return groupables.contains(creatureName);
   }

   private void addGroupableType(Class<? extends EntityLiving> className) {
      String name = (String)EntityList.field_75626_c.get(className);
      if (name != null) {
         String localName = StatCollector.func_74838_a("entity." + name + ".name");
         groupables.add(localName);
      }

   }

   private void showCurrentKnownEntities(EntityPlayer player) {
      StringBuffer sb = new StringBuffer();
      String ownerName = this.getOwnerName();
      if (ownerName != null && !ownerName.isEmpty()) {
         sb.append(this.getOwnerName());
      }

      Iterator i$;
      String s;
      for(i$ = this.knownPlayers.iterator(); i$.hasNext(); sb.append(s)) {
         s = (String)i$.next();
         if (sb.length() > 0) {
            sb.append(", ");
         }
      }

      i$ = this.knownCreatureTypes.iterator();

      while(i$.hasNext()) {
         s = (String)i$.next();
         if (sb.length() > 0) {
            sb.append(", ");
         }

         sb.append("#");
         sb.append(s);
      }

      EntityTreefyd.CreatureID cid;
      for(i$ = this.knownCreatures.iterator(); i$.hasNext(); sb.append(cid.toString())) {
         cid = (EntityTreefyd.CreatureID)i$.next();
         if (sb.length() > 0) {
            sb.append(", ");
         }
      }

      String message = this.func_70005_c_() + " (" + sb.toString() + ")";
      ChatUtil.sendPlain(player, message);
   }

   private boolean isFamiliar(Entity entity) {
      if (entity instanceof IFamiliar) {
         IFamiliar familiar = (IFamiliar)entity;
         return familiar.isFamiliar();
      } else {
         return false;
      }
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.treefyd.name");
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.25D);
      this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(3.0D);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(50.0D);
   }

   public boolean func_70650_aV() {
      return true;
   }

   public boolean func_70652_k(Entity entity) {
      if (!this.field_70170_p.field_72995_K && entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         if (!player.func_70644_a(Potion.field_76440_q)) {
            player.func_70690_d(new PotionEffect(Potion.field_76440_q.field_76415_H, 100, 0));
         }
      }

      return super.func_70652_k(entity);
   }

   public int func_82143_as() {
      return this.func_70638_az() == null ? 3 : 3 + (int)(this.func_110143_aJ() - 1.0F);
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(17, "");
      this.field_70180_af.func_75682_a(18, Integer.valueOf(0));
   }

   public boolean isSentinal() {
      return this.field_70180_af.func_75679_c(18) == 1;
   }

   protected void setSentinal(boolean screaming) {
      this.field_70180_af.func_75692_b(18, Integer.valueOf(screaming ? 1 : 0));
   }

   public void func_70014_b(NBTTagCompound nbtRoot) {
      super.func_70014_b(nbtRoot);
      if (this.getOwnerName() == null) {
         nbtRoot.func_74778_a("Owner", "");
      } else {
         nbtRoot.func_74778_a("Owner", this.getOwnerName());
      }

      NBTTagList nbtCreatures;
      Iterator i$;
      String typeName;
      NBTTagCompound nbtKnownCreature;
      if (this.knownPlayers.size() > 0) {
         nbtCreatures = new NBTTagList();
         i$ = this.knownPlayers.iterator();

         while(i$.hasNext()) {
            typeName = (String)i$.next();
            nbtKnownCreature = new NBTTagCompound();
            nbtKnownCreature.func_74778_a("PlayerName", typeName);
            nbtCreatures.func_74742_a(nbtKnownCreature);
         }

         nbtRoot.func_74782_a("KnownPlayers", nbtCreatures);
      }

      if (this.knownCreatureTypes.size() > 0) {
         nbtCreatures = new NBTTagList();
         i$ = this.knownCreatureTypes.iterator();

         while(i$.hasNext()) {
            typeName = (String)i$.next();
            nbtKnownCreature = new NBTTagCompound();
            nbtKnownCreature.func_74778_a("CreatureTypeName", typeName);
            nbtCreatures.func_74742_a(nbtKnownCreature);
         }

         nbtRoot.func_74782_a("KnownCreatureTypes", nbtCreatures);
      }

      if (this.knownCreatures.size() > 0) {
         nbtCreatures = new NBTTagList();
         i$ = this.knownCreatures.iterator();

         while(i$.hasNext()) {
            EntityTreefyd.CreatureID creatureID = (EntityTreefyd.CreatureID)i$.next();
            nbtKnownCreature = new NBTTagCompound();
            nbtKnownCreature.func_74772_a("CreatureMost", creatureID.id.getMostSignificantBits());
            nbtKnownCreature.func_74772_a("CreatureLeast", creatureID.id.getLeastSignificantBits());
            nbtKnownCreature.func_74778_a("CreatureName", creatureID.name);
            nbtCreatures.func_74742_a(nbtKnownCreature);
         }

         nbtRoot.func_74782_a("KnownCreatures", nbtCreatures);
      }

      nbtRoot.func_74757_a("SentinalPlant", this.isSentinal());
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      String s = nbtRoot.func_74779_i("Owner");
      if (s.length() > 0) {
         this.setOwner(s);
      }

      NBTTagList nbtCreatures;
      int i;
      NBTTagCompound nbtKnownCreature;
      String playerName;
      if (nbtRoot.func_74764_b("KnownPlayers")) {
         nbtCreatures = nbtRoot.func_150295_c("KnownPlayers", 10);
         this.knownPlayers = new ArrayList();

         for(i = 0; i < nbtCreatures.func_74745_c(); ++i) {
            nbtKnownCreature = nbtCreatures.func_150305_b(i);
            playerName = nbtKnownCreature.func_74779_i("PlayerName");
            if (playerName != null && !playerName.isEmpty()) {
               this.knownPlayers.add(playerName);
            }
         }
      }

      if (nbtRoot.func_74764_b("KnownCreatureTypes")) {
         nbtCreatures = nbtRoot.func_150295_c("KnownCreatureTypes", 10);
         this.knownCreatureTypes = new ArrayList();

         for(i = 0; i < nbtCreatures.func_74745_c(); ++i) {
            nbtKnownCreature = nbtCreatures.func_150305_b(i);
            playerName = nbtKnownCreature.func_74779_i("CreatureTypeName");
            if (playerName != null && !playerName.isEmpty()) {
               this.knownCreatureTypes.add(playerName);
            }
         }
      }

      if (nbtRoot.func_74764_b("KnownCreatures")) {
         nbtCreatures = nbtRoot.func_150295_c("KnownCreatures", 10);
         this.knownCreatures = new ArrayList();

         for(i = 0; i < nbtCreatures.func_74745_c(); ++i) {
            nbtKnownCreature = nbtCreatures.func_150305_b(i);
            playerName = nbtKnownCreature.func_74779_i("PlayerName");
            long uuidMost = nbtKnownCreature.func_74763_f("CreatureMost");
            long uuidLeast = nbtKnownCreature.func_74763_f("CreatureLeast");
            String cname = nbtKnownCreature.func_74779_i("CreatureName");
            if (uuidMost != 0L || uuidLeast != 0L) {
               UUID creatureID = new UUID(uuidMost, uuidLeast);
               this.knownCreatures.add(new EntityTreefyd.CreatureID(creatureID, cname));
            }
         }
      }

      if (nbtRoot.func_74764_b("SentinalPlant")) {
         this.setSentinal(nbtRoot.func_74767_n("SentinalPlant"));
      }

   }

   public String getOwnerName() {
      return this.field_70180_af.func_75681_e(17);
   }

   public void setOwner(String par1Str) {
      this.func_110163_bv();
      this.field_70180_af.func_75692_b(17, par1Str);
   }

   public EntityPlayer getOwnerEntity() {
      return this.field_70170_p.func_72924_a(this.getOwnerName());
   }

   public void func_70071_h_() {
      super.func_70071_h_();
   }

   protected String func_70621_aR() {
      return "mob.silverfish.hit";
   }

   protected String func_70639_aQ() {
      return "witchery:mob.treefyd.treefyd_say";
   }

   protected String func_70673_aS() {
      return "mob.creeper.death";
   }

   protected Item func_146068_u() {
      return Item.func_150898_a(Blocks.field_150328_O);
   }

   protected void func_70600_l(int par1) {
      this.func_70099_a(Witchery.Items.GENERIC.itemSeedsTreefyd.createStack(), 0.0F);
   }

   protected boolean func_70692_ba() {
      return false;
   }

   private static class CreatureID {
      UUID id;
      String name;

      public CreatureID(UUID id, String name) {
         this.id = id;
         this.name = name;
      }

      public boolean equals(Object obj) {
         if (obj == null) {
            return false;
         } else if (obj == this) {
            return true;
         } else if (obj instanceof UUID) {
            return this.id.equals((UUID)obj);
         } else {
            return obj.getClass() == this.getClass() ? this.id.equals(((EntityTreefyd.CreatureID)obj).id) : false;
         }
      }

      public String toString() {
         return this.name;
      }
   }
}
