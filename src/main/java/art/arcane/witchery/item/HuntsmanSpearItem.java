package art.arcane.witchery.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.UUID;

public final class HuntsmanSpearItem extends SwordItem {
    private static final UUID RESIST_KNOCKBACK = UUID.fromString("032f4b80-ad10-11e3-a5e2-0800200c9a66");
    private final Multimap<Attribute, AttributeModifier> attributes;

    public HuntsmanSpearItem() {
        super(Tiers.DIAMOND, 5, -2.4F, new Properties().rarity(Rarity.EPIC));
        var mainHand = super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
        attributes = ImmutableMultimap.<Attribute, AttributeModifier>builder().putAll(mainHand)
                .put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(RESIST_KNOCKBACK, "Huntsman's Spear",
                        1, AttributeModifier.Operation.ADDITION)).build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? attributes : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BLOCK; }

    @Override
    public int getUseDuration(ItemStack stack) { return 72_000; }

    public static void defend(ServerPlayer wearer, LivingHurtEvent event) {
        if (!wearer.isUsingItem() || wearer.getUsedItemHand() != InteractionHand.MAIN_HAND
                || !(wearer.getUseItem().getItem() instanceof HuntsmanSpearItem)) return;
        DamageSource source = event.getSource();
        // Sword guards halved the incoming hit with one added before the division.
        if (event.getAmount() > 0 && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            event.setAmount((1 + event.getAmount()) * 0.5F);
        }
        if (!(source.getEntity() instanceof LivingEntity attacker) || !attacker.isAlive() || attacker == wearer
                || wearer.getRandom().nextDouble() >= 0.25) return;
        var world = wearer.serverLevel();
        var wolf = EntityType.WOLF.create(world);
        if (wolf == null) return;
        wolf.moveTo(wearer.getX(), wearer.getY(), wearer.getZ(), wearer.getYRot(), 0);
        wolf.setTarget(attacker);
        wolf.setLastHurtByMob(attacker);
        wolf.startPersistentAngerTimer();
        wolf.setPersistentAngerTarget(attacker.getUUID());
        wolf.addEffect(new MobEffectInstance(MobEffects.WITHER, 12_000, 1));
        world.addFreshEntity(wolf);
    }
}
