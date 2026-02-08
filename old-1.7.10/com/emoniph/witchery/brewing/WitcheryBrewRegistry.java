package com.emoniph.witchery.brewing;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.blocks.BlockCircleGlyph;
import com.emoniph.witchery.blocks.BlockWitchCrop;
import com.emoniph.witchery.brewing.action.BrewAction;
import com.emoniph.witchery.brewing.action.BrewActionBlockCircle;
import com.emoniph.witchery.brewing.action.BrewActionDispersal;
import com.emoniph.witchery.brewing.action.BrewActionEffect;
import com.emoniph.witchery.brewing.action.BrewActionImpactModifier;
import com.emoniph.witchery.brewing.action.BrewActionList;
import com.emoniph.witchery.brewing.action.BrewActionModifier;
import com.emoniph.witchery.brewing.action.BrewActionRitual;
import com.emoniph.witchery.brewing.action.BrewActionRitualEntityTarget;
import com.emoniph.witchery.brewing.action.BrewActionRitualRecipe;
import com.emoniph.witchery.brewing.action.BrewActionRitualSummonMob;
import com.emoniph.witchery.brewing.action.BrewActionSetColor;
import com.emoniph.witchery.brewing.action.BrewCurseEffect;
import com.emoniph.witchery.brewing.action.BrewPotionEffect;
import com.emoniph.witchery.brewing.action.effect.BrewActionBiomeChange;
import com.emoniph.witchery.brewing.action.effect.BrewActionBlight;
import com.emoniph.witchery.brewing.action.effect.BrewActionFelling;
import com.emoniph.witchery.brewing.action.effect.BrewActionLilify;
import com.emoniph.witchery.brewing.action.effect.BrewActionPlanting;
import com.emoniph.witchery.brewing.action.effect.BrewActionRaiseLand;
import com.emoniph.witchery.brewing.action.effect.BrewActionRaising;
import com.emoniph.witchery.brewing.action.effect.BrewActionSprouting;
import com.emoniph.witchery.brewing.action.effect.BrewActionTranspose;
import com.emoniph.witchery.brewing.potions.PotionBase;
import com.emoniph.witchery.brewing.potions.PotionSnowTrail;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.common.Shapeshift;
import com.emoniph.witchery.entity.EntityDemon;
import com.emoniph.witchery.entity.EntityEnt;
import com.emoniph.witchery.entity.EntityLeonard;
import com.emoniph.witchery.entity.EntityOwl;
import com.emoniph.witchery.entity.EntitySummonedUndead;
import com.emoniph.witchery.entity.EntityToad;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.infusion.infusions.InfusionInfernal;
import com.emoniph.witchery.network.PacketPushTarget;
import com.emoniph.witchery.util.BlockActionCircle;
import com.emoniph.witchery.util.BlockActionReplaceSphere;
import com.emoniph.witchery.util.BlockActionSphere;
import com.emoniph.witchery.util.BlockPosition;
import com.emoniph.witchery.util.BlockProtect;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.CircleUtil;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.Count;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.Dye;
import com.emoniph.witchery.util.EntityDamageSourceIndirectSilver;
import com.emoniph.witchery.util.EntityPosition;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.SpawnUtil;
import com.emoniph.witchery.util.TimeUtil;
import com.emoniph.witchery.util.TransformCreature;
import com.emoniph.witchery.worldgen.WorldGenLargeWitchTree;
import com.emoniph.witchery.worldgen.WorldGenWitchTree;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenForest;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;

public class WitcheryBrewRegistry {
   public static final WitcheryBrewRegistry INSTANCE = new WitcheryBrewRegistry();
   public static final int MAX_STRENGTH_BOOSTS = 7;
   public static final int MAX_DURATION_BOOSTS = 7;
   private final Hashtable<BrewItemKey, BrewAction> ingredients = new Hashtable();
   private final List<BrewActionRitualRecipe> recipes = new ArrayList();

