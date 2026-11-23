package art.arcane.witchery.item;

import art.arcane.witchery.infusion.ImpContractEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ImpSpellContractItem extends Item {
    private final Spell spell;

    public ImpSpellContractItem(Spell spell) {
        super(new Properties());
        this.spell = spell;
    }

    @Nullable
    public static Spell spellFor(ItemStack stack) {
        if (stack.getItem() instanceof ImpSpellContractItem contract) return contract.spell;
        for (Spell spell : Spell.values()) {
            if (spell.ingredient.matches(stack)) return spell;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        OwnershipContractItem.appendSignature(stack, tooltip);
    }

    public enum Spell {
        LIVING_FLAME(WitcheryIngredient.CONTRACT_LIVING_FLAME),
        FIERY_TOLERANCE(WitcheryIngredient.CONTRACT_FIERY_TOLERANCE),
        EVAPORATION(WitcheryIngredient.CONTRACT_EVAPORATION),
        FIERY_TOUCH(WitcheryIngredient.CONTRACT_FIERY_TOUCH),
        MELTING_TOUCH(WitcheryIngredient.CONTRACT_MELTING_TOUCH);

        private final WitcheryIngredient ingredient;

        Spell(WitcheryIngredient ingredient) {
            this.ingredient = ingredient;
        }

        public Item item() {
            return ingredient.item();
        }

        public boolean activate(LivingEntity target) {
            return switch (this) {
                case LIVING_FLAME -> target.level() instanceof ServerLevel level && summonBlaze(level, target.blockPosition());
                case FIERY_TOLERANCE -> {
                    target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 18_000));
                    yield true;
                }
                case EVAPORATION -> grant(target, ImpContractEffects.EVAPORATION);
                case FIERY_TOUCH -> grant(target, ImpContractEffects.FIERY_TOUCH);
                case MELTING_TOUCH -> grant(target, ImpContractEffects.MELTING_TOUCH);
            };
        }
    }

    private static boolean grant(LivingEntity target, ImpContractEffects effect) {
        if (!(target instanceof ServerPlayer player)) return false;
        effect.grant(player);
        return true;
    }

    private static boolean summonBlaze(ServerLevel level, BlockPos center) {
        var random = level.getRandom();
        int x = random.nextInt(3) - 2;
        int z = random.nextInt(3) - 2;
        BlockPos feet = center.offset(x == 0 ? 2 : x, 0, z == 0 ? 2 : z);
        if (!level.isLoaded(feet)) return false;
        while (!level.isEmptyBlock(feet) && feet.getY() < center.getY() + 8) feet = feet.above();
        while (feet.getY() > level.getMinBuildHeight() && level.isEmptyBlock(feet.below())) feet = feet.below();
        if (feet.getY() <= level.getMinBuildHeight() || !level.isEmptyBlock(feet) || !level.isEmptyBlock(feet.above())) return false;
        var blaze = EntityType.BLAZE.create(level);
        if (blaze == null) return false;
        blaze.moveTo(feet.getX() + 0.5, feet.getY() + 0.05, feet.getZ() + 0.5, 0, 0);
        blaze.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50);
        blaze.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);
        if (!level.addFreshEntity(blaze)) return false;
        level.sendParticles(ParticleTypes.FLAME, blaze.getX(), blaze.getY(0.5), blaze.getZ(), 16, 0.4, 0.8, 0.4, 0.02);
        level.playSound(null, feet, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1, 1);
        return true;
    }
}
