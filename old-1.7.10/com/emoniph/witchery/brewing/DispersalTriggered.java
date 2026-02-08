package com.emoniph.witchery.brewing;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.util.BlockProtect;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.EntityPosition;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

public class DispersalTriggered extends Dispersal {
   public void onImpactSplashPotion(World world, NBTTagCompound nbtBrew, MovingObjectPosition mop, ModifiersImpact modifiers) {
      Coord coord = new Coord(mop, modifiers.impactPosition, true);
      boolean replaceable = BlockProtect.checkModsForBreakOK(world, coord.x, coord.y, coord.z, modifiers.thrower);
      if (replaceable) {
         Block block = coord.getBlock(world);
         if (block == Blocks.field_150430_aB) {
            Witchery.Blocks.CURSED_BUTTON_STONE.replaceButton(world, coord.x, coord.y, coord.z, modifiers, nbtBrew);
            return;
         }

         if (block == Blocks.field_150471_bO) {
            Witchery.Blocks.CURSED_BUTTON_WOOD.replaceButton(world, coord.x, coord.y, coord.z, modifiers, nbtBrew);
            return;
         }

         if (block == Blocks.field_150442_at) {
            Witchery.Blocks.CURSED_LEVER.replaceButton(world, coord.x, coord.y, coord.z, modifiers, nbtBrew);
            return;
         }

         if (block.hasTileEntity(coord.getBlockMetadata(world))) {
            TileEntityCursedBlock tile = (TileEntityCursedBlock)BlockUtil.getTileEntity(world, coord.x, coord.y, coord.z, TileEntityCursedBlock.class);
            if (tile != null) {
               tile.updateCurse(modifiers, nbtBrew);
            }
         }

         coord = new Coord(mop, modifiers.impactPosition, false);
         block = coord.getBlock(world);
         if (block == Blocks.field_150452_aw) {
            Witchery.Blocks.CURSED_WOODEN_PRESSURE_PLATE.replaceButton(world, coord.x, coord.y, coord.z, modifiers, nbtBrew);
         } else if (block == Blocks.field_150456_au) {
            Witchery.Blocks.CURSED_STONE_PRESSURE_PLATE.replaceButton(world, coord.x, coord.y, coord.z, modifiers, nbtBrew);
         } else if (block == Witchery.Blocks.SNOW_PRESSURE_PLATE) {
            Witchery.Blocks.CURSED_SNOW_PRESSURE_PLATE.replaceButton(world, coord.x, coord.y, coord.z, modifiers, nbtBrew);
         } else if (block == Blocks.field_150466_ao) {
            Witchery.Blocks.CURSED_WOODEN_DOOR.replaceButton(world, coord.x, coord.y, coord.z, modifiers, nbtBrew);
         } else if (block.hasTileEntity(coord.getBlockMetadata(world))) {
            int y = coord.y;
            if (block == Witchery.Blocks.CURSED_WOODEN_DOOR) {
               int i1 = ((BlockDoor)Blocks.field_150466_ao).func_150012_g(world, coord.x, coord.y, coord.z);
               if ((i1 & 8) != 0) {
                  --y;
               }
            }

            TileEntityCursedBlock tile = (TileEntityCursedBlock)BlockUtil.getTileEntity(world, coord.x, y, coord.z, TileEntityCursedBlock.class);
            if (tile != null) {
               tile.updateCurse(modifiers, nbtBrew);
            }
         }
      }

   }

   public String getUnlocalizedName() {
      return "witchery:brew.dispersal.triggered";
   }

