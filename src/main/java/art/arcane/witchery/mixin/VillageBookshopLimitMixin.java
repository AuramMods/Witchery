package art.arcane.witchery.mixin;

import art.arcane.witchery.world.WitcheryVillagePools;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Collection;
import java.util.List;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
abstract class VillageBookshopLimitMixin {
    @Shadow
    @Final
    private List<? super PoolElementStructurePiece> pieces;

    @ModifyArg(method = "tryPlacingChildren", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    private Collection<StructurePoolElement> witchery$limitBookshops(Collection<StructurePoolElement> candidates) {
        if (candidates.stream().noneMatch(WitcheryVillagePools::isBookshop)
                || pieces.stream().noneMatch(piece -> piece instanceof PoolElementStructurePiece poolPiece
                        && WitcheryVillagePools.isBookshop(poolPiece.getElement()))) return candidates;
        return candidates.stream().filter(candidate -> !WitcheryVillagePools.isBookshop(candidate)).toList();
    }
}
