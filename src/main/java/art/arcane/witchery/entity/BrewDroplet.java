package art.arcane.witchery.entity;

import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

public final class BrewDroplet extends ThrowableProjectile implements ItemSupplier {
    public static final String TINT_KEY = "BrewDropletColor";
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(BrewDroplet.class, EntityDataSerializers.INT);
    @Nullable
    private CompoundTag brew;
    @Nullable
    private BrewMixture mixture;
    private ItemStack sprite = ItemStack.EMPTY;

    public BrewDroplet(EntityType<? extends BrewDroplet> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(COLOR, 0x88AAFF);
    }

    public void setBrew(CompoundTag brew) {
        this.brew = brew.copy();
        mixture = BrewMixture.from(brew);
        if (mixture != null) {
            entityData.set(COLOR, mixture.color());
        }
    }

    public int color() {
        return entityData.get(COLOR);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSquared) {
        double range = getBoundingBox().getSize() * 256.0;
        return distanceSquared < range * range;
    }

    @Override
    public ItemStack getItem() {
        int color = color();
        if (sprite.isEmpty() || sprite.getTag().getInt(TINT_KEY) != color) {
            sprite = new ItemStack(WitcheryIngredient.QUARTZ_SPHERE.item());
            sprite.getOrCreateTag().putInt(TINT_KEY, color);
        }
        return sprite;
    }

    @Override
    public void tick() {
        if (!level().isClientSide && (mixture == null || tickCount >= 400)) {
            discard();
            return;
        }
        super.tick();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return tickCount >= 5 && super.canHitEntity(target);
    }

    @Override
    protected void onHit(HitResult hit) {
        var impact = position();
        super.onHit(hit);
        if (!(level() instanceof ServerLevel server)) return;
        if (mixture != null) {
            if (hit instanceof BlockHitResult block) {
                mixture.applyToBlock(server, block.getBlockPos(), block.getDirection(), 1, true,
                        FakePlayerFactory.getMinecraft(server), impact);
            } else if (hit instanceof EntityHitResult entity && entity.getEntity() instanceof LivingEntity living) {
                mixture.applyRitualExposure(server, living, 1, impact);
            }
            server.levelEvent(2002, BlockPos.containing(impact), entityData.get(COLOR));
        }
        discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (brew != null) tag.put("Brew", brew.copy());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Brew", Tag.TAG_COMPOUND)) setBrew(tag.getCompound("Brew"));
    }
}
