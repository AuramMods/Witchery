package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.DreamSpiritModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.Mob;

import java.util.function.ToIntFunction;

public final class DreamSpiritRenderer<T extends Mob> extends MobRenderer<T, DreamSpiritModel<T>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/spirit.png");

    public DreamSpiritRenderer(EntityRendererProvider.Context context, ToIntFunction<T> color) {
        super(context, new DreamSpiritModel<>(context.bakeLayer(DreamSpiritModel.LAYER), color), 0);
    }

    @Override
    public ResourceLocation getTextureLocation(T spirit) {
        return TEXTURE;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(T spirit, boolean visible, boolean translucent, boolean glowing) {
        return visible || translucent ? RenderType.entityTranslucent(TEXTURE) : super.getRenderType(spirit, false, false, glowing);
    }
}
