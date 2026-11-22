package art.arcane.witchery.entity;

import art.arcane.witchery.familiar.Familiar;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class Treefyd extends Monster {
    private static final EntityDataAccessor<String> OWNER = SynchedEntityData.defineId(Treefyd.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(Treefyd.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> SENTINEL = SynchedEntityData.defineId(Treefyd.class, EntityDataSerializers.BOOLEAN);
    private final TreefydAllowlist allowlist = new TreefydAllowlist();

    public Treefyd(EntityType<? extends Treefyd> type, Level level) {
        super(type, level);
        getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 50).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 3);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER, "");
        entityData.define(OWNER_ID, Optional.empty());
        entityData.define(SENTINEL, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return !isSentinel() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !isSentinel() && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 1, false, true, this::mayHunt));
    }

    boolean mayHunt(LivingEntity target) {
        return !(target instanceof Treefyd || target instanceof HornedHuntsman || target instanceof Ent
                || target instanceof FlyingMob || target instanceof FlyingAnimal || target instanceof AmbientCreature
                || target instanceof WaterAnimal || target instanceof WitchOwl || target instanceof WingedMonkey
                || target instanceof CovenWitch || target instanceof SleepingBody
                || target instanceof Familiar familiar && familiar.isFamiliar())
                && !isOwner(target) && !allowlist.knows(target);
    }

    private boolean isOwner(LivingEntity entity) {
        return entity instanceof Player player && entityData.get(OWNER_ID).map(player.getUUID()::equals)
                .orElseGet(() -> entityData.get(OWNER).equals(player.getGameProfile().getName()));
    }

    public boolean isSentinel() {
        return entityData.get(SENTINEL);
    }

    @Nullable
    public static Treefyd plant(ServerLevel level, Vec3 position, Player owner) {
        return spawn(level, position, owner, null);
    }

    @Nullable
    private static Treefyd spawn(ServerLevel level, Vec3 position, Player owner, @Nullable Treefyd original) {
        Treefyd plant = WitcheryEntities.TREEFYD.get().create(level);
        if (plant == null) return null;
        plant.moveTo(position.x, position.y, position.z, 0, 0);
        if (!level.noCollision(plant)) return null;
        ForgeEventFactory.onFinalizeSpawn(plant, level, level.getCurrentDifficultyAt(BlockPos.containing(position)),
                MobSpawnType.EVENT, null, null);
        plant.entityData.set(OWNER, owner.getGameProfile().getName());
        plant.entityData.set(OWNER_ID, Optional.of(owner.getUUID()));
        plant.setPersistenceRequired();
        if (original != null) plant.allowlist.copyFrom(original.allowlist);
        return level.addFreshEntity(plant) ? plant : null;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!isOwner(player) || held.is(Items.NAME_TAG)) return super.mobInteract(player, hand);
        boolean taglock = held.getItem() instanceof TaglockItem;
        boolean seed = WitcheryIngredient.TREEFYD_SEEDS.matches(held);
        boolean creeper = WitcheryIngredient.CREEPER_HEART.matches(held);
        boolean demon = WitcheryIngredient.DEMON_HEART.matches(held) || held.is(WitcheryBlocks.getRequired("demonheart").get().asItem());
        boolean boline = held.is(WitcheryItems.getRequired("boline").get());
        if (level() instanceof ServerLevel level) {
            if (taglock) {
                setTarget(null);
                if (TaglockItem.isBound(held) && allowlist.update(held, player, getUUID(), player.isShiftKeyDown())) held.shrink(1);
            } else if (seed) {
                Treefyd offspring = spawn(level, position().add(0.5, 0, 0.5), player, this);
                if (offspring == null) return InteractionResult.FAIL;
                offspring.plantingEffects(level);
                consume(player, held);
            } else if (creeper || demon) {
                getAttribute(Attributes.MAX_HEALTH).setBaseValue(creeper ? 100 : 150);
                getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(creeper ? 4 : 5);
                setHealth(getMaxHealth());
                pulse(level, creeper ? ParticleTypes.ITEM_SLIME : ParticleTypes.FLAME,
                        creeper ? SoundEvents.SILVERFISH_DEATH : SoundEvents.ENDER_DRAGON_GROWL);
                consume(player, held);
            } else if (boline) {
                entityData.set(SENTINEL, !isSentinel());
                if (isSentinel() && getTarget() == null) getNavigation().stop();
                player.displayClientMessage(Component.translatable(isSentinel()
                        ? "message.witchery.treefyd.sentinel" : "message.witchery.treefyd.patrol", getDisplayName()), true);
                return InteractionResult.CONSUME;
            }
            showAllowlist(player);
        }
        return taglock || seed || creeper || demon || boline ? InteractionResult.sidedSuccess(level().isClientSide)
                : super.mobInteract(player, hand);
    }

    private void showAllowlist(Player player) {
        var names = allowlist.names();
        names.add(0, entityData.get(OWNER));
        player.sendSystemMessage(Component.translatable("message.witchery.treefyd.allowlist", getDisplayName(), String.join(", ", names)));
    }

    private static void consume(Player player, ItemStack held) {
        if (!player.getAbilities().instabuild) held.shrink(1);
    }

    public void plantingEffects(ServerLevel level) {
        pulse(level, ParticleTypes.ITEM_SLIME, SoundEvents.SILVERFISH_DEATH);
        level.sendParticles(ParticleTypes.POOF, getX(), getY(0.5), getZ(), 16, 0.5, 0.5, 0.5, 0.04);
    }

    private void pulse(ServerLevel level, ParticleOptions particles, SoundEvent sound) {
        level.sendParticles(particles, getX(), getY(0.5), getZ(), 16, 0.5, 0.5, 0.5, 0.04);
        playSound(sound, 1, 1);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof Player player && !player.hasEffect(MobEffects.BLINDNESS)) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100), this);
        }
        return super.doHurtTarget(target);
    }

    @Override
    public int getMaxFallDistance() {
        return getTarget() == null ? 3 : 3 + (int) (getHealth() - 1);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return false;
    }

    @Override
    public int getExperienceReward() {
        return 2 * super.getExperienceReward();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        int flowers = random.nextInt(3) + (looting > 0 ? random.nextInt(looting + 1) : 0);
        if (flowers > 0) spawnAtLocation(new ItemStack(Items.POPPY, flowers));
        if (recentlyHit && random.nextInt(200) - looting < 5) spawnAtLocation(WitcheryIngredient.TREEFYD_SEEDS.item());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WitcherySounds.TREEFYD_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Owner", entityData.get(OWNER));
        entityData.get(OWNER_ID).ifPresent(id -> tag.putUUID("OwnerUUID", id));
        tag.putBoolean("SentinalPlant", isSentinel());
        allowlist.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(OWNER, tag.getString("Owner"));
        entityData.set(OWNER_ID, tag.hasUUID("OwnerUUID") ? Optional.of(tag.getUUID("OwnerUUID")) : Optional.empty());
        entityData.set(SENTINEL, tag.getBoolean("SentinalPlant"));
        if (!entityData.get(OWNER).isEmpty()) setPersistenceRequired();
        allowlist.load(tag);
    }
}
