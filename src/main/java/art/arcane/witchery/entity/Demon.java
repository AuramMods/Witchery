package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Demon extends AbstractGolem implements RangedAttackMob, Merchant {
    private static final byte SWING = 4;
    private static final int SWING_TICKS = 10;
    private static final int ATTACK_RANGE = 3;
    private static final int OFFER_STOCK = 2;
    private final boolean inDream;
    private boolean summoned;
    private int escapeTicks = -1;
    private int attackTicks;
    @Nullable private Player customer;
    @Nullable private MerchantOffers offers;

    public Demon(EntityType<? extends Demon> type, Level level) {
        super(type, level);
        inDream = level.dimension().equals(WitcheryDimensions.DREAM);
        xpReward = 10;
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 7)
                .add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1, true) {
            @Override
            public boolean canUse() {
                return customer == null && getTarget() != null && distanceToSqr(getTarget()) <= ATTACK_RANGE * ATTACK_RANGE && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return customer == null && getTarget() != null && distanceToSqr(getTarget()) <= ATTACK_RANGE * ATTACK_RANGE && super.canContinueToUse();
            }

            @Override
            protected double getAttackReachSqr(LivingEntity target) {
                return ATTACK_RANGE * ATTACK_RANGE;
            }
        });
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1, 20, 60, 15) {
            @Override
            public boolean canUse() {
                return customer == null && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return customer == null && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.6) {
            @Override
            public boolean canUse() {
                return customer == null && super.canUse();
            }
        });
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
    }

    public void summonedByRite() {
        summoned = true;
        setPersistenceRequired();
        addEffect(new MobEffectInstance(MobEffects.JUMP, -1, 4, false, false));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTicks > 0) attackTicks--;
        if (!(level() instanceof ServerLevel level) || !isAlive()) return;
        if (inDream && !DreamTravel.isDemonicNightmare(level)) {
            discard();
            return;
        }
        if (customer != null) {
            if (!customer.isAlive() || distanceToSqr(customer) > 16 || !(customer.containerMenu instanceof MerchantMenu)
                    || isInWater() || !onGround() || hurtMarked) {
                setTradingPlayer(null);
            } else {
                setTarget(null);
                getNavigation().stop();
                getLookControl().setLookAt(customer, 30, 30);
            }
        }
        if (escapeTicks > 0 && --escapeTicks == 0) {
            escapeTicks = -1;
            level.explode(this, getX(), getY(), getZ(), 3, true, Level.ExplosionInteraction.MOB);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return customer == null && super.canAttack(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, Math.min(amount, 15));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        attackTicks = SWING_TICKS;
        level().broadcastEntityEvent(this, SWING);
        boolean hit = target.hurt(damageSources().mobAttack(this), 7 + random.nextInt(15));
        if (hit) {
            target.push(0, 0.4, 0);
            doEnchantDamageEffects(this, target);
        }
        playSound(SoundEvents.IRON_GOLEM_ATTACK);
        return hit;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (!(level() instanceof ServerLevel level) || hasTongueCharm(target) && random.nextFloat() >= 0.05F) return;
        double spread = Mth.sqrt(velocity) * 0.5;
        double fromY = getY(0.5) + 0.5;
        LargeFireball bolt = new LargeFireball(level, this,
                target.getX() - getX() + random.nextGaussian() * spread, target.getY(0.5) - fromY,
                target.getZ() - getZ() + random.nextGaussian() * spread, 1);
        Vec3 view = getViewVector(1);
        bolt.setPos(getX() + view.x, fromY, getZ() + view.z);
        level.levelEvent(null, 1016, blockPosition(), 0);
        level.addFreshEntity(bolt);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == SWING) {
            attackTicks = SWING_TICKS;
        } else {
            super.handleEntityEvent(event);
        }
    }

    public int attackTicks() {
        return attackTicks;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damage, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(damage, looting, recentlyHit);
        if (recentlyHit) {
            int count = random.nextInt(2 + looting);
            if (count > 0) spawnAtLocation(new ItemStack(Items.MAGMA_CREAM, count));
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (inDream || customer != null || !isAlive() || held.getItem() instanceof SpawnEggItem || held.getItem() instanceof NameTagItem) {
            return super.mobInteract(player, hand);
        }
        if (player instanceof ServerPlayer) {
            setTradingPlayer(player);
            if (hasTongueCharm(player)) {
                for (MerchantOffer offer : getOffers()) {
                    ItemStack buy = offer.getBaseCostA();
                    int discount = buy.is(Items.GOLD_INGOT) ? 5 : buy.is(Items.EMERALD) ? 2 : buy.is(Items.DIAMOND) ? 0 : 1;
                    offer.setSpecialPriceDiff(-discount);
                }
            }
            openTradingScreen(player, getDisplayName(), 0);
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    private static boolean hasTongueCharm(LivingEntity entity) {
        return entity.getMainHandItem().is(WitcheryItems.getRequired("devilstonguecharm").get());
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        customer = player;
        if (offers != null && player == null) offers.forEach(MerchantOffer::resetSpecialPriceDiff);
    }

    @Override
    public Player getTradingPlayer() {
        return customer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (offers == null) {
            offers = new MerchantOffers();
            if (!level().isClientSide()) generateOffers(offers);
        }
        return offers;
    }

    private void generateOffers(MerchantOffers offers) {
        int count = 6 + random.nextInt(4);
        List<MerchantOffer> choices = new ArrayList<>();
        List<Enchantment> enchantments = BuiltInRegistries.ENCHANTMENT.stream().toList();
        for (int index = 0; index < count; index++) {
            Enchantment enchantment = enchantments.get(random.nextInt(enchantments.size()));
            int rank = Mth.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
            ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, rank));
            choices.add(offer(price(2 + random.nextInt(5 + rank * 10) + 3 * rank), book));
        }
        if (random.nextDouble() < 0.25) choices.add(offer(price(8 + random.nextInt(3)),
                new ItemStack(WitcheryIngredient.SPECTRAL_DUST.item(), 3 + random.nextInt(4))));
        if (random.nextDouble() < 0.25) choices.add(offer(price(8 + random.nextInt(3)),
                new ItemStack(WitcheryIngredient.DOG_TONGUE.item(), 4 + random.nextInt(4))));
        if (random.nextDouble() < 0.15) choices.add(offer(price(20 + random.nextInt(10)),
                new ItemStack(WitcheryIngredient.REDSTONE_SOUP.item())));
        if (random.nextDouble() < 0.15) choices.add(offer(new ItemStack(Items.DIAMOND), new ItemStack(Items.GHAST_TEAR, 2)));
        if (random.nextDouble() < 0.15) choices.add(offer(new ItemStack(Items.DIAMOND), new ItemStack(Items.ENDER_PEARL, 2)));
        Collections.shuffle(choices, new java.util.Random(random.nextLong()));
        Item currency = currency();
        choices.add(random.nextInt(3), offer(new ItemStack(currency, currency == Items.GOLD_INGOT ? 30 : 3),
                new ItemStack(WitcheryIngredient.DEMON_HEART.item())));
        offers.addAll(choices.subList(0, count));
    }

    private MerchantOffer offer(ItemStack cost, ItemStack item) {
        return new MerchantOffer(cost, item, OFFER_STOCK, 0, 0);
    }

    private Item currency() {
        double choice = random.nextDouble();
        if (choice < 0.2) return Items.BLAZE_ROD;
        if (choice < 0.4) return Items.MAGMA_CREAM;
        if (choice < 0.5) return Items.DIAMOND;
        return choice < 0.75 ? Items.EMERALD : Items.GOLD_INGOT;
    }

    private ItemStack price(int base) {
        Item currency = currency();
        int divisor = currency == Items.GOLD_INGOT ? 1 : currency == Items.EMERALD ? 3 : currency == Items.DIAMOND ? 5 : 4;
        return new ItemStack(currency, Math.max(1, base / divisor));
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        ItemStack currency = offer.getBaseCostA();
        if (!isClientSide() && (currency.is(Items.MAGMA_CREAM) || currency.is(Items.BLAZE_ROD))) {
            playSound(SoundEvents.WITHER_SHOOT);
            escapeTicks = 50;
        } else {
            playSound(SoundEvents.BLAZE_AMBIENT);
        }
        if (customer != null && hasTongueCharm(customer)) {
            customer.getMainHandItem().hurtAndBreak(5, customer, player -> player.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack trade) {
        if (!isClientSide() && ambientSoundTime > -getAmbientSoundInterval() + 20) {
            ambientSoundTime = -getAmbientSoundInterval();
            playSound(trade.isEmpty() ? SoundEvents.WITHER_AMBIENT : SoundEvents.BLAZE_AMBIENT);
        }
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    public boolean isClientSide() {
        return level().isClientSide();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damage) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.IRON_GOLEM_STEP);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("PlayerCreated", summoned);
        tag.putInt("EscapeTicks", escapeTicks);
        if (offers != null) tag.put("Bargains", offers.createTag());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        summoned = tag.getBoolean("PlayerCreated");
        if (summoned) setPersistenceRequired();
        escapeTicks = tag.contains("EscapeTicks", Tag.TAG_ANY_NUMERIC) ? tag.getInt("EscapeTicks") : -1;
        if (tag.contains("Bargains", Tag.TAG_COMPOUND)) {
            CompoundTag bargains = tag.getCompound("Bargains");
            for (Tag value : bargains.getList("Recipes", Tag.TAG_COMPOUND)) {
                CompoundTag offer = (CompoundTag) value;
                for (String slot : new String[]{"buy", "buyB", "sell"}) {
                    if (offer.contains(slot, Tag.TAG_COMPOUND)) {
                        ItemStack item = WitcheryIngredient.loadInventoryStack(offer.getCompound(slot));
                        offer.put(slot, item.save(new CompoundTag()));
                    }
                }
            }
            offers = new MerchantOffers(bargains);
            offers.forEach(MerchantOffer::resetSpecialPriceDiff);
        }
    }
}
