package art.arcane.witchery.item;

import art.arcane.witchery.client.renderer.WitchHandRenderer;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.infusion.LightInfusion;
import art.arcane.witchery.infusion.OtherwhereInfusion;
import art.arcane.witchery.infusion.OverworldInfusion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class WitchHandItem extends Item {
    private static final String COOLDOWN = "WITCCooldown";
    private static final int USE_TICKS = 400;
    private static final int TELEPORT_COOLDOWN_MILLIS = 1500;

    public WitchHandItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private WitchHandRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new WitchHandRenderer(Minecraft.getInstance(), false);
                return renderer;
            }
        });
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TICKS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        if (living instanceof ServerPlayer player) {
            int elapsed = USE_TICKS - remaining;
            switch (InfusionEnergy.type(player)) {
                case LightInfusion.ID -> LightInfusion.channel(player, elapsed);
                case OverworldInfusion.ID -> OverworldInfusion.channel(player, elapsed);
                case OtherwhereInfusion.ID -> OtherwhereInfusion.channel(player, elapsed);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int remaining) {
        if (!(living instanceof ServerPlayer player)) return;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getLong(COOLDOWN) > System.currentTimeMillis()) return;
        int elapsed = USE_TICKS - remaining;
        switch (InfusionEnergy.type(player)) {
            case LightInfusion.ID -> LightInfusion.release(player, elapsed);
            case OverworldInfusion.ID -> OverworldInfusion.release(player, elapsed);
            case InfernalInfusion.ID -> InfernalInfusion.release(player, elapsed);
            case OtherwhereInfusion.ID -> {
                if (OtherwhereInfusion.release(player, elapsed)) {
                    stack.getOrCreateTag().putLong(COOLDOWN, System.currentTimeMillis() + TELEPORT_COOLDOWN_MILLIS);
                }
            }
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity living, int remaining) {
        if (living instanceof ServerPlayer player) LightInfusion.endCloak(player);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (player instanceof ServerPlayer caster && target instanceof LivingEntity living) {
            switch (InfusionEnergy.type(caster)) {
                case LightInfusion.ID -> LightInfusion.attack(caster, living);
                case OverworldInfusion.ID -> OverworldInfusion.attack(caster, living);
                case OtherwhereInfusion.ID -> OtherwhereInfusion.attack(caster, living);
                case InfernalInfusion.ID -> InfernalInfusion.attack(caster, living);
            }
        }
        return true;
    }
}
