package art.arcane.witchery.event;

import art.arcane.witchery.config.WitcheryServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

public final class WitcheryBlockBreak {
    private WitcheryBlockBreak() {}

    public static boolean isCanceled(ServerLevel level, BlockPos pos, BlockState state, Player player) {
        return WitcheryServerConfig.ALLOW_INTER_MOD_BLOCK_BREAK_EVENTS.get()
                && MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos.immutable(), state, player));
    }
}
