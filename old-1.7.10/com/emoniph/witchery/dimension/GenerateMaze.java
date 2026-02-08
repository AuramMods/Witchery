package com.emoniph.witchery.dimension;

import com.emoniph.witchery.Witchery;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class GenerateMaze {
   private final int width;
   private final int depth;
   private final int[][] maze;
   public static final int WALL_HEIGHT = 6;

   public GenerateMaze(int width, int depth, Random rand) {
      this.width = width;
      this.depth = depth;
      this.maze = new int[width][depth];
      this.generateMaze(0, 0, rand);
   }

   public void display(World world, int origX, int origY, int origZ, Block walls, Block floor) {
      int i;
      int j;
      for(i = 0; i < this.depth; ++i) {
         for(j = 0; j < this.width; ++j) {
            if ((this.maze[j][i] & 1) == 0) {
               this.drawWall(world, origX + j * 2, origY, origZ + 2 * i, walls, floor);
               this.drawWall(world, origX + j * 2 + 1, origY, origZ + 2 * i, walls, floor);
            } else {
               this.drawWall(world, origX + j * 2, origY, origZ + 2 * i, walls, floor);
               this.drawPassage(world, origX + j * 2 + 1, origY, origZ + 2 * i, walls, floor);
            }
         }

         this.drawWall(world, origX + j * 2, origY, origZ + 2 * i, walls, floor);

         for(j = 0; j < this.width; ++j) {
            if ((this.maze[j][i] & 8) == 0) {
               this.drawWall(world, origX + j * 2, origY, origZ + 2 * i + 1, walls, floor);
               this.drawPassage(world, origX + j * 2 + 1, origY, origZ + 2 * i + 1, walls, floor);
            } else {
               this.drawPassage(world, origX + j * 2, origY, origZ + 2 * i + 1, walls, floor);
               this.drawPassage(world, origX + j * 2 + 1, origY, origZ + 2 * i + 1, walls, floor);
            }
         }

         this.drawWall(world, origX + j * 2, origY, origZ + 2 * i + 1, walls, floor);
      }

      for(j = 0; j < this.width; ++j) {
         this.drawWall(world, origX + j * 2, origY, origZ + 2 * i, walls, floor);
         this.drawWall(world, origX + j * 2 + 1, origY, origZ + 2 * i, walls, floor);
      }

      this.drawWall(world, origX + j * 2, origY, origZ + 2 * i, walls, floor);
      int CHAMBER_WIDTH = 7;
      int CHAMBER_WIDTH_HALF = CHAMBER_WIDTH / 2;

      int x;
      int shift;
      for(x = 0; x < CHAMBER_WIDTH; ++x) {
         for(shift = 0; shift < CHAMBER_WIDTH; ++shift) {
            this.drawPassage(world, origX + this.width + x - CHAMBER_WIDTH_HALF, origY, origZ + shift + 1, walls, floor);
         }
      }

      for(x = 0; x < CHAMBER_WIDTH; ++x) {
         for(shift = 0; shift < CHAMBER_WIDTH + 2; ++shift) {
            this.drawPassage(world, origX + this.width + x - CHAMBER_WIDTH_HALF, origY, origZ + 2 * this.depth + shift - CHAMBER_WIDTH, walls, floor);
         }
      }

      this.drawPortal(world, origX + this.width, origY, origZ + 2 * this.depth, walls, floor);
      CHAMBER_WIDTH = 5;
      CHAMBER_WIDTH_HALF = CHAMBER_WIDTH / 2;
      int MAX_SHIFT = true;
      shift = world.field_73012_v.nextInt(11) - 5;

      int x;
      int y;
      for(x = 0; x < CHAMBER_WIDTH; ++x) {
         for(y = 0; y < CHAMBER_WIDTH; ++y) {
            this.drawPassage(world, origX + x + 1, origY, origZ + y + this.depth - CHAMBER_WIDTH_HALF + shift, walls, floor);
         }
      }

      drawChest(world, origX + CHAMBER_WIDTH_HALF + 1, origY, origZ + this.depth + shift, walls, floor);
      shift = world.field_73012_v.nextInt(11) - 5;

      for(x = 0; x < CHAMBER_WIDTH; ++x) {
         for(y = 0; y < CHAMBER_WIDTH; ++y) {
            this.drawPassage(world, origX + 2 * this.width + x - CHAMBER_WIDTH, origY, origZ + y + this.depth - CHAMBER_WIDTH_HALF + shift, walls, floor);
         }
      }

      drawChest(world, origX + 2 * this.width - CHAMBER_WIDTH_HALF - 1, origY, origZ + this.depth + shift, walls, floor);
      int ROOM_WIDTH = true;
      int ROOM_WIDTH_HALF = true;

      for(int x = 0; x < 7; ++x) {
         for(int y = 0; y < 7; ++y) {
            this.drawPassage(world, origX + this.width + x - 3, origY, origZ + this.depth + y - 3, walls, floor);
         }
      }

      drawChest(world, origX + this.width, origY, origZ + this.depth, walls, floor);
   }

   private void drawPortal(World world, int x, int y, int z, Block wallBlock, Block floorBlock) {
      world.func_147449_b(x, y + 1, z, Witchery.Blocks.TORMENT_PORTAL);
      world.func_147449_b(x, y + 2, z, Witchery.Blocks.TORMENT_PORTAL);
      world.func_147449_b(x, y + 3, z, floorBlock);
      world.func_147449_b(x - 1, y + 1, z, floorBlock);
      world.func_147449_b(x - 1, y + 2, z, floorBlock);
      world.func_147449_b(x - 1, y + 3, z, floorBlock);
      world.func_147449_b(x + 1, y + 1, z, floorBlock);
      world.func_147449_b(x + 1, y + 2, z, floorBlock);
      world.func_147449_b(x + 1, y + 3, z, floorBlock);
   }

   private static void drawChest(World world, int x, int y, int z, Block wallBlock, Block floorBlock) {
      world.func_147449_b(x, y, z, Witchery.Blocks.REFILLING_CHEST);
   }

   private void drawWall(World world, int x, int y, int z, Block wallBlock, Block floorBlock) {
      for(int h = 0; h < 6; ++h) {
         world.func_147449_b(x, y + h, z, wallBlock);
      }

   }

   private void drawPassage(World world, int x, int y, int z, Block wallBlock, Block floorBlock) {
      world.func_147449_b(x, y - 1, z, floorBlock);
      if (world.field_73012_v.nextInt(100) == 0) {
         world.func_147449_b(x, y, z, Blocks.field_150391_bh);
      } else {
         world.func_147449_b(x, y, z, floorBlock);
      }

      for(int h = 1; h < 5; ++h) {
         world.func_147468_f(x, y + h, z);
      }

      world.func_147449_b(x, y + 6 - 1, z, wallBlock);
   }

   private void generateMaze(int cx, int cy, Random rand) {
      GenerateMaze.DIR[] dirs = GenerateMaze.DIR.values();
      Collections.shuffle(Arrays.asList(dirs), rand);
      GenerateMaze.DIR[] arr$ = dirs;
      int len$ = dirs.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         GenerateMaze.DIR dir = arr$[i$];
         int nx = cx + dir.dx;
         int ny = cy + dir.dy;
         if (between(nx, this.width) && between(ny, this.depth) && this.maze[nx][ny] == 0) {
            int[] var10000 = this.maze[cx];
            var10000[cy] |= dir.bit;
            var10000 = this.maze[nx];
            var10000[ny] |= dir.opposite.bit;
            this.generateMaze(nx, ny, rand);
         }
      }

   }

   private static boolean between(int v, int upper) {
      return v >= 0 && v < upper;
   }

   private static enum DIR {
      N(1, 0, -1),
      S(2, 0, 1),
      E(4, 1, 0),
      W(8, -1, 0);

      private final int bit;
      private final int dx;
      private final int dy;
      private GenerateMaze.DIR opposite;

      private DIR(int bit, int dx, int dy) {
         this.bit = bit;
         this.dx = dx;
         this.dy = dy;
      }

      static {
         N.opposite = S;
         S.opposite = N;
         E.opposite = W;
         W.opposite = E;
      }
   }
}
