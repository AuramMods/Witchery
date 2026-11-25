package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.world.WitcheryDimensions;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class NightmareCombatTests {
    @GameTest(template = "qa_coven_arena")
    public static void rejectedHitStillActivatesNightmareGuard(GameTestHelper helper) {
        ServerLevel waking = helper.getLevel();
        ServerLevel dream = waking.getServer().getLevel(WitcheryDimensions.DREAM);
        helper.assertTrue(dream != null, "Dream must be available");
        BlockPos position = helper.absolutePos(new BlockPos(3, 2, 3)).atY(250);
        verifyVeto(helper, waking, position, false, 40);
        verifyVeto(helper, waking, position, true, 30);
        verifyVeto(helper, dream, position, false, 80);
        verifyVeto(helper, dream, position, true, 40);
        helper.succeed();
    }

    private static void verifyVeto(GameTestHelper helper, ServerLevel level, BlockPos position, boolean spear, int ticks) {
        var nightmare = WitcheryEntities.NIGHTMARE.get().create(level);
        nightmare.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        var attacker = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "NightmareAttack"));
        if (spear) {
            attacker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(WitcheryItems.getRequired("huntsmanspear").get()));
        }
        var attacks = new ArrayList<Integer>();
        Consumer<LivingAttackEvent> reject = event -> {
            if (event.getEntity() != nightmare) return;
            CompoundTag data = new CompoundTag();
            nightmare.addAdditionalSaveData(data);
            attacks.add(data.getInt("DefenseTicks"));
            event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(reject);
        try {
            var source = level.damageSources().playerAttack(attacker);
            helper.assertFalse(nightmare.hurt(source, 50), "Forge rejected the attack");
            helper.assertTrue(nightmare.isDefended() && nightmare.getHealth() == 100,
                    "The rejected hit opens the guard window without damaging the Nightmare");
            helper.assertFalse(nightmare.hurt(source, 50), "Guard blocks follow-up damage");
            helper.assertTrue(attacks.equals(List.of(ticks)), "The first attack sees the original guard duration");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(reject);
            nightmare.discard();
            attacker.discard();
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void playsTheOriginalHitAndDeathSounds(GameTestHelper helper) {
        var level = helper.getLevel();
        var wounded = WitcheryEntities.NIGHTMARE.get().create(level);
        var dying = WitcheryEntities.NIGHTMARE.get().create(level);
        BlockPos position = helper.absolutePos(new BlockPos(3, 2, 3)).atY(250);
        wounded.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        dying.setPos(wounded.position());
        dying.setHealth(1);
        var sounds = new ArrayList<SoundEvent>();
        Consumer<PlayLevelSoundEvent.AtPosition> capture = event -> {
            if (event.getPosition().equals(wounded.position()) && event.getSound() != null) {
                sounds.add(event.getSound().value());
            }
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            var source = level.damageSources().generic();
            helper.assertTrue(wounded.hurt(source, 50) && wounded.getHealth() == 85,
                    "A fresh attack is capped at fifteen damage");
            helper.assertTrue(dying.hurt(source, 5) && dying.isDeadOrDying(), "A lethal attack finishes the Nightmare");
            helper.assertTrue(sounds.equals(List.of(WitcherySounds.NIGHTMARE_DEAD.get(), WitcherySounds.NIGHTMARE_HIT.get())),
                    "Expected dead cue on hurt and hit cue on death, heard " + sounds);
            helper.assertTrue(wounded.getAmbientSoundInterval() == 160, "Ambient calls have the original interval");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
            wounded.discard();
            dying.discard();
        }
        helper.succeed();
    }
}
