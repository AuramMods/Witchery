package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class DiseasedEffect extends MobEffect {
    public DiseasedEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", 2,
                AttributeModifier.Operation.ADDITION);
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        return -0.5 * (Math.min(amplifier, 1) + 1);
    }

    @Override
    public void addAttributeModifiers(LivingEntity target, AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(target, attributes, amplifier);
        if (!(target.level() instanceof ServerLevel level)) return;
        // The overlay needs the effect instance on clients tracking another entity.
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
        var effect = target.getEffect(WitcheryEffects.DISEASED.get());
        if (effect != null) viewer.connection.send(new ClientboundUpdateMobEffectPacket(target.getId(), effect));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 4;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (!(target.level() instanceof ServerLevel level) || level.random.nextInt(3) != 0) return;
        var pos = target.blockPosition();
        var below = pos.below();
        if (level.isEmptyBlock(pos) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            BrewTerrain.setBlock(target, level, pos, WitcheryBlocks.getRequired("disease").get().defaultBlockState(), Direction.UP);
        }
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
