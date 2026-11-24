package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.PoppetShelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class LegacyShelfTickets {
    private LegacyShelfTickets() {}

    @SubscribeEvent
    public static void restoreAfterImport(ServerStartedEvent event) {
        if (!LegacyWorldIds.isImportRequested()) return;
        var server = event.getServer();
        Path root = server.getWorldPath(LevelResource.ROOT);
        for (ServerLevel level : server.getAllLevels()) {
            restore(level, DimensionType.getStorageFolder(level.dimension(), root).resolve("forcedchunks.dat"));
        }
    }

    private static void restore(ServerLevel level, Path source) {
        CompoundTag data;
        try (var input = new DataInputStream(new BufferedInputStream(Files.newInputStream(source)))) {
            // Forge 1.7.10 wrote this file as uncompressed NBT.
            data = NbtIo.read(input);
        } catch (NoSuchFileException missing) {
            return;
        } catch (IOException unavailable) {
            Witchery.LOGGER.warn("Could not read legacy Poppet Shelf tickets from {}", source, unavailable);
            return;
        }

        var shelves = new HashSet<BlockPos>();
        for (Tag ownerEntry : data.getList("TicketList", Tag.TAG_COMPOUND)) {
            var owner = (CompoundTag) ownerEntry;
            if (!Witchery.MODID.equals(owner.getString("Owner"))) continue;
            for (Tag ticket : owner.getList("Tickets", Tag.TAG_COMPOUND)) {
                var position = ((CompoundTag) ticket).getCompound("ModData");
                if (!position.contains("poppetX", Tag.TAG_INT) || !position.contains("poppetY", Tag.TAG_INT)
                        || !position.contains("poppetZ", Tag.TAG_INT)) continue;
                shelves.add(new BlockPos(position.getInt("poppetX"), position.getInt("poppetY"), position.getInt("poppetZ")));
            }
        }
        for (BlockPos pos : shelves) {
            if (Level.isInSpawnableBounds(pos) && !level.isOutsideBuildHeight(pos)
                    && level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity shelf) {
                shelf.keepChunkLoaded();
            }
        }
    }
}
