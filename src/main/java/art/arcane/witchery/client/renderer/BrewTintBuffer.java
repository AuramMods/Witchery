package art.arcane.witchery.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.FastColor;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;

public final class BrewTintBuffer extends VertexConsumerWrapper {
    private final int red;
    private final int green;
    private final int blue;

    public BrewTintBuffer(VertexConsumer target, int rgb) {
        super(target);
        red = FastColor.ARGB32.red(rgb);
        green = FastColor.ARGB32.green(rgb);
        blue = FastColor.ARGB32.blue(rgb);
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        return super.color(r * red / 256, g * green / 256, b * blue / 256, a);
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        super.defaultColor(r * red / 256, g * green / 256, b * blue / 256, a);
    }
}
