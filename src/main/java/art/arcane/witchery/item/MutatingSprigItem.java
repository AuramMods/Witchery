package art.arcane.witchery.item;

import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class MutatingSprigItem extends Item {
    public MutatingSprigItem() {
        super(new Properties().rarity(Rarity.UNCOMMON).durability(15));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState clicked = level.getBlockState(pos);
        boolean surface = clicked.is(Blocks.GRASS_BLOCK) || clicked.is(Blocks.MYCELIUM);
        boolean clay = MutandisItem.isSoakedDirt(level, pos);
        boolean water = level.getBlockState(pos.below()).is(Blocks.WATER) && level.getFluidState(pos.below()).isSource();
        boolean grasspers = water && clicked.is(Blocks.CHEST) && sides(level, pos, Blocks.VINE, false);
        boolean snares = water && clicked.is(Blocks.COBWEB)
                && sides(level, pos, WitcheryBlocks.getRequired("witchsapling").get(), true);
        if (level instanceof ServerLevel server) {
            if (surface) {
                Block changed = clicked.is(Blocks.GRASS_BLOCK) ? Blocks.MYCELIUM : Blocks.GRASS_BLOCK;
                server.setBlockAndUpdate(pos, changed.defaultBlockState());
            } else if (clay) {
                MutandisItem.makeClay(server, pos);
                for (Direction side : Direction.Plane.HORIZONTAL) MutandisItem.makeClay(server, pos.relative(side));
            } else if (grasspers && server.getBlockEntity(pos) instanceof ChestBlockEntity chest && chest.isEmpty()) {
                replaceAround(server, pos, WitcheryBlocks.getRequired("grassper").get());
            } else if (snares) {
                var area = new AABB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                        pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
                var zombies = server.getEntitiesOfClass(Zombie.class, area, Zombie::isAlive);
                if (zombies.isEmpty()) return InteractionResult.PASS;
                zombies.get(0).playSound(SoundEvents.ZOMBIE_DEATH, 1, 1);
                zombies.get(0).discard();
                replaceAround(server, pos, WitcheryBlocks.getRequired("crittersnare").get());
            } else {
                return InteractionResult.PASS;
            }
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(context.getHand()));
            server.sendParticles(surface || clay ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.ITEM_SLIME,
                    pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 16, 0.4, 0.4, 0.4, 0.1);
            server.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1, 1);
            return InteractionResult.CONSUME;
        }
        return surface || clay || grasspers || snares ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static boolean sides(Level level, BlockPos center, Block block, boolean alderOnly) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(center.relative(side));
            if (!state.is(block) || alderOnly && state.getValue(WitchWoodType.PROPERTY) != WitchWoodType.ALDER) return false;
        }
        return true;
    }

    private static void replaceAround(ServerLevel level, BlockPos center, Block output) {
        BlockState state = output.defaultBlockState();
        for (Direction side : Direction.Plane.HORIZONTAL) level.setBlockAndUpdate(center.relative(side), state);
        level.removeBlock(center, false);
    }
}
