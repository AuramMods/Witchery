package art.arcane.witchery.infusion;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class InfusionChargeTests {
    private static final String CHARGE = "witcheryInfusionCharges";
    private static final String CAPACITY = "witcheryInfusionChargesMax";

    @GameTest(template = "qa_coven_arena")
    public static void lightCloakClearsChargeOnItsFirstUnaffordablePulse(GameTestHelper helper) {
        FakePlayer caster = caster(helper);
        InfusionEnergy.grant(caster, LightInfusion.ID, 1);
        ItemStack hand = new ItemStack(WitcheryItems.getRequired("witchhand").get());
        hand.getItem().onUseTick(caster.level(), caster, hand, hand.getUseDuration() - 30);
        helper.assertTrue(caster.hasEffect(MobEffects.INVISIBILITY) && infusion(caster).contains(CHARGE)
                        && infusion(caster).getInt(CHARGE) == 0,
                "An exact spend leaves the zero charge field while the cloak is active");
        hand.getItem().onUseTick(caster.level(), caster, hand, hand.getUseDuration() - 60);
        helper.assertTrue(!caster.hasEffect(MobEffects.INVISIBILITY) && !infusion(caster).contains(CHARGE)
                        && InfusionEnergy.type(caster) == LightInfusion.ID && infusion(caster).getInt(CAPACITY) == 1,
                "The unaffordable pulse clears its charge field and cloak while keeping the infusion and capacity");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void branchAndContractKeepAnUnaffordableBalance(GameTestHelper helper) {
        FakePlayer caster = caster(helper);
        InfusionEnergy.grant(caster, OverworldInfusion.ID, 9);
        ItemStack contract = new ItemStack(WitcheryIngredient.CONTRACT_OF_TORMENT.item());
        SymbolSpell.NOX.cast(helper.getLevel(), caster);
        contract.getItem().onUseTick(caster.level(), caster, contract, contract.getUseDuration());
        helper.assertTrue(infusion(caster).getInt(CHARGE) == 9 && contract.getCount() == 1,
                "The Branch and Contract leave all nine charges available after their failed attempts");
        caster.getAbilities().instabuild = true;
        infusion(caster).remove(CHARGE);
        helper.assertTrue(!InfusionEnergy.canSpend(caster, 10),
                "The Contract and other ordinary consumers still require an infusion charge field in creative");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void passiveCreatureDefenseIgnoresAnUnaffordableHit(GameTestHelper helper) {
        FakePlayer caster = caster(helper);
        InfusionEnergy.grant(caster, InfernalInfusion.ID, 5);
        CreaturePower.SKELETON.absorb(caster);
        infusion(caster).putInt(CHARGE, 4);
        var sounds = new AtomicInteger();
        Consumer<PlayLevelSoundEvent.AtEntity> capture = cue -> {
            if (cue.getEntity() == caster) sounds.incrementAndGet();
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            var drowning = caster.damageSources().drown();
            var unaffordable = new LivingHurtEvent(caster, drowning, 2);
            MinecraftForge.EVENT_BUS.post(unaffordable);
            helper.assertTrue(!unaffordable.isCanceled() && infusion(caster).getInt(CHARGE) == 4 && sounds.get() == 0,
                    "The passive Skeleton power keeps four charges and stays quiet on an unaffordable hit");
            infusion(caster).putInt(CHARGE, 5);
            var affordable = new LivingHurtEvent(caster, drowning, 2);
            MinecraftForge.EVENT_BUS.post(affordable);
            helper.assertTrue(affordable.isCanceled() && infusion(caster).contains(CHARGE)
                            && infusion(caster).getInt(CHARGE) == 0 && caster.hasEffect(MobEffects.WATER_BREATHING),
                    "Five available charges pay for the original drowning defense");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
        }
        helper.succeed();
    }

    private static FakePlayer caster(GameTestHelper helper) {
        FakePlayer caster = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ChargeCaster"));
        Vec3 site = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 0, 6)).atY(200));
        caster.moveTo(site.x, site.y, site.z, 0, 0);
        return caster;
    }

    private static CompoundTag infusion(FakePlayer caster) {
        return caster.getPersistentData().getCompound(FakePlayer.PERSISTED_NBT_TAG);
    }
}
