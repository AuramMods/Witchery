package art.arcane.witchery.mixin;

import art.arcane.witchery.world.ScryingView;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class ScryingEntityTrackingMixin {
    @Redirect(method = "updatePlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 witchery$scryingPosition(ServerPlayer player) {
        var eye = ScryingView.eye(player);
        return eye == null ? player.position() : eye.position();
    }
}
