package art.arcane.witchery.item;

import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.CursedControls;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class BrewBottleItem extends Item {
    public BrewBottleItem() {
        super(new Properties().stacksTo(8));
    }

    public static int color(ItemStack stack) {
        var tag = stack.getTag();
        return tag != null && tag.contains("Color") ? tag.getInt("Color") & 0xFFFFFF : 0x008000;
    }

    public static boolean isSplash(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return false;
        if (tag.contains("Splash")) return tag.getBoolean("Splash");
        BrewMixture mixture = BrewMixture.from(tag);
        return mixture != null && mixture.splash();
    }

    @Override
    public Component getName(ItemStack stack) {
        BrewMixture mixture = BrewMixture.from(stack.getTag());
        if (mixture == null) return super.getName(stack);
        List<Component> names = new ArrayList<>();
        for (var effect : mixture.effects()) names.add(effect.effect().getDisplayName());
        if (mixture.extinguishes()) names.add(Component.translatable("brew.witchery.extinguish"));
        Component effects = names.isEmpty() ? Component.translatable("item.witchery.brewbottle.water")
                : ComponentUtils.formatList(names, Component.literal(", "));
        String key = "item.witchery.brewbottle." + switch (mixture.delivery()) {
            case DRINK -> "drink";
            case SPLASH -> "splash";
            case GAS -> "gas";
            case LIQUID -> "liquid";
            case TRIGGERED -> "triggered";
        };
        return Component.translatable(key, effects);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        BrewMixture mixture = BrewMixture.from(stack.getTag());
        if (mixture == null) return;
        for (var application : mixture.effects()) {
            MutableComponent text = application.effect().getDisplayName().copy();
            if (application.amplifier() > 0) {
                text.append(" ").append(Component.translatable("potion.potency." + application.amplifier()));
            }
            if (application.ticks() > 0) {
                text.append(" (").append(MobEffectUtil.formatDuration(application.instance(application.ticks()), 1)).append(")");
            }
            tooltip.add(text);
        }
        if (mixture.extinguishes()) tooltip.add(Component.translatable("brew.witchery.extinguish"));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isSplash(stack) ? UseAnim.NONE : UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        BrewMixture mixture = BrewMixture.from(stack.getTag());
        return mixture != null ? mixture.drinkTicks() : 32;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BrewMixture mixture = BrewMixture.from(stack.getTag());
        if (mixture == null) return InteractionResultHolder.pass(stack);
        if (!mixture.splash()) return ItemUtils.startUsingInstantly(level, player, hand);

        if (!level.isClientSide) {
            ThrownBrew brew = new ThrownBrew(player, level);
            brew.setItem(stack);
            brew.shootFromRotation(player, player.getXRot(), player.getYRot(), -20, 0.75F, 1);
            if (!level.addFreshEntity(brew)) return InteractionResultHolder.fail(stack);
            level.playSound(null, player, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5F,
                    0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity drinker) {
        BrewMixture mixture = BrewMixture.from(stack.getTag());
        if (mixture == null || mixture.splash()) return stack;
        if (drinker instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (!level.isClientSide) {
            mixture.apply(drinker, drinker, drinker, 1, 1);
            drinker.gameEvent(GameEvent.DRINK);
        }
        if (drinker instanceof Player player && player.getAbilities().instabuild) return stack;
        stack.shrink(1);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (stack.isEmpty()) return bottle;
        if (drinker instanceof ServerPlayer player && !player.getInventory().add(bottle)) {
            player.drop(bottle, false);
        }
        return stack;
    }

    public void impact(ServerLevel level, ThrownBrew brew, HitResult hit) {
        BrewMixture mixture = BrewMixture.from(brew.getItem().getTag());
        if (mixture == null || !mixture.splash()) return;
        switch (mixture.delivery()) {
            case SPLASH -> splash(level, brew, hit, mixture);
            case GAS, LIQUID -> releaseFluid(level, brew, hit, mixture);
            case TRIGGERED -> CursedControls.impact(level, hit, brew.getItem().getTag(), mixture, color(brew.getItem()),
                    brew.getOwner());
            case DRINK -> { return; }
        }
        level.levelEvent(2002, BlockPos.containing(hit.getLocation()), color(brew.getItem()));
    }

    private void releaseFluid(ServerLevel level, ThrownBrew brew, HitResult hit, BrewMixture mixture) {
        String id = mixture.delivery() == BrewMixture.Delivery.GAS ? "brewgas" : "brewliquid";
        var cloud = (BrewDispersalBlock) WitcheryBlocks.getRequired(id).get();
        BlockPos pos;
        Direction face = Direction.UP;
        if (hit instanceof BlockHitResult block) {
            face = block.getDirection();
            pos = block.getBlockPos().relative(face);
        } else if (hit instanceof EntityHitResult entity) {
            pos = entity.getEntity().blockPosition();
        } else {
            pos = BlockPos.containing(hit.getLocation());
        }
        cloud.release(level, pos, face, brew.getItem().getTag(), mixture, color(brew.getItem()), brew.getOwner());
    }

    private void splash(ServerLevel level, ThrownBrew brew, HitResult hit, BrewMixture mixture) {
        Vec3 impact = hit.getLocation();
        int radius = mixture.splashRadius();
        LivingEntity caster = brew.getOwner() instanceof LivingEntity living ? living : null;
        LivingEntity direct = hit instanceof EntityHitResult entity && entity.getEntity() instanceof LivingEntity living ? living : null;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(impact, impact).inflate(radius))) {
            double distance = target.position().distanceTo(impact);
            if (distance >= radius && target != direct) continue;
            double scale = target == direct ? 1 : 1 - distance / radius;
            mixture.apply(target, brew, caster, scale, scale * 0.5);
        }
        if (hit instanceof BlockHitResult block) {
            mixture.applyToBlock(level, block.getBlockPos(), radius, false, caster instanceof Player player ? player : null);
        }
    }
}
