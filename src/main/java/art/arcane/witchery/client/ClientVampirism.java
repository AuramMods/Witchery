package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.config.WitcheryClientConfig;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.vampirism.BloodFeeding;
import art.arcane.witchery.vampirism.VampireAbilities;
import art.arcane.witchery.vampirism.VampireUltimate;
import art.arcane.witchery.vampirism.Vampirism;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class ClientVampirism {
    private static final ResourceLocation DROPLET = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/bdrop.png");
    private static final ResourceLocation FADED = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/bdropfaded.png");
    private static final ResourceLocation WIDGETS = ResourceLocation.withDefaultNamespace("textures/gui/widgets.png");
    private static final ResourceLocation FANGS = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/item/vteeth.png");
    private static final ResourceLocation EYE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/item/veye.png");
    private static final ResourceLocation SPEED = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/item/vspeed.png");
    private static final ResourceLocation BAT = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/item/vbat.png");
    private static final ResourceLocation STORM = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/item/vstorm.png");
    private static final ResourceLocation SWARM = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/item/vfist.png");
    private static final ResourceLocation FARM = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/item/vcoffin.png");
    private static int aimingAt = -1;
    private static int trackedBlood = -1;
    private static int nextReadTick;
    private static int highlightedPower;
    private static int powerNameFrames;

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
    public static void onHotbarKey(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS) handleHotbarPress(InputConstants.getKey(event.getKey(), event.getScanCode()));
    }

    @SubscribeEvent
    public static void onHotbarMouseButton(InputEvent.MouseButton.Post event) {
        if (event.getAction() == GLFW.GLFW_PRESS) handleHotbarPress(InputConstants.Type.MOUSE.getOrCreate(event.getButton()));
    }

    private static void handleHotbarPress(InputConstants.Key key) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || player.isSpectator() || client.screen != null || client.getOverlay() != null
                || BloodFeeding.paralysis(player, 4)) return;
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            int max = VampireAbilities.maxPower(data.getVampireLevel());
            if (max == 0) return;
            for (int slot = 0; slot < client.options.keyHotbarSlots.length; slot++) {
                if (!client.options.keyHotbarSlots[slot].isActiveAndMatches(key)) continue;
                int selected = data.getSelectedVampirePower();
                if (slot == 0 && player.getInventory().selected == 0) choose(player, selected >= max ? 0 : selected + 1);
                else if (slot != 0 && selected != 0) choose(player, 0);
                return;
            }
        });
    }

    @SubscribeEvent
    public static void hideHeldItemName(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.ITEM_NAME.type()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && !player.isSpectator() && VampireAbilities.selected(player) != 0) event.setCanceled(true);
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
        if (event.getHand() == InteractionHand.MAIN_HAND && selected >= VampireAbilities.MESMERIZE) {
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
        if (player == null || player.isSpectator() || Vampirism.level(player) == 0) {
            clearBloodSight();
            return;
        }
        if (client.screen != null) return;
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (player.getInventory().selected != 0 && data.getSelectedVampirePower() != 0) choose(player, 0);
        });
        var target = BloodFeeding.findSightTarget(player);
        int id = target == null ? -1 : target.getId();
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
    public static void onPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        clearBloodSight();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clearBloodSight();
        highlightedPower = 0;
        powerNameFrames = 0;
    }

    private static void clearBloodSight() {
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
            int activePower = VampireAbilities.selected(client.player);
            int x = WitcheryClientConfig.GUI_ON_LEFT.get() ? 10 : width - 10;
            int y = height / 2 + 16;
            RenderSystem.enableBlend();
            for (int i = 0; i < data.getMaxBloodPower() / 250; i++) {
                int row = y - i * 8;
                drawDrop(graphics, x, row, data.getBloodPower() - i * 250, 250);
            }
            int hotbarLeft = width / 2 - 91;
            boolean rightHanded = client.player.getMainArm() == HumanoidArm.RIGHT;
            // The selected power's outline extends three pixels past the slot.
            if (rightHanded && !client.player.getOffhandItem().isEmpty()) hotbarLeft -= 29 + 3;
            if (!rightHanded && client.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) hotbarLeft -= 22 + 3;
            int powersPerRow = Math.max(1, (hotbarLeft - 1) / 20);
            for (int power = 1; power <= VampireAbilities.maxPower(data.getVampireLevel()); power++) {
                int index = power - 1;
                int left = hotbarLeft - (index % powersPerRow + 1) * 20;
                int top = height - 22 - index / powersPerRow * 23;
                graphics.blit(WIDGETS, left, top, 0, 0, 21, 22, 256, 256);
                if (power == activePower) graphics.blit(WIDGETS, left - 1, top - 1, 0, 22, 24, 22, 256, 256);
                ResourceLocation icon = switch (power) {
                    case VampireAbilities.DRINK -> FANGS;
                    case VampireAbilities.MESMERIZE -> EYE;
                    case VampireAbilities.SPEED -> SPEED;
                    case VampireAbilities.BAT -> BAT;
                    default -> switch (data.getVampireUltimate()) {
                        case STORM -> STORM;
                        case FARM -> FARM;
                        default -> SWARM;
                    };
                };
                boolean faded = power == VampireAbilities.ULTIMATE
                        && (data.getVampireUltimate() == VampireUltimate.NONE || data.getVampireUltimateCharges() == 0);
                if (faded) graphics.setColor(1, 1, 1, 0.20F);
                graphics.blit(icon, left + 3, top + 3, 0, 0, 16, 16, 16, 16);
                if (faded) graphics.setColor(1, 1, 1, 1);
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
            renderPowerName(client, graphics, width, height, activePower, data);
            RenderSystem.disableBlend();
        });
    }

    private static void renderPowerName(Minecraft client, GuiGraphics graphics, int width, int height,
                                        int power, WitcheryPlayerData data) {
        if (power != highlightedPower) {
            highlightedPower = power;
            powerNameFrames = power == 0 ? 0 : 100;
        }
        if (powerNameFrames == 0 || --powerNameFrames == 0) return;
        Component name = switch (power) {
            case VampireAbilities.DRINK -> Component.translatable("hud.witchery.vampire.drink");
            case VampireAbilities.MESMERIZE -> Component.translatable("hud.witchery.vampire.mesmerize");
            case VampireAbilities.SPEED -> Component.translatable("hud.witchery.vampire.speed");
            case VampireAbilities.BAT -> Component.translatable("hud.witchery.vampire.bat");
            case VampireAbilities.ULTIMATE -> Component.translatable(
                    "message.witchery.vampire.ultimate." + data.getVampireUltimate().name().toLowerCase(Locale.ROOT),
                    data.getVampireUltimateCharges());
            default -> Component.empty();
        };
        int alpha = Math.min(255, powerNameFrames * 256 / 10);
        int y = height - (client.player.getAbilities().instabuild ? 55 : 69);
        graphics.drawCenteredString(client.font, name, width / 2, y, (alpha << 24) | 0xFFFFFF);
    }

    private static void drawDrop(GuiGraphics graphics, int x, int y, int amount, int capacity) {
        int filled = Math.round(8 * Math.min(capacity, Math.max(0, amount)) / (float) capacity);
        graphics.blit(FADED, x, y, 0, 0, 8, 8, 8, 8);
        if (filled > 0) graphics.blit(DROPLET, x, y + 8 - filled, 0, 8 - filled, 8, filled, 8, 8);
    }
}
