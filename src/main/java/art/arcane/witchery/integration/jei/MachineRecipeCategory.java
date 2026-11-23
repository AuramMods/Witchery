package art.arcane.witchery.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class MachineRecipeCategory implements IRecipeCategory<MachineRecipeCategory.Entry> {
    private static final int WIDTH = 172;
    private static final int INPUT_COLUMNS = 6;
    private static final int SLOT_STEP = 18;
    private final RecipeType<Entry> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable arrow;
    private final int height;

    MachineRecipeCategory(RecipeType<Entry> type, ItemStack machine, int inputRows, IGuiHelper gui) {
        this.type = type;
        title = machine.getHoverName();
        icon = gui.createDrawableItemStack(machine);
        arrow = gui.getRecipeArrow();
        height = inputRows * SLOT_STEP + 58;
    }

    @Override
    public RecipeType<Entry> getRecipeType() { return type; }

    @Override
    public Component getTitle() { return title; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public int getWidth() { return WIDTH; }

    @Override
    public int getHeight() { return height; }

    @Override
    public ResourceLocation getRegistryName(Entry entry) { return entry.id(); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Entry entry, IFocusGroup focuses) {
        int index = 0;
        for (Input input : entry.inputs()) {
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, 2 + index % INPUT_COLUMNS * SLOT_STEP,
                    2 + index / INPUT_COLUMNS * SLOT_STEP).setStandardSlotBackground().addItemStacks(input.stacks());
            if (input.tooltip() != null) slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(input.tooltip()));
            index++;
        }
        int output = 0;
        for (ItemStack stack : entry.outputs()) {
            if (stack.isEmpty()) continue;
            builder.addSlot(RecipeIngredientRole.OUTPUT, 134 + output % 2 * SLOT_STEP,
                    2 + output / 2 * SLOT_STEP).setOutputSlotBackground().addItemStack(stack);
            output++;
        }
    }

    @Override
    public void draw(Entry entry, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        if (entry.outputs().stream().anyMatch(stack -> !stack.isEmpty())) arrow.draw(graphics, 110, 2);
        var font = Minecraft.getInstance().font;
        int rows = (entry.inputs().size() + INPUT_COLUMNS - 1) / INPUT_COLUMNS;
        int outputRows = (int) ((1 + entry.outputs().stream().filter(stack -> !stack.isEmpty()).count()) / 2);
        int y = 4 + Math.max(rows, outputRows) * SLOT_STEP;
        for (Component note : entry.details()) {
            for (var line : font.split(note, WIDTH - 4)) {
                graphics.drawString(font, line, 2, y, 0x404040, false);
                y += 10;
            }
        }
    }

    record Input(List<ItemStack> stacks, @Nullable Component tooltip) {}

    record Entry(ResourceLocation id, List<Input> inputs, List<ItemStack> outputs, List<Component> details) {}
}
