package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.item.GrenadeItem;
import art.arcane.witchery.item.MirrorItem;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.world.MirrorDisguise;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class MirrorBlockEntity extends BlockEntity {
    private final MirrorConversation conversation = new MirrorConversation();
    @Nullable private GlobalPos counterpart;
    @Nullable private UUID connectionId;
    private boolean connected;
    private boolean demonSlain;
    private int glassTicks;
    private boolean glassOccupied;

    public MirrorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction facing() {
        return getBlockState().getValue(MirrorBlock.FACING);
    }

    public boolean isRoomExit() {
        return ((MirrorBlock) getBlockState().getBlock()).isRoomExit();
    }

    @Nullable
    public GlobalPos counterpart() {
        return counterpart;
    }

    @Nullable
    public UUID connectionId() {
        return connectionId;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean demonSlain() {
        return demonSlain;
    }

    public void interact(ServerLevel level, ServerPlayer player, InteractionHand hand) {
        if (demonSlain || level.dimension().equals(WitcheryDimensions.MIRROR)) return;
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof TaglockItem) {
            MirrorDisguise.use(level, worldPosition, player, held);
        } else if (held.is(WitcheryIngredient.QUARTZ_SPHERE.item())) {
            fillDuplicate(level, player, hand, held);
        } else if (conversation.ask(level, worldPosition, facing(), player)) {
            setChanged();
        }
    }

    private void fillDuplicate(ServerLevel level, ServerPlayer player, InteractionHand hand, ItemStack sphere) {
        var altar = AltarPower.nearest(level, worldPosition);
        if (altar == null || !altar.consumePower(2_000)) {
            WitcheryNetwork.sendMirrorDuplicateCue(player, false);
            return;
        }
        WitcheryNetwork.sendMirrorDuplicateCue(player, true);
        ItemStack grenade = GrenadeItem.duplicate(player.getGameProfile().getName());
        if (sphere.getCount() == 1) {
            player.setItemInHand(hand, grenade);
        } else {
            sphere.shrink(1);
            if (!player.getInventory().add(grenade)) player.drop(grenade, false);
        }
    }

    public static void tickMirror(Level level, BlockPos pos, BlockState state, MirrorBlockEntity mirror) {
        if (level instanceof ServerLevel server && !mirror.demonSlain && !server.dimension().equals(WitcheryDimensions.MIRROR)
                && server.getGameTime() % 40 == 1 && mirror.conversation.rememberVisitors(server, viewingArea(pos, mirror.facing()))) {
            mirror.setChanged();
        }
    }

    public static void tickGlass(Level level, BlockPos pos, BlockState state, MirrorBlockEntity mirror) {
        if (mirror.glassTicks == 0) {
            mirror.glassOccupied = !level.getEntitiesOfClass(LivingEntity.class, viewingArea(pos, mirror.facing())).isEmpty();
        }
        mirror.glassTicks = (mirror.glassTicks + 1) % 10;
    }

    public boolean hasGlassVisitor() {
        return glassOccupied;
    }

    private static AABB viewingArea(BlockPos pos, Direction front) {
        return new AABB(pos).inflate(front.getAxis() == Direction.Axis.Z ? 1 : 0, 0,
                front.getAxis() == Direction.Axis.X ? 1 : 0).expandTowards(front.getStepX() * 4, 0, front.getStepZ() * 4);
    }

    public void bind(GlobalPos destination, UUID connection) {
        counterpart = destination;
        connectionId = connection;
        connected = true;
        sync();
    }

    public void setDisconnected() {
        if (connected) {
            connected = false;
            sync();
        }
    }

    public void markDemonSlain() {
        if (!demonSlain) {
            demonSlain = true;
            sync();
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStack toItem() {
        ItemStack item = new ItemStack(WitcheryItems.getRequired("mirror").get());
        CompoundTag data = new CompoundTag();
        writeMirrorData(data);
        conversation.save(data);
        item.setTag(data);
        return item;
    }

    public void applyItem(ItemStack stack) {
        if (stack.hasTag()) {
            readMirrorData(stack.getTag());
            conversation.load(stack.getTag());
            sync();
        }
    }

    public static boolean carriesLink(ItemStack stack, GlobalPos room, @Nullable UUID connection) {
        return !stack.isEmpty() && stack.getItem() instanceof MirrorItem && stack.hasTag()
                && (connection == null || stack.getTag().hasUUID("Connection") && connection.equals(stack.getTag().getUUID("Connection")))
                && room.equals(itemDestination(stack));
    }

    public static @Nullable GlobalPos itemDestination(ItemStack stack) {
        return stack.hasTag() ? readPosition(stack.getTag(), WitcheryDimensions.MIRROR) : null;
    }

    private static @Nullable GlobalPos readPosition(CompoundTag root, ResourceKey<Level> fallback) {
        CompoundTag coords = root.getCompound("DimCoords");
        if (!coords.contains("posX", Tag.TAG_ANY_NUMERIC) || !coords.contains("posY", Tag.TAG_ANY_NUMERIC)
                || !coords.contains("posZ", Tag.TAG_ANY_NUMERIC)) {
            return null;
        }
        String rawDimension = coords.getString("DimensionId");
        ResourceLocation key = rawDimension.isEmpty() ? null : ResourceLocation.tryParse(rawDimension);
        ResourceKey<Level> dimension = key == null ? fallback : ResourceKey.create(Registries.DIMENSION, key);
        return GlobalPos.of(dimension, new BlockPos(coords.getInt("posX"), coords.getInt("posY"), coords.getInt("posZ")));
    }

    private static ResourceKey<Level> legacyDimension(int id) {
        return switch (id) {
            case -39 -> WitcheryDimensions.MIRROR;
            case -38 -> WitcheryDimensions.TORMENT;
            case -37 -> WitcheryDimensions.DREAM;
            case -1 -> Level.NETHER;
            case 1 -> Level.END;
            default -> Level.OVERWORLD;
        };
    }

    private void readMirrorData(CompoundTag root) {
        ResourceKey<Level> fallback = isRoomExit()
                ? legacyDimension(root.getCompound("DimCoords").getInt("Dimension")) : WitcheryDimensions.MIRROR;
        counterpart = readPosition(root, fallback);
        connectionId = root.hasUUID("Connection") ? root.getUUID("Connection") : null;
        connected = counterpart != null && (!root.contains("Connected") || root.getBoolean("Connected"));
        demonSlain = root.getBoolean("DemonSlain");
    }

    private void writeMirrorData(CompoundTag root) {
        if (counterpart != null) {
            CompoundTag coords = new CompoundTag();
            coords.putInt("posX", counterpart.pos().getX());
            coords.putInt("posY", counterpart.pos().getY());
            coords.putInt("posZ", counterpart.pos().getZ());
            coords.putString("DimensionId", counterpart.dimension().location().toString());
            root.put("DimCoords", coords);
        }
        if (connectionId != null) root.putUUID("Connection", connectionId);
        root.putBoolean("DemonSlain", demonSlain);
        root.putBoolean("Connected", connected);
    }

    @Override
    public void load(CompoundTag root) {
        super.load(root);
        readMirrorData(root);
        conversation.load(root);
    }

    @Override
    protected void saveAdditional(CompoundTag root) {
        super.saveAdditional(root);
        writeMirrorData(root);
        conversation.save(root);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag update = new CompoundTag();
        writeMirrorData(update);
        return update;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
