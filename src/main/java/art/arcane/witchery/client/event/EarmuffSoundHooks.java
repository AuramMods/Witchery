package art.arcane.witchery.client.event;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class EarmuffSoundHooks {
    private EarmuffSoundHooks() {
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getItemBySlot(EquipmentSlot.HEAD).is(WitcheryItems.getRequired("earmuffs").get())) {
            event.setSound(null);
        }
    }
}
