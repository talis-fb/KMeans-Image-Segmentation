package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanStrategy;
import imd.ufrn.br.kmeans.KmeanCommon;

import java.util.List;

public class KmeansParallelStream implements KmeanStrategy {
    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        List<Cluster> clusters = initialCenters.stream().map(Cluster::build_with_center).toList();

        while (true) {
            List<Cluster> finalClusters = clusters;
            values.parallelStream().forEach(point -> {
                var index = KmeanCommon.getIndexClosestCluster(point, finalClusters);
                finalClusters.get(index).addPointSync(point);
            });

            var newCenters = clusters.parallelStream().map(Cluster::calculateCenterPoint).toList();
            var oldCenters = clusters.parallelStream().map(Cluster::getCenter).toList();

            if (KmeanCommon.converged(newCenters, oldCenters)) {
                return clusters;
            }

            clusters = newCenters.parallelStream().map(Cluster::build_with_center).toList();
        }
    }

}
