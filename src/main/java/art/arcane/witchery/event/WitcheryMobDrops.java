package art.arcane.witchery.event;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.CovenWitch;
import art.arcane.witchery.entity.WitchOwl;
import art.arcane.witchery.entity.WitchPet;
import art.arcane.witchery.entity.WitchToad;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class WitcheryMobDrops {
    private static final TagKey<EntityType<?>> BATS = creatureTag("bats");
    private static final TagKey<EntityType<?>> CANINES = creatureTag("canines");
    private static final TagKey<EntityType<?>> WOLF_HEAD_DROPPERS = creatureTag("wolf_head_droppers");

    private WitcheryMobDrops() {
    }

    private static TagKey<EntityType<?>> creatureTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Witchery.MODID, name));
    }

    @SubscribeEvent
    public static void addDrops(LivingDropsEvent event) {
        LivingEntity creature = event.getEntity();
        if (creature.level().isClientSide) return;
        if (creature.getPersistentData().getBoolean("WITCNoDrops")) {
            event.setCanceled(true);
            return;
        }

        ListTag questDrops = creature.getPersistentData().getList(CovenWitch.EXTRA_DROPS, Tag.TAG_COMPOUND);
        for (int index = 0; index < questDrops.size(); index++) {
            ItemStack trophy = loadQuestDrop(questDrops.getCompound(index));
            if (!trophy.isEmpty()) addDrop(event, trophy);
        }
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer player)) return;
        boolean arthana = player.getMainHandItem().is(WitcheryItems.getRequired("arthana").get());
        int looting = event.getLootingLevel();
        ItemStack ingredient = ingredientDrop(creature, arthana, looting);
        if (!ingredient.isEmpty()) {
            addDrop(event, ingredient);
        }
        if (creature.getType().is(WOLF_HEAD_DROPPERS)
                && creature.getRandom().nextInt(12) <= Math.min(looting, 3)) {
            addDrop(event, new ItemStack(WitcheryBlocks.getRequired("wolfhead").get()));
        }
    }

    private static ItemStack loadQuestDrop(CompoundTag tag) {
        // The original quest used vanilla items stored inside ForgeData, outside the entity's item schema.
        ResourceLocation itemId = ResourceLocation.tryParse(tag.getString("id"));
        boolean namedLegacyStack = tag.contains("Damage", Tag.TAG_ANY_NUMERIC)
                && itemId != null && ResourceLocation.DEFAULT_NAMESPACE.equals(itemId.getNamespace());
        if (tag.contains("id", Tag.TAG_ANY_NUMERIC) || namedLegacyStack) {
            int version = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
            if (DataFixers.getDataFixer().update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, tag), -1, version)
                    .getValue() instanceof CompoundTag upgraded) {
                tag = upgraded;
            }
        }
        return WitcheryIngredient.loadInventoryStack(tag);
    }

    @SubscribeEvent
    public static void suppressSummonExperience(LivingExperienceDropEvent event) {
        if (event.getEntity().getPersistentData().getBoolean("WITCNoDrops")) event.setDroppedExperience(0);
    }

    private static ItemStack ingredientDrop(LivingEntity creature, boolean arthana, int looting) {
        RandomSource random = creature.getRandom();
        double multiplier = 1.0 + looting;
        if (creature.getType().is(BATS)) {
            return woolOrTongue(random, arthana, looting, WitcheryIngredient.BAT_WOOL);
        }
        if (creature.getType().is(CANINES)) {
            return woolOrTongue(random, arthana, looting, WitcheryIngredient.DOG_TONGUE);
        }
        if (creature instanceof WitchPet pet && !pet.isTemporary()) {
            WitcheryIngredient ingredient = pet instanceof WitchOwl ? WitcheryIngredient.OWLETS_WING
                    : pet instanceof WitchToad ? WitcheryIngredient.TOE_OF_FROG : null;
            if (ingredient != null && roll(random, arthana ? (looting > 0 ? 1 : 0.5) : 0.2)) {
                return new ItemStack(ingredient.item());
            }
        }
        if (creature.getType() == EntityType.CREEPER) {
            if (arthana && roll(random, 0.01 * multiplier)) {
                return new ItemStack(Items.CREEPER_HEAD);
            }
            return roll(random, (arthana ? 0.08 : 0.02) * multiplier)
                    ? new ItemStack(WitcheryIngredient.CREEPER_HEART.item()) : ItemStack.EMPTY;
        }
        if (creature instanceof Witch || creature instanceof CovenWitch) {
            return roll(random, arthana ? 0.5 : 0.33)
                    ? new ItemStack(WitcheryItems.getRequired("witchhand").get()) : ItemStack.EMPTY;
        }

        if (arthana && creature instanceof AbstractSkeleton) {
            if (creature.getType() == EntityType.SKELETON && roll(random, 0.05 * multiplier)) {
                return new ItemStack(Items.SKELETON_SKULL);
            }
            if (roll(random, 0.04 * multiplier)) {
                return new ItemStack(WitcheryIngredient.SPECTRAL_DUST.item());
            }
        } else if (arthana && creature instanceof Zombie) {
            if (roll(random, 0.02 * multiplier)) {
                return new ItemStack(Items.ZOMBIE_HEAD);
            }
            if (roll(random, 0.03 * multiplier)) {
                return new ItemStack(WitcheryIngredient.SPECTRAL_DUST.item());
            }
        } else if (arthana && creature instanceof ServerPlayer victim && roll(random, 0.1 * (1 + looting / 2))) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.getOrCreateTag().put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), victim.getGameProfile()));
            return head;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack woolOrTongue(RandomSource random, boolean arthana, int looting, WitcheryIngredient ingredient) {
        double chance = arthana ? (looting > 0 ? 1.0 : 0.75) : 0.33;
        return roll(random, chance) ? new ItemStack(ingredient.item()) : ItemStack.EMPTY;
    }

    private static boolean roll(RandomSource random, double chance) {
        return random.nextDouble() <= Math.min(chance, 1.0);
    }

    private static void addDrop(LivingDropsEvent event, ItemStack stack) {
        LivingEntity creature = event.getEntity();
        event.getDrops().add(new ItemEntity(creature.level(), creature.getX(), creature.getY() + 1, creature.getZ(), stack));
    }
}
