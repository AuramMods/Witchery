package art.arcane.witchery.data;

import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;

public final class WolfBoneLootModifier extends LootModifier {
    private static final int FIND_COOLDOWN = 1200;
    public static final Codec<WolfBoneLootModifier> CODEC =
            RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, WolfBoneLootModifier::new));

    private WolfBoneLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Codec<WolfBoneLootModifier> codec() { return CODEC; }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> drops, LootContext context) {
        if (drops.size() != 1 || !context.hasParam(LootContextParams.BLOCK_STATE) || !context.hasParam(LootContextParams.TOOL)
                || !(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player)
                || !Lycanthropy.isWolf(player) || Lycanthropy.rank(player) < 3) return drops;
        ItemStack dirt = drops.get(0);
        if (!dirt.is(Items.DIRT) && !dirt.is(Items.COARSE_DIRT) && !dirt.is(Items.PODZOL)) return drops;
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            long now = context.getLevel().getGameTime();
            long previous = data.getLastBoneFindGameTime();
            if (previous != Long.MIN_VALUE && now - previous < FIND_COOLDOWN) return;
            var random = context.getRandom();
            if (random.nextInt(20) == 0) {
                drops.add(new ItemStack(Items.BONE, random.nextInt(5) == 0 ? 2 : 1));
                data.markBoneFound(now);
            }
        });
        return drops;
    }
}
