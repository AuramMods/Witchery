package com.emoniph.witchery.integration;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockPitDirt;
import com.emoniph.witchery.blocks.BlockPitGrass;
import com.emoniph.witchery.blocks.BlockPlantMine;
import com.emoniph.witchery.blocks.BlockWitchDoor;
import com.emoniph.witchery.entity.EntityIllusionCreeper;
import com.emoniph.witchery.entity.EntityIllusionSpider;
import com.emoniph.witchery.entity.EntityIllusionZombie;
import com.emoniph.witchery.entity.EntityVillagerWere;
import java.util.List;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModHookWailaRegistrar implements IWailaDataProvider, IWailaEntityProvider {
   private static final ItemStack yellowPlant;
   private static final ItemStack redPlant;
   private static final ItemStack shrubPlant;
   private static final ItemStack door;
   private static final ItemStack dirt;
   private static final ItemStack grass;
   private static final ItemStack rowandoor;
   private static Entity CREEPER;
   private static Entity ZOMBIE;
   private static Entity SPIDER;
   private static EntityVillager VILLAGER;

   public static void callbackRegister(IWailaRegistrar registrar) {
      ModHookWailaRegistrar provider = new ModHookWailaRegistrar();
      registrar.registerStackProvider(provider, BlockPlantMine.class);
      registrar.registerStackProvider(provider, BlockWitchDoor.class);
      registrar.registerStackProvider(provider, BlockPitDirt.class);
      registrar.registerStackProvider(provider, BlockPitGrass.class);
      registrar.registerOverrideEntityProvider(provider, EntityIllusionCreeper.class);
      registrar.registerOverrideEntityProvider(provider, EntityIllusionSpider.class);
      registrar.registerOverrideEntityProvider(provider, EntityIllusionZombie.class);
      registrar.registerOverrideEntityProvider(provider, EntityVillagerWere.class);
   }

   public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
      if (accessor.getBlock() == Witchery.Blocks.TRAPPED_PLANT) {
         int foundMeta = accessor.getMetadata();
         if (foundMeta == 5 || foundMeta == 6 || foundMeta == 7 || foundMeta == 4) {
            return yellowPlant;
         }

         if (foundMeta == 1 || foundMeta == 2 || foundMeta == 3 || foundMeta == 0) {
            return redPlant;
         }

         if (foundMeta == 9 || foundMeta == 10 || foundMeta == 11 || foundMeta == 8) {
            return shrubPlant;
         }
      } else {
         if (accessor.getBlock() == Witchery.Blocks.DOOR_ALDER) {
            return door;
         }

         if (accessor.getBlock() == Witchery.Blocks.DOOR_ROWAN) {
            return rowandoor;
         }

         if (accessor.getBlock() == Witchery.Blocks.PIT_DIRT) {
            return dirt;
         }

         if (accessor.getBlock() == Witchery.Blocks.PIT_GRASS) {
            return grass;
         }
      }

      return null;
   }

   public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
      return currenttip;
   }

   public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
      return currenttip;
   }

   public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
      return currenttip;
   }

   public Entity getWailaOverride(IWailaEntityAccessor accessor, IWailaConfigHandler config) {
      if (accessor.getEntity() instanceof EntityIllusionCreeper) {
         if (CREEPER == null || CREEPER.field_70170_p != accessor.getWorld()) {
            CREEPER = new EntityCreeper(accessor.getWorld());
         }

         return CREEPER;
      } else if (accessor.getEntity() instanceof EntityIllusionZombie) {
         if (ZOMBIE == null || ZOMBIE.field_70170_p != accessor.getWorld()) {
            ZOMBIE = new EntityZombie(accessor.getWorld());
         }

         return ZOMBIE;
      } else if (accessor.getEntity() instanceof EntityIllusionSpider) {
         if (SPIDER == null || SPIDER.field_70170_p != accessor.getWorld()) {
            SPIDER = new EntitySpider(accessor.getWorld());
         }

         return SPIDER;
      } else if (!(accessor.getEntity() instanceof EntityVillagerWere)) {
         return null;
      } else {
         EntityVillagerWere were = (EntityVillagerWere)accessor.getEntity();
         if (VILLAGER == null || VILLAGER.field_70170_p != accessor.getWorld()) {
            VILLAGER = new EntityVillager(accessor.getWorld());
         }

         VILLAGER.func_70938_b(were.func_70946_n());
         return VILLAGER;
      }
   }

   public List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
      return currenttip;
   }

   public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
      return currenttip;
   }

   public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
      return currenttip;
   }

   static {
      yellowPlant = new ItemStack(Blocks.field_150327_N);
      redPlant = new ItemStack(Blocks.field_150328_O);
      shrubPlant = new ItemStack(Blocks.field_150330_I);
      door = new ItemStack(Items.field_151135_aq);
      dirt = new ItemStack(Blocks.field_150346_d);
      grass = new ItemStack(Blocks.field_150349_c);
      rowandoor = new ItemStack(Witchery.Blocks.DOOR_ROWAN);
   }
}
