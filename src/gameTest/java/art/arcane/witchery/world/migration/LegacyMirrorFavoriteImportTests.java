package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.block.entity.MirrorBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyMirrorFavoriteImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_mirror_favorite")
    public static void keepsLegacyFavoriteProfileThroughItemAndConversation(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var mirrorBlock = (MirrorBlock) WitcheryBlocks.getRequired("mirrorblock").get();
        var frame = helper.absolutePos(new BlockPos(3, 5, 4));
        var remountedFrame = helper.absolutePos(new BlockPos(7, 5, 4));
        helper.assertTrue(mirrorBlock.placePair(level, frame, Direction.NORTH)
                && mirrorBlock.placePair(level, remountedFrame, Direction.NORTH), "Place two exterior mirror frames");
        var legacy = TagParser.parseTag("""
                {Favorite:{Name:"LegacyFavorite", Id:"5396dde2-1485-447c-b08c-4f3eb2c86b50", Properties:{
                    textures:[{Value:"original-texture",Signature:"original-signature"}],
                    note:[{Value:"old-value"}]}}, PlayersSeen:["LegacyVisitor"]}
                """);
        legacy.putLong("LastFairestSpawnTime", level.getGameTime());
        var original = (MirrorBlockEntity) level.getBlockEntity(frame);
        original.load(legacy);

        var remounted = (MirrorBlockEntity) level.getBlockEntity(remountedFrame);
        remounted.applyItem(original.toItem());
        var saved = remounted.saveWithoutMetadata();
        var profile = saved.getCompound("Favorite");
        var favoriteId = UUID.fromString("5396dde2-1485-447c-b08c-4f3eb2c86b50");
        helper.assertTrue(profile.hasUUID("Id") && profile.getUUID("Id").equals(favoriteId)
                        && saved.getList("PlayersSeen", Tag.TAG_STRING).getString(0).equals("LegacyVisitor"),
                "A legacy favorite retains its UUID and visitor list through the portable item");
        assertTexture(helper, profile);

        var ownerProfile = new GameProfile(favoriteId, "RenamedFavorite");
        ownerProfile.getProperties().put("note", new Property("note", "updated-value"));
        var player = new FakePlayer(level, ownerProfile);
        remounted.interact(level, player, InteractionHand.MAIN_HAND);
        var after = remounted.saveWithoutMetadata();
        var updated = after.getCompound("Favorite");
        var notes = updated.getCompound("Properties").getList("note", Tag.TAG_COMPOUND);
        helper.assertTrue(updated.getString("Name").equals("RenamedFavorite") && updated.getUUID("Id").equals(favoriteId)
                        && notes.size() == 1 && notes.getCompound(0).getString("Value").equals("updated-value")
                        && after.getList("PlayersSeen", Tag.TAG_STRING).isEmpty(),
                "The returning favorite refreshes the live properties and clears the visitor list");
        assertTexture(helper, updated);
        helper.succeed();
    }

    private static void assertTexture(GameTestHelper helper, CompoundTag profile) {
        var texture = profile.getCompound("Properties").getList("textures", Tag.TAG_COMPOUND).getCompound(0);
        helper.assertTrue(texture.getString("Value").equals("original-texture")
                        && texture.getString("Signature").equals("original-signature"),
                "The original signed texture stays available when the returning profile has no texture property");
    }
}
