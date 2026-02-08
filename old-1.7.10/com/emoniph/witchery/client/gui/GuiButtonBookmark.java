package com.emoniph.witchery.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
class GuiButtonBookmark extends GuiButton {
   public final int nextPage;

   public GuiButtonBookmark(int commandID, int x, int y, int pageIndex, String label) {
      super(commandID, x, y, 60, 11, label);
      this.nextPage = pageIndex;
   }

   public void func_146112_a(Minecraft mc, int par2, int par3) {
      if (this.field_146125_m) {
         boolean flag = par2 >= this.field_146128_h && par3 >= this.field_146129_i && par2 < this.field_146128_h + this.field_146120_f && par3 < this.field_146129_i + this.field_146121_g;
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         mc.func_110434_K().func_110577_a(GuiScreenWitchcraftBook.DOUBLE_BOOK_TEXTURE);
         this.func_73729_b(this.field_146128_h, this.field_146129_i, 26, 220 + (flag ? this.field_146121_g : 0), this.field_146120_f, this.field_146121_g);
         mc.field_71466_p.func_78276_b(this.field_146126_j, this.field_146128_h + 2, this.field_146129_i + 2, 0);
      }

   }
}
