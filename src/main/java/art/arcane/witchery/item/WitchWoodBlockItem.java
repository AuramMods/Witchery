package art.arcane.witchery.item;

import art.arcane.witchery.block.WitchSaplingBlock;
import art.arcane.witchery.block.WitchWoodType;
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
}
