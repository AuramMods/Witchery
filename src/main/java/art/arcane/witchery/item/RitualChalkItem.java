package art.arcane.witchery.item;

import art.arcane.witchery.block.RitualGlyphBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class RitualChalkItem extends Item {
    private static final int USES = 64;
    private final String glyph;

    public RitualChalkItem(String glyph) {
        super(new Properties().durability(USES).setNoRepair());
        this.glyph = glyph;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stack.isDamaged() ? 1 : USES;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockState existing = level.getBlockState(clicked);
        Block glyphBlock = WitcheryBlocks.getRequired(glyph).get();
        boolean replacesGlyph = glyphBlock instanceof RitualGlyphBlock && existing.getBlock() instanceof RitualGlyphBlock;
        if (!replacesGlyph && context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        BlockPos target = replacesGlyph ? clicked : clicked.above();
        if ((!replacesGlyph && !level.getBlockState(target).canBeReplaced())
                || !RitualGlyphBlock.supportsGlyph(level, target)) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        ItemStack chalk = context.getItemInHand();
        if (player != null && !player.mayUseItemAt(target, context.getClickedFace(), chalk)) {
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel server) {
            BlockState state = glyphBlock.defaultBlockState();
            if (state.hasProperty(RitualGlyphBlock.PATTERN)) {
                state = state.setValue(RitualGlyphBlock.PATTERN, server.random.nextInt(RitualGlyphBlock.PATTERN_COUNT));
            }
            if (state != level.getBlockState(target) && !server.setBlockAndUpdate(target, state)) {
                return InteractionResult.FAIL;
            }
            server.playSound(null, target, WitcherySounds.CHALK.get(), SoundSource.BLOCKS, 1, 1);
            if (player != null && !player.getAbilities().instabuild) {
                damageChalk(chalk, player, context);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void damageChalk(ItemStack chalk, Player player, UseOnContext context) {
        ItemStack unused = ItemStack.EMPTY;
        if (chalk.getCount() > 1) {
            unused = chalk.copyWithCount(chalk.getCount() - 1);
            chalk.setCount(1);
        }
        chalk.hurtAndBreak(1, player, holder -> holder.broadcastBreakEvent(context.getHand()));
        if (!unused.isEmpty() && !player.getInventory().add(unused)) {
            player.drop(unused, false);
        }
    }
}
