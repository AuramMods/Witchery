package art.arcane.witchery.network;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.LegacyRegistryData;
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
        discriminator = registerNoPayloadPacket(discriminator, "player_sync", PlayerSyncPacket.class, PlayerSyncPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "push_target", PushTargetPacket.class, PushTargetPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "sound", SoundPacket.class, SoundPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "spell_prepared", SpellPreparedPacket.class, SpellPreparedPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "clear_fall_damage", ClearFallDamagePacket.class, ClearFallDamagePacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "sync_entity_size", SyncEntitySizePacket.class, SyncEntitySizePacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "sync_markup_book", SyncMarkupBookPacket.class, SyncMarkupBookPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "extended_player_sync", ExtendedPlayerSyncPacket.class, ExtendedPlayerSyncPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "howl", HowlPacket.class, HowlPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "extended_villager_sync", ExtendedVillagerSyncPacket.class, ExtendedVillagerSyncPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "select_player_ability", SelectPlayerAbilityPacket.class, SelectPlayerAbilityPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "extended_entity_request_sync_to_client", ExtendedEntityRequestSyncToClientPacket.class, ExtendedEntityRequestSyncToClientPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "partial_extended_player_sync", PartialExtendedPlayerSyncPacket.class, PartialExtendedPlayerSyncPacket::new);
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

    private static PacketBinding packetBinding(String intentKey) {
        String normalized = intentKey.toLowerCase();
        PacketBinding binding = PACKET_BINDINGS_BY_KEY.get(normalized);
        if (binding == null) {
            throw new IllegalArgumentException("Unknown legacy packet intent: " + intentKey);
        }
        return binding;
    }

    private static <T> int registerNoPayloadPacket(int discriminator, String key, Class<T> packetType, Supplier<T> factory) {
        LegacyRegistryData.LegacyPacketIntent intent = PACKET_INTENTS_BY_KEY.get(key);
        if (intent == null) {
            throw new IllegalStateException("Missing legacy packet intent metadata for key: " + key);
        }

        PacketBinding previous = PACKET_BINDINGS_BY_KEY.putIfAbsent(key, new PacketBinding(intent, () -> factory.get()));
        if (previous != null) {
            throw new IllegalStateException("Duplicate packet binding for key: " + key);
        }

        CHANNEL.messageBuilder(packetType, discriminator)
                .encoder((message, buffer) -> {
                })
                .decoder(buffer -> factory.get())
                .consumerMainThread((message, contextSupplier) -> handleTypedPacket(intent, contextSupplier))
                .add();

        return discriminator + 1;
    }

    private static void handleTypedPacket(LegacyRegistryData.LegacyPacketIntent intent, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
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

    public record PlayerSyncPacket() {
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

    public record ExtendedPlayerSyncPacket() {
    }

    public record HowlPacket() {
    }

    public record ExtendedVillagerSyncPacket() {
    }

    public record SelectPlayerAbilityPacket() {
    }

    public record ExtendedEntityRequestSyncToClientPacket() {
    }

    public record PartialExtendedPlayerSyncPacket() {
    }

    public record SetClientPlayerFacingPacket() {
    }
}
