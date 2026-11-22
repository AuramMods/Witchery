package art.arcane.witchery.infusion;

import art.arcane.witchery.event.WitcheryBlockBreak;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public enum ImpContractEffects {
    FIERY_TOUCH("witchery.imp.firetouch"),
    EVAPORATION("witchery.imp.evaporation"),
    MELTING_TOUCH("witchery.im.meltingtouch");

    private static final String EFFECTS_TAG = "witchery.effects";
    private static final int DURATION = 12_000;
    private static final int UPDATE_INTERVAL = 20;
    private final String key;

    ImpContractEffects(String key) {
        this.key = key;
    }

    public void grant(ServerPlayer player) {
        CompoundTag playerData = persisted(player);
        CompoundTag effects = playerData.getCompound(EFFECTS_TAG);
        effects.putInt(key, DURATION);
        playerData.put(EFFECTS_TAG, effects);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, playerData);
    }

    public boolean isActive(ServerPlayer player) {
        return persisted(player).getCompound(EFFECTS_TAG).getInt(key) > 0;
    }

    public static void clear(ServerPlayer player) {
        CompoundTag playerData = persisted(player);
        CompoundTag effects = playerData.getCompound(EFFECTS_TAG);
        for (ImpContractEffects effect : values()) effects.remove(effect.key);
        if (effects.isEmpty()) playerData.remove(EFFECTS_TAG);
    }

    public static void tick(ServerPlayer player) {
        if (player.serverLevel().getGameTime() % UPDATE_INTERVAL != 3 || !player.isAlive()) return;
        CompoundTag playerData = persisted(player);
        CompoundTag effects = playerData.getCompound(EFFECTS_TAG);
        if (effects.isEmpty()) return;
        for (ImpContractEffects effect : values()) {
            int remaining = effects.getInt(effect.key);
            if (remaining > UPDATE_INTERVAL) effects.putInt(effect.key, remaining - UPDATE_INTERVAL);
            else effects.remove(effect.key);
        }
        if (effects.isEmpty()) playerData.remove(EFFECTS_TAG);
        else if (effects.getInt(EVAPORATION.key) > 0 && player.getRandom().nextInt(5) == 0) evaporate(player);
    }

    public static void touch(ServerPlayer player, BlockPos clicked, Direction face, boolean leftClick) {
        var level = player.serverLevel();
        var random = player.getRandom();
        if (face == null || !FIERY_TOUCH.isActive(player) || random.nextDouble() >= 0.2 || level.isEmptyBlock(clicked)) return;
        BlockPos pos = clicked.relative(face);
        if (leftClick) pos = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
        var fire = Blocks.FIRE.defaultBlockState();
        if (!level.isLoaded(pos) || !level.isEmptyBlock(pos) || level.isEmptyBlock(pos.below())
                || !fire.canSurvive(level, pos) || !level.mayInteract(player, pos)
                || ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, pos), face)) return;
        if (level.setBlockAndUpdate(pos, fire)) {
            level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1, 0.8F + random.nextFloat() * 0.4F);
        }
    }

    private static void evaporate(ServerPlayer player) {
        var level = player.serverLevel();
        BlockPos center = player.blockPosition();
        boolean evaporated = false;
        for (BlockPos column : BlockPos.betweenClosed(center.offset(-3, 0, -3), center.offset(3, 0, 3))) {
            for (int height = 2; height >= -1; height--) {
                BlockPos pos = column.above(height);
                if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 9 || !level.isLoaded(pos)) continue;
                var state = level.getBlockState(pos);
                if (!state.is(Blocks.WATER) || !level.isEmptyBlock(pos.above()) || !level.mayInteract(player, pos)
                        || WitcheryBlockBreak.isCanceled(level, pos, state, player)) continue;
                if (level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState())) {
                    evaporated = true;
                    level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                            16, 0.5, 0.4, 0.5, 0.02);
                }
            }
        }
        if (evaporated) {
            var random = player.getRandom();
            level.playSound(null, center, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1,
                    2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
        }
    }

    private static CompoundTag persisted(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }
}
