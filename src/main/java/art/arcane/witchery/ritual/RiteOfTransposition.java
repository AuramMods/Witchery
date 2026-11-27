package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AreaWardBlockEntity;
import art.arcane.witchery.entity.CovenWitch;
import art.arcane.witchery.event.WitcheryBlockBreak;
import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.item.VoodooPoppetItem;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.world.WitcheryDimensions;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public final class RiteOfTransposition {
    public static final ResourceLocation ORE_RECIPE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "rite_of_transposition_ores");
    public static final int ORE_PULSE_TICKS = 10;
    private static final int ORE_RADIUS = 8;

    private RiteOfTransposition() {}

    @Nullable
    public static String summon(ServerLevel level, BlockPos circle, @Nullable LivingEntity target,
                                @Nullable ServerPlayer caster, boolean legacy) {
        if (target == null) return "message.witchery.rite.no_taglock_target";
        if (target.level().dimension().equals(WitcheryDimensions.DREAM)
                || level.dimension().equals(WitcheryDimensions.DREAM)) return "message.witchery.rite.transpose_dream";
        if (HunterClothingItem.resistsCurse(target)) return "message.witchery.rite.hunter_curse_protection";
        if (AreaWardBlockEntity.protectsSummoning(target)) return "message.witchery.rite.transpose_statue";
        if (VoodooPoppetItem.deflectSummoning(caster, target)) return "message.witchery.rite.transpose_warded";
        var inhibition = target.getEffect(WitcheryEffects.ENDER_INHIBITION.get());
        if (inhibition != null && inhibition.getAmplifier() >= 3) return "message.witchery.rite.transpose_inhibited";
        Vec3 arrival = legacy ? Vec3.atLowerCornerOf(circle) : Vec3.atBottomCenterOf(circle);
        boolean moved = legacy ? WitcheryTeleport.moveWithLegacyCues(target, level, arrival)
                : WitcheryTeleport.canArrive(target, level, arrival) && WitcheryTeleport.move(target, level, arrival);
        return moved ? null : "message.witchery.rite.transpose_obstructed";
    }

    public static int oreDepthLimit(int covenSize) {
        return 30 + 5 * covenSize;
    }

    public static boolean transposeOreLayer(ServerLevel level, BlockPos circle, int depth, int covenSize,
                                            @Nullable ServerPlayer caster) {
        BlockPos center = circle.below(depth);
        int minimumY = level.getMinBuildHeight() + 2;
        if (depth > oreDepthLimit(covenSize) || level.isOutsideBuildHeight(center)) return false;

        boolean gold = covenSize == CovenWitch.MAX_MEMBERS;
        ServerPlayer actor = caster != null && caster.level() == level ? caster : FakePlayerFactory.getMinecraft(level);
        for (int x = -ORE_RADIUS; x <= ORE_RADIUS; x++) {
            for (int z = -ORE_RADIUS; z <= ORE_RADIUS; z++) {
                extractOre(level, circle, center.offset(x, 0, z), gold, actor);
            }
        }
        return depth < oreDepthLimit(covenSize) && center.getY() > minimumY;
    }

    private static void extractOre(ServerLevel level, BlockPos circle, BlockPos pos, boolean gold, ServerPlayer actor) {
        var state = level.getBlockState(pos);
        if (!state.is(BlockTags.IRON_ORES) && !(gold && state.is(BlockTags.GOLD_ORES))) return;
        var stack = new ItemStack(state.getBlock());
        if (stack.isEmpty() || state.hasBlockEntity() || state.getDestroySpeed(level, pos) < 0
                || !level.getWorldBorder().isWithinBounds(pos) || !level.mayInteract(actor, pos)
                || WitcheryBlockBreak.isCanceled(level, pos, state, actor)
                || !level.getBlockState(pos).equals(state)) return;

        int x = circle.getX() + level.getRandom().nextIntBetweenInclusive(-ORE_RADIUS, ORE_RADIUS);
        int z = circle.getZ() + level.getRandom().nextIntBetweenInclusive(-ORE_RADIUS, ORE_RADIUS);
        // The original consumes world randomness for position only. Item motion uses a separate source.
        var motion = ThreadLocalRandom.current();
        var item = new ItemEntity(level, x, circle.getY() + 2, z, stack,
                motion.nextDouble(-0.1, 0.1), 0.2, motion.nextDouble(-0.1, 0.1));
        if (level.addFreshEntity(item) && (!level.getBlockState(pos).equals(state) || !level.removeBlock(pos, false))) {
            item.discard();
        }
    }
}
