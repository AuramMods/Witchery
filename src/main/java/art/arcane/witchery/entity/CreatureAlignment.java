package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.infusion.InfusedBrews;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraftforge.common.Tags;

import java.util.Locale;

public final class CreatureAlignment {
    private static final TagKey<EntityType<?>> DEMONIC = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "demonic"));

    private CreatureAlignment() {}

    public static boolean demonic(LivingEntity entity) {
        return entity.getType().is(DEMONIC);
    }

    public static boolean undead(LivingEntity entity) {
        if (entity.getMobType() == MobType.UNDEAD || Vampirism.isVampire(entity)) return true;
        if (!(entity instanceof ServerPlayer player)) return false;
        var brew = InfusedBrews.status(player);
        return brew.type() == InfusedBrews.GRAVE && brew.remainingTicks() > 0;
    }

    public static boolean werewolf(LivingEntity entity) {
        return Lycanthropy.isShifted(entity) || entity instanceof Reflection copy && copy.hasWolfmanModel()
                || mobNameContains(entity, "werewolf");
    }

    public static boolean vampire(LivingEntity entity) {
        return Vampirism.isVampire(entity) || mobNameContains(entity, "vampire");
    }

    private static boolean mobNameContains(LivingEntity entity, String name) {
        // Witchery 0.24.1 recognized other mods' mobs by their simple class names.
        return entity instanceof Mob && entity.getClass().getSimpleName().toLowerCase(Locale.ROOT).contains(name);
    }

    public static boolean boss(LivingEntity entity) {
        return Vampirism.isBoss(entity) || entity.getType().is(Tags.EntityTypes.BOSSES)
                || entity instanceof HobgoblinGod || entity instanceof Leonard;
    }

    public static boolean immuneToDisease(LivingEntity entity) {
        return undead(entity) || demonic(entity) || Lycanthropy.isWerewolf(entity) || werewolf(entity)
                || boss(entity) || entity instanceof AbstractGolem;
    }
}
