package art.arcane.witchery.lycanthropy;

import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.entity.WereVillager;
import art.arcane.witchery.entity.WitcheryBolt;
import art.arcane.witchery.entity.Wolfman;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.UUID;

public final class Lycanthropy {
    private static final int HUMAN = 0;
    private static final int WOLF = 1;
    private static final int WOLFMAN = 2;
    public static final EntityDimensions WOLF_DIMENSIONS = EntityDimensions.fixed(0.6F, 0.8F);
    public static final float WOLF_EYE_HEIGHT = 0.736F;
    private static final UUID SPEED = UUID.fromString("10536417-7aa6-4033-a598-8e934ca77d98");
    private static final UUID ATTACK = UUID.fromString("46c5271c-193b-4d41-9cab-d071aae9d4a");
    private static final UUID HEALTH = UUID.fromString("615920f9-6675-4779-8b18-6a62a3671e94");
    private static final UUID STEP = UUID.fromString("3359163d-0a72-4ca2-ae7b-f1d13a6c6031");

    private record FormStats(double speed, double jump, double leap, int health, float attack, float resistance,
                             int fallAllowance, float damageCap) {}

    private static final FormStats NONE = new FormStats(0, 0, 0, 0, 0, 0, 0, 0);
    private static final FormStats[] WOLF_STATS = {
            NONE,
            new FormStats(0.5, 0.2, 0.2, 0, 1, 0, 2, 4),
            new FormStats(0.5, 0.2, 0.2, 0, 1, 0, 2, 3),
            new FormStats(0.75, 0.2, 0.3, 0, 2, 0, 2, 3),
            new FormStats(0.75, 0.2, 0.4, 0, 2, 0, 3, 3),
            new FormStats(0.75, 0.2, 0.5, 0, 2, 0, 3, 2),
            new FormStats(1, 0.2, 0.6, 0, 2, 1, 3, 2),
            new FormStats(1.25, 0.3, 0.7, 4, 2, 1, 4, 2),
            new FormStats(1.5, 0.3, 0.8, 8, 3, 2, 4, 2),
            new FormStats(1.75, 0.3, 0.9, 12, 3, 3, 5, 2),
            new FormStats(1.75, 0.3, 1, 12, 3, 3, 5, 2)
    };
    private static final FormStats[] WOLFMAN_STATS = {
            NONE,
            new FormStats(0, 0, 0, 0, 0, 0, 0, 4),
            new FormStats(0, 0, 0, 0, 0, 0, 0, 3),
            new FormStats(0, 0, 0, 0, 0, 0, 0, 3),
            new FormStats(0, 0, 0, 0, 0, 0, 0, 3),
            new FormStats(0.2, 0.2, 0.2, 20, 4, 3, 3, 2),
            new FormStats(0.2, 0.3, 0.2, 20, 4, 3, 4, 2),
            new FormStats(0.4, 0.4, 0.4, 20, 5, 4, 5, 2),
            new FormStats(0.4, 0.5, 0.4, 30, 6, 4, 6, 2),
            new FormStats(0.5, 0.6, 0.6, 40, 7, 5, 7, 2),
            new FormStats(0.5, 0.6, 0.6, 40, 7, 5, 7, 2)
    };

    private Lycanthropy() {}

    public static int rank(Player player) {
        return WitcheryPlayerDataProvider.get(player).map(WitcheryPlayerData::getWerewolfLevel).orElse(0);
    }

    public static boolean isWolf(Player player) {
        return WitcheryPlayerDataProvider.get(player).map(data -> data.getCreatureTypeOrdinal() == WOLF).orElse(false);
    }

    public static boolean isWolfman(Player player) {
        return WitcheryPlayerDataProvider.get(player).map(data -> data.getCreatureTypeOrdinal() == WOLFMAN).orElse(false);
    }

