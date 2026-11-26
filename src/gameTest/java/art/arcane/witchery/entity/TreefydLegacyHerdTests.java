package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class TreefydLegacyHerdTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_treefyd_legacy_herds")
    public static void localizedLegacyHobgoblinAllowlistProtectsTheHerd(GameTestHelper helper) {
        var level = helper.getLevel();
        var plant = WitcheryEntities.TREEFYD.get().create(level);
        var hobgoblin = WitcheryEntities.GOBLIN.get().create(level);
        var otherHerd = EntityType.PIG.create(level);
        try {
            for (String name : List.of("Duende", "Хобгоблин", "妖精", "Hobgoblin")) {
                CompoundTag legacy = herd(name, "");
                plant.readAdditionalSaveData(legacy);
                helper.assertTrue(!plant.mayHunt(hobgoblin) && plant.mayHunt(otherHerd),
                        "The saved label " + name + " still protects its original herd");
                var saved = plant.saveWithoutId(new CompoundTag());
                var restored = saved.getList("KnownCreatureTypes", Tag.TAG_COMPOUND).getCompound(0);
                helper.assertTrue(restored.getString("CreatureType").equals("witchery:goblin")
                                && restored.getString("CreatureTypeName").equals(name),
                        "The save keeps the original label and records the stable type");
                plant.readAdditionalSaveData(saved);
                helper.assertTrue(!plant.mayHunt(hobgoblin), "The canonical entry still protects the herd after reopen");
            }
            plant.readAdditionalSaveData(herd("Duende", "minecraft:pig"));
            helper.assertTrue(plant.mayHunt(hobgoblin) && !plant.mayHunt(otherHerd),
                    "An explicit current type takes precedence over the saved display label");
            plant.readAdditionalSaveData(herd("Custom companion", ""));
            hobgoblin.setCustomName(Component.literal("Custom companion"));
            helper.assertTrue(!plant.mayHunt(hobgoblin), "Unrecognized names retain the original name-based rules");
        } finally {
            plant.discard();
            hobgoblin.discard();
            otherHerd.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_treefyd_legacy_taglocks")
    public static void localizedLegacyTaglockTeachesAndForgetsTheHobgoblinHerd(GameTestHelper helper) {
        var level = helper.getLevel();
        var plant = WitcheryEntities.TREEFYD.get().create(level);
        var hobgoblin = WitcheryEntities.GOBLIN.get().create(level);
        var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LegacyGardener"));
        var before = new CompoundTag();
        before.putString("Owner", owner.getGameProfile().getName());
        plant.readAdditionalSaveData(before);
        var signature = UUID.randomUUID();
        ItemStack kit = new ItemStack(WitcheryItems.getRequired("taglockkit").get());
        var tag = kit.getOrCreateTag();
        tag.putLong("WITCEntityIDm1", signature.getMostSignificantBits());
        tag.putLong("WITCEntityIDl1", signature.getLeastSignificantBits());
        tag.putString("WITCDisplayName1", "Duende");
        try {
            owner.setItemInHand(InteractionHand.MAIN_HAND, kit.copy());
            plant.mobInteract(owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(!plant.mayHunt(hobgoblin) && owner.getMainHandItem().isEmpty(),
                    "Normal use spends the old Taglock and allows the whole herd");
            owner.setShiftKeyDown(true);
            owner.setItemInHand(InteractionHand.MAIN_HAND, kit.copy());
            plant.mobInteract(owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(plant.mayHunt(hobgoblin) && owner.getMainHandItem().isEmpty(),
                    "Sneak-use forgets the same herd through the old Taglock");
        } finally {
            plant.discard();
            hobgoblin.discard();
            owner.discard();
        }
        helper.succeed();
    }

    private static CompoundTag herd(String label, String id) {
        var creature = new CompoundTag();
        creature.putString("CreatureTypeName", label);
        if (!id.isEmpty()) creature.putString("CreatureType", id);
        var names = new ListTag();
        names.add(creature);
        var data = new CompoundTag();
        data.put("KnownCreatureTypes", names);
        return data;
    }
}
