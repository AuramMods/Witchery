package art.arcane.witchery.brewing;

import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.block.WitchCropBlock;
import art.arcane.witchery.entity.Ent;
import art.arcane.witchery.event.WitcheryBlockPlacement;
import art.arcane.witchery.item.WitchSeedsItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DeadBushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

final class BrewFarming {
    private static final Block[] FLOWERS = {
            Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET,
            Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY
    };
    // States flattened from the original stone block and its metadata.
    private static final Set<Block> LEGACY_STONE = Set.of(Blocks.STONE, Blocks.GRANITE, Blocks.POLISHED_GRANITE,
            Blocks.DIORITE, Blocks.POLISHED_DIORITE, Blocks.ANDESITE, Blocks.POLISHED_ANDESITE);

    static boolean isLegacyStone(BlockState state) {
        return LEGACY_STONE.contains(state.getBlock());
    }

    private BrewFarming() {
    }

    static void flowers(ServerLevel level, BlockPos center, int radius, int strength, @Nullable Player caster) {
        Player actor = actor(level, caster);
        int odds = Math.max(1, 8 - strength);
        BrewTerrain.forEachColumn(level, center, radius + strength, column -> {
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos pos = column.offset(0, dy, 0);
                var state = level.getBlockState(pos);
                if (!canReplaceWithFlower(state) || !state.getFluidState().isEmpty()
                        || !BrewTerrain.canChange(actor, level, pos, state)
                        || !Blocks.DANDELION.defaultBlockState().canSurvive(level, pos)
                        || level.random.nextInt(odds) != 0) continue;
                var flower = FLOWERS[level.random.nextInt(FLOWERS.length)].defaultBlockState();
                if (flower.canSurvive(level, pos)) WitcheryBlockPlacement.place(level, pos, flower, actor, Direction.UP);
            }
        });
    }

    private static boolean canReplaceWithFlower(BlockState state) {
        // The original brew replaced partial snow layers, but left all double plants intact.
        if (state.is(Blocks.SNOW)) return state.getValue(SnowLayerBlock.LAYERS) < 8;
        return state.canBeReplaced() && !(state.getBlock() instanceof DoublePlantBlock);
    }

    static void fertilize(ServerLevel level, BlockPos center, int radius, int strength, @Nullable Player caster) {
        Player actor = actor(level, caster);
        BrewTerrain.forEachColumn(level, center, radius + strength, column -> {
            for (int dy = 1; dy >= -1; dy--) {
                BlockPos pos = column.offset(0, dy, 0);
                var state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof BonemealableBlock) && !(state.getBlock() instanceof IPlantable)) continue;
                if (BrewTerrain.canChange(actor, level, pos, state)) {
                    for (int attempt = 0; attempt <= strength; attempt++) {
                        BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, actor);
                    }
                }
                return;
            }
        });
    }

    static void harvest(ServerLevel level, BlockPos center, int radius, int strength, @Nullable Player caster) {
        Player actor = actor(level, caster);
        var changed = new MutableInt();
        ItemStack tool = lootTool(Math.max(strength - 1, 0));
        BrewTerrain.forEachColumn(level, center, radius + strength, column -> {
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos pos = column.offset(0, dy, 0);
                var state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof BushBlock) || !canHarvest(level, pos, state, actor)) continue;
                // The legacy brew uprooted double grass and ferns without producing seed drops.
                boolean mandrake = state.is(WitchCrop.MANDRAKE.block());
                List<ItemStack> drops = mandrake || state.is(Blocks.TALL_GRASS) || state.is(Blocks.LARGE_FERN)
                        ? List.of() : Block.getDrops(state, level, pos, null, null, tool);
                if (!level.removeBlock(pos, false)) continue;
                if (mandrake && !WitchCropBlock.awakenMandrake(level, pos, state)) {
                    drops = Block.getDrops(state, level, pos, null, null, tool);
                }
                if (level.random.nextInt(changed.incrementAndGet()) == 0) level.levelEvent(2001, pos, Block.getId(state));
                for (ItemStack drop : drops) {
                    if (drop.isEmpty()) continue;
                    Vec3 location = Vec3.atCenterOf(pos);
                    level.addFreshEntity(new ItemEntity(level, location.x, location.y, location.z, drop));
                }
            }
        });
    }

    private static boolean canHarvest(ServerLevel level, BlockPos pos, BlockState state, Player caster) {
        if (!BrewTerrain.canChange(caster, level, pos, state)) return false;
        if (!(state.getBlock() instanceof DoublePlantBlock)) return true;
        var half = state.getValue(DoublePlantBlock.HALF);
        var otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        var other = level.getBlockState(otherPos);
        // Removing either half can also remove its partner during the neighbor update.
        return !other.is(state.getBlock()) || other.getValue(DoublePlantBlock.HALF) == half
                || BrewTerrain.canChange(caster, level, otherPos, other);
    }

    static void till(ServerLevel level, BlockPos center, int radius, int strength, @Nullable Player caster) {
        Player actor = actor(level, caster);
        var changed = new MutableInt();
        BrewTerrain.forEachColumn(level, center, radius + strength, column -> {
            for (int dy = -1; dy <= 1; dy++) {
                BlockPos pos = column.offset(0, dy, 0);
                var state = level.getBlockState(pos);
                if (!canTill(state, strength) || !level.isEmptyBlock(pos.above())
                        || !BrewTerrain.setBlock(actor, level, pos, Blocks.FARMLAND.defaultBlockState(), Direction.UP)) continue;
                if (level.random.nextInt(changed.incrementAndGet()) == 0) {
                    var sound = state.getSoundType(level, pos, actor);
                    level.playSound(null, pos, sound.getStepSound(), SoundSource.BLOCKS,
                            (sound.getVolume() + 1) / 2, sound.getPitch() * 0.8F);
                }
            }
        });
    }

    private static boolean canTill(BlockState state, int strength) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)
                || strength >= 1 && (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND))
                || strength >= 2 && state.is(Blocks.NETHERRACK)
                || strength >= 3 && state.is(Blocks.SOUL_SAND);
    }

    static void plant(ServerLevel level, BlockPos center, int radius, int strength, @Nullable Player caster) {
        int reach = radius + strength;
        Vec3 origin = Vec3.atLowerCornerOf(center);
        var seeds = new ArrayDeque<>(level.getEntitiesOfClass(ItemEntity.class, new AABB(origin, origin).inflate(reach),
                seed -> seed.isAlive() && seed.distanceToSqr(origin) <= reach * reach && isPlantable(seed.getItem())));
        if (seeds.isEmpty()) return;

        Player actor = actor(level, caster);
        BrewTerrain.forEachColumn(level, center, reach, column -> {
            while (!seeds.isEmpty() && !seeds.getLast().isAlive()) seeds.removeLast();
            ItemEntity seed = seeds.peekLast();
            if (seed == null) return;
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos soil = column.offset(0, dy, 0);
                BlockPos site = soil.above();
                if (!level.getBlockState(soil).blocksMotion() || !level.isEmptyBlock(site)
                        || !level.isInWorldBounds(site) || !level.mayInteract(actor, soil) || !level.mayInteract(actor, site)) continue;
                if (!plantSeed(level, soil, site, seed.getItem(), actor)) continue;
                if (seed.getItem().getCount() == 1) {
                    seed.discard();
                    seeds.removeLast();
                } else {
                    seed.setItem(seed.getItem().copyWithCount(seed.getItem().getCount() - 1));
                }
                break;
            }
        });
    }

    private static boolean isPlantable(ItemStack stack) {
        var item = stack.getItem();
        // Modern Forge exposes the planted block through a distinct seed item such as Wheat Seeds.
        return item instanceof IPlantable || item instanceof WitchSeedsItem.WaterSeeds
                || item instanceof ItemNameBlockItem seed && seed.getBlock() instanceof IPlantable;
    }

    private static boolean plantSeed(ServerLevel level, BlockPos soil, BlockPos site, ItemStack stack, Player actor) {
        var seed = stack.copyWithCount(1);
        var hit = new BlockHitResult(Vec3.atCenterOf(soil).add(0, 0.5, 0), Direction.UP, soil, false);
        var context = new UseOnContext(level, actor, InteractionHand.MAIN_HAND, seed, hit);
        // Vanilla carrots and potatoes fall back to eating if their block cannot be placed.
        if (seed.isEdible() && seed.getItem().getClass() == ItemNameBlockItem.class) {
            var state = ((BlockItem) seed.getItem()).getBlock().getStateForPlacement(new BlockPlaceContext(context));
            if (state == null || !state.canSurvive(level, site)) return false;
        }
        return seed.useOn(context).consumesAction() && !level.isEmptyBlock(site);
    }

    static void prune(ServerLevel level, BlockPos center, int radius, int strength, @Nullable Player caster) {
        Player actor = actor(level, caster);
        int reach = Math.max(radius - 1, 0);
        ItemStack tool = lootTool(strength);
        forEachBlock(level, center, reach, reach * reach, pos -> {
            var state = level.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof DreamCatcherBlock) {
                pruneDreamCatcher(level, pos, state, actor);
            } else if (state.is(BlockTags.LEAVES) || state.canBeReplaced()
                    && (block instanceof TallGrassBlock || block instanceof DeadBushBlock || block instanceof VineBlock)) {
                cut(level, pos, state, tool, actor);
            }
        });
    }

    private static void pruneDreamCatcher(ServerLevel level, BlockPos pos, BlockState state, Player caster) {
        // Dream Catchers shared the original vine material even though they carried a tile.
        if (!BrewTerrain.canEdit(caster, level, pos, state)) return;
        var weave = DreamCatcherBlock.weaveAt(level, pos, state);
        if (level.removeBlock(pos, false)) {
            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(weave.item())));
        }
    }

    static void fell(ServerLevel level, BlockPos center, int radius, int strength, @Nullable Player caster) {
        Player actor = actor(level, caster);
        int reach = Math.max(radius, 1);
        ItemStack tool = lootTool(strength);
        forEachBlock(level, center, reach, reach * reach, pos -> {
            var state = level.getBlockState(pos);
            if (state.is(BlockTags.LOGS) && !state.is(BlockTags.CRIMSON_STEMS) && !state.is(BlockTags.WARPED_STEMS)) {
                cut(level, pos, state, tool, actor);
            }
        });
    }

    private static void cut(ServerLevel level, BlockPos pos, BlockState state, ItemStack tool, Player caster) {
        if (!remove(level, pos, state, caster)) return;
        var witchLog = WitcheryBlocks.getRequired("witchlog").get();
        if (state.is(witchLog)) Ent.tryAwaken(level, pos, witchLog);
        for (ItemStack drop : Block.getDrops(state, level, pos, null, null, tool)) Block.popResource(level, pos, drop);
    }

    private static boolean remove(ServerLevel level, BlockPos pos, BlockState state, Player caster) {
        return BrewTerrain.canChange(caster, level, pos, state) && level.removeBlock(pos, false);
    }

    private static Player actor(ServerLevel level, @Nullable Player caster) {
        return caster != null ? caster : FakePlayerFactory.getMinecraft(level);
    }

    private static ItemStack lootTool(int fortune) {
        if (fortune == 0) return ItemStack.EMPTY;
        var tool = new ItemStack(Items.STICK);
        tool.enchant(Enchantments.BLOCK_FORTUNE, fortune);
        return tool;
    }

    static void pulverise(ServerLevel level, BlockPos center, int radius, @Nullable Player caster) {
        Player actor = actor(level, caster);
        int reach = Math.max(radius - 1, 1);
        int limit = Math.max(0, reach * reach - 2);
        Consumer<BlockPos> convert = pos -> pulveriseBlock(level, pos, actor);
        for (int x = -reach; x <= reach; x++) {
            for (int z = -reach; z <= reach; z++) {
                for (int y = -reach; y <= reach; y++) {
                    if (x * x + y * y + z * z <= limit) visit(level, center.offset(x, y, z), convert);
                }
            }
        }
        level.playSound(null, center.getX(), center.getY(), center.getZ(), SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 0.5F, 2);
    }

    private static void pulveriseBlock(ServerLevel level, BlockPos pos, Player caster) {
        var state = level.getBlockState(pos);
        Block replacement;
        if (isLegacyStone(state)) replacement = Blocks.COBBLESTONE;
        else if (state.is(Blocks.COBBLESTONE)) replacement = Blocks.GRAVEL;
        else if (state.is(Blocks.GRAVEL) || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.CHISELED_SANDSTONE) || state.is(Blocks.CUT_SANDSTONE)) replacement = Blocks.SAND;
        else if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) replacement = Blocks.AIR;
        else return;
        if (!BrewTerrain.setBlock(caster, level, pos, replacement.defaultBlockState(), Direction.UP)) return;
        if (replacement == Blocks.AIR) {
            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.SAND)));
        }
        WitcheryNetwork.sendBrewConversionSmoke(level, pos);
    }

    private static void forEachBlock(ServerLevel level, BlockPos center, int reach, int limit, Consumer<BlockPos> action) {
        for (int y = -reach; y <= reach; y++) {
            for (int x = -reach; x <= reach; x++) {
                for (int z = -reach; z <= reach; z++) {
                    if (x * x + y * y + z * z <= limit) visit(level, center.offset(x, y, z), action);
                }
            }
        }
    }

    private static void visit(ServerLevel level, BlockPos pos, Consumer<BlockPos> action) {
        if (level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos) && level.hasChunkAt(pos)) {
            action.accept(pos);
        }
    }
}
