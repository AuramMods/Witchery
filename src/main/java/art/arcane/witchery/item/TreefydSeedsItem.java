package art.arcane.witchery.item;

import art.arcane.witchery.entity.Treefyd;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public final class TreefydSeedsItem extends Item {
    public TreefydSeedsItem() {
        super(new Properties());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || context.getClickedFace() != Direction.UP) return InteractionResult.PASS;
        var level = context.getLevel();
        var pos = context.getClickedPos().above();
        var grass = Blocks.GRASS.defaultBlockState();
        if (!player.mayUseItemAt(pos, Direction.UP, stack)
                || !level.getBlockState(pos).canBeReplaced() || !grass.canSurvive(level, pos)) {
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        // Keep the grass, guardian, and seed consumption together when a Forge event cancels planting.
        var snapshot = BlockSnapshot.create(server.dimension(), server, pos);
        if (snapshot.getReplacedBlock() != grass
                && !server.setBlock(pos, grass, Block.UPDATE_NONE | Block.UPDATE_KNOWN_SHAPE)) return InteractionResult.FAIL;
        if (ForgeEventFactory.onBlockPlace(player, snapshot, Direction.UP)) {
            snapshot.restore(true, false);
            return InteractionResult.FAIL;
        }
        var plant = Treefyd.plant(server, Vec3.atBottomCenterOf(pos), player);
        if (plant == null) {
            snapshot.restore(true, false);
            return InteractionResult.FAIL;
        }
        server.markAndNotifyBlock(pos, server.getChunkAt(pos), snapshot.getReplacedBlock(), grass, Block.UPDATE_ALL, Block.UPDATE_LIMIT);
        plant.plantingEffects(server);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.CONSUME;
    }
}
