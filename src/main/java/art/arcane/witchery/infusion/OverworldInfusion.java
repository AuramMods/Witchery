package art.arcane.witchery.infusion;

import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.ritual.ProtectionRites;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class OverworldInfusion {
    public static final int ID = 2;
    private static final Set<Block> SOFT_LANDING = Set.of(Blocks.GRASS_BLOCK, Blocks.MYCELIUM, Blocks.GRAVEL,
            Blocks.SAND, Blocks.RED_SAND, Blocks.SNOW_BLOCK);
    private static final Set<Item> METAL = Set.of(Items.IRON_INGOT, Items.GOLD_INGOT, Items.GOLD_NUGGET,
            Items.IRON_AXE, Items.IRON_HOE, Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_SWORD,
            Items.GOLDEN_AXE, Items.GOLDEN_HOE, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_SWORD,
            Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
            Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
            Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
    private static final Set<Block> ROCK_SOURCE = Set.of(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL,
            Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.COBBLESTONE,
            Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE,
            Blocks.CUT_SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE_SLAB, Blocks.PETRIFIED_OAK_SLAB,
            Blocks.SMOOTH_STONE_SLAB, Blocks.COBBLESTONE_SLAB, Blocks.STONE_BRICK_SLAB, Blocks.BRICK_SLAB,
            Blocks.NETHER_BRICK_SLAB, Blocks.SANDSTONE_SLAB, Blocks.RED_SANDSTONE_SLAB, Blocks.QUARTZ_SLAB,
            Blocks.BRICKS, Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE_STAIRS, Blocks.CLAY, Blocks.SOUL_SAND,
            Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS,
            Blocks.BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS, Blocks.MYCELIUM, Blocks.NETHER_BRICKS,
            Blocks.NETHER_BRICK_STAIRS, Blocks.SANDSTONE_STAIRS, Blocks.RED_SANDSTONE_STAIRS, Blocks.TERRACOTTA,
            Blocks.COAL_BLOCK, Blocks.NETHERRACK);
    private static final Map<ServerLevel, List<Shockwave>> WAVES = new WeakHashMap<>();

    private OverworldInfusion() {}

    public static void fall(ServerPlayer player, LivingFallEvent event) {
        if (event.getDistance() <= 3) return;
        ServerLevel level = player.serverLevel();
        BlockPos ground = player.blockPosition().below();
        Block block = level.getBlockState(ground).getBlock();
        if (!SOFT_LANDING.contains(block)) return;

        if (player.isShiftKeyDown()) {
            if (!InfusionEnergy.spend(player, 10)) return;
            event.setDistance(0);
            level.explode(player, player.getX(), ground.getY() + 0.5, player.getZ(), 3, Level.ExplosionInteraction.TNT);
        } else if (InfusionEnergy.canSpend(player, 5) && level.removeBlock(ground, false)) {
            InfusionEnergy.spend(player, 5);
            event.setDistance(0);
            drop(level, ground, new ItemStack(block == Blocks.RED_SAND ? Blocks.SAND : block));
        }
    }

    public static void attack(ServerPlayer player, LivingEntity target) {
        if (!target.isAlive() || target.isSpectator()) return;
        for (ItemStack stack : target.getAllSlots()) {
            if (!METAL.contains(stack.getItem())) continue;
            boolean sneak = player.isShiftKeyDown();
            if (InfusionEnergy.spend(player, sneak ? 4 : 2)) {
                Vec3 look = player.getLookAngle();
                Vec3 motion = new Vec3(look.x * 2.4, sneak ? 1.5 : 0.3, look.z * 2.4);
                target.setDeltaMovement(motion);
                target.hasImpulse = true;
                if (target instanceof ServerPlayer victim) {
                    WitcheryNetwork.sendPushTarget(victim, victim.getId(), motion.x, motion.y, motion.z);
                } else {
                    target.hurtMarked = true;
                }
            }
            return;
        }
    }

    public static void channel(ServerPlayer player, int elapsed) {
        if (elapsed < 40) return;
        if (!player.isShiftKeyDown()) {
            if (elapsed % 20 == 0) WitcherySounds.playLegacyPlayerCue(player, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F);
            return;
        }
        if (elapsed % 4 != 0 || !InfusionEnergy.spend(player, 1)) return;

        ServerLevel level = player.serverLevel();
        Vec3 feet = player.position();
        Vec3 destination = player.getEyePosition();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(feet, feet).inflate(6),
                entity -> METAL.contains(entity.getItem().getItem()))) {
            Vec3 offset = destination.subtract(item.position());
            // The original makes X the one-block step. Use a bounded pull close to alignment.
            double alongX = Math.abs(offset.x);
            double distance = alongX >= 1 ? alongX : Math.max(1, offset.length());
            item.setPos(item.position().add(offset.scale(1 / distance)));
        }

        convertNearbyOres(player, level, new BlockPos((int) feet.x, (int) feet.y, (int) feet.z));
    }

    private static void convertNearbyOres(ServerPlayer player, ServerLevel level, BlockPos center) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -6; x <= 6; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -6; z <= 6; z++) {
                    pos.setWithOffset(center, x, y, z);
                    BlockState state = level.getBlockState(pos);
                    Item ingot = oreToIngot(state);
                    if (ingot == null) continue;
                    if (!InfusionEnergy.canSpend(player, 2)) return;
                    if (replaceOre(level, pos, state, ingot, 1)) InfusionEnergy.spend(player, 2);
                }
            }
        }
    }

    public static void release(ServerPlayer player, int elapsed) {
        HitResult hit = OtherwhereInfusion.rayTrace(player, 4);
        if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) {
            blockRelease(player, block);
            return;
        }
        if (!player.isShiftKeyDown() && hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof Mob target
                && InfusionEnergy.spend(player, 2)) {
            ItemStack held = target.getMainHandItem();
            if (METAL.contains(held.getItem()) && target.spawnAtLocation(held, 2) != null) {
                target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            return;
        }

        int seconds = elapsed / 20;
        if (!player.isShiftKeyDown() && seconds >= 2 && InfusionEnergy.spend(player, 6 * seconds)) {
            WAVES.computeIfAbsent(player.serverLevel(), level -> new ArrayList<>())
                    .add(new Shockwave(player, 2 * seconds + 2));
        } else {
            WitcherySounds.playLegacyPlayerCue(player, SoundEvents.NOTE_BLOCK_SNARE.value(), 0.5F);
        }
    }

    private static void blockRelease(ServerPlayer player, BlockHitResult hit) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (player.isShiftKeyDown()) {
            if (!InfusionEnergy.spend(player, 2)) return;
            Item ingot = oreToIngot(state);
            if (ingot != null) replaceOre(level, pos, state, ingot, 2);
            return;
        }
        if (hit.getDirection() == Direction.UP) {
            if (level.getBlockState(pos.below(10)).blocksMotion() && InfusionEnergy.spend(player, 2)) {
                raiseTerrain(level, pos);
            }
            return;
        }
        if (hit.getDirection() == Direction.DOWN || !ROCK_SOURCE.contains(state.getBlock())
                || (state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE)
                || level.getBlockState(pos.relative(hit.getDirection().getClockWise())).blocksMotion()
                || !InfusionEnergy.canSpend(player, 3)) return;
        ThrownBrew rock = ThrownBrew.forTerrainRock(level, player, pos);
        if (!level.addFreshEntity(rock)) return;
        if (!state.equals(level.getBlockState(pos)) || !level.removeBlock(pos, false)) {
            rock.discard();
            return;
        }
        InfusionEnergy.spend(player, 3);
        WitcheryNetwork.sendTerrainRockCue(level, Vec3.atLowerCornerOf(pos), true);
    }

    private static void raiseTerrain(ServerLevel level, BlockPos top) {
        List<Entity> standing = level.getEntities(null, new AABB(top).expandTowards(0, 1, 0));
        if (!shiftColumn(level, top, 6, 3)) return;
        for (Entity entity : standing) entity.teleportTo(entity.getX(), entity.getY() + 3, entity.getZ());
    }

    private static boolean shiftColumn(ServerLevel level, BlockPos top, int depth, int delta) {
        boolean shifted = false;
        for (int i = 0; i < depth; i++) {
            BlockPos source = top.below(delta > 0 ? i : depth - i - 1);
            BlockPos destination = source.above(delta);
            BlockState moving = level.getBlockState(source);
            BlockState previous = level.getBlockState(destination);
            if (!canMove(level, source, moving) || !canMove(level, destination, previous)) continue;
            if (level.setBlockAndUpdate(destination, moving) || moving.equals(level.getBlockState(destination))) {
                shifted |= moving.isAir() ? !previous.isAir() : level.removeBlock(source, false);
            }
        }
        return shifted;
    }

    private static boolean canMove(ServerLevel level, BlockPos pos, BlockState state) {
        return !level.isOutsideBuildHeight(pos) && !state.hasBlockEntity() && state.getDestroySpeed(level, pos) >= 0;
    }

    @Nullable
    private static Item oreToIngot(BlockState state) {
        if (!state.is(Tags.Blocks.ORES)) return null;
        return state.getTags().map(tag -> tag.location())
                .filter(id -> id.getNamespace().equals("forge") && id.getPath().startsWith("ores/"))
                .map(id -> ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "ingots/" + id.getPath().substring(5))))
                .flatMap(tag -> BuiltInRegistries.ITEM.getTag(tag).stream())
                .flatMap(values -> values.stream()).map(Holder::value).findFirst().orElse(null);
    }

    private static boolean replaceOre(ServerLevel level, BlockPos pos, BlockState ore, Item ingot, int amount) {
        if (!canMove(level, pos, ore)) return false;
        Block host = Blocks.STONE;
        if (ore.is(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE)) host = Blocks.DEEPSLATE;
        else if (ore.is(Tags.Blocks.ORES_IN_GROUND_NETHERRACK)) host = Blocks.NETHERRACK;
        if (!level.setBlockAndUpdate(pos, host.defaultBlockState())) return false;
        drop(level, pos, new ItemStack(ingot, amount));
        return true;
    }

    private static void drop(ServerLevel level, BlockPos pos, ItemStack stack) {
        level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack));
    }

    public static void tick(ServerLevel level) {
        List<Shockwave> waves = WAVES.get(level);
        if (waves == null) return;
        waves.removeIf(wave -> wave.tick(level));
        if (waves.isEmpty()) WAVES.remove(level);
    }

    private static final class Shockwave {
        private final BlockPos center;
        private final UUID caster;
        private final int stages;
        private int stage;

        private Shockwave(ServerPlayer player, int stages) {
            center = new BlockPos((int) player.getX(), (int) player.getY() - 1, (int) player.getZ());
            caster = player.getUUID();
            this.stages = stages;
        }

        private boolean tick(ServerLevel level) {
            int radius = ++stage + 2;
            if (stage == 1) ring(level, center, radius, 1);
            else {
                ring(level, center.above(2), radius, -1);
                ring(level, center.above(), radius - 1, -1);
            }
            if (stage < stages) ring(level, center, radius + 1, 2);
            else ring(level, center.above(), radius, -1);

            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(caster);
            Vec3 origin = Vec3.atLowerCornerOf(center);
            AABB area = new AABB(center.offset(-radius, 1, -radius), center.offset(radius, 3, radius));
            int outerRadius = radius + 1;
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area)) {
                double distance = center.distSqr(victim.blockPosition());
                if (distance < radius * radius || distance > outerRadius * outerRadius) continue;
                victim.hurt(owner == null ? level.damageSources().magic() : level.damageSources().playerAttack(owner), 8);
                Vec3 offset = victim.position().subtract(origin);
                if (offset.lengthSqr() <= 36 && !(victim instanceof Player) && !(victim instanceof EnderDragon)) {
                    Vec3 motion = ProtectionRites.repulsion(offset);
                    victim.push(motion.x, motion.y, motion.z);
                }
            }
            return stage >= stages;
        }

        private static void ring(ServerLevel level, BlockPos center, int radius, int delta) {
            int x = radius;
            int z = 0;
            int error = 1 - radius;
            while (x >= z) {
                pixel(level, center.offset(x, 0, z), delta);
                pixel(level, center.offset(z, 0, x), delta);
                pixel(level, center.offset(-x, 0, z), delta);
                pixel(level, center.offset(-z, 0, x), delta);
                pixel(level, center.offset(-x, 0, -z), delta);
                pixel(level, center.offset(-z, 0, -x), delta);
                pixel(level, center.offset(x, 0, -z), delta);
                pixel(level, center.offset(z, 0, -x), delta);
                z++;
                if (error < 0) error += 2 * z + 1;
                else {
                    x--;
                    error += 2 * (z - x + 1);
                }
            }
        }

        private static void pixel(ServerLevel level, BlockPos pos, int delta) {
            BlockPos solid = delta > 0 ? pos.below() : pos;
            BlockPos opening = delta > 0 ? pos.above() : pos.above(delta - 1);
            if (!level.isEmptyBlock(solid) && !level.getBlockState(opening).blocksMotion()) {
                shiftColumn(level, pos, 2, delta);
            }
        }
    }
}
