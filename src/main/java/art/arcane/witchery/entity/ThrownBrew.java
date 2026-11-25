package art.arcane.witchery.entity;

import art.arcane.witchery.brewing.BrewCreatureEffects;
import art.arcane.witchery.brewing.BrewTerrain;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class ThrownBrew extends ThrowableItemProjectile {
    private String legacyOwnerName = "";

    public ThrownBrew(EntityType<? extends ThrownBrew> type, Level level) {
        super(type, level);
    }

    public ThrownBrew(LivingEntity thrower, Level level) {
        super(WitcheryEntities.THROWN_BREW.get(), thrower, level);
    }

    @Nullable
    @Override
    public Entity getOwner() {
        var owner = super.getOwner();
        if (owner != null || legacyOwnerName.isEmpty() || !(level() instanceof ServerLevel server)) return owner;
        for (var player : server.players()) {
            if (player.getGameProfile().getName().equals(legacyOwnerName)) {
                setOwner(player);
                legacyOwnerName = "";
                return player;
            }
        }
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!legacyOwnerName.isEmpty()) tag.putString("ownerName", legacyOwnerName);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        legacyOwnerName = tag.hasUUID("Owner") ? "" : tag.getString("ownerName");
        boolean fixedRecipe = getType() == WitcheryEntities.LEGACY_FIXED_BREW.get();
        if ((fixedRecipe || getType() == WitcheryEntities.LEGACY_CUSTOM_BREW.get()) && getItem().isEmpty()) {
            setItem(readLegacyPayload(tag, fixedRecipe));
            if (!fixedRecipe && tag.getBoolean("Spell")) setNoGravity(true);
        }
    }

    public static ItemStack readLegacyPayload(CompoundTag tag, boolean fixedRecipe) {
        if (!fixedRecipe) {
            ItemStack bottle = ItemStack.of(tag.getCompound("Brew"));
            return bottle.getItem() instanceof BrewBottleItem ? bottle : ItemStack.EMPTY;
        }
        if (!tag.contains("damageValue", Tag.TAG_ANY_NUMERIC)) return ItemStack.EMPTY;
        var ingredient = new CompoundTag();
        ingredient.putString("id", "witchery:ingredient");
        ingredient.putByte("Count", (byte) 1);
        ingredient.putInt("Damage", tag.getInt("damageValue"));
        ItemStack stack = WitcheryIngredient.loadInventoryStack(ingredient);
        return stack.getItem() instanceof ThrowableBrewItem || stack.is(WitcheryIngredient.QUICKLIME.item())
                || stack.is(WitcheryIngredient.ROCK.item()) || stack.is(WitcheryIngredient.WEB.item())
                ? stack : ItemStack.EMPTY;
    }

    public static ThrownBrew forUrnSpell(ServerLevel level, Player caster, ItemStack bottle) {
        var brew = new ThrownBrew(caster, level);
        brew.setItem(bottle);
        brew.setNoGravity(true);
        // The original constructor chose the ordinary brew speed before setting its spell flag.
        BrewProjectileLaunch.fromPlayer(brew, caster, 0, 0.75F);
        return brew;
    }

    public static ThrownBrew forTerrainRock(ServerLevel level, Player caster, BlockPos source) {
        var rock = new ThrownBrew(caster, level);
        rock.setItem(new ItemStack(WitcheryIngredient.ROCK.item()));
        BrewProjectileLaunch.fromPlayer(rock, caster, 0, 1.5F);
        rock.setPos(Vec3.atCenterOf(source));
        return rock;
    }

    public static boolean throwBottle(ServerLevel level, Player player, ItemStack stack) {
        var brew = new ThrownBrew(player, level);
        brew.setItem(stack);
        // The original fixed-brew entity launched before assigning the ingredient subtype.
        if (stack.getItem() instanceof BrewBottleItem) BrewProjectileLaunch.fromPlayer(brew, player, -20, 0.75F);
        else BrewProjectileLaunch.fromPlayer(brew, player, 0, 1.5F);
        if (!level.addFreshEntity(brew)) return false;
        level.playSound(null, player, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return true;
    }

    @Override
    public void shoot(double x, double y, double z, float speed, float inaccuracy) {
        BrewProjectileLaunch.shoot(this, random, new Vec3(x, y, z), speed, inaccuracy);
    }

    @Override
    public void tick() {
        if (getItem().isEmpty()) {
            if (!level().isClientSide) discard();
            return;
        }
        super.tick();
        if (level() instanceof ServerLevel level) {
            boolean spell = isNoGravity();
            if (tickCount >= 200 && (spell || getItem().getItem() instanceof BrewBottleItem)) discard();
            else if (spell && !isRemoved()) {
                int color = BrewBottleItem.color(getItem());
                var options = new net.minecraft.core.particles.DustParticleOptions(
                        net.minecraft.world.phys.Vec3.fromRGB24(color).toVector3f(), 1);
                level.sendParticles(options, getX(), getY(), getZ(), 4, 0.12, 0.12, 0.12, 0);
            }
        }
    }

    @Override
    protected Item getDefaultItem() {
        return getType() == WitcheryEntities.THROWN_BREW.get() ? WitcheryIngredient.BREW_OF_INK.item() : Items.AIR;
    }

    @Override
    protected float getGravity() {
        return getItem().is(WitcheryIngredient.ROCK.item()) || getItem().is(WitcheryIngredient.WEB.item())
                ? super.getGravity() : 0.05F;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (getItem().getItem() instanceof BrewBottleItem && !(target instanceof LivingEntity)) return false;
        if (target == getOwner()) return tickCount >= 5 && target.canBeHitByProjectile();
        return super.canHitEntity(target);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel server) {
            Vec3 impact = hit.getLocation();
            if (getItem().is(WitcheryIngredient.ROCK.item())) {
                if (hit instanceof EntityHitResult entity) {
                    entity.getEntity().hurt(server.damageSources().thrown(this, getOwner()), 6);
                }
                WitcheryNetwork.sendTerrainRockCue(server, position(), false);
                discard();
                return;
            }
            if (getItem().is(WitcheryIngredient.WEB.item())) {
                impactWeb(server, hit);
                discard();
                return;
            }
            if (getItem().is(WitcheryIngredient.QUICKLIME.item())) {
                BrewCreatureEffects.quicklime(this, hit);
                discard();
                return;
            }
            if (getItem().getItem() instanceof BrewBottleItem brew) {
                brew.impact(server, this, hit);
                discard();
                return;
            }
            if (getItem().getItem() instanceof ThrowableBrewItem brew) {
                brew.impact(this, hit);
            }
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, getItem()),
                    impact.x, impact.y, impact.z, 12, 0.2, 0.2, 0.2, 0.1);
            server.playSound(null, impact.x, impact.y, impact.z, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
            discard();
        }
    }

    private void impactWeb(ServerLevel level, HitResult hit) {
        var web = Blocks.COBWEB.defaultBlockState();
        if (hit instanceof EntityHitResult entity) {
            Entity target = entity.getEntity();
            BlockPos pos = new BlockPos((int) target.getX(), (int) target.getY(), (int) target.getZ());
            if (!BrewTerrain.isProtectedTerrain(level.getBlockState(pos))) {
                BrewTerrain.setBlock(getOwner(), level, pos, web, Direction.UP);
            }
        } else if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos struck = block.getBlockPos();
            Direction face = Direction.UP;
            BlockPos pos = struck;
            if (!level.getBlockState(struck).is(Blocks.SNOW_BLOCK)) {
                // The original Thrown Web maps the north/south faces to X and west/east to Z.
                face = switch (block.getDirection()) {
                    case NORTH -> Direction.WEST;
                    case SOUTH -> Direction.EAST;
                    case WEST -> Direction.NORTH;
                    case EAST -> Direction.SOUTH;
                    default -> block.getDirection();
                };
                pos = struck.relative(face);
            }
            var previous = level.getBlockState(pos);
            if ((!previous.blocksMotion() || previous.is(Blocks.SNOW_BLOCK))
                    && BrewTerrain.setBlock(getOwner(), level, pos, web, face)) {
                WitcheryNetwork.sendThrownWebImpact(level, Vec3.atCenterOf(pos));
            }
        }
    }
}
