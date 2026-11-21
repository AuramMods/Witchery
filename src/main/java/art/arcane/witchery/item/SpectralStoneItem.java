package art.arcane.witchery.item;

import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.infusion.OtherwhereInfusion;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SpectralStoneItem extends Item {
    private static final String CONTENTS = "SpectralContents";
    private static final int CHARGE_TICKS = 40;
    private static final int MAX_CREATURES = 3;

    public SpectralStoneItem() {
        super(new Properties().stacksTo(16).rarity(Rarity.RARE));
    }

    public static int typeId(ItemStack stack) {
        var tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(CONTENTS) & 15;
    }

    public static int creatureCount(ItemStack stack) {
        var tag = stack.getTag();
        return tag == null || kind(stack) == null ? 0 : Math.min(MAX_CREATURES, tag.getInt(CONTENTS) >>> 4 & 7);
    }

    @Nullable
    private static SpectralUndead.Kind kind(ItemStack stack) {
        int type = typeId(stack);
        for (SpectralUndead.Kind kind : SpectralUndead.Kind.values()) {
            if (kind.legacyId() == type) return kind;
        }
        return null;
    }

    public static void applyLegacyContents(ItemStack stack, int metadata) {
        if (metadata == 0) stack.removeTagKey(CONTENTS);
        else stack.getOrCreateTag().putInt(CONTENTS, metadata & 127);
    }

    public static boolean bindFromCircle(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atBottomCenterOf(pos);
        List<SpectralUndead> creatures = level.getEntitiesOfClass(SpectralUndead.class, new AABB(center, center).inflate(5),
                entity -> entity.isAlive() && entity.distanceToSqr(center) <= 25);
        if (creatures.isEmpty()) return false;
        SpectralUndead.Kind kind = creatures.get(0).kind();
        List<SpectralUndead> selected = creatures.stream().filter(entity -> entity.kind() == kind).limit(MAX_CREATURES).toList();

        ItemStack stone = new ItemStack(WitcheryItems.getRequired("spectralstone").get());
        applyLegacyContents(stone, kind.legacyId() | selected.size() << 4);
        ItemEntity drop = new ItemEntity(level, center.x, center.y + 1.5, center.z, stone, 0, 0.3, 0);
        drop.setDefaultPickUpDelay();
        if (!level.addFreshEntity(drop)) return false;
        for (SpectralUndead mob : selected) {
            level.sendParticles(ParticleTypes.PORTAL, mob.getX(), mob.getY(0.5), mob.getZ(), 16, 0.45, 0.7, 0.45, 0.1);
            level.playSound(null, mob.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.7F, 1);
            mob.discard();
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        SpectralUndead.Kind kind = kind(stack);
        if (kind != null && creatureCount(stack) > 0) {
            tooltip.add(Component.translatable("tooltip.witchery.spectralstone.contents", kind.type().getDescription(), creatureCount(stack)));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 400;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (user instanceof ServerPlayer player && getUseDuration(stack) - remaining == CHARGE_TICKS) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1, 1);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        if (!(level instanceof ServerLevel server) || !(user instanceof ServerPlayer player)) return;
        SpectralUndead.Kind kind = kind(stack);
        int count = creatureCount(stack);
        if (kind == null || count == 0 || getUseDuration(stack) - remaining < CHARGE_TICKS) {
            playMiss(player);
            return;
        }
        HitResult hit = OtherwhereInfusion.rayTrace(player, 16, target -> true);
        BlockPos pos;
        if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) {
            pos = block.getBlockPos().relative(block.getDirection());
        } else if (hit instanceof EntityHitResult entity) {
            pos = entity.getEntity().blockPosition();
        } else {
            playMiss(player);
            return;
        }

        int released = 0;
        for (int i = 0; i < count; i++) {
            SpectralUndead mob = SpectralUndead.summonNear(server, pos, kind, 0, 1);
            if (mob == null) break;
            mob.setSummoner(player);
            released++;
        }
        if (released == 0) {
            playMiss(player);
        } else if (!player.getAbilities().instabuild) {
            int contents = count == released ? 0 : kind.legacyId() | (count - released) << 4;
            if (stack.getCount() == 1) {
                applyLegacyContents(stack, contents);
            } else {
                ItemStack used = stack.split(1);
                applyLegacyContents(used, contents);
                player.getInventory().placeItemBackInInventory(used);
            }
        }
    }

    private static void playMiss(ServerPlayer player) {
        player.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 1, 1);
    }
}
