package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.HobgoblinSpawnModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class HobgoblinSpawnTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_hobgoblin_spawn_overlap")
    public static void matchingHabitatsUpdateTheFirstSpawnEntry(GameTestHelper helper) {
        var biome = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.SWAMP);
        HolderSet<Biome> matching = HolderSet.direct(biome);
        Optional<HolderSet<Biome>> overlap = Optional.of(matching);
        Optional<HolderSet<Biome>> absent = Optional.empty();
        int baseWeight = WitcheryServerConfig.HOBGOBLIN_SPAWN_WEIGHT.get();
        int plainsWeight = Math.max(1, baseWeight - 1);

        var forest = new HobgoblinSpawnModifier(matching, 3, false, absent, overlap, overlap);
        var swamp = new HobgoblinSpawnModifier(matching, 2, false, overlap, overlap, absent);
        var plains = new HobgoblinSpawnModifier(matching, 2, true, absent, absent, absent);
        var triple = groups(biome, List.of(swamp, forest, plains));
        helper.assertTrue(triple.equals(List.of(new Group(plainsWeight, 2), new Group(baseWeight, 3), new Group(plainsWeight, 2))),
                "With three habitats, Plains replaces the first entry and leaves the Forest entry in the middle");

        var withoutPlains = new HobgoblinSpawnModifier(matching, 2, false, overlap, absent, absent);
        var swampAndForest = groups(biome, List.of(withoutPlains, forest));
        helper.assertTrue(swampAndForest.equals(List.of(new Group(baseWeight, 3), new Group(baseWeight, 3))),
                "When Swamp and Forest overlap, both entries use the Forest group size");

        var withoutSwamp = new HobgoblinSpawnModifier(matching, 3, false, absent, overlap, absent);
        var forestAndPlains = groups(biome, List.of(withoutSwamp, plains));
        helper.assertTrue(forestAndPlains.equals(List.of(new Group(plainsWeight, 2), new Group(plainsWeight, 2))),
                "When Forest and Plains overlap, both entries use the Plains weight and group size");
        helper.succeed();
    }

    private static List<Group> groups(Holder<Biome> biome, List<HobgoblinSpawnModifier> modifiers) {
        var builder = ModifiableBiomeInfo.BiomeInfo.Builder.copyOf(biome.value().modifiableBiomeInfo().getOriginalBiomeInfo());
        for (var modifier : modifiers) modifier.modify(biome, BiomeModifier.Phase.ADD, builder);
        return builder.getMobSpawnSettings().getSpawner(MobCategory.CREATURE).stream()
                .filter(entry -> entry.type == WitcheryEntities.GOBLIN.get())
                .map(entry -> new Group(entry.getWeight().asInt(), entry.maxCount))
                .toList();
    }

    private record Group(int weight, int maxCount) {
    }
}
