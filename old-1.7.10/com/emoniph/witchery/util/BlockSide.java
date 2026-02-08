package com.emoniph.witchery.util;

public enum BlockSide {
   NONE(-2),
   RAYTRACE_FULL_LENGTH(-1),
   BOTTOM(0),
   TOP(1),
   EAST(2),
   WEST(3),
   NORTH(4),
   SOUTH(5);

   final int sideID;

   private BlockSide(int sideID) {
      this.sideID = sideID;
   }

   public boolean isEqual(int side) {
      return this.sideID == side;
   }

   public int getSideID() {
      return this.sideID;
   }

   public static BlockSide fromInteger(int integer) {
      switch(integer) {
      case -2:
      default:
         return NONE;
      case -1:
         return RAYTRACE_FULL_LENGTH;
      case 0:
         return BOTTOM;
      case 1:
         return TOP;
      case 2:
         return EAST;
      case 3:
         return WEST;
      case 4:
         return NORTH;
      case 5:
         return SOUTH;
      }
   }
}
