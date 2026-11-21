package art.arcane.witchery.world;

import art.arcane.witchery.registry.WitcheryApothecary;
import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

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
    protected StructureProcessorType<?> getType() {
        return WitcheryRegistries.APOTHECARY_PROCESSOR.get();
    }
}
