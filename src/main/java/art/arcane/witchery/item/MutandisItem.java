package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.SpanishMossBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.config.ConfiguredBlockState;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class MutandisItem extends Item {
    private static final TagKey<Block> PLANTS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "mutandis_plants"));
    private static final TagKey<Block> EXTREMIS_PLANTS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "mutandis_extremis_plants"));
    private static final List<Block> POTTED_PLANTS = List.of(Blocks.POTTED_POPPY, Blocks.POTTED_DANDELION, Blocks.POTTED_OAK_SAPLING,
            Blocks.POTTED_SPRUCE_SAPLING, Blocks.POTTED_BIRCH_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, Blocks.POTTED_RED_MUSHROOM,
            Blocks.POTTED_BROWN_MUSHROOM, Blocks.POTTED_CACTUS, Blocks.POTTED_DEAD_BUSH, Blocks.POTTED_FERN);

    private final boolean extremis;

    public MutandisItem(Properties properties, boolean extremis) {
        super(properties);
        this.extremis = extremis;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (POTTED_PLANTS.contains(existing.getBlock())) {
            return mutate(context, existing, true);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState existing = level.getBlockState(pos);
        if (extremis) {
            if (existing.is(Blocks.GRASS_BLOCK) || existing.is(Blocks.MYCELIUM)) {
                if (level instanceof ServerLevel server) {
                    if (server.random.nextInt(2) == 0) {
                        Block replacement = existing.is(Blocks.GRASS_BLOCK) ? Blocks.MYCELIUM : Blocks.GRASS_BLOCK;
                        server.setBlockAndUpdate(pos, replacement.defaultBlockState());
                    }
                    WitcheryNetwork.sendMutandisFizz(server, pos.above());
                    consumeDose(context);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (isSoakedDirt(level, pos)) {
                if (level instanceof ServerLevel server) {
                    if (server.random.nextInt(2) == 0) {
                        makeClayCross(server, pos);
                    } else {
                        WitcheryNetwork.sendMutandisFizz(server, pos.above());
                    }
                    consumeDose(context);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return mutate(context, existing, false);
    }

    private InteractionResult mutate(UseOnContext context, BlockState existing, boolean potted) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Block sapling = WitcheryBlocks.getRequired("witchsapling").get();
        boolean dream = level.dimension().equals(WitcheryDimensions.DREAM);
        List<BlockState> extras = potted ? List.of() : additionalBlocks();
        boolean basePlant = existing.is(PLANTS) || existing.is(sapling)
                || (extremis && existing.is(EXTREMIS_PLANTS)) || (dream && existing.is(Blocks.NETHER_WART));
        if (!potted && !basePlant && !extras.remove(existing)) return InteractionResult.PASS;

        if (level instanceof ServerLevel server) {
            List<BlockState> candidates = new ArrayList<>();
            if (potted) {
                for (Block block : POTTED_PLANTS) {
                    if (!existing.is(block)) {
                        candidates.add(block.defaultBlockState());
                    }
                }
            } else {
                for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(PLANTS)) {
                    Block block = holder.value();
                    if (block == sapling) {
                        addWitchwoodVariants(existing, candidates, sapling);
                    } else if (!existing.is(block)) {
                        candidates.add(block.defaultBlockState());
                    }
                }
                if (!sapling.defaultBlockState().is(PLANTS)) {
                    addWitchwoodVariants(existing, candidates, sapling);
                }
                candidates.addAll(extras);
                if (extremis) {
                    addExtremisPlants(existing, candidates);
                } else if (dream && !existing.is(Blocks.NETHER_WART)) {
                    candidates.add(Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, 3));
                }
            }
            if (candidates.isEmpty()) return InteractionResult.FAIL;
            BlockState replacement = candidates.get(server.random.nextInt(candidates.size()));
            if (replacement.getBlock() instanceof SpanishMossBlock moss) replacement = moss.mutatedState(server, pos);
            if (replacement != existing && !server.setBlockAndUpdate(pos, replacement)) return InteractionResult.FAIL;
            WitcheryNetwork.sendMutandisFizz(server, pos);
            consumeDose(context);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void addWitchwoodVariants(BlockState existing, List<BlockState> candidates, Block sapling) {
        for (WitchWoodType wood : WitchWoodType.values()) {
            if (!existing.is(sapling) || existing.getValue(WitchWoodType.PROPERTY) != wood) {
                candidates.add(sapling.defaultBlockState().setValue(WitchWoodType.PROPERTY, wood));
            }
        }
    }

    private static List<BlockState> additionalBlocks() {
        List<BlockState> extras = new ArrayList<>();
        for (String entry : WitcheryServerConfig.MUTANDIS_ADDITIONAL_BLOCKS.get()) {
            BlockState state = ConfiguredBlockState.parse(entry);
            if (state != null) extras.add(state);
        }
        return extras;
    }

    private static void addExtremisPlants(BlockState existing, List<BlockState> candidates) {
        IntegerProperty inputAge = ageProperty(existing);
        int age = inputAge == null ? 0 : existing.getValue(inputAge);
        for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(EXTREMIS_PLANTS)) {
            Block block = holder.value();
            if (existing.is(block)) {
                continue;
            }
            BlockState candidate = block.defaultBlockState();
            IntegerProperty ageProperty = ageProperty(candidate);
            if (ageProperty != null && block != Blocks.CACTUS) {
                int maxAge = block instanceof CropBlock crop ? Math.min(7, crop.getMaxAge()) : 7;
                int target = ageProperty.getPossibleValues().stream().mapToInt(Integer::intValue)
                        .filter(value -> value <= Math.min(age, maxAge)).max().orElse(candidate.getValue(ageProperty));
                candidate = candidate.setValue(ageProperty, target);
            }
            candidates.add(candidate);
        }
    }

    @Nullable
    private static IntegerProperty ageProperty(BlockState state) {
        return state.getBlock().getStateDefinition().getProperty("age") instanceof IntegerProperty age ? age : null;
    }

    static boolean isSoakedDirt(Level level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos);
        return (soil.is(Blocks.DIRT) || soil.is(Blocks.COARSE_DIRT) || soil.is(Blocks.PODZOL))
                && level.getBlockState(pos.above()).is(Blocks.WATER);
    }

    static void makeClayCross(ServerLevel level, BlockPos pos) {
        makeClay(level, pos);
        makeClay(level, pos.east());
        makeClay(level, pos.west());
        makeClay(level, pos.south());
        makeClay(level, pos.north());
    }

    private static void makeClay(ServerLevel level, BlockPos pos) {
        if (isSoakedDirt(level, pos) && level.setBlockAndUpdate(pos, Blocks.CLAY.defaultBlockState())) {
            WitcheryNetwork.sendClayMutationCue(level, pos);
        }
    }

    private static void consumeDose(UseOnContext context) {
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
    }
}
