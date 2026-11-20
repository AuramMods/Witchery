package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.VampireBookItem;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;

public final class VampireBookScreen extends Screen {
    private static final ResourceLocation PARCHMENT = texture("booksingle");
    private static final ResourceLocation RITUAL = texture("vritual");
    private static final ResourceLocation CAGE = texture("vcage");
    private static final String TEXT = "book.witchery.immortal.";

    private final int inventorySlot;
    private final int pageCount;
    private int page;
    private PageButton previous;
    private PageButton next;

    private VampireBookScreen(ItemStack stack, int inventorySlot) {
        super(stack.getHoverName());
        this.inventorySlot = inventorySlot;
        pageCount = VampireBookItem.chapterCount(stack);
        page = VampireBookItem.bookmark(stack);
    }

    public static void open(ItemStack stack, int inventorySlot) {
        Minecraft.getInstance().setScreen(new VampireBookScreen(stack, inventorySlot));
    }

    private static ResourceLocation texture(String filename) {
        return ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/" + filename + ".png");
    }

    @Override
    protected void init() {
        int left = (width - 192) / 2;
        int top = top();
        previous = addRenderableWidget(new PageButton(left + 39, top + 151, false, button -> turn(-1), true));
        next = addRenderableWidget(new PageButton(left + 126, top + 151, true, button -> turn(1), true));
        turn(0);
    }

    private int top() {
        return Math.max(2, (height - 192) / 2);
    }

    private void turn(int direction) {
        page = Math.max(0, Math.min(page + direction, pageCount - 1));
        previous.visible = page > 0;
        next.visible = page < pageCount - 1;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if ((key == GLFW_KEY_PAGE_UP || key == GLFW_KEY_LEFT) && previous.visible) {
            previous.onPress();
            previous.playDownSound(minecraft.getSoundManager());
            return true;
        }
        if ((key == GLFW_KEY_PAGE_DOWN || key == GLFW_KEY_RIGHT) && next.visible) {
            next.onPress();
            next.playDownSound(minecraft.getSoundManager());
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = (width - 192) / 2;
        int top = top();
        graphics.blit(PARCHMENT, left, top, 0, 0, 192, 192);
        var indicator = Component.translatable("book.pageIndicator", page + 1, pageCount);
        graphics.drawString(font, indicator, left + 149 - font.width(indicator), top + 15, 0x6C5643, false);
        String chapter = VampireBookItem.chapter(page);
        int y = top + 34;
        if (page == 0) {
            for (var line : font.split(getTitle(), 100)) {
                graphics.drawString(font, line, left + 94 - font.width(line) / 2, y, 0x5D150E, false);
                y += 11;
            }
            y += 15;
        }
        ResourceLocation illustration = switch (chapter) {
            case "ritual3" -> RITUAL;
            case "maker2" -> CAGE;
            default -> null;
        };
        if (illustration != null) {
            graphics.blit(illustration, left + 62, y, 64, 64, 0, 0, 256, 256, 256, 256);
            y += 69;
        }
        for (var line : font.split(Component.translatable(TEXT + chapter), 114)) {
            graphics.drawString(font, line, left + 38, y, 0x261912, false);
            y += 9;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void removed() {
        if (minecraft.getConnection() != null) WitcheryNetwork.sendMarkupBookBookmark(inventorySlot, VampireBookItem.chapter(page));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
