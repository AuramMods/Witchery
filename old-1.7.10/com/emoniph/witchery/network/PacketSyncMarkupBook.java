package com.emoniph.witchery.network;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.item.ItemMarkupBook;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public class PacketSyncMarkupBook implements IMessage {
   private int slot;
   private List<String> pages;

   public PacketSyncMarkupBook() {
   }

   public PacketSyncMarkupBook(int slot, List<String> pageStack) {
      this.slot = slot;
      this.pages = new ArrayList(pageStack);
   }

   public void toBytes(ByteBuf buffer) {
      buffer.writeInt(this.slot);
      buffer.writeInt(this.pages.size());
      Iterator i$ = this.pages.iterator();

      while(i$.hasNext()) {
         String s = (String)i$.next();
         ByteBufUtils.writeUTF8String(buffer, s);
      }

   }

   public void fromBytes(ByteBuf buffer) {
      this.slot = buffer.readInt();
      int size = buffer.readInt();
      this.pages = new ArrayList(size);

      for(int i = 0; i < size; ++i) {
         this.pages.add(ByteBufUtils.readUTF8String(buffer));
      }

   }

   public static class Handler implements IMessageHandler<PacketSyncMarkupBook, IMessage> {
      public IMessage onMessage(PacketSyncMarkupBook message, MessageContext ctx) {
         EntityPlayer player = Witchery.proxy.getPlayer(ctx);
         if (message.slot >= 0 && message.slot < player.field_71071_by.func_70302_i_()) {
            ItemStack stack = player.field_71071_by.func_70301_a(message.slot);
            if (stack != null && stack.func_77973_b() instanceof ItemMarkupBook) {
               if (!stack.func_77942_o()) {
                  stack.func_77982_d(new NBTTagCompound());
               }

               NBTTagList pageStack = new NBTTagList();
               Iterator i$ = message.pages.iterator();

               while(i$.hasNext()) {
                  String s = (String)i$.next();
                  pageStack.func_74742_a(new NBTTagString(s));
               }

               stack.func_77978_p().func_74782_a("pageStack", pageStack);
               ((ItemMarkupBook)stack.func_77973_b()).onBookRead(stack, player.field_70170_p, player);
            }
         }

         return null;
      }
   }
}
