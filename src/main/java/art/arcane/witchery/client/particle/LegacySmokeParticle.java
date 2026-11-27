package art.arcane.witchery.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public final class LegacySmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private LegacySmokeParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                                SpriteSet sprites, float scale) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = sprites;
        xd = xd * 0.1F + dx;
        yd = yd * 0.1F + dy;
        zd = zd * 0.1F + dz;
        float shade = (float) (random.nextDouble() * 0.3F);
        setColor(shade, shade, shade);
        quadSize *= 0.75F;
        quadSize *= scale;
        lifetime = (int) ((int) (8.0D / (random.nextDouble() * 0.8D + 0.2D)) * scale);
        setSpriteFromAge(sprites);
    }

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites, float scale) {
        return (type, level, x, y, z, dx, dy, dz) -> new LegacySmokeParticle(level, x, y, z, dx, dy, dz, sprites, scale);
    }

    public void applyTint(int color) {
        hasPhysics = false;
        setColor(((color >>> 16) & 255) / 256.0F, ((color >>> 8) & 255) / 256.0F, (color & 255) / 256.0F);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return quadSize * Mth.clamp((age + partialTick) / lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        setSprite(sprites.get(Math.min(age * 8 / lifetime, 7), 7));
        yd += 0.004D;
        move(xd, yd, zd);
        if (y == yo) {
            xd *= 1.1D;
            zd *= 1.1D;
        }
        xd *= 0.96F;
        yd *= 0.96F;
        zd *= 0.96F;
        if (onGround) {
            xd *= 0.7F;
            zd *= 0.7F;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
}
