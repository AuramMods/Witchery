package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ChunkPromotionTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_chunk_promotion")
    public static void aQueuedPromotionKeepsTheLiveChunk(GameTestHelper helper) throws ReflectiveOperationException {
        var level = helper.getLevel();
        var pos = new ChunkPos(311, 311);
        var chestPos = pos.getBlockAt(8, 100, 8);
        var staleInput = new ProtoChunk(pos, UpgradeData.EMPTY, level, level.registryAccess().registryOrThrow(Registries.BIOME), null);
        boolean forced = level.setChunkForced(pos.x, pos.z, true);
        try {
            level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
            var chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
            chest.setItem(2, new ItemStack(Items.REDSTONE, 10));
            LevelChunk live = level.getChunkAt(chestPos);
            ChunkMap chunks = level.getChunkSource().chunkMap;
            var visible = ObfuscationReflectionHelper.findMethod(ChunkMap.class, "m_140327_", long.class);
            var holder = (ChunkHolder) visible.invoke(chunks, pos.toLong());
            var promote = ObfuscationReflectionHelper.findMethod(ChunkMap.class, "m_214854_", ChunkHolder.class, ChunkAccess.class);
            var result = (LevelChunk) promote.invoke(chunks, holder, staleInput);

            helper.assertTrue(result == live && result.getBlockEntity(chestPos) == chest,
                    "The queued full promotion must reuse the populated live chunk");
            helper.assertTrue(result.getBlockEntityNbtForSaving(chestPos).getList("Items", Tag.TAG_COMPOUND)
                            .getCompound(0).getByte("Count") == 10,
                    "The promoted result must save the chest's ten redstone");
            helper.succeed();
        } finally {
            if (forced) level.setChunkForced(pos.x, pos.z, false);
        }
    }
}
