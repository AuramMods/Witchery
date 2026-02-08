package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.brewing.EntityBrew;
import com.emoniph.witchery.brewing.WitcheryBrewRegistry;
import com.emoniph.witchery.infusion.infusions.symbols.StrokeSet;
import com.emoniph.witchery.network.PacketBrewPrepared;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Dye;
import com.emoniph.witchery.util.SoundEffect;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemBrewBag extends ItemBase {
   private static final float THRESHOLD_ORTHOGONAL = 7.0F;
   private static final float THRESHOLD_DIAGONAL = 3.5F;

   public ItemBrewBag() {
      this.func_77625_d(1);
      this.func_77664_n();
   }

   public int func_82790_a(ItemStack stack, int parse) {
      return getColor(stack);
   }

   public static void setColor(ItemStack stack, Dye color) {
      setColor(stack, color.rgb);
   }

   public static void setColor(ItemStack stack, int rgb) {
      if (!stack.func_77942_o()) {
         stack.func_77982_d(new NBTTagCompound());
      }

      NBTTagCompound nbtRoot = stack.func_77978_p();
      nbtRoot.func_74768_a("WITCColor", rgb);
   }

   public static int getColor(ItemStack stack) {
      if (stack.func_77942_o()) {
         NBTTagCompound nbtRoot = stack.func_77978_p();
         if (nbtRoot.func_74764_b("WITCColor")) {
            return nbtRoot.func_74762_e("WITCColor");
         }
      }

      return Dye.COCOA_BEANS.rgb;
   }

   public int func_77626_a(ItemStack stack) {
      return 36000;
   }

   public ItemStack func_77659_a(ItemStack stack, World world, EntityPlayer player) {
      if (!player.func_70093_af()) {
         NBTTagCompound nbtTag = player.getEntityData();
         nbtTag.func_74773_a("Strokes", new byte[0]);
         nbtTag.func_74776_a("startPitch", player.field_70125_A);
         nbtTag.func_74776_a("startYaw", player.field_70759_as);
         nbtTag.func_82580_o("WITCLastBrewIndex");
         player.func_71008_a(stack, this.func_77626_a(stack));
      } else if (!world.field_72995_K && player.func_70093_af()) {
         player.openGui(Witchery.instance, 5, world, 0, 0, 0);
      }

      return stack;
   }

   public void onUsingTick(ItemStack stack, EntityPlayer player, int countdown) {
      NBTTagCompound nbtTag;
      if (player.field_70170_p.field_72995_K) {
         nbtTag = player.getEntityData();
         if (nbtTag == null) {
            return;
         }

         if (player.func_70093_af()) {
            nbtTag.func_82580_o("Strokes");
            nbtTag.func_74776_a("startPitch", player.field_70125_A);
            nbtTag.func_74776_a("startYaw", player.field_70759_as);
            return;
         }

         float yawDiff = nbtTag.func_74760_g("startYaw") - player.field_70759_as;
         float pitchDiff = nbtTag.func_74760_g("startPitch") - player.field_70125_A;
         byte[] oldStrokes = nbtTag.func_74770_j("Strokes");
         byte[] strokes = oldStrokes;
         int strokesStart = oldStrokes.length;
         if (strokesStart == 0) {
            if (pitchDiff >= 3.5F && yawDiff <= -3.5F) {
               strokes = this.addNewStroke(nbtTag, oldStrokes, (byte)4);
            } else if (pitchDiff >= 3.5F && yawDiff >= 3.5F) {
               strokes = this.addNewStroke(nbtTag, oldStrokes, (byte)6);
            } else if (pitchDiff <= -3.5F && yawDiff <= -3.5F) {
               strokes = this.addNewStroke(nbtTag, oldStrokes, (byte)7);
            } else if (pitchDiff <= -3.5F && yawDiff >= 3.5F) {
               strokes = this.addNewStroke(nbtTag, oldStrokes, (byte)5);
            } else if (pitchDiff >= 7.0F) {
               strokes = this.addNewStroke(nbtTag, oldStrokes, (byte)0);
            } else if (pitchDiff <= -7.0F) {
               strokes = this.addNewStroke(nbtTag, oldStrokes, (byte)1);
            } else if (yawDiff <= -7.0F) {
               strokes = this.addNewStroke(nbtTag, oldStrokes, (byte)2);
            } else if (yawDiff >= 7.0F) {
               strokes = this.addNewStroke(nbtTag, oldStrokes, (byte)3);
            }

            if (strokes.length > strokesStart) {
               nbtTag.func_74776_a("startPitch", player.field_70125_A);
               nbtTag.func_74776_a("startYaw", player.field_70759_as);
            }

            if (oldStrokes != strokes && strokes.length > 0) {
               int brewIndex = StrokeSet.Stroke.STROKE_TO_INDEX[strokes[0]];
               ItemBrewBag.InventoryBrewBag inv = new ItemBrewBag.InventoryBrewBag(player);
               ItemStack brew = inv.func_70301_a(brewIndex);
               if (brew != null) {
                  Witchery.packetPipeline.sendToServer(new PacketBrewPrepared(brewIndex));
               } else {
                  nbtTag.func_82580_o("Strokes");
               }
            }
         }
      } else if (player.func_70093_af()) {
         nbtTag = player.getEntityData();
         if (nbtTag.func_74764_b("WITCLastBrewIndex")) {
            nbtTag.func_82580_o("WITCLastBrewIndex");
         }
      }

   }

   public byte[] addNewStroke(NBTTagCompound nbtTag, byte[] strokes, byte stroke) {
      byte[] newStrokes = new byte[1];
      newStrokes[newStrokes.length - 1] = stroke;
      nbtTag.func_74773_a("Strokes", newStrokes);
      return newStrokes;
   }

   public void func_77615_a(ItemStack stack, World world, EntityPlayer player, int countdown) {
      NBTTagCompound nbtTag = player.getEntityData();
      if (nbtTag != null) {
         if (!world.field_72995_K && nbtTag.func_74764_b("WITCLastBrewIndex")) {
            int brewIndex = nbtTag.func_74762_e("WITCLastBrewIndex");
            nbtTag.func_82580_o("WITCLastBrewIndex");
            if (!player.func_70093_af()) {
               if (brewIndex > -1) {
                  ItemBrewBag.InventoryBrewBag inv = new ItemBrewBag.InventoryBrewBag(player);
                  ItemStack brew = inv.func_70301_a(brewIndex);
                  if (brew != null) {
                     if (brew.func_77973_b() == Witchery.Items.BREW) {
                        if (!player.field_71075_bZ.field_75098_d) {
                           --brew.field_77994_a;
                        }

                        world.func_72956_a(player, "random.bow", 0.5F, 0.4F / (field_77697_d.nextFloat() * 0.4F + 0.8F));
                        if (!world.field_72995_K) {
                           world.func_72838_d(new EntityBrew(world, player, brew, false));
                        }
                     } else {
                        Witchery.Items.GENERIC.throwBrew(brew, world, player);
                     }

                     if (brew.field_77994_a == 0) {
                        inv.func_70299_a(brewIndex, (ItemStack)null);
                     }

                     inv.writeToNBT();
                  }
               } else {
                  ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.infuse.branch.unknownsymbol");
                  SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
               }
            }
         } else {
            nbtTag.func_82580_o("Strokes");
            nbtTag.func_82580_o("startYaw");
            nbtTag.func_82580_o("startPitch");
         }
      }

   }

   private static class SlotBrewBag extends Slot {
      public SlotBrewBag(IInventory inventory, int slot, int x, int y) {
         super(inventory, slot, x, y);
      }

      public boolean func_75214_a(ItemStack stack) {
         return isBrew(stack);
      }

      public static boolean isBrew(ItemStack stack) {
         return stack != null && (Witchery.Items.GENERIC.isBrew(stack) || Witchery.Items.BREW == stack.func_77973_b() && WitcheryBrewRegistry.INSTANCE.isSplash(stack.func_77978_p()));
      }
   }

   public static class ContainerBrewBag extends Container {
      private int numRows;
      private ItemStack bag;

      public ContainerBrewBag(IInventory playerInventory, IInventory bagInventory, ItemStack bag) {
         this.numRows = bagInventory.func_70302_i_() / 8;
         bagInventory.func_70295_k_();
         int offset = (this.numRows - 4) * 18;

         int col;
         int col;
         for(col = 0; col < this.numRows; ++col) {
            for(col = 0; col < 8; ++col) {
               this.func_75146_a(new ItemBrewBag.SlotBrewBag(bagInventory, col + col * 8, 16 + col * 18, 18 + col * 18));
            }
         }

         for(col = 0; col < 3; ++col) {
            for(col = 0; col < 9; ++col) {
               this.func_75146_a(new Slot(playerInventory, col + col * 9 + 9, 8 + col * 18, 103 + col * 18 + offset));
            }
         }

         for(col = 0; col < 9; ++col) {
            this.func_75146_a(new Slot(playerInventory, col, 8 + col * 18, 161 + offset));
         }

         this.bag = bag;
      }

      public boolean func_75145_c(EntityPlayer player) {
         ItemStack itemStack = null;
         if (player.func_71045_bC() != null) {
            itemStack = player.func_71045_bC();
         }

         return itemStack != null && itemStack.func_77973_b() == Witchery.Items.BREW_BAG;
      }

      public ItemStack func_82846_b(EntityPlayer player, int index) {
         ItemStack returnStack = null;
         Slot slot = (Slot)this.field_75151_b.get(index);
         if (slot != null && slot.func_75216_d()) {
            ItemStack itemStack = slot.func_75211_c();
            if (!ItemBrewBag.SlotBrewBag.isBrew(itemStack)) {
               return returnStack;
            }

            returnStack = itemStack.func_77946_l();
            if (index < this.numRows * 9) {
               if (!this.func_75135_a(itemStack, this.numRows * 9, this.field_75151_b.size(), true)) {
                  return null;
               }
            } else if (!this.func_75135_a(itemStack, 0, this.numRows * 9, false)) {
               return null;
            }

            if (itemStack.field_77994_a == 0) {
               slot.func_75215_d((ItemStack)null);
            } else {
               slot.func_75218_e();
            }
         }

         return returnStack;
      }
   }

   public static class InventoryBrewBag extends InventoryBasic {
      protected String title;
      protected EntityPlayer player;
      protected boolean locked = false;

      public InventoryBrewBag(EntityPlayer player) {
         super("", false, 8);
         this.player = player;
         if (!this.hasInventory()) {
            this.createInventory();
         }

         this.readFromNBT();
      }

      public void func_70296_d() {
         super.func_70296_d();
         if (!this.locked) {
            this.writeToNBT();
         }

      }

      public void func_70295_k_() {
         this.readFromNBT();
      }

      public void func_70305_f() {
         this.writeToNBT();
      }

      public String func_145825_b() {
         return this.title;
      }

      protected boolean hasInventory() {
         ItemStack bag = this.player.func_70694_bm();
         return bag != null && bag.func_77942_o() && bag.func_77978_p().func_74764_b("Inventory");
      }

      protected void createInventory() {
         ItemStack bag = this.player.func_70694_bm();
         this.title = new String(bag.func_82833_r());
         this.writeToNBT();
      }

      protected void writeToNBT() {
         ItemStack bag = this.player.func_70694_bm();
         if (bag != null && bag.func_77973_b() == Witchery.Items.BREW_BAG) {
            if (!bag.func_77942_o()) {
               bag.func_77982_d(new NBTTagCompound());
            }

            NBTTagCompound nbtRoot = bag.func_77978_p();
            NBTTagCompound name = new NBTTagCompound();
            name.func_74778_a("Name", this.func_145825_b());
            nbtRoot.func_74782_a("display", name);
            NBTTagList itemList = new NBTTagList();

            for(int i = 0; i < this.func_70302_i_(); ++i) {
               if (this.func_70301_a(i) != null) {
                  NBTTagCompound slotEntry = new NBTTagCompound();
                  slotEntry.func_74774_a("Slot", (byte)i);
                  this.func_70301_a(i).func_77955_b(slotEntry);
                  itemList.func_74742_a(slotEntry);
               }
            }

            NBTTagCompound inventory = new NBTTagCompound();
            inventory.func_74782_a("Items", itemList);
            nbtRoot.func_74782_a("Inventory", inventory);
         }
      }

      protected void readFromNBT() {
         this.locked = true;
         ItemStack bag = this.player.func_70694_bm();
         if (bag != null && bag.func_77973_b() == Witchery.Items.BREW_BAG && bag.func_77942_o()) {
            NBTTagCompound nbtRoot = bag.func_77978_p();
            this.title = nbtRoot.func_74775_l("display").func_74779_i("Name");
            NBTTagList itemList = nbtRoot.func_74775_l("Inventory").func_150295_c("Items", 10);

            for(int i = 0; i < itemList.func_74745_c(); ++i) {
               NBTTagCompound slotEntry = itemList.func_150305_b(i);
               int j = slotEntry.func_74771_c("Slot") & 255;
               if (j >= 0 && j < this.func_70302_i_()) {
                  this.func_70299_a(j, ItemStack.func_77949_a(slotEntry));
               }
            }
         }

         this.locked = false;
      }
   }
}
