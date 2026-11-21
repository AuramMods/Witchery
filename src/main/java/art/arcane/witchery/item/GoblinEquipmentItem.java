package art.arcane.witchery.item;

import art.arcane.witchery.client.model.GoblinEquipmentModel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class GoblinEquipmentItem extends ArmorItem {
    public enum Piece {
        HELM(Type.HELMET, "_head"), QUIVER(Type.CHESTPLATE, ""), GURDLE(Type.LEGGINGS, "_legs");

        private final Type type;
        private final String texture;

        Piece(Type type, String texture) {
            this.type = type;
            this.texture = texture;
        }
    }

    private final Piece piece;

    public GoblinEquipmentItem(Piece piece) {
        super(piece == Piece.HELM ? ArmorMaterials.IRON : ArmorMaterials.LEATHER, piece.type,
                new Properties().durability((piece == Piece.HELM ? ArmorMaterials.DIAMOND : ArmorMaterials.IRON)
                        .getDurabilityForType(piece.type)).rarity(piece == Piece.HELM ? Rarity.RARE : Rarity.EPIC));
        this.piece = piece;
    }

    public static boolean wears(LivingEntity wearer, Piece piece) {
        return wearer.getItemBySlot(piece.type.getSlot()).getItem() instanceof GoblinEquipmentItem item && item.piece == piece;
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player wearer) {
        if (!(level instanceof ServerLevel server)) return;
        long time = level.getGameTime();
        if (piece == Piece.HELM) {
            if (time % 5 == 1) deflectGazes(server, wearer);
        } else if (time % 100 == 0) {
            Piece partner = piece == Piece.QUIVER ? Piece.GURDLE : Piece.QUIVER;
            var nearby = new AABB(wearer.position(), wearer.position()).inflate(8);
            if (!level.getEntitiesOfClass(Player.class, nearby,
                    other -> other != wearer && other.isAlive() && !other.isSpectator() && wears(other, partner)).isEmpty()) {
                wearer.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                wearer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
            }
        }
    }

    private static void deflectGazes(ServerLevel level, Player wearer) {
        Vec3 center = wearer.position().add(0, wearer.getBbHeight() / 2, 0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(16),
                living -> living != wearer && living.isAlive() && !living.isSpectator())) {
            var head = target.getItemBySlot(EquipmentSlot.HEAD);
            if (head.is(Items.CARVED_PUMPKIN) || head.is(Items.PUMPKIN)) continue;
            Vec3 direction = center.subtract(target.getEyePosition());
            double distance = direction.length();
            if (distance == 0 || target.getViewVector(1).dot(direction.scale(1 / distance)) <= 1 - 0.025 / distance
                    || !target.hasLineOfSight(wearer)) continue;
            if (target instanceof ServerPlayer player) {
                float yaw = (float) Math.toDegrees(Math.atan2(player.getZ() - wearer.getZ(), player.getX() - wearer.getX())) - 90;
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), yaw, player.getXRot());
                if (!player.hasEffect(MobEffects.HUNGER)) player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100), wearer);
            } else if (!target.hasEffect(MobEffects.WEAKNESS)) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100), wearer);
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return piece != Piece.HELM || super.isFoil(stack);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, @Nullable String type) {
        return "witchery:textures/entities/goblinclothes" + piece.texture + (type == null ? "" : "_overlay") + ".png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(GoblinEquipmentModel.extension(piece == Piece.GURDLE));
    }
}
