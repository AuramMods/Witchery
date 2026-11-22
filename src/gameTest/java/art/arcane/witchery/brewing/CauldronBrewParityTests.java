package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CauldronBrewParityTests {
    @GameTest(template = "qa_coven_arena", timeoutTicks = 140)
    public static void dispersalIngredientsRemoveEarlierWheat(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 4, 4));
        level.setBlockAndUpdate(pos.below(2), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(pos.below(), Blocks.FIRE.defaultBlockState());
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var cauldron = (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
        var tank = cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        tank.fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE);

        helper.runAfterDelay(105, () -> {
            drop(helper, pos, Items.NETHER_WART);
            drop(helper, pos, WitcheryIngredient.WOLFSBANE.item());
            var dispersal = List.of(Items.GUNPOWDER, WitcheryIngredient.GLOBE_ARTICHOKE.item(),
                    WitcheryIngredient.BAT_WOOL.item(), WitcheryIngredient.WORMWOOD.item(), Items.ZOMBIE_HEAD);
            var deliveries = List.of(BrewMixture.Delivery.SPLASH, BrewMixture.Delivery.SPLASH,
                    BrewMixture.Delivery.GAS, BrewMixture.Delivery.LIQUID, BrewMixture.Delivery.TRIGGERED);
            for (int index = 0; index < dispersal.size(); index++) {
                drop(helper, pos, Items.WHEAT);
                var withWheat = BrewMixture.from(tank.getFluidInTank(0).getTag());
                helper.assertTrue(withWheat != null && withWheat.effects().stream()
                                .anyMatch(effect -> effect.effect() == WitcheryEffects.FEEL_NO_PAIN.get()),
                        "Wheat added after the previous dispersal still gives Moonshine");

                drop(helper, pos, dispersal.get(index));
                cauldron.load(cauldron.saveWithFullMetadata());
                var converted = BrewMixture.from(tank.getFluidInTank(0).getTag());
                helper.assertTrue(converted != null && converted.delivery() == deliveries.get(index)
                                && converted.used() == 1 && converted.effects().size() == 1
                                && converted.effects().get(0).effect() == WitcheryEffects.WOLFSBANE.get(),
                        dispersal.get(index) + " removes the earlier wheat while keeping Wolfsbane after reload");
            }
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void diseaseBrewHonorsLegacyCreatureImmunity(GameTestHelper helper) {
        var pig = helper.spawn(EntityType.PIG, new BlockPos(3, 3, 3));
        var zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 3, 5));
        var golem = helper.spawn(EntityType.IRON_GOLEM, new BlockPos(5, 3, 3));
        var demon = helper.spawn(WitcheryEntities.DEMON.get(), new BlockPos(5, 3, 5));
        var werevillager = helper.spawn(WitcheryEntities.WERE_VILLAGER.get(), new BlockPos(7, 3, 3));
        var reflection = helper.spawn(WitcheryEntities.REFLECTION.get(), new BlockPos(7, 3, 5));
        var recipe = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.DIAMOND), new ItemStack(Items.ROTTEN_FLESH));

        drink(pig, recipe);
        var disease = WitcheryEffects.DISEASED.get();
        helper.assertTrue(pig.hasEffect(disease), "The ordinary brew still infects a susceptible creature");
        for (var immune : List.of(zombie, golem, demon, werevillager, reflection)) {
            drink(immune, recipe);
            helper.assertTrue(!immune.hasEffect(disease), "The ordinary brew respects immunity for " + immune.getType());
        }

        drink(golem, List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.FERMENTED_SPIDER_EYE), new ItemStack(Items.ROTTEN_FLESH)));
        helper.assertTrue(golem.hasEffect(disease), "Inverting the recipe bypasses the brew's creature immunity check");
        helper.succeed();
    }

    private static void drink(LivingEntity target, List<ItemStack> recipe) {
        var bottle = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        bottle.setTag(BrewMixture.ingredientTag(recipe, 0x99afae));
        bottle.finishUsingItem(target.level(), target);
    }

    private static void drop(GameTestHelper helper, BlockPos pos, Item item) {
        var level = helper.getLevel();
        var center = Vec3.atCenterOf(pos);
        var dropped = new ItemEntity(level, center.x, center.y, center.z, new ItemStack(item));
        level.getBlockState(pos).entityInside(level, pos, dropped);
        helper.assertTrue(dropped.isRemoved(), "The boiling cauldron accepts " + item);
    }
}
