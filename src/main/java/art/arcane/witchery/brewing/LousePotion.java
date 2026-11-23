package art.arcane.witchery.brewing;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum LousePotion {
    REGENERATION(8193, Potions.REGENERATION, 900, 0),
    STRONG_REGENERATION(8225, Potions.STRONG_REGENERATION, 450, 1),
    LONG_REGENERATION(8257, Potions.LONG_REGENERATION, 2400, 0),
    SWIFTNESS(8194, Potions.SWIFTNESS, 3600, 0),
    STRONG_SWIFTNESS(8226, Potions.STRONG_SWIFTNESS, 1800, 1),
    LONG_SWIFTNESS(8258, Potions.LONG_SWIFTNESS, 9600, 0),
    FIRE_RESISTANCE(8227, Potions.FIRE_RESISTANCE, 3600, 0),
    LONG_FIRE_RESISTANCE(8259, Potions.LONG_FIRE_RESISTANCE, 9600, 0),
    POISON(8196, Potions.POISON, 900, 0),
    STRONG_POISON(8228, Potions.STRONG_POISON, 450, 1),
    LONG_POISON(8260, Potions.LONG_POISON, 2400, 0),
    HEALING(8261, Potions.HEALING, 1, 0),
    STRONG_HEALING(8229, Potions.STRONG_HEALING, 1, 1),
    NIGHT_VISION(8230, Potions.NIGHT_VISION, 3600, 0),
    LONG_NIGHT_VISION(8262, Potions.LONG_NIGHT_VISION, 9600, 0),
    WEAKNESS(8200, Potions.WEAKNESS, 1800, 0),
    LONG_WEAKNESS(8264, Potions.LONG_WEAKNESS, 4800, 0),
    STRENGTH(8201, Potions.STRENGTH, 3600, 0),
    STRONG_STRENGTH(8233, Potions.STRONG_STRENGTH, 1800, 1),
    LONG_STRENGTH(8265, Potions.LONG_STRENGTH, 9600, 0),
    SLOWNESS(8202, Potions.SLOWNESS, 1800, 0),
    LONG_SLOWNESS(8266, Potions.LONG_SLOWNESS, 4800, 0),
    HARMING(8268, Potions.HARMING, 1, 0),
    STRONG_HARMING(8236, Potions.STRONG_HARMING, 1, 1),
    INVISIBILITY(8206, Potions.INVISIBILITY, 3600, 0),
    LONG_INVISIBILITY(8270, Potions.LONG_INVISIBILITY, 9600, 0);

    public static final String TAG = "BitePotionEffect";
    private static final Map<Integer, LousePotion> BY_ID = new HashMap<>();
    private static final Map<Potion, LousePotion> BY_POTION = new HashMap<>();

    static {
        for (LousePotion value : values()) {
            BY_ID.put(value.id, value);
            BY_POTION.put(value.potion, value);
        }
        BY_ID.put(8232, WEAKNESS);
        BY_ID.put(8234, SLOWNESS);
        BY_ID.put(8238, INVISIBILITY);
    }

    private final int id;
    private final Potion potion;
    private final int duration;
    private final int amplifier;

    LousePotion(int id, Potion potion, int duration, int amplifier) {
        this.id = id;
        this.potion = potion;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Nullable
    public static LousePotion byId(int id) {
        return BY_ID.get(id);
    }

    @Nullable
    public static LousePotion fromBottle(ItemStack stack) {
        return stack.is(Items.POTION) ? BY_POTION.get(PotionUtils.getPotion(stack)) : null;
    }

    @Nullable
    public static LousePotion get(ItemStack stack) {
        return stack.hasTag() ? byId(stack.getTag().getInt(TAG)) : null;
    }

    public static void set(ItemStack stack, int id) {
        if (byId(id) != null) stack.getOrCreateTag().putInt(TAG, id);
        else stack.removeTagKey(TAG);
    }

    public int id() {
        return id;
    }

    public ItemStack bottle() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
    }

    public MobEffectInstance effect() {
        return new MobEffectInstance(potion.getEffects().get(0).getEffect(), duration, amplifier);
    }

    public Component tooltip() {
        var dose = effect();
        var line = Component.empty().append(Component.translatable(dose.getDescriptionId()).withStyle(ChatFormatting.GOLD));
        if (amplifier > 0) line.append(" ").append(Component.translatable("potion.potency." + amplifier));
        if (duration > 20) line.append(" [").append(MobEffectUtil.formatDuration(dose, 1)).append("]");
        return line;
    }

    public boolean regenerates() {
        return this == REGENERATION || this == STRONG_REGENERATION || this == LONG_REGENERATION;
    }

    public boolean retaliates() {
        var effect = potion.getEffects().get(0).getEffect();
        return effect == MobEffects.POISON || effect == MobEffects.MOVEMENT_SLOWDOWN || effect == MobEffects.WEAKNESS;
    }

    public void apply(LivingEntity target, LivingEntity source) {
        var dose = effect();
        if (dose.getEffect().isInstantenous()) {
            dose.getEffect().applyInstantenousEffect(source, source, target, amplifier, 1);
        } else {
            target.addEffect(dose, source);
        }
    }
}
