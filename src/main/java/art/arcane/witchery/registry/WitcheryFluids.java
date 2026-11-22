package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.DiseaseBlock;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.item.BrewBucketItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class WitcheryFluids {
    public record LegacyFluid(RegistryObject<FluidType> fluidType,
                              RegistryObject<FlowingFluid> source,
                              RegistryObject<FlowingFluid> flowing,
                              String blockPath,
                              String bucketPath) {
    }

    private record LegacyFluidConfig(int density,
                                     int viscosity,
                                     String blockPath,
                                     String bucketPath,
                                     String stillTexture,
                                     String flowingTexture) {
    }

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Witchery.MODID);
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Witchery.MODID);

    private static final Map<String, LegacyFluidConfig> LEGACY_FLUID_CONFIGS = Map.of(
            "fluidspirit", new LegacyFluidConfig(500, 2000, "spiritflowing", "bucketspirit", "flowspirit_still", "flowspirit_flow"),
            "hollowtears", new LegacyFluidConfig(100, 1500, "hollowtears", "buckethollowtears", "tears_still", "tears_flow"),
            "brew", new LegacyFluidConfig(100, 1500, "brew", "bucketbrew", "brew_still", "brew_flow"),
            "brewgas", new LegacyFluidConfig(1000, 1000, "brewgas", null, "brewgas", "brewgas"),
            "brewliquid", new LegacyFluidConfig(1000, 1000, "brewliquid", null, "brew_still", "brew_flow"),
            "fluiddisease", new LegacyFluidConfig(50, 3000, "disease", null, "disease_still", "disease_flow")
    );

    public static final Map<String, LegacyFluid> LEGACY_FLUIDS = new LinkedHashMap<>();

    static {
        for (String legacyName : LegacyRegistryData.FLUIDS) {
            registerLegacyFluid(legacyName);
        }
    }

    private WitcheryFluids() {
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }

    public static Map<String, LegacyFluid> all() {
        return Collections.unmodifiableMap(LEGACY_FLUIDS);
    }

    public static LegacyFluid getRequired(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        LegacyFluid fluid = LEGACY_FLUIDS.get(path);
        if (fluid == null) {
            throw new IllegalStateException("Missing legacy fluid placeholder for path: " + path);
        }
        return fluid;
    }

    public static RegistryObject<FlowingFluid> getRequiredSource(String legacyName) {
        return getRequired(legacyName).source();
    }

    public static RegistryObject<FlowingFluid> getRequiredFlowing(String legacyName) {
        return getRequired(legacyName).flowing();
    }

    private static void registerLegacyFluid(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        if (LEGACY_FLUIDS.containsKey(path)) {
            return;
        }

        LegacyFluidConfig config = LEGACY_FLUID_CONFIGS.get(path);
        if (config == null) {
            throw new IllegalStateException("Missing fluid config for legacy fluid: " + path);
        }

        FluidType.Properties fluidProperties = FluidType.Properties.create()
                .density(config.density())
                .viscosity(config.viscosity());

        if (path.equals("fluidspirit") || path.equals("hollowtears")) {
            fluidProperties.lightLevel(3);
        }
        ResourceLocation still = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "block/" + config.stillTexture());
        ResourceLocation flow = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "block/" + config.flowingTexture());
        boolean dispersal = path.equals("brewgas") || path.equals("brewliquid");
        boolean brew = path.equals("brew");
        RegistryObject<FluidType> fluidType = FLUID_TYPES.register(path,
                () -> new FluidType(fluidProperties) {
                    @Override
                    public ItemStack getBucket(FluidStack stack) {
                        return brew ? BrewBucketItem.filled(stack) : super.getBucket(stack);
                    }

                    @Override
                    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                        consumer.accept(new IClientFluidTypeExtensions() {
                            @Override
                            public ResourceLocation getStillTexture() {
                                return still;
                            }

                            @Override
                            public ResourceLocation getFlowingTexture() {
                                return flow;
                            }

                            @Override
                            public int getTintColor(FluidState state, BlockAndTintGetter level, BlockPos pos) {
                                if (dispersal && level.getBlockEntity(pos) instanceof BrewFluidBlockEntity fluid) {
                                    return 0xFF000000 | fluid.color();
                                }
                                return getTintColor();
                            }

                            @Override
                            public int getTintColor(FluidStack stack) {
                                var tag = stack.getTag();
                                return brew && tag != null && tag.contains("Color") ? 0xFF000000 | tag.getInt("Color") : getTintColor();
                            }
                        });
                    }
                });

        final ForgeFlowingFluid.Properties[] properties = new ForgeFlowingFluid.Properties[1];

        RegistryObject<FlowingFluid> source = FLUIDS.register(path, () -> dispersal
                ? new ForgeFlowingFluid.Source(properties[0]) {
                    @Override
                    public void tick(Level level, BlockPos pos, FluidState state) {
                        // The block owns spreading and preserves the mixture on every new cell.
                    }
                } : brew ? new ForgeFlowingFluid.Source(properties[0]) {
                    @Override
                    protected boolean canSpreadTo(BlockGetter level, BlockPos pos, BlockState state, Direction direction,
                                                  BlockPos target, BlockState targetState, FluidState existing, Fluid incoming) {
                        return acceptsBrew(existing, incoming)
                                && super.canSpreadTo(level, pos, state, direction, target, targetState, existing, incoming);
                    }
                } : new ForgeFlowingFluid.Source(properties[0]));
        RegistryObject<FlowingFluid> flowing = FLUIDS.register("flowing_" + path, () -> dispersal
                ? new ForgeFlowingFluid.Flowing(properties[0]) {
                    @Override
                    public void tick(Level level, BlockPos pos, FluidState state) {}
                } : brew ? new ForgeFlowingFluid.Flowing(properties[0]) {
                    @Override
                    protected boolean canSpreadTo(BlockGetter level, BlockPos pos, BlockState state, Direction direction,
                                                  BlockPos target, BlockState targetState, FluidState existing, Fluid incoming) {
                        return acceptsBrew(existing, incoming)
                                && super.canSpreadTo(level, pos, state, direction, target, targetState, existing, incoming);
                    }
                } : new ForgeFlowingFluid.Flowing(properties[0]));

        properties[0] = new ForgeFlowingFluid.Properties(fluidType, source, flowing)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1);
        if (path.equals("fluiddisease")) {
            properties[0].levelDecreasePerBlock(8).tickRate(DiseaseBlock.TICK_INTERVAL);
        } else if (brew) {
            properties[0].levelDecreasePerBlock(4).tickRate(config.viscosity() / 200);
        }

        if (config.blockPath() != null) {
            properties[0].block(() -> (LiquidBlock) WitcheryBlocks.getRequired(config.blockPath()).get());
        }
        if (config.bucketPath() != null) {
            properties[0].bucket(() -> WitcheryItems.getRequired(config.bucketPath()).get());
        }

        LEGACY_FLUIDS.put(path, new LegacyFluid(fluidType, source, flowing, config.blockPath(), config.bucketPath()));
    }

    private static boolean acceptsBrew(FluidState existing, Fluid incoming) {
        return existing.isEmpty() || existing.getType().isSame(incoming);
    }
}
