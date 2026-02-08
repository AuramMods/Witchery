package com.emoniph.witchery.blocks;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.ritual.Rite;
import com.emoniph.witchery.ritual.rites.RiteCurseCreature;
import com.emoniph.witchery.ritual.rites.RiteTeleportEntity;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.Log;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class BlockAreaMarker extends BlockBaseContainer {
   public BlockAreaMarker(Class<? extends BlockAreaMarker.TileEntityAreaMarker> clazzTile) {
      super(Material.field_151576_e, clazzTile);
      this.func_149722_s();
      this.func_149752_b(9999.0F);
      this.func_149711_c(2.5F);
      this.func_149672_a(field_149769_e);
      this.func_149676_a(0.15F, 0.0F, 0.15F, 0.85F, 0.5F, 0.85F);
   }

   public void func_149689_a(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
      int l = MathHelper.func_76128_c((double)(entity.field_70177_z * 4.0F / 360.0F) + 0.5D) & 3;
      if (l == 0) {
         world.func_72921_c(x, y, z, 2, 2);
      }

      if (l == 1) {
         world.func_72921_c(x, y, z, 5, 2);
      }

      if (l == 2) {
         world.func_72921_c(x, y, z, 3, 2);
      }

      if (l == 3) {
         world.func_72921_c(x, y, z, 4, 2);
      }

      if (!world.field_72995_K && entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         TileEntity tile = world.func_147438_o(x, y, z);
         if (tile != null && tile instanceof BlockAreaMarker.TileEntityAreaMarker) {
            BlockAreaMarker.TileEntityAreaMarker marker = (BlockAreaMarker.TileEntityAreaMarker)tile;
            marker.setOwner(player.func_70005_c_());
         }
      }

   }

   public void func_149699_a(World world, int x, int y, int z, EntityPlayer player) {
      if (!world.field_72995_K & player != null) {
         TileEntity tile = world.func_147438_o(x, y, z);
         if (tile != null && tile instanceof BlockAreaMarker.TileEntityAreaMarker) {
            BlockAreaMarker.TileEntityAreaMarker marker = (BlockAreaMarker.TileEntityAreaMarker)tile;
            if (player.field_71075_bZ.field_75098_d || player.func_70005_c_().equals(marker.getOwner()) && player.func_70093_af()) {
               for(int dy = y; world.func_147439_a(x, dy, z) == this; ++dy) {
                  world.func_147468_f(x, dy, z);
                  world.func_72838_d(new EntityItem(world, 0.5D + (double)x, 0.5D + (double)dy, 0.5D + (double)z, new ItemStack(this)));
               }
            }
         }
      }

   }

   public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
      TileEntity tile = world.func_147438_o(x, y, z);
      if (tile != null && tile instanceof BlockAreaMarker.TileEntityAreaMarker) {
         BlockAreaMarker.TileEntityAreaMarker marker = (BlockAreaMarker.TileEntityAreaMarker)tile;
         return marker.activateBlock(world, x, y, z, player, side);
      } else {
         return false;
      }
   }

   public boolean func_149662_c() {
      return false;
   }

   public boolean func_149686_d() {
      return false;
   }

   public int func_149645_b() {
      return 0;
   }

   public int func_149745_a(Random rand) {
      return 1;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World world, int x, int y, int z, Random rand) {
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister par1IconRegister) {
      this.field_149761_L = par1IconRegister.func_94245_a("stone");
   }

   public static class AreaMarkerEventHooks {
      @SubscribeEvent(
         priority = EventPriority.NORMAL
      )
      public void onLivingDeath(LivingDeathEvent event) {
         if (!event.isCanceled() && !event.entityLiving.field_70170_p.field_72995_K && event.entityLiving instanceof EntityPlayer && event.source.func_76364_f() != null && event.source.func_76364_f() instanceof EntityPlayer && event.source.func_76364_f() != event.entityLiving) {
            EntityPlayer attacker = (EntityPlayer)event.source.func_76364_f();
            Iterator i$ = BlockAreaMarker.AreaMarkerRegistry.instance().tiles.iterator();

            while(i$.hasNext()) {
               BlockAreaMarker.TileEntityAreaMarker tile = (BlockAreaMarker.TileEntityAreaMarker)i$.next();
               if (tile.isNear(attacker)) {
                  tile.setKiller(attacker);
               }
            }
         }

      }
   }

   public abstract static class TileEntityAreaMarker extends TileEntityBase {
      private static final String OWNER_KEY = "WITCPlacer";
      private String owner;
      private ArrayList<String> killers = new ArrayList();

      protected void initiate() {
         super.initiate();
         if (!this.field_145850_b.field_72995_K) {
            if (this.field_145850_b.func_147439_a(this.field_145851_c, this.field_145848_d, this.field_145849_e) == this.getExpectedBlockType()) {
               BlockAreaMarker.AreaMarkerRegistry.instance().add(this);
            } else {
               Log.instance().warning("Area Marker tile entity exists without a corresponding block at: " + this.field_145851_c + ", " + this.field_145848_d + ", " + this.field_145849_e);
            }
         }

      }

      public void func_145843_s() {
         super.func_145843_s();
         if (!this.field_145850_b.field_72995_K) {
            BlockAreaMarker.AreaMarkerRegistry.instance().remove(this);
         }

      }

      public void setOwner(String username) {
         this.owner = username;
      }

      public String getOwner() {
         return this.owner != null ? this.owner : "";
      }

      public void setKiller(EntityPlayer player) {
         String username = player.func_70005_c_();
         if (!this.killers.contains(username)) {
            this.killers.add(username);
         }

      }

      public boolean checkIsProtected(EntityLivingBase entity, Rite rite) {
         if (!this.isNear(entity)) {
            return false;
         } else {
            boolean killer = entity instanceof EntityPlayer && this.killers.contains(entity.func_70005_c_());
            return this.isProtected(entity, killer, rite);
         }
      }

      public void func_145841_b(NBTTagCompound nbtTag) {
         super.func_145841_b(nbtTag);
         nbtTag.func_74778_a("WITCPlacer", this.getOwner());
         NBTTagList nbtKillers = new NBTTagList();
         Iterator i$ = this.killers.iterator();

         while(i$.hasNext()) {
            String killer = (String)i$.next();
            nbtKillers.func_74742_a(new NBTTagString(killer));
         }

         nbtTag.func_74782_a("Killers", nbtKillers);
      }

      public void func_145839_a(NBTTagCompound nbtTag) {
         super.func_145839_a(nbtTag);
         if (nbtTag.func_74764_b("WITCPlacer")) {
            this.owner = nbtTag.func_74779_i("WITCPlacer");
         } else {
            this.owner = "";
         }

         NBTTagList nbtKillers = nbtTag.func_150295_c("Killers", 8);
         int i = 0;

         for(int count = nbtKillers.func_74745_c(); i < count; ++i) {
            this.killers.add(nbtKillers.func_150307_f(i));
         }

      }

      public abstract boolean activateBlock(World var1, int var2, int var3, int var4, EntityPlayer var5, int var6);

      protected abstract boolean isNear(EntityLivingBase var1);

      protected abstract boolean isProtected(EntityLivingBase var1, boolean var2, Rite var3);

      protected abstract Block getExpectedBlockType();
   }

   public static class AreaMarkerRegistry {
      private static BlockAreaMarker.AreaMarkerRegistry INSTANCE_CLIENT;
      private static BlockAreaMarker.AreaMarkerRegistry INSTANCE_SERVER;
      private final ArrayList<BlockAreaMarker.TileEntityAreaMarker> tiles = new ArrayList();

      public static BlockAreaMarker.AreaMarkerRegistry instance() {
         return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER ? INSTANCE_SERVER : INSTANCE_CLIENT;
      }

      public static void serverStart() {
         INSTANCE_CLIENT = new BlockAreaMarker.AreaMarkerRegistry();
         INSTANCE_SERVER = new BlockAreaMarker.AreaMarkerRegistry();
      }

      private void add(BlockAreaMarker.TileEntityAreaMarker tile) {
         if (!this.tiles.contains(tile)) {
            try {
               Iterator it = this.tiles.iterator();

               label35:
               while(true) {
                  BlockAreaMarker.TileEntityAreaMarker source;
                  do {
                     if (!it.hasNext()) {
                        break label35;
                     }

                     source = (BlockAreaMarker.TileEntityAreaMarker)it.next();
                  } while(source != null && !source.func_145837_r() && (source.field_145851_c != tile.field_145851_c || source.field_145848_d != tile.field_145848_d || source.field_145849_e != tile.field_145849_e));

                  it.remove();
               }
            } catch (Throwable var4) {
               Log.instance().warning(var4, "Exception occured validating existing power source entries");
            }

            this.tiles.add(tile);
         }

      }

      private void remove(BlockAreaMarker.TileEntityAreaMarker tile) {
         if (this.tiles.contains(tile)) {
            this.tiles.remove(tile);
         }

         try {
            Iterator it = this.tiles.iterator();

            while(true) {
               while(it.hasNext()) {
                  BlockAreaMarker.TileEntityAreaMarker source = (BlockAreaMarker.TileEntityAreaMarker)it.next();
                  if (source != null && !source.func_145837_r()) {
                     if (source.func_145831_w().func_147438_o(source.field_145851_c, source.field_145848_d, source.field_145849_e) != source) {
                        it.remove();
                     }
                  } else {
                     it.remove();
                  }
               }

               return;
            }
         } catch (Throwable var4) {
            Log.instance().warning(var4, "Exception occured removing existing power source entries");
         }
      }

      public boolean isProtectionActive(EntityLivingBase entity, Rite rite) {
         Iterator i$ = this.tiles.iterator();

         BlockAreaMarker.TileEntityAreaMarker tile;
         do {
            if (!i$.hasNext()) {
               return false;
            }

            tile = (BlockAreaMarker.TileEntityAreaMarker)i$.next();
         } while(!tile.checkIsProtected(entity, rite));

         return true;
      }
   }

   public static class TileEntityAreaTeleportPullProtect extends BlockAreaMarker.TileEntityAreaMarker {
      public boolean activateBlock(World world, int x, int y, int z, EntityPlayer player, int side) {
         return false;
      }

      protected boolean isProtected(EntityLivingBase entity, boolean killer, Rite rite) {
         return !killer && Config.instance().allowDecurseTeleport && (rite == null || rite instanceof RiteTeleportEntity);
      }

      protected boolean isNear(EntityLivingBase entity) {
         int RADIUS = Config.instance().decurseTeleportPullRadius;
         int RADIUS_SQ = RADIUS * RADIUS;
         boolean inRange = Coord.distanceSq(entity.field_70165_t, 1.0D, entity.field_70161_v, (double)this.field_145851_c, 1.0D, (double)this.field_145849_e) <= (double)RADIUS_SQ;
         return inRange && this.field_145850_b.field_73011_w.field_76574_g == entity.field_71093_bK;
      }

      protected Block getExpectedBlockType() {
         return Witchery.Blocks.DECURSE_TELEPORT;
      }
   }

   public static class TileEntityAreaCurseProtect extends BlockAreaMarker.TileEntityAreaMarker {
      public boolean activateBlock(World world, int x, int y, int z, EntityPlayer player, int side) {
         return false;
      }

      protected boolean isProtected(EntityLivingBase entity, boolean killer, Rite rite) {
         return !killer && Config.instance().allowDecurseDirected && (rite == null || rite instanceof RiteCurseCreature);
      }

      protected boolean isNear(EntityLivingBase entity) {
         int RADIUS = Config.instance().decurseDirectedRadius;
         int RADIUS_SQ = RADIUS * RADIUS;
         boolean inRange = Coord.distanceSq(entity.field_70165_t, 1.0D, entity.field_70161_v, (double)this.field_145851_c, 1.0D, (double)this.field_145849_e) <= (double)RADIUS_SQ;
         return inRange && this.field_145850_b.field_73011_w.field_76574_g == entity.field_71093_bK;
      }

      protected Block getExpectedBlockType() {
         return Witchery.Blocks.DECURSE_DIRECTED;
      }
   }
}
