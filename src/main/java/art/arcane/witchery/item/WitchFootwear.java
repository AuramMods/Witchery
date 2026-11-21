package art.arcane.witchery.item;

import art.arcane.witchery.brewing.BrewTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class WitchFootwear {
    private WitchFootwear() {}

    public static void tick(ServerPlayer player) {
        ItemStack shoes = player.getItemBySlot(EquipmentSlot.FEET);
        var style = WitchClothingItem.style(shoes);
        if (style == WitchClothingItem.Style.ICY_SLIPPERS) freezeUnderfoot(player, shoes);
        if (style == WitchClothingItem.Style.SEEPING_SHOES) purgePoison(player);
    }

    private static void freezeUnderfoot(ServerPlayer player, ItemStack shoes) {
        ServerLevel level = player.serverLevel();
        BlockPos corner = BlockPos.containing(player.getX() - 0.5, player.getY() - 1, player.getZ() - 0.5);
        for (BlockPos pos : BlockPos.betweenClosed(corner, corner.offset(1, 0, 1))) {
            Block block = level.getBlockState(pos).getBlock();
            if (block == Blocks.WATER) {
                BrewTerrain.setBlock(player, level, pos, Blocks.ICE.defaultBlockState(), Direction.UP);
            } else if (block == Blocks.LAVA
                    && BrewTerrain.setBlock(player, level, pos, Blocks.OBSIDIAN.defaultBlockState(), Direction.UP)
                    && level.random.nextInt(10) == 0) {
                shoes.hurtAndBreak(1, player, wearer -> wearer.broadcastBreakEvent(EquipmentSlot.FEET));
                if (shoes.isEmpty()) return;
            }
        }
    }

    private static void purgePoison(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (level.getGameTime() % 20 != 0 || !player.onGround()) return;
        boolean cured = player.removeEffect(MobEffects.POISON);
        cured |= player.removeEffect(MobEffects.WITHER);
        if (!cured) return;
        BlockPos center = player.blockPosition();
        // The original scan covers the full seven-by-seven square on three elevations.
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, 1, 3))) {
            if (!level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above()) && level.mayInteract(player, pos)) {
                BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, player);
            }
        }
    }
}
