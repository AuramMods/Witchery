package com.emoniph.witchery.infusion.infusions.symbols;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.util.Const;
import com.emoniph.witchery.util.TimeUtil;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class SymbolEffect {
   private final int effectID;
   private final String unlocalisedName;
   private final int chargeCost;
   private final boolean curse;
   private final boolean fallsToEarth;
   private final String knowledgeKey;
   private final boolean isVisible;
   private byte[] defaultStrokes;
   private final int cooldownTicks;

   public SymbolEffect(int effectID, String unlocalisedName) {
      this(effectID, unlocalisedName, 1, false, false, (String)null, 0, true);
   }

   public SymbolEffect(int effectID, String unlocalisedName, int spellCost, boolean curse, boolean fallsToEarth, String knowledgeKey, int cooldown) {
      this(effectID, unlocalisedName, spellCost, curse, fallsToEarth, knowledgeKey, cooldown, true);
   }

   public SymbolEffect(int effectID, String unlocalisedName, int spellCost, boolean curse, boolean fallsToEarth, String knowledgeKey, int cooldown, boolean isVisible) {
      this.effectID = effectID;
      this.unlocalisedName = unlocalisedName;
      this.chargeCost = spellCost;
      this.curse = curse;
      this.fallsToEarth = fallsToEarth;
      this.knowledgeKey = knowledgeKey;
      this.cooldownTicks = cooldown;
      this.isVisible = isVisible;
   }

   public int getEffectID() {
      return this.effectID;
   }

   public boolean isCurse() {
      return this.curse;
   }

   public boolean isUnforgivable() {
      return this.curse && this.knowledgeKey == null;
   }

   public String getLocalizedName() {
      return Witchery.resource(this.unlocalisedName);
   }

   public abstract void perform(World var1, EntityPlayer var2, int var3);

   public int getChargeCost(World world, EntityPlayer player, int level) {
      return MathHelper.func_76128_c(Math.pow(2.0D, (double)(level - 1)) * (double)this.chargeCost);
   }

   public boolean fallsToEarth() {
      return this.fallsToEarth;
   }

   public boolean hasValidInfusion(EntityPlayer player, int infusionID) {
      if (player.field_71075_bZ.field_75098_d) {
         return true;
      } else if (infusionID <= 0) {
         return false;
      } else {
         return !this.isUnforgivable() || infusionID == Witchery.Recipes.infusionBeast.infusionID;
      }
   }

   public boolean isVisible(EntityPlayer player) {
      return this.isVisible;
   }

   public String getDescription() {
      StringBuffer sb = new StringBuffer();
      sb.append("§n");
      sb.append(Witchery.resource(this.unlocalisedName));
      sb.append("§r");
      sb.append(Const.BOOK_NEWLINE);
      sb.append(Const.BOOK_NEWLINE);
      String descKey = this.unlocalisedName + ".info";
      String description = Witchery.resource(descKey);
      if (description != null && !description.isEmpty() && !description.equals(descKey)) {
         sb.append(description);
         sb.append(Const.BOOK_NEWLINE);
         sb.append(Const.BOOK_NEWLINE);
      }

      sb.append("§8");
      sb.append(Witchery.resource("witchery.book.wands.strokes"));
      sb.append("§0");
      sb.append(Const.BOOK_NEWLINE);
      int i = 1;
      byte[] arr$ = this.defaultStrokes;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         byte stroke = arr$[i$];
         sb.append(i++);
         sb.append(": ");
         sb.append(Witchery.resource("witchery.book.wands.stroke." + stroke));
         sb.append(Const.BOOK_NEWLINE);
      }

      return sb.toString();
   }

   public void setDefaultStrokes(byte[] strokes) {
      this.defaultStrokes = strokes;
   }

   public boolean hasValidKnowledge(EntityPlayer player, NBTTagCompound nbtPlayer) {
      if (player.field_71075_bZ.field_75098_d) {
         return true;
      } else if (this.knowledgeKey != null) {
         if (nbtPlayer.func_74764_b("WITCSpellBook")) {
            NBTTagCompound nbtSpells = nbtPlayer.func_74775_l("WITCSpellBook");
            return nbtSpells.func_74767_n(this.knowledgeKey);
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public void acquireKnowledge(EntityPlayer player) {
      if (this.knowledgeKey != null) {
         NBTTagCompound nbtPlayer = Infusion.getNBT(player);
         if (nbtPlayer != null) {
            if (!nbtPlayer.func_74764_b("WITCSpellBook")) {
               nbtPlayer.func_74782_a("WITCSpellBook", new NBTTagCompound());
            }

            NBTTagCompound nbtSpells = nbtPlayer.func_74775_l("WITCSpellBook");
            nbtSpells.func_74757_a(this.knowledgeKey, true);
         }
      }

   }

   public static String getKnowledge(EntityPlayer player) {
      StringBuilder sb = new StringBuilder();
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      if (nbtPlayer != null && nbtPlayer.func_74764_b("WITCSpellBook")) {
         Iterator i$ = EffectRegistry.instance().getEffects().iterator();

         while(i$.hasNext()) {
            SymbolEffect effect = (SymbolEffect)i$.next();
            if (effect.knowledgeKey != null && effect.hasValidKnowledge(player, nbtPlayer)) {
               if (sb.length() > 0) {
                  sb.append(", ");
               }

               sb.append(effect.getLocalizedName());
            }
         }
      }

      return sb.toString();
   }

   public long cooldownRemaining(EntityPlayer player, NBTTagCompound nbtPlayer) {
      if (this.cooldownTicks > 0 && this.knowledgeKey != null && nbtPlayer.func_74764_b("WITCSpellBook")) {
         NBTTagCompound nbtSpells = nbtPlayer.func_74775_l("WITCSpellBook");
         long lastUseTime = nbtSpells.func_74763_f(this.knowledgeKey + "LastUse");
         long timeNow = TimeUtil.getServerTimeInTicks();
         if (timeNow < lastUseTime + (long)this.cooldownTicks) {
            return lastUseTime + (long)this.cooldownTicks - timeNow;
         }
      }

      return 0L;
   }

   public void setOnCooldown(EntityPlayer player) {
      if (this.cooldownTicks > 0 && this.knowledgeKey != null && !player.field_71075_bZ.field_75098_d) {
         NBTTagCompound nbtPlayer = Infusion.getNBT(player);
         if (nbtPlayer != null && nbtPlayer.func_74764_b("WITCSpellBook")) {
            NBTTagCompound nbtSpells = nbtPlayer.func_74775_l("WITCSpellBook");
            nbtSpells.func_74772_a(this.knowledgeKey + "LastUse", TimeUtil.getServerTimeInTicks());
         }
      }

   }

   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         if (obj == this) {
            return true;
         } else {
            SymbolEffect other = (SymbolEffect)obj;
            return other.effectID == this.effectID;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = 17;
      int result = 37 * result + this.effectID;
      return result;
   }
}
