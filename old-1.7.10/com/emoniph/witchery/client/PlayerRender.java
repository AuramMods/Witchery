package com.emoniph.witchery.client;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.brewing.potions.PotionResizing;
import com.emoniph.witchery.client.renderer.RenderInfusionEnergyBar;
import com.emoniph.witchery.infusion.InfusedBrewEffect;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePower;
import com.emoniph.witchery.infusion.infusions.creature.CreaturePowerSpeed;
import com.emoniph.witchery.infusion.infusions.symbols.EffectRegistry;
import com.emoniph.witchery.infusion.infusions.symbols.StrokeSet;
import com.emoniph.witchery.infusion.infusions.symbols.SymbolEffect;
import com.emoniph.witchery.integration.ModHookMorph;
import com.emoniph.witchery.item.ItemBrewBag;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.EntitySizeInfo;
import com.emoniph.witchery.util.KeyBindHelper;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.RenderUtil;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class PlayerRender {
   protected static RenderInfusionEnergyBar infusionEnergyBar;
   protected static RenderInfusionEnergyBar creatureEnergyBar;
   private boolean remoteViewingActive = true;
   public static long ticksSinceActive = 0L;
   public static boolean moveCameraActive = false;
   public static int moveCameraToEntityID = 0;
   private static final ResourceLocation RADIAL_LOCATION = new ResourceLocation("witchery", "textures/gui/radial.png");
   EntityRenderer prevRender;
   RenderEntityViewer renderer;
   int currentBeastForm = 0;
   private static final ResourceLocation BARK_TEXTURES = new ResourceLocation("witchery", "textures/gui/creatures.png");
   private static RenderItem drawItems = new RenderItem();
   private int lastY = 0;
   private static final int[] glyphOffsetX = new int[]{0, 0, 1, -1, 1, -1, -1, 1};
   private static final int[] glyphOffsetY = new int[]{-1, 1, 0, 0, -1, 1, -1, 1};
   private static final ResourceLocation TEXTURE_GRID = new ResourceLocation("witchery", "textures/gui/grid.png");
   private Field fieldAccess = null;

   @SubscribeEvent
   public void onRenderTick(RenderTickEvent event) {
      EntityClientPlayerMP player;
      Minecraft mc;
      if (event.phase == Phase.START) {
         player = Minecraft.func_71410_x().field_71439_g;
         mc = Minecraft.func_71410_x();
         if (player != null && mc.field_71462_r == null) {
            if (Minecraft.func_71386_F() - ticksSinceActive > 3000L) {
               moveCameraActive = false;
            }

            this.remoteViewingActive = moveCameraActive;
            if (this.remoteViewingActive) {
               Iterator i$ = player.field_70170_p.field_72996_f.iterator();

               while(i$.hasNext()) {
                  Object entity = i$.next();
                  if (((Entity)entity).func_145782_y() == moveCameraToEntityID && entity instanceof EntityLivingBase) {
                     EntityLivingBase living = (EntityLivingBase)entity;
                     if (!living.field_70128_L) {
                        Minecraft.func_71410_x().field_71451_h = living;
                     }
                     break;
                  }
               }
            } else {
               EntitySizeInfo size = new EntitySizeInfo(player);
               PotionEffect shrunk = Witchery.Potions.RESIZING != null ? player.func_70660_b(Witchery.Potions.RESIZING) : null;
               if ((shrunk != null || !size.isDefault) && !ModHookMorph.isMorphed(player, true)) {
                  if (this.renderer == null) {
                     this.renderer = new RenderEntityViewer(mc);
                  }

                  if (mc.field_71474_y.field_74320_O == 0) {
                     if (mc.field_71460_t != this.renderer) {
                        this.prevRender = mc.field_71460_t;
                        mc.field_71460_t = this.renderer;
                     }
                  } else if (this.prevRender != null) {
                     mc.field_71460_t = this.prevRender;
                  }

                  float normalSize = 1.8F;
                  float scale = size.defaultHeight / 1.8F * (shrunk != null ? PotionResizing.getScaleFactor(shrunk.func_76458_c()) : 1.0F);
                  float requiredSize;
                  float currentSize;
                  if (scale < 1.0F) {
                     requiredSize = 1.8F * (1.0F - scale);
                     currentSize = this.renderer.getOffset();
                     if (currentSize < requiredSize) {
                        currentSize = Math.min(currentSize + 0.01F, requiredSize);
                     } else if (currentSize > requiredSize) {
                        currentSize = Math.min(currentSize - 0.01F, requiredSize);
                     }

                     this.renderer.setOffset(currentSize);
                  } else {
                     requiredSize = -(1.8F * scale - 1.8F);
                     currentSize = this.renderer.getOffset();
                     if (currentSize > requiredSize) {
                        currentSize = Math.max(currentSize - 0.01F, requiredSize);
                     }

                     this.renderer.setOffset(currentSize);
                  }
               } else if (this.prevRender != null && mc.field_71460_t != this.prevRender) {
                  if (this.renderer != null) {
                     this.renderer.setOffset(0.0F);
                  }

                  mc.field_71460_t = this.prevRender;
               }
            }
         }
      } else if (event.phase == Phase.END) {
         player = Minecraft.func_71410_x().field_71439_g;
         if (player != null && Minecraft.func_71410_x().field_71462_r == null) {
            mc = Minecraft.func_71410_x();
            if (this.remoteViewingActive) {
               Minecraft.func_71410_x().field_71451_h = player;
            }

            ScaledResolution screen = new ScaledResolution(mc, mc.field_71443_c, mc.field_71440_d);
            int maxEnergy = Infusion.getMaxEnergy(player);
            double left;
            if (maxEnergy > 0) {
               if (infusionEnergyBar == null) {
                  infusionEnergyBar = new RenderInfusionEnergyBar(true);
               }

               double left = Config.instance().guiOnLeft ? 20.0D : (double)(screen.func_78326_a() - 20);
               left = (double)screen.func_78328_b() / 2.0D - 16.0D;
               Infusion infusion = Infusion.Registry.instance().get(player);
               infusionEnergyBar.draw(left, left, (double)Infusion.getCurrentEnergy(player) / (double)maxEnergy, player, infusion.infusionID);
            }

            int powerID = CreaturePower.getCreaturePowerID(player);
            int charges = CreaturePower.getCreaturePowerCharges(player);
            if (powerID > 0) {
               if (creatureEnergyBar == null) {
                  creatureEnergyBar = new RenderInfusionEnergyBar(false);
               }

               left = Config.instance().guiOnLeft ? 30.0D : (double)(screen.func_78326_a() - 30);
               double top = (double)screen.func_78328_b() / 2.0D - 16.0D;
               creatureEnergyBar.draw(left, top, (double)charges, player, powerID);
            }

            ItemStack belt = player.func_71124_b(2);
            if (belt != null && belt.func_77973_b() == Witchery.Items.BARK_BELT) {
               int beltCharges = Math.min(Witchery.Items.BARK_BELT.getChargeLevel(belt), Witchery.Items.BARK_BELT.getMaxChargeLevel(player));
               this.drawBarkBeltCharges(player, beltCharges, screen);
            }

            this.drawInfusedBrews(player, screen);
            ItemStack stack = player.func_71011_bu();
            int x;
            int y;
            int color;
            if (stack != null && stack.func_77973_b() == Witchery.Items.MYSTIC_BRANCH) {
               byte[] strokes = player.getEntityData().func_74770_j("Strokes");
               mc.func_110434_K().func_110577_a(TEXTURE_GRID);
               GL11.glPushMatrix();
               int iconOffset = 0;
               if (Config.instance().branchIconSet == 1) {
                  iconOffset = 64;
               }

               try {
                  x = screen.func_78326_a() / 2 - 8;
                  y = screen.func_78328_b() / 2 - 8;
                  int DELAY = true;
                  this.lastY = this.lastY == 120 ? 0 : this.lastY + 1;
                  color = this.lastY / 8;
                  int imageIndex = color > 7 ? 15 - color : color;

                  for(int i = 0; i < strokes.length; ++i) {
                     x += glyphOffsetX[strokes[i]] * 16;
                     y += glyphOffsetY[strokes[i]] * 16;
                     drawTexturedModalRect(x, y, strokes[i] * 16 + iconOffset, imageIndex * 16, 16, 16);
                  }

                  SymbolEffect effect = EffectRegistry.instance().getEffect(strokes);
                  if (effect != null) {
                     String text = effect.getLocalizedName();
                     int tx = screen.func_78326_a() / 2 - (int)(getStringWidth(text) / 2.0D);
                     int ty = screen.func_78328_b() / 2 + 20;
                     drawString(text, (double)tx, (double)ty, 16777215);
                  }
               } finally {
                  GL11.glPopMatrix();
               }
            } else if (stack != null && stack.func_77973_b() == Witchery.Items.BREW_BAG && !player.func_70093_af()) {
               ItemBrewBag.InventoryBrewBag inv = new ItemBrewBag.InventoryBrewBag(player);
               byte[] strokes = player.getEntityData().func_74770_j("Strokes");
               GL11.glPushMatrix();

               try {
                  x = screen.func_78326_a() / 2 - 8;
                  y = screen.func_78328_b() / 2 - 8;
                  if (strokes.length == 0) {
                     mc.func_110434_K().func_110577_a(RADIAL_LOCATION);
                     GL11.glPushMatrix();
                     float scale = 0.33333334F;
                     GL11.glTranslatef((float)(x - 42 + 5), (float)(y - 42 + 5), 0.0F);
                     GL11.glScalef(scale, scale, scale);
                     color = ItemBrewBag.getColor(stack);
                     float red = (float)(color >>> 16 & 255) / 256.0F;
                     float green = (float)(color >>> 8 & 255) / 256.0F;
                     float blue = (float)(color & 255) / 256.0F;
                     GL11.glColor4f(red, green, blue, 1.0F);
                     drawTexturedModalRect(8, 8, 0, 0, 256, 256);
                     GL11.glPopMatrix();
                  }

                  this.drawBrewInSlot(inv, 0, strokes, x + 0, y - 32, 0, -11, 1);
                  this.drawBrewInSlot(inv, 1, strokes, x + 24, y - 24, 23, 6, 0);
                  this.drawBrewInSlot(inv, 2, strokes, x + 32, y - 0, 23, 6, 0);
                  this.drawBrewInSlot(inv, 3, strokes, x + 24, y + 24, 23, 6, 0);
                  this.drawBrewInSlot(inv, 4, strokes, x + 0, y + 32, 0, 19, 1);
                  this.drawBrewInSlot(inv, 5, strokes, x - 24, y + 24, -5, 6, 2);
                  this.drawBrewInSlot(inv, 6, strokes, x - 32, y - 0, -5, 6, 2);
                  this.drawBrewInSlot(inv, 7, strokes, x - 24, y - 24, -5, 6, 2);
               } finally {
                  GL11.glPopMatrix();
               }
            }
         }
      }

   }

   private void drawInfusedBrews(EntityClientPlayerMP player, ScaledResolution screen) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      InfusedBrewEffect effect = InfusedBrewEffect.getActiveBrew(nbtPlayer);
      if (effect != null) {
         String remainingTime = InfusedBrewEffect.getMinutesRemaining(player.field_70170_p, nbtPlayer, effect);
         if (remainingTime != null && !remainingTime.isEmpty()) {
            Minecraft mc = Minecraft.func_71410_x();
            mc.func_110434_K().func_110577_a(BARK_TEXTURES);
            int tx = screen.func_78326_a() / 2 - 91;
            int screenHeight = screen.func_78328_b();
            int top = screen.func_78328_b() / 2 + 26;
            int screenMid = screenHeight / 2;
            int left = Config.instance().guiOnLeft ? 17 : screen.func_78326_a() - 23;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            int ICON_WIDTH = true;
            int ICON_HEIGHT = true;
            drawTexturedModalRect(left, top, effect.imageMapX, effect.imageMapY, 16, 16);
            double width = getStringWidth(remainingTime) / 2.0D;
            drawString(remainingTime, (double)(left + 8) - width, (double)(top + 10), -285212673);
         }
      }

   }

   private void drawBarkBeltCharges(EntityClientPlayerMP player, int beltCharges, ScaledResolution screen) {
      if (beltCharges > 0 && !player.field_71075_bZ.field_75098_d) {
         Minecraft mc = Minecraft.func_71410_x();
         mc.func_110434_K().func_110577_a(BARK_TEXTURES);
         int tx = screen.func_78326_a() / 2 - 91;
         int par2 = screen.func_78328_b();
         int ty = par2 / 2;
         IAttributeInstance attributeinstance = mc.field_71439_g.func_110148_a(SharedMonsterAttributes.field_111267_a);
         int i2 = par2 - 39;
         float f = (float)attributeinstance.func_111126_e();
         float f1 = mc.field_71439_g.func_110139_bj();
         int j2 = MathHelper.func_76123_f((f + f1) / 2.0F / 10.0F);
         int k2 = Math.max(10 - (j2 - 2), 3);
         int l2 = Witchery.modHooks.isTinkersPresent ? i2 - 10 : i2 - (j2 - 1) * k2 - 10;
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         int iconOffsetX = false;
         int ICON_WIDTH = true;
         int ICON_HEIGHT = true;
         int iconOffsetY = true;

         for(int i = 0; i < beltCharges; ++i) {
            drawTexturedModalRect(tx + i * 8, l2, 0, 248, 8, 8);
         }
      }

   }

   private void drawBrewInSlot(ItemBrewBag.InventoryBrewBag inv, int slot, byte[] strokes, int x, int y, int fx, int fy, int align) {
      ItemStack brew = inv.func_70301_a(slot);
      if (brew != null && (strokes.length == 0 || strokes[0] == StrokeSet.Stroke.INDEX_TO_STROKE[slot])) {
         drawItem(x, y, brew);
         String s = brew.func_82833_r();
         if (s != null) {
            s.trim();
            double fontX = (double)(x + fx);
            double fontY = (double)(y + fy);
            if (align != 0) {
               double width = getStringWidth(s);
               if (align == 1) {
                  fontX -= width / 2.0D;
               } else if (align == 2) {
                  fontX -= width;
               }
            }

            drawString(s, fontX, fontY, 2013265919);
         }
      }

   }

   private static FontRenderer getFontRenderer(ItemStack stack) {
      if (stack != null && stack.func_77973_b() != null) {
         FontRenderer f = stack.func_77973_b().getFontRenderer(stack);
         if (f != null) {
            return f;
         }
      }

      return Minecraft.func_71410_x().field_71466_p;
   }

   private static void drawItem(int i, int j, ItemStack itemstack) {
      drawItem(i, j, itemstack, getFontRenderer(itemstack));
   }

   private static void drawItem(int i, int j, ItemStack itemstack, FontRenderer fontRenderer) {
      Minecraft mc = Minecraft.func_71410_x();
      GL11.glEnable(2896);
      GL11.glEnable(2929);
      RenderItem var10000 = drawItems;
      var10000.field_77023_b += 100.0F;

      try {
         drawItems.func_82406_b(fontRenderer, mc.field_71446_o, itemstack, i, j);
         drawItems.func_77021_b(fontRenderer, mc.field_71446_o, itemstack, i, j);
      } catch (Exception var6) {
      }

      var10000 = drawItems;
      var10000.field_77023_b -= 100.0F;
      GL11.glDisable(2896);
      GL11.glDisable(2929);
   }

   public static void drawString(String s, double x, double y, int color) {
      RenderHelper.func_74518_a();
      RenderUtil.blend(true);
      RenderUtil.render2d(true);
      Minecraft.func_71410_x().field_71466_p.func_78261_a(s, (int)x, (int)y, color);
      RenderUtil.render2d(false);
      RenderUtil.blend(false);
   }

   public static double getStringWidth(String s) {
      GL11.glPushAttrib(262144);

      double val;
      try {
         val = (double)Minecraft.func_71410_x().field_71466_p.func_78256_a(s);
      } finally {
         GL11.glPopAttrib();
      }

      return val;
   }

   private static void drawTexturedModalRect(int par1, int par2, int par3, int par4, int par5, int par6) {
      double zLevel = 0.0D;
      float f = 0.00390625F;
      float f1 = 0.00390625F;
      Tessellator tessellator = Tessellator.field_78398_a;
      tessellator.func_78382_b();
      tessellator.func_78374_a((double)(par1 + 0), (double)(par2 + par6), zLevel, (double)((float)(par3 + 0) * f), (double)((float)(par4 + par6) * f1));
      tessellator.func_78374_a((double)(par1 + par5), (double)(par2 + par6), zLevel, (double)((float)(par3 + par5) * f), (double)((float)(par4 + par6) * f1));
      tessellator.func_78374_a((double)(par1 + par5), (double)(par2 + 0), zLevel, (double)((float)(par3 + par5) * f), (double)((float)(par4 + 0) * f1));
      tessellator.func_78374_a((double)(par1 + 0), (double)(par2 + 0), zLevel, (double)((float)(par3 + 0) * f), (double)((float)(par4 + 0) * f1));
      tessellator.func_78381_a();
   }

   @SubscribeEvent
   public void onClientTick(ClientTickEvent event) {
      Minecraft minecraft;
      EntityClientPlayerMP player;
      int len$;
      int x;
      if (event.phase == Phase.START) {
         minecraft = Minecraft.func_71410_x();
         player = minecraft.field_71439_g;
         if (player != null) {
            boolean allowSpeedUp = true;
            int creaturePowerID = CreaturePower.getCreaturePowerID(player);
            if (creaturePowerID > 0) {
               CreaturePower power = CreaturePower.Registry.instance().get(creaturePowerID);
               if (power != null) {
                  power.onUpdate(player.field_70170_p, player);
                  allowSpeedUp = !(power instanceof CreaturePowerSpeed);
               }
            }

            if (player.func_70694_bm() != null && player.func_70694_bm().func_77973_b() != null && allowSpeedUp && (player.func_70694_bm().func_77973_b() == Witchery.Items.MYSTIC_BRANCH || player.func_70694_bm().func_77973_b() == Witchery.Items.BREW_BAG) && player.func_71039_bw()) {
               boolean canGo = Math.abs(player.field_70159_w) <= 0.1D && Math.abs(player.field_70179_y) <= 0.1D;
               if (player.field_70170_p.func_147439_a(MathHelper.func_76128_c(player.field_70165_t), MathHelper.func_76128_c(player.field_70163_u) - 2, MathHelper.func_76128_c(player.field_70161_v)) != Blocks.field_150432_aD) {
                  if (player.field_70122_E) {
                     if (!player.func_70090_H()) {
                        if (canGo) {
                           player.field_70159_w *= 1.6500000476837158D;
                           player.field_70179_y *= 1.6500000476837158D;
                        }
                     } else if (canGo) {
                        player.field_70159_w *= 1.100000023841858D;
                        player.field_70179_y *= 1.100000023841858D;
                     }
                  }
               } else if (canGo) {
                  player.field_70159_w *= 1.100000023841858D;
                  player.field_70179_y *= 1.100000023841858D;
               }
            }

            len$ = Infusion.getSinkingCurseLevel(player);
            if (len$ > 0 && player.func_70090_H()) {
               if (player.field_70181_x < -0.03D && !player.field_70122_E) {
                  player.field_70181_x *= 1.5D + Math.min(0.05D * (double)(len$ - 1), 0.2D);
               } else if (!player.field_70122_E && player.func_70055_a(Material.field_151586_h) && player.field_70181_x > 0.0D) {
               }
            } else if (len$ > 0) {
               if (!player.field_71075_bZ.field_75098_d && player.field_71075_bZ.field_75101_c && player.field_71075_bZ.field_75100_b) {
                  player.field_70181_x = -0.20000000298023224D;
               }
            } else if (player.func_70644_a(Potion.field_76421_d) && !player.field_71075_bZ.field_75098_d && player.field_71075_bZ.field_75101_c && player.field_71075_bZ.field_75100_b) {
               PotionEffect effect = player.func_70660_b(Potion.field_76421_d);
               if (effect != null && effect.func_76458_c() > 4) {
                  player.field_70181_x = -0.20000000298023224D;
               }
            }

            if (len$ == 0 && BlockUtil.getBlockMaterial(player).func_76224_d() && player.func_82169_q(0) != null && player.func_82169_q(0).func_77973_b() == Witchery.Items.DEATH_FEET && player.field_70181_x < 0.0D) {
               player.field_70181_x += 0.1D;
            }

            if (player.field_70122_E && KeyBindHelper.isKeyBindDown(minecraft.field_71474_y.field_74314_A)) {
               x = MathHelper.func_76128_c(player.field_70165_t);
               int y = MathHelper.func_76128_c(player.field_70163_u);
               int z = MathHelper.func_76128_c(player.field_70161_v);
               if (player.field_70170_p.func_147439_a(x, y - 1, z) == Witchery.Blocks.LEAPING_LILY) {
                  player.func_85030_a("random.bowhit", 1.0F, 0.4F / ((float)player.field_70170_p.field_73012_v.nextDouble() * 0.4F + 0.8F));
               }
            }
         }
      } else if (event.phase == Phase.END) {
         minecraft = Minecraft.func_71410_x();
         player = minecraft.field_71439_g;
         if (player != null && minecraft.field_71462_r != null && minecraft.field_71462_r instanceof InventoryEffectRenderer && player.field_71093_bK == Config.instance().dimensionDreamID && !player.field_71075_bZ.field_75098_d) {
            if (this.fieldAccess == null) {
               try {
                  Field[] fields = GuiScreen.class.getDeclaredFields();
                  if (fields.length > 3) {
                     if (fields[3].getType() == List.class) {
                        Field field = fields[3];
                        field.setAccessible(true);
                        this.fieldAccess = field;
                     } else {
                        Field[] arr$ = fields;
                        len$ = fields.length;

                        for(x = 0; x < len$; ++x) {
                           Field field = arr$[x];
                           if (field.getType() == List.class) {
                              field.setAccessible(true);
                              this.fieldAccess = field;
                              break;
                           }
                        }
                     }
                  }
               } catch (Exception var11) {
                  Log.instance().debug(String.format("Exception occurred setting player gui. %s", var11.toString()));
               }
            }

            if (this.fieldAccess != null) {
               try {
                  List list = (List)this.fieldAccess.get(minecraft.field_71462_r);
                  if (list.size() > 0) {
                     list.clear();
                  }
               } catch (IllegalAccessException var10) {
                  Log.instance().warning(var10, "Exception occurred setting player gui screen");
               }
            }
         }
      }

   }
}
