package art.arcane.witchery.entity;

import art.arcane.witchery.item.ImpSpellContractItem;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.util.WitcheryTime;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class Imp extends TamableAnimal {
    private static final EntityDataAccessor<Boolean> POWERED = SynchedEntityData.defineId(Imp.class, EntityDataSerializers.BOOLEAN);
    private static final long GIFT_COOLDOWN = 3600;
    private static final long POWER_DURATION = 72_000;
    private static final int OWNERSHIP_COST = 25;
    private static final WitcheryIngredient[] SECRETS = {
            WitcheryIngredient.BREW_SOUL_HUNGER, WitcheryIngredient.BREW_SOUL_FEAR,
            WitcheryIngredient.BREW_SOUL_ANGUISH, WitcheryIngredient.CONTRACT_OF_TORMENT
    };
    private static final WitcheryIngredient[] EXTRA_GIFTS = {
            WitcheryIngredient.BAT_WOOL, WitcheryIngredient.DOG_TONGUE, WitcheryIngredient.TOE_OF_FROG,
            WitcheryIngredient.OWLETS_WING, WitcheryIngredient.ENT_BRANCH, WitcheryIngredient.INFERNAL_BLOOD,
            WitcheryIngredient.CREEPER_HEART
    };
    private static final int[] EXTRA_GIFT_COUNTS = {5, 5, 2, 2, 1, 2, 2};
    private static final Map<Item, Integer> SHINY_GIFTS = Map.ofEntries(
            Map.entry(Items.DIAMOND, 8), Map.entry(Items.DIAMOND_AXE, 24), Map.entry(Items.DIAMOND_HOE, 16),
            Map.entry(Items.DIAMOND_SWORD, 16), Map.entry(Items.DIAMOND_SHOVEL, 8), Map.entry(Items.DIAMOND_PICKAXE, 24),
            Map.entry(Items.EMERALD, 3), Map.entry(Items.GOLD_INGOT, 1), Map.entry(Items.NETHER_STAR, 16),
            Map.entry(Items.BLAZE_ROD, 1), Map.entry(Items.GHAST_TEAR, 4), Map.entry(Items.GOLDEN_AXE, 3),
            Map.entry(Items.GOLDEN_SWORD, 2), Map.entry(Items.GOLDEN_HOE, 2), Map.entry(Items.GOLDEN_SHOVEL, 1),
            Map.entry(Items.GOLDEN_PICKAXE, 3), Map.entry(Items.GOLD_BLOCK, 9), Map.entry(Items.EMERALD_BLOCK, 27),
            Map.entry(Items.DIAMOND_BLOCK, 72), Map.entry(Items.LAPIS_BLOCK, 7), Map.entry(Items.REDSTONE_BLOCK, 5));
    private static final String[] DEMON_NAMES = {
        "Ppaironael", "Aethon", "Tyrnak", "Beelzebuth", "Botis", "Moloch", "Taet", "Epnanaet",
        "Unonom", "Hexpemsazon", "Thayax", "Ethahoat", "Pruslas", "Ahtuxies", "Laripael", "Elxar",
        "Tarihimal", "Sapanolr", "Sahaminapiel", "Honed", "Oghmus", "Zedeson", "Halmaneop", "Nopoz",
        "Ekarnahox", "Sacuhatakael", "Ticos", "Arametheus", "Azmodaeus", "Larhepeis", "Topriraiz", "Rarahaimzah",
        "Tedrahamael", "Osaselael", "Phlegon", "Nelokhiel", "Haristum", "Zul", "Larhepeis", "Aamon",
        "Tramater", "Ehhbes", "Kra`an", "Quarax", "Hotesiatrem", "Surgat", "Nu`uhn", "Litedabh",
        "Unonom", "Bolenoz", "Hilopael", "Haristum", "Uhn", "Hiepacth", "Pemcapso", "Ankou",
        "Pundohien", "Koit", "Montobulus", "Amsaset", "Aropet", "Isnal", "Solael", "Exroh",
        "Sidragrosam", "Pnecamob", "Malashim", "Beelzebuth", "Ehohit", "Izatap", "Olon", "Assoaz",
        "Agalierept", "Krakus", "Umlaboor", "Aknrar", "Damaz", "Rhysus", "Pundohien", "Ba`al",
        "Rasuniolpas", "Anhoor", "Nyarlathotep", "Krakus", "Larhepeis", "Itakup", "Erdok", "Umlaboor",
        "Ezon", "Krakus", "Glassyalabolas", "Kra`an", "Ehnnat", "Terxor", "Asramel", "Tadal",
        "Arpzih", "Azmodaeus", "Henbolaron", "Rhysus",
    };

    private int affection;
    private int secretsShared;
    private long lastGiftTime;
    private long poweredUntil;
    private int ageTicks;

    public Imp(EntityType<? extends Imp> type, Level level) {
        super(type, level);
        GroundPathNavigation navigation = (GroundPathNavigation) getNavigation();
        navigation.setCanOpenDoors(true);
        navigation.setCanFloat(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 50).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.FOLLOW_RANGE, 24);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(4, new OpenDoorGoal(this, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 1, false, true,
                target -> !isTame()));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(POWERED, false);
    }

    public boolean isPowered() {
        return entityData.get(POWERED);
    }

    private void setPoweredUntil(long until) {
        poweredUntil = until;
        entityData.set(POWERED, until > WitcheryTime.epochTicks());
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(isPowered() ? 8 : 4);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (POWERED.equals(accessor)) refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return isPowered() ? EntityDimensions.scalable(0.6F, 1.3F) : super.getDimensions(pose);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack offered = player.getItemInHand(hand);
        if (!isTame() && !WitcheryIngredient.CONTRACT_OF_OWNERSHIP.matches(offered)) return InteractionResult.PASS;
        if (player instanceof ServerPlayer server) {
            if (!isTame()) signContract(server, offered);
            else if (!offered.isEmpty()) offerGift(server, offered);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private void signContract(ServerPlayer player, ItemStack stack) {
        if (!TaglockItem.isBoundTo(stack, player)) {
            say(player, TaglockItem.isBound(stack) ? "wrong_signer" : "unsigned");
            return;
        }
        if (!player.getAbilities().instabuild && player.experienceLevel < OWNERSHIP_COST) {
            say(player, "no_experience", OWNERSHIP_COST);
            return;
        }
        consume(player, stack);
        player.giveExperienceLevels(-OWNERSHIP_COST);
        tame(player);
        setTarget(null);
        getNavigation().stop();
        setPersistenceRequired();
        restrictTo(blockPosition(), 16);
        setCustomName(Component.literal(DEMON_NAMES[random.nextInt(DEMON_NAMES.length)]));
        say(player, "bound");
    }

    private void offerGift(ServerPlayer player, ItemStack item) {
        long now = WitcheryTime.epochTicks();
        var spell = TaglockItem.isBound(item) ? ImpSpellContractItem.spellFor(item) : null;
        if (item.is(WitcheryIngredient.DEMON_HEART.item())) {
            consume(player, item);
            setPoweredUntil(now + POWER_DURATION);
            say(player, "powered");
        } else if (item.is(WitcheryIngredient.ICY_NEEDLE.item())) {
            consume(player, item);
            setPoweredUntil(0);
            say(player, "power_lost");
        } else if (spell != null) {
            castContract(player, item, spell, now);
        } else {
            Integer value = SHINY_GIFTS.get(item.getItem());
            if (value == null || item.isDamaged()) {
                say(player, "dislikes");
                return;
            }
            consume(player, item);
            affection = (int) Math.min(Integer.MAX_VALUE, (long) affection + value);
            boolean ready = giftReady(player, now);
            if (affection >= 20 && ready && random.nextInt(Math.max(1, 30 - affection)) == 0) {
                reciprocate(player, now);
            } else {
                say(player, ready ? "likes" : "wait");
            }
        }
    }

    private void castContract(ServerPlayer player, ItemStack stack, ImpSpellContractItem.Spell spell, long now) {
        if (isPowered()) {
            say(player, "spell_powered");
        } else if (affection < 20) {
            say(player, "spell_disliked");
        } else if (!giftReady(player, now)) {
            say(player, "spell_wait");
        } else {
            var target = TaglockItem.isBoundTo(stack, player) ? player : TaglockItem.resolveTarget(player.serverLevel(), stack);
            if (target == null || !target.isAlive()) {
                say(player, "spell_missing_target", TaglockItem.boundName(stack));
            } else if (!spell.activate(target)) {
                say(player, "spell_failed");
            } else {
                lastGiftTime = now;
                consume(player, stack);
                say(player, "spell_cast", target.getDisplayName());
            }
        }
    }

    private boolean giftReady(ServerPlayer player, long now) {
        return now > lastGiftTime + GIFT_COOLDOWN || player.getAbilities().instabuild;
    }

    private void reciprocate(ServerPlayer player, long now) {
        ItemStack reward;
        if (secretsShared < SECRETS.length) {
            reward = new ItemStack(SECRETS[secretsShared].item());
        } else {
            int bonus = random.nextInt(EXTRA_GIFTS.length);
            reward = new ItemStack(EXTRA_GIFTS[bonus].item(), EXTRA_GIFT_COUNTS[bonus]);
        }
        if (spawnAtLocation(reward) == null) return;
        secretsShared = Math.min(secretsShared + 1, SECRETS.length);
        lastGiftTime = now;
        affection = 0;
        say(player, "reward");
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, getX(), getY(0.6), getZ(), 16, 0.4, 0.4, 0.4, 0);
        playSound(SoundEvents.NOTE_BLOCK_HARP.value(), 1, 1);
    }

    private static void consume(ServerPlayer player, ItemStack stack) {
        if (!player.getAbilities().instabuild) stack.shrink(1);
    }

    private void say(ServerPlayer player, String action, Object... args) {
        player.sendSystemMessage(Component.literal("<").append(getName()).append("> ")
                .append(Component.translatable("message.witchery.imp." + action, args)).withStyle(ChatFormatting.DARK_RED));
        playSound(WitcherySounds.IMP_LAUGH.get(), 0.5F, getVoicePitch());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (isPowered()) level().addParticle(ParticleTypes.FLAME, getRandomX(0.7), getRandomY(), getRandomZ(0.7), 0, 0.02, 0);
            return;
        }
        ageTicks++;
        if (poweredUntil > 0 && WitcheryTime.epochTicks() >= poweredUntil) setPoweredUntil(0);
        if (ageTicks % 400 == 0 || isPowered() && ageTicks % 20 == 0) heal(1);
        if (ageTicks % 6000 == 0 && getOwner() instanceof ServerPlayer owner) {
            affection = Math.max(0, affection - 1);
            if (affection == 0 && ageTicks > POWER_DURATION && random.nextInt(100) == 0) {
                say(owner, "left");
                discard();
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, Math.min(amount, isPowered() ? 5 : 15));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WitcherySounds.IMP_LAUGH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WitcherySounds.IMP_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WitcherySounds.IMP_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 800;
    }

    @Override
    protected float getSoundVolume() {
        return 0.5F;
    }

    @Override
    public float getVoicePitch() {
        return (random.nextFloat() - random.nextFloat()) * 0.2F + (isPowered() ? 0.7F : 1.1F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Affection", affection);
        tag.putInt("SecretsShared", secretsShared);
        tag.putLong("LastGiftTime", lastGiftTime);
        tag.putLong("PowerUpUntil2", poweredUntil);
        tag.putInt("LifeTicks", ageTicks);
        if (isTame() && hasRestriction()) {
            BlockPos home = getRestrictCenter();
            tag.putInt("HomeLocX", home.getX());
            tag.putInt("HomeLocY", home.getY());
            tag.putInt("HomeLocZ", home.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        affection = Math.max(0, tag.getInt("Affection"));
        secretsShared = Mth.clamp(tag.getInt("SecretsShared"), 0, SECRETS.length);
        long now = WitcheryTime.epochTicks();
        long worldTime = level().getGameTime();
        lastGiftTime = WitcheryTime.toEpochTick(tag.getLong("LastGiftTime"), worldTime, now);
        ageTicks = Math.max(0, tag.getInt("LifeTicks"));
        long until = tag.contains("PowerUpUntil2", Tag.TAG_ANY_NUMERIC)
                ? WitcheryTime.toEpochTick(tag.getLong("PowerUpUntil2"), worldTime, now)
                : tag.getLong("PowerUpUntil") > 0 ? now + POWER_DURATION : 0;
        setPoweredUntil(until);
        if (isTame() && tag.contains("HomeLocY", Tag.TAG_ANY_NUMERIC)) {
            restrictTo(new BlockPos(tag.getInt("HomeLocX"), tag.getInt("HomeLocY"), tag.getInt("HomeLocZ")), 16);
        }
    }
}
