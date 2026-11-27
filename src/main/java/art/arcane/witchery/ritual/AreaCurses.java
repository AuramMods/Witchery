package art.arcane.witchery.ritual;

import art.arcane.witchery.brewing.BrewCreatureEffects;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.entity.WitchHunterPursuit;
import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.item.VoodooPoppetItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

final class AreaCurses {
    private static final TicketType<BlockPos> HELL_SITE =
            TicketType.create("witchery_hell_on_earth", Comparator.comparingLong(BlockPos::asLong), 60);
    private static final Set<Block> ORIGINAL_FLOWERS = Set.of(Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID,
            Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP, Blocks.OXEYE_DAISY);

    private AreaCurses() {}

    static int curseRadius(int coven) {
        return 80 + 2 * coven;
    }

    static boolean pulse(ServerLevel level, BlockPos heart, BlockPos target, int stage, int coven,
                         ItemRiteRecipe.Action action, @Nullable ServerPlayer initiator, boolean summon, boolean legacy) {
        ServerPlayer caster = initiator != null && initiator.level() == level ? initiator : null;
        if (stage == 1 && caster != null) WitchHunterPursuit.noticeBlackMagic(caster);
        int radius = stage + 3;
        boolean enhanced = caster != null && FamiliarBinding.hasCurseMastery(caster);
        ServerPlayer terrainActor = caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
        if (action == ItemRiteRecipe.Action.HELL_ON_EARTH) {
            return hellOnEarth(level, heart, target, stage, coven, radius, enhanced, terrainActor, summon);
        }
        boolean blind = action == ItemRiteRecipe.Action.AREA_BLINDNESS;
        Vec3 center = Vec3.atCenterOf(target);
        MobEffect effect = blind ? MobEffects.BLINDNESS : MobEffects.CONFUSION;
        int duration = blind && enhanced ? 6000 : 2400;
        for (ServerPlayer player : level.players()) {
            if (!ExpandingWave.contains(player, center, radius)) continue;
            if (VoodooPoppetItem.deflectRite(caster, player, 6)) return false;
            if (!player.hasEffect(effect)) player.addEffect(new MobEffectInstance(effect, duration, blind ? 0 : 1));
        }

        if (legacy) originalCreatureActions(level, center, radius, blind, duration);
        else creatureActions(level, target, radius, center, blind, duration);
        ExpandingWave.visitSurface(level, target, radius, true, pos -> {
            if (!blind) alterSurface(level, pos, radius, false, enhanced, terrainActor, legacy);
        });
        return true;
    }

