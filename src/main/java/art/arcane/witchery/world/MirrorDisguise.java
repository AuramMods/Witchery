package art.arcane.witchery.world;

import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.power.AltarPower;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class MirrorDisguise {
    private static final int PLAYER_FORM = 4;

    private MirrorDisguise() {}

    public static String alias(Player player) {
        return WitcheryPlayerDataProvider.get(player).map(data -> data.getCreatureTypeOrdinal() == PLAYER_FORM
                ? data.getOtherPlayerSkin() : "").orElse("");
    }

    public static void use(ServerLevel level, BlockPos mirror, ServerPlayer player, ItemStack taglock) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            int form = data.getCreatureTypeOrdinal();
            if (form != 0 && form != PLAYER_FORM) return;
            String name = TaglockItem.boundPlayerName(taglock);
            if (name.isEmpty() || TaglockItem.isBoundTo(taglock, player)
                    || name.equalsIgnoreCase(player.getGameProfile().getName())) {
                if (!reveal(player)) WitcheryNetwork.sendMirrorDisguiseCue(player, false);
                return;
            }
            var altar = AltarPower.nearest(level, mirror);
            if (altar != null && altar.consumePower(4_000)) {
                apply(player, data, PLAYER_FORM, name);
            } else {
                WitcheryNetwork.sendMirrorDisguiseCue(player, false);
            }
        });
    }

    public static boolean reveal(ServerPlayer player) {
        var data = WitcheryPlayerDataProvider.get(player).resolve().orElse(null);
        if (data == null || data.getCreatureTypeOrdinal() != PLAYER_FORM) return false;
        apply(player, data, 0, "");
        return true;
    }

    private static void apply(ServerPlayer player, WitcheryPlayerData data, int form, String name) {
        WitcheryNetwork.sendMirrorDisguiseCue(player, true);
        data.setCreatureTypeOrdinal(form);
        data.setOtherPlayerSkin(name);
        data.bumpSyncRevision();
        player.refreshDimensions();
        WitcheryNetwork.sendPartialExtendedPlayerSync(player, data);
        WitcheryNetwork.broadcastPlayerStyle(player, data);
    }
}
