package art.arcane.witchery.client.screen;

import art.arcane.witchery.menu.LeonardsUrnMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class LeonardsUrnScreen extends AbstractContainerScreen<LeonardsUrnMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("witchery", "textures/gui/urn.png");

    public LeonardsUrnScreen(LeonardsUrnMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 184;
        inventoryLabelY = 90;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        for (int i = 0; i < menu.urnSlots(); i++) {
            var slot = menu.getSlot(i);
            graphics.blit(TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, imageWidth, 0, 18, 18);
        }
    }
}
