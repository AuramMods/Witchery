package art.arcane.witchery.mixin;

import art.arcane.witchery.world.WitcheryVillagePools;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SinglePoolElement.class)
abstract class VillageClusterElementMixin implements WitcheryVillagePools.ClusteredElement {
    @Unique
    private WitcheryVillagePools.ClusterLimit witchery$clusterLimit;

    @Override
    public WitcheryVillagePools.ClusterLimit witchery$getClusterLimit() {
        return witchery$clusterLimit;
    }

    @Override
    public void witchery$setClusterLimit(WitcheryVillagePools.ClusterLimit limit) {
        witchery$clusterLimit = limit;
    }
}
