package art.arcane.witchery.item;

import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DoorKeyItem extends Item {
    private static final String KEYS = "doorKeys";
    private static final String DIMENSION = "doorDimension";

    private record Address(BlockPos pos, @Nullable ResourceKey<Level> dimension, @Nullable Integer unresolvedDimension) {
        boolean matches(Level level, BlockPos base) {
            return pos.equals(base) && unresolvedDimension == null && (dimension == null || dimension.equals(level.dimension()));
        }
    }

    public DoorKeyItem() {
        super(new Properties());
    }

    public static ItemStack forDoor(Level level, BlockPos base) {
        var key = new ItemStack(WitcheryIngredient.DOOR_KEY.item());
        var tag = key.getOrCreateTag();
        tag.putInt("doorX", base.getX());
        tag.putInt("doorY", base.getY());
        tag.putInt("doorZ", base.getZ());
        String dimension = level.dimension().location().toString();
        tag.putString(DIMENSION, dimension);
        tag.putString("doorDN", dimension);
        Integer oldId = WitcheryDimensions.legacyId(level.dimension());
        if (oldId != null) tag.putInt("doorD", oldId);
        return key;
    }

    public static boolean canOpen(Player player, Level level, BlockPos base) {
        for (ItemStack stack : player.getInventory().items) {
            if (opens(stack, level, base)) return true;
        }
        return opens(player.getOffhandItem(), level, base);
    }

    private static boolean opens(ItemStack stack, Level level, BlockPos base) {
        var tag = stack.getTag();
        if (tag == null) return false;
        if (WitcheryIngredient.DOOR_KEY.matches(stack)) {
            var address = address(tag);
            return address != null && address.matches(level, base);
        }
        if (WitcheryIngredient.DOOR_KEYRING.matches(stack)) {
            var keys = tag.getList(KEYS, Tag.TAG_COMPOUND);
            for (int i = 0; i < keys.size(); i++) {
                var address = address(keys.getCompound(i));
                if (address != null && address.matches(level, base)) return true;
            }
        }
        return false;
    }

    public static void addToRing(ItemStack ring, ItemStack key) {
        var source = key.getTag();
        var address = address(source);
        if (address == null) return;
        var ringTag = ring.getOrCreateTag();
        var keys = ringTag.getList(KEYS, Tag.TAG_COMPOUND);
        for (int i = 0; i < keys.size(); i++) {
            if (address.equals(address(keys.getCompound(i)))) return;
        }
        keys.add(source.copy());
        ringTag.put(KEYS, keys);
    }

    @Nullable
    private static Address address(@Nullable CompoundTag tag) {
        if (tag == null || !tag.contains("doorX", Tag.TAG_ANY_NUMERIC) || !tag.contains("doorY", Tag.TAG_ANY_NUMERIC)
                || !tag.contains("doorZ", Tag.TAG_ANY_NUMERIC)) return null;
        var pos = new BlockPos(tag.getInt("doorX"), tag.getInt("doorY"), tag.getInt("doorZ"));
        if (tag.contains(DIMENSION, Tag.TAG_STRING)) {
            var id = ResourceLocation.tryParse(tag.getString(DIMENSION));
            return id == null ? null : new Address(pos, ResourceKey.create(Registries.DIMENSION, id), null);
        }
        if (tag.contains("doorD", Tag.TAG_ANY_NUMERIC)) {
            int oldId = tag.getInt("doorD");
            var dimension = WitcheryDimensions.fromLegacyId(oldId);
            return new Address(pos, dimension, dimension == null ? oldId : null);
        }
        return new Address(pos, null, null);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        var tag = stack.getTag();
        if (tag == null) return;
        if (stack.is(WitcheryIngredient.DOOR_KEYRING.item())) {
            var keys = tag.getList(KEYS, Tag.TAG_COMPOUND);
            for (int i = 0; i < keys.size(); i++) showLocation(keys.getCompound(i), tooltip);
        } else {
            showLocation(tag, tooltip);
        }
    }

    private static void showLocation(CompoundTag tag, List<Component> tooltip) {
        var address = address(tag);
        if (address == null) return;
        var pos = address.pos();
        String coords = pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        String dimension = tag.getString("doorDN");
        if (dimension.isEmpty() && address.dimension() != null) dimension = address.dimension().location().toString();
        tooltip.add(Component.literal(dimension.isEmpty() ? coords : dimension + ": " + coords).withStyle(ChatFormatting.GRAY));
    }
}
