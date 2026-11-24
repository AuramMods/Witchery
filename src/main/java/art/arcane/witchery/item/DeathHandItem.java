package art.arcane.witchery.item;

import art.arcane.witchery.client.renderer.WitchHandRenderer;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class DeathHandItem extends Item {
    private static final String DEPLOYED_KEY = "WITCScytheDeployed";

    public DeathHandItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    public static boolean deployed(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean(DEPLOYED_KEY);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!DeathClothingItem.fullSet(player)) return InteractionResultHolder.pass(stack);
        if (level instanceof ServerLevel server) {
            boolean deploying = !deployed(stack);
            stack.getOrCreateTag().putBoolean(DEPLOYED_KEY, deploying);
            if (deploying) {
                server.sendParticles(ParticleTypes.ENTITY_EFFECT, player.getX(), player.getY(0.5), player.getZ(),
                        16, 1, 1, 1, 0);
                server.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1, 1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity carrier, int slot, boolean selected) {
        if (level.isClientSide || !(carrier instanceof Player player) || !deployed(stack)) return;
        if (!DeathClothingItem.fullSet(player)) {
            stack.getOrCreateTag().putBoolean(DEPLOYED_KEY, false);
        } else if (level.getGameTime() % 20 == 0) {
            var food = player.getFoodData();
            food.eat(-Math.min(food.getFoodLevel(), 2), 0);
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity clicked) {
        if (player.level().isClientSide || !(clicked instanceof LivingEntity victim)) return true;
        var source = WitcheryDamageTypes.spectralTouch(player);
        if (!deployed(stack) || !DeathClothingItem.fullSet(player)) {
            victim.hurt(source, 2);
            return true;
        }

        Strike strike = Strike.atHunger(player.getFoodData().getFoodLevel());
        AABB box = new AABB(victim.getX() - 1.5, victim.getBoundingBox().minY, victim.getZ() - 1.5,
                victim.getX() + 1.5, victim.getBoundingBox().maxY, victim.getZ() + 1.5);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != player && entity.isAlive())) {
            float damage = Math.min(15, Math.max(Math.min(target.getMaxHealth(), 20) * strike.fraction, strike.minimum));
            if (target.hurt(source, damage)) {
                player.getFoodData().eat(strike.food, 0);
                if (strike.healing > 0) player.heal(strike.healing);
            }
        }
        return true;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private WitchHandRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new WitchHandRenderer(Minecraft.getInstance(), true);
                return renderer;
            }
        });
    }

    private record Strike(float fraction, float minimum, int food, int healing) {
        private static Strike atHunger(int food) {
            if (food == 0) return new Strike(0.5F, 4, 10, 3);
            if (food <= 4) return new Strike(0.25F, 4, 3, 2);
            if (food <= 10) return new Strike(0.2F, 3, 2, 1);
            return new Strike(0.15F, 3, 1, 0);
        }
    }
}
