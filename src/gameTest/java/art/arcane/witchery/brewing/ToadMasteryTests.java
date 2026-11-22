package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ToadMasteryTests {
    @GameTest(template = "qa_coven_arena")
    public static void vinesFollowWiderLedgesWithAnActiveToadAtImpact(GameTestHelper helper) {
        var actor = player(helper);
        var familiar = toadRecord(actor);
        var item = (ThrowableBrewItem) WitcheryIngredient.BREW_OF_VINES.item();

        for (boolean active : new boolean[]{false, true}) {
            var wall = new BlockPos(10, 5, active ? 7 : 3);
            helper.setBlock(wall, Blocks.STONE);
            helper.setBlock(wall.below().east(), Blocks.STONE);
            helper.setBlock(wall.below(2).east(3), Blocks.STONE);
            helper.setBlock(wall.above().west(), Blocks.STONE);
            helper.setBlock(wall.above(2).west(3), Blocks.STONE);

            familiar.putBoolean("FamiliarSummoned", !active);
            var brew = new ThrownBrew(actor, helper.getLevel());
            familiar.putBoolean("FamiliarSummoned", active);
            helper.assertTrue(BrewImpacts.enhanced(brew) == active, "The projectile must read the toad state at impact");
            var impact = helper.absolutePos(wall);
            item.impact(brew, new BlockHitResult(Vec3.atCenterOf(impact).add(0.5, 0, 0),
                    Direction.EAST, impact, false));

            assertWestFacingVine(helper, wall.east());
            assertWestFacingVine(helper, wall.below().east(2));
            assertWestFacingVine(helper, wall.above());
            var lowerLedge = wall.below(2).east(4);
            var upperLedge = wall.above(2).west(2);
            if (active) {
                assertWestFacingVine(helper, lowerLedge);
                assertWestFacingVine(helper, upperLedge);
            } else {
                helper.assertBlockNotPresent(Blocks.VINE, lowerLedge);
                helper.assertBlockNotPresent(Blocks.VINE, upperLedge);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void expertBottlingUsesTheTwoToadYieldTiers(GameTestHelper helper) {
        var actor = player(helper);
        var familiar = toadRecord(actor);
        var mixture = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(), Items.SUGAR.getDefaultInstance()));
        var data = WitcheryPlayerDataProvider.get(actor).orElseThrow(IllegalStateException::new);
        actor.setItemSlot(EquipmentSlot.HEAD, WitcheryItems.getRequired("witchhat").get().getDefaultInstance());
        actor.setItemSlot(EquipmentSlot.CHEST, WitcheryItems.getRequired("witchrobe").get().getDefaultInstance());
        actor.addEffect(new MobEffectInstance(WitcheryEffects.BREWING_EXPERTISE.get(), 200, 2));
        familiar.putBoolean("FamiliarSummoned", true);
        data.setPotionBottling(59);
        helper.assertTrue(BrewBottling.portion(actor, mixture) == 1000, "The expert familiar tiers require sixty skill points");
        data.setPotionBottling(60);
        helper.assertTrue(BrewBottling.portion(actor, mixture) == 200, "Two garment points and a toad give fifteen servings");
        actor.setItemSlot(EquipmentSlot.HEAD, WitcheryItems.getRequired("babashat").get().getDefaultInstance());
        helper.assertTrue(BrewBottling.portion(actor, mixture) == 100, "Baba's Hat and robes give thirty servings with a toad");
        familiar.putBoolean("FamiliarSummoned", false);
        helper.assertTrue(BrewBottling.portion(actor, mixture) == 300, "Dismissing the toad restores ten servings");
        helper.succeed();
    }

    private static void assertWestFacingVine(GameTestHelper helper, BlockPos relative) {
        var absolute = helper.absolutePos(relative);
        var state = helper.getLevel().getBlockState(absolute);
        helper.assertTrue(state.is(Blocks.VINE) && state.getValue(VineBlock.WEST),
                "Expected a west-facing vine at " + relative + ", found " + state);
    }

    private static FakePlayer player(GameTestHelper helper) {
        return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ToadBrewFixture"));
    }

    private static CompoundTag toadRecord(FakePlayer actor) {
        var record = new CompoundTag();
        record.putInt("FamiliarType", 2);
        var persisted = new CompoundTag();
        persisted.put("WITCFamiliar", record);
        actor.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        return record;
    }
}
