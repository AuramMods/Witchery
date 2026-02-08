package com.emoniph.witchery.item;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockChalice;
import com.emoniph.witchery.blocks.BlockCircle;
import com.emoniph.witchery.blocks.BlockCrystalBall;
import com.emoniph.witchery.blocks.BlockDreamCatcher;
import com.emoniph.witchery.blocks.BlockPlacedItem;
import com.emoniph.witchery.blocks.BlockWickerBundle;
import com.emoniph.witchery.brewing.BrewItemKey;
import com.emoniph.witchery.brewing.potions.PotionEnslaved;
import com.emoniph.witchery.common.ExtendedPlayer;
import com.emoniph.witchery.common.Shapeshift;
import com.emoniph.witchery.dimension.WorldProviderDreamWorld;
import com.emoniph.witchery.entity.EntityBroom;
import com.emoniph.witchery.entity.EntityCovenWitch;
import com.emoniph.witchery.entity.EntityDeathsHorse;
import com.emoniph.witchery.entity.EntityEye;
import com.emoniph.witchery.entity.EntityLordOfTorment;
import com.emoniph.witchery.entity.EntitySpirit;
import com.emoniph.witchery.entity.EntitySummonedUndead;
import com.emoniph.witchery.entity.EntityTreefyd;
import com.emoniph.witchery.entity.EntityWitchProjectile;
import com.emoniph.witchery.familiar.Familiar;
import com.emoniph.witchery.infusion.InfusedBrew;
import com.emoniph.witchery.infusion.InfusedBrewEffect;
import com.emoniph.witchery.infusion.Infusion;
import com.emoniph.witchery.infusion.PlayerEffects;
import com.emoniph.witchery.infusion.infusions.InfusionInfernal;
import com.emoniph.witchery.infusion.infusions.InfusionOtherwhere;
import com.emoniph.witchery.infusion.infusions.symbols.EffectRegistry;
import com.emoniph.witchery.infusion.infusions.symbols.SymbolEffect;
import com.emoniph.witchery.item.brew.BrewFluid;
import com.emoniph.witchery.item.brew.BrewSolidifySpirit;
import com.emoniph.witchery.item.brew.BrewSoul;
import com.emoniph.witchery.network.PacketCamPos;
import com.emoniph.witchery.network.PacketParticles;
import com.emoniph.witchery.network.PacketPlayerStyle;
import com.emoniph.witchery.util.BlockProtect;
import com.emoniph.witchery.util.BlockSide;
import com.emoniph.witchery.util.BlockUtil;
import com.emoniph.witchery.util.ChatUtil;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Coord;
import com.emoniph.witchery.util.CreatureUtil;
import com.emoniph.witchery.util.EffectSpiral;
import com.emoniph.witchery.util.EntityUtil;
import com.emoniph.witchery.util.ISpiralBlockAction;
import com.emoniph.witchery.util.MutableBlock;
import com.emoniph.witchery.util.ParticleEffect;
import com.emoniph.witchery.util.SoundEffect;
import com.emoniph.witchery.util.TargetPointUtil;
import com.emoniph.witchery.util.TimeUtil;
import com.emoniph.witchery.util.TransformCreature;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S14PacketEntity.S17PacketEntityLookMove;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class ItemGeneral extends ItemBase {
   public final ArrayList<ItemGeneral.SubItem> subItems = new ArrayList();
   public final ArrayList<ItemGeneral.DreamWeave> weaves = new ArrayList();
   public final ItemGeneral.SubItem itemCandelabra;
   public final ItemGeneral.SubItem itemChaliceEmpty;
   public final ItemGeneral.SubItem itemChaliceFull;
   public final ItemGeneral.DreamWeave itemDreamMove;
   public final ItemGeneral.DreamWeave itemDreamDig;
   public final ItemGeneral.DreamWeave itemDreamEat;
   public final ItemGeneral.DreamWeave itemDreamNightmare;
   public final ItemGeneral.SubItem itemBoneNeedle;
   public final ItemGeneral.SubItem itemBroom;
   public final ItemGeneral.SubItem itemBroomEnchanted;
   public final ItemGeneral.SubItem itemAttunedStone;
   public final ItemGeneral.SubItem itemAttunedStoneCharged;
   public final ItemGeneral.SubItem itemWaystone;
   public final ItemGeneral.SubItem itemWaystoneBound;
   public final ItemGeneral.SubItem itemMutandis;
   public final ItemGeneral.SubItem itemMutandisExtremis;
   public final ItemGeneral.SubItem itemQuicklime;
   public final ItemGeneral.SubItem itemGypsum;
   public final ItemGeneral.SubItem itemAshWood;
   public final ItemGeneral.SubItem itemBelladonnaFlower;
   public final ItemGeneral.SubItem itemMandrakeRoot;
   private static final int DEMON_FOOD_DURATION = 2400;
   public final ItemGeneral.SubItem itemDemonHeart;
   public final ItemGeneral.SubItem itemBatWool;
   public final ItemGeneral.SubItem itemDogTongue;
   public final ItemGeneral.SubItem itemSoftClayJar;
   public final ItemGeneral.SubItem itemEmptyClayJar;
   public final ItemGeneral.SubItem itemFoulFume;
   public final ItemGeneral.SubItem itemDiamondVapour;
   public final ItemGeneral.SubItem itemOilOfVitriol;
   public final ItemGeneral.SubItem itemExhaleOfTheHornedOne;
   public final ItemGeneral.SubItem itemBreathOfTheGoddess;
   public final ItemGeneral.SubItem itemHintOfRebirth;
   public final ItemGeneral.SubItem itemWhiffOfMagic;
   public final ItemGeneral.SubItem itemReekOfMisfortune;
   public final ItemGeneral.SubItem itemOdourOfPurity;
   public final ItemGeneral.SubItem itemTearOfTheGoddess;
   public final ItemGeneral.SubItem itemRefinedEvil;
   public final ItemGeneral.SubItem itemDropOfLuck;
   public final ItemGeneral.SubItem itemRedstoneSoup;
   public final ItemGeneral.SubItem itemFlyingOintment;
   public final ItemGeneral.SubItem itemGhostOfTheLight;
   public final ItemGeneral.SubItem itemSoulOfTheWorld;
   public final ItemGeneral.SubItem itemSpiritOfOtherwhere;
   public final ItemGeneral.SubItem itemInfernalAnimus;
   public final ItemGeneral.SubItem itemBookOven;
   public final ItemGeneral.SubItem itemBookDistilling;
   public final ItemGeneral.SubItem itemBookCircleMagic;
   public final ItemGeneral.SubItem itemBookInfusions;
   public final ItemGeneral.SubItem itemOddPorkRaw;
   public final ItemGeneral.SubItem itemOddPorkCooked;
   public final ItemGeneral.SubItem itemDoorRowan;
   public final ItemGeneral.SubItem itemDoorAlder;
   public final ItemGeneral.SubItem itemDoorKey;
   public final ItemGeneral.SubItem itemRock;
   public final ItemGeneral.SubItem itemWeb;
   public final ItemGeneral.SubItem itemBrewOfVines;
   public final ItemGeneral.SubItem itemBrewOfWebs;
   public final ItemGeneral.SubItem itemBrewOfThorns;
   public final ItemGeneral.SubItem itemBrewOfInk;
   public final ItemGeneral.SubItem itemBrewOfSprouting;
   public final ItemGeneral.SubItem itemBrewOfErosion;
   public final ItemGeneral.SubItem itemRowanBerries;
   public final ItemGeneral.SubItem itemNecroStone;
   public final ItemGeneral.SubItem itemBrewOfRaising;
   public final ItemGeneral.SubItem itemSpectralDust;
   public final ItemGeneral.SubItem itemEnderDew;
   public final ItemGeneral.SubItem itemArtichoke;
   public final ItemGeneral.SubItem itemSeedsTreefyd;
   public final ItemGeneral.SubItem itemBrewGrotesque;
   public final ItemGeneral.SubItem itemImpregnatedLeather;
   public final ItemGeneral.SubItem itemFumeFilter;
   public final ItemGeneral.SubItem itemCreeperHeart;
   public final ItemGeneral.SubItem itemBrewOfLove;
   public final ItemGeneral.SubItem itemBrewOfIce;
   public final ItemGeneral.SubItem itemBrewOfTheDepths;
   public final ItemGeneral.SubItem itemIcyNeedle;
   public final ItemGeneral.SubItem itemFrozenHeart;
   public final ItemGeneral.SubItem itemInfernalBlood;
   public final ItemGeneral.SubItem itemBookHerbology;
   public final ItemGeneral.SubItem itemBranchEnt;
   public final ItemGeneral.SubItem itemMysticUnguent;
   public final ItemGeneral.SubItem itemDoorKeyring;
   public final ItemGeneral.SubItem itemBrewOfFrogsTongue;
   public final ItemGeneral.SubItem itemBrewOfCursedLeaping;
   public final ItemGeneral.SubItem itemBrewOfHitchcock;
   public final ItemGeneral.SubItem itemBrewOfInfection;
   public final ItemGeneral.SubItem itemOwletsWing;
   public final ItemGeneral.SubItem itemToeOfFrog;
   public final ItemGeneral.SubItem itemWormyApple;
   public final ItemGeneral.SubItem itemQuartzSphere;
   public final ItemGeneral.SubItem itemHappenstanceOil;
   public final ItemGeneral.SubItem itemSeerStone;
   public final ItemGeneral.SubItem itemBrewOfSleeping;
   public final ItemGeneral.SubItem itemBrewOfFlowingSpirit;
   public final ItemGeneral.SubItem itemBrewOfWasting;
   public final ItemGeneral.SubItem itemSleepingApple;
   public final ItemGeneral.SubItem itemDisturbedCotton;
   public final ItemGeneral.SubItem itemFancifulThread;
   public final ItemGeneral.SubItem itemTormentedTwine;
   public final ItemGeneral.SubItem itemGoldenThread;
   public final ItemGeneral.SubItem itemMellifluousHunger;
   public final ItemGeneral.DreamWeave itemDreamIntensity;
   public final ItemGeneral.SubItem itemPurifiedMilk;
   public final ItemGeneral.SubItem itemBookBiomes;
   public final ItemGeneral.SubItem itemBookWands;
   public final ItemGeneral.SubItem itemBatBall;
   public final ItemGeneral.SubItem itemBrewOfBats;
   public final ItemGeneral.SubItem itemCharmOfDisruptedDreams;
   public final ItemGeneral.SubItem itemWormwood;
   public final ItemGeneral.SubItem itemSubduedSpirit;
   public final ItemGeneral.SubItem itemFocusedWill;
   public final ItemGeneral.SubItem itemCondensedFear;
   public final ItemGeneral.SubItem itemBrewOfHollowTears;
   public final ItemGeneral.SubItem itemBrewOfSolidRock;
   public final ItemGeneral.SubItem itemBrewOfSolidDirt;
   public final ItemGeneral.SubItem itemBrewOfSolidSand;
   public final ItemGeneral.SubItem itemBrewOfSolidSandstone;
   public final ItemGeneral.SubItem itemBrewOfSolidErosion;
   public final ItemGeneral.SubItem itemInfusionBase;
   public final ItemGeneral.SubItem itemBrewOfSoaring;
   public final ItemGeneral.SubItem itemBrewGrave;
   public final ItemGeneral.SubItem itemBrewRevealing;
   public final ItemGeneral.SubItem itemBrewSubstitution;
   public final ItemGeneral.SubItem itemCongealedSpirit;
   public final ItemGeneral.SubItem itemBookBurning;
   public final ItemGeneral.SubItem itemGraveyardDust;
   public final ItemGeneral.SubItem itemBinkyHead;
   public final ItemGeneral.SubItem itemNullCatalyst;
   public final ItemGeneral.SubItem itemNullifiedLeather;
   public final ItemGeneral.SubItem itemBoltStake;
   public final ItemGeneral.SubItem itemBoltAntiMagic;
   public final ItemGeneral.SubItem itemBoltHoly;
   public final ItemGeneral.SubItem itemBoltSplitting;
   public final ItemGeneral.SubItem itemBrewSoulHunger;
   public final ItemGeneral.SubItem itemBrewSoulAnguish;
   public final ItemGeneral.SubItem itemBrewSoulFear;
   public final ItemGeneral.SubItem itemBrewSoulTorment;
   public final ItemGeneral.SubItem itemContractOwnership;
   public final ItemGeneral.SubItem itemContractTorment;
   public final ItemGeneral.SubItem itemContractBlaze;
   public final ItemGeneral.SubItem itemContractResistFire;
   public final ItemGeneral.SubItem itemContractEvaporate;
   public final ItemGeneral.SubItem itemContractFieryTouch;
   public final ItemGeneral.SubItem itemContractSmelting;
   public final ItemGeneral.SubItem itemWaystonePlayerBound;
   public final ItemGeneral.SubItem itemKobolditeDust;
   public final ItemGeneral.SubItem itemKobolditeNugget;
   public final ItemGeneral.SubItem itemKobolditeIngot;
   public final ItemGeneral.SubItem itemKobolditePentacle;
   public final ItemGeneral.SubItem itemDoorIce;
   public final ItemGeneral.SubItem itemAnnointingPaste;
   public final ItemGeneral.SubItem itemSubduedSpiritVillage;
   public final ItemGeneral.SubItem itemBoltSilver;
   public final ItemGeneral.SubItem itemWolfsbane;
   public final ItemGeneral.SubItem itemSilverDust;
   public final ItemGeneral.SubItem itemMuttonRaw;
   public final ItemGeneral.SubItem itemMuttonCooked;
   public final ItemGeneral.SubItem itemVampireBookPage;
   public final ItemGeneral.SubItem itemDarkCloth;
   public final ItemGeneral.SubItem itemWoodenStake;
   public final ItemGeneral.SubItem itemBloodWarm;
   public final ItemGeneral.SubItem itemBloodLiliths;
   public final ItemGeneral.SubItem itemHeartOfGold;
   @SideOnly(Side.CLIENT)
   private IIcon overlayGenericIcon;
   @SideOnly(Side.CLIENT)
   private IIcon overlayBroomIcon;
   @SideOnly(Side.CLIENT)
   private IIcon overlaySolidifierIcon;
   @SideOnly(Side.CLIENT)
   private IIcon overlayInfusedBrewIcon;

   public ItemGeneral() {
      this.itemCandelabra = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(0, "candelabra"), this.subItems);
      this.itemChaliceEmpty = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(1, "chalice"), this.subItems);
      this.itemChaliceFull = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(2, "chaliceFull"), this.subItems);
      this.itemDreamMove = ItemGeneral.DreamWeave.register(new ItemGeneral.DreamWeave(3, 0, "weaveMoveFast", Potion.field_76424_c, Potion.field_76421_d, 7200, 0, 17, 10), this.subItems, this.weaves);
      this.itemDreamDig = ItemGeneral.DreamWeave.register(new ItemGeneral.DreamWeave(4, 1, "weaveDigFast", Potion.field_76422_e, Potion.field_76419_f, 7200, 0, 17, 4), this.subItems, this.weaves);
      this.itemDreamEat = ItemGeneral.DreamWeave.register(new ItemGeneral.DreamWeave(5, 2, "weaveSaturation", Potion.field_76443_y, Potion.field_76438_s, 4800, 0, 17, 16), this.subItems, this.weaves);
      this.itemDreamNightmare = ItemGeneral.DreamWeave.register(new ItemGeneral.DreamWeave(6, 3, "weaveNightmares", Potion.field_76437_t, Potion.field_76440_q, 1200, 0, 4, 4), this.subItems, this.weaves);
      this.itemBoneNeedle = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(7, "boneNeedle"), this.subItems);
      this.itemBroom = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(8, "broom"), this.subItems);
      this.itemBroomEnchanted = ItemGeneral.SubItem.register((new ItemGeneral.SubItem(9, "broomEnchanted", 3)).setEnchanted(true), this.subItems);
      this.itemAttunedStone = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(10, "attunedStone"), this.subItems);
      this.itemAttunedStoneCharged = ItemGeneral.SubItem.register((new ItemGeneral.SubItem(11, "attunedStoneCharged")).setEnchanted(true), this.subItems);
      this.itemWaystone = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(12, "waystone"), this.subItems);
      this.itemWaystoneBound = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(13, "waystoneBound", 1, false), this.subItems);
      this.itemMutandis = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(14, "mutandis"), this.subItems);
      this.itemMutandisExtremis = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(15, "mutandisExtremis"), this.subItems);
      this.itemQuicklime = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(16, "quicklime"), this.subItems);
      this.itemGypsum = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(17, "gypsum"), this.subItems);
      this.itemAshWood = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(18, "ashWood"), this.subItems);
      this.itemBelladonnaFlower = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(21, "belladonna"), this.subItems);
      this.itemMandrakeRoot = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(22, "mandrakeRoot"), this.subItems);
      this.itemDemonHeart = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(23, "demonHeart", 2, EnumAction.eat, new PotionEffect[]{new PotionEffect(Potion.field_76434_w.field_76415_H, 2400, 4), new PotionEffect(Potion.field_76428_l.field_76415_H, 2400, 1), new PotionEffect(Potion.field_76420_g.field_76415_H, 2400, 2), new PotionEffect(Potion.field_76424_c.field_76415_H, 2400, 2), new PotionEffect(Potion.field_76426_n.field_76415_H, 2400, 2), new PotionEffect(Potion.field_76431_k.field_76415_H, 2400), new PotionEffect(Potion.field_76438_s.field_76415_H, 3600, 1)}), this.subItems);
      this.itemBatWool = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(24, "batWool"), this.subItems);
      this.itemDogTongue = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(25, "dogTongue"), this.subItems);
      this.itemSoftClayJar = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(26, "clayJarSoft"), this.subItems);
      this.itemEmptyClayJar = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(27, "clayJar"), this.subItems);
      this.itemFoulFume = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(28, "foulFume"), this.subItems);
      this.itemDiamondVapour = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(29, "diamondVapour"), this.subItems);
      this.itemOilOfVitriol = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(30, "oilOfVitriol"), this.subItems);
      this.itemExhaleOfTheHornedOne = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(31, "exhaleOfTheHornedOne"), this.subItems);
      this.itemBreathOfTheGoddess = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(32, "breathOfTheGoddess"), this.subItems);
      this.itemHintOfRebirth = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(33, "hintOfRebirth"), this.subItems);
      this.itemWhiffOfMagic = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(34, "whiffOfMagic"), this.subItems);
      this.itemReekOfMisfortune = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(35, "reekOfMisfortune"), this.subItems);
      this.itemOdourOfPurity = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(36, "odourOfPurity"), this.subItems);
      this.itemTearOfTheGoddess = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(37, "tearOfTheGoddess"), this.subItems);
      this.itemRefinedEvil = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(38, "refinedEvil"), this.subItems);
      this.itemDropOfLuck = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(39, "dropOfLuck"), this.subItems);
      this.itemRedstoneSoup = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(40, "redstoneSoup", 1, new PotionEffect[]{new PotionEffect(Potion.field_76434_w.field_76415_H, 2400, 1)}), this.subItems);
      this.itemFlyingOintment = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(41, "flyingOintment", 2, new PotionEffect[]{new PotionEffect(Potion.field_76436_u.field_76415_H, 1200, 2)}), this.subItems);
      this.itemGhostOfTheLight = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(42, "ghostOfTheLight", 2, new PotionEffect[]{new PotionEffect(Potion.field_76436_u.field_76415_H, 1200, 1)}), this.subItems);
      this.itemSoulOfTheWorld = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(43, "soulOfTheWorld", 2, new PotionEffect[]{new PotionEffect(Potion.field_76436_u.field_76415_H, 1200, 1)}), this.subItems);
      this.itemSpiritOfOtherwhere = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(44, "spiritOfOtherwhere", 2, new PotionEffect[]{new PotionEffect(Potion.field_76436_u.field_76415_H, 1200, 1)}), this.subItems);
      this.itemInfernalAnimus = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(45, "infernalAnimus", 2, new PotionEffect[]{new PotionEffect(Potion.field_76436_u.field_76415_H, 1200, 1), new PotionEffect(Potion.field_82731_v.field_76415_H, 3600, 2)}), this.subItems);
      this.itemBookOven = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(46, "bookOven"), this.subItems);
      this.itemBookDistilling = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(47, "bookDistilling"), this.subItems);
      this.itemBookCircleMagic = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(48, "bookCircleMagic"), this.subItems);
      this.itemBookInfusions = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(49, "bookInfusions"), this.subItems);
      this.itemOddPorkRaw = ItemGeneral.SubItem.register(new ItemGeneral.Edible(50, "oddPorkchopRaw", 3, 0.3F, true), this.subItems);
      this.itemOddPorkCooked = ItemGeneral.SubItem.register(new ItemGeneral.Edible(51, "oddPorkchopCooked", 8, 0.8F, true), this.subItems);
      this.itemDoorRowan = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(52, "doorRowan"), this.subItems);
      this.itemDoorAlder = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(53, "doorAlder"), this.subItems);
      this.itemDoorKey = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(54, "doorKey"), this.subItems);
      this.itemRock = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(55, "rock"), this.subItems);
      this.itemWeb = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(56, "web"), this.subItems);
      this.itemBrewOfVines = ItemGeneral.SubItem.register(new ItemGeneral.Brew(57, "brewVines"), this.subItems);
      this.itemBrewOfWebs = ItemGeneral.SubItem.register(new ItemGeneral.Brew(58, "brewWeb"), this.subItems);
      this.itemBrewOfThorns = ItemGeneral.SubItem.register(new ItemGeneral.Brew(59, "brewThorns"), this.subItems);
      this.itemBrewOfInk = ItemGeneral.SubItem.register(new ItemGeneral.Brew(60, "brewInk"), this.subItems);
      this.itemBrewOfSprouting = ItemGeneral.SubItem.register(new ItemGeneral.Brew(61, "brewSprouting"), this.subItems);
      this.itemBrewOfErosion = ItemGeneral.SubItem.register(new ItemGeneral.Brew(62, "brewErosion"), this.subItems);
      this.itemRowanBerries = ItemGeneral.SubItem.register(new ItemGeneral.Edible(63, "berriesRowan", 1, 6.0F, false), this.subItems);
      this.itemNecroStone = ItemGeneral.SubItem.register((new ItemGeneral.SubItem(64, "necroStone", 1)).setEnchanted(true), this.subItems);
      this.itemBrewOfRaising = ItemGeneral.SubItem.register(new ItemGeneral.Brew(65, "brewRaising"), this.subItems);
      this.itemSpectralDust = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(66, "spectralDust"), this.subItems);
      this.itemEnderDew = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(67, "enderDew"), this.subItems);
      this.itemArtichoke = ItemGeneral.SubItem.register(new ItemGeneral.Edible(69, "artichoke", 20, 0.0F, false), this.subItems);
      this.itemSeedsTreefyd = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(70, "seedsTreefyd"), this.subItems);
      this.itemBrewGrotesque = ItemGeneral.SubItem.register((new ItemGeneral.Drinkable(71, "brewGrotesque", 1, new PotionEffect[0])).setPotion(true), this.subItems);
      this.itemImpregnatedLeather = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(72, "impregnatedLeather"), this.subItems);
      this.itemFumeFilter = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(73, "fumeFilter"), this.subItems);
      this.itemCreeperHeart = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(74, "creeperHeart", 1, EnumAction.eat, new PotionEffect[]{new PotionEffect(Potion.field_76426_n.field_76415_H, 20, 0)}), this.subItems);
      this.itemBrewOfLove = ItemGeneral.SubItem.register(new ItemGeneral.Brew(75, "brewLove"), this.subItems);
      this.itemBrewOfIce = ItemGeneral.SubItem.register(new ItemGeneral.Brew(76, "brewIce"), this.subItems);
      this.itemBrewOfTheDepths = ItemGeneral.SubItem.register((new ItemGeneral.Drinkable(77, "brewDepths", 1, new PotionEffect[0])).setPotion(true), this.subItems);
      this.itemIcyNeedle = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(78, "icyNeedle"), this.subItems);
      this.itemFrozenHeart = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(79, "frozenHeart", 1, EnumAction.eat, new PotionEffect[]{new PotionEffect(Potion.field_76426_n.field_76415_H, 20, 0)}), this.subItems);
      this.itemInfernalBlood = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(80, "infernalBlood"), this.subItems);
      this.itemBookHerbology = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(81, "bookHerbology"), this.subItems);
      this.itemBranchEnt = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(82, "entbranch"), this.subItems);
      this.itemMysticUnguent = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(83, "mysticunguent", 2, new PotionEffect[]{new PotionEffect(Potion.field_76437_t.field_76415_H, 1200, 1)}), this.subItems);
      this.itemDoorKeyring = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(84, "doorKeyring"), this.subItems);
      this.itemBrewOfFrogsTongue = ItemGeneral.SubItem.register(new ItemGeneral.Brew(85, "brewFrogsTongue"), this.subItems);
      this.itemBrewOfCursedLeaping = ItemGeneral.SubItem.register(new ItemGeneral.Brew(86, "brewCursedLeaping"), this.subItems);
      this.itemBrewOfHitchcock = ItemGeneral.SubItem.register(new ItemGeneral.Brew(87, "brewHitchcock"), this.subItems);
      this.itemBrewOfInfection = ItemGeneral.SubItem.register(new ItemGeneral.Brew(88, "brewInfection"), this.subItems);
      this.itemOwletsWing = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(89, "owletsWing"), this.subItems);
      this.itemToeOfFrog = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(90, "toeOfFrog"), this.subItems);
      this.itemWormyApple = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(91, "appleWormy", 0, EnumAction.eat, new PotionEffect[]{new PotionEffect(Potion.field_76436_u.field_76415_H, 60)}), this.subItems);
      this.itemQuartzSphere = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(92, "quartzSphere"), this.subItems);
      this.itemHappenstanceOil = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(93, "happenstanceOil", 1, new PotionEffect[]{new PotionEffect(Potion.field_76439_r.field_76415_H, 1200)}), this.subItems);
      this.itemSeerStone = ItemGeneral.SubItem.register((new ItemGeneral.SubItem(94, "seerStone", 1)).setEnchanted(true), this.subItems);
      this.itemBrewOfSleeping = ItemGeneral.SubItem.register((new ItemGeneral.Drinkable(95, "brewSleep", 1, new PotionEffect[0])).setPotion(true), this.subItems);
      this.itemBrewOfFlowingSpirit = ItemGeneral.SubItem.register(new BrewFluid(96, "brewFlowingSpirit", Witchery.Fluids.FLOWING_SPIRIT), this.subItems);
      this.itemBrewOfWasting = ItemGeneral.SubItem.register(new ItemGeneral.Brew(97, "brewWasting"), this.subItems);
      this.itemSleepingApple = ItemGeneral.SubItem.register(new ItemGeneral.Edible(98, "sleepingApple", 3, 3.0F, false, true), this.subItems);
      this.itemDisturbedCotton = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(99, "disturbedCotton"), this.subItems);
      this.itemFancifulThread = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(100, "fancifulThread"), this.subItems);
      this.itemTormentedTwine = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(101, "tormentedTwine"), this.subItems);
      this.itemGoldenThread = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(102, "goldenThread"), this.subItems);
      this.itemMellifluousHunger = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(103, "mellifluousHunger"), this.subItems);
      this.itemDreamIntensity = ItemGeneral.DreamWeave.register(new ItemGeneral.DreamWeave(104, 4, "weaveIntensity", Potion.field_76439_r, Potion.field_76440_q, 300, 0, 17, 22), this.subItems, this.weaves);
      this.itemPurifiedMilk = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(105, "purifiedMilk", 1, new PotionEffect[0]), this.subItems);
      this.itemBookBiomes = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(106, "bookBiomes"), this.subItems);
      this.itemBookWands = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(107, "bookWands"), this.subItems);
      this.itemBatBall = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(108, "batBall"), this.subItems);
      this.itemBrewOfBats = ItemGeneral.SubItem.register(new ItemGeneral.Brew(109, "brewBats"), this.subItems);
      this.itemCharmOfDisruptedDreams = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(110, "charmDisruptedDreams"), this.subItems);
      this.itemWormwood = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(111, "wormwood"), this.subItems);
      this.itemSubduedSpirit = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(112, "subduedSpirit"), this.subItems);
      this.itemFocusedWill = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(113, "focusedWill"), this.subItems);
      this.itemCondensedFear = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(114, "condensedFear"), this.subItems);
      this.itemBrewOfHollowTears = ItemGeneral.SubItem.register(new BrewFluid(115, "brewHollowTears", Witchery.Fluids.HOLLOW_TEARS), this.subItems);
      this.itemBrewOfSolidRock = ItemGeneral.SubItem.register(new BrewSolidifySpirit(116, "brewSolidStone", Blocks.field_150348_b), this.subItems);
      this.itemBrewOfSolidDirt = ItemGeneral.SubItem.register(new BrewSolidifySpirit(117, "brewSolidDirt", Blocks.field_150346_d), this.subItems);
      this.itemBrewOfSolidSand = ItemGeneral.SubItem.register(new BrewSolidifySpirit(118, "brewSolidSand", Blocks.field_150354_m), this.subItems);
      this.itemBrewOfSolidSandstone = ItemGeneral.SubItem.register(new BrewSolidifySpirit(119, "brewSolidSandstone", Blocks.field_150322_A), this.subItems);
      this.itemBrewOfSolidErosion = ItemGeneral.SubItem.register(new BrewSolidifySpirit(120, "brewSolidErosion", (Block)null), this.subItems);
      this.itemInfusionBase = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(121, "infusionBase", 2, new PotionEffect[]{new PotionEffect(Potion.field_82731_v.field_76415_H, 200, 3)}), this.subItems);
      this.itemBrewOfSoaring = ItemGeneral.SubItem.register(new InfusedBrew(122, "brewSoaring", InfusedBrewEffect.Soaring), this.subItems);
      this.itemBrewGrave = ItemGeneral.SubItem.register(new InfusedBrew(123, "brewGrave", InfusedBrewEffect.Grave), this.subItems);
      this.itemBrewRevealing = ItemGeneral.SubItem.register(new ItemGeneral.Brew(124, "brewRevealing") {
         public ItemGeneral.Brew.BrewResult onImpact(World world, EntityLivingBase thrower, MovingObjectPosition mop, boolean enhanced, double brewX, double brewY, double brewZ, AxisAlignedBB brewBounds) {
            double RADIUS = enhanced ? 8.0D : 5.0D;
            double RADIUS_SQ = RADIUS * RADIUS;
            AxisAlignedBB areaOfEffect = brewBounds.func_72314_b(RADIUS, RADIUS, RADIUS);
            List entities = world.func_72872_a(EntityLivingBase.class, areaOfEffect);
            if (entities != null && !entities.isEmpty()) {
               Iterator entityIterator = entities.iterator();

               while(entityIterator.hasNext()) {
                  EntityLivingBase entityLiving = (EntityLivingBase)entityIterator.next();
                  double distanceSq = entityLiving.func_70092_e(brewX, brewY, brewZ);
                  if (distanceSq <= RADIUS_SQ) {
                     double var10000 = 1.0D - Math.sqrt(distanceSq) / RADIUS;
                     if (entityLiving == mop.field_72308_g) {
                        double var22 = 1.0D;
                     }

                     if (entityLiving.func_70644_a(Potion.field_76441_p)) {
                        entityLiving.func_82170_o(Potion.field_76441_p.field_76415_H);
                     }

                     if (entityLiving instanceof EntityPlayerMP && entityLiving.func_82150_aj()) {
                        entityLiving.func_82142_c(false);
                     }

                     if (entityLiving instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer)entityLiving;
                        ExtendedPlayer playerEx = ExtendedPlayer.get(player);
                        if (playerEx != null && playerEx.getCreatureType() == TransformCreature.PLAYER) {
                           ParticleEffect.SMOKE.send(SoundEffect.WITCHERY_RANDOM_POOF, player, 0.5D, 2.0D, 16);
                           Shapeshift.INSTANCE.shiftTo(player, TransformCreature.NONE);
                        }
                     }

                     if (entityLiving instanceof EntitySummonedUndead && ((EntitySummonedUndead)entityLiving).isObscured()) {
                        ((EntitySummonedUndead)entityLiving).setObscured(false);
                     }
                  }
               }
            }

            return ItemGeneral.Brew.BrewResult.SHOW_EFFECT;
         }
      }, this.subItems);
      this.itemBrewSubstitution = ItemGeneral.SubItem.register(new ItemGeneral.Brew(125, "brewSubstitution") {
         public ItemGeneral.Brew.BrewResult onImpact(World world, EntityLivingBase thrower, final MovingObjectPosition mop, boolean enhanced, double brewX, double brewY, double brewZ, AxisAlignedBB brewBounds) {
            if (mop != null && mop.field_72313_a != MovingObjectType.ENTITY) {
               int RADIUS = enhanced ? 6 : 4;
               final double RADIUS_SQ = (double)(RADIUS * RADIUS);
               AxisAlignedBB areaOfEffect = brewBounds.func_72314_b((double)RADIUS, (double)RADIUS, (double)RADIUS);
               List entities = world.func_72872_a(EntityItem.class, areaOfEffect);
               if (entities != null && !entities.isEmpty()) {
                  final ArrayList<EntityItem> items = new ArrayList();
                  Iterator entityIterator = entities.iterator();

                  while(entityIterator.hasNext()) {
                     EntityItem item = (EntityItem)entityIterator.next();
                     double distanceSq = item.func_70092_e(brewX, brewY, brewZ);
                     if (distanceSq <= RADIUS_SQ) {
                        ItemStack stack = item.func_92059_d();
                        if (stack.func_77973_b() instanceof ItemBlock) {
                           items.add(item);
                        }
                     }
                  }

                  final Block refBlock = BlockUtil.getBlock(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d);
                  final int refMeta = BlockUtil.getBlockMetadata(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d);
                  if (items.size() > 0 && refBlock != null && BlockProtect.canBreak(refBlock, world)) {
                     (new EffectSpiral(new ISpiralBlockAction() {
                        int stackIndex = 0;
                        int subCount = 0;

                        public void onSpiralActionStart(World world, int posX, int posY, int posZ) {
                        }

                        public boolean onSpiralBlockAction(World world, int posX, int posY, int posZ) {
                           if (Coord.distanceSq((double)mop.field_72311_b, (double)mop.field_72312_c, (double)mop.field_72309_d, (double)posX, (double)posY, (double)posZ) < RADIUS_SQ) {
                              boolean found = false;
                              if (BlockUtil.getBlock(world, posX, posY, posZ) == refBlock && BlockUtil.isReplaceableBlock(world, posX, posY + 1, posZ)) {
                                 found = true;
                              } else if (BlockUtil.getBlock(world, posX, posY + 1, posZ) == refBlock && BlockUtil.isReplaceableBlock(world, posX, posY + 2, posZ)) {
                                 ++posY;
                                 found = true;
                              } else if (BlockUtil.getBlock(world, posX, posY - 1, posZ) == refBlock && BlockUtil.isReplaceableBlock(world, posX, posY, posZ)) {
                                 --posY;
                                 found = true;
                              } else if (BlockUtil.getBlock(world, posX, posY + 2, posZ) == refBlock && BlockUtil.isReplaceableBlock(world, posX, posY + 3, posZ)) {
                                 posY += 2;
                                 found = true;
                              } else if (BlockUtil.getBlock(world, posX, posY - 2, posZ) == refBlock && BlockUtil.isReplaceableBlock(world, posX, posY - 1, posZ)) {
                                 posY -= 2;
                                 found = true;
                              }

                              if (found) {
                                 ++this.subCount;
                                 ItemStack stack = ((EntityItem)items.get(this.stackIndex)).func_92059_d();
                                 BlockUtil.setBlock(world, posX, posY, posZ, (ItemBlock)((ItemBlock)stack.func_77973_b()), stack.func_77960_j(), 3);
                                 ParticleEffect.INSTANT_SPELL.send(SoundEffect.NONE, world, (double)posX, (double)posY, (double)posZ, 1.0D, 1.0D, 16);
                                 if (--stack.field_77994_a == 0) {
                                    ((EntityItem)items.get(this.stackIndex)).func_70106_y();
                                    ++this.stackIndex;
                                 }
                              }
                           }

                           return this.stackIndex < items.size();
                        }

                        public void onSpiralActionStop(World world, int posX, int posY, int posZ) {
                           while(this.subCount > 0) {
                              int quantity = this.subCount > 64 ? 64 : this.subCount;
                              this.subCount -= quantity;
                              world.func_72838_d(new EntityItem(world, 0.5D + (double)posX, 1.5D + (double)posY, 0.5D + (double)posZ, new ItemStack(refBlock, quantity, refMeta)));
                           }

                        }
                     })).apply(world, mop.field_72311_b, mop.field_72312_c, mop.field_72309_d, RADIUS * 2, RADIUS * 2);
                     return ItemGeneral.Brew.BrewResult.SHOW_EFFECT;
                  }
               }

               return ItemGeneral.Brew.BrewResult.DROP_ITEM;
            } else {
               return ItemGeneral.Brew.BrewResult.DROP_ITEM;
            }
         }
      }, this.subItems);
      this.itemCongealedSpirit = ItemGeneral.SubItem.register((new ItemGeneral.Drinkable(126, "brewCongealedSpirit", 1, new PotionEffect[]{new PotionEffect(Potion.field_76439_r.field_76415_H, TimeUtil.secsToTicks(30), 1)})).setPotion(true), this.subItems);
      this.itemBookBurning = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(127, "bookBurning"), this.subItems);
      this.itemGraveyardDust = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(128, "graveyardDust"), this.subItems);
      this.itemBinkyHead = ItemGeneral.SubItem.register(new ItemGeneral.BoltType(129, "binkyhead"), this.subItems);
      this.itemNullCatalyst = ItemGeneral.SubItem.register(new ItemGeneral.BoltType(130, "nullcatalyst"), this.subItems);
      this.itemNullifiedLeather = ItemGeneral.SubItem.register(new ItemGeneral.BoltType(131, "nullifiedleather"), this.subItems);
      this.itemBoltStake = ItemGeneral.SubItem.register(new ItemGeneral.BoltType(132, "boltStake"), this.subItems);
      this.itemBoltAntiMagic = ItemGeneral.SubItem.register(new ItemGeneral.BoltType(133, "boltAntiMagic"), this.subItems);
      this.itemBoltHoly = ItemGeneral.SubItem.register(new ItemGeneral.BoltType(134, "boltHoly"), this.subItems);
      this.itemBoltSplitting = ItemGeneral.SubItem.register(new ItemGeneral.BoltType(135, "boltSplitting"), this.subItems);
      this.itemBrewSoulHunger = ItemGeneral.SubItem.register(new BrewSoul(136, "brewSoulHunger", EffectRegistry.CarnosaDiem), this.subItems);
      this.itemBrewSoulAnguish = ItemGeneral.SubItem.register(new BrewSoul(137, "brewSoulAnguish", EffectRegistry.Ignianima), this.subItems);
      this.itemBrewSoulFear = ItemGeneral.SubItem.register(new BrewSoul(138, "brewSoulFear", EffectRegistry.MORSMORDRE), this.subItems);
      this.itemBrewSoulTorment = ItemGeneral.SubItem.register(new BrewSoul(139, "brewSoulTorment", EffectRegistry.Tormentum), this.subItems);
      this.itemContractOwnership = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(140, "contract"), this.subItems);
      this.itemContractTorment = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(141, "contractTorment"), this.subItems);
      this.itemContractBlaze = ItemGeneral.SubItem.register(new ItemGeneralContract(142, "contractBlaze") {
         public boolean activate(ItemStack stack, EntityLivingBase targetEntity) {
            EntityCreature blaze = InfusionInfernal.spawnCreature(targetEntity.field_70170_p, EntityBlaze.class, targetEntity, 1, 2, ParticleEffect.FLAME, SoundEffect.RANDOM_FIZZ);
            if (blaze != null) {
               blaze.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(50.0D);
               blaze.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(7.0D);
               return true;
            } else {
               return false;
            }
         }
      }, this.subItems);
      this.itemContractResistFire = ItemGeneral.SubItem.register(new ItemGeneralContract(143, "contractResistFire") {
         public boolean activate(ItemStack stack, EntityLivingBase targetEntity) {
            targetEntity.func_70690_d(new PotionEffect(Potion.field_76426_n.field_76415_H, TimeUtil.minsToTicks(15)));
            return true;
         }
      }, this.subItems);
      this.itemContractEvaporate = ItemGeneral.SubItem.register(new ItemGeneralContract(144, "contractEvaporate") {
         public boolean activate(ItemStack stack, EntityLivingBase targetEntity) {
            if (targetEntity instanceof EntityPlayer) {
               PlayerEffects.IMP_EVAPORATION.applyTo((EntityPlayer)targetEntity, TimeUtil.minsToTicks(10));
               return true;
            } else {
               return false;
            }
         }
      }, this.subItems);
      this.itemContractFieryTouch = ItemGeneral.SubItem.register(new ItemGeneralContract(145, "contractFieryTouch") {
         public boolean activate(ItemStack stack, EntityLivingBase targetEntity) {
            if (targetEntity instanceof EntityPlayer) {
               PlayerEffects.IMP_FIRE_TOUCH.applyTo((EntityPlayer)targetEntity, TimeUtil.minsToTicks(10));
               return true;
            } else {
               return false;
            }
         }
      }, this.subItems);
      this.itemContractSmelting = ItemGeneral.SubItem.register(new ItemGeneralContract(146, "contractSmelting") {
         public boolean activate(ItemStack stack, EntityLivingBase targetEntity) {
            if (targetEntity instanceof EntityPlayer) {
               PlayerEffects.IMP_METLING_TOUCH.applyTo((EntityPlayer)targetEntity, TimeUtil.minsToTicks(10));
               return true;
            } else {
               return false;
            }
         }
      }, this.subItems);
      this.itemWaystonePlayerBound = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(147, "waystoneCreatureBound", 1, false), this.subItems);
      this.itemKobolditeDust = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(148, "kobolditedust"), this.subItems);
      this.itemKobolditeNugget = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(149, "kobolditenugget"), this.subItems);
      this.itemKobolditeIngot = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(150, "kobolditeingot"), this.subItems);
      this.itemKobolditePentacle = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(151, "pentacle"), this.subItems);
      this.itemDoorIce = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(152, "doorIce"), this.subItems);
      this.itemAnnointingPaste = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(153, "annointingPaste"), this.subItems);
      this.itemSubduedSpiritVillage = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(154, "subduedSpiritVillage"), this.subItems);
      this.itemBoltSilver = ItemGeneral.SubItem.register(new ItemGeneral.BoltType(155, "boltSilver"), this.subItems);
      this.itemWolfsbane = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(156, "wolfsbane"), this.subItems);
      this.itemSilverDust = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(157, "silverdust"), this.subItems);
      this.itemMuttonRaw = ItemGeneral.SubItem.register(new ItemGeneral.Edible(158, "muttonraw", 2, 0.2F, true), this.subItems);
      this.itemMuttonCooked = ItemGeneral.SubItem.register(new ItemGeneral.Edible(159, "muttoncooked", 6, 0.8F, true), this.subItems);
      this.itemVampireBookPage = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(160, "vbookPage"), this.subItems);
      this.itemDarkCloth = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(161, "darkCloth"), this.subItems);
      this.itemWoodenStake = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(162, "stake"), this.subItems);
      this.itemBloodWarm = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(163, "warmBlood", 1, new PotionEffect[0]) {
         public void onDrunk(World world, EntityPlayer player, ItemStack itemstack) {
            if (!world.field_72995_K) {
               ExtendedPlayer playerEx = ExtendedPlayer.get(player);
               if (playerEx.isVampire()) {
                  playerEx.increaseBloodPower(500);
               } else {
                  player.func_70690_d(new PotionEffect(Potion.field_76438_s.field_76415_H, TimeUtil.secsToTicks(6)));
               }
            }

         }
      }, this.subItems);
      this.itemBloodLiliths = ItemGeneral.SubItem.register(new ItemGeneral.Drinkable(164, "lilithsBlood", 2, new PotionEffect[0]) {
         public void onDrunk(World world, EntityPlayer player, ItemStack itemstack) {
            if (!world.field_72995_K) {
               ExtendedPlayer playerEx = ExtendedPlayer.get(player);
               int level = playerEx.getVampireLevel();
               if (level == 10) {
                  playerEx.increaseBloodPower(2000);
               } else {
                  playerEx.increaseVampireLevel();
               }
            }

         }
      }, this.subItems);
      this.itemHeartOfGold = ItemGeneral.SubItem.register(new ItemGeneral.SubItem(165, "heartofgold"), this.subItems);
      this.func_77656_e(0);
      this.func_77625_d(64);
      this.func_77627_a(true);
   }

   public boolean isBrew(int itemDamage) {
      return this.subItems.get(itemDamage) instanceof ItemGeneral.Brew;
   }

   public boolean isBrew(ItemStack stack) {
      return stack != null && stack.func_77973_b() == this && this.isBrew(stack.func_77960_j());
   }

   public String func_77667_c(ItemStack itemStack) {
      return this.func_77658_a() + "." + ((ItemGeneral.SubItem)this.subItems.get(itemStack.func_77960_j())).unlocalizedName;
   }

   @SideOnly(Side.CLIENT)
   public void func_94581_a(IIconRegister iconRegister) {
      String defaultIconName = this.func_111208_A();
      Iterator i$ = this.subItems.iterator();

      while(i$.hasNext()) {
         ItemGeneral.SubItem subItem = (ItemGeneral.SubItem)i$.next();
         if (subItem != null) {
            subItem.registerIcon(iconRegister, this);
         }
      }

      this.overlayGenericIcon = iconRegister.func_94245_a(defaultIconName + ".genericoverlay");
      this.overlayBroomIcon = iconRegister.func_94245_a(defaultIconName + ".broomOverlay");
      this.overlaySolidifierIcon = iconRegister.func_94245_a(defaultIconName + ".brewSolidOverlay");
      this.overlayInfusedBrewIcon = iconRegister.func_94245_a(defaultIconName + ".brewInfusedOverlay");
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_77617_a(int damage) {
      return ((ItemGeneral.SubItem)this.subItems.get(Math.max(damage, 0))).icon;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_77618_c(int damage, int pass) {
      if (pass == 0) {
         return super.func_77618_c(damage, pass);
      } else if (((ItemGeneral.SubItem)this.subItems.get(damage)).isSolidifier()) {
         return this.overlaySolidifierIcon;
      } else if (((ItemGeneral.SubItem)this.subItems.get(damage)).isInfused()) {
         return this.overlayInfusedBrewIcon;
      } else if (((ItemGeneral.SubItem)this.subItems.get(damage)).isPotion()) {
         return Items.field_151068_bn.func_77618_c(this.subItems.get(damage) instanceof ItemGeneral.Brew ? 16384 : 0, pass);
      } else {
         return this.itemBroomEnchanted.damageValue == damage ? this.overlayBroomIcon : this.overlayGenericIcon;
      }
   }

   @SideOnly(Side.CLIENT)
   public int func_82790_a(ItemStack stack, int pass) {
      if (this.itemBroomEnchanted.isMatch(stack) && pass != 0) {
         int j = this.getBroomItemColor(stack);
         if (j > 15) {
            return super.func_82790_a(stack, pass);
         } else {
            if (j < 0) {
               j = 12;
            }

            return ItemDye.field_150922_c[BlockColored.func_150031_c(j)];
         }
      } else {
         return super.func_82790_a(stack, pass);
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean func_77623_v() {
      return true;
   }

   public boolean hasEffect(ItemStack stack, int pass) {
      return pass == 0 && ((ItemGeneral.SubItem)this.subItems.get(stack.func_77960_j())).isEnchanted() || this.itemBroomEnchanted.isMatch(stack) || this.itemSubduedSpirit.isMatch(stack) || this.itemSubduedSpiritVillage.isMatch(stack);
   }

   @SideOnly(Side.CLIENT)
   public void func_150895_a(Item item, CreativeTabs tab, List itemList) {
      Iterator i$ = this.subItems.iterator();

      while(i$.hasNext()) {
         ItemGeneral.SubItem subItem = (ItemGeneral.SubItem)i$.next();
         if (subItem != null && subItem.showInCreativeTab) {
            itemList.add(subItem.createStack());
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public EnumRarity func_77613_e(ItemStack itemstack) {
      return EnumRarity.values()[((ItemGeneral.SubItem)this.subItems.get(itemstack.func_77960_j())).rarity];
   }

   @SideOnly(Side.CLIENT)
   public void func_77624_a(ItemStack stack, EntityPlayer player, List list, boolean includeHandlers) {
      String location = this.getBoundDisplayName(stack);
      if (location != null && !location.isEmpty()) {
         list.add(location);
      }

      String taglock = Witchery.Items.TAGLOCK_KIT.getBoundEntityDisplayName(stack, 1);
      if (!taglock.isEmpty()) {
         list.add(String.format(Witchery.resource("item.witcheryTaglockKit.boundto"), taglock));
      }

      NBTTagCompound nbtTag;
      int len$;
      int i$;
      if (this.itemDoorKey.isMatch(stack)) {
         nbtTag = stack.func_77978_p();
         if (nbtTag != null && nbtTag.func_74764_b("doorX") && nbtTag.func_74764_b("doorY") && nbtTag.func_74764_b("doorZ")) {
            int doorX = nbtTag.func_74762_e("doorX");
            len$ = nbtTag.func_74762_e("doorY");
            i$ = nbtTag.func_74762_e("doorZ");
            if (nbtTag.func_74764_b("doorD") && nbtTag.func_74764_b("doorDN")) {
               list.add(String.format("%s: %d, %d, %d", nbtTag.func_74779_i("doorDN"), doorX, len$, i$));
            } else {
               list.add(String.format("%d, %d, %d", doorX, len$, i$));
            }
         }
      } else if (this.itemDoorKeyring.isMatch(stack)) {
         nbtTag = stack.func_77978_p();
         if (nbtTag != null && nbtTag.func_74764_b("doorKeys")) {
            NBTTagList keyList = nbtTag.func_150295_c("doorKeys", 10);
            if (keyList != null) {
               for(len$ = 0; len$ < keyList.func_74745_c(); ++len$) {
                  NBTTagCompound keyTag = keyList.func_150305_b(len$);
                  if (keyTag != null && keyTag.func_74764_b("doorX") && keyTag.func_74764_b("doorY") && keyTag.func_74764_b("doorZ")) {
                     int doorX = keyTag.func_74762_e("doorX");
                     int doorY = keyTag.func_74762_e("doorY");
                     int doorZ = keyTag.func_74762_e("doorZ");
                     if (keyTag.func_74764_b("doorD") && keyTag.func_74764_b("doorDN")) {
                        list.add(String.format("%s: %d, %d, %d", keyTag.func_74779_i("doorDN"), doorX, doorY, doorZ));
                     } else {
                        list.add(String.format("%d, %d, %d", doorX, doorY, doorZ));
                     }
                  }
               }
            }
         }
      }

      if (stack.func_77960_j() == this.itemContractTorment.damageValue) {
         String localText = Witchery.resource("item.witchery:ingredient.contractTorment.tip");
         if (localText != null) {
            String[] arr$ = localText.split("\n");
            len$ = arr$.length;

            for(i$ = 0; i$ < len$; ++i$) {
               String s = arr$[i$];
               if (!s.isEmpty()) {
                  list.add(s);
               }
            }
         }
      }

   }

   public String getBoundDisplayName(ItemStack itemstack) {
      NBTTagCompound tag = itemstack.func_77978_p();
      return tag != null && tag.func_74764_b("PosX") && tag.func_74764_b("PosY") && tag.func_74764_b("PosZ") && tag.func_74764_b("NameD") ? String.format("%s: %d, %d, %d", tag.func_74779_i("NameD"), tag.func_74762_e("PosX"), tag.func_74762_e("PosY"), tag.func_74762_e("PosZ")) : "";
   }

   public void bindToLocation(World world, int posX, int posY, int posZ, int dimension, String dimensionName, ItemStack itemstack) {
      if (!itemstack.func_77942_o()) {
         itemstack.func_77982_d(new NBTTagCompound());
      }

      itemstack.func_77978_p().func_74768_a("PosX", posX);
      itemstack.func_77978_p().func_74768_a("PosY", posY);
      itemstack.func_77978_p().func_74768_a("PosZ", posZ);
      itemstack.func_77978_p().func_74768_a("PosD", dimension);
      itemstack.func_77978_p().func_74778_a("NameD", dimensionName);
   }

   public boolean hasLocationBinding(ItemStack itemstack) {
      if (!itemstack.func_77942_o()) {
         return false;
      } else {
         NBTTagCompound nbtTag = itemstack.func_77978_p();
         return nbtTag.func_74764_b("PosX") && nbtTag.func_74764_b("PosY") && nbtTag.func_74764_b("PosZ") && nbtTag.func_74764_b("PosD") && nbtTag.func_74764_b("NameD");
      }
   }

   public void copyLocationBinding(ItemStack from, ItemStack to) {
      if (this.hasLocationBinding(from)) {
         NBTTagCompound nbtTagFrom = from.func_77978_p();
         if (!to.func_77942_o()) {
            to.func_77982_d(new NBTTagCompound());
         }

         NBTTagCompound nbtTagTo = to.func_77978_p();
         nbtTagTo.func_74768_a("PosX", nbtTagFrom.func_74762_e("PosX"));
         nbtTagTo.func_74768_a("PosY", nbtTagFrom.func_74762_e("PosY"));
         nbtTagTo.func_74768_a("PosZ", nbtTagFrom.func_74762_e("PosZ"));
         nbtTagTo.func_74768_a("PosD", nbtTagFrom.func_74762_e("PosD"));
         nbtTagTo.func_74778_a("NameD", nbtTagFrom.func_74779_i("NameD"));
         if (from.func_82837_s()) {
            to.func_151001_c(from.func_82833_r());
         }
      }

   }

   public boolean copyLocationBinding(World world, ItemStack from, BlockCircle.TileEntityCircle.ActivatedRitual to) {
      if (!this.hasLocationBinding(from)) {
         return false;
      } else {
         NBTTagCompound nbtTagFrom = from.func_77978_p();
         if (nbtTagFrom.func_74762_e("PosD") != world.field_73011_w.field_76574_g) {
            return false;
         } else {
            Coord coord = new Coord(nbtTagFrom.func_74762_e("PosX"), nbtTagFrom.func_74762_e("PosY"), nbtTagFrom.func_74762_e("PosZ"));
            to.setLocation(coord);
            return true;
         }
      }
   }

   public ItemStack func_77654_b(ItemStack itemstack, World world, EntityPlayer player) {
      ItemGeneral.SubItem subItem = (ItemGeneral.SubItem)this.subItems.get(itemstack.func_77960_j());
      if (this.itemWaystoneBound.isMatch(itemstack)) {
         if (!world.field_72995_K && player instanceof EntityPlayerMP) {
            Witchery.packetPipeline.sendTo((IMessage)(new PacketCamPos(false, false, (Entity)null)), (EntityPlayerMP)((EntityPlayerMP)player));
         }

         return itemstack;
      } else {
         int len$;
         if (subItem instanceof ItemGeneral.Edible) {
            if (!player.field_71075_bZ.field_75098_d) {
               --itemstack.field_77994_a;
               if (itemstack.field_77994_a <= 0) {
                  itemstack.field_77994_a = 0;
                  player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
               }
            }

            ItemGeneral.Edible edible = (ItemGeneral.Edible)subItem;
            if (subItem == this.itemArtichoke) {
               int foodLevel = player.func_71024_bL().func_75116_a();
               player.func_71024_bL().func_75122_a(edible.healAmount, edible.saturationModifier);
               len$ = player.func_71024_bL().func_75116_a() - foodLevel;
               player.func_70690_d(new PotionEffect(Potion.field_76438_s.field_76415_H, 3 * len$ * 20, 2));
            } else if (subItem == this.itemSleepingApple) {
               player.func_71024_bL().func_75122_a(edible.healAmount, edible.saturationModifier);
               if (player.field_71093_bK == 0 && !world.field_72995_K && !WorldProviderDreamWorld.getPlayerIsGhost(player)) {
                  WorldProviderDreamWorld.sendPlayerToSpiritWorld(player, 1.0D);
                  itemstack.field_77994_a = 0;
                  world.func_72956_a(player, "random.burp", 0.5F, world.field_73012_v.nextFloat() * 0.1F + 0.9F);
                  return player.func_71045_bC() != null ? player.func_71045_bC() : itemstack;
               }
            } else {
               player.func_71024_bL().func_75122_a(edible.healAmount, edible.saturationModifier);
            }

            world.func_72956_a(player, "random.burp", 0.5F, world.field_73012_v.nextFloat() * 0.1F + 0.9F);
         } else if (subItem instanceof ItemGeneral.Drinkable) {
            if (this.itemDemonHeart.isMatch(itemstack) && player.func_70093_af()) {
               return itemstack;
            }

            if (!player.field_71075_bZ.field_75098_d) {
               --itemstack.field_77994_a;
               if (itemstack.field_77994_a <= 0) {
                  itemstack.field_77994_a = 0;
                  player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
               }
            }

            ItemGeneral.Drinkable drinkable = (ItemGeneral.Drinkable)subItem;
            PotionEffect[] arr$ = drinkable.effects;
            len$ = arr$.length;

            int itemIndex;
            PotionEffect effect;
            for(itemIndex = 0; itemIndex < len$; ++itemIndex) {
               effect = arr$[itemIndex];
               player.func_70690_d(new PotionEffect(effect));
            }

            if (this.itemDemonHeart.isMatch(itemstack)) {
               player.func_70015_d(2640);
            } else if (this.itemBrewGrotesque.isMatch(itemstack)) {
               if (!world.field_72995_K) {
                  Infusion.getNBT(player).func_74768_a("witcheryGrotesque", 1200);
                  Witchery.packetPipeline.sendToDimension(new PacketPlayerStyle(player), player.field_71093_bK);
               }
            } else if (this.itemBrewOfSleeping.isMatch(itemstack)) {
               if (player.field_71093_bK == 0 && !world.field_72995_K && !WorldProviderDreamWorld.getPlayerIsGhost(player)) {
                  WorldProviderDreamWorld.sendPlayerToSpiritWorld(player, 0.998D);
                  itemstack.field_77994_a = 0;
                  world.func_72956_a(player, "random.burp", 0.5F, world.field_73012_v.nextFloat() * 0.1F + 0.9F);
                  return player.func_71045_bC() != null ? player.func_71045_bC() : itemstack;
               }
            } else if (this.itemPurifiedMilk.isMatch(itemstack)) {
               if (!world.field_72995_K && world.field_73012_v.nextInt(2) == 0) {
                  Collection c = player.func_70651_bq();
                  if (c != null && !c.isEmpty()) {
                     Object[] objs = c.toArray();
                     itemIndex = world.field_73012_v.nextInt(c.size());
                     effect = (PotionEffect)objs[itemIndex];
                     player.func_82170_o(effect.func_76456_a());
                  }
               }
            } else if (this.itemBrewOfTheDepths.isMatch(itemstack)) {
               if (!world.field_72995_K) {
                  Infusion.getNBT(player).func_74768_a("witcheryDepths", 300);
               }
            } else if (this.itemCreeperHeart.isMatch(itemstack)) {
               if (!world.field_72995_K) {
                  if (Config.instance().allowExplodingCreeperHearts) {
                     world.func_72876_a(player, player.field_70165_t, player.field_70163_u, player.field_70161_v, 3.0F, true);
                  } else {
                     world.func_72876_a(player, player.field_70165_t, player.field_70163_u, player.field_70161_v, 1.0F, false);
                  }
               }
            } else if (this.itemFrozenHeart.isMatch(itemstack)) {
               if (!world.field_72995_K) {
                  PlayerEffects.onDeath(player);
               }
            } else {
               drinkable.onDrunk(world, player, itemstack);
            }

            world.func_72956_a(player, "random.burp", 0.5F, world.field_73012_v.nextFloat() * 0.1F + 0.9F);
         }

         return itemstack;
      }
   }

   public int func_77626_a(ItemStack itemstack) {
      ItemGeneral.SubItem subItem = (ItemGeneral.SubItem)this.subItems.get(itemstack.func_77960_j());
      if (!(subItem instanceof ItemGeneral.Edible) && !(subItem instanceof ItemGeneral.Drinkable)) {
         if (this.itemWaystoneBound.isMatch(itemstack)) {
            return 1200;
         } else if (this.itemContractTorment.isMatch(itemstack)) {
            return 1200;
         } else {
            return this.itemSeerStone.isMatch(itemstack) ? 1200 : super.func_77626_a(itemstack);
         }
      } else {
         return 32;
      }
   }

   public EnumAction func_77661_b(ItemStack itemstack) {
      ItemGeneral.SubItem subItem = (ItemGeneral.SubItem)this.subItems.get(itemstack.func_77960_j());
      if (subItem instanceof ItemGeneral.Edible) {
         return EnumAction.eat;
      } else if (subItem instanceof ItemGeneral.Drinkable) {
         return ((ItemGeneral.Drinkable)subItem).useAction;
      } else if (this.itemContractTorment.isMatch(itemstack)) {
         return EnumAction.bow;
      } else {
         return this.itemSeerStone.isMatch(itemstack) ? EnumAction.bow : super.func_77661_b(itemstack);
      }
   }

   public void onUsingTick(ItemStack stack, EntityPlayer player, int countdown) {
      World world = player.field_70170_p;
      int elapsedTicks = this.func_77626_a(stack) - countdown;
      if (!world.field_72995_K && player instanceof EntityPlayerMP) {
         if (this.itemWaystoneBound.isMatch(stack)) {
            if (elapsedTicks % 20 == 0) {
               if (elapsedTicks == 0) {
                  NBTTagCompound tag = stack.func_77978_p();
                  if (tag != null && tag.func_74764_b("PosX") && tag.func_74764_b("PosY") && tag.func_74764_b("PosZ") && tag.func_74764_b("PosD")) {
                     int newX = tag.func_74762_e("PosX");
                     int newY = tag.func_74762_e("PosY");
                     int newZ = tag.func_74762_e("PosZ");
                     int newD = tag.func_74762_e("PosD");
                     EntityEye eye = new EntityEye(world);
                     eye.func_70012_b((double)newX, (double)newY, (double)newZ, player.field_70177_z, 90.0F);
                     world.func_72838_d(eye);
                     Witchery.packetPipeline.sendTo((IMessage)(new PacketCamPos(true, elapsedTicks == 0, eye)), (EntityPlayerMP)((EntityPlayerMP)player));
                  }
               } else {
                  Witchery.packetPipeline.sendTo((IMessage)(new PacketCamPos(true, false, (Entity)null)), (EntityPlayerMP)((EntityPlayerMP)player));
               }
            }
         } else if (this.itemContractTorment.isMatch(stack)) {
            if (!world.field_72995_K) {
               if (elapsedTicks != 0 && elapsedTicks != 40) {
                  if (elapsedTicks != 80 && elapsedTicks != 120) {
                     if (elapsedTicks != 160 && elapsedTicks != 200 && elapsedTicks != 240) {
                        if (elapsedTicks == 280 && Infusion.aquireEnergy(world, player, 10, true)) {
                           if (this.circleNear(world, player)) {
                              ParticleEffect.MOB_SPELL.send(SoundEffect.NONE, player, 1.0D, 2.0D, 16);
                              ParticleEffect.FLAME.send(SoundEffect.NONE, player, 1.0D, 2.0D, 16);
                              ParticleEffect.FLAME.send(SoundEffect.NONE, player, 1.0D, 2.0D, 16);
                              player.func_71041_bz();
                              EntityLiving living = InfusionInfernal.spawnCreature(world, EntityLordOfTorment.class, (int)player.field_70165_t, (int)player.field_70163_u, (int)player.field_70161_v, (EntityLivingBase)null, 2, 4, ParticleEffect.FLAME, SoundEffect.MOB_ENDERDRAGON_GROWL);
                              if (living != null) {
                                 if (!player.field_71075_bZ.field_75098_d) {
                                    --stack.field_77994_a;
                                    if (stack.field_77994_a <= 0) {
                                       player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
                                    }
                                 }

                                 living.func_110163_bv();
                                 world.func_72885_a(living, living.field_70165_t, living.field_70163_u + (double)living.func_70047_e(), living.field_70161_v, 7.0F, false, world.func_82736_K().func_82766_b("mobGriefing"));
                              } else {
                                 SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
                              }
                           } else {
                              SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
                              player.func_71041_bz();
                           }
                        }
                     } else if (Infusion.aquireEnergy(world, player, 10, true)) {
                        ParticleEffect.MOB_SPELL.send(SoundEffect.MOB_BLAZE_DEATH, player, 1.0D, 2.0D, 16);
                        ParticleEffect.FLAME.send(SoundEffect.NONE, player, 1.0D, 2.0D, 16);
                     }
                  } else if (Infusion.aquireEnergy(world, player, 10, true)) {
                     ParticleEffect.MOB_SPELL.send(SoundEffect.MOB_BLAZE_DEATH, player, 1.0D, 2.0D, 16);
                  }
               } else if (Infusion.aquireEnergy(world, player, 10, true)) {
                  if (elapsedTicks <= 0 && !this.circleNear(world, player)) {
                     SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
                     ChatUtil.sendTranslated(EnumChatFormatting.RED, player, "item.witchery:ingredient.contractTorment.nostones");
                     player.func_71041_bz();
                  } else {
                     SoundEffect.MOB_BLAZE_DEATH.playAtPlayer(world, player);
                  }
               }
            }
         } else if (this.itemSeerStone.isMatch(stack)) {
            EntityCovenWitch.summonCoven(world, player, new Coord(player), elapsedTicks);
         }
      }

   }

   private boolean circleNear(World world, EntityPlayer player) {
      int midX = MathHelper.func_76128_c(player.field_70165_t);
      int midY = MathHelper.func_76128_c(player.field_70163_u);
      int midZ = MathHelper.func_76128_c(player.field_70161_v);
      int[][] PATTERN = new int[][]{{0, 0, 0, 0, 4, 3, 4, 0, 0, 0, 0}, {0, 0, 4, 3, 1, 1, 1, 3, 4, 0, 0}, {0, 4, 1, 1, 1, 1, 1, 1, 1, 4, 0}, {0, 3, 1, 1, 1, 1, 1, 1, 1, 3, 0}, {4, 1, 1, 1, 2, 2, 2, 1, 1, 1, 4}, {3, 1, 1, 1, 2, 1, 2, 1, 1, 1, 3}, {4, 1, 1, 1, 2, 2, 2, 1, 1, 1, 4}, {0, 3, 1, 1, 1, 1, 1, 1, 1, 3, 0}, {0, 4, 1, 1, 1, 1, 1, 1, 1, 4, 0}, {0, 0, 4, 3, 1, 1, 1, 3, 4, 0, 0}, {0, 0, 0, 0, 4, 3, 4, 0, 0, 0, 0}};
      int offsetZ = (PATTERN.length - 1) / 2;

      for(int z = 0; z < PATTERN.length - 1; ++z) {
         int worldZ = midZ - offsetZ + z;
         int offsetX = (PATTERN[z].length - 1) / 2;

         for(int x = 0; x < PATTERN[z].length; ++x) {
            int worldX = midX - offsetX + x;
            int value = PATTERN[PATTERN.length - 1 - z][x];
            if (value != 0 && !this.isPost(world, worldX, midY, worldZ, value == 2 || value == 4, value == 4, value == 3 || value == 4)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean isPost(World world, int x, int y, int z, boolean bottomSolid, boolean midSolid, boolean topSolid) {
      Block blockBelow = BlockUtil.getBlock(world, x, y - 1, z);
      Block blockBottom = BlockUtil.getBlock(world, x, y, z);
      Block blockMid = BlockUtil.getBlock(world, x, y + 1, z);
      Block blockTop = BlockUtil.getBlock(world, x, y + 2, z);
      Block blockAbove = BlockUtil.getBlock(world, x, y + 3, z);
      if (blockBelow != null && blockBelow.func_149688_o().func_76220_a()) {
         if (bottomSolid) {
            if (blockBottom == null || !blockBottom.func_149688_o().func_76220_a()) {
               return false;
            }
         } else if (blockBottom != null && blockBottom.func_149688_o().func_76220_a()) {
            return false;
         }

         if (midSolid) {
            if (blockMid == null || !blockMid.func_149688_o().func_76220_a()) {
               return false;
            }
         } else if (blockMid != null && blockMid.func_149688_o().func_76220_a()) {
            return false;
         }

         if (topSolid) {
            if (blockTop == null || !blockTop.func_149688_o().func_76220_a()) {
               return false;
            }
         } else if (blockTop != null && blockTop.func_149688_o().func_76220_a()) {
            return false;
         }

         return blockAbove == null || !blockAbove.func_149688_o().func_76220_a();
      } else {
         return false;
      }
   }

   public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
      Block block = BlockUtil.getBlock(world, x, y, z);
      if (this.itemWaystoneBound.isMatch(stack) && block == Witchery.Blocks.CRYSTAL_BALL) {
         if (!world.field_72995_K && BlockCrystalBall.tryConsumePower(world, player, x, y, z)) {
            NBTTagCompound tag = stack.func_77978_p();
            if (tag != null && tag.func_74764_b("PosX") && tag.func_74764_b("PosY") && tag.func_74764_b("PosZ") && tag.func_74764_b("PosD")) {
               int newX = tag.func_74762_e("PosX");
               int newY = tag.func_74762_e("PosY");
               int newZ = tag.func_74762_e("PosZ");
               int newD = tag.func_74762_e("PosD");
               double MAX_DISTANCE = 22500.0D;
               if (newD == player.field_71093_bK && player.func_70092_e((double)newX, (double)newY, (double)newZ) <= 22500.0D) {
                  player.func_71008_a(stack, this.func_77626_a(stack));
               } else {
                  SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
               }
            } else {
               SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
            }
         } else if (world.field_72995_K) {
            player.func_71008_a(stack, this.func_77626_a(stack));
         }

         return !world.field_72995_K;
      } else {
         return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
      }
   }

   public void func_77615_a(ItemStack stack, World world, EntityPlayer player, int countdown) {
      if (!world.field_72995_K && this.itemWaystoneBound.isMatch(stack) && player instanceof EntityPlayerMP) {
         Witchery.packetPipeline.sendTo((IMessage)(new PacketCamPos(false, false, (Entity)null)), (EntityPlayerMP)((EntityPlayerMP)player));
      }

   }

   public boolean isBook(ItemStack itemstack) {
      return this.itemBookOven.isMatch(itemstack) || this.itemBookDistilling.isMatch(itemstack) || this.itemBookCircleMagic.isMatch(itemstack) || this.itemBookInfusions.isMatch(itemstack) || this.itemBookHerbology.isMatch(itemstack) || this.itemBookBiomes.isMatch(itemstack) || this.itemBookWands.isMatch(itemstack) || this.itemBookBurning.isMatch(itemstack);
   }

   public ItemStack func_77659_a(ItemStack itemstack, World world, EntityPlayer player) {
      ItemGeneral.SubItem subItem = (ItemGeneral.SubItem)this.subItems.get(itemstack.func_77960_j());
      if (this.isBook(itemstack)) {
         this.openWitchcraftBook(world, player, itemstack);
      } else if (this.itemWolfsbane.isMatch(itemstack)) {
         if (!world.field_72995_K) {
            MovingObjectPosition mop = InfusionOtherwhere.raytraceEntities(world, player, true, 2.0D);
            if (mop != null && mop.field_72308_g != null) {
               if (CreatureUtil.isWerewolf(mop.field_72308_g, true)) {
                  ParticleEffect.FLAME.send(SoundEffect.WITCHERY_MOB_WOLFMAN_HOWL, mop.field_72308_g, 0.5D, 1.5D, 16);
               } else {
                  SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
               }

               --itemstack.field_77994_a;
               if (itemstack.field_77994_a <= 0) {
                  itemstack.field_77994_a = 0;
                  player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
               }
            }
         }
      } else if (subItem instanceof ItemGeneral.Edible) {
         if (player.func_71043_e(false) || ((ItemGeneral.Edible)subItem).eatAnyTime) {
            player.func_71008_a(itemstack, this.func_77626_a(itemstack));
         }
      } else if (subItem instanceof ItemGeneral.Drinkable) {
         player.func_71008_a(itemstack, this.func_77626_a(itemstack));
      } else if (this.itemContractTorment.isMatch(itemstack)) {
         player.func_71008_a(itemstack, this.func_77626_a(itemstack));
      } else if (subItem instanceof ItemGeneral.Brew) {
         this.throwBrew(itemstack, world, player, subItem);
      } else if (this.itemQuicklime.isMatch(itemstack)) {
         this.throwBrew(itemstack, world, player, this.itemQuicklime);
      } else if (this.itemNecroStone.isMatch(itemstack)) {
         this.useNecroStone(world, player, itemstack);
      } else if (this.itemBatBall.isMatch(itemstack)) {
         --itemstack.field_77994_a;
         if (itemstack.field_77994_a <= 0) {
            player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
         }

         if (!world.field_72995_K) {
            EntityItem item = new EntityItem(world, player.field_70165_t, player.field_70163_u + 1.3D, player.field_70161_v, this.itemBatBall.createStack());
            item.field_145804_b = 5;
            item.func_70012_b(player.field_70165_t, player.field_70163_u + (double)player.func_70047_e(), player.field_70161_v, player.field_70177_z, player.field_70125_A);
            item.field_70165_t -= (double)(MathHelper.func_76134_b(item.field_70177_z / 180.0F * 3.1415927F) * 0.16F);
            item.field_70163_u -= 0.10000000149011612D;
            item.field_70161_v -= (double)(MathHelper.func_76126_a(item.field_70177_z / 180.0F * 3.1415927F) * 0.16F);
            item.func_70107_b(item.field_70165_t, item.field_70163_u, item.field_70161_v);
            item.field_70129_M = 0.0F;
            float f = 0.4F;
            item.field_70159_w = (double)(-MathHelper.func_76126_a(item.field_70177_z / 180.0F * 3.1415927F) * MathHelper.func_76134_b(item.field_70125_A / 180.0F * 3.1415927F) * f);
            item.field_70179_y = (double)(MathHelper.func_76134_b(item.field_70177_z / 180.0F * 3.1415927F) * MathHelper.func_76134_b(item.field_70125_A / 180.0F * 3.1415927F) * f);
            item.field_70181_x = (double)(-MathHelper.func_76126_a((item.field_70125_A + 0.0F) / 180.0F * 3.1415927F) * f);
            this.setThrowableHeading(item, item.field_70159_w, item.field_70181_x, item.field_70179_y, 1.0F, 1.0F);
            world.func_72838_d(item);
         }
      } else if (this.itemSeerStone.isMatch(itemstack)) {
         this.useSeerStone(world, player, itemstack);
      } else if (this.itemIcyNeedle.isMatch(itemstack)) {
         this.useIcyNeedle(world, player, itemstack);
         return player.func_71045_bC() != null ? player.func_71045_bC() : itemstack;
      }

      return super.func_77659_a(itemstack, world, player);
   }

   public void setThrowableHeading(EntityItem item, double par1, double par3, double par5, float par7, float par8) {
      float f2 = MathHelper.func_76133_a(par1 * par1 + par3 * par3 + par5 * par5);
      par1 /= (double)f2;
      par3 /= (double)f2;
      par5 /= (double)f2;
      par1 += item.field_70170_p.field_73012_v.nextGaussian() * 0.007499999832361937D * (double)par8;
      par3 += item.field_70170_p.field_73012_v.nextGaussian() * 0.007499999832361937D * (double)par8;
      par5 += item.field_70170_p.field_73012_v.nextGaussian() * 0.007499999832361937D * (double)par8;
      par1 *= (double)par7;
      par3 *= (double)par7;
      par5 *= (double)par7;
      item.field_70159_w = par1;
      item.field_70181_x = par3;
      item.field_70179_y = par5;
      float f3 = MathHelper.func_76133_a(par1 * par1 + par5 * par5);
      item.field_70126_B = item.field_70177_z = (float)(Math.atan2(par1, par5) * 180.0D / 3.141592653589793D);
      item.field_70127_C = item.field_70125_A = (float)(Math.atan2(par3, (double)f3) * 180.0D / 3.141592653589793D);
   }

   private void useIcyNeedle(World world, EntityPlayer player, ItemStack itemstack) {
      if (!player.field_71075_bZ.field_75098_d) {
         --itemstack.field_77994_a;
         if (itemstack.field_77994_a <= 0) {
            itemstack.field_77994_a = 0;
            player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
         }
      }

      if (world.field_73011_w.field_76574_g == Config.instance().dimensionDreamID) {
         WorldProviderDreamWorld.returnPlayerToOverworld(player);
         itemstack.field_77994_a = 0;
      } else if (WorldProviderDreamWorld.getPlayerIsGhost(player)) {
         WorldProviderDreamWorld.returnGhostPlayerToSpiritWorld(player);
         itemstack.field_77994_a = 0;
      } else {
         player.func_70097_a(DamageSource.func_76365_a(player), 1.0F);
      }

   }

   public void throwBrew(ItemStack itemstack, World world, EntityPlayer player) {
      if (itemstack != null && itemstack.func_77973_b() == this) {
         ItemGeneral.SubItem subItem = (ItemGeneral.SubItem)this.subItems.get(itemstack.func_77960_j());
         this.throwBrew(itemstack, world, player, subItem);
      }

   }

   private void throwBrew(ItemStack itemstack, World world, EntityPlayer player, ItemGeneral.SubItem item) {
      if (!player.field_71075_bZ.field_75098_d) {
         --itemstack.field_77994_a;
      }

      world.func_72956_a(player, "random.bow", 0.5F, 0.4F / (field_77697_d.nextFloat() * 0.4F + 0.8F));
      if (!world.field_72995_K) {
         world.func_72838_d(new EntityWitchProjectile(world, player, item));
      }

   }

   private void openWitchcraftBook(World world, EntityPlayer player, ItemStack itemstack) {
      int posX = MathHelper.func_76128_c(player.field_70165_t);
      int posY = MathHelper.func_76128_c(player.field_70163_u);
      int posZ = MathHelper.func_76128_c(player.field_70161_v);
      player.openGui(Witchery.instance, 1, world, posX, posY, posZ);
   }

   public boolean func_77648_a(ItemStack itemstack, EntityPlayer player, World world, int posX, int posY, int posZ, int side, float par8, float par9, float par10) {
      if (this.itemMutandis.isMatch(itemstack)) {
         return this.useMutandis(false, itemstack, player, world, posX, posY, posZ);
      } else if (this.itemAnnointingPaste.isMatch(itemstack)) {
         return this.useAnnointingPaste(itemstack, player, world, posX, posY, posZ);
      } else if (this.itemKobolditePentacle.isMatch(itemstack)) {
         if (world.func_147439_a(posX, posY, posZ) == Witchery.Blocks.ALTAR && side == 1 && world.func_147439_a(posX, posY + 1, posZ) == Blocks.field_150350_a) {
            BlockPlacedItem.placeItemInWorld(itemstack, player, world, posX, posY + 1, posZ);
            player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
            return !world.field_72995_K;
         } else {
            return super.func_77648_a(itemstack, player, world, posX, posY, posZ, side, par8, par9, par10);
         }
      } else if (this.itemMutandisExtremis.isMatch(itemstack)) {
         return this.useMutandis(true, itemstack, player, world, posX, posY, posZ);
      } else if (!this.itemChaliceEmpty.isMatch(itemstack) && !this.itemChaliceFull.isMatch(itemstack)) {
         if (this.itemCandelabra.isMatch(itemstack)) {
            return this.placeBlock(Witchery.Blocks.CANDELABRA, itemstack, player, world, posX, posY, posZ, side, par8, par9, par10);
         } else if (this.itemBroomEnchanted.isMatch(itemstack)) {
            return this.placeBroom(itemstack, player, world, posX, posY, posZ, side, par8, par9, par10);
         } else if (this.subItems.get(itemstack.func_77960_j()) instanceof ItemGeneral.DreamWeave) {
            return this.placeDreamCatcher(world, player, itemstack, posX, posY, posZ, side);
         } else if (this.itemDoorRowan.isMatch(itemstack)) {
            return this.placeDoor(world, player, itemstack, posX, posY, posZ, side, Witchery.Blocks.DOOR_ROWAN);
         } else if (this.itemDoorAlder.isMatch(itemstack)) {
            return this.placeDoor(world, player, itemstack, posX, posY, posZ, side, Witchery.Blocks.DOOR_ALDER);
         } else if (this.itemDoorIce.isMatch(itemstack)) {
            return this.placeDoor(world, player, itemstack, posX, posY, posZ, side, Witchery.Blocks.PERPETUAL_ICE_DOOR);
         } else if (!this.itemSubduedSpirit.isMatch(itemstack) && !this.itemSubduedSpiritVillage.isMatch(itemstack)) {
            if (this.itemSeedsTreefyd.isMatch(itemstack)) {
               return this.placeTreefyd(world, player, itemstack, posX, posY, posZ, side);
            } else if (this.itemBinkyHead.isMatch(itemstack)) {
               return this.placeBinky(world, player, itemstack, posX, posY, posZ, side);
            } else if (this.itemInfernalBlood.isMatch(itemstack)) {
               return this.placeInfernalBlood(world, player, itemstack, posX, posY, posZ, side);
            } else if (this.itemDemonHeart.isMatch(itemstack) && player.func_70093_af()) {
               return this.placeBlock(Witchery.Blocks.DEMON_HEART, itemstack, player, world, posX, posY, posZ, side, par8, par9, par10);
            } else if (this.itemBoneNeedle.isMatch(itemstack) && ExtendedPlayer.get(player).isVampire()) {
               Block block = world.func_147439_a(posX, posY, posZ);
               if (block == Blocks.field_150325_L && world.func_72805_g(posX, posY, posZ) == 0) {
                  ExtendedPlayer playerEx = ExtendedPlayer.get(player);
                  if (playerEx.getVampireLevel() >= 4 && playerEx.decreaseBloodPower(125, true)) {
                     world.func_147449_b(posX, posY, posZ, Witchery.Blocks.BLOODED_WOOL);
                     ParticleEffect.REDDUST.send(SoundEffect.WITCHERY_RANDOM_DRINK, world, (double)posX + 0.5D, (double)posY + 0.5D, (double)posZ + 0.5D, 1.0D, 1.0D, 16);
                     return true;
                  }
               }

               SoundEffect.NOTE_SNARE.playOnlyTo(player);
               return true;
            } else {
               return super.func_77648_a(itemstack, player, world, posX, posY, posZ, side, par8, par9, par10);
            }
         } else {
            return this.useSubduedSpirit(world, player, itemstack, posX, posY, posZ, side);
         }
      } else {
         return this.placeBlock(Witchery.Blocks.CHALICE, itemstack, player, world, posX, posY, posZ, side, par8, par9, par10);
      }
   }

   private boolean placeBinky(World world, EntityPlayer player, ItemStack itemstack, int posX, int posY, int posZ, int side) {
      if (side != 1) {
         return false;
      } else {
         ++posY;
         Material material = world.func_147439_a(posX, posY, posZ).func_149688_o();
         if (material == null || !material.func_76220_a()) {
            if (!world.field_72995_K) {
               EntityDeathsHorse horse = new EntityDeathsHorse(world);
               horse.func_110234_j(true);
               horse.func_110214_p(4);
               horse.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.3D);
               horse.func_110163_bv();
               horse.func_94058_c(Witchery.resource("item.witchery.horseofdeath.customname"));
               horse.func_70012_b(0.5D + (double)posX, 0.01D + (double)posY, 0.5D + (double)posZ, 0.0F, 0.0F);
               NBTTagCompound nbtHorse = horse.getEntityData();
               if (nbtHorse != null) {
                  nbtHorse.func_74757_a("WITCIsBinky", true);
               }

               ParticleEffect.INSTANT_SPELL.send(SoundEffect.NONE, world, 0.5D + (double)posX, (double)posY, 0.5D + (double)posZ, 1.0D, 1.0D, 16);
               world.func_72838_d(horse);
            }

            --itemstack.field_77994_a;
         }

         return true;
      }
   }

   private boolean placeInfernalBlood(World world, EntityPlayer player, ItemStack itemstack, int posX, int posY, int posZ, int side) {
      Block block = world.func_147439_a(posX, posY, posZ);
      int meta = world.func_72805_g(posX, posY, posZ);
      if (block == Witchery.Blocks.WICKER_BUNDLE && BlockWickerBundle.limitToValidMetadata(meta) == 0) {
         if (!world.field_72995_K) {
            int uses = 5;

            for(int y = posY - 1; y <= posY + 1 && uses > 0; ++y) {
               for(int x = posX - 1; x <= posX + 1 && uses > 0; ++x) {
                  for(int z = posZ - 1; z <= posZ + 1 && uses > 0; ++z) {
                     Block b = world.func_147439_a(x, y, z);
                     int m = world.func_72805_g(x, y, z);
                     if (b == Witchery.Blocks.WICKER_BUNDLE && BlockWickerBundle.limitToValidMetadata(m) == 0) {
                        world.func_147465_d(x, y, z, b, m | 1, 3);
                        --uses;
                     }
                  }
               }
            }
         }

         --itemstack.field_77994_a;
         return true;
      } else {
         return false;
      }
   }

   private boolean placeTreefyd(World world, EntityPlayer player, ItemStack itemstack, int posX, int posY, int posZ, int side) {
      if (side != 1) {
         return false;
      } else {
         ++posY;
         Material material = world.func_147439_a(posX, posY, posZ).func_149688_o();
         if (Blocks.field_150329_H.func_149718_j(world, posX, posY, posZ) && (material == null || !material.func_76220_a())) {
            if (!world.field_72995_K) {
               world.func_147465_d(posX, posY, posZ, Blocks.field_150329_H, 1, 3);
               EntityTreefyd entity = new EntityTreefyd(world);
               entity.func_70012_b(0.5D + (double)posX, (double)posY, 0.5D + (double)posZ, 0.0F, 0.0F);
               entity.func_110161_a((IEntityLivingData)null);
               entity.func_110163_bv();
               entity.setOwner(player.func_70005_c_());
               world.func_72838_d(entity);
               ParticleEffect.SLIME.send(SoundEffect.MOB_SILVERFISH_KILL, entity, 1.0D, 2.0D, 16);
               ParticleEffect.EXPLODE.send(SoundEffect.NONE, entity, 1.0D, 2.0D, 16);
            }

            --itemstack.field_77994_a;
         }

         return true;
      }
   }

   private boolean useSeerStone(World world, EntityPlayer player, ItemStack stack) {
      if (player.func_70093_af()) {
         if (!world.field_72995_K) {
            double MAX_TARGET_RANGE = 3.0D;
            MovingObjectPosition mop = InfusionOtherwhere.doCustomRayTrace(world, player, true, 3.0D);
            if (mop != null) {
               switch(mop.field_72313_a) {
               case ENTITY:
                  if (mop.field_72308_g instanceof EntityPlayer) {
                     this.readPlayer(player, (EntityPlayer)mop.field_72308_g);
                     return true;
                  }
                  break;
               default:
                  this.readPlayer(player, player);
               }
            } else {
               this.readPlayer(player, player);
            }
         }
      } else {
         player.func_71008_a(stack, this.func_77626_a(stack));
      }

      return true;
   }

   private void readPlayer(EntityPlayer player, EntityPlayer targetPlayer) {
      String reading = "";
      NBTTagCompound nbtPlayer = Infusion.getNBT(targetPlayer);
      if (nbtPlayer != null && nbtPlayer.func_74764_b("WITCManifestDuration")) {
         int timeRemaining = nbtPlayer.func_74762_e("WITCManifestDuration");
         if (timeRemaining > 0) {
            reading = reading + String.format(Witchery.resource("item.witchery:ingredient.seerstone.manifestationtime"), Integer.valueOf(timeRemaining).toString()) + " ";
         } else {
            reading = reading + Witchery.resource("item.witchery:ingredient.seerstone.nomanifestationtime") + " ";
         }
      } else {
         reading = reading + Witchery.resource("item.witchery:ingredient.seerstone.nomanifestationtime") + " ";
      }

      String familiarName = Familiar.getFamiliarName(targetPlayer);
      if (familiarName != null && !familiarName.isEmpty()) {
         reading = reading + String.format(Witchery.resource("item.witchery:ingredient.seerstone.familiar"), familiarName) + " ";
      } else {
         reading = reading + Witchery.resource("item.witchery:ingredient.seerstone.nofamiliar") + " ";
      }

      int covenSize = EntityCovenWitch.getCovenSize(targetPlayer);
      if (covenSize > 0) {
         reading = reading + String.format(Witchery.resource("item.witchery:ingredient.seerstone.covensize"), Integer.valueOf(covenSize).toString()) + " ";
      } else {
         reading = reading + Witchery.resource("item.witchery:ingredient.seerstone.nocoven") + " ";
      }

      String spellKnowledge = SymbolEffect.getKnowledge(targetPlayer);
      if (!spellKnowledge.isEmpty()) {
         reading = reading + String.format(Witchery.resource("item.witchery:ingredient.seerstone.knownspells"), spellKnowledge) + " ";
      } else {
         reading = reading + Witchery.resource("item.witchery:ingredient.seerstone.nospells") + " ";
      }

      ExtendedPlayer playerEx = ExtendedPlayer.get(targetPlayer);
      int level;
      if (playerEx != null) {
         level = playerEx.getSkillPotionBottling();
         reading = reading + String.format(Witchery.resource("item.witchery:ingredient.seerstone.bottlingskill"), Integer.valueOf(level).toString()) + " ";
      }

      if (nbtPlayer == null || !nbtPlayer.func_74764_b("witcheryCursed") && !nbtPlayer.func_74764_b("witcheryInsanity") && !nbtPlayer.func_74764_b("witcherySinking") && !nbtPlayer.func_74764_b("witcheryOverheating") && !nbtPlayer.func_74764_b("witcheryWakingNightmare")) {
         reading = reading + Witchery.resource("witchery.item.seerstone.notcursed");
      } else {
         if (nbtPlayer.func_74764_b("witcheryCursed")) {
            level = nbtPlayer.func_74762_e("witcheryCursed");
            if (!reading.isEmpty()) {
               reading = reading + ", ";
            }

            reading = reading + String.format(Witchery.resource("witchery.item.seerstone.misfortune"), level);
         }

         if (nbtPlayer.func_74764_b("witcheryInsanity")) {
            level = nbtPlayer.func_74762_e("witcheryInsanity");
            if (!reading.isEmpty()) {
               reading = reading + ", ";
            }

            reading = reading + String.format(Witchery.resource("witchery.item.seerstone.insanity"), level);
         }

         if (nbtPlayer.func_74764_b("witcherySinking")) {
            level = nbtPlayer.func_74762_e("witcherySinking");
            if (!reading.isEmpty()) {
               reading = reading + ", ";
            }

            reading = reading + String.format(Witchery.resource("witchery.item.seerstone.sinking"), level);
         }

         if (nbtPlayer.func_74764_b("witcheryOverheating")) {
            level = nbtPlayer.func_74762_e("witcheryOverheating");
            if (!reading.isEmpty()) {
               reading = reading + ", ";
            }

            reading = reading + String.format(Witchery.resource("witchery.item.seerstone.overheating"), level);
         }

         if (nbtPlayer.func_74764_b("witcheryWakingNightmare")) {
            level = nbtPlayer.func_74762_e("witcheryWakingNightmare");
            if (!reading.isEmpty()) {
               reading = reading + ", ";
            }

            reading = reading + String.format(Witchery.resource("witchery.item.seerstone.nightmare"), level);
         }
      }

      ChatUtil.sendPlain(EnumChatFormatting.BLUE, player, reading);
   }

   private boolean useSubduedSpirit(World world, EntityPlayer player, ItemStack itemstack, int x, int y, int z, int side) {
      if (!world.field_72995_K) {
         EntityCreature creature = Infusion.spawnCreature(world, EntitySpirit.class, x, y, z, (EntityLivingBase)null, 0, 0, ParticleEffect.INSTANT_SPELL, (SoundEffect)null);
         if (creature != null) {
            EntitySpirit spirit = (EntitySpirit)creature;
            creature.func_110163_bv();
            if (this.itemSubduedSpiritVillage.isMatch(itemstack)) {
               spirit.setTarget("Village", 1);
            }

            if (!player.field_71075_bZ.field_75098_d && --itemstack.field_77994_a == 0) {
               player.field_71071_by.func_70299_a(player.field_71071_by.field_70461_c, (ItemStack)null);
               if (player instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)player).func_71120_a(player.field_71069_bz);
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean useNecroStone(World world, EntityPlayer player, ItemStack itemstack) {
      if (world.field_72995_K) {
         return false;
      } else {
         double MAX_TARGET_RANGE = 15.0D;
         MovingObjectPosition mop = InfusionOtherwhere.doCustomRayTrace(world, player, true, 15.0D);
         if (mop != null) {
            boolean r;
            switch(mop.field_72313_a) {
            case ENTITY:
               if (mop.field_72308_g instanceof EntityLivingBase) {
                  if (!player.func_70093_af()) {
                     EntityLivingBase targetEntityLivingBase = (EntityLivingBase)mop.field_72308_g;
                     r = true;
                     int minionCount = 0;
                     AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(player.field_70165_t - 50.0D, player.field_70163_u - 15.0D, player.field_70161_v - 50.0D, player.field_70165_t + 50.0D, player.field_70163_u + 15.0D, player.field_70161_v + 50.0D);
                     Iterator i$ = world.func_72872_a(EntityLiving.class, bounds).iterator();

                     while(i$.hasNext()) {
                        Object obj = i$.next();
                        EntityLiving nearbyLivingEntity = (EntityLiving)obj;
                        if (nearbyLivingEntity.func_70668_bt() == EnumCreatureAttribute.UNDEAD && PotionEnslaved.isMobEnslavedBy(nearbyLivingEntity, player)) {
                           ++minionCount;
                           EntityUtil.setTarget(nearbyLivingEntity, targetEntityLivingBase);
                        }
                     }

                     if (minionCount > 0) {
                        Witchery.packetPipeline.sendToAllAround(new PacketParticles(ParticleEffect.CRIT, SoundEffect.MOB_ZOMBIE_DEATH, targetEntityLivingBase, 0.5D, 2.0D), TargetPointUtil.from(targetEntityLivingBase, 16.0D));
                        return true;
                     }
                  } else {
                     if (InfusedBrewEffect.Grave.isActive(player) && InfusedBrewEffect.Grave.tryUseEffect(player, mop)) {
                        Witchery.packetPipeline.sendToAllAround(new PacketParticles(ParticleEffect.MOB_SPELL, SoundEffect.MOB_ZOMBIE_INFECT, mop.field_72308_g, 1.0D, 1.0D), TargetPointUtil.from(mop.field_72308_g, 16.0D));
                        return true;
                     }

                     Witchery.packetPipeline.sendToAllAround(new PacketParticles(ParticleEffect.SMOKE, SoundEffect.NOTE_SNARE, mop.field_72308_g, 1.0D, 1.0D), TargetPointUtil.from(mop.field_72308_g, 16.0D));
                  }
               }
               break;
            case BLOCK:
               if (world.func_147439_a(mop.field_72311_b, mop.field_72312_c, mop.field_72309_d) == Witchery.Blocks.ALLURING_SKULL) {
                  return false;
               }

               if (BlockSide.TOP.isEqual(mop.field_72310_e)) {
                  int minionCount = 0;
                  r = true;
                  AxisAlignedBB bounds = AxisAlignedBB.func_72330_a(player.field_70165_t - 50.0D, player.field_70163_u - 15.0D, player.field_70161_v - 50.0D, player.field_70165_t + 50.0D, player.field_70163_u + 15.0D, player.field_70161_v + 50.0D);
                  Iterator i$ = world.func_72872_a(EntityLiving.class, bounds).iterator();

                  label83:
                  while(true) {
                     EntityLiving creature;
                     EntityCreature creature2;
                     do {
                        do {
                           do {
                              if (!i$.hasNext()) {
                                 if (minionCount > 0) {
                                    ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_POP, world, 0.5D + (double)mop.field_72311_b, (double)(mop.field_72312_c + 1), 0.5D + (double)mop.field_72309_d, 1.0D, 1.0D, 16);
                                    return true;
                                 }
                                 break label83;
                              }

                              Object obj = i$.next();
                              creature = (EntityLiving)obj;
                              creature2 = creature instanceof EntityCreature ? (EntityCreature)creature : null;
                           } while(creature.func_70668_bt() != EnumCreatureAttribute.UNDEAD);
                        } while(!PotionEnslaved.isMobEnslavedBy(creature, player));

                        ++minionCount;
                        creature.func_70624_b((EntityLivingBase)null);
                        creature.func_70604_c((EntityLivingBase)null);
                        if (creature2 != null) {
                           creature2.func_70784_b((Entity)null);
                        }
                     } while(!(creature instanceof EntitySpider) && creature.func_70661_as().func_75492_a((double)mop.field_72311_b, (double)(mop.field_72312_c + 1), (double)mop.field_72309_d, 1.0D));

                     if (creature2 != null) {
                        creature2.func_70778_a(world.func_72844_a(creature, mop.field_72311_b, mop.field_72312_c + 1, mop.field_72309_d, 10.0F, true, false, false, true));
                     }
                  }
               }
            case MISS:
            }
         }

         SoundEffect.NOTE_SNARE.playAtPlayer(world, player);
         return false;
      }
   }

   private boolean placeDoor(World world, EntityPlayer player, ItemStack itemstack, int posX, int posY, int posZ, int side, Block block) {
      if (side != 1) {
         return false;
      } else {
         ++posY;
         if (player.func_82247_a(posX, posY, posZ, side, itemstack) && player.func_82247_a(posX, posY + 1, posZ, side, itemstack)) {
            if (!block.func_149742_c(world, posX, posY, posZ)) {
               return false;
            } else {
               int i1 = MathHelper.func_76128_c((double)((player.field_70177_z + 180.0F) * 4.0F / 360.0F) - 0.5D) & 3;
               ItemDoor.func_150924_a(world, posX, posY, posZ, i1, block);
               if (!world.field_72995_K && this.itemDoorRowan.isMatch(itemstack)) {
                  ItemStack keyStack = Witchery.Items.GENERIC.itemDoorKey.createStack();
                  if (!keyStack.func_77942_o()) {
                     keyStack.func_77982_d(new NBTTagCompound());
                  }

                  NBTTagCompound nbtTag = keyStack.func_77978_p();
                  nbtTag.func_74768_a("doorX", posX);
                  nbtTag.func_74768_a("doorY", posY);
                  nbtTag.func_74768_a("doorZ", posZ);
                  nbtTag.func_74768_a("doorD", world.field_73011_w.field_76574_g);
                  nbtTag.func_74778_a("doorDN", world.field_73011_w.func_80007_l());
                  world.func_72838_d(new EntityItem(world, player.field_70165_t, player.field_70163_u + 0.5D, player.field_70161_v, keyStack));
               }

               --itemstack.field_77994_a;
               return true;
            }
         } else {
            return false;
         }
      }
   }

   public static void setBlockToClay(World world, int x, int y, int z) {
      Block block = world.func_147439_a(x, y, z);
      Block blockAbove = world.func_147439_a(x, y + 1, z);
      if (block == Blocks.field_150346_d && (blockAbove == Blocks.field_150355_j || blockAbove == Blocks.field_150358_i)) {
         world.func_147449_b(x, y, z, Blocks.field_150435_aG);
         if (!world.field_72995_K) {
            ParticleEffect.INSTANT_SPELL.send(SoundEffect.MOB_SLIME_BIG, world, 0.5D + (double)x, 1.5D + (double)y, 0.5D + (double)z, 1.0D, 1.0D, 16);
         }
      }

   }

   private boolean useAnnointingPaste(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
      if (!world.field_72995_K) {
         Block block = world.func_147439_a(x, y, z);
         world.func_72805_g(x, y, z);
         if (block == Blocks.field_150383_bp) {
            world.func_147449_b(x, y, z, Witchery.Blocks.CAULDRON);
            --stack.field_77994_a;
            ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_FIZZ, world, (double)x, (double)y, (double)z, 1.0D, 1.0D, 16);
            ParticleEffect.LARGE_EXPLODE.send(SoundEffect.RANDOM_LEVELUP, world, (double)x, (double)y, (double)z, 1.0D, 1.0D, 16);
         }
      }

      return true;
   }

   private boolean useMutandis(boolean extremis, ItemStack itemstack, EntityPlayer player, World world, int posX, int posY, int posZ) {
      if (!world.field_72995_K) {
         Block block = world.func_147439_a(posX, posY, posZ);
         Block blockAbove = world.func_147439_a(posX, posY + 1, posZ);
         if (!extremis || block != Blocks.field_150349_c && block != Blocks.field_150391_bh) {
            if (!extremis || block != Blocks.field_150346_d || blockAbove != Blocks.field_150355_j && blockAbove != Blocks.field_150358_i) {
               int metadata = world.func_72805_g(posX, posY, posZ);
               ArrayList list;
               MutableBlock[] blocks;
               if (block == Blocks.field_150457_bL && metadata > 0) {
                  blocks = new MutableBlock[]{new MutableBlock(Blocks.field_150457_bL, 1), new MutableBlock(Blocks.field_150457_bL, 2), new MutableBlock(Blocks.field_150457_bL, 3), new MutableBlock(Blocks.field_150457_bL, 4), new MutableBlock(Blocks.field_150457_bL, 5), new MutableBlock(Blocks.field_150457_bL, 6), new MutableBlock(Blocks.field_150457_bL, 7), new MutableBlock(Blocks.field_150457_bL, 8), new MutableBlock(Blocks.field_150457_bL, 9), new MutableBlock(Blocks.field_150457_bL, 10), new MutableBlock(Blocks.field_150457_bL, 11)};
                  list = new ArrayList(Arrays.asList(blocks));
               } else {
                  blocks = new MutableBlock[]{new MutableBlock(Blocks.field_150345_g, 0), new MutableBlock(Blocks.field_150345_g, 1), new MutableBlock(Blocks.field_150345_g, 2), new MutableBlock(Blocks.field_150345_g, 3), new MutableBlock(Blocks.field_150345_g, 4), new MutableBlock(Blocks.field_150345_g, 5), new MutableBlock(Witchery.Blocks.SAPLING, 0), new MutableBlock(Witchery.Blocks.SAPLING, 1), new MutableBlock(Witchery.Blocks.SAPLING, 2), new MutableBlock(Witchery.Blocks.EMBER_MOSS, 0), new MutableBlock(Blocks.field_150329_H, 1), new MutableBlock(Blocks.field_150392_bi), new MutableBlock(Blocks.field_150338_P), new MutableBlock(Blocks.field_150337_Q), new MutableBlock(Blocks.field_150328_O, 0), new MutableBlock(Blocks.field_150327_N), new MutableBlock(Witchery.Blocks.SPANISH_MOSS, 1)};
                  list = new ArrayList(Arrays.asList(blocks));
                  String[] arr$ = Config.instance().mutandisExtras;
                  int len$ = arr$.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     String extra = arr$[i$];

                     try {
                        list.add(new MutableBlock(extra));
                     } catch (Throwable var18) {
                     }
                  }

                  MutableBlock[] spiritBlocks;
                  if (extremis) {
                     spiritBlocks = new MutableBlock[]{new MutableBlock(Blocks.field_150459_bM, -1, Math.min(metadata, 7)), new MutableBlock(Blocks.field_150469_bN, -1, Math.min(metadata, 7)), new MutableBlock(Blocks.field_150464_aj, -1, Math.min(metadata, 7)), new MutableBlock(Blocks.field_150436_aH, -1, Math.min(metadata, 7)), new MutableBlock(Witchery.Blocks.CROP_BELLADONNA, -1, Math.min(metadata, Witchery.Blocks.CROP_BELLADONNA.getNumGrowthStages())), new MutableBlock(Witchery.Blocks.CROP_MANDRAKE, -1, Math.min(metadata, Witchery.Blocks.CROP_MANDRAKE.getNumGrowthStages())), new MutableBlock(Witchery.Blocks.CROP_ARTICHOKE, -1, Math.min(metadata, Witchery.Blocks.CROP_ARTICHOKE.getNumGrowthStages())), new MutableBlock(Blocks.field_150393_bb, -1, Math.min(metadata, 7)), new MutableBlock(Blocks.field_150434_aF), new MutableBlock(Blocks.field_150394_bc, -1, Math.min(metadata, 7)), new MutableBlock(Blocks.field_150388_bm, -1, Math.min(metadata, 3))};
                     list.addAll(Arrays.asList(spiritBlocks));
                  } else if (player.field_71093_bK == Config.instance().dimensionDreamID) {
                     spiritBlocks = new MutableBlock[]{new MutableBlock(Blocks.field_150388_bm, -1, 3)};
                     list.addAll(Arrays.asList(spiritBlocks));
                  }
               }

               MutableBlock mutableBlock = new MutableBlock(block, metadata, 0);
               int index = list.indexOf(mutableBlock);
               if (index != -1) {
                  list.remove(index);
                  ((MutableBlock)list.get(world.field_73012_v.nextInt(list.size()))).mutate(world, posX, posY, posZ);
                  ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_FIZZ, world, (double)posX, (double)posY, (double)posZ, 1.0D, 1.0D, 16);
                  --itemstack.field_77994_a;
               }
            } else {
               if (world.field_73012_v.nextInt(2) == 0) {
                  setBlockToClay(world, posX, posY, posZ);
                  setBlockToClay(world, posX + 1, posY, posZ);
                  setBlockToClay(world, posX - 1, posY, posZ);
                  setBlockToClay(world, posX, posY, posZ + 1);
                  setBlockToClay(world, posX, posY, posZ - 1);
               } else {
                  ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_FIZZ, world, (double)posX, (double)(posY + 1), (double)posZ, 1.0D, 1.0D, 16);
               }

               --itemstack.field_77994_a;
            }
         } else {
            if (world.field_73012_v.nextInt(2) == 0) {
               world.func_147449_b(posX, posY, posZ, (Block)(block == Blocks.field_150349_c ? Blocks.field_150391_bh : Blocks.field_150349_c));
            }

            ParticleEffect.INSTANT_SPELL.send(SoundEffect.RANDOM_FIZZ, world, (double)posX, (double)(posY + 1), (double)posZ, 1.0D, 1.0D, 16);
            --itemstack.field_77994_a;
         }
      }

      return true;
   }

   private boolean placeBroom(ItemStack itemstack, EntityPlayer player, World world, int posX, int posY, int posZ, int side, float par8, float par9, float par10) {
      float f = 1.0F;
      float f1 = player.field_70127_C + (player.field_70125_A - player.field_70127_C) * 1.0F;
      float f2 = player.field_70126_B + (player.field_70177_z - player.field_70126_B) * 1.0F;
      double d0 = player.field_70169_q + (player.field_70165_t - player.field_70169_q) * 1.0D;
      double d1 = player.field_70167_r + (player.field_70163_u - player.field_70167_r) * 1.0D + 1.62D - (double)player.field_70129_M;
      double d2 = player.field_70166_s + (player.field_70161_v - player.field_70166_s) * 1.0D;
      Vec3 vec3 = Vec3.func_72443_a(d0, d1, d2);
      float f3 = MathHelper.func_76134_b(-f2 * 0.017453292F - 3.1415927F);
      float f4 = MathHelper.func_76126_a(-f2 * 0.017453292F - 3.1415927F);
      float f5 = -MathHelper.func_76134_b(-f1 * 0.017453292F);
      float f6 = MathHelper.func_76126_a(-f1 * 0.017453292F);
      float f7 = f4 * f5;
      float f8 = f3 * f5;
      double d3 = 5.0D;
      Vec3 vec31 = vec3.func_72441_c((double)f7 * 5.0D, (double)f6 * 5.0D, (double)f8 * 5.0D);
      MovingObjectPosition movingobjectposition = world.func_72901_a(vec3, vec31, true);
      if (movingobjectposition == null) {
         return super.func_77648_a(itemstack, player, world, posX, posY, posZ, side, par8, par9, par10);
      } else {
         Vec3 vec32 = player.func_70676_i(1.0F);
         boolean flag = false;
         float f9 = 1.0F;
         List list = world.func_72839_b(player, player.field_70121_D.func_72321_a(vec32.field_72450_a * 5.0D, vec32.field_72448_b * 5.0D, vec32.field_72449_c * 5.0D).func_72314_b(1.0D, 1.0D, 1.0D));

         int i;
         for(i = 0; i < list.size(); ++i) {
            Entity entity = (Entity)list.get(i);
            if (entity.func_70067_L()) {
               float f10 = entity.func_70111_Y();
               AxisAlignedBB axisalignedbb = entity.field_70121_D.func_72314_b((double)f10, (double)f10, (double)f10);
               if (axisalignedbb.func_72318_a(vec3)) {
                  flag = true;
               }
            }
         }

         if (flag) {
            return super.func_77648_a(itemstack, player, world, posX, posY, posZ, side, par8, par9, par10);
         } else {
            if (movingobjectposition.field_72313_a == MovingObjectType.BLOCK) {
               i = movingobjectposition.field_72311_b;
               int j = movingobjectposition.field_72312_c;
               int k = movingobjectposition.field_72309_d;
               if (world.func_147439_a(i, j, k) == Blocks.field_150433_aE) {
                  --j;
               }

               EntityBroom broomEntity = new EntityBroom(world, (double)((float)i + 0.5F), (double)((float)j + 1.0F), (double)((float)k + 0.5F));
               if (itemstack.func_82837_s()) {
                  broomEntity.setCustomNameTag(itemstack.func_82833_r());
               }

               this.setBroomEntityColor(broomEntity, itemstack);
               broomEntity.field_70177_z = player.field_70177_z;
               if (!world.func_72945_a(broomEntity, broomEntity.field_70121_D.func_72314_b(-0.1D, -0.1D, -0.1D)).isEmpty()) {
                  super.func_77648_a(itemstack, player, world, posX, posY, posZ, side, par8, par9, par10);
               }

               broomEntity.field_70177_z = (float)(((MathHelper.func_76128_c((double)(player.field_70177_z * 4.0F / 360.0F) + 0.5D) & 3) - 1) * 90);
               if (!world.field_72995_K) {
                  world.func_72838_d(broomEntity);
                  int l = MathHelper.func_76141_d(broomEntity.field_70177_z * 256.0F / 360.0F);
                  Witchery.packetPipeline.sendToAllAround(new S17PacketEntityLookMove(broomEntity.func_145782_y(), (byte)0, (byte)0, (byte)0, (byte)Math.max(Math.min(l, 255), 0), (byte)0), world, TargetPointUtil.from(broomEntity, 128.0D));
               }

               if (!player.field_71075_bZ.field_75098_d) {
                  --itemstack.field_77994_a;
               }
            }

            return super.func_77648_a(itemstack, player, world, posX, posY, posZ, side, par8, par9, par10);
         }
      }
   }

   private void setBroomEntityColor(EntityBroom broomEntity, ItemStack itemstack) {
      broomEntity.setBrushColor(this.getBroomItemColor(itemstack));
   }

   public void setBroomItemColor(ItemStack itemstack, int brushColor) {
      if (brushColor >= 0 && brushColor <= 15) {
         if (!itemstack.func_77942_o()) {
            itemstack.func_77982_d(new NBTTagCompound());
         }

         NBTTagCompound nbtTag = itemstack.func_77978_p();
         nbtTag.func_74774_a("BrushColor", Byte.valueOf((byte)brushColor));
      }

   }

   public int getBroomItemColor(ItemStack stack) {
      NBTTagCompound nbtTag = stack.func_77978_p();
      return nbtTag != null && nbtTag.func_74764_b("BrushColor") ? nbtTag.func_74771_c("BrushColor") & 15 : -1;
   }

   private boolean placeBlock(Block spawnBlock, ItemStack itemstack, EntityPlayer player, World world, int posX, int posY, int posZ, int side, float par8, float par9, float par10) {
      Block block = world.func_147439_a(posX, posY, posZ);
      if (block == Blocks.field_150433_aE && (world.func_72805_g(posX, posY, posZ) & 7) < 1) {
         side = 1;
      } else if (block != Blocks.field_150395_bd && block != Blocks.field_150329_H && block != Blocks.field_150330_I) {
         if (side == 0) {
            --posY;
         } else if (side == 1) {
            ++posY;
         } else if (side == 2) {
            --posZ;
         } else if (side == 3) {
            ++posZ;
         } else if (side == 4) {
            --posX;
         } else if (side == 5) {
            ++posX;
         }
      }

      if (!player.func_82247_a(posX, posY, posZ, side, itemstack)) {
         return false;
      } else if (itemstack.field_77994_a == 0) {
         return false;
      } else {
         if (world.func_147472_a(spawnBlock, posX, posY, posZ, false, side, (Entity)null, itemstack)) {
            int j1 = spawnBlock.func_149660_a(world, posX, posY, posZ, side, par8, par9, par10, 0);
            if (world.func_147465_d(posX, posY, posZ, spawnBlock, j1, 3)) {
               if (world.func_147439_a(posX, posY, posZ) == spawnBlock) {
                  spawnBlock.func_149689_a(world, posX, posY, posZ, player, itemstack);
                  spawnBlock.func_149714_e(world, posX, posY, posZ, j1);
                  if (spawnBlock == Witchery.Blocks.CHALICE) {
                     BlockChalice.TileEntityChalice tileEntity = (BlockChalice.TileEntityChalice)world.func_147438_o(posX, posY, posZ);
                     if (tileEntity != null) {
                        tileEntity.setFilled(this.itemChaliceFull.isMatch(itemstack));
                     }
                  }
               }

               world.func_72908_a((double)((float)posX + 0.5F), (double)((float)posY + 0.5F), (double)((float)posZ + 0.5F), block.field_149762_H.func_150496_b(), (block.field_149762_H.func_150497_c() + 1.0F) / 2.0F, block.field_149762_H.func_150494_d() * 0.8F);
               --itemstack.field_77994_a;
            }
         }

         return true;
      }
   }

   private boolean placeDreamCatcher(World world, EntityPlayer player, ItemStack itemstack, int posX, int posY, int posZ, int side) {
      if (side == 0) {
         return false;
      } else if (!world.func_147439_a(posX, posY, posZ).func_149688_o().func_76220_a()) {
         return false;
      } else {
         if (side == 1) {
            ++posY;
         } else if (side == 2) {
            --posZ;
         } else if (side == 3) {
            ++posZ;
         } else if (side == 4) {
            --posX;
         } else if (side == 5) {
            ++posX;
         }

         if (!player.func_82247_a(posX, posY, posZ, side, itemstack)) {
            return false;
         } else if (!Witchery.Blocks.DREAM_CATCHER.func_149742_c(world, posX, posY, posZ)) {
            return false;
         } else if (world.field_72995_K) {
            return true;
         } else {
            if (side != 1) {
               world.func_147465_d(posX, posY, posZ, Witchery.Blocks.DREAM_CATCHER, side, 3);
               --itemstack.field_77994_a;
               BlockDreamCatcher.TileEntityDreamCatcher tileEntity = (BlockDreamCatcher.TileEntityDreamCatcher)world.func_147438_o(posX, posY, posZ);
               if (tileEntity != null) {
                  ItemGeneral.DreamWeave weave = (ItemGeneral.DreamWeave)this.subItems.get(itemstack.func_77960_j());
                  weave.setEffect(tileEntity);
               }
            }

            return true;
         }
      }
   }

   public static boolean isWaystoneBound(ItemStack stack) {
      NBTTagCompound tag = stack.func_77978_p();
      return tag != null && tag.func_74764_b("PosX") && tag.func_74764_b("PosY") && tag.func_74764_b("PosZ") && tag.func_74764_b("PosD");
   }

   public static int getWaystoneDimension(ItemStack stack) {
      NBTTagCompound tag = stack.func_77978_p();
      if (tag != null && tag.func_74764_b("PosX") && tag.func_74764_b("PosY") && tag.func_74764_b("PosZ") && tag.func_74764_b("PosD")) {
         int newX = tag.func_74762_e("PosX");
         int newY = tag.func_74762_e("PosY");
         int newZ = tag.func_74762_e("PosZ");
         int newD = tag.func_74762_e("PosD");
         return newD;
      } else {
         return 0;
      }
   }

   private boolean isRestrictedTeleportTarget(int source, int target) {
      if (source == target) {
         return false;
      } else {
         return source == Config.instance().dimensionDreamID || source == Config.instance().dimensionMirrorID || target == Config.instance().dimensionDreamID || target == Config.instance().dimensionMirrorID;
      }
   }

   public boolean teleportToLocation(World world, ItemStack itemstack, Entity entity, int radius, boolean presetPosition) {
      NBTTagCompound tag = itemstack.func_77978_p();
      if (tag != null && tag.func_74764_b("PosX") && tag.func_74764_b("PosY") && tag.func_74764_b("PosZ") && tag.func_74764_b("PosD")) {
         int newX = tag.func_74762_e("PosX") - radius + world.field_73012_v.nextInt(radius * 2 + 1);
         int newY = tag.func_74762_e("PosY");
         int newZ = tag.func_74762_e("PosZ") - radius + world.field_73012_v.nextInt(radius * 2 + 1);
         int newD = tag.func_74762_e("PosD");
         if (!this.isRestrictedTeleportTarget(entity.field_71093_bK, newD)) {
            teleportToLocation(world, (double)newX, (double)newY, (double)newZ, newD, entity, presetPosition);
            return true;
         }
      } else if (tag != null) {
         EntityLivingBase target = Witchery.Items.TAGLOCK_KIT.getBoundEntity(world, (Entity)null, itemstack, 1);
         if (entity != null && target != null && !this.isRestrictedTeleportTarget(entity.field_71093_bK, target.field_71093_bK)) {
            teleportToLocation(world, target.field_70165_t, target.field_70163_u, target.field_70161_v, target.field_71093_bK, entity, presetPosition);
            return true;
         }
      }

      return false;
   }

   public static boolean teleportToLocationSafely(World world, double posX, double posY, double posZ, int dimension, Entity entity, boolean presetPosition) {
      World targetWorld = MinecraftServer.func_71276_C().func_71218_a(dimension);
      int x = MathHelper.func_76128_c(posX);
      int y = MathHelper.func_76128_c(posY);
      int z = MathHelper.func_76128_c(posZ);

      for(int i = 0; i < 16; ++i) {
         int dy = y + i;
         if (dy < 250 && !BlockUtil.isReplaceableBlock(targetWorld, x, dy, z) && BlockUtil.isReplaceableBlock(targetWorld, x, dy + 1, z) && BlockUtil.isReplaceableBlock(targetWorld, x, dy + 2, z)) {
            teleportToLocation(world, (double)x, (double)(dy + 1), (double)z, dimension, entity, presetPosition);
            return true;
         }

         dy = y - i;
         if (i > 0 && dy > 1 && !BlockUtil.isReplaceableBlock(targetWorld, x, dy, z) && BlockUtil.isReplaceableBlock(targetWorld, x, dy + 1, z) && BlockUtil.isReplaceableBlock(targetWorld, x, dy + 2, z)) {
            teleportToLocation(world, (double)x, (double)(dy + 1), (double)z, dimension, entity, presetPosition);
            return true;
         }
      }

      return false;
   }

   public static void teleportToLocation(World world, double posX, double posY, double posZ, int dimension, Entity entity, boolean presetPosition) {
      teleportToLocation(world, posX, posY, posZ, dimension, entity, presetPosition, ParticleEffect.PORTAL, SoundEffect.MOB_ENDERMEN_PORTAL);
   }

   public static void teleportToLocation(World world, double posX, double posY, double posZ, int dimension, Entity entity, boolean presetPosition, ParticleEffect particle, SoundEffect sound) {
      boolean isVampire = CreatureUtil.isVampire(entity);
      if (isVampire) {
         Witchery.packetPipeline.sendToAllAround(new PacketParticles(ParticleEffect.SMOKE, SoundEffect.WITCHERY_RANDOM_POOF, entity, 0.5D, 2.0D), TargetPointUtil.from(entity, 16.0D));
      } else {
         Witchery.packetPipeline.sendToAllAround(new PacketParticles(particle, sound, entity, 0.5D, 2.0D), TargetPointUtil.from(entity, 16.0D));
      }

      if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         if (entity.field_71093_bK != dimension) {
            if (presetPosition) {
               player.func_70107_b(posX, posY, posZ);
            }

            travelToDimension(player, dimension);
         }

         player.func_70634_a(posX, posY, posZ);
      } else if (entity instanceof EntityLiving) {
         if (entity.field_71093_bK != dimension) {
            travelToDimension(entity, dimension, posX, posY, posZ);
         } else {
            entity.func_70012_b(posX, posY, posZ, entity.field_70177_z, entity.field_70125_A);
         }
      } else if (entity.field_71093_bK != dimension) {
         travelToDimension(entity, dimension, posX, posY, posZ);
      } else {
         entity.func_70012_b(posX, posY, posZ, entity.field_70177_z, entity.field_70125_A);
      }

      if (isVampire) {
         Witchery.packetPipeline.sendToAllAround(new PacketParticles(ParticleEffect.SMOKE, SoundEffect.WITCHERY_RANDOM_POOF, entity, 0.5D, 2.0D), TargetPointUtil.from(entity, 16.0D));
      } else {
         Witchery.packetPipeline.sendToAllAround(new PacketParticles(particle, sound, entity, 0.5D, 2.0D), TargetPointUtil.from(entity, 16.0D));
      }

   }

   public static void travelToDimension(EntityPlayer player, int dimension) {
      if (!player.field_70170_p.field_72995_K & player instanceof EntityPlayerMP) {
         MinecraftServer server = MinecraftServer.func_71276_C();
         WorldServer newWorldServer = server.func_71218_a(dimension);
         server.func_71203_ab().transferPlayerToDimension((EntityPlayerMP)player, dimension, new ItemGeneral.Teleporter2(newWorldServer));
      }

   }

   private static Entity travelToDimension(Entity thisE, int par1, double posX, double posY, double posZ) {
      if (!thisE.field_70170_p.field_72995_K && !thisE.field_70128_L) {
         thisE.field_70170_p.field_72984_F.func_76320_a("changeDimension");
         MinecraftServer minecraftserver = MinecraftServer.func_71276_C();
         int j = thisE.field_71093_bK;
         WorldServer worldserver = minecraftserver.func_71218_a(j);
         WorldServer worldserver1 = minecraftserver.func_71218_a(par1);
         thisE.field_71093_bK = par1;
         if (j == 1 && par1 == 1) {
            worldserver1 = minecraftserver.func_71218_a(0);
            thisE.field_71093_bK = 0;
         }

         thisE.field_70170_p.func_72900_e(thisE);
         thisE.field_70128_L = false;
         thisE.field_70170_p.field_72984_F.func_76320_a("reposition");
         minecraftserver.func_71203_ab().transferEntityToWorld(thisE, j, worldserver, worldserver1, new ItemGeneral.Teleporter2(worldserver1));
         thisE.field_70170_p.field_72984_F.func_76318_c("reloading");
         Entity entity = EntityList.func_75620_a(EntityList.func_75621_b(thisE), worldserver1);
         if (entity != null) {
            entity.func_82141_a(thisE, true);
            entity.func_70012_b(posX, posY, posZ, entity.field_70177_z, entity.field_70125_A);
            worldserver1.func_72838_d(entity);
         }

         thisE.field_70128_L = true;
         thisE.field_70170_p.field_72984_F.func_76319_b();
         worldserver.func_82742_i();
         worldserver1.func_82742_i();
         thisE.field_70170_p.field_72984_F.func_76319_b();
         return entity;
      } else {
         return null;
      }
   }

   public static class DreamWeave extends ItemGeneral.SubItem {
      public final int weaveID;
      public final int textureOffsetX;
      public final int textureOffsetY;
      private final Potion potionDream;
      private final Potion potionNightmare;
      private final int duration;
      private final int amplifier;

      private static ItemGeneral.DreamWeave register(ItemGeneral.DreamWeave subItem, ArrayList<ItemGeneral.SubItem> subItems, ArrayList<ItemGeneral.DreamWeave> weaves) {
         weaves.add(ItemGeneral.SubItem.register(subItem, subItems));
         return subItem;
      }

      private DreamWeave(int damageValue, int weaveID, String unlocalizedName, Potion potionDream, Potion potionNightmare, int duration, int amplifier, int textureX, int textureY) {
         super(damageValue, unlocalizedName, 1, null);
         this.potionDream = potionDream;
         this.potionNightmare = potionNightmare;
         this.duration = duration;
         this.amplifier = amplifier;
         this.textureOffsetX = textureX;
         this.textureOffsetY = textureY;
         this.weaveID = weaveID;
      }

      public void setEffect(BlockDreamCatcher.TileEntityDreamCatcher dreamCatcherEntity) {
         dreamCatcherEntity.setEffect(this);
      }

      public void applyEffect(EntityPlayer player, boolean isDream, boolean isEnhanced) {
         if (isDream) {
            player.func_70690_d(new PotionEffect(this.potionDream.func_76396_c(), isEnhanced && this.potionDream.field_76415_H == Potion.field_76443_y.field_76415_H ? this.duration + 2400 : (isEnhanced ? this.duration - 2400 : this.duration), isEnhanced && this.potionDream.field_76415_H != Potion.field_76443_y.field_76415_H ? this.amplifier + 1 : this.amplifier));
         } else {
            player.func_70690_d(new PotionEffect(this.potionNightmare.func_76396_c(), this.duration, isEnhanced ? this.amplifier + 1 : this.amplifier));
         }

      }

      // $FF: synthetic method
      DreamWeave(int x0, int x1, String x2, Potion x3, Potion x4, int x5, int x6, int x7, int x8, Object x9) {
         this(x0, x1, x2, x3, x4, x5, x6, x7, x8);
      }
   }

   public static class Drinkable extends ItemGeneral.SubItem {
      protected PotionEffect[] effects;
      protected EnumAction useAction;

      protected Drinkable(int damageValue, String unlocalizedName, int rarity, PotionEffect... effects) {
         this(damageValue, unlocalizedName, rarity, EnumAction.drink, effects);
      }

      protected Drinkable(int damageValue, String unlocalizedName, int rarity, EnumAction useAction, PotionEffect... effects) {
         super(damageValue, unlocalizedName, rarity, null);
         this.effects = effects;
         this.useAction = useAction;
      }

      public void onDrunk(World world, EntityPlayer player, ItemStack itemstack) {
      }
   }

   public static class Brew extends ItemGeneral.SubItem {
      public Brew(int damageValue, String unlocalizedName) {
         super(damageValue, unlocalizedName);
         this.setPotion(true);
      }

      public ItemGeneral.Brew.BrewResult onImpact(World world, EntityLivingBase thrower, MovingObjectPosition mop, boolean enhanced, double brewX, double brewY, double brewZ, AxisAlignedBB brewBounds) {
         return ItemGeneral.Brew.BrewResult.SHOW_EFFECT;
      }

      protected static boolean setBlockIfNotSolid(World world, int x, int y, int z, Block block) {
         return setBlockIfNotSolid(world, x, y, z, block, 0);
      }

      protected static boolean setBlockIfNotSolid(World world, int x, int y, int z, Block block, int metadata) {
         if (world.func_147439_a(x, y, z).func_149688_o().func_76220_a() && (block != Blocks.field_150321_G || BlockUtil.getBlock(world, x, y, z) != Blocks.field_150433_aE)) {
            return false;
         } else {
            BlockUtil.setBlock(world, x, y, z, (Block)block, metadata, 3);
            ParticleEffect.EXPLODE.send(SoundEffect.NONE, world, 0.5D + (double)x, 0.5D + (double)y, 0.5D + (double)z, 1.0D, 1.0D, 16);
            return true;
         }
      }

      public static enum BrewResult {
         DROP_ITEM,
         SHOW_EFFECT,
         HIDE_EFFECT;
      }
   }

   public static class Edible extends ItemGeneral.SubItem {
      public final boolean eatAnyTime;
      private final int healAmount;
      private final float saturationModifier;
      private final boolean wolfsFavorite;

      private Edible(int damageValue, String unlocalizedName, int healAmount, float saturationModifier, boolean wolfsFavorite) {
         this(damageValue, unlocalizedName, healAmount, saturationModifier, wolfsFavorite, false);
      }

      private Edible(int damageValue, String unlocalizedName, int healAmount, float saturationModifier, boolean wolfsFavorite, boolean eatAnyTime) {
         super(damageValue, unlocalizedName);
         this.healAmount = healAmount;
         this.saturationModifier = saturationModifier;
         this.wolfsFavorite = wolfsFavorite;
         this.eatAnyTime = eatAnyTime;
      }

      // $FF: synthetic method
      Edible(int x0, String x1, int x2, float x3, boolean x4, Object x5) {
         this(x0, x1, x2, x3, x4);
      }

      // $FF: synthetic method
      Edible(int x0, String x1, int x2, float x3, boolean x4, boolean x5, Object x6) {
         this(x0, x1, x2, x3, x4, x5);
      }
   }

   public static class BoltType extends ItemGeneral.SubItem {
      private BoltType(int damageValue, String unlocalizedName) {
         super(damageValue, unlocalizedName);
      }

      public static ItemGeneral.BoltType getBolt(ItemStack stack) {
         if (stack != null && stack.func_77973_b() == Witchery.Items.GENERIC) {
            ItemGeneral.SubItem item = (ItemGeneral.SubItem)Witchery.Items.GENERIC.subItems.get(stack.func_77960_j());
            if (item instanceof ItemGeneral.BoltType) {
               return (ItemGeneral.BoltType)item;
            }
         }

         return null;
      }

      // $FF: synthetic method
      BoltType(int x0, String x1, Object x2) {
         this(x0, x1);
      }
   }

   public static class SubItem {
      public final int damageValue;
      private final String unlocalizedName;
      private final int rarity;
      private final boolean showInCreativeTab;
      protected boolean enchanted;
      protected boolean potion;
      @SideOnly(Side.CLIENT)
      private IIcon icon;

      private static <T extends ItemGeneral.SubItem> T register(T subItem, ArrayList<ItemGeneral.SubItem> subItems) {
         assert subItems.size() == subItem.damageValue : "Misalignement with subItem registration";

         while(subItems.size() <= subItem.damageValue) {
            subItems.add((Object)null);
         }

         subItems.set(subItem.damageValue, subItem);
         return subItem;
      }

      public boolean isSolidifier() {
         return false;
      }

      public boolean isInfused() {
         return false;
      }

      public SubItem(int damageValue, String unlocalizedName) {
         this(damageValue, unlocalizedName, 0, true);
      }

      private SubItem(int damageValue, String unlocalizedName, int rarity) {
         this(damageValue, unlocalizedName, rarity, true);
      }

      private SubItem(int damageValue, String unlocalizedName, int rarity, boolean showInCreativeTab) {
         this.damageValue = damageValue;
         this.unlocalizedName = unlocalizedName;
         this.rarity = rarity;
         this.showInCreativeTab = showInCreativeTab;
         this.enchanted = false;
         this.potion = false;
      }

      @SideOnly(Side.CLIENT)
      private void registerIcon(IIconRegister iconRegister, ItemGeneral itemIngredient) {
         this.icon = iconRegister.func_94245_a(itemIngredient.func_111208_A() + "." + this.unlocalizedName);
      }

      public boolean isMatch(ItemStack itemstack) {
         return itemstack != null && Witchery.Items.GENERIC == itemstack.func_77973_b() && itemstack.func_77960_j() == this.damageValue;
      }

      public ItemStack createStack(int stackSize) {
         return new ItemStack(Witchery.Items.GENERIC, stackSize, this.damageValue);
      }

      public ItemStack createStack() {
         return this.createStack(1);
      }

      public boolean isItemInInventory(InventoryPlayer inventory) {
         return this.getItemSlotFromInventory(inventory) != -1;
      }

      public int getItemSlotFromInventory(InventoryPlayer inventory) {
         for(int k = 0; k < inventory.field_70462_a.length; ++k) {
            if (inventory.field_70462_a[k] != null && inventory.field_70462_a[k].func_77973_b() == Witchery.Items.GENERIC && inventory.field_70462_a[k].func_77960_j() == this.damageValue) {
               return k;
            }
         }

         return -1;
      }

      public boolean consumeItemFromInventory(InventoryPlayer inventory) {
         int j = this.getItemSlotFromInventory(inventory);
         if (j < 0) {
            return false;
         } else {
            if (--inventory.field_70462_a[j].field_77994_a <= 0) {
               inventory.field_70462_a[j] = null;
            }

            return true;
         }
      }

      public boolean isEnchanted() {
         return this.enchanted || this.potion;
      }

      public ItemGeneral.SubItem setEnchanted(boolean enchanted) {
         this.enchanted = enchanted;
         return this;
      }

      public ItemGeneral.SubItem setPotion(boolean potion) {
         this.potion = potion;
         return this;
      }

      public boolean isPotion() {
         return this.potion;
      }

      public BrewItemKey getBrewItemKey() {
         return new BrewItemKey(Witchery.Items.GENERIC, this.damageValue);
      }

      // $FF: synthetic method
      SubItem(int x0, String x1, int x2, Object x3) {
         this(x0, x1, x2);
      }

      // $FF: synthetic method
      SubItem(int x0, String x1, int x2, boolean x3, Object x4) {
         this(x0, x1, x2, x3);
      }
   }

   private static class Teleporter2 extends Teleporter {
      public Teleporter2(WorldServer server) {
         super(server);
      }

      public boolean func_85188_a(Entity par1Entity) {
         return false;
      }

      public boolean func_77184_b(Entity par1Entity, double par2, double par4, double par6, float par8) {
         return false;
      }

      public void func_77185_a(Entity par1Entity, double par2, double par4, double par6, float par8) {
      }

      public void func_85189_a(long par1) {
      }
   }
}
