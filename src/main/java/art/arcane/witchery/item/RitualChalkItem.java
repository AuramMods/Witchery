package art.arcane.witchery.item;

import art.arcane.witchery.block.RitualGlyphBlock;
import art.arcane.witchery.event.WitcheryBlockBreak;
import art.arcane.witchery.event.WitcheryBlockPlacement;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

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
        Player player = context.getPlayer();
        ItemStack chalk = context.getItemInHand();
        InteractionResult result = draw(level, context.getClickedPos(), context.getClickedFace(),
                WitcheryBlocks.getRequired(glyph).get(), player, chalk);
        if (!level.isClientSide && result.consumesAction() && player != null && !player.getAbilities().instabuild) {
            damageChalk(chalk, player, context);
        }
        return result;
    }

    public static void drawInfernal(ServerLevel level, BlockHitResult hit, ServerPlayer player) {
        draw(level, hit.getBlockPos(), hit.getDirection(), WitcheryBlocks.getRequired("circleglyphinfernal").get(),
                player, player.getUseItem());
    }

    private static InteractionResult draw(Level level, BlockPos clicked, Direction face, Block glyphBlock,
                                          @Nullable Player player, ItemStack held) {
        boolean replacesGlyph = glyphBlock instanceof RitualGlyphBlock
                && level.getBlockState(clicked).getBlock() instanceof RitualGlyphBlock;
        if (!replacesGlyph && face != Direction.UP) {
            return InteractionResult.PASS;
        }
        BlockPos target = replacesGlyph ? clicked : clicked.above();
        BlockState previous = level.getBlockState(target);
        if ((!replacesGlyph && !previous.canBeReplaced())
                || !RitualGlyphBlock.supportsGlyph(level, target)) {
            return InteractionResult.PASS;
        }

        if (player != null && !player.mayUseItemAt(target, face, held)) {
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel server) {
            BlockState state = glyphBlock.defaultBlockState();
            if (state.hasProperty(RitualGlyphBlock.PATTERN)) {
                state = state.setValue(RitualGlyphBlock.PATTERN, server.random.nextInt(RitualGlyphBlock.PATTERN_COUNT));
            }
            if (state != previous) {
                if (player != null && (!server.mayInteract(player, target)
                        || !server.getWorldBorder().isWithinBounds(target)
                        || !previous.isAir() && WitcheryBlockBreak.isCanceled(server, target, previous, player))) {
                    return InteractionResult.FAIL;
                }
                // Resolve the write before sound or splitting a stacked chalk item under Forge's outer item-use capture.
                boolean placed = player == null ? server.setBlockAndUpdate(target, state)
                        : WitcheryBlockPlacement.placeNow(server, target, state, player, face);
                if (!placed) return InteractionResult.FAIL;
            }
            server.playSound(null, clicked.getX(), clicked.getY(), clicked.getZ(),
                    WitcherySounds.CHALK.get(), SoundSource.BLOCKS, 1, 1);
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
