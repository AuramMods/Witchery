package art.arcane.witchery.brewing;

import art.arcane.witchery.world.BiomePainting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

public final class SnowTrailEffect extends MobEffect {
    private static final BlockState SNOW = Blocks.SNOW.defaultBlockState();
    private static final int MAX_SURFACE_SEARCH = 7;

    public SnowTrailEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (!(target.level() instanceof ServerLevel level)) return;
        Player actor = target instanceof Player player ? player : FakePlayerFactory.getMinecraft(level);
        for (int z = -1; z <= 1; z += 2) {
            for (int x = -1; x <= 1; x += 2) {
                BlockPos pos = BlockPos.containing(target.getX() + x * 0.25, target.getY(), target.getZ() + z * 0.25);
                if (level.isEmptyBlock(pos) && level.getBiome(pos).value().getTemperature(pos) < 1.6F
                        && SNOW.canSurvive(level, pos)) {
                    BrewTerrain.setBlock(actor, level, pos, SNOW, Direction.UP);
                }
            }
        }
        if (target instanceof SnowGolem && level.random.nextInt(20) == 0) {
            level.explode(target, target.getX(), target.getY(), target.getZ(), 3, Level.ExplosionInteraction.NONE);
            cover(level, target.blockPosition(), 8, actor);
            target.discard();
        }
    }

    static void cover(ServerLevel level, BlockPos center, int radius, @Nullable Player caster) {
        Player actor = caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
        if (!BrewTerrain.canEdit(actor, level, center, level.getBlockState(center))) return;
        int reach = radius - 1;
        int[] widths = BiomePainting.circleWidths(reach);
        for (int z = -reach; z <= reach; z++) {
            int width = widths[Math.abs(z)];
            for (int x = -width; x <= width; x++) {
                coverColumn(level, center.offset(x, 0, z), actor);
            }
        }
    }

    private static void coverColumn(ServerLevel level, BlockPos center, Player actor) {
        BlockPos surface = center;
        if (level.isEmptyBlock(center)) {
            int depth = 0;
            while (level.isEmptyBlock(surface) && depth < MAX_SURFACE_SEARCH) {
                surface = surface.below();
                depth++;
            }
            if (level.isEmptyBlock(surface)) return;
            surface = surface.above();
        } else {
            int height = 0;
            while (!level.isEmptyBlock(surface) && height < MAX_SURFACE_SEARCH) {
                surface = surface.above();
                height++;
            }
            if (!level.isEmptyBlock(surface)) return;
        }
        BlockPos below = surface.below();
        BlockState support = level.getBlockState(below);
        if ((support.isSolidRender(level, below) || support.is(BlockTags.LEAVES)) && SNOW.canSurvive(level, surface)) {
            BrewTerrain.setBlock(actor, level, surface, SNOW, Direction.UP);
        }
    }
}
