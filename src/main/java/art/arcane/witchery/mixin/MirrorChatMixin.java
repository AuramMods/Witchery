package art.arcane.witchery.mixin;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.world.MirrorDisguise;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MirrorChatMixin {
    @Shadow public ServerPlayer player;

    @ModifyArg(method = "broadcastChatMessage", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"),
            index = 2)
    private ChatType.Bound witchery$disguiseChatName(ChatType.Bound chat) {
        if (!WitcheryServerConfig.ALLOW_CHAT_MASQUERADING.get()) return chat;
        String alias = MirrorDisguise.alias(player);
        if (alias.isEmpty()) return chat;
        String name = player.getGameProfile().getName();
        Component reveal = Component.translatable("message.witchery.mirror.chat_reveal", alias, name)
                .withStyle(ChatFormatting.GOLD);
        for (ServerPlayer viewer : player.serverLevel().players()) {
            if (viewer.isCreative() && player.server.getPlayerList().isOp(viewer.getGameProfile())) {
                viewer.sendSystemMessage(reveal);
            }
        }
        Component displayed = PlayerTeam.formatNameForTeam(player.getTeam(), Component.literal(alias))
                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + name + " ")));
        return new ChatType.Bound(chat.chatType(), displayed, chat.targetName());
    }
}
