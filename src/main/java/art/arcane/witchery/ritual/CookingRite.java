package art.arcane.witchery.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class CookingRite {
    private static final double RADIUS = 5;

    private CookingRite() {}

    public static boolean cook(ServerLevel level, BlockPos heart, boolean portable) {
        Vec3 center = Vec3.atBottomCenterOf(heart);
        AABB bounds = new AABB(center, center.add(0, 1, 0)).inflate(RADIUS, 0, RADIUS);
        Vec3 output = center.add(0, 0.05, 0);
        double burnChance = portable ? 0.16 : 0.08;
        boolean cookedAny = false;
        for (ItemEntity food : level.getEntitiesOfClass(ItemEntity.class, bounds,
                item -> item.isAlive() && item.distanceToSqr(center) <= RADIUS * RADIUS)) {
            ItemStack raw = food.getItem();
            var input = new SimpleContainer(raw.copyWithCount(1));
            var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level).orElse(null);
            if (recipe == null) continue;
            ItemStack cooked = recipe.assemble(input, level.registryAccess());
            if (!cooked.isEdible()) continue;

            int burned = 0;
            for (int i = 0; i < raw.getCount(); i++) {
                if (level.random.nextDouble() < burnChance) burned++;
            }
            var spawned = new ArrayList<ItemEntity>();
            if (spawn(level, output, cooked, raw.getCount() - burned, spawned)
                    && spawn(level, output, new ItemStack(Items.CHARCOAL), burned, spawned)) {
                food.discard();
                cookedAny = true;
            } else {
                spawned.forEach(Entity::discard);
            }
        }
        if (cookedAny) {
            level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.15, center.z, 16, 1.5, 0.6, 1.5, 0.02);
            level.playSound(null, heart, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 1, 1);
        }
        return cookedAny;
    }

    private static boolean spawn(ServerLevel level, Vec3 pos, ItemStack template, int count, List<ItemEntity> spawned) {
        while (count > 0) {
            int amount = Math.min(count, template.getMaxStackSize());
            var drop = new ItemEntity(level, pos.x, pos.y, pos.z, template.copyWithCount(amount), 0, 0.2, 0);
            drop.setDefaultPickUpDelay();
            if (!level.addFreshEntity(drop)) return false;
            spawned.add(drop);
            count -= amount;
        }
        return true;
    }
}
