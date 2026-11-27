package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class OreTranspositionTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_ore_transposition_order")
    public static void oreTranspositionUsesTheOriginalColumnOrderAndHonorsVetoes(GameTestHelper helper) {
        var level = helper.getLevel();
        var main = new BlockPos(12, 5, 5);
        var heart = helper.absolutePos(main);
        var gold = main.offset(-3, -1, 3);
        var iron = main.offset(-2, -1, -4);
        var protectedOre = main.offset(1, -1, -4);
        var failedDrop = main.offset(2, -1, 0);
        helper.setBlock(gold, Blocks.GOLD_ORE);
        helper.setBlock(iron, Blocks.IRON_ORE);
        helper.setBlock(protectedOre, Blocks.IRON_ORE);
        helper.setBlock(failedDrop, Blocks.DEEPSLATE_IRON_ORE);
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "OreTranspositionFixture"));
        var drops = new ArrayList<ItemEntity>();
        Consumer<BlockEvent.BreakEvent> breakVeto = event -> {
            if (event.getPlayer() == caster && event.getPos().equals(helper.absolutePos(protectedOre))) {
                event.setCanceled(true);
            }
        };
        Consumer<EntityJoinLevelEvent> recordDrops = event -> {
            if (event.getLevel() != level || !(event.getEntity() instanceof ItemEntity item)) return;
            drops.add(item);
            if (item.getItem().is(Items.DEEPSLATE_IRON_ORE)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(breakVeto);
        MinecraftForge.EVENT_BUS.addListener(recordDrops);
        try {
            level.getRandom().setSeed(42);
            helper.assertTrue(RiteOfTransposition.transposeOreLayer(level, heart, 1, 6, caster),
                    "The first layer starts upkeep");
            helper.assertTrue(drops.stream().map(item -> item.getItem().getItem()).toList()
                            .equals(List.of(Items.GOLD_ORE, Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE)),
                    "The western block is processed first and the break veto produces no item");
            helper.assertTrue(drops.get(0).blockPosition().equals(heart.offset(1, 2, 3))
                            && drops.get(1).blockPosition().equals(heart.offset(-8, 2, 4)),
                    "Item motion and vetoed breaks do not shift the following launch positions");
            helper.assertBlockPresent(Blocks.AIR, gold);
            helper.assertBlockPresent(Blocks.AIR, iron);
            helper.assertBlockPresent(Blocks.IRON_ORE, protectedOre);
            helper.assertBlockPresent(Blocks.DEEPSLATE_IRON_ORE, failedDrop);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(recordDrops);
            MinecraftForge.EVENT_BUS.unregister(breakVeto);
        }
        helper.succeed();
    }
}
