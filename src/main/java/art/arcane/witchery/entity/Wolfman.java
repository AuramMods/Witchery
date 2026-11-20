package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class Wolfman extends Monster {
    private int attackTicks;
    private boolean infectious;
    @Nullable
    private CompoundTag formerVillager;

    public Wolfman(EntityType<? extends Wolfman> type, Level level) {
        super(type, level);
        xpReward = 20;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 40).add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 5).add(Attributes.MAX_HEALTH, 80).add(Attributes.ARMOR, 10);
    }

    @Override
    protected void registerGoals() {
        if (getNavigation() instanceof GroundPathNavigation navigation) navigation.setCanOpenDoors(true);
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new BreakDoorGoal(this, difficulty -> difficulty == Difficulty.HARD));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1));
        goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1, false, 4, () -> true));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false,
                target -> !Lycanthropy.isShifted(target)));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, 0, false, false, null));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, WitchHunter.class, 0, true, false, null));
    }

    @Override
    public void tick() {
        super.tick();
        if (attackTicks > 0) attackTicks--;
        if (!level().isClientSide && isAlive()) {
            if (tickCount % 40 == 4) removeEffect(MobEffects.POISON);
            if (hasFormerVillager() && tickCount % 100 == 3
                    && (!Lycanthropy.isFullMoon(level()) || level().getDifficulty() == Difficulty.PEACEFUL)
                    && !Lycanthropy.hasWolfsbane(this)) WerewolfVillagers.revert(this, true);
        }
    }

    @Override
    protected boolean shouldDespawnInPeaceful() { return !hasFormerVillager(); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        return super.hurt(source, Lycanthropy.isSilver(source) ? Math.min(amount * 1.5F, 15) : Math.min(amount, 1));
    }

    @Override
    public boolean doHurtTarget(Entity victim) {
        playAttack();
        level().broadcastEntityEvent(this, (byte) 4);
        return super.doHurtTarget(victim);
    }

    public void playAttack() { attackTicks = 10; }

    public int attackTicks() { return attackTicks; }

    public void setPlayerAnimation(Player player) {
        attackTicks = player.swinging ? Math.max(0, 10 - player.swingTime) : 0;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) playAttack();
        else super.handleEntityEvent(event);
    }

    public void setFormerVillager(CompoundTag data, boolean infectious) {
        formerVillager = data;
        this.infectious = infectious;
    }

    public boolean hasFormerVillager() { return formerVillager != null; }

    public CompoundTag formerVillagerData() { return formerVillager == null ? new CompoundTag() : formerVillager.copy(); }

    public boolean isInfectious() { return infectious; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Infectious", infectious);
        if (formerVillager != null) tag.put("FormerVillager", formerVillager.copy());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        infectious = tag.getBoolean("Infectious");
        formerVillager = tag.contains("FormerVillager", Tag.TAG_COMPOUND) ? tag.getCompound("FormerVillager").copy()
                : WerewolfVillagers.legacyProfession(tag);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean playerKill) {
        super.dropCustomDeathLoot(source, looting, playerKill);
        if (playerKill && random.nextInt(200) < 5 + looting) {
            switch (random.nextInt(3)) {
                case 0 -> spawnAtLocation(WitcheryIngredient.SILVER_DUST.item(), random.nextInt(3) + 1);
                case 1 -> spawnAtLocation(Items.BONE);
                case 2 -> spawnAtLocation(Items.LEATHER);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return random.nextInt(20) == 0 ? WitcherySounds.WOLFMAN_HOWL.get() : WitcherySounds.WOLFMAN_SAY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return WitcherySounds.WOLFMAN_HIT.get(); }

    @Override
    protected SoundEvent getDeathSound() { return WitcherySounds.WOLFMAN_DEATH.get(); }
}
