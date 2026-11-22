package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerData.WolfmanQuestState;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.vampirism.VampireAbilities;
import art.arcane.witchery.vampirism.VampireFlight;
import art.arcane.witchery.vampirism.VampireUltimate;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WolfTokenTests {
    @GameTest(template = "qa_coven_arena")
    public static void wolfTokenCompletesAFullWerewolfCycle(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var player = new TokenPlayer(level);
        var data = WitcheryPlayerDataProvider.get(player).resolve().orElseThrow();
        var stack = WitcheryIngredient.loadInventoryStack(TagParser.parseTag(
                "{id:\"witchery:wolftoken\",Damage:0s,Count:1b,tag:{Keepsake:1b}}"));
        var token = WitcheryItems.getRequired("wolftoken").get();
        helper.assertTrue(stack.is(token) && stack.getMaxStackSize() == 1 && stack.getRarity() == Rarity.EPIC
                        && stack.getTag().getBoolean("Keepsake") && stack.getUseAnimation() == UseAnim.BOW
                        && stack.getUseDuration() == 20,
                "The original saved token keeps its data, epic rarity and one-second draw");
        var tooltip = new ArrayList<Component>();
        token.appendHoverText(stack, level, tooltip, TooltipFlag.Default.NORMAL);
        helper.assertTrue(tooltip.size() == 3 && level.getRecipeManager().getRecipes().stream()
                        .noneMatch(recipe -> recipe.getResultItem(level.registryAccess()).is(token)),
                "The token has its original instructions and no crafting recipe");

        try {
            var hand = InteractionHand.MAIN_HAND;
            startUse(helper, player, hand, stack);
            player.tickItemUse(19);
            helper.assertTrue(player.isUsingItem() && data.getWerewolfLevel() == 0,
                    "The rank is still zero before the last use tick");
            player.releaseUsingItem();
            helper.assertTrue(data.getWerewolfLevel() == 0, "Releasing early leaves the rank alone");

            for (int rank = 1; rank <= 10; rank++) {
                startUse(helper, player, hand, stack);
                player.tickItemUse(20);
                helper.assertTrue(data.getWerewolfLevel() == rank && data.getVampireLevel() == 0
                                && player.getMainHandItem().is(token) && player.getMainHandItem().getCount() == 1,
                        "Ordinary use reaches werewolf rank " + rank + " and keeps the token");
            }
            data.setWolfmanQuestState(WolfmanQuestState.STARTED);
            data.advanceWolfmanQuest();
            data.markWolfmanQuestChunk(3, 7);
            player.setShiftKeyDown(true);
            helper.assertTrue(Lycanthropy.useMoonCharm(player) && Lycanthropy.isWolfman(player),
                    "The rank-ten player can enter Wolfman form");

            startUse(helper, player, hand, stack);
            player.tickItemUse(19);
            player.setShiftKeyDown(false);
            player.tickItemUse(1);
            helper.assertTrue(data.getWerewolfLevel() == 0 && data.getVampireLevel() == 0
                            && !Lycanthropy.isShifted(player) && data.getWolfmanQuestState() == WolfmanQuestState.NOT_STARTED
                            && data.getWolfmanQuestCounter() == 0 && data.markWolfmanQuestChunk(3, 7),
                    "Standing on the last tick selects the wolf path and the wrap clears form and trial progress");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void wolfTokenCompletesAFullVampireCycle(GameTestHelper helper) {
        var player = new TokenPlayer(helper.getLevel());
        var data = WitcheryPlayerDataProvider.get(player).resolve().orElseThrow();
        var token = new ItemStack(WitcheryItems.getRequired("wolftoken").get());
        try {
            var hand = InteractionHand.OFF_HAND;
            Lycanthropy.setRank(player, 5);
            player.setShiftKeyDown(true);
            player.getInventory().selected = 1;
            for (int rank = 1; rank <= 10; rank++) {
                if (rank == 2) {
                    data.setBloodPower(350);
                    data.setSelectedVampirePower(VampireAbilities.DRINK);
                    data.setVampireQuestCounter(3);
                    data.visitVampireVillage("minecraft:overworld", 4, 2);
                }
                startUse(helper, player, hand, token);
                player.tickItemUse(20);
                helper.assertTrue(data.getVampireLevel() == rank && data.getWerewolfLevel() == 5
                                && data.getHumanBlood() == 0 && data.getBloodPower() == (rank == 1 ? 125 : 350)
                                && data.getSelectedVampirePower() == 0 && data.getVampireQuestCounter() == 0,
                        "Sneak-use reaches vampire rank " + rank + " with original blood and trial values");
                if (rank == 2) helper.assertTrue(data.visitVampireVillage("minecraft:overworld", 4, 2),
                        "Changing rank forgets visited trial villages");
            }
            helper.assertTrue(data.getVampireLevelCap() < 10, "The creative token bypasses the journal rank limit");
            player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            data.setVampireVision(true);
            player.getInventory().selected = 0;
            VampireAbilities.select(player, VampireAbilities.BAT);
            VampireAbilities.activate(player);
            helper.assertTrue(VampireFlight.isBat(player) && player.getAbilities().flying,
                    "The rank-ten player can enter Bat form");
            helper.assertTrue(VampireAbilities.chargeUltimate(player, VampireUltimate.SWARM), "An ultimate can be charged");
            player.getInventory().selected = 1;
            startUse(helper, player, hand, token);
            player.tickItemUse(20);
            helper.assertTrue(data.getVampireLevel() == 0 && data.getWerewolfLevel() == 5
                            && data.getBloodPower() == 0 && data.getHumanBlood() == 50
                            && data.getVampireUltimate() == VampireUltimate.NONE && data.getVampireUltimateCharges() == 0
                            && !VampireFlight.isBat(player) && !player.getAbilities().mayfly && data.hasVampireVision()
                            && player.getOffhandItem().is(token.getItem()),
                    "Wrapping restores human blood and flight, clears the ultimate and keeps the saved vision preference");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    private static void startUse(GameTestHelper helper, TokenPlayer player, InteractionHand hand, ItemStack stack) {
        player.setItemInHand(hand, stack);
        var result = player.gameMode.useItem(player, helper.getLevel(), stack, hand);
        helper.assertTrue(result.consumesAction() && player.isUsingItem(),
                "Use starts drawing the token in slot " + player.getInventory().selected + " with result " + result);
    }

    private static final class TokenPlayer extends FakePlayer {
        private TokenPlayer(ServerLevel level) {
            super(level, new GameProfile(UUID.randomUUID(), "WolfTokenQA"));
            gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        }

        private void tickItemUse(int ticks) {
            for (int tick = 0; tick < ticks && isUsingItem(); tick++) updateUsingItem(getUseItem());
        }
    }
}
