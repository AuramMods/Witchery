package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.LeechChestBlockEntity;
import art.arcane.witchery.block.entity.LeechSamples;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class LeechChestBlock extends LegacyPairingChestBlock {
    public LeechChestBlock() {
        super(Properties.copy(Blocks.CHEST).strength(25, 1000).requiresCorrectToolForDrops(), WitcheryBlockEntities.LEECH_CHEST::get);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LeechChestBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        MenuProvider menu = getMenuProvider(state, level, pos);
        if (menu != null && player.openMenu(menu).isPresent()) {
            player.awardStat(getOpenChestStat());
            PiglinAi.angerNearbyPiglins(player, true);
            if (player instanceof ServerPlayer opener && !opener.isSpectator()
                    && level.getBlockEntity(pos) instanceof LeechChestBlockEntity chest) chest.record(opener);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CompoundTag tag = stack.getTag();
        if (!level.isClientSide && tag != null && tag.contains(LeechSamples.PLAYERS_TAG, Tag.TAG_LIST)
                && level.getBlockEntity(pos) instanceof LeechChestBlockEntity chest) chest.loadSamples(tag);
    }
}
