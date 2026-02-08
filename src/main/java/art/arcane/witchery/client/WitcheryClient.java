package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WitcheryClient {
    private WitcheryClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (String name : new String[]{
                    "belladonna", "mandrake", "artichoke", "snowbell", "wormwood", "mindrake", "wolfsbane", "garlicplant",
                    "bloodrose", "glintweed", "embermoss", "crittersnare", "plantmine", "bramble", "somniancotton",
                    "spanishmoss", "vine", "web", "witchsapling", "lilypad", "leapinglily",
                    "rowanwooddoor", "alderwooddoor", "cwoodendoor", "icedoor"
            }) {
                setRenderType(name, RenderType.cutout());
            }

            setRenderType("witchleaves", RenderType.cutoutMipped());

            for (String name : new String[]{"shadedglass", "shadedglass_active", "perpetualice"}) {
                setRenderType(name, RenderType.translucent());
            }
        });
    }

    private static void setRenderType(String legacyName, RenderType renderType) {
        Block block = WitcheryBlocks.getRequired(legacyName).get();
        ItemBlockRenderTypes.setRenderLayer(block, renderType);
    }
}
