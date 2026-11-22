package art.arcane.witchery.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.datafix.fixes.BlockStateData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockStateData.class)
public interface LegacyBlockStateNamesAccessor {
    @Accessor("ID_BY_OLD_NAME")
    static Object2IntMap<String> witchery$getIdsByOldName() {
        throw new AssertionError();
    }
}
