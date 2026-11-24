package art.arcane.witchery.crafting;

import art.arcane.witchery.entity.CreatureAlignment;
import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
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

    private static final int LEGACY_NO_SOUND_ID = 0;
    private static final int LEGACY_MOB_SPELL_PARTICLE_ID = 9;
    private static final int LEGACY_INSTANT_SPELL_PARTICLE_ID = 11;
    private static final int LEGACY_EXPLODE_PARTICLE_ID = 15;
    private static final int LEGACY_HEART_PARTICLE_ID = 28;
    private final int burnTicks;
    private final List<Supplier<Item>> ingredients;

    BrazierRecipe(int burnTicks, Supplier<Item> first, Supplier<Item> second, Supplier<Item> third) {
        this.burnTicks = burnTicks;
        this.ingredients = List.of(first, second, third);
    }

    public int burnTicks() {
        return burnTicks;
    }

    public List<Item> ingredients() {
        return ingredients.stream().map(Supplier::get).toList();
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

    public int burn(ServerLevel level, BlockPos pos, long ticks) {
        Vec3 center = Vec3.atBottomCenterOf(pos);
        switch (this) {
            case SPECTRE, BANSHEE, POLTERGEIST -> {
                if (ticks % 100 == 0) {
                    WitcheryNetwork.sendParticlesAround(level, LEGACY_INSTANT_SPELL_PARTICLE_ID, LEGACY_NO_SOUND_ID,
                            center.add(0, 1, 0), 0.5, 1, 16);
                }
            }
            case MIST -> WitcheryNetwork.sendParticlesAround(level, LEGACY_EXPLODE_PARTICLE_ID, LEGACY_NO_SOUND_ID,
                    center, 16, 4, 64);
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

        WitcheryNetwork.sendParticlesAround(level, LEGACY_MOB_SPELL_PARTICLE_ID, LEGACY_NO_SOUND_ID,
                Vec3.atBottomCenterOf(pos.above()), 0.3, 0.5, 8);
        AABB area = new AABB(Vec3.atLowerCornerOf(pos), Vec3.atLowerCornerOf(pos)).inflate(3);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area,
                candidate -> candidate.isAlive() && candidate.getHealth() < candidate.getMaxHealth()
                        && CreatureAlignment.undead(candidate))) {
            entity.heal(entity.getMaxHealth() * 0.1F);
            WitcheryNetwork.sendParticlesAround(level, LEGACY_HEART_PARTICLE_ID, LEGACY_NO_SOUND_ID,
                    entity.position(), 0.5, 1, 8);
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
