package art.arcane.witchery.item;

import art.arcane.witchery.registry.WitcheryFluids;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.Nullable;

public final class BrewBucketItem extends Item {
    public BrewBucketItem() {
        super(new Properties().stacksTo(1).craftRemainder(Items.BUCKET));
    }

    public static ItemStack filled(FluidStack fluid) {
        var bucket = new ItemStack(WitcheryItems.getRequired("bucketbrew").get());
        if (fluid.hasTag()) bucket.setTag(fluid.getTag().copy());
        return bucket;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidBucketWrapper(stack) {
            @Override
            public FluidStack getFluid() {
                if (!container.is(BrewBucketItem.this)) return super.getFluid();
                return new FluidStack(WitcheryFluids.getRequiredSource("brew").get(),
                        FluidType.BUCKET_VOLUME, container.getTag());
            }
        };
    }
}
