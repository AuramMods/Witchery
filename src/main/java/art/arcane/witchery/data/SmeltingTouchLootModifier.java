package art.arcane.witchery.data;

import art.arcane.witchery.infusion.ImpContractEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;

public final class SmeltingTouchLootModifier extends LootModifier {
    public static final Codec<SmeltingTouchLootModifier> CODEC =
            RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, SmeltingTouchLootModifier::new));

    private SmeltingTouchLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Codec<SmeltingTouchLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> drops, LootContext context) {
        if (context.getParamOrNull(LootContextParams.BLOCK_STATE) == null
                || !(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player)
                || !ImpContractEffects.MELTING_TOUCH.isActive(player)) return drops;
        var level = context.getLevel();
        for (int i = 0; i < drops.size(); i++) {
            var input = new SimpleContainer(drops.get(i));
            var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level).orElse(null);
            if (recipe == null) continue;
            ItemStack smelted = recipe.assemble(input, level.registryAccess());
            if (context.getRandom().nextDouble() < 0.25) smelted.grow(1);
            drops.set(i, smelted);
        }
        return drops;
    }
}
