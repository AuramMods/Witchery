package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.data.GrassSeedLootModifier;
import art.arcane.witchery.data.PredictionLootModifier;
import art.arcane.witchery.data.SmeltingTouchLootModifier;
import art.arcane.witchery.data.WolfBoneLootModifier;
import art.arcane.witchery.entity.HobgoblinMiningLootModifier;
import art.arcane.witchery.world.DreamCottonFeature;
import art.arcane.witchery.world.DreamSpiritSpawnModifier;
import art.arcane.witchery.world.HellhoundSpawnModifier;
import art.arcane.witchery.world.HobgoblinSpawnModifier;
import art.arcane.witchery.world.MirrorChunkGenerator;
import art.arcane.witchery.world.SurfaceStructuresFeature;
import art.arcane.witchery.world.VillageApothecaryProcessor;
import art.arcane.witchery.world.VillageBiomeModifier;
import art.arcane.witchery.world.TormentChunkGenerator;
import art.arcane.witchery.world.VillageBookshopProcessor;
import art.arcane.witchery.world.VillageFoundation;
import art.arcane.witchery.world.VillageWallPiece;
import art.arcane.witchery.world.WickerManFeature;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.StructureModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class WitcheryRegistries {
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Witchery.MODID);
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Witchery.MODID);
    private static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, Witchery.MODID);
    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Witchery.MODID);
    private static final DeferredRegister<Codec<? extends StructureModifier>> STRUCTURE_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS, Witchery.MODID);
    private static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Witchery.MODID);
    private static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Witchery.MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Witchery.MODID);
    public static final RegistryObject<SimpleParticleType> ALTAR_POWER =
            PARTICLES.register("altar_power", () -> new SimpleParticleType(false));
    public static final RegistryObject<Codec<HobgoblinSpawnModifier>> HOBGOBLIN_SPAWNS =
            BIOME_MODIFIERS.register("hobgoblin_spawns", () -> HobgoblinSpawnModifier.CODEC);
    public static final RegistryObject<Codec<DreamSpiritSpawnModifier>> DREAM_SPIRIT_SPAWNS =
            BIOME_MODIFIERS.register("dream_spirit_spawns", () -> DreamSpiritSpawnModifier.CODEC);
    public static final RegistryObject<Codec<HellhoundSpawnModifier>> HELLHOUND_SPAWNS =
            BIOME_MODIFIERS.register("hellhound_spawns", () -> HellhoundSpawnModifier.CODEC);
    public static final RegistryObject<Codec<VillageBiomeModifier>> VILLAGE_BIOMES =
            STRUCTURE_MODIFIERS.register("village_biomes", () -> VillageBiomeModifier.CODEC);
    public static final RegistryObject<StructureProcessorType<VillageBookshopProcessor>> BOOKSHOP_PROCESSOR =
            STRUCTURE_PROCESSORS.register("village_bookshop", () -> () -> VillageBookshopProcessor.CODEC);
    public static final RegistryObject<StructureProcessorType<VillageApothecaryProcessor>> APOTHECARY_PROCESSOR =
            STRUCTURE_PROCESSORS.register("village_apothecary", () -> () -> VillageApothecaryProcessor.CODEC);
    public static final RegistryObject<StructureProcessorType<VillageFoundation>> VILLAGE_FOUNDATION =
            STRUCTURE_PROCESSORS.register("village_foundation", () -> () -> VillageFoundation.CODEC);
    public static final RegistryObject<StructurePieceType> VILLAGE_WALL =
            STRUCTURE_PIECES.register("village_wall", () -> (StructurePieceType.ContextlessType) VillageWallPiece::new);

    static {
        LOOT_MODIFIERS.register("grass_seeds", () -> GrassSeedLootModifier.CODEC);
        LOOT_MODIFIERS.register("mining_predictions", () -> PredictionLootModifier.CODEC);
        LOOT_MODIFIERS.register("wolf_bones", () -> WolfBoneLootModifier.CODEC);
        LOOT_MODIFIERS.register("hobgoblin_mining", () -> HobgoblinMiningLootModifier.CODEC);
        LOOT_MODIFIERS.register("smelting_touch", () -> SmeltingTouchLootModifier.CODEC);
        FEATURES.register("dream_cotton", DreamCottonFeature::new);
        FEATURES.register("wicker_man", WickerManFeature::new);
        FEATURES.register("surface_structures", SurfaceStructuresFeature::new);
        CHUNK_GENERATORS.register("mirror", () -> MirrorChunkGenerator.CODEC);
        CHUNK_GENERATORS.register("torment", () -> TormentChunkGenerator.CODEC);
    }

    private WitcheryRegistries() {
    }

    public static void register(IEventBus eventBus) {
        WitcheryBlocks.register(eventBus);
        WitcheryItems.register(eventBus);
        WitcheryFluids.register(eventBus);
        WitcheryBlockEntities.register(eventBus);
        WitcheryEntities.register(eventBus);
        WitcheryApothecary.register(eventBus);
        WitcheryMenus.register(eventBus);
        WitcheryEffects.register(eventBus);
        WitcheryCreativeTabs.register(eventBus);
        WitcheryRecipes.register(eventBus);
        WitcherySounds.register(eventBus);
        LOOT_MODIFIERS.register(eventBus);
        FEATURES.register(eventBus);
        CHUNK_GENERATORS.register(eventBus);
        BIOME_MODIFIERS.register(eventBus);
        STRUCTURE_MODIFIERS.register(eventBus);
        STRUCTURE_PROCESSORS.register(eventBus);
        STRUCTURE_PIECES.register(eventBus);
        PARTICLES.register(eventBus);
    }
}
