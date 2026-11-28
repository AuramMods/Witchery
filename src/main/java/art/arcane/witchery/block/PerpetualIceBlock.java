package art.arcane.witchery.block;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

public final class PerpetualIceBlock extends IceBlock {
    static final int LIGHT_ATTENUATION = 3;

    public PerpetualIceBlock() {
        super(Properties.copy(Blocks.ICE).friction(0.98F).strength(2, 3));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return LIGHT_ATTENUATION;
    }

    public static final class Stairs extends StairBlock {
        public Stairs() {
            super(() -> WitcheryBlocks.getRequired("perpetualice").get().defaultBlockState(),
                    Properties.copy(Blocks.GLASS).mapColor(MapColor.ICE).friction(0.98F).strength(2, 3));
        }

        @Override
        public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
            return LIGHT_ATTENUATION;
        }
    }

    public static final class Slab extends SlabBlock {
        public Slab() {
            super(Properties.copy(Blocks.GLASS).mapColor(MapColor.ICE).sound(SoundType.STONE).strength(2, 3));
        }

        @Override
        public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
            return LIGHT_ATTENUATION;
        }
    }

    public static final class DoubleSlab extends Block {
        public DoubleSlab() {
            super(Properties.copy(Blocks.ICE).strength(2, 3));
        }

        @Override
        public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
            return LIGHT_ATTENUATION;
        }
    }

    public static final class Gate extends FenceGateBlock {
        public Gate() {
            super(Properties.copy(Blocks.OAK_FENCE_GATE).strength(2, 3), WoodType.OAK);
        }

        @Override
        public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
            return LIGHT_ATTENUATION;
        }
    }
}
