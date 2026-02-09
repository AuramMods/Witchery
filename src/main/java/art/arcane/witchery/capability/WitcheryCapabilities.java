package art.arcane.witchery.capability;

import art.arcane.witchery.Witchery;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WitcheryCapabilities {
    public static final Capability<WitcheryPlayerData> PLAYER_DATA = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation PLAYER_DATA_ID = ResourceLocation.parse(Witchery.MODID + ":player_data");

    private WitcheryCapabilities() {
    }

    public static ResourceLocation playerDataId() {
        return PLAYER_DATA_ID;
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(WitcheryPlayerData.class);
    }
}
