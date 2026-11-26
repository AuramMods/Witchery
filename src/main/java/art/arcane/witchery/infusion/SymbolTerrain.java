package art.arcane.witchery.infusion;

import art.arcane.witchery.block.BrazierBlock;
import art.arcane.witchery.block.KettleBlock;
import art.arcane.witchery.block.WickerBundleBlock;
import art.arcane.witchery.block.WitchDoorBlock;
import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.block.entity.BrazierBlockEntity;
import art.arcane.witchery.event.WitcheryBlockBreak;
import art.arcane.witchery.event.WitcheryBlockPlacement;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;


public final class SymbolTerrain {
    private static final TagKey<Block> DEFODIO_BLOCKS = TagKey.create(Registries.BLOCK,
            ResourceLocation.parse("witchery:defodio_blocks"));

    private SymbolTerrain() {}

    public static void pourWater(ServerLevel level, @Nullable Entity caster, @Nullable HitResult hit, int strength) {
        boolean hotDimension = level.dimensionType().ultraWarm();
        if (hotDimension && strength < 3) return;
        boolean expands = !hotDimension && strength > 1;
        BlockPos center;
        Direction face;
        if (hit instanceof EntityHitResult entityHit) {
            center = entityHit.getEntity().blockPosition();
            face = Direction.UP;
        } else if (hit instanceof BlockHitResult blockHit) {
            BlockPos struck = blockHit.getBlockPos();
            face = blockHit.getDirection();
            Block block = level.getBlockState(struck).getBlock();
            if (!expands && (block instanceof WitchesCauldronBlock || block instanceof KettleBlock)) {
                fillBrewingVessel(level, caster, struck, block instanceof WitchesCauldronBlock ? 3000 : 1000);
                return;
            }
            center = outsideFace(level, struck, face);
        } else {
            return;
        }

        BlockState water = Blocks.WATER.defaultBlockState();
        place(level, caster, center, water, face);
        if (expands) {
            for (Direction side : Direction.values()) {
                BlockPos neighbor = center.relative(side);
                if (level.isEmptyBlock(neighbor)) place(level, caster, neighbor, water, side);
            }
        }
    }

    private static void fillBrewingVessel(ServerLevel level, @Nullable Entity caster, BlockPos pos, int amount) {
        if (!level.getWorldBorder().isWithinBounds(pos) || caster instanceof Player player && !level.mayInteract(player, pos)) return;
        var vessel = level.getBlockEntity(pos);
        if (vessel != null && vessel.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .map(handler -> handler.fill(new FluidStack(Fluids.WATER, amount), IFluidHandler.FluidAction.EXECUTE) > 0)
                .orElse(false)) {
            WitcherySounds.playLegacyBlockCue(level, pos, SoundEvents.PLAYER_SWIM, 0.5F);
            level.gameEvent(caster, GameEvent.FLUID_PLACE, pos);
        }
    }

