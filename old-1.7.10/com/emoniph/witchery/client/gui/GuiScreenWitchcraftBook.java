package com.emoniph.witchery.client.gui;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.crafting.BrazierRecipes;
import com.emoniph.witchery.crafting.DistilleryRecipes;
import com.emoniph.witchery.crafting.KettleRecipes;
import com.emoniph.witchery.infusion.infusions.spirit.InfusedSpiritEffect;
import com.emoniph.witchery.infusion.infusions.symbols.EffectRegistry;
import com.emoniph.witchery.infusion.infusions.symbols.SymbolEffect;
import com.emoniph.witchery.network.PacketItemUpdate;
import com.emoniph.witchery.ritual.RiteRegistry;
import com.emoniph.witchery.util.Const;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiScreenWitchcraftBook extends GuiScreen {
   private static final ResourceLocation field_110405_a = new ResourceLocation("textures/gui/book.png");
   public static final ResourceLocation DOUBLE_BOOK_TEXTURE = new ResourceLocation("witchery", "textures/gui/bookDouble.png");
   private static final ResourceLocation[] field_110405_b = new ResourceLocation[]{new ResourceLocation("witchery", "textures/gui/circle_white_large.png"), new ResourceLocation("witchery", "textures/gui/circle_blue_large.png"), new ResourceLocation("witchery", "textures/gui/circle_red_large.png"), new ResourceLocation("witchery", "textures/gui/circle_white_medium.png"), new ResourceLocation("witchery", "textures/gui/circle_blue_medium.png"), new ResourceLocation("witchery", "textures/gui/circle_red_medium.png"), new ResourceLocation("witchery", "textures/gui/circle_white_small.png"), new ResourceLocation("witchery", "textures/gui/circle_blue_small.png"), new ResourceLocation("witchery", "textures/gui/circle_red_small.png")};
   private static final String[] sizes = new String[]{"§715x15§0", "§515x15§0", "§415x15§0", "§711x11§0", "§511x11§0", "§411x11§0", "§77x7§0", "§57x7§0", "§47x7§0"};
   private final EntityPlayer player;
   private final ItemStack itemstack;
   private int updateCount;
   private int bookImageWidth = 192;
   private int bookImageHeight = 192;
   private int bookTotalPages = 1;
   private int currPage;
   private NBTTagList bookPages;
   private String bookTitle = "";
   private GuiButtonNextPage buttonNextPage;
   private GuiButtonNextPage buttonPreviousPage;
   private GuiButton buttonDone;
   private GuiButtonJumpPage buttonJumpPage1;
   private GuiButtonJumpPage buttonJumpPage2;
   private GuiButtonJumpPage buttonJumpPage3;
   private GuiButtonJumpPage buttonJumpPage4;
   private GuiButtonJumpPage buttonJumpPage5;
   private GuiButtonJumpPage buttonJumpPage6;
   private GuiButtonJumpPage buttonJumpPage7;
   private static final String CURRENT_PAGE_KEY = "CurrentPage";

   public GuiScreenWitchcraftBook(EntityPlayer player, ItemStack itemstack) {
      this.player = player;
      this.itemstack = itemstack;
      this.bookTitle = itemstack.func_82833_r();
      this.bookPages = new NBTTagList();
      NBTTagCompound compound;
      if (Witchery.Items.GENERIC.itemBookOven.isMatch(itemstack)) {
         compound = new NBTTagCompound();
         compound.func_74778_a("Summary", Witchery.resource("witchery.book.oven1"));
         this.bookPages.func_74742_a(compound);
         compound = new NBTTagCompound();
         compound.func_74778_a("Summary", Witchery.resource("witchery.book.oven2"));
         this.bookPages.func_74742_a(compound);
         compound = new NBTTagCompound();
         compound.func_74778_a("Summary", Witchery.resource("witchery.book.oven3"));
         this.bookPages.func_74742_a(compound);
      } else {
         String intro;
         Iterator i$;
         if (Witchery.Items.GENERIC.itemBookDistilling.isMatch(itemstack)) {
            compound = new NBTTagCompound();
            intro = Witchery.resource("witchery.book.distillery1");
            compound.func_74778_a("Summary", intro);
            this.bookPages.func_74742_a(compound);
            i$ = DistilleryRecipes.instance().recipes.iterator();

            while(i$.hasNext()) {
               DistilleryRecipes.DistilleryRecipe recipe = (DistilleryRecipes.DistilleryRecipe)i$.next();
               compound = new NBTTagCompound();
               compound.func_74778_a("Summary", recipe.getDescription());
               this.bookPages.func_74742_a(compound);
            }
         } else {
            String intro2;
            if (Witchery.Items.GENERIC.itemBookCircleMagic.isMatch(itemstack)) {
               compound = new NBTTagCompound();
               intro = Witchery.resource("witchery.book.rites1");
               intro2 = Witchery.resource("witchery.book.rites2");
               String anyCircles = Witchery.resource("witchery.book.rites.anycircle");
               compound.func_74778_a("Summary", intro);
               compound.func_74778_a("Summary2", intro2);
               compound.func_74773_a("Circles", new byte[]{0, 3, 6});
               this.bookPages.func_74742_a(compound);
               Iterator i$ = RiteRegistry.instance().getSortedRituals().iterator();

               label113:
               while(true) {
                  RiteRegistry.Ritual ritual;
                  do {
                     if (!i$.hasNext()) {
                        break label113;
                     }

                     ritual = (RiteRegistry.Ritual)i$.next();
                  } while(!ritual.showInBook());

                  compound = new NBTTagCompound();
                  compound.func_74778_a("Summary", ritual.getDescription());
                  byte[] circles = ritual.getCircles();
                  compound.func_74773_a("Circles", circles);
                  if (circles.length == 0) {
                     compound.func_74778_a("Summary2", anyCircles);
                  } else {
                     StringBuilder sb = new StringBuilder();
                     byte[] arr$ = circles;
                     int len$ = circles.length;

                     for(int i$ = 0; i$ < len$; ++i$) {
                        byte cir = arr$[i$];
                        if (sb.length() > 0) {
                           sb.append(", ");
                        }

                        sb.append(sizes[cir]);
                     }

                     compound.func_74778_a("Summary2", sb.toString());
                  }

                  this.bookPages.func_74742_a(compound);
               }
            } else if (Witchery.Items.GENERIC.itemBookInfusions.isMatch(itemstack)) {
               compound = new NBTTagCompound();
               intro = Witchery.resource("witchery.book.brews1");
               compound.func_74778_a("Summary", intro);
               this.bookPages.func_74742_a(compound);
               i$ = KettleRecipes.instance().recipes.iterator();

               while(i$.hasNext()) {
                  KettleRecipes.KettleRecipe recipe = (KettleRecipes.KettleRecipe)i$.next();
                  if (recipe.inBook) {
                     compound = new NBTTagCompound();
                     compound.func_74778_a("Summary", recipe.getDescription());
                     this.bookPages.func_74742_a(compound);
                  }
               }
            } else if (Witchery.Items.GENERIC.itemBookBurning.isMatch(itemstack)) {
               compound = new NBTTagCompound();
               intro = Witchery.resource("witchery.book.burning1");
               compound.func_74778_a("Summary", intro);
               this.bookPages.func_74742_a(compound);
               i$ = BrazierRecipes.instance().recipes.iterator();

               while(i$.hasNext()) {
                  BrazierRecipes.BrazierRecipe recipe = (BrazierRecipes.BrazierRecipe)i$.next();
                  if (recipe.inBook) {
                     compound = new NBTTagCompound();
                     compound.func_74778_a("Summary", recipe.getDescription());
                     this.bookPages.func_74742_a(compound);
                  }
               }

               compound = new NBTTagCompound();
               intro2 = Witchery.resource("witchery.book.burning2");
               compound.func_74778_a("Summary", intro2);
               this.bookPages.func_74742_a(compound);
               Iterator i$ = InfusedSpiritEffect.effectList.iterator();

               while(i$.hasNext()) {
                  InfusedSpiritEffect effect = (InfusedSpiritEffect)i$.next();
                  if (effect != null && effect.isInBook()) {
                     compound = new NBTTagCompound();
                     compound.func_74778_a("Summary", effect.getDescription());
                     this.bookPages.func_74742_a(compound);
                  }
               }
            } else if (Witchery.Items.GENERIC.itemBookHerbology.isMatch(itemstack)) {
               compound = new NBTTagCompound();
               intro = Witchery.resource("witchery.book.herbology1");
               compound.func_74778_a("Summary", intro);
               this.bookPages.func_74742_a(compound);
               this.addPlantPage((Block)Witchery.Blocks.CROP_BELLADONNA, "witchery.book.herbology.belladonna", "witchery:textures/blocks/belladonna_stage_4.png");
               this.addPlantPage(Witchery.Blocks.EMBER_MOSS, "witchery.book.herbology.embermoss", "witchery:textures/blocks/embermoss.png");
               this.addPlantPage(Witchery.Blocks.GLINT_WEED, "witchery.book.herbology.glintweed", "witchery:textures/blocks/glintWeed.png");
               this.addPlantPage((Block)Witchery.Blocks.CROP_MANDRAKE, "witchery.book.herbology.mandrake", "witchery:textures/blocks/mandrake_stage_4.png");
               this.addPlantPage((Block)Witchery.Blocks.CROP_SNOWBELL, "witchery.book.herbology.snowbell", "witchery:textures/blocks/snowbell_stage_4.png");
               this.addPlantPage(Witchery.Blocks.SPANISH_MOSS, "witchery.book.herbology.spanishmoss", "witchery:textures/blocks/spanishMoss.png");
               this.addPlantPage(new ItemStack(Witchery.Blocks.BRAMBLE, 1, 1), "witchery.book.herbology.wildbramble", "witchery:textures/blocks/bramble_wild.png");
               this.addPlantPage(new ItemStack(Witchery.Blocks.BRAMBLE, 1, 0), "witchery.book.herbology.enderbramble", "witchery:textures/blocks/bramble_ender.png");
               this.addPlantPage(Witchery.Blocks.VOID_BRAMBLE, "witchery.book.herbology.voidbramble", "witchery:textures/blocks/voidBramble.png");
               this.addPlantPage((Block)Witchery.Blocks.CROP_ARTICHOKE, "witchery.book.herbology.artichoke", "witchery:textures/blocks/artichoke_stage_4.png");
               this.addPlantPage(Witchery.Blocks.GRASSPER, "witchery.book.herbology.grassper", "witchery:textures/blocks/grassperIcon.png");
               this.addPlantPage(Witchery.Blocks.CRITTER_SNARE, "witchery.book.herbology.crittersnare", "witchery:textures/blocks/critterSnare_empty.png");
               this.addPlantPage(Witchery.Blocks.BLOOD_ROSE, "witchery.book.herbology.bloodrose", "witchery:textures/blocks/bloodrose.png");
               this.addPlantPage(Witchery.Blocks.WISPY_COTTON, "witchery.book.herbology.somniancotton", "witchery:textures/blocks/somnianCotton.png");
               this.addPlantPage((Block)Witchery.Blocks.CROP_WOLFSBANE, "witchery.book.herbology.wolfsbane", "witchery:textures/blocks/wolfsbane_stage_7.png");
               this.addPlantPage((Block)Witchery.Blocks.CROP_GARLIC, "witchery.book.herbology.garlic", "witchery:textures/blocks/garlic_stage_5.png");
               this.addPlantPage(new ItemStack(Witchery.Blocks.SAPLING, 1, 1), "witchery.book.herbology.alder", "witchery:textures/blocks/sapling_alder.png");
               this.addPlantPage(new ItemStack(Witchery.Blocks.SAPLING, 1, 2), "witchery.book.herbology.hawthorn", "witchery:textures/blocks/sapling_hawthorn.png");
               this.addPlantPage(new ItemStack(Witchery.Blocks.SAPLING, 1, 0), "witchery.book.herbology.rowan", "witchery:textures/blocks/sapling_rowan.png");
            } else if (Witchery.Items.GENERIC.itemBookBiomes.isMatch(itemstack)) {
               compound = new NBTTagCompound();
               intro = Witchery.resource("witchery.book.biomes1");
               compound.func_74778_a("Summary", intro);
               this.bookPages.func_74742_a(compound);
               this.addBiomes(Type.FOREST);
               this.addBiomes(Type.PLAINS);
               this.addBiomes(Type.MOUNTAIN);
               this.addBiomes(Type.HILLS);
               this.addBiomes(Type.SWAMP);
               this.addBiomes(Type.WATER);
               this.addBiomes(Type.DESERT);
               this.addBiomes(Type.FROZEN);
               this.addBiomes(Type.JUNGLE);
               this.addBiomes(Type.WASTELAND);
               this.addBiomes(Type.BEACH);
               this.addBiomes(Type.MUSHROOM);
               this.addBiomes(Type.MAGICAL);
            } else if (Witchery.Items.GENERIC.itemBookWands.isMatch(itemstack)) {
               compound = new NBTTagCompound();
               intro = Witchery.resource("witchery.book.wands1");
               compound.func_74778_a("Summary", intro);
               this.bookPages.func_74742_a(compound);
               i$ = EffectRegistry.instance().getEffects().iterator();

               while(i$.hasNext()) {
                  SymbolEffect recipe = (SymbolEffect)i$.next();
                  if (recipe.isVisible(player)) {
                     compound = new NBTTagCompound();
                     compound.func_74778_a("Summary", recipe.getDescription());
                     this.bookPages.func_74742_a(compound);
                  }
               }
            }
         }
      }

      this.bookTotalPages = this.bookPages.func_74745_c();
      compound = itemstack.func_77978_p();
      if (compound != null && compound.func_74764_b("CurrentPage")) {
         this.currPage = Math.min(Math.max(compound.func_74762_e("CurrentPage"), 0), Math.max(this.bookTotalPages, 1) - 1);
      }

   }

   private void addBiomes(Type biomeType) {
      String biomeKey = biomeType.toString().toLowerCase();
      String title = "§n" + Witchery.resource("witchery.book.biomes." + biomeKey + ".name") + "§r" + "\n\n" + "§8" + Witchery.resource("witchery.book.biomes.foci") + ": " + Witchery.resource("witchery.book.biomes." + biomeKey + ".item") + "§0" + Const.BOOK_NEWLINE;
      BiomeGenBase[] biomes = BiomeDictionary.getBiomesForType(biomeType);
      int ITEMS_PER_PAGE = true;
      StringBuilder sb = new StringBuilder();

      for(int glowstone = 1; glowstone <= biomes.length; ++glowstone) {
         sb.append(glowstone);
         sb.append(" : ");
         sb.append(biomes[glowstone - 1].field_76791_y);
         sb.append(Const.BOOK_NEWLINE);
         if (glowstone % 8 == 0 || glowstone == biomes.length) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.func_74778_a("Summary", title + Const.BOOK_NEWLINE + sb.toString());
            this.bookPages.func_74742_a(compound);
            sb = new StringBuilder();
         }
      }

   }

   private void addPlantPage(ItemStack plantStack, String descriptionResourceID, String imageResourceID) {
      NBTTagCompound compound = new NBTTagCompound();
      compound.func_74778_a("Summary", "§n" + plantStack.func_82833_r() + "§r");
      compound.func_74778_a("Details", Witchery.resource(descriptionResourceID));
      compound.func_74778_a("Image", imageResourceID);
      this.bookPages.func_74742_a(compound);
   }

   private void addPlantPage(Block plantBlock, String descriptionResourceID, String imageResourceID) {
      NBTTagCompound compound = new NBTTagCompound();
      compound.func_74778_a("Summary", "§n" + plantBlock.func_149732_F() + "§r");
      compound.func_74778_a("Details", Witchery.resource(descriptionResourceID));
      compound.func_74778_a("Image", imageResourceID);
      this.bookPages.func_74742_a(compound);
   }

   private void storeCurrentPage() {
      if (this.itemstack.func_77978_p() == null) {
         this.itemstack.func_77982_d(new NBTTagCompound());
      }

      this.itemstack.func_77978_p().func_74768_a("CurrentPage", this.currPage);
   }

   public void func_73876_c() {
      super.func_73876_c();
      ++this.updateCount;
   }

   public void func_73866_w_() {
      this.field_146292_n.clear();
      Keyboard.enableRepeatEvents(true);
      this.field_146292_n.add(this.buttonDone = new GuiButton(0, this.field_146294_l / 2 - 100, 4 + this.bookImageHeight, 200, 20, I18n.func_135052_a("gui.done", new Object[0])));
      int i = (this.field_146294_l - this.bookImageWidth) / 2;
      byte b0 = 2;
      if (Witchery.Items.GENERIC.itemBookCircleMagic.isMatch(this.itemstack)) {
         this.field_146292_n.add(this.buttonNextPage = new GuiButtonNextPage(1, i + 180, b0 + 154, true));
         this.field_146292_n.add(this.buttonPreviousPage = new GuiButtonNextPage(2, i + 110, b0 + 154, false));
         this.field_146292_n.add(this.buttonJumpPage7 = new GuiButtonJumpPage(9, i + 214, b0 + 138, 69, 48, 248));
         this.field_146292_n.add(this.buttonJumpPage6 = new GuiButtonJumpPage(8, i + 214, b0 + 118, 58, 40, 248));
         this.field_146292_n.add(this.buttonJumpPage5 = new GuiButtonJumpPage(7, i + 214, b0 + 98, 47, 32, 248));
         this.field_146292_n.add(this.buttonJumpPage4 = new GuiButtonJumpPage(6, i + 214, b0 + 78, 29, 24, 248));
         this.field_146292_n.add(this.buttonJumpPage3 = new GuiButtonJumpPage(5, i + 214, b0 + 58, 23, 16, 248));
         this.field_146292_n.add(this.buttonJumpPage2 = new GuiButtonJumpPage(4, i + 214, b0 + 38, 17, 8, 248));
         this.field_146292_n.add(this.buttonJumpPage1 = new GuiButtonJumpPage(3, i + 214, b0 + 18, 2, 0, 248));
      } else {
         this.field_146292_n.add(this.buttonNextPage = new GuiButtonNextPage(1, i + 120, b0 + 154, true));
         this.field_146292_n.add(this.buttonPreviousPage = new GuiButtonNextPage(2, i + 38, b0 + 154, false));
      }

      this.updateButtons();
   }

   public void func_146281_b() {
      Keyboard.enableRepeatEvents(false);
      this.sendBookToServer(false);
   }

   private void updateButtons() {
      this.buttonNextPage.field_146125_m = this.currPage < this.bookTotalPages - 1;
      this.buttonPreviousPage.field_146125_m = this.currPage > 0;
   }

   private void sendBookToServer(boolean par1) {
      if (this.player != null && this.currPage >= 0 && this.currPage < 1000 && this.player.field_71071_by.field_70461_c >= 0 && this.player.field_71071_by.func_70448_g() != null) {
         Witchery.packetPipeline.sendToServer(new PacketItemUpdate(this.player.field_71071_by.field_70461_c, this.currPage, this.player.field_71071_by.func_70448_g()));
      }

   }

   protected void func_146284_a(GuiButton par1GuiButton) {
      if (par1GuiButton.field_146124_l) {
         if (par1GuiButton.field_146127_k == 0) {
            this.field_146297_k.func_147108_a((GuiScreen)null);
         } else if (par1GuiButton.field_146127_k == 1) {
            if (this.currPage < this.bookTotalPages - 1) {
               ++this.currPage;
               this.storeCurrentPage();
            }
         } else if (par1GuiButton.field_146127_k == 2) {
            if (this.currPage > 0) {
               --this.currPage;
               this.storeCurrentPage();
            }
         } else if (par1GuiButton instanceof GuiButtonJumpPage) {
            GuiButtonJumpPage but = (GuiButtonJumpPage)par1GuiButton;
            this.currPage = but.nextPage - 1;
            this.storeCurrentPage();
         }

         this.updateButtons();
      }

   }

   protected void func_73869_a(char par1, int par2) {
      super.func_73869_a(par1, par2);
   }

   public void func_73863_a(int par1, int par2, float par3) {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      int k;
      byte b0;
      String s;
      String s1;
      String s2;
      NBTTagCompound compound;
      byte[] circles;
      byte[] arr$;
      int len$;
      int i$;
      byte circle;
      if (Witchery.Items.GENERIC.itemBookCircleMagic.isMatch(this.itemstack)) {
         this.field_146297_k.func_110434_K().func_110577_a(DOUBLE_BOOK_TEXTURE);
         this.bookImageWidth = 256;
         k = (this.field_146294_l - this.bookImageWidth) / 2;
         b0 = 2;
         this.func_73729_b(k, b0, 0, 0, this.bookImageWidth, this.bookImageHeight);
         String s3 = "";
         s = I18n.func_135052_a("book.pageIndicator", new Object[]{this.currPage + 1, this.bookTotalPages});
         s1 = "";
         s2 = "";
         if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.func_74745_c()) {
            compound = this.bookPages.func_150305_b(this.currPage);
            s1 = compound.func_74779_i("Summary");
            s2 = compound.func_74779_i("Summary2");
            if (compound.func_74764_b("Circles")) {
               circles = compound.func_74770_j("Circles");
               arr$ = circles;
               len$ = circles.length;

               for(i$ = 0; i$ < len$; ++i$) {
                  circle = arr$[i$];
                  this.field_146297_k.func_110434_K().func_110577_a(field_110405_b[circle]);
                  this.func_73729_b(k, b0, -148, -36, this.bookImageWidth, this.bookImageHeight);
               }
            }
         }

         int l = this.field_146289_q.func_78256_a(s);
         this.field_146289_q.func_78276_b(s, k - l + this.bookImageWidth - 16, b0 + 16, 0);
         this.field_146289_q.func_78279_b(s1, k + 20, b0 + 16, 98, 0);
         if (!s2.isEmpty()) {
            int swidth = this.field_146289_q.func_78256_a(s2);
            if (swidth < 90) {
               this.field_146289_q.func_78279_b(s2, k + this.bookImageWidth / 4 * 3 - swidth / 2, b0 + 125, 98, 0);
            } else {
               this.field_146289_q.func_78279_b(s2, k + 142, b0 + 125, 98, 0);
            }
         }
      } else {
         this.field_146297_k.func_110434_K().func_110577_a(field_110405_a);
         k = (this.field_146294_l - this.bookImageWidth) / 2;
         b0 = 2;
         this.func_73729_b(k, b0, 0, 0, this.bookImageWidth, this.bookImageHeight);
         s2 = "";
         s = I18n.func_135052_a("book.pageIndicator", new Object[]{this.currPage + 1, this.bookTotalPages});
         s1 = "";
         boolean hasImage = false;
         if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.func_74745_c()) {
            compound = this.bookPages.func_150305_b(this.currPage);
            s1 = compound.func_74779_i("Summary");
            if (compound.func_74764_b("Circles")) {
               circles = compound.func_74770_j("Circles");
               arr$ = circles;
               len$ = circles.length;

               for(i$ = 0; i$ < len$; ++i$) {
                  circle = arr$[i$];
                  this.field_146297_k.func_110434_K().func_110577_a(field_110405_b[circle]);
                  this.func_73729_b(k, b0, -62, -70, this.bookImageWidth, this.bookImageHeight);
               }
            }

            hasImage = compound.func_74764_b("Image");
            if (hasImage) {
               String loc = compound.func_74779_i("Image");
               ResourceLocation location = new ResourceLocation(loc);
               this.field_146297_k.func_110434_K().func_110577_a(location);
               drawTexturedQuadFit((double)(k - 32 + this.bookImageWidth - 44), (double)(b0 + 32), 32.0D, 32.0D, (double)this.field_73735_i);
            }

            if (compound.func_74764_b("Details")) {
               s2 = compound.func_74779_i("Details");
            }
         }

         int l = this.field_146289_q.func_78256_a(s);
         this.field_146289_q.func_78276_b(s, k - l + this.bookImageWidth - 44, b0 + 16, 0);
         this.field_146289_q.func_78279_b(s1, k + 36, b0 + 32, 116 - (hasImage ? 34 : 0), 0);
         if (s2 != null && !s2.isEmpty()) {
            this.field_146289_q.func_78279_b(s2, k + 36, b0 + 32 + 34, 116, 0);
         }
      }

      super.func_73863_a(par1, par2, par3);
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

   static ResourceLocation func_110404_g() {
      return field_110405_a;
   }
}
