package com.emoniph.witchery.client;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.network.PacketClearFallDamage;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class KeyboardHandler {
   private final List<KeyboardHandler.KeyInfo> bindings = new ArrayList();
   private final KeyboardHandler.KeyInfo JUMP;
   private final KeyboardHandler.KeyInfo HOTBAR1;

   public KeyboardHandler() {
      this.JUMP = new KeyboardHandler.KeyInfo(Minecraft.func_71410_x().field_71474_y.field_74314_A, this.bindings) {
         private boolean isJumping;
         private int remainingJumps;
         private boolean clearFall;

         protected void onKeyDown(EntityPlayer player, boolean repeated, boolean end) {
            if (!player.field_71075_bZ.field_75098_d && !end) {
               if (this.isJumping) {
                  if (this.remainingJumps > 0) {
                     int jumpsLeft = this.remainingJumps--;
                     player.field_70181_x = 0.42D;
                     if (player.func_70644_a(Potion.field_76430_j)) {
                        player.field_70181_x += 0.1D * (double)(1 + player.func_70660_b(Potion.field_76430_j).func_76458_c());
                     }
                  }
               } else {
                  this.isJumping = player.field_70160_al;
                  if (player.func_70644_a(Witchery.Potions.DOUBLE_JUMP)) {
                     this.remainingJumps += 1 + player.func_70660_b(Witchery.Potions.DOUBLE_JUMP).func_76458_c();
                  }
               }
            }

            if (this.clearFall) {
               this.clearFall = false;
               player.field_70143_R = 0.0F;
               Witchery.packetPipeline.sendToServer(new PacketClearFallDamage());
            }

         }

         protected void onTick(EntityPlayer player, boolean end) {
            if (player.field_70122_E) {
               this.isJumping = false;
               this.remainingJumps = 0;
            }

         }
      };
      this.HOTBAR1 = new KeyboardHandler.KeyInfo(Minecraft.func_71410_x().field_71474_y.field_151456_ac[0], this.bindings) {
         protected void onKeyDown(EntityPlayer player, boolean repeated, boolean end) {
            if (!end) {
               ExtendedPlayer playerEx = ExtendedPlayer.get(player);
               if (playerEx.isVampire() && !Minecraft.func_71410_x().field_71456_v.func_146158_b().func_146241_e()) {
                  int MAXPOWER = playerEx.getMaxAvailablePowerOrdinal();
                  if (player.field_71071_by.field_70461_c == 0) {
                     int power = playerEx.getSelectedVampirePower().ordinal();
                     if (power == MAXPOWER) {
                        playerEx.setSelectedVampirePower(ExtendedPlayer.VampirePower.NONE, true);
                     } else {
                        playerEx.setSelectedVampirePower(ExtendedPlayer.VampirePower.values()[power + 1], true);
                     }
                  }
               }
            }

         }

         protected void onKeyUp(EntityPlayer player, boolean end) {
         }

         protected void onTick(EntityPlayer player, boolean end) {
         }
      };

      for(int i = 1; i < Minecraft.func_71410_x().field_71474_y.field_151456_ac.length; ++i) {
         KeyBinding binding = Minecraft.func_71410_x().field_71474_y.field_151456_ac[i];
         KeyboardHandler.KeyInfo var10001 = new KeyboardHandler.KeyInfo(binding, this.bindings) {
            protected void onKeyDown(EntityPlayer player, boolean repeated, boolean end) {
               if (!end) {
                  ExtendedPlayer playerEx = ExtendedPlayer.get(player);
                  if (playerEx.isVampire() && playerEx.getSelectedVampirePower() != ExtendedPlayer.VampirePower.NONE) {
                     playerEx.setSelectedVampirePower(ExtendedPlayer.VampirePower.NONE, true);
                  }
               }

            }
         };
      }

   }

   @SubscribeEvent
   public void onTick(ClientTickEvent event) {
      if (event.side == Side.CLIENT) {
         Minecraft mc = Minecraft.func_71410_x();
         EntityPlayer player = mc.field_71439_g;
         if (player != null) {
            Iterator i$ = this.bindings.iterator();

            while(i$.hasNext()) {
               KeyboardHandler.KeyInfo keyInfo = (KeyboardHandler.KeyInfo)i$.next();
               keyInfo.doTick(player, event.phase == Phase.END);
            }
         }
      }

   }

   private abstract static class KeyInfo {
      private final KeyBinding bind;
      private boolean repeat;
      private boolean down;

      public KeyInfo(KeyBinding bind, List<KeyboardHandler.KeyInfo> bindings) {
         this.bind = bind;
         bindings.add(this);
      }

      public void doTick(EntityPlayer player, boolean end) {
         int keyCode = this.bind.func_151463_i();
         boolean newlyDown = keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);
         if (newlyDown != this.down || newlyDown && this.repeat) {
            if (newlyDown) {
               this.onKeyDown(player, newlyDown != this.down, end);
            } else {
               this.onKeyUp(player, end);
            }

            if (end) {
               this.down = newlyDown;
            }
         }

         if (end) {
            this.onTick(player, end);
         }

      }

      protected void onKeyDown(EntityPlayer player, boolean repeated, boolean end) {
      }

      protected void onKeyUp(EntityPlayer player, boolean end) {
      }

      protected void onTick(EntityPlayer player, boolean end) {
      }
   }
}
