package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WickerManSpawnerTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_wicker_spawner")
    public static void buildsWickerManWithOriginalSpawnerRule(GameTestHelper helper) {
        var rules = WitcheryServerConfig.STRAWMAN_SPAWNER_RULES;
        var previous = rules.get();
        var site = new SurfaceStructureSite(helper.getLevel(), helper.absolutePos(new BlockPos(3, 1, 3)),
                Direction.SOUTH, 6, 5, false);
        try {
            rules.set(List.of("witchery.hellhound"));
            WickerManFeature.build(site, RandomSource.create(0));
        } finally {
            rules.set(previous);
        }
        if (!(site.level().getBlockEntity(site.at(2, 0, 2)) instanceof SpawnerBlockEntity spawner)) {
            helper.fail("The Wicker Man must generate a spawner at its base");
            return;
        }
        var mob = spawner.saveWithoutMetadata().getCompound("SpawnData").getCompound("entity");
        helper.assertTrue(mob.getString("id").equals("witchery:hellhound"),
                "The generated spawner must use the Hellhound selected by the original configuration name");
        helper.succeed();
    }
}
