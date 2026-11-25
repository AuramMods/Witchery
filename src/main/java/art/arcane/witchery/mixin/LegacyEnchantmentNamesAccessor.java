package art.arcane.witchery.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.util.datafix.fixes.ItemStackEnchantmentNamesFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackEnchantmentNamesFix.class)
public interface LegacyEnchantmentNamesAccessor {
    @Accessor("MAP")
    static Int2ObjectMap<String> witchery$getNamesById() {
        throw new AssertionError();
    }
}
