package art.arcane.witchery.entity;

import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.item.GlassGobletItem;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class Lilith extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> FRIENDLY = SynchedEntityData.defineId(Lilith.class, EntityDataSerializers.BOOLEAN);
    private static final int HEAL_INTERVAL = 20;
    private static final int REFLECTED_FIREBALL_HEAL_PULSES = 10;
    private static final int INNATE_ARMOR = 8;
    private static final RandomSource SPELL_RANDOM = RandomSource.create();
    private final ServerBossEvent bar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS);
    private int invulnerableTicks;
    private int lifetime;
    private int weaknessTicks;
    private int attackTicks;

    public Lilith(EntityType<? extends Lilith> type, Level level) {
        super(type, level);
        xpReward = 60;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 200).add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 50).add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.ATTACK_DAMAGE, 7);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FRIENDLY, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1, 20, 60, 30));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
    }

    public boolean friendly() {
        return entityData.get(FRIENDLY);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTicks > 0) attackTicks--;
        if (!(level() instanceof ServerLevel world)) return;
        bar.setProgress(getHealth() / getMaxHealth());
        if (invulnerableTicks == 0 && !hasEffect(WitcheryEffects.RESIZING.get())) {
            addEffect(new MobEffectInstance(WitcheryEffects.RESIZING.get(), 10000, 3, true, false), this);
        }
        if (friendly()) return;
        if (invulnerableTicks > 0) {
            if (--invulnerableTicks == 0) playSound(SoundEvents.WITHER_SPAWN, 1, 1);
            if (tickCount % 10 == 0) heal(getMaxHealth() / 20);
            return;
        }
        lifetime++;
        if (weaknessTicks > 0) weaknessTicks--;
        if (tickCount % HEAL_INTERVAL != 0) return;
        if (weaknessTicks == 0) {
            var chilled = WitcheryEffects.LEGACY_EFFECTS.get("potion.chilled").get();
            heal(hasEffect(chilled) || hasEffect(MobEffects.WEAKNESS) ? 1 : 5);
        }
        if ((getTarget() != null || getLastHurtMob() != null) && random.nextInt(5) == 0) rainFire(world);
    }

    private void rainFire(ServerLevel world) {
        for (ServerPlayer player : world.getPlayers(candidate -> candidate.isAlive() && !candidate.isSpectator()
                && Math.abs(candidate.getX() - getX()) <= 32 && Math.abs(candidate.getZ() - getZ()) <= 32
                && Math.abs(candidate.getY() - getY()) <= 16)) {
            player.removeEffect(MobEffects.FIRE_RESISTANCE);
            if (!random.nextBoolean()) continue;
            player.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1, 1);
            int count = 3 + random.nextInt(4);
            for (int i = 0; i < count; i++) {
                var shot = new SmallFireball(world, this, 0, -0.2, 0);
                shot.setPos(player.getX() + random.nextDouble() * 4 - 2, player.getY() + 14 + random.nextInt(2),
                        player.getZ() + random.nextDouble() * 4 - 2);
                world.addFreshEntity(shot);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (invulnerableTicks > 0 && !source.is(WitcheryDamageTypes.SPECTRAL_TOUCH)) return false;
        if (source.getDirectEntity() instanceof LargeFireball && source.getEntity() instanceof Player) {
            // The original countdown advanced on healing pulses, including the next one.
            weaknessTicks = REFLECTED_FIREBALL_HEAL_PULSES * HEAL_INTERVAL - tickCount % HEAL_INTERVAL;
        }
        if (source.is(DamageTypeTags.IS_FIRE)) return false;
        return super.hurt(source, WitcheryDamageTypes.capBossDamage(source, amount, 12));
    }

    @Override
    protected float getDamageAfterArmorAbsorb(DamageSource source, float damage) {
        float remaining = super.getDamageAfterArmorAbsorb(source, damage);
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) return remaining;
        // The original eight points absorbed a fixed fraction, regardless of the size of the hit.
        return remaining * (25 - INNATE_ARMOR) / 25;
    }

    @Override
    public void die(DamageSource source) {
        if (!(level() instanceof ServerLevel world)) return;
        setHealth(getMaxHealth());
        entityData.set(FRIENDLY, true);
        setTarget(null);
        setNoAi(true);
        getActiveEffects().stream().filter(effect -> !effect.getCurativeItems().isEmpty())
                .map(MobEffectInstance::getEffect).toList().forEach(this::removeEffect);
        ServerPlayer player = source.getEntity() instanceof ServerPlayer attacker && attacker.isAlive()
                && attacker.level() == world && distanceToSqr(attacker) <= 4096 ? attacker : nearestPlayer(world);
        if (player == null) {
            discard();
            return;
        }
        Vec3 at = player.position().add(random.nextDouble() * 2 - 1, 0.05, random.nextDouble() * 2 - 1);
        if (!world.noCollision(this, getBoundingBox().move(at.subtract(position())))) at = player.position();
        teleportTo(at.x, at.y, at.z);
        portal(world);
        GlassGobletItem.tell(player, "lilith_defeated");
        playSound(WitcherySounds.LILITH_TALK.get(), 1, 1);
    }

    @Nullable
    private ServerPlayer nearestPlayer(ServerLevel world) {
        ServerPlayer nearest = null;
        double distance = Double.MAX_VALUE;
        for (ServerPlayer player : world.players()) {
            if (!player.isAlive() || player.isSpectator() || Math.abs(player.getX() - getX()) > 32
                    || Math.abs(player.getZ() - getZ()) > 32 || Math.abs(player.getY() - getY()) > 16) continue;
            double dist = distanceToSqr(player);
            if (dist < distance) {
                nearest = player;
                distance = dist;
            }
        }
        return nearest;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!friendly()) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        ItemStack offering = player.getItemInHand(hand);
        playSound(WitcherySounds.LILITH_TALK.get(), 1, 1);
        int rank = Vampirism.level(player);
        if (offering.getItem() instanceof GlassGobletItem) {
            if (rank == 0) {
                offering = offering.copy();
                GlassGobletItem.fillFromLilith(offering);
                Vampirism.drainHumanBlood(serverPlayer);
                GlassGobletItem.bloodParticles(serverPlayer.serverLevel(), serverPlayer);
                reward(serverPlayer, hand, offering, "lilith_blood");
            } else GlassGobletItem.tell(serverPlayer, "already_vampire");
        } else if (offering.is(WitcheryItems.getRequired("garlic").get())) {
            if (rank > 0) {
                Vampirism.cure(serverPlayer);
                reward(serverPlayer, hand, ItemStack.EMPTY, "lilith_cured");
            } else GlassGobletItem.tell(serverPlayer, "cure_refused");
        } else if (offering.is(Items.POPPY)) {
            if (Vampirism.acceptPoppy(serverPlayer)) reward(serverPlayer, hand, ItemStack.EMPTY, "lilith_flight");
            else GlassGobletItem.tell(serverPlayer, "flight_refused");
        } else if (!offering.isEmpty()) {
            var enchantments = EnchantmentHelper.selectEnchantment(random, offering, 40, false);
            if (enchantments.isEmpty()) GlassGobletItem.tell(serverPlayer, "lilith_offer");
            else {
                ItemStack enchanted;
                if (offering.is(Items.BOOK)) {
                    enchanted = new ItemStack(Items.ENCHANTED_BOOK, offering.getCount());
                    CompoundTag tag = offering.getTag();
                    if (tag != null) enchanted.setTag(tag.copy());
                } else enchanted = offering.copy();
                if (enchanted.is(Items.ENCHANTED_BOOK)) {
                    for (var enchantment : enchantments) EnchantedBookItem.addEnchantment(enchanted, enchantment);
                } else {
                    var existing = EnchantmentHelper.getEnchantments(enchanted);
                    for (var enchantment : enchantments) existing.merge(enchantment.enchantment, enchantment.level, Math::max);
                    EnchantmentHelper.setEnchantments(existing, enchanted);
                }
                if (enchanted.isDamageableItem()) enchanted.setDamageValue(0);
                reward(serverPlayer, hand, enchanted, "lilith_enchanted");
            }
        } else GlassGobletItem.tell(serverPlayer, "lilith_offer");
        return InteractionResult.CONSUME;
    }

    private void reward(ServerPlayer player, InteractionHand hand, ItemStack reward, String message) {
        player.setItemInHand(hand, ItemStack.EMPTY);
        if (!reward.isEmpty()) {
            int stackLimit = reward.getMaxStackSize();
            while (reward.getCount() > stackLimit) player.drop(reward.split(stackLimit), false);
            player.drop(reward, false);
        }
        GlassGobletItem.tell(player, message);
        portal(player.serverLevel());
        discard();
    }

    private void portal(ServerLevel world) {
        world.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 24, 0.7, 1.1, 0.7, 0.15);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float strength) {
        if (!(level() instanceof ServerLevel world) || friendly() || invulnerableTicks > 0
                || !world.random.nextBoolean()) return;
        beginAttack();
        boolean fireball = world.random.nextInt(3) == 0;
        MobFireball.playLaunchCue(world, this);
        if (fireball) {
            world.addFreshEntity(new MobFireball(world, this, target, strength));
        } else {
            // The symbol branch advances Lilith's random source before aiming in 0.24.1.
            random.nextInt(10);
            SymbolSpell spell = switch ((int) Math.ceil(SPELL_RANDOM.nextDouble() * 13)) {
                case 0, 1 -> SymbolSpell.IGNIANIMA;
                case 2, 3, 4, 5, 6 -> SymbolSpell.FLIPENDO;
                case 7 -> SymbolSpell.IMPEDIMENTA;
                case 8 -> SymbolSpell.CONFUNDUS;
                default -> SymbolSpell.ATTRAHO;
            };
            world.addFreshEntity(SymbolProjectile.mobShot(world, this, target, spell, strength));
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (friendly() || invulnerableTicks > 0) return false;
        beginAttack();
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 1, 1);
        boolean damaged = target.hurt(damageSources().mobAttack(this), 7 + random.nextInt(15));
        if (damaged) target.push(0, 0.4, 0);
        return damaged;
    }

    private void beginAttack() {
        attackTicks = 10;
        level().broadcastEntityEvent(this, (byte) 4);
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return friendly() ? null : WitcherySounds.LILITH_TALK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WitcherySounds.LILITH_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WitcherySounds.LILITH_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Invul", invulnerableTicks);
        tag.putLong("Lifetime", lifetime);
        tag.putBoolean("Friendly", friendly());
        tag.putInt("WeaknessTicks", weaknessTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        invulnerableTicks = tag.getInt("Invul");
        lifetime = tag.getInt("Lifetime");
        weaknessTicks = tag.getInt("WeaknessTicks");
        if (tag.getBoolean("Friendly")) {
            entityData.set(FRIENDLY, true);
            setNoAi(true);
        }
        if (hasCustomName()) bar.setName(getDisplayName());
    }
}
