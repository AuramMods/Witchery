package art.arcane.witchery.item;

import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.vampirism.Vampirism;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CaneSwordItem extends GuardingSwordItem {
    private static final String BLADE_DEPLOYED = "WITCBladeDeployed";
    private static final float ATTACK_SPEED = -2.4F;
    private static final Multimap<Attribute, AttributeModifier> SHEATHED_MODIFIERS = ImmutableMultimap.of(
            Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 1, AttributeModifier.Operation.ADDITION),
            Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", ATTACK_SPEED, AttributeModifier.Operation.ADDITION));

    public CaneSwordItem() {
        super(Tiers.DIAMOND, 4, ATTACK_SPEED, new Properties().rarity(Rarity.RARE));
    }

    public static boolean isDrawn(ItemStack stack) {
        return stack.getItem() instanceof CaneSwordItem && stack.getTag() != null && stack.getTag().getBoolean(BLADE_DEPLOYED);
    }

    @Override
    public int getEnchantmentValue() {
        return Tiers.IRON.getEnchantmentValue();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND && !isDrawn(stack)) return SHEATHED_MODIFIERS;
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean drawn = isDrawn(stack);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                stack.getOrCreateTag().putBoolean(BLADE_DEPLOYED, !drawn);
                player.getInventory().setChanged();
                level.playSound(null, player.blockPosition(),
                        drawn ? WitcherySounds.SWORD_SHEATHE.get() : WitcherySounds.SWORD_DRAW.get(), SoundSource.PLAYERS, 1, 1);
            }
        } else if (drawn) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        } else if (player instanceof ServerPlayer serverPlayer) {
            if (Vampirism.drinkCaneReserve(serverPlayer)) GlassGobletItem.bloodParticles(serverPlayer.serverLevel(), player);
            else serverPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 1, 1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isDrawn(stack) ? UseAnim.BLOCK : UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return isDrawn(stack) ? 72_000 : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.witchery.canesword.controls"));
        lines.add(Component.translatable("item.witchery.canesword.siphon"));
    }
}
