package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class HellhoundCombatTests {
    @GameTest(template = "qa_coven_arena")
    public static void hellhoundKeepsOriginalInnateArmorAndChecksTheArrowShooter(GameTestHelper helper) {
        var level = helper.getLevel();
        var type = WitcheryEntities.HELLHOUND.get();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "HoundCombat"));
        var skeleton = EntityType.SKELETON.create(level);
        var arrow = EntityType.ARROW.create(level);
        var sources = level.damageSources();

        var target = type.create(level);
        helper.assertTrue(target.hurt(sources.playerAttack(player), 4) && Math.abs(target.getHealth() - 16.32F) < 0.001F,
                "The original two armor points absorb eight percent of an ordinary blow");
        target = type.create(level);
        helper.assertTrue(target.hurt(sources.arrow(arrow, player), 8) && Math.abs(target.getHealth() - 12.64F) < 0.001F,
                "An arrow fired by a player receives only the innate armor reduction");
        target = type.create(level);
        helper.assertTrue(target.hurt(sources.arrow(arrow, skeleton), 8) && Math.abs(target.getHealth() - 15.86F) < 0.001F,
                "An arrow fired by a skeleton receives the original non-player reduction before armor");
        target = type.create(level);
        helper.assertTrue(target.hurt(sources.magic(), 4) && Math.abs(target.getHealth() - 16) < 0.001F,
                "Damage that bypasses armor keeps its full value");

        target = type.create(level);
        target.getAttribute(Attributes.ARMOR).setBaseValue(2);
        var oldSave = target.saveWithoutId(new CompoundTag());
        oldSave.remove("InnateArmor");
        var reopened = type.create(level);
        reopened.load(oldSave);
        helper.assertTrue(reopened.getAttributeBaseValue(Attributes.ARMOR) == 0
                        && reopened.hurt(sources.playerAttack(player), 4) && Math.abs(reopened.getHealth() - 16.32F) < 0.001F,
                "The previous Forge save migrates its two base armor points to the innate reduction");
        helper.succeed();
    }
}
