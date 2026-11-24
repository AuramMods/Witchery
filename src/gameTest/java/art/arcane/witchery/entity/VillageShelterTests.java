package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class VillageShelterTests {
    private static final String TEMPLATE = "qa_coven_arena";
    private static final int SHELTER_Y = 280;
    private static final BlockPos APPROACH = new BlockPos(4, SHELTER_Y, 5);
    private static final BlockPos DOOR = new BlockPos(12, SHELTER_Y, 5);
    private static final BlockPos INSIDE = DOOR.east(2);
    private static final BlockPos HOME = APPROACH.north(3);

    @GameTest(template = TEMPLATE, batch = "witchery_shelter_night", timeoutTicks = 920)
    public static void walksThroughAClosedDoorAtNight(GameTestHelper helper) {
        buildBedlessShelter(helper);
        claimHome(helper);
        assertVillageShelter(helper);
        var level = helper.getLevel();
        long oldTime = level.getDayTime();
        Runnable restore = () -> {
            removeHome(helper);
            setTime(helper, oldTime);
        };
        setTime(helper, 18_000);
        helper.runAfterDelay(910, restore);
        CovenWitch witch = helper.spawn(WitcheryEntities.COVEN_WITCH.get(), APPROACH);
        // Control the shelter roll and competing random strolls in the fixture.
        witch.getRandom().setSeed(0);
        BlockPos destination = helper.absolutePos(INSIDE);
        helper.startSequence().thenWaitUntil(() -> {
            var path = witch.getNavigation().getPath();
            helper.assertTrue(path != null && path.getTarget().equals(destination),
                    "The Coven Witch should choose the covered side of the house door");
        }).thenWaitUntil(() -> {
            var path = witch.getNavigation().getPath();
            helper.assertTrue(path != null && path.getTarget().equals(destination)
                            && helper.getBlockState(DOOR).getValue(DoorBlock.OPEN),
                    "The witch should open the wooden door while following the shelter route");
        }).thenWaitUntil(() -> {
            helper.assertTrue(witch.distanceToSqr(Vec3.atBottomCenterOf(destination)) < 1.25
                            && !helper.getBlockState(DOOR).getValue(DoorBlock.OPEN),
                    "The witch should arrive under the roof and close the door");
        }).thenExecute(restore).thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void seeksShelterInDaylightRainAndLeavesLeashedGoblinsToTheirWork(GameTestHelper helper) {
        buildBedlessShelter(helper);
        var level = helper.getLevel();
        BlockPos approach = helper.absolutePos(APPROACH);
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack()
                .withLevel(level).withSuppressedOutput(),
                "fillbiome " + coordinates(approach.offset(-4, -4, -4)) + " "
                        + coordinates(approach.offset(4, 4, 4)) + " minecraft:plains");
        helper.assertTrue(level.getBiome(approach).value().getPrecipitationAt(approach) == Biome.Precipitation.RAIN,
                "The daylight fixture should use a rainy biome");
        var goblin = helper.spawn(WitcheryEntities.GOBLIN.get(), APPROACH);
        goblin.setOnGround(true);
        var goal = new VillageShelterGoal(goblin, true);
        long oldTime = level.getDayTime();
        float oldRain = level.getRainLevel(1);
        try {
            setTime(helper, 6_000);
            level.setRainLevel(1);
            level.updateSkyBrightness();
            helper.assertTrue(!level.isCloseToVillage(approach, 2), "No other fixture should establish a village here");
            helper.assertTrue(!canSeek(goblin, goal), "A lone covered door far from any village should not attract the goblin");
            claimHome(helper);
            assertVillageShelter(helper);
            level.setRainLevel(0);
            level.updateSkyBrightness();
            helper.assertTrue(level.isDay() && !canSeek(goblin, goal),
                    "Clear daylight in a rainy biome should leave the goblin outdoors");
            level.setRainLevel(1);
            level.updateSkyBrightness();
            var leashHolder = helper.spawn(EntityType.ARMOR_STAND, APPROACH.south());
            goblin.setLeashedTo(leashHolder, false);
            helper.assertTrue(!canSeek(goblin, goal), "An attached lead should take precedence over the shelter");
            goblin.dropLeash(false, false);
            helper.assertTrue(canSeek(goblin, goal), "The same goblin should find the shelter in the rain after releasing the lead");
            goal.start();
            goblin.setLeashedTo(leashHolder, false);
            helper.assertTrue(!goal.canContinueToUse(), "A new lead should also stop an active shelter route");
            goal.stop();
        } finally {
            removeHome(helper);
            level.setRainLevel(oldRain);
            setTime(helper, oldTime);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void restrictsDoorNavigationOnlyWhileInsideDuringTheDay(GameTestHelper helper) {
        buildBedlessShelter(helper);
        var level = helper.getLevel();
        long oldTime = level.getDayTime();
        float oldRain = level.getRainLevel(1);
        CovenWitch witch = helper.spawn(WitcheryEntities.COVEN_WITCH.get(), INSIDE);
        var goal = new RestrictVillageDoorGoal(witch);
        var evaluator = witch.getNavigation().getNodeEvaluator();
        try {
            claimHome(helper);
            assertVillageShelter(helper);
            level.setRainLevel(0);
            setTime(helper, 6_000);
            helper.assertTrue(goal.canUse() && evaluator.canOpenDoors() && evaluator.canPassDoors(),
                    "A witch inside the village door during the day should restrict her path through it");
            goal.start();
            helper.assertTrue(!evaluator.canOpenDoors() && !evaluator.canPassDoors() && goal.canContinueToUse(),
                    "The restriction should keep pathfinding indoors");
            setTime(helper, 18_000);
            helper.assertTrue(!goal.canContinueToUse(), "Night should release the restriction");
            goal.stop();
            helper.assertTrue(evaluator.canOpenDoors() && evaluator.canPassDoors(), "The next path may go through a door again");
        } finally {
            removeHome(helper);
            level.setRainLevel(oldRain);
            setTime(helper, oldTime);
        }
        helper.succeed();
    }

    private static void buildBedlessShelter(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(0, SHELTER_Y - 1, 0), new BlockPos(24, SHELTER_Y - 1, 10))) {
            helper.setBlock(pos, Blocks.STONE);
        }
        for (BlockPos pos : BlockPos.betweenClosed(DOOR.offset(0, 0, -2), DOOR.offset(5, 3, 2))) {
            int x = pos.getX() - DOOR.getX();
            int z = pos.getZ() - DOOR.getZ();
            int y = pos.getY() - DOOR.getY();
            if (y == 3 || z == -2 || z == 2 || x == 5 || x == 0 && (z != 0 || y == 2)) {
                helper.setBlock(pos, Blocks.COBBLESTONE);
            }
        }
        placeDoor(helper, DOOR);
        placeDoor(helper, APPROACH.east(3).south(2));
    }

    private static void placeDoor(GameTestHelper helper, BlockPos pos) {
        var door = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST);
        helper.setBlock(pos, door);
        helper.setBlock(pos.above(), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    private static void claimHome(GameTestHelper helper) {
        var level = helper.getLevel();
        var poi = level.getPoiManager();
        BlockPos home = helper.absolutePos(HOME);
        var type = level.registryAccess().registryOrThrow(Registries.POINT_OF_INTEREST_TYPE).getHolderOrThrow(PoiTypes.HOME);
        poi.add(home, type);
        helper.assertTrue(poi.take(holder -> holder.is(PoiTypes.HOME), (holder, pos) -> pos.equals(home), home, 1).isPresent(),
                "An occupied home should establish the village next to the bedless shelter");
    }

    private static void removeHome(GameTestHelper helper) {
        var poi = helper.getLevel().getPoiManager();
        BlockPos home = helper.absolutePos(HOME);
        if (poi.existsAtPosition(PoiTypes.HOME, home)) poi.remove(home);
    }

    private static String coordinates(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static void assertVillageShelter(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos threshold = helper.absolutePos(DOOR);
        var state = level.getBlockState(threshold);
        helper.assertTrue(VillageShelterGoal.isWoodenThreshold(state), "The fixture should contain a lower wooden door");
        helper.assertTrue(level.isCloseToVillage(threshold, 2), "The wooden door should be near the occupied home");
        helper.assertTrue(VillageShelterGoal.coveredSide(level, threshold, state.getValue(DoorBlock.FACING)) == Direction.EAST,
                "The bedless shelter's eastern side should be covered");
    }

    private static boolean canSeek(PathfinderMob mob, VillageShelterGoal goal) {
        long seed = 0;
        while (RandomSource.create(seed).nextInt(50) != 0) seed++;
        mob.getRandom().setSeed(seed);
        return goal.canUse();
    }

    private static void setTime(GameTestHelper helper, long time) {
        var level = helper.getLevel();
        level.setDayTime(time);
        level.updateSkyBrightness();
    }
}
