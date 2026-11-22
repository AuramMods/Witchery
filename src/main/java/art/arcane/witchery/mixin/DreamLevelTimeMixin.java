package art.arcane.witchery.mixin;

import art.arcane.witchery.world.DreamAtmosphere;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
abstract class DreamLevelTimeMixin implements LevelAccessor {
    @Override
    public float getTimeOfDay(float partialTick) {
        Level level = (Level) (Object) this;
        if (level.dimension().equals(WitcheryDimensions.DREAM)) {
            return DreamAtmosphere.mode(level) == 0 ? 0.0F : 0.5F;
        }
        return level.dimensionType().timeOfDay(level.dayTime());
    }
}
