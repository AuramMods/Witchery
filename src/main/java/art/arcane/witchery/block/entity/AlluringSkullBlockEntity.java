package art.arcane.witchery.block.entity;

import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class AlluringSkullBlockEntity extends BlockEntity {
    private static final int UPDATE_INTERVAL = 100;
    private static final int RANGE = 64;
    private static final int HEIGHT = 10;
    private static final List<AABB> QUADRANTS = List.of(
            new AABB(0, -HEIGHT, -RANGE, RANGE, 0, 0),
            new AABB(-RANGE, -HEIGHT, -RANGE, 0, 0, 0),
            new AABB(0, -HEIGHT, 0, RANGE, 0, RANGE),
            new AABB(-RANGE, -HEIGHT, 0, 0, 0, RANGE),
            new AABB(-RANGE, 1, -RANGE, 0, HEIGHT, 0),
            new AABB(0, 1, 0, RANGE, HEIGHT, RANGE),
            new AABB(-RANGE, 1, 0, 0, HEIGHT, RANGE),
            new AABB(0, 1, -RANGE, RANGE, HEIGHT, 0));
    private int ticks;
    private int quadrant;

    public AlluringSkullBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.ALLURING_SKULL.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AlluringSkullBlockEntity skull) {
        if (++skull.ticks < UPDATE_INTERVAL) return;
        skull.ticks = 0;
        skull.quadrant = (skull.quadrant + 1) % QUADRANTS.size();
        for (PathfinderMob undead : level.getEntitiesOfClass(PathfinderMob.class,
                QUADRANTS.get(skull.quadrant).move(pos), mob -> mob.getMobType() == MobType.UNDEAD)) {
            var navigation = undead.getNavigation();
            if (!navigation.moveTo(pos.getX(), pos.getY(), pos.getZ(), 1)) {
                navigation.moveTo(navigation.createPath(pos, 0, 10), 1);
            }
        }
    }
}
