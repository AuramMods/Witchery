package art.arcane.witchery.ritual;

import art.arcane.witchery.block.FetishBlock;
import art.arcane.witchery.block.entity.FetishBlockEntity;
import art.arcane.witchery.block.entity.FetishEffect;
import art.arcane.witchery.entity.DreamSpirit;
import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.item.DeathClothingItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public final class FetishBinding {
    private FetishBinding() {}

    public static boolean isFetish(ItemStack stack) {
        return stack.getItem() instanceof BlockItem block && block.getBlock() instanceof FetishBlock;
    }

    public static boolean bind(ServerLevel level, BlockPos pos, ItemStack offering, boolean legacy) {
        if (!isFetish(offering)) return false;
        AABB area = new AABB(pos.getX() - 5, pos.getY() - 5, pos.getZ() - 5,
                pos.getX() + 5, pos.getY() + 5, pos.getZ() + 5);
        Predicate<LivingEntity> eligible = spirit -> legacy || spirit.isAlive();
        var spirits = level.getEntitiesOfClass(DreamSpirit.class, area, eligible);
        var undead = level.getEntitiesOfClass(SpectralUndead.class, area, eligible);
        var spectres = undead.stream().filter(mob -> mob.kind() == SpectralUndead.Kind.SPECTRE).toList();
        var banshees = undead.stream().filter(mob -> mob.kind() == SpectralUndead.Kind.BANSHEE).toList();
        var poltergeists = undead.stream().filter(mob -> mob.kind() == SpectralUndead.Kind.POLTERGEIST).toList();

        for (FetishEffect effect : FetishEffect.values()) {
            if (spirits.size() < effect.spirits || spectres.size() < effect.spectres || banshees.size() < effect.banshees
                    || poltergeists.size() < effect.poltergeists) continue;
            if (!legacy && !deliver(level, pos, offering, effect, false)) return false;
            consume(level, spirits, effect.spirits, legacy);
            consume(level, spectres, effect.spectres, legacy);
            consume(level, banshees, effect.banshees, legacy);
            consume(level, poltergeists, effect.poltergeists, legacy);
            return !legacy || deliver(level, pos, offering, effect, true);
        }
        return false;
    }

    private static void consume(ServerLevel level, List<? extends LivingEntity> group, int count, boolean legacy) {
        for (LivingEntity spirit : group.subList(0, count)) {
            if (legacy) {
                spirit.discard();
                WitcheryNetwork.sendRiteLivingOffering(level, spirit.position());
            } else {
                level.sendParticles(ParticleTypes.PORTAL, spirit.getX(), spirit.getY(0.5), spirit.getZ(), 16, 0.45, 0.7, 0.45, 0.1);
                level.playSound(null, spirit.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.7F, 1);
                spirit.discard();
            }
        }
    }

    private static boolean deliver(ServerLevel level, BlockPos pos, ItemStack offering, FetishEffect effect, boolean legacy) {
        Vec3 center = Vec3.atBottomCenterOf(pos);
        if (effect == FetishEffect.DEATH) return callDeath(level, pos, center, legacy);
        ItemStack result = offering.copyWithCount(1);
        FetishBlockEntity.setItemEffect(result, effect);
        var drop = new ItemEntity(level, center.x, center.y + 1.5, center.z, result, 0, 0.3, 0);
        if (legacy) {
            drop.setNoPickUpDelay();
            level.addFreshEntity(drop);
            WitcheryNetwork.sendRiteSpellResult(level, drop.position(), 1.5);
            return true;
        }
        drop.setDefaultPickUpDelay();
        return level.addFreshEntity(drop);
    }

    private static boolean callDeath(ServerLevel level, BlockPos pos, Vec3 center, boolean legacy) {
        ServerPlayer wearer = level.getPlayers(player -> DeathClothingItem.armed(player)
                && (legacy || (player.isAlive() && !player.isSpectator()))).stream().findFirst().orElse(null);
        if (wearer != null) {
            if (legacy) {
                WitcheryTeleport.moveWithLegacyCues(wearer, level, Vec3.atLowerCornerOf(pos));
                WitcheryNetwork.sendRiteDeathCall(level, wearer.position());
                return true;
            }
            wearer.teleportTo(level, center.x, center.y + 0.1, center.z, wearer.getYRot(), wearer.getXRot());
        } else {
            var death = WitcheryEntities.DEATH.get().create(level);
            if (death == null) return false;
            death.moveTo(center.x, center.y + 0.1, center.z, 0, 0);
            if (legacy) {
                level.addFreshEntity(death);
                WitcheryNetwork.sendRiteDeathCall(level, death.position());
                return true;
            }
            if (!level.addFreshEntity(death)) return false;
        }
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, center.x, center.y + 1, center.z, 16, 0.5, 0.75, 0.5, 0);
        level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1, 1);
        return true;
    }
}
