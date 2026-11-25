package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.DistilleryBlock;
import art.arcane.witchery.crafting.DistillingRecipe;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.menu.DistilleryMenu;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public final class DistilleryBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int FIRST_INPUT = 0;
    public static final int SECOND_INPUT = 1;
    public static final int JARS = 2;
    public static final int FIRST_RESULT = 3;
    public static final int SLOT_COUNT = FIRST_RESULT + DistillingRecipe.OUTPUTS;
    public static final int COOK_TICKS = 800;
    public static final int DATA_COUNT = 2;
    public static final int PROGRESS = 0;
    public static final int POWERED = 1;

    private static final float POWER_PER_TICK = 0.6F;
    private static final int GLOW_UPDATE_THROTTLE = 20;
    private static final int[] INPUT_SLOTS = {FIRST_INPUT, SECOND_INPUT, JARS};
    private static final int[] SIDE_SLOTS = {FIRST_INPUT, SECOND_INPUT, JARS, 3, 4, 5, 6};
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final Map<Direction, LazyOptional<IItemHandlerModifiable>> handlers = new EnumMap<>(Direction.class);
    private final RecipeManager.CachedCheck<Container, DistillingRecipe> recipes =
            RecipeManager.createCheck(WitcheryRecipes.DISTILLING.get());
    private final AltarPower.WorkstationSource altarSource = new AltarPower.WorkstationSource();
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
    private long lastGlowUpdate;
    private boolean glowUpdatePending;

    public DistilleryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DistilleryBlockEntity distillery) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        int oldCookTime = distillery.cookTime;
        boolean wasPowered = distillery.powered;
        DistillingRecipe recipe = distillery.recipes.getRecipeFor(distillery, level).orElse(null);
        boolean ready = recipe != null && distillery.canStore(recipe);
        if (!ready) {
            distillery.powered = distillery.altarSource.idleTick(server, pos, distillery.powered);
            distillery.cookTime = 0;
        } else {
            distillery.powered = distillery.altarSource.activeTick(server, pos, POWER_PER_TICK);
            if (distillery.powered) {
                if (++distillery.cookTime >= COOK_TICKS) {
                    distillery.finish(recipe);
                    distillery.cookTime = 0;
                }
            }
        }
        distillery.syncVisualState(server, pos, state, oldCookTime > 0, wasPowered);
        if (oldCookTime != distillery.cookTime) {
            level.blockEntityChanged(pos);
        }
    }

    private void syncVisualState(ServerLevel level, BlockPos pos, BlockState state, boolean wasCooking, boolean wasPowered) {
        long ticks = altarSource.loadedTicks();
        boolean cooking = cookTime > 0;
        boolean lit = state.getValue(DistilleryBlock.LIT);
        boolean desiredGlow = cooking && powered;
        if (wasPowered != powered) glowUpdatePending = true;
        if (wasCooking != cooking || ticks == 1 && lit != desiredGlow
                || glowUpdatePending && ticks - lastGlowUpdate > GLOW_UPDATE_THROTTLE) {
            lit = desiredGlow;
            lastGlowUpdate = ticks;
            glowUpdatePending = false;
        }
        int visibleJars = Math.min(4, getItem(JARS).getCount());
        if (state.getValue(DistilleryBlock.LIT) != lit || state.getValue(DistilleryBlock.VISIBLE_JARS) != visibleJars) {
            level.setBlock(pos, state.setValue(DistilleryBlock.LIT, lit)
                    .setValue(DistilleryBlock.VISIBLE_JARS, visibleJars), Block.UPDATE_ALL);
        }
    }

    private boolean canStore(DistillingRecipe recipe) {
        for (int i = 0; i < DistillingRecipe.OUTPUTS; i++) {
            ItemStack output = recipe.outputs().get(i);
            ItemStack existing = items.get(FIRST_RESULT + i);
            if (!output.isEmpty() && (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, output)
                    || existing.getCount() + output.getCount() > Math.min(getMaxStackSize(), output.getMaxStackSize()))) {
                return false;
            }
        }
        return true;
    }

    private void finish(DistillingRecipe recipe) {
        for (int i = 0; i < DistillingRecipe.OUTPUTS; i++) {
            ItemStack output = recipe.outputs().get(i);
            if (output.isEmpty()) {
                continue;
            }
            int slot = FIRST_RESULT + i;
            ItemStack existing = items.get(slot);
            if (existing.isEmpty()) {
                items.set(slot, output.copy());
            } else {
                existing.grow(output.getCount());
            }
        }
        items.get(FIRST_INPUT).shrink(1);
        items.get(SECOND_INPUT).shrink(1);
        items.get(JARS).shrink(recipe.jars());
        setChanged();
    }

    public static boolean acceptsItem(int slot, ItemStack stack) {
        return switch (slot) {
            case FIRST_INPUT, SECOND_INPUT -> !stack.is(WitcheryIngredient.CLAY_JAR.item());
            case JARS -> stack.is(WitcheryIngredient.CLAY_JAR.item());
            default -> false;
        };
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
        return acceptsItem(slot, stack);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction.getAxis().isVertical() ? INPUT_SLOTS : SIDE_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return acceptsItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side.getAxis().isHorizontal() && slot >= FIRST_RESULT;
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
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("CookTime", cookTime);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.witchery.distilleryidle");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new DistilleryMenu(id, inventory, this, progress);
    }
}
