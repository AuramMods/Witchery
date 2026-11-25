package art.arcane.witchery.infusion;

import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
    private static final Set<Block> ROCK_SOURCE = Set.of(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL,
            Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.COBBLESTONE,
            Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE_SLAB,
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
            drop(level, ground, new ItemStack(block));
        }
    }

    public static void attack(ServerPlayer player, LivingEntity target) {
        if (!target.isAlive() || target.isSpectator()) return;
        for (ItemStack stack : target.getAllSlots()) {
            if (!metal(stack)) continue;
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
            if (elapsed % 20 == 0) player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 1);
            return;
        }
        if (elapsed % 4 != 0 || !InfusionEnergy.spend(player, 1)) return;

        ServerLevel level = player.serverLevel();
        Vec3 feet = player.position();
        Vec3 destination = player.getEyePosition();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(feet, feet).inflate(6),
                entity -> metal(entity.getItem()))) {
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
        if (player.isShiftKeyDown()) return;
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof Mob target
                && InfusionEnergy.spend(player, 2)) {
            ItemStack held = target.getMainHandItem();
            if (metal(held) && target.spawnAtLocation(held, 2) != null) {
                target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            return;
        }

        int seconds = elapsed / 20;
        if (seconds >= 2 && InfusionEnergy.spend(player, 6 * seconds)) {
            WAVES.computeIfAbsent(player.serverLevel(), level -> new ArrayList<>())
                    .add(new Shockwave(player, 2 * seconds + 2));
        } else {
            player.playSound(SoundEvents.NOTE_BLOCK_SNARE.value(), 0.5F, 1);
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
                || level.getBlockState(pos.relative(hit.getDirection().getClockWise())).blocksMotion()
                || !InfusionEnergy.canSpend(player, 3) || !level.removeBlock(pos, false)) return;
        InfusionEnergy.spend(player, 3);
        ThrownBrew rock = new ThrownBrew(player, level);
        rock.setItem(new ItemStack(WitcheryIngredient.ROCK.item()));
        rock.setPos(Vec3.atCenterOf(pos));
        rock.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1.5F, 1);
        level.addFreshEntity(rock);
        level.sendParticles(ParticleTypes.POOF, rock.getX(), rock.getY(), rock.getZ(), 8, 0.5, 0.5, 0.5, 0.05);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, player.getSoundSource(), 0.75F, 1);
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
            if (moving.isAir() || !canMove(level, source, moving)
                    || !canMove(level, destination, level.getBlockState(destination))) continue;
            if (level.setBlockAndUpdate(destination, moving) || moving.equals(level.getBlockState(destination))) {
                shifted |= level.removeBlock(source, false);
            }
        }
        return shifted;
    }

    private static boolean canMove(ServerLevel level, BlockPos pos, BlockState state) {
        return !level.isOutsideBuildHeight(pos) && !state.hasBlockEntity() && state.getDestroySpeed(level, pos) >= 0;
    }

    private static boolean metal(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof TieredItem tool) return tool.getTier() == Tiers.IRON || tool.getTier() == Tiers.GOLD;
        if (item instanceof ArmorItem armor) {
            return armor.getMaterial() == ArmorMaterials.CHAIN || armor.getMaterial() == ArmorMaterials.IRON
                    || armor.getMaterial() == ArmorMaterials.GOLD;
        }
        return item == Items.IRON_INGOT || item == Items.GOLD_INGOT || item == Items.GOLD_NUGGET;
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
        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, stack));
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
            center = player.blockPosition().below();
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
            AABB area = new AABB(center.above()).inflate(radius, 0.5, radius);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area)) {
                Vec3 offset = victim.position().subtract(origin);
                double distance = offset.length();
                if (distance < radius || distance > radius + 1) continue;
                victim.hurt(owner == null ? level.damageSources().magic() : level.damageSources().playerAttack(owner), 8);
                if (distance <= 6 && !(victim instanceof Player) && !(victim instanceof EnderDragon)) {
                    victim.push(Math.signum(offset.x) * 0.22, offset.y * 4.32 / Math.pow(distance, 4),
                            Math.signum(offset.z) * 0.22);
                }
            }
            return stage >= stages;
        }

        private static void ring(ServerLevel level, BlockPos center, int radius, int delta) {
            int x = radius;
            int z = 0;
            int error = 1 - radius;
            while (x >= z) {
                for (int dx = -1; dx <= 1; dx += 2) {
                    for (int dz = -1; dz <= 1; dz += 2) {
                        pixel(level, center.offset(dx * x, 0, dz * z), delta);
                        if (x != z) pixel(level, center.offset(dx * z, 0, dz * x), delta);
                    }
                }
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
