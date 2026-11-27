package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WitcheryBoltImpactTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_bolt_entity_impact", setupTicks = 5)
    public static void boltImpactsUseTheOriginalDamageAndRicochetRules(GameTestHelper helper) {
        var level = helper.getLevel();
        var chunk = new ChunkPos(helper.absolutePos(new BlockPos(5, 0, 5)));
        var lane = new BlockPos(chunk.getMiddleBlockX(), level.getMaxBuildHeight() - 32, chunk.getMinBlockZ() + 2);
        var rapid = EntityType.PIG.create(level);
        var fresh = EntityType.SKELETON.create(level);
        var protectedSkeleton = EntityType.SKELETON.create(level);
        var ordinary = EntityType.PIG.create(level);
        var flame = EntityType.PIG.create(level);
        var slow = EntityType.PIG.create(level);
        var enderman = new EnderMan(EntityType.ENDERMAN, level) {
            @Override
            public boolean hurt(DamageSource source, float amount) {
                // Exercise the successful deflection path without random teleport destinations in the fixture.
                return true;
            }
        };
        List<Mob> targets = List.of(rapid, fresh, protectedSkeleton, ordinary, flame, slow, enderman);
        for (int i = 0; i < targets.size(); i++) {
            Mob target = targets.get(i);
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(Vec3.atBottomCenterOf(lane.south(2 * i)).add(0, 0.25, 0));
            helper.assertTrue(level.addFreshEntity(target), "The collision target spawns in the test lane");
        }

        helper.startSequence().thenWaitUntil(() -> {
            for (Mob target : targets) {
                helper.assertTrue(level.getEntitiesOfClass(Mob.class, target.getBoundingBox()).contains(target),
                        "The target is available to the projectile collision query");
            }
        }).thenExecute(() -> {
            try {
                var first = strike(helper, rapid, 0, 1.3, 1.2, false);
                var second = strike(helper, rapid, 0, 1.3, 1.2, false);
                helper.assertTrue(first.lostHealth == 3 && second.lostHealth == 3
                                && rapid.invulnerableTime == 0 && rapid.getArrowCount() == 2,
                        "Two Wooden Bolts can each deal full damage on the same tick");

                var fullBonus = strike(helper, fresh, 3, 1.3, 1.6, false);
                helper.assertTrue(fullBonus.lostHealth == 6 && fullBonus.attackAmounts.equals(List.of(6F)),
                        "Holy damage is multiplied before the attack event");
                protectedSkeleton.hurt(level.damageSources().generic(), 4);
                var partialBonus = strike(helper, protectedSkeleton, 3, 1.3, 1.6, false);
                helper.assertTrue(partialBonus.lostHealth == 2 && partialBonus.attackAmounts.equals(List.of(6F))
                                && partialBonus.removed && protectedSkeleton.invulnerableTime == 0,
                        "A Holy Bolt clears the prior four-point hit with its six-point attack");
                var noBonus = strike(helper, ordinary, 3, 1.3, 1.6, false);
                helper.assertTrue(noBonus.lostHealth == 4 && noBonus.attackAmounts.equals(List.of(4F)),
                        "A Holy Bolt keeps its ordinary base damage against a living animal");

                flame.hurt(level.damageSources().generic(), 6);
                var flaming = strike(helper, flame, 0, 1.3, 1.6, true);
                helper.assertTrue(flaming.lostHealth == 0 && !flaming.removed && flaming.motionX < 0
                                && flame.getRemainingFireTicks() == 100 && flame.invulnerableTime == 20,
                        "A flaming Bolt leaves its five-second ignition on a rejected hit");
                slow.hurt(level.damageSources().generic(), 6);
                double outside = slow.getBbWidth() / 2D + (double) 0.3F + 0.001;
                var slowBounce = strike(helper, slow, 0, outside, 0.002, false);
                helper.assertTrue(slowBounce.lostHealth == 0 && !slowBounce.removed && slowBounce.motionX < 0,
                        "A slow rejected Bolt ricochets as a projectile");

                var deflected = strike(helper, enderman, 0, 1.3, 1.6, true);
                helper.assertTrue(deflected.lostHealth == 0 && !deflected.removed && deflected.motionX > 0
                                && deflected.hitCues == 1 && enderman.getArrowCount() == 1 && !enderman.isOnFire(),
                        "An accepted Enderman deflection keeps the Bolt in flight and plays the original hit cue");
            } finally {
                targets.forEach(Mob::discard);
            }
        }).thenSucceed();
    }

    private static Impact strike(GameTestHelper helper, Mob target, int kind, double distance, double speed, boolean flaming) {
        var level = helper.getLevel();
        var bolt = WitcheryEntities.BOLT.get().create(level);
        var tag = new CompoundTag();
        tag.putInt("boltType", kind);
        bolt.readAdditionalSaveData(tag);
        bolt.setPos(target.position().add(-distance, 0.43, 0));
        bolt.setDeltaMovement(speed, 0, 0);
        bolt.setBaseDamage(2);
        bolt.pickup = AbstractArrow.Pickup.ALLOWED;
        if (flaming) bolt.setSecondsOnFire(30);

        var amounts = new ArrayList<Float>();
        var cues = new ArrayList<PlayLevelSoundEvent.AtPosition>();
        Consumer<LivingAttackEvent> damage = event -> {
            if (event.getEntity() == target && event.getSource().getDirectEntity() == bolt) amounts.add(event.getAmount());
        };
        Consumer<PlayLevelSoundEvent.AtPosition> sound = event -> {
            if (event.getLevel() == level && event.getSound().value() == SoundEvents.ARROW_HIT) cues.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(damage);
        MinecraftForge.EVENT_BUS.addListener(sound);
        try {
            float health = target.getHealth();
            bolt.tick();
            return new Impact(health - target.getHealth(), bolt.isRemoved(), bolt.getDeltaMovement().x, amounts, cues.size());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(damage);
            MinecraftForge.EVENT_BUS.unregister(sound);
            bolt.discard();
        }
    }

    private record Impact(float lostHealth, boolean removed, double motionX, List<Float> attackAmounts, int hitCues) {}
}
