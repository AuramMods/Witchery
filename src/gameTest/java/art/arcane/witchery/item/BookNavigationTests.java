package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.network.WitcheryNetwork.ItemUpdatePacket;
import art.arcane.witchery.registry.WitcheryItems;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BookNavigationTests {
    @GameTest(template = "qa_coven_arena")
    public static void atlasShortcutsFollowTheLegacyFamilyOrder(GameTestHelper helper) {
        var access = helper.getLevel().registryAccess();
        var index = BiomeBookIndex.index(access);
        var registry = access.registryOrThrow(Registries.BIOME);
        var families = List.of(BiomeTags.IS_BEACH, BiomeTags.IS_FOREST, BiomeTags.IS_HILL, BiomeTags.IS_BADLANDS,
                BiomeTags.IS_MOUNTAIN, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_SANDY, Tags.Biomes.IS_SNOWY,
                Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_WASTELAND, BiomeTags.IS_RIVER, BiomeTags.IS_OCEAN,
                Tags.Biomes.IS_SPOOKY, Tags.Biomes.IS_MAGICAL);
        int sectionNumber = 0;
        int start = 0;
        for (var family : families) {
            var tag = registry.getTag(family).orElse(null);
            if (tag == null) continue;
            var entries = tag.stream().filter(holder -> !holder.is(BiomeTags.IS_NETHER) && !holder.is(BiomeTags.IS_END))
                    .map(holder -> holder.unwrapKey().orElseThrow().location()).sorted().toList();
            if (entries.isEmpty()) continue;
            var section = index.sections().get(sectionNumber++);
            helper.assertTrue(section.name().equals(family.location().getPath().substring(3)) && section.firstPage() == start,
                    "The shortcut for " + family.location() + " points to the first page of that family");
            helper.assertTrue(index.pages().subList(start, start + entries.size()).stream().map(BiomeBookIndex.Entry::id)
                            .toList().equals(entries), "The family contains its tagged biomes in display order");
            start += entries.size();
        }
        helper.assertTrue(sectionNumber >= 8, "The clean registry offers the main atlas families");
        if (start < index.pages().size()) {
            helper.assertTrue(index.sections().size() == sectionNumber + 1
                            && index.sections().get(sectionNumber).equals(new BiomeBookIndex.Section("other", start)),
                    "Other biomes remain accessible after the named families");
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void atlasUsesTheClientBiomeIdWhenThePageNumberChanges(GameTestHelper helper) {
        var level = helper.getLevel();
        var access = level.registryAccess();
        var book = new ItemStack(WitcheryItems.getRequired("bookbiomes2").get());
        var pages = BiomeBookIndex.pages(access);
        var snowyBeach = ResourceLocation.parse("minecraft:snowy_beach");
        int stalePage = 0;
        helper.assertFalse(pages.get(stalePage).id().equals(snowyBeach), "The client hint now refers to a different biome");
        var packet = new ItemUpdatePacket(0, GuideBookItem.Volume.ATLAS, stalePage, snowyBeach);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            ItemUpdatePacket.encode(packet, buffer);
            helper.assertTrue(ItemUpdatePacket.decode(buffer).equals(packet), "The atlas selection travels with its biome ID");
        } finally {
            buffer.release();
        }
        int resolved = BiomeBookIndex.saveSelection(book, access, packet.pageIndex(), packet.biomeId());
        helper.assertTrue(resolved > 0 && GuideBookItem.bookmark(book) == resolved
                        && snowyBeach.equals(BiomeBookIndex.selectedBiome(book, access)),
                "The selected biome is restored at its current page");
        int rejected = BiomeBookIndex.saveSelection(book, access, 0, ResourceLocation.parse("witchery:missing_biome"));
        helper.assertTrue(rejected == -1 && GuideBookItem.bookmark(book) == resolved,
                "A removed biome cannot change the previous selection");
        var missing = book.copy();
        missing.getOrCreateTag().putString(BiomeNoteItem.BIOME, "witchery:missing_biome");
        helper.assertTrue(BiomeBookIndex.selectedBiome(missing, access) == null,
                "A saved removed biome cannot copy an unrelated entry at the same page");

        var grid = new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0) {
            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        }, 2, 2);
        grid.setItem(0, Items.PAPER.getDefaultInstance());
        grid.setItem(1, ItemStack.of(book.save(new CompoundTag())));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).orElseThrow();
        ItemStack note = recipe.assemble(grid, access);
        helper.assertTrue(snowyBeach.equals(BiomeNoteItem.biomeId(note)), "The reopened atlas copies the same biome");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void vampireJournalKeepsTheOriginalPageTrail(GameTestHelper helper) {
        ItemStack journal = new ItemStack(WitcheryItems.getRequired("vampirebook").get());
        ListTag original = new ListTag();
        for (String page : List.of("ritual1", "ritual2", "ritual3")) original.add(StringTag.valueOf(page));
        journal.getOrCreateTag().put("pageStack", original);
        helper.assertTrue(VampireBookItem.bookmark(journal) == 3, "The original journal opens to the last visited page");
        helper.assertFalse(VampireBookItem.setBookmark(journal, "transfix"), "A missing torn page keeps its chapter locked");
        helper.assertTrue(journal.getTag().getList("pageStack", Tag.TAG_STRING).equals(original), "A rejected change keeps the trail");

        VampireBookItem.setTornPages(journal, 1);
        helper.assertTrue(VampireBookItem.setBookmark(journal, "transfix"), "The first restored page unlocks the next chapter");
        ItemStack reopened = ItemStack.of(journal.save(new CompoundTag()));
        ListTag trail = reopened.getTag().getList("pageStack", Tag.TAG_STRING);
        helper.assertTrue(trail.size() == 6 && trail.getString(4).equals("ritual5") && trail.getString(5).equals("transfix"),
                "The serialized book holds all pages along the trail");
        helper.assertTrue(VampireBookItem.setBookmark(reopened, "toc"), "The cover is always available");
        helper.assertTrue(VampireBookItem.bookmark(reopened) == 0 && VampireBookItem.tornPages(reopened) == 1
                        && reopened.getTag().getList("pageStack", Tag.TAG_STRING).isEmpty(),
                "Returning to the cover clears only the navigation trail");
        helper.succeed();
    }
}
