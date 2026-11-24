package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LilithCombatTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_lilith", timeoutTicks = 280)
    public static void reflectedFireballSuppressesLilithHealingWithoutDealingFireDamage(GameTestHelper helper) {
        var level = helper.getLevel();
        Lilith lilith = helper.spawnWithNoFreeWill(WitcheryEntities.LILITH.get(), new BlockPos(7, 3, 5));
        var ghast = EntityType.GHAST.create(level);
        ghast.setPos(lilith.position().add(-3, 0, 0));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LilithDefender"));
        player.setPos(lilith.position().add(-5, 0, 0));
        player.setYRot(-90);
        var fireball = new LargeFireball(level, ghast, -1, 0, 0, 1) {
            void hitLilith() {
                onHitEntity(new EntityHitResult(lilith));
            }
        };

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(lilith.tickCount >= 27, "Lilith has passed a healing pulse"))
                .thenExecute(() -> {
                    try {
                        lilith.setHealth(100);
                        fireball.hitLilith();
                        helper.assertTrue(lilith.getHealth() == 100 && weaknessTicks(lilith) == 0,
                                "An unreflected Large Fireball does not harm Lilith or suppress her healing");
                        helper.assertTrue(fireball.hurt(level.damageSources().playerAttack(player), 1)
                                        && fireball.getOwner() == player && fireball.getDeltaMovement().dot(new Vec3(1, 0, 0)) > 0,
                                "A player attack reflects the vanilla Large Fireball and takes ownership");
                        fireball.hitLilith();
                        helper.assertTrue(lilith.getHealth() == 100 && weaknessTicks(lilith) == 200 - lilith.tickCount % 20,
                                "A reflected Large Fireball interrupts the healing cycle without dealing fire damage");
                    } finally {
                        ghast.discard();
                        player.discard();
                        fireball.discard();
                    }
                }).thenIdle(175).thenExecute(() -> helper.assertTrue(lilith.getHealth() == 100,
                        "The boss does not heal during the reflection window"))
                .thenWaitUntil(() -> helper.assertTrue(lilith.getHealth() == 105 && weaknessTicks(lilith) == 0,
                        "The next healing pulse after the window restores five health"))
                .thenSucceed();
    }

    private static int weaknessTicks(Lilith lilith) {
        return lilith.saveWithoutId(new CompoundTag()).getInt("WeaknessTicks");
    }
}
