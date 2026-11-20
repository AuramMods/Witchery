package art.arcane.witchery.block.entity;

import art.arcane.witchery.brewing.KettleRecipe;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class KettleBlockEntity extends BlockEntity {
    private static final int BUCKET = 1000;
    private static final int BREW_SLOT = KettleRecipe.INGREDIENT_SLOTS;
    private static final int BOTTLE_SLOT = BREW_SLOT + 1;
    private final NonNullList<ItemStack> contents = NonNullList.withSize(BOTTLE_SLOT + 1, ItemStack.EMPTY);
    private final List<ItemStack> ingredients = contents.subList(0, BREW_SLOT);
    private boolean ruined;
    private int color;
    @Nullable private ResourceLocation finishedRecipeId;

    private final FluidTank tank = new FluidTank(BUCKET, resource -> resource.getFluid() == Fluids.WATER) {
        @Override
        protected void onContentsChanged() {
            resetBatch();
            update();
        }
    };
    private final ItemStackHandler inventory = new ItemStackHandler(contents) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == BOTTLE_SLOT ? stack.is(Items.GLASS_BOTTLE)
                    : slot < BREW_SLOT && !stack.is(Items.GLASS_BOTTLE) && canAddIngredients() && contents.get(slot).isEmpty();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == BREW_SLOT ? bottleBrew(amount, true, simulate) : ItemStack.EMPTY;
        }

        @Override
        protected void onContentsChanged(int slot) {
            update();
        }
    };
    private LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> tank);
    private LazyOptional<IItemHandler> inventoryCapability = LazyOptional.of(() -> inventory);

    public KettleBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.KETTLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, KettleBlockEntity kettle) {
        if (level instanceof ServerLevel server && level.getGameTime() % 20 == 0) {
            kettle.brew(server);
        }
    }

    private void brew(ServerLevel server) {
        boolean hasIngredients = hasIngredients();
        if (!isFull() || ruined || (!hasIngredients && !isReady())) {
            return;
        }
        if (!server.getBlockState(worldPosition.below()).is(BlockTags.FIRE)) {
            ruinBatch();
            return;
        }
        if (isReady()) {
            return;
        }
        KettleRecipe recipe = server.getRecipeManager().getAllRecipesFor(WitcheryRecipes.KETTLE.get()).stream()
                .filter(candidate -> candidate.canBrewIn(server) && candidate.matchesPartial(ingredients))
                .min((first, second) -> first.getId().compareTo(second.getId()))
                .orElse(null);
        if (recipe == null) {
            ruinBatch();
            return;
        }
        boolean changed = color != recipe.color();
        color = recipe.color();
        if (ingredients.stream().noneMatch(ItemStack::isEmpty) && canPower(server, recipe.power())) {
            contents.set(BREW_SLOT, recipe.result().copy());
            finishedRecipeId = recipe.id();
            clearIngredients();
            changed = true;
        }
        if (changed) {
            update();
        }
    }

    private boolean canPower(ServerLevel level, int power) {
        if (power == 0) {
            return true;
        }
        AltarBlockEntity altar = AltarPower.nearest(level, worldPosition);
        return altar != null && altar.consumePower(power);
    }

    public boolean isFull() {
        return tank.getFluidAmount() == BUCKET;
    }

    public boolean isReady() {
        return !ruined && !contents.get(BREW_SLOT).isEmpty();
    }

    public boolean isRuined() {
        return ruined;
    }

    public boolean hasIngredients() {
        return ingredients.stream().anyMatch(stack -> !stack.isEmpty());
    }

    public int bottleCount() {
        return contents.get(BOTTLE_SLOT).getCount();
    }

    public int liquidColor() {
        if (ruined) {
            return 0x7F5F00;
        }
        if (color == 0 || (!hasIngredients() && !isReady())) {
            return 0x375CC3;
        }
        return isReady() ? color : (color & 0xFEFEFE) >>> 1;
    }

    private boolean canAddIngredients() {
        return isFull() && !ruined && !isReady();
    }

    public ItemStack addDroppedStack(ItemStack stack) {
        if (stack.is(Items.GLASS_BOTTLE)) {
            return inventory.insertItem(BOTTLE_SLOT, stack, false);
        }
        if (!canAddIngredients()) {
            return stack;
        }
        for (int slot = 0; slot < BREW_SLOT; slot++) {
            if (contents.get(slot).isEmpty()) {
                return inventory.insertItem(slot, stack, false);
            }
        }
        ruinBatch();
        return ItemStack.EMPTY;
    }

    public ItemStack bottleBrew() {
        return bottleBrew(1, false, false);
    }

    public ItemStack bottleBrew(Player player) {
        if (!(player instanceof ServerPlayer server)) return ItemStack.EMPTY;
        KettleRecipe recipe = finishedRecipe();
        if (recipe != null && !recipe.canBottle(server)) return ItemStack.EMPTY;
        ItemStack bottled = extractBrew(1, false, false);
        if (bottled.isEmpty() || recipe == null) return bottled;
        int bonus = recipe.bottlingBonus();
        if (bonus <= 0) return bottled;

        var hat = WitchClothingItem.style(player.getItemBySlot(EquipmentSlot.HEAD));
        var robe = WitchClothingItem.style(player.getItemBySlot(EquipmentSlot.CHEST));
        boolean babaHat = hat == WitchClothingItem.Style.BABA_HAT;
        double primaryChance = hat == WitchClothingItem.Style.WITCH_HAT ? 0.35 : babaHat ? 0.25 : 0;
        boolean raising = bottled.is(WitcheryIngredient.BREW_OF_RAISING.item());
        if (robe == (raising ? WitchClothingItem.Style.NECROMANCER_ROBE : WitchClothingItem.Style.WITCH_ROBE)) {
            primaryChance += 0.35;
        }
        double toadChance = FamiliarBinding.hasBrewMastery(server) ? 0.05 : 0;
        primaryChance += toadChance;
        if (primaryChance > 0 && player.getRandom().nextDouble() <= primaryChance) bottled.grow(bonus);
        if (babaHat && player.getRandom().nextDouble() <= 0.25 + toadChance) bottled.grow(bonus);
        return bottled;
    }

    @Nullable
    private KettleRecipe finishedRecipe() {
        ItemStack brew = contents.get(BREW_SLOT);
        if (level == null || !isReady()) return null;
        var recipes = level.getRecipeManager();
        if (finishedRecipeId != null && recipes.byKey(finishedRecipeId).orElse(null) instanceof KettleRecipe recipe
                && ItemStack.isSameItemSameTags(recipe.result(), brew)) return recipe;
        return recipes.getAllRecipesFor(WitcheryRecipes.KETTLE.get()).stream()
                .filter(recipe -> ItemStack.isSameItemSameTags(recipe.result(), brew))
                .min((first, second) -> first.id().compareTo(second.id()))
                .orElse(null);
    }

    private ItemStack bottleBrew(int amount, boolean automated, boolean simulate) {
        KettleRecipe recipe = finishedRecipe();
        return recipe == null || recipe.canBottle(null) ? extractBrew(amount, automated, simulate) : ItemStack.EMPTY;
    }

    private ItemStack extractBrew(int amount, boolean automated, boolean simulate) {
        ItemStack brew = contents.get(BREW_SLOT);
        if (!isReady() || !isFull() || amount <= 0 || (automated && bottleCount() < brew.getCount())) {
            return ItemStack.EMPTY;
        }
        int count = Math.min(amount, brew.getCount());
        if (simulate) {
            return brew.copyWithCount(count);
        }
        ItemStack bottled = brew.split(count);
        if (automated) {
            contents.get(BOTTLE_SLOT).shrink(count);
        }
        if (brew.isEmpty()) {
            tank.setFluid(FluidStack.EMPTY);
            resetBatch();
        }
        update();
        return bottled;
    }

    private void ruinBatch() {
        ruined = true;
        color = 0;
        finishedRecipeId = null;
        contents.set(BREW_SLOT, ItemStack.EMPTY);
        update();
    }

    private void resetBatch() {
        clearIngredients();
        contents.set(BREW_SLOT, ItemStack.EMPTY);
        ruined = false;
        color = 0;
        finishedRecipeId = null;
    }

    private void clearIngredients() {
        for (int slot = 0; slot < BREW_SLOT; slot++) {
            contents.set(slot, ItemStack.EMPTY);
        }
    }

    private void update() {
        setChanged();
        if (level instanceof ServerLevel server) {
            BlockState state = getBlockState();
            server.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        contents.clear();
        for (Tag entry : tag.getList("Items", Tag.TAG_COMPOUND)) {
            CompoundTag stack = (CompoundTag) entry;
            int slot = stack.getByte("Slot") & 255;
            if (slot < contents.size()) {
                contents.set(slot, WitcheryIngredient.loadInventoryStack(stack));
            }
        }
        ruined = tag.getBoolean("Ruined");
        color = tag.getInt("LiquidColor");
        tank.setFluid(FluidStack.EMPTY);
        if (tag.contains("Tank", Tag.TAG_COMPOUND)) {
            tank.readFromNBT(tag.getCompound("Tank"));
        } else if (tag.getString("FluidName").equals("water") || tag.getString("FluidName").equals("minecraft:water")) {
            int amount = Math.min(BUCKET, tag.getInt("Amount"));
            if (amount > 0) {
                tank.setFluid(new FluidStack(Fluids.WATER, amount));
            }
        }
        finishedRecipeId = tag.contains("FinishedRecipe", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("FinishedRecipe")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, contents);
        tag.putBoolean("Ruined", ruined);
        tag.putInt("LiquidColor", color);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        if (finishedRecipeId != null) tag.putString("FinishedRecipe", finishedRecipeId.toString());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
        if (!isRemoved() && capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCapability.cast();
        }
        if (!isRemoved() && capability == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryCapability.cast();
        }
        return super.getCapability(capability, direction);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCapability.invalidate();
        inventoryCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        fluidCapability = LazyOptional.of(() -> tank);
        inventoryCapability = LazyOptional.of(() -> inventory);
    }
}
