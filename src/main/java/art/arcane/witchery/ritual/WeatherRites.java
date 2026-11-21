package art.arcane.witchery.ritual;

import art.arcane.witchery.config.WitcheryServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.WeakHashMap;

public final class WeatherRites {
    public static final int STORM_INTERVAL = 30;
    private static final long DAY_TICKS = 24_000;
    private static final long MIDNIGHT = 18_000;
    private static final TicketType<BlockPos> LIGHTNING_TICKET =
            TicketType.create("witchery_ritual_lightning", Comparator.comparingLong(BlockPos::asLong), 60);
    private static final Map<ServerLevel, Long> LAST_ECLIPSE = new WeakHashMap<>();

    private WeatherRites() {}

    public record Storm(BlockPos target, boolean greater, int stage) {
        @Nullable
        public Storm pulse(ServerLevel level) {
            int nextStage = stage + 1;
            switch (nextStage) {
                case 1, 2 -> strike(level);
                case 3 -> {
                    strike(level);
                    strike(level);
                }
                case 4 -> {
                    ServerLevel overworld = level.getServer().overworld();
                    if (!overworld.isThundering()) {
                        int duration = (300 + level.random.nextInt(600)) * 20;
                        overworld.setWeatherParameters(0, duration, true, true);
                    }
                    strike(level);
                }
                default -> {
                    for (int bolt = 0; bolt < level.random.nextInt(4); bolt++) {
                        strike(level);
                        if (bolt > 0) nextStage++;
                    }
                }
            }
            return nextStage < lastStage() ? new Storm(target, greater, nextStage) : null;
        }

        private int lastStage() {
            return greater ? 18 : 8;
        }

        private void strike(ServerLevel level) {
            BlockPos strike = target.offset(offset(level.random), 0, offset(level.random));
            WeatherRites.strike(level, strike);
        }

        private int offset(RandomSource random) {
            return offsetOutsideCenter(random, greater ? 3 : 0, greater ? 7 : 3);
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.putLong("Target", target.asLong());
            tag.putBoolean("Greater", greater);
            tag.putInt("Stage", stage);
            return tag;
        }

        @Nullable
        public static Storm load(CompoundTag tag) {
            if (!tag.contains("Target", Tag.TAG_LONG)) return null;
            var storm = new Storm(BlockPos.of(tag.getLong("Target")), tag.getBoolean("Greater"), tag.getInt("Stage"));
            return storm.stage >= 0 && storm.stage < storm.lastStage() ? storm : null;
        }
    }

    static void strike(ServerLevel level, BlockPos strike) {
        if (!level.getWorldBorder().isWithinBounds(strike)) return;
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        keepWeatherSiteLoaded(level, strike);
        bolt.moveTo(strike.getX(), strike.getY(), strike.getZ());
        level.addFreshEntity(bolt);
    }

    static void keepWeatherSiteLoaded(ServerLevel level, BlockPos site) {
        level.getChunkSource().addRegionTicket(LIGHTNING_TICKET, new ChunkPos(site), 2, site);
        level.getChunkAt(site);
    }

    static int offsetOutsideCenter(RandomSource random, int inner, int outer) {
        int width = outer - inner;
        int offset = random.nextInt(2 * width + 1);
        return offset - outer + (offset > width ? 2 * inner : 0);
    }

    public static boolean eclipse(ServerLevel level) {
        long now = level.getGameTime();
        long cooldown = WitcheryServerConfig.RITE_OF_ECLIPSE_COOLDOWN_SECONDS.get() * 20L;
        Long last = LAST_ECLIPSE.get(level);
        if (cooldown > 0 && level.players().size() > 1 && last != null && now - last < cooldown) return false;

        ServerLevel overworld = level.getServer().overworld();
        long time = overworld.getDayTime();
        overworld.setDayTime(time - Math.floorMod(time, DAY_TICKS) + MIDNIGHT);
        LAST_ECLIPSE.put(level, now);
        return true;
    }
}