    public static void ignite(ServerLevel level, Projectile projectile, @Nullable HitResult hit, int strength) {
        if (hit == null) return;
        Entity caster = projectile.getOwner();
        if (strength > 1) {
            double radius = strength == 2 ? 3 : 6;
            var impact = projectile.position();
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(impact, impact).inflate(radius),
                    entity -> entity != caster && entity.distanceToSqr(impact) <= radius * radius)) {
                target.setSecondsOnFire(strength);
            }
            if (hit instanceof BlockHitResult blockHit && blockHit.getDirection() == Direction.UP) {
                for (BlockPos pos : impactPlane(blockHit, strength)) {
                    BlockPos above = pos.above();
                    var support = level.getBlockState(pos);
                    if (!support.isAir() && level.isEmptyBlock(above) && canDestroy(level, caster, pos, support)) {
                        place(level, caster, above, BaseFireBlock.getState(level, above), Direction.UP);
                    }
                }
            }
            return;
        }

        if (hit instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            target.setSecondsOnFire(1);
            var type = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.ON_FIRE);
            target.hurt(new DamageSource(type, projectile, caster), 0.1F);
        } else if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            var state = level.getBlockState(pos);
            if (state.getBlock() instanceof BrazierBlock) {
                if (canModify(level, caster, pos, state) && level.getBlockEntity(pos) instanceof BrazierBlockEntity brazier) {
                    brazier.ignite();
                }
                return;
            }
            if (state.getBlock() instanceof WickerBundleBlock && state.getValue(WickerBundleBlock.BUNDLE_TYPE) == WickerBundleBlock.Type.BLOODIED
                    && canModify(level, caster, pos, state)
                    && WickerBundleBlock.igniteEffigy(level, pos, caster == null ? 0 : caster.getYRot()).consumesAction()) return;
            Direction face = blockHit.getDirection();
            BlockPos target = outsideFace(level, pos, face);
            place(level, caster, target, BaseFireBlock.getState(level, target), face);
        }
    }

    public static void placeLight(ServerLevel level, @Nullable Entity caster, @Nullable HitResult hit) {
        if (hit instanceof BlockHitResult blockHit) {
            Direction face = blockHit.getDirection();
            BlockPos pos = blockHit.getBlockPos().relative(face);
            var old = level.getBlockState(pos);
            if (old.isAir() || old.is(Blocks.SNOW)) {
                place(level, caster, pos, WitcheryBlocks.getRequired("glowglobe").get().defaultBlockState(), face);
            }
        }
    }

    public static void toggleDoor(ServerLevel level, @Nullable Entity caster, @Nullable HitResult hit) {
        if (!(hit instanceof BlockHitResult blockHit)) return;
        BlockPos pos = blockHit.getBlockPos();
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof DoorBlock door && canModify(level, caster, pos, state)) {
            door.setOpen(caster, level, state, pos, !door.isOpen(state));
        }
    }

    public static void sealDoor(ServerLevel level, @Nullable Entity caster, @Nullable HitResult hit) {
        if (caster != null && hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            var state = level.getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock) {
                replaceDoor(level, caster, WitchDoorBlock.lowerHalf(pos, state), state.getBlock(),
                        WitcheryBlocks.getRequired("rowanwooddoor").get(), blockHit.getDirection());
            }
        }
    }

    public static void reinforce(ServerLevel level, @Nullable Entity caster, @Nullable HitResult hit, int strength) {
        if (!(hit instanceof BlockHitResult blockHit)) return;
        for (BlockPos pos : impactPlane(blockHit, strength)) {
            var state = level.getBlockState(pos);
            if (caster != null && state.is(Blocks.OAK_DOOR)) {
                replaceDoor(level, caster, WitchDoorBlock.lowerHalf(pos, state), Blocks.OAK_DOOR, Blocks.IRON_DOOR, blockHit.getDirection());
                continue;
            }
            Block replacement = reinforcedBlock(state);
            if (replacement != null && canDestroy(level, caster, pos, state)) {
                level.setBlockAndUpdate(pos, replacement.defaultBlockState());
            }
        }
    }

    @Nullable
    private static Block reinforcedBlock(BlockState state) {
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MYCELIUM) || state.is(Blocks.COBBLESTONE) || state.is(BlockTags.PLANKS)
                || state.is(WitcheryBlocks.getRequired("witchwood").get())) return Blocks.STONE;
        if (state.is(BlockTags.STONE_BRICKS)) return Blocks.BRICKS;
        if (state.is(BlockTags.SAND)) return Blocks.SANDSTONE;
        if (state.is(Blocks.CLAY)) return Blocks.TERRACOTTA;
        return null;
    }

    private static void replaceDoor(ServerLevel level, Entity caster, BlockPos base, Block original, Block replacement, Direction face) {
        base = base.immutable();
        BlockPos above = base.above();
        var bottom = level.getBlockState(base);
        var top = level.getBlockState(above);
        if (!bottom.is(original) || bottom.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER || !top.is(original)
                || top.getValue(DoorBlock.HALF) != DoubleBlockHalf.UPPER || !canModify(level, caster, base, bottom)
                || !canModify(level, caster, above, top)) return;

        Direction facing = Direction.fromYRot(caster.getYRot());
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        DoorHingeSide hinge = (level.getBlockState(base.relative(left)).is(replacement)
                && !level.getBlockState(base.relative(right)).is(replacement))
                || solidSides(level, base, right) > solidSides(level, base, left) ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
        var lower = replacement.defaultBlockState().setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HINGE, hinge);
        var upper = lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        if (lower.equals(bottom) && upper.equals(top)) return;

        WitchDoorBlock.replacePair(level, base, lower, upper, caster, face);
    }

    private static int solidSides(ServerLevel level, BlockPos base, Direction side) {
        BlockPos lower = base.relative(side);
        BlockPos upper = lower.above();
        return (level.getBlockState(lower).isCollisionShapeFullBlock(level, lower) ? 1 : 0)
                + (level.getBlockState(upper).isCollisionShapeFullBlock(level, upper) ? 1 : 0);
    }

    public static void excavate(ServerLevel level, @Nullable Entity caster, @Nullable HitResult hit, int strength) {
        if (!(hit instanceof BlockHitResult blockHit)) return;
        for (BlockPos pos : impactPlane(blockHit, strength)) {
            var state = level.getBlockState(pos);
            if (!state.is(DEFODIO_BLOCKS) || !canDestroy(level, caster, pos, state)) continue;
            var drops = Block.getDrops(state, level, pos, null, caster, ItemStack.EMPTY);
            if (level.removeBlock(pos, false)) {
                for (var drop : drops) {
                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
                }
            }
        }
    }

    private static Iterable<BlockPos> impactPlane(BlockHitResult hit, int strength) {
        var axis = hit.getDirection().getAxis();
        int reach = strength - 1;
        int x = axis == Direction.Axis.X ? 0 : reach;
        int y = axis == Direction.Axis.Y ? 0 : reach;
        int z = axis == Direction.Axis.Z ? 0 : reach;
        var center = hit.getBlockPos();
        return BlockPos.betweenClosed(center.offset(-x, -y, -z), center.offset(x, y, z));
    }

    static void extinguishLights(ServerLevel level, Player caster) {
        BlockPos center = caster.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-10, -10, -10), center.offset(10, 10, 10))) {
            var state = level.getBlockState(pos);
            if (state.getLightEmission(level, pos) > 0 && canDestroy(level, caster, pos, state) && level.removeBlock(pos, false)) {
                Block.dropResources(state, level, pos, null, caster, ItemStack.EMPTY);
            }
        }
    }

    private static BlockPos outsideFace(ServerLevel level, BlockPos pos, Direction face) {
        return face == Direction.UP && !level.getBlockState(pos).blocksMotion() ? pos : pos.relative(face);
    }

    private static boolean canDestroy(ServerLevel level, @Nullable Entity caster, BlockPos pos, BlockState state) {
        return !state.hasBlockEntity() && !state.is(Blocks.DRAGON_EGG)
                && !state.is(WitcheryBlocks.getRequired("force").get()) && !state.is(WitcheryBlocks.getRequired("barrier").get())
                && canModify(level, caster, pos, state);
    }

    private static boolean canModify(ServerLevel level, @Nullable Entity caster, BlockPos pos, BlockState state) {
        return level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos) && state.getDestroySpeed(level, pos) >= 0
                && (!(caster instanceof Player player) || level.mayInteract(player, pos)
                && (state.isAir() || !WitcheryBlockBreak.isCanceled(level, pos, state, player)));
    }

    private static void place(ServerLevel level, @Nullable Entity caster, BlockPos pos, BlockState state, Direction face) {
        var old = level.getBlockState(pos);
        if (!old.equals(state) && canModify(level, caster, pos, old)) {
            WitcheryBlockPlacement.place(level, pos, state, caster, face);
        }
    }
}
