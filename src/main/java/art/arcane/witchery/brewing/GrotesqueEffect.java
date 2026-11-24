package art.arcane.witchery.brewing;

import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.entity.HobgoblinGod;
import art.arcane.witchery.entity.Leonard;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.ritual.ProtectionRites;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

public final class GrotesqueEffect extends TrackedBrewEffect {
    public static final String BREW_TIMER = "witcheryGrotesque";
    private static final int BREW_PULSES = 1200;
    private static final int RADIUS = 4;

    public GrotesqueEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x322116);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel level && level.getGameTime() % 5 == 3) repel(entity, level, false);
    }

    private static void repel(LivingEntity entity, ServerLevel level, boolean brewed) {
        for (Mob mob : level.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(RADIUS),
                mob -> mob != entity && mob.isAlive() && !(mob instanceof Demon || mob instanceof AbstractGolem
                        || mob instanceof HobgoblinGod || mob instanceof Leonard)
                        && !mob.getType().is(Tags.EntityTypes.BOSSES) && !Vampirism.isBoss(mob)
                        && (!brewed || !(mob instanceof Witch)))) {
            Vec3 offset = mob.position().subtract(entity.position());
            double distanceSquared = offset.lengthSqr();
            if (distanceSquared < 1.0E-6 || distanceSquared >= RADIUS * RADIUS) continue;
            Vec3 push = ProtectionRites.repulsion(offset);
            mob.push(push.x, push.y, push.z);
        }
    }

    public static void drinkBrew(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putInt(BREW_TIMER, BREW_PULSES);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        updateBrewStyle(player, BREW_PULSES, true);
    }

    public static void restoreBrew(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        MobEffectInstance previousBrew = player.getEffect(WitcheryEffects.GROTESQUE.get());
        // Earlier port versions used a hidden level-I potion for the bottled brew. Cauldron Fear keeps its icon.
        if (previousBrew != null && previousBrew.getAmplifier() == 0 && !previousBrew.isAmbient()
                && !previousBrew.isVisible() && !previousBrew.showIcon() && previousBrew.getDuration() > 0) {
            if (!persistent.contains(BREW_TIMER, Tag.TAG_ANY_NUMERIC)) {
                persistent.putInt(BREW_TIMER, 1 + (previousBrew.getDuration() - 1) / 4);
                player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
            }
            player.removeEffect(WitcheryEffects.GROTESQUE.get());
        }
        updateBrewStyle(player, persistent.getInt(BREW_TIMER), false);
    }

    public static void tickBrew(ServerPlayer player) {
        if (!player.isAlive()) return;
        CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        int remaining = persistent.getInt(BREW_TIMER);
        if (remaining <= 0) return;
        repel(player, player.serverLevel(), true);
        if (remaining == 1) persistent.remove(BREW_TIMER);
        else persistent.putInt(BREW_TIMER, remaining - 1);
        updateBrewStyle(player, remaining - 1, remaining == 1);
    }

    private static void updateBrewStyle(ServerPlayer player, int pulses, boolean broadcast) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            data.setGrotesqueTicks(pulses);
            if (broadcast) {
                data.bumpSyncRevision();
                WitcheryNetwork.sendPartialExtendedPlayerSync(player, data);
                WitcheryNetwork.broadcastPlayerStyle(player, data);
            }
        });
    }
}
