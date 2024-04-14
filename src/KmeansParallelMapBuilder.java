import java.awt.datatransfer.Clipboard;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KmeansParallelMapBuilder implements Kmean {
    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        List<Cluster> clusters = initialCenters.stream().map(Cluster::build_with_center).toList();

        final Map<Point, List<Point>> clustersMap = clusters
                .stream()
                .collect(Collectors.toMap(Cluster::getCenter, Cluster::getPoints, (a,b) -> a));

        int i =0 ;

        while (true) {

            System.err.println("iter " + i);

            List<Cluster> finalClusters = clusters;
            var saida = values.parallelStream().map(point -> {
                var index = KmeanCommon.getIndexClosestCluster(point, finalClusters);
                var center = finalClusters.get(index).getCenter();

                Map<Point, List<Point>> map = new HashMap<>();

                map.put(center, new ArrayList<>(List.of(point)));
                return map;
            }).reduce(clustersMap, (m1, m2) -> {
                m1.forEach((key, value) -> {
                    m2.merge(key, value, (v1, v2) -> Stream.concat(v1.stream(), v2.stream()).toList());
                });
                return m2;
            });

            clusters = saida.entrySet().stream().map(entry -> new Cluster(entry.getKey(), entry.getValue())).toList();


            var newCenters = clusters.stream().map(Cluster::calculateCenterPoint).toList();
            var oldCenters = clusters.stream().map(Cluster::getCenter).toList();

            if (KmeanCommon.converged(newCenters, oldCenters)) {
                return clusters;
            }

            clusters = newCenters.stream().map(Cluster::build_with_center).toList();
            i += 1;
        }
    }

}
