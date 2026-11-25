package art.arcane.witchery.infusion;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class OverworldTerrainTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_overworld_shockwave")
    public static void shockwaveUsesTheOriginalBlockRingAndHeight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int x = helper.absolutePos(new BlockPos(8, 0, 0)).getX();
        FakePlayer caster = player(level, new Vec3(x + 0.5, 180, -0.5));
        caster.setXRot(-35);
        BlockPos inner = new BlockPos(x, 179, 3);
        BlockPos outer = inner.south();
        for (BlockPos surface : List.of(inner, outer)) {
            level.setBlockAndUpdate(surface.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(surface, Blocks.TERRACOTTA.defaultBlockState());
            level.setBlockAndUpdate(surface.above(), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(surface.above(2), Blocks.AIR.defaultBlockState());
        }
        Pig innerTarget = target(level, x - 2.4, 180, 0.4);
        Pig upperTarget = target(level, x + 2.7, 181.7, 1.1);
        Pig outside = target(level, x + 3.8, 180.15, 0.2);
        var targets = List.of(innerTarget, upperTarget, outside);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                targets.stream().allMatch(pig -> level.isPositionEntityTicking(pig.blockPosition())),
                "Wait for all shockwave target chunks to tick entities")).thenExecute(() -> {
            try {
                for (Pig pig : targets) {
                    helper.assertTrue(level.addFreshEntity(pig), "The world accepts the shockwave target");
                }
                InfusionEnergy.grant(caster, OverworldInfusion.ID, 20);
                release(caster, 40);
                OverworldInfusion.tick(level);
                helper.assertTrue(charge(caster) == 8 && innerTarget.getHealth() == 2 && upperTarget.getHealth() == 2
                                && outside.getHealth() == 10,
                        "The first twelve-charge wave catches block-ring targets at both heights and respects its bounds");
                helper.assertTrue(innerTarget.getDeltaMovement().x < -0.21 && upperTarget.getDeltaMovement().x > 0.21,
                        "The first pulse pushes the affected creatures outward");
                helper.assertTrue(level.getBlockState(inner.above()).is(Blocks.TERRACOTTA)
                                && level.getBlockState(outer.above(2)).is(Blocks.TERRACOTTA),
                        "The first pulse lifts the inner ring by one block and the next ring by two");
                OverworldInfusion.tick(level);
                helper.assertTrue(level.getBlockState(inner).is(Blocks.TERRACOTTA)
                                && level.getBlockState(outer.above()).is(Blocks.TERRACOTTA),
                        "The following pulse lowers the first two rings");
                for (int pulse = 3; pulse <= 6; pulse++) OverworldInfusion.tick(level);
            } finally {
                targets.forEach(Pig::discard);
            }
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void rockStartsAtTheBlockWithIndependentFlight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos block = helper.absolutePos(new BlockPos(5, 0, 6)).atY(181);
        level.setBlockAndUpdate(block, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(block.east(), Blocks.AIR.defaultBlockState());
        FakePlayer caster = player(level, Vec3.atBottomCenterOf(block.below().north(3)));
        caster.setDeltaMovement(4, 3, -3);
        InfusionEnergy.grant(caster, OverworldInfusion.ID, 20);
        Consumer<EntityJoinLevelEvent> veto = event -> {
            if (event.getLevel() == level && isRock(event)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            release(caster, 5);
            helper.assertTrue(charge(caster) == 20 && level.getBlockState(block).is(Blocks.STONE),
                    "A rejected rock does not spend charge or remove the source block");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }

        var launched = new ArrayList<ThrownBrew>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getLevel() == level && isRock(event)) launched.add((ThrownBrew) event.getEntity());
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            release(caster, 5);
            helper.assertTrue(launched.size() == 1 && charge(caster) == 17 && level.isEmptyBlock(block),
                    "A successful launch replaces the aimed block and spends three charges");
            ThrownBrew rock = launched.get(0);
            helper.assertTrue(rock.position().equals(Vec3.atCenterOf(block)), "The rock starts at the source block center");
            helper.assertTrue(rock.getDeltaMovement().distanceTo(new Vec3(0, 0, 1.5)) < 0.2,
                    "The launch preserves the original aim and spread without inheriting the caster's motion");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
            launched.forEach(ThrownBrew::discard);
        }
        helper.succeed();
    }

    private static boolean isRock(EntityJoinLevelEvent event) {
        return event.getEntity() instanceof ThrownBrew brew && brew.getItem().is(WitcheryIngredient.ROCK.item());
    }

    private static FakePlayer player(ServerLevel level, Vec3 position) {
        FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "TerrainCaster"));
        player.moveTo(position.x, position.y, position.z, 0, 0);
        return player;
    }

    private static Pig target(ServerLevel level, double x, double y, double z) {
        Pig pig = EntityType.PIG.create(level);
        pig.moveTo(x, y, z);
        pig.setNoAi(true);
        pig.setNoGravity(true);
        return pig;
    }

    private static void release(FakePlayer player, int elapsed) {
        ItemStack hand = new ItemStack(WitcheryItems.getRequired("witchhand").get());
        hand.getItem().releaseUsing(hand, player.level(), player, hand.getUseDuration() - elapsed);
    }

    private static int charge(FakePlayer player) {
        return player.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG).getInt("witcheryInfusionCharges");
    }
}
