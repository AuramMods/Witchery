package art.arcane.witchery.item;

import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public final class BoneNeedleItem extends Item {
    private static final int BLOOD_COST = 125;

    public BoneNeedleItem() {
        super(new Properties());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var pos = context.getClickedPos();
        if (!(context.getPlayer() instanceof ServerPlayer vampire) || Vampirism.level(vampire) == 0
                || !context.getLevel().getBlockState(pos).is(Blocks.WHITE_WOOL)) return InteractionResult.PASS;
        boolean enoughBlood = vampire.getAbilities().instabuild || WitcheryPlayerDataProvider.get(vampire)
                .map(data -> data.getBloodPower() >= BLOOD_COST).orElse(false);
        if (Vampirism.level(vampire) < 4 || !enoughBlood) return useOn(context);

        var world = vampire.serverLevel();
        var original = BlockSnapshot.create(world.dimension(), world, pos);
        if (!world.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("bloodedwool").get().defaultBlockState())) {
            return InteractionResult.FAIL;
        }
        if (ForgeEventFactory.onBlockPlace(vampire, original, context.getClickedFace())) {
            original.restore(true, true);
            return InteractionResult.FAIL;
        }
        Vampirism.drawBlood(vampire, BLOOD_COST);
        world.sendParticles(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, 16, 0.5, 0.5, 0.5, 0);
        world.playSound(null, pos, WitcherySounds.BLOOD_DRINK.get(), SoundSource.BLOCKS, 1, 1);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || Vampirism.level(player) == 0) return InteractionResult.PASS;
        if (player instanceof ServerPlayer vampire)
            vampire.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 0.65F, 1);
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
