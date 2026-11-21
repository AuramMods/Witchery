package art.arcane.witchery.world;

import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;

public final class VillageFoundation extends StructureProcessor {
    public static final Codec<VillageFoundation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("min_x").forGetter(processor -> processor.minX),
            Codec.INT.fieldOf("max_x").forGetter(processor -> processor.maxX),
            Codec.INT.fieldOf("min_z").forGetter(processor -> processor.minZ),
            Codec.INT.fieldOf("max_z").forGetter(processor -> processor.maxZ)
    ).apply(instance, VillageFoundation::new));

    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;

    private VillageFoundation(int minX, int maxX, int minZ, int maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level, BlockPos origin,
            BlockPos pivot, List<StructureTemplate.StructureBlockInfo> raw,
            List<StructureTemplate.StructureBlockInfo> placed, StructurePlaceSettings settings) {
        // Jigsaw placement also runs the processors on data markers alone.
        if (raw.stream().noneMatch(block -> block.pos().getY() == 0 && block.state().is(Blocks.COBBLESTONE))) return placed;
        var blocks = new ArrayList<>(placed);
        var bounds = settings.getBoundingBox();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                var base = StructureTemplate.calculateRelativePosition(settings, new BlockPos(x, -1, z)).offset(origin);
                for (BlockPos pos = base; pos.getY() > level.getMinBuildHeight() + 1; pos = pos.below()) {
                    if (bounds != null && !bounds.isInside(pos)) break;
                    var floor = level.getBlockState(pos);
                    if (!floor.isAir() && !floor.liquid()) break;
                    blocks.add(new StructureTemplate.StructureBlockInfo(pos, Blocks.COBBLESTONE.defaultBlockState(), null));
                }
            }
        }
        return blocks;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return WitcheryRegistries.VILLAGE_FOUNDATION.get();
    }
}
