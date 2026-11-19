package art.arcane.witchery.item;

import art.arcane.witchery.block.AltarBlock;
import art.arcane.witchery.block.PlacedItemBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class ArthanaItem extends SwordItem {
    public ArthanaItem() {
        super(Tiers.GOLD, 3, -2.4F, new Properties().durability(Tiers.IRON.getUses()).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || context.getClickedFace() != Direction.UP
                || !(level.getBlockState(clicked).getBlock() instanceof AltarBlock) || !level.isEmptyBlock(clicked.above())) {
            return InteractionResult.PASS;
        }
        if (!player.mayUseItemAt(clicked.above(), Direction.UP, stack)) {
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel server && !PlacedItemBlock.place(server, clicked.above(), player, stack)) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
