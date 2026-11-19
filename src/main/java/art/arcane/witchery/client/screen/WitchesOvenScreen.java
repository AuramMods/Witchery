package art.arcane.witchery.client.screen;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.menu.WitchesOvenMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class WitchesOvenScreen extends AbstractContainerScreen<WitchesOvenMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/witches_oven.png");

    public WitchesOvenScreen(WitchesOvenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
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

        int flame = menu.flameHeight(13);
        if (flame > 0) {
            int offset = 13 - flame;
            graphics.blit(TEXTURE, leftPos + 56, topPos + 36 + offset, 176, offset, 14, flame);
        }
        int cook = menu.arrowWidth(24);
        if (cook > 0) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 20, 176, 14, cook, 16);
        }
    }
}
