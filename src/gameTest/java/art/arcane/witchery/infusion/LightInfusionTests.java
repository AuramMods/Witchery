package art.arcane.witchery.infusion;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BarrierBlock;
import art.arcane.witchery.block.entity.BarrierBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LightInfusionTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_light_hand")
    public static void liftingCageUsesOriginalLandingCorner(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = caster(helper);
        var pig = EntityType.PIG.create(level);
        var base = helper.absolutePos(new BlockPos(3, 0, 4)).atY(200);
        pig.moveTo(base.getX() + 0.75, base.getY() + 0.25, base.getZ() + 0.25, 0, 0);
        var hand = new ItemStack(WitcheryItems.getRequired("witchhand").get());
        hand.getItem().onLeftClickEntity(hand, caster, pig);

        var raised = base.above(4);
        var barrier = WitcheryBlocks.getRequired("barrier").get();
        helper.assertTrue(pig.position().equals(Vec3.atLowerCornerOf(raised)),
                "The lifted target lands on the lower corner of its original cage block");
        helper.assertTrue(level.getBlockState(raised.below()).is(barrier)
                        && level.getBlockState(raised.east(2)).is(barrier)
                        && level.getBlockState(raised.above(3)).is(barrier),
                "The free-standing cage surrounds the corner with a floor, wall and roof");
        helper.assertTrue(caster.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG)
                        .getInt("witcheryInfusionCharges") == 15,
                "A successful lift spends five charges");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_light_hand")
    public static void shieldAndProjectionRespectNonreplaceableCircuits(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = caster(helper);
        var wall = helper.absolutePos(new BlockPos(3, 0, 2)).atY(202);
        var torch = wall.south(3);
        level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(torch.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(torch, Blocks.TORCH.defaultBlockState());
        caster.moveTo(wall.getX() + 0.5, wall.getY() - 0.85, wall.getZ() + 6.5, 180, 0);
        caster.setShiftKeyDown(true);
        var hand = new ItemStack(WitcheryItems.getRequired("witchhand").get());
        hand.getItem().releaseUsing(hand, level, caster, hand.getUseDuration() - 5);

        var barrier = WitcheryBlocks.getRequired("barrier").get();
        helper.assertTrue(level.getBlockState(wall.south()).is(barrier) && level.getBlockState(wall.south(2)).is(barrier),
                "The side cast projects a barrier through two replaceable blocks");
        helper.assertTrue(level.getBlockState(torch).is(Blocks.TORCH) && level.isEmptyBlock(wall.south(4)),
                "The projection stops at a torch and preserves the empty space beyond it");

        var shield = wall.east(4);
        level.setBlockAndUpdate(shield, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(shield.above(), Blocks.TORCH.defaultBlockState());
        var previous = shield.above(2);
        BarrierBlock.place(level, previous, null, true, 4);
        caster.setYRot(0);
        LightInfusion.shield(level, shield, caster);
        var renewed = (BarrierBlockEntity) level.getBlockEntity(previous);
        helper.assertTrue(level.getBlockState(shield.above()).is(Blocks.TORCH)
                        && level.getBlockState(shield.above(3)).is(barrier)
                        && renewed.saveWithoutMetadata().getInt("remainingTicks") == 200,
                "The shield skips the torch, fills above it and refreshes existing barrier blocks");
        helper.succeed();
    }

    private static FakePlayer caster(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "LightHand"));
        InfusionEnergy.grant(player, LightInfusion.ID, 20);
        return player;
    }
}
