package com.emoniph.witchery.util;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class KeyBindHelper {
   public static boolean isKeyBindDown(KeyBinding keyBinding) {
      return keyBinding.func_151463_i() >= 0 ? Keyboard.isKeyDown(keyBinding.func_151463_i()) : Mouse.isButtonDown(keyBinding.func_151463_i() + 100);
   }
}
