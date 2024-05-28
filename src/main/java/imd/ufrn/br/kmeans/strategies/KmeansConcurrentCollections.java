package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanCommon;
import imd.ufrn.br.kmeans.KmeanStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KmeansConcurrentCollections implements KmeanStrategy {
    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        List<Cluster> clusters = initialCenters.stream().map(p -> new Cluster(p, Collections.synchronizedList(new ArrayList<>()))).toList();

        System.out.println("initial: " + initialCenters);

        while (true) {
            final var finalClusters = clusters;
            values.parallelStream().forEach(point -> {
                var index = KmeanCommon.getIndexClosestCluster(point, finalClusters);
                finalClusters.get(index).getPoints().add(point);
            });

            List<Point> newCenter = clusters.stream().map(Cluster::calculateCenterPoint).toList();
            List<Point> center = clusters.stream().map(Cluster::getCenter).toList();

            if (KmeanCommon.converged(newCenter, center)) {
                return clusters;
            }

            clusters = newCenter.stream().map(p -> new Cluster(p, Collections.synchronizedList(new ArrayList<>()))).toList();

            // clusters = Collections.synchronizedList(newCenter.stream().map(Cluster::build_with_center).toList());
        }
    }

}
