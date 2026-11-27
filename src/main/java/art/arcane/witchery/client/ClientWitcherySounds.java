package art.arcane.witchery.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public final class ClientWitcherySounds {
    private ClientWitcherySounds() {}

    public static void playLocal(Level level, double x, double y, double z, SoundEvent sound,
                                 SoundSource source, float volume, float pitch) {
        // The playback sample needs an independent seed so it leaves the world's random sequence alone.
        level.playSeededSound(Minecraft.getInstance().player, x, y, z, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                source, volume, pitch, SoundInstance.createUnseededRandom().nextLong());
    }
}
