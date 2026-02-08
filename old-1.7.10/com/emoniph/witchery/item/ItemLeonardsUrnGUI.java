package com.emoniph.witchery.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ItemLeonardsUrnGUI extends GuiContainer {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("witchery", "textures/gui/urn.png");
   private IInventory upperInventory;
   private IInventory lowerInventory;

   public ItemLeonardsUrnGUI(IInventory inventoryPlayer, IInventory inventoryBag) {
      super(new ItemLeonardsUrn.ContainerLeonardsUrn(inventoryPlayer, inventoryBag, (ItemStack)null));
      this.upperInventory = inventoryBag;
      this.lowerInventory = inventoryPlayer;
      this.field_147000_g = 184;
   }

   protected void func_146979_b(int par1, int par2) {
      this.field_146289_q.func_78276_b(StatCollector.func_74838_a(this.upperInventory.func_145825_b()), 8, 6, 4210752);
      this.field_146289_q.func_78276_b(StatCollector.func_74838_a(this.lowerInventory.func_145825_b()), 8, this.field_147000_g - 96 + 2, 4210752);
   }

   protected void func_146976_a(float var1, int var2, int var3) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.field_146297_k.field_71446_o.func_110577_a(TEXTURE_LOCATION);
      int left = (this.field_146294_l - this.field_146999_f) / 2;
      int top = (this.field_146295_m - this.field_147000_g) / 2;
      this.func_73729_b(left, top, 0, 0, this.field_146999_f, this.field_147000_g);

      for(int i = 0; i < this.upperInventory.func_70302_i_(); ++i) {
         Slot slot = this.field_147002_h.func_75147_a(this.upperInventory, i);
         this.func_73729_b(left + slot.field_75223_e - 1, top + slot.field_75221_f - 1, this.field_146999_f, 0, 18, 18);
      }

   }
}
