package art.arcane.witchery.entity;

import art.arcane.witchery.brewing.BrewTerrain;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

public final class InfernoSplatter extends ThrowableProjectile implements ItemSupplier {
    private static final int FIRE_COLOR = 0x990000;
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(InfernoSplatter.class, EntityDataSerializers.INT);
    private ItemStack sprite = ItemStack.EMPTY;
    private int strength;

    public InfernoSplatter(EntityType<? extends InfernoSplatter> type, Level level) {
        super(type, level);
    }

    public static void burst(ServerLevel level, BlockPos pos, int strength, @Nullable Entity caster) {
        if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)) return;
        var center = Vec3.atCenterOf(pos);
        for (int i = 0; i < 3 + strength; i++) {
            var droplet = WitcheryEntities.INFERNO_SPLATTER.get().create(level);
            if (droplet == null) return;
            droplet.strength = strength;
            if (caster != null && caster.level() == level) droplet.setOwner(caster);
            droplet.setPos(center);
            droplet.setDeltaMovement(level.random.nextDouble() * 0.2 - 0.1, level.random.nextDouble() * 0.05 + 0.3,
                    level.random.nextDouble() * 0.2 - 0.1);
            level.addFreshEntity(droplet);
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(COLOR, FIRE_COLOR);
    }

    @Override
    public ItemStack getItem() {
        int color = entityData.get(COLOR);
        if (sprite.isEmpty() || sprite.getOrCreateTag().getInt(BrewDroplet.TINT_KEY) != color) {
            sprite = new ItemStack(WitcheryIngredient.QUARTZ_SPHERE.item());
            sprite.getOrCreateTag().putInt(BrewDroplet.TINT_KEY, color);
        }
        return sprite;
    }

    @Override
    public boolean displayFireAnimation() {
        return true;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return tickCount >= 5 && entity instanceof LivingEntity && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide) hit.getEntity().setSecondsOnFire(5);
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!(level() instanceof ServerLevel level)) return;
        var pos = hit.getBlockPos().relative(hit.getDirection());
        if (level.isInWorldBounds(pos) && level.getBlockState(pos).canBeReplaced()) {
            var fire = BaseFireBlock.getState(level, pos);
            if (fire.canSurvive(level, pos)) {
                var caster = getOwner();
                BrewTerrain.setBlock(caster != null ? caster : FakePlayerFactory.getMinecraft(level), level, pos, fire, hit.getDirection());
            }
        }
        if (strength > 1) burst(level, pos, strength - 1, getOwner());
        discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Level", strength);
        tag.putInt("Color", entityData.get(COLOR));
        tag.putInt("Effect", 1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        strength = Math.max(0, Math.min(10, tag.getInt("Level")));
        // The original splatter saved zero from an unused field in place of its red display color.
        int color = tag.getInt("Color");
        entityData.set(COLOR, color == 0 ? FIRE_COLOR : color);
    }
}
