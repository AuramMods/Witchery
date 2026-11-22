package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.GrenadeItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SunGrenadeFuseTests {
    @GameTest(template = "qa_coven_arena", timeoutTicks = 260)
    public static void sunGrenadeKeepsItsOriginalFuseAfterImpact(GameTestHelper helper) {
        var level = helper.getLevel();
        var relative = new BlockPos(4, 4, 4);
        helper.setBlock(relative.east(3), Blocks.STONE);
        helper.setBlock(relative.east(3).below(), Blocks.STONE);
        helper.setBlock(relative.east(2).below(2), Blocks.STONE);
        var origin = Vec3.atCenterOf(helper.absolutePos(relative));
        var grenade = new GrenadeProjectile(WitcheryEntities.GRENADE.get(), level);
        grenade.setPos(origin);
        grenade.setDeltaMovement(0.65, 0, 0);
        level.addFreshEntity(grenade);

        helper.runAfterDelay(22, () -> {
            helper.assertTrue(grenade.hasImpacted(), "The grenade must hit the stone wall and remain in the world");
            helper.assertTrue(grenade.saveWithoutId(new CompoundTag()).getInt("Remaining") < 190,
                    "Landing must keep the remaining part of the original 200-tick fuse");
        });
        helper.runAfterDelay(220, () -> {
            helper.assertTrue(grenade.isRemoved(), "The sun grenade must spend its fuse about ten seconds after launch");
            helper.assertTrue(!level.getBlockState(grenade.lightPosition()).is(WitcheryBlocks.getRequired("light").get()),
                    "The temporary light must disappear after the grenade bursts");
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(origin, origin).inflate(6),
                    drop -> drop.getItem().is(WitcheryIngredient.QUARTZ_SPHERE.item()));
            helper.assertTrue(drops.size() == 1, "The burst must return one Quartz Sphere");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void loadOriginalAndEarlierPortGrenadeFuses(GameTestHelper helper) {
        var grenade = new GrenadeProjectile(WitcheryEntities.GRENADE.get(), helper.getLevel());
        var saved = new CompoundTag();
        saved.putBoolean("Impacted", true);
        grenade.readAdditionalSaveData(saved);
        helper.assertTrue(grenade.hasImpacted() && grenade.saveWithoutId(new CompoundTag()).getInt("Remaining") == 200,
                "An original landed grenade restarts with its original duration");
        saved.putInt("Remaining", 675);
        grenade.readAdditionalSaveData(saved);
        helper.assertTrue(grenade.saveWithoutId(new CompoundTag()).getInt("Remaining") == 675,
                "An earlier port's saved fuse must retain its remaining ticks");

        saved.remove("Remaining");
        saved.putBoolean("Impacted", false);
        saved.putInt("Mode", 1);
        saved.putString("Owner", "LegacyDisguise");
        grenade.readAdditionalSaveData(saved);
        helper.assertTrue(GrenadeItem.isDuplicate(grenade.getItem())
                        && GrenadeItem.ownerName(grenade.getItem()).equals("LegacyDisguise"),
                "The original duplicate grenade keeps its named skin");
        helper.succeed();
    }
}
