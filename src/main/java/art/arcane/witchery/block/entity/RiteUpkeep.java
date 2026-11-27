package art.arcane.witchery.block.entity;

import art.arcane.witchery.entity.CovenWitch;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.ritual.ItemRiteRecipe;
import art.arcane.witchery.ritual.LandscapeRites;
import art.arcane.witchery.ritual.ProtectionRites;
import art.arcane.witchery.ritual.RiteActivation;
import art.arcane.witchery.ritual.RiteOfTransposition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

interface RiteUpkeep {
    String traceName();

    boolean tick(ServerLevel level, BlockPos heart, Consumer<Runnable> terrainChanges);

    record Ward(ProtectionRites.Upkeep progress) implements RiteUpkeep {
        @Override
        public String traceName() {
            return progress.kind().name();
        }

        @Override
        public boolean tick(ServerLevel level, BlockPos heart, Consumer<Runnable> terrainChanges) {
            return progress.tick(level, heart);
        }
    }

    final class Timed implements RiteUpkeep {
        private static final int LEGACY_ACTIVATION_TICKS = 20;
        private final ResourceLocation rite;
        private final int coven;
        @Nullable private final UUID initiator;
        private String initiatorName = "";
        @Nullable private LandscapeRites.Progress landscape;
        @Nullable private AltarBlockEntity rechargeSource;
        private int oreDepth;
        private int ticks;
        private boolean resumingLegacyWave;

        private Timed(ResourceLocation rite, int coven, @Nullable UUID initiator,
                      @Nullable LandscapeRites.Progress landscape, int oreDepth) {
            this.rite = rite;
            this.coven = coven;
            this.initiator = initiator;
            this.landscape = landscape;
            this.oreDepth = oreDepth;
        }

        static Timed landscape(ResourceLocation rite, int coven, @Nullable UUID initiator, LandscapeRites.Progress progress) {
            return new Timed(rite, coven, initiator, progress, 0);
        }

        static Timed transposeOres(ResourceLocation rite, int coven, @Nullable UUID initiator, int completedDepth) {
            return new Timed(rite, coven, initiator, null, completedDepth);
        }

        static Timed recharge(ResourceLocation rite, int coven, @Nullable UUID initiator) {
            return new Timed(rite, coven, initiator, null, 0);
        }

        private int interval() {
            if (isLegacyFissure()) return 1;
            if (landscape != null) return landscape.interval();
            return oreDepth > 0 ? RiteOfTransposition.ORE_PULSE_TICKS : LEGACY_ACTIVATION_TICKS;
        }

        private boolean isLegacyFissure() {
            return landscape != null && landscape.action() == ItemRiteRecipe.Action.PART_EARTH
                    && RiteActivation.legacyId(rite) == 19;
        }

        private boolean hasLegacyWaveGate() {
            return landscape != null && landscape.action().usesExpansionClock();
        }

        void waitForLegacyWave() {
            if (!hasLegacyWaveGate()) return;
            ticks = interval() - LEGACY_ACTIVATION_TICKS;
            resumingLegacyWave = true;
        }

        void restorePreviousForgeTicks(int savedTicks) {
            // Earlier fissures waited twenty ticks between cuts. Keep their next cut at its saved deadline.
            restoreTicks(isLegacyFissure() ? Mth.clamp(savedTicks, 0, LEGACY_ACTIVATION_TICKS - 1)
                    + 1 - LEGACY_ACTIVATION_TICKS : savedTicks);
        }

        private void restoreTicks(int savedTicks) {
            int interval = interval();
            int minimum = isLegacyFissure() || hasLegacyWaveGate() ? interval - LEGACY_ACTIVATION_TICKS : 0;
            ticks = Mth.clamp(savedTicks, minimum, interval - 1);
        }

        void restoreInitiatorName(String name) {
            initiatorName = name;
        }

        @Override
        public String traceName() {
            return rite.toString();
        }

