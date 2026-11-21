package art.arcane.witchery.mixin;

import art.arcane.witchery.world.WitcheryVillagePools;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @ModifyArg(method = "tryPlacingChildren", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    private Collection<StructurePoolElement> witchery$limitBuildings(Collection<StructurePoolElement> candidates) {
        if (candidates.stream().noneMatch(candidate -> WitcheryVillagePools.limitedBuilding(candidate).isPresent())) return candidates;
        var used = pieces.stream().filter(PoolElementStructurePiece.class::isInstance)
                .map(PoolElementStructurePiece.class::cast)
                .map(piece -> WitcheryVillagePools.limitedBuilding(piece.getElement()))
                .flatMap(Optional::stream).collect(Collectors.toSet());
        if (witchery$allowsWitchHut == null && candidates.stream().anyMatch(candidate ->
                WitcheryVillagePools.limitedBuilding(candidate).filter(WitcheryVillagePools.WITCH_HUT::equals).isPresent())) {
            // The old village generator gave this piece a limit of either zero or one per start.
            witchery$allowsWitchHut = random.nextBoolean();
        }
        if (Boolean.FALSE.equals(witchery$allowsWitchHut)) used.add(WitcheryVillagePools.WITCH_HUT);
        if (used.isEmpty()) return candidates;
        return candidates.stream().filter(candidate -> WitcheryVillagePools.limitedBuilding(candidate).filter(used::contains).isEmpty()).toList();
    }
}
