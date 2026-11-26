package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.CursedControls;
import art.arcane.witchery.block.entity.CursedControlBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class TriggeredBrewTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_triggered_brew_caster")
    public static void itemCurseAndUnattendedControlUseTheWorldFakePlayer(GameTestHelper helper) {
        var level = helper.getLevel();
        var brew = brewTag();
        var causes = new ArrayList<Entity>();
        var holder = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "CurseHolder")) {
            @Override
            public boolean hurt(DamageSource source, float amount) {
                causes.add(source.getEntity());
                return false;
            }
        };
        holder.setGameMode(GameType.SURVIVAL);
        holder.setPos(helper.absoluteVec(new Vec3(3, 2, 3)));
        var stack = new ItemStack(Items.STICK);
        var curse = new CompoundTag();
        curse.put("Curse", brew);
        curse.putInt("Count", 1);
        stack.getOrCreateTag().put("WITCCurse", curse);
        holder.setItemInHand(InteractionHand.MAIN_HAND, stack);

        var controlPos = helper.absolutePos(new BlockPos(5, 2, 3));
        var controlBlock = WitcheryBlocks.getRequired("cbuttonstone").get();
        level.setBlockAndUpdate(controlPos, controlBlock.defaultBlockState());
        var control = (CursedControlBlockEntity) level.getBlockEntity(controlPos);
        control.charge(brew, BrewMixture.from(brew), 0x113344, null);
        var victim = EntityType.PIG.create(level);
        victim.setPos(helper.absoluteVec(new Vec3(5, 2, 4)));
        Consumer<LivingAttackEvent> listener = event -> {
            if (event.getEntity() == victim) {
                causes.add(event.getSource().getEntity());
            }
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(holder, InteractionHand.MAIN_HAND));
            helper.assertTrue(control.spend(level, victim), "The control spends its last valid charge");
            var expected = FakePlayerFactory.getMinecraft(level);
            helper.assertTrue(causes.size() == 2 && causes.stream().allMatch(caster -> caster == expected),
                    "Both curse effects credit the world's fake caster");
            helper.assertTrue(!stack.getOrCreateTag().contains("WITCCurse"), "The item spends its last charge");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            holder.discard();
            victim.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_triggered_brew_offering")
    public static void itemCurseCanReachTheOriginalUpperRim(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 2, 3)).atY(196);
        var item = new ItemEntity(level, pos.getX() - 0.05, pos.getY() + 1.2, pos.getZ() + 0.5,
                new ItemStack(Items.STICK, 2));
        item.setNoGravity(true);
        level.addFreshEntity(item);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(item.getUUID()) == item,
                "The upper-rim offering is loaded")).thenExecute(() -> {
            try {
                var brew = brewTag();
                helper.assertTrue(BrewItemCurse.attach(level, pos, brew), "The original upper rim admits the offering");
                helper.assertTrue(item.getItem().getCount() == 1 && !item.getItem().hasTag(),
                        "Only the separated item is cursed");
                helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, item.getBoundingBox().inflate(1),
                                drop -> drop != item && drop.getItem().hasTag()
                                        && BrewItemCurse.savedBrew(drop.getItem().getTag()).equals(brew)
                                        && drop.getItem().getCount() == 1).size() == 1,
                        "The separated item receives the ordered curse");
            } finally {
                level.getEntitiesOfClass(ItemEntity.class, item.getBoundingBox().inflate(1)).forEach(Entity::discard);
            }
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_triggered_brew_impact")
    public static void thrownCurseDistinguishesTheStruckCellAndItsNeighbor(GameTestHelper helper) {
        var level = helper.getLevel();
        var direct = helper.absolutePos(new BlockPos(5, 6, 5));
        var beside = direct.east();
        for (var pos : List.of(direct.below(), beside.below())) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        var lever = Blocks.LEVER.defaultBlockState().setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR);
        var plate = Blocks.STONE_PRESSURE_PLATE.defaultBlockState();
        var hit = new BlockHitResult(Vec3.atCenterOf(direct), Direction.EAST, direct, false);
        var brew = brewTag();
        var mixture = BrewMixture.from(brew);
        level.setBlockAndUpdate(direct, lever);
        level.setBlockAndUpdate(beside, plate);
        CursedControls.impact(level, hit, brew, mixture, 0x113344, null);
        helper.assertTrue(level.getBlockState(direct).equals(lever) && level.getBlockState(beside).equals(plate),
                "A direct lever and an adjacent plate remain ordinary");

        level.setBlockAndUpdate(direct, plate);
        level.setBlockAndUpdate(beside, lever);
        CursedControls.impact(level, hit, brew, mixture, 0x113344, null);
        helper.assertTrue(level.getBlockState(direct).equals(plate) && curseCount(helper, beside) == 1,
                "A new adjacent lever takes the impact before the direct plate");
        CursedControls.impact(level, hit, brew, mixture, 0x113344, null);
        helper.assertTrue(curseCount(helper, direct) == 1 && curseCount(helper, beside) == 2,
                "The next impact recharges the lever and arms the struck plate");

        var pig = EntityType.PIG.create(level);
        try {
            pig.setPos(Vec3.atBottomCenterOf(direct));
            CursedControls.impact(level, new EntityHitResult(pig), brew, mixture, 0x113344, null);
            helper.assertTrue(curseCount(helper, direct) == 3 && curseCount(helper, beside) == 2,
                    "An entity impact visits its occupied plate in both passes");
        } finally {
            pig.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_triggered_brew_protection")
    public static void thrownCurseHonorsProtectionAtTheStruckFaceAndControl(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 6, 5));
        var plate = Blocks.STONE_PRESSURE_PLATE.defaultBlockState();
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var brew = brewTag();
        var mixture = BrewMixture.from(brew);
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "TriggerProtection"));
        var visited = new ArrayList<BlockPos>();
        var veto = new AtomicReference<>(pos.above());
        Consumer<BlockEvent.BreakEvent> protection = event -> {
            if (event.getLevel() != level || event.getPlayer() != caster) return;
            visited.add(event.getPos());
            if (event.getPos().equals(veto.get())) event.setCanceled(true);
        };
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos, plate);
        MinecraftForge.EVENT_BUS.addListener(protection);
        try {
            CursedControls.impact(level, hit, brew, mixture, 0x113344, caster);
            helper.assertTrue(visited.equals(List.of(pos.above())) && level.getBlockState(pos).equals(plate),
                    "A veto at the impact-facing cell prevents the subsequent direct conversion");
            veto.set(pos);
            visited.clear();
            CursedControls.impact(level, hit, brew, mixture, 0x113344, caster);
            helper.assertTrue(visited.equals(List.of(pos.above(), pos)) && level.getBlockState(pos).equals(plate),
                    "The modern Forge veto still protects the struck plate itself");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            caster.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_triggered_brew_flight")
    public static void fallingCurseBottleHitsAnUncollidablePressurePlate(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 6, 5));
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
        var brew = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        brew.setTag(brewTag());
        var bottle = WitcheryEntities.THROWN_BREW.get().create(level);
        bottle.setItem(brew);
        bottle.setPos(Vec3.atBottomCenterOf(pos).add(0, 1.5, 0));
        bottle.setDeltaMovement(0, -0.7, 0);
        bottle.setNoGravity(true);
        level.addFreshEntity(bottle);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(bottle.isRemoved(), "The thrown bottle lands"))
                .thenExecute(() -> helper.assertTrue(curseCount(helper, pos) == 1,
                        "The blow strikes the plate's outline above the support")).thenSucceed();
    }

    private static int curseCount(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getBlockEntity(pos) instanceof CursedControlBlockEntity control
                ? control.saveWithoutMetadata().getInt("Count") : 0;
    }

    private static CompoundTag brewTag() {
        return BrewMixture.ingredientTag(List.of(new ItemStack(Items.NETHER_WART),
                new ItemStack(Items.PUMPKIN), new ItemStack(Items.ZOMBIE_HEAD)), 0x113344);
    }
}
