package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.brewing.BrewBottling;
import art.arcane.witchery.brewing.BrewColor;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.brewing.BrewRituals;
import art.arcane.witchery.entity.Leonard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import art.arcane.witchery.brewing.CauldronRitualRecipe;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.item.BiomeNoteItem;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.core.registries.Registries;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryFluids;
import art.arcane.witchery.registry.WitcheryRecipes;
import art.arcane.witchery.registry.WitcherySounds;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
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
import java.util.Objects;

public final class WitchesCauldronBlockEntity extends BlockEntity {
    private static final int BUCKET = 1000;
    private static final int CAPACITY = 3 * BUCKET;
    private static final int BOIL_TICKS = 100;
    private static final int POWER_CHECK_INTERVAL = 20;
    private static final int POWER_CHECK_PHASE = 7;
    private static final int RITUAL_PULSE_INTERVAL = 10;
    private static final int RITUAL_PULSE_PHASE = 8;
    private static final int RITUAL_WARMUP_PULSES = 20;
    private static final int RITUAL_REFUND = -1;
    private static final int INTERRUPTED_RITUAL_COLOR = 0xFF888800;
    private static final int DISTANT_TARGET_COLOR = 0xFF008800;
    private static final int INVALID_CIRCLES_COLOR = 0xFF880000;
    private static final int FAILED_RITUAL_COLOR = 0xFF880088;
    private final List<Offering> ingredients = new ArrayList<>();
    @Nullable
    private ResourceLocation ritual;
    @Nullable
    private Offering ritualTrigger;
    // Fresh fixed casts append the trigger. A restored cast can have inactive entries after it.
    private int fixedTriggerPosition = -1;
    @Nullable
    private BrewMixture mixture;
    @Nullable
    private Integer brewColor;
    private long ticks;
    private int heatTicks;
    private int ritualTicks;
    private boolean powered;
    private boolean brewRitual;
    private boolean legacyRitualPending;
    private int comparatorSignal;
    private int clientBrewColor = -1;
    private int clientAmount;
    private int sparkCount;

