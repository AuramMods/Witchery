package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.BrazierBlockEntity;
import art.arcane.witchery.crafting.BrazierRecipe;
import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.infusion.InfusedBrews;
import art.arcane.witchery.item.SpectralStoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrazierBehaviorTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_brazier_behavior")
    public static void manualIgnitionUsesTheStruckFaceAndSpendsFlintWear(GameTestHelper helper) {
        var brazier = place(helper);
        var level = helper.getLevel();
        var pos = brazier.getBlockPos();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrazierLighter"));
        player.setPos(Vec3.atBottomCenterOf(pos.east(2)));
        var hand = InteractionHand.MAIN_HAND;
        var target = pos.east();
        level.setBlockAndUpdate(target.below(), Blocks.STONE.defaultBlockState());
        var cues = new ArrayList<PlayLevelSoundEvent.AtPosition>();
        Consumer<PlayLevelSoundEvent.AtPosition> listener = event -> {
            if (event.getLevel() == level && event.getPosition().equals(Vec3.atCenterOf(target))
                    && event.getSound() != null && event.getSound().value() == SoundEvents.FLINTANDSTEEL_USE) {
                cues.add(event);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            for (boolean loaded : new boolean[]{false, true}) {
                for (boolean blocked : new boolean[]{false, true}) {
                    level.setBlockAndUpdate(target, (blocked ? Blocks.STONE : Blocks.AIR).defaultBlockState());
                    if (loaded) brazier.insert(new ItemStack(Items.GUNPOWDER));
                    var tool = new ItemStack(Items.FLINT_AND_STEEL);
                    player.setItemInHand(hand, tool);
                    var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0.5, 0, 0), Direction.EAST, pos, false);
                    helper.assertTrue(player.gameMode.useItemOn(player, level, tool, hand, hit).consumesAction()
                                    && tool.getDamageValue() == 1 && level.getBlockState(target).is(Blocks.FIRE) == !blocked
                                    && cues.size() == (blocked ? 0 : 1),
                            "The normal strike spends one use and only starts fire and plays the cue on the open side");
                    helper.assertTrue(brazier.saveWithoutMetadata().getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND)
                                    .size() == (loaded ? 2 : 0),
                            "A loaded Brazier receives ash and an empty Brazier keeps its slots empty");
                    cues.clear();
                    BrazierBlockEntity.serverTick(level, pos, brazier.getBlockState(), brazier);
                    helper.assertTrue(brazier.isEmpty() && !brazier.getBlockState().getValue(BrazierBlock.LIT),
                            "An incomplete recipe fizzles and clears the brazier");
                }
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brazier_inventory")
    public static void brazierWeighsOfferingsAndRecoversSavedStacksOnBreak(GameTestHelper helper) {
        var brazier = place(helper);
        var level = helper.getLevel();
        var pos = brazier.getBlockPos();
        helper.assertTrue(brazier.insert(new ItemStack(Items.IRON_SWORD)) && brazier.comparatorSignal() == 4,
                "One unstackable offering emits four comparator levels");
        helper.assertTrue(brazier.insert(new ItemStack(Items.GOLDEN_SWORD)) && brazier.comparatorSignal() == 8,
                "A second unstackable offering raises the comparator to eight");
        brazier.clearBatch();

        var offering = new ItemStack(Items.GUNPOWDER, 48);
        offering.setHoverName(Component.literal("Recovered Offering"));
        var entry = offering.save(new CompoundTag());
        entry.putByte("Slot", (byte) 0);
        var entries = new ListTag();
        entries.add(entry);
        var saved = brazier.saveWithoutMetadata();
        saved.put("Items", entries);
        brazier.load(saved);
        brazier.load(brazier.saveWithoutMetadata());
        helper.assertTrue(brazier.comparatorSignal() == 3, "Loading a saved 48-stack preserves its original comparator level");

        var drops = new ArrayList<ItemEntity>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item
                    && item.getItem().is(Items.GUNPOWDER) && item.distanceToSqr(Vec3.atCenterOf(pos)) < 4) drops.add(item);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            level.random.setSeed(42);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            helper.assertTrue(drops.stream().map(drop -> drop.getItem().getCount()).toList().equals(List.of(27, 10, 11))
                            && drops.stream().allMatch(drop -> drop.getItem().getHoverName().getString().equals("Recovered Offering")
                            && drop.saveWithoutId(new CompoundTag()).getShort("PickupDelay") == 0),
                    "Breaking the brazier spills all saved offerings in the original named fragments without a pickup delay");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            drops.forEach(ItemEntity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brazier_rinse")
    public static void rinsingReturnsTheContainerAndSplashesAtThePlayer(GameTestHelper helper) {
        var brazier = place(helper);
        var level = helper.getLevel();
        var pos = brazier.getBlockPos();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrazierRinse"));
        player.setPos(Vec3.atBottomCenterOf(pos.north(2)));
        var hand = InteractionHand.MAIN_HAND;
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var cues = new ArrayList<PlayLevelSoundEvent.AtEntity>();
        Consumer<PlayLevelSoundEvent.AtEntity> observer = event -> {
            if (event.getEntity() == player) cues.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(observer);
        try {
            for (var filled : List.of(new ItemStack(Items.WATER_BUCKET),
                    PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER))) {
                var empty = filled.is(Items.WATER_BUCKET) ? Items.BUCKET : Items.GLASS_BOTTLE;
                player.setItemInHand(hand, filled);
                brazier.getBlockState().use(level, player, hand, hit);
                helper.assertTrue(player.getMainHandItem() == filled && cues.isEmpty(),
                        "Rinsing an empty Brazier keeps the filled container and stays silent");

                for (var ingredient : BrazierRecipe.MIST.ingredients()) brazier.insert(new ItemStack(ingredient));
                brazier.ignite();
                brazier.getBlockState().use(level, player, hand, hit);
                helper.assertTrue(player.getMainHandItem().is(empty) && brazier.isEmpty() && brazier.comparatorSignal() == 0
                                && !brazier.getBlockState().getValue(BrazierBlock.LIT),
                        "Rinsing a lit Brazier returns the empty container and clears the whole batch");
                helper.assertTrue(cues.size() == 1 && cues.get(0).getSound() != null
                                && cues.get(0).getSound().value() == SoundEvents.PLAYER_SPLASH
                                && cues.get(0).getNewVolume() == 0.5F && cues.get(0).getNewPitch() >= 1F / 3
                                && cues.get(0).getNewPitch() <= 0.5F,
                        "A successful rinse sends the original splash from the player once");
                cues.clear();
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(observer);
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brazier_storage")
    public static void storedWiltingPowerExtendsLaterBatchesAfterAnIdleSave(GameTestHelper helper) {
        var brazier = place(helper);
        var level = helper.getLevel();
        var pos = brazier.getBlockPos();
        var altarPos = pos.east(3);
        var state = WitcheryBlocks.getRequired("altar").get().defaultBlockState();
        for (BlockPos part : BlockPos.betweenClosed(altarPos, altarPos.offset(2, 0, 1))) {
            level.setBlockAndUpdate(part, state);
        }
        AltarBlockEntity.form(level, altarPos);
        var altar = (AltarBlockEntity) level.getBlockEntity(altarPos);
        var power = altar.saveWithoutMetadata();
        power.putFloat("Power", 4);
        altar.load(power);

        for (var ingredient : BrazierRecipe.WILTING.ingredients()) brazier.insert(new ItemStack(ingredient));
        brazier.ignite();
        var savedWilting = brazier.saveWithoutMetadata();
        savedWilting.putLong("PowerStorage", 2);
        brazier.load(savedWilting);
        brazier.clearBatch();
        brazier.load(brazier.saveWithFullMetadata());
        helper.assertTrue(brazier.isEmpty() && brazier.saveWithoutMetadata().getLong("PowerStorage") == 2,
                "Rinsing and restoring an idle Brazier must retain stored Wilting power");

        var strength = BrazierRecipe.STRENGTH.ingredients();
        brazier.insert(new ItemStack(strength.get(0)));
        brazier.ignite();
        helper.assertTrue(brazier.insert(new ItemStack(strength.get(1))) && brazier.insert(new ItemStack(strength.get(2))),
                "The last offerings can enter an already ignited Brazier before its first burn tick");
        var batch = brazier.saveWithoutMetadata();
        batch.putLong("CookTime", 5999);
        brazier.load(batch);
        BrazierBlockEntity.serverTick(level, pos, brazier.getBlockState(), brazier);
        helper.assertTrue(brazier.saveWithoutMetadata().getLong("CookTime") == 6000 && !brazier.isEmpty() && altar.power() == 3,
                "The later Strength batch keeps burning past its ordinary endpoint and pays for the tick");

        batch.putLong("CookTime", 6799);
        brazier.load(batch);
        BrazierBlockEntity.serverTick(level, pos, brazier.getBlockState(), brazier);
        var complete = brazier.saveWithoutMetadata();
        var contents = complete.getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
        helper.assertTrue(complete.getLong("CookTime") == 0 && brazier.isEmpty() && contents.size() == 1
                        && ItemStack.of(contents.getCompound(0)).is(WitcheryIngredient.WOOD_ASH.item())
                        && brazier.comparatorSignal() == 1 && altar.power() == 2,
                "The extended batch consumes its ingredients at tick 6800 and leaves ash for the next tick");
        BrazierBlockEntity.serverTick(level, pos, brazier.getBlockState(), brazier);
        helper.assertTrue(brazier.comparatorSignal() == 0 && brazier.saveWithoutMetadata().getLong("PowerStorage") == 2
                        && altar.power() == 2,
                "The following tick clears the ash and retains the power credit without charging the altar");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brazier_behavior", setupTicks = 5)
    public static void wiltingHealsGraveInfusedPlayersInRange(GameTestHelper helper) {
        var brazier = place(helper);
        var level = helper.getLevel();
        var pos = brazier.getBlockPos();
        var first = pos.offset(-3, -1, -3);
        var last = pos.offset(3, -1, 3);
        for (BlockPos crop : BlockPos.betweenClosed(first, last)) {
            level.setBlock(crop.below(), Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            level.setBlock(crop, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7),
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        long availableCrops = BlockPos.betweenClosedStream(first, last).filter(level::isLoaded)
                .map(level::getBlockState).filter(state -> state.is(Blocks.WHEAT) && state.getValue(CropBlock.AGE) == 7).count();
        helper.assertTrue(availableCrops == 49, "Expected all 49 candidate crops to be loaded and ripe, got " + availableCrops);
        var infused = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "GraveDrinker"));
        infused.setPos(Vec3.atBottomCenterOf(pos.north()));
        infused.setNoGravity(true);
        level.addNewPlayer(infused);
        var outside = EntityType.ZOMBIE.create(level);
        outside.setPos(Vec3.atLowerCornerOf(pos).add(3.75, 0, 0.5));
        outside.setNoAi(true);
        outside.setNoGravity(true);
        level.addFreshEntity(outside);
        var testedArea = new AABB(pos).inflate(5);
        helper.startSequence().thenWaitUntil(() -> {
            var tracked = level.getEntitiesOfClass(Entity.class, testedArea);
            helper.assertTrue(tracked.contains(infused) && tracked.contains(outside),
                    "Both targets must be tracked before the Brazier pulse");
        }).thenExecute(() -> {
            try {
                InfusedBrews.applyGrave(infused);
                infused.setHealth(10);
                outside.setHealth(10);
                int storage = BrazierRecipe.WILTING.burn(level, pos, 5);
                helper.assertTrue(infused.getHealth() == 12 && outside.getHealth() == 10 && storage == 2,
                        "Expected the first wilt to heal only the Grave-infused player and add burn time. Player health "
                                + infused.getHealth() + ", outside Zombie health " + outside.getHealth()
                                + ", storage " + storage);
            } finally {
                level.removePlayerImmediately(infused, Entity.RemovalReason.DISCARDED);
                outside.discard();
            }
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brazier_spectral_landing", setupTicks = 5)
    public static void brazierBansheeFitsAboveASlabUnderATwoBlockCeiling(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = summonFloor(helper);
        BlockPos column = origin.west(2).north();
        level.setBlockAndUpdate(column, Blocks.STONE_SLAB.defaultBlockState());
        level.setBlockAndUpdate(column.above(3), Blocks.STONE.defaultBlockState());
        level.getRandom().setSeed(0);

        var arrivals = arrivalsDuring(helper, () -> BrazierRecipe.BANSHEE.finish(level, origin));
        helper.assertTrue(arrivals.size() == 1, "Only the selected Banshee should arrive");
        var banshee = arrivals.get(0);
        helper.assertTrue(banshee.kind() == SpectralUndead.Kind.BANSHEE
                        && banshee.position().distanceToSqr(Vec3.atBottomCenterOf(column.above()).add(0, 0.05, 0)) < 1.0E-8
                        && Math.abs(banshee.getBbHeight() - 1.8) < 0.0001,
                "The original Banshee fits in the two air blocks above the sampled slab");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brazier_stray_summon", setupTicks = 5)
    public static void blockedBrazierColumnStillAllowsTheSeparateStrayRoll(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = summonFloor(helper);
        BlockPos first = origin.west(2).north().above();
        level.setBlockAndUpdate(first, Blocks.WATER.defaultBlockState());
        level.getRandom().setSeed(0);
        helper.assertTrue(arrivalsDuring(helper, () -> BrazierRecipe.SPECTRE.finish(level, origin)).isEmpty(),
                "A blocked sampled column must not redirect the regular summon to the open floor nearby");

        level.setBlockAndUpdate(first, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(origin.west().south(2).above(), Blocks.WATER.defaultBlockState());
        level.getRandom().setSeed(23);
        var arrivals = arrivalsDuring(helper, () -> BrazierRecipe.SPECTRE.finish(level, origin));
        helper.assertTrue(arrivals.size() == 1 && arrivals.get(0).kind() == SpectralUndead.Kind.POLTERGEIST
                        && arrivals.get(0).blockPosition().equals(origin.west(7).north(7)),
                "The five-percent roll can place a stray Poltergeist even when the main summon is blocked");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_spectral_stone_landing", setupTicks = 5)
    public static void spectralStoneTriesTheNextChargeAndKeepsTheBlockedOne(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = summonFloor(helper);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SpectralRelease"));
        player.setPos(Vec3.atBottomCenterOf(origin.above(3)));
        player.setXRot(90);
        level.setBlockAndUpdate(origin.west().above(), Blocks.WATER.defaultBlockState());
        var stone = (SpectralStoneItem) WitcheryItems.getRequired("spectralstone").get();
        var stack = new ItemStack(stone);
        SpectralStoneItem.applyLegacyContents(stack, 33);
        level.getRandom().setSeed(0);
        var arrivals = arrivalsDuring(helper, () -> stone.releaseUsing(stack, level, player, stone.getUseDuration(stack) - 40));
        helper.assertTrue(arrivals.size() == 1 && arrivals.get(0).blockPosition().equals(origin.south())
                        && SpectralStoneItem.creatureCount(stack) == 1,
                "The second draw succeeds independently and the blocked charge remains in the stone");
        helper.succeed();
    }

    private static BlockPos summonFloor(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(new BlockPos(11, 10, 11));
        var level = helper.getLevel();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -1, -8), center.offset(8, -1, 8))) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(pos.above(2), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        return center;
    }

    private static ArrayList<SpectralUndead> arrivalsDuring(GameTestHelper helper, Runnable summon) {
        var arrivals = new ArrayList<SpectralUndead>();
        Consumer<EntityJoinLevelEvent> observer = event -> {
            if (event.getLevel() == helper.getLevel() && event.getEntity() instanceof SpectralUndead undead) arrivals.add(undead);
        };
        MinecraftForge.EVENT_BUS.addListener(observer);
        try {
            summon.run();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(observer);
        }
        arrivals.forEach(Entity::discard);
        return arrivals;
    }

    private static BrazierBlockEntity place(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(5, 5, 5));
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("brazier").get().defaultBlockState());
        return (BrazierBlockEntity) level.getBlockEntity(pos);
    }
}
