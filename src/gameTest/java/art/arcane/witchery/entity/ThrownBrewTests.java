package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ThrownBrewTests {
    @GameTest(template = "qa_coven_arena")
    public static void thrownWebUsesTheOriginalBlockFacesAndSnowPlacement(GameTestHelper helper) {
        var level = helper.getLevel();
        var north = helper.absolutePos(new BlockPos(3, 0, 3)).atY(200);
        var directions = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        var placements = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
        for (int index = 0; index < directions.length; index++) {
            var struck = north.east(index * 4);
            level.setBlockAndUpdate(struck, Blocks.STONE.defaultBlockState());
            hitWeb(helper, struck, directions[index]);
            helper.assertTrue(level.getBlockState(struck.relative(placements[index])).is(Blocks.COBWEB)
                            && level.isEmptyBlock(struck.relative(directions[index])),
                    "Place the original horizontal web for the " + directions[index] + " face");
        }

        var snow = north.south(4);
        level.setBlockAndUpdate(snow.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(snow, Blocks.SNOW_BLOCK.defaultBlockState());
        hitWeb(helper, snow, Direction.UP);
        helper.assertTrue(level.getBlockState(snow).is(Blocks.COBWEB) && level.isEmptyBlock(snow.above()),
                "The web takes the full snow block's cell");
        helper.succeed();
    }

    private static void hitWeb(GameTestHelper helper, BlockPos struck, Direction face) {
        var level = helper.getLevel();
        var shot = new ThrownBrew(WitcheryEntities.THROWN_BREW.get(), level);
        shot.setItem(new ItemStack(WitcheryIngredient.WEB.item()));
        shot.onHit(new BlockHitResult(Vec3.atCenterOf(struck), face, struck, false));
    }

    @GameTest(template = "qa_coven_arena")
    public static void customSplashBrewsPassBoatsAndHitLivingTargets(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(8, 16, 5));
        var target = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(5, 16, 5));
        var boat = helper.spawn(EntityType.BOAT, new BlockPos(2, 16, 5));
        var targets = List.of(owner, target, boat);
        targets.forEach(entity -> entity.setNoGravity(true));

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                targets.stream().allMatch(entity -> level.getEntity(entity.getUUID()) == entity),
                "The boat and living targets are loaded"))
                .thenExecute(() -> {
                    var custom = projectile(owner, webBottle());
                    var fixed = projectile(owner, new ItemStack(WitcheryIngredient.BREW_OF_INK.item()));
                    try {
                        cross(custom, boat, 1);
                        cross(fixed, boat, 1);
                        helper.assertTrue(!custom.isRemoved() && fixed.isRemoved(),
                                "The custom brew passes through the boat and the fixed brew impacts it");
                        cross(custom, target, 2);
                        helper.assertTrue(custom.isRemoved(), "The custom brew impacts another living target immediately");
                    } finally {
                        custom.discard();
                        fixed.discard();
                        targets.forEach(Entity::discard);
                    }
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void brewsCanHitTheirOwnerOnTheFifthAirTick(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(5, 16, 5));
        owner.setNoGravity(true);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(owner.getUUID()) == owner,
                "The brew's owner is loaded")).thenExecute(() -> {
            var custom = projectile(owner, webBottle());
            custom.setNoGravity(true);
            var fixed = projectile(owner, new ItemStack(WitcheryIngredient.BREW_OF_INK.item()));
            try {
                for (var shot : List.of(custom, fixed)) {
                    for (int tick = 1; tick <= 4; tick++) {
                        cross(shot, owner, tick);
                        helper.assertTrue(!shot.isRemoved(), "The brew passes its owner during air tick " + tick);
                    }
                    cross(shot, owner, 5);
                    helper.assertTrue(shot.isRemoved(), "The brew can hit its owner on air tick five");
                }
            } finally {
                custom.discard();
                fixed.discard();
                owner.discard();
            }
        }).thenSucceed();
    }

    private static ItemStack webBottle() {
        var ingredients = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER),
                new ItemStack(WitcheryIngredient.WEB.item()));
        var bottle = WitcheryItems.getRequired("brewbottle").get().getDefaultInstance();
        bottle.setTag(BrewMixture.from(ingredients).bottleTag(ingredients));
        return bottle;
    }

    private static ThrownBrew projectile(LivingEntity owner, ItemStack item) {
        var brew = new ThrownBrew(owner, owner.level());
        brew.setItem(item);
        return brew;
    }

    private static void cross(ThrownBrew brew, Entity target, int flightTick) {
        Vec3 center = target.getBoundingBox().getCenter();
        brew.setPos(center.add(0, 0, 1.65));
        brew.setDeltaMovement(0, 0, -2.6);
        brew.tickCount = flightTick;
        brew.tick();
    }
}
