package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.event.WitcheryBlockBreak;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class VolatilityEffect extends MobEffect {
    private static final Set<LivingEntity> EXPLODING = new HashSet<>();

    public VolatilityEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) return;
        var effect = victim.getEffect(WitcheryEffects.VOLATILITY.get());
        DamageSource source = event.getSource();
        if (effect == null || !canTrigger(source) || EXPLODING.contains(victim)) return;

        int amplifier = effect.getAmplifier();
        boolean chainReaction = source.is(DamageTypeTags.IS_EXPLOSION);
        var random = victim.getRandom();
        if (!chainReaction && random.nextInt(5 - Math.min(amplifier, 3)) != 0) return;

        if (random.nextInt(amplifier + 3) == 0) victim.removeEffect(WitcheryEffects.VOLATILITY.get());
        boolean damagesBlocks = WitcheryServerConfig.ALLOW_VOLATILITY_POTION_BLOCK_DAMAGE.get()
                && canDamageBlocks(level, victim);
        // Other affected creatures can detonate. An explosion already in this chain cannot reenter its own hurt event.
        EXPLODING.add(victim);
        try {
            level.explode(chainReaction ? victim : null, victim.getX(), victim.getY(), victim.getZ(),
                    Math.min(2.0F + 0.5F * amplifier, 3.0F),
                    damagesBlocks ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
        } finally {
            EXPLODING.remove(victim);
        }
    }

    private static boolean canTrigger(DamageSource source) {
        return !source.is(DamageTypes.DROWN) && !source.is(DamageTypes.IN_WALL) && !source.is(DamageTypes.FALL)
                && !source.is(DamageTypes.FELL_OUT_OF_WORLD) && !source.is(DamageTypes.STARVE);
    }

    private static boolean canDamageBlocks(ServerLevel level, LivingEntity victim) {
        var pos = victim.blockPosition();
        var state = level.getBlockState(pos);
        return state.getDestroySpeed(level, pos) >= 0 && (!(victim instanceof Player player)
                || level.mayInteract(player, pos)
                && !WitcheryBlockBreak.isCanceled(level, pos, state, player));
    }
}
