package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.brewing.BrewBottling;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.brewing.CauldronRitualRecipe;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRecipes;
import art.arcane.witchery.ritual.RitualCircle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class WitchesCauldronBlockEntity extends BlockEntity {
    private static final int BUCKET = 1000;
    private static final int CAPACITY = 3 * BUCKET;
    private static final int BOIL_TICKS = 100;
    private final List<ItemStack> ingredients = new ArrayList<>();
    @Nullable
    private ResourceLocation ritual;
    @Nullable
    private BrewMixture mixture;
    private int heatTicks;
    private int ritualTicks;
    private int comparatorSignal;
    private int clientBrewColor = -1;

    private final FluidTank tank = new FluidTank(CAPACITY, resource -> resource.getFluid() == Fluids.WATER) {
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return ingredients.isEmpty() && ritual == null ? super.fill(resource, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return ingredients.isEmpty() && ritual == null ? super.drain(resource, action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int amount, FluidAction action) {
            return ingredients.isEmpty() && ritual == null ? super.drain(amount, action) : FluidStack.EMPTY;
        }

        @Override
        protected void onContentsChanged() {
            if (isEmpty()) {
                ingredients.clear();
                mixture = null;
                ritual = null;
                heatTicks = 0;
                ritualTicks = 0;
            }
            updateState();
            refreshComparator();
            setChanged();
        }
    };
    private LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> tank);

    public WitchesCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.CAULDRON.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WitchesCauldronBlockEntity cauldron) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        int oldHeat = cauldron.heatTicks;
        int oldProgress = cauldron.ritualTicks;
        if (!cauldron.tank.isEmpty() && level.getBlockState(pos.below()).is(Blocks.FIRE)) {
            cauldron.heatTicks = Math.min(BOIL_TICKS, cauldron.heatTicks + 1);
        } else {
            cauldron.heatTicks = 0;
        }
        if (state.getValue(WitchesCauldronBlock.BOILING) != cauldron.isBoiling()
                || state.getValue(WitchesCauldronBlock.WATER) != cauldron.visibleWaterLevel()) {
            cauldron.updateState();
            cauldron.setChanged();
        }
        if (!cauldron.isBoiling()) {
            cauldron.ritualTicks = 0;
        } else if (cauldron.ritual != null && cauldron.tank.getFluidAmount() == CAPACITY) {
            cauldron.advanceRitual(server, pos);
        }
        if (oldHeat != cauldron.heatTicks || oldProgress != cauldron.ritualTicks) {
            level.blockEntityChanged(pos);
        }
        if (oldHeat != cauldron.heatTicks || server.getGameTime() % 20 == 0) {
            cauldron.refreshComparator();
        }
    }

    private void advanceRitual(ServerLevel level, BlockPos pos) {
        if (!(level.getRecipeManager().byKey(ritual).orElse(null) instanceof CauldronRitualRecipe recipe)) {
            ritual = null;
            ritualTicks = 0;
            setChanged();
            return;
        }
        AltarBlockEntity altar = null;
        int power = 0;
        if (recipe.power() > 0) {
            power = ritualPower(level, pos, recipe.power());
            altar = AltarPower.nearest(level, pos);
            if (altar == null || altar.power() < power) {
                ritualTicks = 0;
                return;
            }
        }
        if (ritualTicks < recipe.time()) {
            ritualTicks++;
        }
        if (ritualTicks == recipe.time()) {
            completeRitual(level, pos, recipe, altar, power);
        }
    }

    private static int ritualPower(ServerLevel level, BlockPos pos, int basePower) {
        Block white = WitcheryBlocks.getRequired("circleglyphritual").get();
        Block red = WitcheryBlocks.getRequired("circleglyphinfernal").get();
        double multiplier = 1.4;
        for (RitualCircle ring : List.of(RitualCircle.SMALL, RitualCircle.MEDIUM)) {
            if (ring.matches(level, pos, white)) {
                multiplier -= 0.2;
            } else if (ring.matches(level, pos, red)) {
                multiplier -= 0.37;
            }
        }
        return (int) Math.floor(basePower * multiplier);
    }

    public boolean addIngredient(ItemStack offered) {
        if (!(level instanceof ServerLevel server) || offered.isEmpty() || tank.isEmpty()) {
            return false;
        }
        if (offered.is(WitcheryIngredient.GYPSUM.item())) {
            clearMixture();
            BlockPos pos = getBlockPos();
            server.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                    8, 0.22, 0.16, 0.22, 0.03);
            server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 1.3F);
            return true;
        }
        if (!isBoiling() || ritual != null) {
            return false;
        }
        List<CauldronRitualRecipe> recipes = server.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get());
        if (tank.getFluidAmount() == CAPACITY) {
            for (CauldronRitualRecipe recipe : recipes) {
                if (recipe.canStart(ingredients, offered)) {
                    ritual = recipe.id();
                    ritualTicks = 0;
                    splash(server);
                    refreshComparator();
                    setChanged();
                    return true;
                }
            }
        }
        for (CauldronRitualRecipe recipe : recipes) {
            if (recipe.acceptsIngredient(ingredients, offered)) {
                ingredients.add(offered.copyWithCount(1));
                onIngredientAdded(server);
                return true;
            }
        }
        if (BrewMixture.canAdd(ingredients, offered)) {
            BrewMixture.add(ingredients, offered);
            onIngredientAdded(server);
            return true;
        }
        return false;
    }

    private void onIngredientAdded(ServerLevel server) {
        mixture = BrewMixture.from(ingredients);
        splash(server);
        refreshComparator();
        BlockState state = getBlockState();
        server.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        setChanged();
    }

    public boolean bottle(ServerPlayer player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server) || !isBoiling() || ritual != null || mixture == null) return false;
        BrewMixture contents = mixture;
        int portion = BrewBottling.portion(player, contents);
        int remaining = tank.getFluidAmount() - portion;
        if (remaining < 0) return false;
        if (contents.power() > 0) {
            AltarBlockEntity altar = AltarPower.nearest(server, worldPosition);
            if (altar == null || !altar.consumePower(contents.power())) return false;
        }
        ItemStack bottle = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        bottle.setTag(contents.bottleTag(ingredients));
        player.setItemInHand(hand, ItemUtils.createFilledResult(player.getItemInHand(hand), player, bottle, false));
        BrewBottling.learn(player, contents);
        if (remaining == 0) {
            clearMixture();
        } else {
            tank.setFluid(new FluidStack(Fluids.WATER, remaining));
            updateState();
            refreshComparator();
            setChanged();
        }
        server.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    public int comparatorSignal() {
        return comparatorSignal;
    }

    private void refreshComparator() {
        if (!(level instanceof ServerLevel server) || isRemoved()) return;
        int signal = 0;
        if (!tank.isEmpty()) {
            signal = isBoiling() ? 6 : 3;
            if (isBoiling() && ritual == null && mixture != null && mixture.used() > 0) {
                AltarBlockEntity altar = mixture.power() > 0 ? AltarPower.nearest(server, worldPosition) : null;
                boolean powered = mixture.power() == 0 || altar != null && altar.power() >= mixture.power();
                signal = powered ? (mixture.used() == mixture.capacity() ? 15 : 12) : 9;
            }
        }
        if (signal != comparatorSignal) {
            comparatorSignal = signal;
            server.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public int clientBrewColor() {
        return clientBrewColor;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Color", mixture != null ? mixture.color() : -1);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        clientBrewColor = tag.getInt("Color");
        if (level != null && level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) handleUpdateTag(packet.getTag());
    }

    private boolean isBoiling() {
        return heatTicks >= BOIL_TICKS;
    }

    private int visibleWaterLevel() {
        return Mth.clamp(Mth.ceil(tank.getFluidAmount() / (float) BUCKET), 0, 3);
    }

    private void completeRitual(ServerLevel server, BlockPos pos, CauldronRitualRecipe recipe,
                                @Nullable AltarBlockEntity altar, int power) {
        ItemEntity result = new ItemEntity(server, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                recipe.getResultItem(server.registryAccess()).copy(), 0, 0.2, 0);
        result.setDefaultPickUpDelay();
        if (!server.addFreshEntity(result)) {
            return;
        }
        if (altar != null && !altar.consumePower(power)) {
            result.discard();
            return;
        }
        clearMixture();
        server.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                8, 0.25, 0.2, 0.25, 0.02);
        server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 1.3F);
    }

    private void clearMixture() {
        ingredients.clear();
        mixture = null;
        ritual = null;
        ritualTicks = 0;
        heatTicks = 0;
        tank.setFluid(FluidStack.EMPTY);
        updateState();
        refreshComparator();
        setChanged();
    }

    private void splash(ServerLevel server) {
        BlockPos pos = getBlockPos();
        server.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                8, 0.2, 0.06, 0.2, 0.03);
        server.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.4F, 1.2F);
    }

    private void updateState() {
        if (level == null || level.isClientSide || isRemoved()) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() instanceof WitchesCauldronBlock) {
            BlockState updated = state.setValue(WitchesCauldronBlock.WATER, visibleWaterLevel())
                    .setValue(WitchesCauldronBlock.BOILING, isBoiling());
            if (updated != state) {
                level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("Tank"));
        ingredients.clear();
        for (Tag item : tag.getList("Ingredients", Tag.TAG_COMPOUND)) {
            ItemStack stack = WitcheryIngredient.loadInventoryStack((CompoundTag) item);
            if (!stack.isEmpty()) {
                ingredients.add(stack);
            }
        }
        mixture = BrewMixture.from(ingredients);
        heatTicks = Mth.clamp(tag.getInt("HeatTicks"), 0, BOIL_TICKS);
        ritual = tag.contains("Ritual", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("Ritual")) : null;
        ritualTicks = Math.max(0, tag.getInt("RitualTicks"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        ListTag stored = new ListTag();
        for (ItemStack stack : ingredients) {
            stored.add(stack.save(new CompoundTag()));
        }
        tag.put("Ingredients", stored);
        tag.putInt("HeatTicks", heatTicks);
        if (ritual != null) {
            tag.putString("Ritual", ritual.toString());
            tag.putInt("RitualTicks", ritualTicks);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (!isRemoved() && capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        fluidHandler = LazyOptional.of(() -> tank);
    }
}
