package art.arcane.witchery.brewing;

import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;

public final class BrewItemCurse {
    private static final String CURSE_KEY = "WITCCurse";
    private static final String BREW_KEY = "Curse";
    private static final String CHARGES_KEY = "Count";
    private static final String DELAY_KEY = "Delay";

    private BrewItemCurse() {}

    public static CompoundTag savedBrew(CompoundTag itemTag) {
        return itemTag.getCompound(CURSE_KEY).getCompound(BREW_KEY);
    }

    public static boolean attach(ServerLevel level, BlockPos cauldron, CompoundTag brew) {
        AABB above = new AABB(cauldron.above());
        // The original offering area is above the rim. Items can also settle inside the modern hollow basin.
        AABB interior = new AABB(cauldron).deflate(0.125, 0, 0.125);
        var offerings = level.getEntitiesOfClass(ItemEntity.class, above.minmax(interior),
                item -> item.isAlive() && !item.getItem().isEmpty()
                        && (above.intersects(item.getBoundingBox()) || interior.intersects(item.getBoundingBox())));
        if (offerings.isEmpty()) return false;
        ItemEntity offering = offerings.stream().filter(item -> above.intersects(item.getBoundingBox()))
                .findFirst().orElse(offerings.get(0));

        AABB players = above.inflate(3);
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
        WitcheryNetwork.sendBrewItemCharge(level, offering.position());
        return true;
    }

    public static void trigger(ServerPlayer player, ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(CURSE_KEY, Tag.TAG_COMPOUND)) return;
        CompoundTag curse = root.getCompound(CURSE_KEY);
        long now = System.currentTimeMillis();
        long delay = curse.getLong(DELAY_KEY);
        if (delay != 0L && now <= delay) return;
        int charges = curse.getInt(CHARGES_KEY);
        if (charges > 0) {
            CompoundTag brew = curse.getCompound(BREW_KEY);
            BrewMixture mixture = BrewMixture.from(brew);
            if (mixture == null || mixture.delivery() != BrewMixture.Delivery.TRIGGERED || !mixture.hasEffects()) return;
            ServerLevel level = player.serverLevel();
            var caster = FakePlayerFactory.getMinecraft(level);
            mixture.applyAt(player, caster, caster, 1, 1, false, player.position());
            WitcheryNetwork.sendTriggeredBrewCue(level, player.position());
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
