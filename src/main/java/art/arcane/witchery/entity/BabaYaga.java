package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class BabaYaga extends Monster implements RangedAttackMob {
    private static final int INNATE_ARMOR = 4;
    private static final AttributeModifier DRINKING_PENALTY = new AttributeModifier(
            UUID.fromString("ab0df555-0786-4951-a8df-ca61749f164e"), "Drinking speed penalty", -0.25,
            AttributeModifier.Operation.ADDITION);
    private static final List<WitcheryIngredient> GIFTS = List.of(WitcheryIngredient.SPECTRAL_DUST,
            WitcheryIngredient.BAT_WOOL, WitcheryIngredient.TOE_OF_FROG, WitcheryIngredient.OWLETS_WING,
            WitcheryIngredient.DOG_TONGUE, WitcheryIngredient.BREW_OF_VINES, WitcheryIngredient.BREW_OF_SPROUTING,
            WitcheryIngredient.BREW_OF_HITCHCOCK, WitcheryIngredient.BREW_OF_CURSED_LEAPING,
            WitcheryIngredient.BREW_OF_FROGS_TONGUE);
    private static final int VISIT_GIFT_COUNT = 7;
    private final ServerBossEvent boss = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS);
    private String ownerName = "";
    @Nullable private UUID ownerId;
    private int visitTicks;
    private int drinkTicks;
    private int teleportCooldown = 100;

    public BabaYaga(EntityType<? extends BabaYaga> type, Level level) {
        super(type, level);
        xpReward = 70;
        setDropChance(EquipmentSlot.MAINHAND, 0);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 500)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1, 60, 10));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new ReachableTargetGoal<>(this, Player.class, false, this::canAttack));
    }

    public void bind(ServerPlayer player) {
        ownerId = player.getUUID();
        ownerName = player.getGameProfile().getName();
        visitTicks = 0;
        setPersistenceRequired();
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity instanceof Player player && (ownerId != null ? ownerId.equals(player.getUUID())
                : !ownerName.isEmpty() && ownerName.equalsIgnoreCase(player.getGameProfile().getName()));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !isOwnedBy(target) && super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target != null && isOwnedBy(target) ? null : target);
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
    public void aiStep() {
        if (level() instanceof ServerLevel server && isAlive()) {
            updatePotion();
            updateTargetAndTeleport();
            updateVisit(server);
            boss.setProgress(getHealth() / getMaxHealth());
            if (random.nextFloat() < 0.00075F) {
                server.sendParticles(ParticleTypes.WITCH, getX(), getY(0.9), getZ(), 16, 0.15, 0.2, 0.15, 0);
            }
        }
        super.aiStep();
    }

    private void updatePotion() {
        if (drinkTicks > 0) {
            if (--drinkTicks == 0) {
                for (MobEffectInstance effect : PotionUtils.getMobEffects(getMainHandItem())) addEffect(new MobEffectInstance(effect));
                setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(DRINKING_PENALTY);
            }
            return;
        }
        if (isOnFire() && !hasEffect(MobEffects.FIRE_RESISTANCE) && random.nextFloat() < 0.15F) {
            startDrinking(Potions.FIRE_RESISTANCE);
        } else if (getHealth() < getMaxHealth() && random.nextFloat() < 0.01F) {
            startDrinking(Potions.STRONG_HEALING);
        } else if (getTarget() != null && distanceToSqr(getTarget()) > 121 && !hasEffect(MobEffects.MOVEMENT_SPEED)
                && random.nextFloat() < 0.4375F) {
            startDrinking(Potions.SWIFTNESS);
        }
    }

    private void startDrinking(Potion potion) {
        setItemSlot(EquipmentSlot.MAINHAND, PotionUtils.setPotion(new ItemStack(Items.POTION), potion));
        drinkTicks = 12;
        setDrinkingSpeed();
        playSound(SoundEvents.WITCH_DRINK, 1, 1);
    }

    private void setDrinkingSpeed() {
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        speed.removeModifier(DRINKING_PENALTY);
        if (drinkTicks > 0) speed.addTransientModifier(DRINKING_PENALTY);
    }

    private void updateTargetAndTeleport() {
        if (teleportCooldown > 0) teleportCooldown--;
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;
        if (teleportCooldown == 0 && (getNavigation().isDone() || random.nextFloat() < 0.02F)) {
            teleportCooldown = 100;
            Vec3 step = target.getEyePosition().subtract(getEyePosition()).normalize().scale(8);
            if (randomTeleport(getX() + step.x + (random.nextDouble() - 0.5) * 8,
                    getY() + step.y + random.nextInt(16) - 8,
                    getZ() + step.z + (random.nextDouble() - 0.5) * 8, true)) {
                playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
            }
        }
        if ((target.isFallFlying() || target instanceof Player player && player.getAbilities().flying)
                && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && random.nextFloat() < 0.05F) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 5));
        }
    }

    private void updateVisit(ServerLevel level) {
        if (ownerId == null && ownerName.isEmpty()) return;
        visitTicks++;
        Player owner = level.players().stream().filter(this::isOwnedBy).findFirst().orElse(null);
        if (owner == null) return;
        if (visitTicks % 100 == 0 && distanceToSqr(owner) < 64) {
            WitcheryIngredient gift = GIFTS.get(random.nextInt(VISIT_GIFT_COUNT));
            int quantity = random.nextInt(3);
            if (quantity > 0) spawnAtLocation(new ItemStack(gift.item(), quantity));
        }
        if (visitTicks > 600) {
            departure(level);
            discard();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == this || !isAlive()) return false;
        boolean spectral = source.is(WitcheryDamageTypes.SPECTRAL_TOUCH);
        boolean magic = source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC) || source.is(DamageTypes.THORNS);
        float bounded = WitcheryDamageTypes.capBossDamage(source, amount, 15);
        boolean damaged = super.hurt(source, magic ? bounded * 0.15F : bounded);
        if (level() instanceof ServerLevel server) {
            if (source.getEntity() instanceof Player player && !spectral && !woodenAttack(source)) {
                player.hurt(damageSources().thorns(this), amount * 0.25F);
            } else if (source.getEntity() instanceof Mob mob && !mob.isRemoved() && (!spectral || damaged)
                    && (mob.getMobType() == MobType.UNDEAD || mob instanceof TamableAnimal pet && !pet.isTame())) {
                // The original touch invoked the retaliation both before and after draining health.
                transformAttacker(server, mob, spectral ? 2 : 1);
            }
        }
        return damaged;
    }

    @Override
    protected float getDamageAfterArmorAbsorb(DamageSource source, float damage) {
        float remaining = super.getDamageAfterArmorAbsorb(source, damage);
        return source.is(DamageTypeTags.BYPASSES_ARMOR) ? remaining : remaining * (25 - INNATE_ARMOR) / 25;
    }

    private void transformAttacker(ServerLevel level, Mob attacker, int count) {
        LivingEntity target = getTarget();
        boolean transformed = false;
        for (int index = 0; index < count; index++) {
            var spider = EntityType.CAVE_SPIDER.create(level);
            if (spider == null) continue;
            spider.moveTo(attacker.getX(), attacker.getY(), attacker.getZ(), attacker.getYRot(), attacker.getXRot());
            spider.setTarget(target);
            spider.setLastHurtByMob(target);
            spider.setLastHurtMob(target);
            if (level.addFreshEntity(spider)) {
                transformed = true;
                WitcheryNetwork.sendSpiderTransformation(level, spider.position());
            }
        }
        if (transformed) attacker.discard();
    }

    private static boolean woodenAttack(DamageSource source) {
        return source.getDirectEntity() instanceof WitcheryBolt bolt && bolt.isWoodenDamage()
                || source.getDirectEntity() instanceof LivingEntity attacker
                && attacker.getMainHandItem().getItem() instanceof SwordItem sword && sword.getTier() == Tiers.WOOD;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (drinkTicks > 0 || isOwnedBy(target)) return;
        Vec3 aim = target.position().add(target.getDeltaMovement()).subtract(position()).add(0, target.getEyeHeight() - 1.1, 0);
        double distance = aim.horizontalDistance();
        Projectile shot;
        int brewChoice = random.nextInt(3) == 0 ? random.nextInt(12) : -1;
        WitcheryIngredient selectedBrew = switch (brewChoice) {
            case 0, 1 -> WitcheryIngredient.BREW_OF_WEBS;
            case 2, 3 -> WitcheryIngredient.BREW_OF_THORNS;
            case 4, 5 -> WitcheryIngredient.BREW_OF_FROGS_TONGUE;
            case 6, 7 -> WitcheryIngredient.BREW_OF_INK;
            case 8, 9 -> WitcheryIngredient.BREW_OF_HITCHCOCK;
            case 10 -> WitcheryIngredient.BREW_OF_BATS;
            case 11 -> WitcheryIngredient.BREW_OF_WASTING;
            default -> null;
        };
        if (selectedBrew != null) {
            ThrownBrew brew = new ThrownBrew(this, level());
            brew.setItem(new ItemStack(selectedBrew.item()));
            shot = brew;
        } else {
            ThrownPotion potion = new ThrownPotion(level(), this);
            potion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), attackPotion(target, distance)));
            shot = potion;
        }
        shot.setXRot(shot.getXRot() + 20);
        shot.shoot(aim.x, aim.y + distance * 0.2, aim.z, 0.75F, 8);
        playSound(SoundEvents.WITCH_THROW, 1, 1);
        level().addFreshEntity(shot);
    }

    private Potion attackPotion(LivingEntity target, double distance) {
        if (distance >= 8 && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) return Potions.SLOWNESS;
        if (target.getHealth() >= 8 && !target.hasEffect(MobEffects.POISON)) return Potions.POISON;
        if (distance <= 3 && !target.hasEffect(MobEffects.WEAKNESS) && random.nextFloat() < 0.25F) return Potions.WEAKNESS;
        return Potions.STRONG_HARMING;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel server && isDeadOrDying()) {
            departure(server);
            discard();
        }
    }

    private void departure(ServerLevel server) {
        server.sendParticles(ParticleTypes.PORTAL, getX(), getY(0.5), getZ(), 16, 0.4, 0.55, 0.4, 0.08);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        int offerings = 2 + random.nextInt(3);
        for (int i = 0; i < offerings; i++) {
            int count = 1 + random.nextInt(3) + (looting > 0 ? random.nextInt(looting + 1) : 0);
            spawnAtLocation(new ItemStack(GIFTS.get(random.nextInt(GIFTS.size())).item(), count));
        }
        var enchantments = BuiltInRegistries.ENCHANTMENT.stream().toList();
        if (!enchantments.isEmpty()) {
            var enchantment = enchantments.get(random.nextInt(enchantments.size()));
            int lowest = Math.min(enchantment.getMinLevel() + 2, enchantment.getMaxLevel());
            int level = lowest + random.nextInt(enchantment.getMaxLevel() - lowest + 1);
            spawnAtLocation(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level)));
        }
        spawnAtLocation(WitcheryItems.getRequired("babashat").get());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        boss.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        boss.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        boss.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WitcherySounds.COVEN_TALK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITCH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WitcherySounds.COVEN_CACKLE.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("InnateArmor", true);
        tag.putString("Owner", ownerName);
        if (ownerId != null) tag.putUUID("OwnerUUID", ownerId);
        tag.putInt("VisitTicks", visitTicks);
        tag.putInt("DrinkTicks", drinkTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        var armor = getAttribute(Attributes.ARMOR);
        if (!tag.getBoolean("InnateArmor") && armor.getBaseValue() == INNATE_ARMOR) armor.setBaseValue(0);
        ownerName = tag.getString("Owner");
        ownerId = tag.hasUUID("OwnerUUID") ? tag.getUUID("OwnerUUID") : null;
        visitTicks = tag.getInt("VisitTicks");
        drinkTicks = tag.getInt("DrinkTicks");
        setDrinkingSpeed();
        boss.setName(getDisplayName());
    }
}
