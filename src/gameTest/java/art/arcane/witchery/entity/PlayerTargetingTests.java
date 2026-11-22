package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PlayerTargetingTests {
    private static final BlockPos CENTER = new BlockPos(7, 4, 7);

    @GameTest(template = "qa_coven_arena")
    public static void rankTenVampiresDeterZombiesAndRobesRetainTheirOwnProtection(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "VampireTarget"));
        var data = WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new);
        var zombie = helper.spawn(EntityType.ZOMBIE, CENTER);
        var skeleton = helper.spawn(EntityType.SKELETON, CENTER.east());
        var creeper = helper.spawn(EntityType.CREEPER, CENTER.west());
        data.setVampireLevel(9);
        zombie.setTarget(player);
        helper.assertTrue(zombie.getTarget() == player, "Rank nine does not deter zombies");
        data.setVampireLevel(10);
        zombie.setTarget(player);
        skeleton.setTarget(player);
        creeper.setTarget(player);
        helper.assertTrue(zombie.getTarget() == null && skeleton.getTarget() == player && creeper.getTarget() == player,
                "Rank ten deters zombies and leaves other mobs free to target the player");

        data.setVampireLevel(0);
        player.setItemSlot(EquipmentSlot.CHEST, WitcheryItems.getRequired("necromancerrobe").get().getDefaultInstance());
        skeleton.setTarget(player);
        creeper.setTarget(player);
        helper.assertTrue(skeleton.getTarget() == null && creeper.getTarget() == player, "The necromancer's robe deters undead");
        player.setItemSlot(EquipmentSlot.CHEST, WitcheryItems.getRequired("witchrobe").get().getDefaultInstance());
        skeleton.setTarget(player);
        creeper.setTarget(player);
        helper.assertTrue(skeleton.getTarget() == player && creeper.getTarget() == null, "The witch's robe deters creepers");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_invisible_target")
    public static void invisibleTargetsUseTheNearestPlayerWithinSixteenBlocks(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = Vec3.atBottomCenterOf(helper.absolutePos(CENTER));
        var zombie = helper.spawn(EntityType.ZOMBIE, CENTER);
        var target = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "InvisibleTarget"));
        var neighbor = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "NearbyTarget"));
        target.setPos(origin.add(3, 0, 0));
        neighbor.setPos(origin.add(1, 0, 0));
        level.addNewPlayer(target);
        level.addNewPlayer(neighbor);
        try {
            zombie.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
            zombie.setTarget(target);
            helper.assertTrue(zombie.getTarget() == null, "A blinded mob drops a visible player target");

            target.setInvisible(true);
            target.setItemSlot(EquipmentSlot.HEAD, WitcheryItems.getRequired("witchhat").get().getDefaultInstance());
            target.setItemSlot(EquipmentSlot.CHEST, WitcheryItems.getRequired("necromancerrobe").get().getDefaultInstance());
            zombie.setTarget(target);
            helper.assertTrue(zombie.getTarget() == null, "A closer player prevents targeting the invisible player");

            neighbor.getAbilities().invulnerable = true;
            zombie.setTarget(target);
            helper.assertTrue(zombie.getTarget() == target, "The closer invulnerable player does not qualify as a target");
            neighbor.getAbilities().invulnerable = false;
            neighbor.setPos(origin.add(10, 0, 0));
            zombie.setTarget(target);
            helper.assertTrue(zombie.getTarget() == target, "The original nearest-player rule takes priority for invisible targets");

            target.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            target.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            zombie.setTarget(target);
            helper.assertTrue(zombie.getTarget() == null, "Removing armor shortens the invisible player's detection radius");
            target.setPos(origin.add(1, 0, 0));
            zombie.setTarget(target);
            helper.assertTrue(zombie.getTarget() == target, "An unarmored invisible player can be seen at close range");
            target.setShiftKeyDown(true);
            zombie.setTarget(target);
            helper.assertTrue(zombie.getTarget() == null, "Crouching reduces the detection radius further");
            target.setShiftKeyDown(false);

            target.setPos(origin.add(17, 0, 0));
            neighbor.setPos(origin.add(18, 0, 0));
            zombie.setTarget(target);
            helper.assertTrue(zombie.getTarget() == null, "An invisible player outside sixteen blocks cannot be targeted");
        } finally {
            level.removePlayerImmediately(neighbor, Entity.RemovalReason.DISCARDED);
            level.removePlayerImmediately(target, Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }
}
