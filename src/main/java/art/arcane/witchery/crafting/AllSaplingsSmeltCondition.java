package art.arcane.witchery.crafting;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryCommonConfig;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public enum AllSaplingsSmeltCondition implements ICondition {
    INSTANCE;

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "all_saplings_smelt");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return WitcheryCommonConfig.SMELT_ALL_SAPLINGS_TO_WOOD_ASH.get();
    }

    public static final class Serializer implements IConditionSerializer<AllSaplingsSmeltCondition> {
        @Override
        public void write(JsonObject json, AllSaplingsSmeltCondition condition) {
        }

        @Override
        public AllSaplingsSmeltCondition read(JsonObject json) {
            return INSTANCE;
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
