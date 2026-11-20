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
        if (!(level instanceof ServerLevel server) || !level.dimension().equals(WitcheryDimensions.TORMENT)) return;
        if (chest.ticks++ % REFILL_INTERVAL == 0 && chest.isEmpty()) {
            chest.refill(server, pos);
        }
        level.getChunkAt(pos).setUnsaved(true);
    }

    private void refill(ServerLevel level, BlockPos pos) {
        LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .create(LootContextParamSets.CHEST);
        List<ItemStack> rolls = level.getServer().getLootData().getLootTable(BuiltInLootTables.SIMPLE_DUNGEON).getRandomItems(params);
        int count = Math.min(rolls.size(), 2 + level.random.nextInt(4));
        for (int index = 0; index < count; index++) {
            int slot;
            do {
                slot = level.random.nextInt(getContainerSize());
            } while (!getItem(slot).isEmpty());
            setItem(slot, rolls.remove(level.random.nextInt(rolls.size())));
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
