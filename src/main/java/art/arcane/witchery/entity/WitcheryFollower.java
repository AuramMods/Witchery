package art.arcane.witchery.entity;

import art.arcane.witchery.item.GlassGobletItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class WitcheryFollower extends TamableAnimal {
    private static final String[] FAIREST_MALE_NAMES = {
            "Abraham", "Adam", "Adrian", "Alex", "Alexander", "Allen", "Ambrose", "Andrew", "Anthony", "Arthur",
            "Avery", "Barnaby", "Bartholomew", "Benedict", "Benjamin", "Bernard", "Billy", "Bobby", "Charles",
            "Charley", "Christopher", "Colin", "Conrad", "Cuthbert", "Daniel", "Danny", "Davey", "David", "Edmund",
            "Edward", "Francis", "Fred", "Freddy", "Geoffrey", "George", "Georgie", "Gerard", "Gilbert", "Giles",
            "Gregory", "Hans", "Hansel", "Heinrich", "Henry", "Hugh", "Humphrey", "Isaac", "Jack", "Jacques",
            "James", "Jamie", "Jerome", "Jim", "Jimmy", "John", "Johnny", "Joseph", "Julian", "Lancelot", "Lawrence",
            "Leonard", "Lou", "Luke", "Mark", "Martin", "Mathias", "Matthew", "Merlin", "Michael", "Miles", "Nat",
            "Nathan", "Nathaniel", "Ned", "Nicholas", "Oliver", "Oswyn", "Patrick", "Paul", "Peter", "Philip",
            "Piers", "Ralph", "Reynold", "Richard", "Ricky", "Robert", "Robin", "Roger", "Rowland", "Samuel",
            "Simon", "Solomon", "Stephen", "Terence", "Thomas", "Tim", "Tobias", "Tom", "Tommy", "Valentine",
            "Walter", "Wendell", "Will", "William", "Willie"
    };
    private static final String[] FAIREST_FEMALE_NAMES = {
            "Agnes", "Alice", "Amy", "Anna", "Annabella", "Anne", "Arabella", "Audrey", "Avis", "Barbara",
            "Beatrice", "Becky", "Bella", "Belle", "Bertha", "Bessy", "Betty", "Blanche", "Bo", "Bonny", "Bridget",
            "Catalina", "Catherine", "Cecily", "Charity", "Charlotte", "Christina", "Christine", "Cinderella",
            "Cindy", "Clara", "Clarissa", "Clemence", "Clementine", "Constance", "Daisy", "Denise", "Dorothy",
            "Edith", "Elinor", "Elizabeth", "Ella", "Ellen", "Elsa", "Elsie", "Emma", "Eve", "Evelyn", "Fawn",
            "Flora", "Florence", "Floretta", "Fortune", "Frances", "Frideswide", "Gertrude", "Gillian", "Ginger",
            "Goat", "Goatley", "Goldie", "Grace", "Gretel", "Helen", "Hilda", "Hazel", "Isabel", "Jane", "Janet",
            "Jemima", "Jill", "Joan", "Joyce", "Judith", "Julia", "Juliet", "Katherine", "Katie", "Kitty", "Lena",
            "Lily", "Liza", "Lizzie", "Lucy", "Mabel", "Maggie", "Margaret", "Margery", "Maria", "Marion", "Marlene",
            "Martha", "Mary", "Maud", "Mildred", "Millicent", "Molly", "Odette", "Pansy", "Pearl", "Petunia",
            "Philippa", "Polly", "Rachel", "Rapunzel", "Rebecca", "Rose", "Rosie", "Ruth", "Sarah", "Shiela", "Snow",
            "Susanna", "Susie", "Sybil", "Talia", "Thomasina", "Trudy", "Ursula", "Winifred"
    };
    private static final String[] FAIREST_SURNAMES = {
            "Apple", "Applegreen", "Applerose", "Appleseed", "Appleton", "Applewhite", "Baker", "Barnes", "Bean",
            "Beanblossom", "Beanstock", "Beaste", "Beasten", "Bell", "Berry", "Bird", "Blackbird", "Blackwood",
            "Boar", "Botter", "Bowers", "Bremen", "Brockett", "Buckle", "Butcher", "Candle", "Castle", "Castleton",
            "Cherry", "Cherrytree", "Cherrywood", "Cherrywell", "Cottage", "Daw", "Deer", "Dilly", "Dove", "Duck",
            "Duckfield", "Duckling", "Faery", "Fairy", "Fiddle", "Fiddler", "Fisher", "Fitcher", "Flinders",
            "Friday", "Frogg", "Frogley", "Frost", "Gold", "Goldencrown", "Goldhair", "Goodfellow", "Goose",
            "Gooseberry", "Gosling", "Gray", "Green", "Greengrass", "Griggs", "Grimm", "Grundy", "Hare", "Hay",
            "Hazeltree", "Hickory", "Hood", "Horner", "Hubbard", "Hunter", "Korbes", "Lamb", "Lampkin", "Lark",
            "Locket", "Locks", "MacDonald", "Mack", "Malone", "Marsh", "McDiddler", "Meadow", "Meadows", "Merrypips",
            "Miller", "Mills", "Mockingbird", "Monday", "Mouse", "Mouseley", "Muffet", "Mulberry", "Nimble", "Nutt",
            "O'Hare", "O'Lairy", "Pea", "Peartree", "Pease", "Peep", "Pie", "Pigeon", "Pinchme", "Piper", "Porgy",
            "Porridge", "Pott", "Pumpkin", "Pumpkinseed", "Reynard", "River", "Rivers", "Roley", "Rooster",
            "Roseberry", "Rosebottom", "Roseleaf", "Shoe", "Shoemaker", "Shorter", "Silver", "Slipper", "Sprat",
            "Saturday", "Sparrow", "Spindle", "Spindler", "Spinner", "Star", "Stone", "Stonebridge", "Sunday",
            "Swan", "Tailor", "Thatcher", "Thumb", "Thursday", "Toad", "Tower", "Towers", "Trot", "Tucker",
            "Tuesday", "Twist", "Wednesday", "White", "Whittington", "Winkie", "Wolf", "Wolfram", "Wolfson",
            "Wolfwood", "Woodcroft", "Woods"
    };
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(WitcheryFollower.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(WitcheryFollower.class, EntityDataSerializers.STRING);
    @Nullable private BlockPos lavaPool;
    private int transformationTicks;
    private int lifetime = -1;

    public WitcheryFollower(EntityType<? extends WitcheryFollower> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, 0);
        entityData.define(SKIN, "");
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new FollowOwnerGoal(this, 1, 4, 2, false) {
            @Override
            public boolean canUse() {
                return lavaPool == null && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return lavaPool == null && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return followerType() != 0 && super.canUse();
            }
        });
    }

    @Nullable
    public static WitcheryFollower spawnFairest(ServerLevel level, BlockPos pos) {
        WitcheryFollower follower = WitcheryEntities.FOLLOWER.get().create(level);
        if (follower == null) return null;
        int type = level.random.nextInt(4) + 1;
        String[] firstNames = type == 4 ? FAIREST_MALE_NAMES : FAIREST_FEMALE_NAMES;
        String name = firstNames[level.random.nextInt(firstNames.length)] + " "
                + FAIREST_SURNAMES[level.random.nextInt(FAIREST_SURNAMES.length)];
        follower.entityData.set(TYPE, type);
        follower.setCustomName(Component.literal(name));
        follower.setPersistenceRequired();
        follower.moveTo(pos.getX() + 0.5, pos.getY() + 0.01, pos.getZ() + 0.5, 0, 0);
        return level.noCollision(follower) && level.addFreshEntity(follower) ? follower : null;
    }

    public static void spawnDuplicate(ServerLevel level, Vec3 position, String skin) {
        WitcheryFollower follower = WitcheryEntities.FOLLOWER.get().create(level);
        if (follower == null) return;
        follower.entityData.set(TYPE, 5);
        follower.entityData.set(SKIN, skin);
        follower.setCustomName(Component.literal(skin.isEmpty() ? "Steve" : skin));
        follower.lifetime = 200;
        follower.setPersistenceRequired();
        follower.moveTo(position.x, position.y, position.z, 0, 0);
        ServerPlayer copied = level.getServer().getPlayerList().getPlayerByName(skin);
        if (copied != null && copied.level() == level) {
            follower.setLeftHanded(copied.getMainArm() == HumanoidArm.LEFT);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                follower.setItemSlot(slot, copied.getItemBySlot(slot).copy());
                follower.setDropChance(slot, 0);
            }
        }
        if (level.addFreshEntity(follower)) follower.attractAttention(level);
    }

    public void bind(ServerPlayer owner) {
        tame(owner);
        setPersistenceRequired();
    }

    public int followerType() {
        return entityData.get(TYPE);
    }

    public boolean isTemporaryDuplicate() {
        return followerType() == 5 && lifetime > 0;
    }

    public String playerSkin() {
        return entityData.get(SKIN);
    }

    @Override
    protected Component getTypeName() {
        return followerType() == 0 ? Component.translatable("entity.witchery.follower.elle") : super.getTypeName();
    }

    @Override
    public boolean fireImmune() {
        return followerType() == 0 || super.fireImmune();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (followerType() == 0 && (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FALL))) return false;
        return super.hurt(source, amount);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (followerType() >= 1 && followerType() <= 4) spawnAtLocation(WitcheryIngredient.HEART_OF_GOLD.item());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob parent) {
        return null;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel world) || !isAlive()) return;
        if (lifetime > 0 && --lifetime == 0) {
            discard();
            return;
        }
        if (followerType() == 5 && tickCount % 40 == 5) attractAttention(world);
        if (followerType() != 0) return;
        if (lavaPool == null && tickCount % 10 == 1) lookForPool(world);
        if (lavaPool != null) approachPool(world);
    }

    private void attractAttention(ServerLevel level) {
        if (playerSkin().isEmpty()) return;
        ServerPlayer copied = level.getServer().getPlayerList().getPlayerByName(playerSkin());
        if (copied == null || copied.level() != level) return;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, getBoundingBox().inflate(16, 8, 16),
                candidate -> candidate instanceof Enemy && candidate.getTarget() == copied)) {
            mob.setTarget(this);
        }
    }

    private void lookForPool(ServerLevel world) {
        BlockPos origin = blockPosition();
        for (int attempt = 0; attempt < 10; attempt++) {
            BlockPos candidate = origin.offset(random.nextInt(30) - 15, random.nextInt(6) - 3, random.nextInt(30) - 15);
            if (isPool(world, candidate)) {
                lavaPool = candidate;
                setNoGravity(true);
                getNavigation().stop();
                return;
            }
        }
    }

    private void approachPool(ServerLevel world) {
        Vec3 destination = Vec3.atBottomCenterOf(lavaPool.above());
        Vec3 difference = destination.subtract(position());
        if (difference.lengthSqr() > 1.5) {
            Vec3 motion = difference.normalize().scale(0.19);
            if (!world.noCollision(this, getBoundingBox().move(motion))) motion = new Vec3(0, 0.18, 0);
            setDeltaMovement(motion);
            getLookControl().setLookAt(destination.x, destination.y, destination.z);
            return;
        }
        setDeltaMovement(Vec3.ZERO);
        if (++transformationTicks == 200 && getOwner() instanceof ServerPlayer player) {
            GlassGobletItem.tell(player, "elle_warning");
            playSound(WitcherySounds.LILITH_TALK.get(), 1, 1);
        }
        if (transformationTicks >= 400) transform(world);
    }

    private void transform(ServerLevel world) {
        Lilith lilith = WitcheryEntities.LILITH.get().create(world);
        if (lilith == null) return;
        lilith.moveTo(getX(), getY() + 0.2, getZ(), getYRot(), 0);
        if (!world.addFreshEntity(lilith)) return;
        world.sendParticles(ParticleTypes.WITCH, getX(), getY() + 1, getZ(), 16, 0.7, 0.7, 0.7, 0.05);
        if (getOwner() instanceof ServerPlayer player) GlassGobletItem.tell(player, "lilith_summoned");
        lilith.playSound(WitcherySounds.LILITH_TALK.get(), 1, 1);
        world.explode(lilith, lilith.getX(), lilith.getY(), lilith.getZ(), 6, true, Level.ExplosionInteraction.MOB);
        discard();
    }

    public static boolean isPool(ServerLevel world, BlockPos center) {
        if (!clearLava(world, center)) return false;
        for (Direction side : Direction.Plane.HORIZONTAL) if (!clearLava(world, center.relative(side))) return false;
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) if (x * x + z * z <= 36 && !sourceLava(world, center.offset(x, 0, z))) return false;
        }
        return true;
    }

    private static boolean clearLava(ServerLevel world, BlockPos pos) {
        return sourceLava(world, pos) && world.isEmptyBlock(pos.above()) && world.isEmptyBlock(pos.above(2));
    }

    private static boolean sourceLava(ServerLevel world, BlockPos pos) {
        if (!world.hasChunkAt(pos)) return false;
        var fluid = world.getFluidState(pos);
        return fluid.is(FluidTags.LAVA) && fluid.isSource();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FollowerType", followerType());
        tag.putString("Skin", entityData.get(SKIN));
        tag.putInt("TicksToLive", lifetime);
        if (lavaPool != null) tag.putLong("LavaPool", lavaPool.asLong());
        tag.putInt("TransformTicks", transformationTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(TYPE, tag.getInt("FollowerType"));
        entityData.set(SKIN, tag.getString("Skin"));
        lifetime = tag.contains("TicksToLive", Tag.TAG_ANY_NUMERIC) ? tag.getInt("TicksToLive") : -1;
        lavaPool = tag.contains("LavaPool", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("LavaPool")) : null;
        transformationTicks = tag.getInt("TransformTicks");
        if (getOwnerUUID() == null && tag.contains("OwnerUUID", Tag.TAG_STRING)) {
            try {
                setOwnerUUID(UUID.fromString(tag.getString("OwnerUUID")));
                setTame(true);
            } catch (IllegalArgumentException ignored) {
                setTame(false);
            }
        }
    }
}
