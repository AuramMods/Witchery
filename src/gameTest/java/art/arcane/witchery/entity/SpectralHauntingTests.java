package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.data.WitcheryLootTables;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SpectralHauntingTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_poltergeist_container")
    public static void nearbySummonerAllowsSeededPoltergeistThrow(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(4, 10, 4));
        var chestPos = origin.east(2);
        var chest = placeChest(level, chestPos);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 2));
        chest.setItem(12, new ItemStack(Items.IRON_INGOT, 3));
        chest.setItem(26, new ItemStack(Items.GOLD_INGOT, 4));

        var body = Vec3.atCenterOf(origin);
        var summoner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "HauntOwner"));
        summoner.setPos(body.add(0, 0, 8));
        level.addNewPlayer(summoner);
        var spirit = WitcheryEntities.POLTERGEIST.get().create(level);
        spirit.setPos(body);
        spirit.setNoAi(true);
        spirit.setSummoner(summoner);
        var drops = new ArrayList<ItemEntity>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item
                    && item.position().equals(Vec3.atCenterOf(chestPos))) drops.add(item);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            spirit.tickCount = 99;
            spirit.aiStep();
            helper.assertTrue(drops.isEmpty() && spirit.throwAnimation() == 0, "The spirit waits for its hundredth tick");

            level.getRandom().setSeed(7);
            spirit.tickCount = 100;
            spirit.aiStep();
            helper.assertTrue(drops.size() == 1 && chest.getItem(0).getCount() == 2
                            && chest.getItem(12).getCount() == 2 && chest.getItem(26).getCount() == 4,
                    "The original world draw selects one item from the middle occupied slot");
            var toss = drops.get(0);
            helper.assertTrue(toss.getItem().is(Items.IRON_INGOT) && toss.getItem().getCount() == 1 && toss.lifespan == 18000
                            && toss.getDeltaMovement().distanceTo(new Vec3(0.083122594, 0.101925791, -0.005636985)) < 1e-8
                            && level.getRandom().nextInt(1000) == 649 && spirit.throwAnimation() == 15,
                    "The selected ingot keeps the original throw vector, lifespan, world draw order, and animation");

            toss.discard();
            Consumer<EntityJoinLevelEvent> protect = event -> {
                if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item
                        && item.position().equals(Vec3.atCenterOf(chestPos))) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(protect);
            try {
                spirit.tickCount = 200;
                spirit.aiStep();
                helper.assertTrue(chest.getItem(0).getCount() == 2 && chest.getItem(12).getCount() == 2
                                && chest.getItem(26).getCount() == 4,
                        "A Forge veto preserves the inventory offering");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(protect);
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
            drops.forEach(Entity::discard);
            level.removePlayerImmediately(summoner, Entity.RemovalReason.DISCARDED);
            spirit.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_poltergeist_loot")
    public static void hauntingLeavesUnusedChestLootSealed(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(4, 10, 4)).atY(188);
        placeChest(level, origin.north());
        var target = placeChest(level, origin.east(2));
        target.setLootTable(WitcheryLootTables.WITCH_SHACK, 14L);
        var farther = placeChest(level, origin.east(4));
        farther.setLootTable(WitcheryLootTables.WITCH_SHACK, 31L);
        var outOfRange = placeChest(level, origin.above(17));
        outOfRange.setLootTable(WitcheryLootTables.WITCH_SHACK, 32L);

        var body = Vec3.atCenterOf(origin);
        var summoner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "HauntLootOwner"));
        summoner.setPos(body.add(0, 0, 8));
        level.addNewPlayer(summoner);
        var spirit = WitcheryEntities.POLTERGEIST.get().create(level);
        spirit.setPos(body);
        spirit.setNoAi(true);
        spirit.setSummoner(summoner);
        var drops = new ArrayList<ItemEntity>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item
                    && item.position().equals(Vec3.atCenterOf(target.getBlockPos()))) drops.add(item);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            level.getRandom().setSeed(7);
            spirit.tickCount = 100;
            spirit.aiStep();
            helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().is(Items.APPLE)
                            && drops.get(0).getItem().getCount() == 1 && target.getItem(26).getCount() == 2,
                    "The Poltergeist passes over the empty chest and ejects one item from the nearest occupied chest");
            for (var unopened : List.of(farther, outOfRange)) {
                helper.assertTrue(unopened.saveWithoutMetadata().contains("LootTable"),
                        "Unselected chest loot remains sealed at " + unopened.getBlockPos());
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
            drops.forEach(Entity::discard);
            level.removePlayerImmediately(summoner, Entity.RemovalReason.DISCARDED);
            spirit.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_poltergeist_tie")
    public static void tiedContainersFollowTheirLoadOrder(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(4, 10, 4)).atY(188);
        var east = origin.east(2);
        var west = origin.west(2);
        var body = Vec3.atCenterOf(origin);
        var summoner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "HauntTieOwner"));
        summoner.setPos(body.add(0, 0, 8));
        level.addNewPlayer(summoner);
        var spirit = WitcheryEntities.POLTERGEIST.get().create(level);
        spirit.setPos(body);
        spirit.setNoAi(true);
        spirit.setSummoner(summoner);
        try {
            for (var first : List.of(east, west)) {
                var other = first.equals(east) ? west : east;
                var firstChest = placeChest(level, first);
                var secondChest = placeChest(level, other);
                firstChest.setItem(0, new ItemStack(Items.APPLE, 3));
                secondChest.setItem(0, new ItemStack(Items.APPLE, 3));
                spirit.tickCount += 100;
                spirit.aiStep();
                helper.assertTrue(firstChest.getItem(0).getCount() == 2 && secondChest.getItem(0).getCount() == 3,
                        "The first registered chest wins the tie at " + first);
                for (var chest : List.of(firstChest, secondChest)) {
                    chest.clearContent();
                    level.setBlockAndUpdate(chest.getBlockPos(), Blocks.AIR.defaultBlockState());
                }
            }
        } finally {
            level.getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(3)).forEach(Entity::discard);
            level.removePlayerImmediately(summoner, Entity.RemovalReason.DISCARDED);
            spirit.discard();
        }
        helper.succeed();
    }

    private static ChestBlockEntity placeChest(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
        return (ChestBlockEntity) level.getBlockEntity(pos);
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_spectral_air")
    public static void submergedSpectralUndeadPreserveAirSupply(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(4, 10, 4));
        for (var fluid : List.of(Blocks.WATER, WitcheryBlocks.getRequired("spiritflowing").get())) {
            var state = fluid.defaultBlockState();
            var fluidType = state.getFluidState().getFluidType();
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, 0, -1), origin.offset(1, 2, 1))) {
                level.setBlockAndUpdate(pos, state);
            }
            for (var kind : SpectralUndead.Kind.values()) {
                var spirit = kind.type().create(level);
                spirit.setPos(Vec3.atBottomCenterOf(origin));
                spirit.setNoAi(true);
                spirit.setAirSupply(1);
                for (int tick = 0; tick < 25; tick++) level.tickNonPassenger(spirit);
                helper.assertTrue(spirit.getEyeInFluidType() == fluidType && spirit.getAirSupply() == 1 && spirit.getHealth() == 20,
                        kind + " keeps its original air supply when submerged in " + fluid.getName().getString());
                spirit.discard();
            }
        }
        helper.succeed();
    }
}
