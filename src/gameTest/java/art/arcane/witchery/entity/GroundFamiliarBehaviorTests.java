package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class GroundFamiliarBehaviorTests {
    @GameTest(template = "qa_coven_arena")
    public static void witchCatsFeedAndTameWithOriginalRawFishVariants(GameTestHelper helper) {
        var level = helper.getLevel();
        var cat = WitcheryEntities.WITCH_CAT.get().create(level);
        Vec3 start = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 2, 5)));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "WitchCatKeeper"));
        cat.setPos(start);
        player.setPos(start.add(3, 0, 0));
        level.addNewPlayer(player);
        try {
            var fish = new ItemStack(Items.PUFFERFISH, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, fish);
            cat.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(!cat.isTame() && cat.isInLove() && fish.getCount() == 1,
                    "The original exactly-three-block fish interaction feeds the cat without taming it");
            cat.resetLove();
            cat.getRandom().setSeed(0);
            player.setPos(start.add(2.99, 0, 0));
            cat.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(cat.isTame() && cat.isOwnedBy(player) && cat.isOrderedToSit() && fish.isEmpty(),
                    "The successful fish roll makes the nearby cat sit under its new owner");

            var stick = new ItemStack(Items.STICK, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, stick);
            cat.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(!cat.isOrderedToSit() && stick.getCount() == 2,
                    "An ordinary held item changes the sit command without consumption");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.NAME_TAG));
            helper.assertTrue(!cat.mobInteract(player, InteractionHand.MAIN_HAND).consumesAction() && !cat.isOrderedToSit(),
                    "Name tags pass through to their item interaction");
            cat.setFamiliar(true);
            cat.setHealth(35);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TROPICAL_FISH));
            cat.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(cat.getHealth() == 45 && player.getMainHandItem().isEmpty() && cat.canBreatheUnderwater(),
                    "Raw tropical fish heals ten health and the familiar retains its air supply");
        } finally {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void toadsReduceArrowDamageByItsAttackerAndKeepFamiliarOrders(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ToadKeeper"));
        var toad = WitcheryEntities.TOAD.get().create(level);
        var skeleton = EntityType.SKELETON.create(level);
        var arrow = new Arrow(level, 0, 0, 0);
        toad.tame(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK, 2));
        toad.setOrderedToSit(false);
        toad.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(toad.isOrderedToSit() && player.getMainHandItem().getCount() == 2,
                "The owner can seat a toad with an ordinary held item");
        toad.hurt(level.damageSources().arrow(arrow, skeleton), 6);
        helper.assertTrue(toad.getHealth() == 6.5F && !toad.isOrderedToSit(), "A skeleton arrow's six damage becomes three and a half");
        toad.setHealth(10);
        toad.invulnerableTime = 0;
        toad.hurt(level.damageSources().arrow(arrow, player), 6);
        helper.assertTrue(toad.getHealth() == 4, "A player arrow keeps all six damage");
        toad.setHealth(10);
        toad.invulnerableTime = 0;
        toad.hurt(level.damageSources().arrow(arrow, null), 6);
        helper.assertTrue(toad.getHealth() == 4 && toad.getAmbientSoundInterval() == 240,
                "An unowned arrow keeps its damage and a toad uses the original croak interval");
        toad.setFamiliar(true);
        toad.setOrderedToSit(true);
        toad.invulnerableTime = 0;
        toad.hurt(level.damageSources().playerAttack(player), 6);
        helper.assertTrue(toad.isOrderedToSit() && toad.getArmorValue() == 5,
                "A hit preserves the bound toad's seating order and innate armor");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void groundFamiliarsWalkBeforeUsingTheFirstOpenLandingCell(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos block = helper.absolutePos(new BlockPos(8, 2, 8)).atY(189);
        Vec3 start = Vec3.atBottomCenterOf(block);
        for (BlockPos floor : BlockPos.betweenClosed(block.offset(-2, -1, -3), block.offset(15, -1, 3))) {
            level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        }
        for (var type : List.of(WitcheryEntities.WITCH_CAT.get(), WitcheryEntities.TOAD.get())) {
            TamableAnimal pet = type.create(level);
            var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "GroundFollow"));
            pet.tame(owner);
            pet.setPos(start);
            owner.setPos(start.add(9.99, 0, 0));
            level.addNewPlayer(owner);
            try {
                var follow = new GroundFamiliarGoals.Follow(pet, pet instanceof WitchToad ? 2 : 5);
                helper.assertTrue(!follow.canUse(), "Following starts at ten blocks");
                owner.setPos(start.add(10, 0, 0));
                helper.assertTrue(follow.canUse(), "Ten blocks begins owner following");
                owner.setPos(start.add(12, 0, 0));
                pet.setOnGround(true);
                follow.start();
                follow.tick();
                helper.assertTrue(pet.position().equals(start) && !pet.getNavigation().isDone(),
                        "A reachable owner twelve blocks away is followed on foot");
                follow.stop();
                pet.setOnGround(false);
                owner.setPos(start.add(11.99, 0, 0));
                helper.assertTrue(follow.canUse(), "The owner is within follow range");
                follow.start();
                follow.tick();
                helper.assertTrue(pet.position().equals(start), "Failed paths below twelve blocks leave the pet in place");
                follow.stop();
                owner.setPos(start.add(12, 0, 0));
                var blocked = owner.blockPosition().offset(-2, 0, -2);
                level.setBlockAndUpdate(blocked, Blocks.STONE.defaultBlockState());
                helper.assertTrue(follow.canUse(), "The owner is eligible for following");
                follow.start();
                follow.tick();
                helper.assertTrue(pet.position().equals(Vec3.atBottomCenterOf(blocked.south())),
                        "A failed path at twelve blocks uses the first clear outer-ring cell");
                level.removeBlock(blocked, false);
            } finally {
                level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
            }
        }
        helper.succeed();
    }
}
