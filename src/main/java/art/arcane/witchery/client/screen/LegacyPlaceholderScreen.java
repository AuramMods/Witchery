package art.arcane.witchery.client.screen;

import art.arcane.witchery.menu.LegacyPlaceholderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class LegacyPlaceholderScreen extends AbstractContainerScreen<LegacyPlaceholderMenu> {
    public LegacyPlaceholderScreen(LegacyPlaceholderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        guiGraphics.fill(left, top, left + imageWidth, top + imageHeight, 0xC0101010);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFF, false);
        guiGraphics.drawString(font, "Legacy menu scaffold:", 8, 26, 0xC0C0C0, false);
        guiGraphics.drawString(font, menu.legacyMenuKey(), 8, 38, 0xFFFFFF, false);
    }
}
