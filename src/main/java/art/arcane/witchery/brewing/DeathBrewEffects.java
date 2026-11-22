package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.LegacyRegistryData;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class DeathBrewEffects {
    private static final String RETAINED_INVENTORY = "WITCKeepInventory";
    private static final String LEGACY_RETAINED_INVENTORY = "WITCLegacyRetainedInventory";
    private static final String EFFECTS_ON_RESPAWN = "WITCPoSpawn";
    private static final int FIRST_LEGACY_WITCHERY_EFFECT = 32;

    private DeathBrewEffects() {}

    public static boolean retainInventory(Player player) {
        if (!(player instanceof ServerPlayer)) return false;
        if (!player.hasEffect(WitcheryEffects.KEEP_INVENTORY.get())) {
            player.getPersistentData().remove(RETAINED_INVENTORY);
            return false;
        }
        player.getPersistentData().putBoolean(RETAINED_INVENTORY, true);
        return true;
    }

    public static void restoreInventory(Player original, Player respawned) {
        CompoundTag data = original.getPersistentData();
        if (data.getBoolean(RETAINED_INVENTORY)) {
            respawned.getInventory().replaceWith(original.getInventory());
            data.remove(RETAINED_INVENTORY);
        }
    }

    public static void storeLegacyInventory(CompoundTag persisted, ListTag inventory) {
        if (!persisted.contains(LEGACY_RETAINED_INVENTORY, Tag.TAG_LIST)) {
            persisted.put(LEGACY_RETAINED_INVENTORY, inventory.copy());
        }
    }

    public static void restorePendingInventory(ServerPlayer player) {
        if (!player.isAlive()) return;
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.contains(LEGACY_RETAINED_INVENTORY, Tag.TAG_LIST)) {
            player.getInventory().load(persisted.getList(LEGACY_RETAINED_INVENTORY, Tag.TAG_COMPOUND));
            persisted.remove(LEGACY_RETAINED_INVENTORY);
            player.inventoryMenu.broadcastChanges();
        }
    }

    public static void onDeath(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        if (entity instanceof ServerPlayer player) saveEffects(player);
        MobEffectInstance reincarnation = entity.getEffect(WitcheryEffects.REINCARNATION.get());
        if (reincarnation != null) reincarnate(level, entity, reincarnation.getAmplifier());
    }

    private static void saveEffects(ServerPlayer player) {
        MobEffectInstance keep = player.getEffect(WitcheryEffects.KEEP_EFFECTS.get());
        ListTag saved = new ListTag();
        for (MobEffectInstance instance : player.getActiveEffects()) {
            MobEffect effect = instance.getEffect();
            if (persistsAfterDeath(effect) || keep != null && effect.getCategory() != MobEffectCategory.HARMFUL
                    && instance.getAmplifier() <= keep.getAmplifier()) {
                saved.add(instance.save(new CompoundTag()));
            }
        }
        CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (saved.isEmpty()) {
            data.remove(EFFECTS_ON_RESPAWN);
        } else {
            data.put(EFFECTS_ON_RESPAWN, saved);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
        }
    }

    private static boolean persistsAfterDeath(MobEffect effect) {
        return effect == WitcheryEffects.KEEP_EFFECTS.get() || effect == WitcheryEffects.SINKING.get()
                || effect == WitcheryEffects.OVERHEATING.get() || effect == WitcheryEffects.INSANITY.get()
                || effect == WitcheryEffects.WAKING_NIGHTMARE.get() || effect == WitcheryEffects.ILL_FITTING.get();
    }

    public static void restoreEffects(ServerPlayer player) {
        if (!player.isAlive()) return;
        CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        ListTag saved = data.getList(EFFECTS_ON_RESPAWN, Tag.TAG_COMPOUND);
        data.remove(EFFECTS_ON_RESPAWN);
        for (int index = 0; index < saved.size(); index++) {
            MobEffectInstance effect = loadEffect(saved.getCompound(index));
            if (effect != null && !player.hasEffect(effect.getEffect())) player.addEffect(effect);
        }
    }

    @Nullable
    private static MobEffectInstance loadEffect(CompoundTag saved) {
        if (saved.contains("forge:id", Tag.TAG_STRING)) return MobEffectInstance.load(saved);
        int id = saved.getByte("Id") & 0xff;
        if (id > 0 && id <= 23) return MobEffectInstance.load(saved);
        int witcheryIndex = id - FIRST_LEGACY_WITCHERY_EFFECT;
        if (witcheryIndex < 0 || witcheryIndex >= LegacyRegistryData.EFFECTS.size()) return null;
        CompoundTag named = saved.copy();
        named.putString("forge:id", Witchery.MODID + ":" + LegacyRegistryData.EFFECTS.get(witcheryIndex));
        return MobEffectInstance.load(named);
    }

    private static void reincarnate(ServerLevel level, LivingEntity original, int amplifier) {
        BlockPos.MutableBlockPos spawn = original.blockPosition().mutable();
        int ceiling = Math.min(spawn.getY() + 8, level.getMaxBuildHeight() - 2);
        while (!level.isEmptyBlock(spawn) && spawn.getY() < ceiling) spawn.move(Direction.UP);
        while (spawn.getY() > level.getMinBuildHeight() + 1 && level.isEmptyBlock(spawn.below())) spawn.move(Direction.DOWN);
        if (!level.isEmptyBlock(spawn) || !level.isEmptyBlock(spawn.above())) return;

        EntityType<? extends Mob> type = original instanceof Animal || original instanceof Spider
                ? amplifier >= 3 ? EntityType.CREEPER : amplifier == 2 ? EntityType.CAVE_SPIDER : EntityType.SPIDER
                : amplifier >= 3 ? EntityType.BLAZE : amplifier == 2 ? EntityType.SKELETON : EntityType.ZOMBIE;
        Mob creature = type.create(level);
        if (creature == null) return;
        Vec3 position = Vec3.atBottomCenterOf(spawn).add(0, 0.05, 0);
        creature.moveTo(position);
        if (!level.noCollision(creature) || !level.addFreshEntity(creature)) return;
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, position.x, position.y, position.z, 16, 0.5, 0.5, 0.5, 0);
        level.playSound(null, spawn, SoundEvents.ZOMBIE_AMBIENT, SoundSource.HOSTILE, 1, 1);
    }
}
