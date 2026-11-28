package art.arcane.witchery.entity;

import art.arcane.witchery.familiar.Familiar;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.world.migration.LegacyGameProfiles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.UUID;

public final class WitchCat extends Cat implements Familiar {
    private static final Ingredient FISH = Ingredient.of(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH);
    private static final EntityDataAccessor<Boolean> FAMILIAR = SynchedEntityData.defineId(WitchCat.class, EntityDataSerializers.BOOLEAN);
    private static final UUID FAMILIAR_ARMOR_ID = UUID.fromString("70dd98ef-cd94-416c-9937-c2ed4f023beb");

    public WitchCat(EntityType<? extends Cat> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(1, new GroundFamiliarGoals.Sit(this));
        goalSelector.addGoal(3, new TemptGoal(this, 0.6, FISH, true));
        goalSelector.addGoal(5, new GroundFamiliarGoals.Follow(this, 5));
        goalSelector.addGoal(6, new CatSitOnBlockGoal(this, 0.8));
        goalSelector.addGoal(7, new LeapAtTargetGoal(this, 0.3F));
        goalSelector.addGoal(8, new OcelotAttackGoal(this));
        goalSelector.addGoal(9, new BreedGoal(this, 0.8));
        goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10));
        // The original rolled one in 750 on every third tick. Modern target goals run every other tick.
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Chicken.class, 3 * 750, false, false, null) {
            @Override
            public boolean canUse() {
                return !isTame() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !isTame() && super.canContinueToUse();
            }
        });
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FAMILIAR, false);
    }

    @Override
    public boolean isFamiliar() {
        return entityData.get(FAMILIAR);
    }

    @Override
    public void setFamiliar(boolean familiar) {
        applyFamiliar(familiar);
        setHealth(getMaxHealth());
    }

    public void setBlackCoat() {
        setVariant(BuiltInRegistries.CAT_VARIANT.getOrThrow(CatVariant.BLACK));
    }

    private void applyFamiliar(boolean familiar) {
        entityData.set(FAMILIAR, familiar);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(familiar ? 50 : 10);
        AttributeInstance armor = getAttribute(Attributes.ARMOR);
        armor.removeModifier(FAMILIAR_ARMOR_ID);
        if (familiar) {
            armor.addTransientModifier(new AttributeModifier(FAMILIAR_ARMOR_ID, "Familiar armor", 5, AttributeModifier.Operation.ADDITION));
        }
    }

    @Override
    public boolean fireImmune() {
        return isFamiliar() || super.fireImmune();
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isFood(ItemStack held) {
        return FISH.test(held);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean client = level().isClientSide();
        boolean owned = isOwnedBy(player);
        if (owned && isFamiliar() && isOrderedToSit() && player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer server) FamiliarBinding.dismiss(server, this);
            return InteractionResult.sidedSuccess(client);
        }
        if (held.getItem() instanceof SpawnEggItem) return super.mobInteract(player, hand);
        if (WitchPet.bypassesSitCommand(held)) return InteractionResult.PASS;
        if (!isFood(held)) {
            if (!owned) return InteractionResult.PASS;
            if (!client) setOrderedToSit(!isOrderedToSit());
            return InteractionResult.sidedSuccess(client);
        }
        if (owned && getHealth() < getMaxHealth()) {
            if (!client) {
                heal(10);
                usePlayerItem(player, hand, held);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (!isTame() && distanceToSqr(player) < 9) {
            if (!client) {
                usePlayerItem(player, hand, held);
                boolean tamed = random.nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(this, player);
                if (tamed) {
                    tame(player);
                    setLegacyCoat(1 + level().random.nextInt(3));
                    setOrderedToSit(true);
                    setPersistenceRequired();
                }
                level().broadcastEntityEvent(this, (byte) (tamed ? 7 : 6));
            }
            return InteractionResult.sidedSuccess(client);
        }
        // The original inherited the ordinary feeding action outside its strict taming radius.
        if (!client && getAge() == 0 && canFallInLove()) {
            usePlayerItem(player, hand, held);
            setInLove(player);
            return InteractionResult.SUCCESS;
        }
        if (isBaby()) {
            usePlayerItem(player, hand, held);
            ageUp(getSpeedUpSecondsWhenFeeding(-getAge()), true);
            return InteractionResult.sidedSuccess(client);
        }
        return client ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Familiar", isFamiliar());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(LegacyGameProfiles.withTameOwner(tag));
        if (!tag.contains("variant", Tag.TAG_STRING) && tag.contains("CatType", Tag.TAG_ANY_NUMERIC)) {
            setLegacyCoat(tag.getInt("CatType"));
        }
        if (tag.contains("Familiar")) applyFamiliar(tag.getBoolean("Familiar"));
    }

    private void setLegacyCoat(int type) {
        var coat = switch (type) {
            case 1 -> CatVariant.BLACK;
            case 2 -> CatVariant.RED;
            case 3 -> CatVariant.SIAMESE;
            default -> CatVariant.TABBY;
        };
        setVariant(BuiltInRegistries.CAT_VARIANT.getOrThrow(coat));
    }
}
