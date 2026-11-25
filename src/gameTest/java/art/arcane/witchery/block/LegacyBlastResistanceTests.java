package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyBlastResistanceTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_blast_resistance")
    public static void legacyBlastThresholdsKeepTheirDrops(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(6, 4, 5));
        level.setBlockAndUpdate(pos.below(), Blocks.OBSIDIAN.defaultBlockState());
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(pos),
                        "The blast fixture must track dropped items"))
                .thenExecute(() -> {
                    var planks = state("witchwood").setValue(WitchWoodType.PROPERTY, WitchWoodType.HAWTHORN);
                    breakWithBlast(helper, pos, planks, 0.7F, WitchWoodType.HAWTHORN.stack(planks.getBlock()));
                    var stockade = state("stockade").setValue(StockadeBlock.WOOD, StockadeBlock.Wood.HAWTHORN);
                    breakWithBlast(helper, pos, stockade, 4.0F, StockadeBlock.Wood.HAWTHORN.stack(stockade.getBlock()));
                    var iceStockade = state("icestockade");
                    breakWithBlast(helper, pos, iceStockade, 4.0F, new ItemStack(iceStockade.getBlock()));
                    var bearTrap = state("beartrap");
                    breakWithBlast(helper, pos, bearTrap, 2.0F, new ItemStack(bearTrap.getBlock()));
                    breakWithBlast(helper, pos, state("wolftrap"), 2.0F, ItemStack.EMPTY);
                    breakWithBlast(helper, pos, state("perpetualice"), 1.0F, ItemStack.EMPTY);
                    for (String path : List.of("barrier", "circleglyphritual", "scarecrow", "mirrorwall")) {
                        var protectedBlock = state(path);
                        level.setBlockAndUpdate(pos, protectedBlock);
                        helper.assertTrue(level.getBlockState(pos).is(protectedBlock.getBlock()), "Place " + path);
                        blast(level, pos, 4.0F);
                        helper.assertTrue(level.getBlockState(pos).is(protectedBlock.getBlock()),
                                path + " still withstands a normal TNT-strength blast");
                    }
                }).thenSucceed();
    }

    private static BlockState state(String path) {
        return WitcheryBlocks.getRequired(path).get().defaultBlockState();
    }

    private static void breakWithBlast(GameTestHelper helper, BlockPos pos, BlockState state, float power, ItemStack drop) {
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, state);
        helper.assertTrue(level.getBlockState(pos).is(state.getBlock()), "Place " + state);
        blast(level, pos, power);
        helper.assertTrue(level.isEmptyBlock(pos) && level.getBlockEntity(pos) == null, "Destroy " + state);
        var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1));
        helper.assertTrue(drop.isEmpty() ? drops.isEmpty() : drops.size() == 1 && ItemStack.matches(drop, drops.get(0).getItem()),
                "The blast produces the original item and wood variant for " + state);
        drops.forEach(ItemEntity::discard);
    }

    private static void blast(Level level, BlockPos pos, float power) {
        var center = Vec3.atCenterOf(pos);
        level.explode(null, center.x, center.y, center.z, power, false, Level.ExplosionInteraction.TNT);
    }
}
