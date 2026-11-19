package art.arcane.witchery.client.screen;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.menu.AltarMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class AltarScreen extends AbstractContainerScreen<AltarMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/altar.png");

    public AltarScreen(AltarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 88;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, 24, 0xffffff);
        graphics.drawCenteredString(font, Component.translatable("container.witchery.altar.power",
                menu.power(), menu.capacity(), menu.rechargeMultiplier()), imageWidth / 2, 44, 0xffffff);
    }
}
