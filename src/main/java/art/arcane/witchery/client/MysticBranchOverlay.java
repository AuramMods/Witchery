package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryClientConfig;
import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.item.MysticBranchItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public final class MysticBranchOverlay {
    private static final ResourceLocation GRID = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/grid.png");
    private static final String DIRECTIONS = "↑↓→←";
    private static final int[] X = {0, 0, 1, -1};
    private static final int[] Y = {-1, 1, 0, 0};
    private static final int SIZE = 16;

    private MysticBranchOverlay() {}

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        var client = Minecraft.getInstance();
        var player = client.player;
        if (player == null || client.options.hideGui || client.screen != null
                || !(player.getUseItem().getItem() instanceof MysticBranchItem)) return;
        String strokes = MysticBranchItem.strokes(player);
        if (strokes.isEmpty()) return;

        int x = width / 2 - SIZE / 2;
        int y = height / 2 - SIZE / 2;
        int column = WitcheryClientConfig.BRANCH_GLYPH_SET.get() == 1 ? 4 : 0;
        int frame = (int) (Util.getMillis() / 125 % 16);
        int row = Math.min(frame, 15 - frame);
        RenderSystem.enableBlend();
        for (int i = 0; i < strokes.length(); i++) {
            int direction = DIRECTIONS.indexOf(strokes.charAt(i));
            if (direction < 0) continue;
            x += X[direction] * SIZE;
            y += Y[direction] * SIZE;
            graphics.blit(GRID, x, y, (column + direction) * SIZE, row * SIZE, SIZE, SIZE);
        }
        RenderSystem.disableBlend();

        var spell = SymbolSpell.match(strokes);
        if (spell != null) {
            graphics.drawCenteredString(client.font, spell.displayName(), width / 2, height / 2 + 20, 0xFFFFFF);
        }
    }
}
