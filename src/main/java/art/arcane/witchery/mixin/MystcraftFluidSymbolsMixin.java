package art.arcane.witchery.mixin;

import art.arcane.witchery.Witchery;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.Set;

@Pseudo
@Mixin(targets = "art.arcane.mystcraft.datapack.symbol.SymbolDatapackLoader", remap = false)
abstract class MystcraftFluidSymbolsMixin {
    @Unique
    private static final Set<String> witchery$excludedSeaFluids = Set.of(
            "fluidspirit", "hollowtears", "brew", "brewliquid", "brewgas");

    @Redirect(method = "registerFluidSeaSymbols", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/DefaultedRegistry;iterator()Ljava/util/Iterator;"))
    private static Iterator<Fluid> witchery$skipRitualFluids(DefaultedRegistry<Fluid> fluids) {
        return fluids.stream().filter(fluid -> {
            ResourceLocation id = fluids.getKey(fluid);
            return id == null || !Witchery.MODID.equals(id.getNamespace()) || !witchery$excludedSeaFluids.contains(id.getPath());
        }).iterator();
    }
}
