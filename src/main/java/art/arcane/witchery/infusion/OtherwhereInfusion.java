package art.arcane.witchery.infusion;

import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.world.WitcheryTeleport;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class OtherwhereInfusion {
    public static final int ID = 3;
    private static final String MESSAGE = "message.witchery.otherwhere.";
    private static final String RECALL = "WITCRecall";
    private static final int SET_RECALL_TICKS = 60;

    private OtherwhereInfusion() {}

    public static void channel(ServerPlayer player, int elapsed) {
        if (player.isShiftKeyDown() && elapsed == SET_RECALL_TICKS) {
            player.displayClientMessage(Component.translatable(MESSAGE + "remember_ready"), true);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.5F, 1);
        } else if (!player.isShiftKeyDown() && elapsed > 0 && elapsed % 20 == 0 && blinkTarget(player, elapsed) != null) {
            player.displayClientMessage(Component.translatable(MESSAGE + "blink_ready"), true);
        }
    }

    public static boolean release(ServerPlayer player, int elapsed) {
        if (player.isShiftKeyDown() && elapsed >= SET_RECALL_TICKS) {
            remember(player);
            return false;
        }
        if (inhibited(player)) return fail(player, "inhibited");
        if (player.isShiftKeyDown()) return recall(player, null);

        Vec3 target = blinkTarget(player, elapsed);
        ServerLevel level = player.serverLevel();
        if (target == null || !WitcheryTeleport.canArrive(player, level, target)) return fail(player, "no_destination");
        if (!InfusionEnergy.canSpend(player, 1) || !WitcheryTeleport.move(player, level, target)) return false;
        return InfusionEnergy.spend(player, 1);
    }

    public static void attack(ServerPlayer player, LivingEntity victim) {
        if (!victim.isAlive() || victim.isSpectator()) return;
        if (inhibited(player)) {
            fail(player, "inhibited");
            return;
        }
        if (player.isShiftKeyDown()) {
            recall(player, victim);
            return;
        }

        ServerLevel level = player.serverLevel();
        Vec3 playerTarget = liftTarget(level, player);
        Vec3 victimTarget = liftTarget(level, victim);
        boolean moveVictim = !inhibited(victim);
        if (playerTarget == null || victimTarget == null || !WitcheryTeleport.canArrive(player, level, playerTarget)
                || (moveVictim && !WitcheryTeleport.canArrive(victim, level, victimTarget))) {
            fail(player, "no_destination");
            return;
        }
        if (InfusionEnergy.canSpend(player, 2) && WitcheryTeleport.move(player, level, playerTarget)) {
            if (moveVictim) WitcheryTeleport.move(victim, level, victimTarget);
            InfusionEnergy.spend(player, 2);
        }
    }

    private static boolean recall(ServerPlayer player, @Nullable LivingEntity companion) {
        CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        ResourceKey<Level> dimension = storedDimension(data);
        ServerLevel destination = dimension == null ? null : player.server.getLevel(dimension);
        if (destination == null || restricted(player.level().dimension()) || restricted(dimension)
                || !data.contains(RECALL + "X", Tag.TAG_ANY_NUMERIC) || !data.contains(RECALL + "Y", Tag.TAG_ANY_NUMERIC)
                || !data.contains(RECALL + "Z", Tag.TAG_ANY_NUMERIC)) return fail(player, "no_recall");
        Vec3 target = new Vec3(data.getDouble(RECALL + "X"), data.getDouble(RECALL + "Y"), data.getDouble(RECALL + "Z"));
        if (!WitcheryTeleport.canArrive(player, destination, target)) return fail(player, "no_destination");
        boolean moveCompanion = companion != null && !inhibited(companion) && WitcheryTeleport.canArrive(companion, destination, target);
        int cost = companion == null ? 2 : 4;
        if (!InfusionEnergy.canSpend(player, cost) || !WitcheryTeleport.move(player, destination, target)) return false;
        if (moveCompanion) WitcheryTeleport.move(companion, destination, target);
        return InfusionEnergy.spend(player, cost);
    }

    private static void remember(ServerPlayer player) {
        CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        ResourceKey<Level> dimension = player.level().dimension();
        if (dimension.equals(Level.OVERWORLD)) data.putInt(RECALL + "D", 0);
        else if (dimension.equals(Level.NETHER)) data.putInt(RECALL + "D", -1);
        else if (dimension.equals(Level.END)) data.putInt(RECALL + "D", 1);
        else data.putString(RECALL + "D", dimension.location().toString());
        data.putDouble(RECALL + "X", player.getX());
        data.putDouble(RECALL + "Y", player.getY());
        data.putDouble(RECALL + "Z", player.getZ());
        BlockPos pos = player.blockPosition();
        player.displayClientMessage(Component.translatable(MESSAGE + "remembered", dimension.location().toString(),
                pos.getX(), pos.getY(), pos.getZ()), true);
        player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7F, 1);
    }

    @Nullable
    private static ResourceKey<Level> storedDimension(CompoundTag data) {
        String key = RECALL + "D";
        if (data.contains(key, Tag.TAG_ANY_NUMERIC)) {
            return switch (data.getInt(key)) {
                case -1 -> Level.NETHER;
                case 0 -> Level.OVERWORLD;
                case 1 -> Level.END;
                default -> null;
            };
        }
        ResourceLocation location = ResourceLocation.tryParse(data.getString(key));
        return location == null ? null : ResourceKey.create(Registries.DIMENSION, location);
    }

    @Nullable
    private static Vec3 blinkTarget(ServerPlayer player, int elapsed) {
        return destination(rayTrace(player, 40 + 20 * (elapsed / 20)));
    }

    static boolean teleportAimed(ServerPlayer player, HitResult hit) {
        Vec3 target = destination(hit);
        ServerLevel level = player.serverLevel();
        return target != null && !inhibited(player) && WitcheryTeleport.canArrive(player, level, target) && WitcheryTeleport.move(player, level, target);
    }

    @Nullable
    private static Vec3 destination(HitResult hitResult) {
        if (hitResult instanceof EntityHitResult entity) return entity.getLocation();
        if (!(hitResult instanceof BlockHitResult block) || block.getType() == HitResult.Type.MISS) return null;
        Vec3 hit = block.getLocation();
        return switch (block.getDirection()) {
            case DOWN -> hit.add(0, -2, 0);
            case UP -> hit;
            default -> hit.add(Vec3.atLowerCornerOf(block.getDirection().getNormal()).scale(0.5));
        };
    }

    static HitResult rayTrace(ServerPlayer player, double distance) {
        ServerLevel level = player.serverLevel();
        Vec3 eyes = player.getEyePosition();
        Vec3 direction = player.getLookAngle().scale(distance);
        BlockHitResult block = level.clip(new ClipContext(eyes, eyes.add(direction),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, player));
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(level, player, eyes, block.getLocation(),
                player.getBoundingBox().expandTowards(block.getLocation().subtract(eyes)).inflate(1),
                target -> target.isPickable() && !target.isSpectator());
        return entity == null ? block : entity;
    }

    @Nullable
    private static Vec3 liftTarget(ServerLevel level, LivingEntity actor) {
        Vec3 eyes = actor.getEyePosition();
        BlockHitResult ceiling = level.clip(new ClipContext(eyes, eyes.add(0, 8, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, actor));
        double room = ceiling.getType() == HitResult.Type.MISS ? 8
                : Math.min(8, ceiling.getLocation().y - actor.getY() - Math.max(2, actor.getBbHeight()));
        return room > 0 ? actor.position().add(0, room, 0) : null;
    }

    private static boolean restricted(ResourceKey<Level> dimension) {
        return dimension.equals(WitcheryDimensions.DREAM) || dimension.equals(WitcheryDimensions.TORMENT)
                || dimension.equals(WitcheryDimensions.MIRROR);
    }

    private static boolean inhibited(LivingEntity actor) {
        MobEffectInstance effect = actor.getEffect(WitcheryEffects.all().get("potion.enderinhibition").get());
        return effect != null && effect.getAmplifier() >= 2;
    }

    private static boolean fail(ServerPlayer player, String message) {
        player.displayClientMessage(Component.translatable(MESSAGE + message).withStyle(ChatFormatting.RED), true);
        player.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 0.5F, 1);
        return false;
    }
}
