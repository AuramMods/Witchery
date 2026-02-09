package art.arcane.witchery.network;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerData;
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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
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
    private static final Map<String, PacketBinding> PACKET_BINDINGS_BY_KEY = new LinkedHashMap<>();
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

        int discriminator = 0;
        discriminator = registerNoPayloadPacket(discriminator, "brew_prepared", BrewPreparedPacket.class, BrewPreparedPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "particles", ParticlesPacket.class, ParticlesPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "cam_pos", CamPosPacket.class, CamPosPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "item_update", ItemUpdatePacket.class, ItemUpdatePacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "player_style", PlayerStylePacket.class, PlayerStylePacket::new);
        discriminator = registerPlayerSyncPacket(discriminator);
        discriminator = registerNoPayloadPacket(discriminator, "push_target", PushTargetPacket.class, PushTargetPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "sound", SoundPacket.class, SoundPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "spell_prepared", SpellPreparedPacket.class, SpellPreparedPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "clear_fall_damage", ClearFallDamagePacket.class, ClearFallDamagePacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "sync_entity_size", SyncEntitySizePacket.class, SyncEntitySizePacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "sync_markup_book", SyncMarkupBookPacket.class, SyncMarkupBookPacket::new);
        discriminator = registerExtendedPlayerSyncPacket(discriminator);
        discriminator = registerNoPayloadPacket(discriminator, "howl", HowlPacket.class, HowlPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "extended_villager_sync", ExtendedVillagerSyncPacket.class, ExtendedVillagerSyncPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "select_player_ability", SelectPlayerAbilityPacket.class, SelectPlayerAbilityPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "extended_entity_request_sync_to_client", ExtendedEntityRequestSyncToClientPacket.class, ExtendedEntityRequestSyncToClientPacket::new);
        discriminator = registerPartialExtendedPlayerSyncPacket(discriminator);
        registerNoPayloadPacket(discriminator, "set_client_player_facing", SetClientPlayerFacingPacket.class, SetClientPlayerFacingPacket::new);

        if (PACKET_BINDINGS_BY_KEY.size() != PACKET_INTENTS_BY_KEY.size()) {
            throw new IllegalStateException("Packet binding mismatch. expected=" + PACKET_INTENTS_BY_KEY.size() + " actual=" + PACKET_BINDINGS_BY_KEY.size());
        }
    }

    public static Collection<LegacyRegistryData.LegacyPacketIntent> intents() {
        return Collections.unmodifiableCollection(PACKET_INTENTS_BY_KEY.values());
    }

    public static Object packet(String intentKey) {
        return packetBinding(intentKey).factory().get();
    }

    public static void sendToServer(String intentKey) {
        PacketBinding binding = packetBinding(intentKey);
        if (binding.intent().flow() != LegacyRegistryData.PacketFlow.SERVERBOUND) {
            Witchery.LOGGER.warn("Sending packet intent '{}' to server but legacy flow is {}", binding.intent().key(), binding.intent().flow());
        }
        CHANNEL.sendToServer(binding.factory().get());
    }

    public static void sendTo(ServerPlayer player, String intentKey) {
        PacketBinding binding = packetBinding(intentKey);
        if (binding.intent().flow() != LegacyRegistryData.PacketFlow.CLIENTBOUND) {
            Witchery.LOGGER.warn("Sending packet intent '{}' to client but legacy flow is {}", binding.intent().key(), binding.intent().flow());
        }
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), binding.factory().get());
    }

    public static void sendExtendedPlayerSync(ServerPlayer player, WitcheryPlayerData data) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new ExtendedPlayerSyncPacket(player.getUUID(), data.isInitialized(), data.getSyncRevision())
        );
    }

    public static void sendPartialExtendedPlayerSync(ServerPlayer player, WitcheryPlayerData data) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new PartialExtendedPlayerSyncPacket(player.getUUID(), data.getSyncRevision())
        );
    }

    public static void sendPlayerSync(ServerPlayer player, WitcheryPlayerData data) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new PlayerSyncPacket(player.getUUID(), data.getSyncRevision())
        );
    }

    private static PacketBinding packetBinding(String intentKey) {
        String normalized = normalize(intentKey);
        PacketBinding binding = PACKET_BINDINGS_BY_KEY.get(normalized);
        if (binding == null) {
            throw new IllegalArgumentException("Unknown legacy packet intent: " + intentKey);
        }
        return binding;
    }

    private static LegacyRegistryData.LegacyPacketIntent packetIntent(String key) {
        LegacyRegistryData.LegacyPacketIntent intent = PACKET_INTENTS_BY_KEY.get(key);
        if (intent == null) {
            throw new IllegalStateException("Missing legacy packet intent metadata for key: " + key);
        }
        return intent;
    }

    private static int registerPlayerSyncPacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "player_sync",
                PlayerSyncPacket.class,
                PlayerSyncPacket::encode,
                PlayerSyncPacket::decode,
                WitcheryNetwork::handlePlayerSyncPacket,
                PlayerSyncPacket::placeholder
        );
    }

    private static int registerExtendedPlayerSyncPacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "extended_player_sync",
                ExtendedPlayerSyncPacket.class,
                ExtendedPlayerSyncPacket::encode,
                ExtendedPlayerSyncPacket::decode,
                WitcheryNetwork::handleExtendedPlayerSyncPacket,
                ExtendedPlayerSyncPacket::placeholder
        );
    }

    private static int registerPartialExtendedPlayerSyncPacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "partial_extended_player_sync",
                PartialExtendedPlayerSyncPacket.class,
                PartialExtendedPlayerSyncPacket::encode,
                PartialExtendedPlayerSyncPacket::decode,
                WitcheryNetwork::handlePartialExtendedPlayerSyncPacket,
                PartialExtendedPlayerSyncPacket::placeholder
        );
    }

    private static <T> int registerNoPayloadPacket(int discriminator, String key, Class<T> packetType, Supplier<T> factory) {
        return registerPacketWithCodec(
                discriminator,
                key,
                packetType,
                (message, buffer) -> {
                },
                buffer -> factory.get(),
                (message, context) -> {
                },
                factory
        );
    }

    private static <T> int registerPacketWithCodec(
            int discriminator,
            String key,
            Class<T> packetType,
            BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, NetworkEvent.Context> onValidDirection,
            Supplier<T> placeholderFactory
    ) {
        String normalized = normalize(key);
        LegacyRegistryData.LegacyPacketIntent intent = packetIntent(normalized);

        PacketBinding previous = PACKET_BINDINGS_BY_KEY.putIfAbsent(normalized, new PacketBinding(intent, () -> placeholderFactory.get()));
        if (previous != null) {
            throw new IllegalStateException("Duplicate packet binding for key: " + normalized);
        }

        CHANNEL.messageBuilder(packetType, discriminator)
                .encoder(encoder)
                .decoder(decoder)
                .consumerMainThread((message, contextSupplier) ->
                        handleTypedPacket(intent, contextSupplier, context -> onValidDirection.accept(message, context)))
                .add();

        return discriminator + 1;
    }

    private static void handleTypedPacket(
            LegacyRegistryData.LegacyPacketIntent intent,
            Supplier<NetworkEvent.Context> contextSupplier,
            Consumer<NetworkEvent.Context> onValidDirection
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            NetworkDirection direction = context.getDirection();
            if (!matchesDirection(intent, direction)) {
                Witchery.LOGGER.warn("Legacy packet intent '{}' received from unexpected direction {}", intent.key(), direction);
                return;
            }
            onValidDirection.accept(context);
        });
        context.setPacketHandled(true);
    }

    private static void handlePlayerSyncPacket(PlayerSyncPacket message, NetworkEvent.Context context) {
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'player_sync' player={} revision={} direction={}",
                message.playerId(), message.syncRevision(), context.getDirection()
        );
    }

    private static void handleExtendedPlayerSyncPacket(ExtendedPlayerSyncPacket message, NetworkEvent.Context context) {
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'extended_player_sync' player={} initialized={} revision={} direction={}",
                message.playerId(), message.initialized(), message.syncRevision(), context.getDirection()
        );
    }

    private static void handlePartialExtendedPlayerSyncPacket(PartialExtendedPlayerSyncPacket message, NetworkEvent.Context context) {
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'partial_extended_player_sync' player={} revision={} direction={}",
                message.playerId(), message.syncRevision(), context.getDirection()
        );
    }

    private static boolean matchesDirection(LegacyRegistryData.LegacyPacketIntent intent, NetworkDirection direction) {
        return switch (intent.flow()) {
            case CLIENTBOUND -> direction == NetworkDirection.PLAY_TO_CLIENT;
            case SERVERBOUND -> direction == NetworkDirection.PLAY_TO_SERVER;
        };
    }

    private static String normalize(String key) {
        return key.toLowerCase(Locale.ROOT);
    }

    private record PacketBinding(LegacyRegistryData.LegacyPacketIntent intent, Supplier<Object> factory) {
    }

    public record BrewPreparedPacket() {
    }

    public record ParticlesPacket() {
    }

    public record CamPosPacket() {
    }

    public record ItemUpdatePacket() {
    }

    public record PlayerStylePacket() {
    }

    public record PlayerSyncPacket(UUID playerId, int syncRevision) {
        private static final UUID PLACEHOLDER_PLAYER = new UUID(0L, 0L);

        public static PlayerSyncPacket placeholder() {
            return new PlayerSyncPacket(PLACEHOLDER_PLAYER, 0);
        }

        public static void encode(PlayerSyncPacket message, FriendlyByteBuf buffer) {
            buffer.writeUUID(message.playerId);
            buffer.writeVarInt(message.syncRevision);
        }

        public static PlayerSyncPacket decode(FriendlyByteBuf buffer) {
            return new PlayerSyncPacket(buffer.readUUID(), buffer.readVarInt());
        }
    }

    public record PushTargetPacket() {
    }

    public record SoundPacket() {
    }

    public record SpellPreparedPacket() {
    }

    public record ClearFallDamagePacket() {
    }

    public record SyncEntitySizePacket() {
    }

    public record SyncMarkupBookPacket() {
    }

    public record ExtendedPlayerSyncPacket(UUID playerId, boolean initialized, int syncRevision) {
        private static final UUID PLACEHOLDER_PLAYER = new UUID(0L, 0L);

        public static ExtendedPlayerSyncPacket placeholder() {
            return new ExtendedPlayerSyncPacket(PLACEHOLDER_PLAYER, false, 0);
        }

        public static void encode(ExtendedPlayerSyncPacket message, FriendlyByteBuf buffer) {
            buffer.writeUUID(message.playerId);
            buffer.writeBoolean(message.initialized);
            buffer.writeVarInt(message.syncRevision);
        }

        public static ExtendedPlayerSyncPacket decode(FriendlyByteBuf buffer) {
            return new ExtendedPlayerSyncPacket(buffer.readUUID(), buffer.readBoolean(), buffer.readVarInt());
        }
    }

    public record HowlPacket() {
    }

    public record ExtendedVillagerSyncPacket() {
    }

    public record SelectPlayerAbilityPacket() {
    }

    public record ExtendedEntityRequestSyncToClientPacket() {
    }

    public record PartialExtendedPlayerSyncPacket(UUID playerId, int syncRevision) {
        private static final UUID PLACEHOLDER_PLAYER = new UUID(0L, 0L);

        public static PartialExtendedPlayerSyncPacket placeholder() {
            return new PartialExtendedPlayerSyncPacket(PLACEHOLDER_PLAYER, 0);
        }

        public static void encode(PartialExtendedPlayerSyncPacket message, FriendlyByteBuf buffer) {
            buffer.writeUUID(message.playerId);
            buffer.writeVarInt(message.syncRevision);
        }

        public static PartialExtendedPlayerSyncPacket decode(FriendlyByteBuf buffer) {
            return new PartialExtendedPlayerSyncPacket(buffer.readUUID(), buffer.readVarInt());
        }
    }

    public record SetClientPlayerFacingPacket() {
    }
}
