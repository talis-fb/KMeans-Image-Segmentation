package imd.ufrn.br.kmeans.strategies;

import imd.ufrn.br.entities.Cluster;
import imd.ufrn.br.entities.Point;
import imd.ufrn.br.kmeans.KmeanStrategy;
import imd.ufrn.br.kmeans.KmeanCommon;

import java.util.List;

public class KmeansSerialStreams implements KmeanStrategy {

    @Override
    public List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters) {
        List<Cluster> clusters = initialCenters.stream().map(Cluster::build_with_center).toList();

        while (true) {
            clusters = assignPoints(values, clusters);

            var newCenters = clusters.stream().map(Cluster::calculateCenterPoint).toList();
            var oldCenters = clusters.stream().map(Cluster::getCenter).toList();

            if (KmeanCommon.converged(newCenters, oldCenters)) {
                return clusters;
            }

            clusters = newCenters.stream().map(Cluster::build_with_center).toList();
        }
    }

    public static List<Cluster> assignPoints(List<Point> points, List<Cluster> clusters) {
        for (var point : points) {
            int index = KmeanCommon.getIndexClosestCluster(point, clusters);
            clusters.get(index).getPoints().add(point);
        }
        return clusters;
    }


}
