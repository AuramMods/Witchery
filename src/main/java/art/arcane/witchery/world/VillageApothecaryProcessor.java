package art.arcane.witchery.world;

import art.arcane.witchery.registry.WitcheryApothecary;
import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public final class VillageApothecaryProcessor extends StructureProcessor {
    public static final Codec<VillageApothecaryProcessor> CODEC = Codec.unit(new VillageApothecaryProcessor());

    private VillageApothecaryProcessor() {}

    @Override
    public StructureTemplate.StructureEntityInfo processEntity(LevelReader level, BlockPos origin,
            StructureTemplate.StructureEntityInfo raw, StructureTemplate.StructureEntityInfo placed,
            StructurePlaceSettings settings, StructureTemplate template) {
        if (!WitcheryApothecary.PROFESSION.getId().toString().equals(raw.nbt.getCompound("VillagerData").getString("profession"))) {
            return placed;
        }
        var tag = placed.nbt.copy();
        tag.put("Offers", WitcheryApothecary.residentOffers().createTag());
        return new StructureTemplate.StructureEntityInfo(placed.pos, placed.blockPos, tag);
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level, BlockPos origin,
            BlockPos pivot, List<StructureTemplate.StructureBlockInfo> raw,
            List<StructureTemplate.StructureBlockInfo> placed, StructurePlaceSettings settings) {
        if (raw.stream().noneMatch(block -> block.state().is(Blocks.CHEST))) return placed;
        return VillageFoundation.fill(level, origin, settings, placed, 0, 8, 1, 6);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return WitcheryRegistries.APOTHECARY_PROCESSOR.get();
    }
}
