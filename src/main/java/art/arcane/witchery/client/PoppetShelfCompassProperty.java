package art.arcane.witchery.client;

import art.arcane.witchery.item.PoppetShelfCompassItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class PoppetShelfCompassProperty implements ItemPropertyFunction {
    static final PoppetShelfCompassProperty INSTANCE = new PoppetShelfCompassProperty();
    private final Map<ItemStack, Reading> readings = new WeakHashMap<>();

    private PoppetShelfCompassProperty() {}

    public static void sample(ItemStack stack, Level level, Entity holder) {
        int signal = PoppetShelfCompassItem.signal(level, holder.getX(), holder.getZ());
        INSTANCE.readings.put(stack, new Reading(new WeakReference<>(holder), signal));
    }

    @Override
    public float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity holder, int seed) {
        if (holder == null || !(holder.level() instanceof ClientLevel world)
                || (level != null && level != world)) return 0;
        var reading = readings.get(stack);
        return reading != null && reading.holder().get() == holder ? reading.signal() : 0;
    }

    private record Reading(WeakReference<Entity> holder, int signal) {}
}
