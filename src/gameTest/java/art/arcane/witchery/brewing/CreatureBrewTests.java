package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CreatureBrewTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_heart_brew")
    public static void heartOfGoldPreservesOwnedPetsAndFamiliars(GameTestHelper helper) {
        var level = helper.getLevel();
        var actor = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrewOwner"));
        actor.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 3, 5))));
        var wild = helper.spawn(EntityType.WOLF, new BlockPos(3, 3, 5));
        var foreign = helper.spawn(EntityType.WOLF, new BlockPos(4, 3, 5));
        var familiar = helper.spawn(WitcheryEntities.TOAD.get(), new BlockPos(7, 3, 5));
        var other = UUID.randomUUID();
        for (var pet : List.of(foreign, familiar)) {
            pet.setOwnerUUID(other);
            pet.setTame(true);
        }
        familiar.setFamiliar(true);
        var pets = List.of(wild, foreign, familiar);
        pets.forEach(pet -> pet.setNoAi(true));

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(pets.stream().allMatch(pet -> level.getEntity(pet.getUUID()) == pet),
                        "All three pets must be visible to the brew"))
                .thenExecute(() -> {
                    try {
                        var capacity = Items.NETHER_STAR.getDefaultInstance();
                        var heart = WitcheryIngredient.HEART_OF_GOLD.item().getDefaultInstance();
                        var ingredients = List.of(capacity, heart);
                        var attraction = BrewMixture.from(ingredients);
                        helper.assertTrue(attraction.used() == 4 && attraction.power() == 650,
                                "Heart of Gold costs four levels and 500 power on top of its capacity ingredient");
                        BrewMixture.from(attraction.bottleTag(ingredients)).apply(actor, actor, actor, 1, 1);
                        helper.assertTrue(actor.getUUID().equals(wild.getOwnerUUID()) && other.equals(foreign.getOwnerUUID()),
                                "Attraction tames a wild wolf and preserves an existing owner");
                        var repulsion = BrewMixture.from(List.of(capacity, Items.FERMENTED_SPIDER_EYE.getDefaultInstance(), heart));
                        repulsion.apply(actor, actor, actor, 1, 1);
                        helper.assertTrue(!foreign.isTame() && foreign.getOwnerUUID() == null && actor.getUUID().equals(wild.getOwnerUUID())
                                        && familiar.isFamiliar() && other.equals(familiar.getOwnerUUID()),
                                "Repulsion releases foreign pets and preserves the actor's pet and the bound familiar");
                    } finally {
                        pets.forEach(pet -> pet.discard());
                    }
                }).thenSucceed();
    }
}
