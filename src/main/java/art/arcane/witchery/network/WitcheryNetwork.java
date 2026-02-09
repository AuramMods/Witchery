package art.arcane.witchery.network;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.registry.LegacyRegistryData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.fml.DistExecutor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    private static final int MAX_USERNAME_LENGTH = 64;
    private static final int MAX_PLAYER_SKIN_LENGTH = 256;
    private static final int MAX_MARKUP_PAGE_COUNT = 256;
    private static final int MAX_MARKUP_PAGE_LENGTH = 2048;
    private static final String KEY_PERSISTENT_SPELL_EFFECT_ID = "WITCSpellEffectID";
    private static final String KEY_PERSISTENT_SPELL_EFFECT_LEVEL = "WITCSpellEffectEnhanced";
    private static final String KEY_PERSISTENT_LAST_HOWL_TICK = "WITCLastHowlTick";
    private static final String KEY_STACK_CURRENT_PAGE = "CurrentPage";
    private static final String KEY_STACK_PAGE_STACK = "pageStack";
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
        discriminator = registerParticlesPacket(discriminator);
        discriminator = registerCamPosPacket(discriminator);
        discriminator = registerItemUpdatePacket(discriminator);
        discriminator = registerPlayerStylePacket(discriminator);
        discriminator = registerPlayerSyncPacket(discriminator);
        discriminator = registerPushTargetPacket(discriminator);
        discriminator = registerSoundPacket(discriminator);
        discriminator = registerSpellPreparedPacket(discriminator);
        discriminator = registerClearFallDamagePacket(discriminator);
        discriminator = registerSyncEntitySizePacket(discriminator);
        discriminator = registerSyncMarkupBookPacket(discriminator);
        discriminator = registerExtendedPlayerSyncPacket(discriminator);
        discriminator = registerHowlPacket(discriminator);
        discriminator = registerNoPayloadPacket(discriminator, "extended_villager_sync", ExtendedVillagerSyncPacket.class, ExtendedVillagerSyncPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "select_player_ability", SelectPlayerAbilityPacket.class, SelectPlayerAbilityPacket::new);
        discriminator = registerNoPayloadPacket(discriminator, "extended_entity_request_sync_to_client", ExtendedEntityRequestSyncToClientPacket.class, ExtendedEntityRequestSyncToClientPacket::new);
        discriminator = registerPartialExtendedPlayerSyncPacket(discriminator);
        registerSetClientPlayerFacingPacket(discriminator);

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

    public static void sendItemUpdateToServer(int slotIndex, int damageValue, int pageIndex) {
        CHANNEL.sendToServer(new ItemUpdatePacket(slotIndex, damageValue, pageIndex));
    }

    public static void sendSyncEntitySize(ServerPlayer player, int entityId, float width, float height) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncEntitySizePacket(entityId, width, height)
        );
    }

    public static void sendSetClientPlayerFacing(ServerPlayer player, float yaw, float pitch) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SetClientPlayerFacingPacket(yaw, pitch)
        );
    }

    public static void sendCamPos(ServerPlayer player, boolean active, boolean updatePosition, int entityId) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new CamPosPacket(active, updatePosition, entityId)
        );
    }

    public static void sendPushTarget(ServerPlayer player, int entityId, double velocityX, double velocityY, double velocityZ) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new PushTargetPacket(entityId, velocityX, velocityY, velocityZ)
        );
    }

    public static void sendSound(ServerPlayer player, String soundId, double x, double y, double z, float volume, float pitch) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SoundPacket(soundId, x, y, z, volume, pitch)
        );
    }

    public static void sendParticles(
            ServerPlayer player,
            int particleEffectId,
            int soundEffectId,
            double x,
            double y,
            double z,
            double width,
            double height,
            int color
    ) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new ParticlesPacket(particleEffectId, soundEffectId, x, y, z, width, height, color)
        );
    }

    public static void sendPlayerStyle(
            ServerPlayer player,
            String username,
            int grotesqueTicks,
            int nightmare,
            boolean ghost,
            int creatureType,
            int blood,
            String playerSkin
    ) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new PlayerStylePacket(username, grotesqueTicks, nightmare, ghost, creatureType, blood, playerSkin)
        );
    }

    public static void sendSpellPreparedToServer(int effectId, int level) {
        CHANNEL.sendToServer(new SpellPreparedPacket(effectId, level));
    }

    public static void sendClearFallDamageToServer() {
        CHANNEL.sendToServer(ClearFallDamagePacket.placeholder());
    }

    public static void sendSyncMarkupBookToServer(int slot, List<String> pages) {
        CHANNEL.sendToServer(new SyncMarkupBookPacket(slot, pages == null ? List.of() : pages));
    }

    public static void sendHowlToServer() {
        CHANNEL.sendToServer(HowlPacket.placeholder());
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

    private static int registerCamPosPacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "cam_pos",
                CamPosPacket.class,
                CamPosPacket::encode,
                CamPosPacket::decode,
                WitcheryNetwork::handleCamPosPacket,
                CamPosPacket::placeholder
        );
    }

    private static int registerItemUpdatePacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "item_update",
                ItemUpdatePacket.class,
                ItemUpdatePacket::encode,
                ItemUpdatePacket::decode,
                WitcheryNetwork::handleItemUpdatePacket,
                ItemUpdatePacket::placeholder
        );
    }

    private static int registerParticlesPacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "particles",
                ParticlesPacket.class,
                ParticlesPacket::encode,
                ParticlesPacket::decode,
                WitcheryNetwork::handleParticlesPacket,
                ParticlesPacket::placeholder
        );
    }

    private static int registerPlayerStylePacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "player_style",
                PlayerStylePacket.class,
                PlayerStylePacket::encode,
                PlayerStylePacket::decode,
                WitcheryNetwork::handlePlayerStylePacket,
                PlayerStylePacket::placeholder
        );
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

    private static int registerSyncEntitySizePacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "sync_entity_size",
                SyncEntitySizePacket.class,
                SyncEntitySizePacket::encode,
                SyncEntitySizePacket::decode,
                WitcheryNetwork::handleSyncEntitySizePacket,
                SyncEntitySizePacket::placeholder
        );
    }

    private static int registerSetClientPlayerFacingPacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "set_client_player_facing",
                SetClientPlayerFacingPacket.class,
                SetClientPlayerFacingPacket::encode,
                SetClientPlayerFacingPacket::decode,
                WitcheryNetwork::handleSetClientPlayerFacingPacket,
                SetClientPlayerFacingPacket::placeholder
        );
    }

    private static int registerPushTargetPacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "push_target",
                PushTargetPacket.class,
                PushTargetPacket::encode,
                PushTargetPacket::decode,
                WitcheryNetwork::handlePushTargetPacket,
                PushTargetPacket::placeholder
        );
    }

    private static int registerSoundPacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "sound",
                SoundPacket.class,
                SoundPacket::encode,
                SoundPacket::decode,
                WitcheryNetwork::handleSoundPacket,
                SoundPacket::placeholder
        );
    }

    private static int registerSpellPreparedPacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "spell_prepared",
                SpellPreparedPacket.class,
                SpellPreparedPacket::encode,
                SpellPreparedPacket::decode,
                WitcheryNetwork::handleSpellPreparedPacket,
                SpellPreparedPacket::placeholder
        );
    }

    private static int registerClearFallDamagePacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "clear_fall_damage",
                ClearFallDamagePacket.class,
                ClearFallDamagePacket::encode,
                ClearFallDamagePacket::decode,
                WitcheryNetwork::handleClearFallDamagePacket,
                ClearFallDamagePacket::placeholder
        );
    }

    private static int registerSyncMarkupBookPacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "sync_markup_book",
                SyncMarkupBookPacket.class,
                SyncMarkupBookPacket::encode,
                SyncMarkupBookPacket::decode,
                WitcheryNetwork::handleSyncMarkupBookPacket,
                SyncMarkupBookPacket::placeholder
        );
    }

    private static int registerHowlPacket(int discriminator) {
        return registerPacketWithCodec(
                discriminator,
                "howl",
                HowlPacket.class,
                HowlPacket::encode,
                HowlPacket::decode,
                WitcheryNetwork::handleHowlPacket,
                HowlPacket::placeholder
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
        runClient(() -> art.arcane.witchery.client.packet.WitcheryClientPacketHandlers.handlePlayerSync(message));
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'player_sync' player={} revision={} direction={}",
                message.playerId(), message.syncRevision(), context.getDirection()
        );
    }

    private static void handleCamPosPacket(CamPosPacket message, NetworkEvent.Context context) {
        runClient(() -> art.arcane.witchery.client.packet.WitcheryClientPacketHandlers.handleCamPos(message));
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'cam_pos' active={} updatePosition={} entityId={} direction={}",
                message.active(), message.updatePosition(), message.entityId(), context.getDirection()
        );
    }

    private static void handleItemUpdatePacket(ItemUpdatePacket message, NetworkEvent.Context context) {
        mutateSenderData(context, "item_update", (sender, data) -> {
            int slot = message.slotIndex();
            if (slot < 0 || slot >= sender.getInventory().getContainerSize()) {
                Witchery.LOGGER.debug("Ignoring scaffold packet 'item_update' with out-of-range slot={} player={}", slot, sender.getScoreboardName());
                return;
            }

            ItemStack stack = sender.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                Witchery.LOGGER.debug("Ignoring scaffold packet 'item_update' for empty slot={} player={}", slot, sender.getScoreboardName());
                return;
            }

            if (stack.getDamageValue() != message.damageValue()) {
                Witchery.LOGGER.debug(
                        "Ignoring scaffold packet 'item_update' with damage mismatch slot={} expected={} actual={} player={}",
                        slot, message.damageValue(), stack.getDamageValue(), sender.getScoreboardName()
                );
                return;
            }

            int clampedPage = Math.max(0, Math.min(message.pageIndex(), 999));
            stack.getOrCreateTag().putInt(KEY_STACK_CURRENT_PAGE, clampedPage);
            sender.getInventory().setChanged();
            sender.containerMenu.broadcastChanges();

            data.applyItemUpdate(slot, message.damageValue(), clampedPage);
            data.bumpSyncRevision();
            sendPlayerSync(sender, data);
        });
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'item_update' slot={} damage={} page={} direction={}",
                message.slotIndex(), message.damageValue(), message.pageIndex(), context.getDirection()
        );
    }

    private static void handleParticlesPacket(ParticlesPacket message, NetworkEvent.Context context) {
        runClient(() -> art.arcane.witchery.client.packet.WitcheryClientPacketHandlers.handleParticles(message));
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'particles' particle={} sound={} x={} y={} z={} width={} height={} color={} direction={}",
                message.particleEffectId(),
                message.soundEffectId(),
                message.x(),
                message.y(),
                message.z(),
                message.width(),
                message.height(),
                message.color(),
                context.getDirection()
        );
    }

    private static void handlePlayerStylePacket(PlayerStylePacket message, NetworkEvent.Context context) {
        runClient(() -> art.arcane.witchery.client.packet.WitcheryClientPacketHandlers.handlePlayerStyle(message));
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'player_style' username={} grotesqueTicks={} nightmare={} ghost={} creatureType={} blood={} skin={} direction={}",
                message.username(),
                message.grotesqueTicks(),
                message.nightmare(),
                message.ghost(),
                message.creatureType(),
                message.blood(),
                message.playerSkin(),
                context.getDirection()
        );
    }

    private static void handleExtendedPlayerSyncPacket(ExtendedPlayerSyncPacket message, NetworkEvent.Context context) {
        runClient(() -> art.arcane.witchery.client.packet.WitcheryClientPacketHandlers.handleExtendedPlayerSync(message));
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'extended_player_sync' player={} initialized={} revision={} direction={}",
                message.playerId(), message.initialized(), message.syncRevision(), context.getDirection()
        );
    }

    private static void handlePartialExtendedPlayerSyncPacket(PartialExtendedPlayerSyncPacket message, NetworkEvent.Context context) {
        runClient(() -> art.arcane.witchery.client.packet.WitcheryClientPacketHandlers.handlePartialExtendedPlayerSync(message));
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'partial_extended_player_sync' player={} revision={} direction={}",
                message.playerId(), message.syncRevision(), context.getDirection()
        );
    }

    private static void handleSyncEntitySizePacket(SyncEntitySizePacket message, NetworkEvent.Context context) {
        runClient(() -> art.arcane.witchery.client.packet.WitcheryClientPacketHandlers.handleSyncEntitySize(message));
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'sync_entity_size' entityId={} width={} height={} direction={}",
                message.entityId(), message.width(), message.height(), context.getDirection()
        );
    }

    private static void handleSetClientPlayerFacingPacket(SetClientPlayerFacingPacket message, NetworkEvent.Context context) {
        runClient(() -> art.arcane.witchery.client.packet.WitcheryClientPacketHandlers.handleSetClientPlayerFacing(message));
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'set_client_player_facing' yaw={} pitch={} direction={}",
                message.yaw(), message.pitch(), context.getDirection()
        );
    }

    private static void handlePushTargetPacket(PushTargetPacket message, NetworkEvent.Context context) {
        runClient(() -> art.arcane.witchery.client.packet.WitcheryClientPacketHandlers.handlePushTarget(message));
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'push_target' entityId={} vx={} vy={} vz={} direction={}",
                message.entityId(), message.velocityX(), message.velocityY(), message.velocityZ(), context.getDirection()
        );
    }

    private static void handleSoundPacket(SoundPacket message, NetworkEvent.Context context) {
        runClient(() -> art.arcane.witchery.client.packet.WitcheryClientPacketHandlers.handleSound(message));
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'sound' id={} x={} y={} z={} volume={} pitch={} direction={}",
                message.soundId(), message.x(), message.y(), message.z(), message.volume(), message.pitch(), context.getDirection()
        );
    }

    private static void handleSpellPreparedPacket(SpellPreparedPacket message, NetworkEvent.Context context) {
        mutateSenderData(context, "spell_prepared", (sender, data) -> {
            sender.getPersistentData().putInt(KEY_PERSISTENT_SPELL_EFFECT_ID, message.effectId());
            sender.getPersistentData().putInt(KEY_PERSISTENT_SPELL_EFFECT_LEVEL, message.level());
            data.applyPreparedSpell(message.effectId(), message.level());
            data.bumpSyncRevision();
            sendPlayerSync(sender, data);
        });
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'spell_prepared' effectId={} level={} direction={}",
                message.effectId(), message.level(), context.getDirection()
        );
    }

    private static void handleClearFallDamagePacket(ClearFallDamagePacket message, NetworkEvent.Context context) {
        ServerPlayer sender = senderOrWarn(context, "clear_fall_damage");
        if (sender != null) {
            sender.fallDistance = 0.0F;
        }
        Witchery.LOGGER.debug("Handled scaffold packet 'clear_fall_damage' direction={}", context.getDirection());
    }

    private static void handleSyncMarkupBookPacket(SyncMarkupBookPacket message, NetworkEvent.Context context) {
        mutateSenderData(context, "sync_markup_book", (sender, data) -> {
            int slot = message.slot();
            if (slot < 0 || slot >= sender.getInventory().getContainerSize()) {
                Witchery.LOGGER.debug("Ignoring scaffold packet 'sync_markup_book' with out-of-range slot={} player={}", slot, sender.getScoreboardName());
                return;
            }

            ItemStack stack = sender.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                Witchery.LOGGER.debug("Ignoring scaffold packet 'sync_markup_book' for empty slot={} player={}", slot, sender.getScoreboardName());
                return;
            }

            ListTag pageStack = new ListTag();
            for (String page : message.pages()) {
                pageStack.add(StringTag.valueOf(page == null ? "" : page));
            }

            CompoundTag stackTag = stack.getOrCreateTag();
            stackTag.put(KEY_STACK_PAGE_STACK, pageStack);
            sender.getInventory().setChanged();
            sender.containerMenu.broadcastChanges();

            data.applyMarkupBookSync(slot, message.pages());
            data.bumpSyncRevision();
            sendPlayerSync(sender, data);
        });
        Witchery.LOGGER.debug(
                "Handled scaffold packet 'sync_markup_book' slot={} pageCount={} direction={}",
                message.slot(), message.pages().size(), context.getDirection()
        );
    }

    private static void handleHowlPacket(HowlPacket message, NetworkEvent.Context context) {
        mutateSenderData(context, "howl", (sender, data) -> {
            long gameTime = sender.serverLevel().getGameTime();
            sender.getPersistentData().putLong(KEY_PERSISTENT_LAST_HOWL_TICK, gameTime);
            data.markHowled(gameTime);
            data.bumpSyncRevision();
            sendPlayerSync(sender, data);
        });
        Witchery.LOGGER.debug("Handled scaffold packet 'howl' direction={}", context.getDirection());
    }

    private static ServerPlayer senderOrWarn(NetworkEvent.Context context, String key) {
        ServerPlayer sender = context.getSender();
        if (sender == null) {
            Witchery.LOGGER.warn("Received packet '{}' without a server sender context", key);
        }
        return sender;
    }

    private static void mutateSenderData(
            NetworkEvent.Context context,
            String key,
            BiConsumer<ServerPlayer, WitcheryPlayerData> mutation
    ) {
        ServerPlayer sender = senderOrWarn(context, key);
        if (sender == null) {
            return;
        }

        var dataOptional = WitcheryPlayerDataProvider.get(sender);
        if (!dataOptional.isPresent()) {
            Witchery.LOGGER.warn("Missing WitcheryPlayerData capability for '{}' while handling '{}'", sender.getScoreboardName(), key);
            return;
        }
        dataOptional.ifPresent(data -> mutation.accept(sender, data));
    }

    private static void runClient(Runnable runnable) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> runnable.run());
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

    public record ParticlesPacket(
            int particleEffectId,
            int soundEffectId,
            double x,
            double y,
            double z,
            double width,
            double height,
            int color
    ) {
        public static ParticlesPacket placeholder() {
            return new ParticlesPacket(0, 0, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0xFFFFFF);
        }

        public static void encode(ParticlesPacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.particleEffectId());
            buffer.writeVarInt(message.soundEffectId());
            buffer.writeDouble(message.x());
            buffer.writeDouble(message.y());
            buffer.writeDouble(message.z());
            buffer.writeDouble(message.width());
            buffer.writeDouble(message.height());
            buffer.writeInt(message.color());
        }

        public static ParticlesPacket decode(FriendlyByteBuf buffer) {
            return new ParticlesPacket(
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readInt()
            );
        }
    }

    public record CamPosPacket(boolean active, boolean updatePosition, int entityId) {
        public static CamPosPacket placeholder() {
            return new CamPosPacket(false, false, 0);
        }

        public static void encode(CamPosPacket message, FriendlyByteBuf buffer) {
            buffer.writeBoolean(message.active());
            buffer.writeBoolean(message.updatePosition());
            buffer.writeVarInt(message.entityId());
        }

        public static CamPosPacket decode(FriendlyByteBuf buffer) {
            return new CamPosPacket(
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readVarInt()
            );
        }
    }

    public record ItemUpdatePacket(int slotIndex, int damageValue, int pageIndex) {
        public static ItemUpdatePacket placeholder() {
            return new ItemUpdatePacket(0, 0, 0);
        }

        public static void encode(ItemUpdatePacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.slotIndex());
            buffer.writeVarInt(message.damageValue());
            buffer.writeVarInt(message.pageIndex());
        }

        public static ItemUpdatePacket decode(FriendlyByteBuf buffer) {
            return new ItemUpdatePacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
        }
    }

    public record PlayerStylePacket(
            String username,
            int grotesqueTicks,
            int nightmare,
            boolean ghost,
            int creatureType,
            int blood,
            String playerSkin
    ) {
        public PlayerStylePacket {
            username = username == null ? "" : username;
            playerSkin = playerSkin == null ? "" : playerSkin;
        }

        public static PlayerStylePacket placeholder() {
            return new PlayerStylePacket("", 0, 0, false, 0, 0, "");
        }

        public static void encode(PlayerStylePacket message, FriendlyByteBuf buffer) {
            buffer.writeUtf(message.username(), MAX_USERNAME_LENGTH);
            buffer.writeVarInt(message.grotesqueTicks());
            buffer.writeVarInt(message.nightmare());
            buffer.writeBoolean(message.ghost());
            buffer.writeVarInt(message.creatureType());
            buffer.writeVarInt(message.blood());
            buffer.writeUtf(message.playerSkin(), MAX_PLAYER_SKIN_LENGTH);
        }

        public static PlayerStylePacket decode(FriendlyByteBuf buffer) {
            return new PlayerStylePacket(
                    buffer.readUtf(MAX_USERNAME_LENGTH),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readBoolean(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readUtf(MAX_PLAYER_SKIN_LENGTH)
            );
        }
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

    public record PushTargetPacket(int entityId, double velocityX, double velocityY, double velocityZ) {
        public static PushTargetPacket placeholder() {
            return new PushTargetPacket(0, 0.0D, 0.0D, 0.0D);
        }

        public static void encode(PushTargetPacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.entityId());
            buffer.writeDouble(message.velocityX());
            buffer.writeDouble(message.velocityY());
            buffer.writeDouble(message.velocityZ());
        }

        public static PushTargetPacket decode(FriendlyByteBuf buffer) {
            return new PushTargetPacket(
                    buffer.readVarInt(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble()
            );
        }
    }

    public record SoundPacket(String soundId, double x, double y, double z, float volume, float pitch) {
        private static final int MAX_SOUND_ID_LENGTH = 128;

        public static SoundPacket placeholder() {
            return new SoundPacket("minecraft:block.note_block.harp", 0.0D, 0.0D, 0.0D, 1.0F, 1.0F);
        }

        public static void encode(SoundPacket message, FriendlyByteBuf buffer) {
            buffer.writeUtf(message.soundId(), MAX_SOUND_ID_LENGTH);
            buffer.writeDouble(message.x());
            buffer.writeDouble(message.y());
            buffer.writeDouble(message.z());
            buffer.writeFloat(message.volume());
            buffer.writeFloat(message.pitch());
        }

        public static SoundPacket decode(FriendlyByteBuf buffer) {
            return new SoundPacket(
                    buffer.readUtf(MAX_SOUND_ID_LENGTH),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readFloat(),
                    buffer.readFloat()
            );
        }
    }

    public record SpellPreparedPacket(int effectId, int level) {
        public static SpellPreparedPacket placeholder() {
            return new SpellPreparedPacket(0, 0);
        }

        public static void encode(SpellPreparedPacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.effectId());
            buffer.writeVarInt(message.level());
        }

        public static SpellPreparedPacket decode(FriendlyByteBuf buffer) {
            return new SpellPreparedPacket(buffer.readVarInt(), buffer.readVarInt());
        }
    }

    public record ClearFallDamagePacket() {
        public static ClearFallDamagePacket placeholder() {
            return new ClearFallDamagePacket();
        }

        public static void encode(ClearFallDamagePacket message, FriendlyByteBuf buffer) {
        }

        public static ClearFallDamagePacket decode(FriendlyByteBuf buffer) {
            return placeholder();
        }
    }

    public record SyncEntitySizePacket(int entityId, float width, float height) {
        public static SyncEntitySizePacket placeholder() {
            return new SyncEntitySizePacket(0, 0.6F, 1.8F);
        }

        public static void encode(SyncEntitySizePacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.entityId());
            buffer.writeFloat(message.width());
            buffer.writeFloat(message.height());
        }

        public static SyncEntitySizePacket decode(FriendlyByteBuf buffer) {
            return new SyncEntitySizePacket(buffer.readVarInt(), buffer.readFloat(), buffer.readFloat());
        }
    }

    public record SyncMarkupBookPacket(int slot, List<String> pages) {
        public SyncMarkupBookPacket {
            if (pages == null || pages.isEmpty()) {
                pages = List.of();
            } else {
                pages = List.copyOf(pages);
            }
        }

        public static SyncMarkupBookPacket placeholder() {
            return new SyncMarkupBookPacket(0, List.of());
        }

        public static void encode(SyncMarkupBookPacket message, FriendlyByteBuf buffer) {
            int clampedCount = Math.min(message.pages().size(), MAX_MARKUP_PAGE_COUNT);
            buffer.writeVarInt(message.slot());
            buffer.writeVarInt(clampedCount);

            for (int index = 0; index < clampedCount; index++) {
                String page = message.pages().get(index);
                buffer.writeUtf(page == null ? "" : page, MAX_MARKUP_PAGE_LENGTH);
            }
        }

        public static SyncMarkupBookPacket decode(FriendlyByteBuf buffer) {
            int slot = buffer.readVarInt();
            int pageCount = Math.min(buffer.readVarInt(), MAX_MARKUP_PAGE_COUNT);
            List<String> pages = new java.util.ArrayList<>(pageCount);
            for (int index = 0; index < pageCount; index++) {
                pages.add(buffer.readUtf(MAX_MARKUP_PAGE_LENGTH));
            }
            return new SyncMarkupBookPacket(slot, pages);
        }
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
        public static HowlPacket placeholder() {
            return new HowlPacket();
        }

        public static void encode(HowlPacket message, FriendlyByteBuf buffer) {
        }

        public static HowlPacket decode(FriendlyByteBuf buffer) {
            return placeholder();
        }
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

    public record SetClientPlayerFacingPacket(float yaw, float pitch) {
        public static SetClientPlayerFacingPacket placeholder() {
            return new SetClientPlayerFacingPacket(0.0F, 0.0F);
        }

        public static void encode(SetClientPlayerFacingPacket message, FriendlyByteBuf buffer) {
            buffer.writeFloat(message.yaw());
            buffer.writeFloat(message.pitch());
        }

        public static SetClientPlayerFacingPacket decode(FriendlyByteBuf buffer) {
            return new SetClientPlayerFacingPacket(buffer.readFloat(), buffer.readFloat());
        }
    }
}
