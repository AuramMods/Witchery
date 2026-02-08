package com.emoniph.witchery.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BlockSpinningWheelGUI extends GuiContainer {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("witchery", "textures/gui/spinningwheel.png");
   private BlockSpinningWheel.TileEntitySpinningWheel spinningWheel;

   public BlockSpinningWheelGUI(InventoryPlayer playerInventory, BlockSpinningWheel.TileEntitySpinningWheel spinningWheel) {
      super(new BlockSpinningWheel.ContainerSpinningWheel(playerInventory, spinningWheel));
      this.spinningWheel = spinningWheel;
   }

   protected void func_146979_b(int par1, int par2) {
      String s = this.spinningWheel.func_145818_k_() ? this.spinningWheel.func_145825_b() : I18n.func_135052_a(this.spinningWheel.func_145825_b(), new Object[0]);
      this.field_146289_q.func_78276_b(s, this.field_146999_f / 2 - this.field_146289_q.func_78256_a(s) / 2, 6, 4210752);
      this.field_146289_q.func_78276_b(I18n.func_135052_a("container.inventory", new Object[0]), 8, this.field_147000_g - 96 + 2, 4210752);
   }

   protected void func_146976_a(float par1, int par2, int par3) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.field_146297_k.func_110434_K().func_110577_a(TEXTURE_LOCATION);
      int k = (this.field_146294_l - this.field_146999_f) / 2;
      int l = (this.field_146295_m - this.field_147000_g) / 2;
      this.func_73729_b(k, l, 0, 0, this.field_146999_f, this.field_147000_g);
      if (this.spinningWheel.powerLevel <= 0) {
         this.func_73729_b(k + 35, l + 58, 197, 0, 9, 9);
      }

      int i1 = this.spinningWheel.getCookProgressScaled(24);
      this.func_73729_b(k + 79, l + 20, 176, 14, i1 + 1, 16);
   }
}
