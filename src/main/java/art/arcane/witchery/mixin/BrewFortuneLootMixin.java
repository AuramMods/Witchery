package art.arcane.witchery.mixin;

import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.LinkedHashMap;

@Mixin(Block.class)
public abstract class BrewFortuneLootMixin {
    @ModifyVariable(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static ItemStack witchery$addFortune(ItemStack tool, BlockState state, ServerLevel level, BlockPos pos,
                                                @Nullable BlockEntity blockEntity, @Nullable Entity breaker) {
        if (!(breaker instanceof Player player) || blockEntity != null || state.hasBlockEntity()
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) return tool;
        var fortune = player.getEffect(WitcheryEffects.FORTUNE.get());
        if (fortune == null) return tool;

        // The loot context needs a stack to carry Fortune even when the player broke the block by hand.
        ItemStack lootTool = tool.isEmpty() ? new ItemStack(Items.STICK) : tool.copy();
        var enchantments = new LinkedHashMap<>(lootTool.getAllEnchantments());
        enchantments.merge(Enchantments.BLOCK_FORTUNE, fortune.getAmplifier() > 2 ? 2 : 1, Integer::sum);
        EnchantmentHelper.setEnchantments(enchantments, lootTool);
        return lootTool;
    }
}
