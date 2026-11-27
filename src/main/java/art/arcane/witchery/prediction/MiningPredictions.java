package art.arcane.witchery.prediction;

import art.arcane.witchery.brewing.BrewTerrain;
import art.arcane.witchery.data.LegacyLootSlots;
import art.arcane.witchery.data.WitcheryLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

final class MiningPredictions {
    private MiningPredictions() {}

    static boolean addOre(Predictions.Kind kind, RandomSource random, BlockState state, List<ItemStack> drops,
                          double chance, boolean veryOld) {
        boolean stone = state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE);
        boolean eligible = switch (kind) {
            case IRON -> state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE) || veryOld && stone;
            case DIAMOND, EMERALD -> stone;
            case COAL -> state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE) || veryOld && stone;
            default -> false;
        };
        if (!eligible || random.nextDouble() >= chance) return false;

        ItemStack bonus = switch (kind) {
            case IRON -> new ItemStack(Blocks.IRON_ORE, 9 + random.nextInt(12));
            case DIAMOND -> new ItemStack(Items.DIAMOND);
            case EMERALD -> new ItemStack(Items.EMERALD);
            case COAL -> new ItemStack(Items.COAL, 11 + random.nextInt(10));
            default -> ItemStack.EMPTY;
        };
        drops.add(bonus);
        return true;
    }

    static boolean buryChest(ServerPlayer player, BlockPos pos, BlockState state, double chance) {
        if (!state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.DIRT) && !state.is(Blocks.COARSE_DIRT)
                && !state.is(Blocks.PODZOL) && !state.is(Blocks.MYCELIUM) && !state.is(Blocks.SAND)
                && !state.is(Blocks.RED_SAND)) return false;

        ServerLevel level = player.serverLevel();
        BlockPos below = pos.below();
        if (pos.getY() <= level.getMinBuildHeight() + 6) return false;
        RandomSource random = level.getRandom();
        if (random.nextDouble() >= chance || BrewTerrain.isProtectedTerrain(level.getBlockState(below))
                || level.isEmptyBlock(below.below())) return false;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.isEmptyBlock(below.relative(side))) return false;
        }
        if (!BrewTerrain.placeGroup(level, player, Direction.UP,
                () -> level.setBlockAndUpdate(below, Blocks.CHEST.defaultBlockState())
                        && level.getBlockEntity(below) instanceof ChestBlockEntity)
                || !(level.getBlockEntity(below) instanceof ChestBlockEntity chest)) return false;

        var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(below))
                .create(LootContextParamSets.CHEST);
        var table = level.getServer().getLootData().getLootTable(WitcheryLootTables.MINESHAFT_CORRIDOR);
        LegacyLootSlots.fillUsingRandom(table, chest, params, random);
        chest.setChanged();
        return true;
    }
}
