package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AreaWardBlockEntity;
import art.arcane.witchery.block.entity.PlacedOwnerBlockEntity;
import art.arcane.witchery.block.entity.VoidBrambleBlockEntity;
import art.arcane.witchery.config.WitcheryCommonConfig;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.crafting.ProtectionRecipeCondition;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.ritual.DirectedCurse;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ProtectionBlockTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_owned_ward_recovery")
    public static void recoveryPreservesVetoedStatuesAndDropsEachAllowedBlock(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ColumnOwner"));
        owner.setShiftKeyDown(true);
        Block ward = WitcheryBlocks.getRequired("decursedirected").get();
        BlockPos lower = helper.absolutePos(new BlockPos(5, 3, 5));
        BlockPos upper = lower.above();
        for (BlockPos pos : List.of(lower, upper)) {
            level.setBlockAndUpdate(pos, ward.defaultBlockState());
            ward.setPlacedBy(level, pos, ward.defaultBlockState(), owner, new ItemStack(ward));
        }
        var rejectBreak = new AtomicBoolean(true);
        var rejectSpawn = new AtomicBoolean(false);
        List<ItemEntity> drops = new ArrayList<>();
        Consumer<BlockEvent.BreakEvent> protect = event -> {
            if (event.getPlayer() == owner && event.getPos().equals(upper) && rejectBreak.get()) event.setCanceled(true);
        };
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (!(event.getEntity() instanceof ItemEntity drop) || !drop.getItem().is(ward.asItem())) return;
            if (rejectSpawn.get()) event.setCanceled(true);
            else drops.add(drop);
        };
        var interModBreaks = WitcheryServerConfig.ALLOW_INTER_MOD_BLOCK_BREAK_EVENTS;
        boolean originalBreaks = interModBreaks.get();
        var tileDrops = level.getGameRules().getRule(GameRules.RULE_DOBLOCKDROPS);
        boolean originalDrops = tileDrops.get();
        MinecraftForge.EVENT_BUS.addListener(protect);
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            interModBreaks.set(true);
            tileDrops.set(false, level.getServer());
            level.getBlockState(lower).attack(level, lower, owner);
            helper.assertTrue(level.isEmptyBlock(lower) && level.getBlockState(upper).is(ward) && drops.size() == 1,
                    "Recovery returns the allowed block and stops at the Forge-protected part of the column");
            rejectBreak.set(false);
            rejectSpawn.set(true);
            level.getBlockState(upper).attack(level, upper, owner);
            helper.assertTrue(level.getBlockEntity(upper) instanceof AreaWardBlockEntity intact && intact.ownedBy(owner)
                            && drops.size() == 1, "A rejected drop preserves the block and its saved owner");
            rejectSpawn.set(false);
            level.getBlockState(upper).attack(level, upper, owner);
            helper.assertTrue(level.isEmptyBlock(upper) && drops.size() == 2
                            && drops.get(0).position().equals(Vec3.atCenterOf(lower))
                            && drops.get(1).position().equals(Vec3.atCenterOf(upper))
                            && drops.stream().allMatch(drop -> drop.saveWithoutId(new CompoundTag()).getShort("PickupDelay") == 0
                                    && drop.getDeltaMovement().y == 0.2F),
                    "Each allowed block returns exactly one item at its original center with immediate pickup");
        } finally {
            tileDrops.set(originalDrops, level.getServer());
            interModBreaks.set(originalBreaks);
            MinecraftForge.EVENT_BUS.unregister(capture);
            MinecraftForge.EVENT_BUS.unregister(protect);
            drops.forEach(ItemEntity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_area_ward_pvp")
    public static void wardExcludesDirectKillersAndKeepsArcherProtection(GameTestHelper helper) {
        var level = helper.getLevel();
        var attacker = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "WardAttacker"));
        var victim = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "WardVictim"));
        var pos = helper.absolutePos(new BlockPos(5, 3, 5));
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("decursedirected").get().defaultBlockState());
        level.setBlockAndUpdate(pos.above(), WitcheryBlocks.getRequired("decurseteleport").get().defaultBlockState());
        attacker.setPos(pos.getX() + 32, pos.getY() + 24, pos.getZ());
        victim.setPos(pos.getX() + 40, pos.getY(), pos.getZ());
        var curse = WitcheryServerConfig.DECURSE_DIRECTED_ENABLED;
        var teleport = WitcheryServerConfig.DECURSE_TELEPORT_ENABLED;
        boolean curseWasEnabled = curse.get(), teleportWasEnabled = teleport.get();
        try {
            curse.set(true);
            teleport.set(true);
            var arrow = EntityType.ARROW.create(level);
            MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(victim, level.damageSources().arrow(arrow, attacker)));
            helper.assertTrue(AreaWardBlockEntity.protectsDirectedCurse(attacker)
                            && AreaWardBlockEntity.protectsSummoning(attacker),
                    "An arrow kill keeps both protections at the inclusive horizontal boundary");
            MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(victim, level.damageSources().playerAttack(attacker)));
            helper.assertFalse(AreaWardBlockEntity.protectsBrew(attacker), "Direct player damage excludes the attacker from both wards");
            for (BlockPos block : List.of(pos, pos.above())) {
                var killers = level.getBlockEntity(block).saveWithFullMetadata().getList("Killers", Tag.TAG_STRING);
                helper.assertTrue(killers.size() == 1 && killers.getString(0).equals("WardAttacker"),
                        "Both wards save the directly attributed attacker by name");
            }
        } finally {
            curse.set(curseWasEnabled);
            teleport.set(teleportWasEnabled);
            level.removeBlock(pos, false);
            level.removeBlock(pos.above(), false);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void brambleSuppressesNewRitesUntilItsOwnerRecoversIt(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrambleOwner"));
        Block bramble = WitcheryBlocks.getRequired("voidbramble").get();
        Block heart = WitcheryBlocks.getRequired("circle").get();
        BlockPos support = new BlockPos(8, 1, 5);
        helper.setBlock(support, Blocks.STONE);
        helper.setBlock(support.above(), Blocks.AIR);
        helper.setBlock(support.above(2), Blocks.AIR);
        BlockPos bramblePos = place(helper, owner, support, new ItemStack(bramble, 2));
        place(helper, owner, support.above(), owner.getMainHandItem());
        helper.setBlock(new BlockPos(4, 2, 5), heart);
        BlockPos center = helper.absolutePos(new BlockPos(4, 2, 5));
        Vec3 offerings = Vec3.atBottomCenterOf(center).add(0, 0.1, 0);
        var talisman = new ItemEntity(level, offerings.x(), offerings.y(), offerings.z(),
                new ItemStack(WitcheryItems.getRequired("circletalisman").get()));
        var redstone = new ItemEntity(level, offerings.x(), offerings.y(), offerings.z(), new ItemStack(Items.REDSTONE));
        level.addFreshEntity(talisman);
        level.addFreshEntity(redstone);

        helper.assertTrue(VoidBrambleBlockEntity.preventsRite(level, bramblePos.east(31))
                        && !VoidBrambleBlockEntity.preventsRite(level, bramblePos.east(32)),
                "The suppression field has a strict 32-block radius");
        var hit = new BlockHitResult(Vec3.atCenterOf(center), Direction.UP, center, false);
        level.getBlockState(center).use(level, owner, InteractionHand.MAIN_HAND, hit);
        helper.assertFalse(level.getBlockState(center).getValue(RiteHeartBlock.ACTIVE), "A bramble prevents the new rite");
        helper.assertTrue(talisman.isAlive() && redstone.isAlive(), "A blocked activation leaves its offerings alone");
        level.getBlockState(bramblePos).attack(level, bramblePos, helper.makeMockSurvivalPlayer());
        helper.assertTrue(level.getBlockState(bramblePos).is(bramble), "An unrelated player cannot reclaim the bramble");
        level.getBlockState(bramblePos).attack(level, bramblePos, owner);
        helper.assertTrue(level.isEmptyBlock(bramblePos) && level.isEmptyBlock(bramblePos.above()),
                "The owner reclaims the full column");
        helper.assertItemEntityCountIs(bramble.asItem(), support.above(), 3, 2);
        level.getBlockState(center).use(level, owner, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(level.getBlockState(center).getValue(RiteHeartBlock.ACTIVE),
                "The same offerings start the rite as soon as the bramble is gone");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void goddessCuresAVisitorAndRequiresItsOwnerToSneakForRecovery(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "GoddessOwner"));
        owner.setYRot(90);
        var visitor = helper.makeMockSurvivalPlayer();
        Block statue = WitcheryBlocks.getRequired("statuegoddess").get();
        BlockPos support = new BlockPos(5, 1, 4);
        helper.setBlock(support, Blocks.STONE);
        helper.setBlock(support.above(), Blocks.AIR);
        BlockPos pos = place(helper, owner, support, new ItemStack(statue));
        helper.assertTrue(level.getBlockState(pos).getValue(StatueGoddessBlock.FACING) == Direction.EAST,
                "The placed statue faces its owner");
        var ailments = List.of(MobEffects.POISON, MobEffects.WEAKNESS, MobEffects.BLINDNESS,
                MobEffects.DIG_SLOWDOWN, MobEffects.MOVEMENT_SLOWDOWN);
        ailments.forEach(effect -> visitor.addEffect(new MobEffectInstance(effect, 200)));
        visitor.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200));
        var tags = visitor.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        for (String key : List.of("witcheryCursed", "witcheryInsanity", "witcherySinking",
                "witcheryOverheating", "witcheryWakingNightmare")) tags.putInt(key, 3);
        visitor.getPersistentData().put(Player.PERSISTED_NBT_TAG, tags);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false);
        helper.assertTrue(level.getBlockState(pos).use(level, visitor, InteractionHand.MAIN_HAND, hit).consumesAction(),
                "A player can use another player's statue");
        for (DirectedCurse curse : DirectedCurse.values()) {
            helper.assertTrue(curse.strength(visitor) == 0, "The statue lifts " + curse);
        }
        helper.assertTrue(ailments.stream().noneMatch(visitor::hasEffect) && visitor.hasEffect(MobEffects.NIGHT_VISION),
                "The curing interaction clears the associated ailments and preserves unrelated effects");
        level.getBlockState(pos).attack(level, pos, owner);
        helper.assertTrue(level.getBlockState(pos).is(statue), "The owner must crouch to reclaim the statue");
        owner.setShiftKeyDown(true);
        level.getBlockState(pos).attack(level, pos, owner);
        helper.assertTrue(level.isEmptyBlock(pos), "A crouching owner can reclaim the statue");
        helper.assertItemEntityCountIs(statue.asItem(), support.above(), 3, 1);
        level.setBlockAndUpdate(pos, statue.defaultBlockState());
        var placeholder = (PlacedOwnerBlockEntity) level.getBlockEntity(pos);
        placeholder.recoverableFromEarlierPort();
        placeholder.load(placeholder.saveWithFullMetadata());
        visitor.setShiftKeyDown(true);
        level.getBlockState(pos).attack(level, pos, visitor);
        helper.assertTrue(level.isEmptyBlock(pos), "An earlier unowned placeholder can still be recovered after saving");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void bothRecipesHonorTheirOwnConfigAndOriginalIngredients(GameTestHelper helper) throws IOException {
        var goddess = WitcheryCommonConfig.ALLOW_GODDESS_STATUE_RECIPE;
        var bramble = WitcheryCommonConfig.ALLOW_VOID_BRAMBLE_RECIPE;
        boolean originalGoddess = goddess.get();
        boolean originalBramble = bramble.get();
        try {
            goddess.set(false);
            bramble.set(false);
            helper.assertTrue(recipe(helper, ProtectionRecipeCondition.GODDESS_STATUE) == null
                            && recipe(helper, ProtectionRecipeCondition.VOID_BRAMBLE) == null,
                    "Both recipes are disabled with their original default settings");
            goddess.set(true);
            ShapedRecipe statue = recipe(helper, ProtectionRecipeCondition.GODDESS_STATUE);
            helper.assertTrue(statue != null && recipe(helper, ProtectionRecipeCondition.VOID_BRAMBLE) == null,
                    "Enabling only the statue recipe leaves Void Bramble disabled");
            var grid = new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0) {
                @Override public boolean stillValid(Player player) { return true; }
                @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
            }, 3, 3);
            for (int slot : new int[]{0, 2, 3, 5}) grid.setItem(slot, new ItemStack(Items.NETHER_STAR));
            for (int slot : new int[]{1, 6, 7, 8}) grid.setItem(slot, new ItemStack(Items.STONE));
            grid.setItem(4, new ItemStack(WitcheryIngredient.DEMON_HEART.item()));
            helper.assertTrue(statue.matches(grid, helper.getLevel()), "The original statue inputs match the recipe");

            goddess.set(false);
            bramble.set(true);
            ShapedRecipe voidBramble = recipe(helper, ProtectionRecipeCondition.VOID_BRAMBLE);
            helper.assertTrue(voidBramble != null && recipe(helper, ProtectionRecipeCondition.GODDESS_STATUE) == null,
                    "Enabling only Void Bramble leaves the statue disabled");
            for (int slot : new int[]{0, 2, 6, 8}) grid.setItem(slot, new ItemStack(WitcheryIngredient.DROP_OF_LUCK.item()));
            for (int slot : new int[]{1, 7}) grid.setItem(slot, new ItemStack(WitcheryIngredient.MUTANDIS_EXTREMIS.item()));
            var center = new ItemStack(WitcheryBlocks.getRequired("bramble").get());
            grid.setItem(4, center);
            helper.assertFalse(voidBramble.matches(grid, helper.getLevel()), "Wild Bramble cannot substitute for Ender Bramble");
            BrambleBlock.setWild(center, false);
            helper.assertTrue(voidBramble.matches(grid, helper.getLevel())
                            && voidBramble.assemble(grid, helper.getLevel().registryAccess()).getCount() == 4,
                    "Ender Bramble completes the original four-block recipe");
        } finally {
            goddess.set(originalGoddess);
            bramble.set(originalBramble);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_protection_statue_mining")
    public static void survivalVisitorsCanMineStoneStatuesWithAPickaxe(GameTestHelper helper) {
        var level = helper.getLevel();
        var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "StatueOwner"));
        var visitor = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "StatueVisitor"));
        BlockPos support = new BlockPos(5, 1, 4);
        helper.setBlock(support, Blocks.STONE);
        helper.setBlock(support.above(), Blocks.AIR);
        var statues = List.of("statuegoddess", "decursedirected", "decurseteleport", "wolfaltar", "statueofworship");
        for (String id : statues) {
            Block statue = WitcheryBlocks.getRequired(id).get();
            BlockPos pos = place(helper, owner, support, new ItemStack(statue));
            BlockState state = level.getBlockState(pos);
            visitor.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            float bareHandProgress = state.getDestroyProgress(visitor, level, pos);
            float hardness = id.equals("statueofworship") ? 3.5F : 2.5F;
            helper.assertTrue(state.getDestroySpeed(level, pos) == hardness && bareHandProgress > 0,
                    id + " has the original finite stone hardness");
            helper.assertFalse(visitor.hasCorrectToolForDrops(state), id + " needs a pickaxe to drop an item");
            visitor.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
            helper.assertTrue(visitor.hasCorrectToolForDrops(state) && state.getDestroyProgress(visitor, level, pos) > bareHandProgress,
                    "A pickaxe harvests " + id + " at the original faster rate");
            helper.assertTrue(visitor.gameMode.destroyBlock(pos) && level.isEmptyBlock(pos), "A visitor can harvest " + id);
        }
        helper.succeedWhen(() -> {
            for (String id : statues) {
                helper.assertItemEntityCountIs(WitcheryBlocks.getRequired(id).get().asItem(), support.above(), 2, 1);
            }
        });
    }

    private static BlockPos place(GameTestHelper helper, Player player, BlockPos support, ItemStack stack) {
        BlockPos pos = helper.absolutePos(support);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
        ItemStack placed = stack.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.assertTrue(stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction()
                        && helper.getLevel().getBlockState(pos.above()).getBlock().asItem() == placed.getItem(),
                "Place " + placed.getHoverName().getString() + " above its support");
        return pos.above();
    }

    private static ShapedRecipe recipe(GameTestHelper helper, ProtectionRecipeCondition condition) throws IOException {
        ResourceLocation id = condition.recipeId();
        var path = id.withPath("recipes/" + id.getPath() + ".json");
        var resource = helper.getLevel().getServer().getResourceManager().getResourceOrThrow(path);
        try (var reader = resource.openAsReader()) {
            return (ShapedRecipe) RecipeManager.fromJson(id, JsonParser.parseReader(reader).getAsJsonObject(), ICondition.IContext.EMPTY);
        }
    }
}
