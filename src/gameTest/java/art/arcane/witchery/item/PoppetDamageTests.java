package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PoppetDamageTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_manual_voodoo", setupTicks = 5)
    public static void manualVoodooRollsTheCastersOutfitForPushAndLava(GameTestHelper helper) {
        var level = helper.getLevel();
        var caster = player(helper);
        caster.setYRot(-90);
        caster.setYHeadRot(-90);
        caster.setXRot(0);
        var target = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(9, 3, 8));
        try {
            equipHunterOutfit(caster);
            String trigger = "WITCHunterTrigger";
            var poppet = equipPoppet(caster, 0, "voodoo_poppet");
            TaglockItem.bindSignature(poppet, target);
            caster.getRandom().setSeed(4097); // First roll reports the action, second roll resists it.
            poppet.use(level, caster, InteractionHand.MAIN_HAND);
            poppet.releaseUsing(level, caster, 20);
            helper.assertTrue(target.getDeltaMovement().equals(Vec3.ZERO) && poppet.getTag().getInt("WITCDamage") == 0,
                    "The caster's outfit must be able to stop a push without spending ten wear");
            var notice = caster.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            helper.assertTrue(notice.getLong(trigger) > 0, "Resisted manual voodoo still draws pursuit");

            for (var slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                caster.setItemSlot(slot, ItemStack.EMPTY);
            }
            equipHunterOutfit(target);
            poppet.use(level, caster, InteractionHand.MAIN_HAND);
            poppet.releaseUsing(level, caster, 20);
            helper.assertTrue(target.getDeltaMovement().x > 2 && poppet.getTag().getInt("WITCDamage") == 10,
                    "Only the caster's outfit can stop the manual push");

            var lava = new BlockPos(7, 4, 5);
            helper.setBlock(lava.below(), Blocks.STONE);
            helper.setBlock(lava, Blocks.LAVA);
            var aim = caster.pick(5, 1, true);
            helper.assertTrue(aim instanceof BlockHitResult hit && hit.getBlockPos().equals(helper.absolutePos(lava)),
                    "The caster's view intersects a lava source: " + aim.getType() + " " + aim.getLocation()
                            + ", eye=" + caster.getEyePosition() + ", lava=" + helper.absolutePos(lava));
            equipHunterOutfit(caster);
            caster.getRandom().setSeed(noticeSeed());
            poppet.use(level, caster, InteractionHand.MAIN_HAND);
            poppet.releaseUsing(level, caster, 20);
            helper.assertTrue(!target.isOnFire() && poppet.isEmpty(),
                    "The caster's outfit can stop lava ignition, which still consumes the whole poppet (fire="
                            + target.isOnFire() + ", count=" + poppet.getCount() + ")");

            var freshPoppet = equipPoppet(caster, 0, "voodoo_poppet");
            TaglockItem.bindSignature(freshPoppet, target);
            caster.getRandom().setSeed(0);
            freshPoppet.use(level, caster, InteractionHand.MAIN_HAND);
            freshPoppet.releaseUsing(level, caster, 20);
            helper.assertTrue(target.isOnFire() && freshPoppet.isEmpty(),
                    "An ignition that passes the outfit roll sets fire to the target and consumes the poppet");
            helper.succeed();
        } finally {
            caster.discard();
            target.discard();
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_submerged_voodoo", setupTicks = 5)
    public static void submergedVoodooReportsTheBearerAndRespectsHunterClothing(GameTestHelper helper) {
        var bearer = player(helper);
        var level = helper.getLevel();
        for (int y = 3; y <= 5; y++) helper.setBlock(new BlockPos(4, y, 5), Blocks.WATER);
        bearer.baseTick();
        bearer.baseTick();
        helper.assertTrue(bearer.isEyeInFluid(FluidTags.WATER), "The bearer is submerged in real water");

        var recipient = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(7, 3, 5));
        var guarded = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(9, 3, 5));
        var poppet = equipPoppet(bearer, 5, "voodoo_poppet");
        TaglockItem.bindSignature(poppet, recipient);

        helper.runAfterDelay(5, () -> {
            try {
                long seed = noticeSeed();
                var persistence = bearer.getPersistentData();
                String trigger = "WITCHunterTrigger";
                bearer.getRandom().setSeed(seed);
                bearer.setAirSupply(1);
                bearer.getInventory().tick();
                helper.assertTrue(recipient.getHealth() == 10 && persistence.getCompound(Player.PERSISTED_NBT_TAG).getLong(trigger) == 0,
                        "A submerged bearer with air neither harms the target nor draws pursuit");

                bearer.setAirSupply(0);
                bearer.getRandom().setSeed(seed);
                recipient.setSecondsOnFire(10);
                bearer.getInventory().tick();
                long now = level.getServer().overworld().getGameTime();
                var notice = persistence.getCompound(Player.PERSISTED_NBT_TAG);
                helper.assertTrue(recipient.getHealth() == 9 && !recipient.isOnFire(),
                        "A carried poppet deals drowning damage and extinguishes the bound target");
                helper.assertTrue(notice.getLong(trigger) == now, "The submerged action records the bearer for pursuit");

                equipHunterOutfit(guarded);
                TaglockItem.bindSignature(poppet, guarded);
                notice.remove(trigger);
                bearer.getRandom().setSeed(seed);
                guarded.getRandom().setSeed(seed);
                guarded.setSecondsOnFire(10);
                bearer.getInventory().tick();
                helper.assertTrue(guarded.getHealth() == 10 && !guarded.isOnFire() && notice.getLong(trigger) == now,
                        "The hunter outfit can block the damage while the action still draws pursuit and extinguishes the target");
                helper.succeed();
            } finally {
                bearer.discard();
                recipient.discard();
                guarded.discard();
            }
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void vampiricPoppetTransfersUnattributedExplosionsOnlyForNonVampires(GameTestHelper helper) {
        var level = helper.getLevel();
        var bearer = player(helper);
        var recipient = helper.spawn(EntityType.PIG, new BlockPos(7, 3, 5));
        var attacker = helper.spawn(EntityType.ZOMBIE, new BlockPos(9, 3, 5));
        var poppet = equipPoppet(bearer, 0, "vampiric_poppet");
        var signature = WitcheryItems.getRequired("taglockkit").get().getDefaultInstance();
        TaglockItem.bindSignature(signature, recipient);
        TaglockItem.copyBinding(signature, poppet, 2);
        var data = WitcheryPlayerDataProvider.get(bearer).orElseThrow(IllegalStateException::new);

        data.setVampireLevel(1);
        var bite = new LivingHurtEvent(bearer, level.damageSources().mobAttack(attacker), 4);
        helper.assertTrue(!MinecraftForge.EVENT_BUS.post(bite) && recipient.getHealth() == 10
                        && poppet.getTag().getInt("WITCDamage") == 0,
                "A vampire bearer should take the hit without using the Vampiric Poppet");

        data.setVampireLevel(0);
        var explosion = new LivingHurtEvent(bearer, level.damageSources().explosion(null, null), 4);
        helper.assertTrue(MinecraftForge.EVENT_BUS.post(explosion) && recipient.getHealth() == 6
                        && poppet.getTag().getInt("WITCDamage") == 66,
                "An explosion without an attacker should transfer four damage and use sixty-six wear");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void lethalPoppetsRespectVampireFormAndPreserveTheStarvationException(GameTestHelper helper) {
        var level = helper.getLevel();
        var bearer = player(helper);
        var death = equipPoppet(bearer, 0, "death_protection_poppet");
        var fire = equipPoppet(bearer, 1, "fire_protection_poppet");
        var hunger = equipPoppet(bearer, 2, "hunger_protection_poppet");
        var data = WitcheryPlayerDataProvider.get(bearer).orElseThrow(IllegalStateException::new);
        bearer.setHealth(1);
        data.setVampireLevel(1);
        var flame = new LivingDamageEvent(bearer, level.damageSources().inFire(), 4);
        helper.assertTrue(!MinecraftForge.EVENT_BUS.post(flame) && !death.isEmpty() && !fire.isEmpty(),
                "Lethal fire must reach vampire mortality without spending an ordinary ward");
        data.setVampireLevel(0);
        data.setWerewolfLevel(1);
        data.setCreatureTypeOrdinal(1);
        var starvation = new LivingDamageEvent(bearer, level.damageSources().starve(), 1);
        helper.assertTrue(MinecraftForge.EVENT_BUS.post(starvation) && hunger.isEmpty() && !death.isEmpty()
                        && bearer.getHealth() == 10,
                "A transformed werewolf can still use a Hunger Protection Poppet at low health");
        helper.succeed();
    }

    private static FakePlayer player(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "PoppetBearer"));
        player.moveTo(helper.absoluteVec(new Vec3(4.5, 3, 5.5)));
        return player;
    }

    private static void equipHunterOutfit(LivingEntity wearer) {
        wearer.setItemSlot(EquipmentSlot.HEAD, WitcheryItems.getRequired("hunterhat").get().getDefaultInstance());
        wearer.setItemSlot(EquipmentSlot.CHEST, WitcheryItems.getRequired("huntercoat").get().getDefaultInstance());
        wearer.setItemSlot(EquipmentSlot.LEGS, WitcheryItems.getRequired("hunterlegs").get().getDefaultInstance());
        wearer.setItemSlot(EquipmentSlot.FEET, WitcheryItems.getRequired("hunterboots").get().getDefaultInstance());
    }

    private static long noticeSeed() {
        long seed = 0;
        while (RandomSource.create(seed).nextFloat() >= 0.1F) seed++;
        return seed;
    }

    private static ItemStack equipPoppet(FakePlayer player, int slot, String path) {
        var stack = WitcheryItems.getRequired(path).get().getDefaultInstance();
        TaglockItem.bindSignature(stack, player);
        player.getInventory().setItem(slot, stack);
        return stack;
    }
}
