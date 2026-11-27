package art.arcane.witchery.ritual;

import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class GlyphicTransformation {
    private GlyphicTransformation() {}

    public static boolean transform(ServerLevel level, BlockPos heart, boolean legacy) {
        AABB range = new AABB(heart.getX() - 4, heart.getY() - 2, heart.getZ() - 4,
                heart.getX() + 4, heart.getY() + 2, heart.getZ() + 4);
        List<ItemEntity> chalk = ItemRiteRecipe.droppedItems(level, range, legacy, drop -> {
            var stack = drop.getItem();
            return (legacy ? stack.getDamageValue() == 0 : !stack.isDamaged()) && glyphFor(stack.getItem()) != null;
        });
        if (chalk.isEmpty()) return false;
        ItemEntity first = chalk.get(0);
        Item chosen = first.getItem().getItem();
        Block glyph = glyphFor(chosen);
        int amount = 0;
        for (ItemEntity drop : chalk) {
            var stack = drop.getItem();
            if (!stack.is(chosen)) continue;
            amount += stack.getCount();
            if (drop == first) {
                if (stack.getCount() == 1) drop.discard();
                else drop.setItem(stack.copyWithCount(stack.getCount() - 1));
            }
            if (legacy) WitcheryNetwork.sendGlyphicSmoke(level, drop.position(), true);
        }
        RitualCircle ring = switch (Math.min(amount, 3)) {
            case 1 -> RitualCircle.SMALL_RITE;
            case 2 -> RitualCircle.MEDIUM_RITE;
            default -> RitualCircle.LARGE_RITE;
        };
        ring.recolor(level, heart, glyph, legacy);
        return true;
    }

    @Nullable
    private static Block glyphFor(Item chalk) {
        if (chalk == WitcheryItems.getRequired("chalkritual").get()) return WitcheryBlocks.getRequired("circleglyphritual").get();
        if (chalk == WitcheryItems.getRequired("chalkotherwhere").get()) return WitcheryBlocks.getRequired("circleglyphotherwhere").get();
        if (chalk == WitcheryItems.getRequired("chalkinfernal").get()) return WitcheryBlocks.getRequired("circleglyphinfernal").get();
        return null;
    }
}
