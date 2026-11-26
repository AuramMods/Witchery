package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewSubstitutionTests {
    private static final BlockPos STRUCK = new BlockPos(5, 4, 7);
    private static final String BATCH = "witchery_substitution";

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void substitutionTakesMaterialAtTheOriginalProjectileBoundary(GameTestHelper helper) {
        helper.setBlock(STRUCK, Blocks.STONE);
        helper.setBlock(STRUCK.east().above(), Blocks.STONE);
        BlockPos struck = helper.absolutePos(STRUCK);
        Vec3 contact = contact(struck);
        var edge = drop(helper, contact.add(0, 0, -5.5), Blocks.BRICKS, 2);
        var outside = drop(helper, contact.add(0, 0, 3.5), Blocks.QUARTZ_BLOCK, 1);

        helper.startSequence().thenWaitUntil(() -> {
            assertLoaded(helper, edge);
            assertLoaded(helper, outside);
        }).thenExecute(() -> {
            cast(helper, struck);
            helper.assertBlockPresent(Blocks.BRICKS, STRUCK);
            helper.assertBlockPresent(Blocks.BRICKS, STRUCK.east().above());
            helper.assertTrue(edge.isRemoved() && !outside.isRemoved(), "Only the two blocks inside the projectile sphere are spent");
            helper.assertTrue(countDrops(helper, struck, Blocks.STONE) == 2, "Both exchanged blocks are returned");
            outside.discard();
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void substitutionExchangesSplitVariantsOfTheStruckBlock(GameTestHelper helper) {
        helper.setBlock(STRUCK, Blocks.RED_WOOL);
        helper.setBlock(STRUCK.east(), Blocks.WHITE_WOOL);
        helper.setBlock(STRUCK.east(2), Blocks.RED_CONCRETE);
        BlockPos struck = helper.absolutePos(STRUCK);
        var bricks = drop(helper, contact(struck).add(0, 0, -2), Blocks.BRICKS, 3);

        helper.startSequence().thenWaitUntil(() -> assertLoaded(helper, bricks)).thenExecute(() -> {
            cast(helper, struck);
            helper.assertBlockPresent(Blocks.BRICKS, STRUCK);
            helper.assertBlockPresent(Blocks.BRICKS, STRUCK.east());
            helper.assertBlockPresent(Blocks.RED_CONCRETE, STRUCK.east(2));
            helper.assertTrue(bricks.getItem().getCount() == 1, "Only the two old wool variants are exchanged");
            helper.assertTrue(countDrops(helper, struck, Blocks.RED_WOOL) == 2
                            && countDrops(helper, struck, Blocks.WHITE_WOOL) == 0,
                    "The returned blocks keep the struck block's color");
            bricks.discard();
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void substitutionSpendsTheBottleAgainstEnclosedSurfaces(GameTestHelper helper) {
        helper.setBlock(STRUCK, Blocks.CALCITE);
        helper.setBlock(STRUCK.above(), Blocks.BEDROCK);
        BlockPos struck = helper.absolutePos(STRUCK);
        var bricks = drop(helper, contact(struck).add(1, 0, -1.5), Blocks.BRICKS, 1);

        helper.startSequence().thenWaitUntil(() -> assertLoaded(helper, bricks)).thenExecute(() -> {
            cast(helper, struck);
            helper.assertBlockPresent(Blocks.CALCITE, STRUCK);
            helper.assertTrue(!bricks.isRemoved() && countDrops(helper, struck, Blocks.CALCITE) == 0,
                    "An enclosed surface leaves all materials intact");
            helper.assertTrue(countDrops(helper, struck, WitcheryIngredient.BREW_OF_SUBSTITUTION.item()) == 0,
                    "The original valid cast spent the bottle with no exposed surface");
            bricks.discard();
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void substitutionReturnsTheBottleWhenForgeVetoesPlacement(GameTestHelper helper) {
        helper.setBlock(STRUCK, Blocks.CALCITE);
        BlockPos struck = helper.absolutePos(STRUCK);
        var bricks = drop(helper, contact(struck).add(1, 0, -1.5), Blocks.BRICKS, 1);
        var owner = EntityType.PIG.create(helper.getLevel());

        helper.startSequence().thenWaitUntil(() -> assertLoaded(helper, bricks)).thenExecute(() -> {
            Consumer<BlockEvent.EntityPlaceEvent> veto = event -> {
                if (event.getLevel() == helper.getLevel() && event.getPos().equals(struck)) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(veto);
            try {
                cast(helper, struck, owner);
            } finally {
                MinecraftForge.EVENT_BUS.unregister(veto);
            }
            helper.assertBlockPresent(Blocks.CALCITE, STRUCK);
            helper.assertTrue(!bricks.isRemoved() && countDrops(helper, struck, Blocks.CALCITE) == 0,
                    "The protected exchange does not spend or return materials");
            helper.assertTrue(countDrops(helper, struck, WitcheryIngredient.BREW_OF_SUBSTITUTION.item()) == 1,
                    "The port refunds the brew when Forge denies every exchange");
            bricks.discard();
        }).thenSucceed();
    }

    private static Vec3 contact(BlockPos struck) {
        return Vec3.atBottomCenterOf(struck).add(0, 0.5, -0.5);
    }

    private static void cast(GameTestHelper helper, BlockPos struck) {
        cast(helper, struck, null);
    }

    private static void cast(GameTestHelper helper, BlockPos struck, Entity owner) {
        var brew = new ThrownBrew(WitcheryEntities.THROWN_BREW.get(), helper.getLevel());
        if (owner != null) brew.setOwner(owner);
        var item = (ThrowableBrewItem) WitcheryIngredient.BREW_OF_SUBSTITUTION.item();
        brew.setItem(item.getDefaultInstance());
        Vec3 contact = contact(struck);
        brew.setPos(contact.add(0, 0, -1.5));
        item.impact(brew, new BlockHitResult(contact, Direction.NORTH, struck, false));
    }

    private static ItemEntity drop(GameTestHelper helper, Vec3 location, ItemLike type, int count) {
        var level = helper.getLevel();
        var item = new ItemEntity(level, location.x, location.y, location.z, new ItemStack(type, count), 0, 0, 0);
        item.setNoGravity(true);
        level.addFreshEntity(item);
        return item;
    }

    private static void assertLoaded(GameTestHelper helper, ItemEntity item) {
        helper.assertTrue(helper.getLevel().getEntity(item.getUUID()) == item, "The material is tracked by the level");
    }

    private static int countDrops(GameTestHelper helper, BlockPos struck, ItemLike type) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(struck).inflate(2),
                item -> item.isAlive() && item.getItem().is(type.asItem())).stream()
                .mapToInt(item -> item.getItem().getCount()).sum();
    }
}
