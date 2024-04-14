import java.util.List;

public interface Kmean {
    List<Cluster> execute(List<Point> values, int k, List<Point> initialCenters);
}
