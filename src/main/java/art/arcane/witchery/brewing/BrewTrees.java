package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class BrewTrees {
    private BrewTrees() {}

    enum Species {
        OAK("oak", TreeFeatures.OAK),
        SPRUCE("spruce", TreeFeatures.SPRUCE),
        BIRCH("birch", TreeFeatures.BIRCH),
        JUNGLE("jungle", TreeFeatures.JUNGLE_TREE_NO_VINE),
        ACACIA("acacia", TreeFeatures.ACACIA),
        DARK_OAK("dark_oak", TreeFeatures.DARK_OAK),
        ROWAN(WitchWoodType.ROWAN),
        ALDER(WitchWoodType.ALDER),
        HAWTHORN(WitchWoodType.HAWTHORN);

        final ResourceLocation key;
        final String nameKey;
        private final ResourceKey<ConfiguredFeature<?, ?>> feature;
        @Nullable private final WitchWoodType wood;

        Species(String name, ResourceKey<ConfiguredFeature<?, ?>> feature) {
            this.key = ResourceLocation.withDefaultNamespace(name + "_sapling");
            this.nameKey = "brew.witchery.tree_" + name;
            this.feature = feature;
            this.wood = null;
        }

        Species(WitchWoodType wood) {
            String name = wood.getSerializedName();
            this.key = witchSaplingKey(wood);
            this.nameKey = "brew.witchery.tree_" + name;
            this.feature = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Witchery.MODID, name));
            this.wood = wood;
        }

        ItemStack ingredient() {
            return wood != null ? wood.stack(WitcheryBlocks.getRequired("witchsapling").get())
                    : new ItemStack(BuiltInRegistries.ITEM.get(key));
        }

        ResourceKey<ConfiguredFeature<?, ?>> feature(int strength, RandomSource random) {
            return switch (this) {
                case OAK -> strength > 1 ? TreeFeatures.FANCY_OAK : feature;
                case SPRUCE -> strength > 1 ? (random.nextBoolean() ? TreeFeatures.MEGA_SPRUCE : TreeFeatures.MEGA_PINE) : feature;
                case JUNGLE -> strength > 2 ? TreeFeatures.MEGA_JUNGLE_TREE : feature;
                default -> feature;
            };
        }
    }

    static ResourceLocation witchSaplingKey(WitchWoodType wood) {
        return ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "witchsapling/" + wood.getSerializedName());
    }

    static void grow(ServerLevel level, BlockPos impact, Direction face, Species species, int strength, @Nullable Player caster) {
        BlockPos pos = BrewPlantEffects.plantSite(level, impact, face, false);
        if (pos == null || !level.isInWorldBounds(pos) || !level.hasChunkAt(pos)) return;
        BlockItem sapling = (BlockItem) species.ingredient().getItem();
        if (!sapling.getBlock().defaultBlockState().canSurvive(level, pos)) return;
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        var random = level.getRandom();
        var features = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        var feature = features.getHolder(species.feature(strength, random)).orElse(null);
        var event = ForgeEventFactory.blockGrowFeature(level, random, pos, feature);
        if (event.getResult() != Event.Result.DENY && event.getFeature() != null) {
            place(level, pos, face, actor, event.getFeature().value(), random);
        }
    }

    private static void place(ServerLevel level, BlockPos pos, Direction face, Player actor,
                              ConfiguredFeature<?, ?> feature, RandomSource random) {
        boolean alreadyCapturing = level.captureBlockSnapshots;
        int first = level.capturedBlockSnapshots.size();
        boolean placed = false;
        List<BlockSnapshot> changes;
        level.captureBlockSnapshots = true;
        try {
            if (feature.place(level, level.getChunkSource().getGenerator(), random, pos)) {
                changes = List.copyOf(level.capturedBlockSnapshots.subList(first, level.capturedBlockSnapshots.size()));
                placed = !changes.isEmpty() && changes.stream().allMatch(change ->
                        BrewTerrain.canChange(actor, level, change.getPos(), change.getReplacedBlock()))
                        && !ForgeEventFactory.onMultiBlockPlace(actor, changes, face);
            }
        } finally {
            level.captureBlockSnapshots = alreadyCapturing;
            var captured = level.capturedBlockSnapshots.subList(first, level.capturedBlockSnapshots.size());
            changes = List.copyOf(captured);
            if (!alreadyCapturing || !placed) captured.clear();
            if (!placed) restore(level, changes);
        }
        if (placed && !alreadyCapturing) {
            for (BlockSnapshot change : changes) {
                BlockPos changed = change.getPos();
                var state = level.getBlockState(changed);
                var previous = change.getReplacedBlock();
                state.onPlace(level, changed, previous, false);
                level.markAndNotifyBlock(changed, level.getChunkAt(changed), previous, state, change.getFlag(), 512);
            }
        }
    }

    private static void restore(ServerLevel level, List<BlockSnapshot> changes) {
        boolean capturing = level.captureBlockSnapshots;
        boolean restoring = level.restoringBlockSnapshots;
        level.captureBlockSnapshots = false;
        level.restoringBlockSnapshots = true;
        try {
            for (int i = changes.size() - 1; i >= 0; i--) changes.get(i).restore(true, false);
        } finally {
            level.captureBlockSnapshots = capturing;
            level.restoringBlockSnapshots = restoring;
        }
    }
}
