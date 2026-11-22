package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyCauldronRitualTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_healing")
    public static void originalMelonBrewsRetainTheirInstantEffects(GameTestHelper helper) {
        var wart = oldItem("minecraft:nether_wart", 0, 1);
        var melon = oldItem("minecraft:speckled_melon", 0, 1);
        var original = originalRitual(3000, 0, true, wart, melon);
        var originalBrew = original.getCompound("Tag");
        originalBrew.remove("RitualTriggered");
        var cauldron = placeCauldron(helper, helper.absolutePos(new BlockPos(6, 4, 5)), original);
        cauldron.load(cauldron.saveWithFullMetadata());
        var saved = cauldron.saveWithFullMetadata();
        var ingredients = saved.getList("Ingredients", Tag.TAG_COMPOUND);
        helper.assertTrue(ingredients.size() == 2 && ingredients.getCompound(0).getString("id").equals("minecraft:nether_wart")
                        && ingredients.getCompound(1).getString("id").equals("minecraft:glistering_melon_slice")
                        && saved.getInt("Color") == originalBrew.getInt("Color") && tank(cauldron).getFluidInTank(0).getAmount() == 3000,
                "The original cauldron should retain its ordered healing brew, amount and color through a save round trip");

        var bottle = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        bottle.setTag(originalBrew.copy());
        var healed = helper.spawn(EntityType.PIG, new BlockPos(4, 3, 5));
        healed.setHealth(4.0F);
        helper.assertTrue(bottle.finishUsingItem(helper.getLevel(), healed).is(Items.GLASS_BOTTLE) && healed.getHealth() == 8.0F,
                "A saved healing bottle should heal by four points and return glass");

        var inverted = originalRitual(3000, 0, true, wart, oldItem("minecraft:fermented_spider_eye", 0, 1), melon)
                .getCompound("Tag");
        inverted.remove("RitualTriggered");
        var second = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        second.setTag(inverted);
        var hurt = helper.spawn(EntityType.PIG, new BlockPos(8, 3, 5));
        helper.assertTrue(second.finishUsingItem(helper.getLevel(), hurt).is(Items.GLASS_BOTTLE) && hurt.getHealth() == 4.0F,
                "The original inverted bottle should deal six points of instant damage");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void resumesOriginalMutandisWarmupAcrossBothSaveFormats(GameTestHelper helper) {
        BlockPos relative = new BlockPos(6, 4, 5);
        BlockPos pos = helper.absolutePos(relative);
        var cauldron = placeCauldron(helper, pos, originalRitual(3000, 19, true,
                oldItem("witchery:ingredient", 22, 1), oldItem("witchery:ingredient", 31, 1), oldItem("minecraft:egg", 0, 1)));
        CompoundTag beforeFirstTick = cauldron.saveWithFullMetadata();
        helper.assertTrue(beforeFirstTick.getBoolean("LegacyRitualPending") && beforeFirstTick.getInt("RitualTicks") == 19,
                "Saving before recipe lookup preserves the original trigger and pulse count");
        cauldron.load(beforeFirstTick);
        helper.runAfterDelay(4, () -> {
            CompoundTag resumed = cauldron.saveWithFullMetadata();
            helper.assertTrue(resumed.getString("Ritual").equals("witchery:mutandis") && resumed.getInt("RitualTicks") >= 190
                            && resumed.getInt("RitualTicks") < 200 && resumed.getList("Ingredients", Tag.TAG_COMPOUND).size() == 2,
                    "The server selects Mutandis and converts warmup pulses to ticks without keeping the Egg as an ingredient");
            cauldron.load(resumed);
        });
        helper.runAfterDelay(28, () -> {
            helper.assertItemEntityCountIs(WitcheryIngredient.MUTANDIS.item(), relative.above(2), 3, 6);
            helper.assertTrue(tank(cauldron).getFluidInTank(0).isEmpty(), "The resumed recipe yields six Mutandis and clears the tank");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void refillsLegacyCastFromItsSavedBrewBucket(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        BlockPos receiver = pos.east(4);
        CompoundTag original = originalRitual(2000, 0, false, oldItem("witchery:ingredient", 22, 1),
                oldItem("minecraft:glowstone_dust", 0, 1), oldItem("minecraft:coal", 1, 1));
        var cauldron = placeCauldron(helper, pos, original);
        var emptyCauldron = placeCauldron(helper, receiver, new CompoundTag());
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LegacyCauldron"));
        helper.runAfterDelay(3, () -> {
            cauldron.load(cauldron.saveWithFullMetadata());
            helper.assertTrue(cauldron.saveWithFullMetadata().getBoolean("LegacyRitualPending")
                            && tank(cauldron).getFluidInTank(0).getAmount() == 2000,
                    "A partially drained legacy cast retains its trigger and liquid across a saved round trip");
            var bucket = new ItemStack(WitcheryItems.getRequired("bucketbrew").get());
            bucket.setTag(original.getCompound("Tag").copy());
            player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
            var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            level.getBlockState(pos).use(level, player, InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "Using the original matching brew bucket restores the final bucket");

            for (int index = 0; index < 3; index++) {
                var portion = new ItemStack(WitcheryItems.getRequired("bucketbrew").get());
                portion.setTag(original.getCompound("Tag").copy());
                player.setItemInHand(InteractionHand.MAIN_HAND, portion);
                var receiverHit = new BlockHitResult(Vec3.atCenterOf(receiver), Direction.UP, receiver, false);
                level.getBlockState(receiver).use(level, player, InteractionHand.MAIN_HAND, receiverHit);
                helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "The empty cauldron accepts all three saved Brew Buckets");
            }
        });
        helper.runAfterDelay(6, () -> {
            CompoundTag restored = cauldron.saveWithFullMetadata();
            helper.assertTrue(restored.getString("Ritual").equals("witchery:brew_fuel_1") && !restored.contains("LegacyRitualPending")
                            && tank(cauldron).getFluidInTank(0).getAmount() == 3000,
                    "Charcoal resolves to the highest matching Brew Fuel recipe and the full cast waits for heat and power");
            helper.assertTrue(emptyCauldron.saveWithFullMetadata().getString("Ritual").equals("witchery:brew_fuel_1"),
                    "The legacy buckets also retain their cast in a previously empty cauldron");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void preservesLegacyGeneralBrewTrigger(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var cauldron = placeCauldron(helper, pos, originalRitual(3000, 0, false, oldItem("minecraft:nether_wart", 0, 1),
                oldItem("minecraft:sugar", 0, 1), oldItem("minecraft:gunpowder", 0, 1), oldItem("witchery:dog_tongue", 0, 1)));
        helper.runAfterDelay(5, () -> {
            var saved = cauldron.saveWithFullMetadata();
            helper.assertTrue(saved.getBoolean("RitualTriggered") && !saved.contains("LegacyRitualPending") && !saved.contains("Ritual")
                            && saved.getList("Ingredients", Tag.TAG_COMPOUND).size() == 4,
                    "The old Tongue of Dog retains its normal cast state and place at the end of the mixture");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void refundsUnmatchedLegacyTriggerAndKeepsMixture(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var cauldron = placeCauldron(helper, pos, originalRitual(3000, 0, false, oldItem("minecraft:nether_wart", 0, 1),
                oldItem("minecraft:egg", 0, 3)));
        helper.runAfterDelay(5, () -> {
            cauldron.load(cauldron.saveWithFullMetadata());
            CompoundTag saved = cauldron.saveWithFullMetadata();
            helper.assertTrue(!saved.contains("Ritual") && !saved.contains("LegacyRitualPending")
                            && saved.getList("Ingredients", Tag.TAG_COMPOUND).size() == 1
                            && tank(cauldron).getFluidInTank(0).getAmount() == 3000,
                    "A recipe missing from the installed data releases the cauldron and preserves the brew");
        });
        helper.runAfterDelay(10, () -> {
            var eggs = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3), item -> item.getItem().is(Items.EGG));
            helper.assertTrue(eggs.size() == 1 && eggs.get(0).getItem().getCount() == 3,
                    "The original trigger stack is refunded once even across a saved round trip");
            helper.succeed();
        });
    }

    private static WitchesCauldronBlockEntity placeCauldron(GameTestHelper helper, BlockPos pos, CompoundTag original) {
        var level = helper.getLevel();
        if (original.getInt("TicksHeated") != 0) {
            level.setBlockAndUpdate(pos.below(2), Blocks.NETHERRACK.defaultBlockState());
            level.setBlockAndUpdate(pos.below(), Blocks.FIRE.defaultBlockState());
        }
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var cauldron = (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
        cauldron.load(original);
        return cauldron;
    }

    private static IFluidHandler tank(WitchesCauldronBlockEntity cauldron) {
        return cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
    }

    private static CompoundTag originalRitual(int fluid, int pulses, boolean heated, CompoundTag... offerings) {
        var root = new CompoundTag();
        root.putString("FluidName", "witchery:brew");
        root.putInt("Amount", fluid);
        root.putInt("TicksHeated", heated ? 100 : 0);
        root.putInt("RitualTicks", pulses);
        var brew = new CompoundTag();
        brew.putInt("Color", 0xb4a96c);
        brew.putBoolean("RitualTriggered", true);
        var items = new ListTag();
        for (var offering : offerings) items.add(offering);
        brew.put("Items", items);
        root.put("Tag", brew);
        return root;
    }

    private static CompoundTag oldItem(String id, int damage, int count) {
        var item = new CompoundTag();
        item.putString("id", id);
        item.putShort("Damage", (short) damage);
        item.putByte("Count", (byte) count);
        return item;
    }
}
