package art.arcane.witchery.mixin;

import art.arcane.witchery.config.WitcheryCommonConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.buuz135.industrial.block.agriculturehusbandry.tile.MobCrusherTile", remap = false)
abstract class IndustrialMobCrusherMixin {
    @Unique
    private static final TagKey<EntityType<?>> witchery$crusherBlacklist = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("industrialforegoing", "mob_crusher_blacklist"));

    // The Crusher checks its instant-kill tag later, after picking a target.
    @Inject(method = "lambda$work$3", at = @At("HEAD"), cancellable = true)
    private static void witchery$skipProtectedMobs(Mob mob, CallbackInfoReturnable<Boolean> result) {
        if (WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get() && mob.getType().is(witchery$crusherBlacklist)) {
            result.setReturnValue(false);
        }
    }
}
