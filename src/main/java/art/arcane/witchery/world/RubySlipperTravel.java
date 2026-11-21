package art.arcane.witchery.world;

import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public final class RubySlipperTravel {
    private static final String HOME_TIME = "WITCLastRubySlipperTime";
    private static final String WAYSTONE_TIME = "WITCLastRubySlipperWayTime";
    private static final long MINUTE = 60_000L;

    private RubySlipperTravel() {}

    public static boolean matches(String message) {
        return message.toLowerCase(Locale.ROOT).replace("'", "").startsWith("theres no place like home");
    }

    public static boolean chant(ServerPlayer player, String message) {
        if (!matches(message) || !player.isAlive() || player.isRemoved()
                || WitchClothingItem.style(player.getItemBySlot(EquipmentSlot.FEET)) != WitchClothingItem.Style.RUBY_SLIPPERS) {
            return false;
        }
        ServerLevel here = player.serverLevel();
        if (here.dimension().equals(WitcheryDimensions.DREAM)) return false;
        var data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        long now = System.currentTimeMillis();
        var nearby = new AABB(player.position(), player.position()).inflate(3);
        for (ItemEntity dropped : here.getEntitiesOfClass(ItemEntity.class, nearby,
                entity -> entity.getItem().is(WitcheryIngredient.BOUND_WAYSTONE.item()))) {
            WaystoneItem.Location location = WaystoneItem.location(dropped.getItem());
            if (location == null) continue;
            travelByStone(player, here, dropped, location, data, now);
            return true;
        }
        returnHome(player, here, data, now);
        return true;
    }

    private static void travelByStone(ServerPlayer player, ServerLevel here, ItemEntity dropped, WaystoneItem.Location location,
                                      CompoundTag data, long now) {
        if (coolingDown(player, data, WAYSTONE_TIME, now, MINUTE)) return;
        ServerLevel target = here.getServer().getLevel(location.dimension());
        if (target == null || target != here
                && (WaystoneTravel.protectedWorld(here.dimension()) || WaystoneTravel.protectedWorld(target.dimension()))) return;
        int cost = target == here ? 40 : 80;
        if (!InfusionEnergy.canSpend(player, cost)) return;
        boolean moved;
        if (here.random.nextDouble() < 0.01) {
            moved = WitcheryTeleport.randomSurface(player, here, player.blockPosition(), 500);
        } else {
            Vec3 landing = WaystoneTravel.findLanding(player, target, location.position(), 2);
            moved = landing != null && WitcheryTeleport.move(player, target, landing);
        }
        if (moved) {
            InfusionEnergy.spend(player, cost);
            dropped.discard();
            remember(player, data, WAYSTONE_TIME, now);
        }
    }

    private static void returnHome(ServerPlayer player, ServerLevel here, CompoundTag data, long now) {
        if (!player.getAbilities().instabuild && coolingDown(player, data, HOME_TIME, now, MINUTE * 30)) return;
        ServerLevel overworld = here.getServer().overworld();
        ServerLevel target = here.getServer().getLevel(player.getRespawnDimension());
        Vec3 arrival = null;
        if ((target == here || target == overworld) && player.getRespawnPosition() != null) {
            arrival = Player.findRespawnPositionAndUseSpawnBlock(target, player.getRespawnPosition(),
                    player.getRespawnAngle(), player.isRespawnForced(), true).orElse(null);
        }
        if (arrival == null) {
            target = overworld;
            var spawn = overworld.getSharedSpawnPos();
            arrival = WaystoneTravel.findLanding(player, target, spawn, 2);
            if (arrival == null) {
                var surface = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn);
                arrival = WaystoneTravel.findLanding(player, target, surface, 2);
            }
        }
        int cost = target == here ? 80 : 120;
        if (arrival != null && InfusionEnergy.canSpend(player, cost) && WitcheryTeleport.move(player, target, arrival)) {
            InfusionEnergy.spend(player, cost);
            remember(player, data, HOME_TIME, now);
        }
    }

    private static boolean coolingDown(ServerPlayer player, CompoundTag data, String key, long now, long cooldown) {
        long elapsed = now - data.getLong(key);
        if (elapsed >= cooldown || elapsed < 0) return false;
        player.displayClientMessage(Component.translatable("message.witchery.ruby_slippers.cooldown",
                Math.max(1, (cooldown - elapsed) / MINUTE)).withStyle(ChatFormatting.RED), false);
        return true;
    }

    private static void remember(ServerPlayer player, CompoundTag data, String key, long now) {
        data.putLong(key, now);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
    }
}
