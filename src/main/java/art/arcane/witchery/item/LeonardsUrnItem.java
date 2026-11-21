package art.arcane.witchery.item;

import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.menu.LeonardsUrnMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public final class LeonardsUrnItem extends Item {
    private static final String TIER = "UrnTier";

    public LeonardsUrnItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    public static int tier(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return 0;
        return Mth.clamp(tag.contains(TIER, Tag.TAG_ANY_NUMERIC) ? tag.getInt(TIER) : tag.getInt("Damage"), 0, 3);
    }

    public static void setTier(ItemStack stack, int tier) {
        stack.getOrCreateTag().putInt(TIER, Mth.clamp(tier, 0, 3));
    }

    public static NonNullList<ItemStack> contents(ItemStack stack) {
        var contents = NonNullList.withSize(tier(stack) + 1, ItemStack.EMPTY);
        var inventory = stack.getTagElement("Inventory");
        if (inventory == null) return contents;
        for (Tag entry : inventory.getList("Items", Tag.TAG_COMPOUND)) {
            CompoundTag item = (CompoundTag) entry;
            int slot = item.getByte("Slot") & 255;
            if (slot < contents.size()) contents.set(slot, WitcheryIngredient.loadInventoryStack(item));
        }
        return contents;
    }

    public static void saveContents(ItemStack urn, List<ItemStack> contents) {
        var items = new ListTag();
        for (int slot = 0; slot < contents.size(); slot++) {
            if (contents.get(slot).isEmpty()) continue;
            var entry = new CompoundTag();
            entry.putByte("Slot", (byte) slot);
            contents.get(slot).save(entry);
            items.add(entry);
        }
        urn.getOrCreateTagElement("Inventory").put("Items", items);
    }

    public static BrewMixture splash(ItemStack stack) {
        if (!(stack.getItem() instanceof BrewBottleItem)) return null;
        BrewMixture mixture = BrewMixture.from(stack.getTag());
        return mixture != null && mixture.splash() ? mixture : null;
    }

    public static ItemStack readSpell(Player player, int slot) {
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof LeonardsUrnItem)) continue;
            var contents = contents(stack);
            return slot >= 0 && slot < contents.size() ? contents.get(slot) : ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack urn = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(urn);
        if (player instanceof ServerPlayer server) {
            int slot = player.getInventory().selected;
            NetworkHooks.openScreen(server, new SimpleMenuProvider(
                    (id, inventory, owner) -> new LeonardsUrnMenu(id, inventory, urn, slot), urn.getHoverName()),
                    buffer -> buffer.writeVarInt(tier(urn) + 1).writeVarInt(slot));
        }
        return InteractionResultHolder.sidedSuccess(urn, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }
}
