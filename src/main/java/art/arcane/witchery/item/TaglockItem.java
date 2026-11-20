package art.arcane.witchery.item;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class TaglockItem extends Item {
    private static final String PLAYER_NAME = "WITCPlayer1";
    private static final String PLAYER_UUID = "WITCPlayerUUID1";
    private static final String CREATURE_NAME = "WITCDisplayName1";
    private static final String CREATURE_MOST = "WITCEntityIDm1";
    private static final String CREATURE_LEAST = "WITCEntityIDl1";
    private static final List<String> SIGNATURE_KEYS = List.of(PLAYER_NAME, PLAYER_UUID, CREATURE_NAME, CREATURE_MOST, CREATURE_LEAST);

    public TaglockItem() {
        super(new Properties().stacksTo(16));
    }

    public static boolean isBound(ItemStack stack) {
        return isBound(stack, 1);
    }

    public static boolean isBound(ItemStack stack, int index) {
        CompoundTag tag = stack.getTag();
        return tag != null && (!tag.getString(indexed(PLAYER_NAME, index)).isEmpty()
                || tag.contains(indexed(CREATURE_MOST, index), Tag.TAG_LONG)
                && tag.contains(indexed(CREATURE_LEAST, index), Tag.TAG_LONG));
    }

    public static boolean isBoundTo(ItemStack stack, ServerPlayer player) {
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.getString(PLAYER_NAME).isEmpty()) return false;
        return tag.hasUUID(PLAYER_UUID) ? tag.getUUID(PLAYER_UUID).equals(player.getUUID())
                : tag.getString(PLAYER_NAME).equalsIgnoreCase(player.getGameProfile().getName());
    }

    @Nullable
    public static LivingEntity resolveTarget(ServerLevel level, ItemStack stack) {
        return resolveTarget(level, stack, 1);
    }

    @Nullable
    public static LivingEntity resolveTarget(ServerLevel level, ItemStack stack, int index) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;
        String playerName = tag.getString(indexed(PLAYER_NAME, index));
        if (!playerName.isEmpty()) {
            var players = level.getServer().getPlayerList();
            String uuid = indexed(PLAYER_UUID, index);
            return tag.hasUUID(uuid) ? players.getPlayer(tag.getUUID(uuid)) : players.getPlayerByName(playerName);
        }
        String most = indexed(CREATURE_MOST, index);
        String least = indexed(CREATURE_LEAST, index);
        if (tag.contains(most, Tag.TAG_LONG) && tag.contains(least, Tag.TAG_LONG)) {
            UUID id = new UUID(tag.getLong(most), tag.getLong(least));
            for (ServerLevel world : level.getServer().getAllLevels()) {
                if (world.getEntity(id) instanceof LivingEntity living) return living;
            }
        }
        return null;
    }

    @Override
    public Component getName(ItemStack stack) {
        String name = boundName(stack);
        return name.isEmpty() ? super.getName(stack) : Component.translatable("item.witchery.taglockkit.named", name);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        String name = boundName(stack);
        Component description = name.isEmpty() ? Component.translatable("item.witchery.taglockkit.unbound")
                : Component.translatable("item.witchery.taglockkit.bound", name);
        tooltip.add(description.copy().withStyle(ChatFormatting.GRAY));
    }

    public static String boundName(ItemStack stack) {
        return boundName(stack, 1);
    }

    public static String boundName(ItemStack stack, int index) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return "";
        String playerName = tag.getString(indexed(PLAYER_NAME, index));
        return !playerName.isEmpty() ? playerName : tag.getString(indexed(CREATURE_NAME, index));
    }

    public static void copyBinding(ItemStack source, ItemStack destination) {
        copyBinding(source, destination, 1);
    }

    public static void copyBinding(ItemStack source, ItemStack destination, int index) {
        CompoundTag signature = source.getTag();
        if (signature == null) return;
        CompoundTag signed = destination.getOrCreateTag();
        for (String key : SIGNATURE_KEYS) {
            Tag value = signature.get(key);
            if (value == null) signed.remove(indexed(key, index));
            else signed.put(indexed(key, index), value.copy());
        }
    }

    public static void clearBinding(ItemStack stack, int index) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        for (String key : SIGNATURE_KEYS) {
            tag.remove(indexed(key, index));
        }
    }

    private static String indexed(String key, int index) {
        return index == 1 ? key : key.substring(0, key.length() - 1) + index;
    }

    public InteractionResult collectFromEntity(Player collector, InteractionHand hand, LivingEntity target) {
        if (!(target instanceof Mob || target instanceof Player)
                || target.getType() == WitcheryEntities.LEGACY_ENTITIES.get("treefyd").get()
                || target.getType() == WitcheryEntities.LEGACY_ENTITIES.get("imp").get()
                || target.getType() == WitcheryEntities.LEGACY_ENTITIES.get("wingedmonkey").get() && !collector.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (collector instanceof ServerPlayer player) {
            if (target instanceof ServerPlayer other && (restricted(player, other) || !sneakSucceeded(player, other))) {
                player.displayClientMessage(Component.translatable("message.witchery.taglock.failed").withStyle(ChatFormatting.RED), true);
                other.displayClientMessage(Component.translatable("message.witchery.taglock.discovered").withStyle(ChatFormatting.GREEN), true);
            } else {
                bind(player, hand, target);
            }
        }
        return InteractionResult.sidedSuccess(collector.level().isClientSide());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockState state = level.getBlockState(clicked);
        if (!(state.getBlock() instanceof BedBlock)) return InteractionResult.PASS;
        if (context.getPlayer() instanceof ServerPlayer player) {
            if (player.isShiftKeyDown()) {
                bind(player, context.getHand(), player);
            } else {
                BlockPos head = bedHead(state, clicked);
                List<ServerPlayer> candidates = player.serverLevel().players().stream()
                        .filter(candidate -> head.equals(candidate.getRespawnPosition())
                                && candidate.getRespawnDimension().equals(level.dimension()))
                        .sorted(Comparator.comparing(candidate -> candidate.getGameProfile().getName())).toList();
                if (candidates.isEmpty()) {
                    failureSound(player);
                } else {
                    ServerPlayer next = candidates.get(0);
                    CompoundTag binding = stack.getTag();
                    String previous = binding == null ? "" : binding.getString(PLAYER_NAME);
                    for (int index = 0; index < candidates.size(); index++) {
                        if (candidates.get(index).getGameProfile().getName().equals(previous)) {
                            next = candidates.get((index + 1) % candidates.size());
                            break;
                        }
                    }
                    if (restricted(player, next)) failureSound(player);
                    else bind(player, context.getHand(), next);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static BlockPos bedHead(BlockState state, BlockPos position) {
        return state.getValue(BedBlock.PART) == BedPart.HEAD ? position : position.relative(state.getValue(BedBlock.FACING));
    }

    private static boolean restricted(ServerPlayer collector, ServerPlayer target) {
        return collector != target && (WitcheryServerConfig.RESTRICT_TAGLOCK_COLLECTION_ON_NON_PVP_SERVERS.get()
                && !collector.server.isPvpAllowed()
                || WitcheryServerConfig.RESTRICT_TAGLOCK_COLLECTION_FOR_OPS.get()
                && collector.server.getPlayerList().isOp(target.getGameProfile()));
    }

    private static boolean sneakSucceeded(ServerPlayer collector, ServerPlayer target) {
        boolean sameFacing = Math.abs(Mth.wrapDegrees(collector.getYRot() - target.getYRot())) < 45;
        double chance;
        if (sameFacing) chance = collector.isShiftKeyDown() ? 0.6 : 0.3;
        else chance = (collector.isShiftKeyDown() ? 0.1 : 0.01) + (collector.isInvisible() ? 0.1 : 0);
        return collector.getRandom().nextDouble() < chance;
    }

    private static void bind(ServerPlayer collector, InteractionHand hand, LivingEntity target) {
        ItemStack held = collector.getItemInHand(hand);
        ItemStack filled = held.getCount() == 1 ? held : held.copyWithCount(1);
        CompoundTag tag = filled.getOrCreateTag();
        clearBinding(filled, 1);
        if (target instanceof ServerPlayer player) {
            tag.putString(PLAYER_NAME, player.getGameProfile().getName());
            tag.putUUID(PLAYER_UUID, player.getUUID());
        } else if (target instanceof Mob mob) {
            UUID uuid = mob.getUUID();
            tag.putLong(CREATURE_MOST, uuid.getMostSignificantBits());
            tag.putLong(CREATURE_LEAST, uuid.getLeastSignificantBits());
            tag.putString(CREATURE_NAME, mob.getDisplayName().getString());
            mob.setPersistenceRequired();
        }
        if (filled != held) {
            held.shrink(1);
            if (!collector.getInventory().add(filled)) collector.drop(filled, false);
        }
        collector.serverLevel().playSound(null, collector.getX(), collector.getY(), collector.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 0.5F);
    }

    private static void failureSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 0.6F, 1);
    }
}
