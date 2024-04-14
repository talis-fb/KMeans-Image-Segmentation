import java.util.List;

public class KmeanCommon {
    static int getIndexClosestCluster(Point point, List<Cluster> clusters) {
        double minDistance = Double.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < clusters.size(); i++) {
            var cluster = clusters.get(i);
            var distance = point.euclideanDistance(cluster.getCenter());
            if (distance < minDistance) {
                minDistance = distance;
                index = i;
            }
        }
        return index;
    }
}
