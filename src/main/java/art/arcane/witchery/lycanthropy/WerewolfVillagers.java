package art.arcane.witchery.lycanthropy;

import art.arcane.witchery.entity.WereVillager;
import art.arcane.witchery.entity.Wolfman;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.vampirism.VampireVillages;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class WerewolfVillagers {
    private static final String ORIGINAL_ID = "WITCWereVillagerUUID";
    private static final Map<MinecraftServer, Map<UUID, WeakReference<Mob>>> BY_ORIGINAL = new WeakHashMap<>();

    private WerewolfVillagers() {}

    public static UUID originalIdentity(Mob creature) {
        var persistent = creature.getPersistentData();
        return persistent.hasUUID(ORIGINAL_ID) ? persistent.getUUID(ORIGINAL_ID) : creature.getUUID();
    }

    public static void track(ServerLevel level, Mob creature) {
        if (!creature.getPersistentData().hasUUID(ORIGINAL_ID)) return;
        BY_ORIGINAL.computeIfAbsent(level.getServer(), server -> new HashMap<>())
                .put(originalIdentity(creature), new WeakReference<>(creature));
    }

    public static void untrack(ServerLevel level, Mob creature) {
        if (!creature.getPersistentData().hasUUID(ORIGINAL_ID)) return;
        var loaded = BY_ORIGINAL.get(level.getServer());
        if (loaded != null) loaded.computeIfPresent(originalIdentity(creature),
                (uuid, reference) -> reference.get() == creature ? null : reference);
    }

    @Nullable
    public static LivingEntity findByTaglock(MinecraftServer server, UUID uuid) {
        var loaded = BY_ORIGINAL.get(server);
        if (loaded == null) return null;
        var reference = loaded.get(uuid);
        Mob creature = reference == null ? null : reference.get();
        return creature != null && creature.isAlive() && creature.level() instanceof ServerLevel level
                && level.getEntity(creature.getUUID()) == creature ? creature : null;
    }

    public static boolean infect(Villager victim, boolean infectious) {
        if (!(victim.level() instanceof ServerLevel level) || victim instanceof WereVillager) return false;
        var type = WitcheryEntities.WERE_VILLAGER.get();
        if (!ForgeEventFactory.canLivingConvert(victim, type, ticks -> {})) return false;
        var villager = type.create(level);
        if (villager == null) return false;
        var data = villagerData(victim);
        data.putBoolean("Infectious", infectious);
        villager.load(data);
        return replace(level, victim, villager);
    }

    public static boolean cleanse(WereVillager victim) {
        if (!(victim.level() instanceof ServerLevel level)
                || !ForgeEventFactory.canLivingConvert(victim, EntityType.VILLAGER, ticks -> {})) return false;
        var villager = EntityType.VILLAGER.create(level);
        if (villager == null) return false;
        villager.load(villagerData(victim));
        return replace(level, victim, villager);
    }

    public static boolean transform(WereVillager victim, boolean infectious) {
        if (!(victim.level() instanceof ServerLevel level)) return false;
        var type = WitcheryEntities.WOLFMAN.get();
        if (!ForgeEventFactory.canLivingConvert(victim, type, ticks -> {})) return false;
        var wolfman = type.create(level);
        if (wolfman == null) return false;
        wolfman.setFormerVillager(villagerData(victim), infectious);
        wolfman.setCustomName(victim.getCustomName());
        wolfman.setCustomNameVisible(victim.isCustomNameVisible());
        wolfman.setInvulnerable(victim.isInvulnerable());
        wolfman.setNoAi(victim.isNoAi());
        if (!replace(level, victim, wolfman)) return false;
        level.playSound(null, wolfman, WitcherySounds.WOLFMAN_HOWL.get(), SoundSource.HOSTILE, 1, 1);
        return true;
    }

    public static boolean revert(Wolfman wolfman, boolean keepCurse) {
        if (!(wolfman.level() instanceof ServerLevel level) || !wolfman.hasFormerVillager()) return false;
        EntityType<? extends Villager> type = keepCurse ? WitcheryEntities.WERE_VILLAGER.get() : EntityType.VILLAGER;
        if (!ForgeEventFactory.canLivingConvert(wolfman, type, ticks -> {})) return false;
        var villager = type.create(level);
        if (villager == null) return false;
        var data = wolfman.formerVillagerData();
        data.putBoolean("Infectious", keepCurse && wolfman.isInfectious());
        villager.load(data);
        return replace(level, wolfman, villager);
    }

    private static boolean replace(ServerLevel level, Mob original, Mob replacement) {
        if (!original.isAlive()) return false;
        replacement.copyPosition(original);
        replacement.getPersistentData().putUUID(ORIGINAL_ID, originalIdentity(original));
        replacement.setPersistenceRequired();
        if (!level.addFreshEntity(replacement)) {
            untrack(level, replacement);
            track(level, original);
            return false;
        }
        if (original instanceof Villager resident) VampireVillages.releaseResidentClaims(resident);
        ForgeEventFactory.onLivingConvert(original, replacement);
        original.discard();
        level.sendParticles(ParticleTypes.POOF, replacement.getX(), replacement.getY(0.5), replacement.getZ(),
                14, 0.4, 0.45, 0.4, 0.04);
        level.playSound(null, replacement, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.NEUTRAL, 0.8F, 1);
        return true;
    }

    private static CompoundTag villagerData(Villager villager) {
        var data = villager.saveWithoutId(new CompoundTag());
        data.remove("UUID");
        data.remove("SleepingX");
        data.remove("SleepingY");
        data.remove("SleepingZ");
        var memories = data.getCompound("Brain").getCompound("memories");
        for (String claimed : new String[]{"home", "job_site", "potential_job_site", "meeting_point"}) {
            memories.remove("minecraft:" + claimed);
        }
        return data;
    }

    @Nullable
    public static CompoundTag legacyProfession(CompoundTag wolfman) {
        if (!wolfman.contains("FormerProfession", Tag.TAG_ANY_NUMERIC) || wolfman.getInt("FormerProfession") < 0) return null;
        var profession = switch (wolfman.getInt("FormerProfession")) {
            case 0 -> VillagerProfession.FARMER;
            case 1 -> VillagerProfession.LIBRARIAN;
            case 2 -> VillagerProfession.CLERIC;
            case 3 -> VillagerProfession.TOOLSMITH;
            case 4 -> VillagerProfession.BUTCHER;
            default -> VillagerProfession.NONE;
        };
        var data = new CompoundTag();
        VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, new VillagerData(VillagerType.PLAINS, profession, 1))
                .result().ifPresent(villagerData -> data.put("VillagerData", villagerData));
        return data;
    }
}
