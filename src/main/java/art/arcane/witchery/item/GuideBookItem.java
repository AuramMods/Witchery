package art.arcane.witchery.item;

import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.nbt.Tag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class GuideBookItem extends Item {
    static final String CURRENT_PAGE = "CurrentPage";
    private static final String INGREDIENT_DAMAGE = "LegacyIngredientDamage";

    public enum Volume {
        FUMES("book_collecting_fumes", 46),
        DISTILLING("book_distilling", 47),
        CIRCLES("book_circle_magic", 48),
        INFUSIONS("book_brews_and_infusions", 49),
        HERBOLOGY("book_herbology", 81),
        BIOMES("book_of_biomes", 106),
        SYMBOLOGY("book_symbology", 107),
        CONJURATION("book_conjuration_and_fetishes", 127),
        ATLAS("bookbiomes2", -1),
        CAULDRON("cauldronbook", -1);

        private final String path;
        private final int legacyDamage;

        Volume(String path, int legacyDamage) {
            this.path = path;
            this.legacyDamage = legacyDamage;
        }

        public String path() { return path; }
        public Component title() { return Component.translatable("item.witchery." + path); }
        public String textKey(String chapter) { return "guide.witchery." + path + "." + chapter; }

        @Nullable
        public static Volume fromPath(String path) {
            for (Volume volume : values()) if (volume.path.equals(path)) return volume;
            return null;
        }

        @Nullable
        public static Volume fromLegacyDamage(int damage) {
            if (damage < 0) return null;
            for (Volume volume : values()) if (volume.legacyDamage == damage) return volume;
            return null;
        }
    }

    private final Volume volume;

    public GuideBookItem(Volume volume) {
        super(new Properties());
        this.volume = volume;
    }

    @Nullable
    public static Volume volume(ItemStack stack) {
        if (stack.getItem() instanceof GuideBookItem book) return book.volume;
        if (stack.getItem() instanceof LegacyIngredient && stack.hasTag()
                && stack.getTag().contains(INGREDIENT_DAMAGE, Tag.TAG_ANY_NUMERIC)) {
            return Volume.fromLegacyDamage(stack.getTag().getInt(INGREDIENT_DAMAGE));
        }
        return null;
    }

    public static int bookmark(ItemStack stack) {
        return stack.hasTag() ? Math.max(0, stack.getTag().getInt(CURRENT_PAGE)) : 0;
    }

    public static void setBookmark(ItemStack stack, int page) {
        stack.getOrCreateTag().putInt(CURRENT_PAGE, page);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return read(level, player, hand, volume);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (volume != Volume.ATLAS || level == null) return;
        var biome = BiomeBookIndex.selectedBiome(stack, level.registryAccess());
        if (biome != null) {
            tooltip.add(Component.translatable("guide.witchery.atlas_selection", Component.translatable(biome.toLanguageKey("biome")))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static InteractionResultHolder<ItemStack> read(Level level, Player player, InteractionHand hand, Volume volume) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSpectator()) return InteractionResultHolder.pass(stack);
        if (player instanceof ServerPlayer server && server.connection != null) {
            int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : player.getInventory().getContainerSize() - 1;
            WitcheryNetwork.openGuideBook(server, slot, volume);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static final class LegacyIngredient extends Item {
        public LegacyIngredient() {
            super(new Properties());
        }

        @Override
        public Component getName(ItemStack stack) {
            if (WitcheryIngredient.TREEFYD_SEEDS.matches(stack)) return WitcheryIngredient.TREEFYD_SEEDS.item().getName(stack);
            Volume volume = volume(stack);
            return volume == null ? super.getName(stack) : volume.title();
        }

        @Override
        public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
            if (WitcheryIngredient.TREEFYD_SEEDS.matches(stack)) {
                return WitcheryIngredient.TREEFYD_SEEDS.item().onItemUseFirst(stack, context);
            }
            return super.onItemUseFirst(stack, context);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            Volume volume = volume(player.getItemInHand(hand));
            return volume == null ? super.use(level, player, hand) : read(level, player, hand, volume);
        }
    }
}
