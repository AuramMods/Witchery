package art.arcane.witchery.entity;

import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.vampirism.VampireVillages;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.ForgeEventFactory;

public final class VillageGuardRecruitment {
    private VillageGuardRecruitment() {}

    public static boolean isOffer(Player player, Villager villager, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown() && !villager.isBaby()
                && player.getMainHandItem().is(Items.LEATHER_CHESTPLATE);
    }

    public static void offer(ServerPlayer player, Villager villager) {
        ServerLevel world = player.serverLevel();
        var village = VampireVillages.territory(world, villager.blockPosition()).orElse(null);
        if (village == null) return;
        if (villager.getPlayerReputation(player) < 10) {
            decline(player, "reputation");
            return;
        }

        var area = village.residents();
        int residents = world.getEntitiesOfClass(Villager.class, area, Villager::isAlive).size();
        if (residents < 9) {
            decline(player, "small_village");
            return;
        }
        if (world.getEntitiesOfClass(VillageGuard.class, area, VillageGuard::isAlive).size() >= residents / 4) {
            decline(player, "enough_guards");
            return;
        }

        if (villager.getRandom().nextInt(Math.max(1, villager.getOffers().size()) * 2 + 1) != 0
                || !appoint(world, villager, village)) {
            villager.playSound(SoundEvents.VILLAGER_NO, 1, 1);
            player.displayClientMessage(Component.translatable("message.witchery.village.guard_declined").withStyle(ChatFormatting.RED), true);
        } else {
            player.displayClientMessage(Component.translatable("message.witchery.village.guard_accepted").withStyle(ChatFormatting.GREEN), true);
        }
    }

    private static boolean appoint(ServerLevel world, Villager villager, VampireVillages.Territory village) {
        var type = WitcheryEntities.VILLAGE_GUARD.get();
        if (!ForgeEventFactory.canLivingConvert(villager, type, delay -> {})) return false;
        VillageGuard guard = type.create(world);
        if (guard == null) return false;
        guard.copyPosition(villager);
        guard.setCustomName(villager.getCustomName());
        guard.setCustomNameVisible(villager.isCustomNameVisible());
        guard.finalizeSpawn(world, world.getCurrentDifficultyAt(guard.blockPosition()), MobSpawnType.CONVERSION, null, null);
        guard.restrictTo(village.center(), village.radius() * 3 / 2);
        if (!world.addFreshEntity(guard)) return false;

        ForgeEventFactory.onLivingConvert(villager, guard);
        villager.releasePoi(MemoryModuleType.HOME);
        villager.releasePoi(MemoryModuleType.JOB_SITE);
        villager.releasePoi(MemoryModuleType.POTENTIAL_JOB_SITE);
        villager.releasePoi(MemoryModuleType.MEETING_POINT);
        villager.discard();
        world.sendParticles(ParticleTypes.INSTANT_EFFECT, guard.getX(), guard.getY(0.5), guard.getZ(), 16, 0.3, 0.5, 0.3, 0.03);
        guard.playSound(SoundEvents.VILLAGER_YES, 1, 1);
        return true;
    }

    private static void decline(ServerPlayer player, String reason) {
        player.displayClientMessage(Component.translatable("message.witchery.village." + reason).withStyle(ChatFormatting.AQUA), true);
        player.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 1, 1);
    }
}
