package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.CursedControlBlockEntity;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.item.RitualChalkItem;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PlacementProtectionTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_placement_protection")
    public static void poweredControlsNotifyNeighborsOnlyAfterApproval(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ControlProtection"));
        var ingredients = List.of(Items.NETHER_WART.getDefaultInstance(), Items.SPIDER_EYE.getDefaultInstance(),
                Items.ZOMBIE_HEAD.getDefaultInstance());
        var mixture = BrewMixture.from(ingredients);
        var brew = mixture.bottleTag(ingredients);
        try {
            for (var kind : CursedControls.Kind.values()) {
                if (kind != CursedControls.Kind.WOODEN_DOOR) {
                    var control = helper.absolutePos(new BlockPos(2 + kind.ordinal(), 6, 5));
                    checkPoweredConversion(helper, caster, control, kind, mixture, brew);
                }
            }
        } finally {
            caster.discard();
        }
        helper.succeed();
    }

    private static void checkPoweredConversion(GameTestHelper helper, FakePlayer caster, BlockPos pos,
                                               CursedControls.Kind kind, BrewMixture mixture,
                                               CompoundTag brew) {
        var level = helper.getLevel();
        var support = pos.below();
        var lamp = support.below();
        var original = kind.original().defaultBlockState().setValue(BlockStateProperties.POWERED, true);
        if (original.hasProperty(BlockStateProperties.ATTACH_FACE)) {
            original = original.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR);
        }
        level.setBlockAndUpdate(support, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos, original);
        level.setBlockAndUpdate(lamp, Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT, true));
        helper.assertTrue(level.hasNeighborSignal(lamp), kind + " powers its supporting block");
        boolean scheduledBefore = level.getBlockTicks().hasScheduledTick(lamp, Blocks.REDSTONE_LAMP);

        var ordered = new ArrayList<String>();
        Consumer<BlockEvent.EntityPlaceEvent> protect = event -> {
            if (event.getLevel() != level || event.getEntity() != caster || !event.getPos().equals(pos)
                    || !event.getPlacedBlock().is(kind.cursed())) return;
            if (!ordered.contains("placement")) event.setCanceled(true);
            ordered.add("placement");
        };
        Consumer<BlockEvent.NeighborNotifyEvent> notify = event -> {
            if (event.getLevel() == level && (event.getPos().equals(pos) || event.getPos().equals(support))) {
                ordered.add("neighbors");
            }
        };
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        MinecraftForge.EVENT_BUS.addListener(protect);
        MinecraftForge.EVENT_BUS.addListener(notify);
        try {
            CursedControls.impact(level, hit, brew, mixture, 0x51a038, caster);
            var restored = level.getBlockState(pos);
            var restoredData = level.getBlockEntity(pos);
            boolean lit = level.getBlockState(lamp).getValue(RedstoneLampBlock.LIT);
            boolean scheduledAfter = level.getBlockTicks().hasScheduledTick(lamp, Blocks.REDSTONE_LAMP);
            helper.assertTrue(ordered.equals(List.of("placement")) && restored.equals(original)
                            && restoredData == null && lit && scheduledAfter == scheduledBefore,
                    "Veto preserves the powered " + kind + ". Events " + ordered + ", state " + restored
                            + ", data " + restoredData + ", lit " + lit + ", scheduled " + scheduledBefore + " to " + scheduledAfter);
            CursedControls.impact(level, hit, brew, mixture, 0x51a038, caster);
            boolean button = kind == CursedControls.Kind.STONE_BUTTON || kind == CursedControls.Kind.WOODEN_BUTTON;
            boolean plate = !button && kind != CursedControls.Kind.LEVER;
            helper.assertTrue(ordered.size() > 2 && ordered.get(1).equals("placement")
                            && level.getBlockState(pos).is(kind.cursed())
                            && level.getBlockState(pos).getValue(BlockStateProperties.POWERED) == !button
                            && level.getBlockTicks().hasScheduledTick(lamp, Blocks.REDSTONE_LAMP) == (scheduledBefore || button)
                            && level.getBlockTicks().hasScheduledTick(pos, kind.cursed()) == plate,
                    "The approved " + kind + " propagates the original signal only after the placement event. " + ordered);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protect);
            MinecraftForge.EVENT_BUS.unregister(notify);
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_placement_protection")
    public static void cursedDoorPlacementIsVetoedAsAPair(GameTestHelper helper) {
        var level = helper.getLevel();
        var bottom = helper.absolutePos(new BlockPos(5, 6, 5));
        var top = bottom.above();
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "DoorProtection"));
        var door = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST)
                .setValue(DoorBlock.HINGE, DoorHingeSide.RIGHT).setValue(DoorBlock.OPEN, true);
        var upper = door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        var cursed = CursedControls.Kind.WOODEN_DOOR.cursed();
        level.setBlockAndUpdate(bottom.below(), Blocks.STONE.defaultBlockState());
        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        level.setBlock(bottom, door, flags);
        level.setBlock(top, upper, flags);

        var ingredients = List.of(Items.NETHER_WART.getDefaultInstance(), Items.SPIDER_EYE.getDefaultInstance(),
                Items.ZOMBIE_HEAD.getDefaultInstance());
        var mixture = BrewMixture.from(ingredients);
        var brew = mixture.bottleTag(ingredients);
        var hit = new BlockHitResult(Vec3.atCenterOf(bottom), Direction.UP, bottom, false);
        var events = new ArrayList<BlockEvent.EntityMultiPlaceEvent>();
        Consumer<BlockEvent.EntityMultiPlaceEvent> protect = event -> {
            if (event.getLevel() != level || event.getEntity() != caster || !event.getPos().equals(bottom)) return;
            var snapshots = event.getReplacedBlockSnapshots();
            helper.assertTrue(snapshots.size() == 2 && snapshots.get(0).getReplacedBlock().equals(door)
                            && snapshots.get(1).getReplacedBlock().equals(upper)
                            && snapshots.get(0).getCurrentBlock().is(cursed)
                            && snapshots.get(1).getCurrentBlock().is(cursed),
                    "One event sees both cursed halves and the complete original door");
            events.add(event);
            if (events.size() == 1) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(protect);
        try {
            CursedControls.impact(level, hit, brew, mixture, 0x51a038, caster);
            helper.assertTrue(events.size() == 1 && level.getBlockState(bottom).equals(door)
                            && level.getBlockState(top).equals(upper)
                            && level.getBlockEntity(bottom) == null && level.getBlockEntity(top) == null,
                    "Veto restores both vanilla halves and clears the temporary curse data");
            CursedControls.impact(level, hit, brew, mixture, 0x51a038, caster);
            helper.assertTrue(events.size() == 2 && level.getBlockState(bottom).equals(cursed.withPropertiesOf(door))
                            && level.getBlockState(top).equals(cursed.withPropertiesOf(upper))
                            && level.getBlockEntity(bottom) instanceof CursedControlBlockEntity control
                            && control.saveWithoutMetadata().getInt("Count") == 1,
                    "Approved conversion retains the door state and installs one charge");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protect);
            caster.discard();
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_placement_protection")
    public static void canceledChalkUseKeepsTheStackAndStaysQuiet(GameTestHelper helper) {
        var level = helper.getLevel();
        var support = helper.absolutePos(new BlockPos(5, 6, 5));
        var target = support.above();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ChalkProtection"));
        var chalk = WitcheryItems.getRequired("chalkritual").get();
        var glyph = WitcheryBlocks.getRequired("circleglyphritual").get();
        var infernal = WitcheryBlocks.getRequired("circleglyphinfernal").get();
        var held = new ItemStack(chalk, 3);
        var hit = new BlockHitResult(Vec3.atBottomCenterOf(target), Direction.UP, support, false);
        level.setBlockAndUpdate(support, Blocks.STONE.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        var placements = new ArrayList<BlockEvent.EntityPlaceEvent>();
        var cues = new ArrayList<Vec3>();
        Consumer<BlockEvent.EntityPlaceEvent> protect = event -> {
            if (event.getLevel() != level || event.getEntity() != player || !event.getPos().equals(target)) return;
            placements.add(event);
            if ((placements.size() == 1 && event.getPlacedBlock().is(glyph)) || event.getPlacedBlock().is(infernal)) {
                event.setCanceled(true);
            }
        };
        Consumer<PlayLevelSoundEvent.AtPosition> sound = event -> {
            if (event.getLevel() == level && event.getSound() != null
                    && event.getSound().value() == WitcherySounds.CHALK.get()
                    && event.getPosition().distanceTo(Vec3.atLowerCornerOf(support)) < 3) cues.add(event.getPosition());
        };
        MinecraftForge.EVENT_BUS.addListener(protect);
        MinecraftForge.EVENT_BUS.addListener(sound);
        try {
            held.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            helper.assertTrue(level.isEmptyBlock(target) && held.getCount() == 3 && held.getDamageValue() == 0
                            && player.getInventory().countItem(chalk) == 3 && cues.isEmpty(),
                    "A veto leaves all three chalk sticks intact and emits no drawing sound");
            RitualChalkItem.drawInfernal(level, hit, player);
            helper.assertTrue(placements.size() == 2 && placements.get(1).getPlacedBlock().is(infernal)
                            && level.isEmptyBlock(target) && cues.isEmpty(),
                    "The infernal cast exposes its actual glyph before any drawing sound");
            held.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            helper.assertTrue(placements.size() == 3 && level.getBlockState(target).is(glyph)
                            && held.getCount() == 1 && held.getDamageValue() == 1
                            && player.getInventory().countItem(chalk) == 3,
                    "An allowed draw splits the stack once without duplicating any chalk");
            helper.assertTrue(cues.equals(List.of(Vec3.atLowerCornerOf(support))),
                    "One chalk cue plays at the original clicked block's coordinates");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protect);
            MinecraftForge.EVENT_BUS.unregister(sound);
            player.discard();
        }
    }
}
