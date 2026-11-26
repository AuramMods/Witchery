package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
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
import net.minecraft.world.level.block.LiquidBlock;
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
        // The original revisits the same layer in smaller circles, including already empty cells.
        for (int ring = radius; ring > 0; ring--) {
            BrewTerrain.forEachColumn(level, center, ring, pos -> {
                BlockState state = level.getBlockState(pos);
                if (BrewTerrain.isProtectedTerrain(state)
                        || !BrewTerrain.setBlock(actor, level, pos, Blocks.AIR.defaultBlockState(), Direction.UP)) return;
                if (state.is(Blocks.OBSIDIAN)) obsidian.increment();
                WitcheryNetwork.sendErosionSplash(level, Vec3.atLowerCornerOf(pos), 0.5, 16);
            });
        }
        Vec3 impact = Vec3.atLowerCornerOf(center);
        level.playSound(null, impact.x, impact.y, impact.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 2);
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
                BrewImpacts.recoverBrew(brew, hit);
            }
        }
    }

    private static void erode(ServerLevel level, ThrownBrew brew, BlockHitResult hit) {
        BlockPos center = hit.getBlockPos();
        if (!BrewTerrain.canEdit(brew.getOwner(), level, center, level.getBlockState(center))) return;
        var obsidian = new MutableInt();
        erodeLayer(level, brew, center, center, 3, obsidian);
        for (int distance = 1; distance <= 2; distance++) {
            erodeLayer(level, brew, center, center.above(distance), 3 - distance, obsidian);
            erodeLayer(level, brew, center, center.below(distance), 3 - distance, obsidian);
        }
        if (obsidian.intValue() > 0) {
            Vec3 impact = Vec3.atCenterOf(center);
            level.addFreshEntity(new ItemEntity(level, impact.x, impact.y, impact.z,
                    new ItemStack(Items.OBSIDIAN, obsidian.intValue())));
        }
    }

    private static void erodeLayer(ServerLevel level, ThrownBrew brew, BlockPos struck, BlockPos center, int radius,
                                   MutableInt obsidian) {
        BrewTerrain.forEachColumn(level, center, radius, pos -> {
            BlockState state = level.getBlockState(pos);
            if (!canDissolve(state) || !pos.equals(struck) && !BrewTerrain.canEdit(brew.getOwner(), level, pos, state)) return;
            // A waterlogged block leaves its fluid. A Witchery fluid block is itself eroded.
            boolean changed = state.getBlock() instanceof LiquidBlock
                    ? level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState())
                    : level.removeBlock(pos, false);
            if (!changed) return;
            if (state.is(Blocks.OBSIDIAN)) obsidian.increment();
            WitcheryNetwork.sendErosionSplash(level, brew.position(), 1, 8);
        });
    }

    private static boolean canDissolve(BlockState state) {
        return !state.isAir() && !state.is(Blocks.WATER) && !state.is(Blocks.LAVA)
                && !state.is(BlockTags.FIRE) && !BrewTerrain.isProtectedTerrain(state);
    }

    private static void corrode(ServerLevel level, ThrownBrew brew, LivingEntity target) {
        var owner = brew.getOwner();
        target.hurt(level.damageSources().thrown(target, owner), BrewImpacts.enhanced(brew) ? 10 : 8);
        var random = owner instanceof LivingEntity caster ? caster.getRandom() : level.getRandom();
        ServerPlayer player = owner instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        boolean creative = owner instanceof Player caster && caster.isCreative();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.OFFHAND) continue;
            ItemStack stack = target.getItemBySlot(slot);
            if (!CORRODIBLE_GEAR.contains(stack.getItem()) || !stack.isDamageableItem()) continue;
            if (!creative) {
                stack.hurt(100, random, player);
                // The legacy stack breaks only after exceeding its maximum damage.
                if (stack.getDamageValue() > stack.getMaxDamage()) {
                    target.broadcastBreakEvent(slot);
                    if (player != null) player.awardStat(Stats.ITEM_BROKEN.get(stack.getItem()));
                    stack.shrink(1);
                    stack.setDamageValue(0);
                }
            }
            if (stack.getDamageValue() <= 0) target.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
}
