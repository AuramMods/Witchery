package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.entity.Mandrake;
import art.arcane.witchery.entity.Nightmare;
import art.arcane.witchery.entity.Reflection;
import art.arcane.witchery.entity.SleepingBody;
import art.arcane.witchery.entity.ThrownBrew;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WitcheryEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Witchery.MODID);
    public static final Map<String, RegistryObject<? extends EntityType<? extends Mob>>> LEGACY_ENTITIES = new LinkedHashMap<>();
    public static final RegistryObject<EntityType<Mandrake>> MANDRAKE =
            ENTITY_TYPES.register("mandrake", () -> EntityType.Builder.of(Mandrake::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.9F).build(Witchery.MODID + ":mandrake"));
    public static final RegistryObject<EntityType<SleepingBody>> SLEEPING_BODY =
            ENTITY_TYPES.register("corpse", () -> EntityType.Builder.of(SleepingBody::new, MobCategory.MISC)
                    .sized(1.2F, 0.5F).clientTrackingRange(8).build(Witchery.MODID + ":corpse"));
    public static final RegistryObject<EntityType<Nightmare>> NIGHTMARE =
            ENTITY_TYPES.register("nightmare", () -> EntityType.Builder.of(Nightmare::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).build(Witchery.MODID + ":nightmare"));
    public static final RegistryObject<EntityType<Demon>> DEMON =
            ENTITY_TYPES.register("demon", () -> EntityType.Builder.of(Demon::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.9F).fireImmune().build(Witchery.MODID + ":demon"));
    public static final RegistryObject<EntityType<Reflection>> REFLECTION =
            ENTITY_TYPES.register("reflection", () -> EntityType.Builder.of(Reflection::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).fireImmune().clientTrackingRange(8).build(Witchery.MODID + ":reflection"));
    public static final RegistryObject<EntityType<ThrownBrew>> THROWN_BREW =
            ENTITY_TYPES.register("thrown_brew", () -> EntityType.Builder.<ThrownBrew>of(ThrownBrew::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(3).build(Witchery.MODID + ":thrown_brew"));

    static {
        LEGACY_ENTITIES.put("mandrake", MANDRAKE);
        LEGACY_ENTITIES.put("corpse", SLEEPING_BODY);
        LEGACY_ENTITIES.put("nightmare", NIGHTMARE);
        LEGACY_ENTITIES.put("demon", DEMON);
        LEGACY_ENTITIES.put("reflection", REFLECTION);
        for (String legacyName : LegacyRegistryData.ENTITIES) {
            registerLegacyEntity(legacyName);
        }
    }

    private WitcheryEntities() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
        eventBus.addListener(WitcheryEntities::registerEntityAttributes);
    }

    public static Map<String, RegistryObject<? extends EntityType<? extends Mob>>> all() {
        return Collections.unmodifiableMap(LEGACY_ENTITIES);
    }

    private static void registerLegacyEntity(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        if (LEGACY_ENTITIES.containsKey(path)) {
            return;
        }

        RegistryObject<EntityType<Zombie>> entityType = ENTITY_TYPES.register(path,
                () -> EntityType.Builder.<Zombie>of((entityTypeFactory, level) -> new Zombie(entityTypeFactory, level), MobCategory.MONSTER)
                        .sized(0.6F, 1.95F)
                        .build(Witchery.MODID + ":" + path));

        LEGACY_ENTITIES.put(path, entityType);
    }

    private static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        for (RegistryObject<? extends EntityType<? extends Mob>> entry : LEGACY_ENTITIES.values()) {
            var attributes = entry == MANDRAKE ? Mandrake.attributes()
                    : entry == SLEEPING_BODY ? SleepingBody.attributes()
                    : entry == NIGHTMARE ? Nightmare.attributes()
                    : entry == DEMON ? Demon.attributes()
                    : entry == REFLECTION ? Reflection.attributes() : Zombie.createAttributes();
            event.put(entry.get(), attributes.build());
        }
    }
}
