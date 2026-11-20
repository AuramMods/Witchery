package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.infusion.InfusedBrews;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;

final class CreatureAlignment {
    private static final TagKey<EntityType<?>> DEMONIC = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "demonic"));

    private CreatureAlignment() {}

    static boolean demonic(LivingEntity entity) {
        return entity.getType().is(DEMONIC);
    }

    static boolean undead(LivingEntity entity) {
        if (entity.getMobType() == MobType.UNDEAD || Vampirism.isVampire(entity)) return true;
        if (!(entity instanceof ServerPlayer player)) return false;
        var brew = InfusedBrews.status(player);
        return brew.type() == InfusedBrews.GRAVE && brew.remainingTicks() > 0;
    }
}
