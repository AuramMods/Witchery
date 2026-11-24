package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.SpinningWheelBlock;
import art.arcane.witchery.crafting.SpinningRecipe;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.menu.SpinningWheelMenu;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import static art.arcane.witchery.crafting.SpinningRecipe.FIBRE_SLOT;
import static art.arcane.witchery.crafting.SpinningRecipe.OUTPUT_SLOT;
import static art.arcane.witchery.crafting.SpinningRecipe.SLOT_COUNT;

public final class SpinningWheelBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int COOK_TICKS = 300;
    public static final int DATA_COUNT = 2;
    public static final int PROGRESS = 0;
    public static final int POWERED = 1;
    private static final float POWER_PER_TICK = 0.6F;
    private static final int[] TOP_SLOTS = {FIBRE_SLOT};
    private static final int[] BOTTOM_SLOTS = {4, 1, 3};
    private static final int[] SIDE_SLOTS = {3, OUTPUT_SLOT, 4, 1};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final Map<Direction, LazyOptional<IItemHandlerModifiable>> handlers = new EnumMap<>(Direction.class);
    private final RecipeManager.CachedCheck<Container, SpinningRecipe> recipes = RecipeManager.createCheck(WitcheryRecipes.SPINNING.get());
    private final ContainerData progress = new ContainerData() {
        @Override
        public int get(int index) {
            return index == PROGRESS ? cookTime : (powered ? 1 : 0);
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };
    private int cookTime;
    private boolean powered;
    @Nullable
    private ResourceLocation activeRecipe;
    @Nullable
    private AltarBlockEntity altar;

    public SpinningWheelBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.SPINNING_WHEEL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpinningWheelBlockEntity wheel) {
        if (level instanceof ServerLevel server) {
            wheel.spin(server, pos, state);
        }
    }

    private void spin(ServerLevel level, BlockPos pos, BlockState state) {
        int oldTime = cookTime;
        boolean wasPowered = powered;
        SpinningRecipe recipe = items.get(FIBRE_SLOT).isEmpty() ? null : recipes.getRecipeFor(this, level).orElse(null);
        boolean ready = recipe != null && canStore(recipe.result());
        if (level.getGameTime() % 40 == 0 || ready && altar == null
                || altar != null && (altar.isRemoved() || !altar.isInRange(pos) || !altar.isCore(level))) {
            altar = AltarPower.nearest(level, pos);
            if (!ready) powered = altar != null;
        }
        boolean spinning = false;
        if (!ready) {
            cookTime = 0;
            activeRecipe = null;
        } else {
            if (activeRecipe != null && !activeRecipe.equals(recipe.id())) {
                cookTime = 0;
            }
            activeRecipe = recipe.id();
            powered = altar != null && altar.consumePower(POWER_PER_TICK);
            if (powered) {
                if (++cookTime >= COOK_TICKS) {
                    finish(recipe);
                    cookTime = 0;
                } else {
                    spinning = true;
                }
            }
        }
        if (state.getValue(SpinningWheelBlock.SPINNING) != spinning) {
            level.setBlock(pos, state.setValue(SpinningWheelBlock.SPINNING, spinning), Block.UPDATE_ALL);
        }
        if (oldTime != cookTime || wasPowered != powered) {
            level.blockEntityChanged(pos);
        }
    }

    private boolean canStore(ItemStack result) {
        ItemStack output = items.get(OUTPUT_SLOT);
        return (output.isEmpty() || ItemStack.isSameItemSameTags(output, result))
                && output.getCount() + result.getCount() <= Math.min(getMaxStackSize(), result.getMaxStackSize());
    }

    private void finish(SpinningRecipe recipe) {
        int[] slots = recipe.matchModifiers(this);
        if (slots == null) {
            return;
        }
        if (items.get(OUTPUT_SLOT).isEmpty()) {
            items.set(OUTPUT_SLOT, recipe.result().copy());
        } else {
            items.get(OUTPUT_SLOT).grow(recipe.result().getCount());
        }
        items.get(FIBRE_SLOT).shrink(recipe.fibreCount());
        for (int slot : slots) {
            items.get(slot).shrink(1);
        }
        setChanged();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.witchery.spinningwheel");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SpinningWheelMenu(id, inventory, this, progress);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        stack.setCount(Math.min(stack.getCount(), Math.min(getMaxStackSize(), stack.getMaxStackSize())));
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != OUTPUT_SLOT;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case UP -> TOP_SLOTS;
            case DOWN -> BOTTOM_SLOTS;
            default -> SIDE_SLOTS;
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side.getAxis().isHorizontal() && slot == OUTPUT_SLOT;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (!isRemoved() && side != null && capability == ForgeCapabilities.ITEM_HANDLER) {
            return handlers.computeIfAbsent(side, direction -> LazyOptional.of(() -> new SidedInvWrapper(this, direction))).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handlers.values().forEach(LazyOptional::invalidate);
        handlers.clear();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        for (Tag entry : tag.getList("Items", Tag.TAG_COMPOUND)) {
            CompoundTag stack = (CompoundTag) entry;
            int slot = stack.getByte("Slot") & 255;
            if (slot < SLOT_COUNT) {
                items.set(slot, WitcheryIngredient.loadInventoryStack(stack));
            }
        }
        cookTime = Mth.clamp(tag.getInt("CookTime"), 0, COOK_TICKS - 1);
        powered = tag.getInt("PowerLevel") > 0;
        activeRecipe = tag.contains("Recipe", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("Recipe")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("CookTime", cookTime);
        tag.putShort("PowerLevel", (short) (powered ? 1 : 0));
        if (activeRecipe != null) {
            tag.putString("Recipe", activeRecipe.toString());
        }
    }
}
