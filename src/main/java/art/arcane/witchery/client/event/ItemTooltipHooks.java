package art.arcane.witchery.client.event;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class ItemTooltipHooks {
    private ItemTooltipHooks() {}

    @SubscribeEvent
    public static void addItemDescription(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        String description = stack.getDescriptionId();
        if (!description.startsWith("item.witchery.")) return;
        var tag = stack.getTag();
        if (tag != null && (tag.getInt("HideFlags") & ItemStack.TooltipPart.ADDITIONAL.getMask()) != 0) return;
        if (description.equals("item.witchery.deathscowl") && WitcheryServerConfig.SPEC.isLoaded()
                && !WitcheryServerConfig.ALLOW_DEATHS_HOOD_TO_FREEZE_VICTIMS.get()) return;

        var language = Language.getInstance();
        String key = description + ".tooltip";
        if (!language.has(key)) return;
        var tooltip = event.getToolTip();
        int index = Math.min(1, tooltip.size());
        if (tooltip.size() > 1 && tooltip.get(1).getContents() instanceof TranslatableContents contents
                && contents.getKey().equals("guide.witchery.atlas_selection")) {
            index++;
        }
        // Legacy translations use separate lines to preserve their original wrapping and color codes.
        for (String line : language.getOrDefault(key).split("\\|")) {
            if (!line.isBlank()) tooltip.add(index++, Component.literal(line));
        }
    }
}
