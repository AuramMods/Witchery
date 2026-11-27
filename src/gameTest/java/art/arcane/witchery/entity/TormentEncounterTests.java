package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class TormentEncounterTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_torment_transition")
    public static void rejectedHitStartsTormentForNearbyPlayersAndSavedAttackers(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 3, 4)));
        var corner = player(level, "TormentCorner", center.add(15.9, 31, 15.9), GameType.SURVIVAL);
        var edge = player(level, "TormentEdge", center.add(16.5, 0, 0), GameType.SURVIVAL);
        var builder = player(level, "TormentBuilder", center.add(-3, 0, 0), GameType.CREATIVE);
        var archer = player(level, "TormentArcher", center.add(40, 0, 0), GameType.SURVIVAL);
        var caster = player(level, "TormentCaster", center.add(45, 0, 0), GameType.SURVIVAL);
        var melee = player(level, "TormentMelee", center.add(50, 0, 0), GameType.SURVIVAL);
        var shot = EntityType.ARROW.create(level);
        shot.setPos(center);
        var boss = WitcheryEntities.LORD_OF_TORMENT.get().create(level);
        boss.setPos(center);
        try {
            helper.assertTrue(boss.hurt(level.damageSources().arrow(shot, archer), 20) && boss.getHealth() == 495,
                    "A regular ranged hit is capped at five");
            boss.invulnerableTime = 0;
            helper.assertTrue(boss.hurt(WitcheryDamageTypes.demonic(level, shot, caster), 20) && boss.getHealth() == 487,
                    "A demonic hit is capped at eight");
            boss.invulnerableTime = 0;
            helper.assertTrue(boss.hurt(level.damageSources().playerAttack(melee), 20) && boss.getHealth() == 482,
                    "A direct player hit is capped at five");

            var reopened = WitcheryEntities.LORD_OF_TORMENT.get().create(level);
            reopened.load(boss.saveWithoutId(new CompoundTag()));
            var magic = level.damageSources().magic();
            helper.assertTrue(reopened.hurt(magic, 40), "A prior hit starts the damage cooldown");
            reopened.setHealth(250);
            helper.assertTrue(!reopened.hurt(level.damageSources().explosion(shot, archer), 40) && !reopened.isRemoved(),
                    "An explosion cannot hurt the Lord or start the transition");
            helper.assertTrue(!reopened.hurt(magic, 5) && reopened.isRemoved(),
                    "A hit rejected by damage cooldown still starts the half-health transition");

            int floor = torment(corner).getInt("WITCForceTormentLevel");
            helper.assertTrue(floor >= 0 && floor < 6, "The transition chooses one of the six floors");
            for (var player : List.of(corner, builder, caster, melee)) {
                var data = torment(player);
                helper.assertTrue(data.getInt("WITCForceTorment") == 2 && data.getInt("WITCForceTormentLevel") == floor,
                        player.getGameProfile().getName() + " is sent to the shared floor");
            }
            helper.assertTrue(torment(edge).getInt("WITCForceTorment") == 0
                            && torment(archer).getInt("WITCForceTorment") == 0,
                    "A player just outside the box and a distant ordinary archer remain in the Overworld");
        } finally {
            for (var player : List.of(corner, edge, builder, archer, caster, melee)) {
                level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
            }
            shot.discard();
            boss.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_torment_reward")
    public static void tormentRewardContainsTwoCopiesOfOneEnchantedBook(GameTestHelper helper) {
        var level = helper.getLevel();
        var position = helper.absolutePos(new BlockPos(4, 3, 4));
        var boss = WitcheryEntities.LORD_OF_TORMENT.get().create(level);
        boss.setPos(Vec3.atBottomCenterOf(position));
        boss.dropCustomDeathLoot(level.damageSources().magic(), 0, true);
        var area = new AABB(position).inflate(2);
        helper.succeedWhen(() -> {
            var drops = level.getEntitiesOfClass(ItemEntity.class, area);
            var books = drops.stream().map(ItemEntity::getItem).filter(stack -> stack.is(Items.ENCHANTED_BOOK)).toList();
            helper.assertTrue(books.size() == 2 && ItemStack.isSameItemSameTags(books.get(0), books.get(1)),
                    "The Lord drops two identical enchanted books");
            helper.assertTrue(drops.stream().anyMatch(drop -> drop.getItem().is(WitcheryIngredient.DEMON_HEART.item()))
                            && drops.stream().anyMatch(drop -> drop.getItem().is(WitcheryIngredient.BREW_SOUL_TORMENT.item())),
                    "The Demon Heart and Brew of Soul Torment remain guaranteed");
            drops.forEach(Entity::discard);
            boss.discard();
        });
    }

    private static FakePlayer player(ServerLevel level, String name, Vec3 position, GameType mode) {
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), name));
        player.setPos(position);
        player.gameMode.changeGameModeForPlayer(mode);
        level.addNewPlayer(player);
        return player;
    }

    private static CompoundTag torment(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }
}
