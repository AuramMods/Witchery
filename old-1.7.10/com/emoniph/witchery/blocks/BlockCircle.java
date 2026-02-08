package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.common.PowerSources;
import com.emoniph.witchery.entity.EntityCovenWitch;
import com.emoniph.witchery.ritual.Circle;
import com.emoniph.witchery.ritual.RiteRegistry;
import com.emoniph.witchery.ritual.RitualStep;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCircle extends BlockBaseContainer {
   public BlockCircle() {
      super(Material.field_151582_l, BlockCircle.TileEntityCircle.class);
      super.registerWithCreateTab = false;
      this.func_149711_c(3.0F);
      this.func_149752_b(1000.0F);
      float f1 = 0.015625F;
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 0.015625F, 1.0F);
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int par1, int par2) {
      return this.field_149761_L;
   }

   public void func_149699_a(World world, int posX, int posY, int posZ, EntityPlayer player) {
      if (!world.field_72995_K) {
         ItemStack itemstack = player.func_70694_bm();
         if (itemstack != null && (Witchery.Items.GENERIC.itemBroom.isMatch(itemstack) || Witchery.Items.GENERIC.itemBroomEnchanted.isMatch(itemstack))) {
            world.func_147480_a(posX, posY, posZ, false);
         }
      }

   }

   public AxisAlignedBB func_149668_a(World par1World, int par2, int par3, int par4) {
      return null;
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public int func_149745_a(Random rand) {
      return 0;
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      return new ItemStack(Witchery.Items.CHALK_GOLDEN);
   }

   public void func_149695_a(World par1World, int par2, int par3, int par4, Block par5) {
      if (this.func_111046_k(par1World, par2, par3, par4)) {
         boolean flag = par1World.func_72864_z(par2, par3, par4);
         BlockCircle.TileEntityCircle tileCircle = (BlockCircle.TileEntityCircle)BlockUtil.getTileEntity(par1World, par2, par3, par4, BlockCircle.TileEntityCircle.class);
         if (tileCircle != null && tileCircle.previousRedstoneState != flag) {
            if (flag) {
               this.activateBlock(par1World, par2, par3, par4, (EntityPlayer)null, false);
            }

            tileCircle.previousRedstoneState = flag;
         }
      }

   }

   private boolean func_111046_k(World par1World, int par2, int par3, int par4) {
      if (!this.func_149718_j(par1World, par2, par3, par4)) {
         par1World.func_147468_f(par2, par3, par4);
         return false;
      } else {
         return true;
      }
   }

   public boolean func_149718_j(World world, int x, int y, int z) {
      Material material = world.func_147439_a(x, y - 1, z).func_149688_o();
      return !world.func_147437_c(x, y - 1, z) && material != null && material.func_76218_k() && material.func_76220_a();
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      return par5 == 1;
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
      int metadata = world.func_72805_g(x, y, z);
      if (metadata == 1) {
         double d0 = (double)((float)x + 0.4F + rand.nextFloat() * 0.2F);
         double d1 = (double)((float)y + 0.1F + rand.nextFloat() * 0.3F);
         double d2 = (double)((float)z + 0.4F + rand.nextFloat() * 0.2F);
         world.func_72869_a(ParticleEffect.REDDUST.toString(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }

   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
      ItemStack stack = player.func_70694_bm();
      this.activateBlock(world, x, y, z, player, stack != null && Witchery.Items.GENERIC.itemSeerStone.isMatch(stack));
      return true;
   }

   private void activateBlock(World world, int posX, int posY, int posZ, EntityPlayer player, boolean summonCoven) {
      BlockCircle.TileEntityCircle tileEntity = (BlockCircle.TileEntityCircle)BlockUtil.getTileEntity(world, posX, posY, posZ, BlockCircle.TileEntityCircle.class);
      if (tileEntity != null) {
         if (tileEntity.isRitualActive()) {
            tileEntity.deactivate();
         } else if (!world.field_72995_K) {
            if (!PowerSources.instance().isAreaNulled(world, posX, posY, posZ) && world.field_73011_w.field_76574_g != Config.instance().dimensionDreamID) {
               Circle a = new Circle(16);
               Circle b = new Circle(28);
               Circle c = new Circle(40);
               Circle _ = new Circle(0);
               Circle[][] PATTERN = new Circle[][]{{_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _}, {_, _, _, _, _, c, c, c, c, c, c, c, _, _, _, _, _}, {_, _, _, _, c, _, _, _, _, _, _, _, c, _, _, _, _}, {_, _, _, c, _, _, b, b, b, b, b, _, _, c, _, _, _}, {_, _, c, _, _, b, _, _, _, _, _, b, _, _, c, _, _}, {_, c, _, _, b, _, _, a, a, a, _, _, b, _, _, c, _}, {_, c, _, b, _, _, a, _, _, _, a, _, _, b, _, c, _}, {_, c, _, b, _, a, _, _, _, _, _, a, _, b, _, c, _}, {_, c, _, b, _, a, _, _, _, _, _, a, _, b, _, c, _}, {_, c, _, b, _, a, _, _, _, _, _, a, _, b, _, c, _}, {_, c, _, b, _, _, a, _, _, _, a, _, _, b, _, c, _}, {_, c, _, _, b, _, _, a, a, a, _, _, b, _, _, c, _}, {_, _, c, _, _, b, _, _, _, _, _, b, _, _, c, _, _}, {_, _, _, c, _, _, b, b, b, b, b, _, _, c, _, _, _}, {_, _, _, _, c, _, _, _, _, _, _, _, c, _, _, _, _}, {_, _, _, _, _, c, c, c, c, c, c, c, _, _, _, _, _}, {_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _}};
               int offsetZ = (PATTERN.length - 1) / 2;

               int worldX;
               for(int z = 0; z < PATTERN.length - 1; ++z) {
                  int worldZ = posZ - offsetZ + z;
                  int offsetX = (PATTERN[z].length - 1) / 2;

                  for(int x = 0; x < PATTERN[z].length; ++x) {
                     worldX = posX - offsetX + x;
                     PATTERN[PATTERN.length - 1 - z][x].addGlyph(world, worldX, posY, worldZ);
                  }
               }

               boolean isDaytime = world.func_72935_r();
               boolean isRainPossible = world.func_72807_a(posX, posZ).func_76738_d();
               boolean isRaining = world.func_72896_J() && isRainPossible;
               boolean isThundering = world.func_72911_I();
               worldX = PATTERN.length / 2;
               AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)(posX - worldX), (double)posY, (double)(posZ - worldX), (double)(posX + worldX), (double)(posY + 1), (double)(posZ + worldX));
               ArrayList<Entity> entities = new ArrayList();
               Iterator i$ = world.func_72872_a(Entity.class, bounds).iterator();

               while(i$.hasNext()) {
                  Object obj = i$.next();
                  Entity item = (Entity)obj;
                  entities.add(item);
               }

               ArrayList<ItemStack> grassperStacks = new ArrayList();
               int radius = true;

               for(int x = posX - 5; x <= posX + 5; ++x) {
                  for(int z = posZ - 5; z <= posZ + 5; ++z) {
                     Block block = world.func_147439_a(x, posY, z);
                     if (block == Witchery.Blocks.GRASSPER) {
                        TileEntity tile = world.func_147438_o(x, posY, z);
                        if (tile != null && tile instanceof BlockGrassper.TileEntityGrassper) {
                           BlockGrassper.TileEntityGrassper grassper = (BlockGrassper.TileEntityGrassper)tile;
                           ItemStack stack = grassper.func_70301_a(0);
                           if (stack != null) {
                              grassperStacks.add(stack);
                           }
                        }
                     }
                  }
               }

               Circle[] circles = new Circle[]{a, b, c};
               boolean ritualFound = false;
               int covenSize = summonCoven ? EntityCovenWitch.getCovenSize(player) : 0;
               Iterator i$ = RiteRegistry.instance().getRituals().iterator();

               while(i$.hasNext()) {
                  RiteRegistry.Ritual ritual = (RiteRegistry.Ritual)i$.next();
                  if (ritual.isMatch(world, posX, posY, posZ, circles, entities, grassperStacks, isDaytime, isRaining, isThundering)) {
                     tileEntity.queueRitual(ritual, bounds, player, covenSize, summonCoven);
                     summonCoven = false;
                     ritualFound = true;
                  }
               }

               if (!ritualFound && !world.field_72995_K) {
                  RiteRegistry.RiteError("witchery.rite.unknownritual", player, world);
                  SoundEffect.NOTE_SNARE.playAt(world, (double)posX, (double)posY, (double)posZ);
               }

            } else {
               ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "witchery.rite.nullfield");
               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }
         }
      }
   }

   public static class TileEntityCircle extends TileEntityBase {
      public boolean previousRedstoneState;
      private final ArrayList<BlockCircle.TileEntityCircle.ActivatedRitual> activeRituals = new ArrayList();
      private final ArrayList<BlockCircle.TileEntityCircle.ActivatedRitual> upkeepRituals = new ArrayList();
      private boolean abortNext = false;

      public void func_145841_b(NBTTagCompound nbtTag) {
         super.func_145841_b(nbtTag);
         byte[] ritualIDs = new byte[this.upkeepRituals.size()];
         byte[] stages = new byte[this.upkeepRituals.size()];
         byte[] covenSizes = new byte[this.upkeepRituals.size()];
         NBTTagList nbtList = new NBTTagList();
         NBTTagList nbtLocationList = new NBTTagList();

         for(int i = 0; i < this.upkeepRituals.size(); ++i) {
            ritualIDs[i] = ((BlockCircle.TileEntityCircle.ActivatedRitual)this.upkeepRituals.get(i)).ritual.getRitualID();
            stages[i] = (byte)((BlockCircle.TileEntityCircle.ActivatedRitual)this.upkeepRituals.get(i)).getCurrentStage();
            covenSizes[i] = (byte)((BlockCircle.TileEntityCircle.ActivatedRitual)this.upkeepRituals.get(i)).covenSize;
            nbtList.func_74742_a(new NBTTagString(((BlockCircle.TileEntityCircle.ActivatedRitual)this.upkeepRituals.get(i)).getInitiatingPlayerName()));
            nbtLocationList.func_74742_a(((BlockCircle.TileEntityCircle.ActivatedRitual)this.upkeepRituals.get(i)).getLocationTag());
         }

         nbtTag.func_74773_a("Rituals", ritualIDs);
         nbtTag.func_74773_a("RitualStages", stages);
         nbtTag.func_74782_a("Initiators", nbtList);
         nbtTag.func_74782_a("Locations", nbtLocationList);
         nbtTag.func_74773_a("RitualCovens", covenSizes);
      }

      public void func_145839_a(NBTTagCompound nbtTag) {
         super.func_145839_a(nbtTag);
         if (nbtTag.func_74764_b("Rituals") && nbtTag.func_74764_b("RitualStages")) {
            byte[] stages = nbtTag.func_74770_j("RitualStages");
            byte[] ritualIDs = nbtTag.func_74770_j("Rituals");
            Coord[] locations = new Coord[stages.length];
            if (nbtTag.func_74764_b("Locations")) {
               NBTTagList list = nbtTag.func_150295_c("Locations", 10);

               for(int i = 0; i < Math.min(list.func_74745_c(), locations.length); ++i) {
                  NBTTagCompound nbtListItem = list.func_150305_b(i);
                  locations[i] = Coord.fromTagNBT(nbtListItem);
               }
            }

            String[] initators = new String[stages.length];
            int i;
            if (nbtTag.func_74764_b("Initiators")) {
               NBTTagList list = nbtTag.func_150295_c("Initiators", 8);

               for(i = 0; i < Math.min(list.func_74745_c(), initators.length); ++i) {
                  String nbtListItem = list.func_150307_f(i);
                  initators[i] = nbtListItem != null && !nbtListItem.isEmpty() ? nbtListItem : null;
               }
            }

            byte[] covenSizes = nbtTag.func_74764_b("RitualCovens") ? nbtTag.func_74770_j("RitualCovens") : null;

            for(i = 0; i < ritualIDs.length; ++i) {
               RiteRegistry.Ritual ritual = RiteRegistry.instance().getRitual(ritualIDs[i]);
               if (ritual != null) {
                  ArrayList<RitualStep> ritualSteps = new ArrayList();
                  ritual.addRiteSteps(ritualSteps, stages[i]);
                  if (!ritualSteps.isEmpty()) {
                     BlockCircle.TileEntityCircle.ActivatedRitual activatedRitual = new BlockCircle.TileEntityCircle.ActivatedRitual(ritual, ritualSteps, initators[i], covenSizes != null ? covenSizes[i] : 0);
                     activatedRitual.setLocation(locations[i]);
                     this.upkeepRituals.add(activatedRitual);
                  }
               }
            }
         }

      }

      public void func_145845_h() {
         super.func_145845_h();
         if (!this.field_145850_b.field_72995_K) {
            if (!this.upkeepRituals.isEmpty()) {
               Iterator i$ = this.upkeepRituals.iterator();

               while(i$.hasNext()) {
                  BlockCircle.TileEntityCircle.ActivatedRitual upkeepRitual = (BlockCircle.TileEntityCircle.ActivatedRitual)i$.next();
                  RitualStep.Result result = ((RitualStep)upkeepRitual.steps.get(0)).run(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, this.ticks, upkeepRitual);
                  if (result != RitualStep.Result.UPKEEP && Config.instance().traceRites()) {
                     Log.instance().traceRite(String.format(" - Upkeep ritual=%s, step=%s, result=%s", upkeepRitual.ritual.getUnlocalizedName(), ((RitualStep)upkeepRitual.steps.get(0)).toString(), result.toString()));
                  }

                  switch(result) {
                  case COMPLETED:
                     upkeepRitual.steps.clear();
                     break;
                  case ABORTED:
                  case ABORTED_REFUND:
                     upkeepRitual.steps.clear();
                     SoundEffect.NOTE_SNARE.playAt((TileEntity)this);
                  }
               }

               for(int i = this.upkeepRituals.size() - 1; i >= 0; --i) {
                  if (((BlockCircle.TileEntityCircle.ActivatedRitual)this.upkeepRituals.get(i)).steps.isEmpty()) {
                     this.upkeepRituals.remove(i);
                  }
               }
            }

            if (!this.activeRituals.isEmpty()) {
               BlockCircle.TileEntityCircle.ActivatedRitual ritual = (BlockCircle.TileEntityCircle.ActivatedRitual)this.activeRituals.get(0);
               RitualStep.Result result = ((RitualStep)ritual.steps.get(0)).run(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, this.ticks, ritual);
               ritual.postProcess(this.field_145850_b);
               if (this.abortNext) {
                  this.abortNext = false;
                  result = RitualStep.Result.ABORTED_REFUND;
                  this.activeRituals.clear();
               }

               if (result != RitualStep.Result.STARTING && Config.instance().traceRites()) {
                  Log.instance().traceRite(String.format("Active ritual=%s, step=%s, result=%s", ritual.ritual.getUnlocalizedName(), ((RitualStep)ritual.steps.get(0)).toString(), result.toString()));
               }

               label74:
               switch(result) {
               case COMPLETED:
                  if (ritual.steps.size() > 0) {
                     ritual.steps.remove(0);
                  }

                  if (ritual.steps.isEmpty()) {
                     this.activeRituals.remove(0);
                  }
                  break;
               case ABORTED:
               case ABORTED_REFUND:
                  if (this.activeRituals.size() > 0) {
                     this.activeRituals.remove(0);
                  }

                  if (this.field_145850_b.field_72995_K) {
                     break;
                  }

                  SoundEffect.NOTE_SNARE.playAt((TileEntity)this);
                  if (result != RitualStep.Result.ABORTED_REFUND) {
                     break;
                  }

                  Iterator i$ = ritual.sacrificedItems.iterator();

                  while(true) {
                     if (!i$.hasNext()) {
                        break label74;
                     }

                     RitualStep.SacrificedItem sacrificedItem = (RitualStep.SacrificedItem)i$.next();
                     this.field_145850_b.func_72838_d(new EntityItem(this.field_145850_b, 0.5D + (double)sacrificedItem.location.x, 0.5D + (double)sacrificedItem.location.y, 0.5D + (double)sacrificedItem.location.z, sacrificedItem.itemstack));
                  }
               case UPKEEP:
                  if (this.activeRituals.size() > 0) {
                     this.activeRituals.remove(0);
                  }

                  this.upkeepRituals.add(ritual);
               case STARTING:
               }
            }

            if (!this.isRitualActive() && this.func_145832_p() != 0) {
               this.field_145850_b.func_72921_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, 0, 3);
            }
         }

      }

      public void deactivate() {
         if (!this.field_145850_b.field_72995_K) {
            if (this.activeRituals.size() > 0) {
               this.abortNext = true;
            }

            this.upkeepRituals.clear();
            this.field_145850_b.func_72921_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, 0, 3);
            SoundEffect.NOTE_SNARE.playAt((TileEntity)this);
         }

      }

      public boolean isRitualActive() {
         return !this.activeRituals.isEmpty() || !this.upkeepRituals.isEmpty();
      }

      public void queueRitual(RiteRegistry.Ritual ritual, AxisAlignedBB bounds, EntityPlayer player, int covenSize, boolean summonCoven) {
         ArrayList<RitualStep> ritualSteps = new ArrayList();
         if (summonCoven) {
            EntityCovenWitch.summonCoven(ritualSteps, player.field_70170_p, player, new int[][]{{this.field_145851_c - 2, this.field_145848_d, this.field_145849_e - 2}, {this.field_145851_c + 2, this.field_145848_d, this.field_145849_e - 2}, {this.field_145851_c - 2, this.field_145848_d, this.field_145849_e + 2}, {this.field_145851_c + 2, this.field_145848_d, this.field_145849_e + 2}, {this.field_145851_c, this.field_145848_d, this.field_145849_e + 3}, {this.field_145851_c, this.field_145848_d, this.field_145849_e - 3}});
         }

         ritual.addSteps(ritualSteps, bounds);
         if (ritualSteps.size() > 0 && !this.field_145850_b.field_72995_K) {
            this.activeRituals.add(new BlockCircle.TileEntityCircle.ActivatedRitual(ritual, ritualSteps, player != null ? player.func_70005_c_() : null, covenSize));
            this.field_145850_b.func_72921_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, 1, 3);
         }

      }

      public Packet func_145844_m() {
         NBTTagCompound nbtTag = new NBTTagCompound();
         this.func_145841_b(nbtTag);
         return new S35PacketUpdateTileEntity(this.field_145851_c, this.field_145848_d, this.field_145849_e, 1, nbtTag);
      }

      public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
         super.onDataPacket(net, packet);
         this.func_145839_a(packet.func_148857_g());
         this.field_145850_b.func_147479_m(this.field_145851_c, this.field_145848_d, this.field_145849_e);
      }

      public static class ActivatedRitual {
         private final RiteRegistry.Ritual ritual;
         private final ArrayList<RitualStep> steps;
         public final String playerName;
         public final ArrayList<RitualStep.SacrificedItem> sacrificedItems;
         public final int covenSize;
         private Coord coord;

         private ActivatedRitual(RiteRegistry.Ritual ritual, ArrayList<RitualStep> steps, String playerName, int covenSize) {
            this.sacrificedItems = new ArrayList();
            this.ritual = ritual;
            this.steps = steps;
            this.playerName = playerName;
            this.covenSize = covenSize;
         }

         public Coord getLocation() {
            return this.coord;
         }

         public NBTTagCompound getLocationTag() {
            return this.coord != null ? this.coord.toTagNBT() : new NBTTagCompound();
         }

         public void setLocation(Coord coord) {
            this.coord = coord;
         }

         public String getInitiatingPlayerName() {
            return this.playerName != null ? this.playerName : "";
         }

         public EntityPlayer getInitiatingPlayer(World world) {
            return world.func_72924_a(this.getInitiatingPlayerName());
         }

         public void postProcess(World world) {
            for(int i = 0; i < this.sacrificedItems.size(); ++i) {
               RitualStep.SacrificedItem sacrificedItem = (RitualStep.SacrificedItem)this.sacrificedItems.get(i);
               if (sacrificedItem != null && sacrificedItem.itemstack != null) {
                  if (!this.ritual.isConsumeAttunedStoneCharged() && Witchery.Items.GENERIC.itemAttunedStoneCharged.isMatch(sacrificedItem.itemstack)) {
                     world.func_72838_d(new EntityItem(world, 0.5D + (double)sacrificedItem.location.x, 0.5D + (double)sacrificedItem.location.y, 0.5D + (double)sacrificedItem.location.z, Witchery.Items.GENERIC.itemAttunedStone.createStack()));
                     this.sacrificedItems.remove(i);
                     break;
                  }

                  if (sacrificedItem.itemstack.func_77973_b() == Witchery.Items.ARTHANA) {
                     world.func_72838_d(new EntityItem(world, 0.5D + (double)sacrificedItem.location.x, 0.5D + (double)sacrificedItem.location.y, 0.5D + (double)sacrificedItem.location.z, sacrificedItem.itemstack));
                     this.sacrificedItems.remove(i);
                     break;
                  }

                  if (sacrificedItem.itemstack.func_77973_b() == Witchery.Items.BOLINE) {
                     world.func_72838_d(new EntityItem(world, 0.5D + (double)sacrificedItem.location.x, 0.5D + (double)sacrificedItem.location.y, 0.5D + (double)sacrificedItem.location.z, sacrificedItem.itemstack));
                     this.sacrificedItems.remove(i);
                     break;
                  }

                  if (!this.ritual.isConsumeNecroStone() && Witchery.Items.GENERIC.itemNecroStone.isMatch(sacrificedItem.itemstack)) {
                     world.func_72838_d(new EntityItem(world, 0.5D + (double)sacrificedItem.location.x, 0.5D + (double)sacrificedItem.location.y, 0.5D + (double)sacrificedItem.location.z, sacrificedItem.itemstack));
                     this.sacrificedItems.remove(i);
                     break;
                  }
               }
            }

         }

         public int getCurrentStage() {
            return !this.steps.isEmpty() ? ((RitualStep)this.steps.get(0)).getCurrentStage() : 0;
         }

         // $FF: synthetic method
         ActivatedRitual(RiteRegistry.Ritual x0, ArrayList x1, String x2, int x3, Object x4) {
            this(x0, x1, x2, x3);
         }
      }
   }
}
