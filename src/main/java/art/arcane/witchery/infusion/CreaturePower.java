package art.arcane.witchery.infusion;

import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum CreaturePower {
    CAVE_SPIDER(1, "cave_spider"),
    SPIDER(2, "spider"),
    CREEPER(3, "creeper"),
    BAT(4, "bat"),
    SQUID(5, "squid"),
    GHAST(6, "ghast"),
    BLAZE(7, "blaze"),
    ZOMBIFIED_PIGLIN(8, "zombified_piglin"),
    ZOMBIE(9, "zombie", "zombie_villager"),
    SKELETON(10, "skeleton", "wither_skeleton"),
    MAGMA_CUBE(11, "magma_cube"),
    SLIME(12, "slime"),
    SILVERFISH(13, "silverfish"),
    OCELOT(14, "ocelot", "cat"),
    WOLF(15, "wolf"),
    HORSE(16, "horse", "donkey", "mule", "zombie_horse", "skeleton_horse"),
    ENDERMAN(17, "enderman"),
    SHEEP(18, 1, "sheep"),
    COW(19, 1, "cow"),
    CHICKEN(20, 1, "chicken"),
    PIG(21, 1, "pig"),
    VILLAGER(22, 2, "villager"),
    MOOSHROOM(23, 2, "mooshroom"),
    OWL(24, "witchery:owl"),
    TOAD(25, "witchery:toad");

    public static final int MAX_CHARGES = 20;
    private static final String POWER = "WITCBeastPower";
    private static final String CHARGES = "WITCBeastPowerCharges";
    private static final CreaturePower[] BY_ID = values();
    private static final Map<ResourceLocation, CreaturePower> BY_ENTITY = Stream.of(BY_ID)
            .flatMap(power -> power.creatures.stream().map(creature -> Map.entry(creature, power)))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    private final int id;
    private final List<ResourceLocation> creatures;
    private final int perSacrifice;

    CreaturePower(int id, String... creatures) {
        this(id, 10, creatures);
    }

    CreaturePower(int id, int perSacrifice, String... creatures) {
        this.id = id;
        this.creatures = Stream.of(creatures).map(ResourceLocation::parse).toList();
        this.perSacrifice = perSacrifice;
    }

    @Nullable
    public static CreaturePower forId(int id) {
        return id > 0 && id <= BY_ID.length ? BY_ID[id - 1] : null;
    }

    @Nullable
    public static CreaturePower forCreature(EntityType<?> type) {
        return BY_ENTITY.get(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static int activeId(ServerPlayer player) {
        return data(player).getInt(POWER);
    }

    public static int charges(ServerPlayer player) {
        return Mth.clamp(data(player).getInt(CHARGES), 0, MAX_CHARGES);
    }

    public static void tickFlight(ServerPlayer player) {
        CreaturePower power = forId(activeId(player));
        if ((power == BAT || power == OWL) && player.connection != null) player.connection.clientIsFloating = false;
    }

    public void absorb(ServerPlayer player) {
        CompoundTag data = data(player);
        int total = activeId(player) == id ? charges(player) + perSacrifice : perSacrifice;
        data.putInt(POWER, id);
        data.putInt(CHARGES, Math.min(total, MAX_CHARGES));
        InfusionEnergy.sync(player);
    }

    public void use(ServerPlayer player, int elapsed, HitResult hit) {
        int cost = this == CREEPER && elapsed >= 60 ? 2 : 1;
        int remaining = charges(player) - cost;
        if (remaining < 0) {
            InfernalInfusion.fail(player);
            return;
        }
        if (!InfusionEnergy.spendHandCharge(player, 1)) return;
        activate(player, elapsed, hit);
        if (!player.getAbilities().instabuild) {
            data(player).putInt(CHARGES, remaining);
            InfusionEnergy.sync(player);
        }
    }

    private void activate(ServerPlayer player, int elapsed, HitResult hit) {
        ServerLevel level = player.serverLevel();
        switch (this) {
            case CAVE_SPIDER, SPIDER -> launchWeb(player);
            case CREEPER -> level.explode(player, player.getX(), player.getY(), player.getZ(), elapsed >= 60 ? 6 : 3,
                    Level.ExplosionInteraction.MOB);
            case BAT, OWL -> buff(player, MobEffects.NIGHT_VISION, 400, 0);
            case SQUID -> {
                if (hit instanceof EntityHitResult entity && entity.getEntity() instanceof LivingEntity victim) {
                    victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
                    float pitch = 1 / (level.random.nextFloat() * 0.4F + 1.2F) + 0.5F;
                    level.playSound(null, player, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1, pitch);
                }
            }
            case GHAST -> launchFire(player, true);
            case BLAZE -> launchFire(player, false);
            case ZOMBIFIED_PIGLIN -> {
                buff(player, MobEffects.DAMAGE_RESISTANCE, 600, 2);
                buff(player, MobEffects.DAMAGE_BOOST, 600, 2);
                fizz(player);
            }
            case ZOMBIE -> {
                buff(player, MobEffects.DAMAGE_RESISTANCE, 600, 1);
                buff(player, MobEffects.DAMAGE_BOOST, 600, 0);
            }
            case SKELETON -> launchArrow(player, elapsed);
            case MAGMA_CUBE, SLIME, TOAD -> {
                buff(player, MobEffects.JUMP, 400, 3);
                fizz(player);
            }
            case SILVERFISH, OCELOT, WOLF, HORSE -> {
                buff(player, MobEffects.MOVEMENT_SPEED, 400, 3);
                fizz(player);
            }
            case ENDERMAN -> {
                if (!OtherwhereInfusion.teleportAimed(player, hit)) InfernalInfusion.fail(player);
            }
            case SHEEP, COW, CHICKEN, PIG, VILLAGER, MOOSHROOM -> {
                buff(player, MobEffects.HEAL, 10, 0);
                fizz(player);
            }
        }
    }

    public void hurt(ServerPlayer player, LivingHurtEvent event) {
        if (this == CREEPER && event.getSource().is(DamageTypes.LIGHTNING_BOLT)) {
            InfusionEnergy.recharge(player, 25);
            event.setCanceled(true);
        } else if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            if (this == ZOMBIFIED_PIGLIN && InfusionEnergy.spendCreatureDefense(player, 3)) {
                buff(player, MobEffects.FIRE_RESISTANCE, 1200, 0);
                fizz(player);
                event.setCanceled(true);
            } else if ((this == GHAST || this == BLAZE) && !player.hasEffect(MobEffects.FIRE_RESISTANCE)
                    && InfusionEnergy.spendCreatureDefense(player, 3)) {
                buff(player, MobEffects.FIRE_RESISTANCE, 200, 0);
                fizz(player);
            }
        } else if (event.getSource().is(DamageTypes.DROWN)) {
            if (this == SQUID && InfusionEnergy.spendCreatureDefense(player, 1)) {
                buff(player, MobEffects.WATER_BREATHING, 1200, 1);
                player.setAirSupply(300);
                fizz(player);
                event.setCanceled(true);
            } else if (this == SKELETON && InfusionEnergy.spendCreatureDefense(player, 5)) {
                buff(player, MobEffects.WATER_BREATHING, 600, 0);
                player.setAirSupply(30);
                fizz(player);
                event.setCanceled(true);
            }
        }
    }

    public void fall(ServerPlayer player, LivingFallEvent event) {
        switch (this) {
            case BAT, OWL -> event.setDistance(Math.min(event.getDistance(), 5));
            case MAGMA_CUBE, SLIME, TOAD -> {
                event.setDistance(0);
                WitcheryNetwork.sendSoundAround(player.serverLevel(), player.position(), 8, "mob.slime.big", -1, -1);
            }
        }
    }

    private static CompoundTag data(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static void buff(ServerPlayer player, MobEffect effect, int duration, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, duration, amplifier));
    }

    private static void fizz(ServerPlayer player) {
        WitcherySounds.playLegacyPlayerCue(player, SoundEvents.FIRE_EXTINGUISH, 0.5F);
    }

    private static void launchWeb(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ThrownBrew web = new ThrownBrew(player, level);
        web.setItem(new ItemStack(WitcheryIngredient.WEB.item()));
        launchAimed(player, web, 1.5F);
        level.addFreshEntity(web);
        float pitch = 0.4F / (level.random.nextFloat() * 0.4F + 0.8F);
        level.playSound(null, player, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5F, pitch);
    }

    private static void launchArrow(ServerPlayer player, int elapsed) {
        float draw = elapsed / 20F;
        float power = Math.min(1, (draw * draw + draw * 2) / 3);
        ServerLevel level = player.serverLevel();
        Arrow arrow = new Arrow(level, player);
        launchAimed(player, arrow, power * 3);
        arrow.setCritArrow(power == 1);
        arrow.setBaseDamage(arrow.getBaseDamage() + 1);
        arrow.setKnockback(1);
        level.addFreshEntity(arrow);
        float pitch = 1 / (level.random.nextFloat() * 0.4F + 1.2F) + power * 0.5F;
        level.playSound(null, player, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1, pitch);
    }

    private static void launchAimed(ServerPlayer player, Projectile shot, float speed) {
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        shot.setPos(player.getX() - Mth.cos(yaw) * 0.16F, player.getEyeY() - 0.1,
                player.getZ() - Mth.sin(yaw) * 0.16F);
        Vec3 aim = player.getLookAngle();
        shot.shoot(aim.x, aim.y, aim.z, speed, 1);
    }

    private static void launchFire(ServerPlayer player, boolean ghast) {
        ServerLevel level = player.serverLevel();
        Vec3 aim = player.getLookAngle();
        Vec3 start = player.position().add(aim.x, player.getBbHeight() / 2 + 0.5, aim.z);
        if (ghast) {
            var projectile = new LargeFireball(level, player, aim.x, aim.y, aim.z, 1);
            projectile.setPos(start);
            level.addFreshEntity(projectile);
            level.levelEvent(null, 1016, player.blockPosition(), 0);
            return;
        }
        for (int i = 0; i < 3; i++) {
            double x = aim.x + level.random.nextDouble() * 0.2;
            double z = aim.z + level.random.nextDouble() * 0.2;
            var projectile = new SmallFireball(level, player, x, aim.y, z);
            projectile.setPos(start);
            level.addFreshEntity(projectile);
        }
        level.levelEvent(null, 1018, player.blockPosition(), 0);
    }
}
