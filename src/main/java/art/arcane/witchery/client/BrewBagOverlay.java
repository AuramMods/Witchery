package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.BrewBagItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public final class BrewBagOverlay {
    private static final ResourceLocation WHEEL = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/radial.png");
    private static final int[] X = {0, 24, 32, 24, 0, -24, -32, -24};
    private static final int[] Y = {-32, -24, 0, 24, 32, 24, 0, -24};

    private BrewBagOverlay() {}

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        var client = Minecraft.getInstance();
        var player = client.player;
        if (player == null || client.options.hideGui || client.screen != null || player.isShiftKeyDown() || !player.isUsingItem()) return;
        ItemStack bag = player.getUseItem();
        if (!(bag.getItem() instanceof BrewBagItem)) return;

        int selected = BrewBagItem.preparedSlot(player);
        int centerX = width / 2;
        int centerY = height / 2;
        if (selected < 0) {
            int color = BrewBagItem.color(bag);
            RenderSystem.enableBlend();
            graphics.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1);
            graphics.blit(WHEEL, centerX - 42, centerY - 42, 85, 85, 0, 0, 256, 256, 256, 256);
            graphics.setColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
        }
        var contents = BrewBagItem.contents(bag);
        for (int slot = 0; slot < contents.size(); slot++) {
            if (contents.get(slot).isEmpty() || selected >= 0 && slot != selected) continue;
            var brew = contents.get(slot);
            int x = centerX + X[slot] - 8;
            int y = centerY + Y[slot] - 8;
            graphics.renderItem(brew, x, y);
            graphics.renderItemDecorations(client.font, brew, x, y);
            var name = brew.getHoverName();
            int labelWidth = client.font.width(name);
            int labelX = X[slot] < 0 ? x - labelWidth - 5 : X[slot] > 0 ? x + 21 : centerX - labelWidth / 2;
            int labelY = X[slot] != 0 ? y + 5 : Y[slot] < 0 ? y - 11 : y + 19;
            graphics.drawString(client.font, name, labelX, labelY, 0xCCFFFFFF);
        }
    }
}