    private static void creatureActions(ServerLevel level, BlockPos target, int radius, Vec3 center, boolean blind, int duration) {
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(target).inflate(radius),
                candidate -> candidate.isAlive() && ExpandingWave.contains(candidate, center, radius))) {
            if (blind) {
                if (!mob.hasEffect(MobEffects.BLINDNESS)) mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration));
            } else if (mob instanceof Villager villager) {
                if (level.random.nextInt(10) == 0) BrewCreatureEffects.infectVillager(level, villager);
            } else if (mob instanceof Cow cow && level.random.nextInt(20) == 0) {
                BrewCreatureEffects.turnIntoMooshroom(level, cow);
            } else if (mob instanceof Animal && level.random.nextInt(3) == 0) {
                mob.hurt(level.damageSources().magic(), 20);
            }
        }
    }

    private static void originalCreatureActions(ServerLevel level, Vec3 center, int radius, boolean blind, int duration) {
        if (blind) {
            for (var entity : level.getAllEntities()) {
                if (entity instanceof Mob mob && ExpandingWave.contains(mob, center, radius) && !mob.hasEffect(MobEffects.BLINDNESS)) {
                    mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration));
                }
            }
            return;
        }

        List<Villager> villagers = new ArrayList<>();
        List<Cow> cows = new ArrayList<>();
        List<Animal> animals = new ArrayList<>();
        for (var entity : level.getAllEntities()) {
            if (!ExpandingWave.contains(entity, center, radius)) continue;
            if (entity instanceof Villager villager) {
                if (level.random.nextInt(10) == 0) villagers.add(villager);
            } else if (entity instanceof Cow cow && level.random.nextInt(20) == 0) {
                cows.add(cow);
            } else if (entity instanceof Animal animal && level.random.nextInt(3) == 0) {
                animals.add(animal);
            }
        }
        villagers.forEach(villager -> BrewCreatureEffects.infectVillager(level, villager, true));
        cows.forEach(cow -> BrewCreatureEffects.turnIntoMooshroom(level, cow, true));
        animals.forEach(animal -> animal.hurt(level.damageSources().magic(), 20));
    }

    private static void alterSurface(ServerLevel level, BlockPos surface, int radius, boolean hell,
                                     boolean enhanced, ServerPlayer caster, boolean legacy) {
        BlockState state = level.getBlockState(surface);
        BlockPos soil = surface;
        boolean grass = state.is(Blocks.GRASS) || state.is(Blocks.FERN);
        if (grass || isCrop(state, legacy)) {
            BlockState replacement = hell ? Blocks.FIRE.defaultBlockState()
                    : (grass ? Blocks.AIR : Blocks.DEAD_BUSH).defaultBlockState();
            if (!hell || enhanced && WitcheryServerConfig.ALLOW_HELL_ON_EARTH_FIRES.get()) {
                if (!LandscapeRites.replace(level, surface, replacement, caster) && legacy) return;
            }
            soil = surface.below();
        } else if (!hell && state.is(Blocks.FARMLAND)) {
            LandscapeRites.replace(level, surface, Blocks.SAND.defaultBlockState(), caster);
            return;
        } else if (!state.blocksMotion()) {
            soil = surface.below();
        }
        BlockState ground = level.getBlockState(soil);
        if (!(ground.is(Blocks.DIRT) || ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.MYCELIUM)
                || ground.is(Blocks.FARMLAND) || hell && ground.is(Blocks.SAND))) return;
        if (hell) {
            if (level.random.nextInt(radius < 20 / 3 ? 2 : radius < 10 ? 4 : 6) == 0) {
                LandscapeRites.replace(level, soil, Blocks.NETHERRACK.defaultBlockState(), caster);
            }
        } else {
            int roll = level.random.nextInt(enhanced ? 4 : 5);
            if (roll < 2) LandscapeRites.replace(level, soil, (roll == 0 ? Blocks.SAND : Blocks.DIRT).defaultBlockState(), caster);
        }
    }

    private static boolean isCrop(BlockState state, boolean legacy) {
        return (legacy ? ORIGINAL_FLOWERS.contains(state.getBlock()) : state.is(BlockTags.SMALL_FLOWERS))
                || state.is(Blocks.CARROTS) || state.is(Blocks.WHEAT)
                || state.is(Blocks.POTATOES) || state.is(Blocks.PUMPKIN_STEM) || state.is(Blocks.MELON_STEM)
                || state.is(Blocks.ATTACHED_PUMPKIN_STEM) || state.is(Blocks.ATTACHED_MELON_STEM)
                || state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN);
    }

    private static boolean hellOnEarth(ServerLevel level, BlockPos heart, BlockPos target, int stage, int coven,
                                       int radius, boolean enhanced, ServerPlayer caster, boolean summon) {
        int maxRadius = 20 + 2 * coven;
        if (!target.equals(heart)) {
            level.getChunkSource().addRegionTicket(HELL_SITE, new ChunkPos(target), 2 + maxRadius / 16, target);
            level.getChunkAt(target);
        }
        if (radius <= maxRadius) {
            ExpandingWave.visitSurface(level, target, radius, true,
                    pos -> alterSurface(level, pos, radius, true, enhanced, caster, false));
        }
        if (radius >= maxRadius && stage <= 250 && summon) {
            var altar = AltarPower.nearest(level, target);
            if (altar == null || !altar.consumePower(200)) return false;
            spawnNetherCreature(level, target.above(2));
        }
        return true;
    }

    private static void spawnNetherCreature(ServerLevel level, BlockPos pos) {
        double roll = level.random.nextDouble();
        EntityType<? extends Mob> type = roll < 0.02 ? WitcheryEntities.DEMON.get()
                : roll < 0.1 ? EntityType.GHAST : roll < 0.4 ? EntityType.BLAZE
                : roll < 0.6 ? EntityType.MAGMA_CUBE : EntityType.ZOMBIFIED_PIGLIN;
        Mob creature = type.create(level);
        if (creature == null) return;
        Vec3 arrival = Vec3.atBottomCenterOf(pos);
        creature.moveTo(arrival);
        ForgeEventFactory.onFinalizeSpawn(creature, level, level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
        level.addFreshEntity(creature);
        // The original cue follows each paid attempt, including a spawn canceled by Forge.
        WitcheryNetwork.sendHellOnEarthArrival(level, arrival);
    }
}
