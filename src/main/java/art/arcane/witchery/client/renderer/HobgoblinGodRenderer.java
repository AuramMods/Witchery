package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.HobgoblinGodModel;
import art.arcane.witchery.entity.HobgoblinGod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class HobgoblinGodRenderer extends MobRenderer<HobgoblinGod, HobgoblinGodModel> {
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.HEAD);
    private final ResourceLocation texture;

    public HobgoblinGodRenderer(EntityRendererProvider.Context context, boolean mog) {
        super(context, new HobgoblinGodModel(context.bakeLayer(mog ? HobgoblinGodModel.MOG : HobgoblinGodModel.GULG)), 0.5F);
        texture = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/" + (mog ? "mog" : "gulg") + ".png");
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        addLayer(new BossGlintLayer<>(this, BossGlintLayer.Tint.PATRON));
        var layer = mog ? HobgoblinGodModel.MOG : HobgoblinGodModel.GULG;
        var inner = new HobgoblinGodModel(context.bakeLayer(layer));
        var outer = new HobgoblinGodModel(context.bakeLayer(layer));
        addLayer(new HumanoidArmorLayer<>(this, inner, outer, context.getModelManager()) {
            @Override
            public void render(PoseStack pose, MultiBufferSource buffers, int light, HobgoblinGod god, float stride,
                               float speed, float partial, float age, float yaw, float pitch) {
                super.render(pose, buffers, light, god, stride, speed, partial, age, yaw, pitch);
                if (!BossGlintLayer.isVisible(god)) return;
                for (EquipmentSlot slot : ARMOR_SLOTS) {
                    var stack = god.getItemBySlot(slot);
                    if (stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == slot) {
                        var model = slot == EquipmentSlot.LEGS ? inner : outer;
                        getParentModel().copyPropertiesTo(model);
                        setPartVisibility(model, slot);
                        BossGlintLayer.draw(getArmorModelHook(god, stack, slot, model), god.tickCount, pose, buffers,
                                light, BossGlintLayer.Tint.PATRON, true);
                    }
                }
            }
        });
    }

    @Override
    protected int getBlockLightLevel(HobgoblinGod god, BlockPos pos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(HobgoblinGod god) {
        return texture;
    }

    @Override
    public Vec3 getRenderOffset(HobgoblinGod god, float partialTick) {
        Vec3 offset = super.getRenderOffset(god, partialTick);
        return god.isShiftKeyDown() ? offset.add(0, -0.125, 0) : offset;
    }
}
