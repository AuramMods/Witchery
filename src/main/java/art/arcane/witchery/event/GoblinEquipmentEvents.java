package art.arcane.witchery.event;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.entity.MogQuiverArrow;
import art.arcane.witchery.item.GoblinEquipmentItem;
import art.arcane.witchery.item.GoblinEquipmentItem.Piece;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class GoblinEquipmentEvents {
    private static final String MOGGED = "WITCMogged";
    private static final RandomSource SHOT_SOUND_RANDOM = RandomSource.create();
    private static final Set<LivingEntity> LAUNCHED = Collections.newSetFromMap(new WeakHashMap<>());

    private GoblinEquipmentEvents() {}

    @SubscribeEvent
    public static void nock(ArrowNockEvent event) {
        var player = event.getEntity();
        if (!(event.getBow().getItem() instanceof BowItem)) return;
        if (WitcheryPlayerDataProvider.get(player).map(data -> data.getCreatureTypeOrdinal() != 0).orElse(false)) {
            if (player.isUsingItem() && player.getUseItem() == event.getBow()) player.stopUsingItem();
            event.setAction(InteractionResultHolder.fail(event.getBow()));
        } else if (event.getAction() == null && GoblinEquipmentItem.wears(player, Piece.QUIVER)) {
            player.startUsingItem(event.getHand());
            event.setAction(InteractionResultHolder.consume(event.getBow()));
        }
    }

    @SubscribeEvent
    public static void loose(ArrowLooseEvent event) {
        var player = event.getEntity();
        if (!(event.getBow().getItem() instanceof BowItem) || !GoblinEquipmentItem.wears(player, Piece.QUIVER)) return;
        float power = BowItem.getPowerForTime(event.getCharge());
        if (power < 0.1F) return;
        event.setCanceled(true);
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        shoot(event, power, level, player);
    }

    @SubscribeEvent
    public static void hurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                && event.getSource().getDirectEntity() instanceof AbstractArrow arrow && arrow.getPersistentData().getBoolean(MOGGED)) {
            if (target.hasImpulse) event.setAmount(event.getAmount() * 3);
            target.removeEffect(MobEffects.WEAKNESS);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200), arrow);
        } else if (event.getSource().is(DamageTypes.PLAYER_ATTACK) && event.getSource().getEntity() instanceof Player player
                && player.getMainHandItem().isEmpty() && GoblinEquipmentItem.wears(player, Piece.GURDLE)) {
            event.setAmount(5);
            LAUNCHED.add(target);
        }
    }

    @SubscribeEvent
    public static void launchPunchedTargets(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel) || LAUNCHED.isEmpty()) return;
        var iterator = LAUNCHED.iterator();
        while (iterator.hasNext()) {
            LivingEntity target = iterator.next();
            if (target.level() != event.level) continue;
            iterator.remove();
            if (!target.isAlive()) continue;
            var motion = target.getDeltaMovement();
            target.setDeltaMovement(motion.x, 1, motion.z);
            target.hurtMarked = true;
            if (target instanceof ServerPlayer player) {
                WitcheryNetwork.sendPushTarget(player, target.getId(), motion.x, 1, motion.z);
            }
        }
    }

    private static void shoot(ArrowLooseEvent event, float power, ServerLevel level, Player player) {
        var bow = event.getBow();
        Arrow arrow = new MogQuiverArrow(player, power);
        arrow.getPersistentData().putBoolean(MOGGED, true);
        arrow.setCritArrow(power == 1);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        int strength = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
        if (strength > 0) arrow.setBaseDamage(arrow.getBaseDamage() + strength * 0.5 + 0.5);
        int knockback = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
        if (knockback > 0) arrow.setKnockback(knockback);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) arrow.setSecondsOnFire(100);
        if (!level.addFreshEntity(arrow)) return;
        bow.hurtAndBreak(1, player, wearer -> wearer.broadcastBreakEvent(wearer.getUsedItemHand()));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1,
                1 / (SHOT_SOUND_RANDOM.nextFloat() * 0.4F + 1.2F) + power * 0.5F);
    }
}
