package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.config.WitcheryClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public final class ClientVampirism {
    private static final ResourceLocation DROPLET = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/bdrop.png");
    private static final ResourceLocation FADED = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/bdropfaded.png");

    private ClientVampirism() {}

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
                int filled = Math.round(8 * Math.min(250, Math.max(0, data.getBloodPower() - i * 250)) / 250.0F);
                graphics.blit(FADED, x, row, 0, 0, 8, 8, 8, 8);
                if (filled > 0) graphics.blit(DROPLET, x, row + 8 - filled, 0, 8 - filled, 8, filled, 8, 8);
            }
            RenderSystem.disableBlend();
        });
    }
}
