package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanStrategy;
import imd.ufrn.br.kmeans.KmeanCommon;

import java.util.List;
import java.util.stream.Collectors;

public class KmeansParallelStreamMap implements KmeanStrategy {
    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        List<Cluster> clusters = initialCenters.stream().map(Cluster::build_with_center).toList();

        while (true) {
            List<Cluster> finalClusters = clusters;

            List<Cluster> newClusters = values
                    .parallelStream()
                    .map(point -> {
                        var index = KmeanCommon.getIndexClosestCluster(point, finalClusters);
                        var center = finalClusters.get(index).getCenter();
                        return new Point[]{center, point};
                    })
                    .collect(Collectors.groupingBy(el -> el[0], Collectors.toList()))
                    .entrySet()
                    .parallelStream()
                    .map(entry -> {
                        var center = entry.getKey();
                        var points = entry.getValue().parallelStream().map(p -> p[1]).toList();
                        return new Cluster(center, points);
                    }).toList();

            var newCenters = newClusters.parallelStream().map(Cluster::calculateCenterPoint).toList();
            var oldCenters = finalClusters.parallelStream().map(Cluster::getCenter).toList();

            if (KmeanCommon.converged(newCenters, oldCenters)) {
                return newClusters;
            }

            clusters = newCenters.parallelStream().map(Cluster::build_with_center).toList();
        }
    }

}
