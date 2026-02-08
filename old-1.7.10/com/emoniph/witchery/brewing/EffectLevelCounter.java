package com.emoniph.witchery.brewing;

public class EffectLevelCounter {
   private int maxLevel;
   private int currentLevel;
   private int effects;

   public void increaseAvailableLevelIf(EffectLevel incement, EffectLevel ceilingLevel) {
      if (this.maxLevel < ceilingLevel.getLevel()) {
         this.maxLevel += incement.getLevel();
      }

   }

   public int remainingCapactiy() {
      return this.maxLevel - this.currentLevel;
   }

   public int usedCapacity() {
      return this.currentLevel;
   }

   public int getEffectCount() {
      return this.effects;
   }

   public boolean tryConsumeLevel(EffectLevel level) {
      if (this.canConsumeLevel(level)) {
         this.currentLevel += level.getLevel();
         ++this.effects;
         return true;
      } else {
         return false;
      }
   }

   public boolean canConsumeLevel(EffectLevel level) {
      return level.getLevel() + this.currentLevel <= this.maxLevel;
   }

   public boolean hasEffects() {
      return this.currentLevel > 0;
   }

   public boolean canIncreasePlayerSkill(int currentSkillLevel) {
      if (this.currentLevel <= this.maxLevel && this.maxLevel != 0) {
         if (currentSkillLevel < 30) {
            return this.maxLevel > 1 && this.currentLevel > 1;
         } else if (currentSkillLevel < 60) {
            return this.maxLevel >= 4 && this.currentLevel >= 4;
         } else if (currentSkillLevel < 90) {
            return this.maxLevel >= 6 && this.currentLevel >= 6;
         } else if (currentSkillLevel == 100) {
            return false;
         } else {
            return this.maxLevel >= 8 && this.currentLevel >= 8;
         }
      } else {
         return false;
      }
   }
}
