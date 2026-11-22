package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class HobgoblinHaulTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_covered_storage", setupTicks = 5, timeoutTicks = 180)
    public static void haulsToAContainerFromItsWalkableSide(GameTestHelper helper) {
        BlockPos start = new BlockPos(4, 280, 5);
        BlockPos storage = start.east(5);
        for (var pos : BlockPos.betweenClosed(start.offset(-2, -1, -2), storage.offset(2, -1, 2))) {
            helper.setBlock(pos, Blocks.STONE);
        }
        helper.setBlock(storage, Blocks.BARREL);
        helper.setBlock(storage.above(), Blocks.STONE);
        helper.setBlock(storage.above(2), Blocks.STONE);
        var barrel = (BarrelBlockEntity) helper.getBlockEntity(storage);
        var goblin = helper.spawn(WitcheryEntities.GOBLIN.get(), start);
        var holder = helper.spawn(EntityType.ARMOR_STAND, start.south());
        goblin.setLeashedTo(holder, false);
        goblin.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RAW_IRON, 3));
        helper.assertTrue(goblin.distanceToSqr(Vec3.atCenterOf(barrel.getBlockPos())) > 6.25,
                "The goblin starts outside transfer range");
        helper.succeedWhen(() -> {
            helper.assertTrue(barrel.countItem(Items.RAW_IRON) == 3 && goblin.getMainHandItem().isEmpty(),
                    "The leashed goblin walks to the covered barrel and deposits all three ore from the side: position="
                            + goblin.position() + ", ticks=" + goblin.tickCount + ", on ground=" + goblin.onGround()
                            + ", lead=" + goblin.isLeashed() + ", held=" + goblin.getMainHandItem()
                            + ", navigation=" + goblin.getNavigation().getPath() + "/" + goblin.getNavigation().getTargetPos()
                            + ", barrel=" + barrel.getBlockPos());
            helper.assertTrue(goblin.isLeashed(), "The goblin remains on the lead after delivering its load");
        });
    }
}
