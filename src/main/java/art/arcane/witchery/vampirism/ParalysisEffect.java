package art.arcane.witchery.vampirism;

import art.arcane.witchery.entity.Lilith;
import art.arcane.witchery.entity.LordOfTorment;
import art.arcane.witchery.entity.VillageGuard;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class ParalysisEffect extends MobEffect {
    public ParalysisEffect() {
        super(MobEffectCategory.HARMFUL, 0x7A7191);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "E69059D5-CAE6-4695-9BE3-C6F0F22151E8", -1,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (freezes(entity, amplifier)) super.addAttributeModifiers(entity, attributes, amplifier);
    }

    private static boolean freezes(LivingEntity entity, int amplifier) {
        return !(entity instanceof Lilith || entity instanceof LordOfTorment || entity.getType().is(Tags.EntityTypes.BOSSES))
                && (amplifier < 5 || !(entity instanceof Villager || entity instanceof VillageGuard))
                && (!(entity instanceof Player) || amplifier >= 2);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (amplifier >= 5 && entity instanceof Villager villager && entity.level() instanceof ServerLevel world) {
            if (villager.tickCount % 10 == 0) {
                lureTarget(villager, world).ifPresent(vampire -> villager.getBrain()
                        .setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vampire, 0.8F, 1)));
            }
            return;
        }
        if (!freezes(entity, amplifier)) return;
        entity.setDeltaMovement(0, Math.min(0, entity.getDeltaMovement().y), 0);
        if (entity instanceof Mob mob) mob.getNavigation().stop();
        if (entity.level().isClientSide) return;
        if (entity instanceof Creeper creeper) creeper.setSwellDir(-1);
        if (amplifier >= 4 && entity instanceof ServerPlayer player) {
            MobEffectInstance effect = entity.getEffect(this);
            if (effect != null && effect.getDuration() <= 1) {
                player.addEffect(new MobEffectInstance(WitcheryEffects.QUEASY.get(), 1_800, 0, true, true));
            }
        }
    }

    public static Optional<ServerPlayer> lureTarget(LivingEntity resident, ServerLevel world) {
        return world.getPlayers(player -> player.isAlive() && !player.isSpectator() && Vampirism.level(player) >= 8
                        && player.distanceToSqr(resident) <= 256).stream()
                .min(Comparator.comparingDouble(resident::distanceToSqr));
    }
}
