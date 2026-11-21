package art.arcane.witchery.brewing;

import art.arcane.witchery.block.entity.AreaWardBlockEntity;
import art.arcane.witchery.entity.CovenWitch;
import art.arcane.witchery.entity.Leonard;
import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.VoodooPoppetItem;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.Comparator;

public final class BrewRituals {
    private static final TicketType<BlockPos> CAST_TICKET =
            TicketType.create("witchery_brew_ritual", Comparator.comparingLong(BlockPos::asLong), 160);

    public enum Result {
        ONGOING, COMPLETE, FAILED, TOO_FAR
    }

    private BrewRituals() {}

    public static boolean leonardNearby(ServerLevel level, BlockPos cauldron) {
        return !level.getEntitiesOfClass(Leonard.class, new AABB(cauldron).inflate(16),
                entity -> entity.isAlive() && entity.getHealth() > 1).isEmpty();
    }

    public static Result pulse(ServerLevel source, BlockPos origin, BrewMixture mixture, CompoundTag brew,
                               ItemStack trigger, int pulse, boolean leonard) {
        if (trigger.getItem() instanceof TaglockItem) return taglockCast(source, origin, mixture, trigger, leonard);
        var destination = mixture.destination();
        ServerLevel level = destination == null ? source : source.getServer().getLevel(destination.dimension());
        BlockPos target = destination == null ? origin : destination.position();
        if (level == null || !level.isInWorldBounds(target) || !level.getWorldBorder().isWithinBounds(target)) return Result.TOO_FAR;
        if (destination != null && !canReach(source, origin, level, target.distSqr(origin), leonard)) return Result.TOO_FAR;

        return switch (mixture.delivery()) {
            case TRIGGERED -> BrewItemCurse.attach(source, origin, brew) ? Result.COMPLETE : Result.FAILED;
            case SPLASH -> {
                mixture.applyToBlock(level, target, Direction.UP, mixture.splashRadius(), true, null);
                yield Result.COMPLETE;
            }
            case GAS -> {
                if (level != source || !target.equals(origin)) {
                    level.getChunkSource().addRegionTicket(CAST_TICKET, new ChunkPos(target), 3, target);
                }
                gasWave(level, target, mixture, pulse - 1);
                yield pulse < 8 + mixture.extent() * 8 ? Result.ONGOING : Result.COMPLETE;
            }
            case LIQUID -> {
                // The original rain selects the target dimension but scatters around the cauldron's X and Z.
                BlockPos center = new BlockPos(origin.getX(), target.getY(), origin.getZ());
                rain(level, center, brew, 16 + 8 * mixture.extent());
                yield pulse < 10 + mixture.lifetime() * 5 ? Result.ONGOING : Result.COMPLETE;
            }
            case DRINK -> Result.FAILED;
        };
    }

    private static Result taglockCast(ServerLevel source, BlockPos origin, BrewMixture mixture, ItemStack trigger,
                                      boolean leonard) {
        LivingEntity target = TaglockItem.resolveTarget(source, trigger);
        if (target == null || !target.isAlive() || !(target.level() instanceof ServerLevel level)) return Result.FAILED;
        if (!canReach(source, origin, level, target.distanceToSqr(Vec3.atLowerCornerOf(origin)), leonard)) {
            return Result.TOO_FAR;
        }
        if (!HunterClothingItem.resistsCurse(target) && !AreaWardBlockEntity.protectsBrew(target)
                && !VoodooPoppetItem.deflectBrew(target)) {
            var caster = FakePlayerFactory.getMinecraft(source);
            mixture.apply(target, caster, caster, 1, 1);
        }
        return Result.COMPLETE;
    }

