package art.arcane.witchery.vampirism;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class VampireReserveTests {
    @GameTest(template = "qa_coven_arena")
    public static void collectsFromDirectKillsAndKeepsTheMortalReserve(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var killer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ReserveHunter"));
        var data = WitcheryPlayerDataProvider.get(killer).orElseThrow(IllegalStateException::new);
        var victim = EntityType.VILLAGER.create(level);
        victim.load(TagParser.parseTag("{Health:1.0f,WitcheryExtendedVillager:{Blood:180}}"));
        helper.assertTrue(victim.hurt(level.damageSources().playerAttack(killer), 10) && !victim.isAlive(),
                "A mortal without a weapon can kill the weakened villager");
        helper.assertTrue(data.getBloodReserve() == 0 && data.serializeNBT().getInt("BloodReserve") == 180,
                "The villager's saved blood collects in the mortal's hidden reserve");

        var arrow = new Arrow(level, killer);
        Vampirism.onKilled(victim, killer, level.damageSources().arrow(arrow, killer));
        var prevented = EntityType.VILLAGER.create(level);
        prevented.getPersistentData().putBoolean("WITCNoDrops", true);
        Vampirism.onKilled(prevented, killer, level.damageSources().playerAttack(killer));
        helper.assertTrue(data.serializeNBT().getInt("BloodReserve") == 180,
                "Projectile kills and creatures flagged for no drops contribute no reserve");

        data.deserializeNBT(data.serializeNBT());
        var mortal = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ReserveVictim"));
        WitcheryPlayerDataProvider.get(mortal).ifPresent(target -> target.setHumanBlood(150));
        Vampirism.onKilled(mortal, killer, level.damageSources().playerAttack(killer));
        data.setVampireLevel(1);
        helper.assertTrue(data.getBloodReserve() == 250,
                "The mortal's accumulated reserve becomes available on turning and caps at 250");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void drinksFromTheSheathedCaneAndAdvancesTheFirstTrial(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ReserveDrinker"));
        var data = WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new);
        data.setVampireLevel(1);
        data.setBloodPower(600);
        data.setBloodReserve(180);
        var cane = WitcheryItems.getRequired("canesword").get();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(cane));
        var quests = WitcheryServerConfig.ALLOW_VAMPIRE_QUESTS;
        boolean previous = quests.get();
        try {
            quests.set(true);
            cane.use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(data.getBloodReserve() == 0 && data.getBloodPower() == 750 && data.getVampireLevel() == 2,
                    "Drinking the reserve fills rank one's blood capacity and completes the first trial");
            data.setBloodPower(data.getMaxBloodPower());
            data.setBloodReserve(100);
            cane.use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(data.getBloodReserve() == 100, "A full vampire keeps the collected reserve");
        } finally {
            quests.set(previous);
        }
        helper.succeed();
    }
}
