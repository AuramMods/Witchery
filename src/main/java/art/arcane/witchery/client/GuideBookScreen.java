package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.BiomeBookIndex;
import art.arcane.witchery.item.CircleBookIndex;
import art.arcane.witchery.item.GuideBookItem;
import art.arcane.witchery.item.GuideBookPages;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;

public final class GuideBookScreen extends Screen {
    private static final ResourceLocation PAPER = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/booksingle.png");
    private static final int INK = 0x352418;
    private static final int BODY_WIDTH = 112;
    private static final int BODY_HEIGHT = 94;
    private static final int CONTENTS_ROWS = 8;

    private final int slot;
    private final GuideBookItem.Volume volume;
    private final List<GuideBookPages.Page> pages;
    private final BiomeBookIndex.Index atlas;
    private int page;
    private int scroll;
    private int maxScroll;
    private int contentsStart;
    private boolean contents;
    private boolean families;
    private PageButton previous;
    private PageButton next;
    private Button contentsButton;
    private Button familiesButton;

    private GuideBookScreen(int slot, GuideBookItem.Volume volume, ItemStack stack, Level level, int knownSpells) {
        super(stack.getHoverName());
        this.slot = slot;
        this.volume = volume;
        atlas = volume == GuideBookItem.Volume.ATLAS ? BiomeBookIndex.index(level.registryAccess()) : null;
        pages = atlas != null && !atlas.pages().isEmpty()
                ? atlas.pages().stream().map(GuideBookPages::atlas).toList()
                : GuideBookPages.entries(volume, level, knownSpells);
        if (atlas != null) page = BiomeBookIndex.bookmark(stack, atlas.pages());
        else if (volume == GuideBookItem.Volume.CIRCLES) page = CircleBookIndex.bookmark(stack, level, pages);
        else page = Mth.clamp(GuideBookItem.bookmark(stack), 0, pages.size() - 1);
    }

