package art.arcane.witchery.data;

import art.arcane.witchery.registry.WitcheryRegistries;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public record LegacyFixedLootCount(int count) implements NumberProvider {
    @Override
    public int getInt(LootContext context) {
        // The original chest generator drew the quantity even when the range contained one value.
        context.getRandom().nextInt(1);
        return count;
    }

    @Override
    public float getFloat(LootContext context) {
        return getInt(context);
    }

    @Override
    public LootNumberProviderType getType() {
        return WitcheryRegistries.LEGACY_FIXED_LOOT_COUNT.get();
    }

    public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LegacyFixedLootCount> {
        @Override
        public void serialize(JsonObject json, LegacyFixedLootCount value, JsonSerializationContext context) {
            json.addProperty("count", value.count());
        }

        @Override
        public LegacyFixedLootCount deserialize(JsonObject json, JsonDeserializationContext context) {
            return new LegacyFixedLootCount(GsonHelper.getAsInt(json, "count"));
        }
    }
}
