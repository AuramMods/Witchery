package com.emoniph.witchery.brewing;

import com.emoniph.witchery.Witchery;

public class BrewNamePart {
   protected final String text;
   protected final String invertedText;
   protected final BrewNamePart.Position position;
   protected long baseDuration;
   protected long invertedDuration;

   public BrewNamePart(String resourceId) {
      this(resourceId, resourceId, BrewNamePart.Position.NONE);
   }

   public BrewNamePart(String resourceId, BrewNamePart.Position position) {
      this(resourceId, resourceId, position);
   }

   public BrewNamePart(String resourceId, String invertedResourceId) {
      this(resourceId, invertedResourceId, BrewNamePart.Position.NONE);
   }

   public BrewNamePart(String resourceId, String invertedResourceId, BrewNamePart.Position position) {
      this.text = Witchery.resource(resourceId);
      this.invertedText = Witchery.resource(invertedResourceId);
      this.position = position;
   }

   public void applyTo(BrewNameBuilder nameBuilder) {
      switch(this.position) {
      case NONE:
         nameBuilder.append(this.text, this.invertedText, this.baseDuration, this.invertedDuration);
         break;
      case PREFIX:
         nameBuilder.appendPrefix(this.text);
         break;
      case POSTFIX:
         nameBuilder.appendPostfix(this.text);
      }

   }

   public BrewNamePart setBaseDuration(long baseDuration, long invertedDuration) {
      this.baseDuration = baseDuration;
      this.invertedDuration = invertedDuration;
      return this;
   }

   public BrewNamePart setBaseDuration(int baseDuration) {
      return this.setBaseDuration((long)baseDuration, (long)baseDuration);
   }

   public static enum Position {
      NONE,
      PREFIX,
      POSTFIX;
   }
}
