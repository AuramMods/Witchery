package com.emoniph.witchery.brewing;

import com.emoniph.witchery.Witchery;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.util.StatCollector;

public class BrewNameBuilder {
   int dispersalExtent;
   int dispersalDuration;
   int strength;
   int durationModifier;
   int totalStrength;
   int totalDuration;
   boolean inverted;
   private boolean terse;
   ArrayList<BrewNameBuilder.Part> parts = new ArrayList();
   ArrayList<String> prefixes = new ArrayList();
   ArrayList<String> postfixes = new ArrayList();
   boolean removePowerCeiling;

   public BrewNameBuilder(boolean terse) {
      this.terse = terse;
   }

   private static String ticksToElapsedTime(int p_76337_0_) {
      int j = p_76337_0_ / 20;
      int k = j / 60;
      j %= 60;
      return j < 10 ? k + ":0" + j : k + ":" + j;
   }

   public void append(String text, String invertedText, long duration, long invertedDuration) {
      StringBuilder builder = new StringBuilder();
      if (this.inverted) {
         builder.append(invertedText);
      } else {
         builder.append(text);
      }

      this.inverted = false;
      if (!this.terse && this.strength > 0) {
         builder.append(" ");
         builder.append(StatCollector.func_74838_a("potion.potency." + this.strength));
      }

      this.strength = 0;
      this.parts.add(new BrewNameBuilder.Part(builder.toString(), duration * (long)(this.durationModifier + 1)));
      this.durationModifier = 0;
   }

   public void appendPrefix(String text) {
      this.prefixes.add(text);
      if (!this.terse && this.dispersalExtent > 0) {
         this.prefixes.add(StatCollector.func_74838_a("potion.potency." + this.dispersalExtent));
      }

      this.dispersalExtent = 0;
      if (!this.terse && this.dispersalDuration > 0) {
         this.prefixes.add(String.format("[%s %s]", Witchery.resource("witchery:brew.lifetime"), StatCollector.func_74838_a("potion.potency." + this.dispersalDuration)));
      }

      this.dispersalDuration = 0;
   }

   public void appendPostfix(String text) {
      this.postfixes.add(text);
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      Iterator i$ = this.prefixes.iterator();

      String text;
      while(i$.hasNext()) {
         text = (String)i$.next();
         builder.append(text);
         builder.append(" ");
      }

      if (this.terse) {
         builder.append(Witchery.resource("witchery:brew.potion"));
         builder.append(" ");
      } else {
         builder.append("\n");
      }

      if (this.parts.size() > 0) {
         for(int i = 0; i < this.parts.size(); ++i) {
            builder.append(((BrewNameBuilder.Part)this.parts.get(i)).toString(this.prefixes.size() > 0, this.terse));
            builder.append(this.terse ? (i < this.parts.size() - 2 ? ", " : (i < this.parts.size() - 1 ? " & " : " ")) : "\n");
         }
      } else {
         builder.append(Witchery.resource("witchery:brew.potionwater"));
         if (!this.terse) {
            builder.append("\n");
         }
      }

      i$ = this.postfixes.iterator();

      while(i$.hasNext()) {
         text = (String)i$.next();
         builder.append(text);
         builder.append(" ");
      }

      builder.trimToSize();
      return builder.toString();
   }

   public void addStrength(int strength2) {
      if (this.totalStrength < 7 || this.removePowerCeiling) {
         this.strength += strength2;
         this.totalStrength += strength2;
      }

   }

   public void addDuration(int duration) {
      if (this.totalDuration < 7 || this.removePowerCeiling) {
         this.durationModifier += duration;
         this.totalDuration += duration;
      }

   }

   private static class Part {
      String base;
      long duration;

      private Part(String base, long duration) {
         this.base = base;
         this.duration = duration;
      }

      public String toString(boolean splash, boolean terse) {
         long modDuration = splash && this.duration > 0L ? this.duration / 2L : this.duration;
         return !terse && modDuration > 0L ? this.base + " [" + BrewNameBuilder.ticksToElapsedTime((int)modDuration) + "]" : this.base;
      }

      // $FF: synthetic method
      Part(String x0, long x1, Object x2) {
         this(x0, x1);
      }
   }
}
