package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.ritual.CookingRite;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CookingRiteTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_cooking")
    public static void poweredCookingReturnsOriginalDrops(GameTestHelper helper) {
        checkDrops(helper, false, 1);
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_cooking")
    public static void portableCookingReturnsOriginalDrops(GameTestHelper helper) {
        checkDrops(helper, true, 3);
    }

    private static void checkDrops(GameTestHelper helper, boolean portable, int burned) {
        ServerLevel level = helper.getLevel();
        BlockPos heart = helper.absolutePos(new BlockPos(8, 8, 8));
        Vec3 corner = Vec3.atLowerCornerOf(heart);
        var east = drop(level, corner.add(5.25, 0.1, 0.5), new ItemStack(Items.PORKCHOP, 16));
        var west = drop(level, corner.add(-4.75, 0.1, 0.5), new ItemStack(Items.PORKCHOP));
        var high = drop(level, corner.add(0.5, 1.25, 0.5), new ItemStack(Items.PORKCHOP));
        var ore = drop(level, corner.add(1.5, 0.1, 1.5), new ItemStack(Items.RAW_IRON));

        helper.startSequence().thenWaitUntil(() -> {
            for (var item : new ItemEntity[]{east, west, high, ore}) {
                helper.assertTrue(level.getEntity(item.getUUID()) == item, "The original cooking input is tracked");
            }
        }).thenExecute(() -> {
            level.random.setSeed(0);
            helper.assertTrue(CookingRite.cook(level, heart, portable), "The east-edge food can be cooked");
            helper.assertTrue(east.isRemoved() && west.isAlive() && high.isAlive() && ore.isAlive(),
                    "Cooking uses the original centered radius, one-block layer, and edible smelting filter");

            var outputs = level.getEntitiesOfClass(ItemEntity.class, new AABB(heart), item ->
                    item.getItem().is(Items.COOKED_PORKCHOP) || item.getItem().is(Items.CHARCOAL));
            helper.assertTrue(outputs.size() == 2, "The cast drops both cooked porkchops and charcoal");
            for (var output : outputs) {
                int count = output.getItem().is(Items.CHARCOAL) ? burned : 16 - burned;
                helper.assertTrue(output.getItem().getCount() == count, "The original seed yields the same burn count");
                helper.assertTrue(output.position().equals(corner.add(0, 0.05, 0))
                                && output.getDeltaMovement().equals(new Vec3(0, 0.2, 0)) && !output.hasPickUpDelay(),
                        "Each output starts at the glyph's lower corner with upward motion and immediate pickup");
                output.discard();
            }
            helper.assertTrue(!CookingRite.cook(level, heart, portable), "An ore and out-of-range food cannot complete a cast");
            west.discard();
            high.discard();
            ore.discard();
        }).thenSucceed();
    }

    private static ItemEntity drop(ServerLevel level, Vec3 point, ItemStack stack) {
        var item = new ItemEntity(level, point.x, point.y, point.z, stack, 0, 0, 0);
        item.setNoGravity(true);
        item.setNeverPickUp();
        level.addFreshEntity(item);
        return item;
    }
}
