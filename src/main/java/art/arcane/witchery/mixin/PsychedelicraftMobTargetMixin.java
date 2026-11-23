package art.arcane.witchery.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "ivorius.psychedelicraft.entity.drug.DrugEvents", remap = false)
abstract class PsychedelicraftMobTargetMixin {
    @Inject(method = "mobTick", at = @At("HEAD"), cancellable = true, require = 0)
    private static void witchery$skipDisconnectedTarget(LivingEvent.LivingTickEvent event, CallbackInfo info) {
        // Disconnect invalidates the player's drug capability before mobs clear their target.
        if (event.getEntity() instanceof Mob mob && mob.getTarget() instanceof Player player && player.isRemoved()) {
            info.cancel();
        }
    }
}
