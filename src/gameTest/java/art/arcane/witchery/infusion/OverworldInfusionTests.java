package art.arcane.witchery.infusion;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class OverworldInfusionTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_overworld_magnet")
    public static void overworldMagnetUsesTheOriginalPullAndOreOrder(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int x = helper.absolutePos(new BlockPos(8, 0, 0)).getX();
        FakePlayer caster = player(level, "Magnet", new Vec3(x + 0.5, 180, -0.5));
        caster.setShiftKeyDown(true);
        Vec3 eye = caster.getEyePosition();
        ItemEntity metal = item(level, Items.IRON_INGOT, eye.add(-4, -1, -3));
        ItemEntity aligned = item(level, Items.GOLD_NUGGET, eye.add(0, 0, -4));
        ItemEntity diamond = item(level, Items.DIAMOND, eye.add(-2, -1, -2));
        ItemEntity above = item(level, Items.GOLD_INGOT, caster.position().add(-2, 6.5, 0));
        List<ItemEntity> items = List.of(metal, aligned, diamond, above);
        BlockPos first = new BlockPos(x - 3, 180, 3);
        BlockPos second = new BlockPos(x + 3, 180, -4);
        BlockPos edge = new BlockPos(x + 4, 180, 6);
        level.setBlockAndUpdate(first, Blocks.IRON_ORE.defaultBlockState());
        level.setBlockAndUpdate(second, Blocks.GOLD_ORE.defaultBlockState());
        level.setBlockAndUpdate(edge, Blocks.IRON_ORE.defaultBlockState());
        // The negative-Z targets must still tick when the test scheduler places the arena far from Z zero.
        List<ChunkPos> forced = items.stream().map(ItemEntity::chunkPosition).distinct()
                .filter(chunk -> level.setChunkForced(chunk.x, chunk.z, true)).toList();
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                items.stream().allMatch(item -> level.isPositionEntityTicking(item.blockPosition())),
                "Wait for the magnet fixture chunks to tick entities")).thenExecute(() -> {
            try {
                for (ItemEntity item : items) {
                    helper.assertTrue(level.addFreshEntity(item), "The world accepts each magnet fixture item");
                }
                InfusionEnergy.grant(caster, OverworldInfusion.ID, 3);
                OverworldInfusion.channel(caster, 39);
                helper.assertTrue(charge(caster) == 3 && metal.position().equals(eye.add(-4, -1, -3)),
                        "The magnet waits for the two-second pulse");
                OverworldInfusion.channel(caster, 40);
                helper.assertTrue(metal.position().distanceTo(eye.add(-3, -0.75, -2.25)) < 1e-6,
                        "A diagonal metal item moves one block along X plus the original Y and Z offsets");
                helper.assertTrue(aligned.position().distanceTo(eye.add(0, 0, -3)) < 1e-6,
                        "An aligned item moves safely toward the caster");
                helper.assertTrue(diamond.position().equals(eye.add(-2, -1, -2))
                                && above.position().equals(caster.position().add(-2, 6.5, 0)),
                        "The magnet leaves nonmetal and out-of-range items in place");
                helper.assertTrue(charge(caster) == 0 && level.getBlockState(first).is(Blocks.STONE)
                                && level.getBlockState(second).is(Blocks.GOLD_ORE),
                        "The pulse spends one charge and scans X before Z when only one ore can be converted");
                level.removeBlock(second, false);
                InfusionEnergy.grant(caster, OverworldInfusion.ID, 3);
                OverworldInfusion.channel(caster, 44);
                helper.assertTrue(charge(caster) == 0 && level.getBlockState(edge).is(Blocks.STONE),
                        "An ore at Z six is in range when the caster's Z is minus one-half");
            } finally {
                items.forEach(ItemEntity::discard);
                for (BlockPos pos : List.of(first, second, edge)) level.removeBlock(pos, false);
                for (ChunkPos chunk : forced) level.setChunkForced(chunk.x, chunk.z, false);
            }
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void overworldHandRepelsMetalEquippedTargetsForTheOriginalCharge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 site = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(6, 0, 6)).atY(160));
        FakePlayer caster = player(level, "HandCaster", site);
        FakePlayer target = player(level, "HandTarget", site.add(0, 0, 1));
        InfusionEnergy.grant(caster, OverworldInfusion.ID, 20);
        ItemStack hand = new ItemStack(WitcheryItems.getRequired("witchhand").get());
        target.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        hand.getItem().onLeftClickEntity(hand, caster, target);
        helper.assertTrue(charge(caster) == 20 && target.getDeltaMovement().lengthSqr() == 0,
                "The Witch's Hand leaves a target in leather alone");
        target.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(WitcheryItems.getRequired("arthana").get()));
        hand.getItem().onLeftClickEntity(hand, caster, target);
        helper.assertTrue(charge(caster) == 20 && target.getDeltaMovement().lengthSqr() == 0,
                "The Arthana stays outside the original list of metal items");
        target.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        hand.getItem().onLeftClickEntity(hand, caster, target);
        helper.assertTrue(charge(caster) == 18 && target.getDeltaMovement().distanceTo(new Vec3(0, 0.3, 2.4)) < 1e-6,
                "Repulsion costs two charges and uses the original look and lift");
        caster.setShiftKeyDown(true);
        hand.getItem().onLeftClickEntity(hand, caster, target);
        helper.assertTrue(charge(caster) == 14 && target.getDeltaMovement().distanceTo(new Vec3(0, 1.5, 2.4)) < 1e-6,
                "Crouched repulsion costs four charges and uses the stronger lift");
        helper.succeed();
    }

    private static FakePlayer player(ServerLevel level, String name, Vec3 pos) {
        FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), name));
        player.moveTo(pos.x, pos.y, pos.z, 0, 0);
        return player;
    }

    private static ItemEntity item(ServerLevel level, Item kind, Vec3 position) {
        ItemEntity item = new ItemEntity(level, position.x, position.y, position.z, new ItemStack(kind));
        item.setNoGravity(true);
        item.setDeltaMovement(Vec3.ZERO);
        item.setNeverPickUp();
        return item;
    }

    private static int charge(FakePlayer player) {
        return player.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG).getInt("witcheryInfusionCharges");
    }
}
