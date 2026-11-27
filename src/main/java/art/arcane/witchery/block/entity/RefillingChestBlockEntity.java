package art.arcane.witchery.block.entity;

import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RefillingChestBlockEntity extends ChestBlockEntity {
    private static final long REFILL_INTERVAL = 72_000;
    private long ticks;

    public RefillingChestBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.REFILLING_CHEST.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RefillingChestBlockEntity chest) {
        if (!(level instanceof ServerLevel server)) return;
        if (chest.ticks == 0) chest.tryRefill(server, pos);
        else if (chest.ticks == Long.MAX_VALUE) chest.ticks = 1;
        if (++chest.ticks % REFILL_INTERVAL == 0) chest.tryRefill(server, pos);
        level.getChunkAt(pos).setUnsaved(true);
    }

    private void tryRefill(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(WitcheryDimensions.TORMENT) || !isEmpty()) return;
        int requested = 2 + level.random.nextInt(4);
        LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .create(LootContextParamSets.CHEST);
        List<ItemStack> rolls = level.getServer().getLootData().getLootTable(BuiltInLootTables.SIMPLE_DUNGEON).getRandomItems(params);
        int count = Math.min(rolls.size(), requested);
        for (int index = 0; index < count; index++) {
            ItemStack rolled = rolls.remove(level.random.nextInt(rolls.size()));
            setItem(level.random.nextInt(getContainerSize()), rolled);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ticks = Math.max(0, tag.getLong("WITCLifeTicks"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("WITCLifeTicks", ticks);
    }
}
