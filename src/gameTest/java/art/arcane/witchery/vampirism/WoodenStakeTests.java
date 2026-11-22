package art.arcane.witchery.vampirism;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.WitcheryIngredient;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WoodenStakeTests {
    @GameTest(template = "qa_coven_arena")
    public static void stakesOnlySleepingVampiresWhenEnabled(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var attacker = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "StakeAttacker"));
        var victim = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "StakeVictim"));
        var data = WitcheryPlayerDataProvider.get(victim).orElseThrow(IllegalStateException::new);
        var stake = WitcheryIngredient.loadInventoryStack(TagParser.parseTag("""
                {id:"witchery:ingredient",Count:2b,Damage:162s,tag:{TestMarker:"legacy stake"}}
                """));
        helper.assertTrue(stake.is(WitcheryIngredient.WOODEN_STAKE.item()) && stake.getCount() == 2
                        && "legacy stake".equals(stake.getTag().getString("TestMarker")),
                "The original subtype retains its count and custom data as a Wooden Stake");
        attacker.setItemInHand(InteractionHand.MAIN_HAND, stake);

        var enabled = WitcheryServerConfig.ALLOW_STAKING_VAMPIRES;
        boolean previous = enabled.get();
        try {
            enabled.set(true);
            victim.startSleeping(helper.absolutePos(new BlockPos(7, 4, 7)));
            helper.assertTrue(!interact(attacker, victim) && victim.isAlive() && stake.getCount() == 2,
                    "A sleeping mortal is unaffected and the stake remains in hand");
            victim.stopSleeping();
            data.setVampireLevel(1);
            helper.assertTrue(!interact(attacker, victim) && victim.isAlive() && stake.getCount() == 2,
                    "An awake vampire is unaffected and the stake remains in hand");

            victim.startSleeping(helper.absolutePos(new BlockPos(7, 4, 7)));
            enabled.set(false);
            helper.assertTrue(!interact(attacker, victim) && victim.isAlive() && stake.getCount() == 2,
                    "Disabling staking leaves a sleeping vampire and the stack intact");

            enabled.set(true);
            helper.assertTrue(interact(attacker, victim) && !victim.isAlive() && !victim.isSleeping()
                            && victim.getLastHurtByMob() == attacker && stake.getCount() == 1,
                    "Staking wakes and kills the vampire, credits the attacker and spends one stake");
        } finally {
            enabled.set(previous);
        }
        helper.succeed();
    }

    private static boolean interact(FakePlayer attacker, FakePlayer victim) {
        return MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(attacker, InteractionHand.MAIN_HAND, victim));
    }
}
