package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.registry.WitcheryEffects;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SymbolSpellTests {
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
