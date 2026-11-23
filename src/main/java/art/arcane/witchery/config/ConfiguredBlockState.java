package art.arcane.witchery.config;

import art.arcane.witchery.world.migration.LegacySchematicBlocks;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ConfiguredBlockState {
    private ConfiguredBlockState() {}

    @Nullable
    public static BlockState parse(String entry) {
        entry = entry.strip();
        int separator = entry.lastIndexOf(',');
        if (separator >= 0 && !entry.contains("[")) {
            try {
                int metadata = Integer.parseInt(entry.substring(separator + 1).strip());
                return metadata >= 0 && metadata <= 15
                        ? LegacySchematicBlocks.read(entry.substring(0, separator).strip(), metadata, 0) : null;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        try {
            var reader = new StringReader(entry);
            var block = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), reader, false);
            return reader.canRead() ? null : block.blockState();
        } catch (CommandSyntaxException ignored) {
            return LegacySchematicBlocks.read(entry, 0, 0);
        }
    }
}
