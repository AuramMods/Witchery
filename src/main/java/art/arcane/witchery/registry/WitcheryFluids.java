package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
                                     String bucketPath) {
    }

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Witchery.MODID);
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Witchery.MODID);

    private static final Map<String, LegacyFluidConfig> LEGACY_FLUID_CONFIGS = Map.of(
            "fluidspirit", new LegacyFluidConfig(500, 2000, "spiritflowing", "bucketspirit"),
            "hollowtears", new LegacyFluidConfig(100, 1500, "hollowtears", "buckethollowtears"),
            "brew", new LegacyFluidConfig(100, 1500, "brew", "bucketbrew"),
            "brewgas", new LegacyFluidConfig(1000, 1000, "brewgas", null),
            "brewliquid", new LegacyFluidConfig(1000, 1000, "brewliquid", null),
            "fluiddisease", new LegacyFluidConfig(50, 3000, "disease", null)
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

        RegistryObject<FluidType> fluidType = FLUID_TYPES.register(path,
                () -> new FluidType(fluidProperties) {
                });

        final ForgeFlowingFluid.Properties[] properties = new ForgeFlowingFluid.Properties[1];

        RegistryObject<FlowingFluid> source = FLUIDS.register(path,
                () -> new ForgeFlowingFluid.Source(properties[0]));
        RegistryObject<FlowingFluid> flowing = FLUIDS.register("flowing_" + path,
                () -> new ForgeFlowingFluid.Flowing(properties[0]));

        properties[0] = new ForgeFlowingFluid.Properties(fluidType, source, flowing)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1);

        if (config.blockPath() != null) {
            properties[0].block(() -> (LiquidBlock) WitcheryBlocks.getRequired(config.blockPath()).get());
        }
        if (config.bucketPath() != null) {
            properties[0].bucket(() -> (BucketItem) WitcheryItems.getRequired(config.bucketPath()).get());
        }

        LEGACY_FLUIDS.put(path, new LegacyFluid(fluidType, source, flowing, config.blockPath(), config.bucketPath()));
    }
}
