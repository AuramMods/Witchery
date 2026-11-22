package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.CritterSnareBlock;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.block.entity.GrassperBlockEntity;
import art.arcane.witchery.entity.Mandrake;
import art.arcane.witchery.entity.WitchOwl;
import art.arcane.witchery.entity.WitchPet;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CocoaBlock;
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
    private static final TagKey<Block> BLOOD_ROSE_FLOWERS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "blood_rose_flowers"));

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
        boolean water = sourceWater(level, pos.below());
        boolean grasspers = water && clicked.is(Blocks.CHEST) && (sides(level, pos, Blocks.VINE, false)
                || Direction.Plane.HORIZONTAL.stream().map(pos::relative).map(level::getBlockState)
                .allMatch(state -> state.is(Blocks.GRASS) || state.is(Blocks.FERN)));
        boolean leech = clicked.is(Blocks.TRAPPED_CHEST) && sides(level, pos, Blocks.VINE, false) && hasCardinalWater(level, pos);
        boolean roses = water && clicked.is(WitcheryBlocks.getRequired("leechchest").get())
                && Direction.Plane.HORIZONTAL.stream().map(pos::relative).map(level::getBlockState)
                .allMatch(state -> state.is(BLOOD_ROSE_FLOWERS));
        boolean cobweb = water && clicked.is(Blocks.COBWEB);
        boolean snares = cobweb && sides(level, pos, WitcheryBlocks.getRequired("witchsapling").get(), true);
        CreatureMutation mutation = cobweb && !snares ? findCreatureMutation(level, pos) : null;
        MindrakeMutation mindrake = cobweb && !snares && mutation == null ? findMindrakeMutation(level, pos) : null;
        MonkeyMutation monkey = cobweb && !snares && mutation == null && mindrake == null ? findMonkeyMutation(level, pos) : null;
        LouseMutation louse = findLouseMutation(level, pos, clicked);
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
            } else if (leech && server.getBlockEntity(pos) instanceof ChestBlockEntity chest && chest.isEmpty()) {
                for (Direction side : Direction.Plane.HORIZONTAL) server.removeBlock(pos.relative(side), false);
                BlockState output = WitcheryBlocks.getRequired("leechchest").get().defaultBlockState()
                        .setValue(ChestBlock.FACING, clicked.getValue(ChestBlock.FACING))
                        .setValue(ChestBlock.WATERLOGGED, clicked.getValue(ChestBlock.WATERLOGGED));
                server.setBlockAndUpdate(pos, output);
            } else if (roses && server.getBlockEntity(pos) instanceof ChestBlockEntity chest && chest.isEmpty()) {
                replaceAround(server, pos, WitcheryBlocks.getRequired("bloodrose").get());
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
            } else if (mindrake != null) {
                mindrake.apply(server, pos);
            } else if (monkey != null) {
                if (!monkey.apply(server, pos)) return InteractionResult.PASS;
            } else if (louse != null) {
                if (!louse.apply(server, pos)) return InteractionResult.PASS;
            } else {
                return InteractionResult.PASS;
            }
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(context.getHand()));
            server.sendParticles(surface || clay ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.ITEM_SLIME,
                    pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 16, 0.4, 0.4, 0.4, 0.1);
            server.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1, 1);
            return InteractionResult.CONSUME;
        }
        return surface || clay || plant != null || wormwood || grasspers || leech || roses || snares
                || mutation != null || mindrake != null || monkey != null || louse != null
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

    private static boolean hasCardinalWater(Level level, BlockPos center) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (!sourceWater(level, center.below().relative(side))) return false;
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
    private static MindrakeMutation findMindrakeMutation(Level level, BlockPos center) {
        var mandrake = WitchCrop.MANDRAKE;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(center.relative(side));
            if (!state.is(mandrake.block()) || state.getValue(CropBlock.AGE) != mandrake.maxAge()) return null;
        }
        var offerings = diagonalGrasspers(level, center);
        long mutandis = offerings.stream().filter(grassper -> WitcheryIngredient.MUTANDIS_EXTREMIS.matches(grassper.getItem(0))).count();
        long stones = offerings.stream().filter(grassper -> WitcheryIngredient.CHARGED_ATTUNED_STONE.matches(grassper.getItem(0))).count();
        long will = offerings.stream().filter(grassper -> WitcheryIngredient.FOCUSED_WILL.matches(grassper.getItem(0))).count();
        if (mutandis != 2 || stones != 1 || will != 1) return null;

        var area = catalystArea(center);
        var creepers = level.getEntitiesOfClass(Creeper.class, area, Creeper::isAlive);
        var mandrakes = level.getEntitiesOfClass(Mandrake.class, area, Mandrake::isAlive);
        return creepers.isEmpty() || mandrakes.isEmpty() ? null : new MindrakeMutation(creepers.get(0), mandrakes.get(0), offerings);
    }

    private record MindrakeMutation(Creeper creeper, Mandrake mandrake, List<GrassperBlockEntity> offerings) {
        void apply(ServerLevel level, BlockPos center) {
            creeper.playSound(SoundEvents.CREEPER_DEATH, 1, 1);
            mandrake.playSound(SoundEvents.GHAST_DEATH, 1, 1);
            creeper.discard();
            mandrake.discard();
            replaceAround(level, center, WitchCrop.MINDRAKE.block());
            for (GrassperBlockEntity grassper : offerings) grassper.clearContent();
        }
    }

    @Nullable
    private static MonkeyMutation findMonkeyMutation(Level level, BlockPos center) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(center.relative(side));
            if (!state.is(Blocks.COCOA) || state.getValue(CocoaBlock.AGE) != CocoaBlock.MAX_AGE) return null;
        }
        var offerings = diagonalGrasspers(level, center);
        long mutandis = offerings.stream().filter(grassper -> WitcheryIngredient.MUTANDIS_EXTREMIS.matches(grassper.getItem(0))).count();
        long stones = offerings.stream().filter(grassper -> WitcheryIngredient.CHARGED_ATTUNED_STONE.matches(grassper.getItem(0))).count();
        long flowers = offerings.stream().filter(grassper -> grassper.getItem(0).is(Items.POPPY)).count();
        if (mutandis != 2 || stones != 1 || flowers != 1) return null;

        var area = catalystArea(center);
        var owls = level.getEntitiesOfClass(WitchOwl.class, area, WitchOwl::isAlive);
        var wolves = level.getEntitiesOfClass(Wolf.class, area, Wolf::isAlive);
        return owls.isEmpty() || wolves.isEmpty() ? null : new MonkeyMutation(owls.get(0), wolves.get(0), offerings);
    }

    private record MonkeyMutation(WitchOwl owl, Wolf wolf, List<GrassperBlockEntity> offerings) {
        boolean apply(ServerLevel level, BlockPos center) {
            var monkey = WitcheryEntities.WINGED_MONKEY.get().create(level);
            if (monkey == null) return false;
            monkey.moveTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0, 0);
            monkey.setPersistenceRequired();
            ForgeEventFactory.onFinalizeSpawn(monkey, level, level.getCurrentDifficultyAt(center), MobSpawnType.EVENT, null, null);
            if (!level.addFreshEntity(monkey)) return false;

            owl.playSound(SoundEvents.CREEPER_DEATH, 1, 1);
            wolf.playSound(SoundEvents.GHAST_DEATH, 1, 1);
            owl.discard();
            wolf.discard();
            level.removeBlock(center, false);
            for (Direction side : Direction.Plane.HORIZONTAL) level.removeBlock(center.relative(side), false);
            for (GrassperBlockEntity grassper : offerings) grassper.clearContent();
            return true;
        }
    }

    @Nullable
    private static LouseMutation findLouseMutation(Level level, BlockPos center, BlockState clicked) {
        if (!(clicked.getBlock() instanceof CritterSnareBlock)
                || clicked.getValue(CritterSnareBlock.CRITTER) != CritterSnareBlock.Critter.SILVERFISH
                || clicked.getValue(CritterSnareBlock.STOCKED)) return null;
        var vines = Direction.Plane.HORIZONTAL.stream().map(center::relative)
                .filter(pos -> level.getBlockState(pos).is(Blocks.VINE)).toList();
        if (vines.size() < 2 || Direction.Plane.HORIZONTAL.stream().map(center.below()::relative)
                .noneMatch(pos -> sourceWater(level, pos))) return null;
        var offerings = diagonalGrasspers(level, center);
        long mutandis = offerings.stream().filter(grassper -> WitcheryIngredient.MUTANDIS.matches(grassper.getItem(0))).count();
        long tongues = offerings.stream().filter(grassper -> WitcheryIngredient.DOG_TONGUE.matches(grassper.getItem(0))).count();
        long stones = offerings.stream().filter(grassper -> WitcheryIngredient.CHARGED_ATTUNED_STONE.matches(grassper.getItem(0))).count();
        return mutandis == 2 && tongues == 1 && stones == 1 ? new LouseMutation(vines, offerings) : null;
    }

    private record LouseMutation(List<BlockPos> vines, List<GrassperBlockEntity> offerings) {
        boolean apply(ServerLevel level, BlockPos center) {
            var louse = WitcheryEntities.LOUSE.get().create(level);
            if (louse == null) return false;
            louse.moveTo(center.getX() + 0.5, center.getY() + 0.001, center.getZ() + 0.5, 1, 0);
            louse.setPersistenceRequired();
            ForgeEventFactory.onFinalizeSpawn(louse, level, level.getCurrentDifficultyAt(center), MobSpawnType.EVENT, null, null);
            if (!level.addFreshEntity(louse)) return false;
            level.removeBlock(center, false);
            for (BlockPos vine : vines) level.removeBlock(vine, false);
            for (GrassperBlockEntity offering : offerings) offering.removeItem(0, 1);
            return true;
        }
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
