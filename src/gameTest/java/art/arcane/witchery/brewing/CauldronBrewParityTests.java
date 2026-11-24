package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.block.entity.AltarBlockEntity;
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
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
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
        level.setBlockAndUpdate(impact, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(occupiedCell, Blocks.CHEST.defaultBlockState());
        var weak = BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER),
                new ItemStack(WitcheryIngredient.WEB.item())));
        var strong = BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER),
                new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(Items.BLAZE_ROD), new ItemStack(WitcheryIngredient.WEB.item())));
        helper.assertTrue(weak != null && strong != null, "Both original cauldron recipes are valid");
        Consumer<BlockEvent.EntityPlaceEvent> protection = event -> {
            if (event.getLevel() == level && event.getEntity() == player && event.getPos().equals(protectedCell)) {
                event.setCanceled(true);
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
            helper.assertTrue(level.getBlockState(occupiedCell).is(Blocks.CHEST) && level.isEmptyBlock(protectedCell)
                            && level.isEmptyBlock(center.above().east().south()),
                    "The expansion respects occupied and protected cells and leaves the corners clear");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            player.discard();
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void lilifyRequiresSourceWaterForStablePads(GameTestHelper helper) {
        var level = helper.getLevel();
        var source = helper.absolutePos(new BlockPos(4, 5, 5));
        var flowing = source.east(3);
        var ice = source.south(3);
        var water = Blocks.WATER.defaultBlockState();
        level.setBlockAndUpdate(source, water);
        level.setBlockAndUpdate(flowing, water.setValue(LiquidBlock.LEVEL, 4));
        level.setBlockAndUpdate(ice, Blocks.ICE.defaultBlockState());
        var lily = WitcheryBlocks.getRequired("lilypad").get();
        var recipe = BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER),
                new ItemStack(Items.LILY_PAD)));
        helper.assertTrue(recipe != null, "The original Lilify recipe is valid");
        recipe.applySplashToBlock(level, source.west(), Direction.EAST, Vec3.atCenterOf(source), null);
        recipe.applySplashToBlock(level, flowing.west(), Direction.EAST, Vec3.atCenterOf(flowing), null);
        helper.assertTrue(level.getBlockState(source.above()).is(lily) && level.isEmptyBlock(flowing.above()),
                "Lilify establishes a pad on the source surface and leaves the flowing surface clear");
        helper.assertTrue(!lily.defaultBlockState().canSurvive(level, flowing.above())
                        && !lily.defaultBlockState().canSurvive(level, ice.above()),
                "The original Witchery pad requires source water for support");
        level.setBlockAndUpdate(source, water.setValue(LiquidBlock.LEVEL, 4));
        helper.assertTrue(level.isEmptyBlock(source.above()), "An established pad breaks when its source starts flowing");
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

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_dispenser", timeoutTicks = 245)
    public static void dispenserBottlesOnlyBoilingPoweredCauldrons(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        var dispenser = dispenser(helper, pos.north(), new ItemStack(Items.GLASS_BOTTLE, 2));
        level.setBlockAndUpdate(pos.below(2), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(pos.below(), Blocks.FIRE.defaultBlockState());
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var cauldron = (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
        var tank = cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        tank.fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE);

        pulse(helper, dispenser, true);
        helper.runAfterDelay(6, () -> {
            helper.assertTrue(dispenser.getItem(0).getCount() == 2 && tank.getFluidInTank(0).getAmount() == 3000,
                    "A cold adjacent cauldron holds the glass bottle and the water");
            pulse(helper, dispenser, false);
        });
        helper.runAfterDelay(105, () -> {
            drop(helper, pos, Items.NETHER_WART);
            drop(helper, pos, WitcheryIngredient.WOLFSBANE.item());
            drop(helper, pos, Items.GUNPOWDER);
            pulse(helper, dispenser, true);
        });
        helper.runAfterDelay(111, () -> {
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
            cauldron.load(cauldron.saveWithFullMetadata());
            pulse(helper, dispenser, true);
            helper.runAfterDelay(6, () -> {
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
                helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                        level.getBlockState(pos).getValue(WitchesCauldronBlock.BOILING), "The refilled brew is boiling"))
                        .thenExecute(() -> pulse(helper, dispenser, true)).thenIdle(6).thenExecute(() -> {
                            helper.assertTrue(tank.getFluidInTank(0).isEmpty() && dispenser.getItem(0).is(Items.GLASS_BOTTLE)
                                            && dispenser.getItem(0).getCount() == 1,
                                    "Drain the partial hot brew without using a bottle. Remaining brew: "
                                            + tank.getFluidInTank(0).getAmount() + ", altar power: " + altar.power());
                        }).thenSucceed();
            });
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

    @GameTest(template = "qa_coven_arena", timeoutTicks = 135)
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

        helper.runAfterDelay(105, () -> {
            drop(helper, pos, WitcheryIngredient.MANDRAKE_ROOT.item());
            drop(helper, pos, WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item());
            drop(helper, pos, Items.WHEAT);
            var egg = new ItemStack(Items.EGG).setHoverName(Component.literal("Test Offering"));
            drop(helper, pos, egg);
            var saved = cauldron.saveWithFullMetadata();
            helper.assertTrue(saved.getString("Ritual").equals("witchery:mutandis"), "The egg begins the ordinary Mutandis cast");
            cauldron.load(saved);
            pulse(helper, dispenser, true);
            helper.runAfterDelay(6, () -> {
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
                helper.succeed();
            });
        });
    }

    @GameTest(template = "qa_coven_arena", timeoutTicks = 135)
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

        helper.runAfterDelay(105, () -> {
            drop(helper, pos, WitcheryIngredient.MANDRAKE_ROOT.item());
            drop(helper, pos, WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item());
            var egg = new ItemStack(Items.EGG).setHoverName(Component.literal("Saved Offering"));
            drop(helper, pos, egg);
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
            helper.succeed();
        });
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
