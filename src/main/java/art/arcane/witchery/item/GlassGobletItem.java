package art.arcane.witchery.item;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.entity.WitcheryFollower;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public final class GlassGobletItem extends Item {
    private static final String BLOOD_UUID = "WITCBloodUUID";
    private static final String BLOOD_NAME = "WITCBloodName";
    private static final String CHICKEN = "__chicken";
    private static final String LILITH = "__lilith";
    private static final DustParticleOptions BLOOD_PARTICLES = new DustParticleOptions(new Vector3f(0.7F, 0.01F, 0.03F), 1);
    private static final int[][] STRING_POSITIONS = {
            {0, -3}, {1, -3}, {2, -3}, {2, -2}, {3, -2}, {3, -1}, {3, 0}, {3, 1}, {3, 2}, {2, 2},
            {2, 3}, {1, 3}, {0, 3}, {-1, 3}, {-2, 3}, {-2, 2}, {-3, 2}, {-3, 1}, {-3, 0}
    };

    public GlassGobletItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        BlockPos skull = context.getClickedPos();
        if (!level.getBlockState(skull).is(Blocks.SKELETON_SKULL)) return InteractionResult.PASS;
        if (context.getPlayer() instanceof ServerPlayer player) {
            ItemStack stack = context.getItemInHand();
            var world = player.serverLevel();
            boolean valid = CHICKEN.equals(source(stack)) && world.dimension().equals(Level.OVERWORLD) && world.isNight()
                    && world.canSeeSky(skull) && WitcheryServerConfig.ALLOW_VAMPIRE_RITUAL.get()
                    && ritualAt(world, skull)
                    && world.getEntitiesOfClass(WitcheryFollower.class, new AABB(skull).inflate(32)).isEmpty();
            if (!valid || !summonFollower(world, skull, player)) {
                tell(player, "ritual_failed");
                player.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 0.6F, 1);
                return InteractionResult.CONSUME;
            }
            empty(stack);
            world.removeBlock(skull, false);
            var lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.moveTo(Vec3.atBottomCenterOf(skull));
                world.addFreshEntity(lightning);
            }
            tell(player, "follow_elle");
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean summonFollower(ServerLevel world, BlockPos skull, ServerPlayer owner) {
        var follower = WitcheryEntities.FOLLOWER.get().create(world);
        if (follower == null) return false;
        follower.moveTo(Vec3.atBottomCenterOf(skull.above()));
        follower.bind(owner);
        if (!world.addFreshEntity(follower)) return false;
        bloodParticles(world, follower);
        follower.playSound(WitcherySounds.LILITH_TALK.get(), 1, 1);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return net.minecraft.world.item.ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return filled(stack) ? UseAnim.DRINK : UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity drinker) {
        if (!(drinker instanceof ServerPlayer player)) return stack;
        if (filled(stack)) {
            if (!CHICKEN.equals(source(stack))) Vampirism.drinkGoblet(player);
            empty(stack);
            player.playSound(SoundEvents.PLAYER_BURP, 0.5F, 1);
        } else if (Vampirism.level(player) >= 9 && Vampirism.drawBlood(player, 125)) {
            fill(stack, player.getUUID().toString(), player.getGameProfile().getName());
            bloodParticles(player.serverLevel(), player);
        } else {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 0.6F, 1);
            tell(player, Vampirism.level(player) == 0 ? "empty" : Vampirism.level(player) < 9 ? "rank" : "blood_low");
        }
        return stack;
    }

    public static void onChickenOffering(Chicken offering, ServerPlayer player) {
        if (!(player.getMainHandItem().getItem() instanceof BolineItem)) return;
        ItemStack goblet = ItemStack.EMPTY;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack held = player.getInventory().getItem(slot);
            if (held.getItem() instanceof GlassGobletItem) {
                goblet = held;
                break;
            }
        }
        if (goblet.isEmpty()) return;
        var world = player.serverLevel();
        for (BlockPos pos : BlockPos.betweenClosed(offering.blockPosition().offset(-1, -1, -1),
                offering.blockPosition().offset(1, 1, 1))) {
            if (ritualAt(world, pos)) {
                fill(goblet, CHICKEN, "Chicken");
                bloodParticles(world, offering);
                return;
            }
        }
    }

    private static boolean ritualAt(ServerLevel level, BlockPos skull) {
        if (!level.getBlockState(skull).is(Blocks.SKELETON_SKULL)) return false;
        for (int[] offset : STRING_POSITIONS) {
            if (!level.getBlockState(skull.offset(offset[0], 0, offset[1])).is(Blocks.TRIPWIRE)) return false;
        }
        for (BlockPos pos : BlockPos.betweenClosed(skull.offset(-3, 0, -3), skull.offset(3, 0, 3))) {
            if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                    || !level.isEmptyBlock(pos.above()) || !level.isEmptyBlock(pos.above(2))) return false;
            int x = Math.abs(skull.getX() - pos.getX());
            int z = Math.abs(skull.getZ() - pos.getZ());
            if (x == 3 && z == 3 && !level.getBlockState(pos).is(Blocks.TORCH)) return false;
            if (Math.max(x, z) == 1 && !level.getBlockState(pos).is(Blocks.REDSTONE_WIRE)) return false;
        }
        return true;
    }

    public static void bloodParticles(ServerLevel level, LivingEntity entity) {
        level.sendParticles(BLOOD_PARTICLES, entity.getX(), entity.getY() + entity.getBbHeight() * 0.8, entity.getZ(),
                16, 0.5, 0.2, 0.5, 0);
        entity.playSound(WitcherySounds.BLOOD_DRINK.get(), 1, 1);
    }

    public static void fillFromLilith(ItemStack stack) {
        fill(stack, LILITH, "Lilith");
    }

    private static void fill(ItemStack stack, String source, String name) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(BLOOD_UUID, source);
        tag.putString(BLOOD_NAME, name);
    }

    private static void empty(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(BLOOD_UUID);
            tag.remove(BLOOD_NAME);
        }
    }

    public static boolean filled(ItemStack stack) {
        return !source(stack).isEmpty();
    }

    private static String source(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(BLOOD_UUID);
    }

    @Override
    public Component getName(ItemStack stack) {
        return filled(stack) ? Component.translatable("item.witchery.glassgoblet.filled") : super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        if (filled(stack)) {
            tooltip.add(Component.translatable("item.witchery.glassgoblet.source", stack.getTag().getString(BLOOD_NAME))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public static void tell(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.translatable("message.witchery.glassgoblet." + message).withStyle(ChatFormatting.DARK_PURPLE));
    }
}
