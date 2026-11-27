package art.arcane.witchery.item;

import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.client.renderer.HandBowRenderer;
import art.arcane.witchery.entity.WitcheryBolt;
import art.arcane.witchery.entity.WitcheryBolt.Kind;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class HandBowItem extends BowItem {
    private static final String CURRENT = "WITCBoltTypeCurrent";
    private static final String PREFERRED = "WITCBoltTypePreferred";
    private static final int LOAD_TICKS = 10;
    private static final RandomSource SHOT_SOUND_RANDOM = RandomSource.create();

    public enum Ammo {
        STAKE(WitcheryIngredient.STAKE_BOLT, Kind.STAKE), ANTI_MAGIC(WitcheryIngredient.ANTI_MAGIC_BOLT, Kind.ANTI_MAGIC),
        HOLY(WitcheryIngredient.HOLY_BOLT, Kind.HOLY), SPLITTING(WitcheryIngredient.SPLITTING_BOLT, Kind.STAKE),
        SILVER(WitcheryIngredient.SILVER_BOLT, Kind.SILVER);

        private static final Ammo[] TYPES = values();
        private final WitcheryIngredient ingredient;
        private final Kind projectile;

        Ammo(WitcheryIngredient ingredient, Kind projectile) {
            this.ingredient = ingredient;
            this.projectile = projectile;
        }

        public ItemStack stack() {
            return ingredient.item().getDefaultInstance();
        }

        private int id() {
            return ordinal() + 1;
        }

        @Nullable
        private static Ammo fromId(int id) {
            return id > 0 && id <= TYPES.length ? TYPES[id - 1] : null;
        }

        private static boolean matches(ItemStack stack) {
            for (Ammo ammo : TYPES) {
                if (stack.is(ammo.ingredient.item())) return true;
            }
            return false;
        }
    }

    public HandBowItem() {
        super(new Properties().durability(768));
    }

    @Nullable
    public static Ammo loaded(ItemStack bow) {
        return bow.getTag() == null ? null : Ammo.fromId(bow.getTag().getInt(CURRENT));
    }

    private static boolean canUse(Player player) {
        return WitcheryPlayerDataProvider.get(player).map(data -> data.getCreatureTypeOrdinal() == 0).orElse(true);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return Ammo::matches;
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return Ammo::matches;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack bow = player.getItemInHand(hand);
        if (!canUse(player) || loaded(bow) == null && !player.isShiftKeyDown()) return InteractionResultHolder.pass(bow);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(bow);
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack bow, int remaining) {
        int elapsed = getUseDuration(bow) - remaining;
        if (user instanceof ServerPlayer player && player.isShiftKeyDown()
                && (elapsed == 5 || elapsed == 10 || elapsed == 15) && findAmmo(player, bow, loaded(bow)) != null) {
            player.playNotifySound(WitcherySounds.BOW_CLICK.get(), SoundSource.PLAYERS, 1, 1);
        }
    }

    @Override
    public void releaseUsing(ItemStack bow, Level world, LivingEntity user, int remaining) {
        if (!(user instanceof ServerPlayer player) || !canUse(player)) return;
        int elapsed = getUseDuration(bow) - remaining;
        Ammo current = loaded(bow);
        if (player.isShiftKeyDown() && elapsed >= LOAD_TICKS) load(player, bow, current);
        else if (current != null) fire(player, bow, current, elapsed);
    }

    @Nullable
    private static Ammo findAmmo(Player player, ItemStack bow, @Nullable Ammo current) {
        Ammo preferred = bow.getTag() == null ? null : Ammo.fromId(bow.getTag().getInt(PREFERRED));
        int start = current != null ? current.id() : preferred == null ? 0 : preferred.ordinal();
        for (int i = 0; i < Ammo.TYPES.length; i++) {
            Ammo candidate = Ammo.TYPES[(start + i) % Ammo.TYPES.length];
            if (candidate != current && !ammoInInventory(player, candidate).isEmpty()) return candidate;
        }
        return null;
    }

    private static ItemStack ammoInInventory(Player player, Ammo ammo) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(ammo.ingredient.item())) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static void load(ServerPlayer player, ItemStack bow, @Nullable Ammo current) {
        Ammo next = findAmmo(player, bow, current);
        if (next == null) return;
        if (!player.isCreative()) {
            ammoInInventory(player, next).shrink(1);
            if (current != null) {
                ItemStack returned = current.stack();
                if (!player.addItem(returned)) player.drop(returned, false);
            }
        }
        bow.getOrCreateTag().putInt(CURRENT, next.id());
        if (current != null) bow.getOrCreateTag().putInt(PREFERRED, next.id());
        player.getInventory().setChanged();
        player.playNotifySound(WitcherySounds.BOW_WIND_UP.get(), SoundSource.PLAYERS, 1, 1);
    }

    private static void fire(ServerPlayer player, ItemStack bow, Ammo ammo, int elapsed) {
        Level world = player.level();
        float power = getPowerForTime(elapsed >= 20 ? 20 : 19);
        boolean retained = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0
                && world.random.nextDouble() < 0.25D;
        boolean powered = ammo == Ammo.ANTI_MAGIC && HunterClothingItem.fullSet(player, HunterClothingItem.Upgrade.PLAIN);
        Kind projectile = powered ? Kind.POWERED_ANTI_MAGIC : ammo.projectile;
        int count = ammo == Ammo.SPLITTING ? 3 : 1;
        boolean fired = false;
        for (int i = 0; i < count; i++) {
            WitcheryBolt bolt = new WitcheryBolt(player, projectile);
            bolt.setBaseDamage(count == 3 ? 1 : 2);
            bolt.setCritArrow(power == 1);
            bolt.pickup = !retained && !player.isCreative() ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
            if (ammo != Ammo.ANTI_MAGIC) bolt.applyBowEnchantments(bow);
            float yaw = player.getYRot() + (count == 3 ? (i - 1) * 20 : 0);
            bolt.shootFromCrossbow(player, yaw, power);
            fired |= world.addFreshEntity(bolt);
        }
        if (!fired) return;
        if (powered) InfusionEnergy.drain(player, 1.0F);
        if (!retained) bow.getOrCreateTag().remove(CURRENT);
        bow.hurtAndBreak(2, player, shooter -> shooter.broadcastBreakEvent(shooter.getUsedItemHand()));
        player.getInventory().setChanged();
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1,
                1 / (SHOT_SOUND_RANDOM.nextFloat() * 0.4F + 1.2F) + power * 0.5F);
    }

    @Override
    public void appendHoverText(ItemStack bow, @Nullable Level world, List<Component> lines, TooltipFlag flag) {
        Ammo ammo = loaded(bow);
        if (ammo != null) lines.add(Component.translatable("item.witchery.handbow.loaded", ammo.stack().getHoverName()));
        lines.add(Component.translatable("item.witchery.handbow.controls"));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HandBowRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new HandBowRenderer(Minecraft.getInstance());
                return renderer;
            }
        });
    }
}
