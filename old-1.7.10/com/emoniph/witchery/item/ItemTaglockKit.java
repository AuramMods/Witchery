package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockBloodRose;
import com.emoniph.witchery.blocks.BlockCrystalBall;
import com.emoniph.witchery.blocks.BlockLeechChest;
import com.emoniph.witchery.entity.EntityEye;
import com.emoniph.witchery.entity.EntityImp;
import com.emoniph.witchery.entity.EntityTreefyd;
import com.emoniph.witchery.entity.EntityWingedMonkey;
import com.emoniph.witchery.network.PacketCamPos;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

public class ItemTaglockKit extends ItemBase {
   @SideOnly(Side.CLIENT)
   private IIcon emptyIcon;
   @SideOnly(Side.CLIENT)
   private IIcon fullIcon;
   static final String KEY_PLAYER_NAME = "WITCPlayer";
   static final String KEY_DISPLAY_NAME = "WITCDisplayName";
   static final String KEY_ENTITY_ID_MOST = "WITCEntityIDm";
   static final String KEY_ENTITY_ID_LEAST = "WITCEntityIDl";

   public ItemTaglockKit() {
      this.func_77625_d(16);
      this.func_77656_e(0);
   }

   public String func_77653_i(ItemStack itemStack) {
      String entityID = this.getBoundEntityDisplayName(itemStack, 1);
      String localizedName = super.func_77653_i(itemStack);
      return !entityID.isEmpty() ? String.format("%s (%s)", localizedName, entityID) : localizedName;
   }

   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean advTooltips) {
      super.func_77624_a(stack, player, list, advTooltips);
      String entityID = this.getBoundEntityDisplayName(stack, 1);
      if (entityID != null && !entityID.isEmpty()) {
         list.add(String.format(Witchery.resource("item.witcheryTaglockKit.boundto"), entityID));
      } else {
         list.add(Witchery.resource("item.witcheryTaglockKit.unbound"));
      }

   }

   public void func_94581_a(IIconRegister par1IconRegister) {
      this.emptyIcon = par1IconRegister.func_94245_a(this.func_111208_A());
      this.fullIcon = par1IconRegister.func_94245_a(this.func_111208_A() + ".full");
   }

   public int func_77626_a(ItemStack stack) {
      return 1200;
   }

   public void onUsingTick(ItemStack stack, EntityPlayer player, int countdown) {
      World world = player.field_70170_p;
      int elapsedTicks = this.func_77626_a(stack) - countdown;
      if (!world.field_72995_K && elapsedTicks % 20 == 0) {
         EntityLivingBase entity = this.getBoundEntity(world, player, stack, 1);
         if (entity != null && entity.field_71093_bK == player.field_71093_bK) {
            if (entity == player) {
               if (elapsedTicks == 0) {
                  EntityEye eye = new EntityEye(world);
                  eye.func_70012_b(player.field_70165_t, player.field_70163_u, player.field_70161_v, player.field_70177_z, 90.0F);
                  world.func_72838_d(eye);
                  Witchery.packetPipeline.sendTo((IMessage)(new PacketCamPos(true, elapsedTicks == 0, eye)), (EntityPlayer)player);
               } else {
                  Witchery.packetPipeline.sendTo((IMessage)(new PacketCamPos(true, false, (Entity)null)), (EntityPlayer)player);
               }
            } else {
               Witchery.packetPipeline.sendTo((IMessage)(new PacketCamPos(true, elapsedTicks == 0, entity)), (EntityPlayer)player);
            }
         } else {
            Witchery.packetPipeline.sendTo((IMessage)(new PacketCamPos(false, false, (Entity)null)), (EntityPlayer)player);
         }
      }

   }

   public EnumAction func_77661_b(ItemStack stack) {
      return EnumAction.none;
   }

   public ItemStack func_77654_b(ItemStack stack, World world, EntityPlayer player) {
      if (!world.field_72995_K) {
         Witchery.packetPipeline.sendTo((IMessage)(new PacketCamPos(false, false, (Entity)null)), (EntityPlayer)player);
      }

      return stack;
   }

   public void func_77615_a(ItemStack stack, World world, EntityPlayer player, int countdown) {
      if (!world.field_72995_K) {
         Witchery.packetPipeline.sendTo((IMessage)(new PacketCamPos(false, false, (Entity)null)), (EntityPlayer)player);
      }

   }

   @SideOnly(Side.CLIENT)
   public IIcon func_77617_a(int damageValue) {
      return damageValue == 1 ? this.fullIcon : this.emptyIcon;
   }

   public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
      Block block = world.func_147439_a(x, y, z);
      if (block != Blocks.field_150324_C && block != Witchery.Blocks.COFFIN && !block.func_149739_a().equals("tile.blockCarpentersBed") && !block.isBed(world, x, y, z, player)) {
         TileEntity tile;
         String username;
         if (block == Witchery.Blocks.LEECH_CHEST) {
            if (!world.field_72995_K) {
               tile = world.func_147438_o(x, y, z);
               if (tile != null && tile instanceof BlockLeechChest.TileEntityLeechChest) {
                  BlockLeechChest.TileEntityLeechChest chest = (BlockLeechChest.TileEntityLeechChest)tile;
                  username = chest.popUserExcept(player);
                  if (username != null && !username.isEmpty()) {
                     this.setTaglockForEntity(world, player, itemstack, username);
                     return !world.field_72995_K;
                  }
               }

               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }

            return !world.field_72995_K;
         } else if (block == Witchery.Blocks.BLOOD_ROSE) {
            if (!world.field_72995_K) {
               tile = world.func_147438_o(x, y, z);
               if (tile != null && tile instanceof BlockBloodRose.TileEntityBloodRose) {
                  BlockBloodRose.TileEntityBloodRose chest = (BlockBloodRose.TileEntityBloodRose)tile;
                  username = chest.popUserExcept(player, false);
                  if (username != null && !username.isEmpty()) {
                     this.setTaglockForEntity(world, player, itemstack, username);
                     return !world.field_72995_K;
                  }
               }

               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }

            return !world.field_72995_K;
         } else if (block != Witchery.Blocks.CRYSTAL_BALL) {
            return super.onItemUseFirst(itemstack, player, world, x, y, z, side, hitX, hitY, hitZ);
         } else {
            if (itemstack.func_77960_j() > 0) {
               if (!world.field_72995_K && BlockCrystalBall.tryConsumePower(world, player, x, y, z)) {
                  player.func_71008_a(itemstack, this.func_77626_a(itemstack));
               } else if (world.field_72995_K) {
                  player.func_71008_a(itemstack, this.func_77626_a(itemstack));
               }
            } else {
               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }

            return !world.field_72995_K;
         }
      } else {
         int i1 = world.func_72805_g(x, y, z);
         Log.instance().debug(String.format("Using taglock on bed [%s] meta %d", block.func_149739_a(), i1));
         if (block == Blocks.field_150324_C && !BlockBed.func_149975_b(i1)) {
            int j1 = BlockBed.func_149895_l(i1);
            x += BlockBed.field_149981_a[j1][0];
            z += BlockBed.field_149981_a[j1][1];
            if (world.func_147439_a(x, y, z) != Blocks.field_150324_C) {
               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
               return !world.field_72995_K;
            }
         }

         ChunkCoordinates clickedBedLocation = new ChunkCoordinates(x, y, z);
         if (player.func_70093_af()) {
            if (!world.field_72995_K) {
               this.setTaglockForEntity(world, player, itemstack, player);
            }

            return !world.field_72995_K;
         } else {
            if (!world.field_72995_K) {
               boolean taglockSaved = this.tryBindTaglockFromBed(itemstack, player, world, clickedBedLocation);
               if (!taglockSaved && block != Blocks.field_150324_C) {
                  if (world.func_147439_a(x + 1, y, z) == block) {
                     taglockSaved = this.tryBindTaglockFromBed(itemstack, player, world, new ChunkCoordinates(x + 1, y, z));
                  }

                  if (!taglockSaved && world.func_147439_a(x, y, z + 1) == block) {
                     taglockSaved = this.tryBindTaglockFromBed(itemstack, player, world, new ChunkCoordinates(x, y, z + 1));
                  }

                  if (!taglockSaved && world.func_147439_a(x - 1, y, z) == block) {
                     taglockSaved = this.tryBindTaglockFromBed(itemstack, player, world, new ChunkCoordinates(x - 1, y, z));
                  }

                  if (!taglockSaved && world.func_147439_a(x, y, z - 1) == block) {
                     taglockSaved = this.tryBindTaglockFromBed(itemstack, player, world, new ChunkCoordinates(x, y, z - 1));
                  }
               }

               if (taglockSaved) {
                  return !world.field_72995_K;
               }
            }

            if (!world.field_72995_K) {
               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }

            return !world.field_72995_K;
         }
      }
   }

   private boolean tryBindTaglockFromBed(ItemStack itemstack, EntityPlayer player, World world, ChunkCoordinates clickedBedLocation) {
      String boundName = "";
      EntityLivingBase boundEntity = this.getBoundEntity(world, player, itemstack, 1);
      if (boundEntity != null && boundEntity instanceof EntityPlayer) {
         boundName = ((EntityPlayer)boundEntity).func_70005_c_();
      }

      ArrayList<EntityPlayer> players = new ArrayList();
      Iterator i$ = world.field_73010_i.iterator();

      while(i$.hasNext()) {
         Object obj = i$.next();
         EntityPlayer worldPlayer = (EntityPlayer)obj;
         ChunkCoordinates playerBedLocation = worldPlayer.getBedLocation(player.field_71093_bK);
         if (playerBedLocation != null && playerBedLocation.equals(clickedBedLocation)) {
            players.add(worldPlayer);
         }
      }

      Collections.sort(players, new ItemTaglockKit.PlayerComparitor());
      boolean taglockSaved = false;
      if (players.size() > 0) {
         if (boundName.isEmpty()) {
            taglockSaved = this.setTaglockForEntity(world, player, itemstack, (EntityPlayer)players.get(0));
         } else {
            boolean found = false;

            for(int i = 0; i < players.size() && !found; ++i) {
               if (((EntityPlayer)players.get(i)).func_70005_c_().equals(boundName)) {
                  if (i == players.size() - 1) {
                     taglockSaved = this.setTaglockForEntity(world, player, itemstack, (EntityPlayer)players.get(0));
                  } else {
                     taglockSaved = this.setTaglockForEntity(world, player, itemstack, (EntityPlayer)players.get(i + 1));
                  }

                  found = true;
               }
            }

            if (!found) {
               taglockSaved = this.setTaglockForEntity(world, player, itemstack, (EntityPlayer)players.get(0));
            }
         }
      }

      return taglockSaved;
   }

   public static boolean isTaglockRestricted(EntityPlayer collector, EntityLivingBase target) {
      if (target instanceof EntityPlayer && !collector.equals(target)) {
         if (Config.instance().restrictTaglockCollectionOnNonPVPServers && !MinecraftServer.func_71276_C().func_71219_W()) {
            return true;
         } else {
            EntityPlayer targetPlayer = (EntityPlayer)target;
            return Config.instance().restrictTaglockCollectionForStaffMembers && MinecraftServer.func_71276_C().func_71203_ab().func_152596_g(targetPlayer.func_146103_bH());
         }
      } else {
         return false;
      }
   }

   private boolean setTaglockForEntity(World world, EntityPlayer player, ItemStack itemstack, EntityPlayer victim) {
      if (!isTaglockRestricted(player, victim)) {
         this.setTaglockForEntity(world, player, itemstack, victim.func_70005_c_());
         return true;
      } else {
         return false;
      }
   }

   private void setTaglockForEntity(World world, EntityPlayer player, ItemStack itemstack, String victimUsername) {
      if (!world.field_72995_K) {
         if (itemstack.field_77994_a > 1) {
            ItemStack newStack = new ItemStack(this, 1, 1);
            this.setTaglockForEntity(newStack, player, victimUsername, true, 1);
            --itemstack.field_77994_a;
            if (itemstack.field_77994_a <= 0) {
               player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
            }

            if (!player.field_71071_by.func_70441_a(newStack)) {
               world.func_72838_d(new EntityItem(world, player.field_70165_t + 0.5D, player.field_70163_u + 1.5D, player.field_70161_v + 0.5D, newStack));
            } else if (player instanceof EntityPlayerMP) {
               ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
            }
         } else {
            this.setTaglockForEntity(itemstack, player, victimUsername, true, 1);
            itemstack.func_77964_b(1);
            if (player instanceof EntityPlayerMP) {
               ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
            }
         }
      }

   }

   public void onEntityInteract(World world, EntityPlayer player, ItemStack stack, EntityInteractEvent event) {
      if (!world.field_72995_K && stack != null && stack.func_77973_b() == Witchery.Items.TAGLOCK_KIT && event.target != null && event.target instanceof EntityLivingBase) {
         EntityLivingBase target = (EntityLivingBase)event.target;
         if (target instanceof EntityPlayer && !this.isSneakSuccessful(player, target)) {
            ChatUtil.sendTranslated(EnumChatFormatting.RED, event.entityPlayer, "witchery.taglockkit.taglockfailed");
            if (target instanceof EntityPlayer) {
               ChatUtil.sendTranslated(EnumChatFormatting.GREEN, (EntityPlayer)target, "witchery.taglockkit.taglockdiscovered");
            }
         } else {
            if (target instanceof EntityTreefyd || target instanceof EntityImp || target instanceof EntityWingedMonkey && !player.func_70093_af()) {
               return;
            }

            if (stack.field_77994_a > 1) {
               ItemStack newStack = new ItemStack(Witchery.Items.TAGLOCK_KIT, 1, 1);
               Witchery.Items.TAGLOCK_KIT.setTaglockForEntity(newStack, player, (Entity)target, true, 1);
               --stack.field_77994_a;
               if (stack.field_77994_a <= 0) {
                  player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
               }

               if (!player.field_71071_by.func_70441_a(newStack)) {
                  world.func_72838_d(new EntityItem(world, player.field_70165_t + 0.5D, player.field_70163_u + 1.5D, player.field_70161_v + 0.5D, newStack));
               } else if (player instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
               }
            } else {
               Witchery.Items.TAGLOCK_KIT.setTaglockForEntity(stack, player, (Entity)target, true, 1);
               stack.func_77964_b(1);
               if (player instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
               }
            }
         }

         event.setCanceled(true);
      }

   }

   private boolean isSneakSuccessful(EntityPlayer sneaker, EntityLivingBase target) {
      if (isTaglockRestricted(sneaker, target)) {
         return false;
      } else {
         double sneakerFacing = (double)((sneaker.field_70759_as + 90.0F) % 360.0F);
         if (sneakerFacing < 0.0D) {
            sneakerFacing += 360.0D;
         }

         double targetFacing = (double)((target.field_70759_as + 90.0F) % 360.0F);
         if (targetFacing < 0.0D) {
            targetFacing += 360.0D;
         }

         double ARC = 45.0D;
         double diff = Math.abs(targetFacing - sneakerFacing);
         double chance = 0.0D;
         if (!(360.0D - diff % 360.0D < 45.0D) && !(diff % 360.0D < 45.0D)) {
            chance = sneaker.func_70093_af() ? 0.1D : 0.01D;
            if (sneaker.func_82150_aj()) {
               chance += 0.1D;
            }
         } else {
            chance = sneaker.func_70093_af() ? 0.6D : 0.3D;
         }

         return sneaker.field_70170_p.field_73012_v.nextDouble() < chance;
      }
   }

   public void setTaglockForEntity(ItemStack stack, EntityPlayer player, Entity entity, boolean playSoundAtPlayer, Integer index) {
      if (!stack.func_77942_o()) {
         stack.func_77982_d(new NBTTagCompound());
      }

      if (entity instanceof EntityPlayer) {
         EntityPlayer hitPlayer = (EntityPlayer)entity;
         stack.func_77978_p().func_74778_a("WITCPlayer" + index.toString(), hitPlayer.func_70005_c_());
         stack.func_77978_p().func_82580_o("WITCEntityIDm" + index.toString());
         stack.func_77978_p().func_82580_o("WITCEntityIDl" + index.toString());
      } else {
         if (!(entity instanceof EntityLiving)) {
            return;
         }

         stack.func_77978_p().func_82580_o("WITCPlayer" + index.toString());
         UUID id = entity.getPersistentID();
         ((EntityLiving)entity).func_110163_bv();
         stack.func_77978_p().func_74772_a("WITCEntityIDm" + index.toString(), id.getMostSignificantBits());
         stack.func_77978_p().func_74772_a("WITCEntityIDl" + index.toString(), id.getLeastSignificantBits());
         stack.func_77978_p().func_74778_a("WITCDisplayName" + index.toString(), entity.func_70005_c_());
      }

      if (playSoundAtPlayer) {
         player.field_70170_p.func_72956_a(player, "random.orb", 0.5F, 0.4F / ((float)player.field_70170_p.field_73012_v.nextDouble() * 0.4F + 0.8F));
      }

   }

   public void clearTaglock(ItemStack stack, Integer index) {
      if (stack != null) {
         stack.func_77978_p().func_82580_o("WITCEntityIDm" + index.toString());
         stack.func_77978_p().func_82580_o("WITCEntityIDl" + index.toString());
         stack.func_77978_p().func_82580_o("WITCPlayer" + index.toString());
         stack.func_77978_p().func_82580_o("WITCDisplayName" + index.toString());
      }

   }

   public void setTaglockForEntity(ItemStack stack, EntityPlayer player, String username, boolean playSoundAtPlayer, Integer index) {
      if (!stack.func_77942_o()) {
         stack.func_77982_d(new NBTTagCompound());
      }

      if (username != null && !username.isEmpty()) {
         stack.func_77978_p().func_74778_a("WITCPlayer" + index.toString(), username);
         stack.func_77978_p().func_82580_o("WITCEntityIDm" + index.toString());
         stack.func_77978_p().func_82580_o("WITCEntityIDl" + index.toString());
         if (playSoundAtPlayer) {
            player.field_70170_p.func_72956_a(player, "random.orb", 0.5F, 0.4F / ((float)player.field_70170_p.field_73012_v.nextDouble() * 0.4F + 0.8F));
         }

      }
   }

   public boolean isTaglockPresent(ItemStack itemStack, Integer index) {
      if (itemStack.func_77942_o()) {
         if (itemStack.func_77978_p().func_74764_b("WITCPlayer" + index.toString())) {
            String playerName = itemStack.func_77978_p().func_74779_i("WITCPlayer" + index.toString());
            if (playerName != null && !playerName.isEmpty()) {
               return true;
            }
         }

         if (itemStack.func_77978_p().func_74764_b("WITCEntityIDm" + index.toString()) && itemStack.func_77978_p().func_74764_b("WITCEntityIDl" + index.toString())) {
            return true;
         }
      }

      return false;
   }

   public String getBoundEntityDisplayName(ItemStack itemStack, Integer index) {
      if (itemStack.func_77942_o()) {
         String displayName;
         if (itemStack.func_77978_p().func_74764_b("WITCPlayer" + index.toString())) {
            displayName = itemStack.func_77978_p().func_74779_i("WITCPlayer" + index.toString());
            return displayName;
         }

         if (itemStack.func_77978_p().func_74764_b("WITCEntityIDm" + index.toString()) && itemStack.func_77978_p().func_74764_b("WITCEntityIDl" + index.toString()) && itemStack.func_77978_p().func_74764_b("WITCDisplayName" + index.toString())) {
            displayName = itemStack.func_77978_p().func_74779_i("WITCDisplayName" + index.toString());
            return displayName;
         }
      }

      return "";
   }

   public ItemTaglockKit.BoundType getBoundEntityType(ItemStack itemStack, Integer index) {
      if (itemStack.func_77942_o()) {
         if (itemStack.func_77978_p().func_74764_b("WITCPlayer" + index.toString())) {
            return ItemTaglockKit.BoundType.PLAYER;
         }

         if (itemStack.func_77978_p().func_74764_b("WITCEntityIDm" + index.toString()) && itemStack.func_77978_p().func_74764_b("WITCEntityIDl" + index.toString()) && itemStack.func_77978_p().func_74764_b("WITCDisplayName" + index.toString())) {
            ItemTaglockKit.BoundType var10000 = ItemTaglockKit.BoundType.PLAYER;
            return ItemTaglockKit.BoundType.CREATURE;
         }
      }

      return ItemTaglockKit.BoundType.NONE;
   }

   public String getBoundUsername(ItemStack itemStack, Integer index) {
      if (itemStack.func_77942_o() && itemStack.func_77978_p().func_74764_b("WITCPlayer" + index.toString())) {
         String playerName = itemStack.func_77978_p().func_74779_i("WITCPlayer" + index.toString());
         return playerName;
      } else {
         return "";
      }
   }

   public UUID getBoundCreatureID(ItemStack itemStack, Integer index) {
      if (itemStack.func_77942_o() && itemStack.func_77978_p().func_74764_b("WITCEntityIDm" + index.toString()) && itemStack.func_77978_p().func_74764_b("WITCEntityIDl" + index.toString()) && itemStack.func_77978_p().func_74764_b("WITCDisplayName" + index.toString())) {
         String displayName = itemStack.func_77978_p().func_74779_i("WITCDisplayName" + index.toString());
         UUID entityID = new UUID(itemStack.func_77978_p().func_74763_f("WITCEntityIDm" + index.toString()), itemStack.func_77978_p().func_74763_f("WITCEntityIDl" + index.toString()));
         return entityID;
      } else {
         return new UUID(0L, 0L);
      }
   }

   public void addTagLockToPoppet(ItemStack stackTaglockKit, ItemStack stackPoppet, Integer index) {
      assert stackTaglockKit != null : "stack of taglock kit cannot be null";

      assert stackPoppet != null : "stack poppet cannot be null";

      Integer tagLockIndex = 1;
      if (!stackPoppet.func_77942_o()) {
         stackPoppet.func_77982_d(new NBTTagCompound());
      }

      if (stackTaglockKit.func_77942_o()) {
         if (stackTaglockKit.func_77978_p().func_74764_b("WITCPlayer" + tagLockIndex.toString())) {
            String playerName = stackTaglockKit.func_77978_p().func_74779_i("WITCPlayer" + tagLockIndex.toString());
            if (playerName != null) {
               stackPoppet.func_77978_p().func_74778_a("WITCPlayer" + index.toString(), playerName);
            }
         } else if (stackTaglockKit.func_77978_p().func_74764_b("WITCEntityIDm" + tagLockIndex.toString()) && stackTaglockKit.func_77978_p().func_74764_b("WITCEntityIDl" + tagLockIndex.toString()) && stackTaglockKit.func_77978_p().func_74764_b("WITCDisplayName" + tagLockIndex.toString())) {
            stackPoppet.func_77978_p().func_74772_a("WITCEntityIDm" + index.toString(), stackTaglockKit.func_77978_p().func_74763_f("WITCEntityIDm" + tagLockIndex.toString()));
            stackPoppet.func_77978_p().func_74772_a("WITCEntityIDl" + index.toString(), stackTaglockKit.func_77978_p().func_74763_f("WITCEntityIDl" + tagLockIndex.toString()));
            stackPoppet.func_77978_p().func_74778_a("WITCDisplayName" + index.toString(), stackTaglockKit.func_77978_p().func_74779_i("WITCDisplayName" + tagLockIndex.toString()));
         }
      }

   }

   public boolean containsTaglockForEntity(ItemStack itemStack, Entity entity, Integer index) {
      if (itemStack.func_77942_o()) {
         if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entity;
            if (itemStack.func_77978_p().func_74764_b("WITCPlayer" + index.toString())) {
               String playerName = itemStack.func_77978_p().func_74779_i("WITCPlayer" + index.toString());
               if (playerName != null && playerName.equals(player.func_70005_c_())) {
                  return true;
               }
            }
         } else if (entity instanceof EntityLiving && itemStack.func_77978_p().func_74764_b("WITCEntityIDm" + index.toString()) && itemStack.func_77978_p().func_74764_b("WITCEntityIDl" + index.toString())) {
            UUID entityID = new UUID(itemStack.func_77978_p().func_74763_f("WITCEntityIDm" + index.toString()), itemStack.func_77978_p().func_74763_f("WITCEntityIDl" + index.toString()));
            if (entityID.equals(entity.getPersistentID())) {
               return true;
            }
         }
      }

      return false;
   }

   public EntityLivingBase getBoundEntity(World world, Entity entity, ItemStack stack, Integer index) {
      if (stack.func_77942_o()) {
         MinecraftServer server;
         WorldServer[] arr$;
         int len$;
         int i$;
         WorldServer worldServer;
         Iterator i$;
         Object obj;
         Iterator i$;
         Object obj;
         if (stack.func_77978_p().func_74764_b("WITCPlayer" + index.toString())) {
            String playerName = stack.func_77978_p().func_74779_i("WITCPlayer" + index.toString());
            if (playerName != null && !playerName.isEmpty()) {
               if (!world.field_72995_K) {
                  server = MinecraftServer.func_71276_C();
                  arr$ = server.field_71305_c;
                  len$ = arr$.length;

                  for(i$ = 0; i$ < len$; ++i$) {
                     worldServer = arr$[i$];
                     i$ = worldServer.field_73010_i.iterator();

                     while(i$.hasNext()) {
                        obj = i$.next();
                        EntityPlayer player = (EntityPlayer)obj;
                        if (player.func_70005_c_().equalsIgnoreCase(playerName)) {
                           return player;
                        }
                     }
                  }
               } else {
                  i$ = world.field_73010_i.iterator();

                  while(i$.hasNext()) {
                     obj = i$.next();
                     EntityPlayer player = (EntityPlayer)obj;
                     if (player.func_70005_c_().equalsIgnoreCase(playerName)) {
                        return player;
                     }
                  }
               }

               return null;
            }
         }

         if (stack.func_77978_p().func_74764_b("WITCEntityIDm" + index.toString()) && stack.func_77978_p().func_74764_b("WITCEntityIDl" + index.toString())) {
            UUID entityID = new UUID(stack.func_77978_p().func_74763_f("WITCEntityIDm" + index.toString()), stack.func_77978_p().func_74763_f("WITCEntityIDl" + index.toString()));
            if (!world.field_72995_K) {
               server = MinecraftServer.func_71276_C();
               arr$ = server.field_71305_c;
               len$ = arr$.length;

               for(i$ = 0; i$ < len$; ++i$) {
                  worldServer = arr$[i$];
                  i$ = worldServer.field_72996_f.iterator();

                  while(i$.hasNext()) {
                     obj = i$.next();
                     if (obj instanceof EntityLivingBase) {
                        EntityLivingBase living = (EntityLivingBase)obj;
                        UUID livingID = living.getPersistentID();
                        if (entityID.equals(livingID) && living.func_70089_S()) {
                           return living;
                        }
                     }
                  }
               }
            } else {
               i$ = world.field_72996_f.iterator();

               while(i$.hasNext()) {
                  obj = i$.next();
                  if (obj instanceof EntityLivingBase) {
                     EntityLivingBase living = (EntityLivingBase)obj;
                     UUID livingID = living.getPersistentID();
                     if (entityID.equals(livingID) && living.func_70089_S()) {
                        return living;
                     }
                  }
               }
            }

            return null;
         }
      }

      return null;
   }

   public static enum BoundType {
      NONE,
      PLAYER,
      CREATURE;
   }

   public static class PlayerComparitor implements Comparator<EntityPlayer> {
      public int compare(EntityPlayer p1, EntityPlayer p2) {
         return p1.func_70005_c_().compareTo(p2.func_70005_c_());
      }
   }
}
