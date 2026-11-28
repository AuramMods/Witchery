package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class HuntsmanSpearTests {
    @GameTest(template = "qa_coven_arena")
    public static void spearUsesItsOriginalAnvilMaterialAndEnchantability(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockSurvivalPlayer();
        player.experienceLevel = 10;
        var pos = helper.absolutePos(new BlockPos(4, 3, 4));
        level.setBlockAndUpdate(pos, Blocks.ANVIL.defaultBlockState());
        var anvil = new AnvilMenu(1, player.getInventory(), ContainerLevelAccess.create(level, pos));
        var spear = new ItemStack(WitcheryItems.getRequired("huntsmanspear").get());
        spear.setDamageValue(600);
        helper.assertTrue(spear.getMaxDamage() == 1561 && spear.getItem().getEnchantmentValue() == 15,
                "The spear has the original durability and enchantability");
        anvil.getSlot(0).set(spear);
        anvil.getSlot(1).set(Items.DIAMOND.getDefaultInstance());
        helper.assertTrue(anvil.getSlot(2).getItem().isEmpty(), "A diamond does not repair the spear");
        anvil.getSlot(1).set(Items.OAK_PLANKS.getDefaultInstance());
        var repaired = anvil.getSlot(2).getItem();
        helper.assertTrue(repaired.is(spear.getItem()) && repaired.getDamageValue() == 210 && anvil.getCost() == 1,
                "A plank restores a quarter of the original durability through the anvil");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void spearRetaliatesWhileGuarding(GameTestHelper helper) {
        var level = helper.getLevel();
        var spear = WitcheryItems.getRequired("huntsmanspear").get();
        var holder = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "HuntsmanSpearGuard"));
        var standing = helper.absolutePos(new BlockPos(5, 2, 5));
        holder.setPos(standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5);
        holder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(spear));
        var attacker = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(5, 2, 7));
        var attack = level.damageSources().mobAttack(attacker);
        var vicinity = new AABB(standing).inflate(4);

        holder.setSprinting(true);
        holder.getRandom().setSeed(4096);
        var sprinting = new LivingHurtEvent(holder, attack, 10);
        MinecraftForge.EVENT_BUS.post(sprinting);
        helper.assertTrue(sprinting.getAmount() == 10 && level.getEntitiesOfClass(Wolf.class, vicinity).isEmpty(),
                "Sprinting without using the spear leaves the hit and wolf count unchanged");
        holder.setSprinting(false);

        helper.assertTrue(spear.use(level, holder, InteractionHand.MAIN_HAND).getResult().consumesAction()
                        && holder.isUsingItem() && holder.getUseItem().getUseAnimation() == UseAnim.BLOCK,
                "The ordinary use interaction raises the spear to guard");
        holder.getRandom().setSeed(4096);
        var guarded = new LivingHurtEvent(holder, attack, 10);
        MinecraftForge.EVENT_BUS.post(guarded);
        var wolves = level.getEntitiesOfClass(Wolf.class, vicinity);
        helper.assertTrue(guarded.getAmount() == 5.5F && wolves.size() == 1
                        && wolves.get(0).getTarget() == attacker && wolves.get(0).hasEffect(MobEffects.WITHER),
                "A guarded hit applies original sword mitigation and can call a retaliating wolf");

        var bypassesGuard = new LivingHurtEvent(holder, level.damageSources().fellOutOfWorld(), 10);
        MinecraftForge.EVENT_BUS.post(bypassesGuard);
        helper.assertTrue(bypassesGuard.getAmount() == 10, "Armor-bypassing damage still reaches the guarded player");
        holder.stopUsingItem();
        var released = new LivingHurtEvent(holder, attack, 10);
        MinecraftForge.EVENT_BUS.post(released);
        helper.assertTrue(released.getAmount() == 10 && level.getEntitiesOfClass(Wolf.class, vicinity).size() == 1,
                "Releasing use ends the guard and the retaliation chance");
        helper.succeed();
    }
}
