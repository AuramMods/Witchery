package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.FetishBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public final class FetishBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = box(3.2, 0, 3.2, 12.8, 16, 12.8);
    private final String path;

    public FetishBlock(String path) {
        super(properties(path));
        this.path = path;
    }

    private static Properties properties(String path) {
        var properties = Properties.copy(Blocks.OAK_PLANKS).strength(3.5F, 100000.0F).noOcclusion();
        return path.equals("witchsladder") ? properties.noCollission() : properties.dynamicShape();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof FetishBlockEntity fetish && fetish.isSpectral()) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof FetishBlockEntity fetish && fetish.isSpectral()) return 0;
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public FetishBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FetishBlockEntity(pos, state, path);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity actor, ItemStack stack) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof FetishBlockEntity fetish) fetish.loadItem(stack);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(entity instanceof FetishBlockEntity fetish)) return super.getDrops(state, params);
        if (fetish.isSpectral()) return List.of();
        List<ItemStack> drops = super.getDrops(state, params);
        for (ItemStack drop : drops) {
            if (drop.is(asItem())) fetish.saveItem(drop);
        }
        return drops;
    }
}
