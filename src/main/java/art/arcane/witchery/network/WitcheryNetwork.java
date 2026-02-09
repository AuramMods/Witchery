package art.arcane.witchery.network;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.LegacyRegistryData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class WitcheryNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.parse(Witchery.MODID + ":main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static final Map<String, LegacyRegistryData.LegacyPacketIntent> PACKET_INTENTS_BY_KEY = new LinkedHashMap<>();
    private static boolean registered;

    static {
        for (LegacyRegistryData.LegacyPacketIntent packet : LegacyRegistryData.PACKETS) {
            LegacyRegistryData.LegacyPacketIntent previous = PACKET_INTENTS_BY_KEY.putIfAbsent(packet.key(), packet);
            if (previous != null) {
                throw new IllegalStateException("Duplicate legacy packet intent key: " + packet.key());
            }
        }
    }

    private WitcheryNetwork() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        CHANNEL.messageBuilder(LegacyIntentPacket.class, 0)
                .encoder(LegacyIntentPacket::encode)
                .decoder(LegacyIntentPacket::decode)
                .consumerMainThread(WitcheryNetwork::handleIntentPacket)
                .add();
    }

    public static Collection<LegacyRegistryData.LegacyPacketIntent> intents() {
        return Collections.unmodifiableCollection(PACKET_INTENTS_BY_KEY.values());
    }

    public static LegacyIntentPacket packet(String intentKey) {
        String normalized = intentKey.toLowerCase();
        LegacyRegistryData.LegacyPacketIntent intent = PACKET_INTENTS_BY_KEY.get(normalized);
        if (intent == null) {
            throw new IllegalArgumentException("Unknown legacy packet intent: " + intentKey);
        }
        return new LegacyIntentPacket(intent.key());
    }

    public static void sendToServer(String intentKey) {
        CHANNEL.sendToServer(packet(intentKey));
    }

    public static void sendTo(ServerPlayer player, String intentKey) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet(intentKey));
    }

    private static void handleIntentPacket(LegacyIntentPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            LegacyRegistryData.LegacyPacketIntent intent = PACKET_INTENTS_BY_KEY.get(message.intentKey());
            if (intent == null) {
                Witchery.LOGGER.warn("Received unknown legacy packet intent '{}'", message.intentKey());
                return;
            }

            NetworkDirection direction = context.getDirection();
            if (!matchesDirection(intent, direction)) {
                Witchery.LOGGER.warn("Legacy packet intent '{}' received from unexpected direction {}", intent.key(), direction);
            }
        });
        context.setPacketHandled(true);
    }

    private static boolean matchesDirection(LegacyRegistryData.LegacyPacketIntent intent, NetworkDirection direction) {
        return switch (intent.flow()) {
            case CLIENTBOUND -> direction == NetworkDirection.PLAY_TO_CLIENT;
            case SERVERBOUND -> direction == NetworkDirection.PLAY_TO_SERVER;
        };
    }

    public record LegacyIntentPacket(String intentKey) {
        private static final int MAX_KEY_LENGTH = 96;

        public static void encode(LegacyIntentPacket message, FriendlyByteBuf buffer) {
            buffer.writeUtf(message.intentKey, MAX_KEY_LENGTH);
        }

        public static LegacyIntentPacket decode(FriendlyByteBuf buffer) {
            return new LegacyIntentPacket(buffer.readUtf(MAX_KEY_LENGTH));
        }
    }
}
