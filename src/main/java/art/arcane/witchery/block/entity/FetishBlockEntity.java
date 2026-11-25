package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.FetishBlock;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

public final class FetishBlockEntity extends BlockEntity {
    private static final int RANGE = 16;
    private final FetishAlarm alarm = new FetishAlarm();
    private int effectId;
    private int color;
    private boolean spectral;
    private long lastActivation;

    public FetishBlockEntity(BlockPos pos, BlockState state, String path) {
        super(WitcheryBlockEntities.fetish(path).get(), pos, state);
    }

    public static boolean enhancesPoppets(ServerPlayer player) {
        var chunks = player.serverLevel().getChunkSource();
        BlockPos pos = player.blockPosition();
        int maxX = SectionPos.blockToSectionCoord(pos.getX() + RANGE);
        int maxZ = SectionPos.blockToSectionCoord(pos.getZ() + RANGE);
        for (int x = SectionPos.blockToSectionCoord(pos.getX() - RANGE); x <= maxX; x++) {
            for (int z = SectionPos.blockToSectionCoord(pos.getZ() - RANGE); z <= maxZ; z++) {
                var chunk = chunks.getChunkNow(x, z);
                if (chunk == null) continue;
                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    if (!(entity instanceof FetishBlockEntity fetish) || fetish.isRemoved()
                            || fetish.effectId != FetishEffect.ENHANCED_POPPETS.id) continue;
                    BlockPos center = fetish.getBlockPos();
                    if (player.distanceToSqr(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5) <= RANGE * RANGE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FetishBlockEntity fetish) {
        if (!(level instanceof ServerLevel server) || (server.getGameTime() + pos.asLong()) % 20 != 0) return;
        FetishEffect effect = FetishEffect.byId(fetish.effectId);
        if (effect == null || effect.radius == 0) {
            fetish.setPowered(false);
            return;
        }

        var detection = fetish.alarm.detect(server, pos, effect.radius);
        if (detection.triggered() && (effect.cooldown == 0
                || server.getGameTime() - fetish.lastActivation > effect.cooldown)) {
            effect.activate(server, pos, ((FetishBlock) state.getBlock()).isLadderFetish(), detection.intruders());
            if (effect.cooldown > 0) {
                fetish.lastActivation = server.getGameTime();
                fetish.setChanged();
            }
        }
        fetish.setPowered(effect == FetishEffect.SCREAMER && detection.triggered());
    }

    private void setPowered(boolean powered) {
        BlockState state = getBlockState();
        if (level instanceof ServerLevel && state.hasProperty(FetishBlock.POWERED) && state.getValue(FetishBlock.POWERED) != powered) {
            level.setBlock(worldPosition, state.setValue(FetishBlock.POWERED, powered), Block.UPDATE_ALL);
            level.updateNeighborsAt(worldPosition.below(), state.getBlock());
        }
    }

    public void loadItem(ItemStack item) {
        CompoundTag tag = item.getTag();
        if (tag == null) return;
        CompoundTag data = tag.getCompound("TileData");
        readSettings(data);
        if (!data.contains("BlockColor", Tag.TAG_ANY_NUMERIC)) color = tag.getByte("BlockColor") & 15;
        if (effectId == 0) effectId = tag.getInt("WITCSpiritEffect");
        sync();
    }

    public void saveItem(ItemStack item) {
        CompoundTag tag = item.getOrCreateTag();
        tag.putByte("BlockColor", (byte) color);
        tag.putInt("WITCSpiritEffect", effectId);
        tag.put("TileData", saveSettings(new CompoundTag()));
    }

    public static int itemEffect(ItemStack item) {
        CompoundTag tag = item.getTag();
        if (tag == null) return 0;
        int saved = tag.getCompound("TileData").getInt("EffectTypeID");
        return saved == 0 ? tag.getInt("WITCSpiritEffect") : saved;
    }

    public static void setItemEffect(ItemStack item, FetishEffect effect) {
        item.getOrCreateTag().putInt("WITCSpiritEffect", effect.id);
        var data = item.getTagElement("TileData");
        if (data != null) data.putInt("EffectTypeID", effect.id);
    }

    public boolean isSpectral() {
        return spectral;
    }

    public static int itemColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        CompoundTag data = tag.getCompound("TileData");
        return (data.contains("BlockColor", Tag.TAG_ANY_NUMERIC) ? data.getByte("BlockColor") : tag.getByte("BlockColor")) & 15;
    }

    public int colorIndex() {
        return color;
    }

    public void dye(DyeColor dye) {
        color = dye.getId();
        updateSettings();
    }

    public void cycleAlarm(Player player) {
        alarm.cycle();
        updateSettings();
        showAlarm(player);
    }

    public void clearSubjects(Player player) {
        alarm.clearSubjects();
        updateSettings();
        showAlarm(player);
    }

    public boolean bindSubject(ServerLevel level, ItemStack stack, Player player) {
        boolean changed = !spectral && alarm.toggle(stack, level);
        if (changed) updateSettings();
        showAlarm(player);
        return changed;
    }

    private void showAlarm(Player player) {
        String key = "message.witchery.fetish." + alarm.mode().name().toLowerCase(Locale.ROOT);
        player.displayClientMessage(Component.translatable(key, alarm.subjects()), false);
    }

    private void updateSettings() {
        sync();
        if (level instanceof ServerLevel server && server.dimension().equals(WitcheryDimensions.DREAM)) {
            var overworld = server.getServer().overworld();
            if (overworld.hasChunkAt(worldPosition) && overworld.getBlockEntity(worldPosition) instanceof FetishBlockEntity copy
                    && copy.isSpectral() && copy.getBlockState().is(getBlockState().getBlock())) {
                copy.readSettings(saveSettings(new CompoundTag()));
                copy.sync();
            }
        }
    }

    public void projectFromDream(ServerLevel dream) {
        if (!dream.dimension().equals(WitcheryDimensions.DREAM)) return;
        ServerLevel overworld = dream.getServer().overworld();
        if (!overworld.hasChunkAt(worldPosition) || !overworld.getWorldBorder().isWithinBounds(worldPosition)
                || !overworld.getBlockState(worldPosition).canBeReplaced() || overworld.getBlockEntity(worldPosition) != null) return;
        BlockState state = getBlockState().setValue(FetishBlock.POWERED, false);
        if (overworld.setBlockAndUpdate(worldPosition, state)
                && overworld.getBlockEntity(worldPosition) instanceof FetishBlockEntity copy) {
            copy.spectral = true;
            copy.readSettings(saveSettings(new CompoundTag()));
            copy.sync();
        }
    }

    private void sync() {
        setChanged();
        if (level instanceof ServerLevel) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private void readSettings(CompoundTag tag) {
        effectId = tag.getInt("EffectTypeID");
        color = tag.getByte("BlockColor") & 15;
        alarm.load(tag);
    }

    private CompoundTag saveSettings(CompoundTag tag) {
        tag.putInt("EffectTypeID", effectId);
        tag.putByte("BlockColor", (byte) color);
        alarm.save(tag);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readSettings(tag);
        spectral = tag.getBoolean("Spectral");
        lastActivation = tag.getLong("LastActivation");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveSettings(tag);
        if (spectral) tag.putBoolean("Spectral", true);
        if (lastActivation != 0) tag.putLong("LastActivation", lastActivation);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Spectral", spectral);
        tag.putByte("BlockColor", (byte) color);
        tag.putInt("EffectTypeID", effectId);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, entity -> entity.getUpdateTag());
    }
}