    public static void open(int slot, GuideBookItem.Volume volume, int knownSpells) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || slot < 0 || slot >= client.player.getInventory().getContainerSize()) return;
        var stack = client.player.getInventory().getItem(slot);
        if (GuideBookItem.volume(stack) == volume) client.setScreen(new GuideBookScreen(slot, volume, stack, client.level, knownSpells));
    }

    @Override
    protected void init() {
        previous = addRenderableWidget(new PageButton(left() + 38, top() + 154, false, button -> turn(page - 1), true));
        next = addRenderableWidget(new PageButton(left() + 124, top() + 154, true, button -> turn(page + 1), true));
        contentsButton = addRenderableWidget(Button.builder(Component.translatable("guide.witchery.contents"), button -> toggleContents())
                .bounds(left() + 64, top() + 153, 57, 16).build());
        if (atlas != null && !atlas.sections().isEmpty()) {
            familiesButton = addRenderableWidget(Button.builder(Component.translatable("guide.witchery.atlas.families"),
                            button -> toggleFamilies()).bounds(left() + 95, top() + 153, 56, 16).build());
        }
        updateButtons();
    }

    private int left() { return (width - 192) / 2; }
    private int top() { return Math.max(2, (height - 192) / 2); }

    private void updateButtons() {
        previous.visible = !contents && page > 0;
        next.visible = !contents && page + 1 < pages.size();
        contentsButton.setMessage(Component.translatable(contents ? "guide.witchery.return" : "guide.witchery.contents"));
        if (familiesButton != null) {
            familiesButton.visible = contents;
            contentsButton.setX(left() + (contents ? 36 : 64));
            familiesButton.setMessage(Component.translatable(families ? "guide.witchery.atlas.biomes" : "guide.witchery.atlas.families"));
        }
    }

    private void turn(int index) {
        page = Mth.clamp(index, 0, pages.size() - 1);
        contents = false;
        scroll = 0;
        maxScroll = 0;
        updateButtons();
    }

    private void toggleContents() {
        contents = !contents;
        positionContents();
        updateButtons();
    }

    private void toggleFamilies() {
        families = !families;
        positionContents();
        updateButtons();
    }

    private void positionContents() {
        contentsStart = Mth.clamp(currentContentsEntry(), 0, Math.max(0, contentsSize() - CONTENTS_ROWS));
    }

    private int contentsSize() {
        return families ? atlas.sections().size() : pages.size();
    }

    private int currentContentsEntry() {
        if (!families) return page;
        var sections = atlas.sections();
        for (int i = sections.size() - 1; i > 0; i--) {
            if (sections.get(i).firstPage() <= page) return i;
        }
        return 0;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (key == GLFW_KEY_HOME) {
            toggleContents();
            return true;
        }
        if (!contents && (key == GLFW_KEY_LEFT || key == GLFW_KEY_PAGE_UP) && previous.visible) {
            previous.onPress();
            previous.playDownSound(minecraft.getSoundManager());
            return true;
        }
        if (!contents && (key == GLFW_KEY_RIGHT || key == GLFW_KEY_PAGE_DOWN) && next.visible) {
            next.onPress();
            next.playDownSound(minecraft.getSoundManager());
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        int direction = (int) Math.signum(amount);
        if (contents) contentsStart = Mth.clamp(contentsStart - direction * 3, 0, Math.max(0, contentsSize() - CONTENTS_ROWS));
        else scroll = Mth.clamp(scroll - direction * 18, 0, maxScroll);
        return true;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int row = (int) (y - (top() + 42)) / 13;
        if (contents && button == 0 && x >= left() + 35 && x <= left() + 150
                && y >= top() + 42 && row < CONTENTS_ROWS && contentsStart + row < contentsSize()) {
            int index = contentsStart + row;
            turn(families ? atlas.sections().get(index).firstPage() : index);
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = left();
        int top = top();
        graphics.blit(PAPER, left, top, 0, 0, 192, 192);
        if (contents) renderContents(graphics, left, top, mouseX, mouseY);
        else renderPage(graphics, left, top);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderContents(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        var title = Component.translatable(families ? "guide.witchery.atlas.families" : "guide.witchery.contents");
        graphics.drawString(font, title, left + 92 - font.width(title) / 2, top + 25, 0x713426, false);
        Component tooltip = null;
        int selected = currentContentsEntry();
        for (int row = 0; row < CONTENTS_ROWS && contentsStart + row < contentsSize(); row++) {
            int index = contentsStart + row;
            int y = top + 42 + row * 13;
            boolean hover = mouseX >= left + 35 && mouseX <= left + 150 && mouseY >= y && mouseY < y + 13;
            int color = hover || index == selected ? 0x8C2618 : INK;
            Component entry = families ? Component.translatable("guide.witchery.atlas.family." + atlas.sections().get(index).name())
                    : pages.get(index).title();
            String name = families ? entry.getString() : (index + 1) + "  " + entry.getString();
            if (font.width(name) > BODY_WIDTH) {
                name = font.plainSubstrByWidth(name, BODY_WIDTH - font.width("…")) + "…";
                if (hover) tooltip = entry;
            }
            graphics.drawString(font, name, left + 36, y, color, false);
        }
        if (tooltip != null) graphics.renderTooltip(font, tooltip, mouseX, mouseY);
    }

    private void renderPage(GuiGraphics graphics, int left, int top) {
        var entry = pages.get(page);
        var indicator = Component.translatable("book.pageIndicator", page + 1, pages.size());
        graphics.drawString(font, indicator, left + 148 - font.width(indicator), top + 13, 0x6C5643, false);
        int y = top + 28;
        for (var line : font.split(entry.title(), BODY_WIDTH)) {
            graphics.drawString(font, line, left + 93 - font.width(line) / 2, y, 0x713426, false);
            y += 10;
        }

        int bodyTop = top + 50;
        int bottom = bodyTop + BODY_HEIGHT;
        graphics.enableScissor(left + 34, bodyTop, left + 151, bottom);
        y = bodyTop - scroll;
        if (entry.picture() != null) {
            graphics.blit(entry.picture(), left + 79, y, 28, 28, 0, 0, 16, 16, 16, 16);
            y += 32;
        }
        if (!entry.icons().isEmpty()) {
            int x = left + 92 - entry.icons().size() * 9;
            for (var stack : entry.icons()) {
                graphics.renderItem(stack, x, y);
                x += 18;
            }
            y += 19;
        }
        if (!entry.circles().isEmpty()) {
            renderCircles(graphics, entry, left + 92, y + 15);
            y += 34;
        }
        for (var paragraph : entry.text()) {
            for (var line : font.split(paragraph, BODY_WIDTH)) {
                graphics.drawString(font, line, left + 36, y, INK, false);
                y += 9;
            }
            y += 3;
        }
        graphics.disableScissor();
        maxScroll = Math.max(0, y + scroll - bottom);
        if (maxScroll > 0) {
            int marker = bodyTop + (BODY_HEIGHT - 10) * scroll / maxScroll;
            graphics.fill(left + 150, bodyTop, left + 151, bottom, 0x55995E35);
            graphics.fill(left + 149, marker, left + 151, marker + 10, 0xFF995E35);
        }
    }

    private static void renderCircles(GuiGraphics graphics, GuideBookPages.Page entry, int x, int y) {
        for (var ring : entry.circles()) {
            var path = BuiltInRegistries.BLOCK.getKey(ring.glyph()).getPath();
            int ink = path.contains("infernal") || path.endsWith("red") ? 0xFF972E1A
                    : path.contains("otherwhere") || path.endsWith("blue") ? 0xFF584682 : 0xFF766057;
            for (var offset : ring.pattern().offsets()) {
                int cellX = x + offset.getX() * 2;
                int cellY = y + offset.getZ() * 2;
                graphics.fill(cellX, cellY, cellX + 2, cellY + 2, ink);
            }
        }
        graphics.fill(x, y, x + 2, y + 2, 0xFFB4853D);
    }

    @Override
    public void removed() {
        if (minecraft.getConnection() == null || atlas != null && atlas.pages().isEmpty()) return;
        WitcheryNetwork.sendGuideBookmark(slot, volume, page, atlas == null ? pages.get(page).entryId() : atlas.pages().get(page).id());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
