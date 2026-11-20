package art.arcane.witchery.item;

import art.arcane.witchery.entity.CovenWitch;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.world.DreamTravel;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class SeerStoneItem extends Item {
    private static final String MESSAGE = "message.witchery.seer_stone.";
    private static final int USE_TICKS = 1200;
    private static final String[] CURSE_TAGS = {"witcheryCursed", "witcheryInsanity", "witcherySinking",
            "witcheryOverheating", "witcheryWakingNightmare"};
    private static final String[] CURSE_NAMES = {"misfortune", "insanity", "sinking", "overheating", "waking_nightmare"};

    public SeerStoneItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TICKS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer reader) read(reader, lookedAtPlayer(reader));
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        if (!(living instanceof ServerPlayer player)) return;
        int elapsed = USE_TICKS - remaining;
        if (elapsed <= 0 || elapsed % 20 != 0) return;
        int member = elapsed / 20 - 1;
        if (member < CovenWitch.covenSize(player)) {
            CovenWitch.recallMember(player, member);
        } else if (member == 0) {
            player.playSound(SoundEvents.NOTE_BLOCK_SNARE.value(), 0.9F, 1.0F);
            player.displayClientMessage(Component.translatable(MESSAGE + "no_coven"), true);
        }
    }

    private static ServerPlayer lookedAtPlayer(ServerPlayer reader) {
        Vec3 eyes = reader.getEyePosition();
        Vec3 trace = reader.getViewVector(1).scale(3);
        HitResult block = reader.pick(3, 1, false);
        double distance = block.getType() == HitResult.Type.MISS ? 9 : eyes.distanceToSqr(block.getLocation());
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(reader, eyes, eyes.add(trace),
                reader.getBoundingBox().expandTowards(trace).inflate(1),
                entity -> entity instanceof ServerPlayer player && !player.isSpectator(), distance);
        return hit == null ? reader : (ServerPlayer) hit.getEntity();
    }

    private static void read(ServerPlayer reader, ServerPlayer target) {
        CompoundTag persisted = target.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        int manifest = DreamTravel.manifestSeconds(target);
        String familiar = FamiliarBinding.familiarName(target);
        int coven = CovenWitch.covenSize(target);
        CompoundTag knownSpells = persisted.getCompound("WITCSpellBook");

        MutableComponent reading = Component.translatable(MESSAGE + "reading", target.getDisplayName())
                .append(" ").append(Component.translatable(MESSAGE + "manifestation", manifest))
                .append(" ").append(familiar.isEmpty() ? Component.translatable(MESSAGE + "no_familiar")
                        : Component.translatable(MESSAGE + "familiar", familiar))
                .append(" ").append(coven == 0 ? Component.translatable(MESSAGE + "no_coven")
                        : Component.translatable(MESSAGE + "coven", coven));
        MutableComponent spells = Component.empty();
        for (String known : knownSpells.getAllKeys().stream().sorted().toList()) {
            if (knownSpells.getBoolean(known)) {
                if (!spells.getSiblings().isEmpty()) spells.append(", ");
                spells.append(Component.translatableWithFallback("spell.witchery." + known, known));
            }
        }
        reading.append(" ").append(spells.getSiblings().isEmpty() ? Component.translatable(MESSAGE + "no_spells")
                : Component.translatable(MESSAGE + "spells", spells));
        boolean cursed = false;
        for (int index = 0; index < CURSE_TAGS.length; index++) {
            int level = persisted.getInt(CURSE_TAGS[index]);
            if (level > 0) {
                reading.append(" ").append(Component.translatable(MESSAGE + "curse." + CURSE_NAMES[index], level));
                cursed = true;
            }
        }
        if (!cursed) reading.append(" ").append(Component.translatable(MESSAGE + "no_curse"));
        reader.sendSystemMessage(reading.withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
