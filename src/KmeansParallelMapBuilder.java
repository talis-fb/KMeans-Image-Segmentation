import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KmeansParallelMapBuilder implements Kmean {
    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        List<Cluster> clusters = initialCenters.stream().map(Cluster::build_with_center).toList();

        while (true) {
            List<Cluster> finalClusters = clusters;

            clusters = values
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

            var newCenters = clusters.parallelStream().map(Cluster::calculateCenterPoint).toList();
            var oldCenters = clusters.parallelStream().map(Cluster::getCenter).toList();

            if (KmeanCommon.converged(newCenters, oldCenters)) {
                return clusters;
            }

            clusters = newCenters.parallelStream().map(Cluster::build_with_center).toList();
        }
    }

}
