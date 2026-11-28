package art.arcane.witchery.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;

public final class MogQuiverArrow extends Arrow {
    public MogQuiverArrow(Player shooter, float power) {
        // Use the vanilla type so the original ForgeData marker is enough after an ordinary arrow reload.
        super(shooter.level(), shooter);
        LegacyArrowLaunch.shoot(this, shooter, shooter.getYRot(), 0.1F, power * 3F * 1.5F, random);
    }
}
