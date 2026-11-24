package art.arcane.witchery.data;

import art.arcane.witchery.registry.WitcheryRegistries;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public record LootingBonus(int base) implements NumberProvider {
    @Override
    public int getInt(LootContext context) {
        return (int) Math.min(Integer.MAX_VALUE, (long) base + Math.max(0, context.getLootingModifier()));
    }

    @Override
    public float getFloat(LootContext context) {
        return getInt(context);
    }

    @Override
    public LootNumberProviderType getType() {
        return WitcheryRegistries.LOOTING_BONUS.get();
    }

    public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootingBonus> {
        @Override
        public void serialize(JsonObject json, LootingBonus value, JsonSerializationContext context) {
            json.addProperty("base", value.base());
        }

        @Override
        public LootingBonus deserialize(JsonObject json, JsonDeserializationContext context) {
            return new LootingBonus(GsonHelper.getAsInt(json, "base", 0));
        }
    }
}
