package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
public final class SpectralHauntingTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_poltergeist_container")
    public static void nearbySummonerAllowsSeededPoltergeistThrow(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(4, 10, 4));
        var chestPos = origin.east(2);
        level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        var chest = (Container) level.getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 2));
        chest.setItem(12, new ItemStack(Items.IRON_INGOT, 3));
        chest.setItem(26, new ItemStack(Items.GOLD_INGOT, 4));

        var body = Vec3.atCenterOf(origin);
        var summoner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "HauntOwner"));
        summoner.setPos(body.add(0, 0, 8));
        level.addNewPlayer(summoner);
        var spirit = WitcheryEntities.POLTERGEIST.get().create(level);
        spirit.setPos(body);
        spirit.setNoAi(true);
        spirit.setSummoner(summoner);
        var drops = new ArrayList<ItemEntity>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item
                    && item.position().equals(Vec3.atCenterOf(chestPos))) drops.add(item);
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            spirit.tickCount = 99;
            spirit.aiStep();
            helper.assertTrue(drops.isEmpty() && spirit.throwAnimation() == 0, "The spirit waits for its hundredth tick");

            level.getRandom().setSeed(7);
            spirit.tickCount = 100;
            spirit.aiStep();
            helper.assertTrue(drops.size() == 1 && chest.getItem(0).getCount() == 2
                            && chest.getItem(12).getCount() == 2 && chest.getItem(26).getCount() == 4,
                    "The original world draw selects one item from the middle occupied slot");
            var toss = drops.get(0);
            helper.assertTrue(toss.getItem().is(Items.IRON_INGOT) && toss.getItem().getCount() == 1 && toss.lifespan == 18000
                            && toss.getDeltaMovement().distanceTo(new Vec3(0.083122594, 0.101925791, -0.005636985)) < 1e-8
                            && level.getRandom().nextInt(1000) == 649 && spirit.throwAnimation() == 15,
                    "The selected ingot keeps the original throw vector, lifespan, world draw order, and animation");

            toss.discard();
            Consumer<EntityJoinLevelEvent> protect = event -> {
                if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item
                        && item.position().equals(Vec3.atCenterOf(chestPos))) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(protect);
            try {
                spirit.tickCount = 200;
                spirit.aiStep();
                helper.assertTrue(chest.getItem(0).getCount() == 2 && chest.getItem(12).getCount() == 2
                                && chest.getItem(26).getCount() == 4,
                        "A Forge veto preserves the inventory offering");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(protect);
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
            drops.forEach(Entity::discard);
            level.removePlayerImmediately(summoner, Entity.RemovalReason.DISCARDED);
            spirit.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_spectral_air")
    public static void submergedSpectralUndeadPreserveAirSupply(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(4, 10, 4));
        for (var fluid : List.of(Blocks.WATER, WitcheryBlocks.getRequired("spiritflowing").get())) {
            var state = fluid.defaultBlockState();
            var fluidType = state.getFluidState().getFluidType();
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, 0, -1), origin.offset(1, 2, 1))) {
                level.setBlockAndUpdate(pos, state);
            }
            for (var kind : SpectralUndead.Kind.values()) {
                var spirit = kind.type().create(level);
                spirit.setPos(Vec3.atBottomCenterOf(origin));
                spirit.setNoAi(true);
                spirit.setAirSupply(1);
                for (int tick = 0; tick < 25; tick++) level.tickNonPassenger(spirit);
                helper.assertTrue(spirit.getEyeInFluidType() == fluidType && spirit.getAirSupply() == 1 && spirit.getHealth() == 20,
                        kind + " keeps its original air supply when submerged in " + fluid.getName().getString());
                spirit.discard();
            }
        }
        helper.succeed();
    }
}
