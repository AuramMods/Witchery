package art.arcane.witchery.client;

import art.arcane.witchery.item.PoppetShelfCompassItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

final class PoppetShelfCompassProperty implements ItemPropertyFunction {
    private final Map<LivingEntity, Reading> readings = new WeakHashMap<>();
    private WeakReference<ClientLevel> lastWorld = new WeakReference<>(null);

    @Override
    public float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity holder, int seed) {
        if (holder == null || !(holder.level() instanceof ClientLevel world)
                || (level != null && level != world)) return 0;
        if (lastWorld.get() != world) {
            readings.clear();
            lastWorld = new WeakReference<>(world);
        }
        long tick = world.getGameTime();
        var reading = readings.get(holder);
        if (reading == null || tick < reading.tick() || tick - reading.tick() >= 20) {
            int signal = PoppetShelfCompassItem.signal(world, holder.getX(), holder.getZ());
            reading = new Reading(tick, signal);
            readings.put(holder, reading);
        }
        return reading.signal();
    }

    private record Reading(long tick, int signal) {}
}
