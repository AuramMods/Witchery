package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.infusion.ImpContractEffects;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ImpContractTests {
    private static final String TEMPLATE = "qa_imp_contracts";
    private static final InteractionHand HAND = InteractionHand.MAIN_HAND;
    private static final BlockPos CENTER = new BlockPos(4, 2, 4);

    @GameTest(template = TEMPLATE)
    public static void craftsSignsAndImportsAllFiveContracts(GameTestHelper helper) {
        FakePlayer player = player(helper);
        var ownership = WitcheryIngredient.CONTRACT_OF_OWNERSHIP.item();
        assertRecipe(helper, player, WitcheryIngredient.CONTRACT_LIVING_FLAME, 142,
                ownership, Items.BLAZE_ROD, WitcheryIngredient.HINT_OF_REBIRTH.item());
        assertRecipe(helper, player, WitcheryIngredient.CONTRACT_FIERY_TOLERANCE, 143, ownership, Items.BLAZE_POWDER);
        assertRecipe(helper, player, WitcheryIngredient.CONTRACT_EVAPORATION, 144, ownership, Items.MAGMA_CREAM, Items.BLAZE_ROD);
        assertRecipe(helper, player, WitcheryIngredient.CONTRACT_FIERY_TOUCH, 145,
                ownership, WitcheryBlocks.getRequired("embermoss").get(), Items.BLAZE_ROD);
        assertRecipe(helper, player, WitcheryIngredient.CONTRACT_MELTING_TOUCH, 146, ownership, Items.LAVA_BUCKET);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void checksAffectionPowerTargetAndSharedCooldown(GameTestHelper helper) {
        FakePlayer player = player(helper);
        Imp imp = imp(helper, player);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, CENTER);
        ItemStack tolerance = signed(WitcheryIngredient.CONTRACT_FIERY_TOLERANCE, pig);
        setGiftState(imp, 19, -3_601);
        ItemStack unsigned = new ItemStack(WitcheryIngredient.CONTRACT_FIERY_TOLERANCE.item());
        offer(imp, player, unsigned);
        offer(imp, player, tolerance);
        helper.assertTrue(unsigned.getCount() == 1 && tolerance.getCount() == 1 && !pig.hasEffect(MobEffects.FIRE_RESISTANCE),
                "The Imp should need twenty affection");
        setGiftState(imp, 20, -3_601);
        offer(imp, player, new ItemStack(WitcheryIngredient.DEMON_HEART.item()));
        offer(imp, player, tolerance);
        helper.assertTrue(tolerance.getCount() == 1 && imp.isPowered(), "A powered Imp should decline the signed contract");
        offer(imp, player, new ItemStack(WitcheryIngredient.ICY_NEEDLE.item()));
        ItemStack wrongSpecies = signed(WitcheryIngredient.CONTRACT_EVAPORATION, pig);
        offer(imp, player, wrongSpecies);
        helper.assertTrue(wrongSpecies.getCount() == 1, "A player-only spell should leave a creature's contract intact");
        ItemStack missing = new ItemStack(WitcheryIngredient.CONTRACT_FIERY_TOLERANCE.item());
        missing.getOrCreateTag().putString("WITCPlayer1", "OfflineContractTest");
        offer(imp, player, missing);
        helper.assertTrue(missing.getCount() == 1, "An offline target should leave the contract intact");
        long beforeCast = System.currentTimeMillis() / 50;
        offer(imp, player, tolerance);
        var active = pig.getEffect(MobEffects.FIRE_RESISTANCE);
        helper.assertTrue(tolerance.isEmpty() && active != null && active.getDuration() == 18_000,
                "Fiery Tolerance should apply fifteen minutes of Fire Resistance to a creature");
        CompoundTag saved = imp.saveWithoutId(new CompoundTag());
        helper.assertTrue(saved.getInt("Affection") == 20 && saved.getLong("LastGiftTime") >= beforeCast
                        && saved.getLong("LastGiftTime") <= System.currentTimeMillis() / 50,
                "Casting should preserve affection and start the gift cooldown");
        imp.load(saved);
        ItemStack gift = new ItemStack(Items.DIAMOND_BLOCK);
        offer(imp, player, gift);
        ItemStack second = signed(WitcheryIngredient.CONTRACT_FIERY_TOLERANCE, pig);
        offer(imp, player, second);
        helper.assertTrue(gift.isEmpty() && second.getCount() == 1
                        && imp.saveWithoutId(new CompoundTag()).getInt("SecretsShared") == saved.getInt("SecretsShared"),
                "Reloading should keep the same cooldown for spells and gift rewards");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_imp_timer_migration")
    public static void impKeepsTheOriginalPowerAndGiftTimersAcrossSaveFormats(GameTestHelper helper) {
        FakePlayer player = player(helper);
        Imp imp = imp(helper, player);
        long now = System.currentTimeMillis() / 50;
        var legacy = imp.saveWithoutId(new CompoundTag());
        legacy.putInt("Affection", 30);
        legacy.putInt("SecretsShared", 1);
        legacy.putLong("LastGiftTime", now - 800);
        legacy.putLong("PowerUpUntil2", now + 12_000);
        imp.load(legacy);
        var active = imp.saveWithoutId(new CompoundTag());
        helper.assertTrue(imp.isPowered() && active.getLong("PowerUpUntil2") == now + 12_000,
                "An original Imp keeps the future power deadline exactly");

        var gift = new ItemStack(Items.DIAMOND_BLOCK);
        offer(imp, player, gift);
        active = imp.saveWithoutId(new CompoundTag());
        helper.assertTrue(gift.isEmpty() && active.getInt("SecretsShared") == 1 && active.getLong("LastGiftTime") == now - 800,
                "An original gift still blocks rewards during the remaining cooldown");

        active.putLong("PowerUpUntil2", now - 100);
        imp.load(active);
        imp.aiStep();
        helper.assertTrue(!imp.isPowered() && imp.saveWithoutId(new CompoundTag()).getLong("PowerUpUntil2") == 0,
                "Power that expired while offline is cleared on the next loaded tick");

        long world = helper.getLevel().getGameTime();
        long priorGift = Math.max(1, world - 200);
        active.putLong("LastGiftTime", priorGift);
        active.putLong("PowerUpUntil2", world + 1_200);
        long beforeLoad = System.currentTimeMillis() / 50;
        imp.load(active);
        var normalized = imp.saveWithoutId(new CompoundTag());
        long afterLoad = System.currentTimeMillis() / 50;
        helper.assertTrue(imp.isPowered() && normalized.getLong("PowerUpUntil2") >= beforeLoad + 1_200
                        && normalized.getLong("PowerUpUntil2") <= afterLoad + 1_200
                        && normalized.getLong("LastGiftTime") >= beforeLoad + priorGift - world
                        && normalized.getLong("LastGiftTime") <= afterLoad + priorGift - world,
                "The previous port's deadlines preserve their offsets when converted");
        imp.load(normalized);
        var reloaded = imp.saveWithoutId(new CompoundTag());
        helper.assertTrue(normalized.getLong("PowerUpUntil2") == reloaded.getLong("PowerUpUntil2")
                        && normalized.getLong("LastGiftTime") == reloaded.getLong("LastGiftTime"),
                "Reloading keeps the normalized timestamps");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void livingFlameSummonsTheOriginalBlaze(GameTestHelper helper) {
        FakePlayer player = player(helper);
        Imp imp = imp(helper, player);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, CENTER);
        ItemStack stack = signed(WitcheryIngredient.CONTRACT_LIVING_FLAME, pig);
        offer(imp, player, stack);
        var blazes = helper.getLevel().getEntitiesOfClass(Blaze.class, new AABB(pig.blockPosition()).inflate(3));
        helper.assertTrue(stack.isEmpty() && blazes.size() == 1, "A signed Living Flame should summon one Blaze by its subject");
        Blaze blaze = blazes.get(0);
        helper.assertTrue(blaze.getMaxHealth() == 50 && blaze.getAttributeValue(Attributes.ATTACK_DAMAGE) == 7,
                "The Blaze should have the original boosted attributes");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 1_010)
    public static void castsPlayerEffectsAndCuresThemWithFrozenHeart(GameTestHelper helper) {
        FakePlayer player = player(helper);
        Imp imp = imp(helper, player);
        castOnPlayer(helper, imp, player, WitcheryIngredient.CONTRACT_FIERY_TOUCH, ImpContractEffects.FIERY_TOUCH);
        castOnPlayer(helper, imp, player, WitcheryIngredient.CONTRACT_EVAPORATION, ImpContractEffects.EVAPORATION);
        castOnPlayer(helper, imp, player, WitcheryIngredient.CONTRACT_MELTING_TOUCH, ImpContractEffects.MELTING_TOUCH);
        var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        var effects = persisted.getCompound("witchery.effects");
        helper.assertTrue(effects.getInt("witchery.im.meltingtouch") == 12_000, "The original key should hold ten minutes");
        effects.putBoolean("Unrelated", true);
        var level = helper.getLevel();
        BlockPos stone = helper.absolutePos(CENTER.east(2));
        level.setBlockAndUpdate(stone, Blocks.IRON_ORE.defaultBlockState());
        player.setItemInHand(HAND, new ItemStack(Items.IRON_PICKAXE));
        helper.assertTrue(player.gameMode.destroyBlock(stone), "The survival player should mine its ore");
        var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(stone).inflate(1));
        helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().is(Items.IRON_INGOT),
                "Melting Touch should smelt actual harvested ore");

        BlockPos clicked = helper.absolutePos(CENTER.east());
        level.setBlockAndUpdate(clicked, Blocks.STONE.defaultBlockState());
        var hit = new BlockHitResult(Vec3.atCenterOf(clicked).add(0, 0.5, 0), Direction.UP, clicked, false);
        var ignition = clicked.above();
        var placements = new ArrayList<BlockEvent.EntityPlaceEvent>();
        var sounds = new ArrayList<PlayLevelSoundEvent.AtPosition>();
        Consumer<BlockEvent.EntityPlaceEvent> protection = event -> {
            if (event.getLevel() == level && event.getEntity() == player && event.getPos().equals(ignition)
                    && event.getPlacedBlock().is(Blocks.FIRE)) {
                placements.add(event);
                if (placements.size() == 1) event.setCanceled(true);
            }
        };
        Consumer<PlayLevelSoundEvent.AtPosition> listen = event -> {
            if (event.getLevel() == level && BlockPos.containing(event.getPosition()).equals(ignition)
                    && event.getSound() != null && event.getSound().value().equals(SoundEvents.FIRE_AMBIENT)) sounds.add(event);
        };
        player.setItemInHand(HAND, ItemStack.EMPTY);
        MinecraftForge.EVENT_BUS.addListener(protection);
        MinecraftForge.EVENT_BUS.addListener(listen);
        try {
            player.getRandom().setSeed(5120);
            player.gameMode.useItemOn(player, level, ItemStack.EMPTY, HAND, hit);
            helper.assertTrue(placements.size() == 1 && placements.get(0).getBlockSnapshot().getReplacedBlock().isAir()
                            && level.isEmptyBlock(ignition) && !level.getBlockTicks().hasScheduledTick(ignition, Blocks.FIRE)
                            && sounds.isEmpty(),
                    "A veto on Fiery Touch's proposed fire keeps the cell empty and suppresses its cue and tick");
            player.getRandom().setSeed(5120);
            player.gameMode.useItemOn(player, level, ItemStack.EMPTY, HAND, hit);
            helper.assertTrue(placements.size() == 2 && level.getBlockState(ignition).is(Blocks.FIRE)
                            && level.getBlockTicks().hasScheduledTick(ignition, Blocks.FIRE) && sounds.size() == 1,
                    "An approved Fiery Touch starts fire at the clicked face and plays its cue once");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protection);
            MinecraftForge.EVENT_BUS.unregister(listen);
        }

        BlockPos evaporationCenter = helper.absolutePos(CENTER.above(7));
        player.setPos(Vec3.atBottomCenterOf(evaporationCenter));
        BlockPos water = evaporationCenter.west();
        level.setBlockAndUpdate(water.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(water.above(2), Blocks.AIR.defaultBlockState());
        for (int height = 0; height <= 1; height++) {
            BlockPos layer = water.above(height);
            for (Direction side : Direction.Plane.HORIZONTAL) level.setBlockAndUpdate(layer.relative(side), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(layer, Blocks.WATER.defaultBlockState());
        }
        BlockPos covered = evaporationCenter.south();
        level.setBlockAndUpdate(covered.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(covered.above(), Blocks.STONE.defaultBlockState());
        for (Direction side : Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(covered.relative(side), Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(covered, Blocks.WATER.defaultBlockState());
        helper.onEachTick(() -> {
            if (level.getGameTime() % 20 == 3) player.getRandom().setSeed(0);
            MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
        });
        helper.succeedWhen(() -> {
            int remaining = effects.getInt("witchery.imp.evaporation");
            helper.assertTrue(remaining < 12_000, "The saved duration should elapse while active");
            helper.assertTrue(level.isEmptyBlock(water) && level.isEmptyBlock(water.above()),
                    "Evaporation should remove exposed water from the top down");
            helper.assertTrue(level.getBlockState(covered).is(Blocks.WATER), "Evaporation should leave the covered source");
            ItemStack heart = new ItemStack(WitcheryIngredient.FROZEN_HEART.item());
            heart.finishUsingItem(level, player);
            helper.assertTrue(heart.isEmpty() && effects.size() == 1 && effects.getBoolean("Unrelated"),
                    "A Frozen Heart should clear all three effects and preserve unrelated data");
        });
    }

    private static void assertRecipe(GameTestHelper helper, FakePlayer player, WitcheryIngredient contract, int damage,
                                     ItemLike... ingredients) {
        CraftingContainer grid = craftingGrid();
        for (int index = 0; index < ingredients.length; index++) grid.setItem(index, new ItemStack(ingredients[index]));
        var level = helper.getLevel();
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).orElseThrow();
        ItemStack unsigned = recipe.assemble(grid, level.registryAccess());
        helper.assertTrue(unsigned.is(contract.item()) && !TaglockItem.isBound(unsigned), "The original recipe should craft " + contract.path());
        if (ingredients[1] == Items.LAVA_BUCKET) {
            helper.assertTrue(recipe.getRemainingItems(grid).get(1).is(Items.BUCKET), "Melting Touch should return the empty bucket");
        }

        ItemStack taglock = new ItemStack(WitcheryItems.getRequired("taglockkit").get());
        TaglockItem.bindSignature(taglock, player);
        grid.clearContent();
        grid.setItem(0, unsigned);
        grid.setItem(1, taglock);
        var signing = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).orElseThrow();
        ItemStack signed = signing.assemble(grid, level.registryAccess());
        helper.assertTrue(signed.is(contract.item()) && TaglockItem.isBoundTo(signed, player), "Signing should preserve the contract and Taglock");

        var legacy = new CompoundTag();
        legacy.putString("id", "witchery:ingredient");
        legacy.putByte("Count", (byte) 1);
        legacy.putShort("Damage", (short) damage);
        legacy.put("tag", signed.getOrCreateTag().copy());
        ItemStack imported = WitcheryIngredient.loadInventoryStack(legacy);
        helper.assertTrue(imported.is(contract.item()) && TaglockItem.isBoundTo(imported, player),
                "The legacy ingredient subtype should keep its signer on import");
    }

    private static FakePlayer player(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ImpContractQA"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(CENTER)));
        return player;
    }

    private static Imp imp(GameTestHelper helper, FakePlayer owner) {
        Imp imp = helper.spawnWithNoFreeWill(WitcheryEntities.IMP.get(), CENTER.north());
        imp.tame(owner);
        setGiftState(imp, 20, -3_601);
        return imp;
    }

    private static void setGiftState(Imp imp, int affection, long lastGiftTime) {
        CompoundTag saved = imp.saveWithoutId(new CompoundTag());
        saved.putInt("Affection", affection);
        saved.putLong("LastGiftTime", lastGiftTime);
        imp.load(saved);
    }

    private static void castOnPlayer(GameTestHelper helper, Imp imp, FakePlayer player, WitcheryIngredient contract,
                                     ImpContractEffects effect) {
        setGiftState(imp, 20, -3_601);
        ItemStack item = signed(contract, player);
        offer(imp, player, item);
        helper.assertTrue(item.isEmpty() && effect.isActive(player), contract.path() + " should affect the signed player");
    }

    private static void offer(Imp imp, FakePlayer player, ItemStack item) {
        player.setItemInHand(HAND, item);
        imp.interact(player, HAND);
    }

    private static ItemStack signed(WitcheryIngredient contract, LivingEntity target) {
        var item = new ItemStack(contract.item());
        TaglockItem.bindSignature(item, target);
        return item;
    }

    private static CraftingContainer craftingGrid() {
        return new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0) {
            @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
            @Override public boolean stillValid(Player player) { return true; }
        }, 3, 3);
    }
}
