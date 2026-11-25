package art.arcane.witchery.brewing;

import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.entity.GrenadeProjectile;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.mixin.DispenserBlockAccessor;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.List;
import java.util.function.Function;

public final class BrewDispenser {
    private static final List<Direction> CAULDRON_SIDES = List.of(
            Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    private BrewDispenser() {}

    public static void register() {
        var launch = launcher(level -> new ThrownBrew(WitcheryEntities.THROWN_BREW.get(), level));
        var launchGrenade = launcher(level -> new GrenadeProjectile(WitcheryEntities.GRENADE.get(), level));
        DispenserBlock.registerBehavior(WitcheryItems.getRequired("sungrenade").get(), launchGrenade);
        DispenserBlock.registerBehavior(WitcheryItems.getRequired("dupgrenade").get(), launchGrenade);
        var eject = new DefaultDispenseItemBehavior();
        DispenserBlock.registerBehavior(WitcheryItems.getRequired("brewbottle").get(), (source, stack) ->
                (BrewBottleItem.isSplash(stack) ? launch : eject).dispense(source, stack));
        for (var entry : WitcheryItems.all()) {
            if (entry.get() instanceof ThrowableBrewItem brew) DispenserBlock.registerBehavior(brew, launch);
        }

        var glassBottle = ((DispenserBlockAccessor) Blocks.DISPENSER).witchery$getDispenseMethod(new ItemStack(Items.GLASS_BOTTLE));
        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE, (source, stack) -> {
            var level = source.getLevel();
            var facing = source.getBlockState().getValue(DispenserBlock.FACING);
            for (Direction side : CAULDRON_SIDES) {
                if (side == facing) continue;
                if (!(level.getBlockEntity(source.getPos().relative(side)) instanceof WitchesCauldronBlockEntity cauldron)) continue;
                ItemStack filled = cauldron.dispenseBottle();
                if (!filled.isEmpty()) {
                    DefaultDispenseItemBehavior.spawnItem(level, filled, 6, facing, DispenserBlock.getDispensePosition(source));
                    stack.shrink(1);
                    level.levelEvent(1000, source.getPos(), 0);
                }
                return stack;
            }
            return glassBottle.dispense(source, stack);
        });
    }

    private static AbstractProjectileDispenseBehavior launcher(Function<Level, ThrowableItemProjectile> createProjectile) {
        return new AbstractProjectileDispenseBehavior() {
            @Override
            protected ThrowableItemProjectile getProjectile(Level level, Position pos, ItemStack stack) {
                var projectile = createProjectile.apply(level);
                projectile.setPos(pos.x(), pos.y(), pos.z());
                projectile.setItem(stack);
                return projectile;
            }

            @Override
            protected float getPower() {
                return 1.375F;
            }

            @Override
            protected float getUncertainty() {
                return 3.0F;
            }
        };
    }
}
