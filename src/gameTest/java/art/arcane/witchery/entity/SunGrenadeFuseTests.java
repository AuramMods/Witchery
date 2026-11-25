package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.SunlightBlock;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.item.GrenadeItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
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
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SunGrenadeFuseTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_sun_grenade", setupTicks = 5)
    public static void dispensersLaunchGrenadesAndBrewsWithOriginalAim(GameTestHelper helper) {
        var level = helper.getLevel();
        var ingredients = List.of(Items.NETHER_WART.getDefaultInstance(), Items.SPIDER_EYE.getDefaultInstance(),
                Items.GUNPOWDER.getDefaultInstance());
        var splash = WitcheryItems.getRequired("brewbottle").get().getDefaultInstance();
        splash.setTag(BrewMixture.from(ingredients).bottleTag(ingredients));
        var fixtures = List.of(
                new Launcher(new BlockPos(3, 8, 3), Direction.EAST,
                        WitcheryItems.getRequired("sungrenade").get().getDefaultInstance()),
                new Launcher(new BlockPos(7, 8, 3), Direction.UP, GrenadeItem.duplicate("FlightFixture")),
                new Launcher(new BlockPos(3, 8, 7), Direction.DOWN, splash),
                new Launcher(new BlockPos(7, 8, 7), Direction.NORTH,
                        WitcheryIngredient.BREW_OF_INK.item().getDefaultInstance()));
        var shots = new ArrayList<Launch>();
        Consumer<EntityJoinLevelEvent> observe = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ThrowableItemProjectile shot
                    && (shot instanceof GrenadeProjectile || shot instanceof ThrownBrew)
                    && fixtures.stream().anyMatch(fixture -> ItemStack.isSameItemSameTags(shot.getItem(), fixture.payload())
                            && shot.position().distanceTo(Vec3.atCenterOf(helper.absolutePos(fixture.pos()))) < 1)) {
                shots.add(new Launch(shot.getItem().copy(), shot.position(), shot.getDeltaMovement()));
            }
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        for (var fixture : fixtures) {
            var pos = helper.absolutePos(fixture.pos());
            level.setBlockAndUpdate(pos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, fixture.face()));
            ((DispenserBlockEntity) level.getBlockEntity(pos)).setItem(0, fixture.payload().copyWithCount(2));
            level.setBlockAndUpdate(pos.south(), Blocks.REDSTONE_BLOCK.defaultBlockState());
        }
        helper.runAfterDelay(8, () -> {
            MinecraftForge.EVENT_BUS.unregister(observe);
            helper.assertTrue(shots.size() == fixtures.size(), "One projectile leaves each Witchery dispenser");
            for (var fixture : fixtures) {
                var pos = helper.absolutePos(fixture.pos());
                var shot = shots.stream().filter(launch -> ItemStack.matches(launch.payload(), fixture.payload()))
                        .findFirst().orElseThrow();
                var direction = Vec3.atLowerCornerOf(fixture.face().getNormal());
                var outlet = Vec3.atCenterOf(pos).add(direction.scale(0.7));
                var heading = direction.add(0, 0.1F, 0).normalize().scale(1.375F);
                helper.assertTrue(shot.origin().distanceTo(outlet) < 1.0E-6
                                && shot.velocity().distanceTo(heading) < 0.17,
                        "Original dispenser launch for " + fixture.payload() + " facing " + fixture.face() + ". Got " + shot);
                helper.assertTrue(ItemStack.matches(((DispenserBlockEntity) level.getBlockEntity(pos)).getItem(0),
                                fixture.payload()), "A redstone pulse spends exactly one matching item");
            }
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void handGrenadesLaunchFromBesideTheEyeWithoutInheritingMotion(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "HandGrenadeFlight"));
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 16, 5))));
        player.setYRot(0);
        player.setXRot(0);
        player.setDeltaMovement(0.3, -0.42, 0.2);
        var shots = new ArrayList<GrenadeProjectile>();
        Consumer<EntityJoinLevelEvent> observe = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof GrenadeProjectile shot && shot.getOwner() == player) {
                shots.add(shot);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            for (var stack : List.of(WitcheryItems.getRequired("sungrenade").get().getDefaultInstance(),
                    GrenadeItem.duplicate("FlightFixture"))) {
                var payload = stack.copy();
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                stack.getItem().use(level, player, InteractionHand.MAIN_HAND);
                helper.assertTrue(stack.isEmpty() && shots.size() == 1, "A hand throw spends one grenade");
                var shot = shots.remove(0);
                var origin = new Vec3(player.getX() - 0.16F, player.getEyeY() - 0.1F, player.getZ());
                var heading = new Vec3(0, Math.sin(Math.toRadians(20)), 1).normalize().scale(0.75F);
                helper.assertTrue(shot.position().distanceTo(origin) < 1.0E-6
                                && shot.getDeltaMovement().distanceTo(heading) < 0.055
                                && ItemStack.matches(shot.getItem(), payload),
                        "Original hand grenade launch. Got " + shot.position() + " at " + shot.getDeltaMovement());
                shot.discard();
            }
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(observe);
            shots.forEach(GrenadeProjectile::discard);
        }
    }

    private record Launcher(BlockPos pos, Direction face, ItemStack payload) {}

    private record Launch(ItemStack payload, Vec3 origin, Vec3 velocity) {}

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
