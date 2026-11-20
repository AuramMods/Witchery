package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class InfusionChargeOverlay {
    private static final ResourceLocation FRAME =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/glass.png");
    private static final ResourceLocation ICONS =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/creatures.png");
    private static final int WIDTH = 8;
    private static final int HEIGHT = 32;
    private static int infusion;
    private static int charge;
    private static int capacity;

    private InfusionChargeOverlay() {}

    public static void setStatus(int type, int current, int max) {
        infusion = type;
        capacity = Math.max(max, 0);
        charge = Mth.clamp(current, 0, capacity);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        setStatus(0, 0, 0);
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft client = Minecraft.getInstance();
        if (capacity == 0 || client.player == null || client.player.isSpectator()
                || client.screen != null || client.options.hideGui) return;
        Block material = switch (infusion) {
            case 1 -> Blocks.SNOW_BLOCK;
            case 2 -> Blocks.DIRT;
            case 3 -> Blocks.NETHER_PORTAL;
            case 4 -> Blocks.NETHERRACK;
            default -> null;
        };
        if (material == null) return;

        int x = WitcheryClientConfig.GUI_ON_LEFT.get() ? 20 : width - 20;
        int y = height / 2 - HEIGHT / 2;
        int fill = Math.round((float) charge / capacity * HEIGHT);
        if (fill > 0) {
            TextureAtlasSprite sprite = client.getBlockRenderer().getBlockModelShaper()
                    .getParticleIcon(material.defaultBlockState());
            graphics.enableScissor(x, y + HEIGHT - fill, x + WIDTH, y + HEIGHT);
            for (int row = 0; row < HEIGHT; row += WIDTH) {
                graphics.blit(x, y + row, 0, WIDTH, WIDTH, sprite);
            }
            graphics.disableScissor();
        }

        RenderSystem.enableBlend();
        graphics.blit(FRAME, x, y, WIDTH, HEIGHT, 0F, 0F, 8, 1, 8, 1);
        graphics.blit(ICONS, x, y + HEIGHT + 1, 8, (infusion - 1) * 8, 8, 8, 256, 256);
        RenderSystem.disableBlend();
    }
}
