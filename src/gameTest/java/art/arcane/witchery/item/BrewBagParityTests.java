package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.menu.BrewBagMenu;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewBagParityTests {
    private static final EnumSet<WitcheryIngredient> ORIGINAL_THROWABLES = EnumSet.of(
            WitcheryIngredient.BREW_OF_VINES, WitcheryIngredient.BREW_OF_WEBS, WitcheryIngredient.BREW_OF_THORNS,
            WitcheryIngredient.BREW_OF_INK, WitcheryIngredient.BREW_OF_SPROUTING, WitcheryIngredient.BREW_OF_EROSION,
            WitcheryIngredient.BREW_OF_RAISING, WitcheryIngredient.BREW_OF_LOVE, WitcheryIngredient.BREW_OF_ICE,
            WitcheryIngredient.BREW_OF_FROGS_TONGUE, WitcheryIngredient.BREW_OF_CURSED_LEAPING,
            WitcheryIngredient.BREW_OF_HITCHCOCK, WitcheryIngredient.BREW_OF_INFECTION,
            WitcheryIngredient.BREW_OF_FLOWING_SPIRIT, WitcheryIngredient.BREW_OF_WASTING, WitcheryIngredient.BREW_OF_BATS,
            WitcheryIngredient.BREW_OF_HOLLOW_TEARS, WitcheryIngredient.BREW_OF_SOLID_ROCK, WitcheryIngredient.BREW_OF_SOLID_DIRT,
            WitcheryIngredient.BREW_OF_SOLID_SAND, WitcheryIngredient.BREW_OF_SOLID_SANDSTONE,
            WitcheryIngredient.BREW_OF_SOLID_EROSION, WitcheryIngredient.BREW_OF_REVEALING,
            WitcheryIngredient.BREW_OF_SUBSTITUTION);

    @GameTest(template = "qa_coven_arena", batch = "witchery_bag_original_throwables", timeoutTicks = 120)
    public static void storesReloadsAndThrowsEveryOriginalFixedBrew(GameTestHelper helper) {
        for (var ingredient : WitcheryIngredient.values()) {
            helper.assertTrue(BrewBagItem.accepts(new ItemStack(ingredient.item())) == ORIGINAL_THROWABLES.contains(ingredient),
                    "Original Brew Bag eligibility for " + ingredient);
        }

        var level = helper.getLevel();
        var spawn = helper.absolutePos(new BlockPos(4, 3, 4));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrewBagParity"));
        player.setPos(Vec3.atBottomCenterOf(spawn));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(spawn),
                "The throw position is ticking")).thenExecute(() -> {
            var shots = new ArrayList<ThrownBrew>();
            Consumer<EntityJoinLevelEvent> observe = event -> {
                if (event.getLevel() == level && event.getEntity() instanceof ThrownBrew shot && shot.getOwner() == player) {
                    shots.add(shot);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(observe);
            try {
                for (var ingredient : ORIGINAL_THROWABLES) {
                    var filled = new ItemStack(ingredient.item(), 2);
                    filled.getOrCreateTag().putString("Fixture", ingredient.name());
                    var single = filled.copyWithCount(1);
                    var bag = new ItemStack(WitcheryItems.getRequired("brewbag").get());
                    player.setItemInHand(InteractionHand.MAIN_HAND, bag);
                    player.getInventory().setItem(9, filled);
                    var menu = new BrewBagMenu(1, player.getInventory(), bag, 0);
                    helper.assertTrue(!menu.quickMoveStack(player, BrewBagItem.SLOT_COUNT).isEmpty(),
                            "Shift-click stores " + ingredient);

                    var reopened = ItemStack.of(bag.save(new CompoundTag()));
                    player.setItemInHand(InteractionHand.MAIN_HAND, reopened);
                    player.setXRot(0);
                    reopened.getItem().use(level, player, InteractionHand.MAIN_HAND);
                    player.setXRot(-10);
                    player.releaseUsingItem();

                    helper.assertTrue(shots.size() == 1 && ItemStack.matches(shots.get(0).getItem(), single),
                            "The up gesture throws one matching " + ingredient + " projectile");
                    helper.assertTrue(ItemStack.matches(BrewBagItem.contents(reopened).get(0), single),
                            "The reopened bag retains one tagged serving of " + ingredient);
                    shots.remove(0).discard();
                }
            } finally {
                MinecraftForge.EVENT_BUS.unregister(observe);
                shots.forEach(ThrownBrew::discard);
            }
        }).thenSucceed();
    }
}
