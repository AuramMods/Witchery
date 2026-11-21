package art.arcane.witchery.block;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.entity.CreatureAlignment;
import art.arcane.witchery.entity.HobgoblinGod;
import art.arcane.witchery.entity.Leonard;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryFluids;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class DiseaseBlock extends LiquidBlock {
    public static final int TICK_INTERVAL = 15;

    public DiseaseBlock() {
        super(WitcheryFluids.getRequiredSource("fluiddisease"), Properties.copy(Blocks.WATER)
                .strength(100).noCollission().noLootTable());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moved) {
        super.onPlace(state, level, pos, previous, moved);
        if (level instanceof ServerLevel && !previous.is(this)) level.scheduleTick(pos, this, TICK_INTERVAL);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int chance = WitcheryServerConfig.DISEASE_REMOVAL_CHANCE.get();
        if (chance <= 0) return;
        if (random.nextInt(chance) == 0) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        else level.scheduleTick(pos, this, TICK_INTERVAL);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel) || !(entity instanceof LivingEntity target) || !target.isAlive()) return;
        var effect = WitcheryEffects.DISEASED.get();
        if (target.hasEffect(effect) || CreatureAlignment.undead(target) || CreatureAlignment.demonic(target)
                || Lycanthropy.isWerewolf(target) || CreatureAlignment.werewolf(target) || Vampirism.isBoss(target)
                || target instanceof AbstractGolem || target instanceof HobgoblinGod || target instanceof Leonard
                || level.random.nextInt(3) != 0) return;
        target.addEffect(new MobEffectInstance(effect, 1200 * (1 + level.random.nextInt(4))));
    }
}
