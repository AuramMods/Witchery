package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SpectralCombatTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_spectral_player_damage")
    public static void spectralTouchPassesThroughARaisedShieldAndAbsorption(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new ShieldBearer(level, helper.absolutePos(new BlockPos(5, 5, 5)));
        var spectre = WitcheryEntities.SPECTRE.get().create(level);
        spectre.setPos(player.getX(), player.getY(), player.getZ() + 1.5);
        player.setYRot(0);
        player.raiseShield();
        player.setAbsorptionAmount(4);
        helper.assertTrue(player.isDamageSourceBlocked(level.damageSources().mobAttack(spectre)),
                "An ordinary frontal blow is covered by the raised shield");

        spectre.doHurtTarget(player);
        helper.assertTrue(player.getHealth() == 17 && player.getAbsorptionAmount() == 4
                        && player.getOffhandItem().getDamageValue() == 0,
                "The Spectre drains three health while leaving the shield and absorption hearts intact");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_spectral_mob_damage")
    public static void spectralTouchPreservesForgeDamageHooksAndMeleeEnchants(GameTestHelper helper) {
        var level = helper.getLevel();
        var pig = EntityType.PIG.create(level);
        var spectre = WitcheryEntities.SPECTRE.get().create(level);
        BlockPos pos = helper.absolutePos(new BlockPos(5, 5, 5));
        pig.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 1.5);
        spectre.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        pig.setAbsorptionAmount(4);

        var sword = new ItemStack(Items.IRON_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 1);
        sword.enchant(Enchantments.KNOCKBACK, 1);
        sword.enchant(Enchantments.FIRE_ASPECT, 1);
        spectre.setItemSlot(EquipmentSlot.MAINHAND, sword);
        Consumer<LivingHurtEvent> veto = event -> {
            if (event.getEntity() == pig && event.getSource().is(WitcheryDamageTypes.SPECTRAL_TOUCH)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            spectre.doHurtTarget(pig);
            helper.assertTrue(pig.getHealth() == 10 && pig.getAbsorptionAmount() == 4,
                    "The Forge damage veto still protects the target");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }
        spectre.doHurtTarget(pig);
        helper.assertTrue(pig.getHealth() == 7.5F && pig.getAbsorptionAmount() == 4
                        && pig.getRemainingFireTicks() >= 80 && pig.getDeltaMovement().horizontalDistanceSqr() > 0,
                "Sharpness I adds to the health drain, and a landed attack applies Fire Aspect and Knockback");

        pig.invulnerableTime = 0;
        pig.hurt(level.damageSources().mobAttack(spectre), 2);
        helper.assertTrue(pig.getHealth() == 7.5F && pig.getAbsorptionAmount() == 2,
                "An ordinary melee hit still spends absorption before reaching health");
        helper.succeed();
    }

    private static final class ShieldBearer extends Player {
        private ShieldBearer(ServerLevel level, BlockPos pos) {
            super(level, pos, 0, new GameProfile(UUID.randomUUID(), "SpectralShield"));
        }

        private void raiseShield() {
            ItemStack shield = new ItemStack(Items.SHIELD);
            setItemInHand(InteractionHand.OFF_HAND, shield);
            startUsingItem(InteractionHand.OFF_HAND);
            for (int tick = 0; tick < 6; tick++) updateUsingItem(shield);
        }

        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isCreative() {
            return false;
        }
    }
}
