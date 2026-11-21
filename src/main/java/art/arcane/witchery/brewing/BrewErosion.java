package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.ThrownBrew;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class BrewErosion {
    private static final Set<Item> CORRODIBLE_GEAR = Set.of(
            Items.IRON_SWORD, Items.IRON_SHOVEL, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_HOE,
            Items.GOLDEN_SWORD, Items.GOLDEN_SHOVEL, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_HOE,
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
            Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
            Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);

    private BrewErosion() {}

    static void erodeFromCauldron(ServerLevel level, BlockPos center, int radius, @Nullable Player caster) {
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        var obsidian = new MutableInt();
        BrewTerrain.forEachColumn(level, center, radius, pos -> {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || BrewTerrain.isProtectedTerrain(state)
                    || !BrewTerrain.setBlock(actor, level, pos, Blocks.AIR.defaultBlockState(), Direction.UP)) return;
            if (state.is(Blocks.OBSIDIAN)) obsidian.increment();
            Vec3 removed = Vec3.atCenterOf(pos);
            level.sendParticles(ParticleTypes.SPLASH, removed.x, removed.y, removed.z, 16, 0.5, 0.5, 0.5, 0);
        });
        level.playSound(null, center, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 2);
        Vec3 impact = Vec3.atCenterOf(center);
        int stackSize = new ItemStack(Items.OBSIDIAN).getMaxStackSize();
        for (int remaining = obsidian.intValue(); remaining > 0; remaining -= stackSize) {
            level.addFreshEntity(new ItemEntity(level, impact.x, impact.y, impact.z,
                    new ItemStack(Items.OBSIDIAN, Math.min(stackSize, remaining))));
        }
    }

    static void corrodeFromCauldron(LivingEntity target, @Nullable LivingEntity caster, double scale) {
        if (!(target.level() instanceof ServerLevel level) || scale <= 0) return;
        int odds = Math.max(1, (int) Math.ceil(5 / scale));
        if (level.random.nextInt(odds) == 0) {
            target.hurt(level.damageSources().thrown(target, caster), Mth.ceil(8 * scale));
        }

        LivingEntity actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.OFFHAND) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.isDamageableItem()) {
                int damage = Mth.ceil((50 + level.random.nextInt(25)) * scale);
                stack.hurtAndBreak(damage, actor, ignored -> target.broadcastBreakEvent(slot));
            }
        }
    }

    public static void impact(ThrownBrew brew, HitResult hit) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        if (hit instanceof BlockHitResult block) {
            erode(level, brew, block);
        } else if (hit instanceof EntityHitResult entity) {
            if (entity.getEntity() instanceof LivingEntity target) {
                corrode(level, brew, target);
            } else {
                BrewImpacts.recoverBrew(brew, level, hit);
            }
        }
    }

    private static void erode(ServerLevel level, ThrownBrew brew, BlockHitResult hit) {
        BlockPos center = hit.getBlockPos();
        if (!BrewTerrain.canEdit(brew.getOwner(), level, center, level.getBlockState(center))) return;
        int removed = 0;
        int obsidian = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
            int height = Math.abs(pos.getY() - center.getY());
            int horizontal = Math.abs(pos.getX() - center.getX()) + Math.abs(pos.getZ() - center.getZ());
            if (height == 0 ? horizontal > 3 : horizontal + height > 2) continue;
            BlockState state = level.getBlockState(pos);
            if (!canDissolve(state)) continue;
            boolean changed = pos.equals(center) ? level.removeBlock(pos, false)
                    : BrewTerrain.setBlock(brew.getOwner(), level, pos, Blocks.AIR.defaultBlockState(), hit.getDirection());
            if (changed) {
                removed++;
                if (state.is(Blocks.OBSIDIAN)) obsidian++;
            }
        }
        Vec3 impact = Vec3.atCenterOf(center);
        if (removed > 0) level.sendParticles(ParticleTypes.SPLASH, impact.x, impact.y, impact.z, removed, 0.4, 0.4, 0.4, 0);
        if (obsidian > 0) {
            level.addFreshEntity(new ItemEntity(level, impact.x, impact.y, impact.z, new ItemStack(Items.OBSIDIAN, obsidian)));
        }
    }

    private static boolean canDissolve(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty()
                && !state.is(BlockTags.FIRE) && !BrewTerrain.isProtectedTerrain(state);
    }

    private static void corrode(ServerLevel level, ThrownBrew brew, LivingEntity target) {
        target.hurt(level.damageSources().indirectMagic(brew, brew.getOwner()), BrewImpacts.enhanced(brew) ? 10 : 8);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.OFFHAND) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (CORRODIBLE_GEAR.contains(stack.getItem())) {
                stack.hurtAndBreak(100, target, wearer -> wearer.broadcastBreakEvent(slot));
            }
        }
    }
}
