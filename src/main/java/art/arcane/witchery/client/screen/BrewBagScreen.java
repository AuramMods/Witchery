package art.arcane.witchery.client.screen;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.menu.BrewBagMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class BrewBagScreen extends AbstractContainerScreen<BrewBagMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/generic_48.png");

    public BrewBagScreen(BrewBagMenu menu, Inventory player, Component title) {
        super(menu, player, title);
        imageHeight = 132;
        inventoryLabelY = 38;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, 35);
        graphics.blit(TEXTURE, leftPos, topPos + 35, 0, 126, imageWidth, 96);
    }
}
