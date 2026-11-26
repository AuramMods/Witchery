package art.arcane.witchery.infusion;

import art.arcane.witchery.entity.SymbolProjectile;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.integration.BloodMagicCompat;
import art.arcane.witchery.item.LeonardsUrnItem;
import art.arcane.witchery.item.RitualChalkItem;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public enum SymbolSpell {
    IGNIANIMA(39, SpellKnowledge.IGNIANIMA, 2, 0xFFE760, 3, 200, false, strokes("←", "↑", "↓")),
    CARNOSA_DIEM(40, SpellKnowledge.CARNOSA_DIEM, 1, 0, 0, 0, false, strokes("→", "↑", "↓")),
    MORSMORDRE(41, SpellKnowledge.MORSMORDRE, 20, 0x000000, 3, 8, false, strokes("↑", "←", "→")),
    TORMENTUM(42, SpellKnowledge.TORMENTUM, 25, 0x222222, 4, 200, true, strokes("↓", "←", "→")),
    CONFUNDUS(8, null, 1, 16_771_328, 1.5F, 200, false, List.of("←←↑↑→", "←←←↑↑→→", "←←←↑↑↑→→", "←←←←↑↑↑↑→→→")),
    EXPELLIARMUS(15, null, 1, 16_747_778, 3, 200, false, ranked("↑", "↓")),
    LEONARD_1(43, null, 5, 0, 0, 0, false, List.of("→↑←←↓")),
    LEONARD_2(44, null, 5, 0, 0, 0, false, List.of("←↓→→↑")),
    LEONARD_3(45, null, 5, 0, 0, 0, false, List.of("↓→↑↑←")),
    LEONARD_4(46, null, 5, 0, 0, 0, false, List.of("↑←↓↓→")),
    FLIPENDO(17, null, 1, 16_775_577, 3, 200, false, ranked("→", "←")),
    IMPEDIMENTA(19, null, 1, 6_191_615, 1.5F, 200, false, ranked("←", "→")),
    ATTRAHO(47, null, 1, 5_322_534, 1, 200, false, List.of("↑↑↑→→↓←")),
    AGUAMENTI(2, null, 1, 1_176_575, 2, 200, false, List.of("↑↑→→↓", "↑↑→→→↓", "↑↑↑→→↓↓", "↑↑↑→→→↓↓", "↑↑↑↑→→↓↓↓", "↑↑↑↑→→→↓↓↓")),
    INCENDIO(21, null, 1, 16_724_023, 2, 200, false, List.of("←↑↑↓↓", "←↑↑↑↓↓↓", "←↑↑↑↑↓↓↓↓")),
    LUMOS(22, null, 1, 16_777_018, 0.5F, 200, false, List.of("↓↓↓→")),
    METEOLOJINX_RECANTO(23, null, 100, 0, 0, 0, false, List.of("↑↑↑→→↓↑→→↓↓")),
    NOX(26, null, 50, 0, 0, 0, false, List.of("↑↑→↓→↑")),
    ACCIO(1, null, 1, 5_322_534, 1, 200, false, List.of("←↑→→↓", "←↑→→→↓", "←↑↑→→↓↓", "←↑↑→→→↓↓", "←↑↑↑→→→↓↓↓", "←↑↑↑→→→→↓↓↓")),
    ALOHOMORA(3, null, 1, 5_322_534, 0.5F, 200, false, List.of("→↑→→↓", "→↑→→→↓", "→↑↑→→↓↓", "→↑↑→→→↓↓")),
    CAVE_INIMICUM(5, null, 1, 3_158_064, 3, 200, false, List.of("↑←↑↑→", "↑←↑↑↑→", "↑←←↑↑→→", "↑←←↑↑↑→→", "↑←←←↑↑↑↑→→→")),
    COLLOPORTUS(6, null, 1, 5_322_534, 1, 200, false, List.of("←←↓↓→", "←←↓↓↓→", "←←←↓↓→→", "←←←↓↓→↓→")),
    DEFODIO(10, null, 3, 4_008_220, 2.5F, 200, false, List.of("↑↑←↓", "↑↑↑←↓↓", "↑↑←←↓→", "↑↑↑←←↓↓→", "↑↑↑↑←←↓↓↓→", "↑↑↑←←←↓↓→→", "↑↑↑↑←←←↓↓↓→→")),
    AVADA_KEDAVRA(4, null, 101, 65_280, 2, 200, false, List.of("↓↓→→↑↑←←←←↓↓→")),
    CRUCIO(9, null, 5, 6_684_927, 2, 200, false, List.of("↓←↓↓→", "↓←←↓↓→→", "↓←↓↓↓→", "↓←←↓↓↓→→", "↓←←←↓↓↓↓→→→")),
    ENNERVATE(12, null, 1, 16_713_595, 1.5F, 200, true, List.of("↑←↑→←↑→", "↑←←↑→→←←↑→→", "↑←←←↑→→→←←←↑→→→")),
    EPISKEY(13, null, 1, 0, 0, 0, false, List.of("→↑←↓↓→", "→↑↑←↓↓↓↓→", "→→↑←←↓↓→→", "→→↑↑←←↓↓↓↓→→")),
    FLAGRATE(16, null, 1, 0, 0, 0, false, List.of("→↑→←↑→")),
    IMPERIO(20, null, 10, 10_686_463, 1.5F, 200, false, List.of("→↓↓↓↓")),
    PROTEGO(31, null, 1, 0, 0, 0, false, List.of("↓↓↑", "↓↓↓↑↑", "↓↓↓↓↑↑↑")),
    STUPEFY(36, null, 5, 1_279, 1.5F, 200, true, List.of("→→↑←↑→", "→→→↑←←↑→→", "→→↑↑←↑↑→", "→→→↑↑←←↑↑→→"));

    public static final int TORMENTUM_COOLDOWN = 30 * 60 * 20;
    private final int id;
    @Nullable private final SpellKnowledge knowledge;
    private final int cost;
    private final int color;
    private final float size;
    private final int lifetime;
    private final boolean falls;
    private final List<String> patterns;

    SymbolSpell(int id, @Nullable SpellKnowledge knowledge, int cost, int color, float size, int lifetime, boolean falls,
                List<String> patterns) {
        this.id = id;
        this.knowledge = knowledge;
        this.cost = cost;
        this.color = color;
        this.size = size;
        this.lifetime = lifetime;
        this.falls = falls;
        this.patterns = patterns;
    }

    public int id() { return id; }
    public int color() { return color; }
    public float size() { return size; }
    public int lifetime() { return lifetime; }
    public boolean falls() { return falls; }
    public boolean isCurse() {
        return switch (this) {
            case AVADA_KEDAVRA, CRUCIO, IMPERIO, IGNIANIMA, CARNOSA_DIEM, MORSMORDRE, TORMENTUM -> true;
            default -> false;
        };
    }
    public int baseCost() { return cost; }
    public List<String> gestures() { return patterns; }
    public boolean visibleInGuide(int knownMask) {
        return this != AVADA_KEDAVRA && this != FLAGRATE
                && (knowledge == null || (knownMask & (1 << knowledge.ordinal())) != 0);
    }
    public Component displayName() {
        return knowledge == null ? Component.translatable("spell.witchery." + name().toLowerCase(Locale.ROOT)) : knowledge.nameComponent();
    }

    @Nullable
    public static SymbolSpell byId(int id) {
        for (SymbolSpell spell : values()) {
            if (spell.id == id) return spell;
        }
        return null;
    }

    @Nullable
    public static SymbolSpell match(String strokes) {
        for (SymbolSpell spell : values()) {
            if (spell.patterns.contains(strokes)) return spell;
        }
        return null;
    }

    public void cast(ServerLevel level, ServerPlayer player) {
        cast(level, player, 1);
    }

    public int gestureLevel(String strokes) {
        return switch (this) {
            case CONFUNDUS, EXPELLIARMUS, FLIPENDO, IMPEDIMENTA -> Math.max(1, patterns.indexOf(strokes));
            case AGUAMENTI, ACCIO, CRUCIO, STUPEFY -> 1 + patterns.indexOf(strokes) / 2;
            case INCENDIO, ENNERVATE -> Math.max(1, 1 + patterns.indexOf(strokes));
            case EPISKEY -> 1 + (patterns.indexOf(strokes) + 1) / 2;
            case DEFODIO -> 1 + patterns.indexOf(strokes) / 3;
            case CAVE_INIMICUM -> Math.max(1, patterns.indexOf(strokes) - 1);
            default -> 1;
        };
    }

    public void cast(ServerLevel level, ServerPlayer player, int requestedLevel) {
        boolean creative = player.getAbilities().instabuild;
        if (!creative && !InfusionEnergy.canSpend(player, 0)) return;
        if (id >= LEONARD_1.id && id <= LEONARD_4.id) {
            castUrn(level, player);
            return;
        }
        if (isCurse() && knowledge == null && !creative && InfusionEnergy.type(player) != InfernalInfusion.ID) {
            reject(player, "infernal_required");
            return;
        }
        if (!creative && knowledge != null) {
            if (!knowledge.isKnown(player)) {
                reject(player, "unknown_spell");
                return;
            }
            long cooldown = this == TORMENTUM ? knowledge.cooldownRemaining(player, TORMENTUM_COOLDOWN) : 0;
            if (cooldown > 0) {
                reject(player, "cooldown", (cooldown + 19) / 20);
                return;
            }
        }
        var worship = player.getEffect(WitcheryEffects.WORSHIP.get());
        int effectLevel = requestedLevel > 1 && requestedLevel <= 3 && worship != null
                && requestedLevel <= worship.getAmplifier() + 2 ? requestedLevel : 1;
        int charge = cost << (effectLevel - 1);
        if (!creative && !InfusionEnergy.canSpend(player, charge)) return;

        switch (this) {
            case CARNOSA_DIEM -> {
                float health = player.getMaxHealth() * 0.1F;
                player.hurt(WitcheryDamageTypes.demonic(level, player, null), health);
                InfusionEnergy.recharge(player, 10);
                BloodMagicCompat.boost(player, health);
                level.sendParticles(DustParticleOptions.REDSTONE, player.getX(), player.getY(0.5), player.getZ(),
                        16, 0.5, 1, 0.5, 0);
                level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.7F, 1);
            }
            case METEOLOJINX_RECANTO -> {
                if (level.isRaining()) level.getServer().overworld().setWeatherParameters(0, 0, false, false);
                else snare(player);
            }
            case NOX -> SymbolTerrain.extinguishLights(level, player);
            case EPISKEY -> {
                if (effectLevel == 1) heal(level, player);
                else {
                    double radius = effectLevel == 2 ? 2 : 4;
                    for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(player.position(), player.position()).inflate(radius),
                            target -> target.isAlive() && target.distanceToSqr(player) <= radius * radius)) {
                        heal(level, target);
                    }
                }
            }
            case FLAGRATE, PROTEGO -> {
                if (OtherwhereInfusion.rayTrace(player, 4, ClipContext.Block.OUTLINE) instanceof BlockHitResult hit
                        && hit.getType() == HitResult.Type.BLOCK) {
                    if (this == FLAGRATE) RitualChalkItem.drawInfernal(level, hit, player);
                    else LightInfusion.shield(level, hit.getBlockPos(), player);
                } else {
                    snare(player);
                }
            }
            default -> {
                if (!level.addFreshEntity(new SymbolProjectile(level, player, this, effectLevel))) return;
                level.levelEvent(LevelEvent.SOUND_GHAST_FIREBALL,
                        new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ()), 0);
            }
        }
        if (!creative) InfusionEnergy.spend(player, charge);
    }

    private static void heal(ServerLevel level, LivingEntity target) {
        Player player = target instanceof Player hurtPlayer ? hurtPlayer : null;
        int food = player == null ? 5 : player.getFoodData().getFoodLevel();
        if (food <= 1 || target.getHealth() >= target.getMaxHealth()) return;
        int amount = Math.min(5, food);
        target.heal(amount);
        if (player != null) player.getFoodData().eat(-amount, 0);
        if (!target.hasEffect(MobEffects.CONFUSION)) target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80));
        level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY(0.5), target.getZ(), 16, 0.45, 0.5, 0.45, 0);
        level.playSound(null, target.blockPosition(), SoundEvents.SLIME_SQUISH_SMALL, target.getSoundSource(), 1, 1);
    }

    private void castUrn(ServerLevel level, ServerPlayer player) {
        var brew = LeonardsUrnItem.readSpell(player, id - LEONARD_1.id);
        var mixture = LeonardsUrnItem.splash(brew);
        int charge = cost;
        if (mixture != null) {
            int base = mixture.used();
            var worship = player.getEffect(WitcheryEffects.WORSHIP.get());
            charge = Math.max(4, worship == null ? base * 2
                    : worship.getAmplifier() == 0 ? base + (base + 1) / 2 : base);
        }
        boolean creative = player.getAbilities().instabuild;
        if (!creative && !InfusionEnergy.canSpend(player, charge)) return;
        if (mixture == null) {
            snare(player);
        } else {
            var projectile = ThrownBrew.forUrnSpell(level, player, brew);
            if (!level.addFreshEntity(projectile)) return;
            level.playSound(null, player.blockPosition(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 0.8F, 1);
        }
        if (!creative) InfusionEnergy.spend(player, charge);
    }

    public static void reject(ServerPlayer player, String reason, Object... arguments) {
        player.displayClientMessage(Component.translatable("message.witchery.mystic_branch." + reason, arguments)
                .withStyle(ChatFormatting.RED), true);
        snare(player);
    }

    private static void snare(ServerPlayer player) {
        WitcherySounds.playLegacyPlayerCue(player, SoundEvents.NOTE_BLOCK_SNARE.value(), 0.5F);
    }

    private static List<String> strokes(String first, String turn, String last) {
        return List.of(first.repeat(2) + turn + last.repeat(2), first.repeat(2) + turn.repeat(2) + last.repeat(4),
                first.repeat(3) + turn + last.repeat(2), first.repeat(3) + turn.repeat(2) + last.repeat(4),
                first.repeat(4) + turn + last.repeat(2), first.repeat(4) + turn.repeat(2) + last.repeat(4));
    }

    private static List<String> ranked(String first, String last) {
        return List.of(first.repeat(2) + last, first.repeat(3) + last.repeat(2),
                first.repeat(4) + last.repeat(3), first.repeat(5) + last.repeat(4));
    }
}
