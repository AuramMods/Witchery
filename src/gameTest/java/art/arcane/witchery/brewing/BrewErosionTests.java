package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewErosionTests {
    private static final BlockPos CENTER = new BlockPos(5, 5, 5);
    private static final String BATCH = "witchery_brew_erosion";

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void fixedErosionPreservesWaterAndClearsTheOriginalLayers(GameTestHelper helper) {
        var level = helper.getLevel();
        var brew = brew(helper, GameType.SURVIVAL);
        var struck = helper.absolutePos(CENTER);
        for (BlockPos pos : BlockPos.betweenClosed(CENTER.offset(-2, -2, -2), CENTER.offset(2, 2, 2))) {
            helper.setBlock(pos, Blocks.STONE);
        }
        var warded = CENTER.west();
        var slab = CENTER.east();
        var fluid = CENTER.north();
        var chest = CENTER.south();
        helper.setBlock(CENTER, Blocks.OBSIDIAN);
        helper.setBlock(CENTER.above(), Blocks.OBSIDIAN);
        helper.setBlock(warded, Blocks.OBSIDIAN);
        helper.setBlock(slab, Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED, true));
        helper.setBlock(fluid, WitcheryBlocks.getRequired("hollowtears").get());
        helper.setBlock(chest, Blocks.CHEST);
        Consumer<BlockEvent.BreakEvent> cancel = event -> {
            if (event.getPlayer() == brew.getOwner() && event.getPos().equals(helper.absolutePos(warded))) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(cancel);
        try {
            impact(brew, new BlockHitResult(Vec3.atCenterOf(struck), Direction.NORTH, struck, false));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancel);
        }

        String[] layers = {
                "0000000000001000000000000",
                "0000000100011100010000000",
                "0111011111111111111101110",
                "0000000100011100010000000",
                "0000000000001000000000000"
        };
        for (int y = -2; y <= 2; y++) {
            for (int z = -2; z <= 2; z++) {
                for (int x = -2; x <= 2; x++) {
                    var pos = CENTER.offset(x, y, z);
                    if (pos.equals(warded) || pos.equals(slab) || pos.equals(chest)) continue;
                    boolean removed = layers[y + 2].charAt((z + 2) * 5 + x + 2) == '1';
                    helper.assertBlockPresent(removed ? Blocks.AIR : Blocks.STONE, pos);
                }
            }
        }
        helper.assertBlockPresent(Blocks.OBSIDIAN, warded);
        helper.assertBlockPresent(Blocks.WATER, slab);
        helper.assertBlockPresent(Blocks.CHEST, chest);
        helper.succeedWhen(() -> {
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(struck).inflate(3),
                    item -> item.getItem().is(Items.OBSIDIAN));
            helper.assertTrue(drops.size() == 1 && drops.get(0).getItem().getCount() == 2,
                    "Erosion recovers only the two obsidian blocks it removed");
            drops.forEach(ItemEntity::discard);
        });
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void fixedErosionUsesThrownDamageAndLegacyDurabilityThreshold(GameTestHelper helper) {
        var brew = brew(helper, GameType.SURVIVAL);
        var target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, CENTER);
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.setDamageValue(sword.getMaxDamage() - 100);
        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        ItemStack offhand = new ItemStack(Items.IRON_PICKAXE);
        target.setItemSlot(EquipmentSlot.MAINHAND, sword);
        target.setItemSlot(EquipmentSlot.HEAD, helmet);
        target.setItemSlot(EquipmentSlot.OFFHAND, offhand);
        float expected = target.getHealth() - CombatRules.getDamageAfterAbsorb(8, target.getArmorValue(), 0);

        impact(brew, new EntityHitResult(target));
        var source = target.getLastDamageSource();
        helper.assertTrue(source != null && source.is(DamageTypes.THROWN) && source.getEntity() == brew.getOwner(),
                "Acid damage is thrown damage attributed to the caster");
        helper.assertTrue(Math.abs(target.getHealth() - expected) < 0.01f, "Vanilla armor mitigates the acid hit");
        helper.assertTrue(target.getMainHandItem() == sword && sword.getDamageValue() == sword.getMaxDamage(),
                "A tool at exactly its maximum legacy damage remains equipped");
        helper.assertTrue(helmet.getDamageValue() == 100 && offhand.getDamageValue() == 0,
                "The original equipment slots corrode and the modern offhand is skipped");

        impact(brew, new EntityHitResult(target));
        helper.assertTrue(target.getMainHandItem().isEmpty() && target.getItemBySlot(EquipmentSlot.HEAD).isEmpty(),
                "Corrosion removes equipment after crossing the damage threshold");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void fixedErosionUsesTheThrowersCreativeStateForCorrosion(GameTestHelper helper) {
        var target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, CENTER);
        var sword = new ItemStack(Items.IRON_SWORD);
        sword.setDamageValue(5);
        target.setItemSlot(EquipmentSlot.MAINHAND, sword);
        target.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));

        impact(brew(helper, GameType.CREATIVE), new EntityHitResult(target));
        helper.assertTrue(sword.getDamageValue() == 5, "The creative caster applies no durability wear");
        helper.assertTrue(target.getItemBySlot(EquipmentSlot.FEET).isEmpty(),
                "The original zero-damage result clears the pristine boots");
        target.discard();
        helper.succeed();
    }

    private static ThrownBrew brew(GameTestHelper helper, GameType mode) {
        var actor = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ErosionFixture"));
        actor.setGameMode(mode);
        var brew = new ThrownBrew(actor, helper.getLevel());
        brew.setItem(WitcheryIngredient.BREW_OF_EROSION.item().getDefaultInstance());
        brew.setPos(Vec3.atCenterOf(helper.absolutePos(CENTER.north())));
        return brew;
    }

    private static void impact(ThrownBrew brew, net.minecraft.world.phys.HitResult hit) {
        ((ThrowableBrewItem) brew.getItem().getItem()).impact(brew, hit);
    }
}
