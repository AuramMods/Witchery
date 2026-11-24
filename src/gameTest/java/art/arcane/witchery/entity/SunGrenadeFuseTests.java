package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.SunlightBlock;
import art.arcane.witchery.item.GrenadeItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SunGrenadeFuseTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_sun_grenade", setupTicks = 5, timeoutTicks = 260)
    public static void sunGrenadeKeepsItsOriginalFuseAfterImpact(GameTestHelper helper) {
        var level = helper.getLevel();
        var relative = new BlockPos(4, 4, 4);
        helper.setBlock(relative.east(3), Blocks.STONE);
        helper.setBlock(relative.east(3).below(), Blocks.STONE);
        for (int x = 2; x <= 8; ++x) {
            for (int z = 2; z <= 6; ++z) {
                helper.setBlock(new BlockPos(x, 2, z), Blocks.STONE);
            }
        }
        var origin = Vec3.atCenterOf(helper.absolutePos(relative));
        var grenade = new GrenadeProjectile(WitcheryEntities.GRENADE.get(), level);
        grenade.setPos(origin);
        grenade.setDeltaMovement(0.65, 0, 0);
        level.addFreshEntity(grenade);
        var dropArea = new AABB(origin, origin).inflate(6);
        var dropPositions = new ArrayList<Vec3>();
        var obstructedPositions = new ArrayList<Vec3>();
        Consumer<EntityJoinLevelEvent> inspectDrop = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity drop
                    && dropArea.contains(drop.position()) && drop.getItem().is(WitcheryIngredient.QUARTZ_SPHERE.item())) {
                dropPositions.add(drop.position());
                if (level.getBlockCollisions(drop, drop.getBoundingBox()).iterator().hasNext()) {
                    obstructedPositions.add(drop.position());
                }
            }
        };
        helper.runAfterDelay(22, () -> {
            helper.assertTrue(grenade.hasImpacted(), "The grenade must hit the stone wall and remain in the world");
            var saved = grenade.saveWithoutId(new CompoundTag());
            helper.assertTrue(saved.getInt("Remaining") < 190,
                    "Landing must keep the remaining part of the original 200-tick fuse");
            grenade.load(saved);
            MinecraftForge.EVENT_BUS.addListener(inspectDrop);
        });
        helper.runAfterDelay(220, () -> {
            MinecraftForge.EVENT_BUS.unregister(inspectDrop);
            helper.assertTrue(grenade.isRemoved(), "The sun grenade must spend its fuse about ten seconds after launch");
            helper.assertTrue(dropPositions.size() == 1 && obstructedPositions.isEmpty(),
                    "The sphere should spawn outside the wall. Drops " + dropPositions + ", obstructed " + obstructedPositions);
            helper.assertTrue(!level.getBlockState(grenade.lightPosition()).is(WitcheryBlocks.getRequired("light").get()),
                    "The temporary light must disappear after the grenade bursts");
            var drops = level.getEntitiesOfClass(ItemEntity.class, dropArea,
                    drop -> drop.getItem().is(WitcheryIngredient.QUARTZ_SPHERE.item()));
            int amount = drops.stream().mapToInt(drop -> drop.getItem().getCount()).sum();
            helper.assertTrue(amount == 1, "The burst must return one Quartz Sphere, found " + amount);
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void loadOriginalAndEarlierPortGrenadeFuses(GameTestHelper helper) {
        var grenade = new GrenadeProjectile(WitcheryEntities.GRENADE.get(), helper.getLevel());
        var saved = new CompoundTag();
        saved.putBoolean("Impacted", true);
        saved.putBoolean("BlockPlaced", true);
        saved.putInt("BlockPlacedX", 42);
        saved.putInt("BlockPlacedY", 70);
        saved.putInt("BlockPlacedZ", -24);
        grenade.readAdditionalSaveData(saved);
        helper.assertTrue(grenade.hasImpacted() && grenade.saveWithoutId(new CompoundTag()).getInt("Remaining") == 200,
                "An original landed grenade restarts with its original duration");
        helper.assertTrue(grenade.isLighting(new BlockPos(42, 70, -24)), "An original landed grenade keeps the position it lit");
        saved.putInt("Remaining", 675);
        saved.remove("BlockPlaced");
        grenade.readAdditionalSaveData(saved);
        helper.assertTrue(grenade.saveWithoutId(new CompoundTag()).getInt("Remaining") == 675,
                "An earlier port's saved fuse must retain its remaining ticks");

        saved.remove("Remaining");
        saved.putBoolean("Impacted", false);
        saved.putInt("Mode", 1);
        saved.putString("Owner", "LegacyDisguise");
        grenade.readAdditionalSaveData(saved);
        helper.assertTrue(GrenadeItem.isDuplicate(grenade.getItem())
                        && GrenadeItem.ownerName(grenade.getItem()).equals("LegacyDisguise"),
                "The original duplicate grenade keeps its named skin");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_sun_grenade")
    public static void sunGrenadeMovesItsLightAndKeepsSharedAndPermanentLights(GameTestHelper helper) {
        var level = helper.getLevel();
        var light = WitcheryBlocks.getRequired("light").get();
        var shared = helper.absolutePos(new BlockPos(4, 4, 4));
        var destination = shared.east(3);
        var permanent = shared.east(6);
        level.setBlockAndUpdate(permanent, light.defaultBlockState());
        var first = new GrenadeProjectile(WitcheryEntities.GRENADE.get(), level);
        var second = new GrenadeProjectile(WitcheryEntities.GRENADE.get(), level);
        for (var grenade : new GrenadeProjectile[]{first, second}) {
            grenade.setPos(Vec3.atCenterOf(shared));
            grenade.setNoGravity(true);
            level.addFreshEntity(grenade);
        }

        helper.runAfterDelay(12, () -> {
            helper.assertTrue(level.getBlockState(shared).is(light), "Both grenades must light their shared block");
            first.setPos(Vec3.atCenterOf(destination));
            first.tick();
            helper.assertTrue(level.getBlockState(shared).is(light) && level.getBlockState(destination).is(light),
                    "Moving one grenade keeps the shared light and immediately lights its new block");

            second.setPos(Vec3.atCenterOf(permanent));
            second.tick();
            helper.assertTrue(level.isEmptyBlock(shared), "Moving the final grenade clears its old light immediately");
            var saved = first.saveWithoutId(new CompoundTag());
            saved.putInt("Remaining", 1);
            first.load(saved);
            first.tick();
            helper.assertTrue(first.isRemoved() && level.isEmptyBlock(destination),
                    "Bursting after reload clears the saved light on the same tick");

            saved = second.saveWithoutId(new CompoundTag());
            saved.putInt("Remaining", 1);
            second.load(saved);
            second.tick();
            var permanentState = level.getBlockState(permanent);
            helper.assertTrue(permanentState.is(light) && !permanentState.getValue(SunlightBlock.TEMPORARY),
                    "A burst keeps an existing permanent light");
            helper.succeed();
        });
    }
}
