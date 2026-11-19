package art.arcane.witchery.client.screen;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.menu.SpinningWheelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class SpinningWheelScreen extends AbstractContainerScreen<SpinningWheelMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/spinningwheel.png");

    public SpinningWheelScreen(SpinningWheelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
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
        if (!menu.powered()) {
            graphics.blit(TEXTURE, leftPos + 35, topPos + 58, 197, 0, 9, 9);
        }
        graphics.blit(TEXTURE, leftPos + 79, topPos + 20, 176, 14, menu.progressWidth(24) + 1, 16);
    }
}
