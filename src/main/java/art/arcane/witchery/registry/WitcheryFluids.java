package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
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
                              RegistryObject<FlowingFluid> flowing) {
    }

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Witchery.MODID);
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Witchery.MODID);

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

    private static void registerLegacyFluid(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        if (LEGACY_FLUIDS.containsKey(path)) {
            return;
        }

        RegistryObject<FluidType> fluidType = FLUID_TYPES.register(path,
                () -> new FluidType(FluidType.Properties.create()) {
                });

        final ForgeFlowingFluid.Properties[] properties = new ForgeFlowingFluid.Properties[1];

        RegistryObject<FlowingFluid> source = FLUIDS.register(path,
                () -> new ForgeFlowingFluid.Source(properties[0]));
        RegistryObject<FlowingFluid> flowing = FLUIDS.register("flowing_" + path,
                () -> new ForgeFlowingFluid.Flowing(properties[0]));

        properties[0] = new ForgeFlowingFluid.Properties(fluidType, source, flowing)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1);

        LEGACY_FLUIDS.put(path, new LegacyFluid(fluidType, source, flowing));
    }
}
