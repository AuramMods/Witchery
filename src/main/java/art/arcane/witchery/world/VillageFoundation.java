package art.arcane.witchery.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;

final class VillageFoundation {
    private VillageFoundation() {}

    static List<StructureTemplate.StructureBlockInfo> fill(ServerLevelAccessor level, BlockPos origin,
            StructurePlaceSettings settings, List<StructureTemplate.StructureBlockInfo> placed,
            int minX, int maxX, int minZ, int maxZ) {
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
}
