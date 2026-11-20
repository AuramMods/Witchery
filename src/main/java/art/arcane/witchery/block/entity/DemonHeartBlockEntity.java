package art.arcane.witchery.block.entity;

import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class DemonHeartBlockEntity extends BlockEntity {
    public static final int PULSE_TICKS = 25;
    private int pulse;

    public DemonHeartBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.DEMON_HEART.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DemonHeartBlockEntity heart) {
        if (++heart.pulse == PULSE_TICKS) {
            heart.pulse = 0;
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    WitcherySounds.HEARTBEAT.get(), SoundSource.BLOCKS, 0.8F, 1, false);
        }
    }

    public int pulse() {
        return pulse;
    }
}
