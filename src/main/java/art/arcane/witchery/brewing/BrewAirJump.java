package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BrewAirJump {
    private static final Map<ServerPlayer, Integer> USED_JUMPS = new WeakHashMap<>();

    private BrewAirJump() {}

    public static boolean canJump(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isPassenger() && !player.getAbilities().flying
                && !player.isFallFlying() && !player.isInWater() && !player.isInLava() && !player.onClimbable()
                && !player.hasEffect(WitcheryEffects.PARALYSIS.get()) && player.hasEffect(WitcheryEffects.DOUBLE_JUMP.get());
    }

    @SubscribeEvent
    public static void onJump(LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && canJump(player) && player.onGround()
                && !player.level().noCollision(player, player.getBoundingBox().move(0, -0.0625, 0))) {
            USED_JUMPS.put(player, 0);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player
                && (player.onGround() || !canJump(player))) USED_JUMPS.remove(player);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) USED_JUMPS.remove(player);
    }

    public static boolean tryJump(ServerPlayer player) {
        Integer used = USED_JUMPS.get(player);
        if (used == null || player.onGround() || !canJump(player)
                || used > player.getEffect(WitcheryEffects.DOUBLE_JUMP.get()).getAmplifier()) return false;
        USED_JUMPS.put(player, used + 1);
        applyJump(player);
        return true;
    }

    public static void applyJump(Player player) {
        Vec3 movement = player.getDeltaMovement();
        player.setDeltaMovement(movement.x, 0.42 + player.getJumpBoostPower(), movement.z);
    }
}
