package art.arcane.witchery.data;

import art.arcane.witchery.prediction.Predictions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;

public final class PredictionLootModifier extends LootModifier {
    public static final Codec<PredictionLootModifier> CODEC =
            RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, PredictionLootModifier::new));

    private PredictionLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Codec<PredictionLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> drops, LootContext context) {
        if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player)) return drops;
        var state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        var origin = context.getParamOrNull(LootContextParams.ORIGIN);
        if (state != null && origin != null && player.level() == context.getLevel()) {
            Predictions.onHarvest(player, BlockPos.containing(origin), state, drops);
        }
        return drops;
    }
}
