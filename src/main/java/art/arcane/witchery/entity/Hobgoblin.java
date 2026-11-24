package art.arcane.witchery.entity;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class Hobgoblin extends AbstractVillager {
    private static final int PROFESSIONS = 5;
    private static final int RESTOCK_DELAY = 40;
    private static final EntityDataAccessor<Integer> PROFESSION =
            SynchedEntityData.defineId(Hobgoblin.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> WORKING =
            SynchedEntityData.defineId(Hobgoblin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WORSHIPPING =
            SynchedEntityData.defineId(Hobgoblin.class, EntityDataSerializers.BOOLEAN);

    private int riches;
    private int restockTicks;
    private boolean preventDespawn;
    private boolean hauling;
    private HobgoblinWorshipGoal worship;
    private HobgoblinMateGoal mating;

    public Hobgoblin(EntityType<? extends Hobgoblin> type, Level level) {
        super(type, level);
        var navigation = (GroundPathNavigation) getNavigation();
        navigation.setCanOpenDoors(true);
        navigation.setCanFloat(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.4).add(Attributes.ATTACK_DAMAGE, 2);
    }

    public static boolean canSpawn(EntityType<Hobgoblin> type, ServerLevelAccessor level, MobSpawnType reason,
                                   BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PROFESSION, 0);
        entityData.define(WORKING, false);
        entityData.define(WORSHIPPING, false);
    }

    public int profession() {
        return entityData.get(PROFESSION);
    }

    public boolean isWorking() {
        return entityData.get(WORKING);
    }

    void setWorking(boolean working) {
        entityData.set(WORKING, working);
    }

    public boolean isWorshipping() {
        return entityData.get(WORSHIPPING);
    }

    void setWorshipping(boolean worshipping) {
        entityData.set(WORSHIPPING, worshipping);
    }

    public void offerWorship(BlockPos statue) {
        if (worship != null) worship.offer(statue);
    }

    HobgoblinMateGoal mateGoal() {
        return mating;
    }

    void setHauling(boolean hauling) {
        this.hauling = hauling;
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder() {
        return !isWorking() && !hauling && !isWorshipping();
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type,
                                        @Nullable SpawnGroupData group, @Nullable CompoundTag tag) {
        entityData.set(PROFESSION, random.nextInt(type == MobSpawnType.STRUCTURE ? 4 : PROFESSIONS));
        return super.finalizeSpawn(level, difficulty, type, group, tag);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        worship = new HobgoblinWorshipGoal(this);
        goalSelector.addGoal(1, worship);
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(2, new HobgoblinHaulGoal(this));
        goalSelector.addGoal(2, new HobgoblinMiningGoal(this));
        goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 8, 0.6, 0.6,
                player -> !isTrading() && !isLeashed() && (player.isSprinting() || !player.isShiftKeyDown())));
        goalSelector.addGoal(3, new AvoidEntityGoal<>(this, VillageGuard.class, 12, 0.8, 0.8,
                guard -> !isLeashed() && hasRaidingParty()));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(5, new VillageShelterGoal(this, true));
        goalSelector.addGoal(5, new RestrictVillageDoorGoal(this));
        goalSelector.addGoal(6, new OpenDoorGoal(this, true));
        mating = new HobgoblinMateGoal(this);
        goalSelector.addGoal(7, mating);
        goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3));
        goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Villager.class, 10, true, true,
                villager -> !isLeashed() && !isTrading() && hasRaidingParty()));
    }

    private boolean hasRaidingParty() {
        return level().getEntitiesOfClass(Hobgoblin.class, getBoundingBox().inflate(8), Hobgoblin::isAlive).size() >= 3;
    }

    private boolean nearVillage() {
        return level() instanceof ServerLevel server && server.isCloseToVillage(blockPosition(), 2);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!preventDespawn && (isLeashed() || (tickCount % 40 == 0 && nearVillage()))) preventDespawn = true;
        if (!isTrading() && restockTicks > 0 && --restockTicks == 0) {
            MerchantOffers stock = getOffers();
            if (stock.size() > 1) {
                for (int i = 0; i < stock.size(); i++) {
                    MerchantOffer offer = stock.get(i);
                    if (!offer.isOutOfStock()) continue;
                    CompoundTag extended = offer.createTag();
                    extended.putInt("maxUses", offer.getMaxUses() + 2 + random.nextInt(6) + random.nextInt(6));
                    stock.set(i, new MerchantOffer(extended));
                }
            }
            updateTrades();
            addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200));
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !isAlive() || isTrading() || isBaby()
                || isLeashed() || isWorshipping() || player.isShiftKeyDown()) return super.mobInteract(player, hand);
        if (!level().isClientSide && nearVillage() && !getOffers().isEmpty()) {
            setTradingPlayer(player);
            openTradingScreen(player, getDisplayName(), 1);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    public InteractionResult exchangePickaxe(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !isAlive() || !isLeashed() || isBaby() || isTrading() || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack offered = player.getItemInHand(hand);
        if (offered.getItem() instanceof SpawnEggItem) return InteractionResult.PASS;
        ItemStack held = getMainHandItem();
        if (held.isEmpty() && !(offered.getItem() instanceof PickaxeItem)) return InteractionResult.PASS;
        if (!level().isClientSide) {
            if (!held.isEmpty()) {
                ItemStack remaining = held.copy();
                player.addItem(remaining);
                if (!remaining.isEmpty()) {
                    var drop = new ItemEntity(level(), getX(), getY() + 1, getZ(), remaining.copy());
                    drop.setDefaultPickUpDelay();
                    if (level().addFreshEntity(drop)) remaining = ItemStack.EMPTY;
                }
                setItemInHand(hand, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            } else {
                setItemInHand(hand, offered.split(1));
                setGuaranteedDrop(EquipmentSlot.MAINHAND);
            }
            preventDespawn = true;
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return !isLeashed() && isAlive();
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return !WitcheryServerConfig.HOBGOBLIN_DESPAWN_BLOCK.get() && !preventDespawn && !isLeashed()
                && !isWorshipping() && !nearVillage();
    }

    @Override
    protected void updateTrades() {
        if (!level().isClientSide && offers != null) HobgoblinTrades.addNext(offers, profession(), random);
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        super.notifyTrade(offer);
        MerchantOffers stock = getOffers();
        if (!stock.isEmpty() && stock.get(stock.size() - 1) == offer) restockTicks = RESTOCK_DELAY;
        if (offer.getBaseCostA().is(Items.EMERALD)) riches += offer.getBaseCostA().getCount();
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {}

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return WitcherySounds.GOBLIN_YES.get();
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean valid) {
        return valid ? WitcherySounds.GOBLIN_YES.get() : WitcherySounds.GOBLIN_NO.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isTrading() ? WitcherySounds.GOBLIN_HAGGLE.get() : WitcherySounds.GOBLIN_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WitcherySounds.GOBLIN_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WitcherySounds.GOBLIN_DEATH.get();
    }

    @Override
    public float getVoicePitch() {
        return 1.2F;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.85F;
    }

    @Override
    public Vec3 getRopeHoldPosition(float partialTick) {
        return getPosition(partialTick).add(0, getBbHeight() * 0.65, 0);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event != 12) {
            super.handleEntityEvent(event);
            return;
        }
        for (int i = 0; i < 5; i++) {
            level().addParticle(net.minecraft.core.particles.ParticleTypes.HEART, getRandomX(1),
                    getRandomY() + 0.4, getRandomZ(1), 0, 0, 0);
        }
    }

    @Override
    @Nullable
    public Hobgoblin getBreedOffspring(ServerLevel level, AgeableMob mate) {
        Hobgoblin child = WitcheryEntities.GOBLIN.get().create(level);
        if (child != null) child.entityData.set(PROFESSION, random.nextInt(PROFESSIONS));
        return child;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Profession", profession());
        tag.putInt("Riches", riches);
        tag.putInt("RestockTicks", restockTicks);
        tag.putBoolean("PreventDespawn", preventDespawn);
        if (worship != null) worship.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(PROFESSION, Mth.clamp(tag.getInt("Profession"), 0, PROFESSIONS - 1));
        riches = Math.max(0, tag.getInt("Riches"));
        restockTicks = Mth.clamp(tag.getInt("RestockTicks"), 0, RESTOCK_DELAY);
        preventDespawn = tag.getBoolean("PreventDespawn");
        if (worship != null) worship.load(tag);
        if (tag.contains("Offers", Tag.TAG_COMPOUND)) {
            CompoundTag migrated = tag.getCompound("Offers").copy();
            for (Tag entry : migrated.getList("Recipes", Tag.TAG_COMPOUND)) {
                var trade = (CompoundTag) entry;
                for (String slot : new String[]{"buy", "buyB", "sell"}) {
                    if (!trade.contains(slot, Tag.TAG_COMPOUND)) continue;
                    CompoundTag stack = trade.getCompound(slot);
                    ItemStack item = loadTradeStack(stack);
                    if (!item.isEmpty()) trade.put(slot, item.save(new CompoundTag()));
                }
            }
            MerchantOffers loaded = new MerchantOffers(migrated);
            loaded.removeIf(offer -> offer.getBaseCostA().isEmpty() || offer.getResult().isEmpty());
            offers = loaded.isEmpty() ? null : loaded;
        }
    }

    private static ItemStack loadTradeStack(CompoundTag stack) {
        if (stack.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            String modernId = switch (stack.getString("id")) {
                case "minecraft:cooked_fished" -> "minecraft:cooked_cod";
                case "minecraft:melon" -> "minecraft:melon_slice";
                case "minecraft:wool" -> "minecraft:white_wool";
                default -> "";
            };
            if (!modernId.isEmpty()) {
                stack = stack.copy();
                stack.putString("id", modernId);
            }
        }
        return WitcheryIngredient.loadInventoryStack(stack);
    }
}
