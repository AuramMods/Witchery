package art.arcane.witchery.item;

import art.arcane.witchery.client.VampireBookScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.List;

public final class VampireBookItem extends Item {
    public static final int MAX_TORN_PAGES = 9;
    private static final String TORN_PAGES = "VampireBookPages";
    private static final String PAGE_STACK = "pageStack";
    private static final List<String> CHAPTERS = List.of("toc", "ritual1", "ritual2", "ritual3", "ritual4", "ritual5",
            "transfix", "knockback", "speed", "resistsun", "smashstone", "bats", "mesmerize", "maker", "maker2", "finalpowers");

    public VampireBookItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : player.getInventory().getContainerSize() - 1;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VampireBookScreen.open(stack, slot));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("book.witchery.immortal.pages", tornPages(stack), MAX_TORN_PAGES)
                .withStyle(ChatFormatting.GRAY));
    }

    public static int tornPages(ItemStack stack) {
        return stack.hasTag() ? Mth.clamp(stack.getTag().getInt(TORN_PAGES), 0, MAX_TORN_PAGES) : 0;
    }

    public static void setTornPages(ItemStack stack, int pages) {
        stack.getOrCreateTag().putInt(TORN_PAGES, Mth.clamp(pages, 0, MAX_TORN_PAGES));
    }

    public static String chapter(int index) {
        return CHAPTERS.get(index);
    }

    public static int chapterCount(ItemStack stack) {
        int pages = tornPages(stack);
        return 6 + pages + (pages >= 8 ? 1 : 0);
    }

    public static int bookmark(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        ListTag path = stack.getTag().getList(PAGE_STACK, Tag.TAG_STRING);
        if (path.isEmpty()) return 0;
        int chapter = CHAPTERS.indexOf(path.getString(path.size() - 1));
        return Mth.clamp(chapter, 0, chapterCount(stack) - 1);
    }

    public static boolean setBookmark(ItemStack stack, String chapter) {
        int index = CHAPTERS.indexOf(chapter);
        if (index < 0 || index >= chapterCount(stack)) return false;
        ListTag path = new ListTag();
        if (index != 0) path.add(StringTag.valueOf(chapter));
        stack.getOrCreateTag().put(PAGE_STACK, path);
        return true;
    }
}
