package art.arcane.witchery.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public abstract class WitchPet extends TamableAnimal {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(WitchPet.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FAMILIAR = SynchedEntityData.defineId(WitchPet.class, EntityDataSerializers.BOOLEAN);
    protected int timeToLive = -1;

    protected WitchPet(EntityType<? extends WitchPet> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COLOR, 0);
        entityData.define(FAMILIAR, false);
    }

    public int color() {
        return entityData.get(COLOR);
    }

    public void setColor(int color) {
        entityData.set(COLOR, color & 15);
    }

    public boolean isFamiliar() {
        return entityData.get(FAMILIAR);
    }

    public void setFamiliar(boolean familiar) {
        applyFamiliar(familiar);
        setHealth(getMaxHealth());
    }

    private void applyFamiliar(boolean familiar) {
        entityData.set(FAMILIAR, familiar);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(familiar ? 50 : 10);
        getAttribute(Attributes.ARMOR).setBaseValue(familiar ? 5 : naturalArmor());
    }

    protected double naturalArmor() {
        return 0;
    }

    public boolean isTemporary() {
        return timeToLive >= 0;
    }

    @Override
    public boolean isOwnedBy(LivingEntity owner) {
        return owner.getUUID().equals(getOwnerUUID());
    }

    @Override
    public boolean fireImmune() {
        return isFamiliar() || super.fireImmune();
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        if (!isFamiliar()) setOrderedToSit(false);
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean client = level().isClientSide;
        if (isTemporary()) return InteractionResult.sidedSuccess(client);
        if (!isTame() && isFood(held) && canTameFrom(player)) {
            if (!client) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                if (random.nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(this, player)) {
                    tame(player);
                    setOrderedToSit(true);
                    getNavigation().stop();
                    level().broadcastEntityEvent(this, (byte)7);
                } else {
                    level().broadcastEntityEvent(this, (byte)6);
                }
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (isTame() && mayHandleFoodAndDye(player)) {
            if (isFood(held) && getHealth() < getMaxHealth()) {
                if (!client) {
                    if (!player.getAbilities().instabuild) held.shrink(1);
                    heal(10);
                }
                return InteractionResult.sidedSuccess(client);
            }
            if (held.getItem() instanceof DyeItem dye && dye.getDyeColor().getId() != color()) {
                if (!client) {
                    setColor(dye.getDyeColor().getId());
                    if (!player.getAbilities().instabuild) held.shrink(1);
                }
                return InteractionResult.sidedSuccess(client);
            }
        }
        if (isOwnedBy(player) && !isFood(held) && held.isEmpty()) {
            if (!client) {
                setOrderedToSit(!isOrderedToSit());
                getNavigation().stop();
                setTarget(null);
            }
            return InteractionResult.sidedSuccess(client);
        }
        return super.mobInteract(player, hand);
    }

    protected boolean canTameFrom(Player player) {
        return true;
    }

    protected boolean mayHandleFoodAndDye(Player player) {
        return isOwnedBy(player);
    }

    protected abstract String colorKey();

    protected abstract Item naturalDrop();

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (isTemporary()) return;
        int count = random.nextInt(3) + random.nextInt(1 + looting);
        if (count > 0) spawnAtLocation(new ItemStack(naturalDrop(), count));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte(colorKey(), (byte) color());
        tag.putBoolean("Familiar", isFamiliar());
        tag.putInt("SuicideIn", timeToLive);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(colorKey())) setColor(tag.getByte(colorKey()));
        applyFamiliar(tag.getBoolean("Familiar"));
        timeToLive = tag.contains("SuicideIn") ? tag.getInt("SuicideIn") : -1;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel server && isTemporary() && (--timeToLive <= 0 || shouldExpireEarly())) {
            expire(server);
            discard();
        }
    }

    protected boolean shouldExpireEarly() {
        return false;
    }

    protected abstract void expire(ServerLevel server);
}
