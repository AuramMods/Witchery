package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SpiritFlightTests {
    private static final String TEMPLATE = "qa_flying_route";

    @GameTest(template = TEMPLATE)
    public static void subduedSpiritReleasesInTheClickedColumnAndApproachesFocusedWill(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos floor = helper.absolutePos(new BlockPos(8, 1, 8)).atY(188);
        level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        level.removeBlock(floor.above(), false);
        level.removeBlock(floor.above(2), false);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SpiritLure"));
        Vec3 origin = Vec3.atBottomCenterOf(floor.above()).add(0, 0.05, 0);
        player.setPos(origin.add(7, 0, 0));
        level.addNewPlayer(player);
        ItemStack spiritStack = new ItemStack(WitcheryIngredient.SUBDUED_SPIRIT.item());
        player.setItemInHand(InteractionHand.MAIN_HAND, spiritStack);
        var hit = new BlockHitResult(Vec3.atCenterOf(floor).add(-0.5, 0, 0), Direction.WEST, floor, false);
        helper.assertTrue(spiritStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction()
                        && spiritStack.isEmpty(),
                "Release spends one Spirit and uses the clicked column even when targeting the side");
        var spirit = level.getEntitiesOfClass(DreamSpirit.class, new AABB(origin, origin).inflate(1)).get(0);
        helper.assertTrue(spirit.position().distanceTo(origin) < 1e-5 && spirit.canBreatheUnderwater(),
                "The Spirit appears just above the clicked block and keeps its air underwater");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(WitcheryIngredient.FOCUSED_WILL.item()));
        helper.runAfterDelay(10, () -> {
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
            helper.assertTrue(spirit.getX() > origin.x + 0.5 && spirit.getY() > origin.y + 0.1,
                    "The registered lure sends the Spirit toward a still survival player");
            spirit.discard();
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void importedGuideClimbsTowardItsSavedWaypoint(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(8, 1, 8)).atY(193);
        var spirit = WitcheryEntities.SPIRIT.get().create(level);
        spirit.setPos(Vec3.atLowerCornerOf(origin));
        CompoundTag tag = spirit.saveWithoutId(new CompoundTag());
        tag.putInt("SuicideIn", 200);
        tag.putInt("SpiritType", 2);
        tag.put("WITCWaypoint", new ItemStack(WitcheryIngredient.WAYSTONE.item()).save(new CompoundTag()));
        tag.putDouble("HomeX", origin.getX() + 40);
        tag.putDouble("HomeY", origin.getY());
        tag.putDouble("HomeZ", origin.getZ());
        spirit.load(tag);
        level.addFreshEntity(spirit);
        helper.runAfterDelay(10, () -> {
            CompoundTag saved = spirit.saveWithoutId(new CompoundTag());
            helper.assertTrue(BlockPos.of(saved.getLong("GuideDestination")).equals(origin.offset(40, 0, 0))
                            && spirit.getX() > origin.getX() + 0.8 && spirit.getY() > origin.getY() + 0.4,
                    "An in-flight guide follows the imported waypoint and climbs with the original impulse");
            spirit.discard();
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void lostSoulAcceptsTheOriginalDamageTypesAndDropsASpiritAfterReload(GameTestHelper helper) {
        var level = helper.getLevel();
        Vec3 origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(8, 1, 8)).atY(188));
        var soul = WitcheryEntities.LOST_SOUL.get().create(level);
        soul.setPos(origin);
        var damage = level.damageSources();
        soul.configure(1, -1);
        helper.assertTrue(!soul.hurt(damage.cactus(), 6) && !soul.hurt(damage.wither(), 6)
                        && soul.hurt(damage.starve(), 6),
                "The green Soul rejects cactus and wither but accepts the original starvation source");
        soul.configure(2, -1);
        soul.invulnerableTime = 0;
        soul.setHealth(soul.getMaxHealth());
        helper.assertTrue(!soul.hurt(damage.generic(), 20) && soul.hurt(damage.magic(), 20) && soul.getHealth() == 5,
                "The blue Soul rejects ordinary damage and limits a successful magic hit to fifteen");

        var untouched = WitcheryEntities.LOST_SOUL.get().create(level);
        untouched.setPos(origin);
        untouched.configure(2, 1);
        var reloaded = WitcheryEntities.LOST_SOUL.get().create(level);
        reloaded.load(untouched.saveWithoutId(new CompoundTag()));
        untouched.aiStep();
        helper.assertTrue(untouched.isRemoved() && drops(helper, origin) == 0 && reloaded.color() == 0x0000FF,
                "A new Soul expires silently and its serialized color survives loading");
        reloaded.aiStep();
        helper.assertTrue(reloaded.isRemoved() && drops(helper, origin) == 1,
                "Reloading a timed Soul restores the inherited expiry reward");

        soul.hurt(damage.genericKill(), Float.MAX_VALUE);
        helper.assertTrue(drops(helper, origin) == 2, "Killing a Soul drops a Subdued Spirit");
        helper.succeed();
    }

    private static int drops(GameTestHelper helper, Vec3 center) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(center, center).inflate(2),
                item -> item.getItem().is(WitcheryIngredient.SUBDUED_SPIRIT.item())).size();
    }
}
