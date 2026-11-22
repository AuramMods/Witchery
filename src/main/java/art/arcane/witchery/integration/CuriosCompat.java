package art.arcane.witchery.integration;

import art.arcane.witchery.Witchery;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosCompat {
    private static final TagKey<Item> VAMPIRE_MORTALITY_ACCESSORIES = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "vampire_mortality_accessories"));

    private CuriosCompat() {}

    public static boolean disablesVampireProtection(ServerPlayer player) {
        return ModList.get().isLoaded("curios") && Hooks.hasMortalityAccessory(player);
    }

    private static final class Hooks {
        static boolean hasMortalityAccessory(ServerPlayer player) {
            return CuriosApi.getCuriosInventory(player).map(inventory -> inventory.findFirstCurio(
                    stack -> stack.is(VAMPIRE_MORTALITY_ACCESSORIES)).isPresent()).orElse(false);
        }
    }
}
