package art.arcane.witchery.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public record ClientPlayerAppearance(ResourceLocation texture, String model) {
    private static final Cache<String, ClientPlayerAppearance> SKINS = CacheBuilder.newBuilder()
            .maximumSize(256).expireAfterWrite(30, TimeUnit.MINUTES).build();

    public static ClientPlayerAppearance of(String name) {
        if (name.isEmpty()) return new ClientPlayerAppearance(DefaultPlayerSkin.getDefaultSkin(), "default");
        Minecraft minecraft = Minecraft.getInstance();
        String key = name.toLowerCase(Locale.ROOT);
        var connection = minecraft.getConnection();
        var online = connection == null ? null : connection.getPlayerInfo(name);
        ClientPlayerAppearance cached = SKINS.getIfPresent(key);
        if (online != null) {
            var appearance = new ClientPlayerAppearance(online.getSkinLocation(), online.getModelName());
            if (!appearance.equals(cached)) SKINS.put(key, appearance);
            return appearance;
        }
        if (cached != null) return cached;
        var uuid = UUIDUtil.createOfflinePlayerUUID(name);
        var fallback = new ClientPlayerAppearance(DefaultPlayerSkin.getDefaultSkin(uuid), DefaultPlayerSkin.getSkinModelName(uuid));
        SKINS.put(key, fallback);
        if (name.matches("[A-Za-z0-9_]{1,16}")) loadOffline(minecraft, name, key);
        return fallback;
    }

    private static void loadOffline(Minecraft minecraft, String name, String key) {
        Util.backgroundExecutor().execute(() -> new YggdrasilAuthenticationService(minecraft.getProxy())
                .createProfileRepository().findProfilesByNames(new String[]{name}, Agent.MINECRAFT, new ProfileLookupCallback() {
                    @Override
                    public void onProfileLookupSucceeded(GameProfile profile) {
                        minecraft.getSkinManager().registerSkins(profile, (type, location, texture) -> {
                            if (type == MinecraftProfileTexture.Type.SKIN) {
                                String model = "slim".equals(texture.getMetadata("model")) ? "slim" : "default";
                                SKINS.put(key, new ClientPlayerAppearance(location, model));
                            }
                        }, true);
                    }

                    @Override
                    public void onProfileLookupFailed(GameProfile profile, Exception error) {
                    }
                }));
    }
}
