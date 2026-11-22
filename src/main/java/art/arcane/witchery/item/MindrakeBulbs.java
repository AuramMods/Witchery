package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class MindrakeBulbs {
    public static final int LIFESPAN = 60;
    private static final String THROWER = "WITCThrower";

    private MindrakeBulbs() {
    }

    @SubscribeEvent
    public static void markToss(ItemTossEvent event) {
        ItemEntity bulb = event.getEntity();
        if (!bulb.level().isClientSide && bulb.getItem().is(WitchCrop.MINDRAKE.seed())) {
            bulb.getPersistentData().putUUID(THROWER, event.getPlayer().getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hatch(ItemExpireEvent event) {
        ItemEntity bulb = event.getEntity();
        var stack = bulb.getItem();
        if (!(bulb.level() instanceof ServerLevel level) || !stack.is(WitchCrop.MINDRAKE.seed())) return;
        var data = bulb.getPersistentData();
        var owner = data.hasUUID(THROWER) ? data.getUUID(THROWER) : null;
        if (owner == null && data.contains(THROWER, Tag.TAG_STRING)) {
            owner = OldUsersConverter.convertMobOwnerIfNecessary(level.getServer(), data.getString(THROWER));
        }
        while (!stack.isEmpty()) {
            var mindrake = WitcheryEntities.MINDRAKE.get().create(level);
            if (mindrake != null) {
                mindrake.moveTo(bulb.getX(), bulb.getY(), bulb.getZ(), 0, 0);
                if (owner != null) {
                    mindrake.setTame(true);
                    mindrake.setOwnerUUID(owner);
                    mindrake.setPersistenceRequired();
                }
            }
            if (mindrake == null || !level.addFreshEntity(mindrake)) {
                event.setExtraLife(20);
                event.setCanceled(true);
                return;
            }
            stack.shrink(1);
            level.sendParticles(ParticleTypes.POOF, bulb.getX(), bulb.getY(), bulb.getZ(), 8, 0.25, 0.2, 0.25, 0.02);
        }
        level.playSound(null, bulb.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 1, 1);
    }

    public static boolean sameThrower(ItemEntity first, ItemEntity second) {
        return Objects.equals(first.getPersistentData().get(THROWER), second.getPersistentData().get(THROWER));
    }
}
