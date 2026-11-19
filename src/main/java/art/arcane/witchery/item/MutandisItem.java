package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class MutandisItem extends Item {
    private static final TagKey<Block> PLANTS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "mutandis_plants"));
    private static final List<Block> POTTED_PLANTS = List.of(Blocks.POTTED_POPPY, Blocks.POTTED_DANDELION, Blocks.POTTED_OAK_SAPLING,
            Blocks.POTTED_SPRUCE_SAPLING, Blocks.POTTED_BIRCH_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, Blocks.POTTED_RED_MUSHROOM,
            Blocks.POTTED_BROWN_MUSHROOM, Blocks.POTTED_CACTUS, Blocks.POTTED_DEAD_BUSH, Blocks.POTTED_FERN);

    public MutandisItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState existing = level.getBlockState(pos);
        Block sapling = WitcheryBlocks.getRequired("witchsapling").get();
        boolean potted = POTTED_PLANTS.contains(existing.getBlock());
        if (!potted && !existing.is(PLANTS) && !existing.is(sapling)) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel server) {
            List<BlockState> candidates = new ArrayList<>();
            if (potted) {
                for (Block block : POTTED_PLANTS) {
                    if (!existing.is(block)) {
                        candidates.add(block.defaultBlockState());
                    }
                }
            } else {
                for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(PLANTS)) {
                    Block block = holder.value();
                    if (!existing.is(block) && block != sapling) {
                        candidates.add(block.defaultBlockState());
                    }
                }
                for (WitchWoodType wood : WitchWoodType.values()) {
                    if (!existing.is(sapling) || existing.getValue(WitchWoodType.PROPERTY) != wood) {
                        candidates.add(sapling.defaultBlockState().setValue(WitchWoodType.PROPERTY, wood));
                    }
                }
            }
            if (candidates.isEmpty() || !server.setBlockAndUpdate(pos, candidates.get(server.random.nextInt(candidates.size())))) {
                return InteractionResult.FAIL;
            }
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            server.sendParticles(ParticleTypes.INSTANT_EFFECT, pos.getX() + 0.5, pos.getY() + 0.35, pos.getZ() + 0.5,
                    16, 0.22, 0.28, 0.22, 0.01);
            server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.55F, 1.2F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
