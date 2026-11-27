package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.registries.ForgeRegistries;

public final class WitcheryEffectIcons implements IClientMobEffectExtensions {
    public static final WitcheryEffectIcons INSTANCE = new WitcheryEffectIcons();

    private WitcheryEffectIcons() {
    }

    public static boolean owns(MobEffect effect) {
        var id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        return id != null && Witchery.MODID.equals(id.getNamespace());
    }

    public static boolean hasMissingSprite(MobEffect effect) {
        var sprite = Minecraft.getInstance().getMobEffectTextures().get(effect);
        return MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name());
    }

    @Override
    public boolean isVisibleInGui(MobEffectInstance effect) {
        return !hasMissingSprite(effect.getEffect());
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance effect, EffectRenderingInventoryScreen<?> screen,
                                       GuiGraphics graphics, int x, int y, int blitOffset) {
        return hasMissingSprite(effect.getEffect());
    }
}
