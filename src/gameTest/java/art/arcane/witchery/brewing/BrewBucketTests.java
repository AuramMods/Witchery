package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.registry.WitcheryFluids;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewBucketTests {
    @GameTest(template = "qa_coven_arena", timeoutTicks = 160)
    public static void movesBrewThroughSurvivalBucketsAndFluidCapabilities(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrewTransport"));
        var cauldron = WitcheryBlocks.getRequired("cauldron").get();
        BlockPos source = helper.absolutePos(new BlockPos(4, 4, 4));
        BlockPos destination = source.east(3);
        level.setBlockAndUpdate(source.below(2), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(source.below(), Blocks.FIRE.defaultBlockState());
        level.setBlockAndUpdate(source, cauldron.defaultBlockState());
        level.setBlockAndUpdate(destination, cauldron.defaultBlockState());
        var from = (WitchesCauldronBlockEntity) level.getBlockEntity(source);
        var to = (WitchesCauldronBlockEntity) level.getBlockEntity(destination);
        var sourceTank = from.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var destinationTank = to.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        for (int i = 0; i < 3; i++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            use(helper, player, source);
            helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "Filling the cauldron returns an empty bucket");
        }
        helper.runAfterDelay(105, () -> {
            helper.assertTrue(level.getBlockState(source).getValue(WitchesCauldronBlock.BOILING), "Fire brought the brew to a boil");
            for (var item : new ItemStack[]{new ItemStack(Items.NETHER_WART), new ItemStack(Items.SUGAR), new ItemStack(Items.BLUE_WOOL)}) {
                var dropped = new ItemEntity(level, source.getX() + 0.5, source.getY() + 0.4, source.getZ() + 0.5, item);
                level.getBlockState(source).entityInside(level, source, dropped);
                helper.assertTrue(dropped.isRemoved(), "The boiling cauldron accepts the dropped ingredient");
            }
            FluidStack original = sourceTank.getFluidInTank(0);
            helper.assertTrue(original.getFluid() == WitcheryFluids.getRequiredSource("brew").get() && original.getAmount() == 3000,
                    "The exposed tank represents an ordered brew as its registered fluid");
            var originalTag = original.getTag();
            helper.assertTrue(originalTag != null && originalTag.getList("Items", Tag.TAG_COMPOUND).size() == 2,
                    "The transport tag retains the two brewing ingredients in order");

            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
            use(helper, player, source);
            var bucket = player.getMainHandItem();
            helper.assertTrue(bucket.is(WitcheryItems.getRequired("bucketbrew").get()) && originalTag.equals(bucket.getTag())
                            && sourceTank.getFluidInTank(0).getAmount() == 2000,
                    "An empty bucket collects exactly 1,000 mB with its brew tag");
            bucket = ItemStack.of(bucket.save(new net.minecraft.nbt.CompoundTag()));
            player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
            use(helper, player, destination);
            helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "Pouring the saved bucket returns the empty bucket");
            var transferred = destinationTank.getFluidInTank(0);
            helper.assertTrue(transferred.getAmount() == 1000 && transferred.isFluidEqual(original),
                    "Refilling a cold cauldron retains the complete brew");
            to.load(to.saveWithFullMetadata());
            var reopened = destinationTank.getFluidInTank(0);
            var brewedEffect = BrewMixture.from(reopened.getTag());
            helper.assertTrue(reopened.getAmount() == 1000 && reopened.isFluidEqual(original)
                            && brewedEffect != null && brewedEffect.effects().stream().anyMatch(effect -> effect.effect() == MobEffects.MOVEMENT_SPEED),
                    "The transferred color, volume and Swiftness effect survive cauldron serialization");

            var incompatible = reopened.copy();
            var alternatives = incompatible.getTag().getList("Items", Tag.TAG_COMPOUND);
            Tag first = alternatives.remove(0);
            alternatives.add(first);
            helper.assertTrue(destinationTank.fill(incompatible, IFluidHandler.FluidAction.EXECUTE) == 0,
                    "Reordering the ingredients makes a different mixture");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            use(helper, player, destination);
            helper.assertTrue(player.getMainHandItem().is(Items.WATER_BUCKET), "Water cannot dilute a cauldron of brew");

            var externalTank = new FluidTank(1000);
            var moved = FluidUtil.tryFluidTransfer(externalTank, sourceTank, 1000, false);
            helper.assertTrue(moved.getAmount() == 1000 && moved.isFluidEqual(original) && externalTank.isEmpty()
                            && sourceTank.getFluidInTank(0).getAmount() == 2000, "Simulation does not move fluid");
            moved = FluidUtil.tryFluidTransfer(externalTank, sourceTank, 1000, true);
            helper.assertTrue(moved.getAmount() == 1000 && externalTank.getFluid().isFluidEqual(original),
                    "A Forge tank receives the same complete mixture");
            moved = FluidUtil.tryFluidTransfer(destinationTank, externalTank, 1000, true);
            helper.assertTrue(externalTank.isEmpty() && moved.getAmount() == 1000
                            && destinationTank.getFluidInTank(0).getAmount() == 2000,
                    "An identical mixture can be combined again");
            helper.succeed();
        });
    }

    private static void use(GameTestHelper helper, Player player, BlockPos cauldron) {
        var level = helper.getLevel();
        var hit = new BlockHitResult(Vec3.atCenterOf(cauldron), Direction.UP, cauldron, false);
        helper.assertTrue(level.getBlockState(cauldron).use(level, player, InteractionHand.MAIN_HAND, hit).consumesAction(),
                "The cauldron handles the player interaction");
    }
}