    public static boolean canDig(Player player, BlockState state) {
        return player.isShiftKeyDown() && isWolf(player) && rank(player) >= 3
                && (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)
                || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM) || state.is(Blocks.GRAVEL));
    }

    public static boolean isShifted(LivingEntity entity) {
        return entity instanceof Wolfman || entity instanceof Player player
                && WitcheryPlayerDataProvider.get(player).map(Lycanthropy::isShifted).orElse(false);
    }

    public static boolean isWerewolf(LivingEntity entity) {
        return isShifted(entity) || entity instanceof WereVillager || entity instanceof Player player && rank(player) > 0;
    }

    private static boolean isShifted(WitcheryPlayerData data) {
        int form = data.getCreatureTypeOrdinal();
        return form == WOLF || form == WOLFMAN;
    }

    public static boolean isFullMoon(Level level) {
        return level.dimensionType().natural() && !level.dimensionType().hasFixedTime() && level.isNight() && level.getMoonPhase() == 0;
    }

    public static void infect(ServerPlayer player) {
        setRank(player, 1);
        player.displayClientMessage(Component.translatable("message.witchery.werewolf.infection"), true);
    }

    public static void setRank(ServerPlayer player, int rank) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (data.getWerewolfLevel() == rank) return;
            data.setWerewolfLevel(rank);
            if (data.getWerewolfLevel() == 0 && isShifted(data)) setForm(player, data, HUMAN, false);
            else {
                if (isShifted(data)) applyAttributes(player, data);
                sync(player, data);
            }
        });
    }

    public static void restore(ServerPlayer player) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            applyAttributes(player, data);
            if (isShifted(data)) player.refreshDimensions();
        });
    }

    public static void tick(ServerPlayer player) {
        if (player.tickCount % 40 != 1 || !player.isAlive() || player.isSpectator()) return;
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (data.getWerewolfLevel() == 0) return;
            boolean fullMoon = isFullMoon(player.level());
            var charm = WitcheryItems.getRequired("mooncharm").get();
            boolean hasCharm = player.getInventory().items.stream().anyMatch(stack -> stack.is(charm))
                    || player.getOffhandItem().is(charm);
            boolean canShift = !wolfsbaneActive(player, data.getWerewolfLevel()) && !hasCharm;
            if (isShifted(data) && !fullMoon && canShift) setForm(player, data, HUMAN, false);
            if (data.getCreatureTypeOrdinal() == HUMAN && fullMoon && canShift && !player.isSleeping()) {
                setForm(player, data, WOLF, true);
            }
            if (isShifted(data)) enforceShiftedEffects(player, data);
        });
    }

    public static boolean useMoonCharm(ServerPlayer player) {
        return WitcheryPlayerDataProvider.get(player).map(data -> {
            int rank = data.getWerewolfLevel();
            if (rank < 2 || wolfsbaneActive(player, rank) || player.isSleeping()) return false;
            boolean chooseWolfman = rank >= 5 && player.isShiftKeyDown();
            int next = switch (data.getCreatureTypeOrdinal()) {
                case HUMAN -> chooseWolfman ? WOLFMAN : WOLF;
                case WOLF -> chooseWolfman ? WOLFMAN : HUMAN;
                case WOLFMAN -> chooseWolfman ? HUMAN : WOLF;
                default -> -1;
            };
            if (next < 0) return false;
            setForm(player, data, next, false);
            return true;
        }).orElse(false);
    }

    public static boolean wolfsbaneActive(Player player, int rank) {
        var effect = player.getEffect(WitcheryEffects.WOLFSBANE.get());
        return effect != null && Math.max(1, effect.getAmplifier() * 3) >= rank;
    }

    public static boolean hasWolfsbane(LivingEntity entity) {
        return entity.hasEffect(WitcheryEffects.WOLFSBANE.get());
    }

    private static void setForm(ServerPlayer player, WitcheryPlayerData data, int form, boolean howl) {
        data.setCreatureTypeOrdinal(form);
        applyAttributes(player, data);
        player.refreshDimensions();
        sync(player, data);
        player.serverLevel().sendParticles(ParticleTypes.POOF, player.getX(), player.getY(0.6), player.getZ(),
                16, 0.45, 0.6, 0.45, 0.045);
        player.serverLevel().playSound(null, player, howl ? WitcherySounds.WOLFMAN_HOWL.get() : SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 1, 1);
    }

    private static void sync(ServerPlayer player, WitcheryPlayerData data) {
        data.bumpSyncRevision();
        if (player.connection != null) {
            WitcheryNetwork.sendPartialExtendedPlayerSync(player, data);
            WitcheryNetwork.broadcastPlayerStyle(player, data);
        }
    }

    private static void enforceShiftedEffects(ServerPlayer player, WitcheryPlayerData data) {
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
        player.removeEffect(MobEffects.POISON);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            boolean remove = slot.getType() == EquipmentSlot.Type.ARMOR
                    || slot == EquipmentSlot.MAINHAND && data.getCreatureTypeOrdinal() == WOLFMAN;
            if (!remove) continue;
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty() || stack.is(WitcheryItems.getRequired("mooncharm").get())) continue;
            if (player.drop(stack.copy(), false) != null) player.setItemSlot(slot, ItemStack.EMPTY);
        }
    }

    private static FormStats stats(WitcheryPlayerData data) {
        return switch (data.getCreatureTypeOrdinal()) {
            case WOLF -> WOLF_STATS[data.getWerewolfLevel()];
            case WOLFMAN -> WOLFMAN_STATS[data.getWerewolfLevel()];
            default -> NONE;
        };
    }

    private static FormStats stats(Player player) {
        return WitcheryPlayerDataProvider.get(player).map(Lycanthropy::stats).orElse(NONE);
    }

    private static void applyAttributes(ServerPlayer player, WitcheryPlayerData data) {
        var stats = stats(data);
        replaceModifier(player, Attributes.MOVEMENT_SPEED, SPEED, stats.speed(), AttributeModifier.Operation.MULTIPLY_TOTAL);
        replaceModifier(player, Attributes.ATTACK_DAMAGE, ATTACK, stats.attack(), AttributeModifier.Operation.MULTIPLY_TOTAL);
        replaceModifier(player, Attributes.MAX_HEALTH, HEALTH, stats.health(), AttributeModifier.Operation.ADDITION);
        replaceModifier(player, ForgeMod.STEP_HEIGHT_ADDITION.get(), STEP, isShifted(data) ? 0.4 : 0,
                AttributeModifier.Operation.ADDITION);
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void replaceModifier(Player player, Attribute attribute, UUID id, double amount, AttributeModifier.Operation operation) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0) instance.addTransientModifier(new AttributeModifier(id, "Witchery werewolf form", amount, operation));
    }

    public static void jump(Player player) {
        var stats = stats(player);
        if (stats.jump() == 0) return;
        var velocity = player.getDeltaMovement().add(0, stats.jump(), 0);
        if (player.isSprinting()) {
            float yaw = player.getYRot() * Mth.DEG_TO_RAD;
            velocity = velocity.add(-Mth.sin(yaw) * stats.leap(), 0, Mth.cos(yaw) * stats.leap());
        }
        player.setDeltaMovement(velocity);
    }

    public static void fall(Player player, LivingFallEvent event) {
        event.setDistance(Math.max(0, event.getDistance() - stats(player).fallAllowance()));
    }

    public static void adjustDamage(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        var attacker = source.getDirectEntity();
        if (attacker instanceof Player player && isShifted(player) && source.is(DamageTypes.PLAYER_ATTACK)) {
            if (player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE)) {
                event.setAmount(2);
            } else if (player.isSprinting()) event.setAmount(event.getAmount() + stats(player).attack());
        }
        if (!(event.getEntity() instanceof Player player) || !isShifted(player) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.FALL)) return;
        var stats = stats(player);
        float amount = event.getAmount();
        if (!source.is(DamageTypeTags.IS_PROJECTILE)) amount = Math.max(0, amount - stats.resistance());
        if (!(attacker instanceof LivingEntity living) || !isShifted(living)) {
            amount = isSilver(source) ? amount + 5 : Math.max(0.5F, Math.min(stats.damageCap(), amount));
        }
        if (amount > 0) event.setAmount(amount);
        else event.setCanceled(true);
    }

    public static boolean isSilver(DamageSource source) {
        if (source.is(WitcheryDamageTypes.SILVER_BREW)) return true;
        if (source.getDirectEntity() instanceof WitcheryBolt bolt) return bolt.kind() == WitcheryBolt.Kind.SILVER;
        return !source.is(DamageTypeTags.IS_PROJECTILE) && source.getEntity() instanceof LivingEntity living
                && living.getMainHandItem().is(WitcheryItems.getRequired("silversword").get());
    }
}
