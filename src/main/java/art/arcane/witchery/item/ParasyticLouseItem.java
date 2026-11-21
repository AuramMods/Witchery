package art.arcane.witchery.item;

import art.arcane.witchery.brewing.LousePotion;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ParasyticLouseItem extends Item {
    public ParasyticLouseItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos clicked = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos pos = clicked.relative(face);
        Level level = context.getLevel();
        var shape = level.getBlockState(clicked).getCollisionShape(level, clicked);
        double y = face == Direction.UP && !shape.isEmpty()
                ? Math.max(pos.getY(), clicked.getY() + shape.max(Direction.Axis.Y)) : pos.getY();
        return release(level, context.getPlayer(), context.getItemInHand(), pos, face, y);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockPos pos = hit.getBlockPos();
        if (hit.getType() != HitResult.Type.BLOCK || !level.getBlockState(pos).liquid()
                || !level.getFluidState(pos).is(FluidTags.WATER)) return InteractionResultHolder.pass(stack);
        return new InteractionResultHolder<>(release(level, player, stack, pos, hit.getDirection(), pos.getY()), stack);
    }

    private static InteractionResult release(Level level, @Nullable Player player, ItemStack stack, BlockPos pos,
                                             Direction face, double y) {
        if (!level.isInWorldBounds(pos) || player != null
                && (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, face, stack))) return InteractionResult.FAIL;
        if (level instanceof ServerLevel server) {
            var louse = WitcheryEntities.LOUSE.get().create(server);
            if (louse == null) return InteractionResult.FAIL;
            louse.moveTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, server.random.nextFloat() * 360, 0);
            ForgeEventFactory.onFinalizeSpawn(louse, server, server.getCurrentDifficultyAt(pos), MobSpawnType.SPAWN_EGG, null, null);
            LousePotion potion = LousePotion.get(stack);
            if (potion != null) louse.setBitePotion(stack.getTag().getInt(LousePotion.TAG));
            if (stack.hasCustomHoverName()) louse.setCustomName(stack.getHoverName());
            louse.setPersistenceRequired();
            if (!server.addFreshEntity(louse)) return InteractionResult.FAIL;
            louse.playAmbientSound();
            if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        LousePotion potion = LousePotion.get(stack);
        if (potion == null) return;
        var dose = potion.effect();
        var line = Component.translatable(dose.getDescriptionId()).withStyle(ChatFormatting.GOLD);
        if (dose.getAmplifier() > 0) line.append(" ").append(Component.translatable("potion.potency." + dose.getAmplifier()));
        if (dose.getDuration() > 20) {
            line.append(" [").append(MobEffectUtil.formatDuration(dose, 1)).append("]");
        }
        tooltip.add(line);
    }

    public static boolean spendCarriedPotion(ServerPlayer player, DamageSource source) {
        if (source.getDirectEntity() == null && source.getEntity() == null) return false;
        var inventory = player.getInventory();
        ItemStack firstCharged = ItemStack.EMPTY;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!(stack.getItem() instanceof ParasyticLouseItem)) continue;
            LousePotion potion = LousePotion.get(stack);
            if (potion == null) continue;
            if (firstCharged.isEmpty()) firstCharged = stack;
            LivingEntity target = potion.regenerates() ? player
                    : potion.retaliates() && source.getEntity() instanceof LivingEntity attacker ? attacker : null;
            if (target != null) {
                LousePotion.set(stack, 0);
                potion.apply(target, player);
                inventory.setChanged();
                return true;
            }
        }
        if (firstCharged.isEmpty()) return false;
        LousePotion.set(firstCharged, 0);
        inventory.setChanged();
        return true;
    }
}
