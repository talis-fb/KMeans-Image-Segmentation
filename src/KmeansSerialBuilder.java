import java.util.List;

public class KmeansSerialBuilder {
    List<Point> values;
    int K;

    public void setValues(List<Point> values) {
        this.values = values;
    }

    public void setK(int k) {
        K = k;
    }

    public List<Cluster> execute() {
        List<Point> initialCenters = Utils.extractRandomElementFromList(this.values, this.K);

        var clusters = initialCenters.stream().map(Cluster::build_with_center).toList();

        while (true) {
            clusters = assignPoints(this.values, clusters);

            var newCenters = clusters.stream().map(Cluster::calculateCenterPoint).toList();
            var oldCenters = clusters.stream().map(Cluster::getCenter).toList();

            if (converged(newCenters, oldCenters)) {
                return clusters;
            }

            clusters = newCenters.stream().map(Cluster::build_with_center).toList();
        }
    }

    public static List<Cluster> assignPoints(List<Point> points, List<Cluster> clusters) {
        for (var point : points) {
            double minDistance = Double.MAX_VALUE;
            int index = 0;
            for (int i = 0; i < clusters.size(); i++) {
                var cluster = clusters.get(i);
                var distance = point.euclideanDistance(cluster.getCenter());
                if(distance < minDistance) {
                    minDistance = distance;
                    index = i;
                }
            }
            clusters.get(index).addPoints(point);
        }
        return clusters;
    }

    public boolean converged(List<Point> list1, List<Point> list2) {
        for (int i = 0; i < list1.size(); i++) {
            var p1 = list1.get(i);
            var p2 = list2.get(i);
            if (p1.isEquals(p2))
                return false;
        }
        return true;
    }

}
