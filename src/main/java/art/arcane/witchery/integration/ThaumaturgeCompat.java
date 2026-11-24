package art.arcane.witchery.integration;

import art.arcane.witchery.config.WitcheryCommonConfig;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class ThaumaturgeCompat {
    private static final String MOD_ID = "thaumaturge";
    static final ResourceKey<Registry<Object>> ASPECTS =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "aspect"));

    private ThaumaturgeCompat() {}

    public static void drain(ServerPlayer player, float fraction) {
        if (ModList.get().isLoaded(MOD_ID) && WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get()) {
            Wands.drain(player, fraction);
        }
    }

    public static boolean excludesGeneration(LevelAccessor level) {
        if (!(level instanceof ServerLevelAccessor server) || !WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get()) return false;
        var dimension = server.getLevel().dimension();
        return WitcheryDimensions.DREAM.equals(dimension) || WitcheryDimensions.TORMENT.equals(dimension);
    }

    public static boolean isThaumaturgeContent(@Nullable ResourceLocation id) {
        return id != null && MOD_ID.equals(id.getNamespace());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void filterNaturalSpawns(LevelEvent.PotentialSpawns event) {
        if (!excludesGeneration(event.getLevel())) return;
        for (var entry : List.copyOf(event.getSpawnerDataList())) {
            if (isThaumaturgeContent(ForgeRegistries.ENTITY_TYPES.getKey(entry.type))) {
                event.removeSpawnerData(entry);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void filterGeneratedMobs(MobSpawnEvent.FinalizeSpawn event) {
        var reason = event.getSpawnType();
        if ((reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION || reason == MobSpawnType.STRUCTURE)
                && excludesGeneration(event.getLevel())
                && isThaumaturgeContent(ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()))) {
            event.setSpawnCancelled(true);
        }
    }

    private record WandApi(Class<?> itemType, Method getVis, Method storeVis, Method consumptionModifier) {
        private static WandApi bind() {
            try {
                String packageName = "com.leclowndu93150.thaumaturge.content.wands.";
                Class<?> helper = Class.forName(packageName + "WandVisHelper");
                return new WandApi(Class.forName(packageName + "ItemWand"),
                        helper.getMethod("getVis", ItemStack.class, ResourceKey.class),
                        helper.getMethod("storeVis", ItemStack.class, ResourceKey.class, int.class),
                        helper.getMethod("getConsumptionModifier", ItemStack.class, Player.class,
                                ResourceKey.class, boolean.class));
            } catch (ReflectiveOperationException | LinkageError exception) {
                Witchery.LOGGER.warn("Could not bind Thaumaturge's wand Vis API", exception);
                return null;
            }
        }
    }

    private static final class Wands {
        private static final WandApi API = WandApi.bind();
        private static boolean reportedError;

        private static void drain(ServerPlayer player, float fraction) {
            if (API == null) return;
            var inventory = player.getInventory();
            var wands = new ArrayList<ItemStack>();
            for (int slot = inventory.items.size() - 1; slot >= 0; slot--) {
                ItemStack stack = inventory.items.get(slot);
                if (API.itemType().isInstance(stack.getItem())) wands.add(stack);
            }
            if (wands.isEmpty()) return;

            var aspects = player.level().registryAccess().registryOrThrow(ASPECTS);
            // The original loop attempts its debit before checking the countdown.
            int limit = fraction == 1.0F ? 1_001 : (int) Math.max(150.0F * fraction, 1.0F) + 1;
            try {
                for (var key : aspects.registryKeySet()) {
                    int remaining = limit;
                    for (ItemStack stack : wands) {
                        if (remaining == 0) break;
                        int stored = (int) API.getVis().invoke(null, stack, key);
                        if (stored == 0) continue;
                        float modifier = (float) API.consumptionModifier().invoke(null, stack, player, key, true);
                        int cost = Math.max(1, (int) (100 * modifier));
                        int taken = Math.min(remaining, stored / cost);
                        if (taken > 0) {
                            API.storeVis().invoke(null, stack, key, stored - taken * cost);
                            inventory.setChanged();
                            remaining -= taken;
                        }
                    }
                }
            } catch (ReflectiveOperationException exception) {
                if (!reportedError) {
                    reportedError = true;
                    Witchery.LOGGER.warn("Could not drain Thaumaturge wand Vis", exception);
                }
            }
        }
    }
}
