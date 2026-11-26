package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PatronCombatTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_patron_arrows")
    public static void mogShootsFromInFrontAndUsesTargetImpulse(GameTestHelper helper) {
        var level = helper.getLevel();
        var mog = helper.spawn(WitcheryEntities.MOG.get(), new BlockPos(4, 5, 4));
        mog.setNoAi(true);
        mog.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        var target = EntityType.PIG.create(level);
        target.setPos(mog.getX() + 6, mog.getY(), mog.getZ() + 8);

        var shots = new ArrayList<Arrow>();
        Consumer<EntityJoinLevelEvent> record = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof Arrow shot && shot.getOwner() == mog) {
                shots.add(shot);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(record);
        try {
            for (boolean grounded : new boolean[] {false, true}) {
                for (boolean impulse : new boolean[] {false, true}) {
                    target.setOnGround(grounded);
                    target.hasImpulse = impulse;
                    int start = shots.size();
                    for (int shot = 0; shot < 16; shot++) mog.performRangedAttack(target, 0.75F);
                    var volley = shots.subList(start, shots.size());
                    helper.assertTrue(volley.size() == 16, "Mog should fire one arrow per attack");
                    helper.assertTrue(volley.stream().allMatch(shot ->
                                    Math.abs(shot.getX() - mog.getX() - 0.6) < 1.0E-6
                                    && Math.abs(shot.getY() - mog.getEyeY() + 0.1F) < 1.0E-6
                                    && Math.abs(shot.getZ() - mog.getZ() - 0.8) < 1.0E-6),
                            "The arrow starts one block toward the target at Mog's firing height");
                    double meanSpeed = volley.stream().mapToDouble(shot -> shot.getDeltaMovement().length())
                            .average().orElseThrow();
                    helper.assertTrue(Math.abs(meanSpeed - (impulse ? 4.0 : 2.4)) < 0.25,
                            "The target's impulse should select arrow speed independently of ground contact");
                }
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(record);
            shots.forEach(Arrow::discard);
            mog.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_patron_dying_partner")
    public static void patronProtectionLastsThroughPartnerDeathAnimation(GameTestHelper helper) {
        var level = helper.getLevel();
        var mog = helper.spawn(WitcheryEntities.MOG.get(), new BlockPos(4, 5, 4));
        var gulg = helper.spawn(WitcheryEntities.GULG.get(), new BlockPos(6, 5, 4));
        mog.setNoAi(true);
        gulg.setNoAi(true);
        helper.runAfterDelay(2, () -> {
            helper.assertFalse(mog.hurt(level.damageSources().magic(), 10), "A living partner protects Mog");
            gulg.setHealth(0);
            helper.assertFalse(gulg.isAlive(), "Gulg should be dying");
            helper.assertFalse(mog.hurt(level.damageSources().magic(), 10), "A dying partner still protects Mog");
            gulg.discard();
            helper.assertTrue(mog.hurt(level.damageSources().magic(), 10) && mog.getHealth() == 390,
                    "Mog becomes vulnerable after the partner leaves the world");
            mog.discard();
            helper.succeed();
        });
    }
}
