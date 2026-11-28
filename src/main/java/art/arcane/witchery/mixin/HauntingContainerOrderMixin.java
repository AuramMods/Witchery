package art.arcane.witchery.mixin;

import art.arcane.witchery.block.entity.HauntingContainerOrder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(BlockEntity.class)
abstract class HauntingContainerOrderMixin implements HauntingContainerOrder {
    @Unique private static final AtomicLong WITCHERY_CONTAINER_LOAD_SEQUENCE = new AtomicLong();
    @Unique private long witchery$containerLoadOrder = Long.MAX_VALUE;

    @Inject(method = "setLevel", at = @At("TAIL"))
    private void witchery$recordHauntingContainerOrder(Level level, CallbackInfo ci) {
        if (witchery$containerLoadOrder == Long.MAX_VALUE && level instanceof ServerLevel && this instanceof Container) {
            witchery$containerLoadOrder = WITCHERY_CONTAINER_LOAD_SEQUENCE.incrementAndGet();
        }
    }

    @Override
    public long witchery$hauntingLoadOrder() {
        return witchery$containerLoadOrder;
    }
}
