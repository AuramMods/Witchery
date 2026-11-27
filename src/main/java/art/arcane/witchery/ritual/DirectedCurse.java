package art.arcane.witchery.ritual;

import art.arcane.witchery.block.entity.AreaWardBlockEntity;
import art.arcane.witchery.brewing.EnvironmentalCurseEffect;
import art.arcane.witchery.entity.WitchHunterPursuit;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.item.VoodooPoppetItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public enum DirectedCurse {
    MISFORTUNE("witcheryCursed"),
    INSANITY("witcheryInsanity"),
    SINKING("witcherySinking"),
    OVERHEATING("witcheryOverheating"),
    WAKING_NIGHTMARE("witcheryWakingNightmare");

    private static final List<MobEffect> AILMENTS = List.of(MobEffects.POISON, MobEffects.WEAKNESS,
            MobEffects.BLINDNESS, MobEffects.DIG_SLOWDOWN, MobEffects.MOVEMENT_SLOWDOWN);
    private static final DirectedCurse[] ALL = values();
    private final String tag;

    DirectedCurse(String tag) {
        this.tag = tag;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static boolean clearAtGoddess(Player player) {
        boolean cured = false;
        for (DirectedCurse curse : ALL) {
            if (!curseData(player).contains(curse.tag)) continue;
            curse.setStrength(player, 0);
            player.displayClientMessage(Component.translatable("message.witchery.goddess." + curse.key())
                    .withStyle(ChatFormatting.BLUE), false);
            if (curse == SINKING && player instanceof ServerPlayer server) InfusionEnergy.sync(server);
            cured = true;
        }
        if (cured) clearAilments(player);
        return cured;
    }

    public int strength(LivingEntity target) {
        return curseData(target).getInt(tag);
    }

    private static CompoundTag curseData(LivingEntity target) {
        return target instanceof Player ? target.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG)
                : target.getPersistentData();
    }

    private void setStrength(LivingEntity target, int strength) {
        CompoundTag data = curseData(target);
        if (strength > 0) data.putInt(tag, strength);
        else data.remove(tag);
        if (target instanceof Player player) player.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
    }

    public static void syncSinking(Player player, int strength) {
        SINKING.setStrength(player, strength);
    }

    public static void tickSinkingPlayer(Player player) {
        int strength = SINKING.strength(player);
        if (strength > 0) EnvironmentalCurseEffect.sinkPlayer(player, strength - 1);
    }

    @Nullable
    public String perform(ServerLevel source, BlockPos heart, @Nullable ServerPlayer caster,
                          @Nullable LivingEntity target, int coven, boolean inflict, boolean legacy) {
        if (target == null) return "message.witchery.rite.no_taglock_target";
        int strength = 1 + (caster != null && FamiliarBinding.hasCurseMastery(caster) ? 1 : 0)
                + (coven >= 6 ? 2 : coven >= 3 ? 1 : 0);
        boolean afflicted;
        if (inflict) {
            if (caster != null) WitchHunterPursuit.noticeBlackMagic(caster);
            if ((this == MISFORTUNE || this == WAKING_NIGHTMARE)
                    && (legacy ? HunterClothingItem.resistsOriginalCurse(target) : HunterClothingItem.resistsCurse(target))) {
                return "message.witchery.rite.hunter_curse_protection";
            }
            if (AreaWardBlockEntity.protectsDirectedCurse(target)) return "message.witchery.rite.area_curse_protection";
            afflicted = !(target instanceof ServerPlayer player && VoodooPoppetItem.deflectRite(caster, player, strength > 1 ? 3 : 1));
            if (afflicted) setStrength(target, Math.max(strength, strength(target)));
        } else {
            int current = strength(target);
            int remaining = cureStrength(source.random, strength, current);
            setStrength(target, remaining);
            afflicted = remaining > 0;
            if (!afflicted) clearAilments(target);
        }
        if (this == SINKING && target instanceof ServerPlayer player) InfusionEnergy.sync(player);
        if (legacy) {
            WitcheryNetwork.sendDirectedCurseRite(source, heart, afflicted);
            return null;
        }
        source.sendParticles(afflicted ? ParticleTypes.FLAME : ParticleTypes.INSTANT_EFFECT,
                heart.getX() + 0.5, heart.getY() + 0.1, heart.getZ() + 0.5, 16, 0.5, 0.6, 0.5, 0.035);
        source.playSound(null, heart, afflicted ? SoundEvents.ENDER_DRAGON_GROWL : SoundEvents.PLAYER_LEVELUP,
                SoundSource.BLOCKS, 1, 1);
        return null;
    }

    static int cureStrength(RandomSource random, int cure, int current) {
        if (current <= 0) return 0;
        boolean worsens = cure > current ? random.nextInt(20) == 0
                : cure == current ? random.nextInt(4) == 0 : random.nextInt(4) != 0;
        return worsens ? (current == Integer.MAX_VALUE ? current : current + 1) : 0;
    }

    private static void clearAilments(LivingEntity target) {
        AILMENTS.forEach(target::removeEffect);
    }

    public static void tick(LivingEntity target) {
        if (!target.isAlive()) return;
        if (target instanceof ServerPlayer player) tickSinkingPlayer(player);
        if (target.level().getGameTime() % 20 != 0) return;
        CompoundTag data = curseData(target);
        for (DirectedCurse curse : ALL) {
            if (!data.contains(curse.tag)) continue;
            int strength = data.getInt(curse.tag);
            if (strength > 0) curse.tickPersistent(target, strength);
            else data.remove(curse.tag);
        }
    }

    private void tickPersistent(LivingEntity target, int strength) {
        switch (this) {
            case MISFORTUNE -> tickMisfortune(target, strength);
            case INSANITY -> {
                if (target instanceof ServerPlayer player) EnvironmentalCurseEffect.spreadInsanity(player, strength);
            }
            case SINKING -> {
                if (!(target instanceof Player)) EnvironmentalCurseEffect.sinkCreature(target, strength);
            }
            case OVERHEATING -> EnvironmentalCurseEffect.overheat(target, strength);
            case WAKING_NIGHTMARE -> {
                if (target instanceof ServerPlayer player) {
                    if (player.level().dimension().equals(WitcheryDimensions.DREAM)) setStrength(player, 0);
                    else DreamTravel.spawnWakingNightmare(player, strength);
                }
            }
        }
    }

    private static void tickMisfortune(LivingEntity target, int strength) {
        RandomSource random = target.getRandom();
        if (AILMENTS.stream().anyMatch(target::hasEffect) || random.nextInt(20) != 0) return;
        int amplifier = Math.min(strength - 1, 4);
        switch (random.nextInt(Math.min(strength, 5) + 1)) {
            case 0 -> target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 600, amplifier));
            case 1 -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, amplifier));
            case 2 -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                    (int) Math.min(Integer.MAX_VALUE, 260L + 40L * strength), Math.min(strength - 2, 4)));
            case 3 -> {
                int duration = (int) Math.min(Integer.MAX_VALUE, 100L * strength);
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration));
                if (strength > 5) target.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration));
            }
            case 5 -> dropHeldItem(target);
        }
    }

    private static void dropHeldItem(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            player.drop(true);
            return;
        }
        ItemStack stack = target.getMainHandItem();
        if (!stack.isEmpty() && target.spawnAtLocation(stack) != null) {
            target.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }
}
