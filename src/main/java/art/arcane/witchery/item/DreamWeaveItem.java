package art.arcane.witchery.item;

import art.arcane.witchery.block.DreamWeave;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public final class DreamWeaveItem extends BlockItem {
    private final DreamWeave weave;

    public DreamWeaveItem(Block block, DreamWeave weave) {
        super(block, new Properties().rarity(Rarity.UNCOMMON));
        this.weave = weave;
    }

    public DreamWeave weave() {
        return weave;
    }

    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItem, Item item) {
        // Keep the existing dreamcatcher item as the default block-to-item registration.
    }
}
