package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.world.entity.EntityType;
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
    public static final Map<String, RegistryObject<EntityType<Zombie>>> LEGACY_ENTITIES = new LinkedHashMap<>();

    static {
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

    public static Map<String, RegistryObject<EntityType<Zombie>>> all() {
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
        for (RegistryObject<EntityType<Zombie>> entry : LEGACY_ENTITIES.values()) {
            event.put(entry.get(), Zombie.createAttributes().build());
        }
    }
}