    private static boolean canReach(ServerLevel source, BlockPos origin, ServerLevel level, double distanceSquared,
                                    boolean leonard) {
        AABB covenArea = new AABB(origin).inflate(6);
        Vec3 center = Vec3.atCenterOf(origin);
        int coven = source.getEntitiesOfClass(CovenWitch.class, covenArea, witch -> witch.hasCovenOwner()
                && witch.distanceToSqr(center.x, witch.getY(), center.z) <= 36).size();
        int otherPlayers = source.getEntitiesOfClass(ServerPlayer.class, covenArea, player -> !player.isSpectator()
                && player.distanceToSqr(center.x, player.getY(), center.z) <= 36 && CovenWitch.covenSize(player) > 0).size() - 1;
        coven += Math.max(0, otherPlayers);
        if (source != level) return coven >= 6 && leonard;
        if (coven >= 6) return true;
        int scale = (1 + coven) * 4;
        int distance = 2 * scale * scale;
        return distanceSquared <= (double) distance * distance;
    }

    private static void gasWave(ServerLevel level, BlockPos center, BrewMixture mixture, int radius) {
        if (radius == 0) {
            gasColumn(level, center, mixture);
            return;
        }
        int x = radius;
        int z = 0;
        int error = 1 - radius;
        while (x >= z) {
            gasColumn(level, center.offset(x, 0, z), mixture);
            gasColumn(level, center.offset(z, 0, x), mixture);
            gasColumn(level, center.offset(-x, 0, z), mixture);
            gasColumn(level, center.offset(-z, 0, x), mixture);
            gasColumn(level, center.offset(-x, 0, -z), mixture);
            gasColumn(level, center.offset(-z, 0, -x), mixture);
            gasColumn(level, center.offset(x, 0, -z), mixture);
            gasColumn(level, center.offset(z, 0, -x), mixture);
            z++;
            if (error < 0) {
                error += 2 * z + 1;
            } else {
                x--;
                error += 2 * (z - x + 1);
            }
        }
    }

    private static void gasColumn(ServerLevel level, BlockPos column, BrewMixture mixture) {
        if (!level.getWorldBorder().isWithinBounds(column)) return;
        for (int distance = 0; distance < 8; distance++) {
            if (gasSurface(level, column.above(distance), mixture)) return;
            if (distance > 0 && gasSurface(level, column.below(distance), mixture)) return;
        }
    }

    private static boolean gasSurface(ServerLevel level, BlockPos ground, BrewMixture mixture) {
        if (level.isOutsideBuildHeight(ground.getY()) || level.getBlockState(ground).isAir()
                || !level.getBlockState(ground.above()).isAir()) return false;
        level.sendParticles(ParticleTypes.EFFECT, ground.getX() + 0.5, ground.getY() + 1, ground.getZ() + 0.5,
                16, 0.48, 0.35, 0.48, 0.05);
        if (level.getRandom().nextFloat() < 0.01F) mixture.applyToBlock(level, ground, Direction.UP, 1, true, null);
        Vec3 center = Vec3.atLowerCornerOf(ground);
        AABB contacts = new AABB(center, center).inflate(1.5);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, contacts,
                candidate -> candidate.distanceToSqr(center.x, candidate.getY(), center.z) <= 2.25)) {
            mixture.apply(target, null, null, 1, 1);
        }
        return true;
    }

    private static void rain(ServerLevel level, BlockPos center, CompoundTag brew, int radius) {
        var random = level.getRandom();
        int halfQuantity = radius / 4;
        int quantity = halfQuantity + random.nextInt(halfQuantity);
        for (int drop = 0; drop < quantity; drop++) {
            int heightOffset = random.nextInt(20);
            int x = random.nextInt(2 * radius) - radius;
            int z = random.nextInt(2 * radius) - radius;
            if (x * x + z * z > radius * radius) continue;
            BlockPos column = center.offset(x, 0, z);
            if (!level.getWorldBorder().isWithinBounds(column)) continue;
            level.getChunkSource().addRegionTicket(CAST_TICKET, new ChunkPos(column), 2, center);
            boolean ceiling = level.dimensionType().hasCeiling();
            int surface = ceiling ? center.getY() : Math.max(center.getY(),
                    level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ()));
            int height = Math.max(100, surface + 16) + heightOffset;
            if (ceiling) height = Math.min(height, level.dimensionType().logicalHeight() - 2);
            var droplet = WitcheryEntities.BREW_DROPLET.get().create(level);
            if (droplet == null) continue;
            droplet.setBrew(brew);
            droplet.setPos(column.getX(), height, column.getZ());
            level.addFreshEntity(droplet);
        }
    }
}