   public RitualStatus onUpdateRitual(World world, int x, int y, int z, NBTTagCompound nbtBrew, ModifiersRitual modifiers, ModifiersImpact impactModifiers) {
      AxisAlignedBB bounds = AxisAlignedBB.func_72330_a((double)x, (double)(y + 1), (double)z, (double)(x + 1), (double)(y + 2), (double)(z + 1));
      List<EntityItem> items = world.func_72872_a(EntityItem.class, bounds);
      Iterator i$ = items.iterator();

      EntityItem item;
      ItemStack stack;
      do {
         if (!i$.hasNext()) {
            return RitualStatus.FAILED;
         }

         item = (EntityItem)i$.next();
         stack = item.func_92059_d();
      } while(stack == null);

      if (stack.field_77994_a > 1) {
         stack = stack.func_77979_a(1);
         EntityItem newEntity = new EntityItem(item.field_70170_p, item.field_70165_t, item.field_70163_u, item.field_70161_v, stack);
         world.func_72838_d(newEntity);
      }

      if (!stack.func_77942_o()) {
         stack.func_77982_d(new NBTTagCompound());
      }

      NBTTagCompound nbtRoot = stack.func_77978_p();
      AxisAlignedBB playerBounds = bounds.func_72314_b(3.0D, 3.0D, 3.0D);
      List<EntityPlayer> players = world.func_72872_a(EntityPlayer.class, playerBounds);
      boolean catNear = false;
      Iterator i$ = players.iterator();

      while(i$.hasNext()) {
         EntityPlayer player = (EntityPlayer)i$.next();
         if (Familiar.hasActiveCurseMasteryFamiliar(player)) {
            catNear = true;
            break;
         }
      }

      NBTTagCompound nbtCurse;
      if (nbtRoot.func_74764_b("WITCCurse") && ((NBTTagCompound)nbtRoot.func_74775_l("WITCCurse").func_74781_a("Curse")).func_150295_c("Items", 10).equals(nbtBrew.func_150295_c("Items", 10))) {
         nbtCurse = nbtRoot.func_74775_l("WITCCurse");
         nbtCurse.func_74768_a("Count", nbtCurse.func_74762_e("Count") + (catNear ? 2 : 1));
      } else {
         nbtCurse = new NBTTagCompound();
         nbtCurse.func_74782_a("Curse", nbtBrew.func_74737_b());
         nbtCurse.func_74768_a("Count", catNear ? 2 : 1);
         nbtRoot.func_74782_a("WITCCurse", nbtCurse);
      }

      ParticleEffect.EXPLODE.send(SoundEffect.RANDOM_ORB, item, 0.5D, 0.5D, 16);
      return RitualStatus.COMPLETE;
   }

   public static class EventHooks {
      @SubscribeEvent
      public void onPlayerInteract(PlayerInteractEvent event) {
         if (!event.entityPlayer.field_70170_p.field_72995_K && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) && event.entityPlayer.func_70694_bm() != null && event.entityPlayer.func_70694_bm().func_77942_o() && event.entityPlayer.func_70694_bm().func_77978_p().func_74764_b("WITCCurse")) {
            NBTTagCompound root = event.entityPlayer.func_70694_bm().func_77978_p().func_74775_l("WITCCurse");
            NBTTagCompound tag = root.func_74775_l("Curse");
            int charges = root.func_74762_e("Count");
            long delay = root.func_74763_f("Delay");
            if (delay == 0L || MinecraftServer.func_130071_aq() > delay) {
               if (charges > 0) {
                  WitcheryBrewRegistry.INSTANCE.applyToEntity(event.entityPlayer.field_70170_p, event.entityPlayer, tag, new ModifiersEffect(1.0D, 1.0D, false, new EntityPosition(event.entityPlayer), false, 0, EntityUtil.playerOrFake(event.entityPlayer.field_70170_p, (EntityLivingBase)null)));
                  ParticleEffect.SPELL_COLORED.send(SoundEffect.RANDOM_POP, event.entityPlayer, 1.0D, 1.0D, 16);
                  --charges;
               }

               if (charges > 0) {
                  root.func_74768_a("Count", charges);
                  root.func_74772_a("Delay", MinecraftServer.func_130071_aq() + 5000L);
               } else {
                  event.entityPlayer.func_70694_bm().func_77978_p().func_82580_o("WITCCurse");
                  if (event.entityPlayer.func_70694_bm().func_77978_p().func_82582_d()) {
                     event.entityPlayer.func_70694_bm().func_77982_d((NBTTagCompound)null);
                  }
               }
            }
         }

      }
   }
}
