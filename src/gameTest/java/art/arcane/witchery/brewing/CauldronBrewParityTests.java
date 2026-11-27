package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.DreamWeave;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.block.WitchLilyPadBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.block.entity.SlurpBlockEntity;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CauldronBrewParityTests {
    @GameTest(template = "qa_coven_arena")
    public static void vinesKeepTheirOriginalClimbableCollision(GameTestHelper helper) {
        var level = helper.getLevel();
        var wall = new BlockPos(6, 5, 5);
        helper.setBlock(wall, Blocks.STONE);
        var brew = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(), Items.VINE.getDefaultInstance()));
        var vine = WitcheryBlocks.getRequired("vine").get();
        var climber = EntityType.PIG.create(level);

        for (Direction face : Direction.Plane.HORIZONTAL) {
            var pos = helper.absolutePos(wall.relative(face));
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            var impact = helper.absolutePos(wall);
            brew.applySplashToBlock(level, impact, face, Vec3.atCenterOf(impact), null);
            var state = level.getBlockState(pos);
            helper.assertTrue(state.is(vine) && state.getValue(HorizontalDirectionalBlock.FACING) == face,
                    "The brew must place its climbable vine against the struck face " + face);
            var collision = state.getCollisionShape(level, pos);
            helper.assertTrue(!collision.isEmpty(), "The brew vine must retain its original collision on " + face);
            Direction support = face.getOpposite();
            double start = support.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0.875 : 0;
            var bounds = collision.bounds();
            helper.assertTrue(bounds.min(face.getAxis()) == start && bounds.max(face.getAxis()) == start + 0.125
                            && bounds.minY == 0 && bounds.maxY == 1
                            && state.isLadder(level, pos, climber),
                    "The vine must be climbable with a full-height strip against its support on " + face);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", timeoutTicks = 210)
    public static void transposeFindsFootingOutsideLoadedChunks(GameTestHelper helper) {
        var level = helper.getLevel();
        var target = EntityType.PIG.create(level);
        var center = new BlockPos(16000, level.getMaxBuildHeight() - 16, 16000);
        var first = center.offset(-10, 0, -10);
        var last = center.offset(9, 0, 9);
        for (BlockPos pos : BlockPos.betweenClosed(first, last)) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
            for (int dy = 1; dy <= 3; dy++) {
                level.setBlock(pos.above(dy), Blocks.AIR.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
            }
        }
        var corners = List.of(first, first.south(19), first.east(19), last);
        Vec3 startingPosition = Vec3.atBottomCenterOf(center.above(6));
        target.setPos(startingPosition);
        var brew = transposeBrew();
        helper.assertTrue(brew != null, "The original Ender Pearl brew is valid");
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(corners.stream().noneMatch(level::hasChunkAt),
                "Every chunk containing the prepared landing platform has unloaded")).thenExecute(() -> {
            try {
                brew.applySplash(target, target, null, 1, false, startingPosition);
                var arrival = target.blockPosition();
                helper.assertTrue(arrival.getY() == center.getY() + 1
                                && arrival.getX() >= first.getX() && arrival.getX() <= last.getX()
                                && arrival.getZ() >= first.getZ() && arrival.getZ() <= last.getZ()
                                && level.getBlockState(arrival.below()).is(Blocks.STONE),
                        "The splash should load the platform and find solid footing. Actual position " + target.position());
            } finally {
                target.discard();
            }
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void transposeUsesOriginalVampireAndOrdinaryCues(GameTestHelper helper) {
        var level = helper.getLevel();
        var vampire = WitcheryEntities.VAMPIRE.get().create(level);
        var pig = EntityType.PIG.create(level);
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "TransposeTester"));
        Vec3 start = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 11, 5)));
        var destination = new BrewTransposition.Site(level, helper.absolutePos(new BlockPos(8, 11, 5)));
        Vec3 arrival = Vec3.atBottomCenterOf(destination.pos());
        vampire.setPos(start);
        pig.setPos(start);
        var brew = transposeBrew();
        helper.assertTrue(brew != null, "The original Ender Pearl brew is valid");

        List<SoundEvent> sounds = new ArrayList<>();
        Consumer<PlayLevelSoundEvent.AtPosition> listener = event -> {
            if (event.getLevel() == level && event.getSound() != null
                    && (event.getPosition().equals(start) || event.getPosition().equals(arrival))) {
                sounds.add(event.getSound().value());
            }
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            brew.applyTaglock(vampire, caster, 0, destination);
            var poof = WitcherySounds.MIRROR_POOF.get();
            helper.assertTrue(vampire.position().equals(arrival) && sounds.equals(List.of(poof, poof)),
                    "A vampire gets the original poof at departure and arrival");
            sounds.clear();
            brew.applyTaglock(pig, caster, 0, destination);
            helper.assertTrue(pig.position().equals(arrival)
                            && sounds.equals(List.of(SoundEvents.ENDERMAN_TELEPORT, SoundEvents.ENDERMAN_TELEPORT)),
                    "Ordinary creatures get the original portal cue at both positions");
            sounds.clear();
            vampire.setPos(start);
            vampire.addEffect(new MobEffectInstance(WitcheryEffects.ENDER_INHIBITION.get(), 100, 3));
            brew.applyTaglock(vampire, caster, 0, destination);
            helper.assertTrue(vampire.position().equals(start) && sounds.isEmpty(),
                    "Ender Inhibition prevents the move and both teleport cues");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            caster.discard();
            pig.discard();
            vampire.discard();
        }
    }

    private static BrewMixture transposeBrew() {
        return BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART),
                new ItemStack(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()), new ItemStack(Items.GUNPOWDER),
                new ItemStack(Items.ENDER_PEARL)));
    }

    @GameTest(template = "qa_coven_arena")
    public static void strongerWebBrewExpandsFromAnExistingWeb(GameTestHelper helper) {
        var level = helper.getLevel();
        var impact = helper.absolutePos(new BlockPos(5, 6, 5));
        var center = impact.above();
        var web = WitcheryBlocks.getRequired("web").get();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "WebBrewTester"));
        var protectedCell = center.north();
        var occupiedCell = center.west();
        var grass = Blocks.GRASS.defaultBlockState();
        level.setBlockAndUpdate(impact, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(occupiedCell, Blocks.CHEST.defaultBlockState());
        level.setBlockAndUpdate(protectedCell.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(protectedCell, grass);
        var weak = BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER),
                new ItemStack(WitcheryIngredient.WEB.item())));
        var strong = BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER),
                new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(Items.BLAZE_ROD), new ItemStack(WitcheryIngredient.WEB.item())));
        helper.assertTrue(weak != null && strong != null, "Both original cauldron recipes are valid");
        var seen = new ArrayList<BlockEvent.EntityPlaceEvent>();
        Consumer<BlockEvent.EntityPlaceEvent> protection = event -> {
            if (event.getLevel() == level && event.getEntity() == player && event.getPos().equals(protectedCell)) {
                seen.add(event);
                if (event.getPlacedBlock().is(web)) event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(protection);
        try {
            Vec3 hit = Vec3.atBottomCenterOf(center);
            weak.applySplashToBlock(level, impact, Direction.UP, hit, player);
            helper.assertTrue(level.getBlockState(center).is(web) && level.isEmptyBlock(center.south()),
                    "A base-strength splash puts only a web at the center");
            strong.applySplashToBlock(level, impact, Direction.UP, hit, player);
            helper.assertTrue(level.getBlockState(center).is(web) && level.getBlockState(center.south()).is(web)
                            && level.getBlockState(center.above().east()).is(web),
                    "A second strengthened splash expands around the existing web to faces and edges");
            helper.assertTrue(seen.size() == 1 && seen.get(0).getBlockSnapshot().getReplacedBlock().equals(grass)
                            && seen.get(0).getPlacedBlock().is(web),
                    "The placement event must see the web and retain the replaced grass snapshot");
            helper.assertTrue(level.getBlockState(occupiedCell).is(Blocks.CHEST) && level.getBlockState(protectedCell).equals(grass)
                            && level.isEmptyBlock(center.above().east().south()),
                    "The expansion respects occupied and protected cells and leaves the corners clear");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            player.discard();
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_placement")
    public static void gasPlacementVetoLeavesNoCloudOrBlockEntity(GameTestHelper helper) {
        var level = helper.getLevel();
        var impact = helper.absolutePos(new BlockPos(5, 6, 5));
        var blocked = impact.above();
        var open = blocked.east(3);
        var gas = WitcheryBlocks.getRequired("brewgas").get();
        var ice = WitcheryBlocks.getRequired("perpetualice").get();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "GasBrewTester"));
        var ingredients = List.of(Items.NETHER_WART.getDefaultInstance(), Items.SPIDER_EYE.getDefaultInstance(),
                WitcheryIngredient.BAT_WOOL.item().getDefaultInstance());
        var mixture = BrewMixture.from(ingredients);
        var bottle = WitcheryItems.getRequired("brewbottle").get().getDefaultInstance();
        bottle.setTag(mixture.bottleTag(ingredients));
        level.setBlockAndUpdate(impact, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(open.below(), Blocks.STONE.defaultBlockState());

        var seen = new ArrayList<BlockEvent.EntityPlaceEvent>();
        Consumer<BlockEvent.EntityPlaceEvent> protection = event -> {
            if (event.getLevel() == level && event.getEntity() == player
                    && (event.getPos().equals(blocked) || event.getPos().equals(open))) {
                seen.add(event);
                if (event.getPos().equals(blocked) && event.getPlacedBlock().is(gas)) event.setCanceled(true);
                if (event.getPos().equals(open) && event.getPlacedBlock().is(ice)) event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(protection);
        try {
            for (BlockPos cell : List.of(blocked, open)) {
                var shot = new ThrownBrew(player, level);
                shot.setItem(bottle);
                shot.setPos(Vec3.atBottomCenterOf(cell).add(0, 0.6, 0));
                shot.setDeltaMovement(0, -1.4, 0);
                shot.tick();
            }
            helper.assertTrue(seen.size() == 2 && seen.stream().allMatch(event -> event.getPlacedBlock().is(gas)
                            && event.getBlockSnapshot().getReplacedBlock().isAir()),
                    "Both gas impacts report the actual gas state over the original air");
            helper.assertTrue(level.isEmptyBlock(blocked) && level.getBlockEntity(blocked) == null
                            && !level.getBlockTicks().hasScheduledTick(blocked, gas),
                    "The veto restores the empty cell without leaving a cloud, data or a scheduled tick");
            helper.assertTrue(level.getBlockState(open).is(gas)
                            && level.getBlockEntity(open) instanceof BrewFluidBlockEntity cloud && cloud.hasContents()
                            && level.getBlockTicks().hasScheduledTick(open, gas),
                    "Allowed gas placement initializes the cloud and schedules its first tick");
            var before = level.getBlockEntity(open).saveWithFullMetadata();
            helper.assertTrue(!BrewTerrain.setBlock(player, level, open, ice.defaultBlockState(), Direction.UP)
                            && level.getBlockState(open).is(gas)
                            && level.getBlockEntity(open) instanceof BrewFluidBlockEntity restored
                            && restored.saveWithFullMetadata().equals(before),
                    "A vetoed ice replacement restores the active gas and its entire brew payload");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            player.discard();
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void dissipateVisitsNativeColumnsAndFizzesAtCellCorners(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(6, 4, 5));
        var west = center.offset(-2, 1, 0);
        var north = center.north(2);
        var southeast = center.offset(1, 2, 1);
        var protectedCloud = center.below();
        var fringe = center.offset(2, 0, 2);
        var liquid = center.east();
        var water = center.west();
        var gas = WitcheryBlocks.getRequired("brewgas").get();
        var brewLiquid = WitcheryBlocks.getRequired("brewliquid").get();
        var brewer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "DissipateTester"));
        var ingredients = List.of(Items.NETHER_WART.getDefaultInstance(), Items.SPIDER_EYE.getDefaultInstance(),
                WitcheryIngredient.BAT_WOOL.item().getDefaultInstance());
        var cloudBrew = BrewMixture.from(ingredients);
        for (BlockPos pos : List.of(west, north, southeast, protectedCloud, fringe)) {
            level.setBlockAndUpdate(pos, gas.defaultBlockState());
            ((BrewFluidBlockEntity) level.getBlockEntity(pos)).initialize(cloudBrew.bottleTag(ingredients), cloudBrew, 0x556677, brewer);
        }
        level.setBlockAndUpdate(liquid, brewLiquid.defaultBlockState());
        level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        var saved = level.getBlockEntity(protectedCloud).saveWithFullMetadata();
        List<BlockPos> vetoes = new ArrayList<>();
        List<PlayLevelSoundEvent.AtPosition> fizz = new ArrayList<>();
        Consumer<BlockEvent.BreakEvent> protection = event -> {
            if (event.getLevel() == level && event.getPlayer() == brewer && event.getPos().equals(protectedCloud)) {
                vetoes.add(event.getPos());
                event.setCanceled(true);
            }
        };
        Consumer<PlayLevelSoundEvent.AtPosition> sound = event -> {
            if (event.getLevel() == level && event.getSound() != null && event.getSound().value() == SoundEvents.FIRE_EXTINGUISH) {
                fizz.add(event);
            }
        };
        var config = WitcheryServerConfig.ALLOW_INTER_MOD_BLOCK_BREAK_EVENTS;
        boolean previous = config.get();
        MinecraftForge.EVENT_BUS.addListener(protection);
        MinecraftForge.EVENT_BUS.addListener(sound);
        try {
            config.set(true);
            var brew = BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART),
                    new ItemStack(Items.GUNPOWDER), new ItemStack(Items.STONE)));
            brew.applySplashToBlock(level, center, Direction.UP, Vec3.atCenterOf(center), brewer);
            var cleared = List.of(west, north, southeast);
            helper.assertTrue(cleared.stream().allMatch(pos -> level.isEmptyBlock(pos) && level.getBlockEntity(pos) == null),
                    "Dissipate removes active clouds and their tile data");
            helper.assertTrue(level.getBlockState(fringe).is(gas) && level.getBlockState(liquid).is(brewLiquid)
                            && level.getBlockState(water).is(Blocks.WATER),
                    "The excluded corner and liquids remain");
            helper.assertTrue(vetoes.equals(List.of(protectedCloud, protectedCloud))
                            && level.getBlockEntity(protectedCloud).saveWithFullMetadata().equals(saved),
                    "Both original passes through the center respect protection and preserve the cloud payload");
            helper.assertTrue(fizz.stream().map(PlayLevelSoundEvent.AtPosition::getPosition).toList()
                            .equals(cleared.stream().map(Vec3::atLowerCornerOf).toList())
                            && fizz.stream().allMatch(cue -> cue.getNewVolume() == 1 && cue.getNewPitch() == 2),
                    "Only cleared cells fizz, in the original column order and at the integer block corners");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            MinecraftForge.EVENT_BUS.unregister(sound);
            config.set(previous);
            brewer.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void plantingIgnoresDroppedSaplingsAndFlowers(GameTestHelper helper) {
        var level = helper.getLevel();
        var impact = helper.absolutePos(new BlockPos(6, 5, 5)).atY(196);
        var dirt = impact.west(2);
        var farm = impact.west();
        level.setBlockAndUpdate(dirt.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(farm.below(), Blocks.FARMLAND.defaultBlockState());
        var brew = plantingBrew();

        for (var decoration : List.of(Items.POPPY, Items.OAK_SAPLING)) {
            var seed = spawnLooseItem(helper, impact, Items.WHEAT_SEEDS, 1);
            var plant = spawnLooseItem(helper, impact, decoration, 2);
            try {
                brew.applySplashToBlock(level, impact, Direction.UP, Vec3.atCenterOf(impact), null);
                helper.assertTrue(level.isEmptyBlock(dirt) && level.getBlockState(farm).is(Blocks.WHEAT),
                        "The dropped " + decoration + " must not displace seeds in the original planting queue");
                helper.assertTrue(seed.isRemoved() && plant.getItem().getCount() == 2,
                        "Planting consumes the wheat seed and leaves the decorative stack intact");
            } finally {
                seed.discard();
                plant.discard();
                level.removeBlock(dirt, false);
                level.removeBlock(farm, false);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void plantingKeepsOriginalSeedOrderAndSoils(GameTestHelper helper) {
        var level = helper.getLevel();
        var impact = helper.absolutePos(new BlockPos(6, 5, 5)).atY(196);
        var first = impact.west(2);
        var second = impact.west();
        for (var pos : List.of(first, second)) {
            level.setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
        }
        var brew = plantingBrew();
        var wheat = spawnLooseItem(helper, impact, Items.WHEAT_SEEDS, 2);
        var potato = spawnLooseItem(helper, impact, Items.POTATO, 1);
        var distant = spawnLooseItem(helper, impact.offset(2, 0, 2), Items.WHEAT_SEEDS, 3);
        try {
            brew.applySplashToBlock(level, impact, Direction.UP, Vec3.atCenterOf(impact), null);
            helper.assertTrue(level.getBlockState(first).is(Blocks.POTATOES) && level.getBlockState(second).is(Blocks.WHEAT),
                    "The original scan uses the last in-range plantable stack first");
            helper.assertTrue(potato.isRemoved() && wheat.getItem().getCount() == 1 && distant.getItem().getCount() == 3,
                    "Each planting consumes one seed and the corner stack stays outside the sphere");

            level.removeBlock(first, false);
            level.setBlockAndUpdate(first.below(), Blocks.SOUL_SAND.defaultBlockState());
            var wart = spawnLooseItem(helper, impact, Items.NETHER_WART, 2);
            try {
                brew.applySplashToBlock(level, impact, Direction.UP, Vec3.atCenterOf(impact), null);
                helper.assertTrue(level.getBlockState(first).is(Blocks.NETHER_WART) && wart.getItem().getCount() == 1,
                        "Nether Wart still uses Soul Sand and consumes one Wart");
            } finally {
                wart.discard();
            }

            level.removeBlock(first, false);
            level.removeBlock(second, false);
            level.setBlockAndUpdate(first.below(), Blocks.WATER.defaultBlockState());
            var artichoke = spawnLooseItem(helper, impact, WitchCrop.ARTICHOKE.seed(), 2);
            try {
                brew.applySplashToBlock(level, impact, Direction.UP, Vec3.atCenterOf(impact), null);
                helper.assertTrue(level.isEmptyBlock(first) && level.isEmptyBlock(second)
                                && artichoke.getItem().getCount() == 2 && wheat.getItem().getCount() == 1,
                        "The original Artichoke seed stays queued and Planting only sows from solid surfaces");
            } finally {
                artichoke.discard();
                level.removeBlock(first.below(), false);
            }
        } finally {
            wheat.discard();
            potato.discard();
            distant.discard();
        }
        helper.succeed();
    }

    private static BrewMixture plantingBrew() {
        return BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(), Items.GUNPOWDER.getDefaultInstance(),
                Items.WHEAT_SEEDS.getDefaultInstance()));
    }

    private static ItemEntity spawnLooseItem(GameTestHelper helper, BlockPos pos, Item item, int count) {
        var level = helper.getLevel();
        var center = Vec3.atCenterOf(pos);
        var drop = new ItemEntity(level, center.x, center.y, center.z, new ItemStack(item, count));
        level.addFreshEntity(drop);
        return drop;
    }

    @GameTest(template = "qa_coven_arena")
    public static void fertilizationAdvancesMindrakeOneStagePerVisit(GameTestHelper helper) {
        var level = helper.getLevel();
        var impact = helper.absolutePos(new BlockPos(6, 5, 5)).atY(196);
        var middle = impact.west(2);
        var fringe = middle.north();
        var wolfsbane = fringe.east();
        for (var pos : List.of(middle, fringe, wolfsbane)) {
            level.setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
        }
        var mindrake = WitchCrop.MINDRAKE.block().defaultBlockState();
        level.setBlockAndUpdate(middle, mindrake);
        level.setBlockAndUpdate(fringe, mindrake);
        level.setBlockAndUpdate(wolfsbane, WitchCrop.WOLFSBANE.block().defaultBlockState());

        var brew = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(), Items.GUNPOWDER.getDefaultInstance(),
                Items.BONE_MEAL.getDefaultInstance()));
        var brewer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "CropMealTester"));
        try {
            brew.applySplashToBlock(level, impact, Direction.UP, Vec3.atCenterOf(impact), brewer);
            helper.assertTrue(level.getBlockState(middle).getValue(CropBlock.AGE) == 2
                            && level.getBlockState(fringe).getValue(CropBlock.AGE) == 1
                            && level.getBlockState(wolfsbane).getValue(CropBlock.AGE) == 1,
                    "Mindrake and Wolfsbane each grow one stage per original column visit");

            level.setBlockAndUpdate(fringe, mindrake);
            helper.assertTrue(BoneMealItem.applyBonemeal(Items.BONE_MEAL.getDefaultInstance(), level, fringe, brewer)
                            && level.getBlockState(fringe).getValue(CropBlock.AGE) == 1,
                    "Using bone meal directly also advances Mindrake by one stage");
        } finally {
            brewer.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void flowersRollOnlyAfterBreakPermission(GameTestHelper helper) {
        var level = helper.getLevel();
        var impact = helper.absolutePos(new BlockPos(6, 5, 5)).atY(196);
        var first = impact.west(2);
        var second = impact.west();
        for (var pos : List.of(first, second)) {
            level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        }
        var brewer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "FlowersBrewTester"));
        var brew = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(), Items.GUNPOWDER.getDefaultInstance(),
                Items.DANDELION.getDefaultInstance()));
        Consumer<BlockEvent.BreakEvent> protection = event -> {
            if (event.getLevel() == level && event.getPlayer() == brewer && event.getPos().equals(first)) {
                event.setCanceled(true);
            }
        };
        Consumer<BlockEvent.EntityPlaceEvent> placement = event -> {
            if (event.getLevel() == level && event.getEntity() == brewer && event.getPos().equals(second)) {
                event.setCanceled(true);
            }
        };
        var config = WitcheryServerConfig.ALLOW_INTER_MOD_BLOCK_BREAK_EVENTS;
        boolean previous = config.get();
        try {
            config.set(true);
            level.setBlockAndUpdate(first, Blocks.GRASS.defaultBlockState());
            level.random.setSeed(4096);
            brew.applySplashToBlock(level, impact, Direction.UP, Vec3.atCenterOf(impact), brewer);
            helper.assertTrue(level.getBlockState(first).is(Blocks.OXEYE_DAISY) && level.isEmptyBlock(second),
                    "The first supported cell gets the original Oxeye Daisy roll when its grass can be replaced");

            level.setBlockAndUpdate(first, Blocks.GRASS.defaultBlockState());
            MinecraftForge.EVENT_BUS.addListener(protection);
            level.random.setSeed(4096);
            brew.applySplashToBlock(level, impact, Direction.UP, Vec3.atCenterOf(impact), brewer);
            helper.assertTrue(level.getBlockState(first).is(Blocks.GRASS)
                            && level.getBlockState(second).is(Blocks.OXEYE_DAISY),
                    "Protected grass stays and the next supported cell gets the first roll");

            level.removeBlock(second, false);
            MinecraftForge.EVENT_BUS.addListener(placement);
            level.random.setSeed(4096);
            brew.applySplashToBlock(level, impact, Direction.UP, Vec3.atCenterOf(impact), brewer);
            helper.assertTrue(level.getBlockState(first).is(Blocks.GRASS) && level.isEmptyBlock(second),
                    "Forge can still cancel the flower placement after a successful roll");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            MinecraftForge.EVENT_BUS.unregister(placement);
            config.set(previous);
            brewer.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void flowersPreserveTallPlantsAndFullSnow(GameTestHelper helper) {
        var level = helper.getLevel();
        var impact = helper.absolutePos(new BlockPos(6, 5, 5)).atY(196);
        var first = impact.west(2);
        var second = impact.west();
        for (var pos : List.of(first, second)) {
            level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        }
        var brew = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(), Items.GUNPOWDER.getDefaultInstance(),
                Items.DANDELION.getDefaultInstance()));
        for (var plant : List.of(Blocks.TALL_GRASS, Blocks.LARGE_FERN)) {
            DoublePlantBlock.placeAt(level, plant.defaultBlockState(), first, Block.UPDATE_ALL);
            level.removeBlock(second, false);
            level.random.setSeed(4096);
            brew.applySplashToBlock(level, impact, Direction.UP, Vec3.atCenterOf(impact), null);
            helper.assertTrue(level.getBlockState(first).is(plant) && level.getBlockState(first.above()).is(plant)
                            && level.getBlockState(second).is(Blocks.OXEYE_DAISY),
                    "Both halves stay in place, and the next eligible cell gets the first roll for " + plant);
            level.removeBlock(first, false);
        }
        for (int layers = 1; layers <= 8; layers++) {
            var snow = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
            level.setBlockAndUpdate(first, snow);
            level.removeBlock(second, false);
            level.random.setSeed(4096);
            brew.applySplashToBlock(level, impact, Direction.UP, Vec3.atCenterOf(impact), null);
            if (layers < 8) {
                helper.assertTrue(level.getBlockState(first).is(Blocks.OXEYE_DAISY) && level.isEmptyBlock(second),
                        "Flowers replace the original partial snow depth of " + layers);
            } else {
                helper.assertTrue(level.getBlockState(first).equals(snow) && level.getBlockState(second).is(Blocks.OXEYE_DAISY),
                        "The full snow layer stays and leaves the first roll for the next cell");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void harvestingKeepsBothHalvesOfProtectedPlants(GameTestHelper helper) {
        var level = helper.getLevel();
        var impact = helper.absolutePos(new BlockPos(6, 5, 5));
        var upperOnly = impact.west(2).below(2);
        var wholeFlower = impact.south(2);
        var protectedLower = impact.east(2).below(2);
        var protectedUpper = impact.north(2);
        var tallGrass = impact.offset(1, 0, -1);
        var tallFern = impact.offset(-1, 0, -1);
        for (var pos : List.of(upperOnly, wholeFlower, protectedLower, protectedUpper)) {
            level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
            DoublePlantBlock.placeAt(level, Blocks.SUNFLOWER.defaultBlockState(), pos, Block.UPDATE_ALL);
        }
        level.setBlockAndUpdate(tallGrass.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(tallFern.below(), Blocks.DIRT.defaultBlockState());
        DoublePlantBlock.placeAt(level, Blocks.TALL_GRASS.defaultBlockState(), tallGrass, Block.UPDATE_ALL);
        DoublePlantBlock.placeAt(level, Blocks.LARGE_FERN.defaultBlockState(), tallFern, Block.UPDATE_ALL);

        var brewer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "HarvestBrewTester"));
        var blocked = List.of(protectedLower, protectedUpper.above());
        var vetoed = new ArrayList<BlockPos>();
        Consumer<BlockEvent.BreakEvent> protection = event -> {
            if (event.getLevel() == level && event.getPlayer() == brewer && blocked.contains(event.getPos())) {
                vetoed.add(event.getPos().immutable());
                event.setCanceled(true);
            }
        };
        var config = WitcheryServerConfig.ALLOW_INTER_MOD_BLOCK_BREAK_EVENTS;
        boolean previous = config.get();
        MinecraftForge.EVENT_BUS.addListener(protection);
        try {
            config.set(true);
            var brew = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(), Items.GUNPOWDER.getDefaultInstance(),
                    Items.APPLE.getDefaultInstance()));
            brew.applySplashToBlock(level, impact, Direction.UP, Vec3.atCenterOf(impact), brewer);
            for (var pos : List.of(upperOnly, wholeFlower, tallGrass, tallFern)) {
                helper.assertTrue(level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above()),
                        "Harvesting uproots both halves of the accepted plant at " + pos);
            }
            for (var pos : List.of(protectedLower, protectedUpper)) {
                helper.assertTrue(level.getBlockState(pos).is(Blocks.SUNFLOWER)
                                && level.getBlockState(pos.above()).is(Blocks.SUNFLOWER),
                        "A veto on either half keeps the full sunflower in place");
            }
            helper.assertTrue(vetoed.containsAll(blocked), "The cast checks protected halves outside its scan and within it");
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(impact).inflate(4));
            helper.assertTrue(drops.size() == 2 && drops.stream().allMatch(item -> item.getItem().is(Items.SUNFLOWER)
                            && item.getItem().getCount() == 1),
                    "Only the two unprotected flowers produce drops");
            helper.assertTrue(drops.stream().anyMatch(item -> item.blockPosition().equals(upperOnly))
                            && drops.stream().anyMatch(item -> item.position().equals(Vec3.atCenterOf(wholeFlower))),
                    "Upper-only harvesting drops the flower at its base, and a direct cut drops at the cell center");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            config.set(previous);
            brewer.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_felling_ent", setupTicks = 5)
    public static void fellingAwakensEntsOnlyFromHarvestedWitchwood(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(6, 4, 5)).atY(196);
        var landing = center.offset(-11, 0, -8);
        var protectedLog = center.west(2).below();
        var edgeLog = center.east(3);
        var outsideLog = center.east(4);
        var witchLog = WitcheryBlocks.getRequired("witchlog").get();
        var hawthorn = witchLog.defaultBlockState().setValue(WitchWoodType.PROPERTY, WitchWoodType.HAWTHORN);
        level.setBlockAndUpdate(center, hawthorn);
        level.setBlockAndUpdate(protectedLog, hawthorn);
        level.setBlockAndUpdate(edgeLog, Blocks.OAK_WOOD.defaultBlockState());
        level.setBlockAndUpdate(outsideLog, Blocks.OAK_LOG.defaultBlockState());
        level.setBlockAndUpdate(center.north(), Blocks.OAK_PLANKS.defaultBlockState());
        level.setBlockAndUpdate(center.south(), Blocks.CRIMSON_STEM.defaultBlockState());
        level.setBlockAndUpdate(center.west(), Blocks.STRIPPED_WARPED_HYPHAE.defaultBlockState());
        level.setBlockAndUpdate(landing.below(), Blocks.STONE.defaultBlockState());

        var brewer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "FellingTester"));
        Consumer<BlockEvent.BreakEvent> protection = event -> {
            if (event.getLevel() == level && event.getPlayer() == brewer && event.getPos().equals(protectedLog)) {
                event.setCanceled(true);
            }
        };
        var config = WitcheryServerConfig.ALLOW_INTER_MOD_BLOCK_BREAK_EVENTS;
        boolean previous = config.get();
        MinecraftForge.EVENT_BUS.addListener(protection);
        try {
            config.set(true);
            var brew = BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER),
                    new ItemStack(Items.STRING)));
            level.random.setSeed(5123);
            brew.applySplashToBlock(level, center, Direction.UP, Vec3.atCenterOf(center), brewer);
            helper.assertTrue(level.isEmptyBlock(center) && level.isEmptyBlock(edgeLog)
                            && level.getBlockState(outsideLog).is(Blocks.OAK_LOG),
                    "Felling harvests both wooden log forms inside its inclusive sphere");
            helper.assertTrue(level.getBlockState(protectedLog).equals(hawthorn)
                            && level.getBlockState(center.north()).is(Blocks.OAK_PLANKS)
                            && level.getBlockState(center.south()).is(Blocks.CRIMSON_STEM)
                            && level.getBlockState(center.west()).is(Blocks.STRIPPED_WARPED_HYPHAE),
                    "Protected Witchwood, planks and fungal trunks stay in place");
            var ent = level.getEntities(WitcheryEntities.ENT.get(), new AABB(landing), entity -> true);
            helper.assertTrue(ent.size() == 1 && ent.get(0).blockPosition().equals(landing),
                    "The accepted log harvest rolls one Ent into its original landing column");
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(protectedLog).inflate(4),
                    entity -> entity.getItem().is(witchLog.asItem()));
            helper.assertTrue(drops.size() == 1 && ItemStack.isSameItemSameTags(drops.get(0).getItem(),
                            WitchWoodType.HAWTHORN.stack(witchLog)),
                    "The cast drops one Hawthorn log and leaves the protected log in place");
            ent.get(0).discard();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            config.set(previous);
            brewer.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void extinguishPreservesCastOrderAndBlazeDamageSource(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(6, 4, 5));
        var westLow = center.west(2).below(2);
        var westHigh = center.west(2).above();
        var south = center.south(2);
        var north = center.north(2);
        var corner = center.offset(2, 0, 2);
        var ceiling = center.east().above(4);
        var cleared = List.of(westLow, westHigh, south, north);
        for (var position : List.of(westLow, westHigh, south, north, center, corner, ceiling)) {
            level.setBlockAndUpdate(position.below(), Blocks.NETHERRACK.defaultBlockState());
            level.setBlockAndUpdate(position, Blocks.FIRE.defaultBlockState());
        }

        var brewer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ExtinguishTester"));
        var blaze = EntityType.BLAZE.create(level);
        float health = blaze.getHealth();
        var fizz = new ArrayList<Vec3>();
        var attempts = new ArrayList<BlockPos>();
        var damage = new ArrayList<DamageSource>();
        Consumer<BlockEvent.BreakEvent> protection = event -> {
            if (event.getLevel() == level && event.getPlayer() == brewer && event.getPos().equals(center)) {
                attempts.add(event.getPos().immutable());
                event.setCanceled(true);
            }
        };
        Consumer<PlayLevelSoundEvent.AtPosition> sound = event -> {
            if (event.getLevel() == level && event.getSound() != null && event.getSound().value() == SoundEvents.FIRE_EXTINGUISH
                    && event.getNewVolume() == 1 && event.getNewPitch() == 2) {
                fizz.add(event.getPosition());
            }
        };
        Consumer<LivingHurtEvent> hurt = event -> {
            if (event.getEntity() == blaze) damage.add(event.getSource());
        };
        var config = WitcheryServerConfig.ALLOW_INTER_MOD_BLOCK_BREAK_EVENTS;
        boolean previous = config.get();
        MinecraftForge.EVENT_BUS.addListener(protection);
        MinecraftForge.EVENT_BUS.addListener(sound);
        MinecraftForge.EVENT_BUS.addListener(hurt);
        try {
            config.set(true);
            var brew = BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER),
                    new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(Items.COAL)));
            brew.applySplashToBlock(level, center, Direction.UP, Vec3.atCenterOf(center), brewer);
            helper.assertTrue(cleared.stream().allMatch(level::isEmptyBlock)
                            && List.of(center, corner, ceiling).stream().allMatch(pos -> level.getBlockState(pos).is(Blocks.FIRE)),
                    "The splash clears the filled columns and preserves the protected fire, outer corner and vertical fringe");
            helper.assertTrue(attempts.equals(List.of(center, center)), "Both center-row visits honor the break veto");
            helper.assertTrue(fizz.equals(cleared.stream().map(Vec3::atLowerCornerOf).toList()),
                    "Fizz follows the original column and height order at the lower cell corners");

            brew.applySplash(blaze, brewer, brewer, 1, false);
            helper.assertTrue(blaze.getHealth() == health - 8 && damage.size() == 1
                            && damage.get(0).is(DamageTypes.INDIRECT_MAGIC)
                            && damage.get(0).getDirectEntity() == brewer && damage.get(0).getEntity() == brewer,
                    "Strength-one Extinguish uses indirect magic from its caster for eight Blaze damage");
            brew.applyToBlock(level, center, Direction.UP, 3, true, brewer);
            helper.assertTrue(level.isEmptyBlock(corner) && level.getBlockState(ceiling).is(Blocks.FIRE),
                    "A ritual extends the horizontal circle and preserves the splash's vertical bounds");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            MinecraftForge.EVENT_BUS.unregister(sound);
            MinecraftForge.EVENT_BUS.unregister(hurt);
            config.set(previous);
            blaze.discard();
            brewer.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void lilifyPlantsOnLegacyWaterAndKeepsOnlySourcePads(GameTestHelper helper) {
        var level = helper.getLevel();
        var lily = WitcheryBlocks.getRequired("lilypad").get();
        var recipe = BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER),
                new ItemStack(Items.LILY_PAD)));
        helper.assertTrue(recipe != null, "The original Lilify recipe is valid");
        var source = helper.absolutePos(new BlockPos(4, 5, 3));
        for (var fluid : List.of(Blocks.WATER, WitcheryBlocks.getRequired("hollowtears").get(),
                WitcheryBlocks.getRequired("spiritflowing").get(), WitcheryBlocks.getRequired("brewliquid").get())) {
            var state = fluid.defaultBlockState();
            var flowing = source.east(3);
            level.setBlockAndUpdate(source, state);
            level.setBlockAndUpdate(flowing, state.setValue(LiquidBlock.LEVEL, 4));
            for (BlockPos support : List.of(source, flowing)) {
                var pad = support.above();
                var impact = support.west().below();
                recipe.applySplashToBlock(level, impact, Direction.EAST, Vec3.atCenterOf(impact), null);
                var planted = level.getBlockState(pad);
                helper.assertTrue(planted.is(lily) && level.getBlockTicks().hasScheduledTick(pad, lily),
                        "Lilify finds and plants above either depth of " + fluid);
                planted.tick(level, pad, RandomSource.create(1));
                helper.assertTrue(support.equals(source) ? level.getBlockState(pad).is(lily) : level.isEmptyBlock(pad),
                        "A growth tick only keeps a pad over the source of " + fluid);
            }
            level.setBlockAndUpdate(source, state.setValue(LiquidBlock.LEVEL, 4));
            helper.assertTrue(level.isEmptyBlock(source.above()), "An established pad breaks when " + fluid + " starts flowing");
            source = source.east(5);
        }
        source = helper.absolutePos(new BlockPos(4, 5, 8));
        for (var invalid : List.of(Blocks.ICE, WitcheryBlocks.getRequired("brewgas").get())) {
            level.setBlockAndUpdate(source, invalid.defaultBlockState());
            recipe.applySplashToBlock(level, source.west(), Direction.EAST, Vec3.atCenterOf(source), null);
            helper.assertTrue(level.isEmptyBlock(source.above()), "Lilify skips " + invalid);
            source = source.east(3);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void lilifySpreadsBeforeCheckingSupportAndHonorsPlacementVeto(GameTestHelper helper) {
        var level = helper.getLevel();
        var parent = helper.absolutePos(new BlockPos(4, 6, 5));
        var child = parent.east();
        var lily = WitcheryBlocks.getRequired("lilypad").get();
        var mature = lily.defaultBlockState().setValue(WitchLilyPadBlock.GROWTH, 1);
        var water = Blocks.WATER.defaultBlockState();
        level.setBlockAndUpdate(parent.below(), water.setValue(LiquidBlock.LEVEL, 4));
        level.setBlockAndUpdate(child.below(), WitcheryBlocks.getRequired("hollowtears").get().defaultBlockState());
        BrewTerrainEffects.lilify(level, parent.below().west(), Direction.EAST, 1, null);
        helper.assertTrue(level.getBlockState(parent).equals(mature), "The flowing surface receives the parent with its original growth");
        // This seed chooses the east neighbor from the original weighted directions.
        mature.tick(level, parent, RandomSource.create(1));
        helper.assertTrue(level.isEmptyBlock(parent) && level.getBlockState(child).equals(lily.defaultBlockState()),
                "The unsupported parent spreads onto neighboring Hollow Tears before falling");

        level.setBlockAndUpdate(child, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(parent.below(), water);
        BrewTerrainEffects.lilify(level, parent.below().west(), Direction.EAST, 1, null);
        Consumer<BlockEvent.EntityPlaceEvent> protection = event -> {
            if (event.getLevel() == level && event.getPos().equals(child) && event.getPlacedBlock().is(lily)) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(protection);
        try {
            mature.tick(level, parent, RandomSource.create(1));
            helper.assertTrue(level.getBlockState(parent).equals(mature) && level.getBlockState(child).is(Blocks.STONE),
                    "A veto restores the occupied surface and leaves the parent's growth");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
        }
        mature.tick(level, parent, RandomSource.create(1));
        helper.assertTrue(level.getBlockState(parent).equals(lily.defaultBlockState())
                        && level.getBlockState(child).equals(lily.defaultBlockState()),
                "The original spread replaces the occupied source surface and leaves both pads at growth zero");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void pruningKeepsTheOriginalPlantSelection(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(7, 4, 5));
        var grass = center.east();
        var fern = center.west();
        var tallGrass = center.north();
        var seagrass = center.south().east();
        var deadBush = center.south();
        var vine = center.west().south();
        var moss = center.west().north();
        var brewVine = center.east().north();
        var boundaryLeaf = center.above(2);
        var outerLeaf = center.east(3);
        var crop = center.below();

        for (BlockPos pos : List.of(grass, fern, tallGrass, seagrass)) {
            level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        }
        level.setBlockAndUpdate(deadBush.below(), Blocks.SAND.defaultBlockState());
        level.setBlockAndUpdate(crop.below(), Blocks.FARMLAND.defaultBlockState());
        for (BlockPos pos : List.of(vine, moss)) {
            level.setBlockAndUpdate(pos.relative(Direction.WEST), Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(brewVine.east(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(grass, Blocks.GRASS.defaultBlockState());
        level.setBlockAndUpdate(fern, Blocks.FERN.defaultBlockState());
        level.setBlockAndUpdate(deadBush, Blocks.DEAD_BUSH.defaultBlockState());
        level.setBlockAndUpdate(tallGrass, Blocks.TALL_GRASS.defaultBlockState());
        level.setBlockAndUpdate(tallGrass.above(), Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        level.setBlockAndUpdate(seagrass, Blocks.SEAGRASS.defaultBlockState());
        level.setBlockAndUpdate(crop, Blocks.WHEAT.defaultBlockState());
        level.setBlockAndUpdate(vine, Blocks.VINE.defaultBlockState().setValue(VineBlock.WEST, true));
        var spanishMoss = WitcheryBlocks.getRequired("spanishmoss").get();
        level.setBlockAndUpdate(moss, spanishMoss.defaultBlockState().setValue(VineBlock.WEST, true));
        var witchVine = WitcheryBlocks.getRequired("vine").get();
        level.setBlockAndUpdate(brewVine, witchVine.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST));
        var witchLeaves = WitcheryBlocks.getRequired("witchleaves").get();
        var leaves = witchLeaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        level.setBlockAndUpdate(boundaryLeaf, leaves);
        level.setBlockAndUpdate(outerLeaf, leaves);

        pruningBrew().applySplashToBlock(level, center, Direction.UP, Vec3.atCenterOf(center), null);
        helper.assertTrue(List.of(grass, fern, deadBush, vine, moss, boundaryLeaf).stream().allMatch(level::isEmptyBlock),
                "Pruning clears native vine materials and leaf blocks up to the inclusive sphere boundary");
        helper.assertTrue(level.getBlockState(tallGrass).is(Blocks.TALL_GRASS)
                        && level.getBlockState(tallGrass.above()).is(Blocks.TALL_GRASS)
                        && level.getBlockState(seagrass).is(Blocks.SEAGRASS) && level.getBlockState(crop).is(Blocks.WHEAT)
                        && level.getBlockState(brewVine).is(witchVine) && level.getBlockState(outerLeaf).is(witchLeaves),
                "Two-block grass, aquatic plants, crops, conjured vines and the outer leaf survive");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void pruningReturnsTheSavedDreamWeaveAndHonorsProtection(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(7, 4, 5));
        var glyph = center.east();
        var catcher = WitcheryBlocks.getRequired("dreamcatcher").get();
        var heart = WitcheryBlocks.getRequired("circle").get();
        level.setBlockAndUpdate(center.south(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(center, catcher.defaultBlockState());
        level.setBlockAndUpdate(glyph.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(glyph, heart.defaultBlockState());
        var legacy = new CompoundTag();
        legacy.putInt("WITCWeaveID", DreamWeave.NIGHTMARES.ordinal());
        level.getBlockEntity(center).load(legacy);
        var brewer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "PruningTester"));
        var brew = pruningBrew();
        var drops = new AABB(center).inflate(0.5);
        var config = WitcheryServerConfig.ALLOW_INTER_MOD_BLOCK_BREAK_EVENTS;
        boolean previous = config.get();
        Consumer<BlockEvent.BreakEvent> veto = event -> {
            if (event.getLevel() == level && event.getPlayer() == brewer && event.getPos().equals(center)) event.setCanceled(true);
        };
        try {
            config.set(true);
            MinecraftForge.EVENT_BUS.addListener(veto);
            try {
                brew.applySplashToBlock(level, center, Direction.UP, Vec3.atCenterOf(center), brewer);
                helper.assertTrue(level.getBlockState(center).is(catcher)
                                && level.getBlockEntity(center).saveWithoutMetadata().getInt("WITCWeaveID") == DreamWeave.NIGHTMARES.ordinal()
                                && level.getEntitiesOfClass(ItemEntity.class, drops).isEmpty(),
                        "A Forge veto preserves the fitted weave and produces no drop");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(veto);
            }
            brew.applySplashToBlock(level, center, Direction.UP, Vec3.atCenterOf(center), brewer);
            var result = level.getEntitiesOfClass(ItemEntity.class, drops);
            helper.assertTrue(level.isEmptyBlock(center) && level.getBlockState(glyph).is(heart)
                            && result.size() == 1 && result.get(0).getItem().is(DreamWeave.NIGHTMARES.item())
                            && result.get(0).getItem().getCount() == 1 && result.get(0).position().equals(Vec3.atLowerCornerOf(center)),
                    "Pruning releases the original saved weave at its block corner and preserves the Heart Glyph");
        } finally {
            config.set(previous);
            brewer.discard();
        }
        helper.succeed();
    }

    private static BrewMixture pruningBrew() {
        return BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART),
                new ItemStack(Items.GUNPOWDER), new ItemStack(Items.BROWN_MUSHROOM)));
    }

    @GameTest(template = "qa_coven_arena")
    public static void pulverisationRestoresNativeOrderAndStoneVariants(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(5, 5, 5));
        var stone = center.offset(-1, 0, -1);
        var granite = center.offset(-1, -1, 0);
        var cobble = center.offset(0, 1, -1);
        var gravel = center.offset(0, -1, 1);
        var sandstone = center.offset(1, 0, -1);
        var redSand = center.offset(1, 1, 0);
        var fringe = center.offset(1, 1, 1);
        level.setBlockAndUpdate(stone, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(granite, Blocks.GRANITE.defaultBlockState());
        level.setBlockAndUpdate(cobble, Blocks.COBBLESTONE.defaultBlockState());
        level.setBlockAndUpdate(gravel, Blocks.GRAVEL.defaultBlockState());
        level.setBlockAndUpdate(sandstone, Blocks.CUT_SANDSTONE.defaultBlockState());
        level.setBlockAndUpdate(redSand, Blocks.RED_SAND.defaultBlockState());
        level.setBlockAndUpdate(fringe, Blocks.POLISHED_GRANITE.defaultBlockState());
        level.setBlockAndUpdate(center, Blocks.DIORITE.defaultBlockState());
        var brewer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "PulverisationTester"));
        var recipe = BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART),
                new ItemStack(Items.GUNPOWDER), new ItemStack(Items.FLINT)));

        List<BlockPos> expected = List.of(stone, granite, cobble, center, gravel, sandstone, redSand);
        List<BlockPos> visited = new ArrayList<>();
        List<PlayLevelSoundEvent.AtPosition> sounds = new ArrayList<>();
        Consumer<BlockEvent.BreakEvent> protection = event -> {
            if (event.getLevel() == level && event.getPlayer() == brewer
                    && (expected.contains(event.getPos()) || event.getPos().equals(fringe))) {
                visited.add(event.getPos());
                if (event.getPos().equals(center)) event.setCanceled(true);
            }
        };
        Consumer<PlayLevelSoundEvent.AtPosition> sound = event -> {
            if (event.getLevel() == level && event.getSound() != null && event.getSound().value() == SoundEvents.GHAST_SHOOT) {
                sounds.add(event);
            }
        };
        var config = WitcheryServerConfig.ALLOW_INTER_MOD_BLOCK_BREAK_EVENTS;
        boolean previous = config.get();
        MinecraftForge.EVENT_BUS.addListener(protection);
        MinecraftForge.EVENT_BUS.addListener(sound);
        try {
            config.set(true);
            recipe.applySplashToBlock(level, center, Direction.UP, Vec3.atCenterOf(center), brewer);
            helper.assertTrue(visited.equals(expected), "The open sphere visits blocks once in original X, Z, Y order");
            helper.assertTrue(level.getBlockState(stone).is(Blocks.COBBLESTONE)
                            && level.getBlockState(granite).is(Blocks.COBBLESTONE)
                            && level.getBlockState(cobble).is(Blocks.GRAVEL)
                            && level.getBlockState(gravel).is(Blocks.SAND)
                            && level.getBlockState(sandstone).is(Blocks.SAND),
                    "Each stone, cobblestone, gravel and sandstone advances by exactly one stage");
            helper.assertTrue(level.getBlockState(fringe).is(Blocks.POLISHED_GRANITE)
                            && level.getBlockState(center).is(Blocks.DIORITE),
                    "The open fringe and protected center keep their stone");
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(redSand).inflate(0.5));
            helper.assertTrue(level.isEmptyBlock(redSand) && drops.size() == 1 && drops.get(0).getItem().is(Items.SAND)
                            && drops.get(0).getItem().getCount() == 1 && drops.get(0).position().equals(Vec3.atLowerCornerOf(redSand)),
                    "Red sand yields one ordinary sand item at its original coordinates");
            helper.assertTrue(sounds.size() == 1 && sounds.get(0).getPosition().equals(Vec3.atLowerCornerOf(center))
                            && sounds.get(0).getNewVolume() == 0.5F && sounds.get(0).getNewPitch() == 2,
                    "The ghast cue plays once from the impact corner");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            MinecraftForge.EVENT_BUS.unregister(sound);
            config.set(previous);
            brewer.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void terrainBrewHonorsConfigurableBlockBreakVeto(GameTestHelper helper) {
        var level = helper.getLevel();
        var target = helper.absolutePos(new BlockPos(4, 4, 4));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrewTerrainTester"));
        var config = WitcheryServerConfig.ALLOW_INTER_MOD_BLOCK_BREAK_EVENTS;
        boolean previous = config.get();
        Consumer<BlockEvent.BreakEvent> protection = event -> {
            if (event.getLevel() == level && event.getPlayer() == player && event.getPos().equals(target)) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(protection);
        try {
            var air = Blocks.AIR.defaultBlockState();
            level.setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
            config.set(config.getDefault());
            helper.assertTrue(!BrewTerrain.setBlock(player, level, target, air, Direction.UP)
                            && level.getBlockState(target).is(Blocks.STONE),
                    "Terrain brews respect the block break veto by default");

            config.set(false);
            helper.assertTrue(BrewTerrain.setBlock(player, level, target, air, Direction.UP) && level.isEmptyBlock(target),
                    "Disabling the event allows the brew to remove the stone");
            level.setBlockAndUpdate(target, Blocks.BEDROCK.defaultBlockState());
            helper.assertTrue(!BrewTerrain.setBlock(player, level, target, air, Direction.UP),
                    "Bedrock stays protected with the event disabled");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            config.set(previous);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", timeoutTicks = 140)
    public static void dispersalIngredientsRemoveEarlierWheat(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 4, 4));
        level.setBlockAndUpdate(pos.below(2), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(pos.below(), Blocks.FIRE.defaultBlockState());
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var cauldron = (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
        var tank = cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        tank.fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE);

        helper.runAfterDelay(105, () -> {
            drop(helper, pos, Items.NETHER_WART);
            drop(helper, pos, WitcheryIngredient.WOLFSBANE.item());
            var dispersal = List.of(Items.GUNPOWDER, WitcheryIngredient.GLOBE_ARTICHOKE.item(),
                    WitcheryIngredient.BAT_WOOL.item(), WitcheryIngredient.WORMWOOD.item(), Items.ZOMBIE_HEAD);
            var deliveries = List.of(BrewMixture.Delivery.SPLASH, BrewMixture.Delivery.SPLASH,
                    BrewMixture.Delivery.GAS, BrewMixture.Delivery.LIQUID, BrewMixture.Delivery.TRIGGERED);
            for (int index = 0; index < dispersal.size(); index++) {
                drop(helper, pos, Items.WHEAT);
                var withWheat = BrewMixture.from(tank.getFluidInTank(0).getTag());
                helper.assertTrue(withWheat != null && withWheat.effects().stream()
                                .anyMatch(effect -> effect.effect() == WitcheryEffects.FEEL_NO_PAIN.get()),
                        "Wheat added after the previous dispersal still gives Moonshine");

                drop(helper, pos, dispersal.get(index));
                cauldron.load(cauldron.saveWithFullMetadata());
                var converted = BrewMixture.from(tank.getFluidInTank(0).getTag());
                helper.assertTrue(converted != null && converted.delivery() == deliveries.get(index)
                                && converted.used() == 1 && converted.effects().size() == 1
                                && converted.effects().get(0).effect() == WitcheryEffects.WOLFSBANE.get(),
                        dispersal.get(index) + " removes the earlier wheat while keeping Wolfsbane after reload");
            }
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void diseaseBrewHonorsLegacyCreatureImmunity(GameTestHelper helper) {
        var pig = helper.spawn(EntityType.PIG, new BlockPos(3, 3, 3));
        var zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 3, 5));
        var golem = helper.spawn(EntityType.IRON_GOLEM, new BlockPos(5, 3, 3));
        var demon = helper.spawn(WitcheryEntities.DEMON.get(), new BlockPos(5, 3, 5));
        var werevillager = helper.spawn(WitcheryEntities.WERE_VILLAGER.get(), new BlockPos(7, 3, 3));
        var reflection = helper.spawn(WitcheryEntities.REFLECTION.get(), new BlockPos(7, 3, 5));
        var recipe = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.DIAMOND), new ItemStack(Items.ROTTEN_FLESH));

        drink(pig, recipe);
        var disease = WitcheryEffects.DISEASED.get();
        helper.assertTrue(pig.hasEffect(disease), "The ordinary brew still infects a susceptible creature");
        for (var immune : List.of(zombie, golem, demon, werevillager, reflection)) {
            drink(immune, recipe);
            helper.assertTrue(!immune.hasEffect(disease), "The ordinary brew respects immunity for " + immune.getType());
        }

        drink(golem, List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.FERMENTED_SPIDER_EYE), new ItemStack(Items.ROTTEN_FLESH)));
        helper.assertTrue(golem.hasEffect(disease), "Inverting the recipe bypasses the brew's creature immunity check");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void mixedSplashKeepsSeparateTerrainDuration(GameTestHelper helper) {
        var level = helper.getLevel();
        var impact = helper.absolutePos(new BlockPos(5, 4, 5));
        var water = impact.west(2);
        var lava = impact.east(2);
        level.setBlockAndUpdate(impact, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(lava, Blocks.LAVA.defaultBlockState());
        var pig = helper.spawn(EntityType.PIG, new BlockPos(5, 5, 6));
        pig.setNoAi(true);
        pig.setNoGravity(true);

        var recipe = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(WitcheryIngredient.DIAMOND_VAPOUR.item()),
                new ItemStack(Items.GUNPOWDER), new ItemStack(Items.REDSTONE),
                new ItemStack(WitcheryIngredient.WOLFSBANE.item()), new ItemStack(Items.SAND),
                new ItemStack(Items.COBBLESTONE));
        var bottle = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        bottle.setTag(BrewMixture.ingredientTag(recipe, 0x99afae));
        var projectile = new ThrownBrew(WitcheryEntities.THROWN_BREW.get(), level);
        var start = Vec3.atCenterOf(impact.above(2));
        projectile.setPos(start.x, start.y, start.z);
        projectile.setDeltaMovement(0, -1, 0);
        projectile.setItem(bottle);
        level.addFreshEntity(projectile);

        helper.succeedWhen(() -> {
            var wolfsbane = pig.getEffect(WitcheryEffects.WOLFSBANE.get());
            helper.assertTrue(wolfsbane != null && wolfsbane.getDuration() > 600,
                    "The creature effect receives the redstone boost");
            helper.assertTrue(holdTicks(helper, water) > 395, "Tide Hold receives its own redstone boost");
            int lavaTicks = holdTicks(helper, lava);
            helper.assertTrue(lavaTicks > 195 && lavaTicks <= 200, "Tide Hold then clears the boost for Lava Hold");
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_dispenser", timeoutTicks = 310)
    public static void dispenserBottlesOnlyBoilingPoweredCauldrons(GameTestHelper helper) {
        var level = helper.getLevel();
        // Keep the fire above water flowing from earlier GameTests.
        var pos = helper.absolutePos(new BlockPos(5, 160, 5));
        var dispenser = dispenser(helper, pos.north(), new ItemStack(Items.GLASS_BOTTLE, 2));
        level.setBlockAndUpdate(pos.below(2), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(pos.below(), Blocks.FIRE.defaultBlockState());
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var cauldron = (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
        var tank = cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        tank.fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE);

        pulse(helper, dispenser, true);
        helper.startSequence().thenIdle(6).thenExecute(() -> {
            helper.assertTrue(dispenser.getItem(0).getCount() == 2 && tank.getFluidInTank(0).getAmount() == 3000,
                    "A cold adjacent cauldron holds the glass bottle and the water");
            pulse(helper, dispenser, false);
        }).thenWaitUntil(() -> {
            helper.assertTrue(level.getBlockState(pos).getValue(WitchesCauldronBlock.BOILING), "Boil the dispenser brew");
        }).thenExecute(() -> {
            drop(helper, pos, Items.NETHER_WART);
            drop(helper, pos, WitcheryIngredient.WOLFSBANE.item());
            drop(helper, pos, Items.GUNPOWDER);
            pulse(helper, dispenser, true);
        }).thenIdle(6).thenExecute(() -> {
            helper.assertTrue(dispenser.getItem(0).getCount() == 2 && tank.getFluidInTank(0).getAmount() == 3000,
                    "A prepared cauldron without altar power keeps the brew and the glass bottle");
            pulse(helper, dispenser, false);

            var altarPos = pos.east(3);
            var state = WitcheryBlocks.getRequired("altar").get().defaultBlockState();
            for (var part : BlockPos.betweenClosed(altarPos, altarPos.east(2).south())) level.setBlockAndUpdate(part, state);
            AltarBlockEntity.form(level, altarPos);
            var egg = altarPos.south(3);
            level.setBlockAndUpdate(egg.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(egg, Blocks.DRAGON_EGG.defaultBlockState());
            var altar = (AltarBlockEntity) level.getBlockEntity(altarPos);
            var saved = altar.saveWithFullMetadata();
            saved.putInt("MaxPower", 250);
            saved.putFloat("Power", 150);
            altar.load(saved);
            var brew = tank.getFluidInTank(0).copy();
            helper.startSequence().thenWaitUntil(() -> helper.assertTrue(sampledPower(cauldron),
                    "The boiling cauldron samples the newly available altar power")).thenExecute(() -> {
                cauldron.load(cauldron.saveWithFullMetadata());
                pulse(helper, dispenser, true);
            }).thenIdle(6).thenExecute(() -> {
                var bottles = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos.north()).inflate(4),
                        item -> item.getItem().is(WitcheryItems.getRequired("brewbottle").get()));
                helper.assertTrue(bottles.size() == 1 && bottles.get(0).getItem().getTag().equals(brew.getTag()),
                        "The output is one ejected bottle with the saved brew's ordered ingredients, color and delivery");
                helper.assertTrue(tank.getFluidInTank(0).isEmpty() && dispenser.getItem(0).is(Items.GLASS_BOTTLE)
                                && dispenser.getItem(0).getCount() == 1,
                        "The dispenser uses three buckets and one glass bottle");
                helper.assertTrue(altar.power() >= 150, "The dispenser did not debit the altar's 150 stored power");
                pulse(helper, dispenser, false);
                brew.setAmount(2000);
                helper.assertTrue(tank.fill(brew, IFluidHandler.FluidAction.EXECUTE) == 2000,
                        "Refill the cauldron with two buckets of the saved brew");
            }).thenWaitUntil(() -> helper.assertTrue(
                    level.getBlockState(pos).getValue(WitchesCauldronBlock.BOILING) && sampledPower(cauldron),
                    "The refilled brew is boiling and has sampled power"))
                    .thenExecute(() -> pulse(helper, dispenser, true)).thenIdle(6).thenExecute(() -> {
                        helper.assertTrue(tank.getFluidInTank(0).isEmpty() && dispenser.getItem(0).is(Items.GLASS_BOTTLE)
                                        && dispenser.getItem(0).getCount() == 1,
                                "Drain the partial hot brew without using a bottle");
                    }).thenSucceed();
        });
    }

    @GameTest(template = "qa_coven_arena", timeoutTicks = 35)
    public static void dispenserThrowsBrewsAndKeepsVanillaBottleBehavior(GameTestHelper helper) {
        var level = helper.getLevel();
        var inkPos = helper.absolutePos(new BlockPos(3, 4, 3));
        var splashPos = helper.absolutePos(new BlockPos(3, 4, 8));
        var drinkPos = helper.absolutePos(new BlockPos(3, 4, 6));
        var waterPos = helper.absolutePos(new BlockPos(8, 4, 6));
        var bottle = WitcheryItems.getRequired("brewbottle").get();
        var splash = new ItemStack(bottle);
        splash.setTag(BrewMixture.ingredientTag(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.SPIDER_EYE),
                new ItemStack(Items.GUNPOWDER)), 0x46a238));
        var drink = new ItemStack(bottle);
        drink.setTag(BrewMixture.ingredientTag(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.SUGAR)), 0xfbac81));
        var inkDispenser = dispenser(helper, inkPos, new ItemStack(WitcheryIngredient.BREW_OF_INK.item()));
        var splashDispenser = dispenser(helper, splashPos, splash);
        pulse(helper, inkDispenser, true);
        pulse(helper, splashDispenser, true);
        pulse(helper, dispenser(helper, drinkPos, drink), true);
        level.setBlockAndUpdate(waterPos.east(), Blocks.WATER.defaultBlockState());
        var waterDispenser = dispenser(helper, waterPos, new ItemStack(Items.GLASS_BOTTLE));
        pulse(helper, waterDispenser, true);

        var inkTarget = helper.spawn(EntityType.PIG, new BlockPos(6, 4, 3));
        var splashTarget = helper.spawn(EntityType.PIG, new BlockPos(6, 4, 8));
        inkTarget.setNoAi(true);
        splashTarget.setNoAi(true);
        for (var wall : List.of(inkPos.east(4), splashPos.east(4))) {
            level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(wall.above(), Blocks.STONE.defaultBlockState());
        }
        helper.succeedWhen(() -> {
            helper.assertTrue(inkTarget.hasEffect(MobEffects.BLINDNESS) && splashTarget.hasEffect(MobEffects.POISON),
                    "The two dispenser projectiles apply their brew effects at impact");
            helper.assertTrue(inkDispenser.isEmpty() && splashDispenser.isEmpty(), "Each projectile uses one brew");
            helper.assertTrue(waterDispenser.getItem(0).is(Items.POTION), "A dispenser still fills glass from vanilla water");
            helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(drinkPos).inflate(3),
                    item -> ItemStack.isSameItemSameTags(item.getItem(), drink)).size() == 1,
                    "Drinkable brews are ejected with their original recipe");
        });
    }

    @GameTest(template = "qa_coven_arena", timeoutTicks = 180)
    public static void dispenserBottlesFixedRitualWithItsSavedTrigger(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        var dispenser = dispenser(helper, pos.north(), new ItemStack(Items.GLASS_BOTTLE));
        level.setBlockAndUpdate(pos.below(2), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(pos.below(), Blocks.FIRE.defaultBlockState());
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var cauldron = (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
        var tank = cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        tank.fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE);

        var egg = new ItemStack(Items.EGG).setHoverName(Component.literal("Test Offering"));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.getBlockState(pos).getValue(WitchesCauldronBlock.BOILING), "Boil the ritual's water")).thenExecute(() -> {
            drop(helper, pos, WitcheryIngredient.MANDRAKE_ROOT.item());
            drop(helper, pos, WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item());
            drop(helper, pos, Items.WHEAT);
            drop(helper, pos, egg);
        }).thenWaitUntil(() -> {
            helper.assertTrue(sampledPower(cauldron), "The free ritual receives its first power sample");
        }).thenExecute(() -> {
            var saved = cauldron.saveWithFullMetadata();
            helper.assertTrue(saved.getString("Ritual").equals("witchery:mutandis"), "The egg begins the ordinary Mutandis cast");
            cauldron.load(saved);
            pulse(helper, dispenser, true);
        }).thenIdle(6).thenExecute(() -> {
            var bottles = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4),
                    item -> item.getItem().is(WitcheryItems.getRequired("brewbottle").get()));
            helper.assertTrue(bottles.size() == 1 && dispenser.isEmpty() && tank.getFluidInTank(0).isEmpty(),
                    "Dispensing intercepts the pending ritual and ejects one filled bottle");
            var bottle = bottles.get(0).getItem();
            var tag = bottle.getTag();
            var offerings = tag.getList("Items", Tag.TAG_COMPOUND);
            helper.assertTrue(tag.getString("FixedRitual").equals("witchery:mutandis") && tag.getBoolean("RitualTriggered")
                            && offerings.size() == 4 && ItemStack.matches(ItemStack.of(offerings.getCompound(3)), egg),
                    "The bottled cast preserves the original offerings, including the named trigger after loading");
            var pig = helper.spawn(EntityType.PIG, new BlockPos(6, 4, 6));
            var drink = bottle.copy();
            drink.getOrCreateTag().remove("FixedRitual");
            drink.getOrCreateTag().putString("BrewName", "Brew of Moonshine");
            var empty = drink.finishUsingItem(level, pig);
            helper.assertTrue(empty.is(Items.GLASS_BOTTLE) && pig.hasEffect(WitcheryEffects.FEEL_NO_PAIN.get()),
                    "The bottled effect is usable even when the list contains a fixed-recipe offering in the original format");
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", timeoutTicks = 180)
    public static void handBottlesFixedRitualAndPreservesItsRemainingBrew(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        level.setBlockAndUpdate(pos.below(2), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(pos.below(), Blocks.FIRE.defaultBlockState());
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var cauldron = (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
        var tank = cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        tank.fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "RitualBottler"));
        WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new).setPotionBottling(30);

        var egg = new ItemStack(Items.EGG).setHoverName(Component.literal("Saved Offering"));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.getBlockState(pos).getValue(WitchesCauldronBlock.BOILING), "Boil the ritual's water")).thenExecute(() -> {
            drop(helper, pos, WitcheryIngredient.MANDRAKE_ROOT.item());
            drop(helper, pos, WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item());
            drop(helper, pos, egg);
        }).thenWaitUntil(() -> {
            helper.assertTrue(sampledPower(cauldron), "The free ritual receives its first power sample");
        }).thenExecute(() -> {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
            helper.assertTrue(cauldron.bottle(player, InteractionHand.MAIN_HAND), "Bottle an active fixed cast by hand");
            var bottle = player.getMainHandItem().copy();
            var bottled = bottle.getOrCreateTag();
            var offerings = bottled.getList("Items", Tag.TAG_COMPOUND);
            helper.assertTrue(bottle.is(WitcheryItems.getRequired("brewbottle").get()) && bottled.getBoolean("RitualTriggered")
                            && bottled.getString("FixedRitual").equals("witchery:mutandis") && offerings.size() == 3
                            && ItemStack.matches(ItemStack.of(offerings.getCompound(2)), egg),
                    "The bottle keeps the ordered ingredients, original marker and named trigger");
            helper.assertTrue(tank.getFluidInTank(0).getAmount() == 1500, "Bottling skill leaves a second portion");

            var drained = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            helper.assertTrue(drained.getAmount() == 1000 && bottled.equals(drained.getTag()),
                    "A fluid withdrawal carries the same ordered cast and trigger");
            cauldron.load(cauldron.saveWithFullMetadata());
            var wrong = drained.copy();
            wrong.getOrCreateTag().remove("RitualTriggered");
            helper.assertTrue(tank.fill(wrong, IFluidHandler.FluidAction.EXECUTE) == 0,
                    "A matching color and offerings without the active-cast marker cannot refill it");
            helper.assertTrue(tank.fill(drained, IFluidHandler.FluidAction.EXECUTE) == 1000,
                    "The matching saved cast can refill the remainder after reload");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
            helper.assertTrue(cauldron.bottle(player, InteractionHand.MAIN_HAND) && tank.getFluidInTank(0).isEmpty()
                            && ItemStack.matches(bottle, player.getMainHandItem()),
                    "The remaining portion produces the same tagged bottle and clears the pending cast");
        }).thenSucceed();
    }

    private static boolean sampledPower(WitchesCauldronBlockEntity cauldron) {
        return cauldron.saveWithoutMetadata().getBoolean("Powered");
    }

    private static DispenserBlockEntity dispenser(GameTestHelper helper, BlockPos pos, ItemStack input) {
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST));
        var dispenser = (DispenserBlockEntity) level.getBlockEntity(pos);
        dispenser.setItem(0, input.copy());
        return dispenser;
    }

    private static void pulse(GameTestHelper helper, DispenserBlockEntity dispenser, boolean powered) {
        helper.getLevel().setBlockAndUpdate(dispenser.getBlockPos().above(),
                (powered ? Blocks.REDSTONE_BLOCK : Blocks.AIR).defaultBlockState());
    }

    private static void drink(LivingEntity target, List<ItemStack> recipe) {
        var bottle = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        bottle.setTag(BrewMixture.ingredientTag(recipe, 0x99afae));
        bottle.finishUsingItem(target.level(), target);
    }

    private static int holdTicks(GameTestHelper helper, BlockPos pos) {
        var hold = helper.getLevel().getBlockEntity(pos);
        helper.assertTrue(hold instanceof SlurpBlockEntity, "Expected a fluid hold at " + pos);
        return hold.saveWithoutMetadata().getInt("RemainingTicks");
    }

    private static void drop(GameTestHelper helper, BlockPos pos, Item item) {
        drop(helper, pos, new ItemStack(item));
    }

    private static void drop(GameTestHelper helper, BlockPos pos, ItemStack stack) {
        var level = helper.getLevel();
        var center = Vec3.atCenterOf(pos);
        var dropped = new ItemEntity(level, center.x, center.y, center.z, stack.copy());
        level.getBlockState(pos).entityInside(level, pos, dropped);
        helper.assertTrue(dropped.isRemoved(), "The boiling cauldron accepts " + stack);
    }
}
