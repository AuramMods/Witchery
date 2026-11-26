package art.arcane.witchery.item;

import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.CursedControls;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public final class BrewBottleItem extends Item {
    private static final String TRANSLATION_PREFIX = "item.witchery.brewbottle.";

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
        var description = BrewMixture.describe(stack.getTag());
        if (description == null) return super.getName(stack);
        List<BrewMixture.NamedEffect> effects = description.effects();
        MutableComponent joined = Component.empty();
        if (effects.isEmpty()) joined.append(Component.translatable(TRANSLATION_PREFIX + "water"));
        for (int index = 0; index < effects.size(); index++) {
            if (index > 0) joined.append(index == effects.size() - 1 ? " & " : ", ");
            joined.append(effects.get(index).name());
        }
        return Component.translatable(TRANSLATION_PREFIX + description.delivery().name().toLowerCase(Locale.ROOT), joined);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        var description = BrewMixture.describe(stack.getTag());
        if (description == null) return;
        boolean splash = description.delivery() != BrewMixture.Delivery.DRINK;
        if (splash) {
            var dispersal = Component.translatable(TRANSLATION_PREFIX + "dispersal."
                    + description.delivery().name().toLowerCase(Locale.ROOT));
            if (description.extent() > 0) dispersal.append(" ").append(potency(description.extent()));
            if (description.lifetime() > 0) {
                dispersal.append(" [").append(Component.translatable(TRANSLATION_PREFIX + "linger",
                        potency(description.lifetime()))).append("]");
            }
            tooltip.add(dispersal);
        }
        if (description.effects().isEmpty()) tooltip.add(Component.translatable(TRANSLATION_PREFIX + "water"));
        for (var effect : description.effects()) {
            MutableComponent text = effect.name().copy();
            if (effect.strength() > 0) text.append(" ").append(potency(effect.strength()));
            int duration = splash ? effect.duration() / 2 : effect.duration();
            if (duration > 0) {
                int seconds = duration / 20;
                text.append(String.format(Locale.ROOT, " [%d:%02d]", seconds / 60, seconds % 60));
            }
            tooltip.add(text);
        }
        int speed = drinkTicks(stack.getTag(), description.drinkTicks());
        if (speed != 32) {
            String tier = speed > 48 ? "very_slow" : speed > 32 ? "slow" : speed < 16 ? "very_fast" : "fast";
            tooltip.add(Component.translatable(TRANSLATION_PREFIX + "quaffing",
                    Component.translatable(TRANSLATION_PREFIX + "quaffing." + tier)).withStyle(ChatFormatting.BLUE));
        }
    }

    private static Component potency(int strength) {
        return Component.translatable("potion.potency." + strength);
    }

    private static int drinkTicks(@Nullable CompoundTag tag, int fallback) {
        int saved = tag == null ? 0 : tag.getInt("BrewDrinkSpeed");
        return saved > 0 ? saved : fallback;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isSplash(stack) ? UseAnim.NONE : UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        BrewMixture mixture = BrewMixture.from(stack.getTag());
        return mixture != null ? drinkTicks(stack.getTag(), mixture.drinkTicks()) : 32;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BrewMixture mixture = BrewMixture.from(stack.getTag());
        if (mixture == null) return InteractionResultHolder.pass(stack);
        if (!mixture.splash()) return ItemUtils.startUsingInstantly(level, player, hand);

        if (level instanceof ServerLevel server && !ThrownBrew.throwBottle(server, player, stack)) {
            return InteractionResultHolder.fail(stack);
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
        Player caster = brew.getOwner() instanceof Player player ? player : FakePlayerFactory.getMinecraft(level);
        switch (mixture.impactDelivery()) {
            case SPLASH -> splash(level, hit, mixture, brew.position(), caster);
            case GAS, LIQUID -> releaseFluid(level, brew.getItem(), hit, mixture, caster);
            case TRIGGERED -> CursedControls.impact(level, hit, brew.getItem().getTag(), mixture, color(brew.getItem()), caster);
            case DRINK -> { return; }
        }
        level.levelEvent(2002, brew.blockPosition(), color(brew.getItem()));
    }

    private void releaseFluid(ServerLevel level, ItemStack bottle, HitResult hit, BrewMixture mixture, Player caster) {
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
        cloud.release(level, pos, face, bottle.getTag(), mixture, color(bottle), caster);
    }

    private void splash(ServerLevel level, HitResult hit, BrewMixture mixture, Vec3 center, Player caster) {
        int radius = mixture.splashRadius();
        LivingEntity direct = hit instanceof EntityHitResult entity && entity.getEntity() instanceof LivingEntity living ? living : null;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius))) {
            double distanceSquared = target.distanceToSqr(center);
            if (distanceSquared > radius * radius) continue;
            double scale = target == direct ? 1 : 1 - Math.sqrt(distanceSquared) / radius;
            mixture.applySplash(target, caster, caster, scale, target != direct, center);
        }
        if (hit instanceof BlockHitResult block) {
            mixture.applySplashToBlock(level, block.getBlockPos(), block.getDirection(), center, caster);
        }
    }
}
