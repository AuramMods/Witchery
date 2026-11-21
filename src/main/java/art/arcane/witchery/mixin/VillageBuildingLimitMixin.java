package art.arcane.witchery.mixin;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.world.WitcheryVillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
abstract class VillageBuildingLimitMixin {
    @Shadow
    @Final
    private List<? super PoolElementStructurePiece> pieces;

    @Shadow
    @Final
    private RandomSource random;

    @Unique
    private Boolean witchery$allowsWitchHut;
    @Unique
    private Boolean witchery$allowsKeep;
    @Unique
    private Boolean witchery$allowsWall;
    @Unique
    private final Map<StructurePoolElement, Integer> witchery$groupLimits = new IdentityHashMap<>();

    @ModifyArg(method = "tryPlacingChildren", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    private Collection<StructurePoolElement> witchery$limitBuildings(Collection<StructurePoolElement> candidates) {
        if (candidates.stream().noneMatch(candidate -> WitcheryVillagePools.clusterLimit(candidate) != null
                || WitcheryVillagePools.limitedBuilding(candidate).isPresent())) return candidates;
        Set<ResourceLocation> used = new HashSet<>();
        Map<StructurePoolElement, Integer> groupCounts = new IdentityHashMap<>();
        for (Object piece : pieces) {
            if (!(piece instanceof PoolElementStructurePiece placed)) continue;
            var element = placed.getElement();
            var id = WitcheryVillagePools.limitedBuilding(element);
            if (WitcheryVillagePools.clusterLimit(element) != null || id.filter(WitcheryVillagePools.WATCHTOWER::equals).isPresent()) {
                groupCounts.merge(element, 1, Integer::sum);
            } else {
                id.ifPresent(used::add);
            }
        }
        Map<StructurePoolElement, Boolean> eligible = new IdentityHashMap<>();
        return candidates.stream().filter(candidate -> eligible.computeIfAbsent(candidate, element -> {
            var quota = WitcheryVillagePools.clusterLimit(element);
            if (quota != null) return witchery$allowsGroup(element, groupCounts, quota.min(), quota.max());
            return WitcheryVillagePools.limitedBuilding(element).map(id -> witchery$canPlace(id, element, used, groupCounts)).orElse(true);
        })).toList();
    }

    @Unique
    private boolean witchery$canPlace(ResourceLocation id, StructurePoolElement element,
            Set<ResourceLocation> used, Map<StructurePoolElement, Integer> groupCounts) {
        if (used.contains(id)) return false;
        if (id.equals(WitcheryVillagePools.WITCH_HUT)) {
            if (witchery$allowsWitchHut == null) witchery$allowsWitchHut = random.nextBoolean();
            return witchery$allowsWitchHut;
        }
        if (id.equals(WitcheryVillagePools.KEEP)) {
            if (witchery$allowsKeep == null) {
                int mode = WitcheryServerConfig.TOWN_KEEP_MODE.get();
                witchery$allowsKeep = mode == 2 || mode == 1 && random.nextBoolean();
            }
            return witchery$allowsKeep;
        }
        if (id.equals(WitcheryVillagePools.WALL_ANCHOR)) {
            if (witchery$allowsWall == null) {
                int mode = WitcheryServerConfig.TOWN_WALL_MODE.get();
                witchery$allowsWall = mode == 2 || mode == 1 && random.nextBoolean();
            }
            return witchery$allowsWall;
        }
        if (id.equals(WitcheryVillagePools.WATCHTOWER)) {
            return witchery$allowsGroup(element, groupCounts, WitcheryServerConfig.TOWN_GUARD_TOWER_CLUSTER_MIN.get(),
                    WitcheryServerConfig.TOWN_GUARD_TOWER_CLUSTER_MAX.get());
        }
        return true;
    }

    @Unique
    private boolean witchery$allowsGroup(StructurePoolElement element, Map<StructurePoolElement, Integer> groupCounts,
            int min, int max) {
        // Stop offering the individual entry once its seeded quota is used.
        int limit = witchery$groupLimits.computeIfAbsent(element, group -> max <= min ? min : min + random.nextInt(max - min + 1));
        return groupCounts.getOrDefault(element, 0) < limit;
    }
}
