package art.arcane.witchery.integration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryCommonConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class IntegrationDataPackTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_optional_integration")
    public static void industrialForegoingProtectionFollowsIntegrationConfig(GameTestHelper helper) {
        boolean enabled = WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get();
        for (String tool : List.of("mob_crusher", "mob_duplicator", "mob_imprisonment_tool")) {
            var tag = TagKey.create(Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath("industrialforegoing", tool + "_blacklist"));
            helper.assertTrue(WitcheryEntities.DEATH.get().is(tag) == enabled,
                    "Death's membership in the " + tool + " blacklist follows allowModIntegration");
        }
        helper.succeed();
    }
}
