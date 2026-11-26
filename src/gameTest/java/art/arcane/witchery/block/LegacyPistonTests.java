package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.stream.IntStream;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyPistonTests {
    private enum Extension { MOVE, BLOCK, DESTROY }

    private record PistonCase(BlockState state, Extension extension, ItemStack drop) {}

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_pistons")
    public static void legacyBlockEntitiesDeclareImmovablePistonReaction(GameTestHelper helper) {
        // Quark bypasses the vanilla block-entity check. These blocks must declare their own reaction.
        List<String> ids = List.of("altar", "beartrap", "bloodcrucible", "brazier", "cauldron", "chalice", "crystalball",
                "decursedirected", "decurseteleport", "demonheart", "distilleryburning", "distilleryidle", "kettle",
                "leechchest", "mirrorblock", "placeditem", "poppetshelf", "scarecrow", "silvervat", "spinningwheel",
                "statuegoddess", "statueofworship", "trent", "witchesovenburning", "witchesovenidle", "witchsladder",
                "wolfaltar", "wolftrap");
        for (String id : ids) {
            helper.assertTrue(state(id).getPistonPushReaction() == PushReaction.BLOCK, id + " must block piston movement");
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_pistons", timeoutTicks = 120)
    public static void pistonsMatchLegacyExtensionAndRetraction(GameTestHelper helper) {
        var level = helper.getLevel();
        var snowSlab = state("snowslab").getBlock();
        List<PistonCase> cases = List.of(
                new PistonCase(Blocks.STONE.defaultBlockState(), Extension.MOVE, ItemStack.EMPTY),
                blocked("candelabra"), blocked("fumefunnel"), blocked("filteredfumefunnel"),
                destroyed("circle"), destroyed("circleglyphritual"), destroyed("circleglyphotherwhere"),
                destroyed("circleglyphinfernal"),
                new PistonCase(state("dreamcatcher").setValue(DreamCatcherBlock.WEAVE, DreamWeave.NIGHTMARES),
                        Extension.DESTROY, new ItemStack(WitcheryIngredient.DREAMWEAVE_OF_NIGHTMARES.item())),
                destroyed("embermoss"), dropped("garlicgarland"), dropped("glintweed"), destroyed("plantmine"),
                dropped("snowslab"), new PistonCase(state("snowdoubleslab"), Extension.DESTROY, new ItemStack(snowSlab, 2)),
                new PistonCase(snowSlab.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE),
                        Extension.DESTROY, new ItemStack(snowSlab, 2)));
        List<BlockPos> positions = IntStream.range(0, cases.size()).mapToObj(index ->
                helper.absolutePos(new BlockPos(4 + 5 * (index / 4), 160, 1 + 3 * (index % 4)))).toList();

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(positions.stream().allMatch(pos ->
                                level.isPositionEntityTicking(pos.west(2).north())
                                        && level.isPositionEntityTicking(pos.east(2).south())),
                        "The piston lanes must track moving blocks and their drops"))
                .thenExecute(() -> {
                    encloseLanes(level, positions.get(0).offset(-3, -2, -2),
                            positions.get(positions.size() - 1).offset(3, 11, 2));
                    for (int index = 0; index < cases.size(); index++) {
                        var pos = positions.get(index);
                        var state = cases.get(index).state();
                        level.setBlockAndUpdate(pos.east(), Blocks.AIR.defaultBlockState());
                        containDrops(level, pos);
                        placeFixture(level, pos, state);
                        level.setBlockAndUpdate(pos.west(), Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.EAST));
                        level.setBlockAndUpdate(pos.west(2), Blocks.REDSTONE_BLOCK.defaultBlockState());
                        var sticky = pos.above(8);
                        level.setBlockAndUpdate(sticky, Blocks.AIR.defaultBlockState());
                        level.setBlockAndUpdate(sticky.east(), Blocks.AIR.defaultBlockState());
                        level.setBlockAndUpdate(sticky.west(), Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.EAST));
                        level.setBlockAndUpdate(sticky.west(2), Blocks.REDSTONE_BLOCK.defaultBlockState());
                    }
                }).thenWaitUntil(() -> {
                    for (int index = 0; index < cases.size(); index++) {
                        var pos = positions.get(index);
                        verifyExtension(helper, pos, cases.get(index));
                        helper.assertTrue(level.getBlockState(pos.above(8)).is(Blocks.PISTON_HEAD), "The sticky piston extends into air");
                    }
                }).thenExecute(() -> {
                    for (int index = 0; index < cases.size(); index++) {
                        var sticky = positions.get(index).above(8);
                        placeFixture(level, sticky.east(), cases.get(index).state());
                        level.setBlockAndUpdate(sticky.west(2), Blocks.AIR.defaultBlockState());
                    }
                }).thenWaitUntil(() -> {
                    for (int index = 0; index < cases.size(); index++) {
                        var pistonCase = cases.get(index);
                        var sticky = positions.get(index).above(8);
                        var destination = pistonCase.extension() == Extension.MOVE ? sticky : sticky.east();
                        var base = level.getBlockState(sticky.west());
                        var actual = level.getBlockState(destination);
                        helper.assertTrue(base.is(Blocks.STICKY_PISTON) && !base.getValue(PistonBaseBlock.EXTENDED)
                                        && actual.equals(pistonCase.state()),
                                "Sticky retraction for " + pistonCase.state() + " at " + destination + " had base "
                                        + base + " and destination " + actual);
                    }
                }).thenSucceed();
    }

    private static BlockState state(String path) {
        return WitcheryBlocks.getRequired(path).get().defaultBlockState();
    }

    private static PistonCase blocked(String path) {
        return new PistonCase(state(path), Extension.BLOCK, ItemStack.EMPTY);
    }

    private static PistonCase destroyed(String path) {
        return new PistonCase(state(path), Extension.DESTROY, ItemStack.EMPTY);
    }

    private static PistonCase dropped(String path) {
        var state = state(path);
        return new PistonCase(state, Extension.DESTROY, new ItemStack(state.getBlock()));
    }

    private static void encloseLanes(ServerLevel level, BlockPos min, BlockPos max) {
        // Keep generated gravel and neighboring fixtures out of both rows of pistons.
        var stone = Blocks.STONE.defaultBlockState();
        var air = Blocks.AIR.defaultBlockState();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            boolean wall = pos.getX() == min.getX() || pos.getX() == max.getX()
                    || pos.getY() == min.getY() || pos.getY() == max.getY()
                    || pos.getZ() == min.getZ() || pos.getZ() == max.getZ();
            level.setBlock(pos, wall ? stone : air, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    private static void placeFixture(ServerLevel level, BlockPos pos, BlockState state) {
        var support = state.getBlock() instanceof PlantMineBlock ? Blocks.DIRT : Blocks.STONE;
        level.setBlockAndUpdate(pos.below(), support.defaultBlockState());
        level.setBlockAndUpdate(pos.east().below(), support.defaultBlockState());
        if (state.getBlock() instanceof DreamCatcherBlock || state.getBlock() instanceof GarlicGarlandBlock) {
            level.setBlockAndUpdate(pos.south(), Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(pos, state);
    }

    private static void containDrops(ServerLevel level, BlockPos pos) {
        var stone = Blocks.STONE.defaultBlockState();
        for (var wall : List.of(pos.north(), pos.south(), pos.east().north(), pos.east().south(), pos.east(2))) {
            level.setBlockAndUpdate(wall, stone);
            level.setBlockAndUpdate(wall.above(), stone);
        }
    }

    private static void verifyExtension(GameTestHelper helper, BlockPos pos, PistonCase pistonCase) {
        var level = helper.getLevel();
        var source = level.getBlockState(pos);
        var destination = level.getBlockState(pos.east());
        boolean correct = switch (pistonCase.extension()) {
            case MOVE -> source.is(Blocks.PISTON_HEAD) && destination.equals(pistonCase.state());
            case BLOCK -> source.equals(pistonCase.state()) && destination.isAir();
            case DESTROY -> source.is(Blocks.PISTON_HEAD) && destination.isAir() && level.getBlockEntity(pos) == null;
        };
        helper.assertTrue(correct, "Piston extension for " + pistonCase.state() + " at " + pos + " had base "
                + level.getBlockState(pos.west()) + ", source " + source + " and destination " + destination);
        var area = new AABB(pos).expandTowards(1, 0, 0).inflate(0.8, 1, 0.9);
        var drops = level.getEntitiesOfClass(ItemEntity.class, area);
        var expected = pistonCase.drop();
        int count = drops.stream().map(ItemEntity::getItem).mapToInt(ItemStack::getCount).sum();
        helper.assertTrue(count == expected.getCount() && drops.stream().allMatch(item -> item.getItem().is(expected.getItem())),
                "Piston extension for " + pistonCase.state() + " expected " + expected
                        + " but found " + drops.stream().map(ItemEntity::getItem).toList());
    }
}
