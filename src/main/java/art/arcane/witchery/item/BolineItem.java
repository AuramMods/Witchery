package art.arcane.witchery.item;

import art.arcane.witchery.block.BloodRoseBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.List;

public final class BolineItem extends SwordItem {
    public BolineItem() {
        super(Tiers.IRON, 1, -2.4F, new Properties().rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        boolean rose = state.getBlock() instanceof BloodRoseBlock;
        boolean noWear = rose || state.is(Blocks.COBWEB);
        boolean harvest = noWear || state.is(BlockTags.LEAVES) || state.is(Blocks.GRASS) || state.is(Blocks.FERN)
                || state.is(Blocks.VINE) || state.is(Blocks.DEAD_BUSH);
        if (!harvest && !(state.getBlock() instanceof IForgeShearable)) return InteractionResult.PASS;
        if (player == null || !level.mayInteract(player, pos)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            ItemStack tool = context.getItemInHand();
            List<ItemStack> cuttings;
            if (rose) {
                cuttings = List.of(new ItemStack(state.getBlock()));
            } else if (harvest) {
                cuttings = Block.getDrops(state, server, pos, level.getBlockEntity(pos), player, new ItemStack(Items.SHEARS));
            } else if (state.getBlock() instanceof IForgeShearable shearable && shearable.isShearable(tool, level, pos)) {
                cuttings = shearable.onSheared(player, tool, level, pos, tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE));
            } else {
                return InteractionResult.PASS;
            }
            if (!server.destroyBlock(pos, false, player)) return InteractionResult.PASS;
            cuttings.forEach(cutting -> Block.popResource(server, pos, cutting));
            if (!noWear) tool.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return Items.SHEARS.interactLivingEntity(stack, player, target, hand);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.is(Blocks.COBWEB) || state.is(BlockTags.LEAVES) || state.is(Blocks.GRASS) || state.is(Blocks.FERN)
                || state.is(Blocks.VINE) || state.is(Blocks.TRIPWIRE) || state.getBlock() instanceof IForgeShearable) return true;
        return super.mineBlock(stack, level, state, pos, miner);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(WitcheryBlocks.getRequired("web").get()) || Items.SHEARS.isCorrectToolForDrops(state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(WitcheryBlocks.getRequired("web").get())) return 15;
        if (state.is(WitcheryBlocks.getRequired("plantmine").get())) return 5;
        return Math.max(super.getDestroySpeed(stack, state), Items.SHEARS.getDestroySpeed(stack, state));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return action == ToolActions.SHEARS_HARVEST || super.canPerformAction(stack, action);
    }
}