   private WitcheryBrewRegistry() {
      BrewItemKey triggeredKey = new BrewItemKey(Items.field_151144_bL, 2);
      this.register((new BrewActionDispersal(new BrewItemKey(Items.field_151016_H), new AltarPower(0), new DispersalInstant())).addNullifier(new BrewItemKey(Items.field_151016_H), false).addNullifier(Witchery.Items.GENERIC.itemBatWool.getBrewItemKey(), false).addNullifier(Witchery.Items.GENERIC.itemArtichoke.getBrewItemKey(), false).addNullifier(Witchery.Items.GENERIC.itemWormwood.getBrewItemKey(), false).addNullifier(triggeredKey, false).addNullifier(new BrewItemKey(Items.field_151015_O), false));
      this.register((new BrewActionDispersal(Witchery.Items.GENERIC.itemArtichoke.getBrewItemKey(), new AltarPower(0), new DispersalInstant())).addNullifier(new BrewItemKey(Items.field_151016_H), false).addNullifier(Witchery.Items.GENERIC.itemBatWool.getBrewItemKey(), false).addNullifier(Witchery.Items.GENERIC.itemArtichoke.getBrewItemKey(), false).addNullifier(Witchery.Items.GENERIC.itemWormwood.getBrewItemKey(), false).addNullifier(triggeredKey, false).addNullifier(new BrewItemKey(Items.field_151015_O), false));
      this.register((new BrewActionDispersal(Witchery.Items.GENERIC.itemBatWool.getBrewItemKey(), new AltarPower(0), new DispersalGas())).addNullifier(new BrewItemKey(Items.field_151016_H), false).addNullifier(Witchery.Items.GENERIC.itemArtichoke.getBrewItemKey(), false).addNullifier(Witchery.Items.GENERIC.itemBatWool.getBrewItemKey(), false).addNullifier(Witchery.Items.GENERIC.itemWormwood.getBrewItemKey(), false).addNullifier(triggeredKey, false).addNullifier(new BrewItemKey(Items.field_151015_O), false));
      this.register((new BrewActionDispersal(Witchery.Items.GENERIC.itemWormwood.getBrewItemKey(), new AltarPower(0), new DispersalLiquid())).addNullifier(new BrewItemKey(Items.field_151016_H), false).addNullifier(Witchery.Items.GENERIC.itemBatWool.getBrewItemKey(), false).addNullifier(Witchery.Items.GENERIC.itemArtichoke.getBrewItemKey(), false).addNullifier(Witchery.Items.GENERIC.itemWormwood.getBrewItemKey(), false).addNullifier(triggeredKey, false).addNullifier(new BrewItemKey(Items.field_151015_O), false));
      this.register((new BrewActionDispersal(triggeredKey, new AltarPower(0), new DispersalTriggered())).addNullifier(new BrewItemKey(Items.field_151016_H), false).addNullifier(Witchery.Items.GENERIC.itemBatWool.getBrewItemKey(), false).addNullifier(Witchery.Items.GENERIC.itemArtichoke.getBrewItemKey(), false).addNullifier(Witchery.Items.GENERIC.itemWormwood.getBrewItemKey(), false).addNullifier(triggeredKey, false).addNullifier(new BrewItemKey(Items.field_151015_O), false));
      this.register(new BrewActionImpactModifier(Witchery.Items.GENERIC.itemAshWood.getBrewItemKey(), new BrewNamePartModifier(0, 0, false, 1, 0), new AltarPower(50)) {
         protected void onPrepareSplashPotion(World world, ModifiersImpact modifiers) {
            if (modifiers.extent < 1) {
               ++modifiers.extent;
            }

         }
      });
      this.register(new BrewActionImpactModifier(Dye.COCOA_BEANS.getBrewItemKey(), new BrewNamePartModifier(0, 0, false, 1, 0), new AltarPower(100)) {
         protected void onPrepareSplashPotion(World world, ModifiersImpact modifiers) {
            if (modifiers.extent < 2) {
               ++modifiers.extent;
            }

         }
      });
      this.register(new BrewActionImpactModifier(new BrewItemKey(Witchery.Blocks.WISPY_COTTON), new BrewNamePartModifier(0, 0, false, 1, 0), new AltarPower(150)) {
         protected void onPrepareSplashPotion(World world, ModifiersImpact modifiers) {
            if (modifiers.extent < 3) {
               ++modifiers.extent;
            }

         }
      });
      this.register(new BrewActionImpactModifier(Witchery.Items.GENERIC.itemBelladonnaFlower.getBrewItemKey(), new BrewNamePartModifier(0, 0, false, 0, 1), new AltarPower(50)) {
         protected void onPrepareSplashPotion(World world, ModifiersImpact modifiers) {
            if (modifiers.lifetime < 1) {
               ++modifiers.lifetime;
            }

         }
      });
      this.register(new BrewActionImpactModifier(Dye.LAPIS_LAZULI.getBrewItemKey(), new BrewNamePartModifier(0, 0, false, 0, 1), new AltarPower(100)) {
         protected void onPrepareSplashPotion(World world, ModifiersImpact modifiers) {
            if (modifiers.lifetime < 2) {
               ++modifiers.lifetime;
            }

         }
      });
      this.register(new BrewActionImpactModifier(new BrewItemKey(Blocks.field_150377_bs), new BrewNamePartModifier(0, 0, false, 0, 1), new AltarPower(150)) {
         protected void onPrepareSplashPotion(World world, ModifiersImpact modifiers) {
            if (modifiers.lifetime < 3) {
               ++modifiers.lifetime;
            }

         }
      });
      Dye[] arr$ = Dye.values();
      int len$ = arr$.length;

      int i$;
      final Dye dye;
      for(i$ = 0; i$ < len$; ++i$) {
         dye = arr$[i$];
         this.register(new BrewActionSetColor(new BrewItemKey(Blocks.field_150325_L, 15 - dye.damageValue), new AltarPower(0), dye.rgb));
      }

      this.register(new BrewActionModifier(new BrewItemKey(Items.field_151074_bl), (BrewNamePart)null, new AltarPower(50)) {
         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            modifiers.noParticles = true;
         }
      });
      this.register(new BrewActionModifier(Witchery.Items.GENERIC.itemRowanBerries.getBrewItemKey(), (BrewNamePart)null, new AltarPower(50)) {
         public int getDrinkSpeedModifiers() {
            return -8;
         }
      });
      this.register(new BrewActionModifier(Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.getBrewItemKey(), (BrewNamePart)null, new AltarPower(0)) {
         public int getDrinkSpeedModifiers() {
            return -4;
         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Witchery.Blocks.SPANISH_MOSS), (BrewNamePart)null, new AltarPower(50)) {
         public int getDrinkSpeedModifiers() {
            return -4;
         }
      });
      this.register(new BrewActionModifier(Witchery.Items.GENERIC.itemMandrakeRoot.getBrewItemKey(), (BrewNamePart)null, new AltarPower(0)) {
         public boolean augmentEffectLevels(EffectLevelCounter totalEffects) {
            totalEffects.increaseAvailableLevelIf(new EffectLevel(1), new EffectLevel(1));
            return true;
         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Items.field_151075_bm), (BrewNamePart)null, new AltarPower(50)) {
         public boolean augmentEffectLevels(EffectLevelCounter totalEffects) {
            totalEffects.increaseAvailableLevelIf(new EffectLevel(2), new EffectLevel(2));
            return true;
         }
      });
      this.register(new BrewActionModifier(Witchery.Items.GENERIC.itemTearOfTheGoddess.getBrewItemKey(), (BrewNamePart)null, new AltarPower(100)) {
         public boolean augmentEffectLevels(EffectLevelCounter totalEffects) {
            totalEffects.increaseAvailableLevelIf(new EffectLevel(2), new EffectLevel(4));
            return true;
         }
      });
      this.register(new BrewActionModifier(Witchery.Items.GENERIC.itemDiamondVapour.getBrewItemKey(), (BrewNamePart)null, new AltarPower(150)) {
         public boolean augmentEffectLevels(EffectLevelCounter totalEffects) {
            totalEffects.increaseAvailableLevelIf(new EffectLevel(2), new EffectLevel(6));
            return true;
         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Items.field_151045_i), (BrewNamePart)null, new AltarPower(150)) {
         public boolean augmentEffectLevels(EffectLevelCounter totalEffects) {
            totalEffects.increaseAvailableLevelIf(new EffectLevel(2), new EffectLevel(8));
            return true;
         }
      }).setYieldModifier(new ModifierYield(-2));
      this.register(new BrewActionModifier(new BrewItemKey(Items.field_151156_bN), new BrewNamePartModifier(0, 0, false, 0, 0, true), new AltarPower(150)) {
         public boolean augmentEffectLevels(EffectLevelCounter totalEffects) {
            totalEffects.increaseAvailableLevelIf(new EffectLevel(4), new EffectLevel(10));
            return true;
         }

         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            modifiers.powerhCeilingDisabled = true;
         }
      });
      this.register(new BrewActionModifier(Witchery.Items.GENERIC.itemKobolditePentacle.getBrewItemKey(), (BrewNamePart)null, new AltarPower(1000)) {
         public boolean augmentEffectLevels(EffectLevelCounter totalEffects) {
            totalEffects.increaseAvailableLevelIf(new EffectLevel(6), new EffectLevel(16));
            return true;
         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Items.field_151114_aO), new BrewNamePartModifier(1, 0, false, 0, 0), new AltarPower(50)) {
         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            if (modifiers.strength < 1) {
               modifiers.increaseStrength(1);
            }

         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Items.field_151072_bj), new BrewNamePartModifier(1, 0, false, 0, 0), new AltarPower(100)) {
         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            if (modifiers.strength < 2) {
               modifiers.increaseStrength(1);
            }

         }
      });
      this.register(new BrewActionModifier(Witchery.Items.GENERIC.itemAttunedStoneCharged.getBrewItemKey(), new BrewNamePartModifier(1, 0, false, 0, 0), new AltarPower(150)) {
         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            if (modifiers.strength < 3) {
               modifiers.increaseStrength(1);
            }

         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Items.field_151137_ax), new BrewNamePartModifier(0, 1, false, 0, 0), new AltarPower(50)) {
         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            if (modifiers.duration < 1) {
               modifiers.increaseDuration(1);
            }

         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Blocks.field_150343_Z), new BrewNamePartModifier(0, 1, false, 0, 0), new AltarPower(100)) {
         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            if (modifiers.duration < 2) {
               modifiers.increaseDuration(1);
            }

         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Witchery.Items.SEEDS_MINDRAKE), new BrewNamePartModifier(0, 1, false, 0, 0), new AltarPower(150)) {
         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            if (modifiers.duration < 3) {
               modifiers.increaseDuration(1);
            }

         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Items.field_151071_bq), new BrewNamePartModifier(0, 0, true, 0, 0), new AltarPower(25)) {
         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            modifiers.inverted = true;
         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Items.field_151130_bT), (BrewNamePart)null, new AltarPower(50)) {
         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            modifiers.disableBlockTarget = true;
         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Items.field_151118_aC), (BrewNamePart)null, new AltarPower(50)) {
         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            modifiers.disableEntityTarget = true;
         }
      });
      this.register(new BrewActionModifier(new BrewItemKey(Items.field_151115_aP, 2), (BrewNamePart)null, new AltarPower(200)) {
         public void augmentEffectModifiers(ModifiersEffect modifiers) {
            modifiers.strengthCeilingDisabled = true;
         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Items.field_151126_ay), (new BrewNamePart("witchery:brew.snow")).setBaseDuration(TimeUtil.minsToTicks(3)), new AltarPower(0), new Probability(1.0D), new EffectLevel(1)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            PotionSnowTrail.createSnowCovering(world, x, y, z, 2 + 2 * modifiers.getStrength(), modifiers.caster);
         }

         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            BrewPotionEffect.applyPotionEffect(targetEntity, modifiers, Witchery.Potions.SNOW_TRAIL, TimeUtil.minsToTicks(3), modifiers.noParticles, modifiers.caster);
         }
      });
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151115_aP, 0), new BrewNamePart("witchery:brew.swimming"), new AltarPower(0), new Probability(1.0D), Witchery.Potions.SWIMMING, (long)TimeUtil.minsToTicks(3), new EffectLevel(1)));
      arr$ = new Dye[]{Dye.ROSE_RED, Dye.CACTUS_GREEN, Dye.PURPLE_DYE, Dye.CYAN_DYE, Dye.LIGHT_GRAY_DYE, Dye.GRAY_DYE, Dye.PINK_DYE, Dye.LIME_DYE, Dye.DANDELION_YELLOW, Dye.LIGHT_BLUE_DYE, Dye.MAGENTA_DYE, Dye.ORANGE_DYE};
      len$ = arr$.length;

      for(i$ = 0; i$ < len$; ++i$) {
         dye = arr$[i$];
         this.register(new BrewCurseEffect(dye.getBrewItemKey(), new BrewNamePart("witchery:potion.colorful." + dye.unlocalizedName), new AltarPower(0), new Probability(1.0D), Witchery.Potions.COLORFUL, (long)TimeUtil.secsToTicks(90), new EffectLevel(1), false) {
            public void applyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
               if (!modifiers.disableEntityTarget) {
                  if (!modifiers.protectedFromNegativePotions) {
                     targetEntity.func_70690_d(new PotionEffect(this.potion.field_76415_H, this.baseDuration, dye.ordinal(), true));
                  }

                  modifiers.reset();
               }

            }
         });
      }

      this.register(new BrewPotionEffect(Witchery.Items.GENERIC.itemEnderDew.getBrewItemKey(), new BrewNamePart("witchery:potion.enderinhibition"), new AltarPower(200), new Probability(1.0D), Witchery.Potions.ENDER_INHIBITION, (long)TimeUtil.secsToTicks(90), new EffectLevel(1)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151015_O), new BrewNamePart("witchery:brew.moonshine"), new AltarPower(0), new Probability(1.0D), Witchery.Potions.FEEL_NO_PAIN, (long)TimeUtil.secsToTicks(90), new EffectLevel(1)));
      this.register(new BrewActionEffect(new BrewItemKey(Items.field_151044_h), new BrewNamePart("witchery:brew.extinguish"), new AltarPower(0), new Probability(1.0D), new EffectLevel(1)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
            if (modifiers.getStrength() > 1 || !world.field_73011_w.field_76575_d) {
               if (modifiers.getStrength() > 0 && targetEntity instanceof EntityBlaze) {
                  targetEntity.func_70097_a(DamageSource.func_76354_b(modifiers.caster, modifiers.caster), (float)(modifiers.getStrength() + 1) * 4.0F);
               }

               if (targetEntity.func_70027_ad()) {
                  targetEntity.func_70066_B();
               }
            }

         }

         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, final int radius, final ModifiersEffect modifiers, ItemStack stack) {
            if (modifiers.getStrength() > 1 || !world.field_73011_w.field_76575_d) {
               (new BlockActionCircle() {
                  public void onBlock(World world, int x, int y, int z) {
                     for(int dy = y - radius; dy <= y + radius; ++dy) {
                        Block block = world.func_147439_a(x, dy, z);
                        if (block == Blocks.field_150480_ab && BlockProtect.checkModsForBreakOK(world, x, dy, z, block, world.func_72805_g(x, dy, z), modifiers.caster) && BlockProtect.canBreak(block, world)) {
                           world.func_147468_f(x, dy, z);
                           SoundEffect.RANDOM_FIZZ.playAt(world, (double)x, (double)dy, (double)z, 1.0F, 2.0F);
                        }
                     }

                  }
               }).processFilledCircle(world, x, y, z, radius + (modifiers.ritualised ? 5 : 0));
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150348_b), new BrewNamePart("witchery:brew.dissipate"), new AltarPower(0), new Probability(1.0D), new EffectLevel(1)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
            if (targetEntity instanceof EntitySummonedUndead) {
               targetEntity.func_70097_a(DamageSource.func_76354_b(modifiers.caster, modifiers.caster), (float)(modifiers.getStrength() + 1) * 5.0F);
            }

         }

         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, final int radius, final ModifiersEffect modifiers, ItemStack stack) {
            (new BlockActionCircle() {
               public void onBlock(World world, int x, int y, int z) {
                  for(int dy = y - radius; dy <= y + radius; ++dy) {
                     Block block = world.func_147439_a(x, dy, z);
                     if (block == Witchery.Blocks.BREW_GAS) {
                        if (BlockProtect.checkModsForBreakOK(world, x, dy, z, block, world.func_72805_g(x, dy, z), modifiers.caster) && BlockProtect.canBreak(block, world)) {
                           world.func_147468_f(x, dy, z);
                           SoundEffect.RANDOM_FIZZ.playAt(world, (double)x, (double)dy, (double)z, 1.0F, 2.0F);
                        }
                     } else if (block instanceof IFluidBlock && modifiers.getStrength() >= 1) {
                        IFluidBlock fluidBlock = (IFluidBlock)block;
                        if (fluidBlock.getFluid() != null && fluidBlock.getFluid().isGaseous() && BlockProtect.checkModsForBreakOK(world, x, dy, z, block, world.func_72805_g(x, dy, z), modifiers.caster) && BlockProtect.canBreak(block, world)) {
                           world.func_147468_f(x, dy, z);
                           SoundEffect.RANDOM_FIZZ.playAt(world, (double)x, (double)dy, (double)z, 1.0F, 2.0F);
                        }
                     }
                  }

               }
            }).processFilledCircle(world, x, y, z, radius);
         }
      });
      this.register(new BrewActionBlockCircle(new BrewItemKey(Blocks.field_150327_N), new BrewNamePart("witchery:brew.flowers"), new AltarPower(200), new EffectLevel(1)) {
         private final Block[] BLOCKS;

         {
            this.BLOCKS = new Block[]{Blocks.field_150327_N, Blocks.field_150328_O, Blocks.field_150328_O, Blocks.field_150328_O, Blocks.field_150328_O, Blocks.field_150328_O, Blocks.field_150328_O, Blocks.field_150328_O, Blocks.field_150328_O, Blocks.field_150328_O};
         }

         protected void onCircleBlock(World world, int x, int y, int z, ModifiersEffect modifiers, Count counter) {
            for(int dy = y - 1; dy <= y + 1; ++dy) {
               if (BlockUtil.isReplaceableBlock(world, x, dy, z, modifiers.caster) && !world.func_147439_a(x, dy, z).func_149688_o().func_76224_d() && Blocks.field_150327_N.func_149742_c(world, x, dy, z) && world.field_73012_v.nextInt(8 - modifiers.getStrength()) == 0) {
                  int flowerIndex = world.field_73012_v.nextInt(this.BLOCKS.length);
                  int flowerMeta = Math.max(flowerIndex - 1, 0);
                  world.func_147465_d(x, dy, z, this.BLOCKS[flowerIndex], flowerMeta, 3);
               }
            }

         }
      });
      this.register(new BrewActionBlockCircle(Dye.BONE_MEAL.getBrewItemKey(), new BrewNamePart("witchery:brew.fertilization"), new AltarPower(250), new EffectLevel(1)) {
         private final ItemStack BONEMEAL;

         {
            this.BONEMEAL = Dye.BONE_MEAL.createStack();
         }

         protected void onCircleBlock(World world, int x, int y, int z, ModifiersEffect modifiers, Count counter) {
            for(int dy = y + 1; dy >= y - 1; --dy) {
               Block block = world.func_147439_a(x, dy, z);
               if (block instanceof IGrowable || block instanceof IPlantable || block instanceof BlockWitchCrop) {
                  for(int i = 0; i <= modifiers.getStrength(); ++i) {
                     ItemDye.func_150919_a(this.BONEMEAL, world, x, dy, z);
                  }

                  return;
               }
            }

         }
      });
      this.register(new BrewActionBlockCircle(new BrewItemKey(Items.field_151034_e), new BrewNamePart("witchery:brew.harvesting"), new AltarPower(0), new EffectLevel(1)) {
         protected void onCircleBlock(World world, int x, int y, int z, ModifiersEffect modifiers, Count counter) {
            for(int dy = y - 1; dy <= y + 1; ++dy) {
               Block block = world.func_147439_a(x, dy, z);
               if (block instanceof BlockBush) {
                  int meta = world.func_72805_g(x, dy, z);
                  List<ItemStack> drops = new ArrayList();
                  drops.addAll(block.getDrops(world, x, dy, z, meta, Math.max(modifiers.getStrength() - 1, 0)));
                  world.func_147468_f(x, dy, z);
                  counter.increment();
                  if (world.field_73012_v.nextInt(counter.get()) == 0) {
                     world.func_72926_e(2001, x, dy, z, Block.func_149682_b(block) + (meta << 12));
                  }

                  Iterator i$ = drops.iterator();

                  while(i$.hasNext()) {
                     ItemStack drop = (ItemStack)i$.next();
                     world.func_72838_d(new EntityItem(world, 0.5D + (double)x, 0.5D + (double)dy, 0.5D + (double)z, drop.func_77946_l()));
                  }
               }
            }

         }
      });
      this.register(new BrewActionBlockCircle(new BrewItemKey(Blocks.field_150346_d), new BrewNamePart("witchery:brew.tilling"), new AltarPower(0), new EffectLevel(1)) {
         protected void onCircleBlock(World world, int x, int y, int z, ModifiersEffect modifiers, Count counter) {
            for(int dy = y - 1; dy <= y + 1; ++dy) {
               Block block = world.func_147439_a(x, dy, z);
               if ((block == Blocks.field_150349_c || block == Blocks.field_150346_d || block == Blocks.field_150354_m && modifiers.getStrength() > 0 || block == Blocks.field_150424_aL && modifiers.getStrength() > 1 || block == Blocks.field_150425_aM && modifiers.getStrength() > 2) && world.func_147437_c(x, dy + 1, z)) {
                  world.func_147449_b(x, dy, z, Blocks.field_150458_ak);
                  counter.increment();
                  if (world.field_73012_v.nextInt(counter.get()) == 0) {
                     world.func_72908_a((double)((float)x + 0.5F), (double)((float)dy + 0.5F), (double)((float)z + 0.5F), block.field_149762_H.func_150498_e(), (block.field_149762_H.func_150497_c() + 1.0F) / 2.0F, block.field_149762_H.func_150494_d() * 0.8F);
                  }
               }
            }

         }
      });
      this.register(new BrewActionPlanting(new BrewItemKey(Items.field_151014_N), new BrewNamePart("witchery:brew.planting"), new AltarPower(0), new EffectLevel(1)));
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150338_P), new BrewNamePart("witchery:brew.pruning"), new AltarPower(0), new Probability(1.0D), new EffectLevel(1)) {
         protected void doApplyToBlock(World world, int posX, int posY, int posZ, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack stack) {
            int BLOCK_RADIUS = radius - 1;
            int BLOCK_RADIUS_SQ = BLOCK_RADIUS * BLOCK_RADIUS;
            int blockX = MathHelper.func_76128_c((double)posX);
            int blockY = MathHelper.func_76128_c((double)posY);
            int blockZ = MathHelper.func_76128_c((double)posZ);

            for(int y = blockY - BLOCK_RADIUS; y <= blockY + BLOCK_RADIUS; ++y) {
               for(int x = blockX - BLOCK_RADIUS; x <= blockX + BLOCK_RADIUS; ++x) {
                  for(int z = blockZ - BLOCK_RADIUS; z <= blockZ + BLOCK_RADIUS; ++z) {
                     if (Coord.distanceSq((double)x, (double)y, (double)z, (double)blockX, (double)blockY, (double)blockZ) <= (double)BLOCK_RADIUS_SQ && BlockProtect.checkModsForBreakOK(world, x, y, z, modifiers.caster)) {
                        Material material = world.func_147439_a(x, y, z).func_149688_o();
                        if (material != null && (material == Material.field_151584_j || (material == Material.field_151585_k || material == Material.field_151582_l) && material.func_76222_j())) {
                           Block blockID = world.func_147439_a(x, y, z);
                           if (!(blockID instanceof BlockCircle) && !(blockID instanceof BlockCircleGlyph)) {
                              blockID.func_149697_b(world, x, y, z, world.func_72805_g(x, y, z), modifiers.getStrength());
                              world.func_147468_f(x, y, z);
                           }
                        }
                     }
                  }
               }
            }

         }
      });
      this.register(new BrewActionFelling(Items.field_151007_F, 0, new AltarPower(0), new EffectLevel(1)));
      this.register(new BrewActionEffect(new BrewItemKey(Items.field_151145_ak), new BrewNamePart("witchery:brew.pulverisation"), new AltarPower(250), new Probability(1.0D), new EffectLevel(1)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, final ModifiersEffect modifiers, ItemStack actionStack) {
            if (!world.field_72995_K) {
               (new BlockActionSphere() {
                  public void onBlock(World world, int x, int y, int z) {
                     Block block = world.func_147439_a(x, y, z);
                     if (BlockProtect.checkModsForBreakOK(world, x, y, z, block, world.func_72805_g(x, y, z), modifiers.caster) && BlockProtect.canBreak(block, world)) {
                        if (block == Blocks.field_150348_b) {
                           world.func_147449_b(x, y, z, Blocks.field_150347_e);
                        } else if (block == Blocks.field_150347_e) {
                           world.func_147449_b(x, y, z, Blocks.field_150351_n);
                        } else if (block != Blocks.field_150351_n && block != Blocks.field_150322_A) {
                           if (block != Blocks.field_150354_m) {
                              return;
                           }

                           world.func_147468_f(x, y, z);
                           EntityUtil.spawnEntityInWorld(world, new EntityItem(world, (double)x, (double)y, (double)z, new ItemStack(Blocks.field_150354_m)));
                        } else {
                           world.func_147449_b(x, y, z, Blocks.field_150354_m);
                        }

                        ParticleEffect.SMOKE.send(SoundEffect.NONE, world, (double)x, (double)(y + 1), (double)z, 0.25D, 0.25D, 16);
                     }

                  }
               }).drawFilledSphere(world, x, y, z, Math.max(radius - 1, 1));
               SoundEffect.MOB_GHAST_FIREBALL.playAt(world, (double)x, (double)y, (double)z, 0.5F, 2.0F);
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150354_m), new BrewNamePart("witchery:brew.tidehold"), new AltarPower(0), new Probability(1.0D), new EffectLevel(1)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, final ModifiersEffect modifiers, ItemStack actionStack) {
            if (!world.field_72995_K) {
               (new BlockActionSphere() {
                  public void onBlock(World world, int x, int y, int z) {
                     Block block = world.func_147439_a(x, y, z);
                     if (block == Blocks.field_150355_j || block == Blocks.field_150358_i) {
                        Witchery.Blocks.SLURP.replaceBlockAt(world, x, y, z, modifiers.getModifiedDuration(TimeUtil.secsToTicks(10)));
                     }

                  }
               }).drawFilledSphere(world, x, y, z, Math.max(radius + 2, 1));
            }

         }
      });
      this.register(new BrewActionLilify(new BrewItemKey(Blocks.field_150392_bi), new BrewNamePart("witchery:brew.lilify"), new AltarPower(200), new EffectLevel(1)));
      this.register(new BrewPotionEffect(Witchery.Items.GENERIC.itemWolfsbane.getBrewItemKey(), new BrewNamePart("witchery:potion.wolfsbane"), new AltarPower(0), new Probability(1.0D), Witchery.Potions.WOLFSBANE, (long)TimeUtil.secsToTicks(60), new EffectLevel(1)));
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemPurifiedMilk.getBrewItemKey(), new BrewNamePart("witchery:brew.removedebuffs"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
            List<Potion> effectsToRemove = new ArrayList();
            Collection<PotionEffect> effects = targetEntity.func_70651_bq();
            Iterator i$ = effects.iterator();

            while(true) {
               do {
                  Potion potionx;
                  do {
                     do {
                        PotionEffect effect;
                        do {
                           if (!i$.hasNext()) {
                              i$ = effectsToRemove.iterator();

                              while(i$.hasNext()) {
                                 Potion potion = (Potion)i$.next();
                                 targetEntity.func_82170_o(potion.field_76415_H);
                              }

                              return;
                           }

                           effect = (PotionEffect)i$.next();
                           potionx = Potion.field_76425_a[effect.func_76456_a()];
                        } while(!PotionBase.isDebuff(potionx));

                        if (PotionBase.isCurable(potionx) && effect.func_76458_c() <= modifiers.getStrength()) {
                           effectsToRemove.add(potionx);
                        }
                     } while(potionx != Witchery.Potions.DISEASED);
                  } while(modifiers.getStrength() < 2);

                  effectsToRemove.add(potionx);
                  if (targetEntity instanceof EntityPlayer) {
                     EntityPlayer player = (EntityPlayer)targetEntity;
                     ExtendedPlayer playerEx = ExtendedPlayer.get(player);
                     if (playerEx != null) {
                        playerEx.clearCachedIncurablePotionEffect(potionx);
                     }
                  }
               } while(modifiers.getStrength() < 3);

               int R = true;
               Coord coord = new Coord(targetEntity);

               for(int x = -9; x <= 9; ++x) {
                  for(int z = -9; z <= 9; ++z) {
                     for(int y = -9; y <= 9; ++y) {
                        Block block = world.func_147439_a(coord.x + x, coord.y + y, coord.z + z);
                        if (block == Witchery.Blocks.DISEASE) {
                           world.func_147468_f(coord.x + x, coord.y + y, coord.z + z);
                        }
                     }
                  }
               }
            }
         }

         protected void doApplyRitualToEntity(World world, EntityLivingBase targetEntity, ModifiersRitual ritualModifiers, ModifiersEffect modifiers, ItemStack stack) {
            List<Potion> effectsToRemove = new ArrayList();
            List<PotionEffect> effectsToAdd = new ArrayList();
            Collection<PotionEffect> effects = targetEntity.func_70651_bq();
            int modifiedStrength = modifiers.getStrength();
            Iterator i$ = effects.iterator();

            PotionEffect effect;
            while(i$.hasNext()) {
               effect = (PotionEffect)i$.next();
               Potion potion = Potion.field_76425_a[effect.func_76456_a()];
               if (PotionBase.isDebuff(potion)) {
                  if (effect.func_76458_c() < modifiedStrength) {
                     effectsToRemove.add(potion);
                     if (world.field_73012_v.nextDouble() < 0.01D) {
                        effectsToAdd.add(new PotionEffect(effect.func_76456_a(), effect.func_76459_b(), effect.func_76458_c() + 1));
                     }
                  } else if (effect.func_76458_c() == modifiedStrength) {
                     effectsToRemove.add(potion);
                     if (world.field_73012_v.nextDouble() < 0.25D) {
                        effectsToAdd.add(new PotionEffect(effect.func_76456_a(), effect.func_76459_b(), effect.func_76458_c() + 1));
                     }
                  } else {
                     effectsToRemove.add(potion);
                     if (world.field_73012_v.nextDouble() < 0.75D) {
                        effectsToAdd.add(new PotionEffect(effect.func_76456_a(), effect.func_76459_b(), effect.func_76458_c() + 1));
                     }
                  }
               }
            }

            i$ = effectsToRemove.iterator();

            while(i$.hasNext()) {
               Potion potionx = (Potion)i$.next();
               targetEntity.func_82170_o(potionx.field_76415_H);
               if (!PotionBase.isCurable(potionx) && targetEntity instanceof EntityPlayer) {
                  EntityPlayer player = (EntityPlayer)targetEntity;
                  ExtendedPlayer playerEx = ExtendedPlayer.get(player);
                  if (playerEx != null) {
                     playerEx.clearCachedIncurablePotionEffect(potionx);
                  }
               }
            }

            i$ = effectsToAdd.iterator();

            while(i$.hasNext()) {
               effect = (PotionEffect)i$.next();
               targetEntity.func_70690_d(effect);
            }

            if (effectsToAdd.isEmpty()) {
               ParticleEffect.SPELL.send(SoundEffect.RANDOM_LEVELUP, targetEntity, 0.5D, 2.0D, 16);
            } else {
               ParticleEffect.MOB_SPELL.send(SoundEffect.MOB_ENDERDRAGON_GROWL, targetEntity, 0.5D, 2.0D, 16);
            }

         }
      });
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemReekOfMisfortune.getBrewItemKey(), new BrewNamePart("witchery:brew.removebuffs"), new AltarPower(250), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
            List<Potion> effectsToRemove = new ArrayList();
            Collection<PotionEffect> effects = targetEntity.func_70651_bq();
            Iterator i$ = effects.iterator();

            while(i$.hasNext()) {
               PotionEffect effect = (PotionEffect)i$.next();
               Potion potion = Potion.field_76425_a[effect.func_76456_a()];
               if (!PotionBase.isDebuff(potion) && PotionBase.isCurable(potion) && effect.func_76458_c() <= modifiers.getStrength()) {
                  effectsToRemove.add(potion);
               }
            }

            i$ = effectsToRemove.iterator();

            while(i$.hasNext()) {
               Potion potionx = (Potion)i$.next();
               targetEntity.func_82170_o(potionx.field_76415_H);
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150347_e), new BrewNamePart("witchery:brew.lavahold"), new AltarPower(100), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, final ModifiersEffect modifiers, ItemStack actionStack) {
            if (!world.field_72995_K) {
               (new BlockActionSphere() {
                  public void onBlock(World world, int x, int y, int z) {
                     Block block = world.func_147439_a(x, y, z);
                     if (block == Blocks.field_150353_l || block == Blocks.field_150356_k) {
                        Witchery.Blocks.SLURP.replaceBlockAt(world, x, y, z, modifiers.getModifiedDuration(TimeUtil.secsToTicks(10)));
                     }

                  }
               }).drawFilledSphere(world, x, y, z, Math.max(radius + 2, 1));
            }

         }
      });
      this.register(new BrewPotionEffect(new BrewItemKey(Witchery.Blocks.BRAMBLE, 0), new BrewNamePart("witchery:potion.repellattacker"), new AltarPower(250), new Probability(1.0D), Witchery.Potions.REPELL_ATTACKER, (long)TimeUtil.secsToTicks(90), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Blocks.field_150351_n), new BrewNamePart("witchery:potion.gasmask"), new AltarPower(100), new Probability(1.0D), Witchery.Potions.GAS_MASK, (long)TimeUtil.secsToTicks(90), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151070_bp), new BrewNamePart("witchery:brew.poison"), new AltarPower(0), new Probability(1.0D), Potion.field_76436_u, (long)TimeUtil.secsToTicks(45), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151102_aT), new BrewNamePart("witchery:brew.movespeed", "witchery:brew.moveslow"), new AltarPower(100), new Probability(1.0D), Potion.field_76424_c, (long)TimeUtil.minsToTicks(3), Potion.field_76421_d, (long)TimeUtil.secsToTicks(90), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151115_aP, 3), new BrewNamePart("witchery:brew.waterbreathing"), new AltarPower(100), new Probability(1.0D), Potion.field_76427_o, (long)TimeUtil.minsToTicks(3), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151064_bs), new BrewNamePart("witchery:brew.resistfire"), new AltarPower(100), new Probability(1.0D), Potion.field_76426_n, (long)TimeUtil.minsToTicks(3), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151150_bK), new BrewNamePart("witchery:brew.nightvision", "witchery:brew.invisibility"), new AltarPower(200), new Probability(1.0D), Potion.field_76439_r, (long)TimeUtil.minsToTicks(3), Potion.field_76441_p, (long)TimeUtil.minsToTicks(3), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151073_bk), new BrewNamePart("witchery:brew.regeneration", "witchery:brew.poison"), new AltarPower(200), new Probability(1.0D), Potion.field_76428_l, (long)TimeUtil.secsToTicks(45), Potion.field_76436_u, (long)TimeUtil.secsToTicks(45), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151065_br), new BrewNamePart("witchery:brew.damageboost", "witchery:brew.weakness"), new AltarPower(200), new Probability(1.0D), Potion.field_76420_g, (long)TimeUtil.minsToTicks(3), Potion.field_76437_t, (long)TimeUtil.secsToTicks(90), new EffectLevel(2)));
      this.register((new BrewPotionEffect(new BrewItemKey(Items.field_151060_bw), new BrewNamePart("witchery:brew.healing", "witchery:brew.harming"), new AltarPower(200), new Probability(1.0D), Potion.field_76432_h, 0L, Potion.field_76433_i, 0L, new EffectLevel(2))).setStrengthCeiling(1));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151120_aE), new BrewNamePart("witchery:brew.floating"), new AltarPower(250), new Probability(1.0D), Witchery.Potions.FLOATING, (long)TimeUtil.secsToTicks(90), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151116_aA), new BrewNamePart("witchery:brew.jump"), new AltarPower(200), new Probability(1.0D), Potion.field_76430_j, (long)TimeUtil.minsToTicks(3), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151008_G), new BrewNamePart("witchery:brew.featherfall"), new AltarPower(100), new Probability(1.0D), Witchery.Potions.FEATHER_FALL, (long)TimeUtil.minsToTicks(1), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Blocks.field_150337_Q), new BrewNamePart("witchery:potion.poisonweapons"), new AltarPower(200), new Probability(1.0D), Witchery.Potions.POISON_WEAPONS, (long)TimeUtil.secsToTicks(90), new EffectLevel(2)));
      this.register(new BrewPotionEffect(new BrewItemKey(Blocks.field_150321_G), new BrewNamePart("witchery:potion.reflectprojectiles", "witchery:potion.attractprojectiles"), new AltarPower(250), new Probability(1.0D), Witchery.Potions.REFLECT_PROJECTILES, (long)TimeUtil.secsToTicks(90), Witchery.Potions.ATTRACT_PROJECTILES, (long)TimeUtil.secsToTicks(45), new EffectLevel(2)));
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemBatBall.getBrewItemKey(), new BrewNamePart("witchery:brew.batburst"), new AltarPower(1000), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            int BAT_COUNT = (modifiers.powerScalingFactor == 1.0D && !modifiers.isGlancing && modifiers.strengthPenalty <= 0 ? 10 : 1) + modifiers.getStrength();
            this.explodeBats(world, new Coord(x, y, z), side, BAT_COUNT);
         }

         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            if (!(targetEntity instanceof EntityOwl) && !(targetEntity instanceof EntityBat)) {
               int BAT_COUNT = (modifiers.powerScalingFactor == 1.0D && !modifiers.isGlancing ? 10 : 1) + modifiers.getStrength();
               if (modifiers.powerScalingFactor == 1.0D && !modifiers.isGlancing || world.field_73012_v.nextInt(20) == 0) {
                  this.explodeBats(world, new Coord(targetEntity), ForgeDirection.UP, BAT_COUNT);
               }
            }

         }

         private void explodeBats(World world, Coord coord, ForgeDirection side, int bats) {
            int x = coord.x + side.offsetX;
            int z = coord.z + side.offsetZ;
            int y = coord.y + side.offsetY;
            int NUM_BATS = bats;

            for(int i = 0; i < NUM_BATS; ++i) {
               EntityBat bat = new EntityBat(world);
               EntityUtil.setNoDrops(bat);
               bat.func_70012_b((double)x, (double)y, (double)z, 0.0F, 0.0F);
               world.func_72838_d(bat);
            }

            ParticleEffect.EXPLODE.send(SoundEffect.MOB_ENDERMEN_PORTAL, world, 0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z, 3.0D, 3.0D, 16);
         }
      });
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemOwletsWing.getBrewItemKey(), new BrewNamePart("witchery:brew.bodega"), new AltarPower(1000), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            if (modifiers.caster != null && Familiar.hasActiveBroomMasteryFamiliar(modifiers.caster) && !(targetEntity instanceof EntityOwl) && !(targetEntity instanceof EntityBat)) {
               int BIRD_COUNT = (modifiers.powerScalingFactor == 1.0D && !modifiers.isGlancing ? 3 + world.field_73012_v.nextInt(2) : 1) + modifiers.getStrength();
               if (modifiers.powerScalingFactor == 1.0D && !modifiers.isGlancing || world.field_73012_v.nextInt(20) == 0) {
                  this.explodeBirds(world, targetEntity, BIRD_COUNT);
               }
            }

         }

         private void explodeBirds(World world, EntityLivingBase victim, int birds) {
            for(int i = 0; i < birds; ++i) {
               EntityOwl owl = new EntityOwl(world);
               owl.func_70012_b(victim.field_70165_t - 2.0D + (double)world.field_73012_v.nextInt(5), victim.field_70163_u + (double)victim.field_70131_O + 1.0D + (double)world.field_73012_v.nextInt(2), victim.field_70161_v - 2.0D + (double)world.field_73012_v.nextInt(5), 0.0F, 0.0F);
               owl.func_70624_b(victim);
               owl.setTimeToLive(400);
               world.func_72838_d(owl);
               ParticleEffect.PORTAL.send(SoundEffect.MOB_ENDERMEN_PORTAL, owl, 1.0D, 1.0D, 16);
            }

         }
      });
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemBreathOfTheGoddess.getBrewItemKey(), new BrewNamePart("witchery:brew.airhike"), new AltarPower(750), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            double motionY = 0.6D + 0.2D * (double)modifiers.getStrength();
            targetEntity.field_70143_R = 0.0F;
            if (targetEntity instanceof EntityPlayer) {
               Witchery.packetPipeline.sendTo((IMessage)(new PacketPushTarget(0.0D, motionY, 0.0D)), (EntityPlayer)((EntityPlayer)targetEntity));
            } else {
               targetEntity.field_70181_x = motionY;
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Items.field_151123_aH), new BrewNamePart("witchery:brew.frogtongue"), new AltarPower(150), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            if (modifiers.getStrength() > 0 && !(modifiers.caster instanceof FakePlayer)) {
               EntityUtil.pullTowards(world, targetEntity, new EntityPosition(modifiers.caster), 0.05D, 0.0D);
            } else {
               EntityUtil.pullTowards(world, targetEntity, modifiers.impactLocation, 0.05D, 0.0D);
            }

         }

         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack stack) {
            double R = (double)radius;
            double R_SQ = R * R;
            EntityPosition position = modifiers.impactLocation;
            AxisAlignedBB bb = AxisAlignedBB.func_72330_a(position.x - R, position.y - R, position.z - R, position.x + R, position.y + R, position.z + R);
            List<Entity> list1 = world.func_72872_a(Entity.class, bb);
            Iterator i$ = list1.iterator();

            while(true) {
               while(true) {
                  Entity targetEntity;
                  double distanceSq;
                  do {
                     do {
                        if (!i$.hasNext()) {
                           return;
                        }

                        targetEntity = (Entity)i$.next();
                     } while(targetEntity instanceof EntityLivingBase);

                     distanceSq = position.getDistanceSqToEntity(targetEntity);
                  } while(!(distanceSq <= R_SQ));

                  if (modifiers.getStrength() > 0 && !(modifiers.caster instanceof FakePlayer)) {
                     EntityUtil.pullTowards(world, targetEntity, new EntityPosition(modifiers.caster), 0.05D, 0.0D);
                  } else {
                     EntityUtil.pullTowards(world, targetEntity, modifiers.impactLocation, 0.05D, 0.0D);
                  }
               }
            }
         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150423_aK), new BrewNamePart("witchery:brew.harmundead"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            int strength = Math.min(modifiers.getStrength(), modifiers.strengthCeilingDisabled ? 3 : 1);
            if (CreatureUtil.isUndead(targetEntity)) {
               targetEntity.func_70097_a(DamageSource.func_76354_b(modifiers.caster, modifiers.caster), (float)MathHelper.func_76143_f((double)(10 << strength) * modifiers.powerScalingFactor));
            } else {
               targetEntity.func_70097_a(DamageSource.func_76354_b(modifiers.caster, modifiers.caster), (float)MathHelper.func_76143_f((double)(3 << strength) * modifiers.powerScalingFactor));
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150328_O, 1), new BrewNamePart("witchery:brew.harminsects"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            int strength = Math.min(modifiers.getStrength(), modifiers.strengthCeilingDisabled ? 3 : 1);
            if (CreatureUtil.isInsect(targetEntity)) {
               targetEntity.func_70097_a(DamageSource.func_76354_b(modifiers.caster, modifiers.caster), (float)MathHelper.func_76143_f((double)(10 << strength) * modifiers.powerScalingFactor));
            } else {
               targetEntity.func_70097_a(DamageSource.func_76354_b(modifiers.caster, modifiers.caster), (float)MathHelper.func_76143_f((double)(3 << strength) * modifiers.powerScalingFactor));
            }

         }
      });
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemOilOfVitriol.getBrewItemKey(), new BrewNamePart("witchery:brew.erosion"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, final ModifiersEffect modifiers, ItemStack actionStack) {
            final Count obsidianCount = new Count();

            for(int r = radius; r > 0; --r) {
               int var10000 = radius - r;
               (new BlockActionCircle() {
                  public void onBlock(World world, int x, int y, int z) {
                     Block block = world.func_147439_a(x, y, z);
                     if (BlockProtect.checkModsForBreakOK(world, x, y, z, block, world.func_72805_g(x, y, z), modifiers.caster) && BlockProtect.canBreak(block, world)) {
                        world.func_147468_f(x, y, z);
                        ParticleEffect.SPLASH.send(SoundEffect.NONE, world, (double)x, (double)y, (double)z, 0.5D, 0.5D, 16);
                        obsidianCount.incrementBy(block == Blocks.field_150343_Z ? 1 : 0);
                     }

                  }
               }).processFilledCircle(world, x, y, z, r);
            }

            SoundEffect.RANDOM_FIZZ.playAt(world, (double)x, (double)y, (double)z, 1.0F, 2.0F);
            SpawnUtil.spawnEntityItem(world, (double)x, (double)y, (double)z, Blocks.field_150343_Z, obsidianCount.get());
         }

         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            if (world.field_73012_v.nextInt(MathHelper.func_76143_f(5.0D / modifiers.powerScalingFactor)) == 0) {
               targetEntity.func_70097_a(DamageSource.func_76356_a(targetEntity, modifiers.caster), (float)MathHelper.func_76143_f(8.0D * modifiers.powerScalingFactor));
            }

            for(int slot = 0; slot < 5; ++slot) {
               ItemStack stack = targetEntity.func_71124_b(slot);
               if (stack != null && stack.func_77984_f()) {
                  stack.func_77972_a(MathHelper.func_76143_f((50.0D + (double)world.field_73012_v.nextInt(25)) * modifiers.powerScalingFactor), modifiers.caster);
                  if (stack.func_77960_j() >= stack.func_77958_k() || stack.field_77994_a <= 0) {
                     targetEntity.func_70062_b(slot, (ItemStack)null);
                  }
               }
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150424_aL), new BrewNamePart("witchery:brew.levelling"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int r, final ModifiersEffect modifiers, ItemStack actionStack) {
            final int y0 = modifiers.ritualised ? y - 1 : y;
            int radius = modifiers.ritualised ? r + (modifiers.getStrength() + 1) * 3 : r;
            final Count dirt = new Count();
            final Count stone = new Count();
            final Count sand = new Count();
            final Count sandstone = new Count();
            final Count netherrack = new Count();
            final Count endstone = new Count();
            final int s = modifiers.getStrength();
            int defaultAmount = modifiers.ritualised ? 64 + 32 * modifiers.getStrength() : 16;
            Block hitBlock = world.func_147439_a(x, y0, z);
            if (hitBlock == Blocks.field_150346_d) {
               dirt.incrementBy(defaultAmount);
            } else if (hitBlock == Blocks.field_150348_b) {
               stone.incrementBy(defaultAmount);
            } else if (hitBlock == Blocks.field_150354_m) {
               sand.incrementBy(defaultAmount);
            } else if (hitBlock == Blocks.field_150322_A) {
               sandstone.incrementBy(defaultAmount);
            } else if (hitBlock == Blocks.field_150424_aL) {
               netherrack.incrementBy(defaultAmount);
            } else if (hitBlock == Blocks.field_150377_bs) {
               endstone.incrementBy(defaultAmount);
            }

            (new BlockActionCircle() {
               public void onBlock(World world, int x, int y, int z) {
                  for(int dy = y0 + 1; dy < y0 + 4 + s; ++dy) {
                     Block block = world.func_147439_a(x, dy, z);
                     if (block != Blocks.field_150350_a && BlockProtect.checkModsForBreakOK(world, x, dy, z, block, world.func_72805_g(x, dy, z), modifiers.caster) && BlockProtect.canBreak(block, world)) {
                        if (block == Blocks.field_150348_b) {
                           stone.increment();
                        } else if (block == Blocks.field_150354_m) {
                           sand.increment();
                        } else if (block == Blocks.field_150322_A) {
                           sandstone.increment();
                        } else if (block == Blocks.field_150424_aL) {
                           netherrack.increment();
                        } else if (block == Blocks.field_150377_bs) {
                           endstone.increment();
                        } else {
                           dirt.increment();
                        }

                        world.func_147468_f(x, dy, z);
                        ParticleEffect.SMOKE.send(SoundEffect.NONE, world, (double)x, (double)dy, (double)z, 0.5D, 0.5D, 16);
                     }
                  }

               }
            }).processFilledCircle(world, x, y0, z, radius);

            for(final int newy = y0; newy >= y0 - (4 + s); --newy) {
               (new BlockActionCircle() {
                  public void onBlock(World world, int x, int y, int z) {
                     world.func_147439_a(x, newy, z);
                     if (BlockUtil.isReplaceableBlock(world, x, newy, z, modifiers.caster)) {
                        if (endstone.get() > 0) {
                           endstone.decrement();
                           world.func_147449_b(x, newy, z, Blocks.field_150377_bs);
                        } else if (netherrack.get() > 0) {
                           netherrack.decrement();
                           world.func_147449_b(x, newy, z, Blocks.field_150424_aL);
                        } else if (sandstone.get() > 0) {
                           sandstone.decrement();
                           world.func_147449_b(x, newy, z, Blocks.field_150322_A);
                        } else if (sand.get() > 0) {
                           sand.decrement();
                           world.func_147449_b(x, newy, z, Blocks.field_150354_m);
                        } else if (stone.get() > 0) {
                           stone.decrement();
                           world.func_147449_b(x, newy, z, Blocks.field_150348_b);
                        } else {
                           if (dirt.get() <= 0) {
                              return;
                           }

                           dirt.decrement();
                           world.func_147449_b(x, newy, z, Blocks.field_150346_d);
                        }

                        ParticleEffect.SMOKE.send(SoundEffect.NONE, world, (double)x, (double)newy, (double)z, 0.5D, 0.5D, 16);
                     }

                  }
               }).processFilledCircle(world, x, y0, z, radius);
            }

            SoundEffect.RANDOM_FIZZ.playAt(world, (double)x, (double)y0, (double)z, 1.0F, 2.0F);
         }
      });
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemWeb.getBrewItemKey(), new BrewNamePart("witchery:brew.webs"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            this.placeWeb(world, new Coord(x, y, z), modifiers, side, modifiers.caster);
         }

         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            this.placeWeb(world, new Coord(targetEntity), modifiers, ForgeDirection.UNKNOWN, modifiers.caster);
         }

         private void placeWeb(World world, Coord coord, ModifiersEffect modifiers, ForgeDirection side, EntityPlayer source) {
            Coord location = BlockUtil.getClosestPlantableBlock(world, coord.x, coord.y, coord.z, side, source, true);
            if (location != null) {
               Block web = Witchery.Blocks.WEB;
               BlockUtil.setBlockIfReplaceable(world, location.x, location.y, location.z, web);
               if (modifiers.getStrength() > 0) {
                  BlockUtil.setBlockIfReplaceable(world, location.x + 1, location.y, location.z, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x - 1, location.y, location.z, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x, location.y, location.z + 1, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x, location.y, location.z - 1, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x, location.y + 1, location.z, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x, location.y - 1, location.z, web);
               }

               if (modifiers.getStrength() > 1) {
                  BlockUtil.setBlockIfReplaceable(world, location.x + 1, location.y, location.z + 1, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x - 1, location.y, location.z + 1, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x + 1, location.y, location.z - 1, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x - 1, location.y, location.z - 1, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x + 1, location.y + 1, location.z, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x - 1, location.y + 1, location.z, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x, location.y + 1, location.z + 1, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x, location.y + 1, location.z - 1, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x, location.y - 1, location.z + 1, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x, location.y - 1, location.z - 1, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x + 1, location.y - 1, location.z, web);
                  BlockUtil.setBlockIfReplaceable(world, location.x - 1, location.y - 1, location.z, web);
               }
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150395_bd), (new BrewNamePart("witchery:brew.vines")).setBaseDuration(TimeUtil.secsToTicks(90)), new AltarPower(150), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            int metax = false;
            byte meta;
            switch(side) {
            case NORTH:
               meta = 2;
               break;
            case SOUTH:
               meta = 3;
               break;
            case WEST:
               meta = 4;
               break;
            case EAST:
               meta = 5;
               break;
            default:
               return;
            }

            Block vine = Witchery.Blocks.VINE;
            int newX = x + side.offsetX;
            int newZ = z + side.offsetZ;

            int offsetY;
            for(offsetY = 0; BlockUtil.isReplaceableBlock(world, newX, y + offsetY, newZ) && y + offsetY > 0 && (modifiers.getStrength() > 0 || world.func_147439_a(x, y + offsetY, z).func_149688_o().func_76220_a()); --offsetY) {
               world.func_147465_d(newX, y + offsetY, newZ, vine, meta, 3);
            }

            for(offsetY = 1; (BlockUtil.isReplaceableBlock(world, newX, y + offsetY, newZ) || world.func_147439_a(newX, y + offsetY, newZ).func_149688_o() == Material.field_151584_j) && world.func_147439_a(x, y + offsetY, z).func_149688_o().func_76220_a() && y + offsetY < 255; ++offsetY) {
               world.func_147465_d(newX, y + offsetY, newZ, vine, meta, 3);
            }

         }

         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            BrewPotionEffect.applyPotionEffect(targetEntity, modifiers, Witchery.Potions.WRAPPED_IN_VINE, TimeUtil.secsToTicks(90), modifiers.noParticles, modifiers.caster);
         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150434_aF), (new BrewNamePart("witchery:brew.thorns")).setBaseDuration(TimeUtil.secsToTicks(90)), new AltarPower(150), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            Coord coord = null;
            if (world.func_147439_a(x, y, z) == Witchery.Blocks.CACTUS) {
               ++y;

               while(world.func_147439_a(x, y, z) == Witchery.Blocks.CACTUS) {
                  ++y;
               }

               if (BlockUtil.isReplaceableBlock(world, x, y, z)) {
                  coord = new Coord(x, y, z);
               }
            } else {
               coord = BlockUtil.getClosestPlantableBlock(world, x, y, z, side, modifiers.caster);
            }

            if (coord != null) {
               int i = 0;

               for(int height = 3 + modifiers.getStrength(); i < height && BlockUtil.isReplaceableBlock(world, coord.x, coord.y + i, coord.z) && coord.y + i < 255; ++i) {
                  world.func_147449_b(coord.x, coord.y + i, coord.z, Witchery.Blocks.CACTUS);
               }
            }

         }

         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            BrewPotionEffect.applyPotionEffect(targetEntity, modifiers, Witchery.Potions.SPIKED, TimeUtil.secsToTicks(90), modifiers.noParticles, modifiers.caster);
         }
      });
      this.register(new BrewActionSprouting(Witchery.Items.GENERIC.itemBranchEnt.getBrewItemKey(), (new BrewNamePart("witchery:brew.sprouting")).setBaseDuration(TimeUtil.secsToTicks(15)), new AltarPower(350), new Probability(1.0D), new EffectLevel(2)));
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemIcyNeedle.getBrewItemKey(), (new BrewNamePart("witchery:brew.cold")).setBaseDuration(TimeUtil.minsToTicks(3)), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            if (BlockProtect.checkModsForBreakOK(world, x, y, z, modifiers.caster)) {
               (new BlockActionReplaceSphere() {
                  protected boolean onShouldReplace(World world, int x, int y, int z, Block block) {
                     return block.func_149688_o() == Material.field_151586_h;
                  }

                  protected void onReplaceBlock(World world, int x, int y, int z, Block block) {
                     world.func_147449_b(x, y, z, Blocks.field_150432_aD);
                  }
               }).replaceBlocks(world, x, y, z, 2 + 2 * modifiers.getStrength());
            }

         }

         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            BrewPotionEffect.applyPotionEffect(targetEntity, modifiers, Witchery.Potions.CHILLED, TimeUtil.minsToTicks(3), modifiers.noParticles, modifiers.caster);
         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Items.field_151055_y), new BrewNamePart("witchery:brew.knockback"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            if (modifiers.impactLocation != null) {
               EntityUtil.pushback(world, targetEntity, modifiers.impactLocation, 1.0D + (double)modifiers.getStrength() * modifiers.powerScalingFactor, 0.5D + (double)modifiers.getStrength() * 0.2D);
            } else {
               double radius = (double)(3 + modifiers.getStrength());
               double radiusSq = radius * radius;
               EntityPosition position = new EntityPosition(targetEntity);
               List<Entity> entities = world.func_72839_b(targetEntity, position.getBounds(radius));
               Iterator i$ = entities.iterator();

               while(true) {
                  Entity entity;
                  do {
                     if (!i$.hasNext()) {
                        return;
                     }

                     entity = (Entity)i$.next();
                  } while(!(entity instanceof EntityLivingBase) && !(entity instanceof EntityItem));

                  if (targetEntity.func_70068_e(entity) <= radiusSq) {
                     EntityUtil.pushback(world, entity, position, 1.0D + (double)modifiers.getStrength() * modifiers.powerScalingFactor, 0.5D + (double)modifiers.getStrength() * 0.2D);
                  }
               }
            }
         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150345_g, 0), new BrewNamePart("witchery:brew.treeoak"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            Coord location = BlockUtil.getClosestPlantableBlock(world, x, y, z, side, modifiers.caster);
            if (location != null) {
               ((WorldGenAbstractTree)(modifiers.getStrength() > 1 ? new WorldGenBigTree(true) : new WorldGenTrees(true))).func_76484_a(world, world.field_73012_v, location.x, location.y, location.z);
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150345_g, 1), new BrewNamePart("witchery:brew.treespruce"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            Coord location = BlockUtil.getClosestPlantableBlock(world, x, y, z, side, modifiers.caster);
            if (location != null) {
               ((WorldGenAbstractTree)(modifiers.getStrength() > 1 ? new WorldGenMegaPineTree(false, world.field_73012_v.nextBoolean()) : new WorldGenTaiga2(true))).func_76484_a(world, world.field_73012_v, location.x, location.y, location.z);
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150345_g, 2), new BrewNamePart("witchery:brew.treebirch"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            Coord location = BlockUtil.getClosestPlantableBlock(world, x, y, z, side, modifiers.caster);
            if (location != null) {
               (new WorldGenForest(true, false)).func_76484_a(world, world.field_73012_v, location.x, location.y, location.z);
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150345_g, 3), new BrewNamePart("witchery:brew.treejungle"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            Coord location = BlockUtil.getClosestPlantableBlock(world, x, y, z, side, modifiers.caster);
            if (location != null) {
               ((WorldGenAbstractTree)(modifiers.strength > 2 ? new WorldGenMegaJungle(true, 10, 20, 3, 3) : new WorldGenTrees(true, 4 + world.field_73012_v.nextInt(7), 3, 3, false))).func_76484_a(world, world.field_73012_v, location.x, location.y, location.z);
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150345_g, 4), new BrewNamePart("witchery:brew.treeacacia"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            Coord location = BlockUtil.getClosestPlantableBlock(world, x, y, z, side, modifiers.caster);
            if (location != null) {
               (new WorldGenSavannaTree(true)).func_76484_a(world, world.field_73012_v, location.x, location.y, location.z);
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150345_g, 5), new BrewNamePart("witchery:brew.treedarkoak"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            Coord location = BlockUtil.getClosestPlantableBlock(world, x, y, z, side, modifiers.caster);
            if (location != null) {
               (new WorldGenCanopyTree(true)).func_76484_a(world, world.field_73012_v, location.x, location.y, location.z);
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Witchery.Blocks.SAPLING, 0), new BrewNamePart("witchery:brew.treerowan"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            Coord location = BlockUtil.getClosestPlantableBlock(world, x, y, z, side, modifiers.caster);
            if (location != null) {
               (new WorldGenWitchTree(true, 5, 0, 0, 1, false)).func_76484_a(world, world.field_73012_v, location.x, location.y, location.z);
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Witchery.Blocks.SAPLING, 1), new BrewNamePart("witchery:brew.treealder"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            Coord location = BlockUtil.getClosestPlantableBlock(world, x, y, z, side, modifiers.caster);
            if (location != null) {
               WorldGenLargeWitchTree tree = new WorldGenLargeWitchTree(true, 1, 1, 0.5D);
               tree.func_76487_a(0.6D, 0.5D, 0.5D);
               tree.func_76484_a(world, world.field_73012_v, location.x, location.y, location.z);
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Witchery.Blocks.SAPLING, 2), new BrewNamePart("witchery:brew.treehawthorn"), new AltarPower(200), new Probability(1.0D), new EffectLevel(2)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            Coord location = BlockUtil.getClosestPlantableBlock(world, x, y, z, side, modifiers.caster);
            if (location != null) {
               WorldGenLargeWitchTree tree = new WorldGenLargeWitchTree(true, 2, 2);
               tree.func_76487_a(0.8D, 1.2D, 1.0D);
               tree.func_76484_a(world, world.field_73012_v, location.x, location.y, location.z);
            }

         }
      });
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemHeartOfGold.getBrewItemKey(), new BrewNamePart("witchery:brew.animalattraction", "witchery:brew.animalrepulsion"), new AltarPower(500), new Probability(1.0D), new EffectLevel(4)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            double radius = (double)((modifiers.getStrength() + 1) * 16);
            double radiusSq = radius * radius;
            AxisAlignedBB bounds = targetEntity.field_70121_D.func_72314_b(radius, radius, radius);
            int maxCreatures = (int)Math.ceil(((double)modifiers.getStrength() + 1.0D) * 2.0D * modifiers.powerScalingFactor);
            List<EntityLivingBase> entities = world.func_72872_a(EntityLivingBase.class, bounds);
            Iterator i$ = entities.iterator();

            while(true) {
               while(true) {
                  EntityLivingBase otherEntity;
                  label67:
                  do {
                     EntityTameable tameable;
                     do {
                        do {
                           do {
                              if (!i$.hasNext()) {
                                 return;
                              }

                              otherEntity = (EntityLivingBase)i$.next();
                           } while(otherEntity == targetEntity);
                        } while(!(otherEntity.func_70068_e(targetEntity) < radiusSq));

                        if (!(otherEntity instanceof EntityTameable)) {
                           continue label67;
                        }

                        tameable = (EntityTameable)otherEntity;
                     } while(Familiar.couldBeFamiliar(tameable));

                     if (!modifiers.inverted) {
                        if (!tameable.func_70909_n()) {
                           tameable.func_70903_f(true);
                           tameable.func_70778_a((PathEntity)null);
                           tameable.func_70624_b((EntityLivingBase)null);
                           tameable.func_70606_j(20.0F);
                           tameable.func_152115_b(targetEntity.func_110124_au().toString());
                           world.func_72960_a(tameable, (byte)7);
                           if (tameable instanceof EntityOcelot) {
                              ((EntityOcelot)tameable).func_70912_b(1 + world.field_73012_v.nextInt(3));
                           }
                        }
                     } else if (tameable.func_70909_n() && !tameable.func_152114_e(targetEntity)) {
                        tameable.func_70903_f(false);
                        tameable.func_70778_a((PathEntity)null);
                        tameable.func_70624_b((EntityLivingBase)null);
                        tameable.func_152115_b("");
                        world.func_72960_a(tameable, (byte)6);
                        if (tameable instanceof EntityOcelot) {
                           ((EntityOcelot)tameable).func_70912_b(0);
                        }
                     }
                  } while(!(otherEntity instanceof EntityAnimal));

                  EntityAnimal animal = (EntityAnimal)otherEntity;
                  if (!modifiers.inverted) {
                     if (!animal.func_70661_as().func_75492_a(targetEntity.field_70165_t, targetEntity.field_70163_u, targetEntity.field_70161_v, 1.0D)) {
                        animal.func_70778_a(world.func_72844_a(animal, MathHelper.func_76128_c(targetEntity.field_70165_t), MathHelper.func_76128_c(targetEntity.field_70163_u), MathHelper.func_76128_c(targetEntity.field_70161_v), 10.0F, true, false, false, true));
                     }
                  } else {
                     int RANGE = 6;
                     int newX = MathHelper.func_76128_c(targetEntity.field_70165_t) + (world.field_73012_v.nextBoolean() ? 1 : -1) * (RANGE + world.field_73012_v.nextInt(RANGE));
                     int newZ = MathHelper.func_76128_c(targetEntity.field_70161_v) + (world.field_73012_v.nextBoolean() ? 1 : -1) * (RANGE + world.field_73012_v.nextInt(RANGE));

                     int newY;
                     for(newY = 62; !world.func_147437_c(newX, newY + 1, newZ); ++newY) {
                     }

                     if (!animal.func_70661_as().func_75492_a((double)newX, (double)newY, (double)newZ, 1.0D)) {
                        animal.func_70634_a(0.5D + (double)newX, (double)newY, 0.5D + (double)newZ);
                     }
                  }
               }
            }
         }
      });
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemSilverDust.getBrewItemKey(), new BrewNamePart("witchery:brew.harmwerewolves"), new AltarPower(1000), new Probability(1.0D), new EffectLevel(4)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            int strength = Math.min(modifiers.getStrength(), modifiers.strengthCeilingDisabled ? 3 : 1);
            if (CreatureUtil.isWerewolf(targetEntity)) {
               targetEntity.func_70097_a(new EntityDamageSourceIndirectSilver(modifiers.caster, modifiers.caster), (float)MathHelper.func_76143_f((double)(3 << strength) * modifiers.powerScalingFactor));
            } else {
               targetEntity.func_70097_a(DamageSource.func_76354_b(modifiers.caster, modifiers.caster), (float)MathHelper.func_76143_f((double)(1 << strength) * modifiers.powerScalingFactor));
            }

         }

         protected void doApplyRitualToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersRitual ritualModifiers, ModifiersEffect modifiers, ItemStack stack) {
            if (!world.field_72995_K) {
               WorldInfo worldinfo = ((WorldServer)world).func_72912_H();
               int i = (300 + world.field_73012_v.nextInt(600)) * 20;
               if (!worldinfo.func_76059_o()) {
                  worldinfo.func_76080_g(i);
                  worldinfo.func_76084_b(true);
               }

               if (modifiers.getStrength() >= 1 && !worldinfo.func_76061_m()) {
                  worldinfo.func_76090_f(i);
                  worldinfo.func_76069_a(true);
               }
            }

         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Witchery.Items.SEEDS_GARLIC), new BrewNamePart("witchery:brew.weakenvampires"), new AltarPower(500), new Probability(1.0D), new EffectLevel(4)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            if (CreatureUtil.isVampire(targetEntity)) {
               if (targetEntity instanceof EntityPlayer) {
                  ExtendedPlayer playerEx = ExtendedPlayer.get((EntityPlayer)targetEntity);
                  playerEx.decreaseBloodPower(50 + 20 * modifiers.getStrength(), false);
               }

               BrewPotionEffect.applyPotionEffect(targetEntity, modifiers, Potion.field_76437_t, TimeUtil.secsToTicks(90), false, modifiers.caster);
            }

         }
      });
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151144_bL, 1), new BrewNamePart("witchery:brew.wither"), new AltarPower(200), new Probability(1.0D), Potion.field_82731_v, (long)TimeUtil.secsToTicks(15), new EffectLevel(4)));
      this.register(new BrewActionEffect(new BrewItemKey(Witchery.Blocks.GLINT_WEED), new BrewNamePart("witchery:brew.inferno"), new AltarPower(750), new Probability(1.0D), new EffectLevel(3)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack stack) {
            if (!world.field_72995_K && !modifiers.isGlancing) {
               EntitySplatter.splatter(world, new Coord(x, y, z, side), modifiers.getStrength());
            }

         }

         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
            if (!world.field_72995_K && !modifiers.isGlancing && targetEntity instanceof EntityLivingBase) {
               new Coord(targetEntity);
               targetEntity.func_70015_d(2 + 2 * modifiers.getStrength());
               if (modifiers.powerScalingFactor == 1.0D) {
                  EntitySplatter.splatter(world, new Coord(targetEntity), modifiers.powerScalingFactor == 1.0D ? modifiers.getStrength() : 0);
               }
            }

         }
      });
      this.register(new BrewPotionEffect(Witchery.Items.GENERIC.itemInfernalBlood.getBrewItemKey(), new BrewNamePart("witchery:brew.fear"), new AltarPower(500), new Probability(1.0D), Witchery.Potions.GROTESQUE, (long)TimeUtil.minsToTicks(3), new EffectLevel(2)));
      this.register(new BrewPotionEffect(Dye.INK_SAC.getBrewItemKey(), new BrewNamePart("witchery:brew.blindness"), new AltarPower(1000), new Probability(1.0D), Potion.field_76440_q, (long)TimeUtil.secsToTicks(15), new EffectLevel(4)));
      this.register(new BrewPotionEffect(new BrewItemKey(Blocks.field_150328_O), new BrewNamePart("witchery:brew.love"), new AltarPower(500), new Probability(1.0D), Witchery.Potions.LOVE, (long)TimeUtil.secsToTicks(10), new EffectLevel(4)));
      this.register(new BrewPotionEffect(Witchery.Items.GENERIC.itemDemonHeart.getBrewItemKey(), new BrewNamePart("witchery:brew.paralysis"), new AltarPower(750), new Probability(1.0D), Witchery.Potions.PARALYSED, (long)TimeUtil.secsToTicks(10), new EffectLevel(4)));
      this.register(new BrewCurseEffect(Witchery.Items.GENERIC.itemDropOfLuck.getBrewItemKey(), new BrewNamePart("witchery:brew.potionmaster", "witchery:brew.insanity"), new AltarPower(5000), new Probability(1.0D), Witchery.Potions.BREWING_EXPERT, (long)TimeUtil.minsToTicks(6), Witchery.Potions.INSANITY, (long)TimeUtil.minsToTicks(3), new EffectLevel(4), true));
      this.register(new BrewCurseEffect(new BrewItemKey(Items.field_151078_bh), new BrewNamePart("witchery:potion.diseased"), new AltarPower(2000), new Probability(1.0D), Witchery.Potions.DISEASED, (long)TimeUtil.minsToTicks(3), new EffectLevel(4), false));
      this.register(new BrewCurseEffect(Witchery.Items.GENERIC.itemDisturbedCotton.getBrewItemKey(), new BrewNamePart("witchery:brew.sinking"), new AltarPower(3000), new Probability(1.0D), Witchery.Potions.SINKING, (long)TimeUtil.minsToTicks(3), new EffectLevel(4), false));
      this.register(new BrewCurseEffect(new BrewItemKey(Witchery.Blocks.EMBER_MOSS), new BrewNamePart("witchery:brew.overheating"), new AltarPower(3000), new Probability(1.0D), Witchery.Potions.OVERHEATING, (long)TimeUtil.minsToTicks(3), new EffectLevel(4), false));
      this.register(new BrewCurseEffect(Witchery.Items.GENERIC.itemMellifluousHunger.getBrewItemKey(), new BrewNamePart("witchery:brew.wakingnightmare"), new AltarPower(10000), new Probability(1.0D), Witchery.Potions.WAKING_NIGHTMARE, (long)TimeUtil.minsToTicks(3), new EffectLevel(4), false));
      this.register(new BrewPotionEffect(Witchery.Items.GENERIC.itemToeOfFrog.getBrewItemKey(), new BrewNamePart("witchery:brew.frogsleg"), new AltarPower(500), new Probability(1.0D), Witchery.Potions.DOUBLE_JUMP, (long)TimeUtil.minsToTicks(6), new EffectLevel(4)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151153_ao), new BrewNamePart("witchery:brew.absorbsion"), new AltarPower(1000), new Probability(1.0D), Potion.field_76444_x, (long)TimeUtil.secsToTicks(30), new EffectLevel(4)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151153_ao, 1), new BrewNamePart("witchery:brew.healthboost"), new AltarPower(1000), new Probability(1.0D), Potion.field_76434_w, (long)TimeUtil.minsToTicks(2), new EffectLevel(4)));
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemSubduedSpirit.getBrewItemKey(), new BrewNamePart("witchery:brew.wasting", "witchery:brew.fullness"), new AltarPower(500), new Probability(1.0D), new EffectLevel(4)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
            int hungerTicks = modifiers.getModifiedDuration(TimeUtil.secsToTicks(20));
            int poisonTicks = Math.max(modifiers.getModifiedDuration(TimeUtil.secsToTicks(3)), 40);
            int strength = 1 + modifiers.getStrength() / 2;
            if (targetEntity instanceof EntityPlayer) {
               EntityPlayer victim = (EntityPlayer)targetEntity;
               int minLevel;
               if (modifiers.inverted) {
                  minLevel = 6 + 2 * modifiers.getStrength();
                  victim.func_71024_bL().func_75122_a(minLevel, (float)minLevel);
               } else {
                  minLevel = 10 - modifiers.getStrength();
                  if (victim.func_71024_bL().func_75116_a() > minLevel) {
                     victim.func_71024_bL().func_75122_a(-minLevel, 2.0F);
                  }

                  victim.func_70690_d(new PotionEffect(Potion.field_76438_s.field_76415_H, hungerTicks, strength));
                  victim.func_70690_d(new PotionEffect(Potion.field_76436_u.field_76415_H, poisonTicks, 0));
               }
            } else if (modifiers.inverted) {
               targetEntity.func_70690_d(new PotionEffect(Potion.field_76428_l.field_76415_H, TimeUtil.secsToTicks(30), modifiers.getStrength()));
            } else {
               targetEntity.func_70690_d(new PotionEffect(Potion.field_82731_v.field_76415_H, hungerTicks, strength));
               targetEntity.func_70690_d(new PotionEffect(Potion.field_76436_u.field_76415_H, poisonTicks));
            }

         }
      });
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemOdourOfPurity.getBrewItemKey(), new BrewNamePart("witchery:brew.revealing"), new AltarPower(100), new Probability(1.0D), new EffectLevel(4)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
            boolean doDamage = false;
            if (targetEntity.func_70644_a(Potion.field_76441_p)) {
               targetEntity.func_82170_o(Potion.field_76441_p.field_76415_H);
            }

            if (targetEntity instanceof EntityPlayer && targetEntity.func_82150_aj()) {
               targetEntity.func_82142_c(false);
            }

            if (targetEntity instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)targetEntity;
               ExtendedPlayer playerEx = ExtendedPlayer.get(player);
               if (playerEx != null && playerEx.getCreatureType() == TransformCreature.PLAYER) {
                  ParticleEffect.SMOKE.send(SoundEffect.WITCHERY_RANDOM_POOF, player, 0.5D, 2.0D, 16);
                  Shapeshift.INSTANCE.shiftTo(player, TransformCreature.NONE);
               }
            }

            if (targetEntity instanceof EntitySummonedUndead && ((EntitySummonedUndead)targetEntity).isObscured()) {
               ((EntitySummonedUndead)targetEntity).setObscured(false);
            }

            int strength = modifiers.getStrength();
            if (doDamage && strength > 0) {
               targetEntity.func_70097_a(DamageSource.func_76354_b(modifiers.caster, modifiers.caster), (float)strength);
            }

         }
      });
      this.register(new BrewPotionEffect(Witchery.Items.GENERIC.itemFoulFume.getBrewItemKey(), new BrewNamePart("witchery:potion.stoutbelly"), new AltarPower(1000), new Probability(1.0D), Witchery.Potions.STOUT_BELLY, (long)TimeUtil.secsToTicks(90), new EffectLevel(4)));
      this.register(new BrewActionBlight(new BrewItemKey(Items.field_151170_bI), new BrewNamePart("witchery:brew.blight"), new AltarPower(2000), new Probability(1.0D), new EffectLevel(4)));
      this.register(new BrewActionTranspose(new BrewItemKey(Items.field_151079_bi), new BrewNamePart("witchery:brew.transpose"), new AltarPower(1000), new Probability(1.0D), new EffectLevel(4)));
      this.register(new BrewActionEffect(new BrewItemKey(Items.field_151042_j), new BrewNamePart("witchery:brew.transposeore"), new AltarPower(2000), new Probability(1.0D), new EffectLevel(4)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack stack) {
            int depth = radius + modifiers.strength;
            Block[] blockTypes = new Block[]{Blocks.field_150366_p, Blocks.field_150352_o, Blocks.field_150369_x, Blocks.field_150412_bA};
            this.slurpOres(world, x, y, z, radius, depth, blockTypes, modifiers, y + 2);
         }

         protected void doApplyRitualToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersRitual ritualModifiers, ModifiersEffect modifiers, ItemStack stack) {
            int r = ritualModifiers.covenSize + radius;
            int maxDepth = 4 * ritualModifiers.covenSize * (1 + modifiers.getStrength());
            int steps = true;
            Block[] blockTypes = new Block[]{Blocks.field_150366_p, Blocks.field_150352_o, Blocks.field_150369_x, Blocks.field_150412_bA};
            this.slurpOres(world, x, y - (ritualModifiers.pulses - 1) * 4, z, r, 4, blockTypes, modifiers, y + 2);
            ParticleEffect.FLAME.send(SoundEffect.FIREWORKS_BLAST1, world, (double)x, (double)y, (double)z, 1.0D, 1.0D, 16);
            ritualModifiers.setRitualStatus(ritualModifiers.pulses * 4 < maxDepth ? RitualStatus.ONGOING : RitualStatus.COMPLETE);
         }

         private void slurpOres(World world, int posX, int posY, int posZ, int radius, int depth, Block[] blockTypes, ModifiersEffect modifiers, int returnY) {
            int r = radius;
            int maxType = modifiers.getStrength();

            for(int x = posX - radius; x <= posX + r; ++x) {
               for(int z = posZ - r; z <= posZ + r; ++z) {
                  for(int y = posY - depth; y <= posY + r; ++y) {
                     Block blockID = world.func_147439_a(x, y, z);

                     for(int t = 0; t < blockTypes.length && t < maxType; ++t) {
                        if (blockID == blockTypes[t]) {
                           ItemStack newStack = new ItemStack(blockID);
                           EntityItem entity = new EntityItem(world, (double)posX + 0.5D, (double)posY + 0.5D, (double)posZ + 0.5D, newStack);
                           if (!world.field_72995_K) {
                              world.func_147468_f(x, y, z);
                              world.func_72838_d(entity);
                           }
                        }
                     }
                  }
               }
            }

         }
      });
      this.register(new BrewPotionEffect(new BrewItemKey(Blocks.field_150329_H, 0), new BrewNamePart("witchery:potion.volatility"), new AltarPower(1000), new Probability(1.0D), Witchery.Potions.VOLATILITY, (long)TimeUtil.secsToTicks(180), new EffectLevel(4)));
      this.register(new BrewPotionEffect(new BrewItemKey(Blocks.field_150330_I), new BrewNamePart("witchery:potion.volatility"), new AltarPower(1000), new Probability(1.0D), Witchery.Potions.VOLATILITY, (long)TimeUtil.secsToTicks(180), new EffectLevel(4)));
      this.register(new BrewPotionEffect(new BrewItemKey(Blocks.field_150425_aM), new BrewNamePart("witchery:brew.allergydark"), new AltarPower(4000), new Probability(1.0D), Witchery.Potions.DARKNESS_ALLERGY, (long)TimeUtil.minsToTicks(2), new EffectLevel(4)));
      this.register(new BrewPotionEffect(Witchery.Items.GENERIC.itemWhiffOfMagic.getBrewItemKey(), new BrewNamePart("witchery:potion.absorbmagic"), new AltarPower(2000), new Probability(1.0D), Witchery.Potions.ABSORB_MAGIC, (long)TimeUtil.secsToTicks(60), new EffectLevel(4)));
      this.register(new BrewActionRaising(Items.field_151103_aS, new AltarPower(2000), new EffectLevel(4)));
      this.register(new BrewActionRaiseLand(new BrewItemKey(Items.field_151128_bU), new BrewNamePart("witchery:brew.raiseland"), new AltarPower(2000), new EffectLevel(4)));
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150432_aD), new BrewNamePart("witchery:brew.harmdemons"), new AltarPower(500), new Probability(1.0D), new EffectLevel(5)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            int strength = Math.min(modifiers.getStrength(), modifiers.strengthCeilingDisabled ? 3 : 1);
            if (targetEntity instanceof EntityLeonard) {
               ((EntityLeonard)targetEntity).attackEntityFromWeakness(MathHelper.func_76143_f((double)(10 << strength) * modifiers.powerScalingFactor));
            } else if (CreatureUtil.isDemonic(targetEntity)) {
               targetEntity.func_70097_a(DamageSource.func_76354_b(modifiers.caster, modifiers.caster), (float)MathHelper.func_76143_f((double)(10 << strength) * modifiers.powerScalingFactor));
            } else {
               targetEntity.func_70097_a(DamageSource.func_76354_b(modifiers.caster, modifiers.caster), (float)MathHelper.func_76143_f((double)(3 << strength) * modifiers.powerScalingFactor));
            }

         }
      });
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemFrozenHeart.getBrewItemKey(), new BrewNamePart("witchery:brew.iceshell"), new AltarPower(500), new Probability(1.0D), new EffectLevel(5)) {
         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
            this.createSphere(world, modifiers, BlockUtil.getClosestPlantableBlock(world, x, y, z, side, modifiers.caster));
         }

         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            boolean resistent = targetEntity instanceof EntityDemon || targetEntity instanceof EntityBlaze || targetEntity instanceof IBossDisplayData || targetEntity instanceof EntityEnt || targetEntity instanceof EntityIronGolem;
            if (!resistent) {
               BrewPotionEffect.applyPotionEffect(targetEntity, modifiers, Witchery.Potions.CHILLED, TimeUtil.secsToTicks(10), modifiers.noParticles, modifiers.caster);
               if (!modifiers.isGlancing) {
                  this.createSphere(world, modifiers, new Coord(targetEntity));
               }
            } else if (targetEntity.field_71093_bK != -1) {
               Coord coord = new Coord(targetEntity);
               BlockUtil.setBlockIfReplaceable(world, coord.x, coord.y, coord.z, Blocks.field_150358_i);
            }

         }

         public void createSphere(final World world, ModifiersEffect modifiers, final Coord coord) {
            if (coord != null) {
               final int iceRadius = modifiers.getStrength() + (modifiers.getStrength() > 3 ? 2 : 1);
               (new BlockActionSphere() {
                  protected void onBlock(World worldx, int x, int y, int z) {
                     BlockUtil.setBlockIfReplaceable(worldx, x, y, z, Witchery.Blocks.PERPETUAL_ICE);
                  }

                  protected void onComplete() {
                     this.fillWith(world, coord.x, coord.y, coord.z, iceRadius, Blocks.field_150350_a, Witchery.Blocks.PERPETUAL_ICE);
                  }
               }).drawHollowSphere(world, coord.x, coord.y, coord.z, iceRadius);
            }

         }
      });
      this.register(new BrewPotionEffect(Witchery.Items.GENERIC.itemSpectralDust.getBrewItemKey(), new BrewNamePart("witchery:potion.reflectdamage"), new AltarPower(2000), new Probability(1.0D), Witchery.Potions.REFLECT_DAMAGE, (long)TimeUtil.secsToTicks(90), new EffectLevel(5)));
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemRefinedEvil.getBrewItemKey(), new BrewNamePart("witchery:brew.hellgate"), new AltarPower(3000), new Probability(1.0D), new EffectLevel(5)) {
         public boolean isRitualTargetLocationValid(MinecraftServer server, World world, int x, int y, int z, BlockPosition target, ModifiersRitual modifiers) {
            return CircleUtil.isMediumCircle(target.getWorld(server), target.x, target.y, target.z, Witchery.Blocks.GLYPH_INFERNAL);
         }

         protected void doApplyRitualToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersRitual ritualModifiers, ModifiersEffect modifiers, ItemStack stack) {
            InfusionInfernal.spawnCreature(world, EntityDemon.class, x, y, z, (EntityLivingBase)null, 0, 2, ParticleEffect.FLAME, SoundEffect.MOB_ENDERDRAGON_GROWL);
         }

         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack actionStack) {
         }

         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            BrewPotionEffect.applyPotionEffect(targetEntity, modifiers, Witchery.Potions.NETHER_BOUND, TimeUtil.minsToTicks(3), modifiers.noParticles, modifiers.caster);
         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Items.field_151043_k), new BrewNamePart("witchery:brew.blast"), new AltarPower(500), new Probability(1.0D), new EffectLevel(5)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
            if (modifiers.powerScalingFactor == 1.0D || world.field_73012_v.nextDouble() < modifiers.powerScalingFactor * 0.2D) {
               world.func_72876_a(modifiers.caster, targetEntity.field_70165_t, targetEntity.field_70163_u, targetEntity.field_70161_v, (float)modifiers.getStrength(), true);
            }

         }

         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack stack) {
            world.func_72876_a(modifiers.caster, (double)(x + side.offsetX) + 0.5D, (double)(y + side.offsetY) + 0.5D, (double)(z + side.offsetZ) + 0.5D, (float)modifiers.getStrength(), true);
         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Blocks.field_150398_cm), new BrewNamePart("witchery:brew.poisontoad"), new AltarPower(500), new Probability(1.0D), new EffectLevel(5)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
            if (!(targetEntity instanceof EntityToad) && modifiers.powerScalingFactor == 1.0D || world.field_73012_v.nextDouble() < modifiers.powerScalingFactor * 0.2D) {
               EntityToad toad = new EntityToad(world);
               toad.func_70012_b(targetEntity.field_70165_t, targetEntity.field_70163_u + (double)targetEntity.field_70131_O + 1.0D, targetEntity.field_70161_v, 0.0F, 0.0F);
               toad.setTimeToLive(60 + modifiers.getStrength() * 40, true);
               world.func_72838_d(toad);
            }

         }

         protected void doApplyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, ModifiersEffect modifiers, ItemStack stack) {
            EntityToad toad = new EntityToad(world);
            toad.func_70012_b(0.5D + (double)x, 2.5D + (double)y, 0.5D + (double)z, 0.0F, 0.0F);
            toad.setTimeToLive(60 + modifiers.getStrength() * 40, true);
            world.func_72838_d(toad);
         }
      });
      this.register(new BrewActionEffect(new BrewItemKey(Items.field_151061_bv), new BrewNamePart("witchery:brew.iceworld"), new AltarPower(2000), new Probability(1.0D), new EffectLevel(5)) {
         protected void doApplyToBlock(World world, int x, int y0, int z, ForgeDirection side, int radius, final ModifiersEffect modifiers, ItemStack actionStack) {
            if (!world.field_72995_K) {
               (new BlockActionSphere() {
                  public void onBlock(World world, int x, int y, int z) {
                     Block block = world.func_147439_a(x, y, z);
                     if (BlockProtect.checkModsForBreakOK(world, x, y, z, block, world.func_72805_g(x, y, z), modifiers.caster) && BlockProtect.canBreak(block, world)) {
                        world.func_72805_g(x, y, z);
                        if (block != Blocks.field_150466_ao) {
                           return;
                        }

                        int i1 = ((BlockDoor)block).func_150012_g(world, x, y, z);
                        if ((i1 & 8) != 0) {
                           --y;
                        }

                        world.func_147468_f(x, y, z);
                        world.func_147468_f(x, y + 1, z);
                        ItemDoor.func_150924_a(world, x, y, z, i1 & 3, Witchery.Blocks.PERPETUAL_ICE_DOOR);
                        ParticleEffect.SMOKE.send(SoundEffect.NONE, world, (double)x, (double)(y + 1), (double)z, 0.25D, 0.25D, 16);
                     }

                  }
               }).drawFilledSphere(world, x, y0, z, (int)Math.ceil(Math.max((double)radius * 1.5D, 1.0D)));
               (new BlockActionSphere() {
                  public void onBlock(World world, int x, int y, int z) {
                     Block block = world.func_147439_a(x, y, z);
                     if (BlockProtect.checkModsForBreakOK(world, x, y, z, block, world.func_72805_g(x, y, z), modifiers.caster) && BlockProtect.canBreak(block, world)) {
                        int meta = world.func_72805_g(x, y, z);
                        if (block != Blocks.field_150346_d && block != Blocks.field_150349_c && block != Blocks.field_150391_bh && block != Blocks.field_150354_m) {
                           if (block != Blocks.field_150347_e && block != Blocks.field_150341_Y && block != Blocks.field_150364_r && block != Blocks.field_150363_s && block != Witchery.Blocks.LOG) {
                              if (block != Blocks.field_150348_b && block != Blocks.field_150417_aV && block != Blocks.field_150336_V && block != Blocks.field_150344_f && block != Blocks.field_150362_t && block != Blocks.field_150361_u && block != Witchery.Blocks.LEAVES && block != Witchery.Blocks.PLANKS && block != Blocks.field_150322_A) {
                                 if (block != Blocks.field_150456_au && block != Blocks.field_150452_aw) {
                                    if (block != Blocks.field_150446_ar && block != Blocks.field_150389_bf && block != Blocks.field_150390_bg && block != Blocks.field_150476_ad && block != Blocks.field_150485_bF && block != Blocks.field_150372_bz && block != Blocks.field_150487_bG && block != Blocks.field_150481_bH && block != Blocks.field_150401_cl && block != Blocks.field_150400_ck && block != Witchery.Blocks.STAIRS_ALDER && block != Witchery.Blocks.STAIRS_HAWTHORN && block != Witchery.Blocks.STAIRS_ROWAN) {
                                       if (block != Blocks.field_150333_U && block != Blocks.field_150376_bx && block != Witchery.Blocks.WOOD_SLAB_SINGLE) {
                                          if (block != Blocks.field_150334_T && block != Blocks.field_150373_bw && block != Witchery.Blocks.WOOD_SLAB_DOUBLE) {
                                             if (block != Blocks.field_150422_aJ && block != Blocks.field_150463_bK) {
                                                if (block == Blocks.field_150396_be) {
                                                   world.func_147449_b(x, y, z, Witchery.Blocks.PERPETUAL_ICE_FENCE_GATE);
                                                } else {
                                                   if (block != Witchery.Blocks.STOCKADE) {
                                                      return;
                                                   }

                                                   world.func_147449_b(x, y, z, Witchery.Blocks.STOCKADE_ICE);
                                                }
                                             } else {
                                                world.func_147449_b(x, y, z, Witchery.Blocks.PERPETUAL_ICE_FENCE);
                                             }
                                          } else {
                                             world.func_147449_b(x, y, z, Witchery.Blocks.PERPETUAL_ICE_SLAB_DOUBLE);
                                          }
                                       } else {
                                          world.func_147449_b(x, y, z, Witchery.Blocks.PERPETUAL_ICE_SLAB_SINGLE);
                                       }
                                    } else {
                                       world.func_147465_d(x, y, z, Witchery.Blocks.PERPETUAL_ICE_STAIRS, meta, 3);
                                    }
                                 } else {
                                    world.func_147449_b(x, y, z, Witchery.Blocks.PERPETUAL_ICE_PRESSURE_PLATE);
                                 }
                              } else {
                                 world.func_147449_b(x, y, z, Witchery.Blocks.PERPETUAL_ICE);
                              }
                           } else {
                              world.func_147449_b(x, y, z, Blocks.field_150403_cj);
                           }
                        } else {
                           world.func_147449_b(x, y, z, Blocks.field_150433_aE);
                        }

                        ParticleEffect.SMOKE.send(SoundEffect.NONE, world, (double)x, (double)(y + 1), (double)z, 0.25D, 0.25D, 16);
                     }

                  }
               }).drawFilledSphere(world, x, y0, z, (int)Math.ceil(Math.max((double)radius * 1.5D, 1.0D)));
               SoundEffect.RANDOM_ORB.playAt(world, (double)x, (double)y0, (double)z, 0.5F, 2.0F);
            }

         }
      });
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemCondensedFear.getBrewItemKey(), new BrewNamePart("witchery:brew.drainmagic"), new AltarPower(1000), new Probability(1.0D), new EffectLevel(6)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack actionStack) {
            Witchery.modHooks.reducePowerLevels(targetEntity, 0.25F * (1.0F + (float)modifiers.getStrength()) * (float)modifiers.powerScalingFactor);
         }
      });
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151119_aD), new BrewNamePart("witchery:potion.fortune"), new AltarPower(1000), new Probability(1.0D), Witchery.Potions.FORTUNE, (long)TimeUtil.minsToTicks(3), new EffectLevel(6)));
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151115_aP, 1), new BrewNamePart("witchery:brew.allergysun"), new AltarPower(1000), new Probability(1.0D), Witchery.Potions.SUN_ALLERGY, (long)TimeUtil.secsToTicks(60), new EffectLevel(6)));
      this.register(new BrewPotionEffect(new BrewItemKey(Witchery.Blocks.BRAMBLE, 1), (new BrewNamePart("witchery:potion.illfitting")).setBaseDuration(TimeUtil.secsToTicks(6)), new AltarPower(8000), new Probability(1.0D), Witchery.Potions.ILL_FITTING, (long)TimeUtil.secsToTicks(6), new EffectLevel(6)));
      this.register(new BrewPotionEffect(Witchery.Items.GENERIC.itemHintOfRebirth.getBrewItemKey(), new BrewNamePart("witchery:brew.reincarnate"), new AltarPower(2500), new Probability(1.0D), Witchery.Potions.REINCARNATE, (long)TimeUtil.minsToTicks(3), new EffectLevel(6)));
      this.register(new BrewActionEffect(Witchery.Items.GENERIC.itemCreeperHeart.getBrewItemKey(), new BrewNamePart("witchery:brew.durationboost"), new AltarPower(5000), new Probability(1.0D), new EffectLevel(6)) {
         protected void doApplyToEntity(World world, EntityLivingBase target, ModifiersEffect modifiers, ItemStack actionStack) {
            if (target.func_70644_a(Witchery.Potions.QUEASY)) {
               if (target.field_70170_p.field_73012_v.nextInt(3) == 0) {
                  target.func_70690_d(new PotionEffect(Potion.field_76440_q.field_76415_H, TimeUtil.minsToTicks(2), 0, true));
               } else {
                  target.func_70690_d(new PotionEffect(Potion.field_76431_k.field_76415_H, TimeUtil.minsToTicks(5), 0, true));
               }
            } else {
               Collection<PotionEffect> potionEffects = target.func_70651_bq();
               if (!potionEffects.isEmpty()) {
                  List<PotionEffect> newEffects = new ArrayList();
                  int durationBoost = MathHelper.func_76143_f(modifiers.powerScalingFactor * (double)TimeUtil.minsToTicks(3));
                  Iterator i$ = potionEffects.iterator();

                  PotionEffect potionEffect;
                  while(i$.hasNext()) {
                     potionEffect = (PotionEffect)i$.next();
                     if (!Potion.field_76425_a[potionEffect.func_76456_a()].func_76403_b()) {
                        int remainingTicks = potionEffect.func_76459_b();
                        int newDuration = remainingTicks + Math.min(remainingTicks, durationBoost);
                        newEffects.add(new PotionEffect(potionEffect.func_76456_a(), newDuration, potionEffect.func_76458_c(), potionEffect.func_82720_e()));
                     }
                  }

                  target.func_70674_bp();
                  i$ = newEffects.iterator();

                  while(i$.hasNext()) {
                     potionEffect = (PotionEffect)i$.next();
                     target.func_70690_d(potionEffect);
                  }

                  int mins = 3 * Math.max(1, 4 - modifiers.getStrength());
                  target.func_70690_d(new PotionEffect(Witchery.Potions.QUEASY.field_76415_H, TimeUtil.minsToTicks(mins), 0, true));
               }
            }

         }
      });
      this.register(new BrewPotionEffect(new BrewItemKey(Items.field_151166_bC), new BrewNamePart("witchery:brew.resizing"), new AltarPower(2500), new Probability(1.0D), Witchery.Potions.RESIZING, (long)TimeUtil.secsToTicks(20), new EffectLevel(6)));
      this.register(new BrewActionEffect(new BrewItemKey(Items.field_151144_bL, 0), new BrewNamePart("witchery:brew.stealbuffs"), new AltarPower(100), new Probability(1.0D), new EffectLevel(6)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
            double radius = (double)((modifiers.getStrength() + 1) * 8);
            double radiusSq = radius * radius;
            AxisAlignedBB bounds = targetEntity.field_70121_D.func_72314_b(radius, radius, radius);
            int maxBuffs = (int)Math.ceil(((double)modifiers.getStrength() + 1.0D) * 2.0D * modifiers.powerScalingFactor);
            List<EntityLivingBase> entities = world.func_72872_a(EntityLivingBase.class, bounds);
            Iterator i$ = entities.iterator();

            while(i$.hasNext()) {
               EntityLivingBase otherEntity = (EntityLivingBase)i$.next();
               if (otherEntity != targetEntity && otherEntity.func_70068_e(targetEntity) < radiusSq) {
                  Collection<PotionEffect> potionEffects = otherEntity.func_70651_bq();
                  List<Integer> effectsToRemove = new ArrayList();
                  Iterator i$x = potionEffects.iterator();

                  while(i$x.hasNext()) {
                     PotionEffect effect = (PotionEffect)i$x.next();
                     Potion potion = Potion.field_76425_a[effect.func_76456_a()];
                     if (!PotionBase.isDebuff(potion) && PotionBase.isCurable(potion) && effect.func_76458_c() <= modifiers.getStrength()) {
                        PotionEffect myEffect = targetEntity.func_70660_b(potion);
                        if (myEffect != null) {
                           if (myEffect.func_76459_b() <= effect.func_76459_b() && myEffect.func_76458_c() <= effect.func_76458_c()) {
                              targetEntity.func_70690_d(new PotionEffect(effect));
                              effectsToRemove.add(effect.func_76456_a());
                              --maxBuffs;
                           }
                        } else {
                           targetEntity.func_70690_d(new PotionEffect(effect));
                           effectsToRemove.add(effect.func_76456_a());
                           --maxBuffs;
                        }

                        if (maxBuffs <= 0) {
                           break;
                        }
                     }
                  }

                  i$x = effectsToRemove.iterator();

                  while(i$x.hasNext()) {
                     int id = (Integer)i$x.next();
                     otherEntity.func_82170_o(id);
                  }

                  if (maxBuffs <= 0) {
                     break;
                  }
               }
            }

         }
      });
      this.register(new BrewPotionEffect(Witchery.Items.GENERIC.itemFocusedWill.getBrewItemKey(), new BrewNamePart("witchery:brew.keepinventory"), new AltarPower(10000), new Probability(1.0D), Witchery.Potions.KEEP_INVENTORY, (long)TimeUtil.minsToTicks(6), new EffectLevel(8)));
      this.register(new BrewPotionEffect(Witchery.Items.GENERIC.itemRedstoneSoup.getBrewItemKey(), new BrewNamePart("witchery:potion.keepeffects"), new AltarPower(10000), new Probability(1.0D), Witchery.Potions.KEEP_EFFECTS, (long)TimeUtil.minsToTicks(6), new EffectLevel(8)));
      this.register(new BrewActionBiomeChange(new BrewItemKey(Witchery.Items.BIOME_NOTE, 32767), new BrewNamePart("witchery:brew.seasons"), new AltarPower(5000), new Probability(1.0D), new EffectLevel(8)));
      this.register(new BrewActionEffect(new BrewItemKey(Items.field_151144_bL, 4), new BrewNamePart("witchery:brew.spreaddebuffs"), new AltarPower(2000), new Probability(1.0D), new EffectLevel(8)) {
         protected void doApplyToEntity(World world, EntityLivingBase targetEntity, ModifiersEffect modifiers, ItemStack stack) {
            int strength = modifiers.getStrength();
            double radius = (double)((strength + 1) * 4);
            double radiusSq = radius * radius;
            AxisAlignedBB bounds = targetEntity.field_70121_D.func_72314_b(radius, radius, radius);
            int maxBuffs = (int)Math.ceil(((double)strength + 1.0D) * modifiers.powerScalingFactor);
            List<EntityLivingBase> entities = world.func_72872_a(EntityLivingBase.class, bounds);
            List<EntityLivingBase> others = new ArrayList();
            Iterator i$ = entities.iterator();

            while(i$.hasNext()) {
               EntityLivingBase otherEntity = (EntityLivingBase)i$.next();
               if (otherEntity != targetEntity && otherEntity.func_70068_e(targetEntity) < radiusSq) {
                  others.add(otherEntity);
               }
            }

            Collection<PotionEffect> effects = targetEntity.func_70651_bq();
            List<Integer> effectsToRemove = new ArrayList();
            Iterator i$x = effects.iterator();

            while(i$x.hasNext()) {
               PotionEffect effect = (PotionEffect)i$x.next();
               Potion potion = Potion.field_76425_a[effect.func_76456_a()];
               if (PotionBase.isDebuff(potion) && PotionBase.isCurable(potion) && effect.func_76458_c() <= Math.max(strength - 1, 1)) {
                  effectsToRemove.add(potion.field_76415_H);
                  Iterator i$xx = others.iterator();

                  while(i$xx.hasNext()) {
                     EntityLivingBase other = (EntityLivingBase)i$xx.next();
                     other.func_70690_d(new PotionEffect(effect));
                  }

                  --maxBuffs;
                  if (maxBuffs <= 0) {
                     break;
                  }
               }
            }

            i$x = effectsToRemove.iterator();

            while(i$x.hasNext()) {
               int id = (Integer)i$x.next();
               targetEntity.func_82170_o(id);
            }

         }
      });
      this.register(new BrewActionRitualSummonMob(new BrewItemKey(Witchery.Items.WITCH_HAT), new AltarPower(10000), new BrewActionRitualSummonMob.Recipe[]{new BrewActionRitualSummonMob.Recipe(EntityLeonard.class, new ItemStack[]{new ItemStack(Items.field_151075_bm), Witchery.Items.GENERIC.itemTearOfTheGoddess.createStack(), Witchery.Items.GENERIC.itemDiamondVapour.createStack(), new ItemStack(Items.field_151045_i), new ItemStack(Items.field_151156_bN)})}));
      this.register(new BrewActionModifier(Witchery.Items.GENERIC.itemWaystoneBound.getBrewItemKey(), (BrewNamePart)null, new AltarPower(100)) {
         public void prepareRitual(World world, int x, int y, int z, ModifiersRitual modifiers, ItemStack stack) {
            modifiers.setTarget(stack);
         }
      });
      this.register(new BrewActionRitual(Witchery.Items.GENERIC.itemDogTongue.getBrewItemKey(), new AltarPower(250), true));
      this.register(new BrewActionRitualEntityTarget(new BrewItemKey(Witchery.Items.TAGLOCK_KIT, 32767), new AltarPower(1000)));
      this.register(new BrewActionRitualRecipe(new BrewItemKey(Witchery.Items.CHALK_RITUAL), new AltarPower(2000), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.CHALK_OTHERWHERE), new ItemStack[]{new ItemStack(Items.field_151075_bm), Witchery.Items.GENERIC.itemTearOfTheGoddess.createStack(), new ItemStack(Items.field_151079_bi)}), new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.CHALK_GOLDEN), new ItemStack[]{Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), new ItemStack(Items.field_151074_bl)}), new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.CHALK_INFERNAL), new ItemStack[]{new ItemStack(Items.field_151075_bm), new ItemStack(Items.field_151065_br)})}));
      this.register(new BrewActionRitualRecipe(new BrewItemKey(Items.field_151110_aK), new AltarPower(0), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(Witchery.Items.GENERIC.itemMutandis.createStack(6), new ItemStack[]{Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack()})}));
      this.register(new BrewActionRitualRecipe(Witchery.Items.GENERIC.itemMutandis.getBrewItemKey(), new AltarPower(0), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(new ItemStack(Items.field_151075_bm), new ItemStack[]{Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), Witchery.Items.GENERIC.itemTearOfTheGoddess.createStack(), Witchery.Items.GENERIC.itemDiamondVapour.createStack(), new ItemStack(Items.field_151079_bi), new ItemStack(Items.field_151015_O)}), new BrewActionRitualRecipe.Recipe(Witchery.Items.GENERIC.itemMutandisExtremis.createStack(), new ItemStack[]{new ItemStack(Items.field_151075_bm)})}));
      this.register(new BrewActionRitualRecipe(Witchery.Items.GENERIC.itemMutandisExtremis.getBrewItemKey(), new AltarPower(1800), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(new ItemStack(Blocks.field_150377_bs, 2), new ItemStack[]{Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), new ItemStack(Blocks.field_150348_b), new ItemStack(Blocks.field_150377_bs)}), new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.MUTATING_SPRIG), new ItemStack[]{new ItemStack(Items.field_151075_bm), Witchery.Items.GENERIC.itemBranchEnt.createStack()}), new BrewActionRitualRecipe.Recipe(Witchery.Items.GENERIC.itemDropOfLuck.createStack(), new ItemStack[]{Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), new ItemStack(Items.field_151075_bm), Witchery.Items.GENERIC.itemTearOfTheGoddess.createStack(), Witchery.Items.GENERIC.itemRefinedEvil.createStack()})}));
      this.register(new BrewActionRitualRecipe(new BrewItemKey(Blocks.field_150433_aE), new AltarPower(3000), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.BREW_ENDLESS_WATER, 1, 0), new ItemStack[]{new ItemStack(Items.field_151075_bm), new ItemStack(Items.field_151114_aO), new ItemStack(Items.field_151072_bj), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.BREW_ENDLESS_WATER, 1, 50), new ItemStack[]{new ItemStack(Items.field_151075_bm), new ItemStack(Items.field_151114_aO), new ItemStack(Items.field_151072_bj)}), new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.BREW_ENDLESS_WATER, 1, 80), new ItemStack[]{new ItemStack(Items.field_151075_bm), new ItemStack(Items.field_151114_aO)}), new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.BREW_ENDLESS_WATER, 1, 95), new ItemStack[]{new ItemStack(Items.field_151075_bm)})}));
      this.register(new BrewActionRitualRecipe(new BrewItemKey(Items.field_151044_h, 1), new AltarPower(0), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.BREW_FUEL, 1, 3), new ItemStack[]{Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), new ItemStack(Items.field_151114_aO), new ItemStack(Items.field_151072_bj), Witchery.Items.GENERIC.itemAttunedStoneCharged.createStack()}), new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.BREW_FUEL, 1, 2), new ItemStack[]{Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), new ItemStack(Items.field_151114_aO), new ItemStack(Items.field_151072_bj)}), new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.BREW_FUEL, 1, 1), new ItemStack[]{Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), new ItemStack(Items.field_151114_aO)}), new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.BREW_FUEL, 1, 0), new ItemStack[]{Witchery.Items.GENERIC.itemMandrakeRoot.createStack()})}));
      this.register(new BrewActionRitualRecipe(new BrewItemKey(Items.field_151147_al), new AltarPower(0), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(new ItemStack(Items.field_151157_am), new ItemStack[0])}));
      this.register(new BrewActionRitualRecipe(new BrewItemKey(Items.field_151076_bf), new AltarPower(0), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(new ItemStack(Items.field_151077_bg), new ItemStack[0])}));
      this.register(new BrewActionRitualRecipe(new BrewItemKey(Items.field_151082_bd), new AltarPower(0), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(new ItemStack(Items.field_151083_be), new ItemStack[0])}));
      this.register(new BrewActionRitualRecipe(Witchery.Items.GENERIC.itemOddPorkRaw.getBrewItemKey(), new AltarPower(0), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(Witchery.Items.GENERIC.itemOddPorkCooked.createStack(), new ItemStack[0])}));
      this.register(new BrewActionRitualRecipe(Witchery.Items.GENERIC.itemMuttonRaw.getBrewItemKey(), new AltarPower(0), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(Witchery.Items.GENERIC.itemMuttonCooked.createStack(), new ItemStack[0])}));
      this.register(new BrewActionRitualRecipe(new BrewItemKey(Witchery.Items.WITCH_HAND), new AltarPower(0), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(new ItemStack(Items.field_151078_bh, 6), new ItemStack[0])}));
      this.register(new BrewActionRitualRecipe(Witchery.Items.GENERIC.itemTormentedTwine.getBrewItemKey(), new AltarPower(4000), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Blocks.PIT_GRASS, 4), new ItemStack[]{new ItemStack(Items.field_151075_bm), new ItemStack(Blocks.field_150346_d), new ItemStack(Blocks.field_150327_N)}), new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Blocks.PIT_DIRT, 4), new ItemStack[]{Witchery.Items.GENERIC.itemMandrakeRoot.createStack(), new ItemStack(Blocks.field_150346_d)})}));
      this.register(new BrewActionRitualRecipe(new BrewItemKey(Items.field_151111_aL), new AltarPower(5000), new BrewActionRitualRecipe.Recipe[]{new BrewActionRitualRecipe.Recipe(new ItemStack(Witchery.Items.PLAYER_COMPASS), new ItemStack[]{new ItemStack(Items.field_151075_bm), Witchery.Items.GENERIC.itemTearOfTheGoddess.createStack(), new ItemStack(Blocks.field_150395_bd), new ItemStack(Items.field_151070_bp)})}));
   }

   public List<BrewActionRitualRecipe> getRecipes() {
      return this.recipes;
   }

   private BrewAction register(BrewAction ingredient) {
      if (!this.ingredients.containsKey(ingredient.ITEM_KEY)) {
         this.ingredients.put(ingredient.ITEM_KEY, ingredient);
         if (ingredient instanceof BrewActionRitualRecipe) {
            this.recipes.add((BrewActionRitualRecipe)ingredient);
         }
      }

      return ingredient;
   }

   public int getAltarPower(ItemStack stack) {
      BrewItemKey key = BrewItemKey.fromStack(stack);
      if (key != null) {
         BrewAction action = (BrewAction)this.ingredients.get(key);
         if (action != null) {
            AltarPower power = new AltarPower(0);
            action.accumulatePower(power);
            return power.getPower();
         }
      }

      return -1;
   }

   public BrewAction getActionForItemStack(ItemStack stack) {
      return (BrewAction)this.ingredients.get(BrewItemKey.fromStack(stack));
   }

   public int getBrewColor(NBTTagCompound nbtRoot) {
      return nbtRoot != null ? nbtRoot.func_74762_e("Color") : -16744448;
   }

   public AltarPower getPowerRequired(NBTTagCompound nbtBrew) {
      BrewActionList actionList = new BrewActionList(nbtBrew, this.ingredients);
      AltarPower totalPower = new AltarPower(0);
      Iterator i$ = actionList.actions.iterator();

      while(i$.hasNext()) {
         BrewAction action = (BrewAction)i$.next();
         action.accumulatePower(totalPower);
      }

      return totalPower;
   }

   public boolean isSplash(NBTTagCompound nbtBrew) {
      if (nbtBrew != null && !nbtBrew.func_74764_b("Splash")) {
         nbtBrew.func_74757_a("Splash", false);
         BrewActionList actionList = new BrewActionList(nbtBrew, this.ingredients);
         Iterator i$ = actionList.actions.iterator();

         while(i$.hasNext()) {
            BrewAction action = (BrewAction)i$.next();
            if (action.createsSplash()) {
               nbtBrew.func_74757_a("Splash", true);
               break;
            }
         }
      }

      return nbtBrew != null && nbtBrew.func_74767_n("Splash");
   }

   public String getBrewName(NBTTagCompound nbtRoot) {
      BrewActionList actionList = new BrewActionList(nbtRoot, this.ingredients);
      BrewNameBuilder nameBuilder = new BrewNameBuilder(true);
      Iterator i$ = actionList.actions.iterator();

      while(i$.hasNext()) {
         BrewAction action = (BrewAction)i$.next();
         BrewNamePart name = action.getNamePart();
         if (name != null) {
            name.applyTo(nameBuilder);
         }
      }

      return nameBuilder.toString();
   }

   public String getBrewInformation(NBTTagCompound nbtRoot) {
      BrewActionList actionList = new BrewActionList(nbtRoot, this.ingredients);
      BrewNameBuilder nameBuilder = new BrewNameBuilder(false);
      Iterator i$ = actionList.actions.iterator();

      while(i$.hasNext()) {
         BrewAction action = (BrewAction)i$.next();
         BrewNamePart name = action.getNamePart();
         if (name != null) {
            name.applyTo(nameBuilder);
         }
      }

      String tooltip = nameBuilder.toString();
      int drinkSpeed = this.getBrewDrinkSpeed(nbtRoot);
      if (drinkSpeed != 32) {
         if (drinkSpeed > 48) {
            tooltip = tooltip + String.format(Witchery.resource("witchery:brew.drinkspeed"), Witchery.resource("witchery:brew.drinkspeed.veryslow"));
         } else if (drinkSpeed > 32) {
            tooltip = tooltip + String.format(Witchery.resource("witchery:brew.drinkspeed"), Witchery.resource("witchery:brew.drinkspeed.slow"));
         } else if (drinkSpeed < 16) {
            tooltip = tooltip + String.format(Witchery.resource("witchery:brew.drinkspeed"), Witchery.resource("witchery:brew.drinkspeed.veryfast"));
         } else if (drinkSpeed < 32) {
            tooltip = tooltip + String.format(Witchery.resource("witchery:brew.drinkspeed"), Witchery.resource("witchery:brew.drinkspeed.fast"));
         }
      }

      return tooltip;
   }

   public void updateBrewInformation(NBTTagCompound nbtRoot) {
      BrewActionList actionList = new BrewActionList(nbtRoot, this.ingredients);
      BrewNameBuilder nameBuilder = new BrewNameBuilder(false);
      Iterator i$ = actionList.actions.iterator();

      while(i$.hasNext()) {
         BrewAction action = (BrewAction)i$.next();
         BrewNamePart name = action.getNamePart();
         if (name != null) {
            name.applyTo(nameBuilder);
         }
      }

      String tooltip = nameBuilder.toString();
      int drinkSpeed = this.getBrewDrinkSpeed(nbtRoot);
      if (drinkSpeed != 32) {
         if (drinkSpeed > 48) {
            tooltip = tooltip + String.format(Witchery.resource("witchery:brew.drinkspeed"), Witchery.resource("witchery:brew.drinkspeed.veryslow"));
         } else if (drinkSpeed > 32) {
            tooltip = tooltip + String.format(Witchery.resource("witchery:brew.drinkspeed"), Witchery.resource("witchery:brew.drinkspeed.slow"));
         } else if (drinkSpeed < 16) {
            tooltip = tooltip + String.format(Witchery.resource("witchery:brew.drinkspeed"), Witchery.resource("witchery:brew.drinkspeed.veryfast"));
         } else if (drinkSpeed < 32) {
            tooltip = tooltip + String.format(Witchery.resource("witchery:brew.drinkspeed"), Witchery.resource("witchery:brew.drinkspeed.fast"));
         }
      }

      nbtRoot.func_74778_a("BrewInfo", tooltip);
      EffectLevelCounter effectCounter = new EffectLevelCounter();
      boolean actionFound = false;
      Iterator i$ = actionList.actions.iterator();

      while(i$.hasNext()) {
         BrewAction action = (BrewAction)i$.next();
         action.augmentEffectLevels(effectCounter);
      }

      nbtRoot.func_74768_a("EffectCount", effectCounter.getEffectCount());
      nbtRoot.func_74768_a("RemainingCapacity", effectCounter.remainingCapactiy());
      nbtRoot.func_74768_a("UsedCapacity", effectCounter.usedCapacity());
   }

   public int getUsedCapacity(NBTTagCompound nbtRoot) {
      return nbtRoot != null ? nbtRoot.func_74762_e("UsedCapacity") : 0;
   }

   public int getBrewDrinkSpeed(NBTTagCompound nbtRoot) {
      BrewActionList actionList = new BrewActionList(nbtRoot, this.ingredients);
      int drinkSpeed = 32;

      BrewAction action;
      for(Iterator i$ = actionList.actions.iterator(); i$.hasNext(); drinkSpeed += action.getDrinkSpeedModifiers()) {
         action = (BrewAction)i$.next();
      }

      return Math.max(drinkSpeed, 2);
   }

   public boolean canAdd(NBTTagCompound nbtRoot, BrewAction brewAction, boolean isCauldronFull) {
      if (nbtRoot.func_74767_n("RitualTriggered")) {
         return false;
      } else {
         BrewActionList actionList = new BrewActionList(nbtRoot, this.ingredients);
         EffectLevelCounter effectCounter = new EffectLevelCounter();
         boolean actionFound = false;
         Iterator i$ = actionList.actions.iterator();

         while(true) {
            while(i$.hasNext()) {
               BrewAction action = (BrewAction)i$.next();
               action.augmentEffectLevels(effectCounter);
               if (action == brewAction) {
                  actionFound = true;
               } else if (action instanceof BrewActionEffect || action instanceof BrewPotionEffect) {
                  actionFound = false;
               }
            }

            if (!brewAction.canAdd(actionList, isCauldronFull, effectCounter.hasEffects())) {
               return false;
            }

            return !actionFound && brewAction.augmentEffectLevels(effectCounter);
         }
      }
   }

   public EffectLevelCounter getBrewLevel(NBTTagCompound nbtRoot) {
      BrewActionList actionList = new BrewActionList(nbtRoot, this.ingredients);
      EffectLevelCounter effectCounter = new EffectLevelCounter();
      Iterator i$ = actionList.actions.iterator();

      while(i$.hasNext()) {
         BrewAction action = (BrewAction)i$.next();
         action.augmentEffectLevels(effectCounter);
      }

      return effectCounter;
   }

   public void nullifyItems(NBTTagCompound nbtRoot, NBTTagList nbtItems, BrewAction brewAction) {
      BrewActionList actionList = new BrewActionList(nbtRoot, this.ingredients);
      actionList.nullifyItems(brewAction, nbtItems);
   }

   public void explodeBrew(World world, NBTTagCompound nbtRoot, Entity immuneEntity, double x, double y, double z) {
      BrewActionList actionList = new BrewActionList(nbtRoot, this.ingredients);
      world.func_72876_a(immuneEntity, x, y, z, Math.min(1.0F + (float)actionList.size() * 0.5F, 10.0F), false);
   }

   public ModifierYield getYieldModifier(NBTTagCompound nbtBrew) {
      BrewActionList actionList = new BrewActionList(nbtBrew, this.ingredients);
      ModifierYield counter = new ModifierYield(0);
      Iterator i$ = actionList.actions.iterator();

      while(i$.hasNext()) {
         BrewAction action = (BrewAction)i$.next();
         action.prepareYield(counter);
      }

      return counter;
   }

   public RitualStatus updateRitual(MinecraftServer server, World world, int x, int y, int z, NBTTagCompound nbtBrew, int covenSize, int ritualPulses, boolean lennyPresent) {
      BrewActionList actionList = new BrewActionList(nbtBrew, this.ingredients);
      ModifiersRitual modifiers = new ModifiersRitual(new BlockPosition(world, x, y, z), covenSize, ritualPulses, lennyPresent);
      actionList.prepareRitual(world, x, y, z, modifiers);
      ModifiersImpact modifiersImpact = new ModifiersImpact(new EntityPosition(modifiers.getTarget()), true, modifiers.covenSize, (EntityPlayer)null);
      Iterator i$ = actionList.actions.iterator();

      while(i$.hasNext()) {
         BrewAction action = (BrewAction)i$.next();
         action.prepareSplashPotion(world, actionList, modifiersImpact);
      }

      return actionList.getTopAction().updateRitual(server, actionList, world, x, y, z, modifiers, modifiersImpact);
   }

   public boolean impactSplashPotion(World world, ItemStack stack, MovingObjectPosition mop, ModifiersImpact modifiers) {
      return this.impactSplashPotion(world, new BrewActionList(stack.func_77978_p(), this.ingredients), mop, modifiers);
   }

   public boolean impactSplashPotion(World world, NBTTagCompound nbtBrew, MovingObjectPosition mop, ModifiersImpact modifiers) {
      return this.impactSplashPotion(world, new BrewActionList(nbtBrew, this.ingredients), mop, modifiers);
   }

   public boolean impactSplashPotion(World world, BrewActionList actionList, MovingObjectPosition mop, ModifiersImpact modifiers) {
      Iterator i$ = actionList.actions.iterator();

      while(i$.hasNext()) {
         BrewAction action = (BrewAction)i$.next();
         action.prepareSplashPotion(world, actionList, modifiers);
      }

      modifiers.getDispersal().onImpactSplashPotion(world, actionList.getTagCompound(), mop, modifiers);
      return true;
   }

   public void applyToEntity(World world, EntityLivingBase targetEntity, NBTTagCompound nbtBrew, ModifiersEffect modifiers) {
      BrewActionList actionList = new BrewActionList(nbtBrew, this.ingredients);
      actionList.applyToEntity(world, targetEntity, modifiers);
   }

   public void applyToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, NBTTagCompound nbtBrew, ModifiersEffect modifiers) {
      BrewActionList actionList = new BrewActionList(nbtBrew, this.ingredients);
      actionList.applyToBlock(world, x, y, z, side, radius, modifiers);
   }

   public void applyRitualToEntity(World world, EntityLivingBase target, NBTTagCompound nbtBrew, ModifiersRitual ritualModifiers, ModifiersEffect modifiers) {
      BrewActionList actionList = new BrewActionList(nbtBrew, this.ingredients);
      actionList.applyRitualToEnitity(world, target, ritualModifiers, modifiers);
   }

   public void applyRitualToBlock(World world, int x, int y, int z, ForgeDirection side, int radius, NBTTagCompound nbtBrew, ModifiersRitual ritualModifiers, ModifiersEffect effectModifiers) {
      BrewActionList actionList = new BrewActionList(nbtBrew, this.ingredients);
      actionList.applyRitualToBlock(world, x, y, z, side, radius, ritualModifiers, effectModifiers);
   }

   public void init() {
   }
}
