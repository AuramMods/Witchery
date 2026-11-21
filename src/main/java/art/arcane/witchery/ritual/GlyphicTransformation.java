package art.arcane.witchery.ritual;

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

    public static boolean transform(ServerLevel level, BlockPos heart) {
        AABB range = new AABB(heart.getX() - 4, heart.getY() - 2, heart.getZ() - 4,
                heart.getX() + 4, heart.getY() + 2, heart.getZ() + 4);
        List<ItemEntity> chalk = level.getEntitiesOfClass(ItemEntity.class, range,
                drop -> !drop.getItem().isDamaged() && glyphFor(drop.getItem().getItem()) != null);
        if (chalk.isEmpty()) return false;
        ItemEntity first = chalk.get(0);
        Item chosen = first.getItem().getItem();
        Block glyph = glyphFor(chosen);
        int amount = chalk.stream().filter(drop -> drop.getItem().is(chosen)).mapToInt(drop -> drop.getItem().getCount()).sum();
        RitualCircle ring = switch (Math.min(amount, 3)) {
            case 1 -> RitualCircle.SMALL_RITE;
            case 2 -> RitualCircle.MEDIUM_RITE;
            default -> RitualCircle.LARGE_RITE;
        };
        if (first.getItem().getCount() == 1) first.discard();
        else first.setItem(first.getItem().copyWithCount(first.getItem().getCount() - 1));
        ring.recolor(level, heart, glyph);
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
