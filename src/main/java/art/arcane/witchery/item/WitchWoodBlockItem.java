package art.arcane.witchery.item;

import art.arcane.witchery.block.WitchSaplingBlock;
import art.arcane.witchery.block.WitchWoodType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class WitchWoodBlockItem extends BlockItem {
    public WitchWoodBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId() + "." + WitchWoodType.fromStack(stack).getSerializedName();
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return getBlock() instanceof WitchSaplingBlock ? 100 : super.getBurnTime(stack, recipeType);
    }

    static void importLegacyMetadata(ItemStack stack, int metadata) {
        CompoundTag state = stack.getTagElement(BLOCK_STATE_TAG);
        String property = WitchWoodType.PROPERTY.getName();
        if (state != null && state.contains(property, Tag.TAG_STRING)) return;

        WitchWoodType wood = switch (metadata & 3) {
            case 0 -> WitchWoodType.ROWAN;
            case 1 -> WitchWoodType.ALDER;
            case 2 -> WitchWoodType.HAWTHORN;
            default -> null;
        };
        if (wood != null) stack.getOrCreateTagElement(BLOCK_STATE_TAG).putString(property, wood.getSerializedName());
    }
}
