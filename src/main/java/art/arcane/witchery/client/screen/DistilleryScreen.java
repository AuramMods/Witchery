package art.arcane.witchery.client.screen;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.menu.DistilleryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static art.arcane.witchery.block.entity.DistilleryBlockEntity.COOK_TICKS;

public final class DistilleryScreen extends AbstractContainerScreen<DistilleryMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/distiller.png");
    private static final int[] BUBBLE_HEIGHTS = {29, 24, 20, 16, 11, 6, 0};

    public DistilleryScreen(DistilleryMenu menu, Inventory inventory, Component title) {
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
        if (!menu.powered()) {
            graphics.blit(TEXTURE, leftPos + 35, topPos + 58, 197, 0, 9, 9);
        }
        int width = menu.progressWidth(38);
        if (width > 0) {
            graphics.blit(TEXTURE, leftPos + 68, topPos + 14, 176, 29, width, 35);
            int height = BUBBLE_HEIGHTS[(COOK_TICKS - menu.cookTime()) / 2 % BUBBLE_HEIGHTS.length];
            graphics.blit(TEXTURE, leftPos + 33, topPos + 49 - height, 185, 29 - height, 12, height);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 0x404040, false);
    }
}
