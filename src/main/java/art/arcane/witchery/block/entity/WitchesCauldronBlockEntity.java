package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.brewing.BrewBottling;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.brewing.BrewRituals;
import art.arcane.witchery.entity.Leonard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import art.arcane.witchery.brewing.CauldronRitualRecipe;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRecipes;
import art.arcane.witchery.ritual.RitualCircle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WitchesCauldronBlockEntity extends BlockEntity {
    private static final int BUCKET = 1000;
    private static final int CAPACITY = 3 * BUCKET;
    private static final int BOIL_TICKS = 100;
    private static final int RITUAL_WARMUP_PULSES = 20;
    private static final int RITUAL_REFUND = -1;
    private final List<ItemStack> ingredients = new ArrayList<>();
    @Nullable
    private ResourceLocation ritual;
    @Nullable
    private BrewMixture mixture;
    private int heatTicks;
    private int ritualTicks;
    private boolean brewRitual;
    private int comparatorSignal;
    private int clientBrewColor = -1;
    private boolean clientRitual;

    private final FluidTank tank = new FluidTank(CAPACITY, resource -> resource.getFluid() == Fluids.WATER) {
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return ingredients.isEmpty() && !casting() ? super.fill(resource, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return ingredients.isEmpty() && !casting() ? super.drain(resource, action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int amount, FluidAction action) {
            return ingredients.isEmpty() && !casting() ? super.drain(amount, action) : FluidStack.EMPTY;
        }

        @Override
        protected void onContentsChanged() {
            if (isEmpty()) {
                ingredients.clear();
                mixture = null;
                ritual = null;
                brewRitual = false;
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
        if (cauldron.brewRitual) {
            if (level.getGameTime() % 10 == 8) cauldron.advanceBrewRitual(server, pos);
        } else if (!cauldron.isBoiling()) {
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

    private boolean casting() {
        return ritual != null || brewRitual;
    }

    private void advanceBrewRitual(ServerLevel server, BlockPos pos) {
        if (ritualTicks == RITUAL_REFUND || mixture == null) {
            endBrewRitual(server);
            return;
        }
        var circles = CastCircles.around(server, pos);
        int basePower = mixture.power();
        AltarBlockEntity altar = basePower > 0 ? AltarPower.nearest(server, pos) : null;
        double cost = basePower * circles.powerScale();
        if (!isBoiling() || tank.getFluidAmount() != CAPACITY || basePower > 0 && (altar == null || altar.power() < cost)) {
            if (ritualTicks > RITUAL_WARMUP_PULSES) {
                clearMixture();
                ritualParticles(server, ParticleTypes.SMOKE);
            } else {
                ritualTicks = 0;
            }
            return;
        }
        if (++ritualTicks <= RITUAL_WARMUP_PULSES) return;
        if (altar != null && !altar.consumePower((int) Math.floor(cost))) return;
        boolean leonard = BrewRituals.leonardNearby(server, pos);
        var result = BrewRituals.pulse(server, pos, mixture, mixture.bottleTag(ingredients), lastIngredient(),
                ritualTicks - RITUAL_WARMUP_PULSES, leonard);
        switch (result) {
            case FAILED, TOO_FAR -> endBrewRitual(server);
            case COMPLETE -> {
                clearMixture();
                ritualParticles(server, ParticleTypes.POOF);
                mishap(server, pos, circles.risk() + (leonard ? 1 : 0), basePower);
            }
            case ONGOING -> {
                mishap(server, pos, circles.risk() + (leonard ? 1 : 0), basePower);
            }
        }
    }

    private void endBrewRitual(ServerLevel server) {
        ritualTicks = RITUAL_REFUND;
        ItemStack trigger = lastIngredient();
        if (trigger.is(WitcheryIngredient.DOG_TONGUE.item()) || trigger.getItem() instanceof TaglockItem) {
            ItemEntity refund = new ItemEntity(server, worldPosition.getX() + 0.03, worldPosition.getY() + 1.2,
                    worldPosition.getZ() + 0.03, trigger.copy(), -0.08, 0.1, -0.08);
            refund.setDefaultPickUpDelay();
            if (!server.addFreshEntity(refund)) return;
            ingredients.remove(ingredients.size() - 1);
        }
        brewRitual = false;
        ritualTicks = 0;
        onIngredientAdded(server);
        ritualParticles(server, ParticleTypes.SMOKE);
    }

    private ItemStack lastIngredient() {
        return ingredients.isEmpty() ? ItemStack.EMPTY : ingredients.get(ingredients.size() - 1);
    }

    private boolean canCastWith(ItemStack trigger) {
        return mixture != null && mixture.hasEffects() && (trigger.getItem() instanceof TaglockItem
                || trigger.is(WitcheryIngredient.DOG_TONGUE.item()) && mixture.splash());
    }

    private void ritualParticles(ServerLevel server, SimpleParticleType particles) {
        Vec3 center = Vec3.atCenterOf(worldPosition);
        server.sendParticles(particles, center.x, center.y + 0.5, center.z, 8, 0.25, 0.2, 0.25, 0.02);
        server.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 1.3F);
    }

    private static void mishap(ServerLevel server, BlockPos pos, int risk, int power) {
        if (risk <= 0 || power <= 0) return;
        var random = server.getRandom();
        double roll = random.nextDouble() * (1 + (risk - 1) * 0.2);
        if (roll < 0.5) return;
        MobEffectInstance penalty = roll < 0.75 ? new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 1)
                : roll < 0.9 ? new MobEffectInstance(WitcheryEffects.PARALYSIS.get(), 400, 2)
                : roll < 0.98 ? new MobEffectInstance(WitcheryEffects.INSANITY.get(), 3600, 2)
                : new MobEffectInstance(WitcheryEffects.PARALYSIS.get(), 200, 2);
        Vec3 center = Vec3.atCenterOf(pos);
        AABB area = new AABB(center.x - 16, pos.getY() - 2, center.z - 16, center.x + 16, pos.getY() + 4, center.z + 16);
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, area, target ->
                !(target instanceof Enemy) && target.distanceToSqr(center.x, target.getY(), center.z) < 256)) {
            target.addEffect(new MobEffectInstance(penalty));
        }
        if (roll >= 0.98) {
            int bolts = 2 + random.nextInt(3);
            for (int index = 0; index < bolts; index++) {
                var lightning = EntityType.LIGHTNING_BOLT.create(server);
                if (lightning != null) {
                    int x = random.nextInt(7);
                    int z = random.nextInt(7);
                    lightning.moveTo(Vec3.atBottomCenterOf(pos.offset(x < 4 ? x - 4 : x - 2, 0, z < 4 ? z - 4 : z - 2)));
                    server.addFreshEntity(lightning);
                }
            }
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
        int batchPower = recipe.batchPower(ingredients);
        if (batchPower > 0) {
            power = (int) Math.floor(batchPower * CastCircles.around(level, pos).powerScale());
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

    private record CastCircles(double powerScale, int risk) {
        static CastCircles around(ServerLevel level, BlockPos pos) {
            Block white = WitcheryBlocks.getRequired("circleglyphritual").get();
            Block red = WitcheryBlocks.getRequired("circleglyphinfernal").get();
            int whiteCircles = 0;
            int redCircles = 0;
            for (RitualCircle ring : List.of(RitualCircle.SMALL, RitualCircle.MEDIUM)) {
                if (ring.matches(level, pos, white)) whiteCircles++;
                else if (ring.matches(level, pos, red)) redCircles++;
            }
            double scale = 1.4;
            for (int ring = 0; ring < whiteCircles; ring++) scale -= 0.2;
            for (int ring = 0; ring < redCircles; ring++) scale -= 0.37;
            return new CastCircles(scale, (whiteCircles == 0 ? 1 : 0) + redCircles);
        }
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
        if (!isBoiling() || casting()) {
            return false;
        }
        if (tank.getFluidAmount() == CAPACITY && canCastWith(offered)) {
            ingredients.add(offered.copyWithCount(1));
            brewRitual = true;
            ritualTicks = 0;
            onIngredientAdded(server);
            return true;
        }
        List<CauldronRitualRecipe> recipes = server.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get());
        if (tank.getFluidAmount() == CAPACITY) {
            var recipe = recipes.stream().filter(candidate -> candidate.canStart(ingredients, offered))
                    .max(Comparator.comparingInt(CauldronRitualRecipe::priority)
                            .thenComparingInt(candidate -> candidate.ingredients().size())).orElse(null);
            if (recipe != null) {
                ritual = recipe.id();
                ritualTicks = 0;
                splash(server);
                refreshComparator();
                setChanged();
                return true;
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
        if (!(level instanceof ServerLevel server) || !isBoiling() || ritual != null || ritualTicks == RITUAL_REFUND
                || mixture == null) return false;
        BrewMixture contents = mixture;
        int portion = BrewBottling.portion(player, contents);
        int remaining = tank.getFluidAmount() - portion;
        if (remaining < 0) return false;
        if (contents.power() > 0) {
            AltarBlockEntity altar = AltarPower.nearest(server, worldPosition);
            if (altar == null) return false;
            if (brewRitual && altar.power() < contents.power() * CastCircles.around(server, worldPosition).powerScale()) return false;
            if (!altar.consumePower(contents.power())) return false;
        }
        ItemStack bottle = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        CompoundTag bottled = contents.bottleTag(ingredients);
        if (brewRitual) bottled.putBoolean("RitualTriggered", true);
        bottle.setTag(bottled);
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
            if (isBoiling() && !casting() && mixture != null && mixture.used() > 0) {
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

    public boolean clientRitual() {
        return clientRitual;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Color", mixture != null ? mixture.color() : -1);
        tag.putBoolean("RitualTriggered", brewRitual);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        clientBrewColor = tag.getInt("Color");
        clientRitual = tag.getBoolean("RitualTriggered");
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
        Entity result;
        if (recipe.summon() != null) {
            var summon = recipe.summon().create(server);
            if (summon == null) return;
            summon.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            if (server.getBlockCollisions(summon, summon.getBoundingBox()).iterator().hasNext()) return;
            if (summon instanceof Mob mob) mob.setPersistenceRequired();
            if (summon instanceof Leonard leonard) leonard.beginSummoning();
            result = summon;
        } else {
            ItemEntity drop = new ItemEntity(server, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                    recipe.getResultItem(server.registryAccess()).copy(), 0, 0.2, 0);
            drop.setDefaultPickUpDelay();
            result = drop;
        }
        if (!server.addFreshEntity(result)) return;
        if (altar != null && !altar.consumePower(power)) {
            result.discard();
            return;
        }
        clearMixture();
        boolean summoned = recipe.summon() != null;
        server.sendParticles(summoned ? ParticleTypes.EXPLOSION : ParticleTypes.POOF,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 8, 0.25, 0.2, 0.25, 0.02);
        server.playSound(null, pos, summoned ? SoundEvents.WITHER_SPAWN : SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 0.7F, summoned ? 1 : 1.3F);
    }

    private void clearMixture() {
        ingredients.clear();
        mixture = null;
        ritual = null;
        brewRitual = false;
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
        String legacyFluid = tag.getString("FluidName");
        CompoundTag originalBrew = "witchery:brew".equals(legacyFluid) ? tag.getCompound("Tag") : new CompoundTag();
        if (!tag.contains("Tank", Tag.TAG_COMPOUND) && ("witchery:brew".equals(legacyFluid) || "water".equals(legacyFluid))) {
            int amount = Mth.clamp(tag.getInt("Amount"), 0, CAPACITY);
            tank.setFluid(amount > 0 ? new FluidStack(Fluids.WATER, amount) : FluidStack.EMPTY);
        } else {
            tank.readFromNBT(tag.getCompound("Tank"));
        }
        ingredients.clear();
        ListTag stored = tag.contains("Ingredients", Tag.TAG_LIST) ? tag.getList("Ingredients", Tag.TAG_COMPOUND)
                : originalBrew.getList("Items", Tag.TAG_COMPOUND);
        for (Tag item : stored) {
            ItemStack stack = BrewMixture.loadIngredient((CompoundTag) item);
            if (!stack.isEmpty()) {
                ingredients.add(stack);
            }
        }
        mixture = BrewMixture.from(ingredients);
        heatTicks = Mth.clamp(tag.contains("HeatTicks", Tag.TAG_ANY_NUMERIC)
                ? tag.getInt("HeatTicks") : tag.getInt("TicksHeated"), 0, BOIL_TICKS);
        ritual = tag.contains("Ritual", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("Ritual")) : null;
        brewRitual = ritual == null && (tag.getBoolean("RitualTriggered") || originalBrew.getBoolean("RitualTriggered"))
                && canCastWith(lastIngredient());
        ritualTicks = Math.max(brewRitual ? RITUAL_REFUND : 0, tag.getInt("RitualTicks"));
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
        }
        if (brewRitual) tag.putBoolean("RitualTriggered", true);
        if (casting()) {
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
