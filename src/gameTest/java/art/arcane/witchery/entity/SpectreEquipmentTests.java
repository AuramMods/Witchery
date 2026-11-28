package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SpectreEquipmentTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_spectre_equipment")
    public static void spectreSpawnMatchesOriginalSeededArmorAndEnchantments(GameTestHelper helper) {
        var level = helper.getLevel();
        var position = helper.absolutePos(new BlockPos(4, 12, 4));
        var chunk = level.getChunkAt(position);
        var worldData = level.getServer().getWorldData();
        long savedTime = level.getDayTime();
        long savedInhabitedTime = chunk.getInhabitedTime();
        Difficulty savedDifficulty = worldData.getDifficulty();
        var spirit = WitcheryEntities.SPECTRE.get().create(level);
        var reopened = WitcheryEntities.SPECTRE.get().create(level);
        try {
            worldData.setDifficulty(Difficulty.NORMAL);
            level.setDayTime(0);
            chunk.setInhabitedTime(3_600_000);
            spirit.setPos(Vec3.atBottomCenterOf(position));
            spirit.getRandom().setSeed(-2955887895788881765L);
            spirit.finalizeSpawn(level, level.getCurrentDifficultyAt(position), MobSpawnType.SPAWN_EGG, null, null);
            ItemStack head = spirit.getItemBySlot(EquipmentSlot.HEAD);
            ItemStack chest = spirit.getItemBySlot(EquipmentSlot.CHEST);
            helper.assertTrue(head.is(Items.GOLDEN_HELMET) && head.getAllEnchantments().equals(Map.of(Enchantments.AQUA_AFFINITY, 1))
                            && chest.is(Items.GOLDEN_CHESTPLATE) && chest.getAllEnchantments().equals(Map.of(
                            Enchantments.ALL_DAMAGE_PROTECTION, 3, Enchantments.UNBREAKING, 3)),
                    "The native seed matches both armor materials and their exact enchantments");
            helper.assertTrue(spirit.obscured() && spirit.getItemBySlot(EquipmentSlot.LEGS).isEmpty()
                            && spirit.getItemBySlot(EquipmentSlot.FEET).isEmpty()
                            && Math.abs(spirit.getAttributeValue(Attributes.FOLLOW_RANGE) - 37.150801704) < 1e-7,
                    "The spawn rolls head and chest only after the original follow-range bonus");

            level.setDayTime(96_000);
            chunk.setInhabitedTime(0);
            reopened.load(spirit.saveWithoutId(new CompoundTag()));
            helper.assertTrue(ItemStack.matches(head, reopened.getItemBySlot(EquipmentSlot.HEAD))
                            && ItemStack.matches(chest, reopened.getItemBySlot(EquipmentSlot.CHEST))
                            && reopened.getAttributeValue(Attributes.FOLLOW_RANGE) == spirit.getAttributeValue(Attributes.FOLLOW_RANGE),
                    "Reopening the Spectre keeps its equipment and bonus under different spawn conditions");
        } finally {
            worldData.setDifficulty(savedDifficulty);
            level.setDayTime(savedTime);
            chunk.setInhabitedTime(savedInhabitedTime);
            spirit.discard();
            reopened.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_spectre_spawn_paths")
    public static void spectralSummonsInitializePermanentAndTemporarySpectres(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(4, 0, 4)).atY(220);
        for (var floor : BlockPos.betweenClosed(center.offset(-3, -1, -3), center.offset(3, -1, 3))) {
            level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(floor.above(), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(floor.above(2), Blocks.AIR.defaultBlockState());
        }
        var brazier = SpectralUndead.summonNear(level, center, SpectralUndead.Kind.SPECTRE, SpectralUndead.Summoning.BRAZIER);
        var stone = SpectralUndead.summonNear(level, center, SpectralUndead.Kind.SPECTRE, SpectralUndead.Summoning.STONE);
        helper.assertTrue(brazier != null, "The Brazier Spectre reaches the level");
        helper.assertTrue(stone != null, "The Spectral Stone Spectre reaches the level");
        var intruder = EntityType.PIG.create(level);
        intruder.setPos(Vec3.atBottomCenterOf(center));
        intruder.setNoAi(true);
        level.addFreshEntity(intruder);
        helper.startSequence().thenWaitUntil(() -> {
            helper.assertTrue(level.getEntity(intruder.getUUID()) == intruder, "The intruder is tracked");
        }).thenExecute(() -> SpectralUndead.summonSentinel(level, intruder)).thenWaitUntil(() -> {
            var sentinels = level.getEntitiesOfClass(SpectralUndead.class, new AABB(center).inflate(4), SpectralUndead::temporary);
            helper.assertTrue(sentinels.size() == 1, "The Sentinel Spectre reaches the level");
            for (var spirit : List.of(brazier, stone, sentinels.get(0))) {
                var bonuses = spirit.getAttribute(Attributes.FOLLOW_RANGE).getModifiers(AttributeModifier.Operation.MULTIPLY_BASE);
                helper.assertTrue(spirit.obscured() && bonuses.stream().anyMatch(bonus -> bonus.getName().equals("Random spawn bonus")),
                        "Each summon initializes the Spectre's original spawn bonus and appearance");
                spirit.discard();
            }
            intruder.discard();
        }).thenSucceed();
    }
}
