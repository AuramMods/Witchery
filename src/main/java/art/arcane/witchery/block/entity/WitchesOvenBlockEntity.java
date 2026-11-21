package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.WitchesOvenBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.menu.WitchesOvenMenu;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class WitchesOvenBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int FUME_SLOT = 3;
    public static final int JAR_SLOT = 4;
    public static final int SLOT_COUNT = 5;

    public static final int BURN_PROGRESS = 0;
    public static final int COOK_PROGRESS = 1;
    public static final int PROGRESS_COUNT = 2;
    public static final int PROGRESS_SCALE = 1000;

    private static final int COOK_TICKS = 180;
    private static final int TICKS_PER_FUNNEL = 20;
    private static final int[] TOP_SLOTS = {INPUT_SLOT, JAR_SLOT};
    private static final int[] BOTTOM_SLOTS = {JAR_SLOT, FUEL_SLOT};
    private static final int[] SIDE_SLOTS = {FUME_SLOT, RESULT_SLOT, JAR_SLOT, FUEL_SLOT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final RecipeManager.CachedCheck<Container, SmeltingRecipe> recipes = RecipeManager.createCheck(RecipeType.SMELTING);
    private final Map<ResourceLocation, Integer> recipesUsed = new HashMap<>();
    private final Map<Direction, LazyOptional<IItemHandlerModifiable>> sidedHandlers = new EnumMap<>(Direction.class);
    private int burnTime;
    private int burnDuration;
    private int cookTime;
    private int cookDuration = COOK_TICKS;

    private final ContainerData progress = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case BURN_PROGRESS -> scaleProgress(burnTime, burnDuration);
                case COOK_PROGRESS -> scaleProgress(cookTime, cookDuration);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return PROGRESS_COUNT;
        }
    };

    public WitchesOvenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static int scaleProgress(int current, int duration) {
        if (current <= 0 || duration <= 0) {
            return 0;
        }
        return (int) Math.max(1, Math.min(PROGRESS_SCALE, (long) current * PROGRESS_SCALE / duration));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WitchesOvenBlockEntity oven) {
        int previousBurn = oven.burnTime;
        int previousCook = oven.cookTime;
        if (oven.burnTime > 0) {
            oven.burnTime--;
        }

        ItemStack input = oven.getItem(INPUT_SLOT);
        ItemStack fuel = oven.getItem(FUEL_SLOT);
        boolean inventoryChanged = false;
        SmeltingRecipe recipe = !input.isEmpty() && (oven.burnTime > 0 || !fuel.isEmpty())
                ? oven.recipes.getRecipeFor(oven, level).orElse(null) : null;
        ItemStack result = recipe == null ? ItemStack.EMPTY : recipe.assemble(oven, level.registryAccess());

        boolean canCook = recipe != null && isOvenResult(result) && oven.canStore(RESULT_SLOT, result);
        if (canCook && oven.burnTime == 0) {
            inventoryChanged = oven.consumeFuel(fuel);
        }
        if (canCook && oven.burnTime > 0) {
            Direction facing = state.getValue(WitchesOvenBlock.FACING);
            Direction side = facing.getClockWise();
            double left = funnelBonus(level, pos.relative(side), facing);
            double right = funnelBonus(level, pos.relative(side.getOpposite()), facing);
            double top = funnelBonus(level, pos.above(), facing);
            int funnels = (left > 0 ? 1 : 0) + (right > 0 ? 1 : 0) + (top > 0 ? 1 : 0);
            oven.cookDuration = COOK_TICKS - TICKS_PER_FUNNEL * funnels;
            if (++oven.cookTime >= oven.cookDuration) {
                oven.store(RESULT_SLOT, result);
                if (recipe.getExperience() > 0) {
                    oven.recipesUsed.merge(recipe.getId(), 1, Integer::sum);
                }
                oven.collectFume(level, input, 0.3 + left + right);
                input.shrink(1);
                oven.cookTime = 0;
                inventoryChanged = true;
            }
        } else {
            oven.cookTime = 0;
        }

        boolean lit = oven.burnTime > 0;
        if (state.getValue(WitchesOvenBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(WitchesOvenBlock.LIT, lit), Block.UPDATE_ALL);
        }
        if (inventoryChanged) {
            oven.setChanged();
        } else if (previousBurn != oven.burnTime || previousCook != oven.cookTime) {
            level.blockEntityChanged(pos);
        }
    }

    public static boolean hasOvenRecipe(Level level, ItemStack input) {
        SimpleContainer container = new SimpleContainer(input);
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level)
                .map(recipe -> isOvenResult(recipe.assemble(container, level.registryAccess())))
                .orElse(false);
    }

    private static boolean isOvenResult(ItemStack result) {
        return result.is(Items.CHARCOAL) || result.is(Items.COAL)
                || result.isEdible() || result.is(WitcheryIngredient.WOOD_ASH.item());
    }

    private boolean consumeFuel(ItemStack fuel) {
        int duration = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
        if (duration <= 0) {
            return false;
        }
        burnTime = burnDuration = duration;
        ItemStack remainder = fuel.getCraftingRemainingItem();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            items.set(FUEL_SLOT, remainder);
        }
        return true;
    }

    private static double funnelBonus(Level level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(WitchesOvenBlock.FACING) || state.getValue(WitchesOvenBlock.FACING) != facing) {
            return 0;
        }
        if (state.is(WitcheryBlocks.getRequired("fumefunnel").get())) {
            return 0.25;
        }
        if (state.is(WitcheryBlocks.getRequired("filteredfumefunnel").get())) {
            return WitcheryServerConfig.DOUBLE_FUME_FILTER_CHANCE.get() ? 0.8 : 0.3;
        }
        return 0;
    }

    private void collectFume(Level level, ItemStack input, double chance) {
        ItemStack jars = getItem(JAR_SLOT);
        if (!jars.is(WitcheryIngredient.CLAY_JAR.item()) || level.random.nextDouble() > Math.min(1, chance)) {
            return;
        }
        WitcheryIngredient ingredient = fumeFor(input);
        if (ingredient == null) {
            return;
        }
        ItemStack fume = new ItemStack(ingredient.item());
        if (canStore(FUME_SLOT, fume)) {
            store(FUME_SLOT, fume);
            jars.shrink(1);
        }
    }

    @Nullable
    private static WitcheryIngredient fumeFor(ItemStack input) {
        if (input.is(Items.OAK_SAPLING)) {
            return WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE;
        }
        if (input.is(Items.SPRUCE_SAPLING)) {
            return WitcheryIngredient.HINT_OF_REBIRTH;
        }
        if (input.is(Items.BIRCH_SAPLING)) {
            return WitcheryIngredient.BREATH_OF_THE_GODDESS;
        }
        if (input.is(Items.ACACIA_SAPLING) || input.is(Items.DARK_OAK_SAPLING)) {
            return null;
        }
        if (input.is(WitcheryBlocks.getRequired("witchsapling").get().asItem())) {
            return switch (WitchWoodType.fromStack(input)) {
                case ALDER -> WitcheryIngredient.REEK_OF_MISFORTUNE;
                case HAWTHORN -> WitcheryIngredient.ODOUR_OF_PURITY;
                case ROWAN -> WitcheryIngredient.WHIFF_OF_MAGIC;
            };
        }
        return WitcheryIngredient.FOUL_FUME;
    }

    private boolean canStore(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack stored = getItem(slot);
        return (stored.isEmpty() || ItemStack.isSameItemSameTags(stored, stack))
                && stored.getCount() + stack.getCount() <= Math.min(getMaxStackSize(), stack.getMaxStackSize());
    }

    private void store(int slot, ItemStack stack) {
        ItemStack stored = getItem(slot);
        if (stored.isEmpty()) {
            items.set(slot, stack);
        } else {
            stored.grow(stack.getCount());
        }
    }

    public void awardSmeltingExperience(ServerPlayer player) {
        if (recipesUsed.isEmpty()) return;
        ServerLevel server = player.serverLevel();
        recipesUsed.forEach((id, count) -> server.getRecipeManager().byKey(id).ifPresent(usedRecipe -> {
            if (usedRecipe instanceof SmeltingRecipe smelting) {
                float experience = count * smelting.getExperience();
                int points = Mth.floor(experience);
                float fraction = experience - points;
                if (fraction > 0 && server.random.nextFloat() < fraction) points++;
                ExperienceOrb.award(server, player.position(), points);
            }
        }));
        recipesUsed.clear();
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        for (Tag slotTag : tag.getList("Items", Tag.TAG_COMPOUND)) {
            CompoundTag stackTag = (CompoundTag) slotTag;
            int slot = stackTag.getByte("Slot") & 255;
            if (slot < SLOT_COUNT) {
                items.set(slot, WitcheryIngredient.loadInventoryStack(stackTag));
            }
        }
        burnTime = Math.max(0, tag.getInt("BurnTime"));
        burnDuration = Math.max(burnTime, tag.getInt("BurnDuration"));
        cookTime = Math.max(0, Math.min(COOK_TICKS, tag.getInt("CookTime")));
        recipesUsed.clear();
        CompoundTag used = tag.getCompound("RecipesUsed");
        for (String id : used.getAllKeys()) {
            ResourceLocation recipe = ResourceLocation.tryParse(id);
            int count = used.getInt(id);
            if (recipe != null && count > 0) recipesUsed.put(recipe, count);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnDuration", burnDuration);
        tag.putInt("CookTime", cookTime);
        if (!recipesUsed.isEmpty()) {
            CompoundTag used = new CompoundTag();
            recipesUsed.forEach((id, count) -> used.putInt(id.toString(), count));
            tag.put("RecipesUsed", used);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.witchery.witches_oven");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new WitchesOvenMenu(id, inventory, this, progress);
    }

    @Override
    public int getContainerSize() {
        return items.size();
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
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            if (slot == INPUT_SLOT && items.get(slot).isEmpty()) {
                cookTime = 0;
            }
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == INPUT_SLOT && !ItemStack.isSameItemSameTags(items.get(slot), stack)) {
            cookTime = 0;
        }
        int maxCount = Math.min(getMaxStackSize(), stack.getMaxStackSize());
        if (stack.getCount() > maxCount) {
            stack.setCount(maxCount);
        }
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
        cookTime = 0;
        setChanged();
    }

    public static boolean acceptsItem(int slot, ItemStack stack) {
        return switch (slot) {
            case INPUT_SLOT -> !stack.is(WitcheryIngredient.CLAY_JAR.item());
            case FUEL_SLOT -> ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
            case JAR_SLOT -> stack.is(WitcheryIngredient.CLAY_JAR.item());
            default -> false;
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return acceptsItem(slot, stack);
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
        return switch (side) {
            case UP -> false;
            case DOWN -> slot == FUEL_SLOT && stack.is(Items.BUCKET);
            default -> slot == RESULT_SLOT || slot == FUME_SLOT;
        };
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (!isRemoved() && side != null && capability == ForgeCapabilities.ITEM_HANDLER) {
            return sidedHandlers.computeIfAbsent(side, face -> LazyOptional.of(() -> new SidedInvWrapper(this, face))).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        sidedHandlers.values().forEach(LazyOptional::invalidate);
        sidedHandlers.clear();
    }
}
