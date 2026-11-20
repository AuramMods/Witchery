package art.arcane.witchery.item;

import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class PoppetProtectionItem extends BoundPoppetItem {
    public PoppetProtectionItem() {
        super(Rarity.RARE);
    }

    public static void corrupt(ServerPlayer caster, LivingEntity target) {
        if (!(target instanceof ServerPlayer player)) return;
        if (useWard(player, WitcheryItems.getRequired("poppet_protection").get(), 350)) {
            VoodooPoppetItem.strikeCaster(caster);
            return;
        }
        Item ward = WitcheryItems.getRequired("voodoo_protection_poppet").get();
        for (int i = 0; i < 10; i++) {
            if (!useWard(player, ward, USE_LIMIT)) break;
        }
    }
}
