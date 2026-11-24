package art.arcane.witchery.item;

import art.arcane.witchery.client.model.EarmuffsModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class EarmuffsItem extends ArmorItem {
    public EarmuffsItem() {
        super(ArmorMaterials.LEATHER, Type.HELMET, new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        super.appendHoverText(stack, level, tooltip, flags);
        tooltip.add(Component.translatable("item.witchery.earmuffs.tip"));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity wearer, EquipmentSlot slot, @Nullable String type) {
        return type == null ? "witchery:textures/entities/earmuffs.png"
                : "witchery:textures/entities/empty64x64_overlay.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(EarmuffsModel.extension());
    }
}
