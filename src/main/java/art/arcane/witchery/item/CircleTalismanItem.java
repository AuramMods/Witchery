package art.arcane.witchery.item;

import art.arcane.witchery.block.RitualGlyphBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.ritual.RitualCircle;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;

public final class CircleTalismanItem extends Item {
    static final String PATTERN_TAG = "CirclePattern";
    private static final List<RitualCircle> RINGS = List.of(RitualCircle.SMALL_RITE, RitualCircle.MEDIUM_RITE, RitualCircle.LARGE_RITE);
    private static final List<String> CHALK = List.of("circleglyphritual", "circleglyphotherwhere", "circleglyphinfernal");

    public CircleTalismanItem() {
        super(new Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
    }

    public static int storedPattern(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return 0;
        int packed = tag.contains(PATTERN_TAG, Tag.TAG_ANY_NUMERIC) ? tag.getInt(PATTERN_TAG) : tag.getInt("Damage");
        return normalize(packed);
    }

    public static void setStoredPattern(ItemStack stack, int pattern) {
        pattern = normalize(pattern);
        stack.removeTagKey("Damage");
        if (pattern == 0) stack.removeTagKey(PATTERN_TAG);
        else stack.getOrCreateTag().putInt(PATTERN_TAG, pattern);
    }

    private static int normalize(int packed) {
        int normalized = 0;
        if (packed < 0) return normalized;
        for (int ring = 0; ring < RINGS.size(); ring++) {
            int chalk = chalkAt(packed, ring);
            if (chalk <= CHALK.size()) normalized |= chalk << (ring * 3);
        }
        return normalized;
    }

    private static int chalkAt(int packed, int ring) {
        return packed >>> (ring * 3) & 7;
    }

    public static int model(ItemStack stack) {
        int pattern = storedPattern(stack);
        for (int ring = RINGS.size() - 1; ring >= 0; ring--) {
            int chalk = chalkAt(pattern, ring);
            if (chalk != 0) return ring * 3 + chalk;
        }
        return 0;
    }

    public static int capturePattern(ServerLevel level, BlockPos heart) {
        int stored = 0;
        for (int ring = 0; ring < RINGS.size(); ring++) {
            RitualCircle circle = RINGS.get(ring);
            Block glyph = level.getBlockState(heart.offset(circle.offsets().get(0))).getBlock();
            for (int chalk = 0; chalk < CHALK.size(); chalk++) {
                if (glyph == WitcheryBlocks.getRequired(CHALK.get(chalk)).get()
                        && circle.offsets().stream().allMatch(offset -> level.getBlockState(heart.offset(offset)).is(glyph))) {
                    stored |= (chalk + 1) << (ring * 3);
                    break;
                }
            }
        }
        return stored;
    }

    public static void eraseCapturedGlyphs(ServerLevel level, BlockPos heart) {
        for (RitualCircle ring : RINGS) {
            for (BlockPos offset : ring.offsets()) {
                BlockPos pos = heart.offset(offset);
                if (level.getBlockState(pos).getBlock() instanceof RitualGlyphBlock) level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || storedPattern(stack) == 0) return InteractionResult.PASS;
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        // Forge may roll back the entire placement. Return the blank only after it accepts the blocks.
        InteractionResult placed = ForgeHooks.onPlaceItemIntoWorld(new PlacementContext(player, context));
        if (!placed.consumesAction()) {
            level.playSound(null, context.getClickedPos(), SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 0.5F, 0.5F);
            return InteractionResult.FAIL;
        }
        if (stack.getCount() == 1) setStoredPattern(stack, 0);
        else {
            stack.shrink(1);
            player.getInventory().placeItemBackInInventory(new ItemStack(this));
        }
        level.sendParticles(ParticleTypes.SMOKE, context.getClickLocation().x, context.getClickLocation().y + 0.2,
                context.getClickLocation().z, 12, 0.5, 0.15, 0.5, 0.02);
        level.playSound(null, context.getClickedPos(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, 1);
        return placed;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context instanceof PlacementContext) || !(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.PASS;
        }
        int pattern = storedPattern(context.getItemInHand());
        if (pattern == 0) return InteractionResult.PASS;

        Block heart = WitcheryBlocks.getRequired("circle").get();
        BlockPos clicked = context.getClickedPos();
        boolean existingHeart = context.getClickedFace() == Direction.UP && level.getBlockState(clicked).is(heart);
        BlockPos center = existingHeart ? clicked : clicked.above();
        var placement = new LinkedHashMap<BlockPos, BlockState>();
        if (!existingHeart) placement.put(center, heart.defaultBlockState());
        for (int ring = 0; ring < RINGS.size(); ring++) {
            int chalk = chalkAt(pattern, ring);
            if (chalk == 0) continue;
            Block glyph = WitcheryBlocks.getRequired(CHALK.get(chalk - 1)).get();
            for (BlockPos offset : RINGS.get(ring).offsets()) {
                var state = glyph.defaultBlockState().setValue(RitualGlyphBlock.PATTERN, level.random.nextInt(RitualGlyphBlock.PATTERN_COUNT));
                placement.put(center.offset(offset), state);
            }
        }
        for (var target : placement.entrySet()) {
            BlockPos pos = target.getKey();
            if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)
                    || !level.mayInteract(context.getPlayer(), pos)
                    || !context.getPlayer().mayUseItemAt(pos, Direction.UP, context.getItemInHand())) return InteractionResult.FAIL;
            BlockState old = level.getBlockState(pos);
            boolean replacesGlyph = target.getValue().getBlock() instanceof RitualGlyphBlock && old.getBlock() instanceof RitualGlyphBlock;
            if ((!replacesGlyph && (!old.canBeReplaced() || !old.getFluidState().isEmpty()))
                    || !RitualGlyphBlock.supportsGlyph(level, pos)) return InteractionResult.FAIL;
        }
        placement.forEach(level::setBlockAndUpdate);
        return InteractionResult.CONSUME;
    }

    private static final class PlacementContext extends UseOnContext {
        private PlacementContext(Player player, UseOnContext source) {
            super(player, source.getHand(), new BlockHitResult(source.getClickLocation(), source.getClickedFace(),
                    source.getClickedPos(), source.isInside()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        int pattern = storedPattern(stack);
        for (int ring = 0; ring < RINGS.size(); ring++) {
            int chalk = chalkAt(pattern, ring);
            if (chalk != 0) tooltip.add(Component.translatable("tooltip.witchery.circletalisman." + ring + "." + chalk)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
