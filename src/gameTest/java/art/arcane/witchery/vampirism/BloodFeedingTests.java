package art.arcane.witchery.vampirism;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BloodFeedingTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_blood_sight")
    public static void sensesThroughBlocksWhileOtherCreaturesStillObstruct(GameTestHelper helper) {
        var level = helper.getLevel();
        var viewer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SightReader"));
        BlockPos origin = helper.absolutePos(new BlockPos(3, 2, 2));
        viewer.moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.4, 0, 11);
        helper.setBlock(3, 2, 4, Blocks.STONE);
        helper.setBlock(3, 3, 4, Blocks.STONE);

        var resident = helper.spawn(EntityType.VILLAGER, 3, 2, 5);
        resident.setNoAi(true);
        var eye = viewer.getEyePosition();
        var block = level.clip(new ClipContext(eye, eye.add(viewer.getLookAngle().scale(5)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, viewer));
        helper.assertTrue(block.getType() == HitResult.Type.BLOCK, "The ordinary targeting ray hits the stone wall");
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(BloodFeeding.findSightTarget(viewer) == resident,
                        "Blood sight detects the villager behind the wall"))
                .thenExecute(() -> {
                    var golem = helper.spawn(EntityType.IRON_GOLEM, 3, 2, 3);
                    golem.setNoAi(true);
                    helper.assertTrue(BloodFeeding.findSightTarget(viewer) == null,
                            "The closer golem hides the villager's blood reading");
                    golem.discard();
                    resident.moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 6.5);
                    helper.assertTrue(BloodFeeding.findSightTarget(viewer) == null,
                            "A villager beyond the five-block sight ray is outside range");
                    resident.discard();
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_werewolf_blood")
    public static void bitesWerewolvesWithoutTakingTheirBlood(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var vampire = new BiteRecorder(level);
        BlockPos position = helper.absolutePos(new BlockPos(3, 2, 2));
        vampire.moveTo(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
        var data = WitcheryPlayerDataProvider.get(vampire).orElseThrow(IllegalStateException::new);
        data.setVampireLevel(2);
        data.setBloodPower(400);
        data.setSelectedVampirePower(VampireAbilities.DRINK);

        var resident = WitcheryEntities.WERE_VILLAGER.get().create(level);
        resident.load(TagParser.parseTag("{WitcheryExtendedVillager:{Blood:203}}"));
        resident.moveTo(position.getX() + 0.5, position.getY(), position.getZ() + 1.5);
        VampireAbilities.interact(vampire, resident);
        helper.assertTrue(vampire.biteDamage == 4 && data.getBloodPower() == 400 && BloodFeeding.blood(resident) == 203,
                "Werevillager blood damages only the vampire and preserves both blood stores");

        var humanForm = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "UnshiftedWolf"));
        humanForm.moveTo(position.getX() + 0.5, position.getY(), position.getZ() + 1.5);
        var wolf = WitcheryPlayerDataProvider.get(humanForm).orElseThrow(IllegalStateException::new);
        wolf.setWerewolfLevel(1);
        wolf.setHumanBlood(180);
        VampireAbilities.interact(vampire, humanForm);
        helper.assertTrue(vampire.biteDamage == 8 && data.getBloodPower() == 400 && wolf.getHumanBlood() == 180,
                "An unshifted werewolf player also damages the vampire without losing blood");
        helper.succeed();
    }

    private static final class BiteRecorder extends FakePlayer {
        private float biteDamage;

        private BiteRecorder(ServerLevel level) {
            super(level, new GameProfile(UUID.randomUUID(), "BiteRecorder"));
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            if (source.is(WitcheryDamageTypes.VAMPIRE_BITE) && source.getEntity() == this) {
                biteDamage += amount;
                return true;
            }
            return super.hurt(source, amount);
        }
    }
}
