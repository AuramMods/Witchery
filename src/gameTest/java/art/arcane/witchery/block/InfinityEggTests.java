package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class InfinityEggTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_infinity_egg", timeoutTicks = 100)
    public static void fallingEggLandsOnTheAltarAndStillMinesNormally(GameTestHelper helper) {
        var level = helper.getLevel();
        var egg = WitcheryBlocks.getRequired("infinityegg").get();
        var altar = WitcheryBlocks.getRequired("altar").get();
        var corner = helper.absolutePos(new BlockPos(5, 1, 4));
        for (var pos : BlockPos.betweenClosed(corner, corner.offset(2, 0, 1))) {
            level.setBlockAndUpdate(pos, altar.defaultBlockState());
        }
        var landing = corner.above();
        var origin = landing.above(4);
        for (var pos : BlockPos.betweenClosed(landing, origin.above())) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(origin.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(origin, egg.defaultBlockState());
        var grass = origin.east(8).below();
        level.setBlockAndUpdate(grass.below(), Blocks.GRASS_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(grass, Blocks.GRASS.defaultBlockState());
        level.setBlockAndUpdate(grass.above(), egg.defaultBlockState());
        AltarBlockEntity.form(level, corner);
        var core = (AltarBlockEntity) level.getBlockEntity(corner);

        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "InfinityEggMiner"));
        player.setPos(origin.getX() + 0.5, origin.getY() - 2.0, origin.getZ() + 2.5);
        var gold = new ItemStack(Items.GOLD_BLOCK);
        player.setItemInHand(InteractionHand.MAIN_HAND, gold);
        var hit = new BlockHitResult(Vec3.atCenterOf(origin).add(0, 0.5, 0), Direction.UP, origin, false);
        var use = player.gameMode.useItemOn(player, level, gold, InteractionHand.MAIN_HAND, hit);
        level.getBlockState(origin).attack(level, origin, player);
        helper.assertTrue(use.consumesAction() && gold.getCount() == 1 && level.isEmptyBlock(origin.above())
                        && level.getBlockState(origin).is(egg), "Both clicks leave the egg in place and use does not place the held block");

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(landing)
                        && core.capacity() >= 1000 && core.rechargeMultiplier() == 1,
                "The altar counts the supported egg as nearby nature before it lands"))
                .thenExecute(() -> {
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
                    helper.assertTrue(player.gameMode.destroyBlock(origin.below()), "Mine the egg's support in survival");
                    helper.assertTrue(level.getBlockState(origin).is(egg), "Removing the support schedules the fall");
                }).thenWaitUntil(() -> helper.assertTrue(level.isEmptyBlock(origin)
                        && !level.getEntitiesOfClass(FallingBlockEntity.class, new AABB(landing).expandTowards(0, 6, 0),
                        falling -> falling.getBlockState().is(egg)).isEmpty(), "The Infinity Egg descends as a falling block"))
                .thenWaitUntil(() -> {
                    helper.assertTrue(level.getBlockState(landing).is(egg),
                            "The Infinity Egg must land above the altar, found " + level.getBlockState(landing));
                    helper.assertTrue(core.capacity() >= 10_000 && core.rechargeMultiplier() == 10,
                            "The landed egg boosts the altar tenfold, found capacity " + core.capacity()
                                    + " and recharge multiplier " + core.rechargeMultiplier());
                    helper.assertTrue(level.getBlockState(grass.above()).is(egg) && level.getBlockState(grass).is(Blocks.GRASS),
                            "The other egg stays on grass, found " + level.getBlockState(grass.above())
                                    + " and support " + level.getBlockState(grass));
                })
                .thenExecute(() -> {
                    helper.assertTrue(player.gameMode.destroyBlock(landing) && level.isEmptyBlock(landing), "Mine the landed egg");
                    var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(landing).inflate(1),
                            item -> item.getItem().is(egg.asItem()));
                    helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().getCount() == 1
                                    && core.rechargeMultiplier() == 1,
                            "Mining recovers one egg and immediately clears the recharge bonus");
                }).thenSucceed();
    }
}
