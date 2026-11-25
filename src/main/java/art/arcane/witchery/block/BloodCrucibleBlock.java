package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.BloodCrucibleBlockEntity;
import art.arcane.witchery.vampirism.VampireAbilities;
import art.arcane.witchery.vampirism.VampireUltimate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BloodCrucibleBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = box(4, 0, 4, 12, 5, 12);

    public BloodCrucibleBlock() {
        super(Properties.copy(Blocks.STONE).strength(2.5F, 600).noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BloodCrucibleBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BloodCrucibleBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level instanceof ServerLevel world && player instanceof ServerPlayer vampire
                && level.getBlockEntity(pos) instanceof BloodCrucibleBlockEntity crucible) {
            var held = vampire.getItemInHand(hand);
            VampireUltimate power = VampireUltimate.fromOffering(held);
            boolean charged = (crucible.isFull() || vampire.getAbilities().instabuild)
                    && power != VampireUltimate.NONE && VampireAbilities.chargeUltimate(vampire, power);
            if (charged) {
                crucible.drain();
                held.shrink(1);
            }
            world.sendParticles(charged ? DustParticleOptions.REDSTONE : ParticleTypes.SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 16, 0.25, 0.15, 0.25, 0);
            world.playSound(null, pos, charged ? SoundEvents.FIRE_EXTINGUISH : SoundEvents.NOTE_BLOCK_SNARE.value(),
                    SoundSource.BLOCKS, 0.65F, 1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
