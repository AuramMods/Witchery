package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.VampireBookItem;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
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
    private int scroll;
    private int maxScroll;
    private PageButton previous;
    private PageButton next;
    private Button cover;

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
        previous = addRenderableWidget(new PageButton(left + 39, top + 151, false, button -> turn(page - 1), true));
        next = addRenderableWidget(new PageButton(left + 126, top + 151, true, button -> turn(page + 1), true));
        cover = addRenderableWidget(Button.builder(Component.translatable(TEXT + "cover"), button -> turn(0))
                .bounds(left + 65, top + 150, 57, 16).build());
        turn(page);
    }

    private int top() {
        return Math.max(2, (height - 192) / 2);
    }

    private void turn(int index) {
        page = Math.max(0, Math.min(index, pageCount - 1));
        scroll = 0;
        maxScroll = 0;
        previous.visible = page > 0;
        next.visible = page < pageCount - 1;
        cover.visible = page > 0;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        scroll = Mth.clamp(scroll - (int) Math.signum(amount) * 18, 0, maxScroll);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (key == GLFW_KEY_HOME && cover.visible) {
            cover.onPress();
            cover.playDownSound(minecraft.getSoundManager());
            return true;
        }
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
        int bodyTop = top + 34;
        int bodyBottom = top + 146;
        graphics.enableScissor(left + 36, bodyTop, left + 153, bodyBottom);
        int y = bodyTop - scroll;
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
        graphics.disableScissor();
        maxScroll = Math.max(0, y + scroll - bodyBottom);
        if (maxScroll > 0) {
            int marker = bodyTop + (bodyBottom - bodyTop - 10) * scroll / maxScroll;
            graphics.fill(left + 151, bodyTop, left + 152, bodyBottom, 0x55995E35);
            graphics.fill(left + 151, marker, left + 153, marker + 10, 0xFF995E35);
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
