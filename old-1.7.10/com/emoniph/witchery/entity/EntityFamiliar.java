package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.ai.EntityAIFamiliarFindDiamonds;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TameableUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIOcelotAttack;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityFamiliar extends EntityOcelot {
   private int searches = 0;
   private static final int WATCH_KEY_OBJ_TO_FIND = 23;
   private static final Item[] ITEMS;
   private static final Block[] ORES;
   private static final Integer[] ORE_DEPTH;

   public EntityFamiliar(World world) {
      super(world);
      this.func_70105_a(0.8F, 0.8F);
      this.func_70661_as().func_75491_a(true);
      this.field_70714_bg.field_75782_a.clear();
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, this.field_70911_d);
      this.field_70714_bg.func_75776_a(3, new EntityAIFollowOwner(this, 1.0D, 10.0F, 5.0F));
      this.field_70714_bg.func_75776_a(4, new EntityAIFamiliarFindDiamonds(this, 1.33D));
      this.field_70714_bg.func_75776_a(5, new EntityAILeapAtTarget(this, 0.3F));
      this.field_70714_bg.func_75776_a(6, new EntityAIOcelotAttack(this));
      this.field_70714_bg.func_75776_a(7, new EntityAIWander(this, 0.8D));
      this.field_70714_bg.func_75776_a(8, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.func_70912_b(1);
      this.field_70180_af.func_75682_a(23, -1);
   }

   public String func_70005_c_() {
      return this.func_94056_bM() ? this.func_94057_bL() : StatCollector.func_74838_a("entity.witchery.familiar.name");
   }

   public void setItemIDToFind(int itemID) {
      this.field_70180_af.func_75692_b(23, itemID);
   }

   public int getItemIDToFind() {
      return this.field_70180_af.func_75679_c(23);
   }

   public void func_70014_b(NBTTagCompound nbtTag) {
      super.func_70014_b(nbtTag);
      nbtTag.func_74768_a("ItemToFind", this.getItemIDToFind());
      nbtTag.func_74768_a("Searches", this.searches);
   }

   public void func_70037_a(NBTTagCompound nbtTag) {
      super.func_70037_a(nbtTag);
      this.setItemIDToFind(nbtTag.func_74762_e("ItemToFind"));
      this.searches = nbtTag.func_74762_e("Searches");
   }

   @SideOnly(Side.CLIENT)
   public float func_70053_R() {
      super.func_70053_R();
      return 0.0F;
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(40.0D);
   }

   public boolean func_70652_k(Entity par1Entity) {
      return par1Entity.func_70097_a(DamageSource.func_76358_a(this), 0.5F);
   }

   public boolean func_70085_c(EntityPlayer player) {
      if (this.func_70909_n() && TameableUtil.isOwner(this, player) && !this.field_70170_p.field_72995_K) {
         ItemStack item = player.func_71045_bC();
         int itemToFind = this.hasOre(item);
         if (itemToFind != -1) {
            this.setItemIDToFind(itemToFind);
            ++this.searches;
            --item.field_77994_a;
            if (item.field_77994_a <= 0) {
               player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
            }

            double[] probs = new double[]{0.0D, 0.6D, 0.75D, 0.85D, 0.95D};
            double chance = this.field_70170_p.field_73012_v.nextDouble();
            if (this.searches <= 5 && (this.searches <= 1 || !(chance < probs[Math.max(this.searches - 1, 1)]))) {
               SoundEffect.RANDOM_ORB.playAtPlayer(player.field_70170_p, player);
            } else {
               ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, this, 1.0D, 1.0D, 16);
               this.func_70106_y();
            }
         } else {
            this.field_70911_d.func_75270_a(!this.func_70906_o());
         }
      }

      return true;
   }

   public Block getBlockIDToFind() {
      int idToFind = this.getItemIDToFind();
      return idToFind != -1 ? ORES[idToFind] : null;
   }

   public void clearItemToFind() {
      this.setItemIDToFind(-1);
   }

   public int getDepthToFind() {
      int idToFind = this.getItemIDToFind();
      return idToFind != -1 ? ORE_DEPTH[idToFind] : 1;
   }

   private int hasOre(ItemStack item) {
      return item == null ? -1 : Arrays.asList(ITEMS).indexOf(item.func_77973_b());
   }

   protected String func_70639_aQ() {
      return "mob.pig.say";
   }

   protected String func_70621_aR() {
      return "mob.pig.say";
   }

   protected String func_70673_aS() {
      return "mob.pig.death";
   }

   protected void func_145780_a(int par1, int par2, int par3, Block par4) {
      this.func_85030_a("mob.pig.step", 0.15F, 1.0F);
   }

   protected float func_70599_aP() {
      return 0.4F;
   }

   protected void func_70628_a(boolean par1, int par2) {
      this.func_70099_a(Witchery.Items.GENERIC.itemSpectralDust.createStack(), 0.0F);
   }

   public boolean func_70877_b(ItemStack par1ItemStack) {
      return false;
   }

   public boolean func_70878_b(EntityAnimal par1EntityAnimal) {
      return false;
   }

   public EntityOcelot func_90011_a(EntityAgeable par1EntityAgeable) {
      return null;
   }

   public IEntityLivingData func_110161_a(IEntityLivingData data) {
      this.func_110148_a(SharedMonsterAttributes.field_111265_b).func_111121_a(new AttributeModifier("Random spawn bonus", this.field_70146_Z.nextGaussian() * 0.05D, 1));
      return data;
   }

   public void setAISitting(boolean b) {
      this.func_70904_g(true);
      this.field_70911_d.func_75270_a(true);
   }

   static {
      ITEMS = new Item[]{Items.field_151045_i, Items.field_151166_bC, Items.field_151043_k, Items.field_151042_j, Items.field_151137_ax, Items.field_151100_aR, Items.field_151044_h};
      ORES = new Block[]{Blocks.field_150482_ag, Blocks.field_150412_bA, Blocks.field_150352_o, Blocks.field_150366_p, Blocks.field_150450_ax, Blocks.field_150369_x, Blocks.field_150365_q};
      ORE_DEPTH = new Integer[]{14, 14, 30, 64, 16, 30, 64};
   }
}
