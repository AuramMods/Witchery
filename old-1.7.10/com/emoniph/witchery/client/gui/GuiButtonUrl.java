package com.emoniph.witchery.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
class GuiButtonUrl extends GuiButton {
   public final String nextPage;

   public GuiButtonUrl(int commandID, int x, int y, String page, String label) {
      super(commandID, x, y, 10, 10, label);
      this.nextPage = !page.isEmpty() ? page.toLowerCase(Locale.ROOT).replace(" ", "") : label.toLowerCase(Locale.ROOT).replace(" ", "");
   }

   public void func_146112_a(Minecraft mc, int mouseX, int mouseY) {
      if (this.field_146125_m) {
         String FORMAT_CHAR = "§";
         String FORMAT_CLEAR = "§r";
         boolean flag = mouseX >= this.field_146128_h && mouseY >= this.field_146129_i && mouseX < this.field_146128_h + this.field_146120_f && mouseY < this.field_146129_i + this.field_146121_g;
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         mc.field_71466_p.func_78276_b("§n" + this.field_146126_j + "§r", this.field_146128_h, this.field_146129_i, flag ? 16711680 : 255);
      }

   }
}
