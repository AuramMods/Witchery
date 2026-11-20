package art.arcane.witchery.block;

import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public enum DreamWeave implements StringRepresentable {
    MOVEMENT("movement", WitcheryIngredient.DREAMWEAVE_OF_FLEET_FEET, MobEffects.MOVEMENT_SPEED, MobEffects.MOVEMENT_SLOWDOWN, 7200, 17, 10),
    MINING("mining", WitcheryIngredient.DREAMWEAVE_OF_THE_IRON_ARM, MobEffects.DIG_SPEED, MobEffects.DIG_SLOWDOWN, 7200, 17, 4),
    HUNGER("hunger", WitcheryIngredient.DREAMWEAVE_OF_FASTING, MobEffects.SATURATION, MobEffects.HUNGER, 4800, 17, 16),
    NIGHTMARES("nightmares", WitcheryIngredient.DREAMWEAVE_OF_NIGHTMARES, MobEffects.WEAKNESS, MobEffects.BLINDNESS, 1200, 4, 4),
    INTENSITY("intensity", WitcheryIngredient.DREAMWEAVE_OF_INTENSITY, MobEffects.NIGHT_VISION, MobEffects.BLINDNESS, 300, 17, 22);

    private static final DreamWeave[] CHOICES = values();
    private final String name;
    private final WitcheryIngredient ingredient;
    private final MobEffect dream;
    private final MobEffect nightmare;
    private final int duration;
    private final int textureX;
    private final int textureY;

    DreamWeave(String name, WitcheryIngredient ingredient, MobEffect dream, MobEffect nightmare,
               int duration, int textureX, int textureY) {
        this.name = name;
        this.ingredient = ingredient;
        this.dream = dream;
        this.nightmare = nightmare;
        this.duration = duration;
        this.textureX = textureX;
        this.textureY = textureY;
    }

    public static DreamWeave fromLegacyId(int id) {
        return id >= 0 && id < CHOICES.length ? CHOICES[id] : MOVEMENT;
    }

    public int textureX() {
        return textureX;
    }

    public int textureY() {
        return textureY;
    }

    public Item item() {
        return ingredient.item();
    }

    public void apply(Player player, boolean rested, boolean intense) {
        MobEffect effect = rested ? dream : nightmare;
        int ticks = duration;
        int amplifier = 0;
        if (intense) {
            if (rested && dream == MobEffects.SATURATION) {
                ticks += 2400;
            } else {
                amplifier = 1;
                if (rested) ticks = Math.max(1, ticks - 2400);
            }
        }
        player.addEffect(new MobEffectInstance(effect, ticks, amplifier));
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
