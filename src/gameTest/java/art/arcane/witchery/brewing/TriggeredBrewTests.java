package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.CursedControlBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class TriggeredBrewTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_triggered_brew_caster")
    public static void itemCurseAndUnattendedControlUseTheWorldFakePlayer(GameTestHelper helper) {
        var level = helper.getLevel();
        var brew = brewTag();
        var causes = new ArrayList<Entity>();
        var holder = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "CurseHolder")) {
            @Override
            public boolean hurt(DamageSource source, float amount) {
                causes.add(source.getEntity());
                return false;
            }
        };
        holder.setGameMode(GameType.SURVIVAL);
        holder.setPos(helper.absoluteVec(new Vec3(3, 2, 3)));
        var stack = new ItemStack(Items.STICK);
        var curse = new CompoundTag();
        curse.put("Curse", brew);
        curse.putInt("Count", 1);
        stack.getOrCreateTag().put("WITCCurse", curse);
        holder.setItemInHand(InteractionHand.MAIN_HAND, stack);

        var controlPos = helper.absolutePos(new BlockPos(5, 2, 3));
        var controlBlock = WitcheryBlocks.getRequired("cbuttonstone").get();
        level.setBlockAndUpdate(controlPos, controlBlock.defaultBlockState());
        var control = (CursedControlBlockEntity) level.getBlockEntity(controlPos);
        control.charge(brew, BrewMixture.from(brew), 0x113344, null);
        var victim = EntityType.PIG.create(level);
        victim.setPos(helper.absoluteVec(new Vec3(5, 2, 4)));
        Consumer<LivingAttackEvent> listener = event -> {
            if (event.getEntity() == victim) {
                causes.add(event.getSource().getEntity());
            }
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(holder, InteractionHand.MAIN_HAND));
            helper.assertTrue(control.spend(level, victim), "The control spends its last valid charge");
            var expected = FakePlayerFactory.getMinecraft(level);
            helper.assertTrue(causes.size() == 2 && causes.stream().allMatch(caster -> caster == expected),
                    "Both curse effects credit the world's fake caster");
            helper.assertTrue(!stack.getOrCreateTag().contains("WITCCurse"), "The item spends its last charge");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            holder.discard();
            victim.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_triggered_brew_offering")
    public static void itemCurseCanReachTheOriginalUpperRim(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 2, 3)).atY(196);
        var item = new ItemEntity(level, pos.getX() - 0.05, pos.getY() + 1.2, pos.getZ() + 0.5,
                new ItemStack(Items.STICK, 2));
        item.setNoGravity(true);
        level.addFreshEntity(item);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(item.getUUID()) == item,
                "The upper-rim offering is loaded")).thenExecute(() -> {
            try {
                var brew = brewTag();
                helper.assertTrue(BrewItemCurse.attach(level, pos, brew), "The original upper rim admits the offering");
                helper.assertTrue(item.getItem().getCount() == 1 && !item.getItem().hasTag(),
                        "Only the separated item is cursed");
                helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, item.getBoundingBox().inflate(1),
                                drop -> drop != item && drop.getItem().hasTag()
                                        && BrewItemCurse.savedBrew(drop.getItem().getTag()).equals(brew)
                                        && drop.getItem().getCount() == 1).size() == 1,
                        "The separated item receives the ordered curse");
            } finally {
                level.getEntitiesOfClass(ItemEntity.class, item.getBoundingBox().inflate(1)).forEach(Entity::discard);
            }
        }).thenSucceed();
    }

    private static CompoundTag brewTag() {
        return BrewMixture.ingredientTag(List.of(new ItemStack(Items.NETHER_WART),
                new ItemStack(Items.PUMPKIN), new ItemStack(Items.ZOMBIE_HEAD)), 0x113344);
    }
}
