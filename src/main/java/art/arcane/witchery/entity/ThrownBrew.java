package art.arcane.witchery.entity;

import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ThrownBrew extends ThrowableItemProjectile {
    public ThrownBrew(EntityType<? extends ThrownBrew> type, Level level) {
        super(type, level);
    }

    public ThrownBrew(LivingEntity thrower, Level level) {
        super(WitcheryEntities.THROWN_BREW.get(), thrower, level);
    }

    public static boolean throwBottle(ServerLevel level, Player player, ItemStack stack, float speed) {
        var brew = new ThrownBrew(player, level);
        brew.setItem(stack);
        brew.shootFromRotation(player, player.getXRot(), player.getYRot(), -20, speed, 1);
        if (!level.addFreshEntity(brew)) return false;
        level.playSound(null, player, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (isNoGravity() && level() instanceof ServerLevel level) {
            if (tickCount > 200) discard();
            else if (!isRemoved()) {
                int color = BrewBottleItem.color(getItem());
                var options = new net.minecraft.core.particles.DustParticleOptions(
                        net.minecraft.world.phys.Vec3.fromRGB24(color).toVector3f(), 1);
                level.sendParticles(options, getX(), getY(), getZ(), 4, 0.12, 0.12, 0.12, 0);
            }
        }
    }

    @Override
    protected Item getDefaultItem() {
        return WitcheryIngredient.BREW_OF_INK.item();
    }

    @Override
    protected float getGravity() {
        return getItem().is(WitcheryIngredient.ROCK.item()) || getItem().is(WitcheryIngredient.WEB.item())
                ? super.getGravity() : 0.05F;
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel server) {
            Vec3 impact = hit.getLocation();
            if (getItem().is(WitcheryIngredient.ROCK.item())) {
                if (hit instanceof EntityHitResult entity) {
                    entity.getEntity().hurt(server.damageSources().thrown(this, getOwner()), 6);
                }
                server.sendParticles(ParticleTypes.POOF, impact.x, impact.y, impact.z, 8, 0.5, 0.5, 0.5, 0.05);
                discard();
                return;
            }
            if (getItem().is(WitcheryIngredient.WEB.item())) {
                BlockPos pos = hit instanceof BlockHitResult block ? block.getBlockPos().relative(block.getDirection())
                        : BlockPos.containing(impact);
                if (hit instanceof EntityHitResult entity) pos = entity.getEntity().blockPosition();
                if (server.getBlockState(pos).canBeReplaced()) server.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
                discard();
                return;
            }
            if (getItem().getItem() instanceof BrewBottleItem brew) {
                brew.impact(server, this, hit);
                discard();
                return;
            }
            if (getItem().getItem() instanceof ThrowableBrewItem brew) {
                brew.impact(this, hit);
            }
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, getItem()),
                    impact.x, impact.y, impact.z, 12, 0.2, 0.2, 0.2, 0.1);
            server.playSound(null, impact.x, impact.y, impact.z, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
            discard();
        }
    }
}
