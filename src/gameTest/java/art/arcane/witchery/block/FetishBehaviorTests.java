package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.FetishBlockEntity;
import art.arcane.witchery.block.entity.FetishEffect;
import art.arcane.witchery.block.entity.RiteHeartBlockEntity;
import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.ritual.RitualCircle;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class FetishBehaviorTests {
    private static final String TEMPLATE = "qa_rite_terrain";
    private static final BlockPos CENTER = new BlockPos(19, 220, 19);

    @GameTest(template = TEMPLATE, batch = "witchery_screamer_alarm", setupTicks = 5, timeoutTicks = 160)
    public static void screamerUsesLegacySpeciesAndAlarmControls(GameTestHelper helper) {
        floor(helper);
        var level = helper.getLevel();
        helper.setBlock(CENTER, WitcheryBlocks.getRequired("scarecrow").get());
        helper.setBlock(CENTER.east(), Blocks.REDSTONE_LAMP);
        var fetish = (FetishBlockEntity) helper.getBlockEntity(CENTER);
        var data = alarm(FetishEffect.SCREAMER);
        var pigGroup = new CompoundTag();
        pigGroup.putString("CreatureTypeName", "Pig");
        var creatures = new ListTag();
        creatures.add(pigGroup);
        data.put("KnownCreatureTypes", creatures);
        fetish.load(data);
        var pig = placeMob(helper, EntityType.PIG, CENTER.south(3));
        var player = actor(helper, "FetishTester");

        helper.startSequence().thenIdle(24).thenExecute(() -> {
            helper.assertTrue(level.getEntity(pig.getUUID()) == pig, "The alarm can query the trusted pig");
            helper.assertFalse(lampLit(helper), "The legacy Pig group keeps the lamp off");
        }).thenExecute(() -> placeMob(helper, EntityType.SHEEP, CENTER.north(3)))
                .thenWaitUntil(() -> helper.assertTrue(lampLit(helper), "The unlisted sheep lights the lamp"))
                .thenExecute(() -> {
                    var sheep = level.getEntitiesOfClass(Sheep.class,
                            new AABB(helper.absolutePos(CENTER.north(3))).inflate(1));
                    helper.assertTrue(sheep.size() == 1, "The unlisted sheep stays within the alarm");
                    var sample = new ItemStack(WitcheryItems.getRequired("taglockkit").get());
                    TaglockItem.bindSignature(sample, sheep.get(0));
                    use(helper, player, sample);
                    helper.assertTrue(sample.isEmpty(), "A filled Taglock Kit adds the sheep group");
                    var saved = fetish.saveWithoutMetadata();
                    helper.assertTrue(saved.getList("KnownCreatureTypes", Tag.TAG_COMPOUND).size() == 2,
                            "Both groups are present in the saved alarm");
                    fetish.load(saved);
                }).thenWaitUntil(() -> helper.assertFalse(lampLit(helper), "The saved trusted groups leave the lamp off"))
                .thenExecute(() -> {
                    level.getEntitiesOfClass(Sheep.class, new AABB(helper.absolutePos(CENTER.north(3))).inflate(1))
                            .forEach(Sheep::discard);
                    use(helper, player, new ItemStack(Items.WATER_BUCKET));
                    var washed = fetish.saveWithoutMetadata();
                    helper.assertTrue(player.getMainHandItem().is(Items.BUCKET)
                                    && washed.getList("KnownCreatureTypes", Tag.TAG_COMPOUND).isEmpty()
                                    && washed.getInt("AlarmMode") == 2,
                            "The wash clears the groups, preserves the alarm mode, and returns the empty bucket");
                }).thenWaitUntil(() -> helper.assertTrue(pig.isAlive() && lampLit(helper),
                        "The original pig lights the lamp after the list is cleared"))
                .thenExecute(pig::discard).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_bind_witchs_ladder", setupTicks = 5, timeoutTicks = 240)
    public static void witchsLadderBindsBansheesThroughTheMediumRite(GameTestHelper helper) {
        floor(helper);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        helper.setBlock(CENTER, WitcheryBlocks.getRequired("circle").get());
        var glyph = WitcheryBlocks.getRequired("circleglyphritual").get();
        for (BlockPos offset : RitualCircle.MEDIUM_RITE.offsets()) helper.setBlock(CENTER.offset(offset), glyph);
        var altar = chargedAltar(helper);
        var needed = List.of(
                placeMob(helper, WitcheryEntities.SPIRIT.get(), CENTER.offset(-2, 0, 2)),
                placeMob(helper, WitcheryEntities.SPIRIT.get(), CENTER.south(2)),
                placeMob(helper, WitcheryEntities.SPIRIT.get(), CENTER.offset(2, 0, 2)),
                placeMob(helper, WitcheryEntities.BANSHEE.get(), CENTER.offset(-1, 0, -2)),
                placeMob(helper, WitcheryEntities.BANSHEE.get(), CENTER.offset(1, 0, -2)));
        var unrelated = placeMob(helper, WitcheryEntities.POLTERGEIST.get(), CENTER.east(3));
        var ladder = WitcheryBlocks.getRequired("witchsladder").get().asItem();
        var offering = new ItemStack(ladder);
        offering.getOrCreateTag().putByte("BlockColor", (byte) 14);
        var stone = new ItemStack(WitcheryIngredient.NECROMANTIC_STONE.item());
        stone.setHoverName(Component.literal("Circle keeper"));
        var boline = new ItemStack(WitcheryItems.getRequired("boline").get());
        boline.setDamageValue(9);
        offer(helper, offering, new ItemStack(WitcheryIngredient.ATTUNED_STONE.item()), stone, boline);

        // The structure can be ready before the server starts tracking entities in its chunks.
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                needed.stream().allMatch(mob -> level.getEntity(mob.getUUID()) == mob)
                        && level.getEntity(unrelated.getUUID()) == unrelated,
                "All spirits are tracked before activating the rite")).thenExecute(() -> {
            helper.useBlock(CENTER);
            helper.assertTrue(helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE), "The medium circle starts the binding rite");
            var heart = (RiteHeartBlockEntity) helper.getBlockEntity(CENTER);
            helper.runAfterDelay(60, () -> heart.load(heart.saveWithoutMetadata()));
            helper.runAfterDelay(90, () -> {
                float beforeCharge = altar.power();
                int recharge = 10 * altar.rechargeMultiplier();
                helper.runAfterDelay(20, () -> {
                    var items = level.getEntitiesOfClass(ItemEntity.class, new AABB(center).inflate(6));
                    var bound = items.stream().map(ItemEntity::getItem).filter(item -> item.is(ladder)).findFirst().orElseThrow();
                    helper.assertTrue(FetishBlockEntity.itemEffect(bound) == FetishEffect.SCREAMER.id
                                    && FetishBlockEntity.itemColor(bound) == 14,
                            "The saved binding rite returns the dyed Witch's Ladder as a Screamer");
                    helper.assertTrue(altar.power() == beforeCharge - 6000 + recharge, "The completed rite debits 6,000 power once");
                    helper.assertTrue(needed.stream().allMatch(Mob::isRemoved) && unrelated.isAlive(),
                            "The rite consumes the three Dream Spirits and two Banshees and leaves the Poltergeist");
                    helper.assertTrue(items.size() == 3 && items.stream().anyMatch(item -> ItemStack.matches(item.getItem(), stone))
                                    && items.stream().anyMatch(item -> ItemStack.matches(item.getItem(), boline)),
                            "The rite also returns the original named Necromantic Stone and worn Boline");
                    unrelated.discard();
                    items.forEach(ItemEntity::discard);
                    helper.succeed();
                });
            });
        });
    }

    @GameTest(template = TEMPLATE, batch = "witchery_sentinel_target", setupTicks = 5, timeoutTicks = 120)
    public static void sentinelGuardsKeepTheirIntruderAndRespectTheTrustedPlayer(GameTestHelper helper) {
        floor(helper);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        helper.setBlock(CENTER, WitcheryBlocks.getRequired("trent").get());
        var fetish = (FetishBlockEntity) helper.getBlockEntity(CENTER);
        var owner = actor(helper, "SentinelKeeper");
        owner.setPos(Vec3.atBottomCenterOf(helper.absolutePos(CENTER.south(5))));
        level.addNewPlayer(owner);
        var data = alarm(FetishEffect.SENTINEL);
        var trusted = new CompoundTag();
        trusted.putString("PlayerName", owner.getGameProfile().getName());
        var players = new ListTag();
        players.add(trusted);
        data.put("KnownPlayers", players);
        fetish.load(data);
        var pig = placeMob(helper, EntityType.PIG, CENTER.south(3));
        // Keep the intruder alive without making it ineligible for the target goal.
        pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        pig.setHealth(pig.getMaxHealth());
        var guardArea = new AABB(center).inflate(8);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(fetish.saveWithoutMetadata().getLong("LastActivation") > 0,
                "The loaded fetish detects the intruding pig on its scheduled tick")).thenExecute(() -> {
            var guards = level.getEntitiesOfClass(SpectralUndead.class, guardArea);
            try {
                helper.assertTrue(guards.size() == 2 && guards.stream().allMatch(guard -> guard.temporary() && guard.getTarget() == pig),
                        "The Sentinel summons exactly two temporary guards targeting the pig");
                // The dedicated test server may load chunks before ticking their entities.
                for (int tick = 0; tick < 12; tick++) {
                    guards.forEach(level::tickNonPassenger);
                    helper.assertTrue(guards.stream().allMatch(guard -> guard.isAlive() && guard.getTarget() == pig),
                            "Both guards stay on the pig while the trusted player is nearby");
                }
                for (var guard : guards) {
                    var saved = guard.saveWithoutId(new CompoundTag());
                    helper.assertTrue(saved.getInt("SuicideIn") > 550 && saved.getUUID("SentinelTarget").equals(pig.getUUID()),
                            "The guard saves its remaining lifetime and actual intruder");
                    guard.setTarget(null);
                    guard.load(saved);
                }
                for (int tick = 0; tick < 12; tick++) {
                    guards.forEach(level::tickNonPassenger);
                    helper.assertTrue(guards.stream().allMatch(guard -> guard.isAlive() && guard.getTarget() == pig),
                            "Reloaded guards restore the intruder and continue to ignore the trusted player");
                }
            } finally {
                owner.discard();
                pig.discard();
            }
            guards.forEach(level::tickNonPassenger);
            helper.assertTrue(guards.stream().allMatch(SpectralUndead::isRemoved), "The guards vanish with their target");
        }).thenSucceed();
    }

    private static CompoundTag alarm(FetishEffect effect) {
        var data = new CompoundTag();
        data.putInt("EffectTypeID", effect.id);
        data.putInt("AlarmMode", 2);
        return data;
    }

    private static boolean lampLit(GameTestHelper helper) {
        return helper.getBlockState(CENTER.east()).getValue(RedstoneLampBlock.LIT);
    }

    private static void use(GameTestHelper helper, FakePlayer player, ItemStack stack) {
        var pos = helper.absolutePos(CENTER);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.getBlockState(CENTER).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static FakePlayer actor(GameTestHelper helper, String name) {
        return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
    }

    private static <T extends Mob> T placeMob(GameTestHelper helper, EntityType<T> type, BlockPos relative) {
        var level = helper.getLevel();
        var mob = type.create(level);
        mob.setPos(Vec3.atBottomCenterOf(helper.absolutePos(relative)));
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        helper.assertTrue(level.addFreshEntity(mob), "The alarm fixture can spawn " + type.toShortString());
        return mob;
    }

    private static void floor(GameTestHelper helper) {
        for (var pos : BlockPos.betweenClosed(CENTER.offset(-6, -1, -6), CENTER.offset(10, -1, 6))) {
            helper.setBlock(pos, Blocks.STONE);
        }
    }

    private static void offer(GameTestHelper helper, ItemStack... stacks) {
        var center = Vec3.atBottomCenterOf(helper.absolutePos(CENTER));
        var level = helper.getLevel();
        for (var stack : stacks) {
            var item = new ItemEntity(level, center.x, center.y + 0.2, center.z, stack.copy(), 0, 0, 0);
            item.setNoGravity(true);
            item.setNeverPickUp();
            level.addFreshEntity(item);
        }
    }

    private static AltarBlockEntity chargedAltar(GameTestHelper helper) {
        var corner = CENTER.offset(7, 0, -2);
        var block = WitcheryBlocks.getRequired("altar").get();
        for (var pos : BlockPos.betweenClosed(corner, corner.offset(1, 0, 2))) helper.setBlock(pos, block);
        helper.setBlock(corner.above(), WitcheryBlocks.getRequired("infinityegg").get());
        var altar = (AltarBlockEntity) helper.getBlockEntity(corner);
        var saved = altar.saveWithoutMetadata();
        saved.putInt("MaxPower", 1000);
        saved.putInt("PowerScale", 10);
        saved.putFloat("Power", 8500);
        altar.load(saved);
        return altar;
    }
}
