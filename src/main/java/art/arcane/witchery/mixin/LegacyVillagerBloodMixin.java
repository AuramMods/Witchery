package art.arcane.witchery.mixin;

import art.arcane.witchery.vampirism.BloodFeeding;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
abstract class LegacyVillagerBloodMixin {
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void witchery$importLegacyBlood(CompoundTag tag, CallbackInfo info) {
        BloodFeeding.importLegacyBlood((Villager) (Object) this, tag);
    }
}
