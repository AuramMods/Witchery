package art.arcane.witchery.integration;

import art.arcane.witchery.config.WitcheryCommonConfig;
import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.entity.Illusion;
import art.arcane.witchery.entity.WereVillager;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.callback.JadeRayTraceCallback;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@WailaPlugin
public final class JadeCompat implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addRayTraceCallback(new DisguiseCallback(registration::blockAccessor, registration::entityAccessor));
    }

    private static final class DisguiseCallback implements JadeRayTraceCallback {
        private final Supplier<BlockAccessor.Builder> blocks;
        private final Supplier<EntityAccessor.Builder> entities;
        private final Map<EntityType<?>, WeakReference<Entity>> displays = new HashMap<>();

        private DisguiseCallback(Supplier<BlockAccessor.Builder> blocks, Supplier<EntityAccessor.Builder> entities) {
            this.blocks = blocks;
            this.entities = entities;
        }

        @Override
        @Nullable
        public Accessor<?> onRayTrace(HitResult hit, @Nullable Accessor<?> current, @Nullable Accessor<?> original) {
            if (!WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get()) return current;
            if (current instanceof BlockAccessor block) {
                BlockState state = block.getBlockState();
                Block disguise = blockDisguise(state);
                if (disguise != null) {
                    // Use the cover state for providers such as tool hints as well as the name and icon.
                    return blocks.get().from(block).blockState(disguise.withPropertiesOf(state))
                            .fakeBlock(new ItemStack(disguise)).blockEntity(() -> null)
                            .serverConnected(false).serverData(new CompoundTag()).build();
                }
            } else if (current instanceof EntityAccessor target) {
                return disguiseEntity(target);
            }
            return current;
        }

        @Nullable
        private Accessor<?> disguiseEntity(EntityAccessor accessor) {
            Entity target = accessor.getEntity();
            EntityType<?> disguise = entityDisguise(target);
            if (disguise == null) return accessor;
            if (target instanceof Illusion illusion
                    && !illusion.victimName().equals(accessor.getPlayer().getGameProfile().getName())) return null;
            var cached = displays.get(disguise);
            Entity display = cached == null ? null : cached.get();
            if (display == null || display.level() != target.level()) {
                display = disguise.create(target.level());
                if (display == null) return null;
                displays.put(disguise, new WeakReference<>(display));
            }
            if (display instanceof Villager villager && target instanceof WereVillager wereVillager) {
                villager.setVillagerData(wereVillager.getVillagerData());
                villager.setAge(wereVillager.getAge());
            }
            display.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
            return entities.get().from(accessor).entity(display).serverConnected(false).serverData(new CompoundTag()).build();
        }
    }

    @Nullable
    private static Block blockDisguise(BlockState state) {
        if (state.getBlock() instanceof PlantMineBlock) return state.getValue(PlantMineBlock.VARIANT).tooltipPlant();
        if (state.is(WitcheryBlocks.getRequired("alderwooddoor").get())) return Blocks.OAK_DOOR;
        if (state.is(WitcheryBlocks.getRequired("pitdirt").get())) return Blocks.DIRT;
        if (state.is(WitcheryBlocks.getRequired("pitgrass").get())) return Blocks.GRASS_BLOCK;
        return null; // Jade already uses the original Rowan Door item for both halves.
    }

    @Nullable
    private static EntityType<?> entityDisguise(Entity entity) {
        if (entity instanceof WereVillager) return EntityType.VILLAGER;
        if (entity instanceof Illusion) {
            if (entity.getType() == WitcheryEntities.ILLUSION_CREEPER.get()) return EntityType.CREEPER;
            if (entity.getType() == WitcheryEntities.ILLUSION_SPIDER.get()) return EntityType.SPIDER;
            if (entity.getType() == WitcheryEntities.ILLUSION_ZOMBIE.get()) return EntityType.ZOMBIE;
        }
        return null;
    }
}
