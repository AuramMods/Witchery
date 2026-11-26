package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class BrewIce {
    private static final TagKey<EntityType<?>> RESISTANT = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "brew_of_ice_resistant"));

    private BrewIce() {}

    public static void impact(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        if (hit instanceof BlockHitResult block) {
            impactBlock(level, brew, block);
        } else if (hit instanceof EntityHitResult entity) {
            cage(level, brew.getOwner(), entity.getEntity());
        }
    }

    private static void impactBlock(ServerLevel level, ThrownBrew brew, BlockHitResult hit) {
        BlockPos center = hit.getBlockPos();
        if (Arrays.stream(Direction.values()).anyMatch(side -> level.getBlockState(center.relative(side)).is(Blocks.WATER))) {
            BrewColdEffects.freezeConnectedWater(level, center, brew.getOwner(),
                    pos -> Math.abs(pos.getX() - center.getX()) < 3 && Math.abs(pos.getY() - center.getY()) < 3
                            && Math.abs(pos.getZ() - center.getZ()) < 3);
        } else if (hit.getDirection() == Direction.DOWN) {
            BrewImpacts.recoverBrew(brew, hit);
        } else {
            BlockPos base = hit.getDirection() == Direction.UP ? center : center.relative(hit.getDirection());
            shield(level, brew.getOwner(), base, hit.getDirection());
        }
    }

    private static void shield(ServerLevel level, @Nullable Entity owner, BlockPos base, Direction face) {
        BlockPos first = level.getBlockState(base).blocksMotion() ? base.above() : base;
        column(level, owner, first, face);
        if (owner == null) return;
        Vec3 across = Vec3.directionFromRotation(0, owner.getYRot()).yRot((float) (Math.PI / 2));
        Vec3 center = Vec3.atCenterOf(first);
        column(level, owner, BlockPos.containing(center.add(across)), face);
        column(level, owner, BlockPos.containing(center.subtract(across)), face);
    }

    private static void column(ServerLevel level, @Nullable Entity owner, BlockPos base, Direction face) {
        for (int y = 0; y < 3; y++) placeIce(level, owner, base.above(y), face);
    }

    private static void cage(ServerLevel level, @Nullable Entity owner, Entity target) {
        BlockPos feet = new BlockPos((int) Math.round(target.getX()), target.getBlockY(), (int) Math.round(target.getZ()));
        if (target.getType().is(RESISTANT)) {
            BrewColdEffects.wetTarget(level, feet, owner);
            return;
        }
        BlockPos bottom = feet.below();
        for (int x = -2; x <= 1; x++) {
            for (int z = -2; z <= 1; z++) {
                if (x == -2 || x == 1 || z == -2 || z == 1) {
                    for (int y = 0; y <= 3; y++) placeIce(level, owner, bottom.offset(x, y, z), Direction.UP);
                } else {
                    placeIce(level, owner, bottom.offset(x, 3, z), Direction.UP);
                }
            }
        }
        placeIce(level, owner, bottom, Direction.UP);
        if (target instanceof Creeper creeper) {
            level.explode(creeper, creeper.getX(), creeper.getY(), creeper.getZ(),
                    creeper.isPowered() ? 6 : 3, Level.ExplosionInteraction.MOB);
            creeper.discard();
        }
    }

    private static void placeIce(ServerLevel level, @Nullable Entity owner, BlockPos pos, Direction face) {
        if (!level.getBlockState(pos).blocksMotion() && BrewTerrain.setBlock(owner, level, pos, Blocks.ICE.defaultBlockState(), face)) {
            Vec3 center = Vec3.atCenterOf(pos);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, center.x, center.y, center.z, 1, 0, 0, 0, 0);
        }
    }
}
