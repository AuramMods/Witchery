package art.arcane.witchery.client;

import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class PlayerCompassProperty implements ItemPropertyFunction {
    static final PlayerCompassProperty INSTANCE = new PlayerCompassProperty();
    private static final int DIRECTIONS = 32;
    private final Map<ItemStack, Reading> readings = new WeakHashMap<>();

    private PlayerCompassProperty() {}

    public static void tick(ItemStack stack, Level level, Entity holder) {
        if (!(level instanceof ClientLevel world)) return;
        if (!TaglockItem.isBound(stack)) {
            INSTANCE.readings.remove(stack);
            return;
        }

        var target = findTarget(world, stack);
        int frame = target == null ? frame(Math.random() * Math.PI * 2.0)
                : directionFrame(holder.getYRot(), target.getX() - holder.getX(), target.getZ() - holder.getZ());
        INSTANCE.readings.put(stack, new Reading(new WeakReference<>(holder), frame));
    }

    @Override
    public float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity holder, int seed) {
        if (holder == null || !(holder.level() instanceof ClientLevel world)
                || (level != null && level != world) || !TaglockItem.isBound(stack)) return 0;
        var reading = readings.get(stack);
        return reading != null && reading.holder().get() == holder ? reading.frame() / (float) DIRECTIONS : 0;
    }

    public static void receiveTrackedIdentity(int entityId, UUID entityUuid, UUID originalUuid) {
        var world = Minecraft.getInstance().level;
        if (world != null && world.getEntity(entityId) instanceof Mob mob && entityUuid.equals(mob.getUUID())) {
            WerewolfVillagers.setOriginalIdentity(mob, originalUuid);
        }
    }

    @Nullable
    private static LivingEntity findTarget(ClientLevel world, ItemStack stack) {
        String playerName = TaglockItem.boundPlayerName(stack);
        if (!playerName.isEmpty()) {
            UUID playerId = TaglockItem.boundPlayerId(stack);
            if (playerId != null) return world.getPlayerByUUID(playerId);
            for (var player : world.players()) {
                if (player.getGameProfile().getName().equalsIgnoreCase(playerName)) return player;
            }
            return null;
        }

        UUID creatureId = TaglockItem.boundCreatureId(stack);
        if (creatureId == null) return null;
        LivingEntity converted = null;
        for (Entity entity : world.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
            if (creatureId.equals(living.getUUID())) return living;
            if (living instanceof Mob mob && creatureId.equals(WerewolfVillagers.originalIdentity(mob))) converted = mob;
        }
        return converted;
    }

    private static int directionFrame(float yaw, double x, double z) {
        double angle = -((yaw % 360.0 - 90.0) * Math.PI / 180.0 - Math.atan2(z, x));
        return frame(angle);
    }

    private static int frame(double angle) {
        return 1 + Math.floorMod((int) ((angle / (Math.PI * 2.0) + 1.0) * DIRECTIONS), DIRECTIONS);
    }

    private record Reading(WeakReference<Entity> holder, int frame) {}
}
