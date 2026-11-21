package art.arcane.witchery.world;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class VillageBookshopProcessor extends StructureProcessor {
    private static final VillageBookshopProcessor INSTANCE = new VillageBookshopProcessor();
    public static final Codec<VillageBookshopProcessor> CODEC = Codec.unit(INSTANCE);
    private static final int CHEST_SLOTS = 27;

    private VillageBookshopProcessor() {}

    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos origin, BlockPos pivot,
            StructureTemplate.StructureBlockInfo raw, StructureTemplate.StructureBlockInfo placed,
            StructurePlaceSettings settings, @Nullable StructureTemplate template) {
        if (!raw.state().is(Blocks.CHEST)) return placed;
        var random = settings.getRandom(placed.pos());
        var catalog = WeightedRandomList.create(stock());
        var items = NonNullList.withSize(CHEST_SLOTS, ItemStack.EMPTY);
        int rolls = 5 + random.nextInt(6);
        for (int roll = 0; roll < rolls; roll++) {
            ItemStack loot = catalog.getRandom(random).orElseThrow().getData();
            items.set(random.nextInt(items.size()), loot.copy());
        }
        items.set(random.nextInt(items.size()), new ItemStack(WitcheryItems.getRequired("vampirebook").get()));
        CompoundTag tag = placed.nbt() == null ? new CompoundTag() : placed.nbt();
        ContainerHelper.saveAllItems(tag, items);
        return new StructureTemplate.StructureBlockInfo(placed.pos(), placed.state(), tag);
    }

    @Override
    public StructureTemplate.StructureEntityInfo processEntity(LevelReader level, BlockPos origin,
            StructureTemplate.StructureEntityInfo raw, StructureTemplate.StructureEntityInfo placed,
            StructurePlaceSettings settings, StructureTemplate template) {
        if (!"minecraft:item_frame".equals(raw.nbt.getString("id"))) return placed;
        var catalog = stock();
        var random = settings.getRandom(placed.blockPos);
        var item = catalog.get(random.nextInt(catalog.size())).getData();
        var tag = placed.nbt.copy();
        tag.put("Item", item.save(new CompoundTag()));
        tag.putInt("TileX", placed.blockPos.getX());
        tag.putInt("TileY", placed.blockPos.getY());
        tag.putInt("TileZ", placed.blockPos.getZ());
        return new StructureTemplate.StructureEntityInfo(placed.pos, placed.blockPos, tag);
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level, BlockPos origin,
            BlockPos pivot, List<StructureTemplate.StructureBlockInfo> raw,
            List<StructureTemplate.StructureBlockInfo> placed, StructurePlaceSettings settings) {
        if (raw.stream().noneMatch(block -> block.state().is(Blocks.CHEST))) return placed;
        var blocks = new ArrayList<>(placed);
        var bounds = settings.getBoundingBox();
        for (int x = 1; x <= 6; x++) {
            for (int z = 1; z <= 6; z++) {
                var base = StructureTemplate.calculateRelativePosition(settings, new BlockPos(x, -1, z)).offset(origin);
                if (bounds != null && !bounds.isInside(base)) continue;
                for (BlockPos pos = base; pos.getY() > level.getMinBuildHeight() + 1; pos = pos.below()) {
                    var floor = level.getBlockState(pos);
                    if (!floor.isAir() && !floor.liquid()) break;
                    blocks.add(new StructureTemplate.StructureBlockInfo(pos, Blocks.COBBLESTONE.defaultBlockState(), null));
                }
            }
        }
        return blocks;
    }

    private static List<WeightedEntry.Wrapper<ItemStack>> stock() {
        var catalog = new ArrayList<WeightedEntry.Wrapper<ItemStack>>();
        catalog.add(WeightedEntry.wrap(new ItemStack(Items.BOOK), 1));
        for (int weight = 3; weight >= 1; weight--) {
            catalog.add(WeightedEntry.wrap(new ItemStack(WitcheryIngredient.TORN_VAMPIRE_PAGE.item()), weight));
        }
        for (String entry : WitcheryServerConfig.TOWN_BOOKSHOP_ALLOWED_BOOKS.get()) {
            var stack = configuredBook(entry);
            if (!stack.isEmpty()) catalog.add(WeightedEntry.wrap(stack, 1));
        }
        return catalog;
    }

    private static ItemStack configuredBook(String entry) {
        int comma = entry.lastIndexOf(',');
        var id = ResourceLocation.tryParse((comma < 0 ? entry : entry.substring(0, comma)).trim());
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return ItemStack.EMPTY;
        var tag = new CompoundTag();
        tag.putString("id", id.toString());
        tag.putByte("Count", (byte) 1);
        if (comma >= 0) {
            try {
                tag.putInt("Damage", Integer.parseInt(entry.substring(comma + 1).trim()));
            } catch (NumberFormatException ignored) {
                return ItemStack.EMPTY;
            }
        }
        return WitcheryIngredient.loadInventoryStack(tag);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return WitcheryRegistries.BOOKSHOP_PROCESSOR.get();
    }
}
