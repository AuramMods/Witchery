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
import java.util.ArrayDeque;
import java.util.List;

public final class VampireBookItem extends Item {
    public static final int MAX_TORN_PAGES = 9;
    public static final int MAX_BOOKMARK_DEPTH = 64;
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

    public static int chapterIndex(String chapter) {
        return CHAPTERS.indexOf(chapter);
    }

    public static int chapterCount(ItemStack stack) {
        int pages = tornPages(stack);
        return 6 + pages + (pages >= 8 ? 1 : 0);
    }

    public static int bookmark(ItemStack stack) {
        var trail = bookmarkTrail(stack);
        return trail.isEmpty() ? 0 : chapterIndex(trail.get(trail.size() - 1));
    }

    public static List<String> bookmarkTrail(ItemStack stack) {
        if (!stack.hasTag()) return List.of();
        ListTag path = stack.getTag().getList(PAGE_STACK, Tag.TAG_STRING);
        int unlocked = chapterCount(stack);
        var trail = new ArrayDeque<String>();
        for (int i = 0; i < path.size(); i++) {
            String id = path.getString(i);
            int chapter = chapterIndex(id);
            if (chapter > 0 && chapter < unlocked) {
                if (trail.size() == MAX_BOOKMARK_DEPTH) trail.removeFirst();
                trail.addLast(id);
            } else if (chapter == 0) {
                trail.clear();
            }
        }
        return List.copyOf(trail);
    }

    public static boolean setBookmark(ItemStack stack, String chapter) {
        int index = chapterIndex(chapter);
        if (index < 0 || index >= chapterCount(stack)) return false;
        return setBookmarkTrail(stack, CHAPTERS.subList(1, index + 1));
    }

    public static boolean setBookmarkTrail(ItemStack stack, List<String> trail) {
        if (trail.size() > MAX_BOOKMARK_DEPTH) return false;
        int unlocked = chapterCount(stack);
        ListTag path = new ListTag();
        for (String visited : trail) {
            int chapter = chapterIndex(visited);
            if (chapter <= 0 || chapter >= unlocked) return false;
            path.add(StringTag.valueOf(visited));
        }
        stack.getOrCreateTag().put(PAGE_STACK, path);
        return true;
    }
}
