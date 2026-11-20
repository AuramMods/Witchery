package art.arcane.witchery.lycanthropy;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.entity.Wolfman;
import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public final class WolfCombat {
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };

    private WolfCombat() {}

    public static void onDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        var source = event.getSource();
        float remaining = victim.getHealth() - event.getAmount();
        if (source.is(DamageTypes.PLAYER_ATTACK) && source.getDirectEntity() instanceof ServerPlayer attacker
                && Lycanthropy.isShifted(attacker)) {
            int rank = Lycanthropy.rank(attacker);
            if (rank >= 9 && Lycanthropy.isWolfman(attacker)) rendArmor(victim);
            if (rank >= 10) spreadFromPlayer(event, victim, remaining);
        } else if (source.is(DamageTypes.MOB_ATTACK) && source.getDirectEntity() instanceof Wolfman wolfman && wolfman.isInfectious()) {
            if (victim instanceof Villager villager && remaining < victim.getMaxHealth() * 0.25F) {
                if (WerewolfVillagers.infect(villager, false, Math.max(1, remaining))) event.setCanceled(true);
            } else if (victim instanceof ServerPlayer player && canInfect(player)) {
                Lycanthropy.infect(player);
            }
        }
    }

    private static void spreadFromPlayer(LivingDamageEvent event, LivingEntity victim, float remaining) {
        if (remaining <= 0 || remaining >= victim.getMaxHealth() * 0.25F) return;
        if (victim instanceof Villager villager && victim.getRandom().nextInt(4) == 1) {
            if (WerewolfVillagers.infect(villager, false, remaining)) event.setCanceled(true);
        } else if (victim instanceof ServerPlayer player && WitcheryServerConfig.ALLOW_PLAYER_TO_PLAYER_WOLF_INFECTION.get()
                && !HunterClothingItem.fullSet(player, HunterClothingItem.Upgrade.SILVERED)
                && canInfect(player) && player.getRandom().nextInt(4) == 1) {
            Lycanthropy.infect(player);
        }
    }

    private static boolean canInfect(ServerPlayer victim) {
        return Lycanthropy.rank(victim) == 0 && (WitcheryServerConfig.ALLOW_VAMPIRE_WEREWOLF_HYBRIDS.get()
                || !Vampirism.isVampire(victim));
    }

    private static void rendArmor(LivingEntity victim) {
        EquipmentSlot slot = ARMOR[victim.getRandom().nextInt(ARMOR.length)];
        ItemStack armor = victim.getItemBySlot(slot);
        if (armor.isEmpty()) return;
        int originalDamage = armor.getDamageValue();
        if (armor.isDamageableItem()) {
            armor.hurtAndBreak((armor.getMaxDamage() + 3) / 4, victim, wearer -> wearer.broadcastBreakEvent(slot));
        }
        if (!armor.isEmpty() && armor.getDamageValue() == originalDamage && victim instanceof Player player) {
            var dropped = player.drop(armor.copy(), true, false);
            if (dropped != null) {
                dropped.setPickUpDelay(100);
                player.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }
}
