package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.RiteHeartBlock;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryRecipes;
import art.arcane.witchery.ritual.ItemRiteRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RiteHeartBlockEntity extends BlockEntity {
    private static final int STEP_TICKS = 20;
    private final List<ItemStack> sacrificed = new ArrayList<>();
    @Nullable
    private ResourceLocation activeRite;
    @Nullable
    private UUID initiator;
    private int stepTicks;

    public RiteHeartBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.CIRCLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RiteHeartBlockEntity heart) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(RiteHeartBlock.POWERED)) {
            level.setBlock(pos, state.setValue(RiteHeartBlock.POWERED, powered), Block.UPDATE_CLIENTS);
            if (powered) {
                heart.toggle(server, null);
            }
        }
        heart.updateActiveState(server);
        if (heart.activeRite != null) {
            if (++heart.stepTicks >= STEP_TICKS) {
                heart.stepTicks = 0;
                heart.advance(server);
            }
            level.blockEntityChanged(pos);
        }
    }

    public void toggle(ServerLevel level, @Nullable Player player) {
        if (activeRite != null) {
            abort(level, "message.witchery.rite.cancelled");
            return;
        }
        BlockPos pos = getBlockPos();
        for (ItemRiteRecipe rite : level.getRecipeManager().getAllRecipesFor(WitcheryRecipes.ITEM_RITE.get())) {
            if (rite.hasCircles(level, pos) && rite.findSacrifices(level, pos, 0) != null) {
                activeRite = rite.id();
                initiator = player == null ? null : player.getUUID();
                stepTicks = 0;
                updateActiveState(level);
                setChanged();
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                return;
            }
        }
        if (player != null) {
            player.displayClientMessage(Component.translatable("message.witchery.rite.unknown"), true);
        }
        failSound(level);
    }

    private void advance(ServerLevel level) {
        if (!(level.getRecipeManager().byKey(activeRite).orElse(null) instanceof ItemRiteRecipe rite)
                || sacrificed.size() > rite.sacrifices().size()) {
            abort(level, "message.witchery.rite.unknown");
            return;
        }
        BlockPos pos = getBlockPos();
        if (sacrificed.size() == rite.sacrifices().size()) {
            finish(level, rite);
            return;
        }
        List<ItemEntity> remaining = rite.findSacrifices(level, pos, sacrificed.size());
        if (remaining == null) {
            abort(level, "message.witchery.rite.missing_ingredient");
            return;
        }
        ItemEntity entity = remaining.get(0);
        ItemStack offered = entity.getItem();
        sacrificed.add(offered.copyWithCount(1));
        if (offered.getCount() == 1) {
            entity.discard();
        } else {
            entity.setItem(offered.copyWithCount(offered.getCount() - 1));
        }
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, entity.getX(), entity.getY() + 0.2, entity.getZ(),
                8, 0.2, 0.22, 0.2, 0.02);
    }

    private void finish(ServerLevel level, ItemRiteRecipe rite) {
        BlockPos pos = getBlockPos();
        AltarBlockEntity altar = rite.power() == 0 ? null : AltarPower.nearest(level, pos);
        if (rite.power() > 0 && (altar == null || altar.power() < rite.power())) {
            abort(level, "message.witchery.rite.missing_power");
            return;
        }
        ItemEntity result = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                rite.result().copy(), 0, 0.3, 0);
        result.setDefaultPickUpDelay();
        if (!level.addFreshEntity(result)) {
            return;
        }
        if (altar != null && !altar.consumePower(rite.power())) {
            result.discard();
            abort(level, "message.witchery.rite.missing_power");
            return;
        }
        sacrificed.clear();
        clear(level);
        level.sendParticles(ParticleTypes.ENCHANT, result.getX(), result.getY(), result.getZ(), 16, 0.4, 0.4, 0.4, 0.05);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.9F, 1.35F);
    }

    private void abort(ServerLevel level, String message) {
        ServerPlayer player = initiator == null ? null : level.getServer().getPlayerList().getPlayer(initiator);
        if (player != null) {
            player.displayClientMessage(Component.translatable(message), true);
        }
        refundSacrifices(level);
        clear(level);
        failSound(level);
    }

    public void refundSacrifices(ServerLevel level) {
        for (ItemStack stack : sacrificed) {
            Block.popResource(level, getBlockPos(), stack);
        }
        sacrificed.clear();
    }

    private void clear(ServerLevel level) {
        activeRite = null;
        initiator = null;
        stepTicks = 0;
        updateActiveState(level);
        setChanged();
    }

    private void updateActiveState(ServerLevel level) {
        BlockPos pos = getBlockPos();
        BlockState current = level.getBlockState(pos);
        if (current.hasProperty(RiteHeartBlock.ACTIVE) && current.getValue(RiteHeartBlock.ACTIVE) != (activeRite != null)) {
            level.setBlock(pos, current.setValue(RiteHeartBlock.ACTIVE, activeRite != null), Block.UPDATE_CLIENTS);
        }
    }

    private void failSound(ServerLevel level) {
        level.playSound(null, getBlockPos(), SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 0.9F, 1.0F);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        activeRite = tag.contains("Rite", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("Rite")) : null;
        initiator = tag.hasUUID("Initiator") ? tag.getUUID("Initiator") : null;
        stepTicks = Mth.clamp(tag.getInt("StepTicks"), 0, STEP_TICKS - 1);
        sacrificed.clear();
        for (Tag entry : tag.getList("Sacrifices", Tag.TAG_COMPOUND)) {
            ItemStack stack = WitcheryIngredient.loadInventoryStack((CompoundTag) entry);
            if (!stack.isEmpty()) {
                sacrificed.add(stack);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (activeRite != null) {
            tag.putString("Rite", activeRite.toString());
            tag.putInt("StepTicks", stepTicks);
            if (initiator != null) {
                tag.putUUID("Initiator", initiator);
            }
        }
        ListTag held = new ListTag();
        for (ItemStack stack : sacrificed) {
            held.add(stack.save(new CompoundTag()));
        }
        tag.put("Sacrifices", held);
    }
}
