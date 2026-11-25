package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.AlluringSkullBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyAlluringSkullItemTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_alluring_skull_item_import")
    public static void preservesActiveSkullsInOriginalAndConvertedInventories(GameTestHelper helper) throws Exception {
        var registry = TagParser.parseTag("{ItemData:[{K:\"\u0002witchery:alluringskull\",V:1201}]}");
        var directory = Files.createTempDirectory("witchery-skull-item-import");
        var snapshot = directory.resolve("witchery-legacy-ids.dat");
        try {
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var ids = LegacyWorldIds.find(directory);
            var original = TagParser.parseTag("{id:1201s,Count:1b,Damage:1s,tag:{Marker:\"original\"}}");
            var dormant = TagParser.parseTag("{id:1201s,Count:1b,Damage:0s}");
            ids.upgradeItems(original);
            ids.upgradeItems(dormant);
            var active = ItemStack.of(original);
            helper.assertTrue(AlluringSkullBlock.isActiveItem(active) && "original".equals(active.getTag().getString("Marker"))
                            && !AlluringSkullBlock.isActiveItem(ItemStack.of(dormant)),
                    "Only the original active variant gets the active block state and its other tags stay intact");

            var converted = TagParser.parseTag("""
                    {Item:{id:"witchery:alluringskull",Count:1b,Damage:1s,tag:{display:{Name:"Archive skull"}}},
                     Other:{id:1201s,Count:1b,Damage:1s,tag:{BlockStateTag:{active:"false"}}}}
                    """);
            var fixer = helper.getLevel().getServer().getFixerUpper();
            ids.repairStoredItems(converted, fixer);
            ids.repairStoredItems(converted, fixer);
            var repaired = ItemStack.of(converted.getCompound("Item"));
            helper.assertTrue(AlluringSkullBlock.isActiveItem(repaired) && repaired.getHoverName().getString().equals("Archive skull")
                            && !AlluringSkullBlock.isActiveItem(ItemStack.of(converted.getCompound("Other")))
                            && !converted.getCompound("Item").contains("Damage"),
                    "The ordinary load repair preserves names and the explicit modern state across repeat loads");

            var support = new BlockPos(7, 2, 7);
            helper.setBlock(support, Blocks.STONE);
            var pos = helper.absolutePos(support);
            var level = helper.getLevel();
            var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ArchiveSkull"));
            player.setItemInHand(InteractionHand.MAIN_HAND, active);
            var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
            helper.assertTrue(active.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction(),
                    "The imported item places through normal item use");
            var placed = level.getBlockState(pos.above());
            helper.assertTrue(placed.is(WitcheryBlocks.getRequired("alluringskull").get())
                            && placed.getValue(AlluringSkullBlock.ACTIVE) && active.isEmpty(),
                    "The placed skull starts active and spends the imported item");
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(directory);
        }
        helper.succeed();
    }
}
