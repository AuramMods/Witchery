package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.Illusion;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class EnvironmentalCurseEffect extends MobEffect {
    public enum Kind {
        SINKING(0x990000), OVERHEATING(0x009999), INSANITY(0x000099);

        private final int color;

        Kind(int color) {
            this.color = color;
        }
    }

    private final Kind kind;

    public EnvironmentalCurseEffect(Kind kind) {
        super(MobEffectCategory.HARMFUL, kind.color);
        this.kind = kind;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return kind != Kind.INSANITY || duration % 20 == 13;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        var level = entity.level();
        switch (kind) {
            case SINKING -> {
                if (entity instanceof Player player) {
                    sinkPlayer(player, amplifier);
                } else if (level.getGameTime() % 20 == 3) sinkCreature(entity, amplifier + 2);
            }
            case OVERHEATING -> {
                if (!level.isClientSide && level.getGameTime() % 5 == 3) overheat(entity, amplifier + 1);
            }
            case INSANITY -> {
                if (entity instanceof ServerPlayer player) spreadInsanity(player, amplifier + 1);
            }
        }
    }

    public static void sinkPlayer(Player player, int amplifier) {
        var motion = player.getDeltaMovement();
        if (player.isInWater() && !player.onGround() && motion.y < -0.03) {
            player.setDeltaMovement(motion.multiply(1, 1.5 + Math.min(0.05 * amplifier, 0.2), 1));
        } else if (!player.isInWater() && !player.getAbilities().instabuild && player.getAbilities().flying) {
            player.setDeltaMovement(motion.x, -0.2, motion.z);
        }
    }

    public static void sinkCreature(LivingEntity entity, int level) {
        if (!entity.isInWater()) return;
        var motion = entity.getDeltaMovement();
        double strength = Math.min(0.1 * level, 0.4);
        entity.setDeltaMovement(motion.multiply(1, motion.y < 0 ? 1 + strength : 1 - strength, 1));
    }

    public static void overheat(LivingEntity entity, int level) {
        if (entity.isOnFire() || entity.isInWater()) return;
        var random = entity.getRandom();
        var world = entity.level();
        var biome = world.getBiome(entity.blockPosition()).value();
        if (biome.getBaseTemperature() >= 1.5F && (!biome.hasPrecipitation() || !world.isRaining())
                && random.nextInt(level > 2 ? 20 : level > 1 ? 25 : 30) == 0) {
            entity.setSecondsOnFire(Math.min(1 + random.nextInt(level < 4 ? 2 : level - 1), 4));
        }
    }

    public static void spreadInsanity(ServerPlayer player, int level) {
        var random = player.serverLevel().getRandom();
        if (random.nextInt(level > 2 ? 25 : level > 1 ? 30 : 35) == 0) {
            Illusion.spawnNear(player);
        } else if (level >= 4 && random.nextInt(20) == 0) {
            String sound = random.nextInt(3) == 1 ? "mob.enderman.idle" : "random.explode";
            WitcheryNetwork.sendSound(player, sound, player.getX(), player.getY(), player.getZ(), 1, 1);
        }
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
