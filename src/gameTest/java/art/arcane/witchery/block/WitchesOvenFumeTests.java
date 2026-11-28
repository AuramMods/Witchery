package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.WitchesOvenBlockEntity;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.FUEL_SLOT;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.FUME_SLOT;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.INPUT_SLOT;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.JAR_SLOT;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.RESULT_SLOT;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WitchesOvenFumeTests {
    @GameTest(template = "qa_coven_arena")
    public static void ovenReadsLegacyForestrySpeciesWithoutTheMod(GameTestHelper helper) {
        helper.assertTrue(WitchesOvenBlockEntity.fumeFor(legacyTree("forestry.treeOak"))
                        == WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE,
                "Oak genomes give Exhale of the Horned One");
        helper.assertTrue(WitchesOvenBlockEntity.fumeFor(legacyTree("forestry.treeSpruce"))
                        == WitcheryIngredient.HINT_OF_REBIRTH,
                "Spruce genomes give Hint of Rebirth");
        helper.assertTrue(WitchesOvenBlockEntity.fumeFor(legacyTree("forestry.treeBirch"))
                        == WitcheryIngredient.BREATH_OF_THE_GODDESS,
                "Birch genomes give Breath of the Goddess");
        helper.assertTrue(WitchesOvenBlockEntity.fumeFor(legacyTree("forestry.treeLarch")) == null
                        && WitchesOvenBlockEntity.fumeFor(legacyTree("")) == null,
                "Other or incomplete genomes leave the jar empty");
        helper.assertTrue(WitchesOvenBlockEntity.fumeFor(new ItemStack(Items.JUNGLE_SAPLING)) == WitcheryIngredient.FOUL_FUME,
                "Ordinary approved saplings retain the generic fume");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_oven_input_swap")
    public static void ovenKeepsProgressUntilATickWithoutAValidInput(GameTestHelper helper) {
        var oven = ovenWithFunnel(helper);
        var player = helper.makeMockPlayer();
        var menu = oven.createMenu(1, player.getInventory(), player);
        tick(helper, oven, 80);

        player.getInventory().setItem(0, new ItemStack(Items.BIRCH_SAPLING));
        menu.clicked(INPUT_SLOT, 0, ClickType.SWAP, player);
        helper.assertTrue(oven.getItem(INPUT_SLOT).is(Items.BIRCH_SAPLING)
                        && player.getInventory().getItem(0).is(Items.OAK_SAPLING)
                        && oven.saveWithoutMetadata().getInt("CookTime") == 80,
                "A normal menu swap keeps the old progress and uses the replacement input");

        var birch = oven.removeItem(INPUT_SLOT, 1);
        oven.setItem(INPUT_SLOT, birch);
        tick(helper, oven, 79);
        helper.assertTrue(oven.saveWithoutMetadata().getInt("CookTime") == 159,
                "Removing and returning the input within a tick keeps the batch progress");
        tick(helper, oven, 1);
        helper.assertTrue(oven.getItem(INPUT_SLOT).isEmpty() && oven.getItem(RESULT_SLOT).getCount() == 1,
                "The original 160-tick completion consumes the replacement sapling");

        oven.setItem(INPUT_SLOT, new ItemStack(Items.BIRCH_SAPLING));
        tick(helper, oven, 80);
        var removed = oven.removeItem(INPUT_SLOT, 1);
        tick(helper, oven, 1);
        oven.setItem(INPUT_SLOT, removed);
        helper.assertTrue(oven.saveWithoutMetadata().getInt("CookTime") == 0,
                "An actual tick with an empty input resets the batch");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_oven_fume_stack")
    public static void ovenPreservesTheLastFumeStackSlot(GameTestHelper helper) {
        var oven = ovenWithFunnel(helper);
        var fume = new ItemStack(WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item(), 62);
        fume.setHoverName(Component.literal("Saved Fumes"));
        oven.setItem(FUME_SLOT, fume);
        oven.setItem(INPUT_SLOT, new ItemStack(Items.OAK_SAPLING, 2));
        oven.setItem(JAR_SLOT, new ItemStack(WitcheryIngredient.CLAY_JAR.item(), 2));
        var boost = WitcheryServerConfig.DOUBLE_FUME_FILTER_CHANCE;
        boolean original = boost.get();
        try {
            boost.set(true);
            tick(helper, oven, 160);
            helper.assertTrue(oven.getItem(FUME_SLOT).getCount() == 63 && oven.getItem(FUME_SLOT).hasCustomHoverName()
                            && oven.getItem(JAR_SLOT).getCount() == 1,
                    "A compatible fume joins a stored named stack and consumes one jar");
            tick(helper, oven, 160);
            helper.assertTrue(oven.getItem(RESULT_SLOT).getCount() == 2
                            && oven.getItem(FUME_SLOT).getCount() == 63 && oven.getItem(JAR_SLOT).getCount() == 1,
                    "A stack of 63 fumes blocks the next fume and preserves the jar while ash still cooks");
        } finally {
            boost.set(original);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_oven_inventory")
    public static void ovenGrowsNamedAshWithoutReplacingIt(GameTestHelper helper) {
        var oven = ovenWithFunnel(helper);
        var ash = new ItemStack(WitcheryIngredient.WOOD_ASH.item(), 63);
        ash.setHoverName(Component.literal("Saved Ash"));
        ash.getOrCreateTag().putInt("FixtureMarker", 42);
        oven.setItem(RESULT_SLOT, ash);
        tick(helper, oven, 160);
        helper.assertTrue(oven.getItem(INPUT_SLOT).isEmpty() && oven.getItem(RESULT_SLOT).getCount() == 64
                        && oven.getItem(RESULT_SLOT).getHoverName().getString().equals("Saved Ash")
                        && oven.getItem(RESULT_SLOT).getTag().getInt("FixtureMarker") == 42,
                "Plain ash fills the last result space while preserving the stored item's data");

        oven.setItem(INPUT_SLOT, new ItemStack(Items.OAK_SAPLING));
        tick(helper, oven, 160);
        helper.assertTrue(oven.getItem(INPUT_SLOT).getCount() == 1 && oven.getItem(RESULT_SLOT).getCount() == 64
                        && oven.saveWithoutMetadata().getInt("CookTime") == 0,
                "A full result slot leaves the next input unprocessed");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_oven_inventory")
    public static void ovenUsesOriginalMenuRoutingAndKeepsSidedFilters(GameTestHelper helper) {
        var oven = ovenWithFunnel(helper);
        oven.clearContent();
        var player = helper.makeMockPlayer();
        var menu = oven.createMenu(1, player.getInventory(), player);
        var jar = new ItemStack(WitcheryIngredient.CLAY_JAR.item());
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        var top = oven.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).orElseThrow(IllegalStateException::new);
        var side = oven.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH).orElseThrow(IllegalStateException::new);
        helper.assertTrue(!top.insertItem(0, jar, true).isEmpty() && !side.insertItem(3, sword, true).isEmpty(),
                "Sided automation rejects a jar in the input and a diamond sword as fuel");

        menu.setCarried(jar.copy());
        menu.clicked(INPUT_SLOT, 0, ClickType.PICKUP, player);
        menu.setCarried(sword.copy());
        menu.clicked(FUEL_SLOT, 0, ClickType.PICKUP, player);
        helper.assertTrue(oven.getItem(INPUT_SLOT).is(jar.getItem()) && oven.getItem(FUEL_SLOT).is(Items.DIAMOND_SWORD),
                "The manual input and fuel slots accept items without the sided filters");
        oven.clearContent();

        var inventory = player.getInventory();
        inventory.setItem(0, new ItemStack(Items.IRON_ORE, 2));
        menu.clicked(32, 0, ClickType.QUICK_MOVE, player);
        inventory.setItem(0, new ItemStack(Items.COAL, 3));
        menu.clicked(32, 0, ClickType.QUICK_MOVE, player);
        inventory.setItem(0, jar.copy());
        menu.clicked(32, 0, ClickType.QUICK_MOVE, player);
        inventory.setItem(0, sword.copy());
        menu.clicked(32, 0, ClickType.QUICK_MOVE, player);
        helper.assertTrue(oven.getItem(INPUT_SLOT).is(Items.IRON_ORE) && oven.getItem(INPUT_SLOT).getCount() == 2
                        && oven.getItem(FUEL_SLOT).getCount() == 3 && oven.getItem(JAR_SLOT).is(jar.getItem())
                        && inventory.getItem(9).is(Items.DIAMOND_SWORD) && inventory.getItem(0).isEmpty(),
                "Shift-click routes standard smelting inputs, fuel, clay jars and ordinary tools as in the original");
        tick(helper, oven, 1);
        helper.assertTrue(oven.getItem(RESULT_SLOT).isEmpty() && oven.getItem(FUEL_SLOT).getCount() == 3,
                "The oven does not ignite for the ordinary furnace-only result");
        helper.succeed();
    }

    private static WitchesOvenBlockEntity ovenWithFunnel(GameTestHelper helper) {
        var pos = new BlockPos(5, 4, 5);
        var facing = Direction.NORTH;
        helper.setBlock(pos, WitcheryBlocks.getRequired("witchesovenidle").get().defaultBlockState()
                .setValue(WitchesOvenBlock.FACING, facing));
        helper.setBlock(pos.west(), WitcheryBlocks.getRequired("filteredfumefunnel").get().defaultBlockState()
                .setValue(WitchesOvenBlock.FACING, facing));
        var oven = (WitchesOvenBlockEntity) helper.getBlockEntity(pos);
        oven.setItem(INPUT_SLOT, new ItemStack(Items.OAK_SAPLING));
        oven.setItem(FUEL_SLOT, new ItemStack(Items.COAL));
        return oven;
    }

    private static void tick(GameTestHelper helper, WitchesOvenBlockEntity oven, int ticks) {
        var pos = oven.getBlockPos();
        var level = helper.getLevel();
        for (int i = 0; i < ticks; i++) {
            WitchesOvenBlockEntity.serverTick(level, pos, level.getBlockState(pos), oven);
        }
    }

    private static ItemStack legacyTree(String species) {
        var input = new ItemStack(Items.JUNGLE_SAPLING);
        var chromosomes = new ListTag();
        var active = new CompoundTag();
        active.putString("UID0", species);
        chromosomes.add(active);
        input.getOrCreateTagElement("Genome").put("Chromosomes", chromosomes);
        return input;
    }
}
