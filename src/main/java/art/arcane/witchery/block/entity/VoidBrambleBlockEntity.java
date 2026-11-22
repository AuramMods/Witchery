package art.arcane.witchery.block.entity;

import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class VoidBrambleBlockEntity extends PlacedOwnerBlockEntity {
    private static final int RADIUS = 32;
    private static final Map<ServerLevel, Set<BlockPos>> LOADED_BRAMBLES = new WeakHashMap<>();

    public VoidBrambleBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.VOID_BRAMBLE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            LOADED_BRAMBLES.computeIfAbsent(server, ignored -> new HashSet<>()).add(worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel server) {
            Set<BlockPos> brambles = LOADED_BRAMBLES.get(server);
            if (brambles != null) brambles.remove(worldPosition);
        }
        super.setRemoved();
    }

    public static boolean preventsRite(ServerLevel level, BlockPos heart) {
        for (BlockPos bramble : LOADED_BRAMBLES.getOrDefault(level, Set.of())) {
            if (bramble.distSqr(heart) < RADIUS * RADIUS && level.hasChunkAt(bramble)
                    && level.getBlockEntity(bramble) instanceof VoidBrambleBlockEntity tile && !tile.isRemoved()) return true;
        }
        return false;
    }
}
