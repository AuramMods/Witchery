package art.arcane.witchery.item;

import art.arcane.witchery.entity.WitchHunterPursuit;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class VoodooPoppetItem extends BoundPoppetItem {
    private static final int USE_TICKS = 80;
    private static final int ATTACK_WEAR = 10;
    private static final int DEFLECTION_WEAR = 350;

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TICKS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        if (!(level instanceof ServerLevel server) || !(user instanceof ServerPlayer caster)) return;
        LivingEntity target = TaglockItem.resolveTarget(server, stack);
        if (target == null || !target.isAlive()) {
            caster.displayClientMessage(Component.translatable("message.witchery.voodoo_poppet.no_target"), true);
            return;
        }
        WitchHunterPursuit.noticeBlackMagic(caster);

        HitResult aim = caster.pick(5, 1, true);
        if (aim instanceof BlockHitResult hit && aim.getType() == HitResult.Type.BLOCK
                && server.getFluidState(hit.getBlockPos()).is(FluidTags.LAVA)) {
            if (!warded(caster, target, stack)) target.setSecondsOnFire(10);
            wear(stack, USE_LIMIT);
            caster.playSound(SoundEvents.FIRE_EXTINGUISH, 0.4F, 2.0F);
        } else if (!caster.isShiftKeyDown()) {
            if (warded(caster, target, stack)) return;
            Vec3 look = caster.getLookAngle();
            int strength = (USE_TICKS - remaining) / 20;
            target.setDeltaMovement(look.multiply(0.9, 0.3, 0.9).scale(strength));
            target.hurtMarked = true;
            wear(stack, ATTACK_WEAR);
        } else {
            prick(server, caster, target, stack);
        }
    }

    private static void prick(ServerLevel level, ServerPlayer caster, LivingEntity target, ItemStack poppet) {
        boolean creative = caster.getAbilities().instabuild;
        int slot = -1;
        if (!creative) {
            for (int candidate = 0; candidate < caster.getInventory().items.size(); candidate++) {
                if (caster.getInventory().getItem(candidate).is(WitcheryIngredient.BONE_NEEDLE.item())) {
                    slot = candidate;
                    break;
                }
            }
            if (slot == -1) return;
        }
        if (warded(caster, target, poppet)) return;
        DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.MAGIC), caster);
        target.hurt(source, 0.5F);
        if (!creative) {
            caster.getInventory().removeItem(slot, 1);
            wear(poppet, ATTACK_WEAR);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        if (!(level instanceof ServerLevel server) || !holder.isEyeInFluid(FluidTags.WATER) || holder.getAirSupply() > 0) return;
        LivingEntity target = TaglockItem.resolveTarget(server, stack);
        if (target == null || !target.isAlive() || warded(holder, target, stack)) return;
        if (!target.canBreatheUnderwater() && !target.hasEffect(MobEffects.WATER_BREATHING)
                && (!(target instanceof Player player) || !player.getAbilities().invulnerable)) {
            target.hurt(target.damageSources().drown(), 1);
            if (target.level() instanceof ServerLevel targetLevel) {
                targetLevel.sendParticles(ParticleTypes.BUBBLE, target.getX(), target.getEyeY(), target.getZ(),
                        8, 0.5, 0.5, 0.5, 0);
            }
        }
        target.clearFire();
    }

    static boolean warded(Entity caster, LivingEntity target, ItemStack poppet) {
        if (!(target instanceof ServerPlayer player)) return false;
        if (!useWard(player, WitcheryItems.getRequired("voodoo_protection_poppet").get(), DEFLECTION_WEAR)) return false;
        if (caster instanceof ServerPlayer attacker) {
            wear(poppet, DEFLECTION_WEAR);
            strikeCaster(attacker);
        }
        return true;
    }

    static void strikeCaster(ServerPlayer attacker) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(attacker.serverLevel());
        if (lightning != null) {
            lightning.moveTo(attacker.getX(), attacker.getY(), attacker.getZ());
            attacker.serverLevel().addFreshEntity(lightning);
        }
    }
}
