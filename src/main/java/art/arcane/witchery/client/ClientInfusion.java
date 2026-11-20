package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryClientConfig;
import art.arcane.witchery.infusion.CreaturePower;
import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.infusion.LightInfusion;
import art.arcane.witchery.item.WitchHandItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class ClientInfusion {
    private static final ResourceLocation FRAME =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/glass.png");
    private static final ResourceLocation ICONS =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/gui/creatures.png");
    private static final ResourceLocation CLAY = ResourceLocation.withDefaultNamespace("textures/block/clay.png");
    private static final int WIDTH = 8;
    private static final int HEIGHT = 32;
    private static int infusion;
    private static int charge;
    private static int capacity;
    private static int powerId;
    private static int powerCharge;

    private ClientInfusion() {}

    public static void setStatus(int type, int current, int max, int creature, int remaining) {
        infusion = type;
        capacity = Math.max(max, 0);
        charge = Mth.clamp(current, 0, capacity);
        powerId = type == InfernalInfusion.ID && CreaturePower.forId(creature) != null ? creature : 0;
        powerCharge = Mth.clamp(remaining, 0, CreaturePower.MAX_CHARGES);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        setStatus(0, 0, 0, 0, 0);
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player) || player.isPassenger()) return;
        CreaturePower power = CreaturePower.forId(powerId);
        boolean light = infusion == LightInfusion.ID && player.isUsingItem() && player.getUseItem().getItem() instanceof WitchHandItem;
        if (!light && power == null) return;
        boolean onIce = player.level().getBlockState(player.getOnPos()).is(Blocks.ICE);
        if (light && (player.onGround() || onIce)) {
            double speed = onIce || player.isInWater() ? 1.1 : 1.65;
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * speed, motion.y, motion.z * speed);
        }
        if (power == null) return;
        Vec3 motion = player.getDeltaMovement();
        var input = event.getInput();
        switch (power) {
            case CAVE_SPIDER, SPIDER -> {
                double y = player.level().getBlockState(player.blockPosition().above()).blocksMotion() ? motion.y * 0.6 : motion.y;
                if (player.horizontalCollision) y = input.shiftKeyDown ? 0 : 0.3;
                player.setDeltaMovement(motion.x, y, motion.z);
            }
            case BAT, OWL -> {
                if (input.jumping && !player.onGround()) player.setDeltaMovement(motion.x, 0.42, motion.z);
                else if (input.jumping && motion.y > 0) player.setDeltaMovement(motion.x, motion.y + 0.067, motion.z);
            }
            case SQUID -> {
                if (player.isInWater() && input.up) player.setDeltaMovement(motion.x * 1.15, motion.y, motion.z * 1.15);
            }
            case MAGMA_CUBE, SLIME, TOAD -> {
                if (input.jumping && motion.y > 0) player.setDeltaMovement(motion.x, motion.y + 0.06, motion.z);
            }
            case SILVERFISH, OCELOT, WOLF, HORSE -> {
                if ((input.up || input.down || input.left || input.right) && (onIce || (player.onGround() && !player.isInWater()))) {
                    double speed = onIce ? 1.1 : 1.45;
                    player.setDeltaMovement(motion.x * speed, motion.y, motion.z * speed);
                }
            }
        }
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
        if (powerId > 0) {
            int powerX = WitcheryClientConfig.GUI_ON_LEFT.get() ? x + 10 : x - 10;
            for (int i = 0; i < powerCharge; i++) {
                graphics.blit(CLAY, powerX, y + HEIGHT - 1 - i * 2, WIDTH, 1, 0F, 0F, 16, 2, 16, 16);
            }
            graphics.blit(ICONS, powerX, y + HEIGHT + 1, 0, (powerId - 1) * 8, 8, 8, 256, 256);
        }
        RenderSystem.disableBlend();
    }
}
