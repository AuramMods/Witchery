package art.arcane.witchery.block.entity;

import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class CursedControlBlockEntity extends BlockEntity {
    @Nullable
    private CompoundTag effect;
    @Nullable
    private BrewMixture mixture;
    @Nullable
    private UUID thrower;
    private String throwerName = "";
    private int count;
    private int color;
    private int duration;
    private int expansion;

    public CursedControlBlockEntity(BlockEntityType<CursedControlBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void charge(CompoundTag brew, BrewMixture payload, int tint, @Nullable Entity caster) {
        if (effect != null && effect.getList("Items", Tag.TAG_COMPOUND).equals(brew.getList("Items", Tag.TAG_COMPOUND))) {
            if (count < Integer.MAX_VALUE) count++;
        } else {
            effect = brew.copy();
            mixture = payload;
            count = 1;
            color = tint;
            duration = 5 + 5 * payload.lifetime() * payload.lifetime();
            expansion = 4 + payload.extent();
            thrower = caster != null ? caster.getUUID() : null;
            throwerName = caster != null ? caster.getScoreboardName() : "";
        }
        setChanged();
    }

    public boolean spend(ServerLevel server, Entity target) {
        if (mixture == null || count <= 0) return false;
        if (target instanceof LivingEntity living) {
            Entity owner = thrower != null ? server.getEntity(thrower) : null;
            if (owner == null && thrower != null) owner = server.getServer().getPlayerList().getPlayer(thrower);
            if (owner == null && !throwerName.isEmpty()) owner = server.getServer().getPlayerList().getPlayerByName(throwerName);
            ServerPlayer caster = owner instanceof ServerPlayer player && player.serverLevel() == server
                    ? player : FakePlayerFactory.getMinecraft(server);
            mixture.applyAt(living, caster, caster, 1, 1, false, living.position());
            WitcheryNetwork.sendTriggeredBrewCue(server, living.position());
        }
        count--;
        setChanged();
        return count == 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (effect != null) tag.put("Effect", effect.copy());
        if (thrower != null) tag.putUUID("ThrowerUUID", thrower);
        if (!throwerName.isEmpty()) tag.putString("Thrower", throwerName);
        tag.putInt("Count", count);
        tag.putInt("Color", color);
        tag.putInt("Duration", duration);
        tag.putInt("Expansion", expansion);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        effect = tag.contains("Effect", Tag.TAG_COMPOUND) ? tag.getCompound("Effect").copy() : null;
        mixture = BrewMixture.from(effect);
        count = Math.max(0, tag.getInt("Count"));
        color = tag.getInt("Color") & 0xFFFFFF;
        duration = tag.getInt("Duration");
        expansion = tag.getInt("Expansion");
        thrower = tag.hasUUID("ThrowerUUID") ? tag.getUUID("ThrowerUUID") : null;
        throwerName = tag.getString("Thrower");
    }
}