        @Override
        public boolean tick(ServerLevel level, BlockPos heart, Consumer<Runnable> terrainChanges) {
            if (landscape != null && !LandscapeRites.canReach(level, heart, landscape.target(), coven)) return false;
            if (++ticks < interval()) return true;
            ticks = 0;
            if (landscape != null) {
                if (resumingLegacyWave) {
                    RiteHeartBlockEntity.playOriginalRiteSound(level, landscape.target(), SoundEvents.FIRE_EXTINGUISH);
                    resumingLegacyWave = false;
                }
                landscape = LandscapeRites.pulse(level, heart, landscape, coven, caster(level), terrainChanges,
                        RiteActivation.legacyId(rite) != Integer.MAX_VALUE);
                return landscape != null;
            }
            if (oreDepth > 0) return RiteOfTransposition.transposeOreLayer(level, heart, ++oreDepth, coven);
            return rechargeNearby(level, heart);
        }

        boolean rechargeNearby(ServerLevel level, BlockPos heart) {
            boolean legacy = RiteActivation.legacyId(rite) == 4;
            AltarBlockEntity altar = legacy ? AltarPower.upkeepSource(level, heart, rechargeSource) : AltarPower.nearest(level, heart);
            if (altar == null || !altar.consumePower(40)) {
                ServerPlayer player = caster(level);
                if (player != null) player.displayClientMessage(Component.translatable("message.witchery.rite.missing_power"), true);
                level.playSound(null, heart, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 0.9F, 1);
                return false;
            }
            rechargeSource = altar;
            for (ServerPlayer player : RiteHeartBlockEntity.nearbyPlayers(level, heart, 4, legacy)) {
                if (!InfusionEnergy.recharge(player, 10)) continue;
                if (legacy) {
                    WitcheryNetwork.sendInfusionRecharge(level, player.position());
                } else {
                    level.sendParticles(ParticleTypes.INSTANT_EFFECT, player.getX(), player.getY(0.5), player.getZ(),
                            8, 0.35, 0.65, 0.35, 0);
                    player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.8F, 1);
                }
            }
            return true;
        }

        @Nullable
        private ServerPlayer caster(ServerLevel level) {
            var players = level.getServer().getPlayerList();
            if (initiator != null) return players.getPlayer(initiator);
            ServerPlayer named = initiatorName.isEmpty() ? null : players.getPlayerByName(initiatorName);
            return named != null && named.serverLevel() == level ? named : null;
        }

        void save(CompoundTag tag) {
            tag.putString("Rite", rite.toString());
            tag.putInt("StepTicks", ticks);
            if (resumingLegacyWave) tag.putBoolean("ResumingLegacyWave", true);
            if (isLegacyFissure()) tag.putBoolean("FissurePerTick", true);
            if (coven > 0) {
                tag.putInt("CovenSize", coven);
                tag.putInt("CovenSummoned", coven);
            }
            if (initiator != null) tag.putUUID("Initiator", initiator);
            if (!initiatorName.isEmpty()) tag.putString("InitiatorName", initiatorName);
            if (landscape != null) tag.put("Landscape", landscape.save());
            else if (oreDepth > 0) tag.putInt("OreDepth", oreDepth);
            else tag.putBoolean("Recharging", true);
        }

        @Nullable
        static Timed load(CompoundTag tag) {
            ResourceLocation rite = tag.contains("Rite", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("Rite")) : null;
            if (rite == null) return null;
            int coven = Mth.clamp(tag.getInt("CovenSize"), 0, CovenWitch.MAX_MEMBERS);
            UUID initiator = tag.hasUUID("Initiator") ? tag.getUUID("Initiator") : null;
            LandscapeRites.Progress landscape = LandscapeRites.Progress.load(tag.getCompound("Landscape"), coven);
            int depth = Mth.clamp(tag.getInt("OreDepth"), 0, RiteOfTransposition.oreDepthLimit(coven) - 1);
            if (landscape == null && depth == 0 && !tag.getBoolean("Recharging")) return null;
            var upkeep = new Timed(rite, coven, initiator, landscape, depth);
            upkeep.restoreInitiatorName(tag.getString("InitiatorName"));
            if (tag.getBoolean("FissurePerTick")) upkeep.restoreTicks(tag.getInt("StepTicks"));
            else upkeep.restorePreviousForgeTicks(tag.getInt("StepTicks"));
            upkeep.resumingLegacyWave = tag.getBoolean("ResumingLegacyWave") && upkeep.hasLegacyWaveGate();
            return upkeep;
        }
    }
}
