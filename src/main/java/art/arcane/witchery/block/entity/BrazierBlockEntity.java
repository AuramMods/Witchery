package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.BrazierBlock;
import art.arcane.witchery.crafting.BrazierRecipe;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BrazierBlockEntity extends BlockEntity {
    private static final int INGREDIENT_SLOTS = 3;
    private static final int LEGACY_SMOKE_PARTICLE_ID = 8;
    private static final int LEGACY_FIZZ_SOUND_ID = 2;
    private final AltarPower.WorkstationSource altarSource = new AltarPower.WorkstationSource();
    private final ItemStackHandler ingredients = new ItemStackHandler(INGREDIENT_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (!loading) syncOfferings();
        }
    };
    private LazyOptional<IItemHandler> inventory = LazyOptional.of(() -> ingredients);
    private boolean loading;
    private boolean ignited;
    private boolean powered;
    private boolean redstone;
    private int displayedOfferings;
    private long ticks;
    private long cookTime;
    private long storage;
    private Optional<BrazierRecipe> activeRecipe = Optional.empty();
    private boolean recipeResolved;

    public BrazierBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.BRAZIER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrazierBlockEntity brazier) {
        if (level instanceof ServerLevel server) {
            brazier.ticks++;
            brazier.burn(server, pos);
        }
    }

    private void burn(ServerLevel level, BlockPos pos) {
        setFlame(ignited && hasAllIngredients());
        if (!ignited) {
            updatePower(altarSource.idleTick(level, pos, powered));
            return;
        }
        if (!recipeResolved) {
            activeRecipe = BrazierRecipe.match(ingredients);
            recipeResolved = true;
        }
        if (activeRecipe.isEmpty()) {
            clearBatch();
            updatePower(altarSource.idleTick(level, pos, powered));
            WitcheryNetwork.sendParticlesAround(level, LEGACY_SMOKE_PARTICLE_ID, LEGACY_FIZZ_SOUND_ID,
                    Vec3.atBottomCenterOf(pos.above()), 0.5, 0.5, 8);
            return;
        }
        BrazierRecipe recipe = activeRecipe.get();
        updatePower(altarSource.activeTick(level, pos, recipe.needsPower() ? 1 : 0));
        if (!powered) return;

        cookTime++;
        long elapsed = cookTime - recipe.burnTicks();
        if (elapsed >= 0 && elapsed / 400 >= storage) {
            recipe.finish(level, pos);
            // The last tick consumes the offerings and leaves ash for the next tick to clear.
            clearOfferings();
        } else {
            storage += recipe.burn(level, pos, ticks);
            setChanged();
        }
    }

    private void updatePower(boolean hasPower) {
        if (powered != hasPower) {
            powered = hasPower;
            setChanged();
        }
    }

    private boolean hasAllIngredients() {
        for (int slot = 0; slot < INGREDIENT_SLOTS; slot++) {
            if (ingredients.getStackInSlot(slot).isEmpty()) return false;
        }
        return true;
    }

    public boolean isEmpty() {
        for (int slot = 0; slot < INGREDIENT_SLOTS; slot++) {
            if (!ingredients.getStackInSlot(slot).isEmpty()) return false;
        }
        return true;
    }

    public boolean insert(ItemStack stack) {
        for (int slot = 0; slot < INGREDIENT_SLOTS; slot++) {
            if (ingredients.insertItem(slot, stack.copyWithCount(1), false).isEmpty()) return true;
        }
        return false;
    }

    public boolean ignite() {
        if (ignited || isEmpty()) return false;
        ignited = true;
        setFlame(hasAllIngredients());
        setChanged();
        return true;
    }

    public void updateSignal(boolean signal) {
        if (redstone == signal) return;
        redstone = signal;
        if (signal) ignite();
        setChanged();
    }

    public void clearBatch() {
        ignited = false;
        clearOfferings();
    }

    private void clearOfferings() {
        cookTime = 0;
        activeRecipe = Optional.empty();
        recipeResolved = false;
        ingredients.setSize(INGREDIENT_SLOTS);
        setFlame(false);
        syncOfferings();
    }

    public void dropContents(Level level) {
        if (ignited && hasAllIngredients()) {
            drop(level, new ItemStack(WitcheryIngredient.WOOD_ASH.item()));
            return;
        }
        for (int slot = 0; slot < INGREDIENT_SLOTS; slot++) drop(level, ingredients.getStackInSlot(slot));
        if (ignited) drop(level, new ItemStack(WitcheryIngredient.WOOD_ASH.item()));
    }

    public int comparatorSignal() {
        // One item per slot, even when lit, is below the threshold for strength two.
        return !ignited && isEmpty() ? 0 : 1;
    }

    private void drop(Level level, ItemStack stack) {
        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    private void setFlame(boolean flame) {
        if (level instanceof ServerLevel && getBlockState().getValue(BrazierBlock.LIT) != flame) {
            level.setBlock(worldPosition, getBlockState().setValue(BrazierBlock.LIT, flame), Block.UPDATE_CLIENTS);
        }
    }

    private void syncOfferings() {
        setChanged();
        if (level instanceof ServerLevel server) {
            BlockState state = getBlockState();
            server.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public int displayedOfferings() {
        return displayedOfferings;
    }

    @Override
    public CompoundTag getUpdateTag() {
        int count = 0;
        for (int slot = 0; slot < INGREDIENT_SLOTS; slot++) {
            if (!ingredients.getStackInSlot(slot).isEmpty()) count++;
        }
        CompoundTag tag = new CompoundTag();
        tag.putByte("Offerings", (byte) count);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        displayedOfferings = Mth.clamp(tag.getByte("Offerings"), 0, INGREDIENT_SLOTS);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) handleUpdateTag(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag entries = new ListTag();
        for (int slot = 0; slot < INGREDIENT_SLOTS; slot++) {
            ItemStack stack = ingredients.getStackInSlot(slot);
            if (!stack.isEmpty()) saveSlot(entries, slot, stack);
        }
        if (ignited) saveSlot(entries, INGREDIENT_SLOTS, new ItemStack(WitcheryIngredient.WOOD_ASH.item()));
        tag.put("Items", entries);
        tag.putLong("CookTime", cookTime);
        tag.putInt("PowerLevel", powered ? 1 : 0);
        tag.putLong("PowerStorage", storage);
        tag.putBoolean("Powered", redstone);
    }

    private static void saveSlot(ListTag entries, int slot, ItemStack stack) {
        CompoundTag entry = stack.save(new CompoundTag());
        entry.putByte("Slot", (byte) slot);
        entries.add(entry);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loading = true;
        ingredients.setSize(INGREDIENT_SLOTS);
        ignited = false;
        ListTag entries = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            int slot = Byte.toUnsignedInt(entry.getByte("Slot"));
            ItemStack stack = WitcheryIngredient.loadInventoryStack(entry);
            if (slot < INGREDIENT_SLOTS && !stack.isEmpty()) ingredients.setStackInSlot(slot, stack.copyWithCount(1));
            if (slot == INGREDIENT_SLOTS && stack.is(WitcheryIngredient.WOOD_ASH.item())) ignited = true;
        }
        loading = false;
        cookTime = ignited ? Math.max(0, tag.getLong("CookTime")) : 0;
        powered = tag.getInt("PowerLevel") > 0;
        storage = Math.max(0, tag.getLong("PowerStorage"));
        redstone = tag.getBoolean("Powered");
        recipeResolved = false;
        activeRecipe = Optional.empty();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !isRemoved()) return inventory.cast();
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventory.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        inventory = LazyOptional.of(() -> ingredients);
    }
}
