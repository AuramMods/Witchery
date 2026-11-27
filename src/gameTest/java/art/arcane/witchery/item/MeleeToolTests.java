package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class MeleeToolTests {
    @GameTest(template = "qa_coven_arena")
    public static void restoresOriginalMeleeModifiersAndGuards(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "MeleeGuard"));
        var attacker = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(5, 2, 7));
        var source = level.damageSources().mobAttack(attacker);
        var ids = List.of("arthana", "boline", "silversword", "huntsmanspear", "canesword", "kobolditepickaxe");
        double[] damage = {4, 4, 4, 8, 7, 5};
        int[] durability = {250, 250, 32, 1561, 1561, 1561};
        for (int index = 0; index < ids.size(); index++) {
            String id = ids.get(index);
            var stack = new ItemStack(WitcheryItems.getRequired(id).get());
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            if (id.equals("canesword")) {
                player.gameMode.useItem(player, level, stack, InteractionHand.MAIN_HAND);
                helper.assertFalse(player.isUsingItem(), "A sheathed Cane Sword does not guard");
                stack.getOrCreateTag().putBoolean("WITCBladeDeployed", true);
            }
            double modifier = stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)
                    .stream().mapToDouble(attribute -> attribute.getAmount()).sum();
            helper.assertTrue(modifier == damage[index] && stack.getMaxDamage() == durability[index],
                    id + " matches the original attack modifier and durability");
            player.gameMode.useItem(player, level, stack, InteractionHand.MAIN_HAND);
            boolean guards = !id.equals("kobolditepickaxe");
            helper.assertTrue(player.isUsingItem() == guards && (!guards || stack.getUseAnimation() == UseAnim.BLOCK),
                    id + " follows the original use binding");
            player.getRandom().setSeed(1);
            var hit = new LivingHurtEvent(player, source, 10);
            var bypass = new LivingHurtEvent(player, level.damageSources().fellOutOfWorld(), 10);
            MinecraftForge.EVENT_BUS.post(hit);
            MinecraftForge.EVENT_BUS.post(bypass);
            helper.assertTrue(hit.getAmount() == (guards ? 5.5F : 10) && bypass.getAmount() == 10
                    && stack.getDamageValue() == 0, id + " uses the original guard formula without spending durability");
            player.stopUsingItem();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void cutsPlantsThroughSurvivalMining(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BolineHarvest"));
        var boline = new ItemStack(WitcheryItems.getRequired("boline").get());
        player.setItemInHand(InteractionHand.MAIN_HAND, boline);
        var pos = helper.absolutePos(new BlockPos(5, 2, 4));
        level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        var mine = WitcheryBlocks.getRequired("plantmine").get().defaultBlockState()
                .setValue(PlantMineBlock.VARIANT, PlantMineBlock.Variant.DANDELION_SPROUTING);
        var cases = List.of(Blocks.COBWEB.defaultBlockState(), WitcheryBlocks.getRequired("bloodrose").get().defaultBlockState(),
                mine, Blocks.OAK_LEAVES.defaultBlockState());
        int[] wear = {0, 0, 2, 3};
        float[] speed = {15, 1.5F, 5, 15};
        var area = new AABB(pos).inflate(1);
        for (int index = 0; index < cases.size(); index++) {
            BlockState state = cases.get(index);
            var expected = state == mine ? mine.getBlock().getCloneItemStack(level, pos, mine) : new ItemStack(state.getBlock());
            helper.assertTrue(boline.getDestroySpeed(state) == speed[index], "The Boline keeps the original mining speed");
            level.setBlockAndUpdate(pos, state);
            player.gameMode.destroyBlock(pos);
            var drops = level.getEntitiesOfClass(ItemEntity.class, area);
            helper.assertTrue(level.isEmptyBlock(pos) && drops.size() == 1 && ItemStack.matches(expected, drops.get(0).getItem())
                            && boline.getDamageValue() == wear[index],
                    "Mining " + state + " returns its exact cutting and spends the original durability");
            drops.forEach(ItemEntity::discard);
        }
        level.setBlockAndUpdate(pos, mine);
        Consumer<BlockEvent.BreakEvent> cancel = event -> {
            if (event.getPlayer() == player && event.getPos().equals(pos)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(cancel);
        try {
            player.gameMode.destroyBlock(pos);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancel);
        }
        helper.assertTrue(level.getBlockState(pos) == mine && boline.getDamageValue() == 3
                && level.getEntitiesOfClass(ItemEntity.class, area).isEmpty(), "A denied break keeps the mine, tool and drops unchanged");
        helper.succeed();
    }
}
