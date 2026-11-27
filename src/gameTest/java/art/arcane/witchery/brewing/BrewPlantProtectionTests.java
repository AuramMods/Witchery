package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewPlantProtectionTests {
    @GameTest(template = "qa_coven_arena")
    public static void cauldronPlantsHonorBreakVetoesAtEmptyRoots(GameTestHelper helper) {
        var level = helper.getLevel();
        var actor = FakePlayerFactory.getMinecraft(level);
        var web = helper.absolutePos(new BlockPos(3, 5, 5));
        var cactus = web.east(4);
        var lily = web.south(4);
        var roots = List.of(web, cactus, lily);
        for (BlockPos root : roots) {
            level.setBlockAndUpdate(root.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(root, Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(web.south(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(cactus.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(lily.below(), Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(lily.above(2), Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(lily.above(3), Blocks.AIR.defaultBlockState());
        var seen = new ArrayList<BlockPos>();
        Consumer<BlockEvent.BreakEvent> protect = event -> {
            if (event.getLevel() == level && event.getPlayer() == actor && roots.contains(event.getPos())) {
                helper.assertTrue(event.getState().isAir(), "The original root check observes air");
                seen.add(event.getPos());
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(protect);
        try {
            BrewPlantEffects.webs(level, web.below(), Direction.UP, 2, actor);
            BrewPlantEffects.thorns(level, cactus.below(), Direction.UP, 1, actor);
            BrewTerrainEffects.lilify(level, lily.below().west(), Direction.EAST, 0, actor);
            helper.assertTrue(seen.equals(roots) && roots.stream().allMatch(level::isEmptyBlock),
                    "Webs, Thorns, and Lilify preserve each protected air cell");
            helper.assertTrue(level.isEmptyBlock(web.south()) && level.isEmptyBlock(cactus.above())
                            && level.isEmptyBlock(lily.above(3)),
                    "Denied roots also prevent expansion, growth, and retries above the water");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protect);
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void cauldronWebVisitsFacesAndEdgesInOriginalOrder(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(6, 5, 6));
        var web = WitcheryBlocks.getRequired("web").get();
        var actor = FakePlayerFactory.getMinecraft(level);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(center.below(), Blocks.STONE.defaultBlockState());
        var seen = new ArrayList<BlockPos>();
        Consumer<BlockEvent.EntityPlaceEvent> capture = event -> {
            if (event.getLevel() == level && event.getEntity() == actor && event.getPlacedBlock().is(web)) {
                seen.add(event.getPos());
            }
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            var brew = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(),
                    Items.GLOWSTONE_DUST.getDefaultInstance(), Items.BLAZE_ROD.getDefaultInstance(),
                    WitcheryIngredient.WEB.item().getDefaultInstance(), Items.GUNPOWDER.getDefaultInstance()));
            helper.assertTrue(brew != null, "The strength-two Webs recipe is valid");
            brew.applySplashToBlock(level, center.below(), Direction.UP, Vec3.atBottomCenterOf(center), actor);
            helper.assertTrue(seen.equals(List.of(center, center.east(), center.west(), center.south(), center.north(),
                    center.above(), center.east().south(), center.west().south(), center.east().north(), center.west().north(),
                    center.above().east(), center.above().west(), center.above().south(), center.above().north(),
                    center.below().south(), center.below().north(), center.below().east(), center.below().west())),
                    "The original web order is center, faces, then edges");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
    }
}
