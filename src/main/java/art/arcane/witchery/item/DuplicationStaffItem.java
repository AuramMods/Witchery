package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.infusion.OtherwhereInfusion;
import art.arcane.witchery.world.DuplicationSchematic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;

public final class DuplicationStaffItem extends Item {
    private static final String MARKER = "Marker";
    private static final String SCHEMATIC = "SavedSchematic";
    private static final String ROTATION = "Rotation";

    public DuplicationStaffItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (context.getLevel() instanceof ServerLevel level) {
            BlockPos pos = context.getClickedPos();
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains(SCHEMATIC, Tag.TAG_COMPOUND)) {
                int rotation = player.isShiftKeyDown() ? 0 : (tag.getInt(ROTATION) + 1) & 3;
                if (DuplicationSchematic.place(level, pos.above(), tag.getCompound(SCHEMATIC), rotation)) {
                    tag.putInt(ROTATION, rotation);
                } else {
                    signal(level, pos, false);
                }
            } else {
                select(level, pos, stack, tag);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !stack.hasTag()) return InteractionResultHolder.pass(stack);
        if (player instanceof ServerPlayer server) {
            if (OtherwhereInfusion.rayTrace(server, 6).getType() == HitResult.Type.BLOCK) {
                return InteractionResultHolder.pass(stack);
            }
            stack.removeTagKey(MARKER);
            stack.removeTagKey(SCHEMATIC);
            level.playSound(null, player, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1, 1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void select(ServerLevel level, BlockPos pos, ItemStack stack, CompoundTag tag) {
        CompoundTag marker = tag.getCompound(MARKER);
        if (!marker.contains("posX", Tag.TAG_ANY_NUMERIC) || !marker.contains("posY", Tag.TAG_ANY_NUMERIC)
                || !marker.contains("posZ", Tag.TAG_ANY_NUMERIC)) {
            marker.putInt("posX", pos.getX());
            marker.putInt("posY", pos.getY());
            marker.putInt("posZ", pos.getZ());
            tag.put(MARKER, marker);
            signal(level, pos, true);
            return;
        }

        var first = new BlockPos(marker.getInt("posX"), marker.getInt("posY"), marker.getInt("posZ"));
        var saved = level.isInWorldBounds(first) ? DuplicationSchematic.capture(level, first, pos) : null;
        if (saved == null) {
            signal(level, pos, false);
            return;
        }
        var updated = tag.copy();
        updated.remove(MARKER);
        updated.put(SCHEMATIC, saved);
        if (updated.sizeInBytes() > DuplicationSchematic.ITEM_TAG_QUOTA - 64) {
            signal(level, pos, false);
            return;
        }
        stack.setTag(updated);
        signal(level, pos, true);
        if (WitcheryServerConfig.SAVE_DUP_STAFF_TEMPLATE.get()) {
            try {
                DuplicationSchematic.export(saved, FMLPaths.CONFIGDIR.get());
            } catch (IOException error) {
                Witchery.LOGGER.warn("Could not export the Staff of Duplication capture", error);
            }
        }
    }

    private static void signal(ServerLevel level, BlockPos pos, boolean success) {
        level.sendParticles(success ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.SMOKE,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.5, 0.5, 0.5, 0.02);
        level.playSound(null, pos, success ? SoundEvents.BUBBLE_COLUMN_BUBBLE_POP : SoundEvents.NOTE_BLOCK_SNARE.value(),
                SoundSource.PLAYERS, 1, 1);
    }
}
