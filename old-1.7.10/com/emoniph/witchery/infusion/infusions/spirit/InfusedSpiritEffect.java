package com.emoniph.witchery.infusion.infusions.spirit;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.entity.EntityBanshee;
import com.emoniph.witchery.entity.EntityPoltergeist;
import com.emoniph.witchery.entity.EntitySpectre;
import com.emoniph.witchery.entity.EntitySpirit;
import com.emoniph.witchery.util.Const;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class InfusedSpiritEffect {
   public static final ArrayList<InfusedSpiritEffect> effectList = new ArrayList();
   public static final InfusedSpiritEffect POPPET_ENHANCEMENT = new InfusedSpiritEnhancedPoppetEffect(1, 3, 1, 1, 1);
   public static final InfusedSpiritEffect SENTINAL = new InfusedSpiritSentinalEffect(2, 3, 3, 0, 0);
   public static final InfusedSpiritEffect SCREAMER = new InfusedSpiritScreamerEffect(3, 3, 0, 2, 0);
   public static final InfusedSpiritEffect TWISTER = new InfusedSpiritTwisterEffect(4, 3, 0, 0, 2);
   public static final InfusedSpiritEffect GHOST_WALKER = new InfusedSpiritGhostWalkerEffect(5, 3, 1, 1, 0);
   public static final InfusedSpiritEffect DEATH = new InfusedSpiritEffect(6, "death", 0, 5, 5, 5, false) {
      public boolean doUpdateEffect(TileEntity tile, boolean triggered, ArrayList<EntityLivingBase> foundEntities) {
         return true;
      }
   };
   public static final double RANGE = 16.0D;
   public static final double RANGE_SQ = 256.0D;
   public final int id;
   public final int spirits;
   public final int spectres;
   public final int banshees;
   public final int poltergeists;
   public final String unlocalizedName;
   private boolean inBook;
   private String localizedName;

   protected InfusedSpiritEffect(int id, String unlocalizedName, int spirits, int spectres, int banshees, int poltergeists) {
      this(id, unlocalizedName, spirits, spectres, banshees, poltergeists, true);
   }

   protected InfusedSpiritEffect(int id, String unlocalizedName, int spirits, int spectres, int banshees, int poltergeists, boolean inBook) {
      this.localizedName = null;
      this.id = id;
      this.spirits = spirits;
      this.spectres = spectres;
      this.banshees = banshees;
      this.poltergeists = poltergeists;
      this.unlocalizedName = unlocalizedName;
      this.inBook = inBook;

      while(effectList.size() <= id) {
         effectList.add((Object)null);
      }

      effectList.set(id, this);
   }

   public String getDescription() {
      StringBuffer sb = new StringBuffer();
      sb.append("§n");
      sb.append(this.getDisplayName());
      sb.append("§r");
      sb.append(Const.BOOK_NEWLINE);
      sb.append(Const.BOOK_NEWLINE);
      String description = Witchery.resource("witchery.fetish." + this.unlocalizedName + ".desc");
      if (!description.equals("witchery.fetish." + this.unlocalizedName + ".desc")) {
         sb.append(description);
         sb.append(Const.BOOK_NEWLINE);
         sb.append(Const.BOOK_NEWLINE);
      }

      sb.append(Witchery.resource("witchery.book.burning3"));
      sb.append(Const.BOOK_NEWLINE);
      sb.append(Const.BOOK_NEWLINE);
      if (this.spirits > 0) {
         sb.append(String.format("§8>§0  %s: %d", Witchery.resource("entity.witchery.spirit.name"), this.spirits));
         sb.append(Const.BOOK_NEWLINE);
      }

      if (this.spectres > 0) {
         sb.append(String.format("§8>§0  %s: %d", Witchery.resource("entity.witchery.spectre.name"), this.spectres));
         sb.append(Const.BOOK_NEWLINE);
      }

      if (this.banshees > 0) {
         sb.append(String.format("§8>§0  %s: %d", Witchery.resource("entity.witchery.banshee.name"), this.banshees));
         sb.append(Const.BOOK_NEWLINE);
      }

      if (this.poltergeists > 0) {
         sb.append(String.format("§8>§0  %s: %d", Witchery.resource("entity.witchery.poltergeist.name"), this.poltergeists));
         sb.append(Const.BOOK_NEWLINE);
      }

      return sb.toString();
   }

   private void setInBook(boolean inBook) {
      this.inBook = inBook;
   }

   public boolean isInBook() {
      return this.inBook;
   }

   private String getDisplayName() {
      if (this.localizedName == null) {
         this.localizedName = Witchery.resource("witchery.fetish." + this.unlocalizedName + ".name");
      }

      return this.localizedName;
   }

   public boolean isBound(IFetishTile tile) {
      return tile.getEffectType() == this.id;
   }

   public int getCooldownTicks() {
      return -1;
   }

   public static int tryBindFetish(World world, ItemStack stack, ArrayList<EntitySpirit> spiritList, ArrayList<EntitySpectre> spectreList, ArrayList<EntityBanshee> bansheeList, ArrayList<EntityPoltergeist> poltergeistList) {
      Iterator i$ = effectList.iterator();

      InfusedSpiritEffect effect;
      do {
         if (!i$.hasNext()) {
            return 0;
         }

         effect = (InfusedSpiritEffect)i$.next();
      } while(effect == null || effect.spirits > spiritList.size() || effect.spectres > spectreList.size() || effect.banshees > bansheeList.size() || effect.poltergeists > poltergeistList.size());

      if (!stack.func_77942_o()) {
         stack.func_77982_d(new NBTTagCompound());
      }

      NBTTagCompound nbtRoot = stack.func_77978_p();
      nbtRoot.func_74768_a("WITCSpiritEffect", effect.id);
      consumeSpirits(world, effect.spirits, spiritList);
      consumeSpirits(world, effect.spectres, spectreList);
      consumeSpirits(world, effect.banshees, bansheeList);
      consumeSpirits(world, effect.poltergeists, poltergeistList);
      return effect == DEATH ? 2 : 1;
   }

   private static <T extends EntityLiving> void consumeSpirits(World world, int count, ArrayList<T> list) {
      for(int i = 0; i < count; ++i) {
         EntityLiving entity = (EntityLiving)list.get(i);
         if (!world.field_72995_K) {
            entity.func_70106_y();
            ParticleEffect.PORTAL.send(SoundEffect.RANDOM_POP, entity, 1.0D, 2.0D, 16);
         }
      }

   }

   public static String getEffectDisplayName(ItemStack stack) {
      if (stack.func_77942_o()) {
         NBTTagCompound nbtRoot = stack.func_77978_p();
         if (nbtRoot.func_74764_b("WITCSpiritEffect")) {
            int effect = nbtRoot.func_74762_e("WITCSpiritEffect");
            if (effect > 0) {
               return ((InfusedSpiritEffect)effectList.get(effect)).getDisplayName();
            }
         }
      }

      return null;
   }

   public static int getEffectID(ItemStack stack) {
      if (stack.func_77942_o()) {
         NBTTagCompound nbtRoot = stack.func_77978_p();
         if (nbtRoot.func_74764_b("WITCSpiritEffect")) {
            return nbtRoot.func_74762_e("WITCSpiritEffect");
         }
      }

      return 0;
   }

   public static ItemStack setEffectID(ItemStack stack, int id) {
      if (!stack.func_77942_o()) {
         stack.func_77982_d(new NBTTagCompound());
      }

      NBTTagCompound nbtRoot = stack.func_77978_p();
      nbtRoot.func_74768_a("WITCSpiritEffect", id);
      return stack;
   }

   public static ItemStack setEffect(ItemStack stack, InfusedSpiritEffect effect) {
      return setEffectID(stack, effect.id);
   }

   public boolean isNearTo(EntityPlayer player) {
      Iterator i$ = player.field_70170_p.field_147482_g.iterator();

      while(i$.hasNext()) {
         Object obj = i$.next();
         if (obj instanceof IFetishTile) {
            IFetishTile tile = (IFetishTile)obj;
            if (player.func_70092_e(0.5D + (double)tile.getX(), 0.5D + (double)tile.getY(), 0.5D + (double)tile.getZ()) <= 256.0D && this.isBound(tile)) {
               return true;
            }
         }
      }

      return false;
   }

   public static InfusedSpiritEffect getEffect(IFetishTile tile) {
      return tile.getEffectType() > 0 ? (InfusedSpiritEffect)effectList.get(tile.getEffectType()) : null;
   }

   public abstract boolean doUpdateEffect(TileEntity var1, boolean var2, ArrayList<EntityLivingBase> var3);

   public boolean isRedstoneSignaller() {
      return false;
   }

   public double getRadius() {
      return 0.0D;
   }
}
