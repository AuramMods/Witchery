package com.emoniph.witchery.client.gui;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.brewing.WitcheryBrewRegistry;
import com.emoniph.witchery.network.PacketSyncMarkupBook;
import com.emoniph.witchery.util.NBT;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiScreenMarkupBook extends GuiScreen {
   private static final ResourceLocation BACKGROUND = new ResourceLocation("witchery:textures/gui/bookSingle.png");
   private final EntityPlayer player;
   private final ItemStack itemstack;
   private final int meta;
   private int updateCount;
   private int bookImageWidth = 192;
   private int bookImageHeight = 192;
   private GuiButtonNavigate buttonTopPage;
   private GuiButtonNavigate buttonPreviousPage;
   private GuiButtonNavigate buttonNextPage;
   private final List<String> pageStack = new ArrayList();
   final List<GuiScreenMarkupBook.Element> elements = new ArrayList();
   private GuiScreenMarkupBook.NextPage nextPage;

   public GuiScreenMarkupBook(EntityPlayer player, ItemStack itemstack) {
      this.player = player;
      this.itemstack = itemstack;
      this.meta = itemstack != null ? itemstack.func_77960_j() : 0;
      NBTTagList nbtPageStack = NBT.get(itemstack).func_150295_c("pageStack", 8);

      for(int i = 0; i < nbtPageStack.func_74745_c(); ++i) {
         this.pageStack.add(nbtPageStack.func_150307_f(i));
      }

   }

   public void func_73876_c() {
      super.func_73876_c();
      ++this.updateCount;
   }

   public void func_73866_w_() {
      Keyboard.enableRepeatEvents(true);
      this.constructPage();
   }

   private void constructPage() {
      String page = this.pageStack.size() > 0 ? (String)this.pageStack.get(this.pageStack.size() - 1) : "toc";
      this.field_146292_n.clear();
      this.elements.clear();
      byte b0 = 2;
      int mid = (this.field_146294_l - this.bookImageWidth) / 2;
      this.field_146292_n.add(this.buttonTopPage = new GuiButtonNavigate(1, mid + 120, b0 + 16, 2, BACKGROUND));
      this.field_146292_n.add(this.buttonPreviousPage = new GuiButtonNavigate(2, mid + 34, b0 + 16, 1, BACKGROUND));
      this.field_146292_n.add(this.buttonNextPage = new GuiButtonNavigate(3, mid + 120, b0 + 16, 0, BACKGROUND));
      String itemName = Item.field_150901_e.func_148750_c(this.itemstack.func_77973_b());
      String untranslated = itemName + "." + page;
      StringBuilder markup = new StringBuilder(StatCollector.func_74838_a(untranslated));
      if (markup != null && !markup.toString().equals(untranslated)) {
         for(int i = 0; i < markup.length(); ++i) {
            char c = markup.charAt(i);
            switch(c) {
            case '[':
               this.elements.add(new GuiScreenMarkupBook.Element());
               ((GuiScreenMarkupBook.Element)this.elements.get(this.elements.size() - 1)).append(c);
               break;
            case ']':
               GuiScreenMarkupBook.Element e = (GuiScreenMarkupBook.Element)this.elements.get(this.elements.size() - 1);
               if (e.tag.toString().equals("template")) {
                  String templatePathRoot = Item.field_150901_e.func_148750_c(this.itemstack.func_77973_b());
                  String templatePath = templatePathRoot + "." + e.attribute;
                  String template = StatCollector.func_74838_a(templatePath);
                  if (!template.isEmpty()) {
                     String[] parms = e.text.toString().split("\\s");
                     Object[] components = new Object[parms.length];

                     for(int j = 0; j < parms.length; ++j) {
                        String[] kv = parms[j].split("=");
                        if (kv.length == 2) {
                           if (!kv[0].matches("stack\\|\\d+")) {
                              if (kv[0].matches("\\d+")) {
                                 int index = Math.min(Integer.parseInt(kv[0]), components.length - 1);
                                 components[index] = kv[1];
                              }
                           } else {
                              StringBuilder stackList = new StringBuilder();
                              String[] arr$ = kv[1].split(",");
                              int len$ = arr$.length;

                              for(int i$ = 0; i$ < len$; ++i$) {
                                 String stack = arr$[i$];
                                 stackList.append(String.format("[stack=%s]", stack));
                              }

                              int index = Math.min(Integer.parseInt(kv[0].substring(kv[0].indexOf(124) + 1)), components.length - 1);
                              components[index] = stackList.toString();
                           }
                        }
                     }

                     markup.insert(i + 1, String.format(template, components));
                     this.elements.remove(this.elements.size() - 1);
                  }
               }

               this.elements.add(new GuiScreenMarkupBook.Element());
               break;
            default:
               if (this.elements.size() == 0) {
                  this.elements.add(new GuiScreenMarkupBook.Element());
               }

               ((GuiScreenMarkupBook.Element)this.elements.get(this.elements.size() - 1)).append(c);
            }
         }

         this.nextPage = null;
         Iterator i$ = this.elements.iterator();

         while(i$.hasNext()) {
            GuiScreenMarkupBook.Element element = (GuiScreenMarkupBook.Element)i$.next();
            GuiScreenMarkupBook.NextPage defaultNextPage = element.constructButtons(this.field_146292_n, this.itemstack);
            if (defaultNextPage != null) {
               this.nextPage = defaultNextPage;
            }
         }

         this.updateButtons();
      }
   }

   public void func_146281_b() {
      Keyboard.enableRepeatEvents(false);
      this.sendBookToServer();
   }

   private void updateButtons() {
      this.buttonNextPage.field_146125_m = this.nextPage != null && this.nextPage.visible;
      this.buttonPreviousPage.field_146125_m = this.pageStack.size() > 0;
      this.buttonTopPage.field_146125_m = this.pageStack.size() > 0;
   }

   private void sendBookToServer() {
      if (this.player != null) {
         Witchery.packetPipeline.sendToServer(new PacketSyncMarkupBook(this.player.field_71071_by.field_70461_c, this.pageStack));
      }

   }

   protected void func_146284_a(GuiButton button) {
      if (button.field_146124_l) {
         if (button.field_146127_k == 0) {
            this.field_146297_k.func_147108_a((GuiScreen)null);
         } else if (button.field_146127_k != 1) {
            if (button.field_146127_k == 2) {
               if (this.pageStack.size() > 0) {
                  this.pageStack.remove(this.pageStack.size() - 1);
                  this.constructPage();
               }
            } else if (button.field_146127_k == 3) {
               this.pageStack.add(this.nextPage.pageName);
               this.constructPage();
            } else if (button.field_146127_k == 4) {
               this.pageStack.add(((GuiButtonUrl)button).nextPage);
               this.constructPage();
            }
         } else {
            if (this.pageStack.size() > 0) {
               this.pageStack.remove(this.pageStack.size() - 1);

               for(int i = this.pageStack.size() - 1; i >= 0 && !((String)this.pageStack.get(i)).startsWith("toc/"); --i) {
                  this.pageStack.remove(i);
               }
            }

            this.constructPage();
         }

         this.updateButtons();
      }

   }

   protected void func_73869_a(char par1, int par2) {
      super.func_73869_a(par1, par2);
   }

   public void func_73863_a(int mouseX, int mouseY, float par3) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.field_146297_k.func_110434_K().func_110577_a(BACKGROUND);
      int k = (this.field_146294_l - this.bookImageWidth) / 2;
      byte b0 = 2;
      this.func_73729_b(k, b0, 0, 0, this.bookImageWidth, this.bookImageHeight);
      int maxWidth = true;
      int marginX = k + 36;
      this.buttonPreviousPage.field_146128_h = marginX;
      this.buttonPreviousPage.field_146129_i = 16;
      this.buttonTopPage.field_146128_h = k + this.bookImageWidth / 2 - this.buttonTopPage.field_146120_f / 2 - 4;
      this.buttonTopPage.field_146129_i = 16;
      this.buttonNextPage.field_146128_h = k + this.bookImageWidth - this.buttonNextPage.field_146120_f - 44;
      this.buttonNextPage.field_146129_i = 16;
      int[] pos = new int[]{0, 32};
      GuiScreenMarkupBook.RenderState state = new GuiScreenMarkupBook.RenderState(this.field_146289_q, this.field_73735_i, mouseX, mouseY);
      Iterator i$ = this.elements.iterator();

      while(i$.hasNext()) {
         GuiScreenMarkupBook.Element element = (GuiScreenMarkupBook.Element)i$.next();
         element.draw(pos, marginX, 116, state);
      }

      super.func_73863_a(mouseX, mouseY, par3);
      if (state.tooltipStack != null) {
         this.func_146285_a(state.tooltipStack, mouseX, mouseY + 16);
      }

   }

   protected void func_146285_a(ItemStack stack, int x, int y) {
      List list = stack.func_82840_a(this.field_146297_k.field_71439_g, this.field_146297_k.field_71474_y.field_82882_x);
      int k;
      if (list != null) {
         k = WitcheryBrewRegistry.INSTANCE.getAltarPower(stack);
         if (k >= 0) {
            list.add(String.format(Witchery.resource("witchery.brewing.ingredientpowercost"), k, MathHelper.func_76143_f(1.4D * (double)k)));
         }
      }

      for(k = 0; k < list.size(); ++k) {
         if (k == 0) {
            list.set(k, stack.func_77953_t().field_77937_e + (String)list.get(k));
         } else {
            list.set(k, EnumChatFormatting.GRAY + (String)list.get(k));
         }
      }

      FontRenderer font = stack.func_77973_b().getFontRenderer(stack);
      this.drawHoveringText(list, x, y, font == null ? this.field_146289_q : font);
   }

   private static class RenderState {
      final FontRenderer font;
      final float zLevel;
      final int mouseX;
      final int mouseY;
      ItemStack tooltipStack;
      int lineheight;

      public RenderState(FontRenderer font, float zLevel, int mouseX, int mouseY) {
         this.font = font;
         this.zLevel = zLevel;
         this.mouseX = mouseX;
         this.mouseY = mouseY;
         this.lineheight = font.field_78288_b;
      }

      public void newline(int[] pos) {
         pos[0] = 0;
         pos[1] += this.lineheight + 1;
         this.lineheight = this.font.field_78288_b;
      }

      public void adjustLineHeight(int newHeight) {
         if (newHeight > this.lineheight) {
            this.lineheight = newHeight;
         }

      }
   }

   private static class Element {
      private final StringBuilder tag;
      private final StringBuilder attribute;
      private final StringBuilder text;
      private GuiScreenMarkupBook.Element.Capture capture;
      private static final String FORMAT_CHAR = "§";
      private static final String FORMAT_CLEAR = "§r";
      private static final Hashtable<String, String> FORMATS = getFormats();
      private GuiButtonUrl button;

      private Element() {
         this.tag = new StringBuilder();
         this.attribute = new StringBuilder();
         this.text = new StringBuilder();
         this.capture = GuiScreenMarkupBook.Element.Capture.TEXT;
      }

      public String toString() {
         return String.format("tag=%s attribute=%s text=%s", this.tag, this.attribute, this.text);
      }

      public void append(char c) {
         switch(c) {
         case '=':
            if (this.capture == GuiScreenMarkupBook.Element.Capture.TAG) {
               this.capture = GuiScreenMarkupBook.Element.Capture.ATTRIB;
               return;
            }
         case '\t':
         case ' ':
            if (this.capture != GuiScreenMarkupBook.Element.Capture.TAG && this.capture != GuiScreenMarkupBook.Element.Capture.ATTRIB) {
               break;
            }

            this.capture = GuiScreenMarkupBook.Element.Capture.TEXT;
            return;
         case '[':
            this.capture = GuiScreenMarkupBook.Element.Capture.TAG;
            return;
         }

         if (this.capture == GuiScreenMarkupBook.Element.Capture.TAG) {
            this.tag.append(c);
         } else if (this.capture == GuiScreenMarkupBook.Element.Capture.ATTRIB) {
            this.attribute.append(c);
         } else {
            this.text.append(c);
         }

      }

      private static Hashtable<String, String> getFormats() {
         Hashtable<String, String> formats = new Hashtable();
         formats.put("black", "§0");
         formats.put("darkblue", "§1");
         formats.put("darkgreen", "§2");
         formats.put("darkaqua", "§3");
         formats.put("darkred", "§4");
         formats.put("darkpurple", "§5");
         formats.put("darkyellow", "§6");
         formats.put("gray", "§7");
         formats.put("darkgray", "§8");
         formats.put("blue", "§9");
         formats.put("green", "§a");
         formats.put("aqua", "§b");
         formats.put("red", "§c");
         formats.put("purple", "§d");
         formats.put("yellow", "§e");
         formats.put("white", "§f");
         formats.put("b", "§l");
         formats.put("s", "§m");
         formats.put("u", "§n");
         formats.put("i", "§o");
         formats.put("h1", "§3§o");
         return formats;
      }

      public GuiScreenMarkupBook.NextPage constructButtons(List buttonList, ItemStack stack) {
         String tag = this.tag.toString();
         if (tag.equals("url")) {
            String attrib = this.attribute.toString();
            int pipeIndex = attrib.indexOf(124);
            if (pipeIndex != -1) {
               attrib = attrib.substring(0, pipeIndex);
            }

            this.button = new GuiButtonUrl(4, 0, 0, attrib, this.text.toString());
            buttonList.add(this.button);
         } else if (tag.equals("next")) {
            return new GuiScreenMarkupBook.NextPage(this.attribute.toString(), stack);
         }

         return null;
      }

      public void draw(int[] pos, int marginX, int maxWidth, GuiScreenMarkupBook.RenderState state) {
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         String tag = this.tag.toString();
         if (tag.equals("br")) {
            state.newline(pos);
         } else if (tag.equals("tab")) {
            int TAB_SPACE = true;
            if (pos[0] + 10 > maxWidth) {
               state.newline(pos);
            } else {
               pos[0] += 10;
            }

         } else {
            int i$;
            String[] parms;
            String valign;
            if (tag.equals("img")) {
               parms = this.attribute.toString().split("\\|");
               int defaultWidth = true;
               valign = parms.length > 0 ? parms[0] : "";
               String halign = parms.length > 1 ? parms[1] : "left";
               String valign = parms.length > 2 ? parms[2] : "top";
               i$ = parms.length > 3 ? this.parseInt(parms[3], 32) : 32;
               int height = parms.length > 4 ? this.parseInt(parms[4], i$) : i$;
               if (!valign.isEmpty()) {
                  ResourceLocation location = new ResourceLocation(valign);
                  Minecraft.func_71410_x().func_110434_K().func_110577_a(location);
                  if (halign.equals("right")) {
                     pos[0] = maxWidth - i$;
                  } else if (halign.equals("center")) {
                     pos[0] = maxWidth / 2 - i$ / 2;
                  }

                  if (pos[0] + i$ > maxWidth) {
                     state.newline(pos);
                  }

                  int y = pos[1];
                  if (state.lineheight > height) {
                     if (valign.equals("bottom")) {
                        y += state.lineheight - height;
                     } else if (valign.equals("middle")) {
                        y += state.lineheight / 2 - height / 2;
                     }
                  }

                  drawTexturedQuadFit((double)(pos[0] + marginX), (double)y, (double)i$, (double)height, (double)state.zLevel);
                  pos[0] += i$;
                  state.adjustLineHeight(height);
               }

            } else {
               int size;
               if (tag.equals("url")) {
                  this.button.field_146121_g = state.font.field_78288_b;
                  this.button.field_146120_f = state.font.func_78256_a(this.text.toString());
                  if (pos[0] + this.button.field_146120_f > maxWidth) {
                     state.newline(pos);
                  }

                  parms = this.attribute.toString().split("\\|");
                  String var10000;
                  if (parms.length > 0) {
                     var10000 = parms[0];
                  } else {
                     var10000 = "";
                  }

                  valign = parms.length > 1 ? parms[1] : "top";
                  this.button.field_146128_h = pos[0] + marginX;
                  size = pos[1];
                  if (state.lineheight > this.button.field_146121_g) {
                     if (valign.equals("bottom")) {
                        size += state.lineheight - this.button.field_146121_g;
                     } else if (valign.equals("middle")) {
                        size += state.lineheight / 2 - this.button.field_146121_g / 2;
                     }
                  }

                  this.button.field_146129_i = size;
                  pos[0] += this.button.field_146120_f;
               } else if (!tag.equals("locked")) {
                  String postText;
                  int len$;
                  String valign;
                  if (tag.equals("stack")) {
                     parms = this.attribute.toString().split("\\|");
                     postText = parms.length > 0 ? parms[0] : "";
                     int damage = 0;
                     size = 1;
                     len$ = 1;
                     if (parms.length > len$ && parms[len$].matches("\\d+")) {
                        damage = this.parseInt(parms[len$], 0);
                        ++len$;
                     }

                     if (parms.length > len$ && parms[len$].matches("\\d+")) {
                        size = this.parseInt(parms[len$], 1);
                        ++len$;
                     }

                     String halign = parms.length > len$ ? parms[len$] : "left";
                     ++len$;
                     valign = parms.length > len$ ? parms[len$] : "top";
                     if (!postText.isEmpty()) {
                        boolean empty = postText.equals("empty");
                        Item item = !empty ? (Item)Item.field_150901_e.func_82594_a(postText) : null;
                        ItemStack stack = !empty ? new ItemStack(item, size, damage) : null;
                        int width = 18;
                        int height = 18;
                        if (halign.equals("right")) {
                           pos[0] = maxWidth - width;
                        } else if (halign.equals("center")) {
                           pos[0] = maxWidth / 2 - width / 2;
                        }

                        if (pos[0] + width > maxWidth) {
                           state.newline(pos);
                        }

                        int y = pos[1];
                        if (state.lineheight > height) {
                           if (valign.equals("bottom")) {
                              y += state.lineheight - height;
                           } else if (valign.equals("middle")) {
                              y += state.lineheight / 2 - height / 2;
                           }
                        }

                        if (!empty) {
                           RenderItem render = new RenderItem();
                           GL11.glPushMatrix();
                           GL11.glEnable(3042);
                           GL11.glBlendFunc(770, 771);
                           RenderHelper.func_74520_c();
                           GL11.glEnable(32826);
                           GL11.glEnable(2929);
                           int x = pos[0] + marginX;
                           render.func_82406_b(state.font, Minecraft.func_71410_x().func_110434_K(), stack, x, y);
                           render.func_77021_b(state.font, Minecraft.func_71410_x().func_110434_K(), stack, x, y);
                           RenderHelper.func_74518_a();
                           GL11.glPopMatrix();
                           if (state.mouseX >= x && state.mouseY >= y && state.mouseX <= x + width && state.mouseY <= y + height) {
                              state.tooltipStack = stack;
                           }

                           GL11.glDisable(2896);
                        }

                        pos[0] += width;
                        state.adjustLineHeight(height);
                        String[] words = this.text.toString().split("(?<=\\s)");
                        String[] arr$ = words;
                        int len$ = words.length;

                        for(int i$ = 0; i$ < len$; ++i$) {
                           String word = arr$[i$];
                           int textWidth = state.font.func_78256_a(word);
                           if (pos[0] + textWidth > maxWidth) {
                              state.newline(pos);
                              y = pos[1];
                           }

                           state.font.func_78276_b(word, marginX + pos[0], y + (height - state.font.field_78288_b) / 2, 0);
                           pos[0] += textWidth;
                        }
                     }

                  } else if (!tag.equals("next")) {
                     String preText = FORMATS.containsKey(tag) ? (String)FORMATS.get(tag) : "";
                     postText = FORMATS.containsKey(tag) ? "§r" : "";
                     String[] words = this.text.toString().split("(?<=\\s)");
                     String[] arr$ = words;
                     len$ = words.length;

                     for(i$ = 0; i$ < len$; ++i$) {
                        valign = arr$[i$];
                        int width = state.font.func_78256_a(valign);
                        if (pos[0] + width > maxWidth) {
                           state.newline(pos);
                        }

                        if (pos[0] != 0 || !valign.trim().isEmpty()) {
                           state.font.func_78276_b(preText + valign + postText, marginX + pos[0], pos[1], 0);
                           pos[0] += width;
                        }
                     }

                     if (tag.equals("h1")) {
                        state.adjustLineHeight((int)Math.ceil((double)((float)state.lineheight * 1.5F)));
                        state.newline(pos);
                     }

                  }
               }
            }
         }
      }

      private int parseInt(String text, int defaultValue) {
         try {
            return Integer.parseInt(text);
         } catch (NumberFormatException var4) {
            return defaultValue;
         }
      }

      public static void drawTexturedQuadFit(double x, double y, double width, double height, double zLevel) {
         Tessellator tessellator = Tessellator.field_78398_a;
         tessellator.func_78382_b();
         tessellator.func_78374_a(x + 0.0D, y + height, zLevel, 0.0D, 1.0D);
         tessellator.func_78374_a(x + width, y + height, zLevel, 1.0D, 1.0D);
         tessellator.func_78374_a(x + width, y + 0.0D, zLevel, 1.0D, 0.0D);
         tessellator.func_78374_a(x + 0.0D, y + 0.0D, zLevel, 0.0D, 0.0D);
         tessellator.func_78381_a();
      }

      // $FF: synthetic method
      Element(Object x0) {
         this();
      }

      private static enum Capture {
         TAG,
         ATTRIB,
         TEXT;
      }
   }

   private static class NextPage {
      public final String pageName;
      public final boolean visible;

      public NextPage(String attrib, ItemStack book) {
         int pipeIndex = attrib.indexOf(124);
         if (pipeIndex != -1) {
            this.pageName = attrib.substring(0, pipeIndex);
            this.visible = book.func_77960_j() >= Integer.parseInt(attrib.substring(pipeIndex + 1));
         } else {
            this.pageName = attrib;
            this.visible = true;
         }

      }
   }
}
