package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.ThrownBrew;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BrewSubstitution {
    private static final int[] SURFACE_HEIGHTS = {0, 1, -1, 2, -2};

    private BrewSubstitution() {}

    public static void impact(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        if (!(hit instanceof BlockHitResult block)) {
            BrewImpacts.recoverBrew(brew, level, hit);
            return;
        }
        BlockPos origin = block.getBlockPos();
        BlockState reference = level.getBlockState(origin);
        if (BrewTerrain.isProtectedTerrain(reference) || reference.getDestroySpeed(level, origin) < 0) {
            BrewImpacts.recoverBrew(brew, level, hit);
            return;
        }
        int radius = BrewImpacts.enhanced(brew) ? 6 : 4;
        Vec3 impact = hit.getLocation();
        List<ItemEntity> materials = level.getEntitiesOfClass(ItemEntity.class,
                brew.getBoundingBox().move(impact.subtract(brew.position())).inflate(radius),
                item -> item.isAlive() && item.getItem().getItem() instanceof BlockItem && item.distanceToSqr(impact) <= radius * radius);
        ItemStack returned = reference.getBlock().getCloneItemStack(level, origin, reference);
        int replaced = materials.isEmpty() || returned.isEmpty() ? 0
                : replaceSurfaces(level, brew.getOwner(), origin, reference.getBlock(), radius, materials);
        if (replaced == 0) {
            BrewImpacts.recoverBrew(brew, level, hit);
        } else {
            dropReturns(level, origin, returned, replaced);
        }
    }

    private static int replaceSurfaces(ServerLevel level, @Nullable Entity owner, BlockPos origin, Block reference,
                                       int radius, List<ItemEntity> materials) {
        int itemIndex = 0;
        int replaced = 0;
        int x = 0, z = 0, dx = 0, dz = -1;
        int radiusSquared = radius * radius;
        for (int step = 0; step < 4 * radiusSquared && itemIndex < materials.size(); step++) {
            BlockPos column = origin.offset(x, 0, z);
            if (x * x + z * z < radiusSquared && level.hasChunkAt(column)) {
                BlockPos surface = exposedSurface(level, column, reference);
                ItemEntity material = materials.get(itemIndex);
                if (surface != null && place(level, surface, owner, material.getItem())) {
                    replaced++;
                    if (material.getItem().getCount() == 1) {
                        material.discard();
                        itemIndex++;
                    } else {
                        material.setItem(material.getItem().copyWithCount(material.getItem().getCount() - 1));
                    }
                }
            }
            if (x == z || x < 0 && x == -z || x > 0 && x == 1 - z) {
                int oldX = dx;
                dx = -dz;
                dz = oldX;
            }
            x += dx;
            z += dz;
        }
        return replaced;
    }

    @Nullable
    private static BlockPos exposedSurface(ServerLevel level, BlockPos column, Block reference) {
        for (int height : SURFACE_HEIGHTS) {
            BlockPos pos = column.above(height);
            BlockState existing = level.getBlockState(pos);
            if (existing.is(reference) && !BrewTerrain.isProtectedTerrain(existing)
                    && level.getBlockState(pos.above()).canBeReplaced()) return pos;
        }
        return null;
    }

    private static boolean place(ServerLevel level, BlockPos pos, @Nullable Entity owner, ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem item)) return false;
        BlockState replacement = item.getBlock().defaultBlockState();
        var stateTag = stack.getTagElement("BlockStateTag");
        if (stateTag != null) {
            for (Property<?> property : replacement.getProperties()) {
                replacement = readProperty(replacement, property, stateTag.getString(property.getName()));
            }
        }
        if (!replacement.canSurvive(level, pos) || !BrewTerrain.setBlock(owner, level, pos, replacement, Direction.UP)) return false;
        BlockItem.updateCustomBlockEntityTag(level, owner instanceof Player player ? player : null, pos, stack);
        Vec3 center = Vec3.atCenterOf(pos);
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, center.x, center.y, center.z, 4, 0.3, 0.3, 0.3, 0);
        return true;
    }

    private static <T extends Comparable<T>> BlockState readProperty(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(parsed -> state.setValue(property, parsed)).orElse(state);
    }

    private static void dropReturns(ServerLevel level, BlockPos origin, ItemStack stack, int count) {
        Vec3 drop = Vec3.atCenterOf(origin.above());
        while (count > 0) {
            int size = Math.min(count, stack.getMaxStackSize());
            level.addFreshEntity(new ItemEntity(level, drop.x, drop.y, drop.z, stack.copyWithCount(size)));
            count -= size;
        }
    }
}
