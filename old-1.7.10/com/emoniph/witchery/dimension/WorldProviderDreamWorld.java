package com.emoniph.witchery.dimension;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockDreamCatcher;
import com.emoniph.witchery.blocks.BlockFetish;
import com.emoniph.witchery.entity.EntityCorpse;
import com.emoniph.witchery.entity.EntityDemon;
import com.emoniph.witchery.entity.EntityNightmare;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.infusion.infusions.spirit.InfusedSpiritEffect;
import com.emoniph.witchery.item.ItemGeneral;
import com.emoniph.witchery.network.PacketPlayerStyle;
import com.emoniph.witchery.network.PacketPushTarget;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

public class WorldProviderDreamWorld extends WorldProvider {
   int nightmare = 0;
   private static final String SPIRIT_WORLD_KEY = "WITCSpiritWorld";
   private static final String SPIRIT_WORLD_WALKING_KEY = "WITCSpiritWalking";
   private static final String SPIRIT_WORLD_NIGHTMARE_KEY = "Nightmare";
   private static final String SPIRIT_WORLD_DEMONIC_KEY = "Demonic";
   private static final String SPIRIT_WORLD_OVERWORLD_BODY_KEY = "OverworldBody";
   private static final String SPIRIT_WORLD_OVERWORLD_HEALTH_KEY = "OverworldHealth";
   private static final String SPIRIT_WORLD_SPIRIT_HEALTH_KEY = "SpiritHealth";
   private static final String SPIRIT_WORLD_OVERWORLD_HUNGER_FOOD_KEY = "OverworldHunger";
   private static final String SPIRIT_WORLD_SPIRIT_HUNGER_FOOD_KEY = "SpiritHunger";
   private static final String SPIRIT_WORLD_OVERWORLD_INVENTORY_KEY = "OverworldInventory";
   private static final String SPIRIT_WORLD_SPIRIT_INVENTORY_KEY = "SpiritInventory";
   private static final String SPIRIT_WORLD_MANIFEST_GHOST_KEY = "WITCManifested";
   public static final String SPIRIT_WORLD_MANIFEST_TIME_KEY = "WITCManifestDuration";
   public static final String SPIRIT_WORLD_AWAKEN_PLAYER_KEY = "WITCForceAwaken";
   private static final String SPIRIT_WORLD_LAST_NIGHTMARE_KILL_KEY = "LastNightmareKillTime";
   public static final String SPIRIT_WORLD_MANIFEST_SKIP_TIME_TICK_KEY = "WITCManifestSkipTick";

   public void setDimension(int dim) {
      this.field_76574_g = dim;
      super.setDimension(dim);
   }

   public long getSeed() {
      Long seed = super.getSeed();
      return seed;
   }

   public IChunkProvider func_76555_c() {
      WorldProvider overworldProvider = DimensionManager.getProvider(0);
      return overworldProvider.field_76577_b.getChunkGenerator(this.field_76579_a, this.field_76579_a.func_72912_H().func_82571_y());
   }

   public void func_76572_b() {
      super.func_76572_b();
      this.field_76574_g = Config.instance().dimensionDreamID;
   }

   public String getWelcomeMessage() {
      return this instanceof WorldProviderDreamWorld ? "Entering the " + this.func_80007_l() : null;
   }

   public String getDepartMessage() {
      return this instanceof WorldProviderDreamWorld ? "Leaving the " + this.func_80007_l() : null;
   }

   public String func_80007_l() {
      return "Spirit World";
   }

   public float getStarBrightness(float par1) {
      return 0.0F;
   }

   public boolean func_76567_e() {
      return false;
   }

   public double getMovementFactor() {
      return 1.0D;
   }

   public float func_76563_a(long par1, float par3) {
      return this.nightmare > 0 ? 0.5F : 1.0F;
   }

   public float func_76571_f() {
      return 0.0F;
   }

   public boolean func_76566_a(int par1, int par2) {
      int var3 = this.field_76579_a.func_72825_h(par1, par2);
      return var3 != -1;
   }

   public ChunkCoordinates func_76554_h() {
      return new ChunkCoordinates(100, 50, 0);
   }

   public int func_76557_i() {
      return 64;
   }

   public double getHorizon() {
      return 0.0D;
   }

   @SideOnly(Side.CLIENT)
   public boolean hasVoidParticles(boolean var1) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_76561_g() {
      return true;
   }

   public double func_76565_k() {
      return 1.0D;
   }

   @SideOnly(Side.CLIENT)
   public Vec3 func_76562_b(float par1, float par2) {
      float var3 = MathHelper.func_76134_b(par1 * 3.1415927F * 2.0F) * 2.0F + 0.5F;
      if (var3 < 0.0F) {
         var3 = 0.0F;
      }

      if (var3 > 1.0F) {
         var3 = 1.0F;
      }

      float var4;
      float var5;
      float var6;
      if (this.nightmare == 0) {
         var4 = 0.8F;
         var5 = 0.2F;
         var6 = 0.6F;
      } else if (this.nightmare == 1) {
         var4 = 0.0F;
         var5 = 1.0F;
         var6 = 0.0F;
      } else {
         var4 = 1.0F;
         var5 = 0.0F;
         var6 = 0.0F;
      }

      var4 *= var3 * 0.94F + 0.06F;
      var5 *= var3 * 0.94F + 0.06F;
      var6 *= var3 * 0.91F + 0.09F;
      return Vec3.func_72443_a((double)var4, (double)var5, (double)var6);
   }

