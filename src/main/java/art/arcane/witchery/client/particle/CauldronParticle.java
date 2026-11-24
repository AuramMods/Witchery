package art.arcane.witchery.client.particle;

import art.arcane.witchery.particle.CauldronParticleOptions;
import art.arcane.witchery.particle.CauldronParticleOptions.Form;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class CauldronParticle extends TextureSheetParticle {
    private static final int SPARK_FRAMES = 8;
    private static final int BUBBLE_FRAMES = 16;
    private final Form form;
    private final SpriteSet sprites;

    private CauldronParticle(CauldronParticleOptions options, ClientLevel level, double x, double y, double z,
                             SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.form = options.form();
        this.sprites = sprites;
        friction = 1;
        hasPhysics = false;

        int color = options.color();
        float red = (color >> 16 & 255) / 256F;
        float green = (color >> 8 & 255) / 256F;
        float blue = (color & 255) / 256F;
        if (form == Form.BUBBLE) {
            quadSize = 0.04F;
            boolean rising = random.nextInt(4) == 0;
            setLifetime((rising ? 15 : 10) + random.nextInt(10));
            gravity = rising ? -0.02F : 0;
            if (!rising) setParticleSpeed(0, 0, 0);
            setColor(red, green, blue);
        } else {
            quadSize = 0.06F;
            setLifetime(25 + random.nextInt(10));
            gravity = form == Form.CIRCLING_SPARK ? 0 : 0.25F;
            setParticleSpeed(random.nextDouble() * 0.08 - 0.04, random.nextDouble() * 0.05 + 0.08,
                    random.nextDouble() * 0.08 - 0.04);
            setColor(shiftColor(red), shiftColor(green), shiftColor(blue));
        }
        updateSprite();
    }

    public static ParticleProvider<CauldronParticleOptions> provider(SpriteSet sprites) {
        return (options, level, x, y, z, dx, dy, dz) -> new CauldronParticle(options, level, x, y, z, sprites);
    }

    private float shiftColor(float value) {
        return Mth.clamp(value + random.nextFloat() * 0.4F - 0.2F, 0, 1);
    }

    @Override
    public void tick() {
        hasPhysics = age + 1 > lifetime * 0.9;
        if (form == Form.CIRCLING_SPARK) {
            Vec3 rotated = new Vec3(xd, yd, zd).yRot(0.5F);
            setParticleSpeed(rotated.x * 1.08, rotated.y * 0.85, rotated.z * 1.08);
        }
        super.tick();
        updateSprite();
    }

    private void updateSprite() {
        int phase = age * 8 / 20 % BUBBLE_FRAMES;
        int frame = form == Form.BUBBLE ? SPARK_FRAMES + phase
                : phase < SPARK_FRAMES ? phase : BUBBLE_FRAMES - 1 - phase;
        setSprite(sprites.get(frame, SPARK_FRAMES + BUBBLE_FRAMES - 1));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
