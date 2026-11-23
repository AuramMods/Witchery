package art.arcane.witchery.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = {
        "com.github.minecraftschurlimods.arsmagicalegacy.common.skill.SkillHelper",
        "com.github.minecraftschurlimods.arsmagicalegacy.common.magic.MagicHelper",
        "dev.xkmc.l2library.capability.player.PlayerCapabilityHolder"
}, remap = false)
abstract class PlayerCapabilityLookupMixin {
    @Redirect(method = {"getKnowledgeHolder", "getMagicHolder", "get(Lnet/minecraft/world/entity/player/Player;)Ldev/xkmc/l2library/capability/player/PlayerCapabilityTemplate;"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;invalidateCaps()V"),
            require = 0)
    private void witchery$restoreInvalidationForRemovedPlayer(Player player) {
        // A dead player can still be attached to the world while waiting to respawn.
        if (player.isRemoved()) player.invalidateCaps();
    }
}
