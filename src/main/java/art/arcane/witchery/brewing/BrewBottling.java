package art.arcane.witchery.brewing;

import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;

public final class BrewBottling {
    private static final int[] PORTIONS = {3000, 1500, 1000, 750, 600, 500, 375, 300, 200, 100};

    private BrewBottling() {}

    public static int skill(ServerPlayer player) {
        return WitcheryPlayerDataProvider.get(player).map(data -> data.getPotionBottling()).orElse(0);
    }

    public static int portion(ServerPlayer player, BrewMixture mixture) {
        int yield = yieldLevel(player, skill(player) / 30);
        return PORTIONS[Mth.clamp(yield + mixture.yieldModifier(), 0, PORTIONS.length - 1)];
    }

    private static int yieldLevel(ServerPlayer player, int skillLevel) {
        if (skillLevel == 0) return 0;
        var hat = WitchClothingItem.style(player.getItemBySlot(EquipmentSlot.HEAD));
        var robe = WitchClothingItem.style(player.getItemBySlot(EquipmentSlot.CHEST));
        int gear = hat == WitchClothingItem.Style.BABA_HAT ? 2 : hat == WitchClothingItem.Style.WITCH_HAT ? 1 : 0;
        if (robe == WitchClothingItem.Style.WITCH_ROBE || robe == WitchClothingItem.Style.NECROMANCER_ROBE) gear++;
        var expertise = player.getEffect(WitcheryEffects.BREWING_EXPERTISE.get());
        int potion = expertise != null ? expertise.getAmplifier() + 1 : 0;
        if (skillLevel == 1) return gear > 0 || potion > 0 ? 2 : 1;
        if (gear >= 2 && potion >= 3) {
            if (!FamiliarBinding.hasBrewMastery(player)) return 7;
            return gear >= 3 ? 9 : 8;
        }
        if (gear >= 2 && potion >= 1) return potion >= 2 ? 6 : 5;
        if (gear < 2 && potion == 0) return gear == 0 ? 2 : 3;
        return 4;
    }

    public static void learn(ServerPlayer player, BrewMixture mixture) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            int current = data.getPotionBottling();
            int requiredLevels = Math.min(8, (current / 30 + 1) * 2);
            if (current >= 100 || mixture.used() < requiredLevels) return;
            int next = current + 1;
            data.setPotionBottling(next);
            if (next % 30 == 0) {
                player.sendSystemMessage(Component.translatable("message.witchery.brew.bottling_skill"));
            }
        });
    }
}
