package art.arcane.witchery.entity;

import art.arcane.witchery.familiar.FamiliarBinding;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class WitchCat extends Cat {
    private static final EntityDataAccessor<Boolean> FAMILIAR = SynchedEntityData.defineId(WitchCat.class, EntityDataSerializers.BOOLEAN);
    private static final UUID FAMILIAR_ARMOR_ID = UUID.fromString("70dd98ef-cd94-416c-9937-c2ed4f023beb");

    public WitchCat(EntityType<? extends Cat> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FAMILIAR, false);
    }

    public boolean isFamiliar() {
        return entityData.get(FAMILIAR);
    }

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
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isFamiliar() && isOwnedBy(player)) {
            if (isOrderedToSit() && player.isShiftKeyDown()) {
                if (player instanceof ServerPlayer server) FamiliarBinding.dismiss(server, this);
                return InteractionResult.sidedSuccess(level().isClientSide());
            }
            ItemStack held = player.getItemInHand(hand);
            if (getHealth() < getMaxHealth() && isFood(held)) {
                if (!level().isClientSide()) {
                    heal(10);
                    if (!player.getAbilities().instabuild) held.shrink(1);
                }
                return InteractionResult.sidedSuccess(level().isClientSide());
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Familiar", isFamiliar());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Familiar")) applyFamiliar(tag.getBoolean("Familiar"));
    }
}
