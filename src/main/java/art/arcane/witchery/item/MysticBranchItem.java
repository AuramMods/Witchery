package art.arcane.witchery.item;

import art.arcane.witchery.block.PlacedItemBlock;
import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class MysticBranchItem extends Item {
    private static final String DRAWING = "WITCMysticBranchDrawing";
    private static final float STROKE_ANGLE = 7;
    private static final int MAX_STROKES = 16;

    public MysticBranchItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 36000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return PlacedItemBlock.placeOnAltar(context, stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        CompoundTag drawing = new CompoundTag();
        updateAngles(player, drawing);
        player.getPersistentData().put(DRAWING, drawing);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        if (living instanceof Player player && player.getPersistentData().contains(DRAWING, Tag.TAG_COMPOUND)) {
            recordStroke(player, player.getPersistentData().getCompound(DRAWING));
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int remaining) {
        if (!(level instanceof ServerLevel server) || !(living instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData();
        if (!data.contains(DRAWING)) return;
        CompoundTag drawing = data.getCompound(DRAWING);
        data.remove(DRAWING);
        recordStroke(player, drawing);
        SymbolSpell spell = SymbolSpell.byId(drawing.getInt("Prepared"));
        if (spell != null) spell.cast(server, player, spell.gestureLevel(drawing.getString("Strokes")));
        else SymbolSpell.reject(player, "unknown_symbol");
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity living, int remaining) {
        if (living instanceof Player player) player.getPersistentData().remove(DRAWING);
    }

    public static String strokes(Player player) {
        return player.getPersistentData().getCompound(DRAWING).getString("Strokes");
    }

    private static void recordStroke(Player player, CompoundTag drawing) {
        String strokes = drawing.getString("Strokes");
        if (drawing.contains("Prepared") || strokes.length() >= MAX_STROKES) return;
        float pitch = drawing.getFloat("Pitch") - player.getXRot();
        float yaw = Mth.wrapDegrees(drawing.getFloat("Yaw") - player.getYRot());
        char stroke;
        if (pitch >= STROKE_ANGLE) stroke = '↑';
        else if (pitch <= -STROKE_ANGLE) stroke = '↓';
        else if (yaw <= -STROKE_ANGLE) stroke = '→';
        else if (yaw >= STROKE_ANGLE) stroke = '←';
        else return;

        strokes += stroke;
        drawing.putString("Strokes", strokes);
        updateAngles(player, drawing);
        SymbolSpell spell = SymbolSpell.match(strokes);
        if (spell != null) {
            drawing.putInt("Prepared", spell.id());
            if (player instanceof ServerPlayer server) {
                WitcherySounds.playLegacyPlayerCue(server, WitcherySounds.LEGACY_HARP.get(), 1);
            }
        }
    }

    private static void updateAngles(Player player, CompoundTag drawing) {
        drawing.putFloat("Pitch", player.getXRot());
        drawing.putFloat("Yaw", player.getYRot());
    }
}
