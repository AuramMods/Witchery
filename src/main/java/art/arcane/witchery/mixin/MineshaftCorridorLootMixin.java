package art.arcane.witchery.mixin;

import art.arcane.witchery.data.WitcheryLootTables;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootTable.class)
public abstract class MineshaftCorridorLootMixin {
    @Shadow @Final @Nullable ResourceLocation randomSequence;

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void witchery$fillMineshaftCorridor(Container container, LootParams params, long seed, CallbackInfo callback) {
        var table = (LootTable) (Object) this;
        if (!WitcheryLootTables.MINESHAFT_CORRIDOR.equals(table.getLootTableId())) return;

        var context = new LootContext.Builder(params).withOptionalRandomSeed(seed).create(randomSequence);
        // Legacy mineshaft loot chose a slot on every roll, including already occupied slots.
        table.getRandomItems(context, stack -> container.setItem(context.getRandom().nextInt(container.getContainerSize()), stack));
        callback.cancel();
    }
}
