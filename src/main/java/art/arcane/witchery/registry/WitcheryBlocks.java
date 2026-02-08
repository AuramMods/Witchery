package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WitcheryBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Witchery.MODID);
    public static final Map<String, RegistryObject<Block>> LEGACY_BLOCKS = new LinkedHashMap<>();

    static {
        for (String legacyName : LegacyRegistryData.BLOCKS) {
            registerLegacyBlock(legacyName);
        }
    }

    private WitcheryBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static RegistryObject<Block> getRequired(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        RegistryObject<Block> block = LEGACY_BLOCKS.get(path);
        if (block == null) {
            throw new IllegalStateException("Missing legacy block placeholder for path: " + path);
        }
        return block;
    }

    public static Collection<RegistryObject<Block>> all() {
        return Collections.unmodifiableCollection(LEGACY_BLOCKS.values());
    }

    private static void registerLegacyBlock(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        if (LEGACY_BLOCKS.containsKey(path)) {
            return;
        }

        RegistryObject<Block> block = BLOCKS.register(path, () -> createBlock(path));
        LEGACY_BLOCKS.put(path, block);
    }

    private static Block createBlock(String path) {
        return switch (path) {
            case "witchlog" -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG));
            case "icedoor" -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_DOOR).noOcclusion(), BlockSetType.IRON);
            case "mindrake" -> new LegacyMindrakeCropBlock();
            case "wolftrap" -> new LegacyWolftrapBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion());
            case "wallgen" -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .noLootTable()
                    .noCollission());
            default -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE));
        };
    }

    private static final class LegacyMindrakeCropBlock extends CropBlock {
        private LegacyMindrakeCropBlock() {
            super(BlockBehaviour.Properties.copy(Blocks.WHEAT));
        }

        @Override
        protected Item getBaseSeedId() {
            return net.minecraft.world.item.Items.WHEAT_SEEDS;
        }
    }

    private static final class LegacyWolftrapBlock extends HorizontalDirectionalBlock {
        private LegacyWolftrapBlock(BlockBehaviour.Properties properties) {
            super(properties.sound(SoundType.METAL));
            registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }
    }
}