   public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful) {
      allowPeaceful = true;
   }

   public void updateWeather() {
      if (this.field_76579_a != null && this.field_76579_a.field_73012_v.nextInt(20) == 0) {
         int playerHasNightmare = 0;
         Iterator i$ = this.field_76579_a.field_73010_i.iterator();

         while(i$.hasNext()) {
            Object obj = i$.next();
            EntityPlayer player = (EntityPlayer)obj;
            int level = getPlayerHasNightmare(player);
            if (level > playerHasNightmare) {
               playerHasNightmare = level;
               break;
            }
         }

         if (this.nightmare != playerHasNightmare) {
            this.nightmare = playerHasNightmare;
         }
      }

      super.updateWeather();
   }

   public boolean isNightmare() {
      return this.nightmare > 0;
   }

   public boolean isDemonicNightmare() {
      return this.nightmare > 1;
   }

   public static int getPlayerHasNightmare(EntityPlayer player) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      return getPlayerHasNightmare(nbtPlayer);
   }

   public static int getPlayerHasNightmare(NBTTagCompound nbtPlayer) {
      if (!nbtPlayer.func_74764_b("WITCSpiritWorld")) {
         return 0;
      } else {
         NBTTagCompound nbtSpirit = nbtPlayer.func_74775_l("WITCSpiritWorld");
         boolean nightmare = nbtSpirit.func_74767_n("Nightmare");
         boolean demonic = nbtSpirit.func_74767_n("Demonic");
         return nightmare && demonic ? 2 : (nightmare ? 1 : 0);
      }
   }

   public static void setPlayerHasNightmare(EntityPlayer player, boolean nightmare, boolean demonic) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      setPlayerHasNightmare(nbtPlayer, nightmare, demonic);
   }

   public static void setPlayerHasNightmare(NBTTagCompound nbtPlayer, boolean nightmare, boolean demonic) {
      if (!nbtPlayer.func_74764_b("WITCSpiritWorld")) {
         nbtPlayer.func_74782_a("WITCSpiritWorld", new NBTTagCompound());
      }

      NBTTagCompound nbtSpirit = nbtPlayer.func_74775_l("WITCSpiritWorld");
      nbtSpirit.func_74757_a("Nightmare", nightmare);
      nbtSpirit.func_74757_a("Demonic", demonic);
   }

   public static void setPlayerLastNightmareKillNow(EntityPlayer player) {
      if (player != null) {
         NBTTagCompound nbtPlayer = Infusion.getNBT(player);
         long time = MinecraftServer.func_130071_aq();
         setPlayerLastNightmareKill(nbtPlayer, time);
      }

   }

   public static void setPlayerLastNightmareKill(NBTTagCompound nbtPlayer, long time) {
      if (!nbtPlayer.func_74764_b("WITCSpiritWorld")) {
         nbtPlayer.func_74782_a("WITCSpiritWorld", new NBTTagCompound());
      }

      NBTTagCompound nbtSpirit = nbtPlayer.func_74775_l("WITCSpiritWorld");
      nbtSpirit.func_74772_a("LastNightmareKillTime", time);
   }

   public static long getPlayerLastNightmareKill(NBTTagCompound nbtPlayer) {
      if (!nbtPlayer.func_74764_b("WITCSpiritWorld")) {
         return 0L;
      } else {
         NBTTagCompound nbtSpirit = nbtPlayer.func_74775_l("WITCSpiritWorld");
         if (!nbtSpirit.func_74764_b("LastNightmareKillTime")) {
            return 0L;
         } else {
            long time = nbtSpirit.func_74763_f("LastNightmareKillTime");
            return time;
         }
      }
   }

   public static boolean getPlayerIsSpiritWalking(EntityPlayer player) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      return getPlayerIsSpiritWalking(nbtPlayer);
   }

   public static boolean getPlayerIsSpiritWalking(NBTTagCompound nbtPlayer) {
      boolean walking = nbtPlayer.func_74767_n("WITCSpiritWalking");
      return walking;
   }

   public static void setPlayerIsSpiritWalking(EntityPlayer player, boolean walking) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      setPlayerIsSpiritWalking(nbtPlayer, walking);
   }

   public static void setPlayerIsSpiritWalking(NBTTagCompound nbtPlayer, boolean walking) {
      nbtPlayer.func_74757_a("WITCSpiritWalking", walking);
   }

   private static void addItemToInventory(EntityPlayer player, ItemStack protoStack, int totalQuantity) {
      if (totalQuantity > 0) {
         int itemsRemaining = totalQuantity;
         int maxStack = protoStack.func_77976_d();

         while(itemsRemaining > 0) {
            int quantity = itemsRemaining > maxStack ? maxStack : itemsRemaining;
            itemsRemaining -= quantity;
            ItemStack newStack = new ItemStack(protoStack.func_77973_b(), quantity, protoStack.func_77960_j());
            player.field_71071_by.func_70441_a(newStack);
         }
      }

   }

   private static void addItemToInventory(EntityPlayer player, ArrayList<ItemStack> stacks) {
      Iterator i$ = stacks.iterator();

      while(i$.hasNext()) {
         ItemStack stack = (ItemStack)i$.next();
         if (!player.field_71071_by.func_70441_a(stack)) {
            player.field_70170_p.func_72838_d(new EntityItem(player.field_70170_p, player.field_70165_t, 0.5D + player.field_70163_u, player.field_70161_v, stack));
         }
      }

   }

   public static void sendPlayerToSpiritWorld(EntityPlayer player, double nightmareChance) {
      if (player != null && !player.field_70170_p.field_72995_K) {
         NBTTagCompound nbtPlayer = Infusion.getNBT(player);
         if (!nbtPlayer.func_74764_b("WITCSpiritWorld")) {
            nbtPlayer.func_74782_a("WITCSpiritWorld", new NBTTagCompound());
         }

         NBTTagCompound nbtSpirit = nbtPlayer.func_74775_l("WITCSpiritWorld");
         Coord posBody = new Coord(player);
         posBody.setNBT(nbtSpirit, "OverworldBody");
         int fireFound = 0;
         int heartsFound = 0;
         int spiritPoolFound = 0;
         int cottonFound = 0;
         boolean nightmareCatcherFound = false;
         double modifiedNightmareChance = nightmareChance;
         boolean nightmare;
         int posZ;
         int x;
         if (nightmareChance > 0.0D && nightmareChance < 1.0D) {
            nightmare = true;
            int posX = MathHelper.func_76128_c(player.field_70165_t);
            int posY = MathHelper.func_76128_c(player.field_70163_u);
            posZ = MathHelper.func_76128_c(player.field_70161_v);
            x = posX - 8;

            while(true) {
               if (x > posX + 8) {
                  modifiedNightmareChance = nightmareCatcherFound ? Math.min(Math.max(modifiedNightmareChance, 0.0D), 1.0D) : nightmareChance;
                  break;
               }

               for(int z = posZ - 8; z <= posZ + 8; ++z) {
                  for(int y = posY - 8; y <= posY + 8; ++y) {
                     Block block = player.field_70170_p.func_147439_a(x, y, z);
                     if (!nightmareCatcherFound && block == Witchery.Blocks.DREAM_CATCHER) {
                        BlockDreamCatcher var10000 = Witchery.Blocks.DREAM_CATCHER;
                        if (BlockDreamCatcher.causesNightmares(player.field_70170_p, x, y, z)) {
                           modifiedNightmareChance -= 0.5D;
                           nightmareCatcherFound = true;
                        }
                     }

                     if (spiritPoolFound < 3 && block == Witchery.Blocks.FLOWING_SPIRIT && player.field_70170_p.func_72805_g(x, y, z) == 0) {
                        ++spiritPoolFound;
                        modifiedNightmareChance -= 0.1D;
                     }

                     if (cottonFound < 2 && block == Witchery.Blocks.WISPY_COTTON) {
                        ++cottonFound;
                        modifiedNightmareChance -= 0.1D;
                     }

                     if (heartsFound < 2 && block == Witchery.Blocks.DEMON_HEART) {
                        ++heartsFound;
                        modifiedNightmareChance += 0.35D;
                     }

                     if (fireFound < 3 && block == Blocks.field_150480_ab) {
                        ++fireFound;
                        modifiedNightmareChance += 0.1D;
                     }
                  }
               }

               ++x;
            }
         }

         nightmare = modifiedNightmareChance != 0.0D && (modifiedNightmareChance == 1.0D || player.field_70170_p.field_73012_v.nextDouble() < modifiedNightmareChance);
         boolean demonic = nightmare && nightmareCatcherFound && spiritPoolFound > 0 && heartsFound > 0 && player.field_70170_p.field_73012_v.nextDouble() < (double)heartsFound * 0.35D + (double)fireFound * 0.1D;
         setPlayerHasNightmare(nbtPlayer, nightmare, demonic);
         setPlayerIsSpiritWalking(nbtPlayer, true);
         EntityCorpse corpse = new EntityCorpse(player.field_70170_p);
         corpse.func_70606_j(player.func_110143_aJ());
         corpse.func_94058_c(player.func_70005_c_());
         corpse.setOwner(player.func_70005_c_());
         corpse.func_70012_b(0.5D + (double)MathHelper.func_76128_c(player.field_70165_t), player.field_70163_u, 0.5D + (double)MathHelper.func_76128_c(player.field_70161_v), 0.0F, 0.0F);
         player.field_70170_p.func_72838_d(corpse);
         posZ = player.field_71071_by.func_146027_a(Witchery.Items.GENERIC, Witchery.Items.GENERIC.itemIcyNeedle.damageValue);
         x = player.field_71071_by.func_146027_a(Witchery.Items.GENERIC, Witchery.Items.GENERIC.itemMutandis.damageValue);
         dropBetterBackpacks(player);
         NBTTagList nbtOverworldInventory = new NBTTagList();
         player.field_71071_by.func_70442_a(nbtOverworldInventory);
         nbtSpirit.func_74782_a("OverworldInventory", nbtOverworldInventory);
         if (nbtSpirit.func_74764_b("SpiritInventory")) {
            NBTTagList nbtSpiritInventory = nbtSpirit.func_150295_c("SpiritInventory", 10);
            player.field_71071_by.func_70443_b(nbtSpiritInventory);
            nbtSpirit.func_82580_o("SpiritInventory");
         } else {
            player.field_71071_by.func_146027_a((Item)null, -1);
         }

         addItemToInventory(player, Witchery.Items.GENERIC.itemIcyNeedle.createStack(), posZ);
         addItemToInventory(player, Witchery.Items.GENERIC.itemMutandis.createStack(), x);
         nbtSpirit.func_74776_a("OverworldHealth", Math.max(player.func_110143_aJ(), 1.0F));
         if (nbtSpirit.func_74764_b("SpiritHealth")) {
            float health = Math.max(nbtSpirit.func_74760_g("SpiritHealth"), 10.0F);
            player.func_70606_j(health);
            nbtSpirit.func_82580_o("SpiritHealth");
         }

         NBTTagCompound nbtOverworldFood = new NBTTagCompound();
         player.func_71024_bL().func_75117_b(nbtOverworldFood);
         nbtSpirit.func_74782_a("OverworldHunger", nbtOverworldFood);
         if (nbtSpirit.func_74764_b("SpiritHunger")) {
            NBTTagCompound nbtSpiritFood = nbtSpirit.func_74775_l("SpiritHunger");
            player.func_71024_bL().func_75112_a(nbtSpiritFood);
            player.func_71024_bL().func_75122_a(16, 0.8F);
            nbtSpirit.func_82580_o("SpiritHunger");
         }

         changeDimension(player, Config.instance().dimensionDreamID);
         findTopAndSetPosition(player.field_70170_p, player);
         Witchery.packetPipeline.sendToAll((IMessage)(new PacketPlayerStyle(player)));
         Witchery.packetPipeline.sendTo((IMessage)(new PacketPushTarget(0.0D, 0.1D, 0.0D)), (EntityPlayer)player);
      }

   }

   private static void dropBetterBackpacks(EntityPlayer player) {
      try {
         Class classItemBackpack = Class.forName("net.mcft.copy.betterstorage.item.ItemBackpack");
         Method[] methods = classItemBackpack.getDeclaredMethods();
         if (Config.instance().isDebugging()) {
            Method[] arr$ = methods;
            int len$ = methods.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Method method = arr$[i$];
               Log.instance().debug(method.toString());
            }
         }

         Method methodGetPackpack = classItemBackpack.getMethod("getBackpack", EntityLivingBase.class);
         if (methodGetPackpack == null) {
            Log.instance().debug("No getBackpack method found");
         } else {
            Log.instance().debug("using method: " + methodGetPackpack.toString());
         }

         ItemStack stackBackpack = (ItemStack)methodGetPackpack.invoke((Object)null, player);
         if (stackBackpack == null) {
            Log.instance().debug("No backpack stack found");
         } else {
            Log.instance().debug("got backpack stack: " + stackBackpack.toString());
         }

         Method methodPlaceBackpack = classItemBackpack.getDeclaredMethod("placeBackpack", EntityLivingBase.class, EntityPlayer.class, ItemStack.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, ForgeDirection.class, Boolean.TYPE, Boolean.TYPE);
         if (methodPlaceBackpack == null) {
            Log.instance().debug("No placebackpack method found");
         } else {
            Log.instance().debug("using method: " + methodPlaceBackpack.toString());
         }

         World w = player.field_70170_p;
         int x = MathHelper.func_76128_c(player.field_70165_t);
         int y = MathHelper.func_76128_c(player.field_70163_u);
         int z = MathHelper.func_76128_c(player.field_70161_v);
         boolean found = true;
         if (isReplaceable(w, x + 1, y, z)) {
            ++x;
         } else if (isReplaceable(w, x - 1, y, z)) {
            --x;
         } else if (isReplaceable(w, x, y, z + 1)) {
            ++z;
         } else if (isReplaceable(w, x - 1, y, z - 1)) {
            --z;
         } else if (isReplaceable(w, x + 1, y, z + 1)) {
            ++x;
            ++z;
         } else if (isReplaceable(w, x - 1, y, z + 1)) {
            --x;
            ++z;
         } else if (isReplaceable(w, x + 1, y, z - 1)) {
            ++x;
            --z;
         } else if (isReplaceable(w, x - 1, y, z - 1)) {
            --x;
            --z;
         } else {
            found = false;
         }

         if (found) {
            if (!w.func_147439_a(x, y - 1, z).func_149662_c()) {
               w.func_147449_b(x, y - 1, z, Blocks.field_150348_b);
            }
         } else {
            found = true;
            ++y;
            if (isReplaceable(w, x + 1, y, z)) {
               ++x;
            } else if (isReplaceable(w, x - 1, y, z)) {
               --x;
            } else if (isReplaceable(w, x, y, z + 1)) {
               ++z;
            } else if (isReplaceable(w, x - 1, y, z - 1)) {
               --z;
            } else if (isReplaceable(w, x + 1, y, z + 1)) {
               ++x;
               ++z;
            } else if (isReplaceable(w, x - 1, y, z + 1)) {
               --x;
               ++z;
            } else if (isReplaceable(w, x + 1, y, z - 1)) {
               ++x;
               --z;
            } else if (isReplaceable(w, x - 1, y, z - 1)) {
               --x;
               --z;
            } else {
               found = false;
            }

            if (!found) {
               ++x;
               ++y;
               w.func_147468_f(x, y, z);
               if (!w.func_147439_a(x, y - 1, z).func_149662_c()) {
                  w.func_147449_b(x, y - 1, z, Blocks.field_150348_b);
               }
            }
         }

         Boolean result = (Boolean)methodPlaceBackpack.invoke((Object)null, player, player, stackBackpack, x, y, z, 1, ForgeDirection.NORTH, false, false);
         if (result.equals(Boolean.FALSE)) {
            Log.instance().debug("Backpack could not be placed");
         } else {
            Method methodSetBackpack = classItemBackpack.getDeclaredMethod("setBackpack", EntityLivingBase.class, ItemStack.class, ItemStack[].class);
            if (methodSetBackpack == null) {
               Log.instance().debug("No setBackpack method found");
            } else {
               Log.instance().debug("using method: " + methodPlaceBackpack.toString());
            }

            methodSetBackpack.invoke((Object)null, player, null, null);
         }
      } catch (ClassNotFoundException var13) {
         Log.instance().debug("No class found for ItemBackpack");
      } catch (NoSuchMethodException var14) {
         Log.instance().debug("No onPlaceBackpack method found: " + var14.toString());
      } catch (InvocationTargetException var15) {
         Log.instance().debug("No onPlaceBackpack target found");
      } catch (IllegalAccessException var16) {
         Log.instance().debug("No onPlaceBackpack method access allowed");
      } catch (IndexOutOfBoundsException var17) {
         Log.instance().debug("No onPlaceBackpack method index");
      } catch (Throwable var18) {
         Log.instance().debug("Unexpected onPlaceBackpack error: " + var18.toString());
      }

   }

   private static boolean isReplaceable(World world, int x, int y, int z) {
      Material m = world.func_147439_a(x, y, z).func_149688_o();
      return m == null ? false : m.func_76222_j();
   }

   public static void changeDimension(EntityPlayer player, int dimension) {
      dismountEntity(player);
      ItemGeneral var10000 = Witchery.Items.GENERIC;
      ItemGeneral.travelToDimension(player, dimension);
   }

   private static void dismountEntity(EntityPlayer player) {
      if (player.func_70115_ae()) {
         player.func_70078_a((Entity)null);
      }

   }

   public static void findTopAndSetPosition(World world, EntityPlayer player) {
      findTopAndSetPosition(world, player, player.field_70165_t, player.field_70163_u, player.field_70161_v);
   }

   private static void findTopAndSetPosition(World world, EntityPlayer player, double posX, double posY, double posZ) {
      int x = MathHelper.func_76128_c(posX);
      int y = MathHelper.func_76128_c(posY);
      int z = MathHelper.func_76128_c(posZ);
      if (!isValidSpawnPoint(world, x, y, z)) {
         for(int i = 1; i <= 256; ++i) {
            int yPlus = y + i;
            int yMinus = y - i;
            if (yPlus < 256 && isValidSpawnPoint(world, x, yPlus, z)) {
               y = yPlus;
               break;
            }

            if (yMinus > 2 && isValidSpawnPoint(world, x, yMinus, z)) {
               y = yMinus;
               break;
            }

            if (yMinus <= 2 && yPlus >= 255) {
               break;
            }
         }
      }

      player.func_70634_a(0.5D + (double)x, 0.1D + (double)y, 0.5D + (double)z);
   }

   private static boolean isValidSpawnPoint(World world, int x, int y, int z) {
      Material materialBelow = world.func_147439_a(x, y - 1, z).func_149688_o();
      return !world.func_147437_c(x, y - 1, z) && materialBelow != Material.field_151587_i && world.func_147437_c(x, y, z) && world.func_147437_c(x, y + 1, z);
   }

   public static void returnPlayerToOverworld(EntityPlayer player) {
      if (player != null && !player.field_70170_p.field_72995_K) {
         if (player.field_71093_bK != Config.instance().dimensionDreamID) {
            Log.instance().warning("Player " + player.getDisplayName() + " is in incorrect dimension when returning frmo spirit world, dimension=" + player.field_71093_bK);
         }

         NBTTagCompound nbtPlayer = Infusion.getNBT(player);
         if (!nbtPlayer.func_74764_b("WITCSpiritWorld")) {
            nbtPlayer.func_74782_a("WITCSpiritWorld", new NBTTagCompound());
         }

         NBTTagCompound nbtSpirit = nbtPlayer.func_74775_l("WITCSpiritWorld");
         boolean isSpiritWorld = player.field_71093_bK == Config.instance().dimensionDreamID;
         int cottonRemoved = isSpiritWorld ? player.field_71071_by.func_146027_a(Item.func_150898_a(Witchery.Blocks.WISPY_COTTON), 0) : 0;
         int disturbedCottonRemoved = isSpiritWorld ? player.field_71071_by.func_146027_a(Witchery.Items.GENERIC, Witchery.Items.GENERIC.itemDisturbedCotton.damageValue) : 0;
         int hunger = isSpiritWorld ? player.field_71071_by.func_146027_a(Witchery.Items.GENERIC, Witchery.Items.GENERIC.itemMellifluousHunger.damageValue) : 0;
         int spirit = isSpiritWorld ? player.field_71071_by.func_146027_a(Witchery.Items.GENERIC, Witchery.Items.GENERIC.itemBrewOfFlowingSpirit.damageValue) : 0;
         int subduedSpirits = player.field_71071_by.func_146027_a(Witchery.Items.GENERIC, Witchery.Items.GENERIC.itemSubduedSpirit.damageValue);
         int boneNeedles = player.field_71071_by.func_146027_a(Witchery.Items.GENERIC, Witchery.Items.GENERIC.itemIcyNeedle.damageValue);
         dropBetterBackpacks(player);
         NBTTagList nbtOverworldInventory;
         if (player.field_71093_bK == Config.instance().dimensionDreamID) {
            nbtOverworldInventory = new NBTTagList();
            player.field_71071_by.func_70442_a(nbtOverworldInventory);
            nbtSpirit.func_74782_a("SpiritInventory", nbtOverworldInventory);
         }

         if (nbtSpirit.func_74764_b("OverworldInventory")) {
            nbtOverworldInventory = nbtSpirit.func_150295_c("OverworldInventory", 10);
            player.field_71071_by.func_70443_b(nbtOverworldInventory);
            nbtSpirit.func_82580_o("OverworldInventory");
         } else {
            player.field_71071_by.func_146027_a((Item)null, -1);
         }

         addItemToInventory(player, new ItemStack(Witchery.Blocks.WISPY_COTTON, 1, 0), cottonRemoved);
         addItemToInventory(player, Witchery.Items.GENERIC.itemDisturbedCotton.createStack(), disturbedCottonRemoved);
         addItemToInventory(player, Witchery.Items.GENERIC.itemMellifluousHunger.createStack(), hunger);
         addItemToInventory(player, Witchery.Items.GENERIC.itemIcyNeedle.createStack(), boneNeedles);
         addItemToInventory(player, Witchery.Items.GENERIC.itemBrewOfFlowingSpirit.createStack(), spirit);
         addItemToInventory(player, Witchery.Items.GENERIC.itemSubduedSpirit.createStack(), subduedSpirits);
         nbtSpirit.func_74776_a("SpiritHealth", Math.max(player.func_110143_aJ(), 10.0F));
         if (nbtSpirit.func_74764_b("OverworldHealth")) {
            float health = nbtSpirit.func_74760_g("OverworldHealth");
            player.func_70606_j(health);
            nbtSpirit.func_82580_o("OverworldHealth");
         }

         NBTTagCompound nbtSpiritFood = new NBTTagCompound();
         player.func_71024_bL().func_75117_b(nbtSpiritFood);
         nbtSpirit.func_74782_a("SpiritHunger", nbtSpiritFood);
         if (nbtSpirit.func_74764_b("OverworldHunger")) {
            NBTTagCompound nbtOverworldFood = nbtSpirit.func_74775_l("OverworldHunger");
            player.func_71024_bL().func_75112_a(nbtOverworldFood);
            nbtSpirit.func_82580_o("OverworldHunger");
         }

         setPlayerHasNightmare(nbtPlayer, false, false);
         setPlayerIsGhost(nbtPlayer, false);
         setPlayerIsSpiritWalking(nbtPlayer, false);
         player.func_70066_B();
         Coord posBody = Coord.createFrom(nbtSpirit, "OverworldBody");
         if (player.field_71093_bK != 0) {
            if (posBody != null) {
               dismountEntity(player);
               player.func_70634_a((double)posBody.x, (double)posBody.y, (double)posBody.z);
            }

            changeDimension(player, 0);
         }

         World world = player.field_70170_p;
         if (posBody != null) {
            findTopAndSetPosition(player.field_70170_p, player, (double)posBody.x, (double)posBody.y, (double)posBody.z);
            nbtSpirit.func_82580_o("OverworldBody");
         } else {
            findTopAndSetPosition(player.field_70170_p, player);
         }

         Iterator i$ = player.field_70170_p.field_72996_f.iterator();

         while(i$.hasNext()) {
            Object obj = i$.next();
            if (obj instanceof EntityCorpse) {
               EntityCorpse corpse = (EntityCorpse)obj;
               String owner = corpse.getOwnerName();
               if (owner != null && owner.equalsIgnoreCase(player.func_70005_c_())) {
                  player.field_70170_p.func_72900_e(corpse);
               }
            }
         }

         Witchery.packetPipeline.sendToAll((IMessage)(new PacketPlayerStyle(player)));
         Witchery.packetPipeline.sendTo((IMessage)(new PacketPushTarget(0.0D, 0.1D, 0.0D)), (EntityPlayer)player);
      }

   }

   public static void manifestPlayerInOverworldAsGhost(EntityPlayer player) {
      if (player != null && !player.field_70170_p.field_72995_K) {
         NBTTagCompound nbtPlayer = Infusion.getNBT(player);
         if (!nbtPlayer.func_74764_b("WITCSpiritWorld")) {
            nbtPlayer.func_74782_a("WITCSpiritWorld", new NBTTagCompound());
         }

         NBTTagCompound nbtSpirit = nbtPlayer.func_74775_l("WITCSpiritWorld");
         int boneNeedles = player.field_71071_by.func_146027_a(Witchery.Items.GENERIC, Witchery.Items.GENERIC.itemIcyNeedle.damageValue);
         dropBetterBackpacks(player);
         NBTTagList nbtSpiritInventory = new NBTTagList();
         player.field_71071_by.func_70442_a(nbtSpiritInventory);
         nbtSpirit.func_74782_a("SpiritInventory", nbtSpiritInventory);
         player.field_71071_by.func_146027_a((Item)null, -1);
         addItemToInventory(player, Witchery.Items.GENERIC.itemIcyNeedle.createStack(), boneNeedles);
         nbtSpirit.func_74776_a("SpiritHealth", Math.max(player.func_110143_aJ(), 1.0F));
         setPlayerIsGhost(nbtPlayer, true);
         changeDimension(player, 0);
         findTopAndSetPosition(player.field_70170_p, player);
         Witchery.packetPipeline.sendToAll((IMessage)(new PacketPlayerStyle(player)));
      }

   }

   public static void returnGhostPlayerToSpiritWorld(EntityPlayer player) {
      if (player != null && !player.field_70170_p.field_72995_K) {
         NBTTagCompound nbtPlayer = Infusion.getNBT(player);
         if (!nbtPlayer.func_74764_b("WITCSpiritWorld")) {
            nbtPlayer.func_74782_a("WITCSpiritWorld", new NBTTagCompound());
         }

         NBTTagCompound nbtSpirit = nbtPlayer.func_74775_l("WITCSpiritWorld");
         int boneNeedles = player.field_71071_by.func_146027_a(Witchery.Items.GENERIC, Witchery.Items.GENERIC.itemIcyNeedle.damageValue);
         ArrayList<ItemStack> fetishes = getBoundFetishes(player.field_71071_by);
         player.field_71071_by.func_70436_m();
         dropBetterBackpacks(player);
         if (nbtSpirit.func_74764_b("SpiritInventory")) {
            NBTTagList nbtSpiritInventory = nbtSpirit.func_150295_c("SpiritInventory", 10);
            player.field_71071_by.func_70443_b(nbtSpiritInventory);
            nbtSpirit.func_82580_o("SpiritInventory");
         }

         addItemToInventory(player, Witchery.Items.GENERIC.itemIcyNeedle.createStack(), boneNeedles);
         addItemToInventory(player, fetishes);
         setPlayerIsGhost(nbtPlayer, false);
         changeDimension(player, Config.instance().dimensionDreamID);
         findTopAndSetPosition(player.field_70170_p, player);
         Witchery.packetPipeline.sendToAll((IMessage)(new PacketPlayerStyle(player)));
      }

   }

   private static ArrayList<ItemStack> getBoundFetishes(InventoryPlayer inventory) {
      ArrayList<ItemStack> stacks = new ArrayList();

      for(int i = 0; i < inventory.func_70302_i_(); ++i) {
         ItemStack stack = inventory.func_70301_a(i);
         if (stack != null && stack.func_77973_b() instanceof BlockFetish.ClassItemBlock && InfusedSpiritEffect.getEffectID(stack) > 0) {
            stacks.add(stack);
         }
      }

      return stacks;
   }

   public static void updatePlayerEffects(World world, EntityPlayer player, NBTTagCompound nbtPlayer, long time, long counter) {
      if (!world.field_72995_K) {
         boolean done = false;
         if (counter % 20L == 0L) {
            boolean mustAwaken = getPlayerMustAwaken(nbtPlayer);
            if (mustAwaken) {
               setPlayerMustAwaken(nbtPlayer, false);
               if (player.field_71093_bK != Config.instance().dimensionDreamID && getPlayerIsSpiritWalking(player) && !getPlayerIsGhost(player)) {
                  returnPlayerToOverworld(player);
               } else if (player.field_71093_bK == Config.instance().dimensionDreamID) {
                  returnPlayerToOverworld(player);
               }
            }
         }

         if (!done && counter % 100L == 0L) {
            int nightmareLevel = getPlayerHasNightmare(nbtPlayer);
            if (player.field_71093_bK == Config.instance().dimensionDreamID && nightmareLevel > 0) {
               double R = 18.0D;
               double H = 18.0D;
               AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(player.field_70165_t - 18.0D, player.field_70163_u - 18.0D, player.field_70161_v - 18.0D, player.field_70165_t + 18.0D, player.field_70163_u + 18.0D, player.field_70161_v + 18.0D);
               if (nightmareLevel > 1) {
                  double chance = world.field_73012_v.nextDouble();
                  if (chance < 0.5D) {
                     EntitySmallFireball fireball = new EntitySmallFireball(world, player.field_70165_t - 2.0D + (double)world.field_73012_v.nextInt(5), player.field_70163_u + 15.0D, player.field_70161_v - 2.0D + (double)world.field_73012_v.nextInt(5), 0.0D, -0.2D, 0.0D);
                     world.func_72838_d(fireball);
                  } else if (chance < 0.65D) {
                     EntityLargeFireball fireball = new EntityLargeFireball(world);
                     double par2 = player.field_70165_t - 2.0D + (double)world.field_73012_v.nextInt(5);
                     double par4 = player.field_70163_u + 15.0D;
                     double par6 = player.field_70161_v - 2.0D + (double)world.field_73012_v.nextInt(5);
                     double par8 = 0.0D;
                     double par10 = -0.2D;
                     double par12 = 0.0D;
                     fireball.func_70012_b(par2, par4, par6, fireball.field_70177_z, fireball.field_70125_A);
                     fireball.func_70107_b(par2, par4, par6);
                     double d6 = (double)MathHelper.func_76133_a(par8 * par8 + par10 * par10 + par12 * par12);
                     fireball.field_70232_b = par8 / d6 * 0.1D;
                     fireball.field_70233_c = par10 / d6 * 0.1D;
                     fireball.field_70230_d = par12 / d6 * 0.1D;
                     world.func_72838_d(fireball);
                  } else if (chance < 0.75D) {
                     List entities = world.func_72872_a(EntityMob.class, bounds);
                     if (entities.size() < 10 && !containsDemons(entities, 2)) {
                        new EntityDemon(world);
                        Infusion.spawnCreature(world, EntityDemon.class, MathHelper.func_76128_c(player.field_70165_t), MathHelper.func_76128_c(player.field_70163_u), MathHelper.func_76128_c(player.field_70161_v), player, 4, 8, ParticleEffect.SMOKE, SoundEffect.MOB_WITHER_DEATH);
                     }
                  }
               }

               List entities = world.func_72872_a(EntityNightmare.class, bounds);
               Iterator i$ = entities.iterator();

               while(i$.hasNext()) {
                  Object obj = i$.next();
                  EntityNightmare nightmare = (EntityNightmare)obj;
                  if (nightmare.getVictimName().equalsIgnoreCase(player.func_70005_c_())) {
                     return;
                  }
               }

               long currentTime = MinecraftServer.func_130071_aq();
               long lastKillTime = getPlayerLastNightmareKill(nbtPlayer);
               if (lastKillTime < currentTime - 30000L) {
                  Infusion.spawnCreature(world, EntityNightmare.class, MathHelper.func_76128_c(player.field_70165_t), MathHelper.func_76128_c(player.field_70163_u), MathHelper.func_76128_c(player.field_70161_v), player, 2, 6);
               }
            } else if (player.field_71093_bK != Config.instance().dimensionDreamID && getPlayerIsGhost(nbtPlayer)) {
               int timeRemaining = 0;
               boolean skipNext = getPlayerSkipNextManifestTick(nbtPlayer);
               if (nbtPlayer.func_74764_b("WITCManifestDuration")) {
                  timeRemaining = nbtPlayer.func_74762_e("WITCManifestDuration");
                  timeRemaining = Math.max(0, timeRemaining - 5);
                  if ((timeRemaining >= 60 && timeRemaining <= 64 || timeRemaining >= 30 && timeRemaining <= 34 || timeRemaining >= 15 && timeRemaining <= 19) && !skipNext) {
                     ChatUtil.sendTranslated(EnumChatFormatting.LIGHT_PURPLE, player, "witchery.rite.manifestation.countdown", Integer.valueOf(timeRemaining).toString());
                  }
               }

               if (timeRemaining == 0) {
                  if (nbtPlayer.func_74764_b("WITCManifestDuration")) {
                     nbtPlayer.func_82580_o("WITCManifestDuration");
                  }

                  returnGhostPlayerToSpiritWorld(player);
               } else if (!skipNext) {
                  nbtPlayer.func_74768_a("WITCManifestDuration", timeRemaining);
               } else {
                  setPlayerSkipNextManifestationReduction(nbtPlayer, false);
               }
            }
         }
      }

   }

   public static void setPlayerSkipNextManifestationReduction(EntityPlayer player) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      setPlayerSkipNextManifestationReduction(nbtPlayer, true);
   }

   public static void setPlayerSkipNextManifestationReduction(NBTTagCompound nbtPlayer, boolean skip) {
      nbtPlayer.func_74757_a("WITCManifestSkipTick", skip);
   }

   public static boolean getPlayerSkipNextManifestTick(NBTTagCompound nbtPlayer) {
      return nbtPlayer.func_74767_n("WITCManifestSkipTick");
   }

   private static boolean containsDemons(List entities, int max) {
      int count = 0;
      Iterator i$ = entities.iterator();

      while(i$.hasNext()) {
         Object obj = i$.next();
         if (obj instanceof EntityDemon) {
            ++count;
            if (count >= max) {
               return true;
            }
         }
      }

      return false;
   }

   public static void setPlayerIsGhost(NBTTagCompound nbtPlayer, boolean ghost) {
      nbtPlayer.func_74757_a("WITCManifested", ghost);
   }

   public static boolean getPlayerIsGhost(EntityPlayer player) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      return getPlayerIsGhost(nbtPlayer);
   }

   public static boolean getPlayerIsGhost(NBTTagCompound nbtPlayer) {
      return nbtPlayer.func_74767_n("WITCManifested");
   }

   public static void setPlayerMustAwaken(EntityPlayer player, boolean awaken) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      setPlayerMustAwaken(nbtPlayer, awaken);
   }

   public static void setPlayerMustAwaken(NBTTagCompound nbtPlayer, boolean ghost) {
      nbtPlayer.func_74757_a("WITCForceAwaken", ghost);
   }

   public static boolean getPlayerMustAwaken(EntityPlayer player) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      return getPlayerMustAwaken(nbtPlayer);
   }

   public static boolean getPlayerMustAwaken(NBTTagCompound nbtPlayer) {
      return nbtPlayer.func_74767_n("WITCForceAwaken");
   }

   public static boolean canPlayerManifest(EntityPlayer player) {
      NBTTagCompound nbtPlayer = Infusion.getNBT(player);
      int timeRemaining = 0;
      if (nbtPlayer != null && nbtPlayer.func_74764_b("WITCManifestDuration")) {
         timeRemaining = nbtPlayer.func_74762_e("WITCManifestDuration");
      }

      return timeRemaining >= 5;
   }
}
