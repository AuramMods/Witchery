package art.arcane.witchery.block;

import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public enum WitchCrop {
    BELLADONNA("belladonna", "seedsbelladonna", 4, 4),
    MANDRAKE("mandrake", "seedsmandrake", 4, 5),
    ARTICHOKE("artichoke", "seedsartichoke", 4, 3),
    SNOWBELL("snowbell", "seedssnowbell", 4, 2),
    WORMWOOD("wormwood", "seedswormwood", 4, 0),
    MINDRAKE("mindrake", "seedsmindrake", 4, 0),
    WOLFSBANE("wolfsbane", "seedswolfsbane", 7, 1),
    GARLIC("garlicplant", "garlic", 5, 1);

    private final String blockId;
    private final String seedId;
    private final int maxAge;
    private final int grassWeight;

    WitchCrop(String blockId, String seedId, int maxAge, int grassWeight) {
        this.blockId = blockId;
        this.seedId = seedId;
        this.maxAge = maxAge;
        this.grassWeight = grassWeight;
    }

    public Block block() {
        return WitcheryBlocks.getRequired(blockId).get();
    }

    public Item seed() {
        return WitcheryItems.getRequired(seedId).get();
    }

    public int maxAge() {
        return maxAge;
    }

    public int grassWeight() {
        return grassWeight;
    }

    public static WitchCrop forBlock(String blockId) {
        for (WitchCrop crop : values()) {
            if (crop.blockId.equals(blockId)) {
                return crop;
            }
        }
        throw new IllegalArgumentException("Unknown Witchery crop " + blockId);
    }

    @Nullable
    public static WitchCrop forSeed(String seedId) {
        for (WitchCrop crop : values()) {
            if (crop.seedId.equals(seedId)) {
                return crop;
            }
        }
        return null;
    }
}
