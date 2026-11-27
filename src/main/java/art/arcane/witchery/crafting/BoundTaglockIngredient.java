package art.arcane.witchery.crafting;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.registry.WitcheryItems;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public final class BoundTaglockIngredient extends AbstractIngredient {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "bound_taglock");
    public static final IIngredientSerializer<BoundTaglockIngredient> SERIALIZER = new IIngredientSerializer<>() {
        @Override
        public BoundTaglockIngredient parse(FriendlyByteBuf buffer) {
            return new BoundTaglockIngredient();
        }

        @Override
        public BoundTaglockIngredient parse(JsonObject json) {
            return new BoundTaglockIngredient();
        }

        @Override
        public void write(FriendlyByteBuf buffer, BoundTaglockIngredient ingredient) {
        }
    };

    private BoundTaglockIngredient() {
        super(Stream.of(new ItemValue(new ItemStack(WitcheryItems.getRequired("taglockkit").get()))));
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return stack != null && TaglockItem.isFilledKit(stack);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<BoundTaglockIngredient> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", ID.toString());
        return json;
    }
}
