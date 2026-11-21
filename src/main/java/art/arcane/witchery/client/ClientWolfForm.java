package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.ResizingEffect;
import art.arcane.witchery.entity.Wolfman;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.vampirism.BloodFeeding;
import art.arcane.witchery.vampirism.VampireAbilities;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class ClientWolfForm {
    private static final Map<Player, Mob> MODELS = new WeakHashMap<>();

    private ClientWolfForm() {}

    @SubscribeEvent
    public static void howl(InputEvent.InteractionKeyMappingTriggered event) {
        var player = Minecraft.getInstance().player;
        if (!event.isUseItem() || event.getHand() != InteractionHand.MAIN_HAND || player == null || player.isSpectator()
                || !player.isShiftKeyDown() || player.getXRot() != -90 || VampireAbilities.selected(player) != 0
                || BloodFeeding.paralysis(player, 4)) return;
        int rank = Lycanthropy.rank(player);
        if (rank == 6 && Lycanthropy.isShifted(player) || rank >= 8 && Lycanthropy.isWolf(player)
                || rank >= 7 && Lycanthropy.isWolfman(player)) {
            WitcheryNetwork.sendHowlToServer();
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void renderWolf(RenderPlayerEvent.Pre event) {
        var player = event.getEntity();
        EntityType<? extends Mob> type = Lycanthropy.isWolf(player) ? EntityType.WOLF
                : Lycanthropy.isWolfman(player) ? WitcheryEntities.WOLFMAN.get() : null;
        if (type == null) return;
        Mob proxy = MODELS.compute(player, (key, existing) -> existing != null && existing.getType() == type
                ? existing : type.create(key.level()));
        if (proxy == null) return;
        ResizingEffect.setScale(proxy, ResizingEffect.scale(player));
        proxy.setPos(player.position());
        proxy.setInvisible(player.isInvisible());
        if (proxy instanceof Wolf wolf) wolf.setInSittingPose(player.isPassenger());
        if (proxy instanceof Wolfman wolfman) {
            wolfman.setItemSlot(EquipmentSlot.MAINHAND, player.getMainHandItem());
            wolfman.setItemSlot(EquipmentSlot.OFFHAND, player.getOffhandItem());
            wolfman.setLeftHanded(player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT);
            wolfman.setPlayerAnimation(player);
        }
        proxy.yBodyRotO = player.yBodyRotO;
        proxy.yBodyRot = player.yBodyRot;
        proxy.yHeadRotO = player.yHeadRotO;
        proxy.yHeadRot = player.yHeadRot;
        proxy.xRotO = player.xRotO;
        proxy.setXRot(player.getXRot());
        proxy.hurtTime = player.hurtTime;
        proxy.deathTime = player.deathTime;
        if (proxy.tickCount != player.tickCount) {
            proxy.walkAnimation.update(player.walkAnimation.position() - proxy.walkAnimation.position(), 1);
            proxy.walkAnimation.setSpeed(player.walkAnimation.speed());
            proxy.tickCount = player.tickCount;
        }
        event.setCanceled(true);
        Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(proxy).render(proxy, player.getYRot(),
                event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    @SubscribeEvent
    public static void hideEmptyHand(RenderHandEvent event) {
        var player = Minecraft.getInstance().player;
        if (player != null && event.getItemStack().isEmpty() && Lycanthropy.isShifted(player)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        MODELS.clear();
    }
}
