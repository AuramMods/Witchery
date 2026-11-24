package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.BrazierBlockEntity;
import art.arcane.witchery.crafting.BrazierRecipe;
import art.arcane.witchery.infusion.InfusedBrews;
import art.arcane.witchery.registry.WitcheryBlocks;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

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

    @GameTest(template = "qa_coven_arena", batch = "witchery_brazier_behavior")
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
        infused.setHealth(10);
        level.addNewPlayer(infused);
        try {
            InfusedBrews.applyGrave(infused);
            var outside = EntityType.ZOMBIE.create(level);
            var spot = Vec3.atLowerCornerOf(pos).add(3.75, 0, 0.5);
            outside.setPos(spot);
            outside.setHealth(10);
            level.addFreshEntity(outside);
            int storage = BrazierRecipe.WILTING.burn(level, pos, 5);
            helper.assertTrue(infused.getHealth() == 12 && outside.getHealth() == 10 && storage == 2,
                    "Expected the first wilt to heal only the Grave-infused player and add burn time. Player health "
                            + infused.getHealth() + ", outside Zombie health " + outside.getHealth()
                            + ", storage " + storage);
        } finally {
            level.removePlayerImmediately(infused, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    private static BrazierBlockEntity place(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(5, 5, 5));
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("brazier").get().defaultBlockState());
        return (BrazierBlockEntity) level.getBlockEntity(pos);
    }
}
