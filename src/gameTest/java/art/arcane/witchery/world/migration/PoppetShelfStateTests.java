package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.PoppetShelfBlock;
import art.arcane.witchery.block.entity.PoppetShelfBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PoppetShelfStateTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_poppet_shelf_state")
    public static void breakingShelfScattersItsSavedContents(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("poppetshelf").get().defaultBlockState());
        var shelf = (PoppetShelfBlockEntity) level.getBlockEntity(pos);
        var gunpowder = new ItemStack(Items.GUNPOWDER, 48);
        gunpowder.setHoverName(Component.literal("Saved Offering"));
        shelf.setItem(0, gunpowder);
        var ward = WitcheryItems.getRequired("death_protection_poppet").get().getDefaultInstance();
        ward.getOrCreateTag().putInt("FixtureMarker", 42);
        shelf.setItem(6, ward);

        var drops = new ArrayList<ItemEntity>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity drop
                    && drop.distanceToSqr(Vec3.atCenterOf(pos)) < 4) drops.add(drop);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            level.random.setSeed(42);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            helper.assertTrue(drops.stream().map(drop -> drop.getItem().getCount()).toList().equals(List.of(27, 10, 11, 1))
                            && drops.subList(0, 3).stream().allMatch(drop -> drop.getItem().getHoverName().getString().equals("Saved Offering"))
                            && drops.get(3).getItem().getTag().getInt("FixtureMarker") == 42
                            && drops.stream().allMatch(drop -> drop.saveWithoutId(new CompoundTag()).getShort("PickupDelay") == 0),
                    "Break drops keep the original fragmentation, slot order, custom data and pickup delay");
            helper.assertTrue(!PoppetShelfBlockEntity.activeShelves(level.getServer()).contains(shelf),
                    "Breaking the shelf removes it from future ward searches");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            drops.forEach(ItemEntity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_poppet_shelf_state")
    public static void importsLegacyDirectionsAndUpdatesOnlyThePowerBit(GameTestHelper helper) throws Exception {
        var shelf = WitcheryBlocks.getRequired("poppetshelf");
        for (Direction direction : Direction.values()) {
            for (int powerBit : new int[]{0, 8}) {
                int metadata = direction.get3DDataValue() | powerBit;
                var state = LegacySchematicBlocks.read(shelf.getId().toString(), metadata, 0);
                helper.assertTrue(state != null && state.is(shelf.get()) && state.getValue(PoppetShelfBlock.FACING) == direction
                                && state.getValue(PoppetShelfBlock.POWERED) == (powerBit != 0),
                        "Decode original shelf metadata " + metadata);
            }
        }

        var earlierPort = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),
                TagParser.parseTag("{Name:\"witchery:poppetshelf\"}"));
        helper.assertTrue(earlierPort.is(shelf.get()) && earlierPort.getValue(PoppetShelfBlock.FACING) == Direction.SOUTH
                        && !earlierPort.getValue(PoppetShelfBlock.POWERED),
                "Open the earlier shelf palette using the original default direction");

        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        var support = pos.below();
        level.setBlockAndUpdate(support, Blocks.STONE.defaultBlockState());
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ShelfPlacement"));
        player.moveTo(pos.getX() + 0.5, pos.getY() + 0.185, pos.getZ() - 0.5, 0, -30);
        var stack = new ItemStack(shelf.get());
        var hit = new BlockHitResult(Vec3.atBottomCenterOf(pos), Direction.UP, support, false);
        var item = (BlockItem) stack.getItem();
        helper.assertTrue(item.place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit)).consumesAction()
                        && level.getBlockState(pos).getValue(PoppetShelfBlock.FACING) == Direction.UP,
                "Placement uses the original position-based vertical threshold regardless of pitch");

        var blockEntity = (PoppetShelfBlockEntity) level.getBlockEntity(pos);
        var ward = new ItemStack(WitcheryItems.getRequired("death_protection_poppet").get());
        blockEntity.setItem(4, ward);
        level.setBlockAndUpdate(pos.above(), Blocks.REDSTONE_BLOCK.defaultBlockState());
        helper.assertTrue(level.getBlockState(pos).getValue(PoppetShelfBlock.POWERED),
                "Power from an adjacent source sets the shelf latch");
        level.removeBlock(pos.above(), false);
        helper.assertTrue(!level.getBlockState(pos).getValue(PoppetShelfBlock.POWERED),
                "Removing the adjacent source clears the latch");
        level.setBlockAndUpdate(pos.above(2), Blocks.REDSTONE_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos.east(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(level.getBlockState(pos).getValue(PoppetShelfBlock.POWERED)
                        && level.getBlockState(pos).getValue(PoppetShelfBlock.FACING) == Direction.UP
                        && level.getBlockEntity(pos) == blockEntity && ItemStack.matches(blockEntity.getItem(4), ward),
                "A neighbor update samples the space above the shelf and preserves the original block entity and ward");
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        helper.succeed();
    }
}
