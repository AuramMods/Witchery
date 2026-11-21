package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class WitcheryDamageTypes {
    public static final ResourceKey<DamageType> DEMONIC = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "demonic"));
    public static final ResourceKey<DamageType> VAMPIRE_FIRE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "vampire_fire"));
    public static final ResourceKey<DamageType> SUNLIGHT = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "sunlight"));
    public static final ResourceKey<DamageType> VAMPIRE_BITE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "vampire_bite"));

    public static final ResourceKey<DamageType> SPECTRAL_TOUCH = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "spectral_touch"));
    public static final ResourceKey<DamageType> SILVER_BREW = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "silver_brew"));

    private WitcheryDamageTypes() {}

    public static DamageSource spectralTouch(Entity attacker) {
        var type = attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SPECTRAL_TOUCH);
        return new DamageSource(type, attacker);
    }

    public static DamageSource demonic(Level level, Entity projectile, @Nullable Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DEMONIC),
                projectile, attacker);
    }

    public static DamageSource vampireFire(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(VAMPIRE_FIRE));
    }

    public static DamageSource sunlight(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SUNLIGHT));
    }

    public static DamageSource vampireBite(Entity attacker) {
        var type = attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(VAMPIRE_BITE);
        return new DamageSource(type, attacker);
    }

    public static DamageSource silverBrew(Level level, @Nullable Entity source, @Nullable Entity caster) {
        var type = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SILVER_BREW);
        return new DamageSource(type, source, caster);
    }
}
