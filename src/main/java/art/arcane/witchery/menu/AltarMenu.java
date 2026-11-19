package art.arcane.witchery.menu;

import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.registry.WitcheryMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

public final class AltarMenu extends AbstractContainerMenu {
    @Nullable
    private final AltarBlockEntity altar;
    @Nullable
    private final BlockPos origin;
    private final DataSlot powerLow;
    private final DataSlot powerHigh;
    private final DataSlot capacityLow;
    private final DataSlot capacityHigh;
    private final DataSlot recharge;

    public AltarMenu(int id, Inventory inventory) {
        this(id, null, null);
    }

    public AltarMenu(int id, @Nullable AltarBlockEntity altar, @Nullable BlockPos origin) {
        super(WitcheryMenus.ALTAR.get(), id);
        this.altar = altar;
        this.origin = origin;
        powerLow = track(() -> Math.round(altar.power()), 0);
        powerHigh = track(() -> Math.round(altar.power()), 16);
        capacityLow = track(() -> altar.capacity(), 0);
        capacityHigh = track(() -> altar.capacity(), 16);
        recharge = track(() -> altar.rechargeMultiplier(), 0);
    }

    private DataSlot track(IntSupplier source, int shift) {
        return addDataSlot(altar == null ? DataSlot.standalone() : new DataSlot() {
            @Override
            public int get() {
                return (source.getAsInt() >>> shift) & 0xffff;
            }

            @Override
            public void set(int value) {
            }
        });
    }

    public int power() {
        return (powerHigh.get() << 16) | (powerLow.get() & 0xffff);
    }

    public int capacity() {
        return (capacityHigh.get() << 16) | (capacityLow.get() & 0xffff);
    }

    public int rechargeMultiplier() {
        return recharge.get();
    }

    @Override
    public boolean stillValid(Player player) {
        return altar == null || origin != null && player.level() instanceof ServerLevel server
                && player.distanceToSqr(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5) <= 64
                && altar.isCore(server);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
