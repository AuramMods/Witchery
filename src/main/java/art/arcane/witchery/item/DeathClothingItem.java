package art.arcane.witchery.item;

import art.arcane.witchery.ritual.DirectedCurse;
import art.arcane.witchery.client.model.DeathClothingModel;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.infusion.OtherwhereInfusion;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class DeathClothingItem extends ArmorItem {
    public DeathClothingItem(Type type) {
        super(ArmorMaterials.IRON, type,
                new Properties().durability(ArmorMaterials.DIAMOND.getDurabilityForType(type)).rarity(Rarity.EPIC));
    }

    public static boolean fullSet(LivingEntity wearer) {
        return wears(wearer, EquipmentSlot.HEAD) && wears(wearer, EquipmentSlot.CHEST)
                && wears(wearer, EquipmentSlot.FEET);
    }

    public static boolean armed(LivingEntity wearer) {
        return wearer.getMainHandItem().getItem() instanceof DeathHandItem && fullSet(wearer);
    }

    private static boolean wears(LivingEntity wearer, EquipmentSlot slot) {
        return wearer.getItemBySlot(slot).getItem() instanceof DeathClothingItem piece && piece.getEquipmentSlot() == slot;
    }

    public static void protect(Player wearer, LivingHurtEvent event) {
        if (event.getSource().is(DamageTypeTags.IS_FIRE) && wears(wearer, EquipmentSlot.CHEST)) {
            if (!wearer.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                wearer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100));
            }
            event.setCanceled(true);
        } else if (armed(wearer)) {
            event.setAmount(Math.min(event.getAmount(), 7));
        }
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player wearer) {
        if (getEquipmentSlot() == EquipmentSlot.FEET) {
            var motion = wearer.getDeltaMovement();
            if (motion.y < 0 && (wearer.isInWater() || wearer.isInLava())
                    && !wearer.hasEffect(WitcheryEffects.SINKING.get())
                    && DirectedCurse.SINKING.strength(wearer) <= 0) {
                wearer.setDeltaMovement(motion.add(0, 0.1, 0));
            }
        } else if (getEquipmentSlot() == EquipmentSlot.HEAD && wearer instanceof ServerPlayer player) {
            tickCowl(level, player);
        }
    }

    private static void tickCowl(Level level, ServerPlayer wearer) {
        long phase = level.getGameTime() % 10;
        if (phase == 1 && WitcheryServerConfig.ALLOW_DEATHS_HOOD_TO_FREEZE_VICTIMS.get()) {
            var result = OtherwhereInfusion.rayTrace(wearer, 16, entity -> entity instanceof LivingEntity);
            if (result instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity victim
                    && victim.hasLineOfSight(wearer) && !victim.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2), wearer);
            }
        } else if (phase == 2 && level.getMaxLocalRawBrightness(wearer.blockPosition()) < 8) {
            MobEffectInstance vision = wearer.getEffect(MobEffects.NIGHT_VISION);
            if (vision == null || vision.getDuration() <= 300) {
                wearer.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, true));
            }
        }
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity wearer, EquipmentSlot slot, @Nullable String type) {
        return "witchery:textures/entities/deathsclothes.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(DeathClothingModel.extension());
    }
}
