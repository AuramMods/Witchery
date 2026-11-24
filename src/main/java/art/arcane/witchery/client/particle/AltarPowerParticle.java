package art.arcane.witchery.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class AltarPowerParticle extends TextureSheetParticle {
    private static final double SPEED = 0.25;
    private final SpriteSet sprites;

    private AltarPowerParticle(ClientLevel level, double x, double y, double z,
                               double offsetX, double offsetY, double offsetZ, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        double distance = Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
        setLifetime((int) (distance / SPEED));
        double scale = distance > 0 ? SPEED / distance : 0;
        setParticleSpeed(offsetX * scale, offsetY * scale, offsetZ * scale);
        friction = 1;
        gravity = 0;
        hasPhysics = false;
        quadSize = 0.1F;
        setColor(0.2F, 0.8F, 0);
        setSprite(sprites.get(0, 7));
    }

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites) {
        return (type, level, x, y, z, offsetX, offsetY, offsetZ) ->
                new AltarPowerParticle(level, x, y, z, offsetX, offsetY, offsetZ, sprites);
    }

    @Override
    public void tick() {
        hasPhysics = age + 1 > lifetime * 0.9;
        super.tick();
        int frame = age * 8 / 20 % 16;
        setSprite(sprites.get(frame > 7 ? 15 - frame : frame, 7));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
