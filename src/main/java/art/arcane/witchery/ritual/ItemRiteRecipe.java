package art.arcane.witchery.ritual;

import art.arcane.witchery.block.RitualGlyphBlock;
import art.arcane.witchery.block.entity.GrassperBlockEntity;
import art.arcane.witchery.registry.WitcheryRecipes;
import art.arcane.witchery.world.WitcheryDimensions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.RecipeMatcher;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record ItemRiteRecipe(ResourceLocation id, List<Ingredient> sacrifices, List<Ring> rings, int power,
                             ItemStack result, Action action, boolean nightOnly, @Nullable EntityType<?> livingOffering,
                             boolean consumeNecromanticStone)
        implements Recipe<Container> {
    public enum Action {
        ITEM,
        MANIFESTATION,
        SUMMON_DEMON,
        CURSE_WAKING_NIGHTMARE,
        CURE_WAKING_NIGHTMARE,
        BIND_FAMILIAR,
        CALL_FAMILIAR,
        SUMMON_IMP,
        INFUSE_OVERWORLD,
        RECHARGE_INFUSION,
        INFUSE_INFERNAL,
        INFUSE_LIGHT,
        INFUSE_OTHERWHERE,
        CORRUPT_VOODOO_PROTECTION,
        BIND_WAYSTONE,
        BIND_CREATURE_WAYSTONE,
        TRAVEL_WAYSTONE,
        CURE_VAMPIRISM,
        CURSE_OF_THE_WOLF,
        CURE_LYCANTHROPY,
        TRANSPOSE_CREATURE,
        TRANSPOSE_ORES
    }

    public record Ring(RitualCircle pattern, Block glyph) {
    }

    public record Offering(ItemStack stack, Vec3 position, @Nullable BlockPos grassper, Runnable consume) {
    }

    public boolean hasCircles(ServerLevel level, BlockPos pos) {
        return rings.stream().allMatch(ring -> ring.pattern.matches(level, pos, ring.glyph));
    }

    @Nullable
    public String startFailure(ServerLevel level) {
        if ((action == Action.TRANSPOSE_CREATURE || action == Action.TRANSPOSE_ORES)
                && level.dimension().equals(WitcheryDimensions.DREAM)) return "message.witchery.rite.transpose_dream";
        if (nightOnly && !level.isNight()) return "message.witchery.rite.night_only";
        if (action == Action.CURSE_WAKING_NIGHTMARE && !level.isThundering()) return "message.witchery.rite.storm_only";
        return null;
    }

    @Nullable
    public Mob findLivingOffering(ServerLevel level, BlockPos pos) {
        if (livingOffering == null) return null;
        int radius = rings.stream().mapToInt(ring -> ring.pattern.sacrificeRadius()).max().orElse(4) + 1;
        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        return level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(radius, 1, radius),
                mob -> mob.isAlive() && mob.getType() == livingOffering && mob.distanceToSqr(x, pos.getY(), z) <= radius * radius)
                .stream().findFirst().orElse(null);
    }

    @Nullable
    public List<Offering> findSacrifices(ServerLevel level, BlockPos pos, int first) {
        int radius = rings.stream().mapToInt(ring -> ring.pattern.sacrificeRadius()).max().orElse(4);
        AABB area = new AABB(pos).inflate(radius, 0, radius);
        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        List<Offering> items = new ArrayList<>();
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, area, item -> item.isAlive()
                && (item.getX() - x) * (item.getX() - x) + (item.getZ() - z) * (item.getZ() - z) <= radius * radius)) {
            ItemStack stack = entity.getItem();
            items.add(new Offering(stack, entity.position(), null, () -> {
                if (stack.getCount() == 1) entity.discard();
                else entity.setItem(stack.copyWithCount(stack.getCount() - 1));
            }));
        }
        for (BlockPos point : BlockPos.betweenClosed(pos.offset(-5, 0, -5), pos.offset(5, 0, 5))) {
            if (level.getBlockEntity(point) instanceof GrassperBlockEntity grassper && !grassper.isEmpty()) {
                items.add(new Offering(grassper.getItem(0), Vec3.atBottomCenterOf(point).add(0, 0.8, 0),
                        grassper.getBlockPos(), () -> grassper.removeItem(0, 1)));
            }
        }
        List<Ingredient> required = sacrifices.subList(first, sacrifices.size());
        int[] owners = new int[items.size()];
        int[] assigned = new int[required.size()];
        Arrays.fill(owners, -1);
        for (int ingredient = 0; ingredient < required.size(); ingredient++) {
            if (!assign(ingredient, required, items, owners, assigned, new boolean[items.size()])) {
                return null;
            }
        }
        return Arrays.stream(assigned).mapToObj(items::get).toList();
    }

    private static boolean assign(int ingredient, List<Ingredient> required, List<Offering> items,
                                  int[] owners, int[] assigned, boolean[] visited) {
        for (int item = 0; item < items.size(); item++) {
            if (visited[item] || !required.get(ingredient).test(items.get(item).stack())) {
                continue;
            }
            visited[item] = true;
            if (owners[item] < 0 || assign(owners[item], required, items, owners, assigned, visited)) {
                owners[item] = ingredient;
                assigned[ingredient] = item;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(Container container, Level level) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (!container.getItem(slot).isEmpty()) {
                stacks.add(container.getItem(slot));
            }
        }
        return RecipeMatcher.findMatches(stacks, sacrifices) != null;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= sacrifices.size();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.ITEM_RITE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return WitcheryRecipes.ITEM_RITE.get();
    }

    public static final class Serializer implements RecipeSerializer<ItemRiteRecipe> {
        private static final int MAX_SACRIFICES = 24;
        private static final Map<String, RitualCircle> PATTERNS = Map.of(
                "small", RitualCircle.SMALL_RITE, "medium", RitualCircle.MEDIUM_RITE, "large", RitualCircle.LARGE_RITE);

        @Override
        public ItemRiteRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray sacrifices = GsonHelper.getAsJsonArray(json, "sacrifices");
            JsonArray ringData = GsonHelper.getAsJsonArray(json, "circles");
            int power = GsonHelper.getAsInt(json, "power", 0);
            if (sacrifices.size() > MAX_SACRIFICES || ringData.size() > PATTERNS.size() || power < 0) {
                throw new JsonSyntaxException("Invalid sacrifice, circle or power requirement for " + id);
            }

            List<Ingredient> ingredients = new ArrayList<>();
            sacrifices.forEach(entry -> ingredients.add(Ingredient.fromJson(entry, false)));
            List<Ring> rings = new ArrayList<>();
            ringData.forEach(entry -> {
                JsonObject ring = GsonHelper.convertToJsonObject(entry, "circle");
                RitualCircle pattern = PATTERNS.get(GsonHelper.getAsString(ring, "size"));
                ResourceLocation glyphId = ResourceLocation.tryParse(GsonHelper.getAsString(ring, "glyph"));
                Block glyph = glyphId == null ? null : BuiltInRegistries.BLOCK.getOptional(glyphId).orElse(null);
                if (pattern == null || !(glyph instanceof RitualGlyphBlock)
                        || rings.stream().anyMatch(existing -> existing.pattern == pattern)) {
                    throw new JsonSyntaxException("Invalid circle in " + id);
                }
                rings.add(new Ring(pattern, glyph));
            });
            Action action = switch (GsonHelper.getAsString(json, "action", "item")) {
                case "item" -> Action.ITEM;
                case "manifestation" -> Action.MANIFESTATION;
                case "summon_demon" -> Action.SUMMON_DEMON;
                case "summon_imp" -> Action.SUMMON_IMP;
                case "infuse_overworld" -> Action.INFUSE_OVERWORLD;
                case "recharge_infusion" -> Action.RECHARGE_INFUSION;
                case "infuse_infernal" -> Action.INFUSE_INFERNAL;
                case "infuse_light" -> Action.INFUSE_LIGHT;
                case "infuse_otherwhere" -> Action.INFUSE_OTHERWHERE;
                case "corrupt_voodoo_protection" -> Action.CORRUPT_VOODOO_PROTECTION;
                case "curse_waking_nightmare" -> Action.CURSE_WAKING_NIGHTMARE;
                case "cure_waking_nightmare" -> Action.CURE_WAKING_NIGHTMARE;
                case "bind_familiar" -> Action.BIND_FAMILIAR;
                case "call_familiar" -> Action.CALL_FAMILIAR;
                case "bind_waystone" -> Action.BIND_WAYSTONE;
                case "bind_creature_waystone" -> Action.BIND_CREATURE_WAYSTONE;
                case "travel_waystone" -> Action.TRAVEL_WAYSTONE;
                case "cure_vampirism" -> Action.CURE_VAMPIRISM;
                case "curse_of_the_wolf" -> Action.CURSE_OF_THE_WOLF;
                case "cure_lycanthropy" -> Action.CURE_LYCANTHROPY;
                case "transpose_creature" -> Action.TRANSPOSE_CREATURE;
                case "transpose_ores" -> Action.TRANSPOSE_ORES;
                default -> throw new JsonSyntaxException("Unknown rite action for " + id);
            };
            ItemStack result = action == Action.ITEM
                    ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")) : ItemStack.EMPTY;
            EntityType<?> living = json.has("living_offering")
                    ? EntityType.byString(GsonHelper.getAsString(json, "living_offering")).orElseThrow(
                            () -> new JsonSyntaxException("Unknown living offering for " + id)) : null;
            return new ItemRiteRecipe(id, List.copyOf(ingredients), List.copyOf(rings), power, result,
                    action, GsonHelper.getAsBoolean(json, "night_only", false), living,
                    GsonHelper.getAsBoolean(json, "consume_necromantic_stone", false));
        }

        @Override
        public ItemRiteRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int ingredientCount = buffer.readVarInt();
            if (ingredientCount < 0 || ingredientCount > MAX_SACRIFICES) {
                throw new IllegalArgumentException("Invalid rite sacrifice count");
            }
            List<Ingredient> sacrifices = new ArrayList<>();
            for (int i = 0; i < ingredientCount; i++) {
                sacrifices.add(Ingredient.fromNetwork(buffer));
            }
            int ringCount = buffer.readVarInt();
            if (ringCount < 0 || ringCount > PATTERNS.size()) {
                throw new IllegalArgumentException("Invalid rite circle count");
            }
            List<Ring> rings = new ArrayList<>();
            for (int i = 0; i < ringCount; i++) {
                rings.add(new Ring(buffer.readEnum(RitualCircle.class), BuiltInRegistries.BLOCK.get(buffer.readResourceLocation())));
            }
            return new ItemRiteRecipe(id, List.copyOf(sacrifices), List.copyOf(rings), buffer.readVarInt(), buffer.readItem(),
                    buffer.readEnum(Action.class), buffer.readBoolean(),
                    buffer.readNullable(input -> BuiltInRegistries.ENTITY_TYPE.get(input.readResourceLocation())),
                    buffer.readBoolean());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ItemRiteRecipe recipe) {
            buffer.writeVarInt(recipe.sacrifices.size());
            recipe.sacrifices.forEach(ingredient -> ingredient.toNetwork(buffer));
            buffer.writeVarInt(recipe.rings.size());
            for (Ring ring : recipe.rings) {
                buffer.writeEnum(ring.pattern);
                buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(ring.glyph));
            }
            buffer.writeVarInt(recipe.power);
            buffer.writeItem(recipe.result);
            buffer.writeEnum(recipe.action);
            buffer.writeBoolean(recipe.nightOnly);
            buffer.writeNullable(recipe.livingOffering, (output, type) -> output.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(type)));
            buffer.writeBoolean(recipe.consumeNecromanticStone);
        }
    }
}
