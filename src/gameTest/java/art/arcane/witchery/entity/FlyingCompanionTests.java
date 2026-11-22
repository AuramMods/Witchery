package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class FlyingCompanionTests {
    private static final String TEMPLATE = "qa_flying_route";
    private static final InteractionHand HAND = InteractionHand.MAIN_HAND;

    @GameTest(template = TEMPLATE, timeoutTicks = 450)
    public static void taglockMonkeyFetchesItsSubjectAfterReload(GameTestHelper helper) {
        FlightRoute route = runway(helper);
        WingedMonkey monkey = companion(helper, route, WitcheryEntities.WINGED_MONKEY.get());
        Pig pig = stationaryPig(helper, route);
        var taglock = new ItemStack(WitcheryItems.getRequired("taglockkit").get());
        TaglockItem.bindSignature(taglock, pig);
        route.owner().setItemInHand(HAND, taglock);
        monkey.mobInteract(route.owner(), HAND);

        CompoundTag saved = monkey.saveWithoutId(new CompoundTag());
        helper.assertTrue(taglock.isEmpty() && saved.contains("WITCWaypoint") && monkey.isOrderedToSit(),
                "The seated monkey should save its assigned route and consume one Taglock");
        monkey.load(saved);
        monkey.mobInteract(route.owner(), HAND);
        helper.succeedWhen(() -> {
            helper.assertTrue(monkey.isOrderedToSit() && monkey.distanceToSqr(Vec3.atBottomCenterOf(route.origin())) < 2.25,
                    "The monkey should return to its launch point and sit");
            helper.assertTrue(!pig.isPassenger() && pig.distanceToSqr(Vec3.atBottomCenterOf(route.origin())) < 3,
                    "The same pig should be released at the launch point");
            helper.assertTrue(pig.getHealth() == pig.getMaxHealth(), "The pig should arrive unharmed");
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 320)
    public static void waystoneMonkeyCarriesItsOwner(GameTestHelper helper) {
        FlightRoute route = runway(helper);
        WingedMonkey monkey = companion(helper, route, WitcheryEntities.WINGED_MONKEY.get());
        ItemStack stone = locationStone(helper, route);
        route.owner().setItemInHand(HAND, stone);
        monkey.mobInteract(route.owner(), HAND);
        helper.assertTrue(route.owner().getVehicle() == monkey, "The owner should mount the monkey on using a Waystone");

        helper.succeedWhen(() -> {
            Vec3 destination = Vec3.atBottomCenterOf(route.destination());
            helper.assertTrue(monkey.isOrderedToSit() && monkey.distanceToSqr(destination) < 8,
                    "The monkey should land near the Waystone destination");
            helper.assertTrue(!route.owner().isPassenger() && route.owner().distanceToSqr(destination) < 8,
                    "The owner should arrive and dismount");
            helper.assertTrue(stone.getCount() == 1, "The owner should keep the Waystone");
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 450)
    public static void owlDeliversAndReturnsWithAnOlderWaystone(GameTestHelper helper) {
        FlightRoute route = runway(helper);
        WitchOwl owl = companion(helper, route, WitcheryEntities.OWL.get());
        route.owner().setItemInHand(HAND, new ItemStack(Items.PAPER).setHoverName(Component.literal("Waystone Letter")));
        owl.mobInteract(route.owner(), HAND);
        route.owner().setItemInHand(HAND, locationStone(helper, route));
        owl.mobInteract(route.owner(), HAND);

        CompoundTag saved = owl.saveWithoutId(new CompoundTag());
        CompoundTag waypoint = saved.getCompound("WITCWaypoint");
        waypoint.putString("id", "witchery:ingredient");
        waypoint.putShort("Damage", (short) 13);
        waypoint.getCompound("tag").remove("Dimension");
        owl.load(saved);
        launchOwl(route, owl);

        helper.succeedWhen(() -> {
            var letters = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(route.destination()).inflate(2),
                    drop -> drop.getItem().is(Items.PAPER) && drop.getItem().getHoverName().getString().equals("Waystone Letter"));
            helper.assertTrue(letters.size() == 1, "The owl should deliver the original named parcel once");
            helper.assertTrue(owl.getMainHandItem().isEmpty() && owl.isOrderedToSit()
                            && owl.distanceToSqr(Vec3.atBottomCenterOf(route.origin())) < 2.25,
                    "The owl should return to its launch point without the parcel");
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 320)
    public static void owlThrowsInkAtItsBoundCreature(GameTestHelper helper) {
        FlightRoute route = runway(helper);
        WitchOwl owl = companion(helper, route, WitcheryEntities.OWL.get());
        Pig pig = stationaryPig(helper, route);
        route.owner().setItemInHand(HAND, new ItemStack(WitcheryIngredient.BREW_OF_INK.item()));
        owl.mobInteract(route.owner(), HAND);
        var stone = new ItemStack(WitcheryIngredient.CREATURE_BOUND_WAYSTONE.item());
        TaglockItem.bindSignature(stone, pig);
        route.owner().setItemInHand(HAND, stone);
        owl.mobInteract(route.owner(), HAND);
        launchOwl(route, owl);

        helper.succeedWhen(() -> helper.assertTrue(pig.hasEffect(MobEffects.BLINDNESS) && owl.getMainHandItem().isEmpty(),
                "The owl should throw its Ink at the bound pig"));
    }

    private static FlightRoute runway(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(3, 2, 4)).atY(220);
        BlockPos destination = origin.east(12);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -1, -3), destination.offset(2, -1, 3))) {
            helper.getLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        var owner = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "FlightOwner"));
        owner.getAbilities().instabuild = false;
        owner.setPos(Vec3.atBottomCenterOf(origin.west()));
        return new FlightRoute(origin, destination, owner);
    }

    private static <T extends TamableAnimal> T companion(GameTestHelper helper, FlightRoute route, EntityType<T> type) {
        T companion = type.create(helper.getLevel());
        companion.setPos(Vec3.atBottomCenterOf(route.origin()));
        companion.tame(route.owner());
        companion.setOrderedToSit(true);
        helper.getLevel().addFreshEntity(companion);
        return companion;
    }

    private static Pig stationaryPig(GameTestHelper helper, FlightRoute route) {
        Pig pig = EntityType.PIG.create(helper.getLevel());
        pig.setNoAi(true);
        pig.setPos(Vec3.atBottomCenterOf(route.destination()));
        helper.getLevel().addFreshEntity(pig);
        return pig;
    }

    private static ItemStack locationStone(GameTestHelper helper, FlightRoute route) {
        var stone = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
        WaystoneItem.bindLocation(stone, helper.getLevel(), route.destination());
        return stone;
    }

    private static void launchOwl(FlightRoute route, WitchOwl owl) {
        route.owner().setItemInHand(HAND, ItemStack.EMPTY);
        owl.mobInteract(route.owner(), HAND);
    }

    private record FlightRoute(BlockPos origin, BlockPos destination, FakePlayer owner) {}
}
