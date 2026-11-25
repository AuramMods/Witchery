package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class FamiliarDamageTests {
    private static final String TEMPLATE = "qa_coven_arena";

    @GameTest(template = TEMPLATE)
    public static void dismissedFamiliarKeepsOnePercentOfProtection(GameTestHelper helper) {
        var owner = owner(helper.getLevel(), "DismissedOwner");
        helper.assertTrue(hurtWithSharing(owner, 10) == 10, "Without a familiar record, the entire hit remains");

        var familiarRecord = assignFamiliar(owner, UUID.randomUUID(), false);
        helper.assertTrue(!FamiliarBinding.hasActiveFamiliar(owner) && hurtWithSharing(owner, 10) == 9.9F,
                "A dismissed familiar grants the original 1% reduction while its active perks remain off");

        familiarRecord.remove("UUIDLeast");
        helper.assertTrue(hurtWithSharing(owner, 10) == 10, "The original UUID fields must identify a familiar");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_familiar_damage_nether", timeoutTicks = 160)
    public static void familiarSharesDamageAtTheOriginalCoordinateDistanceAcrossDimensions(GameTestHelper helper) {
        var overworld = helper.getLevel();
        var nether = overworld.getServer().getLevel(Level.NETHER);
        var location = helper.absolutePos(new BlockPos(2, 0, 2)).atY(150);
        var chunk = new ChunkPos(location);
        boolean forced = nether.setChunkForced(chunk.x, chunk.z, true);
        var owner = owner(overworld, "OverworldOwner");
        Vec3 familiarPosition = Vec3.atBottomCenterOf(location);
        var familiar = WitcheryEntities.TOAD.get().create(nether);
        familiar.setPos(familiarPosition);
        familiar.setNoAi(true);
        familiar.setNoGravity(true);
        familiar.setOwnerUUID(owner.getUUID());
        familiar.setTame(true);
        familiar.setFamiliar(true);
        nether.addFreshEntity(familiar);
        assignFamiliar(owner, familiar.getUUID(), true);

        helper.startSequence().thenWaitUntil(() -> {
            helper.assertTrue(nether.getEntity(familiar.getUUID()) == familiar, "The original familiar is tracked in the Nether");
        }).thenExecute(() -> {
            try {
                owner.setPos(familiarPosition.add(24, 0, 0));
                helper.assertTrue(hurtWithSharing(owner, 5) == 4.5F && familiar.getHealth() == 50,
                        "An exactly 24-block separation gives 10% protection, with no familiar hit below one point");

                owner.setPos(familiarPosition.add(24.01, 0, 0));
                helper.assertTrue(hurtWithSharing(owner, 10) == 9.9F && familiar.getHealth() == 50,
                        "A companion beyond 24 blocks grants 1% protection and takes no sub-point hit");

                owner.setPos(familiarPosition.add(24, 0, 0));
                helper.assertTrue(hurtWithSharing(owner, 10) == 9 && familiar.getHealth() == 49,
                        "At the original cutoff, the same Nether companion takes one point from the Overworld owner's hit");
            } finally {
                familiar.discard();
                if (forced) nether.setChunkForced(chunk.x, chunk.z, false);
            }
        }).thenSucceed();
    }

    private static FakePlayer owner(ServerLevel level, String name) {
        return new FakePlayer(level, new GameProfile(UUID.randomUUID(), name));
    }

    private static CompoundTag assignFamiliar(FakePlayer owner, UUID id, boolean summoned) {
        var record = new CompoundTag();
        record.putLong("UUIDMost", id.getMostSignificantBits());
        record.putLong("UUIDLeast", id.getLeastSignificantBits());
        record.putBoolean("FamiliarSummoned", summoned);
        record.putInt("FamiliarType", 2);
        var persisted = new CompoundTag();
        persisted.put("WITCFamiliar", record);
        owner.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        return record;
    }

    private static float hurtWithSharing(FakePlayer owner, float amount) {
        var event = new LivingHurtEvent(owner, owner.damageSources().magic(), amount);
        FamiliarBinding.shareDamage(owner, event);
        return event.getAmount();
    }
}
