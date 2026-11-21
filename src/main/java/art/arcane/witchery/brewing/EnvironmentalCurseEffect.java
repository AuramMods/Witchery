package art.arcane.witchery.brewing;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
                var motion = entity.getDeltaMovement();
                if (entity instanceof Player player) {
                    if (player.isInWater() && !player.onGround() && motion.y < -0.03) {
                        player.setDeltaMovement(motion.multiply(1, 1.5 + Math.min(0.05 * amplifier, 0.2), 1));
                    } else if (!player.isInWater() && !player.getAbilities().instabuild && player.getAbilities().flying) {
                        player.setDeltaMovement(motion.x, -0.2, motion.z);
                    }
                } else if (level.getGameTime() % 20 == 3 && entity.isInWater()) {
                    double strength = Math.min(0.1 * (amplifier + 2), 0.4);
                    entity.setDeltaMovement(motion.multiply(1, motion.y < 0 ? 1 + strength : 1 - strength, 1));
                }
            }
            case OVERHEATING -> {
                if (!level.isClientSide && level.getGameTime() % 5 == 3 && !entity.isOnFire() && !entity.isInWater()
                        && entity.getRandom().nextInt(amplifier > 1 ? 20 : amplifier > 0 ? 25 : 30) == 0
                        && level.getBiome(entity.blockPosition()).value().getBaseTemperature() >= 1.5F
                        && !level.isRainingAt(entity.blockPosition())) {
                    entity.setSecondsOnFire(Math.min(1 + entity.getRandom().nextInt(amplifier < 3 ? 2 : amplifier), 4));
                }
            }
            case INSANITY -> {
                if (entity instanceof ServerPlayer player) {
                    int chance = amplifier >= 2 ? 25 : amplifier == 1 ? 30 : 35;
                    if (player.getRandom().nextInt(chance) == 0) {
                        art.arcane.witchery.entity.Illusion.spawnNear(player);
                    } else if (amplifier >= 3 && player.getRandom().nextInt(20) == 0) {
                        player.playNotifySound(player.getRandom().nextInt(3) == 1 ? SoundEvents.ENDERMAN_AMBIENT
                                : SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1, 1);
                    }
                }
            }
        }
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
