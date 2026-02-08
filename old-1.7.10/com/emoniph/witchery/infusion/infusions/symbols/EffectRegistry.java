package com.emoniph.witchery.infusion.infusions.symbols;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockBrazier;
import com.emoniph.witchery.blocks.BlockWickerBundle;
import com.emoniph.witchery.blocks.BlockWitchDoor;
import com.emoniph.witchery.brewing.EntityBrew;
import com.emoniph.witchery.brewing.WitcheryBrewRegistry;
import com.emoniph.witchery.brewing.potions.PotionEnslaved;
import com.emoniph.witchery.brewing.potions.PotionIllFitting;
import com.emoniph.witchery.dimension.WorldProviderTorment;
import com.emoniph.witchery.entity.EntityDarkMark;
import com.emoniph.witchery.entity.EntityEnt;
import com.emoniph.witchery.entity.EntitySpellEffect;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.infusion.infusions.InfusionLight;
import com.emoniph.witchery.infusion.infusions.InfusionOtherwhere;
import com.emoniph.witchery.item.ItemChalk;
import com.emoniph.witchery.item.ItemLeonardsUrn;
import com.emoniph.witchery.network.PacketPushTarget;
import com.emoniph.witchery.util.BlockProtect;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.DemonicDamageSource;
import com.emoniph.witchery.util.EntityPosition;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.InvUtil;
import com.emoniph.witchery.util.Log;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TimeUtil;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class EffectRegistry {
   private static final EffectRegistry INSTANCE = new EffectRegistry();
   private Hashtable<ByteBuffer, SymbolEffect> effects = new Hashtable();
   private Hashtable<ByteBuffer, Integer> enhanced = new Hashtable();
   private Hashtable<Integer, SymbolEffect> effectID = new Hashtable();
   private ArrayList<SymbolEffect> allEffects = new ArrayList();
   public static final SymbolEffect Accio = instance().addEffect((new SymbolEffectProjectile(1, "witchery.pott.accio") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect spell) {
         if (caster != null && mop != null) {
            double R = spell.getEffectLevel() == 1 ? 0.8D : (spell.getEffectLevel() == 2 ? 3.0D : 9.0D);
            double R_SQ = R * R;
            AxisAlignedBB bb = AxisAlignedBB.func_72330_a(spell.field_70165_t - R, spell.field_70163_u - R, spell.field_70161_v - R, spell.field_70165_t + R, spell.field_70163_u + R, spell.field_70161_v + R);
            List entities = world.func_72872_a(EntityItem.class, bb);
            Iterator i$ = entities.iterator();

            while(i$.hasNext()) {
               Object obj = i$.next();
               EntityItem item = (EntityItem)obj;
               if (item.func_70068_e(spell) <= R_SQ) {
                  item.func_70107_b(caster.field_70165_t, caster.field_70163_u + 1.0D, caster.field_70161_v);
               }
            }
         }

      }
   }).setColor(5322534).setSize(1.0F), new StrokeSet(1, new byte[]{3, 0, 2, 2, 1}), new StrokeSet(1, new byte[]{3, 0, 2, 2, 2, 1}), new StrokeSet(2, new byte[]{3, 0, 0, 2, 2, 1, 1}), new StrokeSet(2, new byte[]{3, 0, 0, 2, 2, 2, 1, 1}), new StrokeSet(3, new byte[]{3, 0, 0, 0, 2, 2, 2, 1, 1, 1}), new StrokeSet(3, new byte[]{3, 0, 0, 0, 2, 2, 2, 2, 1, 1, 1}));
   public static final SymbolEffect Aguamenti = instance().addEffect((new SymbolEffectProjectile(2, "witchery.pott.aguamenti") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect spell) {
         int dx;
         int dy;
         int dz;
         if ((spell.getEffectLevel() != 1 || world.field_73011_w.field_76575_d) && (!world.field_73011_w.field_76575_d || spell.getEffectLevel() != 3)) {
            if (!world.field_73011_w.field_76575_d) {
               int x;
               if (mop.field_72313_a == MovingObjectType.ENTITY) {
                  x = MathHelper.func_76128_c(mop.field_72308_g.field_70165_t);
                  dx = MathHelper.func_76128_c(mop.field_72308_g.field_70163_u);
                  dy = MathHelper.func_76128_c(mop.field_72308_g.field_70161_v);
                  this.setBlock(caster, world, x, dx, dy, Blocks.field_150358_i);
                  this.setIfAir(caster, world, x, dx + 1, dy, Blocks.field_150358_i);
                  this.setIfAir(caster, world, x + 1, dx, dy, Blocks.field_150358_i);
                  this.setIfAir(caster, world, x - 1, dx, dy, Blocks.field_150358_i);
                  this.setIfAir(caster, world, x, dx, dy + 1, Blocks.field_150358_i);
                  this.setIfAir(caster, world, x, dx, dy - 1, Blocks.field_150358_i);
                  this.setIfAir(caster, world, x, dx - 1, dy, Blocks.field_150358_i);
               } else {
                  x = mop.field_72310_e == 5 ? 1 : (mop.field_72310_e == 4 ? -1 : 0);
                  dx = mop.field_72310_e == 0 ? -1 : (mop.field_72310_e == 1 ? 1 : 0);
                  dy = mop.field_72310_e == 3 ? 1 : (mop.field_72310_e == 2 ? -1 : 0);
                  dz = mop.field_72311_b + x;
                  int y = mop.field_72312_c + dx + (!world.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d).func_149688_o().func_76220_a() && mop.field_72310_e == 1 ? -1 : 0);
                  int z = mop.field_72309_d + dy;
                  this.setBlock(caster, world, dz, y, z, Blocks.field_150358_i);
                  this.setIfAir(caster, world, dz, y + 1, z, Blocks.field_150358_i);
                  this.setIfAir(caster, world, dz + 1, y, z, Blocks.field_150358_i);
                  this.setIfAir(caster, world, dz - 1, y, z, Blocks.field_150358_i);
                  this.setIfAir(caster, world, dz, y, z + 1, Blocks.field_150358_i);
                  this.setIfAir(caster, world, dz, y, z - 1, Blocks.field_150358_i);
                  this.setIfAir(caster, world, dz, y - 1, z, Blocks.field_150358_i);
               }
            }
         } else if (mop.field_72313_a == MovingObjectType.ENTITY) {
            this.setBlock(caster, world, MathHelper.func_76128_c(mop.field_72308_g.field_70165_t), MathHelper.func_76128_c(mop.field_72308_g.field_70163_u), MathHelper.func_76128_c(mop.field_72308_g.field_70161_v), Blocks.field_150358_i);
         } else if (mop.field_72313_a == MovingObjectType.BLOCK) {
            Block hitBlock = world.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d);
            if (hitBlock == Witchery.Blocks.CAULDRON) {
               if (Witchery.Blocks.CAULDRON.tryFillWith(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, new FluidStack(FluidRegistry.WATER, 3000))) {
               }
            } else if (hitBlock == Witchery.Blocks.KETTLE) {
               if (Witchery.Blocks.KETTLE.tryFillWith(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, new FluidStack(FluidRegistry.WATER, 1000))) {
               }
            } else {
               dx = mop.field_72310_e == 5 ? 1 : (mop.field_72310_e == 4 ? -1 : 0);
               dy = mop.field_72310_e == 0 ? -1 : (mop.field_72310_e == 1 ? 1 : 0);
               dz = mop.field_72310_e == 3 ? 1 : (mop.field_72310_e == 2 ? -1 : 0);
               this.setBlock(caster, world, mop.field_72311_b + dx, mop.field_72312_c + dy + (!world.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d).func_149688_o().func_76220_a() && mop.field_72310_e == 1 ? -1 : 0), mop.field_72309_d + dz, Blocks.field_150358_i);
            }
         }

      }

      private void setBlock(EntityLivingBase caster, World world, int x, int y, int z, Block block) {
         if (BlockProtect.checkModsForBreakOK(world, x, y, z, caster)) {
            world.func_147449_b(x, y, z, block);
         }

      }

      private void setIfAir(EntityLivingBase caster, World world, int x, int y, int z, Block block) {
         if (world.func_147437_c(x, y, z)) {
            this.setBlock(caster, world, x, y, z, block);
         }

      }
   }).setColor(1176575).setSize(2.0F), new StrokeSet(1, new byte[]{0, 0, 2, 2, 1}), new StrokeSet(1, new byte[]{0, 0, 2, 2, 2, 1}), new StrokeSet(2, new byte[]{0, 0, 0, 2, 2, 1, 1}), new StrokeSet(2, new byte[]{0, 0, 0, 2, 2, 2, 1, 1}), new StrokeSet(3, new byte[]{0, 0, 0, 0, 2, 2, 1, 1, 1}), new StrokeSet(3, new byte[]{0, 0, 0, 0, 2, 2, 2, 1, 1, 1}));
   public static final SymbolEffect Alohomora = instance().addEffect((new SymbolEffectProjectile(3, "witchery.pott.alohomora") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect effectEntity) {
         if (mop.field_72313_a == MovingObjectType.BLOCK) {
            Block blockID = world.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d);
            if (blockID != Witchery.Blocks.DOOR_ALDER && blockID != Witchery.Blocks.DOOR_ROWAN) {
               if (blockID instanceof BlockDoor) {
                  ((BlockDoor)blockID).func_150014_a(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, !((BlockDoor)blockID).func_150015_f(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d));
               }
            } else {
               ((BlockWitchDoor)blockID).onBlockActivatedNormally(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, (EntityPlayer)null, 1, (float)mop.field_72311_b, (float)mop.field_72312_c, (float)mop.field_72309_d);
            }
         }

      }
   }).setColor(5322534).setSize(0.5F), new StrokeSet(new byte[]{2, 0, 2, 2, 1}), new StrokeSet(new byte[]{2, 0, 2, 2, 2, 1}), new StrokeSet(new byte[]{2, 0, 0, 2, 2, 1, 1}), new StrokeSet(new byte[]{2, 0, 0, 2, 2, 2, 1, 1}));
   public static final SymbolEffect AvadaKedavra = instance().addEffect((new SymbolEffectProjectile(4, "witchery.pott.avadakedavra", 101, true, false, (String)null, 0, false) {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect effectEntity) {
         if (mop != null && caster != null && mop.field_72313_a == MovingObjectType.ENTITY && mop.field_72308_g instanceof EntityLivingBase) {
            if (mop.field_72308_g instanceof EntityPlayer) {
               if (world.field_72995_K || !(caster instanceof EntityPlayer) || MinecraftServer.func_71276_C().func_71219_W()) {
                  EntityPlayer hitPlayer = (EntityPlayer)mop.field_72308_g;
                  EntityUtil.instantDeath(hitPlayer, caster);
               }
            } else if (mop.field_72308_g instanceof EntityLiving) {
               EntityLiving hitCreature = (EntityLiving)mop.field_72308_g;
               if (caster instanceof EntityPlayer && ((EntityPlayer)caster).field_71075_bZ.field_75098_d) {
                  EntityUtil.instantDeath(hitCreature, caster);
               } else if ((PotionEnslaved.canCreatureBeEnslaved(hitCreature) || hitCreature instanceof EntityWitch || hitCreature instanceof EntityEnt || hitCreature instanceof EntityGolem) && hitCreature.func_110138_aP() <= 200.0F) {
                  hitCreature.func_70097_a(DamageSource.func_76354_b(effectEntity, caster), 200.0F);
               } else {
                  hitCreature.func_70097_a(DamageSource.func_76354_b(effectEntity, caster), 25.0F);
               }
            }
         }

      }
   }).setColor(65280).setSize(2.0F), new StrokeSet(new byte[]{1, 1, 2, 2, 0, 0, 3, 3, 3, 3, 1, 1, 2}));
   public static final SymbolEffect CaveInimicum = instance().addEffect((new SymbolEffectProjectile(5, "witchery.pott.caveinimicum") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect effectEntity) {
         if (mop.field_72313_a == MovingObjectType.BLOCK) {
            EffectRegistry.applyBlockEffect(world, caster, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, mop.field_72310_e, effectEntity.getEffectLevel(), new EffectRegistry.IBlockEffect() {
               public void doAction(World world, EntityLivingBase actor, int x, int y, int z, Block block, int meta) {
                  Block newBlockID = Blocks.field_150350_a;
                  if (block == Blocks.field_150346_d) {
                     newBlockID = Blocks.field_150348_b;
                  } else if (block == Blocks.field_150349_c) {
                     newBlockID = Blocks.field_150348_b;
                  } else if (block == Blocks.field_150391_bh) {
                     newBlockID = Blocks.field_150348_b;
                  } else if (block == Blocks.field_150347_e) {
                     newBlockID = Blocks.field_150348_b;
                  } else if (block == Blocks.field_150344_f) {
                     newBlockID = Blocks.field_150348_b;
                  } else if (block == Witchery.Blocks.PLANKS) {
                     newBlockID = Blocks.field_150348_b;
                  } else if (block == Blocks.field_150417_aV) {
                     newBlockID = Blocks.field_150336_V;
                  } else if (block == Blocks.field_150354_m) {
                     newBlockID = Blocks.field_150322_A;
                  } else if (block == Blocks.field_150435_aG) {
                     newBlockID = Blocks.field_150405_ch;
                  } else if (block == Blocks.field_150466_ao) {
                     int i1 = ((BlockDoor)block).func_150012_g(world, x, y, z);
                     if ((i1 & 8) != 0) {
                        --y;
                     }

                     world.func_147468_f(x, y, z);
                     world.func_147468_f(x, y + 1, z);
                     int pp1 = MathHelper.func_76128_c((double)((actor.field_70177_z + 180.0F) * 4.0F / 360.0F) - 0.5D) & 3;
                     ItemDoor.func_150924_a(world, x, y, z, pp1, Blocks.field_150454_av);
                  }

                  if (newBlockID != Blocks.field_150350_a) {
                     world.func_147449_b(x, y, z, newBlockID);
                  }

               }
            });
         }

      }
   }).setColor(3158064).setSize(3.0F), new StrokeSet(1, new byte[]{0, 3, 0, 0, 2}), new StrokeSet(1, new byte[]{0, 3, 0, 0, 0, 2}), new StrokeSet(1, new byte[]{0, 3, 3, 0, 0, 2, 2}), new StrokeSet(2, new byte[]{0, 3, 3, 0, 0, 0, 2, 2}), new StrokeSet(3, new byte[]{0, 3, 3, 3, 0, 0, 0, 0, 2, 2, 2}));
   public static final SymbolEffect Colloportus = instance().addEffect((new SymbolEffectProjectile(6, "witchery.pott.colloportus") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect effectEntity) {
         if (mop.field_72313_a == MovingObjectType.BLOCK && caster != null) {
            int y = mop.field_72312_c;
            Block blockID = world.func_147439_a(mop.field_72311_b, y, mop.field_72309_d);
            if (blockID instanceof BlockDoor) {
               int i1 = ((BlockDoor)blockID).func_150012_g(world, mop.field_72311_b, y, mop.field_72309_d);
               if ((i1 & 8) != 0) {
                  --y;
               }

               world.func_147468_f(mop.field_72311_b, y, mop.field_72309_d);
               world.func_147468_f(mop.field_72311_b, y + 1, mop.field_72309_d);
               int pp1 = MathHelper.func_76128_c((double)((caster.field_70177_z + 180.0F) * 4.0F / 360.0F) - 0.5D) & 3;
               ItemDoor.func_150924_a(world, mop.field_72311_b, y, mop.field_72309_d, pp1, Witchery.Blocks.DOOR_ROWAN);
            }
         }

      }
   }).setColor(5322534).setSize(1.0F), new StrokeSet(new byte[]{3, 3, 1, 1, 2}), new StrokeSet(new byte[]{3, 3, 1, 1, 1, 2}), new StrokeSet(new byte[]{3, 3, 3, 1, 1, 2, 2}), new StrokeSet(new byte[]{3, 3, 3, 1, 1, 2, 1, 2}));
   public static final SymbolEffect Confundus = instance().addEffect((new SymbolEffectProjectile(8, "witchery.pott.confundus") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect spell) {
         double radius = spell.getEffectLevel() == 1 ? 0.0D : (spell.getEffectLevel() == 2 ? 2.0D : 4.0D);
         EffectRegistry.applyEntityEffect(world, caster, mop, spell.field_70165_t, spell.field_70163_u, spell.field_70161_v, radius, EntityLivingBase.class, new EffectRegistry.IEntityEffect<EntityLivingBase>() {
            public void doAction(World world, EntityLivingBase actor, double x, double y, double z, EntityLivingBase target) {
               if (target instanceof EntityLivingBase && !target.func_70644_a(Potion.field_76431_k)) {
                  target.func_70690_d(new PotionEffect(Potion.field_76431_k.field_76415_H, 600));
               }

            }
         });
      }
   }).setColor(16771328).setSize(1.5F), new StrokeSet(1, new byte[]{3, 3, 0, 0, 2}), new StrokeSet(1, new byte[]{3, 3, 3, 0, 0, 2, 2}), new StrokeSet(2, new byte[]{3, 3, 3, 0, 0, 0, 2, 2}), new StrokeSet(3, new byte[]{3, 3, 3, 3, 0, 0, 0, 0, 2, 2, 2}));
   public static final SymbolEffect Crucio = instance().addEffect((new SymbolEffectProjectile(9, "witchery.pott.crucio", 5, true, false, (String)null, 0) {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect spell) {
         if (mop != null && caster != null && mop.field_72313_a == MovingObjectType.ENTITY && mop.field_72308_g instanceof EntityLivingBase) {
            if (mop.field_72308_g instanceof EntityPlayer) {
               if (world.field_72995_K || !(caster instanceof EntityPlayer) || MinecraftServer.func_71276_C().func_71219_W()) {
                  EntityPlayer hitPlayer = (EntityPlayer)mop.field_72308_g;
                  hitPlayer.func_70097_a(DamageSource.func_76354_b(spell, caster), (float)(4 + 4 * (spell.getEffectLevel() - 1)));
               }
            } else if (mop.field_72308_g instanceof EntityLiving) {
               EntityLiving hitCreature = (EntityLiving)mop.field_72308_g;
               hitCreature.func_70097_a(DamageSource.func_76354_b(spell, caster), 4.0F);
            }
         }

      }
   }).setColor(6684927).setSize(2.0F), new StrokeSet(1, new byte[]{1, 3, 1, 1, 2}), new StrokeSet(1, new byte[]{1, 3, 3, 1, 1, 2, 2}), new StrokeSet(2, new byte[]{1, 3, 1, 1, 1, 2}), new StrokeSet(2, new byte[]{1, 3, 3, 1, 1, 1, 2, 2}), new StrokeSet(3, new byte[]{1, 3, 3, 3, 1, 1, 1, 1, 2, 2, 2}));
   public static final SymbolEffect Defodio = instance().addEffect((new SymbolEffectProjectile(10, "witchery.pott.defodio", 3, false, false, (String)null, 0) {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect effectEntity) {
         if (mop.field_72313_a == MovingObjectType.BLOCK) {
            EffectRegistry.applyBlockEffect(world, caster, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, mop.field_72310_e, effectEntity.getEffectLevel(), new EffectRegistry.IBlockEffect() {
               public void doAction(World world, EntityLivingBase actor, int x, int y, int z, Block block, int meta) {
                  Material material = block.func_149688_o();
                  if (material == Material.field_151571_B || material == Material.field_151596_z || material == Material.field_151578_c || material == Material.field_151577_b || material == Material.field_151588_w || material == Material.field_151576_e || material == Material.field_151595_p) {
                     world.func_147468_f(x, y, z);
                     Item itemBlock = null;
                     byte itemDamageValuex = -1;

                     try {
                        itemBlock = block.func_149650_a(meta, world.field_73012_v, 0);
                        int itemDamageValue = block.func_149692_a(meta);
                        int quantity = block.quantityDropped(meta, 0, world.field_73012_v);
                        if (itemBlock != null && itemDamageValue >= 0 && quantity > 0) {
                           world.func_72838_d(new EntityItem(world, 0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z, new ItemStack(itemBlock, quantity, itemDamageValue)));
                        }
                     } catch (Throwable var12) {
                        Log.instance().warning(var12, "Exception occured while spawning block as part of Defodio effect: new (" + itemBlock + ", " + itemDamageValuex + ") old (" + block + ", " + meta + ")");
                     }
                  }

               }
            });
         }

      }
   }).setColor(4008220).setSize(2.5F), new StrokeSet(1, new byte[]{0, 0, 3, 1}), new StrokeSet(1, new byte[]{0, 0, 0, 3, 1, 1}), new StrokeSet(1, new byte[]{0, 0, 3, 3, 1, 2}), new StrokeSet(2, new byte[]{0, 0, 0, 3, 3, 1, 1, 2}), new StrokeSet(2, new byte[]{0, 0, 0, 0, 3, 3, 1, 1, 1, 2}), new StrokeSet(2, new byte[]{0, 0, 0, 3, 3, 3, 1, 1, 2, 2}), new StrokeSet(3, new byte[]{0, 0, 0, 0, 3, 3, 3, 1, 1, 1, 2, 2}));
   public static final SymbolEffect Ennervate = instance().addEffect((new SymbolEffectProjectile(12, "witchery.pott.ennervate", 1, false, true, (String)null, 0) {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect spell) {
         double radius = spell.getEffectLevel() == 1 ? 0.0D : (spell.getEffectLevel() == 2 ? 2.0D : 4.0D);
         EffectRegistry.applyEntityEffect(world, caster, mop, spell.field_70165_t, spell.field_70163_u, spell.field_70161_v, radius, EntityLivingBase.class, new EffectRegistry.IEntityEffect<EntityLivingBase>() {
            public void doAction(World world, EntityLivingBase actor, double x, double y, double z, EntityLivingBase target) {
               if (target.func_70644_a(Potion.field_76421_d)) {
                  target.func_82170_o(Potion.field_76421_d.field_76415_H);
               }

               if (target.func_70644_a(Potion.field_76419_f)) {
                  target.func_82170_o(Potion.field_76419_f.field_76415_H);
               }

               if (target.func_70644_a(Potion.field_76431_k)) {
                  target.func_82170_o(Potion.field_76431_k.field_76415_H);
               }

            }
         });
      }
   }).setColor(16713595).setSize(1.5F), new StrokeSet(1, new byte[]{0, 3, 0, 2, 3, 0, 2}), new StrokeSet(2, new byte[]{0, 3, 3, 0, 2, 2, 3, 3, 0, 2, 2}), new StrokeSet(3, new byte[]{0, 3, 3, 3, 0, 2, 2, 2, 3, 3, 3, 0, 2, 2, 2}));
   public static final SymbolEffect Episkey = instance().addEffect(new SymbolEffect(13, "witchery.pott.episkey", 1, false, false, (String)null, 0) {
      public void perform(World world, EntityPlayer player, int effectLevel) {
         double radius = effectLevel == 1 ? 0.0D : (effectLevel == 2 ? 2.0D : 4.0D);
         MovingObjectPosition mop = new MovingObjectPosition(player);
         EffectRegistry.applyEntityEffect(world, player, mop, player.field_70165_t, player.field_70163_u, player.field_70161_v, radius, EntityLivingBase.class, new EffectRegistry.IEntityEffect<EntityLivingBase>() {
            public void doAction(World world, EntityLivingBase actor, double x, double y, double z, EntityLivingBase target) {
               boolean hasFood = target instanceof EntityPlayer;
               int currentFood = hasFood ? ((EntityPlayer)target).func_71024_bL().func_75116_a() : 5;
               if (currentFood > 1 && target.func_110143_aJ() < target.func_110138_aP()) {
                  target.func_70691_i((float)Math.min(5, currentFood));
                  if (hasFood) {
                     ((EntityPlayer)target).func_71024_bL().func_75122_a(-Math.min(5, currentFood), 0.0F);
                  }

                  if (!target.func_70644_a(Potion.field_76431_k)) {
                     target.func_70690_d(new PotionEffect(Potion.field_76431_k.field_76415_H, TimeUtil.secsToTicks(4)));
                  }

                  ParticleEffect.SPLASH.send(SoundEffect.MOB_SLIME_SMALL, target, 1.0D, 1.0D, 16);
               }

            }
         });
      }
   }, new StrokeSet(1, new byte[]{2, 0, 3, 1, 1, 2}), new StrokeSet(2, new byte[]{2, 0, 0, 3, 1, 1, 1, 1, 2}), new StrokeSet(2, new byte[]{2, 2, 0, 3, 3, 1, 1, 2, 2}), new StrokeSet(3, new byte[]{2, 2, 0, 0, 3, 3, 1, 1, 1, 1, 2, 2}));
   public static final SymbolEffect Expelliarmus = instance().addEffect((new SymbolEffectProjectile(15, "witchery.pott.expelliarmus") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect spell) {
         double radius = spell.getEffectLevel() == 1 ? 0.0D : (spell.getEffectLevel() == 2 ? 3.0D : 5.0D);
         EffectRegistry.applyEntityEffect(world, caster, mop, spell.field_70165_t, spell.field_70163_u, spell.field_70161_v, radius, EntityLivingBase.class, new EffectRegistry.IEntityEffect<EntityLivingBase>() {
            public void doAction(World world, EntityLivingBase actor, double x, double y, double z, EntityLivingBase target) {
               if (actor != target) {
                  disarm(target);
               }

            }
         });
      }

      private void disarm(EntityLivingBase target) {
         if (target instanceof EntityPlayer) {
            EntityPlayer playerVictim = (EntityPlayer)target;
            if (playerVictim.field_71070_bA == null || playerVictim.field_71070_bA.field_75152_c == 0) {
               int heldItemIndex = playerVictim.field_71071_by.field_70461_c;
               if (playerVictim.field_71071_by.field_70462_a[heldItemIndex] != null) {
                  playerVictim.func_71019_a(playerVictim.field_71071_by.field_70462_a[heldItemIndex], true);
                  playerVictim.field_71071_by.field_70462_a[heldItemIndex] = null;
               }
            }
         } else if (!PotionIllFitting.isTargetBanned(target)) {
            ItemStack heldItem = target.func_70694_bm();
            if (heldItem != null) {
               if (target instanceof EntityPlayer) {
                  Infusion.dropEntityItemWithRandomChoice(target, heldItem, true);
               } else {
                  target.func_70099_a(heldItem, 0.5F);
               }

               target.func_70062_b(0, (ItemStack)null);
            }
         }

      }
   }).setColor(16747778).setSize(3.0F), new StrokeSet(1, new byte[]{0, 0, 1}), new StrokeSet(1, new byte[]{0, 0, 0, 1, 1}), new StrokeSet(2, new byte[]{0, 0, 0, 0, 1, 1, 1}), new StrokeSet(3, new byte[]{0, 0, 0, 0, 0, 1, 1, 1, 1}));
   public static final SymbolEffect Flagrate = instance().addEffect(new SymbolEffect(16, "witchery.pott.flagrate", 1, false, false, (String)null, 0, false) {
      public void perform(World world, EntityPlayer player, int effectLevel) {
         MovingObjectPosition mop = InfusionOtherwhere.doCustomRayTrace(world, player, true, 4.0D);
         if (mop != null) {
            if (mop.field_72313_a == MovingObjectType.BLOCK) {
               ItemChalk.drawGlyph(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, mop.field_72310_e, Witchery.Blocks.GLYPH_INFERNAL, player);
            } else {
               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }
         } else {
            SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
         }

      }
   }, new StrokeSet(new byte[]{2, 0, 2, 3, 0, 2}));
   public static final SymbolEffect Flipendo = instance().addEffect((new SymbolEffectProjectile(17, "witchery.pott.flipendo") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect spell) {
         final double radius = spell.getEffectLevel() == 1 ? 0.0D : (spell.getEffectLevel() == 2 ? 3.0D : 6.0D);
         final double spellX = spell.field_70159_w;
         final double spellZ = spell.field_70179_y;
         EffectRegistry.applyEntityEffect(world, caster, mop, spell.field_70165_t, spell.field_70163_u, spell.field_70161_v, radius, EntityLivingBase.class, new EffectRegistry.IEntityEffect<EntityLivingBase>() {
            public void doAction(World world, EntityLivingBase actor, double x, double y, double z, EntityLivingBase target) {
               if (radius == 3.0D || target != actor) {
                  double ACCELERATION = 2.0D;
                  if (target.func_70644_a(Potion.field_76421_d)) {
                     ACCELERATION += 0.5D;
                  }

                  double motionX = spellX * ACCELERATION;
                  double motionY = 0.3D;
                  double motionZ = spellZ * ACCELERATION;
                  if (target instanceof EntityPlayer) {
                     EntityPlayer targetPlayer = (EntityPlayer)target;
                     Witchery.packetPipeline.sendTo((IMessage)(new PacketPushTarget(motionX, 0.3D, motionZ)), (EntityPlayer)targetPlayer);
                  } else {
                     target.field_70159_w = motionX;
                     target.field_70181_x = 0.3D;
                     target.field_70179_y = motionZ;
                  }
               }

            }
         });
      }
   }).setColor(16775577).setSize(3.0F), new StrokeSet(1, new byte[]{2, 2, 3}), new StrokeSet(1, new byte[]{2, 2, 2, 3, 3}), new StrokeSet(2, new byte[]{2, 2, 2, 2, 3, 3, 3}), new StrokeSet(3, new byte[]{2, 2, 2, 2, 2, 3, 3, 3, 3}));
   public static final SymbolEffect Impedimenta = instance().addEffect((new SymbolEffectProjectile(19, "witchery.pott.impedimenta") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect spell) {
         double radius = spell.getEffectLevel() == 1 ? 0.0D : (spell.getEffectLevel() == 2 ? 3.0D : 6.0D);
         double spellX = spell.field_70159_w;
         double spellZ = spell.field_70179_y;
         EffectRegistry.applyEntityEffect(world, caster, mop, spell.field_70165_t, spell.field_70163_u, spell.field_70161_v, radius, EntityLivingBase.class, new EffectRegistry.IEntityEffect<EntityLivingBase>() {
            public void doAction(World world, EntityLivingBase actor, double x, double y, double z, EntityLivingBase target) {
               if (target != actor && !target.func_70644_a(Potion.field_76421_d)) {
                  target.func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, 600, 1));
               }

            }
         });
      }
   }).setColor(6191615).setSize(1.5F), new StrokeSet(1, new byte[]{3, 3, 2}), new StrokeSet(1, new byte[]{3, 3, 3, 2, 2}), new StrokeSet(2, new byte[]{3, 3, 3, 3, 2, 2, 2}), new StrokeSet(3, new byte[]{3, 3, 3, 3, 3, 2, 2, 2, 2}));
   public static final SymbolEffect Imperio = instance().addEffect((new SymbolEffectProjectile(20, "witchery.pott.imperio", 10, true, false, (String)null, 0) {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect effectEntity) {
         if (mop != null && caster != null && caster instanceof EntityPlayer && mop.field_72313_a == MovingObjectType.ENTITY && mop.field_72308_g instanceof EntityLivingBase) {
            EntityLivingBase entityLiving = (EntityLivingBase)mop.field_72308_g;
            if (PotionEnslaved.canCreatureBeEnslaved(entityLiving)) {
               EntityPlayer player = (EntityPlayer)caster;
               EntityLiving creature = (EntityLiving)entityLiving;
               NBTTagCompound nbt = entityLiving.getEntityData();
               if (PotionEnslaved.setEnslaverForMob(creature, player)) {
                  ParticleEffect.SPELL.send(SoundEffect.MOB_ZOMBIE_INFECT, creature, 1.0D, 2.0D, 8);
               }
            }
         }

      }
   }).setColor(10686463).setSize(1.5F), new StrokeSet(new byte[]{2, 1, 1, 1, 1}));
   public static final SymbolEffect Incendio = instance().addEffect((new SymbolEffectProjectile(21, "witchery.pott.incendio") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect spell) {
         double radius = spell.getEffectLevel() == 1 ? 0.0D : (spell.getEffectLevel() == 2 ? 3.0D : 6.0D);
         final int level = spell.getEffectLevel();
         if (radius == 0.0D) {
            if (mop.field_72313_a == MovingObjectType.ENTITY) {
               mop.field_72308_g.func_70015_d(1);
               mop.field_72308_g.func_70097_a((new EntityDamageSourceIndirect("onFire", spell, caster)).func_76361_j(), 0.1F);
            } else if (mop.field_72313_a == MovingObjectType.BLOCK) {
               Block hitBlock = BlockUtil.getBlock(world, mop);
               if (hitBlock == Witchery.Blocks.WICKER_BUNDLE && BlockWickerBundle.limitToValidMetadata(world.func_72805_g(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d)) == 1) {
                  if (BlockWickerBundle.tryIgniteMan(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, caster != null ? caster.field_70177_z : 0.0F)) {
                     return;
                  }
               } else if (hitBlock == Witchery.Blocks.BRAZIER) {
                  BlockBrazier.tryIgnite(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d);
                  return;
               }

               int dx = mop.field_72310_e == 5 ? 1 : (mop.field_72310_e == 4 ? -1 : 0);
               int dy = mop.field_72310_e == 0 ? -1 : (mop.field_72310_e == 1 ? 1 : 0);
               int dz = mop.field_72310_e == 3 ? 1 : (mop.field_72310_e == 2 ? -1 : 0);
               world.func_147449_b(mop.field_72311_b + dx, mop.field_72312_c + dy + (!world.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d).func_149688_o().func_76220_a() && mop.field_72310_e == 1 ? -1 : 0), mop.field_72309_d + dz, Blocks.field_150480_ab);
            }
         } else {
            EffectRegistry.applyEntityEffect(world, caster, mop, spell.field_70165_t, spell.field_70163_u, spell.field_70161_v, radius, EntityLivingBase.class, new EffectRegistry.IEntityEffect<EntityLivingBase>() {
               public void doAction(World world, EntityLivingBase actor, double x, double y, double z, EntityLivingBase target) {
                  if (target != actor) {
                     target.func_70015_d(level);
                  }

               }
            });
            if (mop != null && mop.field_72313_a == MovingObjectType.BLOCK) {
               final int side = mop.field_72310_e;
               EffectRegistry.applyBlockEffect(world, caster, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, mop.field_72310_e, level, new EffectRegistry.IBlockEffect() {
                  public void doAction(World world, EntityLivingBase actor, int x, int y, int z, Block block, int meta) {
                     if (side == 1) {
                        int dx = side == 5 ? 1 : (side == 4 ? -1 : 0);
                        int dy = side == 0 ? -1 : (side == 1 ? 1 : 0);
                        int dz = side == 3 ? 1 : (side == 2 ? -1 : 0);
                        int nX = x + dx;
                        int nY = y + dy;
                        int nZ = z + dz;
                        if (world.func_147437_c(nX, nY, nZ)) {
                           world.func_147449_b(nX, nY, nZ, Blocks.field_150480_ab);
                        }
                     }

                  }
               });
            }
         }

      }
   }).setColor(16724023).setSize(2.0F), new StrokeSet(1, new byte[]{3, 0, 0, 1, 1}), new StrokeSet(2, new byte[]{3, 0, 0, 0, 1, 1, 1}), new StrokeSet(3, new byte[]{3, 0, 0, 0, 0, 1, 1, 1, 1}));
   public static final SymbolEffect Lumos = instance().addEffect((new SymbolEffectProjectile(22, "witchery.pott.lumos") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect effectEntity) {
         if (mop.field_72313_a == MovingObjectType.BLOCK) {
            int dx = mop.field_72310_e == 5 ? 1 : (mop.field_72310_e == 4 ? -1 : 0);
            int dy = mop.field_72310_e == 0 ? -1 : (mop.field_72310_e == 1 ? 1 : 0);
            int dz = mop.field_72310_e == 3 ? 1 : (mop.field_72310_e == 2 ? -1 : 0);
            int x = mop.field_72311_b + 1 * dx;
            int y = mop.field_72312_c + 1 * dy;
            int z = mop.field_72309_d + 1 * dz;
            Material material = world.func_147439_a(x, y, z).func_149688_o();
            if (material == Material.field_151579_a || material == Material.field_151597_y) {
               world.func_147449_b(x, y, z, Witchery.Blocks.GLOW_GLOBE);
            }
         }

      }
   }).setColor(16777018).setSize(0.5F), new StrokeSet(new byte[]{1, 1, 1, 2}));
   public static final SymbolEffect MeteolojinxRecanto = instance().addEffect(new SymbolEffect(23, "witchery.pott.meteolojinxrecanto", 100, false, false, (String)null, 0) {
      public void perform(World world, EntityPlayer player, int effectLevel) {
         InfusionOtherwhere.doCustomRayTrace(world, player, true, 4.0D);
         if (world.func_72896_J()) {
            WorldServer worldserver = MinecraftServer.func_71276_C().field_71305_c[0];
            if (worldserver != null) {
               WorldInfo worldinfo = worldserver.func_72912_H();
               worldinfo.func_76084_b(false);
               worldinfo.func_76069_a(false);
            }
         } else {
            SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
         }

      }
   }, new StrokeSet(new byte[]{0, 0, 0, 2, 2, 1, 0, 2, 2, 1, 1}));
   public static final SymbolEffect Nox = instance().addEffect(new SymbolEffect(26, "witchery.pott.nox", 50, false, false, (String)null, 0) {
      public void perform(World world, EntityPlayer player, int effectLevel) {
         int x0 = MathHelper.func_76128_c(player.field_70165_t);
         int y0 = MathHelper.func_76128_c(player.field_70163_u);
         int z0 = MathHelper.func_76128_c(player.field_70161_v);
         int radius = 10;

         for(int y = y0 - radius; y <= y0 + radius; ++y) {
            for(int x = x0 - radius; x <= x0 + radius; ++x) {
               for(int z = z0 - radius; z <= z0 + radius; ++z) {
                  Block blockID = world.func_147439_a(x, y, z);
                  if ((double)blockID.getLightValue(world, x, y, z) > 0.8D && BlockProtect.canBreak(blockID, world)) {
                     int blockMeta = world.func_72805_g(x, y, z);
                     if (BlockProtect.checkModsForBreakOK(world, x, y, z, blockID, blockMeta, player)) {
                        world.func_147468_f(x, y, z);
                        if (blockID.func_149745_a(world.field_73012_v) > 0) {
                           blockID.func_149697_b(world, x, y, z, blockMeta, 0);
                        }
                     }
                  }
               }
            }
         }

      }
   }, new StrokeSet(new byte[]{0, 0, 2, 1, 2, 0}));
   public static final SymbolEffect Protego = instance().addEffect(new SymbolEffect(31, "witchery.pott.protego") {
      public void perform(World world, EntityPlayer player, int effectLevel) {
         MovingObjectPosition mop = InfusionOtherwhere.doCustomRayTrace(world, player, true, 4.0D);
         if (mop != null) {
            if (mop.field_72313_a == MovingObjectType.BLOCK) {
               InfusionLight.placeBarrierShield(world, player, mop);
            } else {
               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }
         } else {
            SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
         }

      }
   }, new StrokeSet(new byte[]{1, 1, 0}), new StrokeSet(new byte[]{1, 1, 1, 0, 0}), new StrokeSet(new byte[]{1, 1, 1, 1, 0, 0, 0}));
   public static final SymbolEffect Stupefy = instance().addEffect((new SymbolEffectProjectile(36, "witchery.pott.stupefy", 5, false, true, (String)null, 0) {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect effectEntity) {
         if (mop != null && mop.field_72313_a == MovingObjectType.ENTITY && mop.field_72308_g instanceof EntityLivingBase) {
            EntityLivingBase entityLiving = (EntityLivingBase)mop.field_72308_g;
            if (!entityLiving.func_70644_a(Potion.field_76421_d)) {
               entityLiving.func_70690_d(new PotionEffect(Potion.field_76421_d.field_76415_H, 6000, 9));
            }
         }

      }
   }).setColor(1279).setSize(1.5F), new StrokeSet(1, new byte[]{2, 2, 0, 3, 0, 2}), new StrokeSet(1, new byte[]{2, 2, 2, 0, 3, 3, 0, 2, 2}), new StrokeSet(2, new byte[]{2, 2, 0, 0, 3, 0, 0, 2}), new StrokeSet(2, new byte[]{2, 2, 2, 0, 0, 3, 3, 0, 0, 2, 2}));
   public static final SymbolEffect Ignianima = instance().addEffect((new SymbolEffectProjectile(39, "witchery.pott.ignianima", 2, true, false, "ignianima", 0) {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect e) {
         double R = 1.5D;
         double R_SQ = 2.25D;
         AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(e.field_70165_t - 1.5D, e.field_70163_u - 1.5D, e.field_70161_v - 1.5D, e.field_70165_t + 1.5D, e.field_70163_u + 1.5D, e.field_70161_v + 1.5D);
         List entities = world.func_72872_a(EntityLivingBase.class, bounds);
         Iterator i$ = entities.iterator();

         while(i$.hasNext()) {
            Object hit = i$.next();
            EntityLivingBase hitEntity = (EntityLivingBase)hit;
            if (e.func_70068_e(hitEntity) <= 2.25D) {
               float damage = 4.0F;
               float scale = hitEntity instanceof EntityPlayer ? hitEntity.func_110138_aP() / 20.0F : 1.0F;
               float health;
               if (caster != null) {
                  health = 20.0F * (caster.func_110143_aJ() / caster.func_110138_aP());
                  if (health > 19.0F) {
                     damage = 2.0F;
                  } else if (health > 15.0F) {
                     damage = 3.0F;
                  } else if (health > 10.0F) {
                     damage = 5.0F;
                  } else {
                     damage = 6.0F + (12.0F - health) / 2.0F;
                  }
               }

               health = damage * scale;
               hitEntity.func_70097_a(new DemonicDamageSource(caster), health);
               ParticleEffect.FLAME.send(SoundEffect.FIRE_IGNITE, hitEntity, 1.0D, 2.0D, 16);
            }
         }

      }
   }).setColor(16770912).setSize(3.0F), new StrokeSet(new byte[]{3, 3, 0, 1, 1}), new StrokeSet(new byte[]{3, 3, 0, 0, 1, 1, 1, 1}), new StrokeSet(new byte[]{3, 3, 3, 0, 1, 1}), new StrokeSet(new byte[]{3, 3, 3, 0, 0, 1, 1, 1, 1}), new StrokeSet(new byte[]{3, 3, 3, 3, 0, 1, 1}), new StrokeSet(new byte[]{3, 3, 3, 3, 0, 0, 1, 1, 1, 1}));
   public static final SymbolEffect CarnosaDiem = instance().addEffect(new SymbolEffect(40, "witchery.pott.carnosadiem", 1, true, false, "carnosadiem", 0) {
      public void perform(World world, EntityPlayer player, int effectLevel) {
         float damage = player.func_110138_aP() * 0.1F;
         player.func_70097_a(new DemonicDamageSource(player), damage);
         ParticleEffect.REDDUST.send(SoundEffect.MOB_ENDERDRAGON_GROWL, player, 1.0D, 2.0D, 16);
         int currentPower = Infusion.getCurrentEnergy(player);
         int maxPower = Infusion.getMaxEnergy(player);
         Infusion.setCurrentEnergy(player, Math.min(currentPower + 10, maxPower));
         Witchery.modHooks.boostBloodPowers(player, damage);
      }
   }, new StrokeSet(new byte[]{2, 2, 0, 1, 1}), new StrokeSet(new byte[]{2, 2, 0, 0, 1, 1, 1, 1}), new StrokeSet(new byte[]{2, 2, 2, 0, 1, 1}), new StrokeSet(new byte[]{2, 2, 2, 0, 0, 1, 1, 1, 1}), new StrokeSet(new byte[]{2, 2, 2, 2, 0, 1, 1}), new StrokeSet(new byte[]{2, 2, 2, 2, 0, 0, 1, 1, 1, 1}));
   public static final SymbolEffect MORSMORDRE = instance().addEffect((new SymbolEffectProjectile(41, "witchery.pott.morsmordre", 20, true, false, "morsmordre", 0) {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect effectEntity) {
         if (!world.field_72995_K) {
            EntityDarkMark entity = new EntityDarkMark(world);
            entity.func_70012_b(effectEntity.field_70165_t, effectEntity.field_70163_u, effectEntity.field_70161_v, 0.0F, 0.0F);
            entity.func_110163_bv();
            world.func_72838_d(entity);
         }

      }
   }).setColor(0).setSize(3.0F).setTimeToLive(8), new StrokeSet(new byte[]{0, 0, 3, 2, 2}), new StrokeSet(new byte[]{0, 0, 3, 3, 2, 2, 2, 2}), new StrokeSet(new byte[]{0, 0, 0, 3, 2, 2}), new StrokeSet(new byte[]{0, 0, 0, 3, 3, 2, 2, 2, 2}), new StrokeSet(new byte[]{0, 0, 0, 0, 3, 2, 2}), new StrokeSet(new byte[]{0, 0, 0, 0, 3, 3, 2, 2, 2, 2}));
   public static final SymbolEffect Tormentum = instance().addEffect((new SymbolEffectProjectile(42, "witchery.pott.tormentum", 25, true, true, "tormentum", TimeUtil.minsToTicks(30)) {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect e) {
         if (!world.field_72995_K && e.field_71093_bK != Config.instance().dimensionTormentID) {
            double R = 2.0D;
            AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(e.field_70165_t - 2.0D, e.field_70163_u - 2.0D, e.field_70161_v - 2.0D, e.field_70165_t + 2.0D, e.field_70163_u + 2.0D, e.field_70161_v + 2.0D);
            List entities = world.func_72872_a(EntityLivingBase.class, bounds);
            boolean setCooldown = false;
            Iterator i$ = entities.iterator();

            while(i$.hasNext()) {
               Object hitEntity = i$.next();
               if (hitEntity instanceof EntityPlayer) {
                  EntityPlayer player = (EntityPlayer)hitEntity;
                  WorldProviderTorment.setPlayerMustTorment((EntityPlayer)player, 1, -1);
                  setCooldown = true;
               } else if (hitEntity instanceof EntityLiving && !(hitEntity instanceof IBossDisplayData)) {
                  EntityLiving hitLiving = (EntityLiving)hitEntity;
                  hitLiving.func_70106_y();
                  setCooldown = true;
               }
            }

            if (setCooldown && caster != null && caster instanceof EntityPlayer) {
               this.setOnCooldown((EntityPlayer)caster);
            }
         }

      }
   }).setColor(2236962).setSize(4.0F), new StrokeSet(new byte[]{1, 1, 3, 2, 2}), new StrokeSet(new byte[]{1, 1, 3, 3, 2, 2, 2, 2}), new StrokeSet(new byte[]{1, 1, 1, 3, 2, 2}), new StrokeSet(new byte[]{1, 1, 1, 3, 3, 2, 2, 2, 2}), new StrokeSet(new byte[]{1, 1, 1, 1, 3, 2, 2}), new StrokeSet(new byte[]{1, 1, 1, 1, 3, 3, 2, 2, 2, 2}));
   public static final SymbolEffect LEONARD_1 = instance().addEffect(new SymbolEffect(43, "witchery.pott.leonard1", 5, false, false, (String)null, 0) {
      public void perform(World world, EntityPlayer player, int level) {
         EffectRegistry.castLeonardSpell(world, player, 0);
      }

      public int getChargeCost(World world, EntityPlayer player, int level) {
         return EffectRegistry.costOfLeonardSpell(world, player, 0);
      }
   }, new StrokeSet(new byte[]{2, 0, 3, 3, 1}));
   public static final SymbolEffect LEONARD_2 = instance().addEffect(new SymbolEffect(44, "witchery.pott.leonard2", 5, false, false, (String)null, 0) {
      public void perform(World world, EntityPlayer player, int level) {
         EffectRegistry.castLeonardSpell(world, player, 1);
      }

      public int getChargeCost(World world, EntityPlayer player, int level) {
         return EffectRegistry.costOfLeonardSpell(world, player, 1);
      }
   }, new StrokeSet(new byte[]{3, 1, 2, 2, 0}));
   public static final SymbolEffect LEONARD_3 = instance().addEffect(new SymbolEffect(45, "witchery.pott.leonard3", 5, false, false, (String)null, 0) {
      public void perform(World world, EntityPlayer player, int level) {
         EffectRegistry.castLeonardSpell(world, player, 2);
      }

      public int getChargeCost(World world, EntityPlayer player, int level) {
         return EffectRegistry.costOfLeonardSpell(world, player, 2);
      }
   }, new StrokeSet(new byte[]{1, 2, 0, 0, 3}));
   public static final SymbolEffect LEONARD_4 = instance().addEffect(new SymbolEffect(46, "witchery.pott.leonard4", 5, false, false, (String)null, 0) {
      public void perform(World world, EntityPlayer player, int level) {
         EffectRegistry.castLeonardSpell(world, player, 3);
      }

      public int getChargeCost(World world, EntityPlayer player, int level) {
         return EffectRegistry.costOfLeonardSpell(world, player, 3);
      }
   }, new StrokeSet(new byte[]{0, 3, 1, 1, 2}));
   public static final SymbolEffect Attraho = instance().addEffect((new SymbolEffectProjectile(47, "witchery.pott.attraho") {
      public void onCollision(World world, EntityLivingBase caster, MovingObjectPosition mop, EntitySpellEffect spell) {
         if (caster != null && mop != null) {
            double R = spell.getEffectLevel() == 1 ? 2.0D : (spell.getEffectLevel() == 2 ? 3.0D : 9.0D);
            double R_SQ = R * R;
            AxisAlignedBB bb = AxisAlignedBB.func_72330_a(spell.field_70165_t - R, spell.field_70163_u - R, spell.field_70161_v - R, spell.field_70165_t + R, spell.field_70163_u + R, spell.field_70161_v + R);
            List<EntityLivingBase> entities = world.func_72872_a(EntityLivingBase.class, bb);
            Iterator i$ = entities.iterator();

            while(i$.hasNext()) {
               EntityLivingBase entity = (EntityLivingBase)i$.next();
               if (entity.func_70068_e(spell) <= R_SQ) {
                  EntityUtil.pullTowards(world, entity, new EntityPosition(caster), 0.04D, 0.1D);
               }
            }
         }

      }
   }).setColor(5322534).setSize(1.0F), new StrokeSet(1, new byte[]{0, 0, 0, 2, 2, 1, 3}));

   public static final EffectRegistry instance() {
      return INSTANCE;
   }

   private EffectRegistry() {
   }

   public SymbolEffect addEffect(SymbolEffect effect, StrokeSet... strokeSets) {
      StrokeSet[] arr$ = strokeSets;
      int len$ = strokeSets.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         StrokeSet strokes = arr$[i$];
         strokes.addTo(this.effects, this.enhanced, effect);
      }

      this.effectID.put(effect.getEffectID(), effect);
      strokeSets[0].setDefaultFor(effect);
      this.allEffects.add(effect);
      return effect;
   }

   public boolean contains(byte[] strokes) {
      return this.getEffect(strokes) != null;
   }

   public SymbolEffect getEffect(byte[] strokes) {
      return (SymbolEffect)this.effects.get(ByteBuffer.wrap(strokes));
   }

   public SymbolEffect getEffect(int effectID) {
      return (SymbolEffect)this.effectID.get(effectID);
   }

   public int getLevel(byte[] strokes) {
      return (Integer)this.enhanced.get(ByteBuffer.wrap(strokes));
   }

   public ArrayList<SymbolEffect> getEffects() {
      return this.allEffects;
   }

   private static <T extends Entity> void applyEntityEffect(World world, EntityLivingBase actor, MovingObjectPosition mop, double xMid, double yMid, double zMid, double radius, Class<T> clazz, EffectRegistry.IEntityEffect<T> effect) {
      if (radius == 0.0D) {
         if (mop != null && mop.field_72313_a == MovingObjectType.ENTITY && mop.field_72308_g != null && clazz.isAssignableFrom(mop.field_72308_g.getClass())) {
            effect.doAction(world, actor, xMid, yMid, zMid, mop.field_72308_g);
         }
      } else {
         double R_SQ = radius * radius;
         AxisAlignedBB bb = AxisAlignedBB.func_72330_a(xMid - radius, yMid - radius, zMid - radius, xMid + radius, yMid + radius, zMid + radius);
         List entities = world.func_72872_a(clazz, bb);
         Iterator i$ = entities.iterator();

         while(i$.hasNext()) {
            Object obj = i$.next();
            T entity = (Entity)obj;
            if (entity.func_70092_e(xMid, yMid, zMid) <= R_SQ) {
               effect.doAction(world, actor, entity.field_70165_t, entity.field_70163_u, entity.field_70161_v, entity);
            }
         }
      }

   }

   private static void applyBlockEffect(World world, EntityLivingBase actor, int midX, int midY, int midZ, int side, int radius, EffectRegistry.IBlockEffect effect) {
      int x;
      if (radius == 1) {
         Block block = world.func_147439_a(midX, midY, midZ);
         x = world.func_72805_g(midX, midY, midZ);
         if (block != Blocks.field_150350_a && BlockProtect.canBreak(block, world) && BlockProtect.checkModsForBreakOK(world, midX, midY, midZ, block, x, actor)) {
            effect.doAction(world, actor, midX, midY, midZ, block, x);
         }
      } else {
         int r = Math.min(radius - 1, 3);
         x = midX;
         int y = midY;
         int z = midZ;

         for(int k = -r; k <= r; ++k) {
            for(int j = -r; j <= r; ++j) {
               switch(side) {
               case 0:
               case 1:
                  x = midX + k;
                  z = midZ + j;
                  break;
               case 2:
               case 3:
                  x = midX + k;
                  y = midY + j;
                  break;
               case 4:
               case 5:
                  y = midY + k;
                  z = midZ + j;
               }

               Block block = world.func_147439_a(x, y, z);
               int meta = world.func_72805_g(x, y, z);
               if (block != Blocks.field_150350_a && BlockProtect.canBreak(block, world) && BlockProtect.checkModsForBreakOK(world, x, y, z, block, meta, actor)) {
                  effect.doAction(world, actor, x, y, z, block, meta);
               }
            }
         }
      }

   }

   private static int costOfLeonardSpell(World world, EntityPlayer player, int spellSlot) {
      int slot = InvUtil.getSlotContainingItem(player.field_71071_by, Witchery.Items.LEONARDS_URN);
      if (slot >= 0 && slot < player.field_71071_by.func_70302_i_()) {
         ItemStack urnStack = player.field_71071_by.func_70301_a(slot);
         if (urnStack != null) {
            ItemLeonardsUrn.InventoryLeonardsUrn inv = new ItemLeonardsUrn.InventoryLeonardsUrn(player, urnStack);
            if (urnStack.func_77960_j() >= spellSlot) {
               ItemStack potion = inv.func_70301_a(spellSlot);
               if (potion != null) {
                  int baseLevel = WitcheryBrewRegistry.INSTANCE.getUsedCapacity(potion.func_77978_p());
                  if (player.func_70644_a(Witchery.Potions.WORSHIP)) {
                     PotionEffect effect = player.func_70660_b(Witchery.Potions.WORSHIP);
                     if (effect.func_76458_c() < 1) {
                        baseLevel += (int)Math.ceil((double)baseLevel * 0.5D);
                     }
                  } else {
                     baseLevel *= 2;
                  }

                  return Math.max(baseLevel, 4);
               }
            }
         }
      }

      return 5;
   }

   private static void castLeonardSpell(World world, EntityPlayer player, int spellSlot) {
      int slot = InvUtil.getSlotContainingItem(player.field_71071_by, Witchery.Items.LEONARDS_URN);
      if (slot >= 0 && slot < player.field_71071_by.func_70302_i_()) {
         ItemStack urnStack = player.field_71071_by.func_70301_a(slot);
         if (urnStack != null) {
            ItemLeonardsUrn.InventoryLeonardsUrn inv = new ItemLeonardsUrn.InventoryLeonardsUrn(player, urnStack);
            if (urnStack.func_77960_j() >= spellSlot) {
               ItemStack potion = inv.func_70301_a(spellSlot);
               if (potion != null) {
                  world.func_72889_a((EntityPlayer)null, 1008, (int)player.field_70165_t, (int)player.field_70163_u, (int)player.field_70161_v, 0);
                  EntityBrew entity = new EntityBrew(world, player, potion, true);
                  world.func_72838_d(entity);
                  return;
               }
            }
         }
      }

      SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
   }

   private interface IBlockEffect {
      void doAction(World var1, EntityLivingBase var2, int var3, int var4, int var5, Block var6, int var7);
   }

   private interface IEntityEffect<T extends Entity> {
      void doAction(World var1, EntityLivingBase var2, double var3, double var5, double var7, T var9);
   }
}
