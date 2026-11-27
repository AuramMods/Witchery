package art.arcane.witchery.data;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.Consumer;

public final class LegacyLootSlots {
    private static final ThreadLocal<LegacyLootSlots> ACTIVE = new ThreadLocal<>();

    private final LootContext context;
    private final int size;
    private final IntArrayList slots = new IntArrayList();
    private int consumed;

    private LegacyLootSlots(LootContext context, int size) {
        this.context = context;
        this.size = size;
    }

    public static void fill(LootTable table, Container container, LootContext context, int size) {
        var previous = ACTIVE.get();
        var draw = new LegacyLootSlots(context, size);
        ACTIVE.set(draw);
        try {
            // Keep the normal loot output path so other mods can still change the stacks.
            table.getRandomItems(context, stack -> container.setItem(draw.next(), stack));
        } finally {
            if (previous == null) ACTIVE.remove();
            else ACTIVE.set(previous);
        }
    }

    public static Consumer<ItemStack> reserveSlots(LootContext context, Consumer<ItemStack> output) {
        var draw = ACTIVE.get();
        if (draw == null || draw.context != context) return output;
        ACTIVE.remove();
        // Reserve a slot before the next roll. Later draws can replace an occupied slot.
        return stack -> {
            output.accept(stack);
            draw.slots.add(draw.randomSlot());
        };
    }

    private int randomSlot() {
        return context.getRandom().nextInt(size);
    }

    private int next() {
        return consumed < slots.size() ? slots.getInt(consumed++) : randomSlot();
    }
}
