package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.util.FakePlayer;

public final class HobgoblinMiningLootModifier extends LootModifier {
    public static final Codec<HobgoblinMiningLootModifier> CODEC =
            RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, HobgoblinMiningLootModifier::new));

    private HobgoblinMiningLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Codec<HobgoblinMiningLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> drops, LootContext context) {
        if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof FakePlayer miner)) return drops;
        boolean koboldite = HobgoblinMiningGoal.KOBOLDITE_MINER.equals(miner.getUUID());
        if (!koboldite && !HobgoblinMiningGoal.MINER.equals(miner.getUUID())) return drops;
        var state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null || drops.isEmpty()) return drops;
        var random = context.getRandom();
        boolean ore = state.is(Tags.Blocks.ORES);
        var level = context.getLevel();
        if (ore && koboldite) {
            for (int i = 0; i < drops.size(); i++) {
                ItemStack drop = drops.get(i);
                var input = new SimpleContainer(drop);
                var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level);
                if (recipe.isPresent() && random.nextDouble() < 0.5) {
                    ItemStack smelted = recipe.get().assemble(input, level.registryAccess());
                    smelted.setCount(smelted.getCount() * (random.nextDouble() < 0.25 ? 3 : 2));
                    drops.set(i, smelted);
                }
            }
        }
        double chance = ore ? (koboldite ? 0.08 : 0.05) : (koboldite ? 0.02 : 0.01);
        if (random.nextDouble() < chance) drops.add(new ItemStack(WitcheryIngredient.KOBOLDITE_DUST.item()));
        return drops;
    }
}
