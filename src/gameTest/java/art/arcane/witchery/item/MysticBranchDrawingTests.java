package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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

    @GameTest(template = "qa_coven_arena", batch = "witchery_mystic_branch_drawing")
    public static void playsOriginalGestureCuesAtTheCaster(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "GestureCues"));
        var hand = InteractionHand.MAIN_HAND;
        var sounds = new ArrayList<PlayLevelSoundEvent.AtEntity>();
        Consumer<PlayLevelSoundEvent.AtEntity> listener = event -> {
            if (event.getEntity() == player) sounds.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            var branch = WitcheryItems.getRequired("mysticbranch").get();
            var branchStack = new ItemStack(branch);
            player.setItemInHand(hand, branchStack);
            branch.use(level, player, hand);
            for (int angle : new int[]{8, 16, 8, 8}) {
                player.setXRot(angle);
                branch.onUseTick(level, player, branchStack, 35_999);
            }
            helper.assertTrue(sounds.size() == 1, "A prepared Branch symbol plays one harp before release");
            branch.releaseUsing(branchStack, level, player, 35_995);
            player.stopUsingItem();

            branch.use(level, player, hand);
            branch.releaseUsing(branchStack, level, player, 35_999);
            player.stopUsingItem();

            var bag = WitcheryItems.getRequired("brewbag").get();
            var bagStack = new ItemStack(bag);
            var contents = NonNullList.withSize(BrewBagItem.SLOT_COUNT, ItemStack.EMPTY);
            contents.set(0, new ItemStack(WitcheryIngredient.BREW_OF_INK.item()));
            BrewBagItem.saveContents(bagStack, contents);
            player.setItemInHand(hand, bagStack);
            player.setXRot(0);
            bag.use(level, player, hand);
            player.setXRot(8);
            bag.onUseTick(level, player, bagStack, 35_999);
            helper.assertTrue(sounds.size() == 3, "An empty Brew Bag slot plays no preparation cue");
            player.setXRot(0);
            bag.onUseTick(level, player, bagStack, 35_998);
            bag.onUseTick(level, player, bagStack, 35_997);
            helper.assertTrue(BrewBagItem.preparedSlot(player) == 0, "The populated upward slot is prepared");
            player.stopUsingItem();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        var expected = List.of(WitcherySounds.LEGACY_HARP.get(), SoundEvents.NOTE_BLOCK_SNARE.value(),
                SoundEvents.NOTE_BLOCK_SNARE.value(), SoundEvents.ITEM_PICKUP);
        helper.assertTrue(sounds.size() == expected.size(), "Each accepted preparation or rejected cast plays one cue");
        for (int index = 0; index < expected.size(); index++) {
            PlayLevelSoundEvent.AtEntity cue = sounds.get(index);
            SoundEvent sound = expected.get(index);
            float volume = sound == SoundEvents.NOTE_BLOCK_SNARE.value() ? 0.5F : 1;
            SoundSource source = sound == SoundEvents.ITEM_PICKUP ? SoundSource.PLAYERS : SoundSource.RECORDS;
            helper.assertTrue(cue.getSound().value() == sound && cue.getSource() == source
                            && cue.getNewVolume() == volume && cue.getNewPitch() >= 1F / 3 && cue.getNewPitch() <= 0.5F,
                    "The caster's cue keeps the original sound, category, volume and pitch range");
        }
        helper.succeed();
    }
}
