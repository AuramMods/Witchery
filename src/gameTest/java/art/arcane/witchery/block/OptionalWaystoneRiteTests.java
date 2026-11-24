package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.GrassperBlockEntity;
import art.arcane.witchery.block.entity.RiteHeartBlockEntity;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.ritual.RitualCircle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class OptionalWaystoneRiteTests {
    private static final BlockPos HEART = new BlockPos(7, 4, 7);

    @GameTest(template = "qa_coven_arena", batch = "witchery_waystone_refund", timeoutTicks = 160, setupTicks = 5)
    public static void refundsSavedGrassperOfferingWhenTheAltarCannotPay(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(HEART.north(2));
        BlockPos target = helper.absolutePos(HEART.west(6));
        helper.setBlock(HEART.north(2).below(), Blocks.STONE);
        helper.setBlock(HEART.north(2), WitcheryBlocks.getRequired("grassper").get());
        var grassper = (GrassperBlockEntity) level.getBlockEntity(origin);
        var stone = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
        WaystoneItem.bindLocation(stone, level, target);
        stone.setHoverName(Component.literal("Western Boundary"));
        grassper.setItem(0, stone);
        startBarrier(helper, heart -> {
            helper.runAfterDelay(64, () -> {
                helper.assertTrue(grassper.isEmpty(), "The rite takes the optional stone before checking altar power");
                var saved = heart.saveWithFullMetadata();
                helper.assertTrue(BlockPos.of(saved.getLong("WaystoneTarget")).equals(target), "The offering step saves its target");
                heart.load(saved);
            });
            helper.runAfterDelay(85, () -> {
                var items = level.getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(1),
                        item -> item.getItem().is(WitcheryIngredient.BOUND_WAYSTONE.item()));
                helper.assertTrue(items.size() == 1 && ItemStack.matches(items.get(0).getItem(), stone),
                        "An underpowered rite returns the original named Waystone where the Grassper held it");
                helper.assertFalse(heart.getBlockState().getValue(RiteHeartBlock.ACTIVE), "The failed rite has ended");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_waystone_target", timeoutTicks = 160, setupTicks = 5)
    public static void usesSavedTargetWithoutTakingASecondWaystone(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos target = helper.absolutePos(HEART.west(6).above(4));
        var stone = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
        WaystoneItem.bindLocation(stone, level, target);
        var offered = drop(helper, stone);
        var altar = chargedAltar(helper);
        startBarrier(helper, heart -> helper.runAfterDelay(64, () -> {
            helper.assertTrue(offered.isRemoved() && altar.power() >= 700, "Taking a Waystone does not debit the altar");
            heart.load(heart.saveWithFullMetadata());
            var laterStone = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
            WaystoneItem.bindLocation(laterStone, level, target.south(4));
            var laterDrop = drop(helper, laterStone);
            helper.runAfterDelay(21, () -> {
                helper.assertTrue(heart.saveWithFullMetadata().getBoolean("AltarPowerOffered") && altar.power() < 400,
                        "The altar pays for the saved target before the ward starts");
            });
            helper.runAfterDelay(41, () -> {
                var wards = heart.saveWithFullMetadata().getList("ProtectionRites", Tag.TAG_COMPOUND);
                helper.assertTrue(wards.size() == 1 && BlockPos.of(wards.getCompound(0).getLong("Target")).equals(target),
                        "A reloaded rite creates the barrier at the first Waystone's destination");
                helper.assertTrue(laterDrop.isAlive() && altar.power() < 400, "The rite charges the altar once and leaves the later stone");
                helper.succeed();
            });
        }));
    }

    private static void startBarrier(GameTestHelper helper, Consumer<RiteHeartBlockEntity> started) {
        var level = helper.getLevel();
        var glyph = WitcheryBlocks.getRequired("circleglyphritual").get();
        for (var offset : RitualCircle.SMALL_RITE.offsets()) {
            var pos = HEART.offset(offset);
            helper.setBlock(pos.below(), Blocks.STONE);
            helper.setBlock(pos, glyph);
        }
        helper.setBlock(HEART.below(), Blocks.STONE);
        helper.setBlock(HEART, WitcheryBlocks.getRequired("circle").get());
        var obsidian = drop(helper, new ItemStack(Items.OBSIDIAN));
        var redstone = drop(helper, new ItemStack(Items.REDSTONE));
        var pos = helper.absolutePos(HEART);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        helper.startSequence().thenWaitUntil(() -> {
            var visible = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos));
            helper.assertTrue(visible.contains(obsidian) && visible.contains(redstone), "The required offerings are in the rite's search area");
        }).thenExecute(() -> {
            level.getBlockState(pos).use(level, helper.makeMockSurvivalPlayer(), InteractionHand.MAIN_HAND, hit);
            var heart = (RiteHeartBlockEntity) level.getBlockEntity(pos);
            helper.assertTrue(heart.getBlockState().getValue(RiteHeartBlock.ACTIVE), "The Heart Glyph starts the Focused Barrier rite");
            started.accept(heart);
        });
    }

    private static ItemEntity drop(GameTestHelper helper, ItemStack stack) {
        var point = Vec3.atCenterOf(helper.absolutePos(HEART));
        var level = helper.getLevel();
        var entity = new ItemEntity(level, point.x, point.y, point.z, stack, 0, 0, 0);
        entity.setNoGravity(true);
        level.addFreshEntity(entity);
        return entity;
    }

    private static AltarBlockEntity chargedAltar(GameTestHelper helper) {
        var corner = HEART.east(5);
        var altar = WitcheryBlocks.getRequired("altar").get();
        for (var pos : BlockPos.betweenClosed(corner, corner.offset(2, 0, 1))) helper.setBlock(pos, altar);
        helper.setBlock(corner.above(), Blocks.WITHER_SKELETON_SKULL);
        helper.setBlock(corner.east().below(), Blocks.STONE);
        helper.setBlock(corner.east().above(), Blocks.DRAGON_EGG);
        var level = helper.getLevel();
        var core = ((AltarBlockEntity) level.getBlockEntity(helper.absolutePos(corner))).resolveCore(level);
        var saved = core.saveWithFullMetadata();
        saved.putFloat("Power", 740);
        core.load(saved);
        return core;
    }
}
