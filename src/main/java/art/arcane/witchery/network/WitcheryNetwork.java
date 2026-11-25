package art.arcane.witchery.network;

import art.arcane.witchery.vampirism.BloodFeeding;
import art.arcane.witchery.vampirism.VampireAbilities;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.BrewAirJump;
import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.item.VampireBookItem;
import art.arcane.witchery.item.GuideBookItem;
import art.arcane.witchery.item.GuideBookPages;
import art.arcane.witchery.item.BiomeBookIndex;
import art.arcane.witchery.item.CircleBookIndex;
import art.arcane.witchery.infusion.SpellKnowledge;
import art.arcane.witchery.lycanthropy.WolfTrials;
import art.arcane.witchery.registry.LegacyRegistryData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    private static final String PROTOCOL_VERSION = "98";
    private static final int MAX_USERNAME_LENGTH = 64;
    private static final int MAX_PLAYER_SKIN_LENGTH = 256;
    private static final int MAX_MARKUP_CHAPTER_LENGTH = 32;
    private static final int LEGACY_HUGE_EXPLOSION_PARTICLE_ID = 0;
    private static final int LEGACY_LARGE_EXPLOSION_PARTICLE_ID = 1;
    private static final int LEGACY_SMOKE_PARTICLE_ID = 8;
    private static final int LEGACY_MOB_SPELL_PARTICLE_ID = 9;
    private static final int LEGACY_SPELL_PARTICLE_ID = 10;
    private static final int LEGACY_INSTANT_SPELL_PARTICLE_ID = 11;
    private static final int LEGACY_PORTAL_PARTICLE_ID = 13;
    private static final int LEGACY_EXPLOSION_PARTICLE_ID = 15;
    private static final int LEGACY_FLAME_PARTICLE_ID = 16;
    private static final int LEGACY_SPLASH_PARTICLE_ID = 19;
    private static final int LEGACY_LARGE_SMOKE_PARTICLE_ID = 20;
    private static final int LEGACY_RED_DUST_PARTICLE_ID = 22;
    private static final int LEGACY_HEART_PARTICLE_ID = 28;
    private static final int LEGACY_NO_SOUND_ID = 0;
    private static final int LEGACY_FIZZ_SOUND_ID = 2;
    private static final int LEGACY_SNARE_SOUND_ID = 3;
    private static final int LEGACY_WATER_SPLASH_SOUND_ID = 4;
    private static final int LEGACY_PLING_SOUND_ID = 9;
    private static final int LEGACY_ENDERMAN_TELEPORT_SOUND_ID = 16;
    private static final int LEGACY_GHAST_FIREBALL_SOUND_ID = 19;
    private static final int LEGACY_BLAZE_DEATH_SOUND_ID = 44;
    private static final int LEGACY_DRINK_SOUND_ID = 56;
    private static final int LEGACY_POOF_SOUND_ID = 57;
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

        // Keep discriminator 0 reserved for the retired Brew Bag gesture packet.
        int discriminator = 1;
        discriminator = registerParticlesPacket(discriminator);
        discriminator = registerCamPosPacket(discriminator);
        discriminator = registerItemUpdatePacket(discriminator);
        discriminator = registerPlayerStylePacket(discriminator);
        discriminator = registerPlayerSyncPacket(discriminator);
        discriminator = registerPushTargetPacket(discriminator);
        discriminator = registerSoundPacket(discriminator);
        discriminator = registerClearFallDamagePacket(discriminator);
        discriminator = registerSyncEntitySizePacket(discriminator);
        discriminator = registerSyncMarkupBookPacket(discriminator);
        discriminator = registerExtendedPlayerSyncPacket(discriminator);
        discriminator = registerHowlPacket(discriminator);
        discriminator = registerBloodTargetPacket(discriminator);
        discriminator = registerVampireAbilityPacket(discriminator);
        discriminator = registerBloodRequestPacket(discriminator);
        discriminator = registerPartialExtendedPlayerSyncPacket(discriminator);
        discriminator = registerSetClientPlayerFacingPacket(discriminator);
        CHANNEL.messageBuilder(DreamAtmospherePacket.class, discriminator++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((message, buffer) -> buffer.writeByte(message.nightmareLevel()))
                .decoder(buffer -> new DreamAtmospherePacket(buffer.readUnsignedByte()))
                .consumerMainThread((message, context) -> {
                    runClient(() -> art.arcane.witchery.client.DreamSpecialEffects.updateClientMode(message.nightmareLevel()));
                    context.get().setPacketHandled(true);
                })
                .add();
        CHANNEL.messageBuilder(InfusionStatusPacket.class, discriminator++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((message, buffer) -> {
                    buffer.writeVarInt(message.type());
                    buffer.writeVarInt(message.charge());
                    buffer.writeVarInt(message.capacity());
                    buffer.writeVarInt(message.power());
                    buffer.writeVarInt(message.powerCharge());
                    buffer.writeVarInt(message.brew());
                    buffer.writeVarInt(message.brewTicks());
                    buffer.writeVarInt(message.sinking());
                })
                .decoder(buffer -> new InfusionStatusPacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                        buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()))
                .consumerMainThread((message, context) -> {
                    runClient(() -> art.arcane.witchery.client.ClientInfusion.setStatus(
                            message.type(), message.charge(), message.capacity(), message.power(), message.powerCharge(),
                            message.brew(), message.brewTicks(), message.sinking()));
                    context.get().setPacketHandled(true);
                })
                .add();
        CHANNEL.messageBuilder(GuideBookOpenPacket.class, discriminator++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((message, buffer) -> {
                    buffer.writeVarInt(message.slot());
                    buffer.writeEnum(message.volume());
                    buffer.writeVarInt(message.knownSpells());
                })
                .decoder(buffer -> new GuideBookOpenPacket(buffer.readVarInt(), buffer.readEnum(GuideBookItem.Volume.class),
                        buffer.readVarInt()))
                .consumerMainThread((message, context) -> {
                    runClient(() -> art.arcane.witchery.client.GuideBookScreen.open(message.slot(), message.volume(), message.knownSpells()));
                    context.get().setPacketHandled(true);
                })
                .add();
        CHANNEL.messageBuilder(BrewJumpPacket.class, discriminator, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((message, buffer) -> {})
                .decoder(buffer -> new BrewJumpPacket())
                .consumerMainThread((message, context) -> {
                    runClient(() -> art.arcane.witchery.client.ClientBrewMovement.jump());
                    context.get().setPacketHandled(true);
                })
                .add();
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
                new ExtendedPlayerSyncPacket(player.getUUID(), data.isInitialized(), data.getSyncRevision(), createPlayerDataPayload(data))
        );
    }

    public static void requestVampirePower(int power, boolean activate) {
        CHANNEL.sendToServer(new SelectPlayerAbilityPacket(power, activate));
    }

    public static void requestBloodTarget(int entityId) {
        CHANNEL.sendToServer(new ExtendedEntityRequestSyncToClientPacket(entityId));
    }

    public static void sendBloodTarget(ServerPlayer player, int entityId, int blood) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ExtendedVillagerSyncPacket(entityId, blood));
    }

    public static void sendPartialExtendedPlayerSync(ServerPlayer player, WitcheryPlayerData data) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new PartialExtendedPlayerSyncPacket(player.getUUID(), data.getSyncRevision(), createPlayerDataPayload(data))
        );
    }

    public static void sendPlayerSync(ServerPlayer player, WitcheryPlayerData data) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new PlayerSyncPacket(player.getUUID(), data.getSyncRevision(), createPlayerDataPayload(data))
        );
    }

    public static void sendGuideBookmark(int slotIndex, GuideBookItem.Volume volume, int pageIndex,
                                         @Nullable ResourceLocation entryId) {
        CHANNEL.sendToServer(new ItemUpdatePacket(slotIndex, volume, pageIndex, entryId));
    }

    public static void openGuideBook(ServerPlayer reader, int slot, GuideBookItem.Volume volume) {
        int known = volume == GuideBookItem.Volume.SYMBOLOGY ? SpellKnowledge.knownMask(reader) : 0;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> reader), new GuideBookOpenPacket(slot, volume, known));
    }

    public static void sendSyncEntitySize(ServerPlayer player, int entityId, float scale) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncEntitySizePacket(entityId, scale)
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

    public static void sendDreamAtmosphere(ServerPlayer player, int nightmareLevel) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DreamAtmospherePacket(nightmareLevel));
    }

    private record DreamAtmospherePacket(int nightmareLevel) {
    }

    public static void sendInfusionStatus(ServerPlayer player, int type, int charge, int capacity, int power, int powerCharge,
                                          int brew, int brewTicks, int sinking) {
        if (player.connection == null || !player.connection.connection.isConnected()) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new InfusionStatusPacket(type, charge, capacity, power, powerCharge, brew, brewTicks, sinking));
    }

    private record InfusionStatusPacket(int type, int charge, int capacity, int power, int powerCharge, int brew,
                                        int brewTicks, int sinking) {
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

    public static void sendSoundAround(ServerLevel level, Vec3 center, double range, String soundId, float volume, float pitch) {
        var target = PacketDistributor.TargetPoint.p(center.x, center.y, center.z, range, level.dimension());
        CHANNEL.send(PacketDistributor.NEAR.with(target), new SoundPacket(soundId, center.x, center.y, center.z, volume, pitch));
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

    public static void sendWaterSplash(ServerLevel level, double x, double y, double z) {
        sendParticlesAround(level, LEGACY_SPLASH_PARTICLE_ID, LEGACY_WATER_SPLASH_SOUND_ID, new Vec3(x, y, z), 0.5D, 0.5D, 5);
    }

    public static void sendInfusionRecharge(ServerLevel level, Vec3 center) {
        sendParticlesAround(level, LEGACY_INSTANT_SPELL_PARTICLE_ID, LEGACY_PLING_SOUND_ID, center, 1, 2, 8);
    }

    public static void sendInfusionRite(ServerLevel level, Vec3 center, boolean manifestation) {
        int particle = manifestation ? LEGACY_INSTANT_SPELL_PARTICLE_ID : LEGACY_HUGE_EXPLOSION_PARTICLE_ID;
        sendParticlesAround(level, particle, LEGACY_FIZZ_SOUND_ID, center, 3, 3, 16);
    }

    public static void sendCircleTeleport(ServerLevel level, Vec3 center, boolean vampire) {
        int particle = vampire ? LEGACY_SMOKE_PARTICLE_ID : LEGACY_PORTAL_PARTICLE_ID;
        int sound = vampire ? LEGACY_POOF_SOUND_ID : LEGACY_ENDERMAN_TELEPORT_SOUND_ID;
        sendParticlesAround(level, particle, sound, center, 0.5, 2, 16);
    }

    public static void sendVampireHuntTravel(ServerLevel level, Vec3 center) {
        sendParticlesAround(level, LEGACY_SMOKE_PARTICLE_ID, LEGACY_POOF_SOUND_ID, center, 0.8, 1.5, 16);
    }

    public static void sendVampireHuntBite(ServerLevel level, Vec3 center) {
        sendParticlesAround(level, LEGACY_RED_DUST_PARTICLE_ID, LEGACY_DRINK_SOUND_ID, center, 0.5, 0.2, 16);
    }

    public static void sendSummonedCreature(ServerLevel level, Vec3 center) {
        sendParticlesAround(level, LEGACY_PORTAL_PARTICLE_ID, LEGACY_FIZZ_SOUND_ID, center, 0.5, 1, 16);
    }

    public static void sendHellOnEarthArrival(ServerLevel level, Vec3 arrival) {
        sendParticlesAround(level, LEGACY_LARGE_EXPLOSION_PARTICLE_ID, LEGACY_BLAZE_DEATH_SOUND_ID, arrival, 1, 2, 16);
    }

    public static void sendSummoningObstruction(ServerLevel level, Vec3 center) {
        sendParticlesAround(level, LEGACY_LARGE_SMOKE_PARTICLE_ID, LEGACY_SNARE_SOUND_ID, center, 0.5, 2, 16);
    }

    public static void sendBanishment(ServerLevel level, Vec3 center) {
        sendParticlesAround(level, LEGACY_EXPLOSION_PARTICLE_ID, LEGACY_NO_SOUND_ID, center, 1, 2, 16);
    }

    public static void sendCookingRite(ServerLevel level, Vec3 origin) {
        sendParticlesAround(level, LEGACY_FLAME_PARTICLE_ID, LEGACY_GHAST_FIREBALL_SOUND_ID, origin, 3, 2, 16);
    }

    public static void sendRiteWaveSurface(ServerLevel level, BlockPos surface, boolean curse, boolean belowGlyph) {
        int particle = curse ? LEGACY_MOB_SPELL_PARTICLE_ID : LEGACY_SPELL_PARTICLE_ID;
        sendParticlesAround(level, particle, LEGACY_NO_SOUND_ID, Vec3.atBottomCenterOf(surface.above()),
                1, 1, belowGlyph ? 32 : 16);
    }

    public static void sendFamiliarRite(ServerLevel level, Vec3 center, boolean binding) {
        if (binding) {
            sendParticlesAround(level, LEGACY_HEART_PARTICLE_ID, LEGACY_FIZZ_SOUND_ID, center, 3, 3, 16);
        } else {
            sendParticlesAround(level, LEGACY_INSTANT_SPELL_PARTICLE_ID, LEGACY_FIZZ_SOUND_ID, center, 1, 2, 16);
        }
    }

    public static void sendFamiliarTransition(ServerLevel level, Vec3 center) {
        sendParticlesAround(level, LEGACY_INSTANT_SPELL_PARTICLE_ID, LEGACY_ENDERMAN_TELEPORT_SOUND_ID, center, 1, 1, 16);
    }

    public static void sendParticlesAround(ServerLevel level, int particleEffectId, int soundEffectId, Vec3 center,
                                           double width, double height, double range) {
        var target = PacketDistributor.TargetPoint.p(center.x, center.y, center.z, range, level.dimension());
        CHANNEL.send(PacketDistributor.NEAR.with(target), new ParticlesPacket(particleEffectId, soundEffectId,
                center.x, center.y, center.z, width, height, 0xFFFFFF));
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

    public static void broadcastPlayerStyle(ServerPlayer player, WitcheryPlayerData data) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new PlayerStylePacket(
                player.getGameProfile().getName(), data.getGrotesqueTicks(), data.getNightmareLevel(), data.isGhost(),
                data.getCreatureTypeOrdinal(), data.getHumanBlood(), data.getOtherPlayerSkin()));
    }

    public static void requestBrewJump() {
        CHANNEL.sendToServer(ClearFallDamagePacket.placeholder());
    }

    public static void sendMarkupBookBookmark(int slot, @Nullable List<String> trail) {
        CHANNEL.sendToServer(new SyncMarkupBookPacket(slot, trail));
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

    private static int registerVampireAbilityPacket(int discriminator) {
        return registerPacketWithCodec(discriminator, "select_player_ability", SelectPlayerAbilityPacket.class,
                (message, buffer) -> buffer.writeVarInt(message.power()).writeBoolean(message.activate()),
                buffer -> new SelectPlayerAbilityPacket(buffer.readVarInt(), buffer.readBoolean()),
                (message, context) -> {
                    ServerPlayer sender = context.getSender();
                    if (sender == null || !sender.isAlive() || sender.isSpectator()) return;
                    if (message.activate()) VampireAbilities.activate(sender);
                    else VampireAbilities.select(sender, message.power());
                }, () -> new SelectPlayerAbilityPacket(0, false));
    }

    private static int registerBloodRequestPacket(int discriminator) {
        return registerPacketWithCodec(discriminator, "extended_entity_request_sync_to_client", ExtendedEntityRequestSyncToClientPacket.class,
                (message, buffer) -> buffer.writeVarInt(message.entityId()),
                buffer -> new ExtendedEntityRequestSyncToClientPacket(buffer.readVarInt()),
                (message, context) -> {
                    ServerPlayer sender = context.getSender();
                    if (sender != null && sender.isAlive() && !sender.isSpectator()) BloodFeeding.shareNearbyBlood(sender, message.entityId());
                }, () -> new ExtendedEntityRequestSyncToClientPacket(0));
    }

    private static int registerBloodTargetPacket(int discriminator) {
        return registerPacketWithCodec(discriminator, "extended_villager_sync", ExtendedVillagerSyncPacket.class,
                (message, buffer) -> buffer.writeVarInt(message.entityId()).writeInt(message.blood()),
                buffer -> new ExtendedVillagerSyncPacket(buffer.readVarInt(), buffer.readInt()),
                (message, context) -> runClient(() -> art.arcane.witchery.client.ClientVampirism.receiveBlood(message.entityId(), message.blood())),
                () -> new ExtendedVillagerSyncPacket(0, -1));
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
            if (sender.isSpectator() || slot != sender.getInventory().selected && slot != sender.getInventory().getContainerSize() - 1) return;
            ItemStack stack = sender.getInventory().getItem(slot);
            var volume = GuideBookItem.volume(stack);
            int page = message.pageIndex();
            if (volume != message.volume() || page < 0) return;
            if (volume == GuideBookItem.Volume.ATLAS) {
                page = BiomeBookIndex.saveSelection(stack, sender.level().registryAccess(), page, message.entryId());
                if (page < 0) return;
            } else {
                var pages = GuideBookPages.entries(volume, sender.level(), SpellKnowledge.knownMask(sender));
                if (volume == GuideBookItem.Volume.CIRCLES) {
                    page = CircleBookIndex.saveSelection(stack, pages, page, message.entryId());
                    if (page < 0) return;
                } else {
                    if (message.entryId() != null || page >= pages.size()) return;
                    GuideBookItem.setBookmark(stack, page);
                }
            }
            sender.getInventory().setChanged();
            sender.containerMenu.broadcastChanges();
            data.applyItemUpdate(slot, stack.getDamageValue(), page);
            data.bumpSyncRevision();
            sendPlayerSync(sender, data);
        });
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

    private static void handleClearFallDamagePacket(ClearFallDamagePacket message, NetworkEvent.Context context) {
        ServerPlayer sender = senderOrWarn(context, "clear_fall_damage");
        if (sender != null && BrewAirJump.tryJump(sender)) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new BrewJumpPacket());
        }
    }

    private static void handleSyncMarkupBookPacket(SyncMarkupBookPacket message, NetworkEvent.Context context) {
        mutateSenderData(context, "sync_markup_book", (sender, data) -> {
            int slot = message.slot();
            if (sender.isSpectator() || slot != sender.getInventory().selected && slot != sender.getInventory().getContainerSize() - 1) return;
            ItemStack stack = sender.getInventory().getItem(slot);
            if (!(stack.getItem() instanceof VampireBookItem)) return;
            if (message.trail() != null) {
                if (!VampireBookItem.setBookmarkTrail(stack, message.trail())) return;
                sender.getInventory().setChanged();
                sender.containerMenu.broadcastChanges();
            }
            data.applyMarkupBookSync(slot, VampireBookItem.bookmarkTrail(stack));
            data.increaseVampireLevelCap(VampireBookItem.tornPages(stack) + 1);
            data.bumpSyncRevision();
            sendPartialExtendedPlayerSync(sender, data);
        });
    }

    private static void handleHowlPacket(HowlPacket message, NetworkEvent.Context context) {
        ServerPlayer sender = senderOrWarn(context, "howl");
        if (sender != null) WolfTrials.howl(sender);
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

    private static CompoundTag createPlayerDataPayload(WitcheryPlayerData data) {
        CompoundTag payload = data.serializeNBT();
        payload.putInt("SelectedVampirePower", data.getSelectedVampirePower());
        return payload;
    }

    private static CompoundTag readPlayerDataPayload(FriendlyByteBuf buffer) {
        CompoundTag payload = buffer.readNbt();
        return payload == null ? new CompoundTag() : payload;
    }

    private record PacketBinding(LegacyRegistryData.LegacyPacketIntent intent, Supplier<Object> factory) {
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

    private record GuideBookOpenPacket(int slot, GuideBookItem.Volume volume, int knownSpells) {}

    public record ItemUpdatePacket(int slotIndex, GuideBookItem.Volume volume, int pageIndex,
                                   @Nullable ResourceLocation entryId) {
        public static ItemUpdatePacket placeholder() {
            return new ItemUpdatePacket(0, GuideBookItem.Volume.FUMES, 0, null);
        }

        public static void encode(ItemUpdatePacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.slotIndex());
            buffer.writeEnum(message.volume());
            buffer.writeVarInt(message.pageIndex());
            buffer.writeNullable(message.entryId(), FriendlyByteBuf::writeResourceLocation);
        }

        public static ItemUpdatePacket decode(FriendlyByteBuf buffer) {
            return new ItemUpdatePacket(buffer.readVarInt(), buffer.readEnum(GuideBookItem.Volume.class),
                    buffer.readVarInt(), buffer.readNullable(FriendlyByteBuf::readResourceLocation));
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

    public record PlayerSyncPacket(UUID playerId, int syncRevision, CompoundTag playerData) {
        private static final UUID PLACEHOLDER_PLAYER = new UUID(0L, 0L);

        public PlayerSyncPacket {
            playerData = playerData == null ? new CompoundTag() : playerData.copy();
        }

        public static PlayerSyncPacket placeholder() {
            return new PlayerSyncPacket(PLACEHOLDER_PLAYER, 0, new CompoundTag());
        }

        public static void encode(PlayerSyncPacket message, FriendlyByteBuf buffer) {
            buffer.writeUUID(message.playerId());
            buffer.writeVarInt(message.syncRevision());
            buffer.writeNbt(message.playerData());
        }

        public static PlayerSyncPacket decode(FriendlyByteBuf buffer) {
            return new PlayerSyncPacket(buffer.readUUID(), buffer.readVarInt(), readPlayerDataPayload(buffer));
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

    private record BrewJumpPacket() {}

    public record SyncEntitySizePacket(int entityId, float scale) {
        public static SyncEntitySizePacket placeholder() {
            return new SyncEntitySizePacket(0, 1);
        }

        public static void encode(SyncEntitySizePacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.entityId());
            buffer.writeFloat(message.scale());
        }

        public static SyncEntitySizePacket decode(FriendlyByteBuf buffer) {
            return new SyncEntitySizePacket(buffer.readVarInt(), buffer.readFloat());
        }
    }

    public record SyncMarkupBookPacket(int slot, @Nullable List<String> trail) {
        public SyncMarkupBookPacket {
            if (trail != null) trail = List.copyOf(trail);
        }

        public static SyncMarkupBookPacket placeholder() {
            return new SyncMarkupBookPacket(0, null);
        }

        public static void encode(SyncMarkupBookPacket message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.slot());
            buffer.writeNullable(message.trail(), (out, trail) ->
                    out.writeCollection(trail, (buf, chapter) -> buf.writeUtf(chapter, MAX_MARKUP_CHAPTER_LENGTH)));
        }

        public static SyncMarkupBookPacket decode(FriendlyByteBuf buffer) {
            int slot = buffer.readVarInt();
            var trail = buffer.readNullable(input -> input.readCollection(
                    FriendlyByteBuf.limitValue(ArrayList<String>::new, VampireBookItem.MAX_BOOKMARK_DEPTH),
                    buf -> buf.readUtf(MAX_MARKUP_CHAPTER_LENGTH)));
            return new SyncMarkupBookPacket(slot, trail);
        }
    }

    public record ExtendedPlayerSyncPacket(UUID playerId, boolean initialized, int syncRevision, CompoundTag playerData) {
        private static final UUID PLACEHOLDER_PLAYER = new UUID(0L, 0L);

        public ExtendedPlayerSyncPacket {
            playerData = playerData == null ? new CompoundTag() : playerData.copy();
        }

        public static ExtendedPlayerSyncPacket placeholder() {
            return new ExtendedPlayerSyncPacket(PLACEHOLDER_PLAYER, false, 0, new CompoundTag());
        }

        public static void encode(ExtendedPlayerSyncPacket message, FriendlyByteBuf buffer) {
            buffer.writeUUID(message.playerId());
            buffer.writeBoolean(message.initialized());
            buffer.writeVarInt(message.syncRevision());
            buffer.writeNbt(message.playerData());
        }

        public static ExtendedPlayerSyncPacket decode(FriendlyByteBuf buffer) {
            return new ExtendedPlayerSyncPacket(buffer.readUUID(), buffer.readBoolean(), buffer.readVarInt(), readPlayerDataPayload(buffer));
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

    public record ExtendedVillagerSyncPacket(int entityId, int blood) {
    }

    public record SelectPlayerAbilityPacket(int power, boolean activate) {
    }

    public record ExtendedEntityRequestSyncToClientPacket(int entityId) {
    }

    public record PartialExtendedPlayerSyncPacket(UUID playerId, int syncRevision, CompoundTag playerData) {
        private static final UUID PLACEHOLDER_PLAYER = new UUID(0L, 0L);

        public PartialExtendedPlayerSyncPacket {
            playerData = playerData == null ? new CompoundTag() : playerData.copy();
        }

        public static PartialExtendedPlayerSyncPacket placeholder() {
            return new PartialExtendedPlayerSyncPacket(PLACEHOLDER_PLAYER, 0, new CompoundTag());
        }

        public static void encode(PartialExtendedPlayerSyncPacket message, FriendlyByteBuf buffer) {
            buffer.writeUUID(message.playerId());
            buffer.writeVarInt(message.syncRevision());
            buffer.writeNbt(message.playerData());
        }

        public static PartialExtendedPlayerSyncPacket decode(FriendlyByteBuf buffer) {
            return new PartialExtendedPlayerSyncPacket(buffer.readUUID(), buffer.readVarInt(), readPlayerDataPayload(buffer));
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
