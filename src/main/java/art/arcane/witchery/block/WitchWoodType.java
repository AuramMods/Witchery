package art.arcane.witchery.block;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum WitchWoodType implements StringRepresentable {
    ROWAN("rowan"),
    ALDER("alder"),
    HAWTHORN("hawthorn");

    public static final EnumProperty<WitchWoodType> PROPERTY = EnumProperty.create("wood_type", WitchWoodType.class);
    private final String name;

    WitchWoodType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public ItemStack stack(ItemLike item) {
        ItemStack stack = new ItemStack(item);
        if (this != ROWAN) {
            stack.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG).putString(PROPERTY.getName(), name);
        }
        return stack;
    }

    public static WitchWoodType fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
        return tag == null ? ROWAN : PROPERTY.getValue(tag.getString(PROPERTY.getName())).orElse(ROWAN);
    }
}
