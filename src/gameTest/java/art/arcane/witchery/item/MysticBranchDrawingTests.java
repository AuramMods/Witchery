package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class MysticBranchDrawingTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_mystic_branch_drawing")
    public static void cancelClearsTheGesturePreview(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "BranchDrawing"));
        var branch = (MysticBranchItem) WitcheryItems.getRequired("mysticbranch").get();
        var stack = new ItemStack(branch);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setYRot(179);
        branch.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        player.setYRot(-173);
        branch.onUseTick(helper.getLevel(), player, stack, 35_999);
        helper.assertTrue(MysticBranchItem.strokes(player).equals("→"), "Crossing the yaw boundary draws one right stroke");

        player.stopUsingItem();
        helper.assertTrue(MysticBranchItem.strokes(player).isEmpty(), "Cancelling the use clears the preview");
        branch.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        player.setXRot(8);
        branch.onUseTick(helper.getLevel(), player, stack, 35_999);
        helper.assertTrue(MysticBranchItem.strokes(player).equals("↓"), "The next use begins with an empty drawing");
        player.stopUsingItem();
        helper.succeed();
    }
}
