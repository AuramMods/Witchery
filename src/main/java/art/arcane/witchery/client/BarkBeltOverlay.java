package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.BarkBeltItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;

final class BarkBeltOverlay {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID,
            "textures/gui/creatures.png");

    private BarkBeltOverlay() {}

    static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        var client = Minecraft.getInstance();
        if (client.player == null || client.player.getAbilities().invulnerable || client.options.hideGui) return;
        int charges = BarkBeltItem.visibleCharges(client.player);
        if (charges == 0) return;
        int x = width / 2 - 91;
        int y = height - gui.leftHeight;
        for (int i = 0; i < charges; i++) {
            graphics.blit(TEXTURE, x + i * 8, y, 0, 248, 8, 8);
        }
        gui.leftHeight += 10;
    }
}
