package art.arcane.witchery.brewing;

import art.arcane.witchery.familiar.FamiliarBinding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public final class BrewItemCurse {
    private static final String CURSE_KEY = "WITCCurse";
    private static final String BREW_KEY = "Curse";
    private static final String CHARGES_KEY = "Count";
    private static final String DELAY_KEY = "Delay";

    private BrewItemCurse() {}

    public static boolean attach(ServerLevel level, BlockPos cauldron, CompoundTag brew) {
        // The modern cauldron is hollow, so offerings can settle inside it as well as above it.
        AABB basin = new AABB(cauldron.getX() + 0.125, cauldron.getY(), cauldron.getZ() + 0.125,
                cauldron.getX() + 0.875, cauldron.getY() + 2, cauldron.getZ() + 0.875);
        ItemEntity offering = level.getEntitiesOfClass(ItemEntity.class, basin,
                item -> item.isAlive() && !item.getItem().isEmpty()).stream().findFirst().orElse(null);
        if (offering == null) return false;

        AABB players = new AABB(cauldron.above()).inflate(3);
        int charges = level.getEntitiesOfClass(ServerPlayer.class, players, FamiliarBinding::hasCurseMastery).isEmpty() ? 1 : 2;
        ItemStack stack = offering.getItem();
        ItemStack cursed = stack.copyWithCount(1);
        CompoundTag root = cursed.getOrCreateTag();
        CompoundTag curse = root.getCompound(CURSE_KEY);
        if (curse.getCompound(BREW_KEY).getList("Items", Tag.TAG_COMPOUND)
                .equals(brew.getList("Items", Tag.TAG_COMPOUND))) {
            int stored = Math.max(0, curse.getInt(CHARGES_KEY));
            curse.putInt(CHARGES_KEY, Math.min(stored, Integer.MAX_VALUE - charges) + charges);
        } else {
            curse = new CompoundTag();
            curse.put(BREW_KEY, brew.copy());
            curse.putInt(CHARGES_KEY, charges);
        }
        root.put(CURSE_KEY, curse);
        if (stack.getCount() > 1) {
            ItemEntity single = new ItemEntity(level, offering.getX(), offering.getY(), offering.getZ(), cursed);
            single.setDefaultPickUpDelay();
            if (!level.addFreshEntity(single)) return false;
            offering.setItem(stack.copyWithCount(stack.getCount() - 1));
        } else {
            offering.setItem(cursed);
        }
        level.sendParticles(ParticleTypes.POOF, offering.getX(), offering.getY(), offering.getZ(), 16, 0.2, 0.25, 0.2, 0.04);
        level.playSound(null, offering, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.7F, 1);
        return true;
    }

    public static void trigger(ServerPlayer player, ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(CURSE_KEY, Tag.TAG_COMPOUND)) return;
        CompoundTag curse = root.getCompound(CURSE_KEY);
        long now = System.currentTimeMillis();
        if (curse.getLong(DELAY_KEY) > now) return;
        int charges = curse.getInt(CHARGES_KEY);
        if (charges > 0) {
            CompoundTag brew = curse.getCompound(BREW_KEY);
            BrewMixture mixture = BrewMixture.from(brew);
            if (mixture == null || mixture.delivery() != BrewMixture.Delivery.TRIGGERED || !mixture.hasEffects()) return;
            mixture.applyAt(player, player, player, 1, 1, false, player.position());
            int color = brew.contains("Color", Tag.TAG_ANY_NUMERIC) ? brew.getInt("Color") : mixture.color();
            var tint = new Vector3f((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F);
            player.serverLevel().sendParticles(new DustParticleOptions(tint, 1), player.getX(), player.getY(0.5),
                    player.getZ(), 16, 0.3, 0.5, 0.3, 0.05);
            player.serverLevel().playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.6F, 1);
        }
        if (charges > 1) {
            curse.putInt(CHARGES_KEY, charges - 1);
            curse.putLong(DELAY_KEY, now + 5000L);
        } else {
            stack.removeTagKey(CURSE_KEY);
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }
}
