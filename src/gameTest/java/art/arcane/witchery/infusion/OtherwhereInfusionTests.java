package art.arcane.witchery.infusion;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class OtherwhereInfusionTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_otherwhere_geometry")
    public static void endermanPowerUsesDirectTravelUnderInhibition(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = caster(helper);
        var base = helper.absolutePos(new BlockPos(2, 0, 2)).atY(200);
        var start = Vec3.atBottomCenterOf(base);
        caster.moveTo(start.x, start.y, start.z, 0, 0);
        level.setBlockAndUpdate(base.offset(0, 1, 5), Blocks.STONE.defaultBlockState());
        caster.addEffect(new MobEffectInstance(WitcheryEffects.ENDER_INHIBITION.get(), 300, 2));

        var hand = new ItemStack(WitcheryItems.getRequired("witchhand").get());
        int remaining = hand.getUseDuration() - 5;
        InfusionEnergy.grant(caster, OtherwhereInfusion.ID, 20);
        hand.getItem().releaseUsing(hand, level, caster, remaining);
        helper.assertTrue(caster.position().equals(start) && charges(caster) == 20,
                "Inhibition III still prevents an Otherwhere blink and preserves the charge");

        InfusionEnergy.grant(caster, InfernalInfusion.ID, 20);
        CreaturePower.ENDERMAN.absorb(caster);
        hand.getItem().releaseUsing(hand, level, caster, remaining);
        helper.assertTrue(Math.abs(caster.getZ() - (base.getZ() + 4.5)) < 0.01
                        && caster.hasEffect(WitcheryEffects.ENDER_INHIBITION.get())
                        && charges(caster) == 19 && CreaturePower.charges(caster) == 9,
                "The original Infernal power can blink under the same effect and spends both charges");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_otherwhere_geometry")
    public static void otherwhereLiftUsesVictimHeightAndSpendsOnObstruction(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = caster(helper);
        var base = helper.absolutePos(new BlockPos(2, 0, 2)).atY(200);
        var start = Vec3.atBottomCenterOf(base);
        var pig = EntityType.PIG.create(level);
        caster.moveTo(start.x, start.y, start.z, 0, 0);
        pig.moveTo(start.x + 2, start.y + 2, start.z, 0, 0);
        level.setBlockAndUpdate(base.above(8), Blocks.STONE.defaultBlockState());
        InfusionEnergy.grant(caster, OtherwhereInfusion.ID, 20);

        var hand = new ItemStack(WitcheryItems.getRequired("witchhand").get());
        hand.getItem().onLeftClickEntity(hand, caster, pig);
        helper.assertTrue(caster.getY() == 204 && pig.getY() == 210 && charges(caster) == 18,
                "The caster's ceiling uses the pig's starting height and the unobstructed pig rises eight blocks");

        caster.moveTo(start.x, start.y, start.z, 0, 0);
        pig.moveTo(start.x + 2, start.y + 0.25, start.z, 0, 0);
        level.setBlockAndUpdate(base.east(2).above(2), Blocks.STONE.defaultBlockState());
        hand.getItem().onLeftClickEntity(hand, caster, pig);
        helper.assertTrue(caster.getY() == 200 && pig.getY() == 200.25 && charges(caster) == 16,
                "A ceiling at the target's feet plus two blocks stops both lifts after the two-charge debit");
        helper.succeed();
    }

    private static FakePlayer caster(GameTestHelper helper) {
        return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "OtherwhereTraveler")) {
            @Override
            public void teleportTo(ServerLevel level, double x, double y, double z, float yaw, float pitch) {
                moveTo(x, y, z, yaw, pitch);
            }
        };
    }

    private static int charges(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getInt("witcheryInfusionCharges");
    }
}
