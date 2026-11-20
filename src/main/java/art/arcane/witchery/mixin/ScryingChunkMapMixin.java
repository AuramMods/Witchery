package art.arcane.witchery.mixin;

import art.arcane.witchery.world.ScryingView;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkMap.class)
public abstract class ScryingChunkMapMixin {
    // Keep vanilla chunk tracking centered on the view while the player entity stays at the ball.
    @ModifyArg(method = {"move", "updatePlayerPos"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/entity/EntityAccess;)Lnet/minecraft/core/SectionPos;"))
    private EntityAccess witchery$scryingSection(EntityAccess entity) {
        if (entity instanceof ServerPlayer player) {
            var camera = ScryingView.camera(player);
            if (camera != null) return camera;
        }
        return entity;
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getBlockX()I"))
    private int witchery$scryingX(ServerPlayer player) {
        var camera = ScryingView.camera(player);
        return camera == null ? player.getBlockX() : camera.getBlockX();
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getBlockZ()I"))
    private int witchery$scryingZ(ServerPlayer player) {
        var camera = ScryingView.camera(player);
        return camera == null ? player.getBlockZ() : camera.getBlockZ();
    }
}
