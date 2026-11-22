package art.arcane.witchery.entity;

import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class CovenWitch extends PathfinderMob implements RangedAttackMob {
    private static final EntityDataAccessor<Integer> SKIN = SynchedEntityData.defineId(CovenWitch.class, EntityDataSerializers.INT);
    public static final int MAX_MEMBERS = 6;
    private static final int[][] RITE_POSITIONS = {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}, {0, 3}, {0, -3}};
    private static final float[] RITE_FACING = {-45, 45, -135, 135, 180, 0};
    private static final AttributeModifier DRINKING_SPEED = new AttributeModifier(
            UUID.fromString("da2e2747-8768-4f9a-9135-258e93b077a4"), "Drinking speed penalty",
            -0.25, AttributeModifier.Operation.ADDITION);
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
    @Nullable private UUID covenOwner;
    private int summonTicks = -1;
    private int drinkTicks;
    private boolean seatedForRite;

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
        goalSelector.addGoal(1, new RitualSeatGoal());
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 60, 10.0F));
        goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        goalSelector.addGoal(4, new VillageShelterGoal(this, false));
        goalSelector.addGoal(4, new RestrictVillageDoorGoal(this));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.6));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new DefendCovenOwnerGoal());
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
        if (hand != InteractionHand.MAIN_HAND || !isAlive() || covenOwner != null || getTarget() != null
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
        portalEffect(player.serverLevel());
        discard();
    }

    public static int covenSize(ServerPlayer player) {
        return Math.min(MAX_MEMBERS, members(player).size());
    }

    public boolean hasCovenOwner() {
        return isAlive() && covenOwner != null && level().getPlayerByUUID(covenOwner) != null;
    }

    private static ListTag members(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getList(COVEN, Tag.TAG_COMPOUND);
    }

    private static List<CovenWitch> nearbyMembers(ServerPlayer owner) {
        return owner.serverLevel().getEntitiesOfClass(CovenWitch.class, owner.getBoundingBox().inflate(64, 16, 64),
                witch -> witch.isAlive() && owner.getUUID().equals(witch.covenOwner));
    }

    public static void tickVisits(ServerPlayer owner) {
        ServerLevel level = owner.serverLevel();
        if (level.getGameTime() % 100 != 0 || !WitcheryServerConfig.ALLOW_COVEN_WITCH_VISITS.get()
                || !owner.isAlive() || owner.isSpectator()) return;
        BlockPos bed = owner.getRespawnPosition();
        if (bed == null || !level.dimension().equals(owner.getRespawnDimension())
                || owner.distanceToSqr(Vec3.atLowerCornerOf(bed)) >= 256 || covenSize(owner) == 0
                || owner.getRandom().nextInt(20) != 0) return;
        visit(owner);
    }

    private static void visit(ServerPlayer owner) {
        var nearby = nearbyMembers(owner);
        boolean spawning = nearby.isEmpty();
        CovenWitch witch = spawning ? createMember(owner, members(owner).getCompound(owner.getRandom().nextInt(covenSize(owner))))
                : nearby.get(owner.getRandom().nextInt(nearby.size()));
        if (witch == null) return;
        if (spawning) witch.summonTicks = 1_800;
        BlockPos center = owner.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, 0, -2), center.offset(2, 0, 2))) {
            if (Math.abs(pos.getX() - center.getX()) != 2 && Math.abs(pos.getZ() - center.getZ()) != 2) continue;
            BlockPos floor = pos.below();
            if (owner.level().getBlockState(floor).isFaceSturdy(owner.level(), floor, Direction.UP)
                    && placeMember(witch, pos, spawning ? 0 : witch.getYRot(), spawning, false)) return;
        }
    }

    public static void dismissNearbyForRite(ServerPlayer player) {
        for (CovenWitch witch : nearbyMembers(player)) {
            witch.portalEffect(player.serverLevel());
            witch.discard();
        }
    }

    public static boolean recallMember(ServerPlayer player, int index) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        BlockPos cauldron = BlockPos.findClosestMatch(center, 9, 9,
                pos -> pos.distSqr(center) <= 81 && level.hasChunkAt(pos)
                        && level.getBlockState(pos).getBlock() instanceof WitchesCauldronBlock).orElse(null);
        BlockPos anchor = cauldron == null ? center : cauldron;
        for (BlockPos candidate : BlockPos.spiralAround(anchor, 3, Direction.EAST, Direction.SOUTH)) {
            if (candidate.distManhattan(anchor) < 2) continue;
            BlockPos floor = candidate.below();
            if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) continue;
            if (moveMember(player, index, candidate, player.getYRot(), 1200, cauldron, false)) return true;
        }
        return false;
    }

    public static boolean summonToRite(ServerPlayer player, BlockPos center, int index) {
        if (index < 0 || index >= RITE_POSITIONS.length) return false;
        int[] offset = RITE_POSITIONS[index];
        return moveMember(player, index, center.offset(offset[0], 0, offset[1]), RITE_FACING[index], 300, null, true);
    }

    private static boolean moveMember(ServerPlayer player, int index, BlockPos pos, float facing, int duration,
                                      @Nullable BlockPos cauldron, boolean stationary) {
        ListTag coven = members(player);
        if (index < 0 || index >= coven.size()) return false;
        CompoundTag entry = coven.getCompound(index);
        String name = entry.getString("WitchName");
        if (name.isEmpty()) return false;
        CovenWitch witch = nearbyMembers(player).stream().filter(candidate -> candidate.getName().getString().equalsIgnoreCase(name))
                .findFirst().orElse(null);
        boolean spawning = witch == null;
        if (spawning) witch = createMember(player, entry);
        if (witch == null || !placeMember(witch, pos, facing, spawning, stationary)) return false;
        witch.setTarget(null);
        witch.seatedForRite = stationary;
        if (cauldron == null) witch.clearRestriction();
        else witch.restrictTo(cauldron, 3);
        witch.summonTicks = duration;
        return true;
    }

    @Nullable
    private static CovenWitch createMember(ServerPlayer player, CompoundTag entry) {
        String name = entry.getString("WitchName");
        if (name.isEmpty()) return null;
        CovenWitch witch = WitcheryEntities.COVEN_WITCH.get().create(player.serverLevel());
        if (witch == null) return null;
        witch.covenOwner = player.getUUID();
        witch.setCustomName(Component.literal(name));
        witch.entityData.set(SKIN, Mth.clamp(entry.getInt("Skin"), 0, 4));
        int quest = Mth.clamp(entry.getInt("Quest"), 0, Quest.values().length - 1);
        witch.quest = Quest.values()[quest];
        witch.setPersistenceRequired();
        return witch;
    }

    private static boolean placeMember(CovenWitch witch, BlockPos pos, float facing, boolean spawning, boolean stationary) {
        ServerLevel level = (ServerLevel) witch.level();
        Vec3 destination = Vec3.atBottomCenterOf(pos);
        AABB bounds = witch.getBoundingBox().move(destination.subtract(witch.position()));
        if (level.getBlockCollisions(witch, bounds).iterator().hasNext()) return false;
        if (!stationary && !level.getEntitiesOfClass(CovenWitch.class, bounds,
                candidate -> candidate.isAlive() && candidate != witch).isEmpty()) return false;
        witch.moveTo(destination.x, destination.y, destination.z, facing, 0);
        witch.setYBodyRot(facing);
        witch.setYHeadRot(facing);
        witch.getNavigation().stop();
        if (spawning && !level.addFreshEntity(witch)) return false;
        witch.portalEffect(level);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel level && covenOwner != null) {
            if (summonTicks > 0) summonTicks--;
            if (summonTicks == 0 && getTarget() == null) {
                portalEffect(level);
                discard();
            }
        }
    }

    private void portalEffect(ServerLevel level) {
        level.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 16, 0.35, 0.75, 0.35, 0.25);
        playSound(SoundEvents.ENDERMAN_TELEPORT);
    }

    @Override
    public void die(DamageSource damage) {
        boolean previouslyDead = dead;
        super.die(damage);
        if (previouslyDead || !dead || !(level() instanceof ServerLevel level) || covenOwner == null
                || !(level.getPlayerByUUID(covenOwner) instanceof ServerPlayer owner)) return;
        ListTag coven = members(owner);
        for (int i = 0; i < coven.size(); i++) {
            if (getName().getString().equalsIgnoreCase(coven.getCompound(i).getString("WitchName"))) {
                coven.remove(i);
                return;
            }
        }
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
        if (drinkTicks > 0 || level().isClientSide) return;
        Vec3 position = target.position().add(target.getDeltaMovement());
        double x = position.x - getX();
        double y = target.getEyeY() - 1.1 - getY();
        double z = position.z - getZ();
        double distance = Math.sqrt(x * x + z * z);
        MobEffectInstance effect = new MobEffectInstance(MobEffects.HARM, 1);
        if (distance >= 8 && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            effect = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1_351);
        } else if (target.getHealth() >= 8 && !target.hasEffect(MobEffects.POISON)) {
            effect = new MobEffectInstance(MobEffects.POISON, 676);
        } else if (distance <= 3 && !target.hasEffect(MobEffects.WEAKNESS) && random.nextFloat() < 0.25F) {
            effect = new MobEffectInstance(MobEffects.WEAKNESS, 1_351);
        }

        ThrownPotion potion = new ThrownPotion(level(), this);
        potion.setItem(PotionUtils.setCustomEffects(new ItemStack(Items.SPLASH_POTION), List.of(effect)));
        potion.setXRot(potion.getXRot() + 20);
        potion.shoot(x, y + distance * 0.2, z, 0.75F, 8.0F);
        playSound(SoundEvents.WITCH_THROW);
        level().addFreshEntity(potion);
    }

    @Override
    public void aiStep() {
        if (level() instanceof ServerLevel level && isAlive() && !isNoAi()) {
            if (drinkTicks > 0) {
                if (--drinkTicks == 0) finishPotion();
            } else {
                Potion potion = defensivePotion();
                if (potion != null) {
                    ItemStack stack = PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
                    setItemSlot(EquipmentSlot.MAINHAND, stack);
                    setDrinkTicks(stack.getUseDuration());
                    playSound(SoundEvents.WITCH_DRINK, 1, 0.8F + random.nextFloat() * 0.4F);
                }
            }
            if (random.nextFloat() < 0.00075F) level.broadcastEntityEvent(this, (byte) 15);
        }
        super.aiStep();
    }

    @Nullable
    private Potion defensivePotion() {
        if (random.nextFloat() < 0.15F && isOnFire() && !hasEffect(MobEffects.FIRE_RESISTANCE)) return Potions.FIRE_RESISTANCE;
        if (random.nextFloat() < 0.05F && getHealth() < getMaxHealth()) return Potions.HEALING;
        LivingEntity target = getTarget();
        if (target != null && !hasEffect(MobEffects.MOVEMENT_SPEED) && target.distanceToSqr(this) > 121
                && (random.nextFloat() < 0.25F || random.nextFloat() < 0.25F)) return Potions.SWIFTNESS;
        return null;
    }

    private void finishPotion() {
        ItemStack consumed = getMainHandItem();
        setDrinkTicks(0);
        if (!consumed.is(Items.POTION)) return;
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        for (MobEffectInstance effect : PotionUtils.getMobEffects(consumed)) {
            addEffect(new MobEffectInstance(effect));
        }
    }

    private void setDrinkTicks(int ticks) {
        drinkTicks = ticks;
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        speed.removeModifier(DRINKING_SPEED);
        if (ticks > 0) speed.addTransientModifier(DRINKING_SPEED);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event != 15) {
            super.handleEntityEvent(event);
            return;
        }
        int count = random.nextInt(35) + 10;
        for (int i = 0; i < count; i++) {
            level().addParticle(ParticleTypes.WITCH, getX() + random.nextGaussian() * 0.13,
                    getBoundingBox().maxY + 0.5 + random.nextGaussian() * 0.13, getZ() + random.nextGaussian() * 0.13, 0, 0, 0);
        }
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
        if (covenOwner != null) {
            tag.putUUID("Owner", covenOwner);
            tag.putInt("SuicideIn", summonTicks);
            tag.putBoolean("Sitting", seatedForRite);
        }
        if (drinkTicks > 0) tag.putInt("DrinkTicks", drinkTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SKIN, Mth.clamp(tag.getInt("SkinType"), 0, 4));
        offeredTo = tag.getString("QuestOffered");
        offeredToId = tag.hasUUID("QuestPlayer") ? tag.getUUID("QuestPlayer") : null;
        covenOwner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        summonTicks = tag.contains("SuicideIn", Tag.TAG_ANY_NUMERIC) ? tag.getInt("SuicideIn") : -1;
        boolean oldRiteSeat = covenOwner != null && !tag.contains("Sitting", Tag.TAG_ANY_NUMERIC) && summonTicks >= 0 && isNoAi();
        seatedForRite = tag.getBoolean("Sitting") || oldRiteSeat;
        if (oldRiteSeat) setNoAi(false);
        ItemStack hand = getMainHandItem();
        setDrinkTicks(hand.is(Items.POTION) ? Mth.clamp(tag.getInt("DrinkTicks"), 0, hand.getUseDuration()) : 0);
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

    private final class RitualSeatGoal extends Goal {
        private RitualSeatGoal() {
            setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!seatedForRite || !onGround() || isInWaterOrBubble()) return false;
            Player owner = covenOwner == null ? null : level().getPlayerByUUID(covenOwner);
            return owner == null || distanceToSqr(owner) >= 144 || owner.getLastHurtByMob() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return seatedForRite;
        }

        @Override
        public void start() {
            getNavigation().stop();
        }
    }

    private final class DefendCovenOwnerGoal extends TargetGoal {
        private int lastAttack;

        private DefendCovenOwnerGoal() {
            super(CovenWitch.this, false);
        }

        @Override
        public boolean canUse() {
            if (covenOwner == null || !(level() instanceof ServerLevel level)
                    || !(level.getPlayerByUUID(covenOwner) instanceof ServerPlayer owner)
                    || owner.getLastHurtMobTimestamp() == lastAttack) return false;
            LivingEntity attacked = owner.getLastHurtMob();
            if (attacked == CovenWitch.this || attacked instanceof CovenWitch other
                    && covenOwner.equals(other.covenOwner) || !canAttack(attacked, TargetingConditions.DEFAULT)) return false;
            lastAttack = owner.getLastHurtMobTimestamp();
            targetMob = attacked;
            return true;
        }

        @Override
        public void start() {
            setTarget(targetMob);
            super.start();
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
