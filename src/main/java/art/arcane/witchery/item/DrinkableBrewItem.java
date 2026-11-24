package art.arcane.witchery.item;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.infusion.ImpContractEffects;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class DrinkableBrewItem extends Item {
    private final Consumer<LivingEntity> effect;
    private final UseAnim useAnimation;

    public DrinkableBrewItem(Supplier<MobEffect> effect, int duration, int amplifier) {
        this(drinker -> drinker.addEffect(new MobEffectInstance(effect.get(), duration, amplifier)));
    }

    public DrinkableBrewItem(Consumer<LivingEntity> effect) {
        this(new Properties(), UseAnim.DRINK, effect);
    }

    public DrinkableBrewItem(Rarity rarity, Consumer<LivingEntity> effect) {
        this(new Properties().rarity(rarity), UseAnim.DRINK, effect);
    }

    private DrinkableBrewItem(Properties properties, UseAnim animation, Consumer<LivingEntity> effect) {
        super(properties);
        this.useAnimation = animation;
        this.effect = effect;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return useAnimation;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity drinker) {
        if (drinker instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (!level.isClientSide) {
            effect.accept(drinker);
            drinker.gameEvent(useAnimation == UseAnim.EAT ? GameEvent.EAT : GameEvent.DRINK);
        }
        if (!(drinker instanceof Player player) || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return stack;
    }

    public static DrinkableBrewItem frozenHeart() {
        return new DrinkableBrewItem(new Properties().rarity(Rarity.UNCOMMON), UseAnim.EAT, drinker -> {
            drinker.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20));
            if (drinker instanceof ServerPlayer player) ImpContractEffects.clear(player);
        });
    }

    public static DrinkableBrewItem creeperHeart() {
        return new DrinkableBrewItem(new Properties().rarity(Rarity.UNCOMMON), UseAnim.EAT, eater -> {
            eater.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20));
            boolean destroysBlocks = WitcheryServerConfig.CREEPER_HEART_TERRAIN_DAMAGE.get();
            eater.level().explode(eater, eater.getX(), eater.getY(), eater.getZ(), destroysBlocks ? 3F : 1F,
                    destroysBlocks ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
        });
    }
}
