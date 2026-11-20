package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.config.WitcheryClientConfig;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.vampirism.BloodFeeding;
import art.arcane.witchery.vampirism.VampireAbilities;
import art.arcane.witchery.vampirism.Vampirism;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class ClientVampirism {
    private static final ResourceLocation DROPLET = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/bdrop.png");
    private static final ResourceLocation FADED = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/bdropfaded.png");
    private static final ResourceLocation WIDGETS = ResourceLocation.withDefaultNamespace("textures/gui/widgets.png");
    private static final ResourceLocation FANGS = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/item/vteeth.png");
    private static final ResourceLocation EYE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/item/veye.png");
    private static int aimingAt = -1;
    private static int trackedBlood = -1;
    private static int nextReadTick;

    private ClientVampirism() {}

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || player.isSpectator() || client.screen != null || event.getScrollDelta() == 0) return;
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            int max = VampireAbilities.maxPower(data.getVampireLevel());
            if (max == 0 || BloodFeeding.paralysis(player, 4)) return;
            int slot = player.getInventory().selected;
            if (slot == 0) {
                int selected = data.getSelectedVampirePower();
                if (event.getScrollDelta() > 0 && selected == max) {
                    choose(player, 0);
                } else if (selected > 0 || event.getScrollDelta() > 0) {
                    choose(player, selected + (event.getScrollDelta() > 0 ? 1 : -1));
                    event.setCanceled(true);
                }
            } else if (slot == 8 && event.getScrollDelta() < 0) {
                choose(player, max);
            }
        });
    }

    private static void choose(LocalPlayer player, int power) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> data.setSelectedVampirePower(power));
        WitcheryNetwork.requestVampirePower(power, false);
    }

    @SubscribeEvent
    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || player.isSpectator()) return;
        if (BloodFeeding.paralysis(player, 4)) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        int selected = VampireAbilities.selected(player);
        if (!event.isUseItem() || selected == 0 || client.hitResult != null && client.hitResult.getType() == HitResult.Type.ENTITY) return;
        if (event.getHand() == InteractionHand.MAIN_HAND && selected == VampireAbilities.MESMERIZE && player.isShiftKeyDown()) {
            WitcheryNetwork.requestVampirePower(selected, true);
        }
        event.setCanceled(true);
        event.setSwingHand(false);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.screen != null || player.isSpectator() || Vampirism.level(player) == 0) return;
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (player.getInventory().selected != 0 && data.getSelectedVampirePower() != 0) choose(player, 0);
        });
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().scale(5);
        Vec3 end = player.level().clip(new ClipContext(eye, eye.add(look), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player)).getLocation();
        var target = ProjectileUtil.getEntityHitResult(player, eye, end, player.getBoundingBox().expandTowards(look).inflate(1),
                entity -> entity.isPickable() && (entity instanceof Villager || entity instanceof Player), eye.distanceToSqr(end));
        int id = target == null ? -1 : target.getEntity().getId();
        if (id != aimingAt) {
            aimingAt = id;
            trackedBlood = -1;
            nextReadTick = 0;
        }
        if (aimingAt >= 0 && nextReadTick <= player.tickCount) {
            nextReadTick = player.tickCount + 10;
            WitcheryNetwork.requestBloodTarget(aimingAt);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        aimingAt = -1;
        trackedBlood = -1;
        nextReadTick = 0;
    }

    public static void receiveBlood(int entityId, int amount) {
        if (entityId == aimingAt) trackedBlood = amount;
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.player.isSpectator() || client.screen != null || client.options.hideGui) return;
        WitcheryPlayerDataProvider.get(client.player).ifPresent(data -> {
            if (data.getVampireLevel() == 0) return;
            int x = WitcheryClientConfig.GUI_ON_LEFT.get() ? 10 : width - 10;
            int y = height / 2 + 16;
            RenderSystem.enableBlend();
            for (int i = 0; i < data.getMaxBloodPower() / 250; i++) {
                int row = y - i * 8;
                drawDrop(graphics, x, row, data.getBloodPower() - i * 250, 250);
            }
            for (int power = 1; power <= VampireAbilities.maxPower(data.getVampireLevel()); power++) {
                int left = width / 2 - 91 - power * 20;
                int top = height - 22;
                graphics.blit(WIDGETS, left, top, 0, 0, 21, 22, 256, 256);
                if (power == data.getSelectedVampirePower()) graphics.blit(WIDGETS, left - 1, top - 1, 0, 22, 24, 22, 256, 256);
                graphics.blit(power == VampireAbilities.DRINK ? FANGS : EYE, left + 3, top + 3, 0, 0, 16, 16, 16, 16);
            }
            if (trackedBlood >= 0) {
                int portion = BloodFeeding.MAX_MORTAL_BLOOD / 4;
                for (int i = 0; i < 4; i++) drawDrop(graphics, width / 2 - 16 + i * 8, height / 2 + 10,
                        trackedBlood - i * portion, portion);
                if (WitcheryClientConfig.SHOW_VAMPIRE_TARGET_BLOOD_TEXT.get()) {
                    graphics.drawCenteredString(client.font, trackedBlood + "/" + BloodFeeding.MAX_MORTAL_BLOOD,
                            width / 2, height / 2 + 20, 0xFFFFFFFF);
                }
            }
            RenderSystem.disableBlend();
        });
    }

    private static void drawDrop(GuiGraphics graphics, int x, int y, int amount, int capacity) {
        int filled = Math.round(8 * Math.min(capacity, Math.max(0, amount)) / (float) capacity);
        graphics.blit(FADED, x, y, 0, 0, 8, 8, 8, 8);
        if (filled > 0) graphics.blit(DROPLET, x, y + 8 - filled, 0, 8 - filled, 8, filled, 8, 8);
    }
}
