package art.arcane.witchery.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootContext.Builder.class)
public interface LootContextBuilderAccessor {
    @Accessor("random")
    void witchery$setRandom(RandomSource random);
}
