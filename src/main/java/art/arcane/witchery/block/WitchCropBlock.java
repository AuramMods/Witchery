package art.arcane.witchery.block;

import art.arcane.witchery.entity.Mandrake;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.PlantType;
import org.jetbrains.annotations.Nullable;

public final class WitchCropBlock extends CropBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);
    private final WitchCrop crop;

    public WitchCropBlock(WitchCrop crop, Properties properties) {
        super(properties);
        this.crop = crop;
    }

    public WitchCrop crop() {
        return crop;
    }

    @Override
    protected Item getBaseSeedId() {
        return crop.seed();
    }

    @Override
    public int getMaxAge() {
        return crop.maxAge();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public PlantType getPlantType(BlockGetter level, BlockPos pos) {
        return crop == WitchCrop.ARTICHOKE ? PlantType.WATER : super.getPlantType(level, pos);
    }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter level, BlockPos pos) {
        if (crop == WitchCrop.ARTICHOKE) {
            return floor.is(Blocks.WATER) && floor.getFluidState().isSource();
        }
        return super.mayPlaceOn(floor, level, pos) || floor.is(BlockTags.DIRT)
                || floor.getBlock() instanceof WitchCropBlock below && below.crop == WitchCrop.WORMWOOD;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return crop == WitchCrop.WORMWOOD || super.isRandomlyTicking(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (crop == WitchCrop.WORMWOOD && isMaxAge(state)) {
            if (level.getRawBrightness(pos.above(), 0) >= 9 && !level.getBlockState(pos.below()).is(this)
                    && level.isEmptyBlock(pos.above()) && ForgeHooks.onCropsGrowPre(level, pos, state, true)) {
                level.setBlockAndUpdate(pos.above(), defaultBlockState());
                ForgeHooks.onCropsGrowPost(level, pos, state);
            }
        } else if (crop == WitchCrop.MINDRAKE) {
            if (random.nextInt(3) != 0) {
                super.randomTick(state, level, pos, random);
            }
        } else {
            super.randomTick(state, level, pos, random);
        }
    }

    @Override
    protected int getBonemealAgeIncrease(Level level) {
        return crop == WitchCrop.WOLFSBANE || crop == WitchCrop.MINDRAKE
                ? 1 : Mth.nextInt(level.random, 2, getMaxAge());
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (crop == WitchCrop.MANDRAKE && isMaxAge(state) && level instanceof ServerLevel server
                && server.getDifficulty() != Difficulty.PEACEFUL
                && server.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)
                && server.random.nextDouble() <= (server.isDay() ? 0.9D : 0.1D)) {
            Mandrake mandrake = WitcheryEntities.MANDRAKE.get().create(server);
            if (mandrake != null) {
                mandrake.moveTo(pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5, 0, 0);
                if (server.addFreshEntity(mandrake)) {
                    player.awardStat(Stats.BLOCK_MINED.get(this));
                    player.causeFoodExhaustion(0.005F);
                    server.sendParticles(ParticleTypes.POOF, mandrake.getX(), mandrake.getY(0.5), mandrake.getZ(),
                            8, 0.2, 0.3, 0.2, 0.02);
                    return;
                }
            }
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
}
