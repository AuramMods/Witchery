package art.arcane.witchery.item;

import art.arcane.witchery.client.renderer.BrewBagRenderer;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.menu.BrewBagMenu;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.function.Consumer;

public final class BrewBagItem extends Item {
    public static final int SLOT_COUNT = 8;
    private static final String AIM = "WITCBrewBagAim";
    private static final String COLOR = "WITCColor";
    private static final int COCOA = 5057301;
    private static final int[] DYE_COLORS = {16777215, 15113780, 14383829, 12179199, 15197994, 8639516, 15574987, 7763574,
            11513789, 3968688, 8532146, 2247599, COCOA, 5732898, 12464176, 1118481};
    private static final float DIAGONAL_ANGLE = 3.5F;
    private static final float CARDINAL_ANGLE = 7.0F;

    public BrewBagItem() {
        super(new Properties().stacksTo(1));
    }

    public static int color(ItemStack stack) {
        var tag = stack.getTag();
        return tag != null && tag.contains(COLOR, Tag.TAG_ANY_NUMERIC) ? tag.getInt(COLOR) & 0xFFFFFF : COCOA;
    }

    public static void dye(ItemStack stack, DyeColor dye) {
        stack.getOrCreateTag().putInt(COLOR, DYE_COLORS[dye.getId()]);
    }

    public static boolean accepts(ItemStack stack) {
        return stack.getItem() instanceof ThrowableBrewItem || LeonardsUrnItem.splash(stack) != null;
    }

    public static NonNullList<ItemStack> contents(ItemStack bag) {
        return ItemInventory.load(bag, SLOT_COUNT);
    }

    public static void saveContents(ItemStack bag, List<ItemStack> contents) {
        ItemInventory.save(bag, contents);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BrewBagRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new BrewBagRenderer(Minecraft.getInstance());
                return renderer;
            }
        });
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag tag) {
        var display = tag.getCompound("display");
        if (!display.contains("Name", Tag.TAG_STRING)) return;
        String name = display.getString("Name");
        try {
            if (Component.Serializer.fromJson(name) != null) return;
        } catch (JsonParseException ignored) {
            // Legacy bags stored plain text here.
        }
        display.putString("Name", Component.Serializer.toJson(Component.literal(name)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(bag);
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer server) {
                int selected = player.getInventory().selected;
                NetworkHooks.openScreen(server, new SimpleMenuProvider(
                        (id, inventory, owner) -> new BrewBagMenu(id, inventory, bag, selected), bag.getHoverName()),
                        buffer -> buffer.writeVarInt(selected));
            }
            return InteractionResultHolder.sidedSuccess(bag, level.isClientSide);
        }
        var aim = new CompoundTag();
        resetAim(player, aim);
        player.getPersistentData().put(AIM, aim);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(bag);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 36_000;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        if (living instanceof Player player && player.getPersistentData().contains(AIM, Tag.TAG_COMPOUND)) {
            updateAim(player, stack, player.getPersistentData().getCompound(AIM));
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int remaining) {
        if (!(level instanceof ServerLevel server) || !(living instanceof ServerPlayer player)
                || player.getMainHandItem() != stack || player.isShiftKeyDown()
                || !player.getPersistentData().contains(AIM, Tag.TAG_COMPOUND)) return;
        var aim = player.getPersistentData().getCompound(AIM);
        updateAim(player, stack, aim);
        int index = preparedSlot(player);
        if (index < 0) return;
        var contents = contents(stack);
        ItemStack brew = contents.get(index);
        if (!accepts(brew)) return;
        if (ThrownBrew.throwBottle(server, player, brew) && !player.getAbilities().instabuild) {
            saveContents(stack, contents);
            player.getInventory().setChanged();
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity living, int remaining) {
        if (living instanceof Player player) player.getPersistentData().remove(AIM);
    }

    public static int preparedSlot(Player player) {
        var aim = player.getPersistentData().getCompound(AIM);
        int slot = aim.contains("Slot", Tag.TAG_INT) ? aim.getInt("Slot") : -1;
        return slot >= 0 && slot < SLOT_COUNT ? slot : -1;
    }

    private static void updateAim(Player player, ItemStack bag, CompoundTag aim) {
        if (player.isShiftKeyDown()) {
            resetAim(player, aim);
            return;
        }
        if (aim.contains("Slot")) return;
        float pitch = aim.getFloat("Pitch") - player.getXRot();
        float yaw = Mth.wrapDegrees(player.getYRot() - aim.getFloat("Yaw"));
        int slot = direction(pitch, yaw);
        if (slot < 0) return;
        resetAim(player, aim);
        if (accepts(contents(bag).get(slot))) {
            aim.putInt("Slot", slot);
            if (player.level() instanceof ServerLevel server) {
                server.playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    private static int direction(float pitch, float yaw) {
        if (Math.abs(pitch) >= DIAGONAL_ANGLE && Math.abs(yaw) >= DIAGONAL_ANGLE) {
            if (pitch > 0) return yaw > 0 ? 1 : 7;
            return yaw > 0 ? 3 : 5;
        }
        if (pitch >= CARDINAL_ANGLE) return 0;
        if (pitch <= -CARDINAL_ANGLE) return 4;
        if (yaw >= CARDINAL_ANGLE) return 2;
        if (yaw <= -CARDINAL_ANGLE) return 6;
        return -1;
    }

    private static void resetAim(Player player, CompoundTag aim) {
        aim.remove("Slot");
        aim.putFloat("Pitch", player.getXRot());
        aim.putFloat("Yaw", player.getYRot());
    }
}
