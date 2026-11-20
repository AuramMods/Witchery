package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class ClientWolfForm {
    private static final Map<Player, Wolf> MODELS = new WeakHashMap<>();

    private ClientWolfForm() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void renderWolf(RenderPlayerEvent.Pre event) {
        var player = event.getEntity();
        if (!Lycanthropy.isWolf(player)) return;
        Wolf wolf = MODELS.computeIfAbsent(player, key -> EntityType.WOLF.create(key.level()));
        if (wolf == null) return;
        wolf.setPos(player.position());
        wolf.setInvisible(player.isInvisible());
        wolf.setInSittingPose(player.isPassenger());
        wolf.yBodyRotO = player.yBodyRotO;
        wolf.yBodyRot = player.yBodyRot;
        wolf.yHeadRotO = player.yHeadRotO;
        wolf.yHeadRot = player.yHeadRot;
        wolf.xRotO = player.xRotO;
        wolf.setXRot(player.getXRot());
        wolf.hurtTime = player.hurtTime;
        wolf.deathTime = player.deathTime;
        if (wolf.tickCount != player.tickCount) {
            wolf.walkAnimation.update(player.walkAnimation.position() - wolf.walkAnimation.position(), 1);
            wolf.walkAnimation.setSpeed(player.walkAnimation.speed());
            wolf.tickCount = player.tickCount;
        }
        event.setCanceled(true);
        Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(wolf).render(wolf, player.getYRot(),
                event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    @SubscribeEvent
    public static void hideEmptyHand(RenderHandEvent event) {
        var player = Minecraft.getInstance().player;
        if (player != null && event.getItemStack().isEmpty() && Lycanthropy.isWolf(player)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        MODELS.clear();
    }
}