    private final CauldronTank tank = new CauldronTank();
    private LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> tank);

    private record Offering(ItemStack stack, @Nullable CompoundTag unavailable) {
        static Offering load(CompoundTag tag) {
            ItemStack stack = BrewMixture.loadIngredient(tag);
            return new Offering(stack, stack.isEmpty() ? tag.copy() : null);
        }

        static Offering copyOf(ItemStack stack) {
            return new Offering(stack.copy(), null);
        }

        CompoundTag save() {
            return unavailable != null ? unavailable.copy() : stack.save(new CompoundTag());
        }

        boolean matches(Offering other) {
            return Objects.equals(unavailable, other.unavailable) && ItemStack.matches(stack, other.stack);
        }
    }

    private static List<Offering> loadOfferings(ListTag stored) {
        List<Offering> offerings = new ArrayList<>(stored.size());
        for (Tag tag : stored) offerings.add(Offering.load((CompoundTag) tag));
        return offerings;
    }

    private static ListTag saveOfferings(List<Offering> offerings) {
        ListTag stored = new ListTag();
        for (Offering offering : offerings) stored.add(offering.save());
        return stored;
    }

    private static List<ItemStack> offeringStacks(List<Offering> offerings) {
        return offerings.stream().map(Offering::stack).toList();
    }

    private static int offeringColor(List<ItemStack> stacks) {
        return BrewColor.fromIngredients(stacks.stream().filter(stack -> !stack.isEmpty()).toList());
    }

    private record TransportedBrew(List<Offering> ingredients, @Nullable Integer color, @Nullable BrewMixture mixture,
                                   boolean ritualTriggered) {
        static TransportedBrew from(FluidStack resource) {
            CompoundTag tag = resource.getTag();
            if (tag == null) return new TransportedBrew(List.of(), null, null, false);
            var inputs = loadOfferings(tag.getList("Items", Tag.TAG_COMPOUND));
            var stacks = offeringStacks(inputs);
            Integer color = BrewColor.saved(tag);
            if (color == null && !inputs.isEmpty()) color = offeringColor(stacks);
            return new TransportedBrew(inputs, color, BrewMixture.fromStoredOfferings(stacks, color),
                    !inputs.isEmpty() && tag.getBoolean("RitualTriggered"));
        }

        int visibleColor() {
            return color == null ? -1 : color & 0xFFFFFF;
        }
    }

    private final class CauldronTank extends FluidTank {
        private CauldronTank() {
            super(CAPACITY, resource -> resource.getFluid() == Fluids.WATER
                    || resource.getFluid() == WitcheryFluids.getRequiredSource("brew").get());
        }

        @Override
        public FluidStack getFluidInTank(int slot) {
            if (slot != 0 || isEmpty()) return FluidStack.EMPTY;
            var brew = WitcheryFluids.getRequiredSource("brew").get();
            var fixed = legacyFixedRecipe();
            if (fixed != null) {
                var contents = fixedBrewTag(fixed);
                if (contents != null) return new FluidStack(brew, getFluidAmount(), contents);
            }
            if (mixture != null || !ingredients.isEmpty() || brewColor != null) {
                return new FluidStack(brew, getFluidAmount(), brewTag(mixture));
            }
            return getFluid().copy();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (ritualTicks == RITUAL_REFUND || resource.isEmpty() || !isFluidValid(resource) || getSpace() == 0) return 0;
            var fixed = legacyFixedRecipe();
            if (ritual != null && fixed == null) return 0;
            var current = getFluidInTank(0);
            if (!current.isEmpty() && current.getFluid() != resource.getFluid()) return 0;
            TransportedBrew brew = null;
            if (resource.getFluid() != Fluids.WATER) {
                brew = TransportedBrew.from(resource);
                if (!isEmpty() && !matchesBrew(brew, fixed)) return 0;
            }
            int filled = Math.min(getSpace(), resource.getAmount());
            if (action.execute()) {
                if (brew != null && isEmpty()) {
                    ingredients.addAll(brew.ingredients());
                    brewColor = brew.color();
                    mixture = brew.mixture();
                    legacyRitualPending = brew.ritualTriggered();
                }
                setFluid(new FluidStack(resource.getFluid(), getFluidAmount() + filled));
                publishTankContents();
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return !resource.isEmpty() && getFluidInTank(0).isFluidEqual(resource)
                    ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int amount, FluidAction action) {
            if (amount <= 0 || isEmpty() || ritualTicks == RITUAL_REFUND
                    || ritual != null && legacyFixedRecipe() == null) return FluidStack.EMPTY;
            FluidStack drained = getFluidInTank(0);
            if (ritual != null && (!drained.hasTag() || !drained.getTag().getBoolean("RitualTriggered"))) return FluidStack.EMPTY;
            drained.setAmount(Math.min(amount, getFluidAmount()));
            if (action.execute()) retainLiquid(getFluidAmount() - drained.getAmount());
            return drained;
        }
    }

    private boolean matchesBrew(TransportedBrew incoming, @Nullable CauldronRitualRecipe fixed) {
        if (casting() != incoming.ritualTriggered()) return false;
        List<Offering> current = fixed == null ? ingredients : fixedOfferings(fixed);
        if (visibleBrewColor() != incoming.visibleColor() || current.size() != incoming.ingredients().size()) return false;
        for (int index = 0; index < current.size(); index++) {
            if (!current.get(index).matches(incoming.ingredients().get(index))) return false;
        }
        return true;
    }

    public WitchesCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.CAULDRON.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WitchesCauldronBlockEntity cauldron) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        long phase = ++cauldron.ticks % POWER_CHECK_INTERVAL;
        int oldHeat = cauldron.heatTicks;
        int oldProgress = cauldron.ritualTicks;
        boolean wasPowered = cauldron.powered;
        if (cauldron.legacyRitualPending && cauldron.tank.getFluidAmount() == CAPACITY) {
            cauldron.restoreLegacyRitual(server);
        }
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
        boolean checkPower = phase == POWER_CHECK_PHASE;
        if (checkPower && cauldron.isBoiling()) {
            cauldron.powered = cauldron.hasAltarPower(server, cauldron.observedFixedRecipe(server));
        }
        boolean ritualPulse = phase % RITUAL_PULSE_INTERVAL == RITUAL_PULSE_PHASE;
        if (cauldron.brewRitual && ritualPulse) {
            cauldron.advanceBrewRitual(server, pos);
        } else if (cauldron.ritual != null) {
            cauldron.advanceRitual(server, pos, ritualPulse);
        } else if (ritualPulse && cauldron.ritualTicks > 0
                && (!cauldron.isBoiling() || !cauldron.powered || cauldron.tank.getFluidAmount() < CAPACITY)) {
            cauldron.pauseRitual(server, pos, cauldron.ritualTicks > RITUAL_WARMUP_PULSES);
        }
        if (oldHeat != cauldron.heatTicks || oldProgress != cauldron.ritualTicks || wasPowered != cauldron.powered) {
            level.blockEntityChanged(pos);
        }
        if (cauldron.ticks == 1 || checkPower || oldHeat != cauldron.heatTicks
                || cauldron.ritualSparkCount(oldProgress) != cauldron.ritualSparkCount(cauldron.ritualTicks)) {
            cauldron.refreshIndicators(server, false);
        }
    }

    private boolean casting() {
        return ritual != null || brewRitual || legacyRitualPending;
    }

    private void restoreLegacyRitual(ServerLevel server) {
        int index = pendingTriggerIndex(server);
        ItemStack trigger = stackAt(index);
        if (canCastWith(trigger)) {
            brewRitual = true;
        } else {
            CauldronRitualRecipe recipe = pendingFixedRecipe(server, index);
            if (recipe != null) {
                ritual = recipe.id();
                ritualTrigger = ingredients.get(index);
                fixedTriggerPosition = index < ingredients.size() - 1 ? index : -1;
                ritualTicks = (int) Math.min(recipe.time(), 10L * ritualTicks);
                ingredients.remove(index);
            } else {
                // A missing final item may be the saved trigger. Keep the batch available for later recovery.
                if (stackAt(ingredients.size() - 1).isEmpty() || !refundRitualTrigger(server, index)) return;
                ritualTicks = 0;
                WitcheryNetwork.sendCauldronRitualFailure(server, worldPosition, FAILED_RITUAL_COLOR);
            }
            mixture = BrewMixture.fromStoredOfferings(offeringStacks(ingredients), brewColor);
        }
        legacyRitualPending = false;
        publishTankContents();
    }

    private int pendingTriggerIndex(ServerLevel server) {
        var recipes = server.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get());
        for (int index = ingredients.size() - 1; index >= 0; index--) {
            ItemStack stack = stackAt(index);
            if (!stack.isEmpty() && (BrewMixture.hasAction(stack)
                    || recipes.stream().anyMatch(recipe -> recipe.legacy() && recipe.trigger().test(stack)))) return index;
        }
        return ingredients.size() - 1;
    }

    @Nullable
    private CauldronRitualRecipe pendingFixedRecipe(ServerLevel server, int index) {
        if (index < 0 || stackAt(index).isEmpty()) return null;
        var recipes = server.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get())
                .stream().filter(CauldronRitualRecipe::legacy).toList();
        return matchingRitual(recipes, offeringStacks(ingredients.subList(0, index)), stackAt(index));
    }

    private void advanceBrewRitual(ServerLevel server, BlockPos pos) {
        if (ritualTicks == RITUAL_REFUND || mixture == null) {
            endBrewRitual(server, FAILED_RITUAL_COLOR);
            return;
        }
        if (!powered || !isBoiling() || tank.getFluidAmount() != CAPACITY) {
            pauseRitual(server, pos, ritualTicks > RITUAL_WARMUP_PULSES);
            return;
        }
        if (++ritualTicks <= RITUAL_WARMUP_PULSES) return;
        var circles = CastCircles.around(server, pos);
        int basePower = mixture.power();
        if (basePower > 0) {
            AltarBlockEntity altar = AltarPower.nearest(server, pos);
            int cost = (int) Math.floor(basePower * circles.powerScale());
            if (altar == null || !altar.consumePower(cost)) {
                if (ritualTicks > RITUAL_WARMUP_PULSES + 1) interruptRitual(server, pos);
                return;
            }
        }
        boolean leonard = BrewRituals.leonardNearby(server, pos);
        var result = BrewRituals.pulse(server, pos, mixture, mixture.bottleTag(saveOfferings(ingredients)), stackAt(lastBrewAction()),
                ritualTicks - RITUAL_WARMUP_PULSES, leonard);
        switch (result) {
            case FAILED -> endBrewRitual(server, FAILED_RITUAL_COLOR);
            case TOO_FAR -> endBrewRitual(server, DISTANT_TARGET_COLOR);
            case INVALID_CIRCLES -> endBrewRitual(server, INVALID_CIRCLES_COLOR);
            case COMPLETE -> {
                clearMixture();
                WitcheryNetwork.sendCauldronRitualComplete(server, pos);
                mishap(server, pos, circles.risk() + (leonard ? 1 : 0), basePower);
            }
            case ONGOING -> {
                mishap(server, pos, circles.risk() + (leonard ? 1 : 0), basePower);
            }
        }
    }

    private void interruptRitual(ServerLevel server, BlockPos pos) {
        clearMixture();
        WitcheryNetwork.sendCauldronRitualFailure(server, pos, INTERRUPTED_RITUAL_COLOR);
    }

    private void pauseRitual(ServerLevel server, BlockPos pos, boolean pastWarmup) {
        if (pastWarmup) interruptRitual(server, pos);
        else ritualTicks = 0;
    }

    private void endBrewRitual(ServerLevel server, int color) {
        if (ritualTicks != RITUAL_REFUND) {
            ritualTicks = RITUAL_REFUND;
            WitcheryNetwork.sendCauldronRitualFailure(server, worldPosition, color);
        }
        int index = lastBrewAction();
        ItemStack trigger = stackAt(index);
        if (trigger.is(WitcheryIngredient.DOG_TONGUE.item()) || trigger.getItem() instanceof TaglockItem) {
            if (!refundRitualTrigger(server, index)) return;
        }
        brewRitual = false;
        ritualTicks = 0;
        refreshMixture(server);
    }

    private boolean refundRitualTrigger(ServerLevel server, int index) {
        ItemEntity refund = new ItemEntity(server, worldPosition.getX() + 0.03, worldPosition.getY() + 1.2,
                worldPosition.getZ() + 0.03, stackAt(index).copy(), -0.08, 0.1, -0.08);
        refund.setDefaultPickUpDelay();
        if (!server.addFreshEntity(refund)) return false;
        ingredients.remove(index);
        return true;
    }

    private int lastBrewAction() {
        for (int index = ingredients.size() - 1; index >= 0; index--) {
            if (BrewMixture.hasAction(stackAt(index))) return index;
        }
        return -1;
    }

    private ItemStack stackAt(int index) {
        return index < 0 ? ItemStack.EMPTY : ingredients.get(index).stack();
    }

    private boolean canCastWith(ItemStack trigger) {
        return mixture != null && mixture.hasEffects() && (trigger.getItem() instanceof TaglockItem
                || trigger.is(WitcheryIngredient.DOG_TONGUE.item()) && mixture.splash());
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
            // The original rolls the loop bound again after each strike.
            for (int index = 0; index < 2 + random.nextInt(3); index++) {
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

    private void advanceRitual(ServerLevel level, BlockPos pos, boolean ritualPulse) {
        if (!(level.getRecipeManager().byKey(ritual).orElse(null) instanceof CauldronRitualRecipe recipe)) {
            ritual = null;
            ritualTrigger = null;
            fixedTriggerPosition = -1;
            ritualTicks = 0;
            setChanged();
            return;
        }
        boolean legacy = recipe.legacy();
        if (legacy && !ritualPulse) return;
        boolean attempted = ritualTicks >= recipe.time();
        if (!isBoiling() || tank.getFluidAmount() != CAPACITY || legacy && !powered) {
            pauseRitual(level, pos, legacy && attempted);
            return;
        }
        if (legacy && !attempted) {
            // Keep elapsed ticks in the saved format used by earlier Forge builds.
            ritualTicks += Math.min(RITUAL_PULSE_INTERVAL, recipe.time() - ritualTicks);
            if (ritualTicks < recipe.time()) return;
        }
        AltarBlockEntity altar = null;
        int power = 0;
        int batchPower = recipe.batchPower(offeringStacks(ingredients));
        CastCircles circles = batchPower > 0 ? CastCircles.around(level, pos) : null;
        if (circles != null) {
            double required = requiredFixedPower(recipe, batchPower, circles);
            altar = AltarPower.nearest(level, pos);
            power = (int) Math.floor(required);
            if (altar == null || altar.power() < power) {
                if (legacy && attempted) interruptRitual(level, pos);
                else if (!legacy) ritualTicks = 0;
                return;
            }
        }
        if (ritualTicks < recipe.time()) {
            ritualTicks++;
        }
        if (ritualTicks >= recipe.time()) {
            int risk = legacy && circles != null
                    ? circles.risk() + (BrewRituals.leonardNearby(level, pos) ? 1 : 0) : 0;
            if (completeRitual(level, pos, recipe, altar, power)) {
                mishap(level, pos, risk, batchPower);
            }
        }
    }

    private static double requiredFixedPower(CauldronRitualRecipe recipe, int batchPower, CastCircles circles) {
        // The original powered check uses the scaled value before the debit is rounded down.
        double scaled = batchPower * circles.powerScale();
        return recipe.legacy() ? scaled : Math.floor(scaled);
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

    public void acceptDroppedItem(ItemEntity item) {
        ItemStack offered = item.getItem();
        if (!(level instanceof ServerLevel server) || offered.isEmpty() || tank.isEmpty()) return;

        if (WitcheryIngredient.GYPSUM.matches(offered)) {
            item.discard();
            Vec3 center = Vec3.atBottomCenterOf(worldPosition).add(0, 0.6, 0);
            server.playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 2.0F);
            WitcheryNetwork.sendCauldronReagentSmoke(server, center);
            emptyTank();
        } else if (WitcheryIngredient.QUICKLIME.matches(offered)) {
            detonate(server, item);
        } else {
            int consumed = addIngredient(server, offered);
            if (consumed == 0) return;
            ItemStack remainder = offered.getCraftingRemainingItem();
            if (consumed == offered.getCount()) item.discard();
            else item.setItem(offered.copyWithCount(offered.getCount() - consumed));
            if (!remainder.isEmpty()) Block.popResource(server, worldPosition.above(), remainder);
            WitcheryNetwork.sendCauldronIngredientSplash(server, worldPosition);
        }
    }

    private void detonate(ServerLevel server, ItemEntity item) {
        if (ingredients.isEmpty() && brewColor == null && ritual == null) return;
        Player nearest = server.getEntitiesOfClass(Player.class, item.getBoundingBox().inflate(5),
                        player -> player.isAlive() && !player.isSpectator()).stream()
                .min(Comparator.comparingDouble(item::distanceToSqr)).orElse(null);
        if (nearest == null) return;

        var recipes = server.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get());
        long actions = ingredients.stream().map(Offering::stack).filter(stack -> BrewMixture.hasAction(stack)
                || recipes.stream().anyMatch(recipe -> recipe.trigger().test(stack)
                        || recipe.ingredients().stream().anyMatch(ingredient -> ingredient.test(stack)))).count();
        // Fixed recipes store their trigger as a recipe ID. Other rituals keep it in the ingredient list.
        if (ritual != null) actions++;
        float power = Math.min(1.0F + 0.5F * actions, 10.0F);
        item.discard();
        emptyTank();
        Vec3 center = Vec3.atCenterOf(worldPosition).add(0, 1, 0);
        server.explode(nearest, center.x, center.y, center.z, power, Level.ExplosionInteraction.NONE);
        WitcheryNetwork.sendCauldronReagentSmoke(server, Vec3.atBottomCenterOf(worldPosition).add(0, 0.6, 0));
    }

    private int addIngredient(ServerLevel server, ItemStack offered) {
        if (!isBoiling() || casting()) {
            return 0;
        }
        // Each accepted legacy stack contributes one ordered brew action.
        int count = offered.getCount();
        int wool = BrewMixture.woolColor(offered);
        if (wool >= 0) {
            brewColor = wool;
            refreshMixture(server);
            return count;
        }
        if (tank.getFluidAmount() == CAPACITY && canCastWith(offered)) {
            ingredients.add(Offering.copyOf(offered));
            brewRitual = true;
            ritualTicks = 0;
            onIngredientAdded(server, offered);
            return count;
        }
        List<CauldronRitualRecipe> recipes = server.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get());
        List<ItemStack> stacks = offeringStacks(ingredients);
        if (tank.getFluidAmount() == CAPACITY) {
            var recipe = matchingRitual(recipes, stacks, offered);
            if (recipe != null) {
                ritual = recipe.id();
                int consumed = recipe.legacy() ? count : 1;
                ritualTrigger = new Offering(offered.copyWithCount(consumed), null);
                ritualTicks = 0;
                onIngredientAdded(server, offered);
                return consumed;
            }
        }
        boolean registeredBrewAction = BrewMixture.hasAction(offered);
        for (CauldronRitualRecipe recipe : recipes) {
            // Ingredients with original brew actions obey their capacity and ordering even for a fixed recipe.
            if (recipe.legacy() && registeredBrewAction) continue;
            if (recipe.acceptsIngredient(stacks, offered)) {
                int consumed = recipe.legacy() ? count : 1;
                ingredients.add(new Offering(offered.copyWithCount(consumed), null));
                onIngredientAdded(server, offered);
                return consumed;
            }
        }
        if (BrewMixture.canAdd(stacks, offered)) {
            if (offered.getItem() instanceof BiomeNoteItem && !server.registryAccess().registryOrThrow(Registries.BIOME)
                    .containsKey(BiomeNoteItem.biomeId(offered))) return 0;
            ingredients.removeIf(entry -> BrewMixture.replaces(offered, entry.stack()));
            ingredients.add(Offering.copyOf(offered));
            onIngredientAdded(server, offered);
            return count;
        }
        return 0;
    }

    @Nullable
    private static CauldronRitualRecipe matchingRitual(List<CauldronRitualRecipe> recipes, List<ItemStack> offerings, ItemStack trigger) {
        return recipes.stream().filter(recipe -> recipe.canStart(offerings, trigger))
                .max(Comparator.comparingInt(CauldronRitualRecipe::priority)
                        .thenComparingInt(recipe -> recipe.ingredients().size())).orElse(null);
    }

    private void refreshMixture(ServerLevel server) {
        mixture = BrewMixture.fromStoredOfferings(offeringStacks(ingredients), brewColor);
        refreshIndicators(server, true);
        setChanged();
    }

    private void onIngredientAdded(ServerLevel server, ItemStack offered) {
        brewColor = BrewColor.append(brewColor == null ? 0 : brewColor, offered);
        refreshMixture(server);
    }

    public boolean bottle(ServerPlayer player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server) || !isBoiling() || !powered || ritualTicks == RITUAL_REFUND) return false;
        CompoundTag bottled = bottlingTag(server);
        if (bottled == null) return false;
        BrewMixture contents = bottled.contains("FixedRitual", Tag.TAG_STRING) ? BrewMixture.from(bottled) : mixture;
        if (contents == null) return false;
        int portion = BrewBottling.portion(player, contents);
        int remaining = tank.getFluidAmount() - portion;
        if (remaining < 0) return false;
        int power = bottled.getInt("Power");
        if (power > 0) {
            AltarBlockEntity altar = AltarPower.nearest(server, worldPosition);
            if (altar == null || !altar.consumePower(power)) return false;
        }
        player.setItemInHand(hand, ItemUtils.createFilledResult(player.getItemInHand(hand), player, filledBottle(bottled), false));
        BrewBottling.learn(player, contents);
        retainLiquid(remaining);
        WitcherySounds.playLegacyPlayerCue(player, SoundEvents.PLAYER_SWIM, 0.5F);
        return true;
    }

    private void retainLiquid(int remaining) {
        if (remaining == 0) {
            emptyTank();
        } else {
            tank.setFluid(new FluidStack(tank.getFluid().getFluid(), remaining));
            publishTankContents();
        }
    }

    public ItemStack dispenseBottle() {
        if (!(level instanceof ServerLevel server) || !isBoiling() || !powered || ritualTicks == RITUAL_REFUND) {
            return ItemStack.EMPTY;
        }
        CompoundTag bottled = bottlingTag(server);
        if (bottled == null) return ItemStack.EMPTY;
        boolean full = tank.getFluidAmount() == CAPACITY;
        // The original dispenser drains the available liquid even when it has less than one full portion.
        emptyTank();
        WitcherySounds.playLegacyBlockCue(server, worldPosition, SoundEvents.PLAYER_SWIM, 0.5F);
        return full ? filledBottle(bottled) : ItemStack.EMPTY;
    }

    private double legacyPowerRequirement(ServerLevel server, int basePower) {
        return basePower > 0 && tank.getFluidAmount() == CAPACITY && casting()
                ? basePower * CastCircles.around(server, worldPosition).powerScale() : basePower;
    }

    private CompoundTag brewTag(@Nullable BrewMixture contents) {
        ListTag stored = saveOfferings(ingredients);
        CompoundTag bottled = contents != null ? contents.bottleTag(stored) : BrewMixture.ingredientTag(stored, brewColor);
        if (brewRitual || legacyRitualPending) bottled.putBoolean("RitualTriggered", true);
        return bottled;
    }

    @Nullable
    private CompoundTag bottlingTag(ServerLevel server) {
        var recipe = observedFixedRecipe(server);
        if (recipe != null) return recipe.legacy() ? fixedBrewTag(recipe) : null;
        if (ritual != null || mixture == null || legacyRitualPending && !canCastWith(stackAt(pendingTriggerIndex(server)))) return null;
        return brewTag(mixture);
    }

    @Nullable
    private CauldronRitualRecipe fixedRecipe() {
        if (ritual == null || level == null) return null;
        return level.getRecipeManager().byKey(ritual).orElse(null) instanceof CauldronRitualRecipe recipe ? recipe : null;
    }

    @Nullable
    private CauldronRitualRecipe legacyFixedRecipe() {
        var recipe = fixedRecipe();
        return recipe != null && recipe.legacy() ? recipe : null;
    }

    @Nullable
    private CauldronRitualRecipe observedFixedRecipe(ServerLevel server) {
        if (!legacyRitualPending) return fixedRecipe();
        int index = pendingTriggerIndex(server);
        return canCastWith(stackAt(index)) ? null : pendingFixedRecipe(server, index);
    }

    @Nullable
    private CompoundTag fixedBrewTag(CauldronRitualRecipe recipe) {
        List<Offering> offered = fixedOfferings(recipe);
        if (offered.isEmpty()) return null;
        var tag = BrewMixture.fixedRitualBottleTag(offeringStacks(offered), brewColor, recipe.id(), fixedBasePower(recipe));
        tag.put("Items", saveOfferings(offered));
        return tag;
    }

    private int fixedBasePower(CauldronRitualRecipe recipe) {
        var offerings = ingredients;
        if (legacyRitualPending && level instanceof ServerLevel server) {
            offerings = ingredients.subList(0, pendingTriggerIndex(server));
        }
        return recipe.batchPower(offeringStacks(offerings));
    }

    private List<Offering> fixedOfferings(CauldronRitualRecipe recipe) {
        if (legacyRitualPending) return ingredients;
        Offering trigger = ritualTrigger;
        if (trigger == null) {
            ItemStack[] knownTriggers = recipe.trigger().getItems();
            if (knownTriggers.length == 0) return List.of();
            trigger = new Offering(knownTriggers[0], null);
        }
        List<Offering> offered = new ArrayList<>(ingredients);
        offered.add(fixedTriggerPosition < 0 ? offered.size() : fixedTriggerPosition, trigger);
        return offered;
    }

    private static ItemStack filledBottle(CompoundTag bottled) {
        ItemStack bottle = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        bottle.setTag(bottled);
        return bottle;
    }

    public int comparatorSignal() {
        return comparatorSignal;
    }

    private void refreshIndicators(ServerLevel server, boolean sync) {
        if (isRemoved()) return;
        int signal = 0;
        int sparks = 0;
        if (!tank.isEmpty()) {
            signal = isBoiling() ? 6 : 3;
            if (isBoiling()) {
                var recipe = observedFixedRecipe(server);
                boolean hasPower = recipe != null && !recipe.legacy() ? hasAltarPower(server, recipe) : powered;
                BrewMixture contents = comparatorMixture(recipe);
                if (contents != null && contents.hasEffects()) {
                    signal = hasPower ? (contents.used() == contents.capacity() ? 15 : 12) : 9;
                }
                if (hasPower) sparks = ritualSparkCount(ritualTicks);
            }
        }
        if (signal != comparatorSignal) {
            comparatorSignal = signal;
            server.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
        if (sync || sparks != sparkCount) {
            sparkCount = sparks;
            BlockState state = getBlockState();
            server.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private int ritualSparkCount(int progress) {
        return 1 + Mth.clamp(ritual == null ? progress : progress / 10, 0, 5);
    }

    @Nullable
    private BrewMixture comparatorMixture(@Nullable CauldronRitualRecipe recipe) {
        if (recipe != null && recipe.legacy()) {
            return BrewMixture.fixedMixture(offeringStacks(fixedOfferings(recipe)), brewColor);
        }
        return ritual == null ? mixture : null;
    }

    private boolean hasAltarPower(ServerLevel server, @Nullable CauldronRitualRecipe recipe) {
        if (ritualTicks == RITUAL_REFUND || recipe == null
                && (ritual != null || legacyRitualPending && !canCastWith(stackAt(pendingTriggerIndex(server))))) return false;

        double power;
        if (recipe != null) {
            int basePower = fixedBasePower(recipe);
            power = basePower;
            if (recipe.legacy()) power = legacyPowerRequirement(server, basePower);
            else if (basePower != 0) power = requiredFixedPower(recipe, basePower, CastCircles.around(server, worldPosition));
        } else if (mixture != null) {
            power = legacyPowerRequirement(server, mixture.power());
        } else {
            return brewColor != null;
        }
        if (power == 0) return true;
        if (power < 0) return false;
        AltarBlockEntity altar = AltarPower.nearest(server, worldPosition);
        return altar != null && altar.power() >= power;
    }

    public int clientBrewColor() {
        return clientBrewColor;
    }

    public double clientFillFraction() {
        return clientAmount / (double) CAPACITY;
    }

    public int sparkCount() {
        return sparkCount;
    }

    private int visibleBrewColor() {
        return brewColor == null ? -1 : brewColor & 0xFFFFFF;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Color", visibleBrewColor());
        tag.putInt("Amount", tank.getFluidAmount());
        tag.putInt("Sparks", sparkCount);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        int oldColor = clientBrewColor;
        clientBrewColor = tag.getInt("Color");
        clientAmount = tag.getInt("Amount");
        sparkCount = tag.getInt("Sparks");
        if (oldColor != clientBrewColor && level != null && level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) handleUpdateTag(packet.getTag());
    }

    private boolean isBoiling() {
        return !tank.isEmpty() && heatTicks >= BOIL_TICKS;
    }

    private int visibleWaterLevel() {
        return Mth.clamp(Mth.ceil(tank.getFluidAmount() / (float) BUCKET), 0, 3);
    }

    private boolean completeRitual(ServerLevel server, BlockPos pos, CauldronRitualRecipe recipe,
                                   @Nullable AltarBlockEntity altar, int power) {
        if (recipe.legacy() && recipe.summon() != null) {
            if (altar != null && !altar.consumePower(power)) return false;
            BrewRituals.summonFromCauldron(server, pos, recipe.summon());
        } else if (!produceRitualResult(server, pos, recipe, altar, power)) {
            return false;
        }
        clearMixture();
        if (recipe.legacy()) {
            WitcheryNetwork.sendCauldronRitualComplete(server, pos);
        } else {
            boolean summoned = recipe.summon() != null;
            server.sendParticles(summoned ? ParticleTypes.EXPLOSION : ParticleTypes.POOF,
                    pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 8, 0.25, 0.2, 0.25, 0.02);
            server.playSound(null, pos, summoned ? SoundEvents.WITHER_SPAWN : SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS, 0.7F, summoned ? 1 : 1.3F);
        }
        return true;
    }

    private boolean produceRitualResult(ServerLevel server, BlockPos pos, CauldronRitualRecipe recipe,
                                        @Nullable AltarBlockEntity altar, int power) {
        Entity result;
        if (recipe.summon() != null) {
            var summon = recipe.summon().create(server);
            if (summon == null) return false;
            summon.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            if (server.getBlockCollisions(summon, summon.getBoundingBox()).iterator().hasNext()) return false;
            if (summon instanceof Mob mob) mob.setPersistenceRequired();
            if (summon instanceof Leonard leonard) leonard.beginSummoning();
            result = summon;
        } else {
            ItemEntity drop = new ItemEntity(server, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                    recipe.getResultItem(server.registryAccess()).copy(), 0, 0.2, 0);
            drop.setDefaultPickUpDelay();
            result = drop;
        }
        if (!server.addFreshEntity(result)) return false;
        if (altar != null && !altar.consumePower(power)) {
            result.discard();
            return false;
        }
        return true;
    }

    private void clearMixture() {
        ritualTicks = 0;
        heatTicks = 0;
        emptyTank();
    }

    private void emptyTank() {
        // A withdrawal leaves the tile's heat and completed pulses to the normal tick checks.
        if (ritual != null && ritualTicks > 0) {
            ritualTicks = legacyFixedRecipe() != null ? ritualTicks / RITUAL_PULSE_INTERVAL : 0;
        }
        ritualTicks = Math.max(0, ritualTicks);
        ingredients.clear();
        mixture = null;
        brewColor = null;
        ritual = null;
        ritualTrigger = null;
        fixedTriggerPosition = -1;
        brewRitual = false;
        legacyRitualPending = false;
        powered = false;
        tank.setFluid(FluidStack.EMPTY);
        publishTankContents();
    }

    private void publishTankContents() {
        updateState();
        if (level instanceof ServerLevel server) refreshIndicators(server, true);
        setChanged();
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
            var fluid = "witchery:brew".equals(legacyFluid) ? WitcheryFluids.getRequiredSource("brew").get() : Fluids.WATER;
            tank.setFluid(amount > 0 ? new FluidStack(fluid, amount) : FluidStack.EMPTY);
        } else {
            tank.readFromNBT(tag.getCompound("Tank"));
        }
        ingredients.clear();
        ListTag stored = tag.contains("Ingredients", Tag.TAG_LIST) ? tag.getList("Ingredients", Tag.TAG_COMPOUND)
                : originalBrew.getList("Items", Tag.TAG_COMPOUND);
        ingredients.addAll(loadOfferings(stored));
        List<ItemStack> stacks = offeringStacks(ingredients);
        heatTicks = Mth.clamp(tag.contains("HeatTicks", Tag.TAG_ANY_NUMERIC)
                ? tag.getInt("HeatTicks") : tag.getInt("TicksHeated"), 0, BOIL_TICKS);
        powered = tag.getBoolean("Powered");
        ritual = tag.contains("Ritual", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("Ritual")) : null;
        ritualTrigger = ritual != null && tag.contains("RitualTrigger", Tag.TAG_COMPOUND)
                ? Offering.load(tag.getCompound("RitualTrigger")) : null;
        fixedTriggerPosition = ritual != null && tag.contains("RitualTriggerPosition", Tag.TAG_INT)
                ? Mth.clamp(tag.getInt("RitualTriggerPosition"), -1, ingredients.size()) : -1;
        brewColor = null;
        if (!tank.isEmpty()) {
            brewColor = BrewColor.saved(tag);
            if (brewColor == null) brewColor = BrewColor.saved(originalBrew);
            if (brewColor == null) brewColor = BrewColor.saved(tank.getFluid().getTag());
            if (brewColor == null && (!ingredients.isEmpty() || ritualTrigger != null)) {
                brewColor = offeringColor(stacks);
                if (ritualTrigger != null && !ritualTrigger.stack().isEmpty()) {
                    brewColor = BrewColor.append(brewColor, ritualTrigger.stack());
                }
            }
        }
        mixture = BrewMixture.fromStoredOfferings(stacks, brewColor);
        legacyRitualPending = ritual == null && !tank.isEmpty() && !ingredients.isEmpty()
                && (tag.getBoolean("LegacyRitualPending") || originalBrew.getBoolean("RitualTriggered"));
        boolean savedCast = ritual == null && !legacyRitualPending && tag.getBoolean("RitualTriggered");
        brewRitual = savedCast && canCastWith(stackAt(lastBrewAction()));
        if (savedCast && !brewRitual && !ingredients.isEmpty() && stackAt(ingredients.size() - 1).isEmpty()) {
            legacyRitualPending = true;
        }
        ritualTicks = Math.max(brewRitual ? RITUAL_REFUND : 0, tag.getInt("RitualTicks"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        tag.put("Ingredients", saveOfferings(ingredients));
        if (brewColor != null) tag.putInt("Color", brewColor);
        tag.putInt("HeatTicks", heatTicks);
        tag.putBoolean("Powered", powered);
        if (ritual != null) {
            tag.putString("Ritual", ritual.toString());
            if (ritualTrigger != null) tag.put("RitualTrigger", ritualTrigger.save());
            if (fixedTriggerPosition >= 0) tag.putInt("RitualTriggerPosition", fixedTriggerPosition);
        }
        if (brewRitual) tag.putBoolean("RitualTriggered", true);
        if (legacyRitualPending) tag.putBoolean("LegacyRitualPending", true);
        if (casting() || ritualTicks > 0) {
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
