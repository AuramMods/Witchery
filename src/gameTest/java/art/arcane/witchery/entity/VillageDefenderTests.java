package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class VillageDefenderTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_hunter_reflection_bolt")
    public static void firesSilverAtWolfmanReflections(GameTestHelper helper) {
        var hunter = helper.spawn(WitcheryEntities.WITCH_HUNTER.get(), new BlockPos(2, 3, 4));
        hunter.setItemSlot(EquipmentSlot.MAINHAND, WitcheryItems.getRequired("handbow").get().getDefaultInstance());
        hunter.setNoAi(true);
        var reflection = spawnReflection(helper, new BlockPos(7, 3, 4), true);
        var level = helper.getLevel();
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(level.getEntity(hunter.getUUID()) == hunter,
                        "Wait for the Witch Hunter to enter the level"))
                .thenExecute(() -> hunter.performRangedAttack(reflection, 1))
                .thenWaitUntil(() -> {
                    var bolts = level.getEntitiesOfClass(WitcheryBolt.class, hunter.getBoundingBox().inflate(3),
                            bolt -> bolt.getOwner() == hunter);
                    helper.assertTrue(bolts.stream().anyMatch(bolt -> bolt.kind() == WitcheryBolt.Kind.SILVER),
                            "A Witch Hunter should fire a Silver Bolt at a Wolfman Reflection");
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_hunter_reflection_target")
    public static void huntsWolfmanReflections(GameTestHelper helper) {
        var hunter = helper.spawn(WitcheryEntities.WITCH_HUNTER.get(), new BlockPos(2, 3, 4));
        spawnReflection(helper, new BlockPos(5, 3, 4), false);
        var wolfman = spawnReflection(helper, new BlockPos(7, 3, 4), true);
        helper.succeedWhen(() -> helper.assertTrue(hunter.getTarget() == wolfman,
                "The Witch Hunter should pursue the Wolfman Reflection instead of a normal Reflection"));
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_reflection_last_form")
    public static void keepsTheLastCopiedFormWithoutAnOwner(GameTestHelper helper) {
        var reflection = spawnReflection(helper, new BlockPos(7, 3, 4), true);
        var saved = reflection.saveWithoutId(new CompoundTag());
        saved.putBoolean("Vampire", true);
        saved.putString("Owner", "AbsentOwner");
        reflection.load(saved);
        reflection.setItemSlot(EquipmentSlot.HEAD, Items.LEATHER_HELMET.getDefaultInstance());
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(reflection.tickCount >= 31,
                        "Wait for the Reflection to update its owner and equipment"))
                .thenExecute(() -> {
                    var current = reflection.saveWithoutId(new CompoundTag());
                    helper.assertTrue(current.getString("Owner").isEmpty()
                                    && reflection.getItemBySlot(EquipmentSlot.HEAD).isEmpty(),
                            "An ownerless Reflection should clear the copied owner and gear");
                    helper.assertTrue(reflection.hasWolfmanModel() && reflection.isVampireCopy()
                                    && reflection.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == 6,
                            "An ownerless Reflection should keep the last form and its attack strength");
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_village_defender_loot")
    public static void usesOriginalVillageDefenderLootingRolls(GameTestHelper helper) {
        var level = helper.getLevel();
        var killer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "DefenderLoot"));
        var hunter = WitcheryEntities.WITCH_HUNTER.get().create(level);
        var guard = WitcheryEntities.VILLAGE_GUARD.get().create(level);
        for (int looting : new int[]{0, 3}) {
            var sword = Items.IRON_SWORD.getDefaultInstance();
            if (looting > 0) sword.enchant(Enchantments.MOB_LOOTING, looting);
            killer.setItemSlot(EquipmentSlot.MAINHAND, sword);
            checkLoot(helper, hunter, killer, looting, true, WitcheryIngredient.STAKE_BOLT.item(),
                    WitcheryIngredient.ANTI_MAGIC_BOLT.item(), 2);
            checkLoot(helper, guard, killer, looting, false, Items.ARROW, Items.LEATHER_CHESTPLATE, 1);
        }
        helper.succeed();
    }

    private static Reflection spawnReflection(GameTestHelper helper, BlockPos position, boolean wolfman) {
        var reflection = helper.spawn(WitcheryEntities.REFLECTION.get(), position);
        var saved = reflection.saveWithoutId(new CompoundTag());
        saved.putInt("Model", wolfman ? 1 : 0);
        saved.putBoolean("FreeSpawn", true);
        saved.putBoolean("NoAI", true);
        reflection.load(saved);
        return reflection;
    }

    private static void checkLoot(GameTestHelper helper, Mob creature, FakePlayer killer, int looting,
                                  boolean hunter, Item common, Item rare, int rareCount) {
        var level = helper.getLevel();
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, creature)
                .withParameter(LootContextParams.ORIGIN, creature.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(killer))
                .withParameter(LootContextParams.KILLER_ENTITY, killer)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, killer)
                .create(LootContextParamSets.ENTITY);
        var table = level.getServer().getLootData().getLootTable(creature.getLootTable());
        for (long seed = 1; seed <= 256; seed++) {
            var random = RandomSource.create(seed);
            int expectedCommon = hunter ? random.nextInt(3 + looting) : random.nextInt(3);
            if (!hunter && looting > 0) expectedCommon += random.nextInt(looting + 1);
            int expectedRare = random.nextInt(200) < 5 + looting ? rareCount : 0;
            var drops = table.getRandomItems(params, seed);
            helper.assertTrue(count(drops, common) == expectedCommon && count(drops, rare) == expectedRare,
                    creature.getType() + " should use the original ammunition and rare-drop rolls at Looting " + looting
                            + " for loot seed " + seed);
        }
    }

    private static int count(List<ItemStack> drops, Item item) {
        return drops.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }
}
