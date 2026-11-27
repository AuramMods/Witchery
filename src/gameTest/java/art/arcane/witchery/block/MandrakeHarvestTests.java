package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.entity.Mandrake;
import art.arcane.witchery.item.WitcheryIngredient;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class MandrakeHarvestTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_mandrake_harvest", setupTicks = 5)
    public static void matureMandrakesEmergeFromActualHarvestPaths(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 3, 5));
        var plant = (WitchCropBlock) WitchCrop.MANDRAKE.block();
        var mature = plant.getStateForAge(plant.getMaxAge());
        var farmer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "MandrakeHarvester"));
        var world = (PrimaryLevelData) level.getServer().getWorldData();
        var difficulty = world.getDifficulty();
        var tileDrops = level.getGameRules().getRule(GameRules.RULE_DOBLOCKDROPS);
        boolean originalTileDrops = tileDrops.get();

        List<Mandrake> attempts = new ArrayList<>();
        var veto = new AtomicBoolean();
        Consumer<EntityJoinLevelEvent> observe = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof Mandrake root && root.blockPosition().equals(pos)) {
                attempts.add(root);
                if (veto.get()) event.setCanceled(true);
            }
        };
        var harvest = BrewMixture.from(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER),
                new ItemStack(Items.APPLE)));
        var center = Vec3.atCenterOf(pos);
        var area = new AABB(pos).inflate(1);
        Runnable cast = () -> harvest.applySplashToBlock(level, pos, Direction.UP, center, farmer);
        Runnable blast = () -> new Explosion(level, null, center.x, center.y, center.z, 2, List.of(pos)).finalizeExplosion(false);
        Runnable playerBreak = () -> farmer.gameMode.destroyBlock(pos);
        Runnable destroy = () -> level.destroyBlock(pos, true);
        Runnable removeSupport = () -> level.removeBlock(pos.below(), false);
        List<Runnable> routes = List.of(playerBreak, destroy, removeSupport, cast, blast);

        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            world.setDifficulty(Difficulty.NORMAL);
            tileDrops.set(false, level.getServer());
            for (Runnable use : routes) {
                level.setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
                level.setBlockAndUpdate(pos, mature);
                level.random.setSeed(4096);
                use.run();
                helper.assertTrue(level.isEmptyBlock(pos) && attempts.size() == 1,
                        "Every real harvest path emits one mature Mandrake with tile drops off");
                Mandrake root = attempts.remove(0);
                helper.assertTrue(root.position().equals(Vec3.atBottomCenterOf(pos).add(0, 0.05, 0)),
                        "The crop releases its creature at the original foot position");
                root.discard();
            }

            tileDrops.set(true, level.getServer());
            veto.set(true);
            level.setBlockAndUpdate(pos, mature);
            level.random.setSeed(4096);
            cast.run();
            helper.assertTrue(level.isEmptyBlock(pos) && attempts.size() == 1
                            && level.getEntity(attempts.get(0).getUUID()) == null
                            && level.getEntitiesOfClass(ItemEntity.class, area).isEmpty(),
                    "A veto removes the harvested plant without returning its root or seed drops");
            attempts.clear();

            level.setBlockAndUpdate(pos, mature);
            level.random.setSeed(4096);
            helper.assertTrue(Block.getDrops(mature, level, pos, null).stream()
                            .anyMatch(stack -> stack.is(WitcheryIngredient.MANDRAKE_ROOT.item()))
                            && attempts.isEmpty(), "An ordinary loot lookup leaves the crop and its creature untouched");
            level.destroyBlock(pos, false);
            helper.assertTrue(attempts.isEmpty(), "Removing a crop without harvest skips the emergence roll");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(observe);
            tileDrops.set(originalTileDrops, level.getServer());
            world.setDifficulty(difficulty);
            attempts.forEach(Mandrake::discard);
        }
        helper.succeed();
    }
}
