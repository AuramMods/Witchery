package art.arcane.witchery.block;

import art.arcane.witchery.entity.Mandrake;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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

import java.util.LinkedHashMap;

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
        return super.mayPlaceOn(floor, level, pos) || floor.is(BlockTags.DIRT) || floor.is(this)
                || floor.getBlock() instanceof WitchCropBlock below && below.crop == WitchCrop.WORMWOOD;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos soil = pos.below();
        return level.getBlockState(soil).canSustainPlant(level, soil, Direction.UP, this);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return crop == WitchCrop.WORMWOOD || super.isRandomlyTicking(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (level.getRawBrightness(above, 0) < 9) return;

        int age = getAge(state);
        if (age < getMaxAge()) {
            if (!level.isAreaLoaded(pos, 1)) return;
            float growth = getGrowthSpeed(this, level, pos);
            if (crop == WitchCrop.MINDRAKE) growth /= 1.5F;
            boolean grows = random.nextInt((int) (25.0F / growth) + 1) == 0;
            if (ForgeHooks.onCropsGrowPre(level, pos, state, grows)) {
                level.setBlock(pos, getStateForAge(age + 1), Block.UPDATE_CLIENTS);
                ForgeHooks.onCropsGrowPost(level, pos, state);
            }
        } else if (crop == WitchCrop.WORMWOOD && !level.getBlockState(pos.below()).is(this)
                && level.isEmptyBlock(above) && ForgeHooks.onCropsGrowPre(level, pos, state, true)) {
            level.setBlockAndUpdate(above, defaultBlockState());
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    @Override
    protected int getBonemealAgeIncrease(Level level) {
        return crop == WitchCrop.WOLFSBANE || crop == WitchCrop.MINDRAKE
                ? 1 : Mth.nextInt(level.random, 2, getMaxAge());
    }

    /** Returns whether the emergence roll replaces this harvest's crop drops. */
    public static boolean awakenMandrake(LevelAccessor level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof WitchCropBlock plant) || plant.crop != WitchCrop.MANDRAKE
                || !plant.isMaxAge(state) || !(level instanceof ServerLevel server)
                || server.getDifficulty() == Difficulty.PEACEFUL
                || server.random.nextDouble() > (server.isDay() ? 0.9D : 0.1D)) return false;

        Mandrake mandrake = WitcheryEntities.MANDRAKE.get().create(server);
        if (mandrake == null) return false;
        mandrake.moveTo(pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5, 0, 0);
        // An emergence attempt consumes the drop even when Forge denies the creature.
        server.addFreshEntity(mandrake);
        WitcheryNetwork.sendMandrakeEmergence(server, mandrake.position());
        return true;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        // Crop harvest ignores the tool's Fortune. Direct loot queries can still request the brew's bonus.
        if (tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE) > 0) {
            tool = tool.copy();
            var enchantments = new LinkedHashMap<>(tool.getAllEnchantments());
            enchantments.remove(Enchantments.BLOCK_FORTUNE);
            EnchantmentHelper.setEnchantments(enchantments, tool);
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
}
