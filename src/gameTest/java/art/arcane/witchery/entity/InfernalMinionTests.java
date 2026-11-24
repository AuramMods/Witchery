package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class InfernalMinionTests {
    @GameTest(template = "qa_coven_arena")
    public static void infernalHandAndImperioRespectLegacyBossImmunity(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "InfernalMinionQA"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        InfusionEnergy.grant(player, InfernalInfusion.ID, 20);
        var hand = WitcheryItems.getRequired("witchhand").get().getDefaultInstance();
        try {
            var bosses = List.of(WitcheryEntities.BABA_YAGA, WitcheryEntities.LORD_OF_TORMENT, WitcheryEntities.DEATH,
                    WitcheryEntities.LILITH, WitcheryEntities.MOG, WitcheryEntities.GULG, WitcheryEntities.HORNED_HUNTSMAN,
                    WitcheryEntities.REFLECTION, WitcheryEntities.LEONARD);
            for (var type : bosses) {
                var boss = type.get().create(level);
                hand.getItem().onLeftClickEntity(hand, player, boss);
                InfernalInfusion.castImperio(player, boss);
                helper.assertTrue(!InfernalInfusion.owns(boss, player) && !boss.hasEffect(WitcheryEffects.ENSLAVED.get()),
                        type.getId() + " is immune to the Infernal Hand and Imperio");
                boss.discard();
            }
            helper.assertTrue(player.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG)
                    .getInt("witcheryInfusionCharges") == 20, "Attempting to bind bosses spends no charge");

            var pig = EntityType.PIG.create(level);
            hand.getItem().onLeftClickEntity(hand, player, pig);
            helper.assertTrue(InfernalInfusion.owns(pig, player) && pig.hasEffect(WitcheryEffects.ENSLAVED.get())
                    && player.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG)
                    .getInt("witcheryInfusionCharges") == 15, "An ordinary creature still binds for five charge");
            pig.discard();
            helper.succeed();
        } finally {
            player.discard();
        }
    }
}
