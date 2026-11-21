package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
abstract class TrackedBrewEffect extends MobEffect {
    TrackedBrewEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity target, AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(target, attributes, amplifier);
        if (!(target.level() instanceof ServerLevel level)) return;
        // Living entity renderers read the effect instance on clients tracking another entity.
        var effect = target.getEffect(this);
        if (effect != null) level.getChunkSource().broadcast(target, new ClientboundUpdateMobEffectPacket(target.getId(), effect));
    }

    @Override
    public void removeAttributeModifiers(LivingEntity target, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(target, attributes, amplifier);
        if (target.level() instanceof ServerLevel level) {
            level.getChunkSource().broadcast(target, new ClientboundRemoveMobEffectPacket(target.getId(), this));
        }
    }

    @SubscribeEvent
    public static void syncTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer viewer) || !(event.getTarget() instanceof LivingEntity target)) return;
        for (var effect : target.getActiveEffects()) {
            if (effect.getEffect() instanceof TrackedBrewEffect) {
                viewer.connection.send(new ClientboundUpdateMobEffectPacket(target.getId(), effect));
            }
        }
    }
}
