package art.arcane.witchery.mixin;

import art.arcane.witchery.item.GoblinEquipmentItem;
import art.arcane.witchery.item.GoblinEquipmentItem.Piece;
import net.minecraft.world.item.BowItem;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "cofh.core.common.event.ArcheryEvents", remap = false)
abstract class CoFHQuiverMixin {
    @Inject(method = "handleArrowNockEvent", at = @At("HEAD"), cancellable = true, require = 0)
    private static void witchery$allowQuiverDraw(ArrowNockEvent event, CallbackInfo callback) {
        if (event.getBow().getItem() instanceof BowItem && GoblinEquipmentItem.wears(event.getEntity(), Piece.QUIVER)) {
            callback.cancel();
        }
    }

    @Inject(method = "handleArrowLooseEvent", at = @At("HEAD"), cancellable = true, require = 0)
    private static void witchery$allowQuiverShot(ArrowLooseEvent event, CallbackInfo callback) {
        if (event.getBow().getItem() instanceof BowItem && GoblinEquipmentItem.wears(event.getEntity(), Piece.QUIVER)) {
            callback.cancel();
        }
    }
}
