package art.arcane.witchery.ritual;

import art.arcane.witchery.block.FetishBlock;
import art.arcane.witchery.block.entity.FetishEffect;
import art.arcane.witchery.entity.DreamSpirit;
import art.arcane.witchery.entity.SpectralUndead;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class FetishBinding {
    private FetishBinding() {}

    public static boolean isFetish(ItemStack stack) {
        return stack.getItem() instanceof BlockItem block && block.getBlock() instanceof FetishBlock;
    }

    public static boolean bind(ServerLevel level, BlockPos pos, ItemStack offering) {
        if (!isFetish(offering)) return false;
        Vec3 center = Vec3.atBottomCenterOf(pos);
        AABB area = new AABB(pos.getX() - 5, pos.getY() - 5, pos.getZ() - 5,
                pos.getX() + 5, pos.getY() + 5, pos.getZ() + 5);
        var spirits = level.getEntitiesOfClass(DreamSpirit.class, area, LivingEntity::isAlive);
        var undead = level.getEntitiesOfClass(SpectralUndead.class, area, LivingEntity::isAlive);
        var spectres = undead.stream().filter(mob -> mob.kind() == SpectralUndead.Kind.SPECTRE).toList();
        var banshees = undead.stream().filter(mob -> mob.kind() == SpectralUndead.Kind.BANSHEE).toList();
        var poltergeists = undead.stream().filter(mob -> mob.kind() == SpectralUndead.Kind.POLTERGEIST).toList();

        for (FetishEffect effect : FetishEffect.values()) {
            if (spirits.size() < effect.spirits || spectres.size() < effect.spectres || banshees.size() < effect.banshees
                    || poltergeists.size() < effect.poltergeists) continue;
            ItemStack result = offering.copyWithCount(1);
            result.getOrCreateTag().putInt("WITCSpiritEffect", effect.id);
            var data = result.getTagElement("TileData");
            if (data != null) data.putInt("EffectTypeID", effect.id);
            var drop = new ItemEntity(level, center.x, center.y + 1.5, center.z, result, 0, 0.3, 0);
            drop.setDefaultPickUpDelay();
            if (!level.addFreshEntity(drop)) return false;

            List<LivingEntity> selected = new ArrayList<>();
            selected.addAll(spirits.subList(0, effect.spirits));
            selected.addAll(spectres.subList(0, effect.spectres));
            selected.addAll(banshees.subList(0, effect.banshees));
            selected.addAll(poltergeists.subList(0, effect.poltergeists));
            for (LivingEntity spirit : selected) {
                level.sendParticles(ParticleTypes.PORTAL, spirit.getX(), spirit.getY(0.5), spirit.getZ(), 16, 0.45, 0.7, 0.45, 0.1);
                level.playSound(null, spirit.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.7F, 1);
                spirit.discard();
            }
            return true;
        }
        return false;
    }
}
