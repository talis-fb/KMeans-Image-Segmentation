import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KmeansSerialBuilder implements Kmean {

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
            clusters.get(index).points.add(point);
        }
        return clusters;
    }


}
