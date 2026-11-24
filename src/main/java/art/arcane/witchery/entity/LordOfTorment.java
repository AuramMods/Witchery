package art.arcane.witchery.entity;

import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.world.TormentTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LordOfTorment extends Monster implements RangedAttackMob {
    private final ServerBossEvent bar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private final Set<String> attackers = new HashSet<>();
    private int attackTicks;

    public LordOfTorment(EntityType<? extends LordOfTorment> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 20, false);
        xpReward = 80;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 500)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FLYING_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 8).add(Attributes.FOLLOW_RANGE, 50);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1, 20, 60, 12));
        goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 0.2));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTicks > 0) attackTicks--;
        if (!level().isClientSide()) {
            bar.setProgress(getHealth() / getMaxHealth());
            if (getTarget() instanceof ServerPlayer player && player.getAbilities().flying && random.nextFloat() < 0.05F
                    && !player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 5));
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE) || !isAlive()) return false;
        if (source.getEntity() instanceof ServerPlayer player) attackers.add(player.getGameProfile().getName());
        boolean damaged = super.hurt(source, Math.min(amount, source.is(WitcheryDamageTypes.DEMONIC) ? 8 : 5));
        if (damaged && isAlive() && level() instanceof ServerLevel server
                && !server.dimension().equals(WitcheryDimensions.TORMENT) && getHealth() <= getMaxHealth() / 2) {
            int floor = TormentTravel.randomFloor(random);
            for (ServerPlayer player : server.players()) {
                if (getBoundingBox().inflate(16, 32, 16).intersects(player.getBoundingBox())
                        || attackers.contains(player.getGameProfile().getName())) {
                    TormentTravel.requestBossEncounter(player, floor);
                }
            }
            TormentTravel.transitionEffect(server, position());
            discard();
        }
        return damaged;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float strength) {
        if (!(level() instanceof ServerLevel server)) return;
        beginAttack();
        SymbolProjectile.playMobCastSound(server, this);
        int bursts = random.nextInt(10) == 0 ? 9 : 3;
        server.addFreshEntity(SymbolProjectile.mobShot(server, this, target, SymbolSpell.IGNIANIMA, strength));
        for (int i = 0; i < bursts; i++) {
            Vec3 aim = SymbolProjectile.mobAim(this, target, strength);
            Soulfire shot = new Soulfire(WitcheryEntities.SOULFIRE.get(), this, aim);
            shot.setPos(getX(), getY(0.5) + 0.5, getZ());
            server.addFreshEntity(shot);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        beginAttack();
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 1, 1);
        boolean hit = target.hurt(damageSources().mobAttack(this), 7 + random.nextInt(15));
        if (hit) target.push(0, 0.4, 0);
        return hit;
    }

    private void beginAttack() {
        attackTicks = 10;
        level().broadcastEntityEvent(this, (byte)4);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) attackTicks = 10;
        else super.handleEntityEvent(event);
    }

    public int attackTicks() {
        return attackTicks;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bar.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        bar.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        List<Enchantment> choices = BuiltInRegistries.ENCHANTMENT.stream().toList();
        if (!choices.isEmpty()) {
            Enchantment first = choices.get(random.nextInt(choices.size()));
            Enchantment second = choices.get(random.nextInt(choices.size()));
            spawnAtLocation(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(first, first.getMaxLevel())));
            int low = Math.min(second.getMaxLevel(), second.getMinLevel() + 1);
            int level = low + random.nextInt(second.getMaxLevel() - low + 1);
            spawnAtLocation(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(second, level)));
        }
        spawnAtLocation(WitcheryIngredient.DEMON_HEART.item());
        spawnAtLocation(WitcheryIngredient.BREW_SOUL_TORMENT.item());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WitcherySounds.TORMENT_LAUGH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WitcherySounds.TORMENT_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WitcherySounds.TORMENT_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag root) {
        super.addAdditionalSaveData(root);
        ListTag names = new ListTag();
        for (String name : attackers) names.add(StringTag.valueOf(name));
        root.put("WITCAttackers", names);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag root) {
        super.readAdditionalSaveData(root);
        attackers.clear();
        ListTag names = root.getList("WITCAttackers", Tag.TAG_STRING);
        for (int index = 0; index < names.size(); index++) attackers.add(names.getString(index));
        bar.setName(getDisplayName());
    }
}
