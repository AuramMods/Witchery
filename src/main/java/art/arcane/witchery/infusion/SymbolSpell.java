package art.arcane.witchery.infusion;

import art.arcane.witchery.entity.SymbolProjectile;
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

public enum SymbolSpell {
    IGNIANIMA(39, SpellKnowledge.IGNIANIMA, 2, 0xFFE760, 3, 200, false, strokes("←", "↑", "↓")),
    CARNOSA_DIEM(40, SpellKnowledge.CARNOSA_DIEM, 1, 0, 0, 0, false, strokes("→", "↑", "↓")),
    MORSMORDRE(41, SpellKnowledge.MORSMORDRE, 20, 0x000000, 3, 8, false, strokes("↑", "←", "→")),
    TORMENTUM(42, SpellKnowledge.TORMENTUM, 25, 0x222222, 4, 200, true, strokes("↓", "←", "→"));

    public static final int TORMENTUM_COOLDOWN = 30 * 60 * 20;
    private final int id;
    private final SpellKnowledge knowledge;
    private final int cost;
    private final int color;
    private final int size;
    private final int lifetime;
    private final boolean falls;
    private final List<String> patterns;

    SymbolSpell(int id, SpellKnowledge knowledge, int cost, int color, int size, int lifetime, boolean falls,
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
    public int size() { return size; }
    public int lifetime() { return lifetime; }
    public boolean falls() { return falls; }
    public Component displayName() { return knowledge.nameComponent(); }

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
        if (!InfusionEnergy.canSpend(player, 0)) return;
        if (!player.getAbilities().instabuild) {
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
        if (!InfusionEnergy.canSpend(player, cost)) return;

        if (this == CARNOSA_DIEM) {
            player.hurt(WitcheryDamageTypes.demonic(level, player, null), player.getMaxHealth() * 0.1F);
            InfusionEnergy.recharge(player, 10);
            level.sendParticles(DustParticleOptions.REDSTONE, player.getX(), player.getY(0.5), player.getZ(),
                    16, 0.5, 1, 0.5, 0);
            level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.7F, 1);
        } else {
            if (!level.addFreshEntity(new SymbolProjectile(level, player, this))) return;
            level.playSound(null, player.blockPosition(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 0.8F, 1);
        }
        InfusionEnergy.spend(player, cost);
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
}
