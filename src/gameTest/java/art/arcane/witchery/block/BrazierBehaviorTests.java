package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.BrazierBlockEntity;
import art.arcane.witchery.crafting.BrazierRecipe;
import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.infusion.InfusedBrews;
import art.arcane.witchery.item.SpectralStoneItem;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.ArrayList;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrazierBehaviorTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_brazier_behavior")
    public static void manualIgnitionSpendsFlintAndSteelWear(GameTestHelper helper) {
        var brazier = place(helper);
        var level = helper.getLevel();
        var pos = brazier.getBlockPos();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrazierLighter"));
        var tool = new ItemStack(Items.FLINT_AND_STEEL);
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        brazier.insert(new ItemStack(Items.GUNPOWDER));

        helper.assertTrue(brazier.getBlockState().use(level, player, InteractionHand.MAIN_HAND, hit).consumesAction()
                        && tool.getDamageValue() == 1 && level.isEmptyBlock(pos.above()),
                "Lighting a nonempty Brazier should spend one durability and leave the space above clear");
        BrazierBlockEntity.serverTick(level, pos, brazier.getBlockState(), brazier);
        helper.assertTrue(brazier.isEmpty() && !brazier.getBlockState().getValue(BrazierBlock.LIT),
                "An incomplete recipe should fizzle and clear the brazier");
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
