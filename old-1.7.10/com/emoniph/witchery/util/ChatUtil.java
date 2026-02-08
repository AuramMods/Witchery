package com.emoniph.witchery.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class ChatUtil {
   public static void sendTranslated(ICommandSender player, String key, Object... params) {
      player.func_145747_a(new ChatComponentTranslation(key, params));
   }

   public static void sendTranslated(EnumChatFormatting color, ICommandSender player, String key, Object... params) {
      player.func_145747_a((new ChatComponentTranslation(key, params)).func_150255_a((new ChatStyle()).func_150238_a(color)));
   }

   public static void sendPlain(ICommandSender player, String text) {
      player.func_145747_a(new ChatComponentText(text));
   }

   public static void sendPlain(EnumChatFormatting color, ICommandSender player, String text) {
      player.func_145747_a((new ChatComponentText(text)).func_150255_a((new ChatStyle()).func_150238_a(color)));
   }
}
