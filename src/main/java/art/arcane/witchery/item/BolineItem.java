package art.arcane.witchery.item;

import art.arcane.witchery.block.BloodRoseBlock;
import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
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

public final class BolineItem extends GuardingSwordItem {
    public BolineItem() {
        super(Tiers.IRON, 2, -2.4F, new Properties().rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        if (player == null || !level.mayInteract(player, pos)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            return cutBlock(server, pos, state, player, context.getHand(), false)
                    ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return hasCuttings(state) || state.getBlock() instanceof IForgeShearable
                ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        ServerLevel level = serverPlayer.serverLevel();
        if (serverPlayer.blockActionRestricted(level, pos, serverPlayer.gameMode.getGameModeForPlayer())) return false;
        return cutBlock(level, pos, level.getBlockState(pos), player, InteractionHand.MAIN_HAND, true);
    }

    private static boolean hasCuttings(BlockState state) {
        return state.getBlock() instanceof BloodRoseBlock || state.getBlock() instanceof PlantMineBlock
                || state.is(Blocks.COBWEB) || state.is(BlockTags.LEAVES) || state.is(Blocks.GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.VINE) || state.is(Blocks.DEAD_BUSH);
    }

    private static boolean cutBlock(ServerLevel level, BlockPos pos, BlockState state, Player player,
                                    InteractionHand hand, boolean mining) {
        ItemStack tool = player.getItemInHand(hand);
        boolean rose = state.getBlock() instanceof BloodRoseBlock;
        boolean web = state.is(Blocks.COBWEB);
        boolean mine = state.getBlock() instanceof PlantMineBlock;
        List<ItemStack> cuttings;
        if (rose || web) {
            cuttings = List.of(new ItemStack(state.getBlock()));
        } else if (mine) {
            cuttings = List.of(state.getBlock().getCloneItemStack(level, pos, state));
        } else if (hasCuttings(state)) {
            cuttings = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, new ItemStack(Items.SHEARS));
        } else if (state.getBlock() instanceof IForgeShearable shearable && shearable.isShearable(tool, level, pos)) {
            cuttings = shearable.onSheared(player, tool, level, pos, tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE));
        } else {
            return false;
        }
        if (!level.destroyBlock(pos, false, player)) return false;
        cuttings.forEach(cutting -> Block.popResource(level, pos, cutting));
        if (!rose && !web) tool.hurtAndBreak(mining && mine ? 2 : 1, player, owner -> owner.broadcastBreakEvent(hand));
        return true;
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
        if (state.is(Blocks.VINE) || state.getBlock() instanceof BloodRoseBlock) return 1.5F;
        return Math.max(super.getDestroySpeed(stack, state), Items.SHEARS.getDestroySpeed(stack, state));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return action == ToolActions.SHEARS_HARVEST || super.canPerformAction(stack, action);
    }
}
