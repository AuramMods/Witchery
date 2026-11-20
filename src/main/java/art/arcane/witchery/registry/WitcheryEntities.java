package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.AttackBat;
import art.arcane.witchery.entity.BabaYaga;
import art.arcane.witchery.entity.CovenWitch;
import art.arcane.witchery.entity.DarkMark;
import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.entity.Ent;
import art.arcane.witchery.entity.GrenadeProjectile;
import art.arcane.witchery.entity.Hellhound;
import art.arcane.witchery.entity.HornedHuntsman;
import art.arcane.witchery.entity.Imp;
import art.arcane.witchery.entity.LegacyWaystoneEntity;
import art.arcane.witchery.entity.LordOfTorment;
import art.arcane.witchery.entity.Lilith;
import art.arcane.witchery.entity.WitcheryFollower;
import art.arcane.witchery.entity.Mandrake;
import art.arcane.witchery.entity.Nightmare;
import art.arcane.witchery.entity.Reflection;
import art.arcane.witchery.entity.ScryEye;
import art.arcane.witchery.entity.SleepingBody;
import art.arcane.witchery.entity.Soulfire;
import art.arcane.witchery.entity.SymbolProjectile;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.entity.Vampire;
import art.arcane.witchery.entity.VillageGuard;
import art.arcane.witchery.entity.WitcheryBolt;
import art.arcane.witchery.entity.WitchBroom;
import art.arcane.witchery.entity.WitchCat;
import art.arcane.witchery.entity.WitchHunter;
import art.arcane.witchery.entity.WitchOwl;
import art.arcane.witchery.entity.WitchToad;
import art.arcane.witchery.entity.WereVillager;
import art.arcane.witchery.entity.Wolfman;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
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
    public static final RegistryObject<EntityType<Ent>> ENT =
            ENTITY_TYPES.register("ent", () -> EntityType.Builder.of(Ent::new, MobCategory.MONSTER)
                    .sized(1.2F, 3.0F).clientTrackingRange(8).build(Witchery.MODID + ":ent"));
    public static final RegistryObject<EntityType<Reflection>> REFLECTION =
            ENTITY_TYPES.register("reflection", () -> EntityType.Builder.of(Reflection::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).fireImmune().clientTrackingRange(8).build(Witchery.MODID + ":reflection"));
    public static final RegistryObject<EntityType<LordOfTorment>> LORD_OF_TORMENT =
            ENTITY_TYPES.register("lordoftorment", () -> EntityType.Builder.of(LordOfTorment::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.9F).fireImmune().clientTrackingRange(10).build(Witchery.MODID + ":lordoftorment"));
    public static final RegistryObject<EntityType<Soulfire>> SOULFIRE =
            ENTITY_TYPES.register("soulfire", () -> EntityType.Builder.<Soulfire>of(Soulfire::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F).fireImmune().clientTrackingRange(4).updateInterval(3).build(Witchery.MODID + ":soulfire"));
    public static final RegistryObject<EntityType<WitcheryBolt>> BOLT =
            ENTITY_TYPES.register("bolt", () -> EntityType.Builder.<WitcheryBolt>of(WitcheryBolt::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(3).build(Witchery.MODID + ":bolt"));
    public static final RegistryObject<EntityType<WitchCat>> WITCH_CAT =
            ENTITY_TYPES.register("cat", () -> EntityType.Builder.<WitchCat>of(WitchCat::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.7F).clientTrackingRange(8).build(Witchery.MODID + ":cat"));
    public static final RegistryObject<EntityType<WitchOwl>> OWL =
            ENTITY_TYPES.register("owl", () -> EntityType.Builder.of(WitchOwl::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.8F).clientTrackingRange(8).build(Witchery.MODID + ":owl"));
    public static final RegistryObject<EntityType<WitchToad>> TOAD =
            ENTITY_TYPES.register("toad", () -> EntityType.Builder.of(WitchToad::new, MobCategory.CREATURE)
                    .sized(0.8F, 0.8F).clientTrackingRange(8).build(Witchery.MODID + ":toad"));
    public static final RegistryObject<EntityType<WitchBroom>> BROOM =
            ENTITY_TYPES.register("broom", () -> EntityType.Builder.of(WitchBroom::new, MobCategory.MISC)
                    .sized(1.2F, 0.5F).clientTrackingRange(8).updateInterval(1).build(Witchery.MODID + ":broom"));
    public static final RegistryObject<EntityType<LegacyWaystoneEntity>> ITEM_WAYSTONE =
            ENTITY_TYPES.register("item", () -> EntityType.Builder.of(LegacyWaystoneEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(20).build(Witchery.MODID + ":item"));
    public static final RegistryObject<EntityType<CovenWitch>> COVEN_WITCH =
            ENTITY_TYPES.register("covenwitch", () -> EntityType.Builder.of(CovenWitch::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F).clientTrackingRange(8).build(Witchery.MODID + ":covenwitch"));
    public static final RegistryObject<EntityType<BabaYaga>> BABA_YAGA =
            ENTITY_TYPES.register("babayaga", () -> EntityType.Builder.of(BabaYaga::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(8).build(Witchery.MODID + ":babayaga"));
    public static final RegistryObject<EntityType<Lilith>> LILITH =
            ENTITY_TYPES.register("lilith", () -> EntityType.Builder.of(Lilith::new, MobCategory.MONSTER)
                    .sized(0.8F, 2.5F).fireImmune().clientTrackingRange(8).build(Witchery.MODID + ":lilith"));
    public static final RegistryObject<EntityType<Vampire>> VAMPIRE =
            ENTITY_TYPES.register("vampire", () -> EntityType.Builder.of(Vampire::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(8).build(Witchery.MODID + ":vampire"));
    public static final RegistryObject<EntityType<VillageGuard>> VILLAGE_GUARD =
            ENTITY_TYPES.register("villageguard", () -> EntityType.Builder.of(VillageGuard::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F).clientTrackingRange(8).build(Witchery.MODID + ":villageguard"));
    public static final RegistryObject<EntityType<WitchHunter>> WITCH_HUNTER =
            ENTITY_TYPES.register("witchhunter", () -> EntityType.Builder.of(WitchHunter::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).clientTrackingRange(8).build(Witchery.MODID + ":witchhunter"));
    public static final RegistryObject<EntityType<Hellhound>> HELLHOUND =
            ENTITY_TYPES.register("hellhound", () -> EntityType.Builder.of(Hellhound::new, MobCategory.MONSTER)
                    .sized(0.9F, 0.9F).fireImmune().clientTrackingRange(8).build(Witchery.MODID + ":hellhound"));
    public static final RegistryObject<EntityType<HornedHuntsman>> HORNED_HUNTSMAN =
            ENTITY_TYPES.register("hornedhuntsman", () -> EntityType.Builder.of(HornedHuntsman::new, MobCategory.MONSTER)
                    .sized(1.4F, 3.2F).fireImmune().clientTrackingRange(10).build(Witchery.MODID + ":hornedhuntsman"));
    public static final RegistryObject<EntityType<Wolfman>> WOLFMAN =
            ENTITY_TYPES.register("wolfman", () -> EntityType.Builder.of(Wolfman::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).clientTrackingRange(8).build(Witchery.MODID + ":wolfman"));
    public static final RegistryObject<EntityType<WereVillager>> WERE_VILLAGER =
            ENTITY_TYPES.register("werevillager", () -> EntityType.Builder.of(WereVillager::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F).clientTrackingRange(8).build(Witchery.MODID + ":werevillager"));
    public static final RegistryObject<EntityType<AttackBat>> ATTACK_BAT =
            ENTITY_TYPES.register("attackbat", () -> EntityType.Builder.of(AttackBat::new, MobCategory.AMBIENT)
                    .sized(0.5F, 0.9F).clientTrackingRange(5).build(Witchery.MODID + ":attackbat"));
    public static final RegistryObject<EntityType<WitcheryFollower>> FOLLOWER =
            ENTITY_TYPES.register("follower", () -> EntityType.Builder.of(WitcheryFollower::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F).clientTrackingRange(8).build(Witchery.MODID + ":follower"));
    public static final RegistryObject<EntityType<ScryEye>> SCRY_EYE =
            ENTITY_TYPES.register("eye", () -> EntityType.Builder.of(ScryEye::new, MobCategory.MISC)
                    .sized(0.01F, 0.01F).noSave().noSummon().clientTrackingRange(10).updateInterval(1)
                    .build(Witchery.MODID + ":eye"));
    public static final RegistryObject<EntityType<Imp>> IMP =
            ENTITY_TYPES.register("imp", () -> EntityType.Builder.of(Imp::new, MobCategory.MONSTER)
                    .sized(0.4F, 1.3F).fireImmune().build(Witchery.MODID + ":imp"));
    public static final RegistryObject<EntityType<ThrownBrew>> THROWN_BREW =
            ENTITY_TYPES.register("thrown_brew", () -> EntityType.Builder.<ThrownBrew>of(ThrownBrew::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(3).build(Witchery.MODID + ":thrown_brew"));
    public static final RegistryObject<EntityType<GrenadeProjectile>> GRENADE =
            ENTITY_TYPES.register("grenade", () -> EntityType.Builder.<GrenadeProjectile>of(GrenadeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(3).build(Witchery.MODID + ":grenade"));
    public static final RegistryObject<EntityType<SymbolProjectile>> SYMBOL_PROJECTILE =
            ENTITY_TYPES.register("spelleffect", () -> EntityType.Builder.<SymbolProjectile>of(SymbolProjectile::new, MobCategory.MISC)
                    .sized(1, 1).clientTrackingRange(4).updateInterval(1).build(Witchery.MODID + ":spelleffect"));
    public static final RegistryObject<EntityType<DarkMark>> DARK_MARK =
            ENTITY_TYPES.register("darkmark", () -> EntityType.Builder.of(DarkMark::new, MobCategory.MISC)
                    .sized(2, 2).clientTrackingRange(4).build(Witchery.MODID + ":darkmark"));

    static {
        LEGACY_ENTITIES.put("mandrake", MANDRAKE);
        LEGACY_ENTITIES.put("corpse", SLEEPING_BODY);
        LEGACY_ENTITIES.put("nightmare", NIGHTMARE);
        LEGACY_ENTITIES.put("demon", DEMON);
        LEGACY_ENTITIES.put("ent", ENT);
        LEGACY_ENTITIES.put("reflection", REFLECTION);
        LEGACY_ENTITIES.put("lordoftorment", LORD_OF_TORMENT);
        LEGACY_ENTITIES.put("cat", WITCH_CAT);
        LEGACY_ENTITIES.put("owl", OWL);
        LEGACY_ENTITIES.put("toad", TOAD);
        LEGACY_ENTITIES.put("covenwitch", COVEN_WITCH);
        LEGACY_ENTITIES.put("babayaga", BABA_YAGA);
        LEGACY_ENTITIES.put("lilith", LILITH);
        LEGACY_ENTITIES.put("vampire", VAMPIRE);
        LEGACY_ENTITIES.put("villageguard", VILLAGE_GUARD);
        LEGACY_ENTITIES.put("witchhunter", WITCH_HUNTER);
        LEGACY_ENTITIES.put("hellhound", HELLHOUND);
        LEGACY_ENTITIES.put("hornedhuntsman", HORNED_HUNTSMAN);
        LEGACY_ENTITIES.put("wolfman", WOLFMAN);
        LEGACY_ENTITIES.put("werevillager", WERE_VILLAGER);
        LEGACY_ENTITIES.put("attackbat", ATTACK_BAT);
        LEGACY_ENTITIES.put("follower", FOLLOWER);
        LEGACY_ENTITIES.put("imp", IMP);
        LEGACY_ENTITIES.put("darkmark", DARK_MARK);
        for (String legacyName : LegacyRegistryData.ENTITIES) {
            registerLegacyEntity(legacyName);
        }
    }

    private WitcheryEntities() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
        eventBus.addListener(WitcheryEntities::registerEntityAttributes);
        eventBus.addListener(WitcheryEntities::registerSpawnPlacements);
    }

    public static Map<String, RegistryObject<? extends EntityType<? extends Mob>>> all() {
        return Collections.unmodifiableMap(LEGACY_ENTITIES);
    }

    private static void registerLegacyEntity(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        if (LEGACY_ENTITIES.containsKey(path) || path.equals("soulfire") || path.equals("spelleffect") || path.equals("broom")
                || path.equals("item") || path.equals("eye") || path.equals("grenade") || path.equals("bolt")) {
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
                    : entry == ENT ? Ent.attributes()
                    : entry == REFLECTION ? Reflection.attributes()
                    : entry == LORD_OF_TORMENT ? LordOfTorment.attributes()
                    : entry == COVEN_WITCH ? CovenWitch.attributes()
                    : entry == BABA_YAGA ? BabaYaga.attributes()
                    : entry == LILITH ? Lilith.attributes()
                    : entry == VAMPIRE ? Vampire.attributes()
                    : entry == VILLAGE_GUARD ? VillageGuard.attributes()
                    : entry == WITCH_HUNTER ? WitchHunter.attributes()
                    : entry == HELLHOUND ? Hellhound.attributes()
                    : entry == HORNED_HUNTSMAN ? HornedHuntsman.attributes()
                    : entry == WOLFMAN ? Wolfman.attributes()
                    : entry == WERE_VILLAGER ? Villager.createAttributes()
                    : entry == ATTACK_BAT ? Bat.createAttributes()
                    : entry == FOLLOWER ? WitcheryFollower.attributes()
                    : entry == IMP ? Imp.attributes()
                    : entry == DARK_MARK ? DarkMark.attributes()
                    : entry == OWL ? WitchOwl.attributes()
                    : entry == TOAD ? WitchToad.attributes()
                    : entry == WITCH_CAT ? Cat.createAttributes() : Zombie.createAttributes();
            event.put(entry.get(), attributes.build());
        }
    }

    private static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(COVEN_WITCH.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CovenWitch::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(HELLHOUND.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkAnyLightMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
