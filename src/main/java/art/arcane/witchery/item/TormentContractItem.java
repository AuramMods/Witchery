package art.arcane.witchery.item;

import art.arcane.witchery.entity.LordOfTorment;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TormentContractItem extends Item {
    private static final int USE_TICKS = 1200;
    private static final int CHANNEL_TICKS = 280;
    private static final int CHARGE_STEP = 40;
    private static final int CHARGE_COST = 10;
    private static final int[] SUMMON_OFFSETS = {-4, -3, -2, 3, 4};
    private static final String[] STANDING_STONES = {
            "00004340000",
            "00431113400",
            "04111111140",
            "03111111130",
            "41112221114",
            "31112121113",
            "41112221114",
            "03111111130",
            "04111111140",
            "00431113400",
            "00004340000"
    };

    public TormentContractItem() {
        super(new Properties());
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        if (!(level instanceof ServerLevel server) || !(living instanceof ServerPlayer player)) return;
        int elapsed = USE_TICKS - remaining;
        if (elapsed > CHANNEL_TICKS || elapsed % CHARGE_STEP != 0) return;
        if (!InfusionEnergy.spend(player, CHARGE_COST)) {
            player.stopUsingItem();
            return;
        }
        if (elapsed == 0 && !isStandingCircle(server, player.blockPosition())) {
            fail(player, "no_standing_stones");
            return;
        }
        server.playSound(null, player.blockPosition(), SoundEvents.BLAZE_DEATH, SoundSource.PLAYERS, 0.7F, 1.0F);
        server.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1, player.getZ(), 16, 0.4, 0.6, 0.4, 0);
        if (elapsed >= 160) {
            server.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 16, 0.5, 0.6, 0.5, 0.02);
        }
        if (elapsed == CHANNEL_TICKS) completeSummoning(server, player, stack);
    }

    private static void completeSummoning(ServerLevel level, ServerPlayer player, ItemStack stack) {
        BlockPos center = player.blockPosition();
        if (!isStandingCircle(level, center)) {
            fail(player, "no_standing_stones");
            return;
        }
        LordOfTorment lord = summonLord(level, center);
        if (lord == null) {
            fail(player, "obstructed");
            return;
        }
        if (!player.getAbilities().instabuild) stack.shrink(1);
        player.stopUsingItem();
        level.sendParticles(ParticleTypes.FLAME, lord.getX(), lord.getY(0.5), lord.getZ(), 32, 0.5, 0.8, 0.5, 0.1);
        level.playSound(null, lord.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 0.9F, 1.0F);
        level.explode(lord, lord.getX(), lord.getEyeY(), lord.getZ(), 7, false, Level.ExplosionInteraction.MOB);
    }

    @Nullable
    private static LordOfTorment summonLord(ServerLevel level, BlockPos center) {
        RandomSource random = level.getRandom();
        int x = SUMMON_OFFSETS[random.nextInt(SUMMON_OFFSETS.length)];
        int z = SUMMON_OFFSETS[random.nextInt(SUMMON_OFFSETS.length)];
        BlockPos pos = center.offset(x, 0, z);
        while (!level.isEmptyBlock(pos) && pos.getY() < center.getY() + 8) pos = pos.above();
        while (level.isEmptyBlock(pos.below()) && pos.getY() > level.getMinBuildHeight()) pos = pos.below();
        if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) return null;
        LordOfTorment lord = WitcheryEntities.LORD_OF_TORMENT.get().create(level);
        if (lord == null) return null;
        lord.moveTo(pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5, 0, 0);
        lord.setPersistenceRequired();
        return level.addFreshEntity(lord) ? lord : null;
    }

    public static boolean isStandingCircle(LevelReader level, BlockPos center) {
        int radius = STANDING_STONES.length / 2;
        // The original check scans ten rows, starting at the opposite edge of the pattern.
        for (int row = 0; row < STANDING_STONES.length - 1; row++) {
            String outline = STANDING_STONES[STANDING_STONES.length - 1 - row];
            for (int column = 0; column < outline.length(); column++) {
                char height = outline.charAt(column);
                if (height == '0') continue;
                BlockPos pos = center.offset(column - radius, 0, row - radius);
                if (!level.getBlockState(pos.below()).blocksMotion()
                        || level.getBlockState(pos).blocksMotion() != (height == '2' || height == '4')
                        || level.getBlockState(pos.above()).blocksMotion() != (height == '4')
                        || level.getBlockState(pos.above(2)).blocksMotion() != (height == '3' || height == '4')
                        || level.getBlockState(pos.above(3)).blocksMotion()) return false;
            }
        }
        return true;
    }

    private static void fail(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable("message.witchery.torment_contract." + key)
                .withStyle(ChatFormatting.RED), true);
        player.playSound(SoundEvents.NOTE_BLOCK_SNARE.value(), 0.9F, 1.0F);
        player.stopUsingItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("item.witchery.contract_of_torment.hint").withStyle(ChatFormatting.GRAY));
    }
}
