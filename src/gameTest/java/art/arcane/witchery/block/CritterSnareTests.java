package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.AnimalMerchant;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CritterSnareTests {
    @GameTest(template = "qa_coven_arena")
    public static void stockedLegacySnareReleasesABatWithStock(GameTestHelper helper) throws Exception {
        var player = helper.makeMockSurvivalPlayer();
        var stack = WitcheryIngredient.loadInventoryStack(TagParser.parseTag("{id:\"witchery:crittersnare\",Count:1b,Damage:9s}"));
        var relative = new BlockPos(5, 3, 5);
        helper.setBlock(relative.below(), Blocks.STONE);
        BlockPos pos = helper.absolutePos(relative);
        BlockPos support = pos.below();
        player.moveTo(Vec3.atCenterOf(pos.south(2)));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var hit = new BlockHitResult(Vec3.atCenterOf(support).add(0, 0.5, 0), Direction.UP, support, false);
        helper.assertTrue(stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction(),
                "Place the original stocked Critter Snare");
        var level = helper.getLevel();
        var placed = level.getBlockState(pos);
        helper.assertTrue(placed.getValue(CritterSnareBlock.CRITTER) == CritterSnareBlock.Critter.BAT
                && placed.getValue(CritterSnareBlock.STOCKED), "The captive and its stock survive item migration and placement");
        player.setShiftKeyDown(true);
        helper.useBlock(relative, player);
        var bats = level.getEntitiesOfClass(Bat.class, new AABB(pos).inflate(2));
        helper.assertTrue(bats.size() == 1 && bats.get(0).getPersistentData().contains(AnimalMerchant.STOCK_TAG, Tag.TAG_COMPOUND)
                        && level.getBlockState(pos).getValue(CritterSnareBlock.CRITTER) == CritterSnareBlock.Critter.EMPTY,
                "Sneak-use empties the snare and returns a bat with its trade stock marker");
        helper.succeed();
    }
}
