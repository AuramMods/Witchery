package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrewPlantBlocks;
import art.arcane.witchery.block.LeapingLilyBlock;
import art.arcane.witchery.brewing.EnvironmentalCurseEffect;
import art.arcane.witchery.config.WitcheryClientConfig;
import art.arcane.witchery.infusion.CreaturePower;
import art.arcane.witchery.infusion.InfusedBrews;
import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.infusion.LightInfusion;
import art.arcane.witchery.item.BrewBagItem;
import art.arcane.witchery.item.DeathClothingItem;
import art.arcane.witchery.item.MysticBranchItem;
import art.arcane.witchery.item.WitchHandItem;
import art.arcane.witchery.ritual.DirectedCurse;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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
    private static final float LEGACY_PLAYER_Y_OFFSET = 1.62F;
    private static final double LEGACY_BAT_LIFT = 0.06699999910593032D;
    private static final double LEGACY_HELD_ITEM_SPEED = 1.6500000476837158D;
    private static int infusion;
    private static int charge;
    private static int capacity;
    private static int powerId;
    private static int powerCharge;
    private static int brewId;
    private static long brewExpires;

    private ClientInfusion() {}

    public static void setStatus(int type, int current, int max, int creature, int remaining, int brew, int brewTicks, int sinking) {
        infusion = type;
        capacity = Math.max(max, 0);
        charge = Mth.clamp(current, 0, capacity);
        powerId = type == InfernalInfusion.ID && CreaturePower.forId(creature) != null ? creature : 0;
        powerCharge = Mth.clamp(remaining, 0, CreaturePower.MAX_CHARGES);
        brewId = brew;
        brewExpires = Util.getMillis() + 50L * brewTicks;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) DirectedCurse.syncSinking(player, sinking);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        setStatus(0, 0, 0, 0, 0, 0, 0, 0);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        var client = Minecraft.getInstance();
        var player = client.player;
        if (player == null) return;

        var controls = client.options;
        CreaturePower power = CreaturePower.forId(powerId);
        boolean speedPower = false;
        if (power != null) {
            Vec3 motion = player.getDeltaMovement();
            switch (power) {
                case CAVE_SPIDER, SPIDER -> {
                    var overhead = BlockPos.containing(player.getX(), player.getY() + LEGACY_PLAYER_Y_OFFSET + 1, player.getZ());
                    var state = player.level().getBlockState(overhead);
                    // Both original webs used solid materials for the ceiling check.
                    boolean solid = state.blocksMotion() || state.is(Blocks.COBWEB) || state.getBlock() instanceof BrewPlantBlocks.Web;
                    double y = solid ? motion.y * 0.6 : motion.y;
                    if (player.horizontalCollision) y = player.isShiftKeyDown() ? 0 : 0.3;
                    player.setDeltaMovement(motion.x, y, motion.z);
                }
                case BAT, OWL -> {
                    if (controls.keyJump.isDown()) {
                        if (!player.onGround()) player.setDeltaMovement(motion.x, 0.42F, motion.z);
                        else if (motion.y > 0) player.setDeltaMovement(motion.x, motion.y + LEGACY_BAT_LIFT, motion.z);
                    }
                }
                case SQUID -> {
                    if (player.isInWater() && controls.keyUp.isDown()) scaleHorizontalMotion(player, 1.15F);
                }
                case MAGMA_CUBE, SLIME, TOAD -> {
                    if (controls.keyJump.isDown() && motion.y > 0) player.setDeltaMovement(motion.x, motion.y + 0.06, motion.z);
                }
                case SILVERFISH, OCELOT, WOLF, HORSE -> {
                    speedPower = true;
                    if (controls.keyUp.isDown() || controls.keyDown.isDown() || controls.keyLeft.isDown() || controls.keyRight.isDown()) {
                        if (isOverIce(player)) scaleHorizontalMotion(player, 1.1F);
                        else if (player.onGround() && !player.isInWater()) scaleHorizontalMotion(player, 1.45F);
                    }
                }
            }
        }

        var item = player.getUseItem().getItem();
        if (!speedPower && player.isUsingItem() && (item instanceof MysticBranchItem || item instanceof BrewBagItem)) {
            Vec3 motion = player.getDeltaMovement();
            if (Math.abs(motion.x) <= 0.1 && Math.abs(motion.z) <= 0.1) applyHeldItemMovement(player);
        }

        int sinking = DirectedCurse.SINKING.strength(player);
        if (sinking > 0) {
            EnvironmentalCurseEffect.sinkPlayer(player, sinking - 1);
        } else {
            var slowness = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            if (slowness != null && slowness.getAmplifier() > 4) EnvironmentalCurseEffect.sinkFlight(player);
            if (sinking == 0) DeathClothingItem.liftInLiquid(player);
        }

        if (player.onGround() && controls.keyJump.isDown()) {
            var pad = BlockPos.containing(player.getX(), player.getY() + LEGACY_PLAYER_Y_OFFSET, player.getZ()).below();
            if (player.level().getBlockState(pad).getBlock() instanceof LeapingLilyBlock) {
                float pitch = 0.4F / ((float) player.level().random.nextDouble() * 0.4F + 0.8F);
                // Draw the playback seed separately from the original pitch roll.
                player.level().playSeededSound(player, player.getX(), player.getY(), player.getZ(),
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ARROW_HIT), SoundSource.PLAYERS, 1, pitch,
                        SoundInstance.createUnseededRandom().nextLong());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLightHandUseTick(LivingEntityUseItemEvent.Tick event) {
        if (infusion == LightInfusion.ID && event.getDuration() > 0 && event.getItem().getItem() instanceof WitchHandItem
                && event.getEntity() instanceof LocalPlayer player && !player.isPassenger()) {
            applyHeldItemMovement(player);
        }
    }

    private static void applyHeldItemMovement(LocalPlayer player) {
        if (isOverIce(player)) scaleHorizontalMotion(player, 1.1F);
        else if (player.onGround()) scaleHorizontalMotion(player, player.isInWater() ? 1.1F : LEGACY_HELD_ITEM_SPEED);
    }

    private static boolean isOverIce(LocalPlayer player) {
        // The original local player's Y coordinate included its 1.62-block offset from the feet.
        var sampled = BlockPos.containing(player.getX(), player.getY() + LEGACY_PLAYER_Y_OFFSET - 2, player.getZ());
        return player.level().getBlockState(sampled).is(Blocks.ICE);
    }

    private static void scaleHorizontalMotion(LocalPlayer player, double speed) {
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x * speed, motion.y, motion.z * speed);
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.player.isSpectator()
                || client.screen != null || client.options.hideGui) return;
        long brewTime = brewExpires - Util.getMillis();
        if (brewId != 0 && brewTime > 0) {
            int left = WitcheryClientConfig.GUI_ON_LEFT.get() ? 17 : width - 23;
            int top = height / 2 + 26;
            RenderSystem.enableBlend();
            graphics.blit(ICONS, left, top, 16, brewId == InfusedBrews.GRAVE ? 16 : 0, 16, 16, 256, 256);
            RenderSystem.disableBlend();
            graphics.drawCenteredString(client.font, Long.toString((brewTime + 59_999) / 60_000), left + 8, top + 10, 0xEEFFFFFF);
        }
        if (capacity == 0) return;
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
