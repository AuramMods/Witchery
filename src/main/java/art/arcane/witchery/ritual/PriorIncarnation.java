package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PriorIncarnation {
    private static final String INVENTORY = "WITCPriIncInv";
    private static final String USER = "WITCPriIncUsr";
    private static final String OWNER = "WITCPriIncOwner";
    private static final String DEATH = "WITCPriIncDeath";
    private static final String X = "WITCPriIncLocX";
    private static final String Y = "WITCPriIncLocY";
    private static final String Z = "WITCPriIncLocZ";
    private static final int PLAYER_RADIUS = 5;
    private static final int DEATH_RADIUS = 16;

    private PriorIncarnation() {}

    public static boolean isAllowed(ServerLevel level) {
        return !level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && trackingEnabled();
    }

    private static boolean trackingEnabled() {
        ModList mods = ModList.get();
        return WitcheryServerConfig.ALLOW_DEATH_ITEM_RECOVERY_RITE.get()
                && (!WitcheryServerConfig.RESPECT_OTHER_DEATH_CHEST_MODS.get()
                || !(mods.isLoaded("tombstone") || mods.isLoaded("gravestone") || mods.isLoaded("corpse") || mods.isLoaded("yigd")));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void markDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getDrops().isEmpty() || !isAllowed(player.serverLevel())) return;
        UUID death = UUID.randomUUID();
        CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        data.remove(INVENTORY);
        data.putUUID(DEATH, death);
        data.putDouble(X, player.getX());
        data.putDouble(Y, player.getY());
        data.putDouble(Z, player.getZ());
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
        for (ItemEntity drop : event.getDrops()) {
            if (drop.getItem().isEmpty()) continue;
            CompoundTag marker = drop.getItem().getOrCreateTag();
            marker.putString(USER, player.getGameProfile().getName());
            marker.putUUID(OWNER, player.getUUID());
            marker.putUUID(DEATH, death);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void saveExpiredDrop(ItemExpireEvent event) {
        ItemEntity drop = event.getEntity();
        if (!(drop.level() instanceof ServerLevel level) || !trackingEnabled()) return;
        ItemStack stack = drop.getItem();
        CompoundTag marker = stack.getTag();
        if (marker == null || !marker.contains(USER, Tag.TAG_STRING)) return;
        var players = level.getServer().getPlayerList();
        ServerPlayer player = marker.hasUUID(OWNER) ? players.getPlayer(marker.getUUID(OWNER)) : players.getPlayerByName(marker.getString(USER));
        if (player == null) return;
        CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!hasLocation(data) || data.hasUUID(DEATH) != marker.hasUUID(DEATH)
                || data.hasUUID(DEATH) && !data.getUUID(DEATH).equals(marker.getUUID(DEATH))) return;
        ItemStack recovered = stack.copy();
        removeMarker(recovered);
        ListTag items = data.getList(INVENTORY, Tag.TAG_COMPOUND);
        items.add(recovered.save(new CompoundTag()));
        data.put(INVENTORY, items);
    }

    public static boolean pickUp(Inventory inventory, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(USER, Tag.TAG_STRING)) return inventory.add(stack);
        ItemStack clean = stack.copy();
        removeMarker(clean);
        boolean pickedUp = inventory.add(clean);
        // Inventory.add can move part of a stack before returning false.
        stack.setCount(clean.getCount());
        return pickedUp;
    }

    public static void recover(ServerLevel level, BlockPos heart) {
        Vec3 center = Vec3.atLowerCornerOf(heart);
        AABB players = new AABB(center.x - PLAYER_RADIUS, center.y, center.z - PLAYER_RADIUS,
                center.x + PLAYER_RADIUS, center.y + 1, center.z + PLAYER_RADIUS);
        boolean recovered = false;
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, players, Player::isAlive)) {
            if (player.distanceToSqr(center) > PLAYER_RADIUS * PLAYER_RADIUS) continue;
            CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            if (!hasLocation(data) || center.distanceToSqr(data.getDouble(X), data.getDouble(Y), data.getDouble(Z)) > DEATH_RADIUS * DEATH_RADIUS) continue;
            if (recoverItems(level, center, data)) {
                recovered = true;
                var skeleton = EntityType.SKELETON.create(level);
                if (skeleton != null) {
                    skeleton.moveTo(center);
                    skeleton.setCustomName(Component.literal(player.getGameProfile().getName()));
                    level.addFreshEntity(skeleton);
                }
            }
        }
        level.sendParticles(recovered ? ParticleTypes.EXPLOSION : ParticleTypes.SMOKE,
                center.x, center.y, center.z, 16, 0.5, 0.5, 0.5, 0);
        level.playSound(null, heart, recovered ? SoundEvents.FIRE_EXTINGUISH : SoundEvents.NOTE_BLOCK_SNARE.value(),
                SoundSource.BLOCKS, 1, 1);
    }

    private static boolean recoverItems(ServerLevel level, Vec3 center, CompoundTag data) {
        ListTag items = data.getList(INVENTORY, Tag.TAG_COMPOUND);
        boolean recovered = false;
        for (int index = 0; index < items.size();) {
            ItemStack stack = WitcheryIngredient.loadInventoryStack(items.getCompound(index));
            removeMarker(stack);
            if (stack.isEmpty()) {
                index++;
                continue;
            }
            ItemEntity drop = new ItemEntity(level, center.x, center.y, center.z, stack);
            drop.setDefaultPickUpDelay();
            if (level.addFreshEntity(drop)) {
                items.remove(index);
                recovered = true;
            } else {
                index++;
            }
        }
        if (recovered && items.isEmpty()) {
            for (String key : new String[]{INVENTORY, X, Y, Z, DEATH}) data.remove(key);
        }
        return recovered;
    }

    private static boolean hasLocation(CompoundTag data) {
        return data.contains(X, Tag.TAG_ANY_NUMERIC) && data.contains(Y, Tag.TAG_ANY_NUMERIC) && data.contains(Z, Tag.TAG_ANY_NUMERIC);
    }

    private static void removeMarker(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        tag.remove(USER);
        tag.remove(OWNER);
        tag.remove(DEATH);
        if (tag.isEmpty()) stack.setTag(null);
    }
}
