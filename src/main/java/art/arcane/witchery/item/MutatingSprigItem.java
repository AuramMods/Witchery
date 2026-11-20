package art.arcane.witchery.item;

import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.CritterSnareBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.block.entity.GrassperBlockEntity;
import art.arcane.witchery.entity.WitchPet;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class MutatingSprigItem extends Item {
    public MutatingSprigItem() {
        super(new Properties().rarity(Rarity.UNCOMMON).durability(15));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState clicked = level.getBlockState(pos);
        boolean surface = clicked.is(Blocks.GRASS_BLOCK) || clicked.is(Blocks.MYCELIUM);
        boolean clay = MutandisItem.isSoakedDirt(level, pos);
        PlantMutation plant = findBrambleMutation(level, pos, clicked);
        boolean wormwood = clicked.is(Blocks.WHEAT) && clicked.getValue(CropBlock.AGE) == CropBlock.MAX_AGE
                && sides(level, pos, WitcheryBlocks.getRequired("somniancotton").get(), false) && hasDiagonalWater(level, pos);
        boolean water = level.getBlockState(pos.below()).is(Blocks.WATER) && level.getFluidState(pos.below()).isSource();
        boolean grasspers = water && clicked.is(Blocks.CHEST) && sides(level, pos, Blocks.VINE, false);
        boolean cobweb = water && clicked.is(Blocks.COBWEB);
        boolean snares = cobweb && sides(level, pos, WitcheryBlocks.getRequired("witchsapling").get(), true);
        CreatureMutation mutation = cobweb && !snares ? findCreatureMutation(level, pos) : null;
        if (level instanceof ServerLevel server) {
            if (surface) {
                Block changed = clicked.is(Blocks.GRASS_BLOCK) ? Blocks.MYCELIUM : Blocks.GRASS_BLOCK;
                server.setBlockAndUpdate(pos, changed.defaultBlockState());
            } else if (clay) {
                MutandisItem.makeClay(server, pos);
                for (Direction side : Direction.Plane.HORIZONTAL) MutandisItem.makeClay(server, pos.relative(side));
            } else if (plant != null) {
                plant.apply(server, pos);
            } else if (wormwood) {
                for (Direction side : Direction.Plane.HORIZONTAL) server.removeBlock(pos.relative(side), false);
                server.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("wormwood").get().defaultBlockState());
            } else if (grasspers && server.getBlockEntity(pos) instanceof ChestBlockEntity chest && chest.isEmpty()) {
                replaceAround(server, pos, WitcheryBlocks.getRequired("grassper").get());
            } else if (snares) {
                var zombies = server.getEntitiesOfClass(Zombie.class, catalystArea(pos), Zombie::isAlive);
                if (zombies.isEmpty()) return InteractionResult.PASS;
                zombies.get(0).playSound(SoundEvents.ZOMBIE_DEATH, 1, 1);
                zombies.get(0).discard();
                replaceAround(server, pos, WitcheryBlocks.getRequired("crittersnare").get());
            } else if (mutation != null) {
                if (!mutation.apply(server, pos)) return InteractionResult.PASS;
            } else {
                return InteractionResult.PASS;
            }
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(context.getHand()));
            server.sendParticles(surface || clay ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.ITEM_SLIME,
                    pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 16, 0.4, 0.4, 0.4, 0.1);
            server.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1, 1);
            return InteractionResult.CONSUME;
        }
        return surface || clay || plant != null || wormwood || grasspers || snares || mutation != null
                ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static boolean sides(Level level, BlockPos center, Block block, boolean alderOnly) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(center.relative(side));
            if (!state.is(block) || alderOnly && state.getValue(WitchWoodType.PROPERTY) != WitchWoodType.ALDER) return false;
        }
        return true;
    }

    private static void replaceAround(ServerLevel level, BlockPos center, Block output) {
        BlockState state = output.defaultBlockState();
        for (Direction side : Direction.Plane.HORIZONTAL) level.setBlockAndUpdate(center.relative(side), state);
        level.removeBlock(center, false);
    }

    private static AABB catalystArea(BlockPos pos) {
        return new AABB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    }

    private static boolean hasDiagonalWater(Level level, BlockPos center) {
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) if (!sourceWater(level, center.offset(x, -1, z))) return false;
        }
        return true;
    }

    private static boolean sourceWater(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.WATER) && level.getFluidState(pos).isSource();
    }

    @Nullable
    private static PlantMutation findBrambleMutation(Level level, BlockPos center, BlockState clicked) {
        boolean wild = clicked.is(Blocks.CACTUS);
        if (!wild && !clicked.is(Blocks.SUGAR_CANE)) return null;
        if (!sides(level, center, WitcheryBlocks.getRequired("spanishmoss").get(), false)) return null;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (!sourceWater(level, center.below().relative(side))) return null;
        }
        var offerings = diagonalGrasspers(level, center);
        if (offerings.isEmpty()) return null;
        long bone = offerings.stream().filter(grassper -> grassper.getItem(0).is(Items.BONE_MEAL)).count();
        long blaze = offerings.stream().filter(grassper -> grassper.getItem(0).is(Items.BLAZE_POWDER)).count();
        long pearls = offerings.stream().filter(grassper -> grassper.getItem(0).is(Items.ENDER_PEARL)).count();
        if (wild ? bone != 2 || blaze != 2 : pearls != 4) return null;
        return new PlantMutation(clicked.getBlock(), wild, offerings);
    }

    private record PlantMutation(Block stem, boolean wild, List<GrassperBlockEntity> offerings) {
        void apply(ServerLevel level, BlockPos center) {
            BlockState output = WitcheryBlocks.getRequired("bramble").get().defaultBlockState().setValue(BrambleBlock.WILD, wild);
            for (Direction side : Direction.Plane.HORIZONTAL) level.removeBlock(center.relative(side), false);
            for (int height = wild ? 4 : 15; height >= 0; height--) {
                BlockPos segment = center.above(height);
                if (level.getBlockState(segment).is(stem)) level.setBlockAndUpdate(segment, output);
            }
            for (GrassperBlockEntity grassper : offerings) grassper.removeItem(0, 1);
        }
    }

    private static List<GrassperBlockEntity> diagonalGrasspers(Level level, BlockPos center) {
        List<GrassperBlockEntity> offerings = new ArrayList<>(4);
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) {
                if (!(level.getBlockEntity(center.offset(x, 0, z)) instanceof GrassperBlockEntity grassper)) return List.of();
                offerings.add(grassper);
            }
        }
        return offerings;
    }

    @Nullable
    private static CreatureMutation findCreatureMutation(Level level, BlockPos center) {
        Item extremis = WitcheryItems.getRequired("mutandis_extremis").get();
        Item attuned = WitcheryItems.getRequired("charged_attuned_stone").get();
        List<GrassperBlockEntity> offerings = diagonalGrasspers(level, center);
        if (offerings.isEmpty()) return null;
        int mutandis = 0;
        int stones = 0;
        for (GrassperBlockEntity grassper : offerings) {
            ItemStack held = grassper.getItem(0);
            if (held.is(extremis)) mutandis++;
            if (held.is(attuned)) stones++;
        }
        if (mutandis != 3 || stones != 1) return null;
        for (CreatureKind kind : CreatureKind.values()) {
            List<BlockPos> captured = new ArrayList<>(4);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos cage = center.relative(side);
                BlockState state = level.getBlockState(cage);
                if (state.getBlock() instanceof CritterSnareBlock && state.getValue(CritterSnareBlock.CRITTER) == kind.source
                        && !state.getValue(CritterSnareBlock.STOCKED)) captured.add(cage);
            }
            if (captured.size() < 2) continue;
            var catalysts = level.getEntitiesOfClass(kind.catalyst, catalystArea(center), Mob::isAlive);
            if (!catalysts.isEmpty()) return new CreatureMutation(kind, captured, offerings, catalysts.get(0));
        }
        return null;
    }

    private enum CreatureKind {
        OWL(CritterSnareBlock.Critter.BAT, Wolf.class, () -> WitcheryEntities.OWL.get(), SoundEvents.WOLF_DEATH),
        TOAD(CritterSnareBlock.Critter.SLIME, Ocelot.class, () -> WitcheryEntities.TOAD.get(), SoundEvents.OCELOT_DEATH);

        private final CritterSnareBlock.Critter source;
        private final Class<? extends Mob> catalyst;
        private final Supplier<? extends EntityType<? extends WitchPet>> result;
        private final SoundEvent deathSound;

        CreatureKind(CritterSnareBlock.Critter source, Class<? extends Mob> catalyst,
                     Supplier<? extends EntityType<? extends WitchPet>> result, SoundEvent deathSound) {
            this.source = source;
            this.catalyst = catalyst;
            this.result = result;
            this.deathSound = deathSound;
        }
    }

    private record CreatureMutation(CreatureKind kind, List<BlockPos> captured, List<GrassperBlockEntity> offerings, Mob catalyst) {
        boolean apply(ServerLevel level, BlockPos center) {
            boolean transformed = false;
            for (BlockPos cage : captured) {
                WitchPet creature = kind.result.get().create(level);
                if (creature == null) continue;
                creature.moveTo(cage.getX() + 0.5, cage.getY() + 0.001, cage.getZ() + 0.5, 1, 0);
                creature.setPersistenceRequired();
                ForgeEventFactory.onFinalizeSpawn(creature, level, level.getCurrentDifficultyAt(cage), MobSpawnType.EVENT, null, null);
                if (level.addFreshEntity(creature)) {
                    transformed = true;
                    level.removeBlock(cage, false);
                }
            }
            if (!transformed) return false;
            catalyst.playSound(kind.deathSound, 1, 1);
            catalyst.discard();
            for (GrassperBlockEntity grassper : offerings) grassper.removeItem(0, 1);
            level.removeBlock(center, false);
            return true;
        }
    }
}
