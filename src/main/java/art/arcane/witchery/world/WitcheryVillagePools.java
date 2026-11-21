package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.mixin.SinglePoolElementAccessor;
import art.arcane.witchery.mixin.StructureTemplatePoolAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class WitcheryVillagePools {
    public static final ResourceLocation BOOKSHOP = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "village/bookshop");
    private static final ResourceKey<StructureProcessorList> BOOKSHOP_PROCESSORS = ResourceKey.create(Registries.PROCESSOR_LIST, BOOKSHOP);
    private static final List<String> VILLAGE_STYLES = List.of("plains", "desert", "savanna", "snowy", "taiga");
    private static final int BOOKSHOP_WEIGHT = 15;

    private WitcheryVillagePools() {}

    @SubscribeEvent
    public static void addVillagePieces(ServerAboutToStartEvent event) {
        if (!WitcheryServerConfig.GENERATE_BOOK_SHOPS.get()) return;
        var registries = event.getServer().registryAccess();
        var pools = registries.registryOrThrow(Registries.TEMPLATE_POOL);
        var processors = registries.registryOrThrow(Registries.PROCESSOR_LIST).getHolderOrThrow(BOOKSHOP_PROCESSORS);
        StructurePoolElement bookshop = StructurePoolElement.single(BOOKSHOP.toString(), processors)
                .apply(StructureTemplatePool.Projection.RIGID);
        for (String style : VILLAGE_STYLES) {
            var poolId = ResourceLocation.withDefaultNamespace("village/" + style + "/houses");
            pools.getOptional(poolId).ifPresent(pool -> addBookshop(pool, bookshop));
        }
    }

    private static void addBookshop(StructureTemplatePool pool, StructurePoolElement bookshop) {
        var access = (StructureTemplatePoolAccessor) pool;
        var entries = new ArrayList<>(access.witchery$getRawTemplates());
        if (entries.stream().anyMatch(entry -> isBookshop(entry.getFirst()))) return;
        entries.add(Pair.of(bookshop, BOOKSHOP_WEIGHT));
        access.witchery$setRawTemplates(entries);
        access.witchery$getTemplates().addAll(Collections.nCopies(BOOKSHOP_WEIGHT, bookshop));
    }

    public static boolean isBookshop(StructurePoolElement element) {
        return element instanceof SinglePoolElement single
                && ((SinglePoolElementAccessor) single).witchery$getTemplate().left().filter(BOOKSHOP::equals).isPresent();
    }
}
