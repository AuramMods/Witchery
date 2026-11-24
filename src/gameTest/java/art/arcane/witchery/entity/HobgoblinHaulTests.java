package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class HobgoblinHaulTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_hobgoblin_cargo", setupTicks = 5)
    public static void retrievingCargoPreservesPartiallyAcceptedStacks(GameTestHelper helper) {
        var start = new BlockPos(4, 2, 5);
        helper.setBlock(start.below(), Blocks.STONE);
        helper.setBlock(start.west().below(), Blocks.STONE);
        var goblin = helper.spawn(WitcheryEntities.GOBLIN.get(), start);
        goblin.setNoAi(true);
        goblin.setLeashedTo(helper.spawn(EntityType.ARMOR_STAND, start.west()), false);
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "CargoCollector"));
        var inventory = player.getInventory();
        var cargo = new ItemStack(Items.RAW_IRON, 6);
        cargo.getOrCreateTag().putString("Marker", "Goblin Cargo");
        goblin.setItemInHand(InteractionHand.MAIN_HAND, cargo);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PIG_SPAWN_EGG));
        helper.assertTrue(goblin.exchangePickaxe(player, InteractionHand.MAIN_HAND) == InteractionResult.PASS
                        && goblin.getMainHandItem().getCount() == 6,
                "A spawn egg should pass through to the usual mob interaction without retrieving cargo");
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            inventory.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        inventory.setItem(0, cargo.copyWithCount(63));

        Consumer<EntityJoinLevelEvent> denyDrop = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item
                    && ItemStack.isSameItemSameTags(item.getItem(), cargo)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(denyDrop);
        try {
            goblin.exchangePickaxe(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(inventory.getItem(0).getCount() == 64
                            && ItemStack.matches(goblin.getMainHandItem(), cargo.copyWithCount(5)),
                    "The goblin should retain exactly the remainder when a partial transfer's drop is denied");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(denyDrop);
        }

        inventory.getItem(0).shrink(2);
        goblin.exchangePickaxe(player, InteractionHand.MAIN_HAND);
        helper.succeedWhen(() -> {
            var overflow = level.getEntitiesOfClass(ItemEntity.class, new AABB(goblin.blockPosition()).inflate(3),
                    drop -> ItemStack.isSameItemSameTags(drop.getItem(), cargo));
            helper.assertTrue(inventory.getItem(0).getCount() == 64 && goblin.getMainHandItem().isEmpty()
                            && overflow.size() == 1 && overflow.get(0).getItem().getCount() == 3,
                    "Retry places two more in the inventory and drops the remaining three exactly once");
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_storage_waypoint", timeoutTicks = 180)
    public static void walksPastWaypointToleranceToDepositOre(GameTestHelper helper) {
        var storage = new BlockPos(3, 280, 7);
        var start = storage.offset(1, 0, -2);
        for (var pos : BlockPos.betweenClosed(storage.offset(-1, -1, -4), storage.offset(3, -1, 0))) {
            helper.setBlock(pos, Blocks.BEDROCK);
        }
        helper.setBlock(storage, Blocks.BARREL);
        var barrel = (BarrelBlockEntity) helper.getBlockEntity(storage);
        var goblin = helper.spawn(WitcheryEntities.GOBLIN.get(), start);
        var holder = helper.spawn(EntityType.ARMOR_STAND, storage.offset(3, 0, -1));
        var waypoint = helper.absolutePos(start);
        goblin.setPos(waypoint.getX() + 0.797834041, waypoint.getY(), waypoint.getZ() + 0.373567435);
        goblin.setLeashedTo(holder, false);
        goblin.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RAW_IRON));
        var storageCenter = Vec3.atCenterOf(barrel.getBlockPos());
        helper.assertTrue(Vec3.atBottomCenterOf(waypoint).distanceToSqr(storageCenter) < 6.25
                        && goblin.distanceToSqr(storageCenter) > 6.25,
                "The node is inside the deposit radius while the goblin is outside it");

        helper.succeedWhen(() -> {
            helper.assertTrue(barrel.countItem(Items.RAW_IRON) == 1 && goblin.getMainHandItem().isEmpty(),
                    "The goblin walks into range and unloads even when navigation is already at the waypoint");
            helper.assertTrue(goblin.distanceToSqr(storageCenter) <= 6.25 && goblin.getLeashHolder() == holder,
                    "The goblin deposited within 2.5 blocks and kept its lead");
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_covered_storage", setupTicks = 5, timeoutTicks = 180)
    public static void haulsToAContainerFromItsWalkableSide(GameTestHelper helper) {
        BlockPos start = new BlockPos(4, 280, 5);
        BlockPos storage = start.east(5);
        for (var pos : BlockPos.betweenClosed(start.offset(-2, -1, -2), storage.offset(2, -1, 2))) {
            helper.setBlock(pos, Blocks.STONE);
        }
        helper.setBlock(storage, Blocks.BARREL);
        helper.setBlock(storage.above(), Blocks.STONE);
        helper.setBlock(storage.above(2), Blocks.STONE);
        var barrel = (BarrelBlockEntity) helper.getBlockEntity(storage);
        var goblin = helper.spawn(WitcheryEntities.GOBLIN.get(), start);
        var holder = helper.spawn(EntityType.ARMOR_STAND, start.south());
        goblin.setLeashedTo(holder, false);
        goblin.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.RAW_IRON, 3));
        helper.assertTrue(goblin.distanceToSqr(Vec3.atCenterOf(barrel.getBlockPos())) > 6.25,
                "The goblin starts outside transfer range");
        helper.succeedWhen(() -> {
            helper.assertTrue(barrel.countItem(Items.RAW_IRON) == 3 && goblin.getMainHandItem().isEmpty(),
                    "The leashed goblin walks to the covered barrel and deposits all three ore from the side: position="
                            + goblin.position() + ", ticks=" + goblin.tickCount + ", on ground=" + goblin.onGround()
                            + ", lead=" + goblin.isLeashed() + ", held=" + goblin.getMainHandItem()
                            + ", navigation=" + goblin.getNavigation().getPath() + "/" + goblin.getNavigation().getTargetPos()
                            + ", barrel=" + barrel.getBlockPos());
            helper.assertTrue(goblin.isLeashed(), "The goblin remains on the lead after delivering its load");
        });
    }
}
