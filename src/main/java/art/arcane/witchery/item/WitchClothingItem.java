package art.arcane.witchery.item;

import art.arcane.witchery.client.model.WitchClothingModel;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.world.WitcheryDimensions;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class WitchClothingItem extends ArmorItem {
    public enum Style {
        WITCH_HAT(Type.HELMET, 3), BABA_HAT(Type.HELMET, 3),
        WITCH_ROBE(Type.CHESTPLATE, 4), NECROMANCER_ROBE(Type.CHESTPLATE, 4);

        private final Type type;
        private final int repairLeather;

        Style(Type type, int repairLeather) {
            this.type = type;
            this.repairLeather = repairLeather;
        }
    }

    private final Style style;

    public WitchClothingItem(Style style) {
        super(ArmorMaterials.LEATHER, style.type,
                new Properties().rarity(style == Style.BABA_HAT ? Rarity.EPIC : Rarity.UNCOMMON));
        this.style = style;
    }

    public Style style() {
        return style;
    }

    public int repairLeather() {
        return style.repairLeather;
    }

    @Nullable
    public static Style style(ItemStack stack) {
        return stack.getItem() instanceof WitchClothingItem clothing ? clothing.style : null;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, @Nullable String type) {
        return "witchery:textures/entities/witchclothes" + (type == null ? "" : "_overlay") + ".png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(WitchClothingModel.extension(this));
    }

    public static boolean deters(Mob aggressor, LivingEntity target) {
        Style robe = style(target.getItemBySlot(EquipmentSlot.CHEST));
        return robe == Style.WITCH_ROBE && aggressor instanceof Creeper
                || robe == Style.NECROMANCER_ROBE && aggressor.getMobType() == MobType.UNDEAD;
    }

    public static boolean evadeWithBabaHat(ServerPlayer player, DamageSource source) {
        if (style(player.getItemBySlot(EquipmentSlot.HEAD)) != Style.BABA_HAT
                || player.level().dimension().equals(WitcheryDimensions.TORMENT)
                || source.getDirectEntity() == null && !source.is(DamageTypeTags.IS_EXPLOSION)) return false;
        ServerLevel level = player.serverLevel();
        RandomSource random = level.getRandom();
        if (random.nextDouble() >= 0.25 || !InfusionEnergy.spend(player, 5)) return false;

        BlockPos ground = player.blockPosition().offset(random.nextInt(12) - 6, 0, random.nextInt(12) - 6);
        while (ground.getY() > level.getMinBuildHeight() && !level.getBlockState(ground).blocksMotion()) {
            ground = ground.below();
        }
        int ceiling = Math.min(level.getMaxBuildHeight() - 3, player.getBlockY() + 64);
        while (ground.getY() < ceiling) {
            var state = level.getBlockState(ground);
            Vec3 landing = Vec3.atBottomCenterOf(ground.above());
            if (state.isFaceSturdy(level, ground, Direction.UP) && !state.is(Blocks.BEDROCK)
                    && level.isEmptyBlock(ground.above()) && level.isEmptyBlock(ground.above(2))
                    && level.isEmptyBlock(ground.above(3)) && WitcheryTeleport.canArrive(player, level, landing)) {
                WitcheryTeleport.move(player, level, landing);
                break;
            }
            ground = ground.above();
        }
        return true;
    }

    public static final class Dyeable extends WitchClothingItem implements DyeableLeatherItem {
        public Dyeable(Style style) {
            super(style);
        }

        @Override
        public int getColor(ItemStack stack) {
            return hasCustomColor(stack) ? DyeableLeatherItem.super.getColor(stack) : 2628115;
        }
    }
}
