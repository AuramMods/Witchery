package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.infusion.SpellKnowledge;
import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.item.LeonardsUrnItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SymbolSpellTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_spell_impact_center")
    public static void areaSpellsUseTheProjectilePositionBeforeCollision(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SpellAreaCaster"));
        caster.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 3, 5))));
        var rearPig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(6, 3, 5));
        var frontPig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(10, 3, 5));
        var huntsman = helper.spawnWithNoFreeWill(WitcheryEntities.HORNED_HUNTSMAN.get(), new BlockPos(7, 3, 6));
        var start = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(7, 3, 5)));
        var end = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(9, 3, 5)));
        var hit = new BlockHitResult(end, Direction.WEST, BlockPos.containing(end), false);
        var creatures = List.of(rearPig, frontPig, huntsman);
        var rearGem = new ItemEntity(level, start.x - 0.55, start.y, start.z, new ItemStack(Items.EMERALD));
        var frontGem = new ItemEntity(level, end.x + 0.55, end.y, end.z, new ItemStack(Items.DIAMOND));
        var items = List.of(rearGem, frontGem);
        items.forEach(item -> {
            item.setNoGravity(true);
            item.setDeltaMovement(Vec3.ZERO);
            level.addFreshEntity(item);
        });

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                creatures.stream().allMatch(entity -> level.getEntity(entity.getUUID()) == entity)
                        && items.stream().allMatch(entity -> level.getEntity(entity.getUUID()) == entity),
                "The area-spell targets are loaded"))
                .thenExecute(() -> {
                    try {
                        var pull = new SymbolProjectile(level, caster, SymbolSpell.ATTRAHO, start, new Vec3(1, 0, 0));
                        pull.onHit(hit);
                        helper.assertTrue(rearPig.hasEffect(MobEffects.JUMP) && rearPig.getDeltaMovement().x < 0
                                        && !frontPig.hasEffect(MobEffects.JUMP) && !huntsman.hasEffect(MobEffects.JUMP),
                                "Attraho pulls the pig behind the collision and spares the nearby Horned Huntsman");
                        var confusion = new SymbolProjectile(level, caster, SymbolSpell.CONFUNDUS, 2);
                        confusion.setPos(start);
                        confusion.onHit(hit);
                        helper.assertTrue(rearPig.hasEffect(MobEffects.CONFUSION) && !frontPig.hasEffect(MobEffects.CONFUSION),
                                "Enhanced Confundus uses the same center for its two-block radius");
                        var retrieval = new SymbolProjectile(level, caster, SymbolSpell.ACCIO, start, new Vec3(1, 0, 0));
                        retrieval.onHit(hit);
                        helper.assertTrue(rearGem.position().equals(caster.position().add(0, 1, 0))
                                        && frontGem.position().distanceToSqr(end) < 0.64,
                                "Accio retrieves the emerald near the projectile and leaves the diamond near the collision");
                    } finally {
                        caster.discard();
                        creatures.forEach(entity -> entity.discard());
                        items.forEach(entity -> entity.discard());
                    }
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void rankedProjectilesRecoverTheirFlightAndReinforcementCannotBeDeflected(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ProjectileCaster"));
        var defender = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ProjectileDefender"));
        caster.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(7, 16, 5))));
        caster.setYRot(180);
        defender.setItemInHand(InteractionHand.MAIN_HAND, WitcheryItems.getRequired("mysticbranch").get().getDefaultInstance());

        try {
            for (var spell : List.of(SymbolSpell.ENNERVATE, SymbolSpell.STUPEFY)) {
                var base = new SymbolProjectile(level, caster, spell, 1);
                var thrown = new SymbolProjectile(level, caster, spell, 2);
                var restored = WitcheryEntities.SYMBOL_PROJECTILE.get().create(level);
                restored.load(thrown.saveWithoutId(new CompoundTag()));
                double startY = restored.getY();
                for (int tick = 0; tick < 8; tick++) {
                    base.tick();
                    restored.tick();
                }
                helper.assertTrue(base.getY() < startY - 0.9 && Math.abs(restored.getY() - startY) < 1.0E-6
                                && restored.getZ() < base.getZ() - 1,
                        spell + " falls at level one and flies straight at the restored enhanced rank");
            }

            var reinforcement = new SymbolProjectile(level, caster, SymbolSpell.CAVE_INIMICUM);
            var other = new SymbolProjectile(level, caster, SymbolSpell.STUPEFY);
            var hit = defender.damageSources().playerAttack(defender);
            helper.assertTrue(!reinforcement.hurt(hit, 1) && reinforcement.getOwner() == caster,
                    "Cave Inimicum keeps its caster when hit with a Mystic Branch");
            helper.assertTrue(other.hurt(hit, 1) && other.getOwner() == defender,
                    "The Branch still deflects other Mystic Branch projectiles");
            helper.succeed();
        } finally {
            caster.discard();
            defender.discard();
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void morsmordreResumesItsCountdownAtTheOriginalImpactCenter(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 16, 4)));
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "MorsmordreCaster"));
        var original = new CompoundTag();
        original.putInt("EffectID", SymbolSpell.MORSMORDRE.id());
        original.putInt("effectLevel", 1);
        original.putInt("lifetime", 2);
        var direction = new ListTag();
        direction.add(DoubleTag.valueOf(1));
        direction.add(DoubleTag.valueOf(0));
        direction.add(DoubleTag.valueOf(0));
        original.put("direction", direction);

        var fresh = new SymbolProjectile(level, caster, SymbolSpell.MORSMORDRE, origin.add(0, 0, 3), new Vec3(1, 0, 0));
        for (int tick = 0; tick < 6; tick++) fresh.tick();
        var modern = fresh.saveWithoutId(new CompoundTag());
        var previousPort = modern.copy();
        previousPort.remove("lifetime");
        previousPort.remove("CurrentAcceleration");
        var priorOrigin = fresh.position().add(0, 0, 3);

        var marks = new ArrayList<DarkMark>();
        Consumer<EntityJoinLevelEvent> recordMark = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof DarkMark mark) marks.add(mark);
        };
        MinecraftForge.EVENT_BUS.addListener(recordMark);
        try {
            var imported = WitcheryEntities.SYMBOL_PROJECTILE.get().create(level);
            imported.load(original);
            imported.setPos(origin);
            var restored = WitcheryEntities.SYMBOL_PROJECTILE.get().create(level);
            restored.load(modern);
            var previous = WitcheryEntities.SYMBOL_PROJECTILE.get().create(level);
            previous.load(previousPort);
            previous.setPos(priorOrigin);
            var bolts = List.of(imported, restored, previous);
            bolts.forEach(SymbolProjectile::tick);
            helper.assertTrue(marks.isEmpty() && bolts.stream().noneMatch(SymbolProjectile::isRemoved)
                            && imported.position().distanceTo(origin.add(1, 0, 0)) < 1.0E-6,
                    "All three saves have one tick left and the original direction supplies the imported motion");
            for (int index = 0; index < bolts.size(); index++) {
                var bolt = bolts.get(index);
                Vec3 lastPosition = bolt.position();
                bolt.tick();
                helper.assertTrue(bolt.isRemoved() && marks.size() == index + 1
                                && marks.get(index).position().distanceTo(lastPosition) < 1.0E-6,
                        "Morsmordre places the Dark Mark before its last movement step");
            }
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(recordMark);
            marks.forEach(DarkMark::discard);
            caster.discard();
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void lateDeflectionRestartsFallingSpellAcceleration(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 16, 4)));
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "FallingSpellCaster"));
        var defender = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "FallingSpellDefender"));
        defender.setYRot(-90);
        defender.setItemInHand(InteractionHand.MAIN_HAND, WitcheryItems.getRequired("mysticbranch").get().getDefaultInstance());

        try {
            var straight = new Vec3(1, 0, 0);
            var old = new SymbolProjectile(level, caster, SymbolSpell.ENNERVATE, origin, straight);
            for (int tick = 0; tick < 10; tick++) old.tick();

            var previousPort = old.saveWithoutId(new CompoundTag());
            previousPort.remove("CurrentAcceleration");
            previousPort.putFloat("AccelerationX", 0.3F);
            var restored = WitcheryEntities.SYMBOL_PROJECTILE.get().create(level);
            restored.load(previousPort);
            old.tick();
            restored.tick();
            helper.assertTrue(restored.getDeltaMovement().distanceTo(old.getDeltaMovement()) < 1.0E-6,
                    "Earlier port saves convert launch acceleration to the current falling acceleration");

            helper.assertTrue(restored.hurt(defender.damageSources().playerAttack(defender), 1),
                    "The Mystic Branch deflects the older bolt");
            restored.tick();
            Vec3 firstMotion = defender.getLookAngle().scale(1.08).add(0, -0.05, 0);
            helper.assertTrue(restored.getDeltaMovement().distanceTo(firstMotion) < 1.0E-6,
                    "Deflection starts a new acceleration even after eleven flight ticks");

            var reloaded = WitcheryEntities.SYMBOL_PROJECTILE.get().create(level);
            reloaded.load(restored.saveWithoutId(new CompoundTag()));
            restored.tick();
            reloaded.tick();
            helper.assertTrue(reloaded.getDeltaMovement().distanceTo(restored.getDeltaMovement()) < 1.0E-6
                            && reloaded.getDeltaMovement().distanceTo(firstMotion.add(defender.getLookAngle().scale(0.064))
                            .add(0, -0.05, 0)) < 1.0E-6,
                    "A saved deflected spell resumes the next decay step without applying the previous flight age");
            helper.succeed();
        } finally {
            caster.discard();
            defender.discard();
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void mobSpellsUseTheirOriginalLaunchGeometry(GameTestHelper helper) {
        var level = helper.getLevel();
        Vec3 origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 10, 4)));
        var target = EntityType.PIG.create(level);
        target.setPos(origin.add(8, 2, 0));
        var leonard = WitcheryEntities.LEONARD.get().create(level);
        var lilith = WitcheryEntities.LILITH.get().create(level);
        var reflection = WitcheryEntities.REFLECTION.get().create(level);
        reflection.setItemInHand(InteractionHand.MAIN_HAND, WitcheryItems.getRequired("mysticbranch").get().getDefaultInstance());

        var shots = new ArrayList<Projectile>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof Projectile shot) shots.add(shot);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            for (Mob caster : List.of(leonard, lilith, reflection)) {
                caster.setPos(origin);
                caster.setYRot(-90);
                caster.setXRot(-35);
                caster.getRandom().setSeed(28);
                for (int attack = 0; attack < 24; attack++) ((RangedAttackMob) caster).performRangedAttack(target, 0.7F);

                var spells = shots.stream().filter(SymbolProjectile.class::isInstance).map(SymbolProjectile.class::cast).toList();
                var fireballs = shots.stream().filter(LargeFireball.class::isInstance).toList();
                helper.assertTrue(spells.size() > 1 && (caster == lilith ? !fireballs.isEmpty() : fireballs.isEmpty()),
                        caster.getName().getString() + " uses its original ranged projectile types");
                Vec3 spellOrigin = new Vec3(caster.getX(), caster.getY(0.5), caster.getZ());
                for (var spell : spells) {
                    helper.assertTrue(spell.getOwner() == caster && spell.position().distanceTo(spellOrigin) < 1.0E-6
                                    && spell.getDeltaMovement().lengthSqr() == 0
                                    && Math.abs(savedAcceleration(spell).length() - 0.1) < 1.0E-6,
                            "The spell starts at the caster's midpoint with zero velocity and 0.1 acceleration");
                }
                helper.assertTrue(spells.stream().map(spell -> savedAcceleration(spell).y).distinct().count() > 1,
                        "The original spread also varies the vertical aim");
                Vec3 look = caster.getLookAngle();
                Vec3 ballOrigin = spellOrigin.add(look.x, 0.5, look.z);
                for (var ball : fireballs) {
                    helper.assertTrue(ball.getOwner() == caster && ball.position().distanceTo(ballOrigin) < 1.0E-6,
                            "Lilith's fireball offsets forward horizontally even when she looks up");
                }
                shots.forEach(Projectile::discard);
                shots.clear();
            }
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            shots.forEach(Projectile::discard);
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void tormentLordCastsIgnianimaAndBothLegacyVolleySizes(GameTestHelper helper) {
        var level = helper.getLevel();
        Vec3 origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 10, 4)));
        var target = EntityType.PIG.create(level);
        target.setPos(origin.add(1.5, 0, 0));
        var lord = WitcheryEntities.LORD_OF_TORMENT.get().create(level);
        lord.setPos(origin);
        Vec3 center = new Vec3(origin.x, lord.getY(0.5), origin.z);
        var shots = new ArrayList<Projectile>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof Projectile shot) shots.add(shot);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            for (long seed : new long[]{0, 1}) {
                lord.getRandom().setSeed(seed);
                lord.performRangedAttack(target, 0.125F);
                int count = RandomSource.create(seed).nextInt(10) == 0 ? 9 : 3;
                helper.assertTrue(shots.size() == count + 1
                                && shots.get(0) instanceof SymbolProjectile spell && spell.spell() == SymbolSpell.IGNIANIMA
                                && spell.getOwner() == lord && spell.position().distanceTo(center) < 1.0E-6
                                && Math.abs(savedAcceleration(spell).length() - 0.1) < 1.0E-6,
                        "Even at close range the Lord launches Ignianima with the original spell flight");
                for (var shot : shots.subList(1, shots.size())) {
                    helper.assertTrue(shot instanceof Soulfire && shot.getOwner() == lord
                                    && shot.position().distanceTo(center.add(0, 0.5, 0)) < 1.0E-6
                                    && !shot.saveWithoutId(new CompoundTag()).getBoolean("Ignianima"),
                            "Each of the " + count + " fireballs is ordinary Soulfire, half a block above the spell");
                }
                shots.forEach(Projectile::discard);
                shots.clear();
            }
            helper.assertTrue(target.getHealth() == target.getMaxHealth(), "The ranged attack did not substitute melee damage");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            shots.forEach(Projectile::discard);
        }
    }

    private static Vec3 savedAcceleration(SymbolProjectile spell) {
        var saved = spell.saveWithoutId(new CompoundTag());
        return new Vec3(saved.getFloat("AccelerationX"), saved.getFloat("AccelerationY"), saved.getFloat("AccelerationZ"));
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_tormentum_targets")
    public static void tormentumSparesLegacyBossesAndOnlyStartsCooldownOnAHit(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "TormentumCaster"));
        SpellKnowledge.TORMENTUM.learn(caster);
        var yaga = helper.spawnWithNoFreeWill(WitcheryEntities.BABA_YAGA.get(), new BlockPos(6, 3, 5));
        var leonard = helper.spawnWithNoFreeWill(WitcheryEntities.LEONARD.get(), new BlockPos(8, 3, 5));
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(12, 3, 5));
        var targets = List.of(yaga, leonard, pig);
        var center = helper.absolutePos(new BlockPos(7, 3, 5));
        var hit = new BlockHitResult(Vec3.atCenterOf(center), Direction.UP, center, false);

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                targets.stream().allMatch(target -> level.getEntity(target.getUUID()) == target), "All targets are tracked"))
                .thenExecute(() -> {
                    try {
                        var first = new SymbolProjectile(level, caster, SymbolSpell.TORMENTUM);
                        first.setPos(Vec3.atCenterOf(center));
                        first.onHit(hit);
                        helper.assertTrue(yaga.isAlive() && leonard.isAlive() && pig.isAlive()
                                        && SpellKnowledge.TORMENTUM.cooldownRemaining(caster, SymbolSpell.TORMENTUM_COOLDOWN) == 0,
                                "An impact beside only bosses spends no cooldown");
                        pig.setPos(Vec3.atBottomCenterOf(center.south()));
                        var second = new SymbolProjectile(level, caster, SymbolSpell.TORMENTUM);
                        second.setPos(Vec3.atCenterOf(center));
                        second.onHit(hit);
                        helper.assertTrue(yaga.isAlive() && leonard.isAlive() && pig.isRemoved()
                                        && SpellKnowledge.TORMENTUM.cooldownRemaining(caster, SymbolSpell.TORMENTUM_COOLDOWN) > 0,
                                "An ordinary creature in the impact still disappears and starts the cooldown");
                    } finally {
                        targets.forEach(target -> target.discard());
                        caster.discard();
                    }
                }).thenSucceed();
    }

    @GameTest(template = "qa_rite_terrain", batch = "witchery_terrain_spell_lumos", timeoutTicks = 140)
    public static void lumosAndNoxRestoreLightAndPermittedDrops(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(15, 220, 15));
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LightGestureTester"));
        player.setPos(Vec3.atBottomCenterOf(center));
        var globe = center.east(3);
        var wall = globe.east();
        var edge = center.offset(10, 0, 10);
        var outside = center.east(11);
        var protectedPos = center.south(5);
        var container = center.west(3);
        var edgeArea = new AABB(edge).inflate(1);
        var globeArea = new AABB(globe).inflate(1);

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(globe)
                && level.isPositionEntityTicking(edge), "The light and the distant torch must be in ticking chunks"))
                .thenExecute(() -> {
                    level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
                    level.setBlockAndUpdate(globe.below(), Blocks.STONE.defaultBlockState());
                    level.setBlockAndUpdate(globe, Blocks.SNOW.defaultBlockState());
                    var hit = new BlockHitResult(Vec3.atCenterOf(globe), Direction.WEST, wall, false);
                    var glowGlobe = WitcheryBlocks.getRequired("glowglobe").get();
                    var placements = new ArrayList<BlockEvent.EntityPlaceEvent>();
                    Consumer<BlockEvent.EntityPlaceEvent> protection = event -> {
                        if (event.getLevel() == level && event.getEntity() == player && event.getPos().equals(globe)
                                && event.getPlacedBlock().is(glowGlobe)) {
                            placements.add(event);
                            event.setCanceled(true);
                        }
                    };
                    MinecraftForge.EVENT_BUS.addListener(protection);
                    try {
                        new SymbolProjectile(level, player, SymbolSpell.LUMOS, 1).onHit(hit);
                        helper.assertTrue(placements.size() == 1
                                        && placements.get(0).getBlockSnapshot().getReplacedBlock().is(Blocks.SNOW)
                                        && level.getBlockState(globe).is(Blocks.SNOW),
                                "A veto on the proposed Glow Globe restores its snow layer");
                    } finally {
                        MinecraftForge.EVENT_BUS.unregister(protection);
                    }
                    new SymbolProjectile(level, player, SymbolSpell.LUMOS, 1).onHit(hit);
                    var light = level.getBlockState(globe);
                    helper.assertTrue(light.is(glowGlobe)
                                    && light.getLightEmission(level, globe) == 14
                                    && light.getCollisionShape(level, globe, CollisionContext.empty()).isEmpty(),
                            "Lumos replaces the snow layer with a level-14, non-solid Glow Globe");
                    for (var pos : List.of(edge, outside, protectedPos)) {
                        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
                        level.setBlockAndUpdate(pos, Blocks.TORCH.defaultBlockState());
                    }
                    level.setBlockAndUpdate(container, Blocks.ENDER_CHEST.defaultBlockState());
                    Consumer<BlockEvent.BreakEvent> protectTorch = event -> {
                        if (event.getLevel() == level && event.getPos().equals(protectedPos)) event.setCanceled(true);
                    };
                    MinecraftForge.EVENT_BUS.addListener(protectTorch);
                    try {
                        InfusionEnergy.grant(player, 2, 200);
                        SymbolSpell.NOX.cast(level, player);
                    } finally {
                        MinecraftForge.EVENT_BUS.unregister(protectTorch);
                    }
                    helper.assertTrue(level.isEmptyBlock(globe) && level.isEmptyBlock(edge)
                                    && level.getBlockState(outside).is(Blocks.TORCH)
                                    && level.getBlockState(protectedPos).is(Blocks.TORCH)
                                    && level.getBlockState(container).is(Blocks.ENDER_CHEST)
                                    && InfusionEnergy.canSpend(player, 150) && !InfusionEnergy.canSpend(player, 151),
                            "Nox covers a cube, respects protected blocks and costs fifty charge");
                }).thenWaitUntil(() -> helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, edgeArea).stream()
                        .anyMatch(item -> item.getItem().is(Items.TORCH)), "The extinguished torch drops as loot"))
                .thenExecute(() -> {
                    helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, globeArea).isEmpty(),
                            "The conjured Glow Globe leaves no loot");
                    level.getEntitiesOfClass(ItemEntity.class, edgeArea).forEach(ItemEntity::discard);
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_worship_spells")
    public static void worshipUnlocksEnhancedDisarming(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "WorshipGestureTester"));
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 3, 4))));
        var direct = helper.spawn(EntityType.HUSK, new BlockPos(5, 3, 5));
        var nearby = helper.spawn(EntityType.HUSK, new BlockPos(8, 3, 5));
        for (var husk : List.of(direct, nearby)) {
            husk.setNoAi(true);
            husk.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_SWORD));
        }

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(direct.getUUID()) == direct
                        && level.getEntity(nearby.getUUID()) == nearby, "Both armed targets must be visible to the spell"))
                .thenExecute(() -> {
                    var gesture = "↑↑↑↑↑↓↓↓↓";
                    var spell = SymbolSpell.match(gesture);
                    helper.assertTrue(spell == SymbolSpell.EXPELLIARMUS && spell.gestureLevel(gesture) == 3,
                            "The original full gesture requests the third rank");
                    InfusionEnergy.grant(player, 4, 100);
                    var projectiles = new ArrayList<SymbolProjectile>();
                    Consumer<EntityJoinLevelEvent> capture = event -> {
                        if (event.getLevel() == level && event.getEntity() instanceof SymbolProjectile projectile) {
                            projectiles.add(projectile);
                        }
                    };
                    MinecraftForge.EVENT_BUS.addListener(capture);
                    try {
                        spell.cast(level, player, 3);
                        player.addEffect(new MobEffectInstance(WitcheryEffects.WORSHIP.get(), 1200, 0));
                        spell.cast(level, player, 2);
                        player.addEffect(new MobEffectInstance(WitcheryEffects.WORSHIP.get(), 1200, 1));
                        spell.cast(level, player, 3);
                    } finally {
                        MinecraftForge.EVENT_BUS.unregister(capture);
                    }
                    try {
                        var ranks = projectiles.stream().map(shot -> shot.saveWithoutId(new CompoundTag()).getInt("effectLevel")).toList();
                        helper.assertTrue(ranks.equals(List.of(1, 2, 3)) && InfusionEnergy.canSpend(player, 93)
                                        && !InfusionEnergy.canSpend(player, 94),
                                "Worship enables both enhanced ranks and the casts spend one, two, and four charges");
                        projectiles.get(2).setPos(direct.position());
                        projectiles.get(2).onHit(new EntityHitResult(direct));
                        helper.assertTrue(direct.getMainHandItem().isEmpty() && nearby.getMainHandItem().isEmpty(),
                                "A third-rank impact disarms the direct and nearby targets");
                    } finally {
                        projectiles.forEach(SymbolProjectile::discard);
                        direct.discard();
                        nearby.discard();
                    }
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void leonardsUrnKeepsTheOriginalBrewFlight(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "UrnSpellCaster"));
        caster.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 16, 5))));
        caster.setYRot(0);
        caster.setXRot(0);
        caster.setDeltaMovement(2, 0, 0);

        var ingredients = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER),
                new ItemStack(WitcheryIngredient.WEB.item()));
        var mixture = BrewMixture.from(ingredients);
        var bottle = WitcheryItems.getRequired("brewbottle").get().getDefaultInstance();
        bottle.setTag(mixture.bottleTag(ingredients));
        var urn = WitcheryItems.getRequired("leonardsurn").get().getDefaultInstance();
        LeonardsUrnItem.saveContents(urn, List.of(bottle));
        caster.getInventory().setItem(1, urn);
        InfusionEnergy.grant(caster, 2, 200);

        var shots = new ArrayList<ThrownBrew>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ThrownBrew shot && shot.getOwner() == caster) {
                shots.add(shot);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            SymbolSpell.LEONARD_1.cast(level, caster);
            helper.assertTrue(shots.size() == 1, "The first Urn gesture launches the stored splash brew");
            var shot = shots.get(0);
            Vec3 motion = shot.getDeltaMovement();
            helper.assertTrue(motion.distanceTo(new Vec3(0, 0, 0.75)) < 0.06,
                    "The spell flies at the original 0.75 speed without inheriting caster movement. Actual " + motion);
            Vec3 launch = new Vec3(caster.getX() - 0.16, caster.getEyeY() - 0.1, caster.getZ());
            helper.assertTrue(shot.position().distanceTo(launch) < 1.0E-6 && shot.isNoGravity(),
                    "The spell starts just beside and below the caster's eyes and has no gravity");
            int remaining = 200 - Math.max(4, mixture.used() * 2);
            helper.assertTrue(ItemStack.matches(shot.getItem(), bottle) && ItemStack.matches(LeonardsUrnItem.contents(urn).get(0), bottle)
                            && InfusionEnergy.canSpend(caster, remaining) && !InfusionEnergy.canSpend(caster, remaining + 1),
                    "The shot carries the stored mixture and spends the original charge without consuming the bottle");

            var restored = WitcheryEntities.THROWN_BREW.get().create(level);
            restored.load(shot.saveWithoutId(new CompoundTag()));
            restored.tick();
            helper.assertTrue(restored.position().distanceTo(shot.position().add(motion)) < 1.0E-6
                            && restored.getDeltaMovement().distanceTo(motion.scale(0.99F)) < 1.0E-6,
                    "A saved Urn spell continues its gravity-free flight with air drag");
            shot.tickCount = 199;
            shot.tick();
            helper.assertTrue(!shot.isRemoved(), "The Urn spell survives the 199th air tick");
            shot.tickCount = 200;
            shot.tick();
            helper.assertTrue(shot.isRemoved(), "The Urn spell expires on the 200th air tick");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
            shots.forEach(ThrownBrew::discard);
            caster.discard();
        }
    }
}
