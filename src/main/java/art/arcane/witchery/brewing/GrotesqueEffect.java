package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class GrotesqueEffect extends MobEffect {
    private static final int RADIUS = 4;

    public GrotesqueEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x322116);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 4 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(RADIUS),
                mob -> mob != entity && mob.isAlive() && !(mob instanceof Demon || mob instanceof WitherBoss
                        || mob instanceof EnderDragon || mob instanceof AbstractGolem || mob instanceof Witch))) {
            Vec3 offset = mob.position().subtract(entity.position());
            double distanceSquared = offset.lengthSqr();
            if (distanceSquared < 1.0E-6 || distanceSquared >= RADIUS * RADIUS) continue;
            double vertical = offset.y * 4.32 / (distanceSquared * distanceSquared);
            mob.push(Math.signum(offset.x) * 0.22, vertical > 0.2 || vertical < -0.1 ? 0.12 : vertical,
                    Math.signum(offset.z) * 0.22);
        }
    }

    public static void restoreLegacyBrew(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        int quarters = persistent.getInt("witcheryGrotesque");
        if (quarters > 0) {
            player.addEffect(new MobEffectInstance(WitcheryEffects.GROTESQUE.get(), (int) Math.min(quarters * 4L, Integer.MAX_VALUE),
                    0, false, false, false));
            persistent.remove("witcheryGrotesque");
        }
    }
}
