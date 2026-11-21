package art.arcane.witchery.ritual;

import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

final class CallOfTheBeasts {
    private static final Set<EntityType<?>> ANIMALS = Set.of(EntityType.PIG, EntityType.CHICKEN, EntityType.COW,
            EntityType.SHEEP, EntityType.MOOSHROOM, EntityType.WOLF, EntityType.OCELOT, EntityType.CAT);
    private static final List<BlockPos> OCTANTS = List.of(
            new BlockPos(1, -1, -1), new BlockPos(-1, -1, -1), new BlockPos(1, -1, 1), new BlockPos(-1, -1, 1),
            new BlockPos(-1, 1, -1), new BlockPos(1, 1, 1), new BlockPos(-1, 1, 1), new BlockPos(1, 1, -1));

    private CallOfTheBeasts() {}

    static void pulse(ServerLevel level, BlockPos heart, int stage) {
        BlockPos octant = OCTANTS.get(stage % OCTANTS.size());
        Vec3 center = Vec3.atLowerCornerOf(heart);
        Vec3 outer = center.add(128 * octant.getX(), 10 * octant.getY(), 128 * octant.getZ());
        AABB bounds = new AABB(outer, octant.getY() > 0 ? center.add(0, 1, 0) : center);
        int attempted = 0;
        for (Animal animal : level.getEntitiesOfClass(Animal.class, bounds,
                mob -> mob.isAlive() && ANIMALS.contains(mob.getType()) && mob.distanceToSqr(center) > 32
                        && !mob.hasEffect(WitcheryEffects.ENDER_INHIBITION.get()))) {
            Vec3 target = center.add(level.random.nextInt(5) - 2, 0, level.random.nextInt(5) - 2);
            if (WitcheryTeleport.canArrive(animal, level, target)) WitcheryTeleport.move(animal, level, target);
            if (++attempted >= 2) return;
        }
    }
}
