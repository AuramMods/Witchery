package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PlayerCompassTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_player_compass")
    public static void aSavedCompassKeepsItsBindingWithoutServerReadings(GameTestHelper helper) {
        var level = helper.getLevel();
        var holder = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "CompassHolder"));
        var targetId = UUID.randomUUID();
        var compass = new ItemStack(WitcheryItems.getRequired("playercompass").get());
        TaglockItem.bindPlayer(compass, "OfflineTarget", targetId);
        compass.getOrCreateTag().putInt("Needle", 31);
        compass.getOrCreateTag().putInt("FixtureMarker", 42);
        var restored = ItemStack.of(compass.save(new CompoundTag()));
        holder.getInventory().setItem(0, restored);
        helper.startSequence().thenWaitUntil(() ->
                helper.assertTrue(level.getGameTime() % 10 == 2, "Wait for the earlier server sampling phase"))
                .thenExecute(() -> {
                    try {
                        long seed = 42;
                        level.random.setSeed(seed);
                        holder.getInventory().tick();
                        var saved = ItemStack.of(restored.save(new CompoundTag()));
                        var tag = saved.getTag();
                        helper.assertTrue(tag != null && !tag.contains("Needle") && tag.getInt("FixtureMarker") == 42
                                        && "OfflineTarget".equals(TaglockItem.boundPlayerName(saved))
                                        && targetId.equals(TaglockItem.boundPlayerId(saved)),
                                "The server clears the old frame and preserves the original binding and custom data");
                        helper.assertTrue(level.random.nextLong() == RandomSource.create(seed).nextLong(),
                                "An unavailable target does not draw from the server world");
                    } finally {
                        holder.discard();
                    }
                }).thenSucceed();
    }
}
