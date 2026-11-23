package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
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
                    new SymbolProjectile(level, player, SymbolSpell.LUMOS, 1)
                            .onHit(new BlockHitResult(Vec3.atCenterOf(globe), Direction.WEST, wall, false));
                    var light = level.getBlockState(globe);
                    helper.assertTrue(light.is(WitcheryBlocks.getRequired("glowglobe").get())
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
}
