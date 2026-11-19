package art.arcane.witchery.data;

import art.arcane.witchery.block.WitchCrop;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;

import java.util.Arrays;

public final class GrassSeedLootModifier extends LootModifier {
    public static final Codec<GrassSeedLootModifier> CODEC =
            RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, GrassSeedLootModifier::new));
    private static final int WHEAT_WEIGHT = 10;
    private static final int TOTAL_WEIGHT = WHEAT_WEIGHT
            + Arrays.stream(WitchCrop.values()).mapToInt(WitchCrop::grassWeight).sum();

    private GrassSeedLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Codec<GrassSeedLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> drops, LootContext context) {
        for (int i = 0; i < drops.size(); i++) {
            if (!drops.get(i).is(Items.WHEAT_SEEDS)) {
                continue;
            }
            int choice = context.getRandom().nextInt(TOTAL_WEIGHT) - WHEAT_WEIGHT;
            if (choice < 0) {
                continue;
            }
            for (WitchCrop crop : WitchCrop.values()) {
                choice -= crop.grassWeight();
                if (choice < 0) {
                    drops.set(i, new ItemStack(crop.seed()));
                    break;
                }
            }
        }
        return drops;
    }
}
