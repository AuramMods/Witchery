package art.arcane.witchery.item;

import art.arcane.witchery.world.WitcheryDimensions;
import art.arcane.witchery.ritual.DroppedWaystones;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class WaystoneItem extends Item {
    private static final Map<Integer, ResourceKey<Level>> LEGACY_DIMENSIONS = Map.of(
            -1, Level.NETHER, 0, Level.OVERWORLD, 1, Level.END,
            -37, WitcheryDimensions.DREAM, -38, WitcheryDimensions.TORMENT, -39, WitcheryDimensions.MIRROR);

    public record Location(ResourceKey<Level> dimension, BlockPos position) {}

    public WaystoneItem() {
        super(new Properties());
    }

    public static boolean isBoundWaystone(ItemStack stack) {
        return stack.is(WitcheryIngredient.BOUND_WAYSTONE.item()) || stack.is(WitcheryIngredient.CREATURE_BOUND_WAYSTONE.item());
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        return DroppedWaystones.update(entity);
    }

    public static void bindLocation(ItemStack stone, ServerLevel level, BlockPos pos) {
        var dimension = level.dimension();
        var tag = stone.getOrCreateTag();
        tag.putInt("PosX", pos.getX());
        tag.putInt("PosY", pos.getY());
        tag.putInt("PosZ", pos.getZ());
        tag.putString("Dimension", dimension.location().toString());
        tag.putString("NameD", dimension.location().toString());
        for (var legacy : LEGACY_DIMENSIONS.entrySet()) {
            if (legacy.getValue().equals(dimension)) {
                tag.putInt("PosD", legacy.getKey());
                break;
            }
        }
    }

    @Nullable
    public static Location location(ItemStack stone) {
        CompoundTag tag = stone.getTag();
        if (tag == null || !tag.contains("PosX", Tag.TAG_ANY_NUMERIC) || !tag.contains("PosY", Tag.TAG_ANY_NUMERIC)
                || !tag.contains("PosZ", Tag.TAG_ANY_NUMERIC)) {
            return null;
        }
        ResourceLocation id = tag.contains("Dimension", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("Dimension")) : null;
        ResourceKey<Level> dimension = id != null ? ResourceKey.create(Registries.DIMENSION, id)
                : tag.contains("PosD", Tag.TAG_ANY_NUMERIC) ? LEGACY_DIMENSIONS.get(tag.getInt("PosD")) : null;
        return dimension == null ? null : new Location(dimension,
                new BlockPos(tag.getInt("PosX"), tag.getInt("PosY"), tag.getInt("PosZ")));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        Location location = location(stack);
        if (location != null) {
            BlockPos pos = location.position();
            String dimension = stack.getTag().getString("NameD");
            if (dimension.isEmpty()) dimension = location.dimension().location().toString();
            tooltip.add(Component.translatable("tooltip.witchery.waystone.location", dimension, pos.getX(), pos.getY(), pos.getZ())
                    .withStyle(ChatFormatting.GRAY));
        }
        String creature = TaglockItem.boundName(stack);
        if (!creature.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.witchery.waystone.creature", creature).withStyle(ChatFormatting.GRAY));
        }
    }
}
