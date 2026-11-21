package art.arcane.witchery.infusion;

import art.arcane.witchery.entity.SymbolProjectile;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.LeonardsUrnItem;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
    ATTRAHO(47, null, 1, 5_322_534, 1, 200, false, List.of("↑↑↑→→↓←"));

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
    public int baseCost() { return cost; }
    public List<String> gestures() { return patterns; }
    public boolean visibleInGuide(int knownMask) { return knowledge == null || (knownMask & (1 << knowledge.ordinal())) != 0; }
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
            default -> 1;
        };
    }

    public void cast(ServerLevel level, ServerPlayer player, int requestedLevel) {
        if (!InfusionEnergy.canSpend(player, 0)) return;
        if (id >= LEONARD_1.id && id <= LEONARD_4.id) {
            castUrn(level, player);
            return;
        }
        if (!player.getAbilities().instabuild && knowledge != null) {
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
        if (!InfusionEnergy.canSpend(player, charge)) return;

        if (this == CARNOSA_DIEM) {
            player.hurt(WitcheryDamageTypes.demonic(level, player, null), player.getMaxHealth() * 0.1F);
            InfusionEnergy.recharge(player, 10);
            level.sendParticles(DustParticleOptions.REDSTONE, player.getX(), player.getY(0.5), player.getZ(),
                    16, 0.5, 1, 0.5, 0);
            level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.7F, 1);
        } else {
            if (!level.addFreshEntity(new SymbolProjectile(level, player, this, effectLevel))) return;
            level.playSound(null, player.blockPosition(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 0.8F, 1);
        }
        InfusionEnergy.spend(player, charge);
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
        if (!InfusionEnergy.canSpend(player, charge)) return;
        if (mixture == null) {
            player.playSound(SoundEvents.NOTE_BLOCK_SNARE.value(), 0.9F, 1);
        } else {
            var projectile = new ThrownBrew(player, level);
            projectile.setItem(brew);
            projectile.setNoGravity(true);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 4, 1);
            if (!level.addFreshEntity(projectile)) return;
            level.playSound(null, player.blockPosition(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 0.8F, 1);
        }
        InfusionEnergy.spend(player, charge);
    }

    public static void reject(ServerPlayer player, String reason, Object... arguments) {
        player.displayClientMessage(Component.translatable("message.witchery.mystic_branch." + reason, arguments)
                .withStyle(ChatFormatting.RED), true);
        player.playSound(SoundEvents.NOTE_BLOCK_SNARE.value(), 0.9F, 1);
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
