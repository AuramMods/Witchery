package art.arcane.witchery.item;

import art.arcane.witchery.brewing.LousePotion;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BitingBeltItem extends WitchClothingItem.Dyeable {
    private static final String FIRST_POTION = "WITCPotion";
    private static final String SECOND_POTION = "WITCPotion2";
    private static final List<String> SLOTS = List.of(FIRST_POTION, SECOND_POTION);

    public BitingBeltItem() {
        super(Style.BITING_BELT);
    }

    public static void charge(ItemStack belt, LousePotion potion) {
        var tag = belt.getOrCreateTag();
        if (!tag.contains(FIRST_POTION)) {
            tag.putInt(FIRST_POTION, potion.id());
        } else {
            if (tag.contains(SECOND_POTION)) tag.putInt(FIRST_POTION, tag.getInt(SECOND_POTION));
            tag.putInt(SECOND_POTION, potion.id());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        if (!stack.hasTag()) return;
        for (String slot : SLOTS) {
            LousePotion potion = LousePotion.byId(stack.getTag().getInt(slot));
            if (potion != null) tooltip.add(potion.tooltip());
        }
    }

    static boolean spendPotion(ServerPlayer player, DamageSource source, boolean regeneration) {
        ItemStack belt = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!(belt.getItem() instanceof BitingBeltItem) || !belt.hasTag()) return false;
        for (String slot : SLOTS) {
            LousePotion potion = LousePotion.byId(belt.getTag().getInt(slot));
            if (potion == null || potion.regenerates() != regeneration) continue;
            var dose = potion.effect();
            if (player.hasEffect(dose.getEffect())) continue;
            belt.removeTagKey(slot);
            LivingEntity target = potion.retaliates() && source.getEntity() instanceof LivingEntity attacker
                    ? attacker : player;
            target.addEffect(dose, player);
            player.getInventory().setChanged();
            return true;
        }
        return false;
    }
}
