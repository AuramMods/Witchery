package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.BrewAirJump;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class ClientBrewMovement {
    private static final double SWIM_BOOST = 1.24;
    private static final double BOOSTED_WATER_DRAG = 0.8 * SWIM_BOOST;
    private static boolean jumpWasDown;

    private ClientBrewMovement() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        var input = event.getInput();
        if (input.jumping && !jumpWasDown && !player.onGround() && BrewAirJump.canJump(player)) {
            WitcheryNetwork.requestBrewJump();
        }
        jumpWasDown = input.jumping;

        if (input.up && player.isInWater() && !player.isPassenger() && player.hasEffect(WitcheryEffects.SWIMMING.get())
                && !player.hasEffect(WitcheryEffects.PARALYSIS.get())) {
            // Sprint swimming and Dolphin's Grace retain more momentum than legacy water movement.
            double drag = player.hasEffect(MobEffects.DOLPHINS_GRACE) ? 0.96 : player.isSprinting() ? 0.9 : 0.8;
            double boost = BOOSTED_WATER_DRAG / drag;
            player.setDeltaMovement(player.getDeltaMovement().multiply(boost, 1, boost));
        }
    }

    public static void jump() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && !player.onGround() && BrewAirJump.canJump(player)) BrewAirJump.applyJump(player);
    }
}
