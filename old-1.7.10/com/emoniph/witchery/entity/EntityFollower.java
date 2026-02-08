package com.emoniph.witchery.entity;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityFollower extends EntityTameable {
   private EntityAIWander aiWander = new EntityAIWander(this, 0.8D);
   int ticksToLive = -1;
   int transformCount;
   @SideOnly(Side.CLIENT)
   private ThreadDownloadImageData downloadImageSkin;
   @SideOnly(Side.CLIENT)
   private ResourceLocation locationSkin;
   private static final String[] FIRST_NAMES_M = new String[]{"Abraham", "Adam", "Adrian", "Alex", "Alexander", "Allen", "Ambrose", "Andrew", "Anthony", "Arthur", "Avery", "Barnaby", "Bartholomew", "Benedict", "Benjamin", "Bernard", "Billy", "Bobby", "Charles", "Charley", "Christopher", "Colin", "Conrad", "Cuthbert", "Daniel", "Danny", "Davey", "David", "Edmund", "Edward", "Francis", "Fred", "Freddy", "Geoffrey", "George", "Georgie", "Gerard", "Gilbert", "Giles", "Gregory", "Hans", "Hansel", "Heinrich", "Henry", "Hugh", "Humphrey", "Isaac", "Jack", "Jacques", "James", "Jamie", "Jerome", "Jim", "Jimmy", "John", "Johnny", "Joseph", "Julian", "Lancelot", "Lawrence", "Leonard", "Lou", "Luke", "Mark", "Martin", "Mathias", "Matthew", "Merlin", "Michael", "Miles", "Nat", "Nathan", "Nathaniel", "Ned", "Nicholas", "Oliver", "Oswyn", "Patrick", "Paul", "Peter", "Philip", "Piers", "Ralph", "Reynold", "Richard", "Ricky", "Robert", "Robin", "Roger", "Rowland", "Samuel", "Simon", "Solomon", "Stephen", "Terence", "Thomas", "Tim", "Tobias", "Tom", "Tommy", "Valentine", "Walter", "Wendell", "Will", "William", "Willie"};
   private static final String[] FIRST_NAMES_F = new String[]{"Agnes", "Alice", "Amy", "Anna", "Annabella", "Anne", "Arabella", "Audrey", "Avis", "Barbara", "Beatrice", "Becky", "Bella", "Belle", "Bertha", "Bessy", "Betty", "Blanche", "Bo", "Bonny", "Bridget", "Catalina", "Catherine", "Cecily", "Charity", "Charlotte", "Christina", "Christine", "Cinderella", "Cindy", "Clara", "Clarissa", "Clemence", "Clementine", "Constance", "Daisy", "Denise", "Dorothy", "Edith", "Elinor", "Elizabeth", "Ella", "Ellen", "Elsa", "Elsie", "Emma", "Eve", "Evelyn", "Fawn", "Flora", "Florence", "Floretta", "Fortune", "Frances", "Frideswide", "Gertrude", "Gillian", "Ginger", "Goat", "Goatley", "Goldie", "Grace", "Gretel", "Helen", "Hilda", "Hazel", "Isabel", "Jane", "Janet", "Jemima", "Jill", "Joan", "Joyce", "Judith", "Julia", "Juliet", "Katherine", "Katie", "Kitty", "Lena", "Lily", "Liza", "Lizzie", "Lucy", "Mabel", "Maggie", "Margaret", "Margery", "Maria", "Marion", "Marlene", "Martha", "Mary", "Maud", "Mildred", "Millicent", "Molly", "Odette", "Pansy", "Pearl", "Petunia", "Philippa", "Polly", "Rachel", "Rapunzel", "Rebecca", "Rose", "Rosie", "Ruth", "Sarah", "Shiela", "Snow", "Susanna", "Susie", "Sybil", "Talia", "Thomasina", "Trudy", "Ursula", "Winifred"};
   private static final String[] SURNAMES = new String[]{"Apple", "Applegreen", "Applerose", "Appleseed", "Appleton", "Applewhite", "Baker", "Barnes", "Bean", "Beanblossom", "Beanstock", "Beaste", "Beasten", "Bell", "Berry", "Bird", "Blackbird", "Blackwood", "Boar", "Botter", "Bowers", "Bremen", "Brockett", "Buckle", "Butcher", "Candle", "Castle", "Castleton", "Cherry", "Cherrytree", "Cherrywood", "Cherrywell", "Cottage", "Daw", "Deer", "Dilly", "Dove", "Duck", "Duckfield", "Duckling", "Faery", "Fairy", "Fiddle", "Fiddler", "Fisher", "Fitcher", "Flinders", "Friday", "Frogg", "Frogley", "Frost", "Gold", "Goldencrown", "Goldhair", "Goodfellow", "Goose", "Gooseberry", "Gosling", "Gray", "Green", "Greengrass", "Griggs", "Grimm", "Grundy", "Hare", "Hay", "Hazeltree", "Hickory", "Hood", "Horner", "Hubbard", "Hunter", "Korbes", "Lamb", "Lampkin", "Lark", "Locket", "Locks", "MacDonald", "Mack", "Malone", "Marsh", "McDiddler", "Meadow", "Meadows", "Merrypips", "Miller", "Mills", "Mockingbird", "Monday", "Mouse", "Mouseley", "Muffet", "Mulberry", "Nimble", "Nutt", "O'Hare", "O'Lairy", "Pea", "Peartree", "Pease", "Peep", "Pie", "Pigeon", "Pinchme", "Piper", "Porgy", "Porridge", "Pott", "Pumpkin", "Pumpkinseed", "Reynard", "River", "Rivers", "Roley", "Rooster", "Roseberry", "Rosebottom", "Roseleaf", "Shoe", "Shoemaker", "Shorter", "Silver", "Slipper", "Sprat", "Saturday", "Sparrow", "Spindle", "Spindler", "Spinner", "Star", "Stone", "Stonebridge", "Sunday", "Swan", "Tailor", "Thatcher", "Thumb", "Thursday", "Toad", "Tower", "Towers", "Trot", "Tucker", "Tuesday", "Twist", "Wednesday", "White", "Whittington", "Winkie", "Wolf", "Wolfram", "Wolfson", "Wolfwood", "Woodcroft", "Woods"};

   public EntityFollower(World world) {
      super(world);
      this.func_70105_a(0.6F, 1.8F);
      this.func_70661_as().func_75495_e(true);
      this.func_70661_as().func_75491_a(false);
      this.field_70714_bg.func_75776_a(1, new EntityAISwimming(this));
      this.field_70714_bg.func_75776_a(2, new EntityAIMoveTowardsRestriction(this, 2.0D));
      this.field_70714_bg.func_75776_a(3, new EntityAIFollowOwner(this, 1.0D, 2.0F, 4.0F));
      this.field_70714_bg.func_75776_a(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.field_70728_aV = 0;
   }

   protected int func_70693_a(EntityPlayer p_70693_1_) {
      return 0;
   }

   protected void func_110147_ax() {
      super.func_110147_ax();
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.3D);
   }

   public void setTTL(int ticks) {
      this.ticksToLive = ticks;
   }

   public EntityAgeable func_90011_a(EntityAgeable lover) {
      return null;
   }

   public String func_70005_c_() {
      switch(this.getFollowerType()) {
      case 0:
         return Witchery.resource("entity.witchery.follower.elle.name");
      default:
         return super.func_70005_c_();
      }
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_75682_a(18, 0);
      this.field_70180_af.func_75682_a(19, String.valueOf(""));
   }

   public String getSkin() {
      return this.field_70180_af.func_75681_e(19);
   }

   public void setSkin(String mode) {
      this.field_70180_af.func_75692_b(19, mode);
   }

   public int getFollowerType() {
      return this.field_70180_af.func_75679_c(18);
   }

   public void func_70030_z() {
      super.func_70030_z();
      if (!this.field_70170_p.field_72995_K && this.field_70173_aa == 1 && this.getFollowerType() == 5) {
         String skin = this.getSkin();
         if (skin != null && !skin.isEmpty()) {
            EntityPlayer player = this.field_70170_p.func_72924_a(this.getSkin());
            if (player != null) {
               for(int i = 0; i <= 4; ++i) {
                  ItemStack stack = player.func_71124_b(i);
                  if (stack != null) {
                     this.func_70062_b(i, stack.func_77946_l());
                  } else {
                     this.func_70062_b(i, (ItemStack)null);
                  }
               }
            }
         }
      }

      if (!this.field_70170_p.field_72995_K && this.field_70173_aa % 40 == 5 && this.getFollowerType() == 5) {
         this.attractAttention();
      }

   }

   public void func_70636_d() {
      super.func_70636_d();
      if (!this.field_70170_p.field_72995_K && this.func_70089_S() && this.ticksToLive >= 0 && --this.ticksToLive == 0) {
         this.func_70106_y();
      }

   }

   public void setFollowerType(int followerType) {
      this.field_70180_af.func_75692_b(18, followerType);
      if (followerType == 0) {
         this.field_70178_ae = true;
      } else if (followerType <= 5) {
         this.field_70714_bg.func_75776_a(5, this.aiWander);
      }

   }

   protected int func_70682_h(int par1) {
      return this.getFollowerType() == 0 ? par1 : super.func_70682_h(par1);
   }

   protected boolean func_70650_aV() {
      return true;
   }

   protected void func_82160_b(boolean p_82160_1_, int p_82160_2_) {
   }

   protected void func_70628_a(boolean par1, int par2) {
      if (this.getFollowerType() >= 1 && this.getFollowerType() <= 4) {
         this.func_70099_a(Witchery.Items.GENERIC.itemHeartOfGold.createStack(), 0.1F);
      }

   }

   private boolean isCourseTraversable(double waypointX, double waypointY, double waypointZ, double p_70790_7_) {
      double d4 = (waypointX - this.field_70165_t) / p_70790_7_;
      double d5 = (waypointY - this.field_70163_u) / p_70790_7_;
      double d6 = (waypointZ - this.field_70161_v) / p_70790_7_;
      AxisAlignedBB axisalignedbb = this.field_70121_D.func_72329_c();

      for(int i = 1; (double)i < p_70790_7_; ++i) {
         axisalignedbb.func_72317_d(d4, d5, d6);
         if (!this.field_70170_p.func_72945_a(this, axisalignedbb).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   protected void func_70619_bc() {
      super.func_70619_bc();
      if (!this.field_70170_p.field_72995_K && this.field_70173_aa % 10 == 1 && this.getFollowerType() == 0) {
         this.doElleAI();
      }

   }

   public void doElleAI() {
      if (this.func_110175_bO()) {
         if (this.func_110173_bK()) {
            ++this.transformCount;
            if (this.transformCount == 20) {
               EntityLivingBase owner = this.func_70902_q();
               if (owner != null && owner instanceof EntityPlayer) {
                  ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, (EntityPlayer)owner, "item.witchery:glassgoblet.lilithquestsummon");
                  SoundEffect.WITCHERY_MOB_LILITH_TALK.playAt((EntityLiving)this);
               }
            } else if (this.transformCount == 40) {
               this.transformCount = 0;
               ParticleEffect.INSTANT_SPELL.send(SoundEffect.FIREWORKS_BLAST1, this, 1.0D, 1.0D, 16);
               EntityLilith vampire = new EntityLilith(this.field_70170_p);
               vampire.func_110163_bv();
               vampire.func_82149_j(this);
               vampire.func_110161_a((IEntityLivingData)null);
               this.field_70170_p.func_72900_e(this);
               this.field_70170_p.func_72838_d(vampire);
               vampire.field_70170_p.func_72889_a((EntityPlayer)null, 1017, (int)vampire.field_70165_t, (int)vampire.field_70163_u, (int)vampire.field_70161_v, 0);
               EntityLivingBase owner = this.func_70902_q();
               if (owner != null && owner instanceof EntityPlayer) {
                  ChatUtil.sendTranslated(EnumChatFormatting.DARK_PURPLE, (EntityPlayer)owner, "item.witchery:glassgoblet.lilithquestsummon2");
                  SoundEffect.WITCHERY_MOB_LILITH_TALK.playAt((EntityLiving)vampire);
               }

               this.field_70170_p.func_72876_a(vampire, vampire.field_70165_t, vampire.field_70163_u, vampire.field_70161_v, 6.0F, true);
            }
         } else {
            double d0 = (double)this.func_110172_bL().field_71574_a - this.field_70165_t;
            double d1 = (double)this.func_110172_bL().field_71572_b - this.field_70163_u;
            double d2 = (double)this.func_110172_bL().field_71573_c - this.field_70161_v;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d3 > 0.0D) {
               d3 = (double)MathHelper.func_76133_a(d3);
               double waypointX = this.field_70165_t + (double)((this.field_70146_Z.nextFloat() * 2.0F - 1.0F) * 16.0F);
               double waypointY = this.field_70163_u + (double)((this.field_70146_Z.nextFloat() * 2.0F - 1.0F) * 16.0F);
               double waypointZ = this.field_70161_v + (double)((this.field_70146_Z.nextFloat() * 2.0F - 1.0F) * 16.0F);
               if (this.isCourseTraversable(waypointX, waypointY, waypointZ, d3)) {
                  this.field_70159_w += d0 / d3 * 0.2D;
                  this.field_70181_x += d1 / d3 * 0.2D;
                  this.field_70179_y += d2 / d3 * 0.2D;
               }
            }
         }
      } else {
         for(int i = 0; i < 10; ++i) {
            int j = MathHelper.func_76128_c(this.field_70165_t + (double)this.field_70146_Z.nextInt(30) - 15.0D);
            int k = MathHelper.func_76128_c(this.field_70121_D.field_72338_b + (double)this.field_70146_Z.nextInt(6) - 3.0D);
            int l = MathHelper.func_76128_c(this.field_70161_v + (double)this.field_70146_Z.nextInt(30) - 15.0D);
            if (isLavaPool(this.field_70170_p, j, k, l, 6)) {
               this.func_110171_b(j, k, l, 2);
               this.func_152115_b("");
               break;
            }
         }
      }

   }

   private static boolean isLavaPool(World world, int x, int y, int z, int max) {
      if (isLavaPoolColumn(world, x, y, z) && isLavaPoolColumn(world, x + 1, y, z) && isLavaPoolColumn(world, x - 1, y, z) && isLavaPoolColumn(world, x, y, z + 1) && isLavaPoolColumn(world, x, y, z - 1)) {
         int max2 = max * max;

         for(int dx = x - max; dx <= x + max; ++dx) {
            for(int dz = z - max; dz <= z + max; ++dz) {
               double dist = Coord.distanceSq((double)x, (double)y, (double)z, (double)dx, (double)y, (double)dz);
               if (dist <= (double)max2 && world.func_147439_a(dx, y, dz) != Blocks.field_150353_l) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean isLavaPoolColumn(World world, int x, int y, int z) {
      if (world.func_147439_a(x, y, z) == Blocks.field_150353_l && world.func_147437_c(x, y + 1, z) && world.func_147437_c(x, y + 2, z)) {
         int depth = 4;

         for(int dy = y - depth; dy < dy; ++dy) {
            if (world.func_147439_a(x, y, z) != Blocks.field_150353_l) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public void setOwner(EntityPlayer player) {
      this.func_70903_f(true);
      this.func_70778_a((PathEntity)null);
      this.func_70624_b((EntityLivingBase)null);
      this.func_70606_j(20.0F);
      this.func_152115_b(player.func_110124_au().toString());
      this.field_70170_p.func_72960_a(this, (byte)7);
   }

   public void func_70037_a(NBTTagCompound nbtRoot) {
      super.func_70037_a(nbtRoot);
      this.setFollowerType(nbtRoot.func_74762_e("FollowerType"));
      if (nbtRoot.func_74764_b("TicksToLive")) {
         this.ticksToLive = nbtRoot.func_74762_e("TicksToLive");
      } else {
         this.ticksToLive = -1;
      }

      if (nbtRoot.func_74764_b("Skin")) {
         this.setSkin(nbtRoot.func_74779_i("Skin"));
      }

   }

   public void func_70109_d(NBTTagCompound nbtRoot) {
      super.func_70109_d(nbtRoot);
      nbtRoot.func_74768_a("FollowerType", this.getFollowerType());
      nbtRoot.func_74768_a("TicksToLive", this.ticksToLive);
      nbtRoot.func_74778_a("Skin", this.getSkin());
   }

   public static String generateFollowerName(int followerType) {
      Random ra = new Random();
      return followerType != 4 ? String.format("%s %s", FIRST_NAMES_F[ra.nextInt(FIRST_NAMES_F.length)], SURNAMES[ra.nextInt(SURNAMES.length)]) : String.format("%s %s", FIRST_NAMES_M[ra.nextInt(FIRST_NAMES_M.length)], SURNAMES[ra.nextInt(SURNAMES.length)]);
   }

   @SideOnly(Side.CLIENT)
   public ResourceLocation getLocationSkin() {
      if (this.locationSkin == null) {
         this.setupCustomSkin();
      }

      return this.locationSkin != null ? this.locationSkin : AbstractClientPlayer.field_110314_b;
   }

   @SideOnly(Side.CLIENT)
   private void setupCustomSkin() {
      String owner = this.getSkin();
      if (owner != null && !owner.isEmpty()) {
         this.locationSkin = AbstractClientPlayer.func_110311_f(owner);
         this.downloadImageSkin = AbstractClientPlayer.func_110304_a(this.locationSkin, owner);
      }

   }

   public void attractAttention() {
      if (!this.field_70170_p.field_72995_K) {
         String owner = this.getSkin();
         if (owner != null && !owner.isEmpty()) {
            EntityPlayer player = this.field_70170_p.func_72924_a(owner);
            if (player != null) {
               List<EntityMob> list = this.field_70170_p.func_72872_a(EntityMob.class, this.field_70121_D.func_72314_b(16.0D, 8.0D, 16.0D));
               Iterator i$ = list.iterator();

               while(i$.hasNext()) {
                  EntityMob mob = (EntityMob)i$.next();
                  if (mob.func_70638_az() == player) {
                     mob.func_70604_c(this);
                     mob.func_70624_b(this);
                     mob.func_70784_b(this);
                  }
               }
            }
         }
      }

   }
}
