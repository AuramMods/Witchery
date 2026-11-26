package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewImpactCasterTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_impact_caster")
    public static void splashDamageCreditsThePlayerOrWorldFakePlayer(GameTestHelper helper) {
        var level = helper.getLevel();
        var target = helper.spawn(EntityType.PIG, new BlockPos(5, 5, 5));
        target.setNoAi(true);
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SplashCaster"));
        var fake = FakePlayerFactory.getMinecraft(level);
        var bottle = bottle(Items.ICE, Items.GUNPOWDER);
        var sources = new ArrayList<DamageSource>();
        Consumer<LivingAttackEvent> record = event -> {
            if (event.getEntity() == target) {
                sources.add(event.getSource());
                event.setCanceled(true);
            }
        };
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(target.getUUID()) == target,
                "The splash can find its target")).thenExecute(() -> {
            var shot = WitcheryEntities.THROWN_BREW.get().create(level);
            var creature = EntityType.PIG.create(level);
            MinecraftForge.EVENT_BUS.addListener(record);
            try {
                shot.setItem(bottle);
                shot.setPos(target.position());
                var direct = new EntityHitResult(target);
                var glancing = new BlockHitResult(target.position(), Direction.UP, target.blockPosition().below(), false);
                var item = (BrewBottleItem) bottle.getItem();
                item.impact(level, shot, direct);
                shot.setOwner(creature);
                item.impact(level, shot, glancing);
                shot.setOwner(caster);
                item.impact(level, shot, direct);
                var expected = List.of(fake, fake, caster);
                helper.assertTrue(sources.stream().map(DamageSource::getEntity).toList().equals(expected)
                                && sources.stream().map(DamageSource::getDirectEntity).toList().equals(expected),
                        "Direct and glancing Harm Demons credit the expected caster in both source fields");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(record);
                shot.discard();
                creature.discard();
                caster.discard();
                target.discard();
            }
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_fluid_caster")
    public static void bottledFluidsCheckProtectionAndKeepTheirDistinctContactCasters(GameTestHelper helper) {
        var level = helper.getLevel();
        var fake = FakePlayerFactory.getMinecraft(level);
        var victim = EntityType.PIG.create(level);
        var creature = EntityType.PIG.create(level);
        var target = helper.absolutePos(new BlockPos(4, 6, 4));
        var veto = target.east();
        var checked = new ArrayList<BlockEvent.BreakEvent>();
        var placed = new ArrayList<BlockEvent.EntityPlaceEvent>();
        var damage = new ArrayList<DamageSource>();
        Consumer<BlockEvent.BreakEvent> protect = event -> {
            if (event.getLevel() != level || !(event.getPos().equals(target) || event.getPos().equals(veto))) return;
            checked.add(event);
            if (event.getPos().equals(veto)) event.setCanceled(true);
        };
        Consumer<BlockEvent.EntityPlaceEvent> placement = event -> {
            if (event.getLevel() == level && event.getPos().equals(target)) placed.add(event);
        };
        Consumer<LivingAttackEvent> contact = event -> {
            if (event.getEntity() == victim) {
                damage.add(event.getSource());
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(protect);
        MinecraftForge.EVENT_BUS.addListener(placement);
        MinecraftForge.EVENT_BUS.addListener(contact);
        try {
            for (boolean gas : List.of(true, false)) {
                Item dispersal = (gas ? WitcheryIngredient.BAT_WOOL : WitcheryIngredient.WORMWOOD).item();
                ItemStack bottle = bottle(Items.PUMPKIN, dispersal);
                for (BlockPos cell : List.of(target, veto)) {
                    level.setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(cell.below(), Blocks.STONE.defaultBlockState());
                    var shot = WitcheryEntities.THROWN_BREW.get().create(level);
                    if (gas) shot.setOwner(creature);
                    shot.setItem(bottle);
                    shot.setPos(Vec3.atBottomCenterOf(cell).add(0, 0.6, 0));
                    shot.setDeltaMovement(0, -1.4, 0);
                    shot.tick();
                }
                helper.assertTrue(level.isEmptyBlock(veto) && level.getBlockEntity(veto) == null,
                        "The break veto prevents the fluid from occupying the cell");
                var fluid = (BrewFluidBlockEntity) level.getBlockEntity(target);
                var saved = fluid.saveWithFullMetadata();
                helper.assertTrue(saved.getUUID("ThrowerUUID").equals(fake.getUUID())
                                && saved.getString("Thrower").equals(fake.getScoreboardName()),
                        "The source cell saves the original caster identity");
                fluid.load(saved);
                fluid.expose(level, victim, gas);
                helper.assertTrue(damage.size() == 1 && damage.get(0).getEntity() == (gas ? null : fake)
                                && damage.get(0).getDirectEntity() == (gas ? null : fake),
                        "Gas contacts use a present player and liquid falls back to the world fake player");
                damage.clear();
            }
            helper.assertTrue(checked.size() == 4 && checked.stream().allMatch(event -> event.getPlayer() == fake)
                            && placed.size() == 2 && placed.stream().allMatch(event -> event.getEntity() == fake),
                    "Initial gas and liquid hits use the world fake player in both protection events");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protect);
            MinecraftForge.EVENT_BUS.unregister(placement);
            MinecraftForge.EVENT_BUS.unregister(contact);
            victim.discard();
            creature.discard();
        }
        helper.succeed();
    }

    private static ItemStack bottle(Item effect, Item dispersal) {
        var ingredients = List.of(WitcheryIngredient.MANDRAKE_ROOT.item().getDefaultInstance(),
                Items.NETHER_WART.getDefaultInstance(), WitcheryIngredient.TEAR_OF_THE_GODDESS.item().getDefaultInstance(),
                effect.getDefaultInstance(), dispersal.getDefaultInstance());
        var bottle = WitcheryItems.getRequired("brewbottle").get().getDefaultInstance();
        bottle.setTag(BrewMixture.from(ingredients).bottleTag(ingredients));
        return bottle;
    }
}
