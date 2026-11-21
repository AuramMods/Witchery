package art.arcane.witchery.brewing;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class LightAllergyEffect extends MobEffect {
    private final boolean sunlight;

    public LightAllergyEffect(boolean sunlight, int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.sunlight = sunlight;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return sunlight || duration % 20 == 4;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (!(target.level() instanceof ServerLevel level)) return;
        if (sunlight) {
            checkSunlight(level, target, amplifier);
        } else if (level.getMaxLocalRawBrightness(target.blockPosition()) < 2 + 2 * amplifier) {
            target.hurt(target.damageSources().fellOutOfWorld(), 1);
        }
    }

    private static void checkSunlight(ServerLevel level, LivingEntity target, int amplifier) {
        if (level.getDayTime() % 20 != 0 || !level.isDay() || target.isInWater()) return;
        float brightness = level.getLightLevelDependentMagicValue(BlockPos.containing(target.getX(), target.getEyeY(), target.getZ()));
        if (brightness <= 0.5F || level.random.nextFloat() >= brightness - 0.45F
                || !level.canSeeSky(target.blockPosition())) return;

        if (target instanceof Player) {
            target.hurt(target.damageSources().fellOutOfWorld(), amplifier >= 3 ? 2 : 1);
            return;
        }
        ItemStack helmet = target.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) {
            target.setSecondsOnFire(8);
        } else if (helmet.isDamageableItem()) {
            helmet.setDamageValue(helmet.getDamageValue() + level.random.nextInt(2));
            if (helmet.getDamageValue() >= helmet.getMaxDamage()) {
                target.broadcastBreakEvent(EquipmentSlot.HEAD);
                target.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
