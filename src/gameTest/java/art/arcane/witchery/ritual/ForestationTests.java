package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ForestationTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_forestation")
    public static void forestationUsesRitualBonemealAndHonorsPlacementVeto(GameTestHelper helper) {
        var level = helper.getLevel();
        var heart = helper.absolutePos(new BlockPos(5, 5, 1));
        // Seed 1 picks (5, 8) as the first candidate inside the no-coven radius.
        var site = heart.offset(5, 0, 8);
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForestationFixture"));
        var ritualPlayer = FakePlayerFactory.getMinecraft(level);
        var progress = new LandscapeRites.Progress(ItemRiteRecipe.Action.FORESTATION, LandscapeRites.Species.OAK,
                heart, 0, null, false, 0);
        var attempts = new ArrayList<BonemealEvent>();
        Consumer<BonemealEvent> watch = event -> {
            if (event.getLevel() == level && event.getPos().equals(site)) {
                attempts.add(event);
                if (level.getBlockState(site).is(Blocks.OAK_SAPLING)) {
                    level.setBlockAndUpdate(site, Blocks.DARK_OAK_SAPLING.defaultBlockState());
                }
                event.setCanceled(true);
            }
        };
        Consumer<BlockEvent.EntityPlaceEvent> veto = event -> {
            if (event.getEntity() == caster && event.getPos().equals(site)) event.setCanceled(true);
        };
        level.setBlockAndUpdate(site, Blocks.AIR.defaultBlockState());
        MinecraftForge.EVENT_BUS.addListener(watch);
        try {
            level.setBlockAndUpdate(site.below(), Blocks.STONE.defaultBlockState());
            level.getRandom().setSeed(1);
            var first = LandscapeRites.pulse(level, heart, progress, 0, caster, Runnable::run, true);
            helper.assertTrue(first != null && first.stage() == 1 && level.isEmptyBlock(site),
                    "A stone column advances the rite without growing a sapling");
            helper.assertTrue(attempts.size() == 1 && attempts.get(0).getEntity() == ritualPlayer,
                    "The ritual attempts bonemeal once on the stone column as the Forge fake player");
            attempts.clear();

            level.setBlockAndUpdate(site.below(), Blocks.GRASS_BLOCK.defaultBlockState());
            MinecraftForge.EVENT_BUS.addListener(veto);
            try {
                level.getRandom().setSeed(1);
                LandscapeRites.pulse(level, heart, progress, 0, caster, Runnable::run, true);
                helper.assertTrue(level.isEmptyBlock(site) && attempts.isEmpty(),
                        "A placement veto leaves the column empty and prevents fertilizing it");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(veto);
            }

            level.getRandom().setSeed(1);
            LandscapeRites.pulse(level, heart, progress, 0, caster, Runnable::run, true);
            helper.assertTrue(level.getBlockState(site).is(Blocks.DARK_OAK_SAPLING) && attempts.size() == 11
                            && attempts.stream().allMatch(event -> event.getEntity() == ritualPlayer),
                    "The rite makes eleven fake-player attempts when a listener plants another legacy sapling");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(watch);
        }
        helper.succeed();
    }
}
