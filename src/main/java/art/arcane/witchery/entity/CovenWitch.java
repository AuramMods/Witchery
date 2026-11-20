package art.arcane.witchery.entity;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public final class CovenWitch extends PathfinderMob implements RangedAttackMob {
    private static final EntityDataAccessor<Integer> SKIN = SynchedEntityData.defineId(CovenWitch.class, EntityDataSerializers.INT);
    private static final int MAX_MEMBERS = 6;
    private static final String COVEN = "WITCCoven";
    private static final String QUEST_HOLDER_LEAST = "WITCQuestOwnerIDLeast";
    private static final String QUEST_HOLDER_MOST = "WITCQuestOwnerIDMost";
    public static final String EXTRA_DROPS = "WITCExtraDrops";
    private static final Item[] DROPS = {Items.GLOWSTONE_DUST, Items.SUGAR, Items.REDSTONE, Items.SPIDER_EYE,
            Items.GLASS_BOTTLE, Items.GUNPOWDER, Items.STICK, Items.STICK};

    private String offeredTo = "";
    @Nullable private UUID offeredToId;
    private boolean accepted;
    private Quest quest = Quest.SPIDER;
    private int remaining;

    public CovenWitch(EntityType<? extends CovenWitch> type, Level level) {
        super(type, level);
        GroundPathNavigation navigation = (GroundPathNavigation) getNavigation();
        navigation.setCanOpenDoors(true);
        navigation.setCanFloat(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    public static boolean canSpawn(EntityType<CovenWitch> type, ServerLevelAccessor level, MobSpawnType reason,
                                   BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)
                && level.getRawBrightness(pos, 0) > 8
                && random.nextInt(WitcheryServerConfig.COVEN_WITCH_SPAWN_WEIGHT.get()) == 0;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 60, 10.0F));
        goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKIN, 0);
    }

    public int skin() {
        return entityData.get(SKIN);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData group, @Nullable CompoundTag tag) {
        entityData.set(SKIN, random.nextInt(5));
        return super.finalizeSpawn(level, difficulty, reason, group, tag);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !isAlive() || getTarget() != null
                || level().dimension().equals(WitcheryDimensions.DREAM)) return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer) negotiate(serverPlayer, player.getItemInHand(hand));
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private void negotiate(ServerPlayer player, ItemStack offeredItem) {
        if (!hasCustomName()) {
            setCustomName(Component.literal(CovenWitchNames.next(random)));
            setPersistenceRequired();
        }

        boolean addressed = offeredToId == null ? offeredTo.equalsIgnoreCase(player.getGameProfile().getName())
                : offeredToId.equals(player.getUUID());
        ListTag coven = members(player);
        if (accepted && !addressed) {
            say(player, "away");
        } else if (coven.size() >= MAX_MEMBERS) {
            if (addressed) clearOffer();
            say(player, "full");
        } else if (!addressed) {
            if (!FamiliarBinding.hasActiveFamiliar(player)) {
                say(player, "refuse." + random.nextInt(3));
                return;
            }
            offeredTo = player.getGameProfile().getName();
            offeredToId = player.getUUID();
            quest = Quest.values()[random.nextInt(Quest.values().length)];
            remaining = quest.count;
            say(player, "offer." + quest.key);
        } else if (!accepted) {
            if (quest.combat() && !spawnQuestPet(player)) {
                say(player, "spawn_failed");
                return;
            }
            offeredToId = player.getUUID();
            accepted = true;
            if (!quest.combat()) say(player, "accepted");
        } else if (!matchesGift(offeredItem)) {
            say(player, "unfinished");
        } else {
            offeredItem.shrink(1);
            if (--remaining > 0) {
                say(player, "remaining", remaining);
            } else {
                recruit(player, coven);
            }
        }
    }

    private void clearOffer() {
        offeredTo = "";
        offeredToId = null;
        accepted = false;
        remaining = 0;
    }

    private boolean matchesGift(ItemStack stack) {
        if (!stack.is(quest.item.get())) return false;
        if (!quest.combat()) return true;
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(QUEST_HOLDER_MOST, Tag.TAG_ANY_NUMERIC)
                && tag.contains(QUEST_HOLDER_LEAST, Tag.TAG_ANY_NUMERIC)
                && getUUID().getMostSignificantBits() == tag.getLong(QUEST_HOLDER_MOST)
                && getUUID().getLeastSignificantBits() == tag.getLong(QUEST_HOLDER_LEAST);
    }

    private boolean spawnQuestPet(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Mob pet = quest == Quest.SPIDER ? EntityType.SPIDER.create(level) : EntityType.ZOMBIE.create(level);
        if (pet == null) return false;
        pet.moveTo(getX(), getY(), getZ(), getYRot(), 0.0F);
        pet.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        pet.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
        pet.setHealth(100);
        pet.setPersistenceRequired();
        if (quest == Quest.ZOMBIE) pet.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.SKELETON_SKULL));
        pet.setCustomName(Component.translatable("message.witchery.coven.pet", getName()));

        ItemStack trophy = new ItemStack(quest.item.get());
        trophy.setHoverName(Component.translatable("message.witchery.coven.trophy." + quest.key, getName()));
        CompoundTag tag = trophy.getOrCreateTag();
        tag.putLong(QUEST_HOLDER_LEAST, getUUID().getLeastSignificantBits());
        tag.putLong(QUEST_HOLDER_MOST, getUUID().getMostSignificantBits());
        ListTag drops = new ListTag();
        drops.add(trophy.save(new CompoundTag()));
        pet.getPersistentData().put(EXTRA_DROPS, drops);
        if (!level.addFreshEntity(pet)) return false;
        pet.setTarget(player);
        level.sendParticles(ParticleTypes.WITCH, pet.getX(), pet.getY() + 1, pet.getZ(), 16, 0.6, 0.75, 0.6, 0);
        return true;
    }

    private void recruit(ServerPlayer player, ListTag coven) {
        String name = getName().getString();
        for (int index = 0; index < coven.size(); index++) {
            if (name.equalsIgnoreCase(coven.getCompound(index).getString("WitchName"))) {
                say(player, "duplicate");
                clearOffer();
                setTarget(player);
                return;
            }
        }
        CompoundTag entry = new CompoundTag();
        entry.putString("WitchName", name);
        entry.putInt("Skin", skin());
        entry.putInt("Quest", quest.ordinal());
        coven.add(entry);
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.put(COVEN, coven);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);

        say(player, "joined");
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 16, 0.35, 0.75, 0.35, 0.25);
        playSound(SoundEvents.ENDERMAN_TELEPORT);
        discard();
    }

    public static int covenSize(ServerPlayer player) {
        return members(player).size();
    }

    private static ListTag members(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getList(COVEN, Tag.TAG_COMPOUND);
    }

    private void say(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(Component.literal("<").append(getName()).append("> ")
                .append(Component.translatable("message.witchery.coven." + key, args)).withStyle(ChatFormatting.BLUE));
        playSound(random.nextBoolean() ? WitcherySounds.COVEN_TALK.get() : WitcherySounds.COVEN_CACKLE.get());
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITCH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITCH_DEATH;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        int rolls = 1 + random.nextInt(3);
        for (int roll = 0; roll < rolls; roll++) {
            int count = random.nextInt(3) + (looting > 0 ? random.nextInt(looting + 1) : 0);
            if (count > 0) spawnAtLocation(new ItemStack(DROPS[random.nextInt(DROPS.length)], count));
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        Vec3 position = target.position().add(target.getDeltaMovement());
        double x = position.x - getX();
        double y = target.getEyeY() - 1.1 - getY();
        double z = position.z - getZ();
        double distance = Math.sqrt(x * x + z * z);
        Potion effect = Potions.HARMING;
        if (distance >= 8 && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            effect = Potions.SLOWNESS;
        } else if (target.getHealth() >= 8 && !target.hasEffect(MobEffects.POISON)) {
            effect = Potions.POISON;
        } else if (distance <= 3 && !target.hasEffect(MobEffects.WEAKNESS) && random.nextFloat() < 0.25F) {
            effect = Potions.WEAKNESS;
        }

        ThrownPotion potion = new ThrownPotion(level(), this);
        potion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), effect));
        potion.setXRot(potion.getXRot() + 20);
        potion.shoot(x, y + distance * 0.2, z, 0.75F, 8.0F);
        playSound(SoundEvents.WITCH_THROW);
        level().addFreshEntity(potion);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinType", skin());
        tag.putString("QuestOffered", offeredTo);
        if (offeredToId != null) tag.putUUID("QuestPlayer", offeredToId);
        tag.putBoolean("QuestAccepted", accepted);
        tag.putInt("QuestType", quest.ordinal());
        tag.putInt("QuestItemsNeeded", remaining);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SKIN, Mth.clamp(tag.getInt("SkinType"), 0, 4));
        offeredTo = tag.getString("QuestOffered");
        offeredToId = tag.hasUUID("QuestPlayer") ? tag.getUUID("QuestPlayer") : null;
        int questType = tag.getInt("QuestType");
        if (questType < 0 || questType >= Quest.values().length) {
            clearOffer();
        } else {
            quest = Quest.values()[questType];
            remaining = tag.getInt("QuestItemsNeeded");
            accepted = tag.getBoolean("QuestAccepted");
            if (remaining <= 0 || remaining > quest.count) clearOffer();
        }
    }

    private enum Quest {
        SPIDER("spider", 1, () -> Items.SPIDER_EYE),
        ZOMBIE("zombie", 1, () -> Items.ROTTEN_FLESH),
        HEART("heart", 1, WitcheryIngredient.DEMON_HEART::item),
        CRYSTAL_BALL("crystal_ball", 1, () -> WitcheryBlocks.getRequired("crystalball").get().asItem()),
        BONES("bones", 30, () -> Items.BONE),
        GROTESQUE("grotesque", 5, WitcheryIngredient.BREW_OF_GROTESQUE::item),
        NECROMANTIC_STONE("necromantic_stone", 1, WitcheryIngredient.NECROMANTIC_STONE::item);

        private final String key;
        private final int count;
        private final Supplier<Item> item;

        Quest(String key, int count, Supplier<Item> item) {
            this.key = key;
            this.count = count;
            this.item = item;
        }

        private boolean combat() {
            return this == SPIDER || this == ZOMBIE;
        }
    }
}
