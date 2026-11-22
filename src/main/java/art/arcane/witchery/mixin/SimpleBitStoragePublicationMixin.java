package art.arcane.witchery.mixin;

import net.minecraft.util.SimpleBitStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

@Mixin(SimpleBitStorage.class)
abstract class SimpleBitStoragePublicationMixin {
    @Unique
    private static final VarHandle witchery$words = MethodHandles.arrayElementVarHandle(long[].class);

    @Redirect(method = "get(I)I", at = @At(value = "FIELD", target = "Lnet/minecraft/util/SimpleBitStorage;data:[J",
            args = "array=get"))
    private long witchery$readPublishedWord(long[] data, int index) {
        return (long) witchery$words.getAcquire(data, index);
    }

    // PalettedContainer adds the palette entry before writing its index into storage.
    @Redirect(method = {"set(II)V", "getAndSet(II)I"}, require = 2,
            at = @At(value = "FIELD", target = "Lnet/minecraft/util/SimpleBitStorage;data:[J", ordinal = 1,
                    args = {"array=set", "fuzz=32"}))
    private void witchery$publishWord(long[] data, int index, long value) {
        witchery$words.setRelease(data, index, value);
    }
}
