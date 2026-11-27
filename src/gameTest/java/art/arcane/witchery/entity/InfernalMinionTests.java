package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.infusion.CreaturePower;
import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class InfernalMinionTests {
    @GameTest(template = "qa_coven_arena")
    public static void ownerProtectionPreservesAnOrderedTarget(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "InfernalOrderQA"));
        var zombie = EntityType.ZOMBIE.create(level);
        var pig = EntityType.PIG.create(level);
        InfernalInfusion.bind(zombie, caster);
        zombie.setTarget(pig);
        zombie.setTarget(caster);
        helper.assertTrue(zombie.getTarget() == pig, "Rejecting an attempt to target the owner preserves the ordered victim");
        zombie.setTarget(null);
        zombie.setTarget(caster);
        helper.assertTrue(zombie.getTarget() == null, "An idle bound zombie also refuses its owner");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void grantsPowerBeforeAttemptingSacrificeDamage(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "InfernalSacrificeQA"));
        caster.setShiftKeyDown(true);
        InfusionEnergy.grant(caster, InfernalInfusion.ID, 20);
        var pig = EntityType.PIG.create(level);
        InfernalInfusion.bind(pig, caster);
        var hand = WitcheryItems.getRequired("witchhand").get().getDefaultInstance();
        var hits = new AtomicInteger();
        Consumer<LivingHurtEvent> cancel = event -> {
            if (event.getEntity() != pig) return;
            hits.incrementAndGet();
            helper.assertTrue(CreaturePower.forId(CreaturePower.activeId(caster)) == CreaturePower.PIG
                            && CreaturePower.charges(caster) == 1,
                    "The original creature power is granted before the sacrifice damage event");
            helper.assertTrue(event.getSource().getDirectEntity() == caster && event.getSource().getEntity() == null,
                    "Sacrifice uses the caster as the direct source with no credited attacker");
            event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(cancel);
        try {
            hand.getItem().onLeftClickEntity(hand, caster, pig);
            helper.assertTrue(hits.get() == 1 && pig.isAlive() && pig.getHealth() == 10
                            && caster.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG)
                            .getInt("witcheryInfusionCharges") == 19,
                    "The veto protects the creature and the original hand and creature charge changes persist");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancel);
            pig.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void splitVanillaCreaturesGrantTheirLegacyPowers(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "InfernalVariants"));
        caster.setShiftKeyDown(true);
        int energy = 80;
        InfusionEnergy.grant(caster, InfernalInfusion.ID, energy);
        var hand = WitcheryItems.getRequired("witchhand").get().getDefaultInstance();
        var variants = List.of(
                Map.entry(EntityType.WITHER_SKELETON, CreaturePower.SKELETON),
                Map.entry(EntityType.ZOMBIE_VILLAGER, CreaturePower.ZOMBIE),
                Map.entry(EntityType.CAT, CreaturePower.OCELOT),
                Map.entry(EntityType.DONKEY, CreaturePower.HORSE),
                Map.entry(EntityType.MULE, CreaturePower.HORSE),
                Map.entry(EntityType.ZOMBIE_HORSE, CreaturePower.HORSE),
                Map.entry(EntityType.SKELETON_HORSE, CreaturePower.HORSE));
        CreaturePower previous = null;
        var data = caster.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG);
        for (var variant : variants) {
            var creature = variant.getKey().create(level);
            var power = variant.getValue();
            hand.getItem().onLeftClickEntity(hand, caster, creature);
            helper.assertTrue(InfernalInfusion.owns(creature, caster)
                            && data.getInt("witcheryInfusionCharges") == energy - 5,
                    "The Infernal Hand binds " + creature.getType().toShortString() + " for five charges");
            hand.getItem().onLeftClickEntity(hand, caster, creature);
            energy -= 6;
            helper.assertTrue(CreaturePower.forId(CreaturePower.activeId(caster)) == power
                            && CreaturePower.charges(caster) == (previous == power ? 20 : 10)
                            && data.getInt("witcheryInfusionCharges") == energy && !creature.isAlive(),
                    "The sacrifice grants the original reserve and shares its cap across variants");
            creature.discard();
            previous = power;
        }
        for (var type : List.of(WitcheryEntities.WITCH_CAT.get(), WitcheryEntities.DEATH_HORSE.get(),
                WitcheryEntities.SPECTRAL_FAMILIAR.get(), EntityType.STRAY, EntityType.HUSK)) {
            helper.assertTrue(CreaturePower.forCreature(type) == null,
                    "Distinct species do not inherit the power from a related vanilla creature");
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void ignoresSideFacesAndRejectsEmptyMovementOrders(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "InfernalMovementQA"));
        var base = helper.absolutePos(new BlockPos(2, 0, 2)).atY(200);
        var start = Vec3.atBottomCenterOf(base);
        caster.moveTo(start.x, start.y, start.z, 0, 0);
        caster.setShiftKeyDown(true);
        level.setBlockAndUpdate(base.offset(0, 1, 5), Blocks.STONE.defaultBlockState());
        InfusionEnergy.grant(caster, InfernalInfusion.ID, 20);
        var hand = WitcheryItems.getRequired("witchhand").get().getDefaultInstance();
        var cues = new AtomicInteger();
        Consumer<PlayLevelSoundEvent.AtEntity> capture = event -> {
            if (event.getEntity() == caster) cues.incrementAndGet();
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            hand.getItem().releaseUsing(hand, level, caster, hand.getUseDuration() - 5);
            helper.assertTrue(cues.get() == 0, "Side faces stay quiet on the original movement command");
            caster.moveTo(start.x, level.getMaxBuildHeight() + 2, start.z, 0, -90);
            hand.getItem().releaseUsing(hand, level, caster, hand.getUseDuration() - 5);
            helper.assertTrue(cues.get() == 1 && caster.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG)
                            .getInt("witcheryInfusionCharges") == 20,
                    "An empty aim plays the rejection cue and movement commands leave the infusion reserve intact");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void infernalHandAndImperioRespectLegacyBossImmunity(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "InfernalMinionQA"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        InfusionEnergy.grant(player, InfernalInfusion.ID, 20);
        var hand = WitcheryItems.getRequired("witchhand").get().getDefaultInstance();
        try {
            var bosses = List.of(WitcheryEntities.BABA_YAGA, WitcheryEntities.LORD_OF_TORMENT, WitcheryEntities.DEATH,
                    WitcheryEntities.LILITH, WitcheryEntities.MOG, WitcheryEntities.GULG, WitcheryEntities.HORNED_HUNTSMAN,
                    WitcheryEntities.REFLECTION, WitcheryEntities.LEONARD);
            for (var type : bosses) {
                var boss = type.get().create(level);
                hand.getItem().onLeftClickEntity(hand, player, boss);
                InfernalInfusion.castImperio(player, boss);
                helper.assertTrue(!InfernalInfusion.owns(boss, player) && !boss.hasEffect(WitcheryEffects.ENSLAVED.get()),
                        type.getId() + " is immune to the Infernal Hand and Imperio");
                boss.discard();
            }
            helper.assertTrue(player.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG)
                    .getInt("witcheryInfusionCharges") == 20, "Attempting to bind bosses spends no charge");

            var pig = EntityType.PIG.create(level);
            hand.getItem().onLeftClickEntity(hand, player, pig);
            helper.assertTrue(InfernalInfusion.owns(pig, player) && pig.hasEffect(WitcheryEffects.ENSLAVED.get())
                    && player.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG)
                    .getInt("witcheryInfusionCharges") == 15, "An ordinary creature still binds for five charge");
            pig.discard();
            helper.succeed();
        } finally {
            player.discard();
        }
    }
}
