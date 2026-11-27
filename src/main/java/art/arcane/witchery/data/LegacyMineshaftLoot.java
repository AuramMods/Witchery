package art.arcane.witchery.data;

import art.arcane.witchery.registry.WitcheryRegistries;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public final class LegacyMineshaftLoot {
    private LegacyMineshaftLoot() {}

    public interface Context {
        ItemStack witchery$getMineshaftBook();
        void witchery$setMineshaftBook(ItemStack book);
    }

    public record Rolls(int min, int max) implements NumberProvider {
        @Override
        public int getInt(LootContext context) {
            // ChestGenHooks prepared the enchanted entry before drawing the inventory's roll count.
            var random = context.getRandom();
            ((Context) context).witchery$setMineshaftBook(LegacyMineshaftBook.roll(random));
            return Mth.nextInt(random, min, max);
        }

        @Override
        public float getFloat(LootContext context) {
            return getInt(context);
        }

        @Override
        public LootNumberProviderType getType() {
            return WitcheryRegistries.LEGACY_MINESHAFT_ROLLS.get();
        }

        public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<Rolls> {
            @Override
            public void serialize(JsonObject json, Rolls value, JsonSerializationContext context) {
                json.addProperty("min", value.min());
                json.addProperty("max", value.max());
            }

            @Override
            public Rolls deserialize(JsonObject json, JsonDeserializationContext context) {
                return new Rolls(GsonHelper.getAsInt(json, "min"), GsonHelper.getAsInt(json, "max"));
            }
        }
    }

    public static final class Book extends LootItemConditionalFunction {
        private Book(LootItemCondition[] conditions) {
            super(conditions);
        }

        @Override
        protected ItemStack run(ItemStack stack, LootContext context) {
            var book = ((Context) context).witchery$getMineshaftBook();
            if (book.isEmpty()) book = LegacyMineshaftBook.roll(context.getRandom());
            context.getRandom().nextInt(1);
            return book.copy();
        }

        @Override
        public LootItemFunctionType getType() {
            return WitcheryRegistries.LEGACY_MINESHAFT_BOOK.get();
        }

        public static final class Serializer extends LootItemConditionalFunction.Serializer<Book> {
            @Override
            public Book deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
                return new Book(conditions);
            }
        }
    }
}
