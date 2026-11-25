package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
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

    @GameTest(template = "qa_coven_arena", batch = "witchery_spectral_boss_caps")
    public static void spectralTouchUsesTheOriginalTargetAndAttackerCaps(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = new ShieldBearer(level, helper.absolutePos(new BlockPos(5, 5, 5)));
        var spectre = WitcheryEntities.SPECTRE.get().create(level);
        var mobTouch = WitcheryDamageTypes.spectralTouch(spectre);
        var playerTouch = WitcheryDamageTypes.spectralTouch(player);
        var caps = new TargetCap[] {
                new TargetCap(WitcheryEntities.VAMPIRE.get(), 0, 0),
                new TargetCap(WitcheryEntities.LEONARD.get(), 0, 0),
                new TargetCap(WitcheryEntities.REFLECTION.get(), 2, 2),
                new TargetCap(WitcheryEntities.LORD_OF_TORMENT.get(), 5, 5),
                new TargetCap(WitcheryEntities.LILITH.get(), 6, 12),
                new TargetCap(WitcheryEntities.DEATH.get(), 6, 15),
                new TargetCap(WitcheryEntities.BABA_YAGA.get(), 6, 15),
                new TargetCap(WitcheryEntities.MOG.get(), 6, 15),
                new TargetCap(WitcheryEntities.GULG.get(), 6, 15),
                new TargetCap(WitcheryEntities.HORNED_HUNTSMAN.get(), 6, 15)
        };
        for (TargetCap cap : caps) {
            assertHealthLoss(helper, cap.type().create(level), mobTouch, cap.mob());
            assertHealthLoss(helper, cap.type().create(level), playerTouch, cap.player());
        }
        helper.assertTrue(player.getHealth() == player.getMaxHealth(),
                "Baba Yaga takes spectral touch without reflecting the player's health drain");

        var leonard = WitcheryEntities.LEONARD.get().create(level);
        leonard.readAdditionalSaveData(new CompoundTag());
        helper.assertTrue(leonard.hurt(level.damageSources().playerAttack(player), 2),
                "The unshielded Leonard still accepts a normal player attack");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_spectral_boss_defenses")
    public static void spectralTouchPassesThroughBossEntranceAndPartnerDefense(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var spectre = WitcheryEntities.SPECTRE.get().create(level);
        var touch = WitcheryDamageTypes.spectralTouch(spectre);
        var melee = level.damageSources().mobAttack(spectre);
        var lilith = WitcheryEntities.LILITH.get().create(level);
        CompoundTag entrance = new CompoundTag();
        entrance.putInt("Invul", 30);
        lilith.readAdditionalSaveData(entrance);
        helper.assertFalse(lilith.hurt(melee, 50), "Lilith's entrance rejects ordinary melee damage");
        assertHealthLoss(helper, lilith, touch, 6);
        lilith.setInvulnerable(true);
        assertHealthLoss(helper, lilith, touch, 0);

        var gulg = helper.spawn(WitcheryEntities.GULG.get(), new BlockPos(4, 5, 4));
        var mog = helper.spawn(WitcheryEntities.MOG.get(), new BlockPos(4, 5, 6));
        helper.succeedWhen(() -> {
            helper.assertTrue(level.getEntitiesOfClass(HobgoblinGod.class, gulg.getBoundingBox().inflate(3)).contains(mog),
                    "Mog must be tracked within Gulg's defensive range");
            helper.assertFalse(gulg.hurt(melee, 50), "Mog protects nearby Gulg from ordinary melee damage");
            assertHealthLoss(helper, gulg, touch, 6);
        });
    }

    private static void assertHealthLoss(GameTestHelper helper, LivingEntity target, DamageSource source, float expected) {
        if (target instanceof Leonard leonard) leonard.readAdditionalSaveData(new CompoundTag());
        target.setHealth(target.getMaxHealth());
        boolean hit = target.hurt(source, 50);
        helper.assertTrue(hit == (expected > 0) && target.getMaxHealth() - target.getHealth() == expected,
                target.getType() + " should lose " + expected + " health to spectral touch from " + source.getEntity());
    }

    private record TargetCap(EntityType<? extends LivingEntity> type, float mob, float player) {}

    @GameTest(template = "qa_coven_arena", batch = "witchery_baba_transforms_attackers")
    public static void babaYagaTransformsSpectresAndUndeadOnContact(GameTestHelper helper) {
        var level = helper.getLevel();
        var baba = helper.spawn(WitcheryEntities.BABA_YAGA.get(), new BlockPos(5, 5, 5));
        var spectre = helper.spawn(WitcheryEntities.SPECTRE.get(), new BlockPos(6, 5, 5));
        var zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(7, 5, 5));
        var target = helper.spawn(EntityType.COW, new BlockPos(3, 5, 5));
        baba.setHealth(baba.getMaxHealth());
        baba.setTarget(target);
        var spiders = new ArrayList<CaveSpider>();
        Consumer<EntityJoinLevelEvent> watch = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof CaveSpider spider) spiders.add(spider);
        };
        MinecraftForge.EVENT_BUS.addListener(watch);
        try {
            helper.assertTrue(spectre.doHurtTarget(baba) && (baba.getMaxHealth() - baba.getHealth()) == 6,
                    "Spectral touch drains six health from Baba Yaga");
            helper.assertTrue(spectre.isRemoved() && spiders.size() == 2 && spiders.stream().allMatch(spider ->
                            spider.position().equals(spectre.position()) && spider.getTarget() == target),
                    "Both retaliation spiders inherit Baba Yaga's target and spawn at the Spectre's feet");

            helper.assertFalse(baba.hurt(level.damageSources().mobAttack(zombie), 4),
                    "Her regular damage cooldown rejects the weaker strike");
            helper.assertTrue(zombie.isRemoved() && spiders.size() == 3
                            && spiders.get(2).position().equals(zombie.position()),
                    "Even a rejected ordinary strike transforms an undead attacker once");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(watch);
            spiders.forEach(LivingEntity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_baba_wooden_attack")
    public static void babaYagaReflectsSilverBoltsAndSparesWoodenAttacks(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new ShieldBearer(level, helper.absolutePos(new BlockPos(6, 5, 5)));
        var baba = WitcheryEntities.BABA_YAGA.get().create(level);
        CompoundTag oldSave = new CompoundTag();
        var attributes = new ListTag();
        CompoundTag armor = new CompoundTag();
        armor.putString("Name", "minecraft:generic.armor");
        armor.putDouble("Base", 4);
        attributes.add(armor);
        oldSave.put("Attributes", attributes);
        baba.readAdditionalSaveData(oldSave);
        helper.assertTrue(baba.getAttributeBaseValue(Attributes.ARMOR) == 0,
                "An older port save uses Baba Yaga's fixed protection without counting the old armor attribute twice");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WOODEN_SWORD));
        var sword = level.damageSources().playerAttack(player);
        baba.setHealth(baba.getMaxHealth());
        helper.assertTrue(baba.hurt(sword, 4) && player.getHealth() == player.getMaxHealth()
                        && Math.abs(baba.getMaxHealth() - baba.getHealth() - 3.36F) < 0.01F,
                "The wooden sword penetrates Baba Yaga's fixed armor without hurting its wielder");

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        var stake = new WitcheryBolt(player, WitcheryBolt.Kind.STAKE);
        helper.assertTrue(!baba.hurt(level.damageSources().arrow(stake, player), 4)
                        && player.getHealth() == player.getMaxHealth(),
                "A wooden bolt is also safe when the boss rejects the hit during damage cooldown");

        var silver = new WitcheryBolt(player, WitcheryBolt.Kind.SILVER);
        helper.assertTrue(!baba.hurt(level.damageSources().arrow(silver, player), 4)
                        && player.getHealth() < player.getMaxHealth(),
                "A silver bolt still hurts its shooter even during the boss's damage cooldown");
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
