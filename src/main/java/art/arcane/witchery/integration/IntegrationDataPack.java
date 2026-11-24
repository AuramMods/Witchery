package art.arcane.witchery.integration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryCommonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.resource.PathPackResources;

import java.util.Set;

public final class IntegrationDataPack {
    private IntegrationDataPack() {}

    public static void addPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        var root = ModList.get().getModFileById(Witchery.MODID).getFile().findResource("mod_integration");
        event.addRepositorySource(packs -> {
            var pack = Pack.readMetaAndCreate(Witchery.MODID + ":mod_integration",
                    Component.literal("Witchery mod integration"), true,
                    id -> new PathPackResources(id, true, root) {
                        @Override
                        public Set<String> getNamespaces(PackType type) {
                            // Keep the pack ID available to saved worlds in both configuration states.
                            return WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get() ? super.getNamespaces(type) : Set.of();
                        }
                    }, PackType.SERVER_DATA, Pack.Position.BOTTOM, PackSource.BUILT_IN);
            if (pack != null) packs.accept(pack);
        });
    }
}
