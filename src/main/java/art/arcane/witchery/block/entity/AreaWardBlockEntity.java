package art.arcane.witchery.block.entity;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public final class AreaWardBlockEntity extends BlockEntity {
    private static final Map<ServerLevel, Set<BlockPos>> LOADED_WARDS = new WeakHashMap<>();

    public enum Kind {
        DIRECTED, TELEPORT
    }

    private final Kind kind;
    private String owner = "";
    private final Set<String> killers = new LinkedHashSet<>();

    public AreaWardBlockEntity(BlockPos pos, BlockState state, Kind kind) {
        super((kind == Kind.DIRECTED ? WitcheryBlockEntities.DECURSE_DIRECTED
                : WitcheryBlockEntities.DECURSE_TELEPORT).get(), pos, state);
        this.kind = kind;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) {
            LOADED_WARDS.computeIfAbsent(server, ignored -> new LinkedHashSet<>()).add(worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel server) {
            Set<BlockPos> positions = LOADED_WARDS.get(server);
            if (positions != null) positions.remove(worldPosition);
        }
        super.setRemoved();
    }

    public void placedBy(Player player) {
        owner = player.getGameProfile().getName();
        setChanged();
    }

    public boolean ownedBy(Player player) {
        return player.getGameProfile().getName().equals(owner);
    }

    public static boolean protectsBrew(LivingEntity entity) {
        return findWard(entity, ward -> ward.enabled() && !ward.isKiller(entity));
    }

    public static boolean protectsDirectedCurse(LivingEntity entity) {
        return findWard(entity, ward -> ward.kind == Kind.DIRECTED && ward.enabled() && !ward.isKiller(entity));
    }

    public static boolean protectsSummoning(LivingEntity entity) {
        return findWard(entity, ward -> ward.kind == Kind.TELEPORT && ward.enabled() && !ward.isKiller(entity));
    }

    public static void recordKiller(ServerPlayer player) {
        findWard(player, ward -> {
            if (ward.killers.add(player.getGameProfile().getName())) ward.setChanged();
            return false;
        });
    }

    private static boolean findWard(LivingEntity entity, Predicate<AreaWardBlockEntity> matches) {
        if (!(entity.level() instanceof ServerLevel server)) return false;
        for (BlockPos pos : LOADED_WARDS.getOrDefault(server, Set.of())) {
            if (server.hasChunkAt(pos) && server.getBlockEntity(pos) instanceof AreaWardBlockEntity ward
                    && ward.inRange(entity) && matches.test(ward)) return true;
        }
        return false;
    }

    private boolean enabled() {
        return (kind == Kind.DIRECTED ? WitcheryServerConfig.DECURSE_DIRECTED_ENABLED
                : WitcheryServerConfig.DECURSE_TELEPORT_ENABLED).get();
    }

    private boolean inRange(LivingEntity entity) {
        int radius = (kind == Kind.DIRECTED ? WitcheryServerConfig.DECURSE_DIRECTED_RADIUS
                : WitcheryServerConfig.DECURSE_TELEPORT_RADIUS).get();
        double dx = entity.getX() - worldPosition.getX();
        double dz = entity.getZ() - worldPosition.getZ();
        return dx * dx + dz * dz <= radius * radius;
    }

    private boolean isKiller(LivingEntity entity) {
        return entity instanceof Player player && killers.contains(player.getGameProfile().getName());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.getString("WITCPlacer");
        killers.clear();
        for (Tag name : tag.getList("Killers", Tag.TAG_STRING)) killers.add(name.getAsString());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("WITCPlacer", owner);
        ListTag savedKillers = new ListTag();
        for (String name : killers) savedKillers.add(StringTag.valueOf(name));
        tag.put("Killers", savedKillers);
    }
}
