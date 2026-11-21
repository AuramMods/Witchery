package art.arcane.witchery.item;

import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

public final class BinkyHeadItem extends Item {
    public BinkyHeadItem() {
        super(new Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) return InteractionResult.PASS;
        var above = context.getClickedPos().above();
        if (context.getPlayer() != null && !context.getPlayer().mayUseItemAt(above, Direction.UP, context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        var horse = WitcheryEntities.DEATH_HORSE.get().create(level);
        if (horse == null) return InteractionResult.FAIL;
        Vec3 spawn = Vec3.atBottomCenterOf(above);
        horse.moveTo(spawn.x, spawn.y + 0.01, spawn.z, 0, 0);
        if (!level.noCollision(horse)) return InteractionResult.FAIL;

        horse.setTamed(true);
        horse.setPersistenceRequired();
        horse.setCustomName(Component.translatable("item.witchery.horseofdeath.customname"));
        horse.getPersistentData().putBoolean("WITCIsBinky", true);
        if (!level.addFreshEntity(horse)) return InteractionResult.FAIL;
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, spawn.x, spawn.y + 0.5, spawn.z, 16, 0.5, 0.8, 0.5, 0);
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
