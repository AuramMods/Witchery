package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.world.migration.LegacyGameProfiles;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public final class SpectralFamiliar extends TamableAnimal {
    private static final EntityDataAccessor<Integer> ORE = SynchedEntityData.defineId(SpectralFamiliar.class, EntityDataSerializers.INT);
    private static final double[] DEPARTURE_CHANCES = {0, 0.60, 0.75, 0.85, 0.95};
    private int searches;

    public SpectralFamiliar(EntityType<? extends SpectralFamiliar> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 0.5);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(3, new FollowOwnerGoal(this, 1, 10, 5, false));
        goalSelector.addGoal(4, new FindOreGoal());
        goalSelector.addGoal(5, new LeapAtTargetGoal(this, 0.3F));
        goalSelector.addGoal(6, new MeleeAttackGoal(this, 1, false));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ORE, -1);
    }

    public boolean isSearching() {
        return entityData.get(ORE) >= 0;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level() instanceof ServerLevel level && isTame() && isOwnedBy(player)) {
            ItemStack offering = player.getItemInHand(hand);
            Ore ore = Ore.forOffering(offering);
            if (ore == null) {
                setOrderedToSit(!isOrderedToSit());
            } else {
                offering.shrink(1);
                entityData.set(ORE, ore.ordinal());
                searches++;
                if (searches > DEPARTURE_CHANCES.length || searches > 1 && random.nextDouble() < DEPARTURE_CHANCES[searches - 1]) {
                    level.sendParticles(ParticleTypes.PORTAL, getX(), getY(0.5), getZ(), 16, 0.4, 0.5, 0.4, 0.1);
                    playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
                    discard();
                } else {
                    player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.PIG_AMBIENT; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.PIG_AMBIENT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.PIG_DEATH; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) { playSound(SoundEvents.PIG_STEP, 0.15F, 1); }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        spawnAtLocation(WitcheryIngredient.SPECTRAL_DUST.item());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ItemToFind", entityData.get(ORE));
        tag.putInt("Searches", searches);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(LegacyGameProfiles.withTameOwner(tag));
        int id = tag.contains("ItemToFind", Tag.TAG_ANY_NUMERIC) ? tag.getInt("ItemToFind") : -1;
        entityData.set(ORE, id >= 0 && id < Ore.VALUES.length ? id : -1);
        searches = Math.max(0, Math.min(tag.getInt("Searches"), DEPARTURE_CHANCES.length));
        if (isTame()) setPersistenceRequired();
    }

    private enum Ore {
        DIAMOND(Items.DIAMOND, BlockTags.DIAMOND_ORES, 14),
        EMERALD(Items.EMERALD, BlockTags.EMERALD_ORES, 14),
        GOLD(Items.GOLD_INGOT, BlockTags.GOLD_ORES, 30),
        IRON(Items.IRON_INGOT, BlockTags.IRON_ORES, 64),
        REDSTONE(Items.REDSTONE, BlockTags.REDSTONE_ORES, 16),
        LAPIS(Items.LAPIS_LAZULI, BlockTags.LAPIS_ORES, 30),
        COAL(Items.COAL, BlockTags.COAL_ORES, 64);

        static final Ore[] VALUES = values();
        final Item offering;
        final TagKey<Block> blocks;
        final int maxY;

        Ore(Item offering, TagKey<Block> blocks, int maxY) {
            this.offering = offering;
            this.blocks = blocks;
            this.maxY = maxY;
        }

        @Nullable
        static Ore forOffering(ItemStack stack) {
            if (stack.getItem() instanceof DyeItem || stack.is(Items.INK_SAC) || stack.is(Items.BONE_MEAL)
                    || stack.is(Items.COCOA_BEANS)) return LAPIS;
            for (Ore ore : VALUES) if (stack.is(ore.offering)) return ore;
            return null;
        }

        boolean matches(BlockState state) {
            if (this == DIAMOND || this == EMERALD) return state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES);
            return state.is(blocks);
        }
    }

    private final class FindOreGoal extends Goal {
        private BlockPos target = BlockPos.ZERO;
        private int selectedOre;
        private int walkTicks;

        FindOreGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            selectedOre = entityData.get(ORE);
            if (!isTame() || isOrderedToSit() || selectedOre < 0 || !ownerNearby() || random.nextInt(10) != 0) return false;
            var ore = Ore.VALUES[selectedOre];
            var origin = blockPosition();
            int bottom = level().getMinBuildHeight() + 1;
            int top = Math.min(ore.maxY, level().getMaxBuildHeight());
            var cursor = new BlockPos.MutableBlockPos();
            for (int y = bottom; y < top; y++) {
                for (int x = -4; x < 4; x++) {
                    for (int z = -4; z < 4; z++) {
                        cursor.set(origin.getX() + x, y, origin.getZ() + z);
                        if (level().hasChunkAt(cursor) && ore.matches(level().getBlockState(cursor))) {
                            target = cursor.immutable();
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return walkTicks < 60 && selectedOre == entityData.get(ORE) && !isOrderedToSit() && ownerNearby()
                    && Ore.VALUES[selectedOre].matches(level().getBlockState(target));
        }

        @Override
        public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void start() {
            walkTicks = 0;
            walkToTarget();
        }

        @Override
        public void stop() { getNavigation().stop(); }

        @Override
        public void tick() {
            if (selectedOre != entityData.get(ORE) || isOrderedToSit()) return;
            if (distanceToSqr(target.getX() + 0.5, getY(), target.getZ() + 0.5) <= 2) {
                if (getOwner() instanceof ServerPlayer owner) {
                    BlockPos point = blockPosition();
                    owner.sendSystemMessage(Component.translatable("message.witchery.spectral_familiar.found", point.getX(),
                            point.getY(), point.getZ()).withStyle(ChatFormatting.LIGHT_PURPLE));
                    owner.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 1, 1);
                }
                entityData.set(ORE, -1);
                setOrderedToSit(true);
            } else if (++walkTicks % 10 == 0) {
                walkToTarget();
            }
        }

        private boolean ownerNearby() {
            LivingEntity owner = getOwner();
            return owner != null && owner.isAlive() && distanceToSqr(owner) < 100;
        }

        private void walkToTarget() {
            double x = target.getX() + 0.5;
            double z = target.getZ() + 0.5;
            if (!getNavigation().moveTo(x, target.getY() + 1, z, 1.33)) getNavigation().moveTo(x, getY(), z, 1.33);
        }
    }
}
