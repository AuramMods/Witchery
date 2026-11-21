package art.arcane.witchery.item;

import art.arcane.witchery.entity.HornedHuntsman;
import art.arcane.witchery.entity.WitcheryBolt;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class BarkBeltItem extends WitchClothingItem.Dyeable {
    private static final String CHARGES = "WITCBarkPieces";

    public BarkBeltItem() {
        super(Style.BARK_BELT);
    }

    public static int visibleCharges(LivingEntity wearer) {
        ItemStack belt = wearer.getItemBySlot(EquipmentSlot.LEGS);
        if (!(belt.getItem() instanceof BarkBeltItem)) return 0;
        return Math.min(storedCharges(belt), capacity(wearer));
    }

    public static void recharge(LivingEntity wearer) {
        Level level = wearer.level();
        if (level.isClientSide || level.getGameTime() % 100 != 0) return;
        ItemStack belt = wearer.getItemBySlot(EquipmentSlot.LEGS);
        if (!(belt.getItem() instanceof BarkBeltItem)) return;
        int charges = storedCharges(belt);
        if (charges >= capacity(wearer)) return;
        var surface = level.getBlockState(wearer.blockPosition().below());
        if (!surface.is(Blocks.GRASS_BLOCK) && !surface.is(Blocks.MYCELIUM)) return;
        belt.getOrCreateTag().putInt(CHARGES, charges + 1);
        float pitch = (float) (0.8 + 2 * level.random.nextGaussian());
        level.playSound(null, wearer, WitcherySounds.WOOD_CREAK.get(), SoundSource.PLAYERS, 0.5F, pitch);
    }

    public static boolean absorb(LivingEntity wearer, DamageSource source) {
        if (source.getDirectEntity() == null && source.getEntity() == null || isWooden(source)) return false;
        int charges = visibleCharges(wearer);
        if (charges <= 0) return false;
        Level level = wearer.level();
        RandomSource random = level.getRandom();
        int spent = charges > 1 && random.nextDouble() < 0.25 ? 2 : 1;
        wearer.getItemBySlot(EquipmentSlot.LEGS).getOrCreateTag().putInt(CHARGES, charges - spent);
        for (int i = 0; i < spent; i++) {
            double x = wearer.getX() + (random.nextBoolean() ? 1 : -1);
            double z = wearer.getZ() + (random.nextBoolean() ? 1 : -1);
            var stick = new ItemEntity(level, x, wearer.getY() + 1.5, z, new ItemStack(Items.STICK));
            stick.setPickUpDelay(60);
            stick.lifespan = 60;
            level.addFreshEntity(stick);
        }
        return true;
    }

    private static boolean isWooden(DamageSource source) {
        if (source.getEntity() instanceof HornedHuntsman && !source.is(DamageTypeTags.IS_PROJECTILE)) return true;
        if (source.getEntity() instanceof LivingEntity attacker
                && attacker.getMainHandItem().getItem() instanceof SwordItem sword && sword.getTier() == Tiers.WOOD) return true;
        return source.getDirectEntity() instanceof WitcheryBolt bolt && switch (bolt.kind()) {
            case STAKE, ANTI_MAGIC, POWERED_ANTI_MAGIC -> true;
            case HOLY, SILVER -> false;
        };
    }

    private static int storedCharges(ItemStack belt) {
        return belt.hasTag() ? Math.max(0, belt.getTag().getInt(CHARGES)) : 0;
    }

    private static int capacity(LivingEntity wearer) {
        int charges = 0;
        for (ItemStack armor : wearer.getArmorSlots()) {
            if (armor.getItem() instanceof WitchClothingItem) charges += 2;
        }
        return charges;
    }
}
