package art.arcane.witchery.crafting;

import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public enum BrazierRecipe {
    SPECTRE(600, WitcheryIngredient.WORMWOOD::item, WitcheryIngredient.BAT_WOOL::item, WitcheryIngredient.GRAVEYARD_DUST::item),
    BANSHEE(600, WitcheryIngredient.WORMWOOD::item, WitcheryIngredient.CONDENSED_FEAR::item, WitcheryIngredient.GRAVEYARD_DUST::item),
    POLTERGEIST(900, WitcheryIngredient.WORMWOOD::item, WitcheryIngredient.REFINED_EVIL::item, WitcheryIngredient.FOCUSED_WILL::item),
    MIST(6000, () -> Items.GUNPOWDER, WitcheryIngredient.QUICKLIME::item, () -> Items.GLOWSTONE_DUST),
    STRENGTH(6000, WitcheryIngredient.TEAR_OF_THE_GODDESS::item, () -> Items.BONE, () -> Items.BLAZE_POWDER),
    RESISTANCE(6000, WitcheryIngredient.TEAR_OF_THE_GODDESS::item, () -> Items.ROTTEN_FLESH, () -> Items.BLAZE_POWDER),
    INVISIBILITY(12000, () -> Items.ENDER_PEARL, () -> Items.SPIDER_EYE, () -> Items.BLAZE_ROD),
    WILTING(1200, WitcheryIngredient.CONDENSED_FEAR::item, WitcheryIngredient.WORMY_APPLE::item, WitcheryIngredient.GRAVEYARD_DUST::item);

    private final int burnTicks;
    private final List<Supplier<Item>> ingredients;

    BrazierRecipe(int burnTicks, Supplier<Item> first, Supplier<Item> second, Supplier<Item> third) {
        this.burnTicks = burnTicks;
        this.ingredients = List.of(first, second, third);
    }

    public int burnTicks() {
        return burnTicks;
    }

    public boolean needsPower() {
        return this != WILTING;
    }

    public static Optional<BrazierRecipe> match(IItemHandler offerings) {
        for (BrazierRecipe recipe : values()) {
            if (recipe.matches(offerings)) return Optional.of(recipe);
        }
        return Optional.empty();
    }

    private boolean matches(IItemHandler offerings) {
        int matched = 0;
        for (Supplier<Item> ingredient : ingredients) {
            int slot;
            for (slot = 0; slot < offerings.getSlots(); slot++) {
                if ((matched & (1 << slot)) == 0 && offerings.getStackInSlot(slot).is(ingredient.get())) break;
            }
            if (slot == offerings.getSlots()) return false;
            matched |= 1 << slot;
        }
        return true;
    }

    public int burn(ServerLevel level, BlockPos pos) {
        long ticks = level.getGameTime();
        Vec3 center = Vec3.atBottomCenterOf(pos);
        switch (this) {
            case SPECTRE, BANSHEE, POLTERGEIST -> {
                if (ticks % 100 == 0) {
                    level.sendParticles(ParticleTypes.INSTANT_EFFECT, center.x, center.y + 1, center.z, 16, 0.5, 0.6, 0.5, 0.02);
                }
            }
            case MIST -> {
                level.sendParticles(ParticleTypes.POOF, center.x, center.y, center.z, 64, 16, 4, 16, 0.01);
            }
            case STRENGTH, RESISTANCE, INVISIBILITY -> {
                if (ticks % 60 == 0) {
                    MobEffect effect = switch (this) {
                        case STRENGTH -> MobEffects.DAMAGE_BOOST;
                        case RESISTANCE -> MobEffects.DAMAGE_RESISTANCE;
                        default -> MobEffects.INVISIBILITY;
                    };
                    int radius = this == INVISIBILITY ? 6 : 4;
                    AABB area = new AABB(center, center).inflate(radius);
                    for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area,
                            candidate -> candidate.isAlive() && candidate.distanceToSqr(center) <= radius * radius)) {
                        entity.addEffect(new MobEffectInstance(effect, 120));
                    }
                }
            }
            case WILTING -> {
                return ticks % 5 == 0 && wilt(level, pos, (int) (ticks % 30) / 5 - 2) ? 2 : 0;
            }
        }
        return 0;
    }

    private static boolean wilt(ServerLevel level, BlockPos pos, int offsetY) {
        BlockPos cropPos = pos.offset(level.random.nextInt(7) - 3, offsetY, level.random.nextInt(7) - 3);
        if (!level.isLoaded(cropPos)) return false;
        var crop = level.getBlockState(cropPos);
        if (!(crop.getBlock() instanceof CropBlock) && !crop.is(BlockTags.CROPS)) return false;
        if (!(crop.getBlock().getStateDefinition().getProperty("age") instanceof IntegerProperty age)
                || crop.getValue(age) <= 0 || !age.getPossibleValues().contains(crop.getValue(age) - 1)) return false;
        if (!level.setBlock(cropPos, crop.setValue(age, crop.getValue(age) - 1), Block.UPDATE_CLIENTS)) return false;

        Vec3 center = Vec3.atBottomCenterOf(pos);
        level.sendParticles(ParticleTypes.ENTITY_EFFECT, center.x, center.y + 1, center.z, 8, 0.3, 0.5, 0.3, 0.05);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(3),
                candidate -> candidate.isAlive() && candidate.getHealth() < candidate.getMaxHealth()
                        && (candidate.isInvertedHealAndHarm() || Vampirism.isVampire(candidate)))) {
            entity.heal(entity.getMaxHealth() * 0.1F);
            level.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getEyeY(), entity.getZ(), 8, 0.25, 0.5, 0.25, 0);
        }
        return true;
    }

    public void finish(ServerLevel level, BlockPos pos) {
        SpectralUndead.Kind kind = switch (this) {
            case SPECTRE -> SpectralUndead.Kind.SPECTRE;
            case BANSHEE -> SpectralUndead.Kind.BANSHEE;
            case POLTERGEIST -> SpectralUndead.Kind.POLTERGEIST;
            default -> null;
        };
        if (kind == null) return;
        if (SpectralUndead.summonNear(level, pos, kind, 1, 2) != null) {
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 1, 1);
        }
        if (level.random.nextFloat() < 0.05F) {
            SpectralUndead.summonNear(level, pos, SpectralUndead.Kind.POLTERGEIST, 6, 10);
        }
    }
}
