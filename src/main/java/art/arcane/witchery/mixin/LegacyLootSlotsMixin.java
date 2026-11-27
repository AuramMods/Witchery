package art.arcane.witchery.mixin;

import art.arcane.witchery.data.LegacyLootSlots;
import art.arcane.witchery.data.WitcheryLootTables;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

@Mixin(LootTable.class)
public abstract class LegacyLootSlotsMixin {
    @Shadow
    private static Consumer<ItemStack> createStackSplitter(ServerLevel level, Consumer<ItemStack> output) {
        throw new AssertionError();
    }

    @Inject(method = "fill", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void witchery$fillLegacySlots(Container container, LootParams params, long seed, CallbackInfo callback,
                                           LootContext context) {
        var table = (LootTable) (Object) this;
        var id = table.getLootTableId();
        boolean mineshaft = WitcheryLootTables.MINESHAFT_CORRIDOR.equals(id);
        if (!mineshaft && (id == null || !WitcheryLootTables.LEGACY_STRUCTURE_CHESTS.contains(id))) return;
        int size = container.getContainerSize();
        if (size == 0) return;

        if (mineshaft) {
            table.getRandomItems(context, stack -> container.setItem(context.getRandom().nextInt(size), stack));
        } else {
            LegacyLootSlots.fill(table, container, context, size);
        }
        callback.cancel();
    }

    @Redirect(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/LootTable;createStackSplitter(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Consumer;)Ljava/util/function/Consumer;"))
    private Consumer<ItemStack> witchery$reserveSlots(ServerLevel level, Consumer<ItemStack> output,
                                                     LootContext context) {
        return createStackSplitter(level, LegacyLootSlots.reserveSlots(context, output));
    }
}
