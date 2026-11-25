package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.DreamTravel;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class NightmareTargetingTests {
    @GameTest(template = "qa_coven_arena")
    public static void creatureApparitionsAreUnboundAndRecoverOlderAnimalBindings(GameTestHelper helper) {
        var level = helper.getLevel();
        var pig = EntityType.PIG.create(level);
        BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4)).atY(210);
        pig.setPos(Vec3.atBottomCenterOf(origin));
        helper.assertTrue(level.addFreshEntity(pig), "The afflicted pig joined the level");
        BlockPos selected = origin.offset(-2, 0, 3);
        level.setBlockAndUpdate(selected.below(), Blocks.STONE.defaultBlockState());
        var bounds = new AABB(selected).inflate(1);
        var spawning = level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING);
        boolean previous = spawning.get();
        spawning.set(true, level.getServer());
        try {
            pig.getRandom().setSeed(88);
            DreamTravel.spawnWakingNightmare(pig, 5);
            var nightmares = level.getEntitiesOfClass(Nightmare.class, bounds);
            helper.assertTrue(nightmares.size() == 1, "Waking Nightmare on a creature can still summon an apparition");
            Nightmare nightmare = nightmares.get(0);
            helper.assertTrue(nightmare.getTarget() == null && !nightmare.targets(pig), "The new apparition is unbound");

            CompoundTag earlierPort = new CompoundTag();
            nightmare.addAdditionalSaveData(earlierPort);
            earlierPort.putString("Victim", "Pig");
            earlierPort.putUUID("VictimUUID", pig.getUUID());
            nightmare.readAdditionalSaveData(earlierPort);
            nightmare.setTarget(pig);
            nightmare.setNoAi(true);
            nightmare.aiStep();
            CompoundTag recovered = new CompoundTag();
            nightmare.addAdditionalSaveData(recovered);
            helper.assertTrue(nightmare.isAlive() && nightmare.getTarget() == null && !recovered.hasUUID("VictimUUID")
                            && recovered.getString("Victim").isEmpty(),
                    "An earlier animal binding is cleared on load while the apparition survives");
        } finally {
            spawning.set(previous, level.getServer());
            level.getEntitiesOfClass(Nightmare.class, bounds).forEach(Entity::discard);
            pig.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void personalNightmareDepartsOnFirstDistantTick(GameTestHelper helper) {
        var level = helper.getLevel();
        var victim = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "DepartingVictim"));
        var nightmare = WitcheryEntities.NIGHTMARE.get().create(level);
        Vec3 origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 2, 3)).atY(210));
        victim.setPos(origin);
        level.addNewPlayer(victim);
        try {
            nightmare.setPos(origin.add(2, 0, 0));
            nightmare.setNoAi(true);
            nightmare.assignVictim(victim);
            victim.setPos(origin.add(40, 0, 0));
            nightmare.aiStep();
            helper.assertTrue(nightmare.isRemoved(), "A fresh personal Nightmare disappears on the first out-of-range tick");
        } finally {
            level.removePlayerImmediately(victim, Entity.RemovalReason.DISCARDED);
            nightmare.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void unboundApparitionCanOutlastHauntingUntilItsNextDepartureRoll(GameTestHelper helper) {
        var level = helper.getLevel();
        var victim = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "AwakeVictim"));
        var nightmare = WitcheryEntities.NIGHTMARE.get().create(level);
        Vec3 origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 2, 3)).atY(210));
        victim.setPos(origin);
        nightmare.setPos(origin.add(2, 0, 0));
        nightmare.setNoAi(true);
        nightmare.getRandom().setSeed(1);
        var effect = WitcheryEffects.WAKING_NIGHTMARE.get();
        victim.addEffect(new MobEffectInstance(effect, 100));
        try {
            nightmare.setTarget(victim);
            for (int tick = 0; tick < 20; ++tick) nightmare.aiStep();
            helper.assertTrue(nightmare.isAlive(), "An unbound Nightmare stays while its player target is haunted");
            victim.removeEffect(effect);
            for (int tick = 0; tick < 64 && !nightmare.isRemoved(); ++tick) nightmare.aiStep();
            helper.assertTrue(nightmare.isRemoved(), "The original departure roll also applies to unbound Nightmares");
        } finally {
            victim.discard();
            nightmare.discard();
        }
        helper.succeed();
    }
}
