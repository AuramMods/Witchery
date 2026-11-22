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
import art.arcane.witchery.item.BiomeNoteItem;
import net.minecraft.core.registries.Registries;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryFluids;
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
    private int brewColor = -1;
    private int heatTicks;
    private int ritualTicks;
    private boolean brewRitual;
    private boolean legacyRitualPending;
    private int comparatorSignal;
    private int clientBrewColor = -1;
    private boolean clientRitual;

    private final CauldronTank tank = new CauldronTank();
    private LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> tank);

    private record TransportedBrew(List<ItemStack> ingredients, int color, @Nullable BrewMixture mixture, boolean ritualTriggered) {
        @Nullable
        static TransportedBrew from(FluidStack resource) {
            CompoundTag tag = resource.getTag();
            if (tag == null) return new TransportedBrew(List.of(), -1, null, false);
            List<ItemStack> inputs = new ArrayList<>();
            for (Tag entry : tag.getList("Items", Tag.TAG_COMPOUND)) {
                ItemStack item = BrewMixture.loadIngredient((CompoundTag) entry);
                if (item.isEmpty()) return null;
                inputs.add(item);
            }
            int color = tag.contains("Color", Tag.TAG_ANY_NUMERIC) ? tag.getInt("Color") & 0xFFFFFF : -1;
            return new TransportedBrew(inputs, color, BrewMixture.from(inputs, color),
                    !inputs.isEmpty() && tag.getBoolean("RitualTriggered"));
        }

        int visibleColor() {
            return mixture == null ? color : mixture.color();
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
            if (mixture != null) return new FluidStack(brew, getFluidAmount(), mixture.bottleTag(ingredients));
            if (!ingredients.isEmpty() || brewColor >= 0) {
                return new FluidStack(brew, getFluidAmount(), BrewMixture.ingredientTag(ingredients, brewColor));
            }
            return getFluid().copy();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (ritual != null || brewRitual || resource.isEmpty() || !isFluidValid(resource) || getSpace() == 0) return 0;
            var current = getFluidInTank(0);
            if (!current.isEmpty() && current.getFluid() != resource.getFluid()) return 0;
            TransportedBrew brew = null;
            if (resource.getFluid() != Fluids.WATER) {
                brew = TransportedBrew.from(resource);
                if (brew == null || !isEmpty() && !matchesBrew(brew)) return 0;
            }
            int filled = Math.min(getSpace(), resource.getAmount());
            if (action.execute()) {
                if (brew != null) {
                    if (isEmpty()) {
                        ingredients.addAll(brew.ingredients());
                        brewColor = brew.color();
                        mixture = brew.mixture();
                    }
                    legacyRitualPending |= brew.ritualTriggered();
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
            if (casting() || amount <= 0 || isEmpty()) return FluidStack.EMPTY;
            FluidStack drained = getFluidInTank(0);
            drained.setAmount(Math.min(amount, getFluidAmount()));
            if (action.execute()) {
                int remaining = getFluidAmount() - drained.getAmount();
                if (remaining == 0) clearMixture();
                else {
                    setFluid(new FluidStack(getFluid().getFluid(), remaining));
                    publishTankContents();
                }
            }
            return drained;
        }
    }

    private boolean matchesBrew(TransportedBrew incoming) {
        int visibleColor = mixture == null ? brewColor : mixture.color();
        if (visibleColor != incoming.visibleColor() || ingredients.size() != incoming.ingredients().size()) return false;
        for (int index = 0; index < ingredients.size(); index++) {
            if (!ItemStack.matches(ingredients.get(index), incoming.ingredients().get(index))) return false;
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
        int oldHeat = cauldron.heatTicks;
        int oldProgress = cauldron.ritualTicks;
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
        return ritual != null || brewRitual || legacyRitualPending;
    }

    private void restoreLegacyRitual(ServerLevel server) {
        ItemStack trigger = lastIngredient();
        if (canCastWith(trigger)) {
            brewRitual = true;
        } else {
            var recipes = server.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get())
                    .stream().filter(CauldronRitualRecipe::legacy).toList();
            var priorOfferings = ingredients.subList(0, ingredients.size() - 1);
            CauldronRitualRecipe recipe = matchingRitual(recipes, priorOfferings, trigger);
            if (recipe != null) {
                ritual = recipe.id();
                ritualTicks = (int) Math.min(recipe.time(), 10L * ritualTicks);
                ingredients.remove(ingredients.size() - 1);
            } else {
                if (!refundRitualTrigger(server)) return;
                ritualTicks = 0;
                ritualParticles(server, ParticleTypes.SMOKE);
            }
            mixture = BrewMixture.from(ingredients, brewColor);
        }
        legacyRitualPending = false;
        publishTankContents();
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
            if (!refundRitualTrigger(server)) return;
        }
        brewRitual = false;
        ritualTicks = 0;
        onIngredientAdded(server);
        ritualParticles(server, ParticleTypes.SMOKE);
    }

    private boolean refundRitualTrigger(ServerLevel server) {
        ItemEntity refund = new ItemEntity(server, worldPosition.getX() + 0.03, worldPosition.getY() + 1.2,
                worldPosition.getZ() + 0.03, lastIngredient().copy(), -0.08, 0.1, -0.08);
        refund.setDefaultPickUpDelay();
        if (!server.addFreshEntity(refund)) return false;
        ingredients.remove(ingredients.size() - 1);
        return true;
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

    public void acceptDroppedItem(ItemEntity item) {
        ItemStack offered = item.getItem();
        if (!(level instanceof ServerLevel server) || offered.isEmpty() || tank.isEmpty()) return;

        if (WitcheryIngredient.GYPSUM.matches(offered)) {
            item.discard();
            clearMixture();
            reagentSmoke(server);
            server.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 2.0F);
        } else if (WitcheryIngredient.QUICKLIME.matches(offered)) {
            detonate(server, item);
        } else if (addIngredient(server, offered)) {
            ItemStack remainder = offered.getCraftingRemainingItem();
            offered.shrink(1);
            if (offered.isEmpty()) item.discard();
            if (!remainder.isEmpty()) Block.popResource(server, worldPosition.above(), remainder);
        }
    }

    private void detonate(ServerLevel server, ItemEntity item) {
        if (ingredients.isEmpty() && brewColor < 0 && ritual == null) return;
        Player nearest = server.getEntitiesOfClass(Player.class, item.getBoundingBox().inflate(5),
                        player -> player.isAlive() && !player.isSpectator()).stream()
                .min(Comparator.comparingDouble(item::distanceToSqr)).orElse(null);
        if (nearest == null) return;

        var recipes = server.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get());
        long actions = ingredients.stream().filter(stack -> BrewMixture.hasAction(stack)
                || recipes.stream().anyMatch(recipe -> recipe.trigger().test(stack)
                        || recipe.ingredients().stream().anyMatch(ingredient -> ingredient.test(stack)))).count();
        // Fixed recipes store their trigger as a recipe ID. Other rituals keep it in the ingredient list.
        if (ritual != null) actions++;
        float power = Math.min(1.0F + 0.5F * actions, 10.0F);
        item.discard();
        clearMixture();
        Vec3 center = Vec3.atCenterOf(worldPosition).add(0, 1, 0);
        server.explode(nearest, center.x, center.y, center.z, power, Level.ExplosionInteraction.NONE);
        reagentSmoke(server);
    }

    private void reagentSmoke(ServerLevel server) {
        server.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.6,
                worldPosition.getZ() + 0.5, 8, 0.22, 0.16, 0.22, 0.03);
    }

    private boolean addIngredient(ServerLevel server, ItemStack offered) {
        if (!isBoiling() || casting()) {
            return false;
        }
        int wool = BrewMixture.woolColor(offered);
        if (wool >= 0) {
            brewColor = wool;
            onIngredientAdded(server);
            return true;
        }
        if (tank.getFluidAmount() == CAPACITY && canCastWith(offered)) {
            ingredients.add(offered.copyWithCount(1));
            brewRitual = true;
            ritualTicks = 0;
            onIngredientAdded(server, offered);
            return true;
        }
        List<CauldronRitualRecipe> recipes = server.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get());
        if (tank.getFluidAmount() == CAPACITY) {
            var recipe = matchingRitual(recipes, ingredients, offered);
            if (recipe != null) {
                ritual = recipe.id();
                ritualTicks = 0;
                onIngredientAdded(server, offered);
                return true;
            }
        }
        for (CauldronRitualRecipe recipe : recipes) {
            if (recipe.acceptsIngredient(ingredients, offered)) {
                ingredients.add(offered.copyWithCount(1));
                onIngredientAdded(server, offered);
                return true;
            }
        }
        if (BrewMixture.canAdd(ingredients, offered)) {
            if (offered.getItem() instanceof BiomeNoteItem && !server.registryAccess().registryOrThrow(Registries.BIOME)
                    .containsKey(BiomeNoteItem.biomeId(offered))) return false;
            BrewMixture.add(ingredients, offered);
            onIngredientAdded(server, offered);
            return true;
        }
        return false;
    }

    @Nullable
    private static CauldronRitualRecipe matchingRitual(List<CauldronRitualRecipe> recipes, List<ItemStack> offerings, ItemStack trigger) {
        return recipes.stream().filter(recipe -> recipe.canStart(offerings, trigger))
                .max(Comparator.comparingInt(CauldronRitualRecipe::priority)
                        .thenComparingInt(recipe -> recipe.ingredients().size())).orElse(null);
    }

    private void onIngredientAdded(ServerLevel server) {
        mixture = BrewMixture.from(ingredients, brewColor);
        splash(server);
        refreshComparator();
        BlockState state = getBlockState();
        server.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        setChanged();
    }

    private void onIngredientAdded(ServerLevel server, ItemStack offered) {
        if (brewColor >= 0) brewColor = BrewMixture.blendColor(brewColor, offered);
        onIngredientAdded(server);
    }

    public boolean bottle(ServerPlayer player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server) || !isBoiling() || ritual != null || legacyRitualPending || ritualTicks == RITUAL_REFUND
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
            tank.setFluid(new FluidStack(tank.getFluid().getFluid(), remaining));
            publishTankContents();
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
        tag.putInt("Color", mixture != null ? mixture.color() : brewColor);
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
        brewColor = -1;
        ritual = null;
        brewRitual = false;
        legacyRitualPending = false;
        ritualTicks = 0;
        heatTicks = 0;
        tank.setFluid(FluidStack.EMPTY);
        publishTankContents();
    }

    private void publishTankContents() {
        updateState();
        refreshComparator();
        if (level instanceof ServerLevel server && !isRemoved()) {
            BlockState state = getBlockState();
            server.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
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
            var fluid = "witchery:brew".equals(legacyFluid) ? WitcheryFluids.getRequiredSource("brew").get() : Fluids.WATER;
            tank.setFluid(amount > 0 ? new FluidStack(fluid, amount) : FluidStack.EMPTY);
        } else {
            tank.readFromNBT(tag.getCompound("Tank"));
        }
        ingredients.clear();
        ListTag stored = tag.contains("Ingredients", Tag.TAG_LIST) ? tag.getList("Ingredients", Tag.TAG_COMPOUND)
                : originalBrew.getList("Items", Tag.TAG_COMPOUND);
        boolean loadedTrigger = false;
        for (int index = 0; index < stored.size(); index++) {
            ItemStack stack = BrewMixture.loadIngredient(stored.getCompound(index));
            if (!stack.isEmpty()) {
                ingredients.add(stack);
                loadedTrigger = index == stored.size() - 1;
            }
        }
        CompoundTag colorSource = tag.contains("Color", Tag.TAG_ANY_NUMERIC) ? tag : originalBrew;
        brewColor = !tank.isEmpty() && colorSource.contains("Color", Tag.TAG_ANY_NUMERIC)
                ? colorSource.getInt("Color") & 0xFFFFFF : -1;
        mixture = BrewMixture.from(ingredients, brewColor);
        heatTicks = Mth.clamp(tag.contains("HeatTicks", Tag.TAG_ANY_NUMERIC)
                ? tag.getInt("HeatTicks") : tag.getInt("TicksHeated"), 0, BOIL_TICKS);
        ritual = tag.contains("Ritual", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("Ritual")) : null;
        legacyRitualPending = ritual == null && !tank.isEmpty() && loadedTrigger
                && (tag.getBoolean("LegacyRitualPending") || originalBrew.getBoolean("RitualTriggered"));
        brewRitual = ritual == null && !legacyRitualPending && tag.getBoolean("RitualTriggered") && canCastWith(lastIngredient());
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
        if (brewColor >= 0) tag.putInt("Color", brewColor);
        tag.putInt("HeatTicks", heatTicks);
        if (ritual != null) {
            tag.putString("Ritual", ritual.toString());
        }
        if (brewRitual) tag.putBoolean("RitualTriggered", true);
        if (legacyRitualPending) tag.putBoolean("LegacyRitualPending", true);
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
