package art.arcane.witchery.item;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class AnointingPasteItem extends Item {
    public AnointingPasteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.CAULDRON) && !state.is(Blocks.WATER_CAULDRON)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            if (!server.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState())) {
                return InteractionResult.FAIL;
            }
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            server.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    16, 0.35, 0.35, 0.35, 0.02);
            server.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.6F, 1.1F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
